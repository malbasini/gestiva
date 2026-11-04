package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoodsReceiptRemainingQuantityServiceTest {

    private GoodsReceiptLineRepository repository;
    private GoodsReceiptRemainingQuantityService service;

    @BeforeEach
    void setUp() {
        repository = mock(GoodsReceiptLineRepository.class);
        service = new GoodsReceiptRemainingQuantityService(repository);
    }

    private BigDecimal remaining() {
        return service.calculateRemainingBaseQuantity(
                2L,
                1L,
                100L,
                200L,
                new BigDecimal("2.000"),
                new BigDecimal("10.000000")
        );
    }

    @Test
    void noPreviousReceiptsReturnsFullQuantity() {
        when(repository.sumReceivedBaseQuantityByOrderLine(
                2L, 1L, 100L, 200L
        )).thenReturn(null);

        assertEquals(
                new BigDecimal("20.000"),
                remaining()
        );

        verify(repository).sumReceivedBaseQuantityByOrderLine(
                2L, 1L, 100L, 200L
        );
    }

    @Test
    void partialReceiptReturnsCorrectRemainingQuantity() {
        when(repository.sumReceivedBaseQuantityByOrderLine(
                2L, 1L, 100L, 200L
        )).thenReturn(new BigDecimal("8.000"));

        assertEquals(
                new BigDecimal("12.000"),
                remaining()
        );
    }

    @Test
    void multipleReceiptsAreHandledThroughAggregatedQuantity() {
        // Il repository ha già aggregato 8 + 7 PZ.
        when(repository.sumReceivedBaseQuantityByOrderLine(
                2L, 1L, 100L, 200L
        )).thenReturn(new BigDecimal("15.000"));

        assertEquals(
                new BigDecimal("5.000"),
                remaining()
        );
    }

    @Test
    void completeReceiptReturnsZero() {
        when(repository.sumReceivedBaseQuantityByOrderLine(
                2L, 1L, 100L, 200L
        )).thenReturn(new BigDecimal("20.000"));

        assertEquals(
                new BigDecimal("0.000"),
                remaining()
        );
    }

    @Test
    void overReceiptIsRejected() {
        when(repository.sumReceivedBaseQuantityByOrderLine(
                2L, 1L, 100L, 200L
        )).thenReturn(new BigDecimal("21.000"));

        assertThrows(
                BusinessException.class,
                this::remaining
        );
    }

    @Test
    void missingCompanyIsRejectedBeforeRepositoryAccess() {
        assertThrows(
                BusinessException.class,
                () -> service.calculateRemainingBaseQuantity(
                        2L,
                        null,
                        100L,
                        200L,
                        new BigDecimal("2"),
                        new BigDecimal("10")
                )
        );

        verifyNoInteractions(repository);
    }
}
