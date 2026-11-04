package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class GoodsReceiptPartialPlanBuilder {

    private GoodsReceiptPartialPlanBuilder() {
    }

    /**
     * Prepara il documento usando quantità storiche già
     * lette sotto blocco dell'ordine fornitore.
     *
     * Il chiamante transazionale è responsabile del lock.
     * Questo componente non accede al database.
     */
    public static GoodsReceiptPartialDocumentAssembler.DocumentPlan build(
            GoodsReceipt header,
            List<PurchaseOrderLine> orderLines,
            Map<Long, BigDecimal> requestedQuantities,
            Map<Long, BigDecimal> alreadyReceivedQuantities) {

        if (header == null
                || header.getTenantId() == null
                || header.getPurchaseOrderId() == null
                || orderLines == null
                || orderLines.isEmpty()
                || alreadyReceivedQuantities == null) {
            throw new BusinessException(
                    "Dati per la pianificazione del ricevimento incompleti."
            );
        }

        Map<Long, BigDecimal> remaining = new LinkedHashMap<>();
        Map<Long, BigDecimal> orderedQuantities = new LinkedHashMap<>();

        for (PurchaseOrderLine line : orderLines) {

            if (line == null
                    || line.getId() == null
                    || !Objects.equals(
                            header.getTenantId(), line.getTenantId())
                    || !Objects.equals(
                            header.getPurchaseOrderId(),
                            line.getPurchaseOrderId())
                    || line.getUomId() == null
                    || line.getQuantity() == null
                    || line.getConversionFactor() == null
                    || line.getConversionFactor().signum() <= 0) {
                throw new BusinessException(
                        "Snapshot o riferimenti della riga ordine non validi."
                );
            }

            Long lineId = line.getId();

            if (!alreadyReceivedQuantities.containsKey(lineId)
                    || alreadyReceivedQuantities.get(lineId) == null) {
                throw new BusinessException(
                        "Quantità storica della riga ordine mancante."
                );
            }

            BigDecimal ordered;
            try {
                ordered = line.getQuantity()
                        .multiply(line.getConversionFactor())
                        .setScale(3, RoundingMode.UNNECESSARY);
            } catch (ArithmeticException ex) {
                throw new BusinessException(
                        "Quantità ordinata non convertibile esattamente."
                );
            }

            BigDecimal residual =
                    GoodsReceiptQuantityCalculator.remainingBaseQuantity(
                            line.getQuantity(),
                            line.getConversionFactor(),
                            alreadyReceivedQuantities.get(lineId)
                    );

            if (remaining.putIfAbsent(lineId, residual) != null) {
                throw new BusinessException(
                        "Righe ordine duplicate."
                );
            }

            orderedQuantities.put(lineId, ordered);
        }

        Map<Long, BigDecimal> validated =
                GoodsReceiptPartialRequestValidator.validate(
                        requestedQuantities,
                        remaining
                );

        List<GoodsReceiptPartialDocumentAssembler.LineInput> inputs =
                new ArrayList<>();

        // Conserviamo l'ordine originario delle righe PO.
        for (PurchaseOrderLine line : orderLines) {

            Long lineId = line.getId();
            BigDecimal requested = validated.get(lineId);

            if (requested == null) {
                continue;
            }

            var planned =
                    GoodsReceiptPartialFifoLineCalculator.calculate(
                            orderedQuantities.get(lineId),
                            alreadyReceivedQuantities.get(lineId),
                            requested,
                            line.getLineSubtotal()
                    );

            inputs.add(
                    new GoodsReceiptPartialDocumentAssembler.LineInput(
                            line,
                            planned
                    )
            );
        }

        return GoodsReceiptPartialDocumentAssembler.prepare(
                header, inputs
        );
    }
}
