package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

@Service
public class GoodsReceiptPartialPlanningService {

    private final GoodsReceiptPartialSnapshotService snapshots;
    private final GoodsReceiptIsolationGuard isolationGuard;

    public GoodsReceiptPartialPlanningService(
            GoodsReceiptPartialSnapshotService snapshots,
            GoodsReceiptIsolationGuard isolationGuard) {

        this.snapshots = snapshots;
        this.isolationGuard = isolationGuard;
    }

    /**
     * Prepara un piano economico nella stessa transazione
     * utilizzata per acquisire lo snapshot.
     *
     * Non crea documenti e non registra movimenti.
     * Il piano non deve essere usato per salvare dati
     * dopo la conclusione della transazione.
     */
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED
    )
    public GoodsReceiptPartialDocumentAssembler.DocumentPlan plan(
            GoodsReceipt header,
            Map<Long, BigDecimal> requestedQuantities) {

        if (header == null
                || header.getId() != null
                || header.getTenantId() == null
                || header.getTenantId() <= 0
                || header.getCompanyId() == null
                || header.getCompanyId() <= 0
                || header.getPurchaseOrderId() == null
                || header.getPurchaseOrderId() <= 0
                || header.getSupplierId() == null
                || header.getSupplierId() <= 0
                || header.getWarehouseId() == null
                || header.getWarehouseId() <= 0
                || header.getReceiptNumber() == null
                || header.getReceiptNumber().isBlank()
                || header.getReceiptDate() == null
                || requestedQuantities == null) {

            throw new BusinessException(
                    "Testata o quantità del ricevimento parziale non valide."
            );
        }

        isolationGuard.requireReadCommitted();

        // La preparazione dello snapshot partecipa
        // alla stessa transazione REQUIRED.
        var snapshot = snapshots.prepare(
                header.getTenantId(),
                header.getPurchaseOrderId(),
                requestedQuantities
        );

        var po = snapshot.purchaseOrder();

        if (po == null
                || !Objects.equals(
                        header.getTenantId(), snapshot.tenantId())
                || !Objects.equals(
                        header.getCompanyId(), snapshot.companyId())
                || !Objects.equals(
                        header.getTenantId(), po.getTenantId())
                || !Objects.equals(
                        header.getCompanyId(), po.getCompanyId())
                || !Objects.equals(
                        header.getPurchaseOrderId(), po.getId())
                || !Objects.equals(
                        header.getSupplierId(), po.getSupplierId())
                || !"CONFIRMED".equalsIgnoreCase(po.getStatus())) {

            throw new BusinessException(
                    "Testata non coerente con l'ordine bloccato."
            );
        }

        return GoodsReceiptPartialPlanBuilder.build(
                header,
                snapshot.orderLines(),
                snapshot.validatedQuantities(),
                snapshot.alreadyReceived()
        );
    }
}
