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
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialDraftCreationRealRollbackIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long SUPPLIER = 1L;
    private static final Long ITEM = 1L;
    private static final Long BOX = 9L;
    private static final Long PIECE = 1L;

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

    private long count(String sql, Object... args) {
        Long value = jdbc.queryForObject(
                sql, Long.class, args
        );
        assertNotNull(value);
        return value;
    }

    private Long id(String sql, Object... args) {
        Long value = jdbc.queryForObject(
                sql, Long.class, args
        );
        assertNotNull(value);
        return value;
    }

    private long sequence() {
        return id("""
                SELECT next_number
                FROM document_sequence
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND document_type = ?
                """,
                TENANT,
                COMPANY,
                DocumentSequenceService.SUPPLIER_INVOICE
        );
    }

    private void verifyDatabase() throws Exception {
        try (Connection c =
                     jdbc.getDataSource().getConnection()) {

            assertEquals("gestiva_test", c.getCatalog());

            String user = c.getMetaData().getUserName();

            assertTrue(
                    user.equals("gestiva_test_runner")
                    || user.startsWith("gestiva_test_runner@")
            );
        }

        assertEquals(1L, count("""
                SELECT COUNT(*)
                FROM unit_of_measure
                WHERE tenant_id = ?
                  AND id = ?
                  AND code = 'BOX'
                """, TENANT, BOX));

        assertEquals(1L, count("""
                SELECT COUNT(*)
                FROM unit_of_measure
                WHERE tenant_id = ?
                  AND id = ?
                  AND code = 'PZ'
                """, TENANT, PIECE));

        assertEquals(1L, count("""
                SELECT COUNT(*)
                FROM item
                WHERE tenant_id = ?
                  AND id = ?
                  AND item_type = 'PRODUCT'
                  AND base_uom_id = ?
                """, TENANT, ITEM, PIECE));

        assertEquals(1L, count("""
                SELECT COUNT(*)
                FROM document_sequence
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND document_type = ?
                """,
                TENANT,
                COMPANY,
                DocumentSequenceService.SUPPLIER_INVOICE
        ));
    }

    @Test
    void realCreationAndSequenceRollbackTogether()
            throws Exception {

        verifyDatabase();

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        String suffix = UUID.randomUUID()
                .toString().substring(0, 12);

        String orderNumber = "IT2058-PO-" + suffix;
        String receiptNumber = "IT2058-GR-" + suffix;

        long sequenceBefore = sequence();

        AtomicReference<Long> purchaseOrderId =
                new AtomicReference<>();

        AtomicReference<Long> receiptId =
                new AtomicReference<>();

        AtomicReference<Long> invoiceId =
                new AtomicReference<>();

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

            // Fixture: ordine temporaneo confermato.
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

            Long orderId = id("""
                    SELECT id
                    FROM purchase_order
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND order_number = ?
                    """,
                    TENANT, COMPANY, orderNumber
            );

            purchaseOrderId.set(orderId);

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
                        'Articolo collaudo fattura parziale',
                        2.000, 10.000000,
                        72.00, 0.00, 22.00,
                        144.00, 31.68, 175.68,
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                    )
                    """,
                    TENANT, orderId, ITEM, BOX
            );

            Long orderLineId = id("""
                    SELECT id
                    FROM purchase_order_line
                    WHERE tenant_id = ?
                      AND purchase_order_id = ?
                      AND line_no = 1
                    """,
                    TENANT, orderId
            );

            // Fixture: primo ricevimento di 8 PZ.
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
                    TENANT, COMPANY, receiptNumber,
                    Date.valueOf(LocalDate.now()),
                    orderId, SUPPLIER
            );

            Long grId = id("""
                    SELECT id
                    FROM goods_receipt
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND receipt_number = ?
                    """,
                    TENANT, COMPANY, receiptNumber
            );

            receiptId.set(grId);

            jdbc.update("""
                    INSERT INTO goods_receipt_line (
                        tenant_id, goods_receipt_id, line_no,
                        purchase_order_line_id, item_id,
                        description, quantity_received,
                        created_at, updated_at
                    ) VALUES (
                        ?, ?, 1, ?, ?,
                        'Ricevimento parziale collaudo',
                        8.000,
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                    )
                    """,
                    TENANT, grId, orderLineId, ITEM
            );

            // Coordinatore reale: acquisisce il lock,
            // calcola il piano, incrementa il numeratore
            // e salva testata e riga DRAFT.
            Long created = creation.create(TENANT, grId);

            assertNotNull(created);
            assertTrue(created > 0);
            invoiceId.set(created);

            // Materializzazione effettiva su MySQL.
            entityManager.flush();

            assertEquals(1L, count("""
                    SELECT COUNT(*)
                    FROM supplier_invoice
                    WHERE id = ?
                      AND tenant_id = ?
                      AND company_id = ?
                      AND goods_receipt_id = ?
                      AND purchase_order_id = ?
                      AND supplier_id = ?
                      AND status = 'DRAFT'
                      AND currency_code = 'EUR'
                      AND subtotal_amount = 57.60
                      AND tax_amount = 12.67
                      AND total_amount = 70.27
                    """,
                    created, TENANT, COMPANY,
                    grId, orderId, SUPPLIER
            ));

            assertEquals(1L, count("""
                    SELECT COUNT(*)
                    FROM supplier_invoice_line
                    WHERE tenant_id = ?
                      AND supplier_invoice_id = ?
                      AND item_id = ?
                      AND quantity = 0.800
                      AND conversion_factor = 10.000000
                      AND line_subtotal = 57.60
                      AND tax_amount = 12.67
                      AND line_total = 70.27
                    """,
                    TENANT, created, ITEM
            ));

            assertEquals(
                    sequenceBefore + 1,
                    sequence(),
                    "Il numeratore non è avanzato nella TX."
            );

            System.out.println(
                    "CREATION REAL: DRAFT 8 PZ = 70.27 EUR OK"
            );
            System.out.println(
                    "CREATION REAL: riga commerciale 0.800 BOX OK"
            );
            System.out.println(
                    "CREATION REAL: numeratore incrementato nella TX"
            );

            // Annulla insieme fixture, fattura e numeratore.
            status.setRollbackOnly();
            return null;
        });

        assertNotNull(purchaseOrderId.get());
        assertNotNull(receiptId.get());
        assertNotNull(invoiceId.get());

        assertEquals(sequenceBefore, sequence());

        assertEquals(0L, count("""
                SELECT COUNT(*)
                FROM supplier_invoice
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND id = ?
                """,
                TENANT, COMPANY, invoiceId.get()
        ));

        assertEquals(0L, count("""
                SELECT COUNT(*)
                FROM supplier_invoice_line
                WHERE tenant_id = ?
                  AND supplier_invoice_id = ?
                """,
                TENANT, invoiceId.get()
        ));

        assertEquals(0L, count("""
                SELECT COUNT(*)
                FROM goods_receipt
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND receipt_number = ?
                """,
                TENANT, COMPANY, receiptNumber
        ));

        assertEquals(0L, count("""
                SELECT COUNT(*)
                FROM goods_receipt_line
                WHERE tenant_id = ?
                  AND goods_receipt_id = ?
                """,
                TENANT, receiptId.get()
        ));

        assertEquals(0L, count("""
                SELECT COUNT(*)
                FROM purchase_order
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND order_number = ?
                """,
                TENANT, COMPANY, orderNumber
        ));

        assertEquals(0L, count("""
                SELECT COUNT(*)
                FROM purchase_order_line
                WHERE tenant_id = ?
                  AND purchase_order_id = ?
                """,
                TENANT, purchaseOrderId.get()
        ));

        verify(gate, times(2)).requireEnabled();

        System.out.println(
                "CREATION REAL: rollback fattura e righe OK"
        );
        System.out.println(
                "CREATION REAL: rollback numeratore OK"
        );
        System.out.println(
                "CREATION REAL: rollback fixture OK"
        );
        System.out.println(
                "CREATION REAL: nessuna fattura persistente"
        );
    }
}
