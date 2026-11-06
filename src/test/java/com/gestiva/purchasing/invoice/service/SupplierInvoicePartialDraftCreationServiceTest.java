package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.settings.sequence.service.DocumentSequenceService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialDraftCreationServiceTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long RECEIPT = 302L;
    private static final Long ORDER = 100L;
    private static final Long SUPPLIER = 1L;

    private SupplierInvoicePartialPreparationService preparation;
    private SupplierInvoicePartialDraftAssembler assembler;
    private DocumentSequenceService sequences;
    private SupplierInvoicePartialDraftPersistenceWriter writer;

    private SupplierInvoicePartialDraftCreationService service;

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private SupplierInvoicePartialDraftPlan plan(Long receiptId) {
        var line = new SupplierInvoicePartialDraftPlan.Line(
                1,
                101L,
                1L,
                "Articolo test coordinatore",
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
                ORDER,
                SUPPLIER,
                "EUR",
                List.of(line),
                bd("50.40"),
                bd("11.09"),
                bd("61.49")
        );
    }

    private SupplierInvoicePartialPreparationService.Preparation
    prepared() {

        PurchaseOrder order = new PurchaseOrder();
        order.setTenantId(TENANT);
        order.setCompanyId(COMPANY);
        order.setSupplierId(SUPPLIER);
        order.setId(ORDER);

        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setTenantId(TENANT);
        receipt.setCompanyId(COMPANY);
        receipt.setId(RECEIPT);
        receipt.setPurchaseOrderId(ORDER);
        receipt.setSupplierId(SUPPLIER);

        return new SupplierInvoicePartialPreparationService.Preparation(
                TENANT,
                COMPANY,
                receipt,
                order,
                List.of()
        );
    }

    @BeforeEach
    void setUp() {
        preparation = mock(
                SupplierInvoicePartialPreparationService.class
        );
        assembler = mock(
                SupplierInvoicePartialDraftAssembler.class
        );
        sequences = mock(DocumentSequenceService.class);
        writer = mock(
                SupplierInvoicePartialDraftPersistenceWriter.class
        );

        service = new SupplierInvoicePartialDraftCreationService(
                preparation,
                assembler,
                sequences,
                writer
        );
    }

    @Test
    void executesStepsInCorrectOrder() {
        var data = prepared();
        var plan = plan(RECEIPT);

        when(preparation.prepare(TENANT, RECEIPT))
                .thenReturn(data);
        when(assembler.assemble(data)).thenReturn(plan);

        when(sequences.nextNumber(
                TENANT,
                COMPANY,
                DocumentSequenceService.SUPPLIER_INVOICE
        )).thenReturn("SI-TEST-00001");

        when(writer.write(
                eq(plan),
                eq("SI-TEST-00001"),
                any(LocalDate.class)
        )).thenReturn(700L);

        assertEquals(700L, service.create(TENANT, RECEIPT));

        var order = inOrder(
                preparation,
                assembler,
                sequences,
                writer
        );

        order.verify(preparation).prepare(TENANT, RECEIPT);
        order.verify(assembler).assemble(data);
        order.verify(sequences).nextNumber(
                TENANT,
                COMPANY,
                DocumentSequenceService.SUPPLIER_INVOICE
        );
        order.verify(writer).write(
                eq(plan),
                eq("SI-TEST-00001"),
                any(LocalDate.class)
        );

        verifyNoMoreInteractions(
                preparation,
                assembler,
                sequences,
                writer
        );
    }

    @Test
    void preparationFailureDoesNotConsumeNumber() {
        when(preparation.prepare(TENANT, RECEIPT))
                .thenThrow(new BusinessException(
                        "Preparazione non disponibile."
                ));

        assertThrows(
                BusinessException.class,
                () -> service.create(TENANT, RECEIPT)
        );

        verifyNoInteractions(assembler, sequences, writer);
    }

    @Test
    void assemblyFailureDoesNotConsumeNumber() {
        var data = prepared();

        when(preparation.prepare(TENANT, RECEIPT))
                .thenReturn(data);
        when(assembler.assemble(data))
                .thenThrow(new BusinessException(
                        "Piano non valido."
                ));

        assertThrows(
                BusinessException.class,
                () -> service.create(TENANT, RECEIPT)
        );

        verifyNoInteractions(sequences, writer);
    }

    @Test
    void mismatchedReceiptIsRejectedBeforeNumbering() {
        var data = prepared();

        when(preparation.prepare(TENANT, RECEIPT))
                .thenReturn(data);

        when(assembler.assemble(data))
                .thenReturn(plan(303L));

        assertThrows(
                BusinessException.class,
                () -> service.create(TENANT, RECEIPT)
        );

        verifyNoInteractions(sequences, writer);
    }

    @Test
    void writerFailureIsPropagated() {
        var data = prepared();
        var plan = plan(RECEIPT);

        when(preparation.prepare(TENANT, RECEIPT))
                .thenReturn(data);
        when(assembler.assemble(data)).thenReturn(plan);

        when(sequences.nextNumber(
                TENANT,
                COMPANY,
                DocumentSequenceService.SUPPLIER_INVOICE
        )).thenReturn("SI-TEST-00002");

        when(writer.write(
                eq(plan),
                eq("SI-TEST-00002"),
                any(LocalDate.class)
        )).thenThrow(new IllegalStateException(
                "Errore simulato del writer."
        ));

        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> service.create(TENANT, RECEIPT)
        );

        assertEquals(
                "Errore simulato del writer.",
                failure.getMessage()
        );

        verify(sequences).nextNumber(
                TENANT,
                COMPANY,
                DocumentSequenceService.SUPPLIER_INVOICE
        );
        verify(writer).write(
                eq(plan),
                eq("SI-TEST-00002"),
                any(LocalDate.class)
        );
    }
}
