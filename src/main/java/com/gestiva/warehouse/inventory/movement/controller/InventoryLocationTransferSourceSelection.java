package com.gestiva.warehouse.inventory.movement.controller;

import com.gestiva.common.exception.BusinessException;

/**
 * GESTIVA ERP V2.
 *
 * Contratto HTTP condiviso per la selezione
 * dell'origine di un trasferimento FIFO.
 *
 * UNLOCATED identifica una selezione esplicita
 * delle scorte prive di ubicazione fisica.
 *
 * Il token HTTP non viene mai persistito.
 */
public final class InventoryLocationTransferSourceSelection {

    public static final String UNLOCATED = "UNLOCATED";

    private InventoryLocationTransferSourceSelection() {
    }

    /**
     * @return null esclusivamente per il token UNLOCATED;
     *         altrimenti l'identificativo fisico positivo.
     */
    public static Long parse(String selection) {

        if (selection == null || selection.isBlank()) {
            throw new BusinessException(
                    "Seleziona l'ubicazione di origine."
            );
        }

        if (UNLOCATED.equals(selection)) {
            return null;
        }

        if (!selection.matches("[1-9][0-9]{0,18}")) {
            throw invalidSelection();
        }

        try {
            return Long.valueOf(selection);
        } catch (NumberFormatException exception) {
            throw invalidSelection();
        }
    }

    private static BusinessException invalidSelection() {
        return new BusinessException(
                "Selezione dell'ubicazione di origine non valida."
        );
    }
}
