package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransfer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * GESTIVA ERP V2.
 *
 * Verifica che un trasferimento recuperato tramite
 * operationKey corrisponda alla richiesta corrente.
 *
 * Nessun accesso al database e nessuna mutazione.
 *
 * Non autorizza la restituzione del trasferimento:
 * lo stato e la completezza del posting devono essere
 * verificati separatamente.
 */
public final class InventoryLocationTransferIdempotencyVerifier {

    private InventoryLocationTransferIdempotencyVerifier() {
    }

    public static void verifySameRequest(
            InventoryLocationTransfer existing,
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long itemId,
            Long sourceLocationId,
            Long targetLocationId,
            BigDecimal quantity,
            LocalDate movementDate) {

        if (existing == null) {
            throw new BusinessException(
                    "Il trasferimento da verificare è obbligatorio."
            );
        }

        requirePositiveId(tenantId);
        requirePositiveId(companyId);
        requirePositiveId(warehouseId);
        requirePositiveId(itemId);
        requirePositiveId(sourceLocationId);
        requirePositiveId(targetLocationId);

        if (sourceLocationId.equals(targetLocationId)) {
            throw new BusinessException(
                    "Le ubicazioni del trasferimento devono essere diverse."
            );
        }

        if (movementDate == null) {
            throw new BusinessException(
                    "La data del trasferimento è obbligatoria."
            );
        }

        if (quantity == null || quantity.signum() <= 0) {
            throw new BusinessException(
                    "La quantità del trasferimento non è valida."
            );
        }

        final BigDecimal requestedQuantity;

        try {
            requestedQuantity = quantity.setScale(
                    3, RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException exception) {
            throw new BusinessException(
                    "La quantità del trasferimento supera tre decimali."
            );
        }

        if (requestedQuantity.precision()
                - requestedQuantity.scale() > 12) {
            throw new BusinessException(
                    "La quantità del trasferimento supera la precisione consentita."
            );
        }

        BigDecimal existingQuantity = existing.getQuantity();

        if (!Objects.equals(existing.getTenantId(), tenantId)
                || !Objects.equals(existing.getCompanyId(), companyId)
                || !Objects.equals(existing.getWarehouseId(), warehouseId)
                || !Objects.equals(existing.getItemId(), itemId)
                || !Objects.equals(existing.getSourceLocationId(), sourceLocationId)
                || !Objects.equals(existing.getTargetLocationId(), targetLocationId)
                || !Objects.equals(existing.getMovementDate(), movementDate)
                || existingQuantity == null
                || existingQuantity.compareTo(requestedQuantity) != 0) {
            throw new BusinessException(
                    "La chiave di idempotenza è già associata "
                    + "a un trasferimento con parametri differenti."
            );
        }
    }

    private static void requirePositiveId(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(
                    "Il contesto della richiesta idempotente non è valido."
            );
        }
    }
}
