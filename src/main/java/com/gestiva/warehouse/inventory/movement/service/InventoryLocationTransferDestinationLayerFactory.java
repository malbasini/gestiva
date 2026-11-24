package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * GESTIVA ERP V2.
 *
 * Costruisce un layer FIFO di destinazione per una
 * singola allocazione di un trasferimento tra ubicazioni.
 *
 * Non persiste il layer e non modifica quello di origine.
 * La persistenza sarà responsabilità del coordinatore
 * transazionale, ancora disabilitato.
 */
public final class InventoryLocationTransferDestinationLayerFactory {

    private InventoryLocationTransferDestinationLayerFactory() {
    }

    public static InventoryLayer create(
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long itemId,
            Long targetLocationId,
            Long inboundMovementId,
            InventoryLocationTransferFifoPlanner.Allocation allocation) {

        requirePositiveId(tenantId, "Tenant");
        requirePositiveId(companyId, "Società");
        requirePositiveId(warehouseId, "Magazzino");
        requirePositiveId(itemId, "Articolo");
        requirePositiveId(targetLocationId, "Ubicazione destinazione");
        requirePositiveId(inboundMovementId, "Movimento IN");

        if (allocation == null) {
            throw new BusinessException(
                    "L'allocazione FIFO è obbligatoria."
            );
        }

        requirePositiveId(
                allocation.sourceLayerId(),
                "Layer di origine"
        );

        if (allocation.layerDate() == null) {
            throw new BusinessException(
                    "La data FIFO originale è obbligatoria."
            );
        }

        BigDecimal quantity = exactPositive(
                allocation.quantity(),
                3,
                "Quantità FIFO"
        );

        BigDecimal unitCost = exactPositive(
                allocation.unitCost(),
                4,
                "Costo unitario FIFO"
        );

        InventoryLayer layer = new InventoryLayer();

        layer.setTenantId(tenantId);
        layer.setCompanyId(companyId);
        layer.setWarehouseId(warehouseId);
        layer.setLocationId(targetLocationId);
        layer.setItemId(itemId);

        // Il nuovo layer nasce dal movimento IN del trasferimento.
        layer.setSourceMovementId(inboundMovementId);

        // Conservazione integrale della data FIFO storica.
        layer.setLayerDate(allocation.layerDate());

        // Ogni allocazione produce un layer distinto.
        layer.setOriginalQty(quantity);
        layer.setRemainingQty(quantity);
        layer.setUnitCost(unitCost);
        layer.setClosed(false);

        return layer;
    }

    private static void requirePositiveId(
            Long value,
            String field) {

        if (value == null || value <= 0) {
            throw new BusinessException(
                    field + " non valido."
            );
        }
    }

    private static BigDecimal exactPositive(
            BigDecimal value,
            int scale,
            String field) {

        if (value == null
                || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    field + " deve essere maggiore di zero."
            );
        }

        try {
            return value.setScale(
                    scale,
                    RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException exception) {
            throw new BusinessException(
                    field + " supera la precisione consentita."
            );
        }
    }
}
