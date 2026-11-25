package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransfer;

/**
 * GESTIVA ERP V2.
 *
 * Controllo strutturale dello stato di registrazione
 * di un trasferimento FIFO tra ubicazioni.
 *
 * La verifica non accede ai repository e non modifica
 * alcuna entità.
 *
 * Lo stato POSTED e i due ID validi sono requisiti
 * necessari, ma non sufficienti, per dimostrare
 * la completezza della registrazione FIFO.
 */
public final class InventoryLocationTransferPostingStateVerifier {

    private InventoryLocationTransferPostingStateVerifier() {
    }

    public static void verifyPostedState(
            InventoryLocationTransfer existing) {

        if (existing == null) {
            throw new BusinessException(
                    "Il trasferimento da verificare è obbligatorio."
            );
        }

        String status = existing.getStatus();

        if (!"POSTED".equals(status)) {
            if ("PENDING".equals(status)) {
                throw new BusinessException(
                        "Il trasferimento è ancora in stato PENDING."
                );
            }

            throw new BusinessException(
                    "Lo stato del trasferimento non è valido per il riutilizzo."
            );
        }

        Long outMovementId = existing.getOutMovementId();
        Long inMovementId = existing.getInMovementId();

        if (outMovementId == null || outMovementId <= 0) {
            throw new BusinessException(
                    "Il trasferimento POSTED non ha un movimento OUT valido."
            );
        }

        if (inMovementId == null || inMovementId <= 0) {
            throw new BusinessException(
                    "Il trasferimento POSTED non ha un movimento IN valido."
            );
        }

        if (outMovementId.equals(inMovementId)) {
            throw new BusinessException(
                    "I movimenti OUT e IN del trasferimento devono essere distinti."
            );
        }
    }
}
