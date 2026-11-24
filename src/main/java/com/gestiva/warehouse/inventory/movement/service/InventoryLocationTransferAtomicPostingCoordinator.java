package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryLocationTransferRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryLocationTransferAllocationRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerConsumptionRepository;
import com.gestiva.warehouse.inventory.valuation.service.InventoryValuationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * GESTIVA ERP V2.
 *
 * Coordinatore transazionale dedicato ai trasferimenti
 * FIFO tra ubicazioni dello stesso magazzino.
 *
 * STATO: NON OPERATIVO.
 *
 * Il posting sarà implementato soltanto dopo i test
 * di isolamento, lock, idempotenza, consumi FIFO,
 * layer di destinazione, rollback e concorrenza.
 *
 * Non introdurre scritture parziali in questa classe.
 */
@Service
public class InventoryLocationTransferAtomicPostingCoordinator {

    private final WarehouseRepository warehouseRepository;
    private final InventoryLocationTransferRepository transferRepository;
    private final InventoryLocationTransferAllocationRepository allocationRepository;
    private final InventoryLayerRepository layerRepository;
    private final InventoryLayerConsumptionRepository consumptionRepository;
    private final InventoryMovementService movementService;
    private final InventoryValuationService valuationService;

    public InventoryLocationTransferAtomicPostingCoordinator(
            WarehouseRepository warehouseRepository,
            InventoryLocationTransferRepository transferRepository,
            InventoryLocationTransferAllocationRepository allocationRepository,
            InventoryLayerRepository layerRepository,
            InventoryLayerConsumptionRepository consumptionRepository,
            InventoryMovementService movementService,
            InventoryValuationService valuationService) {

        this.warehouseRepository = java.util.Objects.requireNonNull(
                warehouseRepository, "warehouseRepository"
        );
        this.transferRepository = java.util.Objects.requireNonNull(
                transferRepository, "transferRepository"
        );
        this.allocationRepository = java.util.Objects.requireNonNull(
                allocationRepository, "allocationRepository"
        );
        this.layerRepository = java.util.Objects.requireNonNull(
                layerRepository, "layerRepository"
        );
        this.consumptionRepository = java.util.Objects.requireNonNull(
                consumptionRepository, "consumptionRepository"
        );
        this.movementService = java.util.Objects.requireNonNull(
                movementService, "movementService"
        );
        this.valuationService = java.util.Objects.requireNonNull(
                valuationService, "valuationService"
        );
    }

    /**
     * Contratto della futura registrazione atomica.
     *
     * Il metodo deve essere invocato attraverso il proxy
     * Spring affinché si applichi la transazione.
     *
     * @return ID del trasferimento, quando il posting
     *         sarà implementato e abilitato.
     */
    @Transactional(
            propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class
    )
    public Long post(
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long itemId,
            Long sourceLocationId,
            Long targetLocationId,
            BigDecimal quantity,
            LocalDate movementDate,
            String notes,
            String operationKey) {

        // Il coordinatore rimane intenzionalmente
        // disabilitato: nessun accesso ai repository,
        // nessun movimento e nessun layer creato.
        throw new BusinessException(
                "Coordinatore atomico dei trasferimenti FIFO non ancora abilitato."
        );
    }
}
