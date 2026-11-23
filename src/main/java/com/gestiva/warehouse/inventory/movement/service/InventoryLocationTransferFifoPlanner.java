package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Gestiva ERP V2.
 *
 * Prepara il piano FIFO di un trasferimento tra ubicazioni.
 *
 * Non modifica InventoryLayer.
 * Non registra movimenti.
 * Non esegue operazioni di persistenza.
 *
 * I layer candidati devono essere forniti nell'ordine FIFO
 * layerDate ASC, id ASC dal chiamante autorizzato.
 */
public final class InventoryLocationTransferFifoPlanner {

    /**
     * Porzione di un layer FIFO da trasferire.
     */
    public record Allocation(
            Long sourceLayerId,
            BigDecimal quantity,
            BigDecimal unitCost,
            LocalDate layerDate
    ) {
    }

    /**
     * Prepara le allocazioni FIFO per una specifica ubicazione.
     *
     * Il metodo non muta la lista o le entità ricevute.
     */
    /**
     * Piano FIFO con isolamento esplicito per tenant,
     * società, magazzino e articolo.
     *
     * Verifica l'intera lista prima del calcolo.
     * Non modifica i layer ricevuti.
     */
    public List<Allocation> plan(
            List<InventoryLayer> candidates,
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long itemId,
            Long sourceLocationId,
            BigDecimal requestedQuantity
    ) {
        if (tenantId == null
                || companyId == null
                || warehouseId == null
                || itemId == null) {
            throw new BusinessException(
                    "Tenant, società, magazzino e articolo sono obbligatori."
            );
        }

        if (candidates == null) {
            throw new BusinessException(
                    "Layer FIFO e ubicazione di origine sono obbligatori."
            );
        }

        // Verifica tutti i candidati, anche quelli appartenenti
        // ad altre ubicazioni e quelli non necessari al prelievo.
        for (InventoryLayer candidate : candidates) {
            if (candidate == null) {
                throw new BusinessException(
                        "Il piano FIFO contiene un layer nullo."
                );
            }

            if (!Objects.equals(tenantId, candidate.getTenantId())
                    || !Objects.equals(companyId, candidate.getCompanyId())
                    || !Objects.equals(warehouseId, candidate.getWarehouseId())
                    || !Objects.equals(itemId, candidate.getItemId())) {
                throw new BusinessException(
                        "Il layer FIFO non appartiene al contesto del trasferimento."
                );
            }
        }

        return plan(
                candidates,
                sourceLocationId,
                requestedQuantity
        );
    }

    public List<Allocation> plan(
            List<InventoryLayer> candidates,
            Long sourceLocationId,
            BigDecimal requestedQuantity
    ) {

        if (candidates == null || sourceLocationId == null) {
            throw new BusinessException(
                    "Layer FIFO e ubicazione di origine sono obbligatori."
            );
        }

        if (requestedQuantity == null
                || requestedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "La quantità da trasferire deve essere maggiore di zero."
            );
        }

        final BigDecimal requested;

        try {
            requested = requestedQuantity.setScale(
                    3,
                    RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException ex) {
            throw new BusinessException(
                    "La quantità da trasferire non può superare tre decimali."
            );
        }

        // Gestiva V2 - Validazione preventiva dell'intera
        // sequenza FIFO prima di preparare le allocazioni.
        // Il chiamante deve fornire i layer ordinati
        // per layerDate ASC, id ASC.
        LocalDate previousDate = null;
        Long previousId = null;

        for (InventoryLayer candidate : candidates) {
            if (candidate == null) {
                throw new BusinessException(
                        "Il piano FIFO contiene un layer nullo."
                );
            }

            if (candidate.getId() == null
                    || candidate.getLayerDate() == null) {
                throw new BusinessException(
                        "Il layer FIFO presenta dati identificativi "
                        + "o di valorizzazione non validi."
                );
            }

            LocalDate currentDate = candidate.getLayerDate();
            Long currentId = candidate.getId();

            if (previousDate != null) {
                int dateComparison =
                        currentDate.compareTo(previousDate);

                if (dateComparison < 0
                        || (dateComparison == 0
                        && currentId.compareTo(previousId) <= 0)) {
                    throw new BusinessException(
                            "I layer FIFO non sono ordinati per data e ID."
                    );
                }
            }

            previousDate = currentDate;
            previousId = currentId;
        }

        BigDecimal remaining = requested;

        List<Allocation> allocations = new ArrayList<>();

        for (InventoryLayer layer : candidates) {

            if (layer == null) {
                throw new BusinessException(
                        "Il piano FIFO contiene un layer nullo."
                );
            }

            if (remaining.signum() == 0) {
                break;
            }

            if (!Objects.equals(
                    sourceLocationId,
                    layer.getLocationId()
            )) {
                continue;
            }

            if (layer.isClosed()) {
                continue;
            }

            if (layer.getRemainingQty() == null) {
                throw new BusinessException(
                        "Il layer FIFO non presenta una quantità residua valida."
                );
            }

            final BigDecimal available;

            try {
                available = layer.getRemainingQty().setScale(
                        3,
                        RoundingMode.UNNECESSARY
                );
            } catch (ArithmeticException ex) {
                throw new BusinessException(
                        "La quantità residua FIFO supera tre decimali."
                );
            }

            if (available.signum() < 0) {
                throw new BusinessException(
                        "Il layer FIFO presenta una quantità residua negativa."
                );
            }

            if (available.signum() == 0) {
                continue;
            }

            if (layer.getId() == null
                    || layer.getLayerDate() == null
                    || layer.getUnitCost() == null
                    || layer.getUnitCost().signum() <= 0) {
                throw new BusinessException(
                        "Il layer FIFO presenta dati identificativi "
                        + "o di valorizzazione non validi."
                );
            }

            BigDecimal allocatedQty = available.min(remaining);

            BigDecimal unitCost = layer.getUnitCost().setScale(
                    4,
                    RoundingMode.HALF_UP
            );

            allocations.add(
                    new Allocation(
                            layer.getId(),
                            allocatedQty,
                            unitCost,
                            layer.getLayerDate()
                    )
            );

            remaining = remaining.subtract(allocatedQty);
        }

        if (remaining.signum() > 0) {
            throw new BusinessException(
                    "Strati FIFO insufficienti nell'ubicazione di origine."
            );
        }

        return List.copyOf(allocations);
    }
}
