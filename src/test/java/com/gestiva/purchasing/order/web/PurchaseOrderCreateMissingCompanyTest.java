package com.gestiva.purchasing.order.web;

import com.gestiva.common.exception.BusinessException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderCreateMissingCompanyTest {

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
    void shouldRejectMissingCompanyBeforeNumberGenerationOrSave() {
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new BusinessException(
                        "Selezionare una società attiva."
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(
                        999L,
                        new PurchaseOrderForm()
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        "Selezionare una società attiva"
                )
        );

        verify(companyContext).requireCurrentCompanyId();

        verifyNoInteractions(
                purchaseOrderRepository,
                purchaseOrderLineRepository,
                goodsReceiptRepository,
                goodsReceiptLineRepository,
                supplierRepository,
                documentSequenceService,
                itemRepository,
                itemUomRepository
        );
    }
}
