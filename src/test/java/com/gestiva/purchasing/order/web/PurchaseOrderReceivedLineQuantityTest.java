package com.gestiva.purchasing.order.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.supplier.repository.SupplierRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.item.repository.ItemUomRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderReceivedLineQuantityTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;
    private static final Long ORDER_ID = 100L;
    private static final Long LINE_ID = 200L;

    @Mock PurchaseOrderRepository purchaseOrderRepository;
    @Mock PurchaseOrderLineRepository purchaseOrderLineRepository;
    @Mock GoodsReceiptRepository goodsReceiptRepository;
    @Mock GoodsReceiptLineRepository goodsReceiptLineRepository;
    @Mock SupplierRepository supplierRepository;
    @Mock DocumentSequenceService documentSequenceService;
    @Mock ItemRepository itemRepository;
    @Mock ItemUomRepository itemUomRepository;
    @Mock CompanyContext companyContext;

    @InjectMocks
    PurchaseOrderWebService service;

    @Test
    void shouldRejectQuantityChangeOnReceivedLine() {

        PurchaseOrder order = new PurchaseOrder();
        order.setTenantId(TENANT_ID);
        order.setCompanyId(COMPANY_ID);
        order.setStatus("DRAFT");

        PurchaseOrderLine original = new PurchaseOrderLine();
        original.setId(LINE_ID);
        original.setTenantId(TENANT_ID);
        original.setPurchaseOrderId(ORDER_ID);
        original.setDescription("Prodotto già ricevuto");
        original.setQuantity(new BigDecimal("2.000"));
        original.setUnitPrice(new BigDecimal("10.00"));
        original.setDiscountPct(new BigDecimal("0.00"));
        original.setTaxPct(new BigDecimal("22.00"));

        PurchaseOrderLineForm submitted =
                new PurchaseOrderLineForm();

        submitted.setLineId(LINE_ID);
        submitted.setDescription("Prodotto già ricevuto");
        submitted.setQuantity("3");
        submitted.setUnitPrice("10");
        submitted.setDiscountPct("0");
        submitted.setTaxPct("22");

        PurchaseOrderForm form = new PurchaseOrderForm();
        form.setSupplierId(10L);
        form.setOrderDate(LocalDate.of(2026, 11, 3));
        form.setStatus("DRAFT");
        form.setCurrencyCode("EUR");
        form.getLines().add(submitted);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(purchaseOrderRepository
                .findLockedByTenantIdAndCompanyIdAndId(
                        TENANT_ID,
                        COMPANY_ID,
                        ORDER_ID
                )).thenReturn(Optional.of(order));

        when(purchaseOrderLineRepository
                .findByTenantIdAndPurchaseOrderIdAndId(
                        TENANT_ID,
                        ORDER_ID,
                        LINE_ID
                )).thenReturn(Optional.of(original));

        when(purchaseOrderLineRepository
                .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        TENANT_ID,
                        ORDER_ID
                )).thenReturn(List.of(original));

        when(goodsReceiptLineRepository
                .existsByTenantIdAndPurchaseOrderLineId(
                        TENANT_ID,
                        LINE_ID
                )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.update(TENANT_ID, ORDER_ID, form)
        );

        assertTrue(
                exception.getMessage().contains(
                        "Impossibile modificare una riga"
                ),
                "Il rifiuto deve dipendere dalla protezione "
                    + "delle righe già ricevute."
        );

        verify(purchaseOrderRepository)
                .findLockedByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, ORDER_ID
                );

        verify(purchaseOrderLineRepository, times(2))
                .findByTenantIdAndPurchaseOrderIdAndId(
                        TENANT_ID, ORDER_ID, LINE_ID
                );

        verify(goodsReceiptLineRepository, times(1))
                .existsByTenantIdAndPurchaseOrderLineId(
                        TENANT_ID, LINE_ID
                );

        verify(purchaseOrderRepository, never())
                .save(any(PurchaseOrder.class));

        verify(purchaseOrderLineRepository, never())
                .save(any(PurchaseOrderLine.class));

        verify(purchaseOrderLineRepository, never())
                .deleteByTenantIdAndPurchaseOrderIdAndId(
                        anyLong(), anyLong(), anyLong()
                );

        assertEquals(
                0,
                original.getQuantity().compareTo(
                        new BigDecimal("2.000")
                )
        );

        verifyNoInteractions(
                supplierRepository,
                documentSequenceService,
                itemRepository,
                itemUomRepository,
                goodsReceiptRepository
        );
    }
}
