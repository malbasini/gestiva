package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.security.company.context.CompanyContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Service
public class GoodsReceiptPartialApplicationService {

    private final GoodsReceiptPartialPersistenceGate gate;
    private final GoodsReceiptPartialCoordinator coordinator;
    private final PurchaseOrderRepository orders;
    private final CompanyContext companyContext;

    public GoodsReceiptPartialApplicationService(
            GoodsReceiptPartialPersistenceGate gate,
            GoodsReceiptPartialCoordinator coordinator,
            PurchaseOrderRepository orders,
            CompanyContext companyContext) {

        this.gate = gate;
        this.coordinator = coordinator;
        this.orders = orders;
        this.companyContext = companyContext;
    }

    /**
     * Ingresso applicativo per un ricevimento parziale.
     *
     * La testata viene costruita da dati attendibili:
     * tenant, società e ordine sono verificati dal server.
     *
     * Il coordinatore mantiene la responsabilità del lock
     * pessimistico, della pianificazione e del posting FIFO.
     */
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class
    )
    public Long create(
            Long tenantId,
            Long purchaseOrderId,
            Long warehouseId,
            Map<Long, BigDecimal> requestedQuantities) {

        // Nessuna lettura documentale prima del gate.
        gate.requireEnabled();

        if (tenantId == null || tenantId <= 0
                || purchaseOrderId == null || purchaseOrderId <= 0
                || warehouseId == null || warehouseId <= 0
                || requestedQuantities == null
                || requestedQuantities.isEmpty()) {

            throw new BusinessException(
                    "Dati del ricevimento parziale non validi."
            );
        }

        Long companyId = companyContext.requireCurrentCompanyId();

        var order = orders
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        purchaseOrderId
                )
                .orElseThrow(() -> new BusinessException(
                        "Ordine fornitore non trovato nella società corrente."
                ));

        GoodsReceipt header = new GoodsReceipt();
        header.setTenantId(tenantId);
        header.setCompanyId(companyId);
        header.setPurchaseOrderId(purchaseOrderId);
        header.setSupplierId(order.getSupplierId());
        header.setWarehouseId(warehouseId);
        header.setLocationId(null);
        header.setReceiptDate(LocalDate.now());

        // Planning e scrittura restano nella stessa transazione.
        return coordinator.create(header, requestedQuantities);
    }
}
