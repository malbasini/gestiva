package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoodsReceiptPartialHistoryReaderTest {

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private PurchaseOrderLine line(
            long id, long tenantId, long orderId) {

        PurchaseOrderLine result =
                mock(PurchaseOrderLine.class);

        when(result.getId()).thenReturn(id);
        when(result.getTenantId()).thenReturn(tenantId);
        when(result.getPurchaseOrderId()).thenReturn(orderId);

        return result;
    }

    @Test
    void readsEveryLineOnceAndNormalizesNullToZero() {

        var repository = mock(GoodsReceiptLineRepository.class);
        var reader = new GoodsReceiptPartialHistoryReader(repository);

        when(repository.sumReceivedBaseQuantityByOrderLine(
                2L, 1L, 31L, 101L
        )).thenReturn(null);

        when(repository.sumReceivedBaseQuantityByOrderLine(
                2L, 1L, 31L, 102L
        )).thenReturn(bd("8.000"));

        var snapshot = reader.read(
                2L, 1L, 31L,
                List.of(
                        line(101L, 2L, 31L),
                        line(102L, 2L, 31L)
                )
        );

        assertEquals(
                0, bd("0.000").compareTo(snapshot.get(101L))
        );

        assertEquals(
                0, bd("8.000").compareTo(snapshot.get(102L))
        );

        assertEquals(
                List.of(101L, 102L),
                List.copyOf(snapshot.keySet())
        );

        verify(repository, times(1))
                .sumReceivedBaseQuantityByOrderLine(
                        2L, 1L, 31L, 101L
                );

        verify(repository, times(1))
                .sumReceivedBaseQuantityByOrderLine(
                        2L, 1L, 31L, 102L
                );

        verifyNoMoreInteractions(repository);

        System.out.println(
                "PARTIAL HISTORY: una lettura per riga confermata"
        );
    }

    @Test
    void snapshotCannotBeModified() {

        var repository = mock(GoodsReceiptLineRepository.class);
        var reader = new GoodsReceiptPartialHistoryReader(repository);

        var snapshot = reader.read(
                2L, 1L, 31L,
                List.of(line(101L, 2L, 31L))
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> snapshot.put(101L, bd("99.000"))
        );

        System.out.println(
                "PARTIAL HISTORY: snapshot non modificabile"
        );
    }

    @Test
    void rejectsDifferentTenantOrOrderBeforeQuery() {

        var repository = mock(GoodsReceiptLineRepository.class);
        var reader = new GoodsReceiptPartialHistoryReader(repository);

        assertThrows(
                BusinessException.class,
                () -> reader.read(
                        2L, 1L, 31L,
                        List.of(line(101L, 9L, 31L))
                )
        );

        assertThrows(
                BusinessException.class,
                () -> reader.read(
                        2L, 1L, 31L,
                        List.of(line(101L, 2L, 99L))
                )
        );

        verifyNoInteractions(repository);

        System.out.println(
                "PARTIAL HISTORY: tenant e ordine estranei rifiutati"
        );
    }

    @Test
    void rejectsDuplicatedOrderLine() {

        var repository = mock(GoodsReceiptLineRepository.class);
        var reader = new GoodsReceiptPartialHistoryReader(repository);

        assertThrows(
                BusinessException.class,
                () -> reader.read(
                        2L, 1L, 31L,
                        List.of(
                                line(101L, 2L, 31L),
                                line(101L, 2L, 31L)
                        )
                )
        );

        verify(repository, times(1))
                .sumReceivedBaseQuantityByOrderLine(
                        2L, 1L, 31L, 101L
                );

        System.out.println(
                "PARTIAL HISTORY: riga duplicata rifiutata"
        );
    }

    @Test
    void rejectsNegativeHistoricalQuantity() {

        var repository = mock(GoodsReceiptLineRepository.class);
        var reader = new GoodsReceiptPartialHistoryReader(repository);

        when(repository.sumReceivedBaseQuantityByOrderLine(
                2L, 1L, 31L, 101L
        )).thenReturn(bd("-1.000"));

        assertThrows(
                BusinessException.class,
                () -> reader.read(
                        2L, 1L, 31L,
                        List.of(line(101L, 2L, 31L))
                )
        );

        System.out.println(
                "PARTIAL HISTORY: quantità negativa rifiutata"
        );
    }
}
