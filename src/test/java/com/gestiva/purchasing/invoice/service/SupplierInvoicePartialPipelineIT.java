package com.gestiva.purchasing.invoice.service;

import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.support.AbstractMySqlIntegrationTest;
import com.gestiva.warehouse.item.entity.Item;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.uom.entity.UnitOfMeasure;
import com.gestiva.warehouse.uom.repository.UnitOfMeasureRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialPipelineIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long ORDER = 100L;
    private static final Long ORDER_LINE = 101L;
    private static final Long ITEM = 50L;
    private static final Long UOM = 5L;
    private static final Long SUPPLIER = 40L;

    @Autowired
    private SupplierInvoicePartialPreparationService preparation;

    @Autowired
    private SupplierInvoicePartialDraftAssembler assembler;

    @Autowired
    private PlatformTransactionManager manager;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private SupplierInvoicePartialGenerationGuard guard;

    @MockitoBean
    private CompanyContext companies;

    @MockitoBean
    private GoodsReceiptRepository receipts;

    @MockitoBean
    private GoodsReceiptLineRepository receiptLines;

    @MockitoBean
    private PurchaseOrderRepository orders;

    @MockitoBean
    private PurchaseOrderLineRepository orderLines;

    @MockitoBean
    private SupplierInvoiceRepository invoices;

    @MockitoBean
    private UnitOfMeasureRepository units;

    @MockitoBean
    private ItemRepository items;

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private void verifyDatabase() throws Exception {
        try (Connection connection =
                     jdbc.getDataSource().getConnection()) {

            assertEquals("gestiva_test", connection.getCatalog());

            String user = connection.getMetaData().getUserName();

            assertTrue(
                    user.equals("gestiva_test_runner")
                    || user.startsWith("gestiva_test_runner@")
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

    private void configureOrderAndSnapshots() {
        PurchaseOrder order = mock(PurchaseOrder.class);

        when(order.getId()).thenReturn(ORDER);
        when(order.getStatus()).thenReturn("CONFIRMED");
        when(order.getSupplierId()).thenReturn(SUPPLIER);
        when(order.getCurrencyCode()).thenReturn("EUR");

        when(orders.findLockedByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, ORDER
        )).thenReturn(Optional.of(order));

        PurchaseOrderLine line = mock(PurchaseOrderLine.class);

        when(line.getItemId()).thenReturn(ITEM);
        when(line.getUomId()).thenReturn(UOM);
        when(line.getQuantity()).thenReturn(bd("2.000"));
        when(line.getConversionFactor())
                .thenReturn(bd("10.000000"));
        when(line.getDescription())
                .thenReturn("Articolo test");
        when(line.getUnitPrice()).thenReturn(bd("72.00"));
        when(line.getDiscountPct()).thenReturn(bd("0.00"));
        when(line.getTaxPct()).thenReturn(bd("22.00"));
        when(line.getLineSubtotal()).thenReturn(bd("144.00"));
        when(line.getTaxAmount()).thenReturn(bd("31.68"));
        when(line.getLineTotal()).thenReturn(bd("175.68"));

        when(orderLines.findByTenantIdAndPurchaseOrderIdAndId(
                TENANT, ORDER, ORDER_LINE
        )).thenReturn(Optional.of(line));

        UnitOfMeasure commercial = mock(UnitOfMeasure.class);
        UnitOfMeasure base = mock(UnitOfMeasure.class);
        Item item = mock(Item.class);

        when(commercial.getCode()).thenReturn("BOX");
        when(base.getCode()).thenReturn("PZ");
        when(item.getBaseUom()).thenReturn(base);

        when(units.findByTenantIdAndId(TENANT, UOM))
                .thenReturn(Optional.of(commercial));

        when(items.findByTenantIdAndId(TENANT, ITEM))
                .thenReturn(Optional.of(item));

        when(companies.requireCurrentCompanyId())
                .thenReturn(COMPANY);
    }

    private void configureReceipt(
            Long receiptId,
            String quantity,
            String previouslyReceived) {

        GoodsReceipt receipt = mock(GoodsReceipt.class);

        when(receipt.getId()).thenReturn(receiptId);
        when(receipt.getPurchaseOrderId()).thenReturn(ORDER);
        when(receipt.getSupplierId()).thenReturn(SUPPLIER);

        when(receipts.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, receiptId
        )).thenReturn(Optional.of(receipt));

        GoodsReceiptLine line = mock(GoodsReceiptLine.class);

        when(line.getPurchaseOrderLineId())
                .thenReturn(ORDER_LINE);
        when(line.getItemId()).thenReturn(ITEM);
        when(line.getQuantityReceived())
                .thenReturn(bd(quantity));

        when(receiptLines
                .findByTenantIdAndCompanyIdAndGoodsReceiptIdOrderByLineNoAsc(
                        TENANT, COMPANY, receiptId
                )).thenReturn(List.of(line));

        when(receiptLines.sumReceivedBeforeReceiptByOrderLine(
                TENANT, COMPANY, ORDER, ORDER_LINE, receiptId
        )).thenReturn(bd(previouslyReceived));
    }

    private SupplierInvoicePartialDraftPlan createPlan(
            Long receiptId) {

        return transaction().execute(status -> {

            var prepared = preparation.prepare(TENANT, receiptId);

            var plan = assembler.assemble(prepared);

            status.setRollbackOnly();

            return plan;
        });
    }

    @Test
    void threeReceiptsReconcileUsingRealPlanner()
            throws Exception {

        verifyDatabase();

        configureOrderAndSnapshots();

        configureReceipt(
                301L, "8.000", "0.000"
        );
        configureReceipt(
                302L, "7.000", "8.000"
        );
        configureReceipt(
                303L, "5.000", "15.000"
        );

        var first = createPlan(301L);
        var second = createPlan(302L);
        var third = createPlan(303L);

        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);

        assertEquals(
                0, bd("0.800").compareTo(
                        first.lines().get(0).commercialQuantity()
                )
        );
        assertEquals(
                0, bd("0.700").compareTo(
                        second.lines().get(0).commercialQuantity()
                )
        );
        assertEquals(
                0, bd("0.500").compareTo(
                        third.lines().get(0).commercialQuantity()
                )
        );

        assertEquals(0,
                bd("57.60").compareTo(first.subtotal()));
        assertEquals(0,
                bd("50.40").compareTo(second.subtotal()));
        assertEquals(0,
                bd("36.00").compareTo(third.subtotal()));

        assertEquals(0,
                bd("12.67").compareTo(first.tax()));
        assertEquals(0,
                bd("11.09").compareTo(second.tax()));
        assertEquals(0,
                bd("7.92").compareTo(third.tax()));

        assertEquals(0,
                bd("70.27").compareTo(first.total()));
        assertEquals(0,
                bd("61.49").compareTo(second.total()));
        assertEquals(0,
                bd("43.92").compareTo(third.total()));

        BigDecimal subtotal = first.subtotal()
                .add(second.subtotal())
                .add(third.subtotal());

        BigDecimal tax = first.tax()
                .add(second.tax())
                .add(third.tax());

        BigDecimal total = first.total()
                .add(second.total())
                .add(third.total());

        assertEquals(0,
                bd("144.00").compareTo(subtotal));
        assertEquals(0,
                bd("31.68").compareTo(tax));
        assertEquals(0,
                bd("175.68").compareTo(total));

        verify(guard, times(3)).requireEnabled();

        verify(orders, times(3))
                .findLockedByTenantIdAndCompanyIdAndId(
                        TENANT, COMPANY, ORDER
                );

        verify(receiptLines)
                .sumReceivedBeforeReceiptByOrderLine(
                        TENANT, COMPANY, ORDER, ORDER_LINE, 301L
                );

        verify(receiptLines)
                .sumReceivedBeforeReceiptByOrderLine(
                        TENANT, COMPANY, ORDER, ORDER_LINE, 302L
                );

        verify(receiptLines)
                .sumReceivedBeforeReceiptByOrderLine(
                        TENANT, COMPANY, ORDER, ORDER_LINE, 303L
                );

        verify(invoices, never()).save(any());
        verify(receipts, never()).save(any());
        verify(orderLines, never()).save(any());

        System.out.println(
                "PIPELINE MYSQL: tre servizi Spring reali"
        );
        System.out.println(
                "PIPELINE MYSQL: 8 + 7 + 5 PZ = 20 PZ"
        );
        System.out.println(
                "PIPELINE MYSQL: totale riconciliato 175.68 EUR"
        );
        System.out.println(
                "PIPELINE MYSQL: nessuna fattura persistita"
        );
    }
}
