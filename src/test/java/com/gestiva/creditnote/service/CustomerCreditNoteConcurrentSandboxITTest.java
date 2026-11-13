
package com.gestiva.creditnote.service;

import com.gestiva.billing.invoice.entity.Invoice;
import com.gestiva.billing.invoice.entity.InvoiceLine;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.sales.order.entity.SalesOrder;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnabledIfEnvironmentVariable(
        named = "GESTIVA_CCN_CONCURRENT_TEST",
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
class CustomerCreditNoteConcurrentSandboxITTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;

    private static final String DATABASE =
            "gestiva_scn_collaudo";

    @Autowired EntityManager em;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager manager;

    @Autowired CustomerCreditNoteDraftService drafts;
    @Autowired CustomerCreditNoteRegistrationService registrations;

    @MockitoBean CompanyContext companyContext;

    record Fixture(
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

        Path file = Path.of(
                System.getProperty("user.home"),
                ".gestiva/mysql-v9-restore/flyway-collaudo.env");

        Properties config = new Properties();

        try (var reader = Files.newBufferedReader(file)) {
            config.load(reader);
        }

        if (!"127.0.0.1".equals(
                config.getProperty("MYSQL_COLLAUDO_HOST"))
                || !"33308".equals(
                config.getProperty("MYSQL_COLLAUDO_PORT"))
                || !DATABASE.equals(
                config.getProperty("MYSQL_COLLAUDO_DATABASE"))
                || !"gestiva_scn_migrator".equals(
                config.getProperty("MYSQL_COLLAUDO_USER"))) {

            throw new IllegalStateException(
                    "Destinazione MySQL CCN inattesa.");
        }

        String password =
                config.getProperty("MYSQL_COLLAUDO_PASSWORD");

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
                        ".gestiva/mysql-v9-restore/data")
                .toRealPath();

        Path actual = Path.of(
                        jdbc.queryForObject(
                                "SELECT @@datadir", String.class))
                .toRealPath();

        assertEquals(expected, actual);
    }

    private void assertOperationalTablesEmpty() {
        for (String table : List.of(
                "customer",
                "sales_order",
                "invoice",
                "invoice_line",
                "credit_note",
                "credit_note_line",
                "credit_note_financial_position",
                "journal_entry",
                "journal_entry_line")) {

            assertEquals(
                    0L,
                    count("SELECT COUNT(*) FROM `" + table + "`"),
                    "Tabella non vuota: " + table);
        }
    }

    private Fixture createFixture() {
        TransactionTemplate tx = new TransactionTemplate(manager);

        Fixture fixture = tx.execute(status -> {
            LocalDate date = LocalDate.of(2026, 11, 13);

            Customer customer = new Customer();
            customer.setTenantId(TENANT);
            customer.setName("Cliente concorrenza CCN");
            customer.setType("COMPANY");
            customer.setStatus("ACTIVE");

            em.persist(customer);
            em.flush();

            SalesOrder order = new SalesOrder();
            order.setTenantId(TENANT);
            order.setCompanyId(COMPANY);
            order.setCustomerId(customer.getId());
            order.setOrderNumber("SO-CCN-CONCURRENT-2395");
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
            invoice.setInvoiceNumber("INV-CCN-CONCURRENT-2395");
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
            line.setDescription("Servizio concorrenza CCN");
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
                    "Test concorrenza registrazione cliente");

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
            long customerSequenceBefore,
            long journalSequenceBefore) {

        TransactionTemplate tx = new TransactionTemplate(manager);

        tx.executeWithoutResult(status -> {

            jdbc.update("""
                    DELETE FROM journal_entry_line
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND journal_entry_id IN (
                          SELECT id
                          FROM journal_entry
                          WHERE tenant_id = ?
                            AND company_id = ?
                            AND reference_type =
                                'CUSTOMER_CREDIT_NOTE'
                            AND reference_id = ?
                      )
                    """,
                    TENANT, COMPANY,
                    TENANT, COMPANY, f.noteId());

            jdbc.update("""
                    DELETE FROM journal_entry
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND reference_type =
                          'CUSTOMER_CREDIT_NOTE'
                      AND reference_id = ?
                    """, TENANT, COMPANY, f.noteId());

            jdbc.update("""
                    DELETE FROM credit_note_financial_position
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND credit_note_id = ?
                    """, TENANT, COMPANY, f.noteId());

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

            // Ripristina i numeratori soltanto se
            // corrispondono esattamente ai valori attesi.
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
                    TENANT, COMPANY,
                    customerSequenceBefore + 1));

            assertEquals(1, jdbc.update("""
                    UPDATE document_sequence
                    SET next_number = ?
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND document_type = 'JOURNAL_ENTRY'
                      AND next_number = ?
                    """,
                    journalSequenceBefore,
                    TENANT, COMPANY,
                    journalSequenceBefore + 1));
        });
    }

    @Test
    @Timeout(75)
    void concurrentRegistrationOfSameNoteMustBeSerialized()
            throws Exception {

        verifySandbox();
        assertOperationalTablesEmpty();

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        long customerSequenceBefore =
                sequence("CUSTOMER_CREDIT_NOTE");

        long journalSequenceBefore =
                sequence("JOURNAL_ENTRY");

        assertEquals(1L, customerSequenceBefore);
        assertEquals(1L, journalSequenceBefore);

        Fixture fixture = null;
        ExecutorService executor = null;

        CountDownLatch aRegistered = new CountDownLatch(1);
        CountDownLatch releaseA = new CountDownLatch(1);
        CountDownLatch bReady = new CountDownLatch(1);

        boolean workersStopped = true;

        try {
            fixture = createFixture();
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

            executor = Executors.newFixedThreadPool(2);

            Future<Long> first = executor.submit(() -> {
                TransactionTemplate tx =
                        new TransactionTemplate(manager);

                tx.setIsolationLevel(
                        TransactionDefinition.ISOLATION_READ_COMMITTED);

                return tx.execute(status -> {
                    Long connectionId = jdbc.queryForObject(
                            "SELECT CONNECTION_ID()", Long.class);

                    registrations.register(TENANT, f.noteId());
                    aRegistered.countDown();

                    try {
                        if (!releaseA.await(
                                15, TimeUnit.SECONDS)) {
                            throw new IllegalStateException(
                                    "Timeout rilascio lock CCN A");
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(ex);
                    }

                    return connectionId;
                });
            });

            assertTrue(
                    aRegistered.await(15, TimeUnit.SECONDS),
                    "A non ha completato la registrazione.");

            System.out.println(
                    "CCN_CONCURRENT_A_HOLDS_LOCK: OK");

            AtomicReference<Long> bConnection =
                    new AtomicReference<>();

            Future<BusinessException> second =
                    executor.submit(() -> {

                        TransactionTemplate tx =
                                new TransactionTemplate(manager);

                        tx.setIsolationLevel(
                                TransactionDefinition.ISOLATION_READ_COMMITTED);

                        return assertThrows(
                                BusinessException.class,
                                () -> tx.execute(status -> {

                                    bConnection.set(
                                            jdbc.queryForObject(
                                                    "SELECT CONNECTION_ID()",
                                                    Long.class));

                                    bReady.countDown();

                                    return registrations.register(
                                            TENANT, f.noteId());
                                }));
                    });

            assertTrue(
                    bReady.await(15, TimeUnit.SECONDS),
                    "B non è entrata nella transazione.");

            assertNotNull(bConnection.get());

            // B non deve terminare mentre A
            // mantiene il lock sulla fattura.
            assertFalse(
                    second.isDone(),
                    "B ha concluso prima del commit di A.");

            try {
                second.get(500, TimeUnit.MILLISECONDS);

                fail("B ha concluso mentre A deteneva il lock.");

            } catch (TimeoutException expected) {
                System.out.println(
                        "CCN_CONCURRENT_B_WAITING: OK");
            }

            releaseA.countDown();

            Long aConnection =
                    first.get(15, TimeUnit.SECONDS);

            BusinessException rejected =
                    second.get(15, TimeUnit.SECONDS);

            assertNotNull(aConnection);
            assertNotNull(rejected.getMessage());

            assertNotEquals(
                    aConnection,
                    bConnection.get(),
                    "A e B hanno utilizzato la stessa connessione.");

            System.out.println(
                    "CCN_CONCURRENT_DISTINCT_CONNECTIONS: OK");

            assertEquals("REGISTERED",
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

            assertEquals(1L, count("""
                    SELECT COUNT(*)
                    FROM credit_note_financial_position
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND credit_note_id = ?
                      AND position_type = 'CUSTOMER'
                      AND status = 'OPEN'
                      AND original_amount = 122.00
                      AND available_amount = 122.00
                    """, TENANT, COMPANY, f.noteId()));

            assertEquals(
                    customerSequenceBefore + 1,
                    sequence("CUSTOMER_CREDIT_NOTE"));

            assertEquals(
                    journalSequenceBefore + 1,
                    sequence("JOURNAL_ENTRY"));

            System.out.println(
                    "CCN_CONCURRENT_SINGLE_POSTING: OK");

        } finally {
            releaseA.countDown();

            if (executor != null) {
                executor.shutdownNow();

                workersStopped = executor.awaitTermination(
                        15, TimeUnit.SECONDS);
            }

            if (!workersStopped) {
                throw new IllegalStateException(
                        "Thread ancora attivi: cleanup sospeso. "
                                + "Non ripetere il test.");
            }

            if (fixture != null) {
                verifySandbox();

                cleanup(
                        fixture,
                        customerSequenceBefore,
                        journalSequenceBefore);
            }

            verifySandbox();
            assertOperationalTablesEmpty();

            assertEquals(
                    customerSequenceBefore,
                    sequence("CUSTOMER_CREDIT_NOTE"));

            assertEquals(
                    journalSequenceBefore,
                    sequence("JOURNAL_ENTRY"));

            System.out.println(
                    "CCN_CONCURRENT_CLEANUP: OK");
        }
    }
}
