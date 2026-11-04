package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.warehouse.inventory.movement.service.InventoryDocumentPostingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GoodsReceiptPartialPersistenceWriterTest {

    private GoodsReceiptPartialPersistenceGate gate;
    private GoodsReceiptPartialWriteTransactionGuard transactionGuard;
    private GoodsReceiptRepository receipts;
    private GoodsReceiptLineRepository lines;
    private InventoryDocumentPostingService posting;
    private GoodsReceiptPartialPersistenceWriter writer;

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    @BeforeEach
    void setup() {
        gate = mock(GoodsReceiptPartialPersistenceGate.class);
        transactionGuard = mock(GoodsReceiptPartialWriteTransactionGuard.class);
        receipts = mock(GoodsReceiptRepository.class);
        lines = mock(GoodsReceiptLineRepository.class);
        posting = mock(InventoryDocumentPostingService.class);

        writer = new GoodsReceiptPartialPersistenceWriter(
                gate, transactionGuard, receipts, lines, posting
        );
    }

    private GoodsReceiptPartialDocumentAssembler.DocumentPlan plan() {

        GoodsReceipt header = new GoodsReceipt();
        header.setTenantId(2L);
        header.setCompanyId(1L);
        header.setWarehouseId(7L);
        header.setPurchaseOrderId(31L);
        header.setSupplierId(12L);
        header.setReceiptNumber("GR-TEST-1908");
        header.setReceiptDate(LocalDate.of(2026, 11, 4));

        PurchaseOrderLine orderLine = mock(PurchaseOrderLine.class);

        when(orderLine.getId()).thenReturn(101L);
        when(orderLine.getTenantId()).thenReturn(2L);
        when(orderLine.getPurchaseOrderId()).thenReturn(31L);
        when(orderLine.getItemId()).thenReturn(50L);
        when(orderLine.getDescription()).thenReturn("Articolo test");

        var planned = GoodsReceiptPartialFifoLineCalculator.calculate(
                bd("20.000"),
                bd("8.000"),
                bd("12.000"),
                bd("144.00")
        );

        return GoodsReceiptPartialDocumentAssembler.prepare(
                header,
                List.of(
                        new GoodsReceiptPartialDocumentAssembler.LineInput(
                                orderLine, planned
                        )
                )
        );
    }

    @Test
    void savesHeaderThenLinesThenPostsFifo() {

        var document = plan();

        when(receipts.save(any(GoodsReceipt.class)))
                .thenAnswer(invocation -> {
                    GoodsReceipt header = invocation.getArgument(0);
                    header.setId(500L);
                    return header;
                });

        Long result = writer.write(document);

        assertEquals(500L, result);

        var calls = inOrder(transactionGuard, gate, receipts, lines, posting);

        calls.verify(transactionGuard).requireWritableReadCommitted();
        calls.verify(gate).requireEnabled();

        calls.verify(receipts)
                .save(same(document.header()));

        calls.verify(lines)
                .saveAll(argThat(rows -> {
                    int count = 0;
                    for (GoodsReceiptLine row : rows) {
                        count++;
                        if (!Long.valueOf(500L).equals(
                                row.getGoodsReceiptId())) {
                            return false;
                        }
                        if (row.getTotalCost()
                                .compareTo(bd("86.4000")) != 0) {
                            return false;
                        }
                    }
                    return count == 1;
                }));

        calls.verify(posting)
                .postPurchaseReceiptFromGoodsReceiptV2(
                        2L, 1L, 500L
                );

        verifyNoMoreInteractions(
                transactionGuard, gate, receipts, lines, posting
        );

        System.out.println(
                "PARTIAL WRITER: testata, righe e FIFO in ordine"
        );
    }

    @Test
    void disabledGatePreventsEveryWrite() {

        doThrow(new BusinessException(
                "Ricevimenti parziali disabilitati."
        )).when(gate).requireEnabled();

        assertThrows(
                BusinessException.class,
                () -> writer.write(plan())
        );

        verifyNoInteractions(receipts, lines, posting);

        System.out.println(
                "PARTIAL WRITER: gate blocca ogni scrittura"
        );
    }


    @Test
    void missingWriteTransactionPreventsEveryWrite() {

        doThrow(new BusinessException(
                "Transazione attiva obbligatoria."
        )).when(transactionGuard).requireWritableReadCommitted();

        assertThrows(
                BusinessException.class,
                () -> writer.write(plan())
        );

        verifyNoInteractions(gate, receipts, lines, posting);

        System.out.println(
                "PARTIAL WRITER: nessuna scrittura senza transazione"
        );
    }

    @Test
    void missingGeneratedIdPreventsLinesAndPosting() {

        when(receipts.save(any(GoodsReceipt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(
                BusinessException.class,
                () -> writer.write(plan())
        );

        verify(receipts, times(1))
                .save(any(GoodsReceipt.class));

        verifyNoInteractions(lines, posting);

        System.out.println(
                "PARTIAL WRITER: ID non generato rifiutato"
        );
    }

    @Test
    void linePersistenceFailurePreventsPosting() {

        when(receipts.save(any(GoodsReceipt.class)))
                .thenAnswer(invocation -> {
                    GoodsReceipt header = invocation.getArgument(0);
                    header.setId(500L);
                    return header;
                });

        when(lines.saveAll(any()))
                .thenThrow(new IllegalStateException(
                        "Errore simulato durante il salvataggio righe."
                ));

        assertThrows(
                IllegalStateException.class,
                () -> writer.write(plan())
        );

        verifyNoInteractions(posting);

        System.out.println(
                "PARTIAL WRITER: errore righe interrompe posting"
        );
    }
}
