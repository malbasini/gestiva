

package com.gestiva.creditnote.service;

import com.gestiva.accounting.due.entity.PaymentDue;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteFinancialPosition;
import com.gestiva.creditnote.entity.CustomerCreditNoteCompensation;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.security.company.context.CompanyContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnabledIfEnvironmentVariable(
        named = "GESTIVA_V11_SANDBOX_TEST",
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
class CustomerCreditNoteCompensationV11ConcurrentSandboxITTest {

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
    @Autowired CustomerCreditNoteCompensationService service;
    @Autowired PlatformTransactionManager transactionManager;


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


    private record Fixture(
            Long customerId,
            Long noteId,
            Long positionId,
            Long dueId
    ) {}

    @Test
    void concurrentCustomerCompensationsRespectLocks() throws Exception {
        assertEquals(DATABASE,
                jdbc.queryForObject("SELECT DATABASE()", String.class));
        assertEquals(33308,
                jdbc.queryForObject("SELECT @@port", Integer.class));
        assertEquals(
                "gestiva_scn_migrator@127.0.0.1",
                jdbc.queryForObject("SELECT CURRENT_USER()", String.class)
        );

        for (String table : new String[] {
                "customer",
                "credit_note",
                "credit_note_financial_position",
                "payment_due",
                "customer_credit_note_compensation"
        }) {
            assertEquals(0L, count(table),
                    "Sandbox non vuoto: " + table);
        }

        long paymentsBefore = count("payment_transaction");
        long journalsBefore = count("journal_entry");

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        LocalDate date = LocalDate.of(2026, 11, 14);
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        TransactionTemplate tx =
                new TransactionTemplate(transactionManager);

        Fixture fixture = null;
        java.util.concurrent.ExecutorService executor = null;
        java.util.concurrent.CountDownLatch aCompensated =
                new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch releaseA =
                new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch bReady =
                new java.util.concurrent.CountDownLatch(1);

        try {
            fixture = tx.execute(status -> {
                Customer customer = new Customer();
                customer.setTenantId(TENANT_ID);
                customer.setName("Cliente commit V11 " + suffix);
                customer.setType("COMPANY");
                customer.setStatus("ACTIVE");
                em.persist(customer);
                em.flush();

                CreditNote note = new CreditNote();
                note.setTenantId(TENANT_ID);
                note.setCompanyId(COMPANY_ID);
                note.setCreditNoteType("CUSTOMER");
                note.setCreditNoteNumber("CCN-V11C-" + suffix);
                note.setCreditNoteDate(date);
                note.setOriginalInvoiceId(900002L);
                note.setPartyId(customer.getId());
                note.setStatus("REGISTERED");
                note.setCurrencyCode("EUR");
                note.setReason("Collaudo commit compensazione V11");
                note.setSubtotalAmount(money("76.50"));
                note.setTaxAmount(money("16.83"));
                note.setTotalAmount(money("93.33"));
                em.persist(note);
                em.flush();

                CreditNoteFinancialPosition position =
                        new CreditNoteFinancialPosition();
                position.setTenantId(TENANT_ID);
                position.setCompanyId(COMPANY_ID);
                position.setCreditNoteId(note.getId());
                position.setPositionType("CUSTOMER");
                position.setCurrencyCode("EUR");
                position.setOriginalAmount(money("93.33"));
                position.setCompensatedAmount(money("0.00"));
                position.setRefundedAmount(money("0.00"));
                position.setAvailableAmount(money("93.33"));
                position.setStatus("OPEN");
                em.persist(position);
                em.flush();

                PaymentDue due = new PaymentDue();
                due.setTenantId(TENANT_ID);
                due.setCompanyId(COMPANY_ID);
                due.setDirection("RECEIVABLE");
                due.setPartyType("CUSTOMER");
                due.setPartyId(customer.getId());
                due.setDocumentNumber("INV-V11C-" + suffix);
                due.setDocumentDate(date);
                due.setDueDate(date.plusDays(30));
                due.setReferenceType("CUSTOMER_INVOICE");
                due.setReferenceId(900002L);
                due.setCurrencyCode("EUR");
                due.setGrossAmount(money("186.66"));
                due.setPaidAmount(money("0.00"));
                due.setCompensatedAmount(money("0.00"));
                due.setOpenAmount(money("186.66"));
                due.setStatus("OPEN");
                em.persist(due);
                em.flush();

                return new Fixture(
                        customer.getId(),
                        note.getId(),
                        position.getId(),
                        due.getId()
                );
            });

            assertNotNull(fixture);

            Long compensationId = service.compensate(
                    TENANT_ID,
                    fixture.positionId(),
                    fixture.dueId(),
                    money("50.00"),
                    date,
                    "V11-COMMIT-" + suffix,
                    "Compensazione cliente con commit reale"
            );

            assertNotNull(compensationId);

            BigDecimal available = jdbc.queryForObject(
                    """
                    SELECT available_amount
                    FROM credit_note_financial_position
                    WHERE id = ? AND tenant_id = ? AND company_id = ?
                    """,
                    BigDecimal.class,
                    fixture.positionId(), TENANT_ID, COMPANY_ID
            );

            BigDecimal compensated = jdbc.queryForObject(
                    """
                    SELECT compensated_amount
                    FROM credit_note_financial_position
                    WHERE id = ? AND tenant_id = ? AND company_id = ?
                    """,
                    BigDecimal.class,
                    fixture.positionId(), TENANT_ID, COMPANY_ID
            );

            BigDecimal dueOpen = jdbc.queryForObject(
                    """
                    SELECT open_amount
                    FROM payment_due
                    WHERE id = ? AND tenant_id = ? AND company_id = ?
                    """,
                    BigDecimal.class,
                    fixture.dueId(), TENANT_ID, COMPANY_ID
            );

            assertNotNull(available);
            assertNotNull(compensated);
            assertNotNull(dueOpen);

            assertEquals(0, money("43.33").compareTo(available));
            assertEquals(0, money("50.00").compareTo(compensated));
            assertEquals(0, money("136.66").compareTo(dueOpen));

            Long saved = jdbc.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM customer_credit_note_compensation
                    WHERE id = ?
                      AND tenant_id = ?
                      AND company_id = ?
                      AND amount = 50.00
                    """,
                    Long.class,
                    compensationId, TENANT_ID, COMPANY_ID
            );

            assertEquals(1L, saved);


            // Due richieste da 30 EUR su credito disponibile 43.33.
            // Soltanto una può essere accettata.
            Fixture concurrentFixture = fixture;

            executor = java.util.concurrent.Executors.newFixedThreadPool(2);

            java.util.concurrent.Future<Long> futureA =
                    executor.submit(() -> {
                        TransactionTemplate worker =
                                new TransactionTemplate(transactionManager);
                        worker.setIsolationLevel(
                                org.springframework.transaction.TransactionDefinition
                                        .ISOLATION_READ_COMMITTED
                        );

                        return worker.execute(status -> {
                            Long connectionA = jdbc.queryForObject(
                                    "SELECT CONNECTION_ID()", Long.class);
                            assertNotNull(connectionA);

                            Long id = service.compensate(
                                    TENANT_ID,
                                    concurrentFixture.positionId(),
                                    concurrentFixture.dueId(),
                                    money("30.00"),
                                    date,
                                    "V11-CONCURRENT-A-" + suffix,
                                    "Compensazione concorrente A"
                            );
                            assertNotNull(id);

                            aCompensated.countDown();

                            try {
                                if (!releaseA.await(
                                        20,
                                        java.util.concurrent.TimeUnit.SECONDS
                                )) {
                                    throw new IllegalStateException(
                                            "Timeout rilascio lock A"
                                    );
                                }
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                                throw new IllegalStateException(ex);
                            }

                            return connectionA;
                        });
                    });

            assertTrue(aCompensated.await(
                    20, java.util.concurrent.TimeUnit.SECONDS));

            java.util.concurrent.atomic.AtomicReference<Long>
                    connectionB = new java.util.concurrent.atomic.AtomicReference<>();

            java.util.concurrent.Future<
                    com.gestiva.common.exception.BusinessException
                    > futureB = executor.submit(() -> {
                TransactionTemplate worker =
                        new TransactionTemplate(transactionManager);
                worker.setIsolationLevel(
                        org.springframework.transaction.TransactionDefinition
                                .ISOLATION_READ_COMMITTED
                );

                return assertThrows(
                        com.gestiva.common.exception.BusinessException.class,
                        () -> worker.execute(status -> {
                            connectionB.set(jdbc.queryForObject(
                                    "SELECT CONNECTION_ID()", Long.class));

                            bReady.countDown();

                            return service.compensate(
                                    TENANT_ID,
                                    concurrentFixture.positionId(),
                                    concurrentFixture.dueId(),
                                    money("30.00"),
                                    date,
                                    "V11-CONCURRENT-B-" + suffix,
                                    "Compensazione concorrente B"
                            );
                        })
                );
            });

            assertTrue(bReady.await(
                    20, java.util.concurrent.TimeUnit.SECONDS));

            // B non deve completare mentre A detiene il lock.
            assertThrows(
                    java.util.concurrent.TimeoutException.class,
                    () -> futureB.get(
                            500, java.util.concurrent.TimeUnit.MILLISECONDS)
            );

            releaseA.countDown();

            Long connectionA = futureA.get(
                    20, java.util.concurrent.TimeUnit.SECONDS);
            var rejected = futureB.get(
                    20, java.util.concurrent.TimeUnit.SECONDS);

            assertNotNull(connectionA);
            assertNotNull(connectionB.get());
            assertNotNull(rejected);
            assertNotEquals(connectionA, connectionB.get());

            BigDecimal finalCredit = jdbc.queryForObject(
                    """
                    SELECT available_amount
                    FROM credit_note_financial_position
                    WHERE id = ? AND tenant_id = ? AND company_id = ?
                    """,
                    BigDecimal.class,
                    fixture.positionId(), TENANT_ID, COMPANY_ID
            );

            BigDecimal finalDue = jdbc.queryForObject(
                    """
                    SELECT open_amount
                    FROM payment_due
                    WHERE id = ? AND tenant_id = ? AND company_id = ?
                    """,
                    BigDecimal.class,
                    fixture.dueId(), TENANT_ID, COMPANY_ID
            );

            assertNotNull(finalCredit);
            assertNotNull(finalDue);

            assertEquals(0, money("13.33").compareTo(finalCredit));
            assertEquals(0, money("106.66").compareTo(finalDue));

            Long accepted = jdbc.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM customer_credit_note_compensation
                    WHERE tenant_id = ? AND company_id = ?
                      AND operation_key = ?
                    """,
                    Long.class,
                    TENANT_ID, COMPANY_ID,
                    "V11-CONCURRENT-A-" + suffix
            );

            Long rejectedRows = jdbc.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM customer_credit_note_compensation
                    WHERE tenant_id = ? AND company_id = ?
                      AND operation_key = ?
                    """,
                    Long.class,
                    TENANT_ID, COMPANY_ID,
                    "V11-CONCURRENT-B-" + suffix
            );

            assertEquals(1L, accepted);
            assertEquals(0L, rejectedRows);

            System.out.println("V11_CONCURRENT_LOCKS: VERIFIED");
            System.out.println("V11_CONCURRENT_EXCESS_REJECTED: VERIFIED");

            Long badCredit = jdbc.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM credit_note_financial_position
                    WHERE id = ?
                      AND original_amount <>
                          compensated_amount + refunded_amount
                          + available_amount
                    """,
                    Long.class, fixture.positionId()
            );

            Long badDue = jdbc.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM payment_due
                    WHERE id = ?
                      AND gross_amount <>
                          paid_amount + compensated_amount + open_amount
                    """,
                    Long.class, fixture.dueId()
            );

            assertEquals(0L, badCredit);
            assertEquals(0L, badDue);

            assertEquals(paymentsBefore, count("payment_transaction"));
            assertEquals(journalsBefore, count("journal_entry"));

            System.out.println("V11_CUSTOMER_COMMIT: VERIFIED");
            System.out.println("V11_CUSTOMER_BALANCES: VERIFIED");

        } finally {
            releaseA.countDown();

            if (executor != null) {
                executor.shutdownNow();

                boolean stopped = executor.awaitTermination(
                        20, java.util.concurrent.TimeUnit.SECONDS);

                if (!stopped) {
                    throw new IllegalStateException(
                            "STOP: worker ancora attivi; cleanup non sicuro");
                }
            }

            if (fixture != null) {
                Fixture f = fixture;

                assertEquals(DATABASE,
                        jdbc.queryForObject("SELECT DATABASE()", String.class));
                assertEquals(33308,
                        jdbc.queryForObject("SELECT @@port", Integer.class));

                tx.executeWithoutResult(status -> {
                    jdbc.update(
                            """
                            DELETE FROM customer_credit_note_compensation
                            WHERE tenant_id = ?
                              AND company_id = ?
                              AND credit_note_financial_position_id = ?
                              AND payment_due_id = ?
                            """,
                            TENANT_ID, COMPANY_ID,
                            f.positionId(), f.dueId()
                    );

                    assertEquals(1, jdbc.update(
                            """
                            DELETE FROM credit_note_financial_position
                            WHERE id = ? AND tenant_id = ? AND company_id = ?
                            """,
                            f.positionId(), TENANT_ID, COMPANY_ID
                    ));

                    assertEquals(1, jdbc.update(
                            """
                            DELETE FROM payment_due
                            WHERE id = ? AND tenant_id = ? AND company_id = ?
                            """,
                            f.dueId(), TENANT_ID, COMPANY_ID
                    ));

                    assertEquals(1, jdbc.update(
                            """
                            DELETE FROM credit_note
                            WHERE id = ? AND tenant_id = ? AND company_id = ?
                            """,
                            f.noteId(), TENANT_ID, COMPANY_ID
                    ));

                    assertEquals(1, jdbc.update(
                            """
                            DELETE FROM customer
                            WHERE id = ? AND tenant_id = ?
                            """,
                            f.customerId(), TENANT_ID
                    ));
                });

                for (String table : new String[] {
                        "customer",
                        "credit_note",
                        "credit_note_financial_position",
                        "payment_due",
                        "customer_credit_note_compensation"
                }) {
                    assertEquals(0L, count(table),
                            "Residui post-cleanup: " + table);
                }

                System.out.println("V11_CUSTOMER_CLEANUP: VERIFIED");
            }
        }
    }
}
