package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class GoodsReceiptPartialLineFactory {

    private GoodsReceiptPartialLineFactory() {
    }

    /**
     * Costruisce una riga del ricevimento in memoria.
     *
     * Nessun accesso al database.
     * Nessuna registrazione di magazzino.
     */
    public static GoodsReceiptLine create(
            Long tenantId,
            Long goodsReceiptId,
            int lineNo,
            PurchaseOrderLine orderLine,
            GoodsReceiptPartialFifoLineCalculator.PlannedLine planned) {

        if (tenantId == null
                || tenantId <= 0
                || goodsReceiptId == null
                || goodsReceiptId <= 0
                || lineNo <= 0
                || orderLine == null
                || orderLine.getId() == null
                || planned == null) {

            throw new BusinessException(
                    "Dati della riga di ricevimento incompleti."
            );
        }

        if (orderLine.getDescription() == null
                || orderLine.getDescription().isBlank()
                || orderLine.getDescription().length() > 500) {

            throw new BusinessException(
                    "Descrizione della riga di ricevimento non valida."
            );
        }

        // Verifica nuovamente la quadratura FIFO anche
        // quando PlannedLine proviene da un altro chiamante.
        BigDecimal checkedUnitCost =
                GoodsReceiptFifoCostReconciler.reconcile(
                        planned.quantityReceived(),
                        planned.totalCost()
                );

        if (planned.unitCost() == null
                || checkedUnitCost.compareTo(
                        planned.unitCost()) != 0) {

            throw new BusinessException(
                    "Costo unitario della riga non coerente con FIFO."
            );
        }

        GoodsReceiptLine line = new GoodsReceiptLine();

        line.setTenantId(tenantId);
        line.setGoodsReceiptId(goodsReceiptId);
        line.setLineNo(lineNo);
        line.setPurchaseOrderLineId(orderLine.getId());
        line.setItemId(orderLine.getItemId());
        line.setDescription(orderLine.getDescription());

        line.setQuantityReceived(
                planned.quantityReceived().setScale(
                        3, RoundingMode.UNNECESSARY
                )
        );

        line.setUnitCost(
                checkedUnitCost.setScale(
                        4, RoundingMode.UNNECESSARY
                )
        );

        line.setTotalCost(
                planned.totalCost().setScale(
                        4, RoundingMode.UNNECESSARY
                )
        );

        return line;
    }
}
