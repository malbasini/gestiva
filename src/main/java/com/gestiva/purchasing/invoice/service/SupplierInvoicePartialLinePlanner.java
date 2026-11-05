package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Pianifica una riga di fattura da ricevimento parziale.
 *
 * Determina la quantità storica utilizzando gli ID dei GR,
 * non la sequenza di creazione delle fatture.
 *
 * Deve essere chiamato in una transazione che abbia già
 * acquisito il lock pessimista dell'ordine fornitore.
 *
 * Nessuna scrittura o assegnazione di numeratori.
 */
@Component
public class SupplierInvoicePartialLinePlanner {

    private final GoodsReceiptLineRepository receiptLines;

    public SupplierInvoicePartialLinePlanner(
            GoodsReceiptLineRepository receiptLines) {
        this.receiptLines = receiptLines;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public SupplierInvoicePartialBillabilityValidator.BillableLine plan(
            Long tenantId,
            Long companyId,
            Long purchaseOrderId,
            Long purchaseOrderLineId,
            Long currentGoodsReceiptId,
            BigDecimal receivedBaseQuantity,
            BigDecimal orderedBaseQuantity,
            BigDecimal conversionFactor,
            BigDecimal orderSubtotal,
            BigDecimal orderTax,
            BigDecimal orderTotal) {

        if (tenantId == null || tenantId <= 0
                || companyId == null || companyId <= 0
                || purchaseOrderId == null || purchaseOrderId <= 0
                || purchaseOrderLineId == null || purchaseOrderLineId <= 0
                || currentGoodsReceiptId == null
                || currentGoodsReceiptId <= 0) {
            throw new BusinessException(
                    "Identificativi della fatturazione parziale non validi."
            );
        }

        if (receivedBaseQuantity == null
                || receivedBaseQuantity.signum() <= 0) {
            throw new BusinessException(
                    "Quantità ricevuta non valida."
            );
        }

        BigDecimal previouslyReceived =
                receiptLines.sumReceivedBeforeReceiptByOrderLine(
                        tenantId,
                        companyId,
                        purchaseOrderId,
                        purchaseOrderLineId,
                        currentGoodsReceiptId
                );

        if (previouslyReceived == null) {
            previouslyReceived = BigDecimal.ZERO;
        }

        if (previouslyReceived.signum() < 0) {
            throw new BusinessException(
                    "Storico quantità ricevute non coerente."
            );
        }

        return SupplierInvoicePartialBillabilityValidator.validate(
                orderedBaseQuantity,
                previouslyReceived,
                receivedBaseQuantity,
                conversionFactor,
                orderSubtotal,
                orderTax,
                orderTotal
        );
    }
}
