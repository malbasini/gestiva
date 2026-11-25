package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransfer;
import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransferAllocation;
import com.gestiva.warehouse.inventory.movement.entity.InventoryMovement;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayerConsumption;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Verifica strutturale e quantitativa di un trasferimento POSTED.
 *
 * Componente puro: non consulta repository e non effettua scritture.
 * Tutte le collezioni devono essere recuperate dal chiamante
 * attraverso repository limitati al contesto tenant/company.
 *
 * Verifica le evidenze persistite, non ricostruisce la storicità
 * delle giacenze residue dei layer sorgenti.
 */
public final class InventoryLocationTransferPostedCompletenessVerifier {

    private InventoryLocationTransferPostedCompletenessVerifier() {
    }

    public static void verify(
            InventoryLocationTransfer transfer,
            InventoryMovement out,
            InventoryMovement in,
            List<InventoryLocationTransferAllocation> allocations,
            List<InventoryLayerConsumption> consumptions,
            List<InventoryLayer> targetLayers) {

        InventoryLocationTransferPostingStateVerifier
                .verifyPostedState(transfer);

        positive(transfer.getId(), "ID trasferimento");
        positive(transfer.getTenantId(), "Tenant");
        positive(transfer.getCompanyId(), "Società");
        positive(transfer.getWarehouseId(), "Magazzino");
        positive(transfer.getItemId(), "Articolo");
        positive(transfer.getSourceLocationId(), "Ubicazione sorgente");
        positive(transfer.getTargetLocationId(), "Ubicazione destinazione");

        if (transfer.getSourceLocationId()
                .equals(transfer.getTargetLocationId())) {
            fail("Ubicazioni sorgente e destinazione coincidenti.");
        }

        if (transfer.getMovementDate() == null) {
            fail("Data trasferimento assente.");
        }

        BigDecimal requested = decimal(
                transfer.getQuantity(), 3, "Quantità trasferimento"
        );

        validateMovement(transfer, out, true);
        validateMovement(transfer, in, false);

        sameDecimal(requested, out.getQuantity(),
                "Quantità movimento OUT");
        sameDecimal(requested, in.getQuantity(),
                "Quantità movimento IN");

        if (allocations == null || allocations.isEmpty()
                || consumptions == null || targetLayers == null) {
            fail("Evidenze FIFO mancanti.");
        }

        if (allocations.size() != consumptions.size()
                || allocations.size() != targetLayers.size()) {
            fail("Numero di evidenze FIFO incoerente.");
        }

        Map<Long, InventoryLayerConsumption> consumptionBySource =
                new HashMap<>();

        for (InventoryLayerConsumption c : consumptions) {
            if (c == null) {
                fail("Consumo FIFO nullo.");
            }

            context(
                    transfer,
                    c.getTenantId(),
                    c.getCompanyId(),
                    c.getWarehouseId(),
                    "Consumo FIFO"
            );

            if (!Objects.equals(
                    transfer.getOutMovementId(),
                    c.getOutMovementId())) {
                fail("Consumo collegato a un OUT differente.");
            }

            positive(c.getLayerId(), "Layer consumato");

            if (consumptionBySource.putIfAbsent(
                    c.getLayerId(), c) != null) {
                fail("Consumo FIFO duplicato.");
            }
        }

        Map<Long, InventoryLayer> layerById = new HashMap<>();

        for (InventoryLayer layer : targetLayers) {
            if (layer == null) {
                fail("Layer destinazione nullo.");
            }

            context(
                    transfer,
                    layer.getTenantId(),
                    layer.getCompanyId(),
                    layer.getWarehouseId(),
                    "Layer destinazione"
            );

            positive(layer.getId(), "ID layer destinazione");

            if (!Objects.equals(
                    transfer.getItemId(), layer.getItemId())
                    || !Objects.equals(
                    transfer.getTargetLocationId(), layer.getLocationId())
                    || !Objects.equals(
                    transfer.getInMovementId(),
                    layer.getSourceMovementId())) {
                fail("Contesto layer destinazione incoerente.");
            }

            if (layerById.putIfAbsent(
                    layer.getId(), layer) != null) {
                fail("Layer destinazione duplicato.");
            }

            BigDecimal original = decimal(
                    layer.getOriginalQty(), 3,
                    "Quantità originale destinazione"
            );

            if (layer.getRemainingQty() == null
                    || layer.getRemainingQty()
                    .compareTo(BigDecimal.ZERO) < 0
                    || layer.getRemainingQty()
                    .compareTo(original) > 0) {
                fail("Quantità residua del layer non valida.");
            }
        }

        Set<Long> sources = new HashSet<>();
        Set<Long> targets = new HashSet<>();

        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (InventoryLocationTransferAllocation a : allocations) {
            if (a == null) {
                fail("Allocazione FIFO nulla.");
            }

            context(
                    transfer,
                    a.getTenantId(),
                    a.getCompanyId(),
                    a.getWarehouseId(),
                    "Allocazione FIFO"
            );

            if (!Objects.equals(
                    transfer.getId(), a.getTransferId())) {
                fail("Allocazione collegata a un altro trasferimento.");
            }

            positive(a.getSourceLayerId(), "Layer sorgente");
            positive(a.getTargetLayerId(), "Layer destinazione");

            if (!sources.add(a.getSourceLayerId())
                    || !targets.add(a.getTargetLayerId())) {
                fail("Layer duplicato nelle allocazioni FIFO.");
            }

            BigDecimal qty = decimal(
                    a.getAllocatedQty(), 3, "Quantità allocata"
            );
            BigDecimal cost = decimal(
                    a.getUnitCost(), 4, "Costo unitario allocato"
            );

            if (a.getLayerDate() == null) {
                fail("Data FIFO dell'allocazione mancante.");
            }

            InventoryLayerConsumption c =
                    consumptionBySource.remove(a.getSourceLayerId());

            InventoryLayer layer =
                    layerById.remove(a.getTargetLayerId());

            if (c == null || layer == null) {
                fail("Corrispondenza FIFO mancante.");
            }

            sameDecimal(qty, c.getConsumedQty(),
                    "Quantità consumo FIFO");
            sameDecimal(cost, c.getUnitCost(),
                    "Costo consumo FIFO");

            BigDecimal lineCost = qty.multiply(cost)
                    .setScale(4, RoundingMode.HALF_UP);

            sameDecimal(lineCost, c.getTotalCost(),
                    "Valore consumo FIFO");

            sameDecimal(qty, layer.getOriginalQty(),
                    "Quantità originale layer destinazione");
            sameDecimal(cost, layer.getUnitCost(),
                    "Costo layer destinazione");

            if (!a.getLayerDate().equals(layer.getLayerDate())) {
                fail("Data FIFO storica non conservata.");
            }

            totalQuantity = totalQuantity.add(qty);
            totalCost = totalCost.add(lineCost);
        }

        if (!consumptionBySource.isEmpty()
                || !layerById.isEmpty()) {
            fail("Evidenze FIFO non riconciliate.");
        }

        sameDecimal(requested, totalQuantity,
                "Totale quantità allocata");

        sameDecimal(totalCost, out.getTotalCost(),
                "Valore movimento OUT");
        sameDecimal(totalCost, in.getTotalCost(),
                "Valore movimento IN");
    }

