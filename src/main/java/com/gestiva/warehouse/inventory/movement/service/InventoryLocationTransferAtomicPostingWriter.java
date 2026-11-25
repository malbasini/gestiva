package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransfer;
import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransferAllocation;
import com.gestiva.warehouse.inventory.movement.entity.InventoryMovement;
import com.gestiva.warehouse.inventory.movement.repository.InventoryLocationTransferRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryLocationTransferAllocationRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayerConsumption;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerConsumptionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Scrittura atomica di un trasferimento FIFO.
 *
 * COMPONENTE NON COLLEGATO AL COORDINATORE PUBBLICO.
 *
 * Richiede una transazione Spring già attiva.
 * Tutte le scritture partecipano alla stessa transazione.
 *
 * Non invoca applyOutboundValuation/applyInboundValuation:
 * consumi e layer destinazione sono costruiti direttamente
 * dal medesimo piano FIFO.
 */
@Service
public class InventoryLocationTransferAtomicPostingWriter {

    private final WarehouseRepository warehouseRepository;
    private final InventoryLocationTransferRepository transferRepository;
    private final InventoryLocationTransferAllocationRepository allocationRepository;
    private final InventoryLayerRepository layerRepository;
    private final InventoryLayerConsumptionRepository consumptionRepository;
    private final InventoryMovementRepository movementRepository;
    private final InventoryMovementService movementService;

    public InventoryLocationTransferAtomicPostingWriter(
            WarehouseRepository warehouseRepository,
            InventoryLocationTransferRepository transferRepository,
            InventoryLocationTransferAllocationRepository allocationRepository,
            InventoryLayerRepository layerRepository,
            InventoryLayerConsumptionRepository consumptionRepository,
            InventoryMovementRepository movementRepository,
            InventoryMovementService movementService) {

        this.warehouseRepository =
                Objects.requireNonNull(warehouseRepository);
        this.transferRepository =
                Objects.requireNonNull(transferRepository);
        this.allocationRepository =
                Objects.requireNonNull(allocationRepository);
        this.layerRepository =
                Objects.requireNonNull(layerRepository);
        this.consumptionRepository =
                Objects.requireNonNull(consumptionRepository);
        this.movementRepository =
                Objects.requireNonNull(movementRepository);
        this.movementService =
                Objects.requireNonNull(movementService);
    }

    @Transactional(
            propagation = Propagation.MANDATORY,
            rollbackFor = Exception.class
    )
    public Long write(
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

        InventoryLocationTransferPostingRequestValidator.validate(
                tenantId,
                companyId,
                warehouseId,
                itemId,
                sourceLocationId,
                targetLocationId,
                quantity,
                movementDate,
                notes,
                operationKey
        );

        // Prima il lock del magazzino, poi quelli FIFO.
        warehouseRepository.findByTenantAndCompanyAndIdForUpdate(
                tenantId, companyId, warehouseId
        ).orElseThrow(() -> new BusinessException(
                "Magazzino non trovato nella società attiva."
        ));

        // Il writer non decide il riutilizzo idempotente.
        // Un'operazione esistente deve essere gestita
        // esclusivamente dal coordinatore.
        if (transferRepository
                .findByTenantIdAndCompanyIdAndOperationKey(
                        tenantId, companyId, operationKey
                ).isPresent()) {
            throw new BusinessException(
                    "Chiave di trasferimento già utilizzata."
            );
        }

        List<InventoryLayer> sourceLayers =
                layerRepository.findTransferSourceLayersForUpdate(
                        tenantId,
                        companyId,
                        warehouseId,
                        itemId,
                        sourceLocationId
                );

        var fifoAllocations =
                new InventoryLocationTransferFifoPlanner().plan(
                        sourceLayers,
                        tenantId,
                        companyId,
                        warehouseId,
                        itemId,
                        sourceLocationId,
                        quantity
                );

        var plan = InventoryLocationTransferPostingPlan
                .fromAllocations(fifoAllocations, quantity);

        BigDecimal averageUnitCost = plan.totalCost()
                .divide(plan.quantity(), 4, RoundingMode.HALF_UP);

        Map<Long, InventoryLayer> sourceById = new HashMap<>();

        for (InventoryLayer source : sourceLayers) {
            if (sourceById.putIfAbsent(
                    source.getId(), source) != null) {
                throw new BusinessException(
                        "Layer sorgente FIFO duplicato."
                );
            }
        }

        InventoryLocationTransfer transfer =
                new InventoryLocationTransfer();

        transfer.setTenantId(tenantId);
        transfer.setCompanyId(companyId);
        transfer.setWarehouseId(warehouseId);
        transfer.setItemId(itemId);
        transfer.setSourceLocationId(sourceLocationId);
        transfer.setTargetLocationId(targetLocationId);
        transfer.setQuantity(plan.quantity());
        transfer.setMovementDate(movementDate);
        transfer.setNotes(notes);
        transfer.setOperationKey(operationKey);
        transfer.setStatus("PENDING");

        transfer = transferRepository.saveAndFlush(transfer);

        requireId(transfer.getId(), "ID trasferimento");

        Long outId = movementService.registerMovement(
                tenantId,
                companyId,
                warehouseId,
                sourceLocationId,
                itemId,
                movementDate,
                "OUT",
                "LOCATION_TRANSFER_OUT",
                plan.quantity(),
                null,
                "LOCATION_TRANSFER",
                transfer.getId(),
                notes
        );

        Long inId = movementService.registerMovement(
                tenantId,
                companyId,
                warehouseId,
                targetLocationId,
                itemId,
                movementDate,
                "IN",
                "LOCATION_TRANSFER_IN",
                plan.quantity(),
                averageUnitCost,
                "LOCATION_TRANSFER",
                transfer.getId(),
                notes
        );

        requireId(outId, "Movimento OUT");
        requireId(inId, "Movimento IN");

        if (outId.equals(inId)) {
            throw new BusinessException(
                    "I movimenti OUT e IN devono essere distinti."
            );
        }

        InventoryMovement out = movementRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, outId
                ).orElseThrow(() -> new BusinessException(
                        "Movimento OUT non trovato dopo la registrazione."
                ));

