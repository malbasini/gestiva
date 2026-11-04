package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class GoodsReceiptPartialPreparationService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final GoodsReceiptService goodsReceiptService;
    private final CompanyContext companyContext;
    private final GoodsReceiptIsolationGuard isolationGuard;

    public GoodsReceiptPartialPreparationService(
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderLineRepository purchaseOrderLineRepository,
            GoodsReceiptService goodsReceiptService,
            CompanyContext companyContext,
            GoodsReceiptIsolationGuard isolationGuard) {

        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderLineRepository = purchaseOrderLineRepository;
        this.goodsReceiptService = goodsReceiptService;
        this.companyContext = companyContext;
        this.isolationGuard = isolationGuard;
    }

    /**
     * Prepara e valida le quantità per un ricevimento parziale.
     *
     * Il blocco dell'ordine precede qualsiasi lettura
     * aggregata delle quantità già ricevute.
     *
     * Non crea ricevimento, non registra FIFO e non
     * modifica lo stato dell'ordine.
     *
     * Deve essere invocato tramite il proxy Spring
     * come confine transazionale del futuro workflow.
     */
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED
    )
    public Map<Long, BigDecimal> prepare(
            Long tenantId,
            Long purchaseOrderId,
            Map<Long, BigDecimal> requestedQuantities) {

        if (tenantId == null || purchaseOrderId == null
                || requestedQuantities == null) {
            throw new BusinessException(
                    "Dati del ricevimento parziale incompleti."
            );
        }

        isolationGuard.requireReadCommitted();

        Long companyId = companyContext.requireCurrentCompanyId();

        if (companyId == null) {
            throw new BusinessException(
                    "Società corrente obbligatoria."
            );
        }

        // Prima operazione di lettura documentale:
        // acquisizione del blocco pessimista sull'ordine.
        var purchaseOrder = purchaseOrderRepository
                .findLockedByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        purchaseOrderId
                )
                .orElseThrow(() -> new BusinessException(
                        "Ordine fornitore non trovato."
                ));

        if (!"CONFIRMED".equalsIgnoreCase(
                purchaseOrder.getStatus())) {
            throw new BusinessException(
                    "Solo gli ordini confermati possono "
                    + "generare ricevimenti parziali."
            );
        }

        var orderLines = purchaseOrderLineRepository
                .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        tenantId,
                        purchaseOrderId
                );

        // La query delle quantità residue viene eseguita
        // soltanto dopo il blocco dell'ordine.
        return goodsReceiptService
                .validatePartialQuantitiesForLockedOrder(
                        tenantId,
                        companyId,
                        purchaseOrderId,
                        orderLines,
                        requestedQuantities
                );
    }
}
