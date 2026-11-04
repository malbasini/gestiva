package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;

import java.time.LocalDate;
import java.util.Objects;

public final class GoodsReceiptPartialHeaderFactory {

    private GoodsReceiptPartialHeaderFactory() {
    }

    /**
     * Costruisce la testata in memoria.
     *
     * Il numero documento deve essere stato ottenuto
     * dal servizio sequenze nel workflow transazionale.
     *
     * Non salva documenti e non registra movimenti.
     */
    public static GoodsReceipt create(
            Long tenantId,
            Long companyId,
            Long warehouseId,
            PurchaseOrder purchaseOrder,
            String receiptNumber,
            LocalDate receiptDate) {

        if (tenantId == null || tenantId <= 0
                || companyId == null || companyId <= 0
                || warehouseId == null || warehouseId <= 0
                || purchaseOrder == null
                || receiptDate == null
                || receiptNumber == null
                || receiptNumber.isBlank()
                || receiptNumber.length() > 50) {

            throw new BusinessException(
                    "Dati della testata di ricevimento incompleti."
            );
        }

        if (!Objects.equals(
                tenantId, purchaseOrder.getTenantId())
                || !Objects.equals(
                        companyId, purchaseOrder.getCompanyId())) {

            throw new BusinessException(
                    "Ordine fornitore non appartenente "
                    + "al tenant e alla società correnti."
            );
        }

        if (purchaseOrder.getId() == null
                || purchaseOrder.getId() <= 0
                || purchaseOrder.getSupplierId() == null
                || purchaseOrder.getSupplierId() <= 0
                || purchaseOrder.getOrderNumber() == null
                || purchaseOrder.getOrderNumber().isBlank()) {

            throw new BusinessException(
                    "Riferimenti dell'ordine fornitore non validi."
            );
        }

        if (!"CONFIRMED".equalsIgnoreCase(
                purchaseOrder.getStatus())) {

            throw new BusinessException(
                    "Solo ordini confermati possono "
                    + "generare ricevimenti parziali."
            );
        }

        GoodsReceipt receipt = new GoodsReceipt();

        receipt.setTenantId(tenantId);
        receipt.setCompanyId(companyId);
        receipt.setWarehouseId(warehouseId);
        receipt.setLocationId(null);
        receipt.setReceiptNumber(receiptNumber);
        receipt.setReceiptDate(receiptDate);
        receipt.setPurchaseOrderId(purchaseOrder.getId());
        receipt.setSupplierId(purchaseOrder.getSupplierId());

        receipt.setNotes(
                "Ricevimento parziale da ordine fornitore "
                + purchaseOrder.getOrderNumber()
        );

        return receipt;
    }
}
