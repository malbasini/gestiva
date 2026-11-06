package com.gestiva.purchasing.invoice.service;

import com.gestiva.purchasing.invoice.repository.SupplierInvoiceLineRepository;
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
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialDraftWriterFailureRollbackIT
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

    // Soltanto le righe sono simulate.
    // La testata è salvata con il repository JPA reale.
    @MockitoBean
    private SupplierInvoiceLineRepository lines;

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
                "Riga simulazione errore persistenza",
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
    void linePersistenceFailureRollsBackRealHeader()
            throws Exception {

        verifyDatabase();

        Map<String, Object> reference =
                jdbc.queryForMap("""
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

        String receiptNumber = "IT2052-GR-" + suffix;
        String invoiceNumber = "IT2052-SI-" + suffix;

        AtomicBoolean failureReached = new AtomicBoolean();

        // L'errore scatta DOPO il salvataggio reale
        // della testata ed il flush JPA.
        doAnswer(invocation -> {

            entityManager.flush();

            assertEquals(1L, count("""
                    SELECT COUNT(*)
                    FROM supplier_invoice
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND invoice_number = ?
                      AND status = 'DRAFT'
                      AND subtotal_amount = 50.40
                      AND tax_amount = 11.09
                      AND total_amount = 61.49
                    """,
                    TENANT, COMPANY, invoiceNumber
            ));

            failureReached.set(true);

            System.out.println(
                    "WRITER FAILURE: testata reale presente prima dell'errore"
            );

            throw new IllegalStateException(
                    "IT2052: errore simulato durante il salvataggio righe"
            );

        }).when(lines).saveAll(any());

        TransactionTemplate tx =
                new TransactionTemplate(manager);

        tx.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );
        tx.setIsolationLevel(
                TransactionDefinition.ISOLATION_READ_COMMITTED
        );
        tx.setReadOnly(false);

        // Nessun setRollbackOnly manuale.
        // L'eccezione deve far annullare la transazione.
        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> tx.execute(status -> {

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
                            TENANT, COMPANY, receiptNumber
                    );

                    assertNotNull(receiptId);

                    return writer.write(
                            plan(orderId, receiptId, supplierId),
                            invoiceNumber,
                            LocalDate.now()
                    );
                })
        );

        assertEquals(
                "IT2052: errore simulato durante il salvataggio righe",
                failure.getMessage()
        );

        assertTrue(
                failureReached.get(),
                "Il guasto simulato non è stato raggiunto."
        );

        // Le letture seguenti avvengono dopo
        // la conclusione della transazione fallita.
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
                FROM goods_receipt
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND receipt_number = ?
                """,
                TENANT, COMPANY, receiptNumber
        ));

        assertEquals(0L, count("""
                SELECT COUNT(*)
                FROM supplier_invoice_line sil
                JOIN supplier_invoice si
                  ON si.id = sil.supplier_invoice_id
                WHERE si.tenant_id = ?
                  AND si.company_id = ?
                  AND si.invoice_number = ?
                """,
                TENANT, COMPANY, invoiceNumber
        ));

        verify(gate, times(1)).requireEnabled();
        verify(lines, times(1)).saveAll(any());

        System.out.println(
                "WRITER FAILURE: eccezione propagata correttamente"
        );
        System.out.println(
                "WRITER FAILURE: rollback automatico testata OK"
        );
        System.out.println(
                "WRITER FAILURE: rollback fixture OK"
        );
        System.out.println(
                "WRITER FAILURE: nessuna fattura persistente"
        );
    }
}
