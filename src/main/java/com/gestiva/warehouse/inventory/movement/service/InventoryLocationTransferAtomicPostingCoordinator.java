package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryLocationTransferRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryLocationTransferAllocationRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerConsumptionRepository;
import com.gestiva.warehouse.inventory.valuation.service.InventoryValuationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * GESTIVA ERP V2.
 *
 * Coordinatore transazionale dedicato ai trasferimenti
 * FIFO tra ubicazioni dello stesso magazzino.
 *
 * STATO: NON OPERATIVO.
 *
 * Il posting sarà implementato soltanto dopo i test
 * di isolamento, lock, idempotenza, consumi FIFO,
 * layer di destinazione, rollback e concorrenza.
 *
 * Non introdurre scritture parziali in questa classe.
 */
@Service
public class InventoryLocationTransferAtomicPostingCoordinator {

    private final WarehouseRepository warehouseRepository;
    private final InventoryLocationTransferRepository transferRepository;
    private final InventoryLocationTransferAllocationRepository allocationRepository;
    private final InventoryLayerRepository layerRepository;
    private final InventoryLayerConsumptionRepository consumptionRepository;
    private final InventoryMovementService movementService;
    private final InventoryValuationService valuationService;
    private final InventoryLocationTransferPostedEvidenceService postedEvidenceService;
    private final InventoryLocationTransferAtomicPostingWriter postingWriter;

    /**
     * Compatibilità con i test esistenti.
     *
     * Senza il servizio evidenze, il percorso POSTED
     * resta bloccato: non può restituire un ID.
     */
    @Deprecated
    public InventoryLocationTransferAtomicPostingCoordinator(
            WarehouseRepository warehouseRepository,
            InventoryLocationTransferRepository transferRepository,
            InventoryLocationTransferAllocationRepository allocationRepository,
            InventoryLayerRepository layerRepository,
            InventoryLayerConsumptionRepository consumptionRepository,
            InventoryMovementService movementService,
            InventoryValuationService valuationService) {

        this(
                warehouseRepository,
                transferRepository,
                allocationRepository,
                layerRepository,
                consumptionRepository,
                movementService,
                valuationService,
                null
        );
    }

    /**
     * Compatibilità con il percorso precedente.
     * Senza writer il posting rimane fail-closed.
     */
    public InventoryLocationTransferAtomicPostingCoordinator(
            WarehouseRepository warehouseRepository,
            InventoryLocationTransferRepository transferRepository,
            InventoryLocationTransferAllocationRepository allocationRepository,
            InventoryLayerRepository layerRepository,
            InventoryLayerConsumptionRepository consumptionRepository,
            InventoryMovementService movementService,
            InventoryValuationService valuationService,
            InventoryLocationTransferPostedEvidenceService postedEvidenceService) {

        this(
                warehouseRepository,
                transferRepository,
                allocationRepository,
                layerRepository,
                consumptionRepository,
                movementService,
                valuationService,
                postedEvidenceService,
                null
        );
    }

    /**
     * Costruttore Spring per l'integrazione interna.
     * Non abilita i servizi applicativi pubblici.
     */
    @Autowired
    public InventoryLocationTransferAtomicPostingCoordinator(
            WarehouseRepository warehouseRepository,
            InventoryLocationTransferRepository transferRepository,
            InventoryLocationTransferAllocationRepository allocationRepository,
            InventoryLayerRepository layerRepository,
            InventoryLayerConsumptionRepository consumptionRepository,
            InventoryMovementService movementService,
            InventoryValuationService valuationService,
            InventoryLocationTransferPostedEvidenceService postedEvidenceService,
            InventoryLocationTransferAtomicPostingWriter postingWriter) {

        this.warehouseRepository = java.util.Objects.requireNonNull(
                warehouseRepository, "warehouseRepository"
        );
        this.transferRepository = java.util.Objects.requireNonNull(
                transferRepository, "transferRepository"
        );
        this.allocationRepository = java.util.Objects.requireNonNull(
                allocationRepository, "allocationRepository"
        );
        this.layerRepository = java.util.Objects.requireNonNull(
                layerRepository, "layerRepository"
        );
        this.consumptionRepository = java.util.Objects.requireNonNull(
                consumptionRepository, "consumptionRepository"
        );
        this.movementService = java.util.Objects.requireNonNull(
                movementService, "movementService"
        );
        this.valuationService = java.util.Objects.requireNonNull(
                valuationService, "valuationService"
        );

        this.postedEvidenceService = postedEvidenceService;
        this.postingWriter = postingWriter;
    }

