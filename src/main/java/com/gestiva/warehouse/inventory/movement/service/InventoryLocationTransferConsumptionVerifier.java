package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayerConsumption;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GESTIVA ERP V2.
 *
 * Riconcilia un Posting Plan FIFO con i consumi
 * effettivamente prodotti dalla valorizzazione OUT.
 *
 * Componente puro: nessun accesso al database.
 *
 * Il chiamante deve recuperare i consumi con una query
 * limitata a tenant, company, warehouse e outMovementId.
 *
 * La validazione del contesto e del movimento OUT
 * resta responsabilità del coordinatore transazionale.
 */
public final class InventoryLocationTransferConsumptionVerifier {

    private InventoryLocationTransferConsumptionVerifier() {
    }

    public static void verify(
            InventoryLocationTransferPostingPlan plan,
            List<InventoryLayerConsumption> consumptions) {

        if (plan == null || consumptions == null) {
            throw new BusinessException(
                    "Piano FIFO e consumi sono obbligatori."
            );
        }

        var allocations = plan.allocations();

        if (allocations == null || allocations.isEmpty()) {
            throw new BusinessException(
                    "Il piano FIFO non contiene allocazioni."
            );
        }

        if (consumptions.size() != allocations.size()) {
            throw new BusinessException(
                    "Il numero dei consumi FIFO non corrisponde al piano."
            );
        }

        Map<Long, InventoryLayerConsumption> bySourceLayer =
                new HashMap<>();

        for (InventoryLayerConsumption consumption : consumptions) {

            if (consumption == null
                    || consumption.getLayerId() == null
                    || consumption.getLayerId() <= 0) {
                throw new BusinessException(
                        "Consumo FIFO con layer di origine non valido."
                );
            }

            InventoryLayerConsumption previous =
                    bySourceLayer.putIfAbsent(
                            consumption.getLayerId(),
                            consumption
                    );

            if (previous != null) {
                throw new BusinessException(
                        "Consumo FIFO duplicato per lo stesso layer."
                );
            }
        }

        BigDecimal verifiedQuantity =
                BigDecimal.ZERO.setScale(3);

        BigDecimal verifiedTotalCost =
                BigDecimal.ZERO.setScale(4);

        for (var allocation : allocations) {

            if (allocation == null
                    || allocation.sourceLayerId() == null) {
                throw new BusinessException(
                        "Allocazione FIFO non valida."
                );
            }

            InventoryLayerConsumption consumption =
                    bySourceLayer.remove(
                            allocation.sourceLayerId()
                    );

            if (consumption == null) {
                throw new BusinessException(
                        "Manca un consumo FIFO previsto dal piano."
                );
            }

            BigDecimal plannedQuantity = exactPositive(
                    allocation.quantity(), 3, "Quantità pianificata"
            );

            BigDecimal plannedUnitCost = exactPositive(
                    allocation.unitCost(), 4, "Costo pianificato"
            );

            BigDecimal actualQuantity = exactPositive(
                    consumption.getConsumedQty(), 3, "Quantità consumata"
            );

            BigDecimal actualUnitCost = exactPositive(
                    consumption.getUnitCost(), 4, "Costo consumato"
            );

            BigDecimal actualTotalCost = exactPositive(
                    consumption.getTotalCost(), 4, "Valore del consumo"
            );

            if (plannedQuantity.compareTo(actualQuantity) != 0) {
                throw new BusinessException(
                        "La quantità consumata non coincide con il piano FIFO."
                );
            }

            if (plannedUnitCost.compareTo(actualUnitCost) != 0) {
                throw new BusinessException(
                        "Il costo unitario consumato non coincide con il piano FIFO."
                );
            }

            BigDecimal expectedCost = plannedQuantity
                    .multiply(plannedUnitCost)
                    .setScale(4, RoundingMode.HALF_UP);

            if (expectedCost.compareTo(actualTotalCost) != 0) {
                throw new BusinessException(
                        "Il valore del consumo non coincide con il piano FIFO."
                );
            }

            verifiedQuantity = verifiedQuantity.add(actualQuantity);

            verifiedTotalCost = verifiedTotalCost.add(actualTotalCost)
                    .setScale(4, RoundingMode.HALF_UP);
        }

        if (!bySourceLayer.isEmpty()) {
            throw new BusinessException(
                    "Sono presenti consumi FIFO non previsti dal piano."
            );
        }

        if (plan.quantity() == null
                || verifiedQuantity.compareTo(plan.quantity()) != 0) {
            throw new BusinessException(
                    "La quantità totale dei consumi FIFO non corrisponde al piano."
            );
        }

        if (plan.totalCost() == null
                || verifiedTotalCost.compareTo(plan.totalCost()) != 0) {
            throw new BusinessException(
                    "Il valore totale dei consumi FIFO non corrisponde al piano."
            );
        }
    }


    /**
     * Verifica il contesto dei consumi prima della
     * riconciliazione quantitativa ed economica.
     *
     * Il chiamante deve recuperare i consumi tramite
     * repository con scope tenant/company/warehouse/OUT.
     *
     * Non accede al database e non modifica i consumi.
     */
    public static void verify(
            InventoryLocationTransferPostingPlan plan,
            List<InventoryLayerConsumption> consumptions,
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long outMovementId) {

        requirePositiveId(tenantId, "Tenant");
        requirePositiveId(companyId, "Società");
        requirePositiveId(warehouseId, "Magazzino");
        requirePositiveId(outMovementId, "Movimento OUT");

        if (consumptions == null) {
            throw new BusinessException(
                    "I consumi FIFO sono obbligatori."
            );
        }

        for (InventoryLayerConsumption consumption : consumptions) {

            if (consumption == null) {
                throw new BusinessException(
                        "Il consumo FIFO non può essere nullo."
                );
            }

            if (!tenantId.equals(consumption.getTenantId())) {
                throw new BusinessException(
                        "Il consumo FIFO appartiene a un tenant differente."
                );
            }

            if (!companyId.equals(consumption.getCompanyId())) {
                throw new BusinessException(
                        "Il consumo FIFO appartiene a una società differente."
                );
            }

            if (!warehouseId.equals(consumption.getWarehouseId())) {
                throw new BusinessException(
                        "Il consumo FIFO appartiene a un magazzino differente."
                );
            }

            if (!outMovementId.equals(consumption.getOutMovementId())) {
                throw new BusinessException(
                        "Il consumo FIFO appartiene a un movimento OUT differente."
                );
            }
        }

        // Riutilizza senza duplicazioni la riconciliazione
        // di quantità, costi, layer e valore complessivo.
        verify(plan, consumptions);
    }

    private static void requirePositiveId(
            Long value,
            String description) {

        if (value == null || value <= 0) {
            throw new BusinessException(
                    description + " non valido."
            );
        }
    }

    private static BigDecimal exactPositive(
            BigDecimal value,
            int scale,
            String description) {

        if (value == null
                || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    description + " non valido."
            );
        }

        try {
            return value.setScale(
                    scale,
                    RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException exception) {
            throw new BusinessException(
                    description + " supera la precisione consentita."
            );
        }
    }
}
