package com.gestiva.warehouse.tracking.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.item.entity.InventoryTrackingType;
import com.gestiva.warehouse.item.entity.Item;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.tracking.entity.InventoryLot;
import com.gestiva.warehouse.tracking.repository.InventoryLotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class InventoryLotService {

    private final InventoryLotRepository lotRepository;
    private final ItemRepository itemRepository;

    public InventoryLotService(
            InventoryLotRepository lotRepository,
            ItemRepository itemRepository
    ) {
        this.lotRepository = lotRepository;
        this.itemRepository = itemRepository;
    }

    // =========================================================
    // CREAZIONE LOTTO
    // =========================================================

    @Transactional
    public InventoryLot create(
            Long tenantId,
            Long itemId,
            String lotCode,
            LocalDate productionDate,
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
                    "Non è possibile creare lotti per un articolo inattivo."
            );
        }

        // 3. Verifica che sia un prodotto fisico
        if (!item.isTrackStock()) {
            throw new BusinessException(
                    "La gestione dei lotti è consentita solo per i prodotti."
            );
        }

        // 4. Verifica tracciabilità LOT
        if (item.getTrackingType() != InventoryTrackingType.LOT) {
            throw new BusinessException(
                    "L'articolo non è configurato per la tracciabilità per lotto."
            );
        }

        // 5. Normalizzazione codice
        String normalizedCode = normalizeLotCode(lotCode);

        if (normalizedCode.length() > 100) {
            throw new BusinessException(
                    "Il codice del lotto non può superare 100 caratteri."
            );
        }

        // 6. Validazione date
        validateDates(productionDate, expirationDate);

        // 7. Controllo gestione scadenza
        if (item.isTrackExpiration() && expirationDate == null) {
            throw new BusinessException(
                    "La data di scadenza è obbligatoria per questo articolo."
            );
        }

        // 8. Validazione note
        String normalizedNotes = normalizeNotes(notes);

        if (normalizedNotes != null && normalizedNotes.length() > 500) {
            throw new BusinessException(
                    "Le note non possono superare 500 caratteri."
            );
        }

        // 9. Verifica duplicati
        if (lotRepository.existsByTenantIdAndItemIdAndLotCode(
                tenantId,
                itemId,
                normalizedCode
        )) {
            throw new BusinessException(
                    "Esiste già un lotto con questo codice per l'articolo."
            );
        }

        // 10. Creazione entity
        InventoryLot lot = new InventoryLot();

        lot.setTenantId(tenantId);
        lot.setItem(item);
        lot.setLotCode(normalizedCode);
        lot.setProductionDate(productionDate);
        lot.setExpirationDate(expirationDate);
        lot.setNotes(normalizedNotes);
        lot.setActive(true);

        return lotRepository.save(lot);
    }

    // =========================================================
    // RECUPERO SINGOLO LOTTO
    // =========================================================

    public InventoryLot getById(
            Long tenantId,
            Long itemId,
            Long lotId
    ) {

        validateIdentifiers(tenantId, itemId);

        if (lotId == null) {
            throw new BusinessException(
                    "L'identificativo del lotto è obbligatorio."
            );
        }

        return lotRepository
                .findByTenantIdAndItemIdAndId(
                        tenantId,
                        itemId,
                        lotId
                )
                .orElseThrow(() ->
                        new BusinessException(
                                "Lotto non trovato."
                        )
                );
    }

    // =========================================================
    // ELENCO LOTTI
    // =========================================================

    public List<InventoryLot> findAll(
            Long tenantId,
            Long itemId
    ) {

        validateIdentifiers(tenantId, itemId);

        return lotRepository
                .findByTenantIdAndItemIdOrderByLotCodeAsc(
                        tenantId,
                        itemId
                );
    }

    // =========================================================
    // ELENCO LOTTI ATTIVI
    // =========================================================

    public List<InventoryLot> findActive(
            Long tenantId,
            Long itemId
    ) {

        validateIdentifiers(tenantId, itemId);

        return lotRepository
                .findByTenantIdAndItemIdAndActiveTrueOrderByLotCodeAsc(
                        tenantId,
                        itemId
                );
    }

    // =========================================================
    // RICERCA PER CODICE
    // =========================================================

    public InventoryLot getByCode(
            Long tenantId,
            Long itemId,
            String lotCode
    ) {

        validateIdentifiers(tenantId, itemId);

        String normalizedCode = normalizeLotCode(lotCode);

        return lotRepository
                .findByTenantIdAndItemIdAndLotCode(
                        tenantId,
                        itemId,
                        normalizedCode
                )
                .orElseThrow(() ->
                        new BusinessException(
                                "Lotto non trovato."
                        )
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

    private String normalizeLotCode(String lotCode) {

        if (lotCode == null || lotCode.isBlank()) {
            throw new BusinessException(
                    "Il codice del lotto è obbligatorio."
            );
        }

        return lotCode.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeNotes(String notes) {

        if (notes == null || notes.isBlank()) {
            return null;
        }

        return notes.trim();
    }

    private void validateDates(
            LocalDate productionDate,
            LocalDate expirationDate
    ) {

        if (productionDate != null
                && expirationDate != null
                && expirationDate.isBefore(productionDate)) {

            throw new BusinessException(
                    "La data di scadenza non può precedere la data di produzione."
            );
        }
    }
}