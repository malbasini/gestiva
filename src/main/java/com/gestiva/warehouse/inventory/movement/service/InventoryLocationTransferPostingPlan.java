package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Piano immutabile per il futuro posting FIFO
 * dei trasferimenti tra ubicazioni.
 *
 * Non effettua operazioni di persistenza.
 *
 * Conserva le allocazioni FIFO originali e
 * calcola il valore totale del trasferimento
 * senza sostituire i costi dei singoli layer
 * con un costo medio.
 */
public record InventoryLocationTransferPostingPlan(
        List<InventoryLocationTransferFifoPlanner.Allocation> allocations,
        BigDecimal quantity,
        BigDecimal totalCost) {

    public InventoryLocationTransferPostingPlan {

        if (allocations == null || allocations.isEmpty()) {
            throw new BusinessException(
                    "Il piano FIFO deve contenere almeno un'allocazione."
            );
        }

        allocations = List.copyOf(allocations);

        if (quantity == null
                || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "La quantità del piano FIFO deve essere positiva."
            );
        }

        try {
            quantity = quantity.setScale(
                    3,
                    RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException exception) {
            throw new BusinessException(
                    "La quantità del piano FIFO non può superare tre decimali."
            );
        }

        if (totalCost == null
                || totalCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(
                    "Il valore del piano FIFO non è valido."
            );
        }

        try {
            totalCost = totalCost.setScale(
                    4,
                    RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException exception) {
            throw new BusinessException(
                    "Il valore del piano FIFO non può superare quattro decimali."
            );
        }

        // Ogni layer di origine può comparire una sola volta.
        // Il controllo vale anche per il costruttore diretto.
        java.util.Set<Long> sourceLayerIds =
                new java.util.HashSet<>();

        for (var allocation : allocations) {

            validateAllocation(allocation);

            if (!sourceLayerIds.add(allocation.sourceLayerId())) {
                throw new BusinessException(
                        "Il piano FIFO contiene layer di origine duplicati."
                );
            }
        }

        BigDecimal calculatedQuantity =
                BigDecimal.ZERO.setScale(3);

        BigDecimal calculatedCost =
                BigDecimal.ZERO.setScale(4);

        for (var allocation : allocations) {

            validateAllocation(allocation);

            BigDecimal allocationQuantity =
                    allocation.quantity().setScale(
                            3,
                            RoundingMode.UNNECESSARY
                    );

            BigDecimal allocationCost =
                    allocation.unitCost().setScale(
                            4,
                            RoundingMode.UNNECESSARY
                    );

            calculatedQuantity = calculatedQuantity.add(
                    allocationQuantity
            );

            calculatedCost = calculatedCost.add(
                    allocationQuantity.multiply(allocationCost)
                            .setScale(4, RoundingMode.HALF_UP)
            );
        }

        if (calculatedQuantity.compareTo(quantity) != 0) {
            throw new BusinessException(
                    "La somma delle allocazioni FIFO non coincide "
                    + "con la quantità del trasferimento."
            );
        }

        if (calculatedCost.compareTo(totalCost) != 0) {
            throw new BusinessException(
                    "Il valore totale FIFO non coincide "
                    + "con le allocazioni."
            );
        }
    }

    /**
     * Costruisce il piano dalle allocazioni FIFO
     * già determinate dal planner.
     */
    public static InventoryLocationTransferPostingPlan fromAllocations(
            List<InventoryLocationTransferFifoPlanner.Allocation> allocations,
            BigDecimal requestedQuantity) {

        if (allocations == null || allocations.isEmpty()) {
            throw new BusinessException(
                    "Il piano FIFO deve contenere almeno un'allocazione."
            );
        }

        if (requestedQuantity == null
                || requestedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "La quantità del piano FIFO deve essere positiva."
            );
        }

        try {
            requestedQuantity.setScale(
                    3,
                    RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException exception) {
            throw new BusinessException(
                    "La quantità del piano FIFO non può superare tre decimali."
            );
        }

        BigDecimal totalCost =
                BigDecimal.ZERO.setScale(4);

        for (var allocation : allocations) {

            validateAllocation(allocation);

            BigDecimal lineCost = allocation.quantity()
                    .multiply(allocation.unitCost())
                    .setScale(4, RoundingMode.HALF_UP);

            totalCost = totalCost.add(lineCost);
        }

        return new InventoryLocationTransferPostingPlan(
                allocations,
                requestedQuantity,
                totalCost
        );
    }

    private static void validateAllocation(
            InventoryLocationTransferFifoPlanner.Allocation allocation) {

        if (allocation == null) {
            throw new BusinessException(
                    "Allocazione FIFO nulla."
            );
        }

        if (allocation.sourceLayerId() == null
                || allocation.sourceLayerId() <= 0) {
            throw new BusinessException(
                    "Identificativo layer FIFO non valido."
            );
        }

        if (allocation.layerDate() == null) {
            throw new BusinessException(
                    "Data originale del layer FIFO obbligatoria."
            );
        }

        if (allocation.quantity() == null
                || allocation.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "Quantità allocata FIFO non valida."
            );
        }

        if (allocation.unitCost() == null
                || allocation.unitCost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "Costo unitario FIFO non valido."
            );
        }

        try {
            allocation.quantity().setScale(
                    3,
                    RoundingMode.UNNECESSARY
            );

            allocation.unitCost().setScale(
                    4,
                    RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException exception) {
            throw new BusinessException(
                    "Precisione delle allocazioni FIFO non valida."
            );
        }
    }
}
