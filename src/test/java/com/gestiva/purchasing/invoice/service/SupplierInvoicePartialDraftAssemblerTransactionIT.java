package com.gestiva.purchasing.invoice.service;

import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;
import com.gestiva.support.AbstractMySqlIntegrationTest;
import com.gestiva.warehouse.item.entity.Item;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.uom.entity.UnitOfMeasure;
import com.gestiva.warehouse.uom.repository.UnitOfMeasureRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialDraftAssemblerTransactionIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long RECEIPT = 301L;
    private static final Long ORDER = 100L;
    private static final Long ORDER_LINE = 101L;
    private static final Long ITEM = 50L;
    private static final Long UOM = 5L;

    @Autowired
    private SupplierInvoicePartialDraftAssembler assembler;

    @Autowired
    private PlatformTransactionManager manager;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private PurchaseOrderLineRepository orderLines;

    @MockitoBean
    private UnitOfMeasureRepository units;

    @MockitoBean
    private ItemRepository items;

    @MockitoBean
    private SupplierInvoicePartialLinePlanner planner;

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private void verifyTestDatabase() throws Exception {
        try (Connection connection =
                     jdbc.getDataSource().getConnection()) {

            assertEquals("gestiva_test", connection.getCatalog());

            String username =
                    connection.getMetaData().getUserName();

            assertTrue(
                    username.equals("gestiva_test_runner")
                    || username.startsWith("gestiva_test_runner@")
            );
        }
    }

    private TransactionTemplate transaction() {
        TransactionTemplate tx = new TransactionTemplate(manager);
        tx.setReadOnly(false);
        tx.setIsolationLevel(
                TransactionDefinition.ISOLATION_READ_COMMITTED
        );
        return tx;
    }

    private SupplierInvoicePartialPreparationService.Preparation
            preparation() {

        GoodsReceipt receipt = mock(GoodsReceipt.class);
        PurchaseOrder order = mock(PurchaseOrder.class);

        GoodsReceiptLine receiptLine =
                mock(GoodsReceiptLine.class);

        when(receipt.getId()).thenReturn(RECEIPT);
        when(receipt.getPurchaseOrderId()).thenReturn(ORDER);
        when(receipt.getSupplierId()).thenReturn(40L);

        when(order.getId()).thenReturn(ORDER);
        when(order.getSupplierId()).thenReturn(40L);
        when(order.getCurrencyCode()).thenReturn("EUR");

        when(receiptLine.getPurchaseOrderLineId())
                .thenReturn(ORDER_LINE);
        when(receiptLine.getItemId()).thenReturn(ITEM);
        when(receiptLine.getQuantityReceived())
                .thenReturn(bd("8.000"));

        return new SupplierInvoicePartialPreparationService.Preparation(
                TENANT,
                COMPANY,
                receipt,
                order,
                List.of(receiptLine)
        );
    }

    @Test
    void springProxyRejectsMissingTransaction()
            throws Exception {

        verifyTestDatabase();

        assertFalse(
                TransactionSynchronizationManager
                        .isActualTransactionActive()
        );

        var prepared = preparation();

        assertThrows(
                IllegalTransactionStateException.class,
                () -> assembler.assemble(prepared)
        );

        verifyNoInteractions(
                orderLines, units, items, planner
        );
    }

    @Test
    void assemblySharesRealSpringTransaction()
            throws Exception {

        verifyTestDatabase();

        var prepared = preparation();

        PurchaseOrderLine orderLine =
                mock(PurchaseOrderLine.class);

        when(orderLine.getItemId()).thenReturn(ITEM);
        when(orderLine.getUomId()).thenReturn(UOM);
        when(orderLine.getQuantity()).thenReturn(bd("2.000"));
        when(orderLine.getConversionFactor())
                .thenReturn(bd("10.000000"));
        when(orderLine.getDescription())
                .thenReturn("Articolo test");
        when(orderLine.getUnitPrice())
                .thenReturn(bd("72.00"));
        when(orderLine.getDiscountPct())
                .thenReturn(bd("0.00"));
        when(orderLine.getTaxPct())
                .thenReturn(bd("22.00"));
        when(orderLine.getLineSubtotal())
                .thenReturn(bd("144.00"));
        when(orderLine.getTaxAmount())
                .thenReturn(bd("31.68"));
        when(orderLine.getLineTotal())
                .thenReturn(bd("175.68"));

        UnitOfMeasure commercialUom =
                mock(UnitOfMeasure.class);
        UnitOfMeasure baseUom =
                mock(UnitOfMeasure.class);
        Item item = mock(Item.class);

        when(commercialUom.getCode()).thenReturn("BOX");
        when(baseUom.getCode()).thenReturn("PZ");
        when(item.getBaseUom()).thenReturn(baseUom);

        when(units.findByTenantIdAndId(TENANT, UOM))
                .thenReturn(Optional.of(commercialUom));

        when(items.findByTenantIdAndId(TENANT, ITEM))
                .thenReturn(Optional.of(item));

        AtomicReference<Long> atOrderLine =
                new AtomicReference<>();

        AtomicReference<Long> atPlanner =
                new AtomicReference<>();

        when(orderLines.findByTenantIdAndPurchaseOrderIdAndId(
                TENANT, ORDER, ORDER_LINE
        )).thenAnswer(invocation -> {

            assertTrue(
                    TransactionSynchronizationManager
                            .isActualTransactionActive()
            );

            assertFalse(
                    TransactionSynchronizationManager
                            .isCurrentTransactionReadOnly()
            );

            atOrderLine.set(
                    jdbc.queryForObject(
                            "SELECT CONNECTION_ID()", Long.class
                    )
            );

            return Optional.of(orderLine);
        });

        when(planner.plan(
                TENANT, COMPANY, ORDER, ORDER_LINE, RECEIPT,
                bd("8.000"), bd("20.000"),
                bd("10.000000"),
                bd("144.00"), bd("31.68"), bd("175.68")
        )).thenAnswer(invocation -> {

            assertTrue(
                    TransactionSynchronizationManager
                            .isActualTransactionActive()
            );

            atPlanner.set(
                    jdbc.queryForObject(
                            "SELECT CONNECTION_ID()", Long.class
                    )
            );

            return new SupplierInvoicePartialBillabilityValidator.BillableLine(
                    bd("0.800"),
                    bd("57.60"),
                    bd("12.67"),
                    bd("70.27")
            );
        });

        transaction().execute(status -> {

            var plan = assembler.assemble(prepared);

            assertEquals(1, plan.lines().size());
            assertEquals(0,
                    bd("0.800").compareTo(
                            plan.lines().get(0).commercialQuantity()));
            assertEquals(0,
                    bd("57.60").compareTo(plan.subtotal()));
            assertEquals(0,
                    bd("12.67").compareTo(plan.tax()));
            assertEquals(0,
                    bd("70.27").compareTo(plan.total()));

            status.setRollbackOnly();
            return null;
        });

        assertNotNull(atOrderLine.get());
        assertNotNull(atPlanner.get());

        assertEquals(
                atOrderLine.get(),
                atPlanner.get(),
                "Lettura ordine e planner devono "
                + "condividere la connessione JDBC."
        );

        var calls = inOrder(
                orderLines, units, items, planner
        );

        calls.verify(orderLines)
                .findByTenantIdAndPurchaseOrderIdAndId(
                        TENANT, ORDER, ORDER_LINE
                );

        calls.verify(units)
                .findByTenantIdAndId(TENANT, UOM);

        calls.verify(items)
                .findByTenantIdAndId(TENANT, ITEM);

        calls.verify(planner).plan(
                TENANT, COMPANY, ORDER, ORDER_LINE, RECEIPT,
                bd("8.000"), bd("20.000"),
                bd("10.000000"),
                bd("144.00"), bd("31.68"), bd("175.68")
        );

        System.out.println(
                "ASSEMBLATORE MYSQL: transazione Spring attiva"
        );
        System.out.println(
                "ASSEMBLATORE MYSQL: connessione JDBC condivisa"
        );
        System.out.println(
                "ASSEMBLATORE MYSQL: piano 0.800 BOX = 70.27 EUR"
        );
        System.out.println(
                "ASSEMBLATORE MYSQL: nessuna scrittura"
        );
    }
}
