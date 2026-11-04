package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.warehouse.inventory.movement.service.InventoryDocumentPostingService;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Componente interno del futuro workflow parziale.
 *
 * Il chiamante deve aver già acquisito il blocco del PO
 * e mantenere aperta la stessa transazione READ COMMITTED.
 *
 * NON definisce una nuova transazione.
 * NON è esposto a controller.
 */
@Component
class GoodsReceiptPartialPersistenceWriter {

    private final GoodsReceiptPartialPersistenceGate gate;
    private final GoodsReceiptPartialWriteTransactionGuard transactionGuard;
    private final GoodsReceiptRepository receiptRepository;
    private final GoodsReceiptLineRepository lineRepository;
    private final InventoryDocumentPostingService postingService;

    GoodsReceiptPartialPersistenceWriter(
            GoodsReceiptPartialPersistenceGate gate,
            GoodsReceiptPartialWriteTransactionGuard transactionGuard,
            GoodsReceiptRepository receiptRepository,
            GoodsReceiptLineRepository lineRepository,
            InventoryDocumentPostingService postingService) {

        this.gate = gate;
        this.transactionGuard = transactionGuard;
        this.receiptRepository = receiptRepository;
        this.lineRepository = lineRepository;
        this.postingService = postingService;
    }

    Long write(
            GoodsReceiptPartialDocumentAssembler.DocumentPlan plan) {

        // Prima di qualunque scrittura:
        // profilo test, feature flag, transazione e isolamento.
        transactionGuard.requireWritableReadCommitted();
        gate.requireEnabled();

        if (plan == null || plan.header() == null
                || plan.inputs() == null
                || plan.inputs().isEmpty()) {
            throw new BusinessException(
                    "Piano di ricevimento parziale incompleto."
            );
        }

        GoodsReceipt header = plan.header();

        if (header.getId() != null
                || header.getTenantId() == null
                || header.getCompanyId() == null
                || header.getPurchaseOrderId() == null
                || header.getWarehouseId() == null
                || header.getReceiptNumber() == null
                || header.getReceiptNumber().isBlank()) {
            throw new BusinessException(
                    "Testata del ricevimento non valida."
            );
        }

        GoodsReceipt saved = receiptRepository.save(header);

        if (saved == null || saved.getId() == null
                || saved.getId() <= 0) {
            throw new BusinessException(
                    "ID del ricevimento non generato."
            );
        }

        Long receiptId = saved.getId();

        List<GoodsReceiptLine> rows = plan.buildLines(receiptId);

        if (rows.isEmpty()) {
            throw new BusinessException(
                    "Il ricevimento parziale non contiene righe."
            );
        }

        lineRepository.saveAll(rows);

        postingService.postPurchaseReceiptFromGoodsReceiptV2(
                header.getTenantId(),
                header.getCompanyId(),
                receiptId
        );

        return receiptId;
    }
}
