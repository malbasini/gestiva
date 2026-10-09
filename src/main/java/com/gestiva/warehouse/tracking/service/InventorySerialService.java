package com.gestiva.warehouse.tracking.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.item.entity.InventoryTrackingType;
import com.gestiva.warehouse.item.entity.Item;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.tracking.entity.InventorySerial;
import com.gestiva.warehouse.tracking.repository.InventorySerialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class InventorySerialService {

    private final InventorySerialRepository serialRepository;
    private final ItemRepository itemRepository;

    public InventorySerialService(
            InventorySerialRepository serialRepository,
            ItemRepository itemRepository
    ) {
        this.serialRepository = serialRepository;
        this.itemRepository = itemRepository;
    }

    // =========================================================
    // CREAZIONE NUMERO DI SERIE
    // =========================================================

    @Transactional
    public InventorySerial create(
            Long tenantId,
            Long itemId,
            String serialNumber,
            LocalDate manufacturingDate,
            LocalDate expirationDate,
            String notes
    ) {

        if (tenantId == null || itemId == null) {
            throw new BusinessException(
                    "Tenant e articolo sono obbligatori."
            );
        }

        // 1. Recupero articolo nel tenant
        Item item = itemRepository
                .findByTenantIdAndId(tenantId, itemId)
                .orElseThrow(() ->
                        new BusinessException(
                                "Articolo non trovato."
                        )
                );

        // 2. Verifica articolo attivo
        if (!item.isActive()) {
            throw new BusinessException(
                    "Non è possibile creare seriali per un articolo inattivo."
            );
        }

        // 3. Verifica prodotto fisico
        if (!item.isTrackStock()) {
            throw new BusinessException(
                    "La gestione dei seriali è consentita solo per i prodotti."
            );
        }

        // 4. Verifica tracciabilità SERIAL
        if (item.getTrackingType() != InventoryTrackingType.SERIAL) {
            throw new BusinessException(
                    "L'articolo non è configurato per la tracciabilità seriale."
            );
        }

        // 5. Normalizzazione seriale
        String normalizedSerial = normalizeSerialNumber(serialNumber);

        if (normalizedSerial.length() > 150) {
            throw new BusinessException(
                    "Il numero di serie non può superare 150 caratteri."
            );
        }

        // 6. Validazione date
        validateDates(manufacturingDate, expirationDate);

        // 7. Scadenza obbligatoria se prevista
        if (item.isTrackExpiration() && expirationDate == null) {
            throw new BusinessException(
                    "La data di scadenza è obbligatoria per questo articolo."
            );
        }

        // 8. Normalizzazione note
        String normalizedNotes = normalizeNotes(notes);

        if (normalizedNotes != null && normalizedNotes.length() > 500) {
            throw new BusinessException(
                    "Le note non possono superare 500 caratteri."
            );
        }

        // 9. Verifica duplicati
        if (serialRepository.existsByTenantIdAndItemIdAndSerialNumber(
                tenantId,
                itemId,
                normalizedSerial
        )) {
            throw new BusinessException(
                    "Esiste già questo numero di serie per l'articolo."
            );
        }

        // 10. Creazione entity
        InventorySerial serial = new InventorySerial();

        serial.setTenantId(tenantId);
        serial.setItem(item);
        serial.setSerialNumber(normalizedSerial);
        serial.setManufacturingDate(manufacturingDate);
        serial.setExpirationDate(expirationDate);
        serial.setNotes(normalizedNotes);
        serial.setActive(true);

        return serialRepository.save(serial);
    }

    // =========================================================
    // RECUPERO SINGOLO SERIALE
    // =========================================================

    public InventorySerial getById(
            Long tenantId,
            Long itemId,
            Long serialId
    ) {

        validateIdentifiers(tenantId, itemId);

        if (serialId == null) {
            throw new BusinessException(
                    "L'identificativo del seriale è obbligatorio."
            );
        }

        return serialRepository
                .findByTenantIdAndItemIdAndId(
                        tenantId,
                        itemId,
                        serialId
                )
                .orElseThrow(() ->
                        new BusinessException(
                                "Numero di serie non trovato."
                        )
                );
    }

    // =========================================================
    // RICERCA PER NUMERO DI SERIE
    // =========================================================

    public InventorySerial getBySerialNumber(
            Long tenantId,
            Long itemId,
            String serialNumber
    ) {

        validateIdentifiers(tenantId, itemId);

        String normalizedSerial = normalizeSerialNumber(serialNumber);

        return serialRepository
                .findByTenantIdAndItemIdAndSerialNumber(
                        tenantId,
                        itemId,
                        normalizedSerial
                )
                .orElseThrow(() ->
                        new BusinessException(
                                "Numero di serie non trovato."
                        )
                );
    }

    // =========================================================
    // ELENCO SERIALI
    // =========================================================

    public List<InventorySerial> findAll(
            Long tenantId,
            Long itemId
    ) {

        validateIdentifiers(tenantId, itemId);

        return serialRepository
                .findByTenantIdAndItemIdOrderBySerialNumberAsc(
                        tenantId,
                        itemId
                );
    }

    // =========================================================
    // ELENCO SERIALI ATTIVI
    // =========================================================

    public List<InventorySerial> findActive(
            Long tenantId,
            Long itemId
    ) {

        validateIdentifiers(tenantId, itemId);

        return serialRepository
                .findByTenantIdAndItemIdAndActiveTrueOrderBySerialNumberAsc(
                        tenantId,
                        itemId
                );
    }

    // =========================================================
    // VALIDAZIONI PRIVATE
    // =========================================================

    private void validateIdentifiers(
            Long tenantId,
            Long itemId
    ) {

        if (tenantId == null || itemId == null) {
            throw new BusinessException(
                    "Tenant e articolo sono obbligatori."
            );
        }
    }

    private String normalizeSerialNumber(String serialNumber) {

        if (serialNumber == null || serialNumber.isBlank()) {
            throw new BusinessException(
                    "Il numero di serie è obbligatorio."
            );
        }

        return serialNumber.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeNotes(String notes) {

        if (notes == null || notes.isBlank()) {
            return null;
        }

        return notes.trim();
    }

    private void validateDates(
            LocalDate manufacturingDate,
            LocalDate expirationDate
    ) {

        if (manufacturingDate != null
                && expirationDate != null
                && expirationDate.isBefore(manufacturingDate)) {

            throw new BusinessException(
                    "La data di scadenza non può precedere la data di produzione."
            );
        }
    }
}