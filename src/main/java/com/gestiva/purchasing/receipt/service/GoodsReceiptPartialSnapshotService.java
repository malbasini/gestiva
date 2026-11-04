package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoodsReceiptPartialSnapshotService {

    private final PurchaseOrderRepository orders;
    private final PurchaseOrderLineRepository lines;
    private final GoodsReceiptPartialHistoryReader historyReader;
    private final CompanyContext companyContext;
    private final GoodsReceiptIsolationGuard isolationGuard;

    public GoodsReceiptPartialSnapshotService(
            PurchaseOrderRepository orders,
            PurchaseOrderLineRepository lines,
            GoodsReceiptPartialHistoryReader historyReader,
            CompanyContext companyContext,
            GoodsReceiptIsolationGuard isolationGuard) {

        this.orders = orders;
        this.lines = lines;
        this.historyReader = historyReader;
        this.companyContext = companyContext;
        this.isolationGuard = isolationGuard;
    }

    public record Snapshot(
            Long tenantId,
            Long companyId,
            PurchaseOrder purchaseOrder,
            List<PurchaseOrderLine> orderLines,
            Map<Long, BigDecimal> alreadyReceived,
            Map<Long, BigDecimal> validatedQuantities
    ) {
    }

    /**
     * Snapshot coerente delle quantità sotto lock del PO.
     *
     * Non salva documenti né registra movimenti.
     *
     * Lo snapshot non deve essere utilizzato per scrivere
     * documenti dopo la conclusione della transazione.
     */
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED
    )
    public Snapshot prepare(
            Long tenantId,
            Long purchaseOrderId,
            Map<Long, BigDecimal> requestedQuantities) {

        if (tenantId == null || tenantId <= 0
                || purchaseOrderId == null || purchaseOrderId <= 0
                || requestedQuantities == null) {
            throw new BusinessException(
                    "Dati del ricevimento parziale incompleti."
            );
        }

        isolationGuard.requireReadCommitted();

        Long companyId = companyContext.requireCurrentCompanyId();

        if (companyId == null || companyId <= 0) {
            throw new BusinessException(
                    "Società corrente obbligatoria."
            );
        }

        PurchaseOrder po = orders
                .findLockedByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, purchaseOrderId
                )
                .orElseThrow(() -> new BusinessException(
                        "Ordine fornitore non trovato."
                ));

        if (!"CONFIRMED".equalsIgnoreCase(po.getStatus())) {
            throw new BusinessException(
                    "Solo ordini confermati possono "
                    + "generare ricevimenti parziali."
            );
        }

        List<PurchaseOrderLine> orderLines =
                lines.findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        tenantId, purchaseOrderId
                );

        if (orderLines == null || orderLines.isEmpty()) {
            throw new BusinessException(
                    "L'ordine fornitore non contiene righe."
            );
        }

        // Una lettura dello storico per ciascuna riga,
        // sempre dopo il lock dell'ordine.
        Map<Long, BigDecimal> historical =
                historyReader.read(
                        tenantId,
                        companyId,
                        purchaseOrderId,
                        orderLines
                );

        Map<Long, BigDecimal> remaining = new LinkedHashMap<>();

        for (PurchaseOrderLine line : orderLines) {
            if (line == null || line.getId() == null
                    || !historical.containsKey(line.getId())
                    || historical.get(line.getId()) == null) {
                throw new BusinessException(
                        "Snapshot storico dell'ordine incompleto."
                );
            }

            BigDecimal residual =
                    GoodsReceiptQuantityCalculator.remainingBaseQuantity(
                            line.getQuantity(),
                            line.getConversionFactor(),
                            historical.get(line.getId())
                    );

            if (remaining.putIfAbsent(
                    line.getId(), residual) != null) {
                throw new BusinessException(
                        "Riga ordine duplicata."
                );
            }
        }

        Map<Long, BigDecimal> validated =
                GoodsReceiptPartialRequestValidator.validate(
                        requestedQuantities, remaining
                );

        return new Snapshot(
                tenantId,
                companyId,
                po,
                List.copyOf(orderLines),
                java.util.Collections.unmodifiableMap(
                        new LinkedHashMap<>(historical)
                ),
                java.util.Collections.unmodifiableMap(
                        new LinkedHashMap<>(validated)
                )
        );
    }
}
