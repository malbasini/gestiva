package com.gestiva.purchasing.invoice.service;

import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialApplicationThreeReceiptsRollbackIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long SUPPLIER = 1L;
    private static final Long ITEM = 1L;
    private static final Long BOX = 9L;

    @Autowired
    private SupplierInvoicePartialApplicationService application;

    @Autowired
    private PlatformTransactionManager manager;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private SupplierInvoicePartialGenerationGuard gate;

    @MockitoBean
    private CompanyContext companyContext;

    private long value(String sql, Object... args) {
        Long result = jdbc.queryForObject(
                sql, Long.class, args
        );
        assertNotNull(result);
        return result;
    }

    private BigDecimal amount(String sql, Object... args) {
        BigDecimal result = jdbc.queryForObject(
                sql, BigDecimal.class, args
        );
        assertNotNull(result);
        return result;
    }

    private void equalsAmount(
            String expected, BigDecimal actual) {

        assertEquals(
                0,
                new BigDecimal(expected).compareTo(actual)
        );
    }

    private long sequence() {
        return value("""
                SELECT next_number FROM document_sequence
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND document_type = ?
                """,
                TENANT, COMPANY,
                DocumentSequenceService.SUPPLIER_INVOICE
        );
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

        assertEquals(1L, value("""
                SELECT COUNT(*) FROM item
                WHERE tenant_id = ? AND id = ?
                  AND item_type = 'PRODUCT'
                  AND base_uom_id = 1
                """, TENANT, ITEM));

        assertEquals(1L, value("""
                SELECT COUNT(*) FROM unit_of_measure
                WHERE tenant_id = ? AND id = ?
                  AND code = 'BOX'
                """, TENANT, BOX));

        assertEquals(1L, value("""
                SELECT COUNT(*) FROM document_sequence
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND document_type = ?
                """,
                TENANT, COMPANY,
                DocumentSequenceService.SUPPLIER_INVOICE
        ));
    }

    private Long receipt(
            String number,
            Long orderId,
            Long lineId,
            String quantity) {

        jdbc.update("""
                INSERT INTO goods_receipt (
                    tenant_id, company_id, receipt_number,
                    receipt_date, purchase_order_id,
                    supplier_id, created_at, updated_at
                ) VALUES (
                    ?, ?, ?, ?, ?, ?,
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """,
                TENANT, COMPANY, number,
                Date.valueOf(LocalDate.now()),
                orderId, SUPPLIER
        );

        Long receiptId = value("""
                SELECT id FROM goods_receipt
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND receipt_number = ?
                """, TENANT, COMPANY, number);

        jdbc.update("""
                INSERT INTO goods_receipt_line (
                    tenant_id, goods_receipt_id, line_no,
                    purchase_order_line_id, item_id,
                    description, quantity_received,
                    created_at, updated_at
                ) VALUES (
                    ?, ?, 1, ?, ?,
                    'Ricevimento test fatture parziali',
                    ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """,
                TENANT, receiptId, lineId, ITEM,
                new BigDecimal(quantity)
        );

        return receiptId;
    }

    @Test
    void threeDraftInvoicesReconcileAndRollback()
            throws Exception {

        verifyDatabase();

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        String suffix = UUID.randomUUID()
                .toString().substring(0, 12);

        String orderNumber = "IT2077-PO-" + suffix;
        String receiptPrefix = "IT2077-GR-" + suffix;

        long before = sequence();

        Long[] orderId = new Long[1];
        Long[] receipts = new Long[3];
        Long[] invoices = new Long[3];

        TransactionTemplate tx =
                new TransactionTemplate(manager);

        tx.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );
        tx.setIsolationLevel(
                TransactionDefinition.ISOLATION_READ_COMMITTED
        );
        tx.setReadOnly(false);

        tx.execute(status -> {

            jdbc.update("""
                    INSERT INTO purchase_order (
                        tenant_id, company_id, order_number,
                        order_date, supplier_id, status,
                        currency_code, subtotal_amount,
                        tax_amount, total_amount,
                        created_at, updated_at
                    ) VALUES (
                        ?, ?, ?, ?, ?, 'CONFIRMED',
                        'EUR', 144.00, 31.68, 175.68,
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                    )
                    """,
                    TENANT, COMPANY, orderNumber,
                    Date.valueOf(LocalDate.now()), SUPPLIER
            );

            orderId[0] = value("""
                    SELECT id FROM purchase_order
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND order_number = ?
                    """,
                    TENANT, COMPANY, orderNumber
            );

            jdbc.update("""
                    INSERT INTO purchase_order_line (
                        tenant_id, purchase_order_id, line_no,
                        item_id, uom_id, description,
                        quantity, conversion_factor,
                        unit_price, discount_pct, tax_pct,
                        line_subtotal, tax_amount, line_total,
                        created_at, updated_at
                    ) VALUES (
                        ?, ?, 1, ?, ?,
                        'Articolo test tre fatture',
                        2.000, 10.000000,
                        72.00, 0.00, 22.00,
                        144.00, 31.68, 175.68,
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                    )
                    """,
                    TENANT, orderId[0], ITEM, BOX
            );

            Long orderLineId = value("""
                    SELECT id FROM purchase_order_line
                    WHERE tenant_id = ?
                      AND purchase_order_id = ?
                      AND line_no = 1
                    """,
                    TENANT, orderId[0]
            );

            receipts[0] = receipt(
                    receiptPrefix + "-1",
                    orderId[0], orderLineId, "8.000"
            );

            receipts[1] = receipt(
                    receiptPrefix + "-2",
                    orderId[0], orderLineId, "7.000"
            );

            receipts[2] = receipt(
                    receiptPrefix + "-3",
                    orderId[0], orderLineId, "5.000"
            );

            assertTrue(receipts[0] < receipts[1]);
            assertTrue(receipts[1] < receipts[2]);

            // Tutti i servizi e repository sono reali.
            for (int i = 0; i < 3; i++) {
                invoices[i] = application.create(
                        TENANT, receipts[i]
                );
                assertNotNull(invoices[i]);
            }

            assertNotEquals(invoices[0], invoices[1]);
            assertNotEquals(invoices[1], invoices[2]);
            assertNotEquals(invoices[0], invoices[2]);

            entityManager.flush();

            String[] subtotals = {
                    "57.60", "50.40", "36.00"
            };
            String[] taxes = {
                    "12.67", "11.09", "7.92"
            };
            String[] totals = {
                    "70.27", "61.49", "43.92"
            };
            String[] quantities = {
                    "0.800", "0.700", "0.500"
            };

            for (int i = 0; i < 3; i++) {

                assertEquals(1L, value("""
                        SELECT COUNT(*) FROM supplier_invoice
                        WHERE id = ?
                          AND tenant_id = ?
                          AND company_id = ?
                          AND goods_receipt_id = ?
                          AND purchase_order_id = ?
                          AND status = 'DRAFT'
                          AND currency_code = 'EUR'
                        """,
                        invoices[i], TENANT, COMPANY,
                        receipts[i], orderId[0]
                ));

                equalsAmount(
                        subtotals[i],
                        amount("""
                            SELECT subtotal_amount
                            FROM supplier_invoice
                            WHERE id = ?
                            """, invoices[i])
                );

                equalsAmount(
                        taxes[i],
                        amount("""
                            SELECT tax_amount
                            FROM supplier_invoice
                            WHERE id = ?
                            """, invoices[i])
                );

                equalsAmount(
                        totals[i],
                        amount("""
                            SELECT total_amount
                            FROM supplier_invoice
                            WHERE id = ?
                            """, invoices[i])
                );

                assertEquals(1L, value("""
                        SELECT COUNT(*)
                        FROM supplier_invoice_line
                        WHERE tenant_id = ?
                          AND supplier_invoice_id = ?
                        """,
                        TENANT, invoices[i]
                ));

                equalsAmount(
                        quantities[i],
                        amount("""
                            SELECT quantity
                            FROM supplier_invoice_line
                            WHERE tenant_id = ?
                              AND supplier_invoice_id = ?
                            """,
                            TENANT, invoices[i])
                );

                equalsAmount(
                        taxes[i],
                        amount("""
                            SELECT tax_amount
                            FROM supplier_invoice_line
                            WHERE tenant_id = ?
                              AND supplier_invoice_id = ?
                            """,
                            TENANT, invoices[i])
                );
            }

            assertEquals(3L, value("""
                    SELECT COUNT(DISTINCT invoice_number)
                    FROM supplier_invoice
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND purchase_order_id = ?
                    """,
                    TENANT, COMPANY, orderId[0]
            ));

            equalsAmount("144.00", amount("""
                    SELECT SUM(subtotal_amount)
                    FROM supplier_invoice
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND purchase_order_id = ?
                    """,
                    TENANT, COMPANY, orderId[0]
            ));

            equalsAmount("31.68", amount("""
                    SELECT SUM(tax_amount)
                    FROM supplier_invoice
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND purchase_order_id = ?
                    """,
                    TENANT, COMPANY, orderId[0]
            ));

            equalsAmount("175.68", amount("""
                    SELECT SUM(total_amount)
                    FROM supplier_invoice
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND purchase_order_id = ?
                    """,
                    TENANT, COMPANY, orderId[0]
            ));

            assertEquals(before + 3, sequence());

            System.out.println(
                    "APPLICATION THREE DRAFTS: 8 + 7 + 5 PZ OK"
            );
            System.out.println(
                    "APPLICATION THREE DRAFTS: 70.27 + 61.49 + 43.92 OK"
            );
            System.out.println(
                    "APPLICATION THREE DRAFTS: imponibile 144.00 IVA 31.68 totale 175.68"
            );
            System.out.println(
                    "APPLICATION THREE DRAFTS: tre numeri distinti e sequenza +3"
            );

            status.setRollbackOnly();
            return null;
        });

        assertEquals(before, sequence());

        assertEquals(0L, value("""
                SELECT COUNT(*) FROM supplier_invoice
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND purchase_order_id = ?
                """,
                TENANT, COMPANY, orderId[0]
        ));

        for (Long invoiceId : invoices) {
            assertNotNull(invoiceId);

            assertEquals(0L, value("""
                    SELECT COUNT(*)
                    FROM supplier_invoice_line
                    WHERE tenant_id = ?
                      AND supplier_invoice_id = ?
                    """,
                    TENANT, invoiceId
            ));
        }

        assertEquals(0L, value("""
                SELECT COUNT(*) FROM purchase_order
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND order_number = ?
                """,
                TENANT, COMPANY, orderNumber
        ));

        assertEquals(0L, value("""
                SELECT COUNT(*) FROM purchase_order_line
                WHERE tenant_id = ?
                  AND purchase_order_id = ?
                """,
                TENANT, orderId[0]
        ));

        assertEquals(0L, value("""
                SELECT COUNT(*) FROM goods_receipt
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND receipt_number LIKE ?
                """,
                TENANT, COMPANY, receiptPrefix + "-%"
        ));

        for (Long receiptId : receipts) {
            assertNotNull(receiptId);

            assertEquals(0L, value("""
                    SELECT COUNT(*)
                    FROM goods_receipt_line
                    WHERE tenant_id = ?
                      AND goods_receipt_id = ?
                    """,
                    TENANT, receiptId
            ));
        }

        verify(gate, times(9)).requireEnabled();

        System.out.println(
                "APPLICATION THREE DRAFTS: rollback tre fatture e righe OK"
        );
        System.out.println(
                "APPLICATION THREE DRAFTS: rollback numeratore OK"
        );
        System.out.println(
                "APPLICATION THREE DRAFTS: rollback fixture completo OK"
        );
        System.out.println(
                "APPLICATION THREE DRAFTS: nessuna fattura persistente"
        );
    }
}
