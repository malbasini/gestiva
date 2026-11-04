package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoodsReceiptPartialIntegrationTest {

    private GoodsReceiptLineRepository repository;
    private GoodsReceiptService service;

    @BeforeEach
    void setUp() {
        repository = mock(GoodsReceiptLineRepository.class);

        service = new GoodsReceiptService(
                null,
                null,
                null,
                repository,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private PurchaseOrderLine orderLine(long id) {
        PurchaseOrderLine line = mock(PurchaseOrderLine.class);

        when(line.getId()).thenReturn(id);
        when(line.getPurchaseOrderId()).thenReturn(100L);
        when(line.getQuantity()).thenReturn(
                new BigDecimal("2.000")
        );
        when(line.getConversionFactor()).thenReturn(
                new BigDecimal("10.000000")
        );

        return line;
    }

    @Test
    void acceptsPartialReceiptAfterPreviousDelivery() {

        when(repository.sumReceivedBaseQuantityByOrderLine(
                2L, 1L, 100L, 101L
        )).thenReturn(new BigDecimal("8.000"));

        var result =
                service.validatePartialQuantitiesForLockedOrder(
                        2L, 1L, 100L,
                        List.of(orderLine(101L)),
                        Map.of(101L, new BigDecimal("7"))
                );

        assertEquals(
                new BigDecimal("7.000"),
                result.get(101L)
        );

        verify(repository).sumReceivedBaseQuantityByOrderLine(
                2L, 1L, 100L, 101L
        );
    }

    @Test
    void acceptsOnlySelectedLines() {

        var result =
                service.validatePartialQuantitiesForLockedOrder(
                        2L, 1L, 100L,
                        List.of(orderLine(101L), orderLine(102L)),
                        Map.of(101L, new BigDecimal("5"))
                );

        assertEquals(1, result.size());
        assertFalse(result.containsKey(102L));
    }

    @Test
    void rejectsQuantityAboveRemaining() {

        when(repository.sumReceivedBaseQuantityByOrderLine(
                2L, 1L, 100L, 101L
        )).thenReturn(new BigDecimal("8.000"));

        assertThrows(
                BusinessException.class,
                () -> service.validatePartialQuantitiesForLockedOrder(
                        2L, 1L, 100L,
                        List.of(orderLine(101L)),
                        Map.of(101L, new BigDecimal("13"))
                )
        );
    }

    @Test
    void rejectsUnknownOrderLine() {

        assertThrows(
                BusinessException.class,
                () -> service.validatePartialQuantitiesForLockedOrder(
                        2L, 1L, 100L,
                        List.of(orderLine(101L)),
                        Map.of(999L, BigDecimal.ONE)
                )
        );
    }

    @Test
    void rejectsAlreadyCompletedOrderLine() {

        when(repository.sumReceivedBaseQuantityByOrderLine(
                2L, 1L, 100L, 101L
        )).thenReturn(new BigDecimal("20.000"));

        assertThrows(
                BusinessException.class,
                () -> service.validatePartialQuantitiesForLockedOrder(
                        2L, 1L, 100L,
                        List.of(orderLine(101L)),
                        Map.of(101L, BigDecimal.ONE)
                )
        );
    }
}
