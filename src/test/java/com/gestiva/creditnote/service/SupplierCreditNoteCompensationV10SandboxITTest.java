

package com.gestiva.creditnote.service;

import com.gestiva.accounting.due.entity.PaymentDue;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteFinancialPosition;
import com.gestiva.creditnote.entity.SupplierCreditNoteCompensation;
import com.gestiva.purchasing.supplier.entity.Supplier;
import com.gestiva.security.company.context.CompanyContext;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnabledIfEnvironmentVariable(
    named = "GESTIVA_V10_SANDBOX_TEST",
    matches = "enabled"
)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.sql.init.mode=never",
        "spring.task.scheduling.enabled=false",
        "spring.batch.job.enabled=false"
    }
)
@ActiveProfiles("test")
class SupplierCreditNoteCompensationV10SandboxITTest {

    private static final String DATABASE =
        "gestiva_scn_collaudo";

    private static final String URL =
        "jdbc:mysql://127.0.0.1:33308/"
        + DATABASE
        + "?sslMode=DISABLED&allowPublicKeyRetrieval=true";

    private static final Long TENANT_ID = 2L;
    private static final Long COMPANY_ID = 1L;

    @Autowired EntityManager em;
    @Autowired JdbcTemplate jdbc;
    @Autowired SupplierCreditNoteCompensationService service;

