package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransfer;
import com.gestiva.warehouse.inventory.movement.repository.InventoryLocationTransferAllocationRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerConsumptionRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Recupera e verifica le evidenze persistite di un
 * trasferimento POSTED.
 *
 * Solo letture, sempre limitate a tenant/company.
 *
 * Non autorizza il riutilizzo idempotente:
 * quella decisione resta al coordinatore transazionale.
 */
@Service
public class InventoryLocationTransferPostedEvidenceService {

    private final InventoryMovementRepository movementRepository;
    private final InventoryLocationTransferAllocationRepository allocationRepository;
    private final InventoryLayerConsumptionRepository consumptionRepository;
    private final InventoryLayerRepository layerRepository;

    public InventoryLocationTransferPostedEvidenceService(
            InventoryMovementRepository movementRepository,
            InventoryLocationTransferAllocationRepository allocationRepository,
            InventoryLayerConsumptionRepository consumptionRepository,
            InventoryLayerRepository layerRepository) {

        this.movementRepository =
                Objects.requireNonNull(movementRepository);

        this.allocationRepository =
                Objects.requireNonNull(allocationRepository);

        this.consumptionRepository =
                Objects.requireNonNull(consumptionRepository);

        this.layerRepository =
                Objects.requireNonNull(layerRepository);
    }

    @Transactional(readOnly = true)
    public void verifyPostedCompleteness(
            InventoryLocationTransfer transfer) {

        // Non effettuare letture di un trasferimento
        // PENDING o strutturalmente incompleto.
        InventoryLocationTransferPostingStateVerifier
                .verifyPostedState(transfer);

        Long tenantId = transfer.getTenantId();
        Long companyId = transfer.getCompanyId();
        Long warehouseId = transfer.getWarehouseId();
        Long transferId = transfer.getId();

        requireId(tenantId, "Tenant");
        requireId(companyId, "Società");
        requireId(warehouseId, "Magazzino");
        requireId(transferId, "Trasferimento");

        var out = movementRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        transfer.getOutMovementId()
                ).orElseThrow(() -> new BusinessException(
                        "Movimento OUT del trasferimento non trovato."
                ));

        var in = movementRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        transfer.getInMovementId()
                ).orElseThrow(() -> new BusinessException(
                        "Movimento IN del trasferimento non trovato."
                ));

        var allocations = allocationRepository
                .findByTenantIdAndCompanyIdAndTransferIdOrderByIdAsc(
                        tenantId,
                        companyId,
                        transferId
                );

        var consumptions = consumptionRepository
                .findByTenantIdAndCompanyIdAndWarehouseIdAndOutMovementIdOrderByIdAsc(
                        tenantId,
                        companyId,
                        warehouseId,
                        transfer.getOutMovementId()
                );

        var layers = layerRepository
                .findByTenantIdAndCompanyIdAndSourceMovementIdOrderByIdAsc(
                        tenantId,
                        companyId,
                        transfer.getInMovementId()
                );

        InventoryLocationTransferPostedCompletenessVerifier.verify(
                transfer,
                out,
                in,
                allocations,
                consumptions,
                layers
        );
    }

    private static void requireId(Long value, String name) {
        if (value == null || value <= 0) {
            throw new BusinessException(name + " non valido.");
        }
    }
}
