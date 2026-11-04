package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class GoodsReceiptPartialDocumentAssembler {

    private GoodsReceiptPartialDocumentAssembler() {
    }

    public record LineInput(
            PurchaseOrderLine orderLine,
            GoodsReceiptPartialFifoLineCalculator.PlannedLine planned
    ) {
    }

    public record DocumentPlan(
            GoodsReceipt header,
            List<LineInput> inputs
    ) {

        /**
         * Genera le righe soltanto quando è disponibile
         * l'ID reale del ricevimento.
         *
         * Non effettua alcuna operazione di persistenza.
         */
        public List<GoodsReceiptLine> buildLines(
                Long savedReceiptId) {

            if (savedReceiptId == null || savedReceiptId <= 0) {
                throw new BusinessException(
                        "ID del ricevimento salvato non valido."
                );
            }

            List<GoodsReceiptLine> result = new ArrayList<>();

            int lineNo = 1;

            for (LineInput input : inputs) {
                result.add(
                        GoodsReceiptPartialLineFactory.create(
                                header.getTenantId(),
                                savedReceiptId,
                                lineNo++,
                                input.orderLine(),
                                input.planned()
                        )
                );
            }

            return List.copyOf(result);
        }
    }

    /**
     * Prepara in memoria la struttura del documento.
     *
     * Verifica che ogni riga appartenga allo stesso
     * tenant e ordine della testata.
     *
     * Non salva testate, righe o movimenti.
     */
    public static DocumentPlan prepare(
            GoodsReceipt header,
            List<LineInput> inputs) {

        if (header == null
                || header.getTenantId() == null
                || header.getCompanyId() == null
                || header.getPurchaseOrderId() == null
                || inputs == null
                || inputs.isEmpty()) {

            throw new BusinessException(
                    "Documento parziale incompleto."
            );
        }

        Set<Long> orderLineIds = new HashSet<>();

        for (LineInput input : inputs) {

            if (input == null
                    || input.orderLine() == null
                    || input.planned() == null) {

                throw new BusinessException(
                        "Riga del documento parziale incompleta."
                );
            }

            PurchaseOrderLine line = input.orderLine();

            if (line.getId() == null
                    || !Objects.equals(
                            header.getTenantId(),
                            line.getTenantId())
                    || !Objects.equals(
                            header.getPurchaseOrderId(),
                            line.getPurchaseOrderId())) {

                throw new BusinessException(
                        "Riga non appartenente all'ordine corrente."
                );
            }

            if (!orderLineIds.add(line.getId())) {
                throw new BusinessException(
                        "Riga ordine duplicata nel ricevimento."
                );
            }

            // Verifica anticipata della quadratura FIFO.
            var planned = input.planned();

            var unitCost =
                    GoodsReceiptFifoCostReconciler.reconcile(
                            planned.quantityReceived(),
                            planned.totalCost()
                    );

            if (planned.unitCost() == null
                    || unitCost.compareTo(
                            planned.unitCost()) != 0) {

                throw new BusinessException(
                        "Costo unitario FIFO della riga incoerente."
                );
            }
        }

        return new DocumentPlan(
                header,
                List.copyOf(inputs)
        );
    }
}
