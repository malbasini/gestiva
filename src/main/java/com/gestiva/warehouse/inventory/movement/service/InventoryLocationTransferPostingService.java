package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Gestiva ERP V2.
 *
 * Servizio dedicato alla registrazione atomica
 * dei trasferimenti FIFO tra ubicazioni.
 *
 * Contratto iniziale:
 * la registrazione effettiva resta disabilitata
 * fino al completamento dei test su movimenti,
 * consumi FIFO, layer di destinazione e rollback.
 */
@Service
@Transactional
public class InventoryLocationTransferPostingService {

    /**
     * Registra un trasferimento FIFO tra due ubicazioni
     * dello stesso magazzino.
     *
     * Non ancora implementato.
     *
     * @return identificativo del trasferimento
     */
    public Long postTransfer(
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long itemId,
            Long sourceLocationId,
            Long targetLocationId,
            BigDecimal quantity,
            LocalDate movementDate,
            String notes) {

        if (tenantId == null
                || companyId == null
                || warehouseId == null
                || itemId == null
                || movementDate == null) {
            throw new BusinessException(
                    "Tenant, società, magazzino, articolo e data sono obbligatori."
            );
        }

        if (sourceLocationId == null || targetLocationId == null) {
            throw new BusinessException(
                    "Le ubicazioni di origine e destinazione sono obbligatorie."
            );
        }

        if (sourceLocationId.equals(targetLocationId)) {
            throw new BusinessException(
                    "Le ubicazioni di origine e destinazione devono essere diverse."
            );
        }

        if (quantity == null
                || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "La quantità da trasferire deve essere maggiore di zero."
            );
        }

        try {
            quantity.setScale(
                    3,
                    java.math.RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException exception) {
            throw new BusinessException(
                    "La quantità da trasferire non può superare tre decimali."
            );
        }

        // Il posting rimane intenzionalmente disabilitato.
        // Nessun movimento o layer viene registrato.
        throw new BusinessException(
                "Registrazione dei trasferimenti FIFO non ancora abilitata."
        );
    }

    /**
     * Variante con chiave di idempotenza esplicita.
     *
     * La chiave e' stabile tra i tentativi della stessa
     * operazione e sara' soggetta al vincolo univoco V12.
     *
     * Nessuna scrittura viene ancora effettuata.
     */
    public Long postTransfer(
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

        if (operationKey == null
                || operationKey.isBlank()) {
            throw new BusinessException(
                    "La chiave di idempotenza e' obbligatoria."
            );
        }

        if (operationKey.length() > 120) {
            throw new BusinessException(
                    "La chiave di idempotenza non puo' superare 120 caratteri."
            );
        }

        if (!operationKey.equals(operationKey.strip())) {
            throw new BusinessException(
                    "La chiave di idempotenza non puo' "
                    + "contenere spazi iniziali o finali."
            );
        }

        // Compatibilita' con le validazioni gia' presenti.
        // La firma originale mantiene il blocco operativo.
        return postTransfer(
                tenantId,
                companyId,
                warehouseId,
                itemId,
                sourceLocationId,
                targetLocationId,
                quantity,
                movementDate,
                notes
        );
    }
}
