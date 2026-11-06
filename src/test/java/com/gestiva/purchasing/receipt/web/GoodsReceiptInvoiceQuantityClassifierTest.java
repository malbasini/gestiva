package com.gestiva.purchasing.receipt.web;

import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.gestiva.purchasing.receipt.web.GoodsReceiptInvoiceQuantityClassifier.Result.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GoodsReceiptInvoiceQuantityClassifierTest {

    private PurchaseOrderLine order(
            Long id, Long itemId,
            String quantity, String factor) {

        PurchaseOrderLine line = mock(PurchaseOrderLine.class);

        when(line.getId()).thenReturn(id);
        when(line.getItemId()).thenReturn(itemId);
        when(line.getUomId()).thenReturn(9L);
        when(line.getQuantity())
                .thenReturn(new BigDecimal(quantity));
        when(line.getConversionFactor())
                .thenReturn(new BigDecimal(factor));

        return line;
    }

    private GoodsReceiptLine receipt(
            Long orderLineId,
            Long itemId,
            String quantity) {

        GoodsReceiptLine line = mock(GoodsReceiptLine.class);

        when(line.getPurchaseOrderLineId())
                .thenReturn(orderLineId);
        when(line.getItemId()).thenReturn(itemId);
        when(line.getQuantityReceived())
                .thenReturn(new BigDecimal(quantity));

        return line;
    }

    @Test
    void completeReceipt() {
        assertEquals(COMPLETE,
                GoodsReceiptInvoiceQuantityClassifier.classify(
                        List.of(order(10L, 1L, "2", "10")),
                        List.of(receipt(10L, 1L, "20"))
                ));
    }

    @Test
    void partialReceipt() {
        assertEquals(PARTIAL,
                GoodsReceiptInvoiceQuantityClassifier.classify(
                        List.of(order(10L, 1L, "2", "10")),
                        List.of(receipt(10L, 1L, "8"))
                ));
    }

    @Test
    void missingOrderLineIsNotComplete() {
        assertEquals(PARTIAL,
                GoodsReceiptInvoiceQuantityClassifier.classify(
                        List.of(
                                order(10L, 1L, "2", "10"),
                                order(11L, 2L, "3", "1")
                        ),
                        List.of(receipt(10L, 1L, "20"))
                ));
    }

    @Test
    void allOrderLinesPresentAreComplete() {
        assertEquals(COMPLETE,
                GoodsReceiptInvoiceQuantityClassifier.classify(
                        List.of(
                                order(10L, 1L, "2", "10"),
                                order(11L, 2L, "3", "1")
                        ),
                        List.of(
                                receipt(10L, 1L, "20"),
                                receipt(11L, 2L, "3")
                        )
                ));
    }

    @Test
    void excessQuantityIsInvalid() {
        assertEquals(INVALID,
                GoodsReceiptInvoiceQuantityClassifier.classify(
                        List.of(order(10L, 1L, "2", "10")),
                        List.of(receipt(10L, 1L, "21"))
                ));
    }

    @Test
    void duplicatedReceiptLineIsInvalid() {
        assertEquals(INVALID,
                GoodsReceiptInvoiceQuantityClassifier.classify(
                        List.of(order(10L, 1L, "2", "10")),
                        List.of(
                                receipt(10L, 1L, "8"),
                                receipt(10L, 1L, "7")
                        )
                ));
    }

    @Test
    void unknownOrderLineIsInvalid() {
        assertEquals(INVALID,
                GoodsReceiptInvoiceQuantityClassifier.classify(
                        List.of(order(10L, 1L, "2", "10")),
                        List.of(receipt(99L, 1L, "8"))
                ));
    }

    @Test
    void mismatchingItemIsInvalid() {
        assertEquals(INVALID,
                GoodsReceiptInvoiceQuantityClassifier.classify(
                        List.of(order(10L, 1L, "2", "10")),
                        List.of(receipt(10L, 2L, "8"))
                ));
    }

    @Test
    void nonIntegralBasePrecisionIsInvalid() {
        assertEquals(INVALID,
                GoodsReceiptInvoiceQuantityClassifier.classify(
                        List.of(order(10L, 1L, "1", "0.0005")),
                        List.of(receipt(10L, 1L, "0.001"))
                ));
    }

    @Test
    void zeroReceivedQuantityIsInvalid() {
        assertEquals(INVALID,
                GoodsReceiptInvoiceQuantityClassifier.classify(
                        List.of(order(10L, 1L, "2", "10")),
                        List.of(receipt(10L, 1L, "0"))
                ));
    }

    @Test
    void missingLinesAreInvalid() {
        assertEquals(INVALID,
                GoodsReceiptInvoiceQuantityClassifier.classify(
                        List.of(order(10L, 1L, "2", "10")),
                        List.of()
                ));
    }
}
