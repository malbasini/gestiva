package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class GoodsReceiptPartialHistoryReader {

    private final GoodsReceiptLineRepository repository;

    public GoodsReceiptPartialHistoryReader(
            GoodsReceiptLineRepository repository) {
        this.repository = repository;
    }

    /**
     * Legge le quantità storiche di tutte le righe PO.
     *
     * Deve essere chiamato nella transazione READ COMMITTED,
     * dopo il blocco PESSIMISTIC_WRITE dell'ordine.
     *
     * Non acquisisce autonomamente il blocco.
     * Non effettua scritture.
     */
    public Map<Long, BigDecimal> read(
            Long tenantId,
            Long companyId,
            Long purchaseOrderId,
            List<PurchaseOrderLine> orderLines) {

        if (tenantId == null || tenantId <= 0
                || companyId == null || companyId <= 0
                || purchaseOrderId == null || purchaseOrderId <= 0
                || orderLines == null || orderLines.isEmpty()) {

            throw new BusinessException(
                    "Dati dell'ordine incompleti per lo storico."
            );
        }

        Map<Long, BigDecimal> snapshot =
                new LinkedHashMap<>();

        for (PurchaseOrderLine line : orderLines) {

            if (line == null
                    || line.getId() == null
                    || !Objects.equals(
                            tenantId, line.getTenantId())
                    || !Objects.equals(
                            purchaseOrderId,
                            line.getPurchaseOrderId())) {

                throw new BusinessException(
                        "Riga ordine non valida per lo storico."
                );
            }

            Long lineId = line.getId();

            if (snapshot.containsKey(lineId)) {
                throw new BusinessException(
                        "Riga ordine duplicata nello storico."
                );
            }

            BigDecimal received =
                    repository.sumReceivedBaseQuantityByOrderLine(
                            tenantId,
                            companyId,
                            purchaseOrderId,
                            lineId
                    );

            if (received == null) {
                received = BigDecimal.ZERO;
            }

            if (received.signum() < 0) {
                throw new BusinessException(
                        "Quantità storica ricevuta negativa."
                );
            }

            snapshot.put(lineId, received);
        }

        return Collections.unmodifiableMap(snapshot);
    }
}
