
package com.gestiva.creditnote.service;

import com.gestiva.billing.invoice.entity.Invoice;
import com.gestiva.billing.invoice.entity.InvoiceLine;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.sales.order.entity.SalesOrder;
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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnabledIfEnvironmentVariable(
        named = "GESTIVA_CCN_COMMITTED_ROLLBACK_TEST",
        matches = "enabled")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.flyway.enabled=false",
                "spring.jpa.hibernate.ddl-auto=validate",
                "spring.sql.init.mode=never",
                "spring.task.scheduling.enabled=false",
                "spring.batch.job.enabled=false"
        })
@ActiveProfiles("test")
class CustomerCreditNoteCommittedDraftRollbackSandboxITTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;

    private static final String DATABASE =
            "gestiva_scn_collaudo";

    private static final String FAILURE =
            "CCN_COMMITTED_DRAFT_FINANCIAL_FAILURE";

    @Autowired EntityManager em;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager manager;
    @Autowired CustomerCreditNoteDraftService drafts;
    @Autowired CustomerCreditNoteRegistrationService registrations;

    @MockitoBean
    CompanyContext companyContext;

    @MockitoBean
    CustomerCreditNoteFinancialPositionService financialPositions;


    private record Fixture(
            Long customerId,
            Long orderId,
            Long invoiceId,
            Long noteId) {
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry)
            throws Exception {

        Path path = Path.of(
                System.getProperty("user.home"),
                ".gestiva/mysql-v9-restore/flyway-collaudo.env");

        Properties values = new Properties();

        try (var reader = Files.newBufferedReader(path)) {
            values.load(reader);
        }

        if (!"127.0.0.1".equals(
                values.getProperty("MYSQL_COLLAUDO_HOST"))
                || !"33308".equals(
                values.getProperty("MYSQL_COLLAUDO_PORT"))
                || !DATABASE.equals(
                values.getProperty("MYSQL_COLLAUDO_DATABASE"))
                || !"gestiva_scn_migrator".equals(
                values.getProperty("MYSQL_COLLAUDO_USER"))) {
            throw new IllegalStateException(
                    "Destinazione MySQL CCN inattesa.");
        }

        String password =
                values.getProperty("MYSQL_COLLAUDO_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Credenziali sandbox mancanti.");
        }

        registry.add("spring.datasource.url", () ->
                "jdbc:mysql://127.0.0.1:33308/"
                        + DATABASE
                        + "?sslMode=DISABLED"
                        + "&allowPublicKeyRetrieval=true");

        registry.add("spring.datasource.username",
                () -> "gestiva_scn_migrator");

        registry.add("spring.datasource.password",
                () -> password);
    }

    private long count(String sql, Object... args) {
        Long value = jdbc.queryForObject(
                sql, Long.class, args);
        assertNotNull(value);
        return value;
    }

    private long sequence(String type) {
        return count("""
                SELECT next_number
                FROM document_sequence
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND document_type = ?
                """, TENANT, COMPANY, type);
    }

    private void verifySandbox() throws Exception {
        assertEquals(DATABASE,
                jdbc.queryForObject(
                        "SELECT DATABASE()", String.class));

        assertEquals(33308,
                jdbc.queryForObject(
                        "SELECT @@port", Integer.class));

        assertEquals(
                "gestiva_scn_migrator@127.0.0.1",
                jdbc.queryForObject(
                        "SELECT CURRENT_USER()", String.class));

        Path expected = Path.of(
                System.getProperty("user.home"),
                ".gestiva/mysql-v9-restore/data").toRealPath();

        Path actual = Path.of(
                jdbc.queryForObject(
                        "SELECT @@datadir", String.class)
        ).toRealPath();

        assertEquals(expected, actual);
    }

    private Fixture createCommittedFixture() {

        TransactionTemplate tx =
                new TransactionTemplate(manager);

        Fixture fixture = tx.execute(status -> {

            LocalDate date =
                    LocalDate.of(2026, 11, 12);

            Customer customer = new Customer();
            customer.setTenantId(TENANT);
            customer.setName(
                    "Cliente CCN committed rollback");
            customer.setType("COMPANY");
            customer.setStatus("ACTIVE");

            em.persist(customer);
            em.flush();

            SalesOrder order = new SalesOrder();
            order.setTenantId(TENANT);
            order.setCompanyId(COMPANY);
            order.setCustomerId(customer.getId());
            order.setOrderNumber(
                    "SO-CCN-COMMITTED-2395");
            order.setOrderDate(date);
            order.setStatus("CONFIRMED");
            order.setCurrencyCode("EUR");
            order.setSubtotalAmount(bd("100.00"));
            order.setTaxAmount(bd("22.00"));
            order.setTotalAmount(bd("122.00"));

            em.persist(order);
            em.flush();

            Invoice invoice = new Invoice();
            invoice.setTenantId(TENANT);
            invoice.setCompanyId(COMPANY);
            invoice.setSalesOrderId(order.getId());
            invoice.setCustomerId(customer.getId());
            invoice.setInvoiceNumber(
                    "INV-CCN-COMMITTED-2395");
            invoice.setInvoiceDate(date);
            invoice.setStatus("ISSUED");
            invoice.setCurrencyCode("EUR");
            invoice.setSubtotalAmount(bd("100.00"));
            invoice.setTaxAmount(bd("22.00"));
            invoice.setTotalAmount(bd("122.00"));
            invoice.setCreatedAt(OffsetDateTime.now());
            invoice.setUpdatedAt(OffsetDateTime.now());

            em.persist(invoice);
            em.flush();

            InvoiceLine line = new InvoiceLine();
            line.setTenantId(TENANT);
            line.setInvoiceId(invoice.getId());
            line.setLineNo(1);
            line.setDescription(
                    "Servizio CCN committed rollback");
            line.setQuantity(bd("1.000"));
            line.setUnitOfMeasure("PZ");
            line.setUnitPrice(bd("100.00"));
            line.setDiscountPct(bd("0.00"));
            line.setTaxPct(bd("22.00"));
            line.setTaxAmount(bd("22.00"));
            line.setLineTotal(bd("122.00"));

            em.persist(line);
            em.flush();

            Long noteId = drafts.createFullDraft(
                    TENANT,
                    invoice.getId(),
                    "Test rollback DRAFT confermata");

            assertNotNull(noteId);

            em.flush();

            return new Fixture(
                    customer.getId(),
                    order.getId(),
                    invoice.getId(),
                    noteId);
        });

        assertNotNull(fixture);
        return fixture;
    }

    private void cleanup(
            Fixture f,
            long customerSequenceBefore) {

        TransactionTemplate tx =
                new TransactionTemplate(manager);

        tx.executeWithoutResult(status -> {

            // La registrazione fallita non deve
            // aver lasciato scritture o posizioni.
            assertEquals(0L, count("""
                    SELECT COUNT(*)
                    FROM journal_entry
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND reference_type =
                          'CUSTOMER_CREDIT_NOTE'
                      AND reference_id = ?
                    """, TENANT, COMPANY, f.noteId()));

            assertEquals(0L, count("""
                    SELECT COUNT(*)
                    FROM credit_note_financial_position
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND credit_note_id = ?
                    """, TENANT, COMPANY, f.noteId()));

            jdbc.update("""
                    DELETE FROM credit_note_line
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND credit_note_id = ?
                    """, TENANT, COMPANY, f.noteId());

            assertEquals(1, jdbc.update("""
                    DELETE FROM credit_note
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND id = ?
                      AND status = 'DRAFT'
                    """, TENANT, COMPANY, f.noteId()));

            jdbc.update("""
                    DELETE FROM invoice_line
                    WHERE tenant_id = ?
                      AND invoice_id = ?
                    """, TENANT, f.invoiceId());

            assertEquals(1, jdbc.update("""
                    DELETE FROM invoice
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND id = ?
                      AND status = 'ISSUED'
                    """, TENANT, COMPANY, f.invoiceId()));

            assertEquals(1, jdbc.update("""
                    DELETE FROM sales_order
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND id = ?
                    """, TENANT, COMPANY, f.orderId()));

            assertEquals(1, jdbc.update("""
                    DELETE FROM customer
                    WHERE tenant_id = ?
                      AND id = ?
                    """, TENANT, f.customerId()));

            // Ripristino controllato del numeratore
            // CCN usato dalla fixture.
            assertEquals(1, jdbc.update("""
                    UPDATE document_sequence
                    SET next_number = ?
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND document_type =
                          'CUSTOMER_CREDIT_NOTE'
                      AND next_number = ?
                    """,
                    customerSequenceBefore,
                    TENANT,
                    COMPANY,
                    customerSequenceBefore + 1));
        });
    }

    @Test
    void committedDraftSurvivesFailedRegistration()
            throws Exception {

        verifySandbox();

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        // La fixture usa un numeratore preesistente,
        // quindi è richiesto un ambiente isolato.
        assertEquals(0L, count(
                "SELECT COUNT(*) FROM invoice"));

        assertEquals(0L, count(
                "SELECT COUNT(*) FROM credit_note"));

        assertEquals(0L, count(
                "SELECT COUNT(*) FROM journal_entry"));

        long customerSequenceBefore =
                sequence("CUSTOMER_CREDIT_NOTE");

        long journalSequenceBefore =
                sequence("JOURNAL_ENTRY");

        Fixture fixture = null;

        try {
            fixture = createCommittedFixture();

            final Fixture f = fixture;

            assertEquals("DRAFT",
                    jdbc.queryForObject("""
                            SELECT status
                            FROM credit_note
                            WHERE tenant_id = ?
                              AND company_id = ?
                              AND id = ?
                            """,
                            String.class,
                            TENANT, COMPANY, f.noteId()));

            assertEquals(1L, count("""
                    SELECT COUNT(*)
                    FROM credit_note_line
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND credit_note_id = ?
                    """, TENANT, COMPANY, f.noteId()));

            assertEquals(
                    customerSequenceBefore + 1,
                    sequence("CUSTOMER_CREDIT_NOTE"));

            doAnswer(invocation -> {

                em.flush();

                assertEquals(1L, count("""
                        SELECT COUNT(*)
                        FROM credit_note
                        WHERE tenant_id = ?
                          AND company_id = ?
                          AND id = ?
                          AND status = 'REGISTERED'
                        """, TENANT, COMPANY, f.noteId()));

                assertEquals(1L, count("""
                        SELECT COUNT(*)
                        FROM journal_entry
                        WHERE tenant_id = ?
                          AND company_id = ?
                          AND reference_type =
                              'CUSTOMER_CREDIT_NOTE'
                          AND reference_id = ?
                          AND posted = b'1'
                        """, TENANT, COMPANY, f.noteId()));

                assertEquals(3L, count("""
                        SELECT COUNT(*)
                        FROM journal_entry_line l
                        JOIN journal_entry j
                          ON j.id = l.journal_entry_id
                        WHERE j.tenant_id = ?
                          AND j.company_id = ?
                          AND j.reference_type =
                              'CUSTOMER_CREDIT_NOTE'
                          AND j.reference_id = ?
                        """, TENANT, COMPANY, f.noteId()));

                System.out.println(
                        "CCN_COMMITTED_AFTER_POSTING: OK");

                throw new IllegalStateException(FAILURE);

            }).when(financialPositions)
                    .createForRegisteredCustomerCreditNote(
                            TENANT, f.noteId());

            IllegalStateException exception =
                    assertThrows(
                            IllegalStateException.class,
                            () -> registrations.register(
                                    TENANT, f.noteId()));

            assertEquals(FAILURE, exception.getMessage());

            // Controlli effettuati DOPO il rollback
            // della transazione applicativa.
            assertEquals("ISSUED",
                    jdbc.queryForObject("""
                            SELECT status FROM invoice
                            WHERE tenant_id = ?
                              AND company_id = ?
                              AND id = ?
                            """,
                            String.class,
                            TENANT, COMPANY, f.invoiceId()));

            assertEquals("DRAFT",
                    jdbc.queryForObject("""
                            SELECT status FROM credit_note
                            WHERE tenant_id = ?
                              AND company_id = ?
                              AND id = ?
                            """,
                            String.class,
                            TENANT, COMPANY, f.noteId()));

            assertEquals(1L, count("""
                    SELECT COUNT(*)
                    FROM credit_note_line
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND credit_note_id = ?
                    """, TENANT, COMPANY, f.noteId()));

            assertEquals(0L, count("""
                    SELECT COUNT(*)
                    FROM journal_entry
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND reference_type =
                          'CUSTOMER_CREDIT_NOTE'
                      AND reference_id = ?
                    """, TENANT, COMPANY, f.noteId()));

            assertEquals(0L, count("""
                    SELECT COUNT(*)
                    FROM credit_note_financial_position
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND credit_note_id = ?
                    """, TENANT, COMPANY, f.noteId()));

            assertEquals(
                    journalSequenceBefore,
                    sequence("JOURNAL_ENTRY"));

            System.out.println(
                    "CCN_COMMITTED_DRAFT_ROLLBACK: OK");

        } finally {
            if (fixture != null) {
                cleanup(fixture, customerSequenceBefore);
            }
        }

        verifySandbox();

        assertEquals(0L, count(
                "SELECT COUNT(*) FROM invoice"));

        assertEquals(0L, count(
                "SELECT COUNT(*) FROM credit_note"));

        assertEquals(0L, count(
                "SELECT COUNT(*) FROM journal_entry"));

        assertEquals(
                customerSequenceBefore,
                sequence("CUSTOMER_CREDIT_NOTE"));

        assertEquals(
                journalSequenceBefore,
                sequence("JOURNAL_ENTRY"));

        System.out.println(
                "CCN_COMMITTED_FIXTURE_CLEANUP: OK");
    }
}
