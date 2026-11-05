package com.gestiva.purchasing.receipt.service;

import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.support.AbstractMySqlIntegrationTest;
import com.gestiva.warehouse.inventory.movement.service.InventoryDocumentPostingService;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@TestPropertySource(properties = {
    "gestiva.purchasing.partial-goods-receipt.enabled=true"
})
class GoodsReceiptPartialDocumentRollbackIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long SUPPLIER = 1L;
    private static final Long WAREHOUSE = 1L;

    @Autowired
    private GoodsReceiptPartialPersistenceWriter writer;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager manager;

    @MockitoBean
    private InventoryDocumentPostingService posting;

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private void verifyDatabase() throws Exception {

        try (Connection connection =
                     jdbc.getDataSource().getConnection()) {

            assertEquals(
                    "gestiva_test",
                    connection.getCatalog()
            );

            String username =
                    connection.getMetaData().getUserName();

            assertTrue(
                    username.equals("gestiva_test_runner")
                    || username.startsWith("gestiva_test_runner@"),
                    "Utente MySQL non autorizzato."
            );
        }

        Integer suppliers = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM supplier
                WHERE id = ? AND tenant_id = ?
                """,
                Integer.class,
                SUPPLIER, TENANT
        );

        Integer warehouses = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM warehouse
                WHERE id = ?
                  AND tenant_id = ?
                  AND company_id = ?
                  AND active = 1
                """,
                Integer.class,
                WAREHOUSE, TENANT, COMPANY
        );

        assertEquals(1, suppliers);
        assertEquals(1, warehouses);
    }

    private long createTemporaryOrder(String number) {

        KeyHolder keys = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            var ps = connection.prepareStatement(
                    """
                    INSERT INTO purchase_order (
                        tenant_id,
                        company_id,
                        order_number,
                        order_date,
                        supplier_id,
                        status,
                        currency_code,
                        subtotal_amount,
                        tax_amount,
                        total_amount,
                        created_at,
                        updated_at
                    )
                    VALUES (
                        ?, ?, ?, CURRENT_DATE,
                        ?, 'CONFIRMED', 'EUR',
                        144.00, 31.68, 175.68,
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                    )
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setLong(1, TENANT);
            ps.setLong(2, COMPANY);
            ps.setString(3, number);
            ps.setLong(4, SUPPLIER);

            return ps;
        }, keys);

        assertNotNull(keys.getKey());
        return keys.getKey().longValue();
    }

    private long createTemporaryOrderLine(long orderId) {

        KeyHolder keys = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            var ps = connection.prepareStatement(
                    """
                    INSERT INTO purchase_order_line (
                        tenant_id,
                        purchase_order_id,
                        line_no,
                        item_id,
                        uom_id,
                        conversion_factor,
                        description,
                        quantity,
                        unit_price,
                        discount_pct,
                        tax_pct,
                        line_subtotal,
                        tax_amount,
                        line_total,
                        created_at,
                        updated_at
                    )
                    VALUES (
                        ?, ?, 1,
                        NULL, NULL, 1.000000,
                        'Fixture rollback ricevimento',
                        20.000, 7.20, 0.00, 22.00,
                        144.00, 31.68, 175.68,
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                    )
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setLong(1, TENANT);
            ps.setLong(2, orderId);

            return ps;
        }, keys);

        assertNotNull(keys.getKey());
        return keys.getKey().longValue();
    }

    private GoodsReceiptPartialDocumentAssembler.DocumentPlan
    preparePlan(long orderId, long orderLineId, String number) {

        GoodsReceipt header = new GoodsReceipt();

        header.setTenantId(TENANT);
        header.setCompanyId(COMPANY);
        header.setPurchaseOrderId(orderId);
        header.setSupplierId(SUPPLIER);
        header.setWarehouseId(WAREHOUSE);
        header.setReceiptNumber(number);
        header.setReceiptDate(LocalDate.of(2026, 11, 5));

        PurchaseOrderLine orderLine =
                mock(PurchaseOrderLine.class);

        when(orderLine.getId()).thenReturn(orderLineId);
        when(orderLine.getTenantId()).thenReturn(TENANT);
        when(orderLine.getPurchaseOrderId()).thenReturn(orderId);
        when(orderLine.getDescription())
                .thenReturn("Fixture rollback ricevimento");

        var planned =
                GoodsReceiptPartialFifoLineCalculator.calculate(
                        bd("20.000"),
                        bd("0.000"),
                        bd("8.000"),
                        bd("144.00")
                );

        return GoodsReceiptPartialDocumentAssembler.prepare(
                header,
                List.of(
                        new GoodsReceiptPartialDocumentAssembler.LineInput(
                                orderLine,
                                planned
                        )
                )
        );
    }

    private int count(String sql, Object... arguments) {

        Integer result = jdbc.queryForObject(
                sql,
                Integer.class,
                arguments
        );

        assertNotNull(result);
        return result;
    }

    @Test
    void realDocumentInsertsAreRolledBackAfterPostingFailure()
            throws Exception {

        verifyDatabase();

        String marker = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 16);

        String orderNumber = "PO-RB-" + marker;
        String receiptNumber = "GR-RB-" + marker;

        AtomicLong orderId = new AtomicLong();
        AtomicLong orderLineId = new AtomicLong();
        AtomicLong receiptId = new AtomicLong();

        TransactionTemplate tx = new TransactionTemplate(manager);

        tx.setIsolationLevel(
                TransactionDefinition.ISOLATION_READ_COMMITTED
        );
        tx.setReadOnly(false);

        doAnswer(invocation -> {

            // Il writer ha già eseguito save() e saveAll().
            // Forziamo ora gli INSERT effettivi su MySQL.
            entityManager.flush();

            long id = receiptId.get();

            assertTrue(id > 0);

            assertEquals(
                    1,
                    count(
                            """
                            SELECT COUNT(*)
                            FROM goods_receipt
                            WHERE id = ?
                              AND tenant_id = ?
                              AND company_id = ?
                            """,
                            id, TENANT, COMPANY
                    )
            );

            assertEquals(
                    1,
                    count(
                            """
                            SELECT COUNT(*)
                            FROM goods_receipt_line
                            WHERE goods_receipt_id = ?
                              AND tenant_id = ?
                            """,
                            id, TENANT
                    )
            );

            System.out.println(
                    "DOCUMENT ROLLBACK: testata e riga inserite su MySQL"
            );

            throw new IllegalStateException(
                    "Errore simulato durante il posting FIFO."
            );

        }).when(posting)
          .postPurchaseReceiptFromGoodsReceiptV2(
                  eq(TENANT), eq(COMPANY), anyLong()
          );

        assertThrows(
                IllegalStateException.class,
                () -> tx.execute(status -> {

                    long po = createTemporaryOrder(orderNumber);
                    long pol = createTemporaryOrderLine(po);

                    orderId.set(po);
                    orderLineId.set(pol);

                    var plan = preparePlan(
                            po, pol, receiptNumber
                    );

                    // L'ID della testata viene assegnato
                    // prima del posting.
                    // Lo catturiamo dalla chiamata al posting.
                    doAnswer(invocation -> {
                        receiptId.set(
                                invocation.getArgument(2, Long.class)
                        );

                        entityManager.flush();

                        assertEquals(
                                1,
                                count(
                                        """
                                        SELECT COUNT(*)
                                        FROM goods_receipt
                                        WHERE id = ?
                                        """,
                                        receiptId.get()
                                )
                        );

                        assertEquals(
                                1,
                                count(
                                        """
                                        SELECT COUNT(*)
                                        FROM goods_receipt_line
                                        WHERE goods_receipt_id = ?
                                        """,
                                        receiptId.get()
                                )
                        );

                        System.out.println(
                                "DOCUMENT ROLLBACK: INSERT reali verificati"
                        );

                        throw new IllegalStateException(
                                "Errore simulato dopo INSERT reali."
                        );
                    }).when(posting)
                      .postPurchaseReceiptFromGoodsReceiptV2(
                              eq(TENANT), eq(COMPANY), anyLong()
                      );

                    writer.write(plan);

                    fail("Il posting doveva interrompere il writer.");
                    return null;
                })
        );

        assertTrue(orderId.get() > 0);
        assertTrue(orderLineId.get() > 0);
        assertTrue(receiptId.get() > 0);

        // Qui la transazione è terminata: tutte le verifiche
        // vengono effettuate tramite nuove letture.
        assertEquals(
                0,
                count(
                        "SELECT COUNT(*) FROM purchase_order WHERE id = ?",
                        orderId.get()
                )
        );

        assertEquals(
                0,
                count(
                        "SELECT COUNT(*) FROM purchase_order_line WHERE id = ?",
                        orderLineId.get()
                )
        );

        assertEquals(
                0,
                count(
                        "SELECT COUNT(*) FROM goods_receipt WHERE id = ?",
                        receiptId.get()
                )
        );

        assertEquals(
                0,
                count(
                        """
                        SELECT COUNT(*)
                        FROM goods_receipt_line
                        WHERE goods_receipt_id = ?
                        """,
                        receiptId.get()
                )
        );

        verify(posting, times(1))
                .postPurchaseReceiptFromGoodsReceiptV2(
                        TENANT, COMPANY, receiptId.get()
                );

        System.out.println(
                "DOCUMENT ROLLBACK: ordine temporaneo annullato"
        );
        System.out.println(
                "DOCUMENT ROLLBACK: riga ordine temporanea annullata"
        );
        System.out.println(
                "DOCUMENT ROLLBACK: testata GR annullata"
        );
        System.out.println(
                "DOCUMENT ROLLBACK: riga GR annullata"
        );
        System.out.println(
                "DOCUMENT ROLLBACK: nessun movimento FIFO creato"
        );
    }
}
