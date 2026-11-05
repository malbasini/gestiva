package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import com.gestiva.warehouse.location.repository.WarehouseRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * Coordinatore interno per il futuro workflow parziale.
 *
 * Non esposto a controller.
 * Non modifica il workflow completo esistente.
 *
 * La transazione copre pianificazione e scrittura.
 *
 * I ricevimenti multipli sono consentiti soltanto
 * entro le quantità residue validate dal planning.
 * Il vincolo UNIQUE sul purchase_order_id deve essere
 * assente nello schema utilizzato.
 */
@Service
class GoodsReceiptPartialCoordinator {

    private final GoodsReceiptPartialPersistenceGate gate;
    private final GoodsReceiptPartialPlanningService planning;
    private final WarehouseRepository warehouses;
    private final GoodsReceiptRepository receipts;
    private final DocumentSequenceService sequences;
    private final GoodsReceiptPartialPersistenceWriter writer;

    GoodsReceiptPartialCoordinator(
            GoodsReceiptPartialPersistenceGate gate,
            GoodsReceiptPartialPlanningService planning,
            WarehouseRepository warehouses,
            GoodsReceiptRepository receipts,
            DocumentSequenceService sequences,
            GoodsReceiptPartialPersistenceWriter writer) {

        this.gate = gate;
        this.planning = planning;
        this.warehouses = warehouses;
        this.receipts = receipts;
        this.sequences = sequences;
        this.writer = writer;
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class
    )
    public Long create(
            GoodsReceipt header,
            Map<Long, BigDecimal> requestedQuantities) {

        // Qualsiasi accesso documentale avviene soltanto
        // dopo la verifica del profilo, flag e isolamento.
        gate.requireEnabled();

        if (header == null
                || header.getId() != null
                || requestedQuantities == null) {
            throw new BusinessException(
                    "Dati del ricevimento parziale non validi."
            );
        }

        // Il numero reale NON viene scelto dal chiamante.
        // È assegnato dal servizio sequenze soltanto
        // dopo la validazione del piano.
        header.setReceiptNumber("PENDING-PARTIAL");

        // PlanningService partecipa alla stessa transazione.
        // Il blocco pessimista del PO rimane attivo
        // fino al termine di questo metodo transazionale.
        var plan = planning.plan(header, requestedQuantities);

        if (plan == null
                || plan.header() != header
                || plan.inputs() == null
                || plan.inputs().isEmpty()) {
            throw new BusinessException(
                    "Piano del ricevimento parziale non valido."
            );
        }

        Long tenantId = header.getTenantId();
        Long companyId = header.getCompanyId();
        Long purchaseOrderId = header.getPurchaseOrderId();
        Long warehouseId = header.getWarehouseId();

        // Magazzino appartenente alla stessa società.
        var warehouse = warehouses
                .findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, warehouseId
                )
                .orElseThrow(() -> new BusinessException(
                        "Magazzino non trovato nella società corrente."
                ));

        if (!warehouse.isActive()) {
            throw new BusinessException(
                    "Magazzino non attivo."
            );
        }

        String receiptNumber = sequences.nextNumber(
                tenantId,
                companyId,
                DocumentSequenceService.GOODS_RECEIPT
        );

        if (receiptNumber == null
                || receiptNumber.isBlank()
                || receiptNumber.length() > 50) {
            throw new BusinessException(
                    "Numero del ricevimento non valido."
            );
        }

        header.setReceiptNumber(receiptNumber);

        // Scrittura effettuata nella STESSA transazione
        // che mantiene il blocco dell'ordine.
        return Objects.requireNonNull(
                writer.write(plan),
                "ID del ricevimento mancante."
        );
    }
}
