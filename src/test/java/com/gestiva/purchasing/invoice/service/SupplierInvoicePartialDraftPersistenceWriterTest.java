package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import com.gestiva.purchasing.invoice.entity.SupplierInvoiceLine;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceLineRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialDraftPersistenceWriterTest {

    private SupplierInvoicePartialGenerationGuard gate;
    private SupplierInvoicePartialIsolationGuard isolation;
    private SupplierInvoiceRepository invoices;
    private SupplierInvoiceLineRepository lines;

    private SupplierInvoicePartialDraftPersistenceWriter writer;

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private SupplierInvoicePartialDraftPlan plan() {

        var line = new SupplierInvoicePartialDraftPlan.Line(
                1,
                101L,
                1L,
                "Articolo collaudo",
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
                2L,
                1L,
                302L,
                100L,
                1L,
                "EUR",
                List.of(line),
                bd("50.40"),
                bd("11.09"),
                bd("61.49")
        );
    }

    @BeforeEach
    void setUp() {

        gate = mock(
                SupplierInvoicePartialGenerationGuard.class
        );

        isolation = mock(
                SupplierInvoicePartialIsolationGuard.class
        );

        invoices = mock(SupplierInvoiceRepository.class);
        lines = mock(SupplierInvoiceLineRepository.class);

        writer = new SupplierInvoicePartialDraftPersistenceWriter(
                gate,
                isolation,
                new SupplierInvoicePartialDraftEntityMapper(),
                invoices,
                lines
        );
    }

    @Test
    void savesHeaderAndAllocatedLinesInOrder() {

        SupplierInvoice saved = mock(SupplierInvoice.class);
        when(saved.getId()).thenReturn(700L);

        when(invoices.save(any(SupplierInvoice.class)))
                .thenReturn(saved);

        Long result = writer.write(
                plan(),
                "SI-TEST-00001",
                LocalDate.of(2026, 11, 5)
        );

        assertEquals(700L, result);

        var order = inOrder(
                gate,
                isolation,
                invoices,
                lines
        );

        order.verify(gate).requireEnabled();

        order.verify(isolation)
                .requireReadCommittedWriteTransaction();

        order.verify(invoices)
                .save(any(SupplierInvoice.class));

        order.verify(lines).saveAll(anyList());

        ArgumentCaptor<SupplierInvoice> headerCaptor =
                ArgumentCaptor.forClass(SupplierInvoice.class);

        verify(invoices).save(headerCaptor.capture());

        var header = headerCaptor.getValue();

        assertEquals("DRAFT", header.getStatus());
        assertNull(header.getId());
        assertEquals(2L, header.getTenantId());
        assertEquals(1L, header.getCompanyId());
        assertEquals(302L, header.getGoodsReceiptId());
        assertEquals(100L, header.getPurchaseOrderId());
        assertEquals(
                0,
                bd("61.49").compareTo(
                        header.getTotalAmount()
                )
        );

        @SuppressWarnings("rawtypes")
        ArgumentCaptor<List> rowsCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(lines).saveAll(rowsCaptor.capture());

        assertEquals(1, rowsCaptor.getValue().size());

        SupplierInvoiceLine line =
                (SupplierInvoiceLine) rowsCaptor
                        .getValue().get(0);

        assertEquals(700L, line.getSupplierInvoiceId());
        assertEquals(0,
                bd("0.700").compareTo(line.getQuantity()));

        assertEquals(0,
                bd("11.09").compareTo(line.getTaxAmount()));

        assertEquals(0,
                bd("61.49").compareTo(line.getLineTotal()));
    }

    @Test
    void gateFailurePreventsAllPersistence() {

        doThrow(new BusinessException("Funzione disabilitata"))
                .when(gate)
                .requireEnabled();

        assertThrows(
                BusinessException.class,
                () -> writer.write(
                        plan(),
                        "SI-TEST-00001",
                        LocalDate.now()
                )
        );

        verifyNoInteractions(isolation, invoices, lines);
    }

    @Test
    void incompatibleTransactionPreventsAllPersistence() {

        doThrow(new BusinessException("Isolamento non valido"))
                .when(isolation)
                .requireReadCommittedWriteTransaction();

        assertThrows(
                BusinessException.class,
                () -> writer.write(
                        plan(),
                        "SI-TEST-00001",
                        LocalDate.now()
                )
        );

        verify(gate).requireEnabled();
        verifyNoInteractions(invoices, lines);
    }

    @Test
    void missingGeneratedIdPreventsLinePersistence() {

        SupplierInvoice saved = mock(SupplierInvoice.class);
        when(saved.getId()).thenReturn(null);

        when(invoices.save(any(SupplierInvoice.class)))
                .thenReturn(saved);

        assertThrows(
                BusinessException.class,
                () -> writer.write(
                        plan(),
                        "SI-TEST-00001",
                        LocalDate.now()
                )
        );

        verify(invoices).save(any(SupplierInvoice.class));
        verifyNoInteractions(lines);
    }
}
