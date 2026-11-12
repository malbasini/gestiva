
package com.gestiva.creditnote.service;

import com.gestiva.billing.invoice.entity.Invoice;
import com.gestiva.billing.invoice.entity.InvoiceLine;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.sales.order.entity.SalesOrder;
import com.gestiva.security.company.context.CompanyContext;
import org.springframework.test.annotation.Rollback;
import jakarta.persistence.EntityManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnabledIfEnvironmentVariable(
        named = "GESTIVA_CCN_SANDBOX_TEST",
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
class CustomerCreditNoteSandboxITTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;

    private static final String DATABASE =
            "gestiva_scn_collaudo";

    private static final String EXPECTED_DATADIR =
            Path.of(System.getProperty("user.home"),
                            ".gestiva/mysql-v9-restore/data")
                    .toAbsolutePath()
                    .normalize()
                    .toString();

    private static final List<String> TABLES = List.of(
            "customer",
            "sales_order",
            "invoice",
            "invoice_line",
            "credit_note",
            "credit_note_line",
            "credit_note_financial_position",
            "journal_entry",
            "journal_entry_line"
    );

    @Autowired
    EntityManager em;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    CustomerCreditNoteDraftService drafts;

    @Autowired
    CustomerCreditNoteRegistrationService registrations;


    @Autowired
    PlatformTransactionManager transactionManager;

    @MockitoSpyBean
    CustomerCreditNoteFinancialPositionService financialPositions;

    private static final String FAILURE_MARKER =
            "CCN_2395_INTENTIONAL_FINANCIAL_FAILURE";


    @MockitoBean
    CompanyContext companyContext;

    private Map<String, Long> baseline;
    private Long creditNoteSequenceBefore;
    private Long journalSequenceBefore;

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
                    "Configurazione MySQL CCN inattesa.");
        }

        String password =
                values.getProperty("MYSQL_COLLAUDO_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Credenziali MySQL CCN mancanti.");
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

        registry.add("spring.flyway.enabled", () -> false);
        registry.add("spring.jpa.hibernate.ddl-auto",
                () -> "validate");
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private long count(String table) {
        Long result = jdbc.queryForObject(
                "SELECT COUNT(*) FROM `" + table + "`",
                Long.class);

        assertNotNull(result);
        return result;
    }

    private Long sequence(String type) {
        return jdbc.queryForObject("""
                SELECT next_number
                FROM document_sequence
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND document_type = ?
                """,
                Long.class, TENANT, COMPANY, type);
    }

    private void verifyDestination() throws Exception {
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

        String actualDatadir = jdbc.queryForObject(
                "SELECT @@datadir", String.class);

        assertNotNull(actualDatadir);

        assertEquals(
                Path.of(EXPECTED_DATADIR).toRealPath(),
                Path.of(actualDatadir).toRealPath());
    }

    @BeforeTransaction
    void beforeTransaction() throws Exception {
        verifyDestination();

        baseline = new HashMap<>();

        for (String table : TABLES) {
            baseline.put(table, count(table));
        }

        creditNoteSequenceBefore =
                sequence("CUSTOMER_CREDIT_NOTE");

        journalSequenceBefore =
                sequence("JOURNAL_ENTRY");

        assertNotNull(creditNoteSequenceBefore);
        assertNotNull(journalSequenceBefore);

        assertEquals(0L, baseline.get("invoice"));
        assertEquals(0L, baseline.get("credit_note"));
        assertEquals(0L, baseline.get("journal_entry"));

        for (String code : List.of("1210", "4100", "2210")) {
            Long usable = jdbc.queryForObject("""
                    SELECT COUNT(*)
                    FROM account
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND code = ?
                      AND active = b'1'
                      AND leaf_account = b'1'
                    """,
                    Long.class, TENANT, COMPANY, code);

            assertEquals(1L, usable,
                    "Conto non utilizzabile: " + code);
        }

        System.out.println(
                "CCN_SANDBOX_PREFLIGHT: OK");
    }

    @Test
    @Transactional
    @Rollback
    void registersCustomerCreditNoteAndRollsBack() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        LocalDate date = LocalDate.of(2026, 11, 12);
        OffsetDateTime now = OffsetDateTime.now();

        Customer customer = new Customer();
        customer.setTenantId(TENANT);
        customer.setName("Cliente temporaneo CCN sandbox");
        customer.setType("COMPANY");
        customer.setStatus("ACTIVE");

        em.persist(customer);
        em.flush();

        SalesOrder order = new SalesOrder();
        order.setTenantId(TENANT);
        order.setCompanyId(COMPANY);
        order.setCustomerId(customer.getId());
        order.setOrderNumber("SO-CCN-SANDBOX-2395");
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
        invoice.setInvoiceNumber("INV-CCN-SANDBOX-2395");
        invoice.setInvoiceDate(date);
        invoice.setStatus("ISSUED");
        invoice.setCurrencyCode("EUR");
        invoice.setSubtotalAmount(bd("100.00"));
        invoice.setTaxAmount(bd("22.00"));
        invoice.setTotalAmount(bd("122.00"));
        invoice.setCreatedAt(now);
        invoice.setUpdatedAt(now);

        em.persist(invoice);
        em.flush();

        InvoiceLine line = new InvoiceLine();
        line.setTenantId(TENANT);
        line.setInvoiceId(invoice.getId());
        line.setLineNo(1);
        line.setDescription("Servizio temporaneo CCN");
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
                "Collaudo transazionale CCN");

        assertNotNull(noteId);

        var result = registrations.register(
                TENANT, noteId);

        assertEquals(noteId, result.creditNoteId());
        assertNotNull(result.journalEntryId());
        assertNotNull(result.financialPositionId());

        em.flush();
        em.clear();

        String noteStatus = jdbc.queryForObject("""
                SELECT status
                FROM credit_note
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND id = ?
                """, String.class, TENANT, COMPANY, noteId);

        assertEquals("REGISTERED", noteStatus);

        Integer posted = jdbc.queryForObject("""
                SELECT posted + 0
                FROM journal_entry
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND id = ?
                  AND reference_type = 'CUSTOMER_CREDIT_NOTE'
                  AND reference_id = ?
                """,
                Integer.class,
                TENANT, COMPANY,
                result.journalEntryId(), noteId);

        assertEquals(1, posted);

        Integer correctLines = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM journal_entry_line l
                JOIN account a
                  ON a.id = l.account_id
                WHERE l.journal_entry_id = ?
                  AND l.tenant_id = ?
                  AND l.company_id = ?
                  AND (
                     (a.code = '4100'
                      AND l.debit_amount = 100.00
                      AND l.credit_amount = 0.00)
                     OR
                     (a.code = '2210'
                      AND l.debit_amount = 22.00
                      AND l.credit_amount = 0.00)
                     OR
                     (a.code = '1210'
                      AND l.debit_amount = 0.00
                      AND l.credit_amount = 122.00)
                  )
                """,
                Integer.class,
                result.journalEntryId(),
                TENANT, COMPANY);

        assertEquals(3, correctLines);

        String positionStatus = jdbc.queryForObject("""
                SELECT status
                FROM credit_note_financial_position
                WHERE id = ?
                  AND tenant_id = ?
                  AND company_id = ?
                  AND credit_note_id = ?
                  AND position_type = 'CUSTOMER'
                  AND original_amount = 122.00
                  AND available_amount = 122.00
                  AND compensated_amount = 0.00
                  AND refunded_amount = 0.00
                """,
                String.class,
                result.financialPositionId(),
                TENANT, COMPANY, noteId);

        assertEquals("OPEN", positionStatus);

        /*
         * PASSO 5.13.2395AU
         * Protezione contro la doppia registrazione CCN.
         *
         * La prima registrazione è già stata eseguita.
         * Il secondo tentativo deve essere rifiutato.
         */

        assertThrows(
                com.gestiva.common.exception.BusinessException.class,
                () -> registrations.register(TENANT, noteId)
        );

        em.flush();
        em.clear();

        // Deve esistere una sola scrittura contabile.
        Long journalCount = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM journal_entry
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND reference_type = 'CUSTOMER_CREDIT_NOTE'
                  AND reference_id = ?
                """,
                Long.class,
                TENANT, COMPANY, noteId);

        assertEquals(1L, journalCount);

        // Deve esistere una sola posizione finanziaria.
        Long financialCount = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM credit_note_financial_position
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND credit_note_id = ?
                """,
                Long.class,
                TENANT, COMPANY, noteId);

        assertEquals(1L, financialCount);

        // La nota deve rimanere REGISTERED.
        String finalStatus = jdbc.queryForObject(
                """
                SELECT status
                FROM credit_note
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND id = ?
                """,
                String.class,
                TENANT, COMPANY, noteId);

        assertEquals("REGISTERED", finalStatus);
        System.out.println(
                "CCN_DUPLICATE_REGISTRATION_REJECTED: OK");
        System.out.println(
                "CCN_SANDBOX_REGISTRATION: OK");
    }


    @Test
    void financialFailureRollsBackCustomerRegistration()
            throws Exception {

        verifyDestination();

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        Map<String, Long> before = new HashMap<>();

        for (String table : TABLES) {
            before.put(table, count(table));
        }

        Long noteSequenceBefore =
                sequence("CUSTOMER_CREDIT_NOTE");

        Long journalSequenceBefore =
                sequence("JOURNAL_ENTRY");

        assertNotNull(noteSequenceBefore);
        assertNotNull(journalSequenceBefore);

        assertEquals(0L, before.get("invoice"));
        assertEquals(0L, before.get("credit_note"));
        assertEquals(0L, before.get("journal_entry"));

        TransactionTemplate tx =
                new TransactionTemplate(transactionManager);

        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> tx.executeWithoutResult(status -> {

                    LocalDate date =
                            LocalDate.of(2026, 11, 12);

                    OffsetDateTime now =
                            OffsetDateTime.now();

                    Customer customer = new Customer();
                    customer.setTenantId(TENANT);
                    customer.setName(
                            "Cliente rollback CCN sandbox");
                    customer.setType("COMPANY");
                    customer.setStatus("ACTIVE");

                    em.persist(customer);
                    em.flush();

                    SalesOrder order = new SalesOrder();
                    order.setTenantId(TENANT);
                    order.setCompanyId(COMPANY);
                    order.setCustomerId(customer.getId());
                    order.setOrderNumber(
                            "SO-CCN-ROLLBACK-2395");
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
                            "INV-CCN-ROLLBACK-2395");
                    invoice.setInvoiceDate(date);
                    invoice.setStatus("ISSUED");
                    invoice.setCurrencyCode("EUR");
                    invoice.setSubtotalAmount(bd("100.00"));
                    invoice.setTaxAmount(bd("22.00"));
                    invoice.setTotalAmount(bd("122.00"));
                    invoice.setCreatedAt(now);
                    invoice.setUpdatedAt(now);

                    em.persist(invoice);
                    em.flush();

                    InvoiceLine line = new InvoiceLine();
                    line.setTenantId(TENANT);
                    line.setInvoiceId(invoice.getId());
                    line.setLineNo(1);
                    line.setDescription(
                            "Servizio rollback CCN");
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
                            "Test rollback forzato CCN");

                    assertNotNull(noteId);

                    /*
                     * Il servizio finanziario genera
                     * intenzionalmente un errore.
                     *
                     * Prima dell'errore controlliamo che
                     * la scrittura contabile sia stata
                     * realmente inserita nella transazione.
                     */
                    doAnswer(invocation -> {

                        em.flush();

                        Long registered = jdbc.queryForObject(
                                """
                                SELECT COUNT(*)
                                FROM credit_note
                                WHERE tenant_id = ?
                                  AND company_id = ?
                                  AND id = ?
                                  AND status = 'REGISTERED'
                                """,
                                Long.class,
                                TENANT, COMPANY, noteId);

                        assertEquals(1L, registered);

                        Long posted = jdbc.queryForObject(
                                """
                                SELECT COUNT(*)
                                FROM journal_entry
                                WHERE tenant_id = ?
                                  AND company_id = ?
                                  AND reference_type =
                                      'CUSTOMER_CREDIT_NOTE'
                                  AND reference_id = ?
                                  AND posted = b'1'
                                """,
                                Long.class,
                                TENANT, COMPANY, noteId);

                        assertEquals(1L, posted);

                        Long journalLines =
                                jdbc.queryForObject(
                                        """
                                        SELECT COUNT(*)
                                        FROM journal_entry_line l
                                        JOIN journal_entry j
                                          ON j.id = l.journal_entry_id
                                        WHERE j.tenant_id = ?
                                          AND j.company_id = ?
                                          AND j.reference_type =
                                              'CUSTOMER_CREDIT_NOTE'
                                          AND j.reference_id = ?
                                        """,
                                        Long.class,
                                        TENANT, COMPANY, noteId);

                        assertEquals(3L, journalLines);

                        System.out.println(
                                "CCN_FAILURE_AFTER_POSTING: OK");

                        throw new IllegalStateException(
                                FAILURE_MARKER);

                    }).when(financialPositions)
                            .createForRegisteredCustomerCreditNote(
                                    TENANT, noteId);

                    /*
                     * L'eccezione deve uscire da register()
                     * e quindi da TransactionTemplate.
                     */
                    registrations.register(TENANT, noteId);
                })
        );

        assertEquals(
                FAILURE_MARKER,
                failure.getMessage());

        /*
         * Siamo fuori dalla transazione fallita.
         * Le query seguenti verificano i dati
         * effettivamente visibili dopo il rollback.
         */
        verifyDestination();

        for (String table : TABLES) {
            assertEquals(
                    before.get(table),
                    count(table),
                    "Residui dopo rollback: " + table);
        }

        assertEquals(
                noteSequenceBefore,
                sequence("CUSTOMER_CREDIT_NOTE"),
                "Numeratore CCN modificato.");

        assertEquals(
                journalSequenceBefore,
                sequence("JOURNAL_ENTRY"),
                "Numeratore contabile modificato.");

        System.out.println(
                "CCN_FAILURE_ATOMIC_ROLLBACK: OK");
    }

    @AfterTransaction
    void afterTransaction() throws Exception {
        verifyDestination();

        for (String table : TABLES) {
            assertEquals(
                    baseline.get(table),
                    count(table),
                    "Tabella non ripristinata: " + table);
        }

        assertEquals(
                creditNoteSequenceBefore,
                sequence("CUSTOMER_CREDIT_NOTE"),
                "Numeratore CCN non ripristinato.");

        assertEquals(
                journalSequenceBefore,
                sequence("JOURNAL_ENTRY"),
                "Numeratore contabile non ripristinato.");

        System.out.println(
                "CCN_SANDBOX_ROLLBACK: OK");
    }
}

