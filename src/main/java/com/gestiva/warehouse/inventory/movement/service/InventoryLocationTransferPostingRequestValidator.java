package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * GESTIVA ERP V2.
 *
 * Validazione strutturale e quantitativa delle richieste
 * di trasferimento FIFO tra ubicazioni.
 *
 * Componente puro: nessun repository e nessuna scrittura.
 *
 * Non sostituisce i controlli di autorizzazione,
 * di appartenenza al magazzino o di disponibilità FIFO.
 * Questi richiedono verifiche transazionali sul database.
 */
public final class InventoryLocationTransferPostingRequestValidator {

    private InventoryLocationTransferPostingRequestValidator() {
    }

    public static void validate(
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

        requirePositiveId(tenantId, "Tenant");
        requirePositiveId(companyId, "Società");
        requirePositiveId(warehouseId, "Magazzino");
        requirePositiveId(itemId, "Articolo");

        requirePositiveId(
                sourceLocationId,
                "Ubicazione di origine"
        );

        requirePositiveId(
                targetLocationId,
                "Ubicazione di destinazione"
        );

        if (sourceLocationId.equals(targetLocationId)) {
            throw new BusinessException(
                    "Le ubicazioni di origine e destinazione "
                    + "devono essere diverse."
            );
        }

        if (quantity == null
                || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "La quantità da trasferire deve essere positiva."
            );
        }

        final BigDecimal normalizedQuantity;

        try {
            normalizedQuantity = quantity.setScale(
                    3,
                    RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException exception) {
            throw new BusinessException(
                    "La quantità non può superare tre decimali."
            );
        }

        // V12: DECIMAL(15,3), massimo 12 cifre intere.
        if (normalizedQuantity.precision()
                - normalizedQuantity.scale() > 12) {
            throw new BusinessException(
                    "La quantità supera la capacità dello schema."
            );
        }

        if (movementDate == null) {
            throw new BusinessException(
                    "La data del trasferimento è obbligatoria."
            );
        }

        if (operationKey == null || operationKey.isBlank()) {
            throw new BusinessException(
                    "La chiave di idempotenza è obbligatoria."
            );
        }

        if (operationKey.length() > 120) {
            throw new BusinessException(
                    "La chiave di idempotenza non può superare "
                    + "120 caratteri."
            );
        }

        if (!operationKey.equals(operationKey.strip())) {
            throw new BusinessException(
                    "La chiave di idempotenza non può contenere "
                    + "spazi iniziali o finali."
            );
        }

        if (notes != null && notes.length() > 1000) {
            throw new BusinessException(
                    "Le note non possono superare 1000 caratteri."
            );
        }
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
}
