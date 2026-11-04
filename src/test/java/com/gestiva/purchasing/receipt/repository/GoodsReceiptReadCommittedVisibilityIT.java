package com.gestiva.purchasing.receipt.repository;

import com.gestiva.support.AbstractMySqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptReadCommittedVisibilityIT
        extends AbstractMySqlIntegrationTest {

    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager manager;
    @Autowired GoodsReceiptLineRepository lines;

    private void verifyDatabase() throws Exception {
        try (Connection c = jdbc.getDataSource().getConnection()) {
            assertEquals("gestiva_test", c.getCatalog());
            String u = c.getMetaData().getUserName();
            assertTrue(u.equals("gestiva_test_runner")
                    || u.startsWith("gestiva_test_runner@"));
        }
    }

    private <T> T transaction(
            java.util.function.Function<
                    org.springframework.transaction.TransactionStatus,T> work) {
        TransactionTemplate tx = new TransactionTemplate(manager);
        tx.setReadOnly(false);
        return tx.execute(work::apply);
    }

    @Test
    @Timeout(35)
    void waitingTransactionReadsCommittedReceipt() throws Exception {
        verifyDatabase();

        Map<String,Object> reference = jdbc.queryForMap("""
            SELECT po.tenant_id, po.company_id, po.supplier_id
            FROM purchase_order po
            ORDER BY po.id ASC LIMIT 1
            """);

        long tenant = ((Number) reference.get("tenant_id")).longValue();
        long company = ((Number) reference.get("company_id")).longValue();
        long supplier = ((Number) reference.get("supplier_id")).longValue();

        String suffix = UUID.randomUUID().toString().substring(0,12);
        String orderNumber = "IT1884-PO-" + suffix;
        String receiptNumber = "IT1884-GR-" + suffix;

        Long[] fixture = new Long[3];
        ExecutorService pool = null;
        CountDownLatch allowCommit = new CountDownLatch(1);

        try {
            transaction(status -> {
                jdbc.update("""
                    INSERT INTO purchase_order (
                        tenant_id,company_id,order_number,order_date,
                        supplier_id,status,currency_code,
                        subtotal_amount,tax_amount,total_amount,
                        created_at,updated_at
                    ) VALUES (
                        ?,?,?,? ,?,'CONFIRMED','EUR',
                        144.00,31.68,175.68,
                        CURRENT_TIMESTAMP,CURRENT_TIMESTAMP
                    )
                    """,
                    tenant,company,orderNumber,
                    Date.valueOf(LocalDate.now()),supplier
                );

                fixture[0] = jdbc.queryForObject("""
                    SELECT id FROM purchase_order
                    WHERE tenant_id=? AND company_id=?
                      AND order_number=?
                    """, Long.class,tenant,company,orderNumber);

                jdbc.update("""
                    INSERT INTO purchase_order_line (
                        tenant_id,purchase_order_id,line_no,description,
                        quantity,conversion_factor,unit_price,
                        discount_pct,tax_pct,line_subtotal,
                        tax_amount,line_total,created_at,updated_at
                    ) VALUES (
                        ?,?,1,'Fixture concorrenza 1882',
                        2.000,10.000000,72.00,
                        0.00,22.00,144.00,
                        31.68,175.68,
                        CURRENT_TIMESTAMP,CURRENT_TIMESTAMP
                    )
                    """,tenant,fixture[0]);

                fixture[1] = jdbc.queryForObject("""
                    SELECT id FROM purchase_order_line
                    WHERE tenant_id=? AND purchase_order_id=?
                      AND line_no=1
                    """,Long.class,tenant,fixture[0]);
                return null;
            });

            System.out.println(
                "COMMIT VISIBILITY: fixture order_id=" + fixture[0]
            );

            CountDownLatch aLocked = new CountDownLatch(1);
            CountDownLatch bAttempt = new CountDownLatch(1);
            CountDownLatch bLocked = new CountDownLatch(1);

            pool = Executors.newFixedThreadPool(2);

            Future<?> a = pool.submit(() -> transaction(status -> {
                Long locked = jdbc.queryForObject("""
                    SELECT id FROM purchase_order
                    WHERE id=? AND tenant_id=? AND company_id=?
                    FOR UPDATE
                    """,Long.class,fixture[0],tenant,company);
                assertEquals(fixture[0],locked);

                jdbc.update("""
                    INSERT INTO goods_receipt (
                        tenant_id,company_id,receipt_number,receipt_date,
                        purchase_order_id,supplier_id,created_at,updated_at
                    ) VALUES (
                        ?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP
                    )
                    """,
                    tenant,company,receiptNumber,
                    Date.valueOf(LocalDate.now()),fixture[0],supplier);

                fixture[2] = jdbc.queryForObject("""
                    SELECT id FROM goods_receipt
                    WHERE tenant_id=? AND company_id=?
                      AND receipt_number=?
                    """,Long.class,tenant,company,receiptNumber);

                jdbc.update("""
                    INSERT INTO goods_receipt_line (
                        tenant_id,goods_receipt_id,line_no,
                        purchase_order_line_id,description,
                        quantity_received,created_at,updated_at
                    ) VALUES (
                        ?,?,1,?,'Fixture 8 PZ',8.000,
                        CURRENT_TIMESTAMP,CURRENT_TIMESTAMP
                    )
                    """,tenant,fixture[2],fixture[1]);

                aLocked.countDown();

                try {
                    if (!allowCommit.await(10,TimeUnit.SECONDS)) {
                        throw new IllegalStateException(
                            "Timeout autorizzazione commit A");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(ex);
                }
                return null;
            }));

            boolean firstReady = aLocked.await(10,TimeUnit.SECONDS);
            if (!firstReady) {
                if (a.isDone()) a.get(2,TimeUnit.SECONDS);
                fail("Transazione A non pronta.");
            }
            System.out.println(
                "COMMIT VISIBILITY: A ha inserito 8 PZ non confermati"
            );

            Future<BigDecimal> b = pool.submit(() -> {

                TransactionTemplate bTransaction =
                        new TransactionTemplate(manager);

                bTransaction.setReadOnly(false);
                bTransaction.setIsolationLevel(
                        org.springframework.transaction
                                .TransactionDefinition
                                .ISOLATION_READ_COMMITTED
                );

                return bTransaction.execute(status -> {
                    BigDecimal beforeLock =
                            lines.sumReceivedBaseQuantityByOrderLine(
                                    tenant, company,
                                    fixture[0], fixture[1]
                            );

                    assertNull(
                            beforeLock,
                            "B non deve vedere i dati non confermati di A."
                    );

                    System.out.println(
                            "READ COMMITTED: B legge NULL prima del blocco"
                    );

                    bAttempt.countDown();

                    Long locked = jdbc.queryForObject("""
                        SELECT id FROM purchase_order
                        WHERE id=? AND tenant_id=? AND company_id=?
                        FOR UPDATE
                        """,Long.class,fixture[0],tenant,company);
                    assertEquals(fixture[0],locked);
                    bLocked.countDown();

                    return lines.sumReceivedBaseQuantityByOrderLine(
                        tenant,company,fixture[0],fixture[1]
                    );
                });
            });

            assertTrue(bAttempt.await(10,TimeUnit.SECONDS));

            assertFalse(
                bLocked.await(400,TimeUnit.MILLISECONDS),
                "B ha acquisito il blocco prima del commit di A."
            );
            System.out.println(
                "READ COMMITTED: B attende il blocco"
            );

            allowCommit.countDown();
            a.get(10,TimeUnit.SECONDS);
            System.out.println(
                "READ COMMITTED: A commit completato"
            );

            BigDecimal observed = b.get(10,TimeUnit.SECONDS);

            assertNotNull(
                    observed,
                    "Con READ COMMITTED B deve vedere "
                    + "il ricevimento confermato da A."
            );

            assertEquals(
                    0,
                    new BigDecimal("8.000").compareTo(observed)
            );

            System.out.println(
                "READ COMMITTED: B legge quantità=" + observed
            );

        } finally {
            allowCommit.countDown();

            if (pool != null) {
                pool.shutdownNow();
                assertTrue(pool.awaitTermination(10,TimeUnit.SECONDS),
                    "Thread ancora attivi: pulizia non sicura.");
            }

            // Eliminazione limitata agli identificativi della fixture.
            // Nessun DELETE generico sui documenti di collaudo.
            if (fixture[0] != null) {
                transaction(status -> {
                    jdbc.update("""
                        DELETE FROM goods_receipt_line
                        WHERE tenant_id=?
                          AND goods_receipt_id IN (
                              SELECT id FROM goods_receipt
                              WHERE tenant_id=? AND company_id=?
                                AND purchase_order_id=?
                                AND receipt_number=?
                          )
                        """,tenant,tenant,company,fixture[0],receiptNumber);

                    jdbc.update("""
                        DELETE FROM goods_receipt
                        WHERE tenant_id=? AND company_id=?
                          AND purchase_order_id=?
                          AND receipt_number=?
                        """,tenant,company,fixture[0],receiptNumber);

                    jdbc.update("""
                        DELETE FROM purchase_order_line
                        WHERE tenant_id=? AND purchase_order_id=?
                          AND id=?
                        """,tenant,fixture[0],fixture[1]);

                    jdbc.update("""
                        DELETE FROM purchase_order
                        WHERE tenant_id=? AND company_id=? AND id=?
                          AND order_number=?
                        """,tenant,company,fixture[0],orderNumber);
                    return null;
                });

                Long remaining = jdbc.queryForObject("""
                    SELECT COUNT(*) FROM purchase_order
                    WHERE id=? AND tenant_id=? AND company_id=?
                      AND order_number=?
                    """,Long.class,fixture[0],tenant,company,orderNumber);

                assertEquals(0L,remaining);
                System.out.println(
                    "READ COMMITTED: fixture eliminata e verificata"
                );
            }
        }
    }
}
