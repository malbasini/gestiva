package com.gestiva.purchasing.invoice.service;

import com.gestiva.purchasing.invoice.repository.SupplierInvoiceLineRepository;
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

import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialDraftCombinedFailureRollbackIT
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

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private SupplierInvoicePartialGenerationGuard gate;

    @MockitoBean
    private CompanyContext companyContext;

    // Solo questa dipendenza viene simulata
    // per provocare il guasto dopo l'INSERT testata.
    @MockitoBean
    private SupplierInvoiceLineRepository invoiceLines;

    private long number(String sql, Object... args) {
        Long result = jdbc.queryForObject(sql, Long.class, args);
        assertNotNull(result);
        return result;
    }

    private long sequence() {
        return number("""
                SELECT next_number
                FROM document_sequence
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

        assertEquals(1L, number("""
                SELECT COUNT(*)
                FROM document_sequence
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND document_type = ?
                """,
                TENANT, COMPANY,
                DocumentSequenceService.SUPPLIER_INVOICE
        ));

        assertEquals(1L, number("""
                SELECT COUNT(*)
                FROM item
                WHERE tenant_id = ?
                  AND id = ?
                  AND item_type = 'PRODUCT'
                  AND base_uom_id = 1
                """, TENANT, ITEM));

        assertEquals(1L, number("""
                SELECT COUNT(*)
                FROM unit_of_measure
                WHERE tenant_id = ?
                  AND id = ?
                  AND code = 'BOX'
                """, TENANT, BOX));
    }

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

    @Test
    void lineFailureRollsBackHeaderSequenceAndFixture()
            throws Exception {

        verifyDatabase();

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        String suffix = UUID.randomUUID()
                .toString().substring(0, 12);

        String orderNumber = "IT2067-PO-" + suffix;
        String receiptNumber = "IT2067-GR-" + suffix;

        long before = sequence();

        AtomicReference<Long> orderId = new AtomicReference<>();
        AtomicReference<Long> receiptId = new AtomicReference<>();
        AtomicReference<Long> createdInvoiceId =
                new AtomicReference<>();

        AtomicBoolean failureReached = new AtomicBoolean();

        doAnswer(invocation -> {

            // La testata deve essere già stata inserita.
            // Il flush rende osservabile anche l'UPDATE
            // del numeratore reale nella stessa TX.
            entityManager.flush();

            Long grId = receiptId.get();
            assertNotNull(grId);

            assertEquals(1L, number("""
                    SELECT COUNT(*)
                    FROM supplier_invoice
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND goods_receipt_id = ?
                      AND status = 'DRAFT'
                      AND subtotal_amount = 57.60
                      AND tax_amount = 12.67
                      AND total_amount = 70.27
                    """,
                    TENANT, COMPANY, grId
            ));

            Long invoiceId = jdbc.queryForObject("""
                    SELECT id
                    FROM supplier_invoice
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND goods_receipt_id = ?
                    """,
                    Long.class,
                    TENANT, COMPANY, grId
            );

            assertNotNull(invoiceId);
            createdInvoiceId.set(invoiceId);

            assertEquals(
                    before + 1,
                    sequence(),
                    "Numeratore non incrementato nella TX."
            );

            assertEquals(0L, number("""
                    SELECT COUNT(*)
                    FROM supplier_invoice_line
                    WHERE tenant_id = ?
                      AND supplier_invoice_id = ?
                    """,
                    TENANT, invoiceId
            ));

            failureReached.set(true);

            System.out.println(
                    "COMBINED FAILURE: testata reale inserita"
            );
            System.out.println(
                    "COMBINED FAILURE: numeratore reale +1"
            );
            System.out.println(
                    "COMBINED FAILURE: errore sulle righe simulato"
            );

            throw new IllegalStateException(
                    "IT2067: errore dopo testata e numeratore"
            );

        }).when(invoiceLines).saveAll(any());

        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> transaction().execute(status -> {

                    jdbc.update("""
                            INSERT INTO purchase_order (
                                tenant_id, company_id,
                                order_number, order_date,
                                supplier_id, status,
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
                            Date.valueOf(LocalDate.now()),
                            SUPPLIER
                    );

                    Long poId = jdbc.queryForObject("""
                            SELECT id
                            FROM purchase_order
                            WHERE tenant_id = ?
                              AND company_id = ?
                              AND order_number = ?
                            """,
                            Long.class,
                            TENANT, COMPANY, orderNumber
                    );

                    assertNotNull(poId);
                    orderId.set(poId);

                    jdbc.update("""
                            INSERT INTO purchase_order_line (
                                tenant_id, purchase_order_id,
                                line_no, item_id, uom_id,
                                description, quantity,
                                conversion_factor, unit_price,
                                discount_pct, tax_pct,
                                line_subtotal, tax_amount,
                                line_total, created_at, updated_at
                            ) VALUES (
                                ?, ?, 1, ?, ?,
                                'Fixture collaudo combinato',
                                2.000, 10.000000, 72.00,
                                0.00, 22.00,
                                144.00, 31.68, 175.68,
                                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                            )
                            """,
                            TENANT, poId, ITEM, BOX
                    );

                    Long poLineId = jdbc.queryForObject("""
                            SELECT id
                            FROM purchase_order_line
                            WHERE tenant_id = ?
                              AND purchase_order_id = ?
                              AND line_no = 1
                            """,
                            Long.class,
                            TENANT, poId
                    );

                    assertNotNull(poLineId);

                    jdbc.update("""
                            INSERT INTO goods_receipt (
                                tenant_id, company_id,
                                receipt_number, receipt_date,
                                purchase_order_id, supplier_id,
                                created_at, updated_at
                            ) VALUES (
                                ?, ?, ?, ?, ?, ?,
                                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                            )
                            """,
                            TENANT, COMPANY, receiptNumber,
                            Date.valueOf(LocalDate.now()),
                            poId, SUPPLIER
                    );

                    Long grId = jdbc.queryForObject("""
                            SELECT id
                            FROM goods_receipt
                            WHERE tenant_id = ?
                              AND company_id = ?
                              AND receipt_number = ?
                            """,
                            Long.class,
                            TENANT, COMPANY, receiptNumber
                    );

                    assertNotNull(grId);
                    receiptId.set(grId);

                    jdbc.update("""
                            INSERT INTO goods_receipt_line (
                                tenant_id, goods_receipt_id,
                                line_no, purchase_order_line_id,
                                item_id, description,
                                quantity_received,
                                created_at, updated_at
                            ) VALUES (
                                ?, ?, 1, ?, ?,
                                'Ricevimento test combinato',
                                8.000,
                                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                            )
                            """,
                            TENANT, grId, poLineId, ITEM
                    );

                    // Non viene chiamato setRollbackOnly().
                    // L'eccezione deve annullare l'intera TX.
                    return creation.create(TENANT, grId);
                })
        );

        assertEquals(
                "IT2067: errore dopo testata e numeratore",
                failure.getMessage()
        );

        assertTrue(failureReached.get());
        assertNotNull(orderId.get());
        assertNotNull(receiptId.get());
        assertNotNull(createdInvoiceId.get());

        assertEquals(
                before,
                sequence(),
                "ROLLBACK FALLITO: numeratore modificato."
        );

        assertEquals(0L, number("""
                SELECT COUNT(*)
                FROM supplier_invoice
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND id = ?
                """,
                TENANT, COMPANY, createdInvoiceId.get()
        ));

        assertEquals(0L, number("""
                SELECT COUNT(*)
                FROM supplier_invoice_line
                WHERE tenant_id = ?
                  AND supplier_invoice_id = ?
                """,
                TENANT, createdInvoiceId.get()
        ));

        assertEquals(0L, number("""
                SELECT COUNT(*)
                FROM goods_receipt
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND receipt_number = ?
                """,
                TENANT, COMPANY, receiptNumber
        ));

        assertEquals(0L, number("""
                SELECT COUNT(*)
                FROM goods_receipt_line
                WHERE tenant_id = ?
                  AND goods_receipt_id = ?
                """,
                TENANT, receiptId.get()
        ));

        assertEquals(0L, number("""
                SELECT COUNT(*)
                FROM purchase_order
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND order_number = ?
                """,
                TENANT, COMPANY, orderNumber
        ));

        assertEquals(0L, number("""
                SELECT COUNT(*)
                FROM purchase_order_line
                WHERE tenant_id = ?
                  AND purchase_order_id = ?
                """,
                TENANT, orderId.get()
        ));

        verify(gate, times(2)).requireEnabled();
        verify(invoiceLines, times(1)).saveAll(any());

        System.out.println(
                "COMBINED FAILURE: eccezione propagata OK"
        );
        System.out.println(
                "COMBINED FAILURE: rollback testata OK"
        );
        System.out.println(
                "COMBINED FAILURE: rollback numeratore OK"
        );
        System.out.println(
                "COMBINED FAILURE: rollback fixture OK"
        );
        System.out.println(
                "COMBINED FAILURE: nessuna fattura persistente"
        );
    }
}
