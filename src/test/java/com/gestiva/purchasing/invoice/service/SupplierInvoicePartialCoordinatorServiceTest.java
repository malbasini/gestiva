package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialCoordinatorServiceTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long RECEIPT = 301L;

    private SupplierInvoicePartialPreparationService preparation;
    private SupplierInvoicePartialDraftAssembler assembler;
    private SupplierInvoicePartialCoordinatorService coordinator;

    private SupplierInvoicePartialPreparationService.Preparation prepared;
    private SupplierInvoicePartialDraftPlan expectedPlan;

    @BeforeEach
    void setup() {
        preparation = mock(
                SupplierInvoicePartialPreparationService.class
        );

        assembler = mock(
                SupplierInvoicePartialDraftAssembler.class
        );

        coordinator = new SupplierInvoicePartialCoordinatorService(
                preparation,
                assembler
        );

        prepared =
                new SupplierInvoicePartialPreparationService.Preparation(
                        TENANT,
                        COMPANY,
                        mock(GoodsReceipt.class),
                        mock(PurchaseOrder.class),
                        List.of(mock(GoodsReceiptLine.class))
                );

        expectedPlan = mock(SupplierInvoicePartialDraftPlan.class);
    }

    @Test
    void preparationPrecedesAssembly() {
        when(preparation.prepare(TENANT, RECEIPT))
                .thenReturn(prepared);

        when(assembler.assemble(prepared))
                .thenReturn(expectedPlan);

        coordinator.plan(TENANT, RECEIPT);

        var order = inOrder(preparation, assembler);

        order.verify(preparation).prepare(TENANT, RECEIPT);
        order.verify(assembler).assemble(prepared);

        verifyNoMoreInteractions(preparation, assembler);
    }

    @Test
    void returnsTheExactAssembledPlan() {
        when(preparation.prepare(TENANT, RECEIPT))
                .thenReturn(prepared);

        when(assembler.assemble(prepared))
                .thenReturn(expectedPlan);

        var actual = coordinator.plan(TENANT, RECEIPT);

        assertSame(expectedPlan, actual);

        verify(preparation).prepare(TENANT, RECEIPT);
        verify(assembler).assemble(prepared);
    }

    @Test
    void preparationFailureStopsBeforeAssembly() {
        doThrow(new BusinessException(
                "Preparazione non consentita"
        )).when(preparation).prepare(TENANT, RECEIPT);

        assertThrows(
                BusinessException.class,
                () -> coordinator.plan(TENANT, RECEIPT)
        );

        verify(preparation).prepare(TENANT, RECEIPT);
        verifyNoInteractions(assembler);
    }

    @Test
    void assemblyFailureIsPropagated() {
        when(preparation.prepare(TENANT, RECEIPT))
                .thenReturn(prepared);

        when(assembler.assemble(prepared))
                .thenThrow(new BusinessException(
                        "Piano fattura non valido"
                ));

        assertThrows(
                BusinessException.class,
                () -> coordinator.plan(TENANT, RECEIPT)
        );

        var order = inOrder(preparation, assembler);

        order.verify(preparation).prepare(TENANT, RECEIPT);
        order.verify(assembler).assemble(prepared);

        verifyNoMoreInteractions(preparation, assembler);
    }
}