    private static void validateMovement(
            InventoryLocationTransfer transfer,
            InventoryMovement movement,
            boolean outbound) {

        String label = outbound ? "OUT" : "IN";

        if (movement == null) {
            fail("Movimento " + label + " mancante.");
        }

        context(
                transfer,
                movement.getTenantId(),
                movement.getCompanyId(),
                movement.getWarehouseId(),
                "Movimento " + label
        );

        Long expectedId = outbound
                ? transfer.getOutMovementId()
                : transfer.getInMovementId();

        Long expectedLocation = outbound
                ? transfer.getSourceLocationId()
                : transfer.getTargetLocationId();

        if (!Objects.equals(expectedId, movement.getId())
                || !Objects.equals(
                transfer.getItemId(), movement.getItemId())
                || !Objects.equals(
                expectedLocation, movement.getLocationId())
                || !Objects.equals(
                transfer.getMovementDate(),
                movement.getMovementDate())) {
            fail("Identità movimento " + label + " incoerente.");
        }

        if (!label.equals(movement.getMovementType())
                || !"LOCATION_TRANSFER".equals(
                movement.getReferenceType())
                || !Objects.equals(
                transfer.getId(), movement.getReferenceId())) {
            fail("Tipo o riferimento movimento " + label + " incoerente.");
        }

        if (movement.getCausalCode() == null
                || movement.getCausalCode().isBlank()) {
            fail("Causale movimento " + label + " mancante.");
        }

        if (movement.isReversed()
                || movement.getReversalOfMovementId() != null) {
            fail("Movimento " + label + " stornato o compensativo.");
        }

        decimal(movement.getQuantity(), 3,
                "Quantità movimento " + label);
    }

    private static void context(
            InventoryLocationTransfer transfer,
            Long tenantId,
            Long companyId,
            Long warehouseId,
            String description) {

        if (!Objects.equals(transfer.getTenantId(), tenantId)
                || !Objects.equals(transfer.getCompanyId(), companyId)
                || !Objects.equals(
                transfer.getWarehouseId(), warehouseId)) {
            fail(description + " fuori contesto tenant/company/warehouse.");
        }
    }

    private static void positive(Long value, String label) {
        if (value == null || value <= 0) {
            fail(label + " non valido.");
        }
    }

    private static BigDecimal decimal(
            BigDecimal value, int scale, String label) {

        if (value == null || value.signum() <= 0) {
            fail(label + " non valido.");
        }

        try {
            return value.setScale(scale, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new BusinessException(
                    label + " supera la precisione consentita."
            );
        }
    }

    private static void sameDecimal(
            BigDecimal expected, BigDecimal actual, String label) {

        if (actual == null || expected.compareTo(actual) != 0) {
            fail(label + " non coerente.");
        }
    }

    private static void fail(String message) {
        throw new BusinessException(message);
    }
}
