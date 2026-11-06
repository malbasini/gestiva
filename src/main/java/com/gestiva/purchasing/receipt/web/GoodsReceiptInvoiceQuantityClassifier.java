package com.gestiva.purchasing.receipt.web;

import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Classificazione esclusivamente quantitativa.
 *
 * Non verifica feature gate, autorizzazioni, duplicati,
 * prezzi, fiscalità o fatturabilità complessiva.
 *
 * Non viene ancora utilizzata dal controller o dalla view.
 */
public final class GoodsReceiptInvoiceQuantityClassifier {

    public enum Result {
        COMPLETE,
        PARTIAL,
        INVALID
    }

    private GoodsReceiptInvoiceQuantityClassifier() {
    }

    public static Result classify(
            List<PurchaseOrderLine> orderLines,
            List<GoodsReceiptLine> receiptLines) {

        if (orderLines == null || orderLines.isEmpty()
                || receiptLines == null || receiptLines.isEmpty()) {
            return Result.INVALID;
        }

        Map<Long, PurchaseOrderLine> orderById = new HashMap<>();

        for (PurchaseOrderLine orderLine : orderLines) {
            if (orderLine == null
                    || orderLine.getId() == null
                    || orderLine.getId() <= 0
                    || orderLine.getItemId() == null
                    || orderLine.getUomId() == null
                    || orderLine.getQuantity() == null
                    || orderLine.getQuantity().signum() <= 0
                    || orderLine.getConversionFactor() == null
                    || orderLine.getConversionFactor().signum() <= 0
                    || orderById.putIfAbsent(
                            orderLine.getId(), orderLine) != null) {

                return Result.INVALID;
            }
        }

        Set<Long> receivedOrderLineIds = new HashSet<>();
        boolean complete = true;

        for (GoodsReceiptLine receiptLine : receiptLines) {
            if (receiptLine == null
                    || receiptLine.getPurchaseOrderLineId() == null
                    || receiptLine.getQuantityReceived() == null
                    || receiptLine.getQuantityReceived().signum() <= 0) {
                return Result.INVALID;
            }

            Long orderLineId = receiptLine.getPurchaseOrderLineId();

            PurchaseOrderLine orderLine = orderById.get(orderLineId);

            if (orderLine == null
                    || !receivedOrderLineIds.add(orderLineId)
                    || !Objects.equals(
                            receiptLine.getItemId(),
                            orderLine.getItemId())) {

                return Result.INVALID;
            }

            BigDecimal orderedBase;

            try {
                orderedBase = orderLine.getQuantity()
                        .multiply(orderLine.getConversionFactor())
                        .setScale(3, RoundingMode.UNNECESSARY);

                BigDecimal receivedBase =
                        receiptLine.getQuantityReceived()
                                .setScale(3, RoundingMode.UNNECESSARY);

                int comparison = receivedBase.compareTo(orderedBase);

                if (comparison > 0) {
                    return Result.INVALID;
                }

                if (comparison < 0) {
                    complete = false;
                }

            } catch (ArithmeticException ex) {
                return Result.INVALID;
            }
        }

        // Un ricevimento completo deve comprendere
        // tutte le righe dell'ordine, non solo alcune.
        if (receivedOrderLineIds.size() != orderById.size()) {
            complete = false;
        }

        return complete ? Result.COMPLETE : Result.PARTIAL;
    }
}
