package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GoodsReceiptPartialRequestValidator {

    private GoodsReceiptPartialRequestValidator() {
    }

    public static Map<Long, BigDecimal> validate(
            Map<Long, BigDecimal> requestedQuantities,
            Map<Long, BigDecimal> remainingQuantities) {

        if (requestedQuantities == null
                || requestedQuantities.isEmpty()) {
            throw new BusinessException(
                    "Seleziona almeno una riga da ricevere."
            );
        }

        if (remainingQuantities == null) {
            throw new BusinessException(
                    "Quantità residue dell'ordine non disponibili."
            );
        }

        Map<Long, BigDecimal> validated =
                new LinkedHashMap<>();

        for (var entry : requestedQuantities.entrySet()) {

            Long lineId = entry.getKey();

            if (lineId == null
                    || !remainingQuantities.containsKey(lineId)) {
                throw new BusinessException(
                        "La richiesta contiene una riga "
                        + "non appartenente all'ordine."
                );
            }

            BigDecimal quantity =
                    GoodsReceiptRequestedQuantityValidator.validate(
                            entry.getValue(),
                            remainingQuantities.get(lineId)
                    );

            validated.put(lineId, quantity);
        }

        return Collections.unmodifiableMap(validated);
    }
}
