package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialDuplicateCommitIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long SUPPLIER = 1L;
    private static final Long ITEM = 1L;
    private static final Long BOX = 9L;

    @Autowired
    private SupplierInvoicePartialDraftCreationService creation;

    @Autowired
    private PlatformTransactionManager manager;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private SupplierInvoicePartialGenerationGuard gate;

    @MockitoBean
    private CompanyContext companyContext;

    // Soltanto il numeratore è simulato.
    // Writer, assembler, preparazione e repository sono reali.
    @MockitoBean
    private DocumentSequenceService sequences;

    private TransactionTemplate transaction() {
        TransactionTemplate tx = new TransactionTemplate(manager);
        tx.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );
        tx.setIsolationLevel(
                TransactionDefinition.ISOLATION_READ_COMMITTED
        );
        tx.setReadOnly(false);
        return tx;
    }

    private long number(String sql, Object... args) {
        Long result = jdbc.queryForObject(
                sql, Long.class, args
        );
        assertNotNull(result);
        return result;
    }

    private void verifyDatabase() throws Exception {
        try (Connection c = jdbc.getDataSource().getConnection()) {
            assertEquals("gestiva_test", c.getCatalog());
            String user = c.getMetaData().getUserName();
            assertTrue(
                    user.equals("gestiva_test_runner")
                    || user.startsWith("gestiva_test_runner@")
            );
        }
    }

    @Test
    @Timeout(45)
    void secondCreationRejectsCommittedDuplicate()
            throws Exception {

        verifyDatabase();

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        String suffix = UUID.randomUUID()
                .toString().substring(0, 12);

        String orderNumber = "IT2064-PO-" + suffix;
        String receiptNumber = "IT2064-GR-" + suffix;
        String invoiceNumber = "IT2064-SI-" + suffix;

        Long[] ids = new Long[4];

        CountDownLatch aAtSequence = new CountDownLatch(1);
        CountDownLatch releaseA = new CountDownLatch(1);
        CountDownLatch bStarted = new CountDownLatch(1);

        ExecutorService pool = null;
        boolean workersStopped = true;

        try {
            // Creazione della sola fixture temporanea.
            transaction().execute(status -> {

                jdbc.update("""
                    INSERT INTO purchase_order (
                        tenant_id,company_id,order_number,
                        order_date,supplier_id,status,currency_code,
                        subtotal_amount,tax_amount,total_amount,
                        created_at,updated_at
                    ) VALUES (
                        ?,?,?,?,?,'CONFIRMED','EUR',
                        144.00,31.68,175.68,
                        CURRENT_TIMESTAMP,CURRENT_TIMESTAMP
                    )
                    """,
                    TENANT, COMPANY, orderNumber,
                    Date.valueOf(LocalDate.now()), SUPPLIER
                );

                ids[0] = number("""
                    SELECT id FROM purchase_order
                    WHERE tenant_id=? AND company_id=?
                      AND order_number=?
                    """, TENANT, COMPANY, orderNumber);

                jdbc.update("""
                    INSERT INTO purchase_order_line (
                        tenant_id,purchase_order_id,line_no,
                        item_id,uom_id,description,
                        quantity,conversion_factor,unit_price,
                        discount_pct,tax_pct,line_subtotal,
                        tax_amount,line_total,created_at,updated_at
                    ) VALUES (
                        ?,?,1,?,?,'Fixture test 2064',
                        2.000,10.000000,72.00,
                        0.00,22.00,144.00,
                        31.68,175.68,
                        CURRENT_TIMESTAMP,CURRENT_TIMESTAMP
                    )
                    """, TENANT, ids[0], ITEM, BOX);

                ids[1] = number("""
                    SELECT id FROM purchase_order_line
                    WHERE tenant_id=? AND purchase_order_id=?
                      AND line_no=1
                    """, TENANT, ids[0]);

                jdbc.update("""
                    INSERT INTO goods_receipt (
                        tenant_id,company_id,receipt_number,
                        receipt_date,purchase_order_id,supplier_id,
                        created_at,updated_at
                    ) VALUES (
                        ?,?,?,?,?,?,
                        CURRENT_TIMESTAMP,CURRENT_TIMESTAMP
                    )
                    """,
                    TENANT, COMPANY, receiptNumber,
                    Date.valueOf(LocalDate.now()),
                    ids[0], SUPPLIER
                );

                ids[2] = number("""
                    SELECT id FROM goods_receipt
                    WHERE tenant_id=? AND company_id=?
                      AND receipt_number=?
                    """, TENANT, COMPANY, receiptNumber);

                jdbc.update("""
                    INSERT INTO goods_receipt_line (
                        tenant_id,goods_receipt_id,line_no,
                        purchase_order_line_id,item_id,
                        description,quantity_received,
                        created_at,updated_at
                    ) VALUES (
                        ?,?,1,?,?,'Fixture test 2064',8.000,
                        CURRENT_TIMESTAMP,CURRENT_TIMESTAMP
                    )
                    """,
                    TENANT, ids[2], ids[1], ITEM
                );

                ids[3] = number("""
                    SELECT id FROM goods_receipt_line
                    WHERE tenant_id=? AND goods_receipt_id=?
                      AND line_no=1
                    """, TENANT, ids[2]);

                return null;
            });

            when(sequences.nextNumber(
                    TENANT,
                    COMPANY,
                    DocumentSequenceService.SUPPLIER_INVOICE
            )).thenAnswer(invocation -> {

                aAtSequence.countDown();

                if (!releaseA.await(
                        10, TimeUnit.SECONDS
                )) {
                    throw new IllegalStateException(
                            "Timeout rilascio creazione A"
                    );
                }

                return invoiceNumber;
            });

            pool = Executors.newFixedThreadPool(2);

            Future<Long> first = pool.submit(
                    () -> creation.create(TENANT, ids[2])
            );

            if (!aAtSequence.await(10, TimeUnit.SECONDS)) {
                if (first.isDone()) {
                    first.get(2, TimeUnit.SECONDS);
                }
                fail("Creazione A non arrivata al numeratore.");
            }

            System.out.println(
                    "DUPLICATE COMMIT: A mantiene il lock"
            );

            Future<BusinessException> second =
                    pool.submit(() -> {
                        bStarted.countDown();
                        return assertThrows(
                                BusinessException.class,
                                () -> creation.create(
                                        TENANT, ids[2]
                                )
                        );
                    });

            assertTrue(
                    bStarted.await(10, TimeUnit.SECONDS)
            );

            assertFalse(
                    second.isDone(),
                    "La richiesta B è terminata prima del commit A."
            );

            assertFalse(
                    second.isDone()
            );

            // Verifica una finestra di contesa reale.
            try {
                second.get(500, TimeUnit.MILLISECONDS);
                fail("B non ha atteso il lock di A.");
            } catch (TimeoutException expected) {
                System.out.println(
                        "DUPLICATE COMMIT: B in attesa"
                );
            }

            releaseA.countDown();

            Long createdId = first.get(
                    10, TimeUnit.SECONDS
            );
            assertNotNull(createdId);

            BusinessException duplicate = second.get(
                    10, TimeUnit.SECONDS
            );

            assertTrue(
                    duplicate.getMessage().contains(
                            "Esiste già una fattura"
                    )
            );

            assertEquals(1L, number("""
                SELECT COUNT(*) FROM supplier_invoice
                WHERE tenant_id=? AND company_id=?
                  AND goods_receipt_id=?
                  AND invoice_number=?
                  AND status='DRAFT'
                """,
                TENANT, COMPANY, ids[2], invoiceNumber
            ));

            assertEquals(1L, number("""
                SELECT COUNT(*) FROM supplier_invoice_line
                WHERE tenant_id=? AND supplier_invoice_id=?
                """, TENANT, createdId));

            assertEquals(1L, number("""
                SELECT COUNT(*) FROM supplier_invoice
                WHERE tenant_id=? AND company_id=?
                  AND goods_receipt_id=?
                """, TENANT, COMPANY, ids[2]));

            verify(sequences, times(1)).nextNumber(
                    TENANT,
                    COMPANY,
                    DocumentSequenceService.SUPPLIER_INVOICE
            );

            System.out.println(
                    "DUPLICATE COMMIT: A ha effettuato commit"
            );
            System.out.println(
                    "DUPLICATE COMMIT: B ha rifiutato il duplicato"
            );
            System.out.println(
                    "DUPLICATE COMMIT: una sola fattura DRAFT"
            );
            System.out.println(
                    "DUPLICATE COMMIT: numeratore invocato una volta"
            );

        } finally {
            releaseA.countDown();

            if (pool != null) {
                pool.shutdownNow();
                workersStopped = pool.awaitTermination(
                        10, TimeUnit.SECONDS
                );
            }

            // Non eliminare dati sotto transazioni ancora vive.
            if (!workersStopped) {
                throw new IllegalStateException(
                        "Thread non terminati: pulizia sospesa. "
                        + "Verificare manualmente fixture " + suffix
                );
            }

            // DELETE limitati ai numeri e agli ID temporanei.
            if (ids[0] != null && ids[1] != null
                    && ids[2] != null && ids[3] != null) {

                transaction().execute(status -> {

                    // Verifica che gli ID corrispondano
                    // ancora ai documenti di questa fixture.
                    assertEquals(1L, number("""
                        SELECT COUNT(*) FROM purchase_order
                        WHERE id=? AND tenant_id=? AND company_id=?
                          AND order_number=?
                        """,
                        ids[0], TENANT, COMPANY, orderNumber
                    ));

                    assertEquals(1L, number("""
                        SELECT COUNT(*) FROM goods_receipt
                        WHERE id=? AND tenant_id=? AND company_id=?
                          AND receipt_number=?
                          AND purchase_order_id=?
                        """,
                        ids[2], TENANT, COMPANY,
                        receiptNumber, ids[0]
                    ));

                    // Non cancellare documenti non attribuibili
                    // esplicitamente al test.
                    assertEquals(0L, number("""
                        SELECT COUNT(*) FROM supplier_invoice
                        WHERE tenant_id=? AND company_id=?
                          AND goods_receipt_id=?
                          AND invoice_number<>?
                        """,
                        TENANT, COMPANY,
                        ids[2], invoiceNumber
                    ));

                    jdbc.update("""
                        DELETE FROM supplier_invoice_line
                        WHERE tenant_id=?
                          AND supplier_invoice_id IN (
                            SELECT id FROM supplier_invoice
                            WHERE tenant_id=? AND company_id=?
                              AND goods_receipt_id=?
                              AND invoice_number=?
                          )
                        """,
                        TENANT, TENANT, COMPANY,
                        ids[2], invoiceNumber
                    );

                    jdbc.update("""
                        DELETE FROM supplier_invoice
                        WHERE tenant_id=? AND company_id=?
                          AND goods_receipt_id=?
                          AND invoice_number=?
                        """,
                        TENANT, COMPANY,
                        ids[2], invoiceNumber
                    );

                    jdbc.update("""
                        DELETE FROM goods_receipt_line
                        WHERE id=? AND tenant_id=?
                          AND goods_receipt_id=?
                        """,
                        ids[3], TENANT, ids[2]
                    );

                    jdbc.update("""
                        DELETE FROM goods_receipt
                        WHERE id=? AND tenant_id=? AND company_id=?
                          AND receipt_number=?
                        """,
                        ids[2], TENANT, COMPANY, receiptNumber
                    );

                    jdbc.update("""
                        DELETE FROM purchase_order_line
                        WHERE id=? AND tenant_id=?
                          AND purchase_order_id=?
                        """,
                        ids[1], TENANT, ids[0]
                    );

                    jdbc.update("""
                        DELETE FROM purchase_order
                        WHERE id=? AND tenant_id=? AND company_id=?
                          AND order_number=?
                        """,
                        ids[0], TENANT, COMPANY, orderNumber
                    );

                    return null;
                });

                assertEquals(0L, number("""
                    SELECT COUNT(*) FROM purchase_order
                    WHERE tenant_id=? AND company_id=?
                      AND order_number=?
                    """, TENANT, COMPANY, orderNumber));

                assertEquals(0L, number("""
                    SELECT COUNT(*) FROM goods_receipt
                    WHERE tenant_id=? AND company_id=?
                      AND receipt_number=?
                    """, TENANT, COMPANY, receiptNumber));

                assertEquals(0L, number("""
                    SELECT COUNT(*) FROM supplier_invoice
                    WHERE tenant_id=? AND company_id=?
                      AND invoice_number=?
                    """, TENANT, COMPANY, invoiceNumber));

                System.out.println(
                        "DUPLICATE COMMIT: fixture eliminata e verificata"
                );
            }
        }
    }
}
