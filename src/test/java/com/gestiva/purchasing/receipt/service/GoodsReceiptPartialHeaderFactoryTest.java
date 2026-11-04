package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrder;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoodsReceiptPartialHeaderFactoryTest {

    private static final LocalDate DATE =
            LocalDate.of(2026, 11, 4);

    private PurchaseOrder order() {
        PurchaseOrder po = mock(PurchaseOrder.class);

        when(po.getId()).thenReturn(31L);
        when(po.getTenantId()).thenReturn(2L);
        when(po.getCompanyId()).thenReturn(1L);
        when(po.getSupplierId()).thenReturn(12L);
        when(po.getOrderNumber()).thenReturn("PO-00031");
        when(po.getStatus()).thenReturn("CONFIRMED");

        return po;
    }

    @Test
    void createsHeaderWithCorrectReferences() {

        var receipt = GoodsReceiptPartialHeaderFactory.create(
                2L, 1L, 7L,
                order(), "GR-00025", DATE
        );

        assertEquals(2L, receipt.getTenantId());
        assertEquals(1L, receipt.getCompanyId());
        assertEquals(7L, receipt.getWarehouseId());
        assertEquals(31L, receipt.getPurchaseOrderId());
        assertEquals(12L, receipt.getSupplierId());
        assertEquals("GR-00025", receipt.getReceiptNumber());
        assertEquals(DATE, receipt.getReceiptDate());
        assertNull(receipt.getLocationId());

        assertTrue(
                receipt.getNotes().contains("PO-00031")
        );

        assertNull(receipt.getId());

        System.out.println(
                "PARTIAL HEADER: testata creata senza persistenza"
        );
    }

    @Test
    void rejectsDifferentTenant() {

        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialHeaderFactory.create(
                        3L, 1L, 7L,
                        order(), "GR-00025", DATE
                )
        );

        System.out.println(
                "PARTIAL HEADER: tenant differente rifiutato"
        );
    }

    @Test
    void rejectsDifferentCompany() {

        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialHeaderFactory.create(
                        2L, 9L, 7L,
                        order(), "GR-00025", DATE
                )
        );

        System.out.println(
                "PARTIAL HEADER: società differente rifiutata"
        );
    }

    @Test
    void rejectsUnconfirmedOrder() {

        PurchaseOrder po = order();

        when(po.getStatus()).thenReturn("DRAFT");

        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialHeaderFactory.create(
                        2L, 1L, 7L,
                        po, "GR-00025", DATE
                )
        );

        System.out.println(
                "PARTIAL HEADER: ordine non confermato rifiutato"
        );
    }

    @Test
    void rejectsInvalidWarehouseOrNumber() {

        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialHeaderFactory.create(
                        2L, 1L, null,
                        order(), "GR-00025", DATE
                )
        );

        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialHeaderFactory.create(
                        2L, 1L, 7L,
                        order(), " ", DATE
                )
        );

        System.out.println(
                "PARTIAL HEADER: magazzino o numero non validi rifiutati"
        );
    }
}
