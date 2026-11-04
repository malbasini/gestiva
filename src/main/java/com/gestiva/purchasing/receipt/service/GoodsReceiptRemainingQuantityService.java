package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GoodsReceiptRemainingQuantityService {

    private final GoodsReceiptLineRepository goodsReceiptLineRepository;

    public GoodsReceiptRemainingQuantityService(
            GoodsReceiptLineRepository goodsReceiptLineRepository) {

        this.goodsReceiptLineRepository = goodsReceiptLineRepository;
    }

    public BigDecimal calculateRemainingBaseQuantity(
            Long tenantId,
            Long companyId,
            Long purchaseOrderId,
            Long purchaseOrderLineId,
            BigDecimal orderedCommercialQuantity,
            BigDecimal conversionFactor) {

        if (tenantId == null || companyId == null
                || purchaseOrderId == null
                || purchaseOrderLineId == null) {
            throw new BusinessException(
                    "Tenant, società, ordine e riga sono obbligatori."
            );
        }

        BigDecimal alreadyReceived =
                goodsReceiptLineRepository
                        .sumReceivedBaseQuantityByOrderLine(
                                tenantId,
                                companyId,
                                purchaseOrderId,
                                purchaseOrderLineId
                        );

        // SUM SQL senza righe restituisce NULL.
        if (alreadyReceived == null) {
            alreadyReceived = BigDecimal.ZERO;
        }

        return GoodsReceiptQuantityCalculator.remainingBaseQuantity(
                orderedCommercialQuantity,
                conversionFactor,
                alreadyReceived
        );
    }
}
