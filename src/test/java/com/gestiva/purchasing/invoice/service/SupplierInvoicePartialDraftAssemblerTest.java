package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;
import com.gestiva.warehouse.item.entity.Item;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.uom.entity.UnitOfMeasure;
import com.gestiva.warehouse.uom.repository.UnitOfMeasureRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialDraftAssemblerTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long RECEIPT = 301L;
    private static final Long ORDER = 100L;
    private static final Long ORDER_LINE = 101L;
    private static final Long ITEM = 50L;
    private static final Long UOM = 5L;
    private static final Long SUPPLIER = 40L;

    private PurchaseOrderLineRepository orderLines;
    private UnitOfMeasureRepository units;
    private ItemRepository items;
    private SupplierInvoicePartialLinePlanner planner;

    private SupplierInvoicePartialDraftAssembler assembler;

    private GoodsReceipt receipt;
    private PurchaseOrder order;
    private GoodsReceiptLine receiptLine;
    private PurchaseOrderLine orderLine;

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    @BeforeEach
    void setup() {
        orderLines = mock(PurchaseOrderLineRepository.class);
        units = mock(UnitOfMeasureRepository.class);
        items = mock(ItemRepository.class);
        planner = mock(SupplierInvoicePartialLinePlanner.class);

        assembler = new SupplierInvoicePartialDraftAssembler(
                orderLines, units, items, planner
        );

        receipt = mock(GoodsReceipt.class);
        order = mock(PurchaseOrder.class);
        receiptLine = mock(GoodsReceiptLine.class);
        orderLine = mock(PurchaseOrderLine.class);

        when(receipt.getId()).thenReturn(RECEIPT);
        when(receipt.getPurchaseOrderId()).thenReturn(ORDER);
        when(receipt.getSupplierId()).thenReturn(SUPPLIER);

        when(order.getId()).thenReturn(ORDER);
        when(order.getSupplierId()).thenReturn(SUPPLIER);
        when(order.getCurrencyCode()).thenReturn("EUR");

        when(receiptLine.getPurchaseOrderLineId())
                .thenReturn(ORDER_LINE);
        when(receiptLine.getItemId()).thenReturn(ITEM);
        when(receiptLine.getQuantityReceived())
                .thenReturn(bd("8.000"));

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
    }

    private SupplierInvoicePartialPreparationService.Preparation
            preparation() {

        return new SupplierInvoicePartialPreparationService.Preparation(
                TENANT,
                COMPANY,
                receipt,
                order,
                List.of(receiptLine)
        );
    }

    private void configureOrderLine() {
        when(orderLines.findByTenantIdAndPurchaseOrderIdAndId(
                TENANT, ORDER, ORDER_LINE
        )).thenReturn(Optional.of(orderLine));
    }

    private void configureSnapshots() {
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
    }

    private void configurePlanner() {
        when(planner.plan(
                eq(TENANT),
                eq(COMPANY),
                eq(ORDER),
                eq(ORDER_LINE),
                eq(RECEIPT),
                eq(bd("8.000")),
                eq(bd("20.000")),
                eq(bd("10.000000")),
                eq(bd("144.00")),
                eq(bd("31.68")),
                eq(bd("175.68"))
        )).thenReturn(
                new SupplierInvoicePartialBillabilityValidator.BillableLine(
                        bd("0.800"),
                        bd("57.60"),
                        bd("12.67"),
                        bd("70.27")
                )
        );
    }

    @Test
    void validReceiptProducesCorrectImmutablePlan() {
        configureOrderLine();
        configureSnapshots();
        configurePlanner();

        var result = assembler.assemble(preparation());

        assertEquals(TENANT, result.tenantId());
        assertEquals(COMPANY, result.companyId());
        assertEquals(RECEIPT, result.goodsReceiptId());
        assertEquals(ORDER, result.purchaseOrderId());
        assertEquals(SUPPLIER, result.supplierId());
        assertEquals("EUR", result.currencyCode());

        assertEquals(1, result.lines().size());

        var line = result.lines().get(0);

        assertEquals(ORDER_LINE, line.purchaseOrderLineId());
        assertEquals("BOX", line.uomCode());
        assertEquals("PZ", line.baseUomCode());

        assertEquals(0,
                bd("0.800").compareTo(
                        line.commercialQuantity()));

        assertEquals(0,
                bd("57.60").compareTo(result.subtotal()));
        assertEquals(0,
                bd("12.67").compareTo(result.tax()));
        assertEquals(0,
                bd("70.27").compareTo(result.total()));

        verify(planner).plan(
                TENANT, COMPANY, ORDER, ORDER_LINE, RECEIPT,
                bd("8.000"), bd("20.000"),
                bd("10.000000"),
                bd("144.00"), bd("31.68"), bd("175.68")
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> result.lines().clear()
        );
    }

    @Test
    void missingPreparationIsRejectedBeforeAnyLookup() {
        assertThrows(
                BusinessException.class,
                () -> assembler.assemble(null)
        );

        verifyNoInteractions(
                orderLines, units, items, planner
        );
    }

    @Test
    void missingOrderLineStopsBeforePlanning() {
        assertThrows(
                BusinessException.class,
                () -> assembler.assemble(preparation())
        );

        verify(orderLines)
                .findByTenantIdAndPurchaseOrderIdAndId(
                        TENANT, ORDER, ORDER_LINE
                );

        verifyNoInteractions(units, items, planner);
    }

    @Test
    void differentArticleIsRejected() {
        when(orderLine.getItemId()).thenReturn(999L);
        configureOrderLine();

        assertThrows(
                BusinessException.class,
                () -> assembler.assemble(preparation())
        );

        verifyNoInteractions(units, items, planner);
    }

    @Test
    void missingCommercialUomIsRejected() {
        configureOrderLine();

        assertThrows(
                BusinessException.class,
                () -> assembler.assemble(preparation())
        );

        verify(units).findByTenantIdAndId(TENANT, UOM);
        verifyNoInteractions(items, planner);
    }

    @Test
    void missingItemStopsBeforePlanning() {
        configureOrderLine();

        UnitOfMeasure commercialUom =
                mock(UnitOfMeasure.class);

        when(units.findByTenantIdAndId(TENANT, UOM))
                .thenReturn(Optional.of(commercialUom));

        assertThrows(
                BusinessException.class,
                () -> assembler.assemble(preparation())
        );

        verify(items).findByTenantIdAndId(TENANT, ITEM);
        verifyNoInteractions(planner);
    }

    @Test
    void plannerFailureStopsAssembly() {
        configureOrderLine();
        configureSnapshots();

        when(planner.plan(
                anyLong(), anyLong(), anyLong(),
                anyLong(), anyLong(),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(BigDecimal.class)
        )).thenThrow(new BusinessException(
                "Quantità non fatturabile"
        ));

        assertThrows(
                BusinessException.class,
                () -> assembler.assemble(preparation())
        );

        verify(planner, times(1)).plan(
                anyLong(), anyLong(), anyLong(),
                anyLong(), anyLong(),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(BigDecimal.class)
        );
    }

    @Test
    void duplicateOrderLineIsRejectedByDraftPlan() {
        configureOrderLine();
        configureSnapshots();
        configurePlanner();

        var duplicatePreparation =
                new SupplierInvoicePartialPreparationService.Preparation(
                        TENANT,
                        COMPANY,
                        receipt,
                        order,
                        List.of(receiptLine, receiptLine)
                );

        assertThrows(
                BusinessException.class,
                () -> assembler.assemble(duplicatePreparation)
        );

        verify(planner, times(2)).plan(
                anyLong(), anyLong(), anyLong(),
                anyLong(), anyLong(),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(BigDecimal.class)
        );
    }
}