        InventoryMovement in = movementRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, inId
                ).orElseThrow(() -> new BusinessException(
                        "Movimento IN non trovato dopo la registrazione."
                ));

        // Il costo complessivo è la somma dei costi
        // dei singoli layer, non qty * costo medio
        // arrotondato.
        out.setUnitCost(averageUnitCost);
        out.setTotalCost(plan.totalCost());
        in.setUnitCost(averageUnitCost);
        in.setTotalCost(plan.totalCost());

        movementRepository.save(out);
        movementRepository.save(in);

        List<InventoryLocationTransferAllocation> savedAllocations =
                new ArrayList<>();

        List<InventoryLayerConsumption> savedConsumptions =
                new ArrayList<>();

        List<InventoryLayer> destinationLayers =
                new ArrayList<>();

        for (var allocation : plan.allocations()) {
            InventoryLayer source =
                    sourceById.get(allocation.sourceLayerId());

            if (source == null
                    || source.isClosed()
                    || source.getRemainingQty() == null
                    || source.getRemainingQty().compareTo(
                    allocation.quantity()) < 0) {
                throw new BusinessException(
                        "Disponibilità FIFO cambiata durante il trasferimento."
                );
            }

            BigDecimal remaining = source.getRemainingQty()
                    .subtract(allocation.quantity())
                    .setScale(3, RoundingMode.UNNECESSARY);

            source.setRemainingQty(remaining);
            source.setClosed(remaining.signum() == 0);
            layerRepository.save(source);

            BigDecimal lineCost = allocation.quantity()
                    .multiply(allocation.unitCost())
                    .setScale(4, RoundingMode.HALF_UP);

            InventoryLayerConsumption consumption =
                    new InventoryLayerConsumption();

            consumption.setTenantId(tenantId);
            consumption.setCompanyId(companyId);
            consumption.setWarehouseId(warehouseId);
            consumption.setOutMovementId(outId);
            consumption.setLayerId(allocation.sourceLayerId());
            consumption.setConsumedQty(allocation.quantity());
            consumption.setUnitCost(allocation.unitCost());
            consumption.setTotalCost(lineCost);

            savedConsumptions.add(
                    consumptionRepository.save(consumption)
            );

            InventoryLayer target = new InventoryLayer();

            target.setTenantId(tenantId);
            target.setCompanyId(companyId);
            target.setWarehouseId(warehouseId);
            target.setLocationId(targetLocationId);
            target.setItemId(itemId);
            target.setSourceMovementId(inId);
            target.setLayerDate(allocation.layerDate());
            target.setOriginalQty(allocation.quantity());
            target.setRemainingQty(allocation.quantity());
            target.setUnitCost(allocation.unitCost());
            target.setClosed(false);

            target = layerRepository.saveAndFlush(target);
            requireId(target.getId(), "Layer destinazione");

            destinationLayers.add(target);

            InventoryLocationTransferAllocation persisted =
                    new InventoryLocationTransferAllocation();

            persisted.setTenantId(tenantId);
            persisted.setCompanyId(companyId);
            persisted.setWarehouseId(warehouseId);
            persisted.setTransferId(transfer.getId());
            persisted.setSourceLayerId(allocation.sourceLayerId());
            persisted.setTargetLayerId(target.getId());
            persisted.setAllocatedQty(allocation.quantity());
            persisted.setUnitCost(allocation.unitCost());
            persisted.setLayerDate(allocation.layerDate());

            savedAllocations.add(
                    allocationRepository.save(persisted)
            );
        }

        transfer.setOutMovementId(outId);
        transfer.setInMovementId(inId);
        transfer.setStatus("POSTED");

        // Verifica in memoria la riconciliazione prima
        // di completare il posting.
        InventoryLocationTransferPostedCompletenessVerifier.verify(
                transfer,
                out,
                in,
                savedAllocations,
                savedConsumptions,
                destinationLayers
        );

        transferRepository.saveAndFlush(transfer);

        return transfer.getId();
    }

    private static void requireId(Long value, String label) {
        if (value == null || value <= 0) {
            throw new BusinessException(label + " non valido.");
        }
    }
}
