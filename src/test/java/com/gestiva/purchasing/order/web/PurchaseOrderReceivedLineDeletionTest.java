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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderReceivedLineDeletionTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;
    private static final Long ORDER_ID = 100L;
    private static final Long RECEIVED_LINE_ID = 200L;

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
    void shouldRejectDeletionOfLineLinkedToGoodsReceipt() {

        PurchaseOrder order = new PurchaseOrder();
        order.setTenantId(TENANT_ID);
        order.setCompanyId(COMPANY_ID);
        order.setStatus("DRAFT");

        PurchaseOrderLine receivedLine =
                new PurchaseOrderLine();

        receivedLine.setId(RECEIVED_LINE_ID);
        receivedLine.setTenantId(TENANT_ID);
        receivedLine.setPurchaseOrderId(ORDER_ID);

        // Il browser invia soltanto una nuova riga:
        // la riga 200 è stata rimossa dal modulo.
        PurchaseOrderLineForm newLine =
                new PurchaseOrderLineForm();

        newLine.setDescription("Nuova riga di prova");
        newLine.setQuantity("1");
        newLine.setUnitPrice("10");
        newLine.setDiscountPct("0");
        newLine.setTaxPct("22");

        PurchaseOrderForm form = new PurchaseOrderForm();
        form.setSupplierId(10L);
        form.setOrderDate(LocalDate.of(2026, 11, 3));
        form.setStatus("DRAFT");
        form.setCurrencyCode("EUR");
        form.getLines().add(newLine);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(purchaseOrderRepository
                .findLockedByTenantIdAndCompanyIdAndId(
                        TENANT_ID,
                        COMPANY_ID,
                        ORDER_ID
                )).thenReturn(Optional.of(order));

        when(purchaseOrderLineRepository
                .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        TENANT_ID,
                        ORDER_ID
                )).thenReturn(List.of(receivedLine));

        when(goodsReceiptLineRepository
                .existsByTenantIdAndPurchaseOrderLineId(
                        TENANT_ID,
                        RECEIVED_LINE_ID
                )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.update(TENANT_ID, ORDER_ID, form)
        );

        assertTrue(
                exception.getMessage().contains(
                        "ricevimento merci"
                )
        );

        verify(purchaseOrderRepository)
                .findLockedByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, ORDER_ID
                );

        verify(goodsReceiptLineRepository)
                .existsByTenantIdAndPurchaseOrderLineId(
                        TENANT_ID, RECEIVED_LINE_ID
                );

        // Il rifiuto deve avvenire prima di ogni scrittura.
        verify(purchaseOrderRepository, never())
                .save(any(PurchaseOrder.class));

        verify(purchaseOrderLineRepository, never())
                .save(any(PurchaseOrderLine.class));

        verify(purchaseOrderLineRepository, never())
                .deleteByTenantIdAndPurchaseOrderIdAndId(
                        anyLong(),
                        anyLong(),
                        anyLong()
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
