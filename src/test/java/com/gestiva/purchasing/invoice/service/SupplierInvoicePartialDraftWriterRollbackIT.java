package com.gestiva.purchasing.invoice.service;

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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialDraftWriterRollbackIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;

    @Autowired
    private SupplierInvoicePartialDraftPersistenceWriter writer;

    @Autowired
    private PlatformTransactionManager manager;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private SupplierInvoicePartialGenerationGuard gate;

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private long count(String sql, Object... args) {
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

    private SupplierInvoicePartialDraftPlan plan(
            Long orderId,
            Long receiptId,
            Long supplierId) {

        var line = new SupplierInvoicePartialDraftPlan.Line(
                1,
                101L,
                1L,
                "Riga sintetica collaudo writer",
                9L,
                "BOX",
                "PZ",
                bd("10.000000"),
                bd("0.700"),
                bd("72.00"),
                bd("0.00"),
                bd("22.00"),
                bd("50.40"),
                bd("11.09"),
                bd("61.49")
        );

        return new SupplierInvoicePartialDraftPlan(
                TENANT,
                COMPANY,
                receiptId,
                orderId,
                supplierId,
                "EUR",
                List.of(line),
                bd("50.40"),
                bd("11.09"),
                bd("61.49")
        );
    }

    @Test
    void realHeaderAndLineAreRolledBack()
            throws Exception {

        verifyDatabase();

        // Ordine esistente: usiamo solo la sua identità.
        // Nessun ordine preesistente viene modificato.
        Map<String, Object> reference = jdbc.queryForMap("""
                SELECT id, supplier_id
                FROM purchase_order
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND status = 'CONFIRMED'
                  AND supplier_id IS NOT NULL
                ORDER BY id ASC
                LIMIT 1
                """, TENANT, COMPANY);

        Long orderId =
                ((Number) reference.get("id")).longValue();

        Long supplierId =
                ((Number) reference.get("supplier_id")).longValue();

        String suffix = UUID.randomUUID()
                .toString().substring(0, 12);

        String receiptNumber = "IT2051-GR-" + suffix;
        String invoiceNumber = "IT2051-SI-" + suffix;

        AtomicReference<Long> invoiceId =
                new AtomicReference<>();

        TransactionTemplate tx = new TransactionTemplate(manager);
        tx.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );
        tx.setIsolationLevel(
                TransactionDefinition.ISOLATION_READ_COMMITTED
        );
        tx.setReadOnly(false);

        tx.execute(status -> {
            // Tutte le scritture sono interne alla
            // medesima transazione destinata al rollback.
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
                    TENANT,
                    COMPANY,
                    receiptNumber,
                    Date.valueOf(LocalDate.now()),
                    orderId,
                    supplierId
            );

            Long receiptId = jdbc.queryForObject("""
                    SELECT id
                    FROM goods_receipt
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND receipt_number = ?
                    """,
                    Long.class,
                    TENANT,
                    COMPANY,
                    receiptNumber
            );

            assertNotNull(receiptId);

            Long createdId = writer.write(
                    plan(orderId, receiptId, supplierId),
                    invoiceNumber,
                    LocalDate.now()
            );

            assertNotNull(createdId);
            assertTrue(createdId > 0);
            invoiceId.set(createdId);

            // Forza gli INSERT JPA sul DB.
            entityManager.flush();

            assertEquals(1L, count("""
                    SELECT COUNT(*)
                    FROM supplier_invoice
                    WHERE id = ?
                      AND tenant_id = ?
                      AND company_id = ?
                      AND invoice_number = ?
                      AND status = 'DRAFT'
                      AND subtotal_amount = 50.40
                      AND tax_amount = 11.09
                      AND total_amount = 61.49
                    """,
                    createdId, TENANT, COMPANY, invoiceNumber
            ));

            assertEquals(1L, count("""
                    SELECT COUNT(*)
                    FROM supplier_invoice_line
                    WHERE supplier_invoice_id = ?
                      AND tenant_id = ?
                      AND quantity = 0.700
                      AND line_subtotal = 50.40
                      AND tax_amount = 11.09
                      AND line_total = 61.49
                    """,
                    createdId, TENANT
            ));

            System.out.println(
                    "WRITER MYSQL: testata DRAFT inserita"
            );
            System.out.println(
                    "WRITER MYSQL: riga con importi allocati inserita"
            );

            status.setRollbackOnly();
            return null;
        });

        assertNotNull(invoiceId.get());

        // Letture eseguite dopo la chiusura della TX.
        assertEquals(0L, count("""
                SELECT COUNT(*)
                FROM supplier_invoice
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND invoice_number = ?
                """,
                TENANT, COMPANY, invoiceNumber
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

        verify(gate, times(1)).requireEnabled();

        System.out.println(
                "WRITER MYSQL: rollback testata OK"
        );
        System.out.println(
                "WRITER MYSQL: rollback righe OK"
        );
        System.out.println(
                "WRITER MYSQL: rollback fixture OK"
        );
        System.out.println(
                "WRITER MYSQL: nessuna fattura persistente"
        );
    }
}