    @MockitoBean CompanyContext companyContext;

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "Variabile richiesta assente: " + name);
        }
        return value;
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry)
            throws Exception {

        String host = requiredEnv("MYSQL_COLLAUDO_HOST");
        String port = requiredEnv("MYSQL_COLLAUDO_PORT");
        String database = requiredEnv("MYSQL_COLLAUDO_DATABASE");
        String user = requiredEnv("MYSQL_COLLAUDO_USER");
        String password = requiredEnv("MYSQL_COLLAUDO_PASSWORD");

        if (!"127.0.0.1".equals(host)
                || !"33308".equals(port)
                || !DATABASE.equals(database)
                || !"gestiva_scn_migrator".equals(user)) {
            throw new IllegalStateException(
                "STOP: configurazione sandbox non autorizzata");
        }

        // Verifica fisica PRIMA del datasource Spring.
        try (var connection =
                DriverManager.getConnection(URL, user, password)) {

            connection.setReadOnly(true);

            try (var statement = connection.createStatement();
                 var rs = statement.executeQuery(
                     "SELECT VERSION(), @@port, DATABASE(), "
                     + "CURRENT_USER(), @@datadir, @@socket")) {

                if (!rs.next()) {
                    throw new IllegalStateException(
                        "Identita MySQL non disponibile");
                }

                Path expectedDatadir = Path.of(
                    System.getProperty("user.home"),
                    ".gestiva/mysql-v9-restore/data"
                ).toRealPath();

                Path actualDatadir =
                    Path.of(rs.getString(5)).toRealPath();

                Path expectedSocket = Path.of(
                    System.getProperty("user.home"),
                    ".gestiva/mysql-v9-restore/mysql.sock"
                ).toAbsolutePath().normalize();

                Path actualSocket =
                    Path.of(rs.getString(6))
                        .toAbsolutePath().normalize();

                if (!"8.0.45".equals(rs.getString(1))
                        || rs.getInt(2) != 33308
                        || !DATABASE.equals(rs.getString(3))
                        || !"gestiva_scn_migrator@127.0.0.1"
                            .equals(rs.getString(4))
                        || !expectedDatadir.equals(actualDatadir)
                        || !expectedSocket.equals(actualSocket)) {
                    throw new IllegalStateException(
                        "STOP: identita fisica MySQL non conforme");
                }
            }
        }

        registry.add("spring.datasource.url", () -> URL);
        registry.add("spring.datasource.username", () -> user);
        registry.add("spring.datasource.password", () -> password);
    }

    private BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    private long count(String table) {
        Long result = jdbc.queryForObject(
            "SELECT COUNT(*) FROM " + table, Long.class);
        assertNotNull(result);
        return result;
    }

    @Test
    @Transactional
    @Rollback
    void partialCompensationPersistsAndRollsBack() {

        // Verifica ulteriore prima della prima scrittura.
        assertEquals(DATABASE,
            jdbc.queryForObject("SELECT DATABASE()", String.class));
        assertEquals(33308,
            jdbc.queryForObject("SELECT @@port", Integer.class));

        for (String table : new String[] {
                "supplier",
                "credit_note",
                "credit_note_financial_position",
                "payment_due",
                "supplier_credit_note_compensation"
        }) {
            assertEquals(0L, count(table),
                "Sandbox non vuoto: " + table);
        }

        when(companyContext.requireCurrentCompanyId())
            .thenReturn(COMPANY_ID);

        LocalDate date = LocalDate.of(2026, 11, 10);
        String suffix = UUID.randomUUID().toString()
            .substring(0, 8);

        Supplier supplier = new Supplier();
        supplier.setTenantId(TENANT_ID);
        supplier.setCode("V10-" + suffix);
        supplier.setName("Fornitore collaudo V10");
        supplier.setActive(true);
        em.persist(supplier);
        em.flush();

        CreditNote note = new CreditNote();
        note.setTenantId(TENANT_ID);
        note.setCompanyId(COMPANY_ID);
        note.setCreditNoteType("SUPPLIER");
        note.setCreditNoteNumber("SCN-V10-" + suffix);
        note.setCreditNoteDate(date);
        note.setOriginalInvoiceId(900001L);
        note.setPartyId(supplier.getId());
        note.setStatus("REGISTERED");
        note.setCurrencyCode("EUR");
        note.setReason("Collaudo compensazione V10");
        note.setSubtotalAmount(money("100.00"));
        note.setTaxAmount(money("22.00"));
        note.setTotalAmount(money("122.00"));
        em.persist(note);
        em.flush();

        CreditNoteFinancialPosition position =
            new CreditNoteFinancialPosition();

        position.setTenantId(TENANT_ID);
        position.setCompanyId(COMPANY_ID);
        position.setCreditNoteId(note.getId());
        position.setPositionType("SUPPLIER");
        position.setCurrencyCode("EUR");
        position.setOriginalAmount(money("122.00"));
        position.setCompensatedAmount(money("0.00"));
        position.setRefundedAmount(money("0.00"));
        position.setAvailableAmount(money("122.00"));
        position.setStatus("OPEN");
        em.persist(position);
        em.flush();

        PaymentDue due = new PaymentDue();
        due.setTenantId(TENANT_ID);
        due.setCompanyId(COMPANY_ID);
        due.setDirection("PAYABLE");
        due.setPartyType("SUPPLIER");
        due.setPartyId(supplier.getId());
        due.setDocumentNumber("SI-V10-" + suffix);
        due.setDocumentDate(date);
        due.setDueDate(date.plusDays(30));
        due.setReferenceType("SUPPLIER_INVOICE");
        due.setReferenceId(900001L);
        due.setCurrencyCode("EUR");
        due.setGrossAmount(money("200.00"));
        due.setPaidAmount(money("0.00"));
        due.setCompensatedAmount(money("0.00"));
        due.setOpenAmount(money("200.00"));
        due.setStatus("OPEN");
        em.persist(due);
        em.flush();

        // Prima compensazione parziale: 50 euro.
        Long compensationId = service.compensate(
            TENANT_ID,
            position.getId(),
            due.getId(),
            money("50.00"),
            date,
            "V10-OP-" + suffix,
            "Test compensazione parziale"
        );

        assertNotNull(compensationId);

        em.flush();
        em.clear();

        CreditNoteFinancialPosition updatedPosition =
            em.find(CreditNoteFinancialPosition.class,
                position.getId());

        PaymentDue updatedDue =
            em.find(PaymentDue.class, due.getId());

        SupplierCreditNoteCompensation saved =
            em.find(SupplierCreditNoteCompensation.class,
                compensationId);

        assertNotNull(updatedPosition);
        assertNotNull(updatedDue);
        assertNotNull(saved);

        assertEquals(0, money("50.00").compareTo(
            updatedPosition.getCompensatedAmount()));

        assertEquals(0, money("72.00").compareTo(
            updatedPosition.getAvailableAmount()));

        assertEquals("PARTIALLY_SETTLED",
            updatedPosition.getStatus());

        assertEquals(0, money("50.00").compareTo(
            updatedDue.getCompensatedAmount()));

        assertEquals(0, money("150.00").compareTo(
            updatedDue.getOpenAmount()));

        assertEquals("PARTIALLY_PAID",
            updatedDue.getStatus());

        assertEquals(0, money("50.00").compareTo(
            saved.getAmount()));

        assertEquals(0, updatedDue.getGrossAmount()
            .compareTo(
                updatedDue.getPaidAmount()
                    .add(updatedDue.getCompensatedAmount())
                    .add(updatedDue.getOpenAmount())
            ));

        assertEquals(0, updatedPosition.getOriginalAmount()
            .compareTo(
                updatedPosition.getCompensatedAmount()
                    .add(updatedPosition.getRefundedAmount())
                    .add(updatedPosition.getAvailableAmount())
            ));

        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());

        // @Rollback annulla automaticamente tutte le fixture
        // e la compensazione al termine del test.
    }
}