    /**
     * Contratto della futura registrazione atomica.
     *
     * Il metodo deve essere invocato attraverso il proxy
     * Spring affinché si applichi la transazione.
     *
     * @return ID del trasferimento, quando il posting
     *         sarà implementato e abilitato.
     */
    @Transactional(
            propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class
    )
    public Long post(
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

        // Validazione strutturale prima di qualsiasi lock,
        // accesso ai repository o operazione di persistenza.
        InventoryLocationTransferPostingRequestValidator.validate(
                tenantId,
                companyId,
                warehouseId,
                itemId,
                sourceLocationId,
                targetLocationId,
                quantity,
                movementDate,
                notes,
                operationKey
        );

        // Lock tenant/company/warehouse prima di ogni
        // eventuale lettura o scrittura FIFO.
        warehouseRepository.findByTenantAndCompanyAndIdForUpdate(
                tenantId,
                companyId,
                warehouseId
        ).orElseThrow(() -> new BusinessException(
                "Magazzino non trovato nella società attiva."
        ));

        // Lookup idempotenza isolato per tenant e società.
        // Nessun record viene creato o modificato.
        var existingTransfer =
                transferRepository.findByTenantIdAndCompanyIdAndOperationKey(
                        tenantId,
                        companyId,
                        operationKey
                );

        // Un record esistente non può essere restituito
        // senza verificare stato e parametri della richiesta.
        if (existingTransfer.isPresent()) {
            var existing = existingTransfer.get();

            // Prima confronta il contenuto della richiesta.
            InventoryLocationTransferIdempotencyVerifier.verifySameRequest(
                    existing,
                    tenantId,
                    companyId,
                    warehouseId,
                    itemId,
                    sourceLocationId,
                    targetLocationId,
                    quantity,
                    movementDate
            );

            // Poi verifica lo stato strutturale del posting.
            InventoryLocationTransferPostingStateVerifier.verifyPostedState(
                    existing
            );

            // Il costruttore legacy è fail-closed.
            if (postedEvidenceService == null) {
                throw new BusinessException(
                        "Riutilizzo del trasferimento POSTED "
                        + "non ancora abilitato."
                );
            }

            // Recupero tenant/company scoped e riconciliazione
            // completa delle evidenze FIFO persistite.
            postedEvidenceService.verifyPostedCompleteness(existing);

            // Le istanze legacy restano fail-closed,
            // anche dopo la verifica delle evidenze.
            if (postingWriter == null) {
                throw new BusinessException(
                        "Riutilizzo del trasferimento POSTED "
                        + "non ancora abilitato."
                );
            }

            // Un ID mancante o non positivo non può
            // essere restituito come risultato idempotente.
            Long existingId = existing.getId();
            if (existingId == null || existingId <= 0) {
                throw new BusinessException(
                        "Il trasferimento POSTED non ha un ID valido."
                );
            }

            // Nessuna nuova scrittura FIFO: restituisce
            // esclusivamente il trasferimento verificato.
            return existingId;
        }

        // Le istanze legacy rimangono fail-closed.
        if (postingWriter == null) {
            throw new BusinessException(
                    "Coordinatore atomico dei trasferimenti FIFO non ancora abilitato."
            );
        }

        // Solo integrazione interna: i servizi pubblici
        // continuano a rifiutare ogni posting.
        // Il writer partecipa alla transazione REQUIRED.
        return postingWriter.write(
                tenantId,
                companyId,
                warehouseId,
                itemId,
                sourceLocationId,
                targetLocationId,
                quantity,
                movementDate,
                notes,
                operationKey
        );
    }
}
