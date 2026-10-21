package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.service.InventoryAvailabilityService;
import com.gestiva.warehouse.inventory.valuation.service.InventoryValuationService;
import com.gestiva.logistics.ddt.repository.DeliveryNoteLineRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;
import com.gestiva.logistics.ddt.entity.DeliveryNote;
import com.gestiva.logistics.ddt.entity.DeliveryNoteLine;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.gestiva.logistics.ddt.repository.DeliveryNoteRepository;

@Service
@Transactional
public class InventoryDocumentPostingService {

    private final InventoryMovementService inventoryMovementService;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final ItemRepository itemRepository;
    private final GoodsReceiptLineRepository goodsReceiptLineRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final DeliveryNoteLineRepository deliveryNoteLineRepository;
    private final InventoryValuationService inventoryValuationService;
    private final InventoryAvailabilityService inventoryAvailabilityService;
    private final WarehouseRepository warehouseRepository;
    private final DeliveryNoteRepository deliveryNoteRepository;

    public InventoryDocumentPostingService(
            InventoryMovementService inventoryMovementService,
            InventoryMovementRepository inventoryMovementRepository,
            ItemRepository itemRepository,
            GoodsReceiptLineRepository goodsReceiptLineRepository,
            GoodsReceiptRepository goodsReceiptRepository,
            DeliveryNoteLineRepository deliveryNoteLineRepository,
            InventoryValuationService inventoryValuationService,
            InventoryAvailabilityService inventoryAvailabilityService,
            WarehouseRepository warehouseRepository,
            DeliveryNoteRepository deliveryNoteRepository) {

        this.inventoryMovementService = inventoryMovementService;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.itemRepository = itemRepository;
        this.goodsReceiptLineRepository = goodsReceiptLineRepository;
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.inventoryValuationService = inventoryValuationService;
        this.inventoryAvailabilityService = inventoryAvailabilityService;
        this.warehouseRepository = warehouseRepository;
        this.deliveryNoteLineRepository = deliveryNoteLineRepository;
        this.deliveryNoteRepository = deliveryNoteRepository;
    }

    // Gestiva V2 - Recupero DDT e blocco del magazzino
    private DeliveryNote requireDeliveryNoteForPosting(
            Long tenantId,
            Long companyId,
            Long deliveryNoteId) {

        if (tenantId == null || companyId == null || deliveryNoteId == null) {
            throw new BusinessException(
                    "Tenant, società e DDT sono obbligatori."
            );
        }

        DeliveryNote deliveryNote = deliveryNoteRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        deliveryNoteId
                )
                .orElseThrow(() -> new BusinessException(
                        "DDT non trovato nella società indicata."
                ));

        Long warehouseId = deliveryNote.getWarehouseId();

        if (warehouseId == null) {
            throw new BusinessException(
                    "Impossibile registrare il DDT: magazzino non selezionato."
            );
        }

        warehouseRepository
                .findByTenantAndCompanyAndIdForUpdate(
                        tenantId,
                        companyId,
                        warehouseId
                )
                .orElseThrow(() -> new BusinessException(
                        "Magazzino non trovato nella società indicata."
                ));

        return deliveryNote;
    }

    // =====================================================
    // GESTIVA V2 - SCARICO MAGAZZINO DA DDT MULTI-SOCIETÀ
    // =====================================================

    public void postSalesDeliveryFromDeliveryNoteV2(
            Long tenantId,
            Long companyId,
            Long deliveryNoteId) {

        // 1. Recupero DDT e blocco pessimista sul magazzino
        DeliveryNote deliveryNote = requireDeliveryNoteForPosting(
                tenantId,
                companyId,
                deliveryNoteId
        );

        Long warehouseId = deliveryNote.getWarehouseId();
        Long locationId = deliveryNote.getLocationId();

        // 2. Controllo idempotenza dopo il blocco
        boolean alreadyPosted = inventoryMovementRepository
                .existsByTenantIdAndCompanyIdAndReferenceTypeAndReferenceIdAndCausalCode(
                        tenantId,
                        companyId,
                        "DELIVERY_NOTE",
                        deliveryNoteId,
                        "SALES_DELIVERY"
                );

        if (alreadyPosted) {
            return;
        }

        // 3. Recupero righe DDT della società
        var lines = deliveryNoteLineRepository
                .findByTenantIdAndCompanyIdAndDeliveryNoteIdOrderByLineNoAsc(
                        tenantId,
                        companyId,
                        deliveryNoteId
                );

        if (lines.isEmpty()) {
            return;
        }

        // 4. Registrazione scarichi
        for (DeliveryNoteLine line : lines) {

            if (line.getItemId() == null
                    || isZeroOrNegative(line.getQuantity())) {
                continue;
            }

            var item = itemRepository
                    .findByTenantIdAndId(
                            tenantId,
                            line.getItemId()
                    )
                    .orElseThrow(() -> new BusinessException(
                            "Articolo non trovato: " + line.getItemId()
                    ));

            if (!item.isTrackStock()) {
                continue;
            }

            // 5. Verifica disponibilità per società e magazzino
            inventoryAvailabilityService
                    .validateAvailabilityByCompanyAndWarehouse(
                            tenantId,
                            companyId,
                            warehouseId,
                            line.getItemId(),
                            locationId,
                            line.getQuantity()
                    );

            // 6. Registrazione movimento V2
            Long movementId = inventoryMovementService.registerMovement(
                    tenantId,
                    companyId,
                    warehouseId,
                    locationId,
                    line.getItemId(),
                    deliveryNote.getDdtDate(),
                    "OUT",
                    "SALES_DELIVERY",
                    line.getQuantity(),
                    null,
                    "DELIVERY_NOTE",
                    deliveryNoteId,
                    "Scarico da DDT " + deliveryNote.getDdtNumber()
            );

            // 7. Valorizzazione FIFO V2
            inventoryValuationService.applyOutboundValuation(
                    tenantId,
                    companyId,
                    warehouseId,
                    movementId
            );
        }
    }

    public void postSalesDeliveryFromDeliveryNote(Long tenantId, DeliveryNote deliveryNote) {
        if (deliveryNote == null) {
            throw new BusinessException("DDT non valido.");
        }

        if (inventoryMovementRepository.existsByTenantIdAndReferenceTypeAndReferenceIdAndCausalCode(
                tenantId, "DELIVERY_NOTE", deliveryNote.getId(), "SALES_DELIVERY")) {
            return;
        }

        var lines = deliveryNoteLineRepository.findByTenantIdAndDeliveryNoteIdOrderByLineNoAsc(tenantId, deliveryNote.getId());

        if (lines.isEmpty()) {
            return;
        }

        for (DeliveryNoteLine line : lines) {
            if (line.getItemId() == null || isZeroOrNegative(line.getQuantity())) {
                continue;
            }

            var item = itemRepository.findByTenantIdAndId(tenantId, line.getItemId())
                    .orElseThrow(() -> new BusinessException("Articolo non trovato: " + line.getItemId()));

            if (!item.isTrackStock()) {
                continue;
            }
            inventoryAvailabilityService.validateAvailability(
                    tenantId,
                    line.getItemId(),
                    line.getQuantity()
            );
            Long idMovement = inventoryMovementService.registerMovement(
                    tenantId,
                    line.getItemId(),
                    deliveryNote.getDdtDate(),
                    "OUT",
                    "SALES_DELIVERY",
                    line.getQuantity(),
                    null,
                    "DELIVERY_NOTE",
                    deliveryNote.getId(),
                    "Scarico da DDT " + deliveryNote.getDdtNumber()
            );
            inventoryValuationService.applyOutboundValuation(tenantId, idMovement);
        }
    }

    // =====================================================
    // GESTIVA V2 - CARICO MERCI MULTI-SOCIETÀ CON FIFO
    // =====================================================

    public void postPurchaseReceiptFromGoodsReceiptV2(
            Long tenantId,
            Long companyId,
            Long goodsReceiptId) {

        if (tenantId == null || companyId == null || goodsReceiptId == null) {
            throw new BusinessException(
                    "Tenant, società e ricevimento merci sono obbligatori."
            );
        }

        GoodsReceipt receipt = goodsReceiptRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        goodsReceiptId
                )
                .orElseThrow(() -> new BusinessException(
                        "Ricevimento merci non trovato nella società indicata."
                ));

        Long warehouseId = receipt.getWarehouseId();

        if (warehouseId == null) {
            throw new BusinessException(
                    "Impossibile registrare il ricevimento: magazzino non selezionato."
            );
        }

        // FIFO V2: per ora il carico avviene a livello di magazzino.
        if (receipt.getLocationId() != null) {
            throw new BusinessException(
                    "Il carico merci per ubicazione non è ancora abilitato."
            );
        }

        Long locationId = null;

        // Blocco transazionale del magazzino.
        var warehouse = warehouseRepository
                .findByTenantAndCompanyAndIdForUpdate(
                        tenantId,
                        companyId,
                        warehouseId
                )
                .orElseThrow(() -> new BusinessException(
                        "Magazzino non trovato nella società indicata."
                ));

        if (!warehouse.isActive()) {
            throw new BusinessException(
                    "Il magazzino selezionato non è attivo."
            );
        }

        // Controllo idempotenza dopo il blocco.
        boolean alreadyPosted = inventoryMovementRepository
                .existsByTenantIdAndCompanyIdAndReferenceTypeAndReferenceIdAndCausalCode(
                        tenantId,
                        companyId,
                        "GOODS_RECEIPT",
                        goodsReceiptId,
                        "PURCHASE_RECEIPT"
                );

        if (alreadyPosted) {
            return;
        }

        var lines = goodsReceiptLineRepository
                .findByTenantIdAndGoodsReceiptIdOrderByLineNoAsc(
                        tenantId,
                        goodsReceiptId
                );

        if (lines.isEmpty()) {
            throw new BusinessException(
                    "Il ricevimento merci non contiene righe."
            );
        }

        for (GoodsReceiptLine line : lines) {

            if (line.getItemId() == null) {
                continue;
            }

            var item = itemRepository
                    .findByTenantIdAndId(tenantId, line.getItemId())
                    .orElseThrow(() -> new BusinessException(
                            "Articolo non trovato: " + line.getItemId()
                    ));

            if (!item.isTrackStock()) {
                continue;
            }

            if (line.getQuantityReceived() == null
                    || line.getQuantityReceived().signum() <= 0) {
                throw new BusinessException(
                        "Quantità ricevuta non valida per l'articolo: "
                                + line.getItemId()
                );
            }

            if (line.getUnitCost() == null
                    || line.getUnitCost().signum() <= 0) {
                throw new BusinessException(
                        "Costo unitario non valido per l'articolo: "
                                + line.getItemId()
                );
            }

            Long movementId = inventoryMovementService.registerMovement(
                    tenantId,
                    companyId,
                    warehouseId,
                    locationId,
                    line.getItemId(),
                    receipt.getReceiptDate(),
                    "IN",
                    "PURCHASE_RECEIPT",
                    line.getQuantityReceived(),
                    line.getUnitCost(),
                    "GOODS_RECEIPT",
                    goodsReceiptId,
                    "Carico da ricevimento merci " + receipt.getReceiptNumber()
            );

            inventoryValuationService.applyInboundValuation(
                    tenantId,
                    companyId,
                    warehouseId,
                    movementId
            );
        }
    }

    public void postPurchaseReceiptFromGoodsReceipt(Long tenantId, GoodsReceipt goodsReceipt) {
        if (goodsReceipt == null) {
            throw new BusinessException("Ricezione merci non valida.");
        }

        var lines = goodsReceiptLineRepository.findByTenantIdAndGoodsReceiptIdOrderByLineNoAsc(tenantId, goodsReceipt.getId());
        if (lines.isEmpty()) {
            return;
        }

        for (GoodsReceiptLine line : lines) {
            if (line.getItemId() == null || line.getQuantityReceived() == null || line.getQuantityReceived().signum() <= 0) {
                continue;
            }

            var item = itemRepository.findByTenantIdAndId(tenantId, line.getItemId())
                    .orElseThrow(() -> new BusinessException("Articolo non trovato: " + line.getItemId()));

            if (!item.isTrackStock()) {
                continue;
            }

            Long idMovement = inventoryMovementService.registerMovement(
                    tenantId,
                    line.getItemId(),
                    goodsReceipt.getReceiptDate(),
                    "IN",
                    "PURCHASE_RECEIPT",
                    line.getQuantityReceived(),
                    line.getUnitCost(),
                    "GOODS_RECEIPT",
                    goodsReceipt.getId(),
                    "Carico da ricezione merci " + goodsReceipt.getReceiptNumber()
            );
            inventoryValuationService.applyInboundValuation(tenantId, idMovement);
        }
    }

    // =====================================================
    // GESTIVA V2 - STORNO DOCUMENTALE MULTI-SOCIETÀ
    // =====================================================

    public void reverseDocumentMovementsV2(
            Long tenantId,
            Long companyId,
            String referenceType,
            Long referenceId,
            LocalDate reversalDate,
            String notes) {

        if (tenantId == null || companyId == null) {
            throw new BusinessException(
                    "Tenant e società sono obbligatori."
            );
        }

        if (referenceType == null || referenceType.isBlank()
                || referenceId == null) {
            throw new BusinessException(
                    "Il riferimento al documento è obbligatorio."
            );
        }

        // Gestiva V2 - Storno documentale attualmente supportato
        // esclusivamente per i DDT multi-società.
        if (!"DELIVERY_NOTE".equals(referenceType)) {
            throw new BusinessException(
                    "Storno documentale V2 non disponibile per il tipo: "
                            + referenceType
            );
        }

        if (reversalDate == null) {
            throw new BusinessException(
                    "La data di storno è obbligatoria."
            );
        }

        // Gestiva V2 - Blocco del magazzino prima del recupero
        // dei movimenti associati al DDT.
        if ("DELIVERY_NOTE".equals(referenceType)) {

            requireDeliveryNoteForPosting(
                    tenantId,
                    companyId,
                    referenceId
            );
        }

        // Recupera esclusivamente i movimenti della società indicata.
        var movements = inventoryMovementRepository
                .findByTenantIdAndCompanyIdAndReferenceTypeAndReferenceIdOrderByIdAsc(
                        tenantId,
                        companyId,
                        referenceType,
                        referenceId
                );

        if (movements.isEmpty()) {
            throw new BusinessException(
                    "Nessun movimento di magazzino trovato per il documento."
            );
        }

        // Gestiva V2 - Verifica della presenza di movimenti originali
        boolean hasOriginalMovements = movements.stream()
                .anyMatch(movement ->
                        movement.getReversalOfMovementId() == null
                );

        if (!hasOriginalMovements) {
            throw new BusinessException(
                    "Il documento non contiene movimenti originali da stornare."
            );
        }

        // Gestiva V2 - Validazione preventiva di tutti i movimenti originali
        for (var movement : movements) {

            if (movement.getReversalOfMovementId() != null) {
                continue;
            }

            if (movement.isReversed()) {
                throw new BusinessException(
                        "Impossibile stornare il documento: " +
                                "uno o più movimenti risultano già stornati."
                );
            }
        }

        // Storno in ordine inverso rispetto alla registrazione.
        // Non utilizziamo il metodo legacy.
        for (int i = movements.size() - 1; i >= 0; i--) {

            var movement = movements.get(i);

            // Esclude eventuali movimenti compensativi.
            if (movement.getReversalOfMovementId() != null) {
                continue;
            }

            if (movement.isReversed()) {
                throw new BusinessException(
                        "Il documento contiene movimenti già stornati."
                );
            }

            inventoryMovementService.reverseMovement(
                    tenantId,
                    companyId,
                    movement.getId(),
                    reversalDate,
                    notes
            );
        }
    }

    public void reverseDocumentMovements(Long tenantId,
                                         String referenceType,
                                         Long referenceId,
                                         LocalDate reversalDate,
                                         String notes) {

        // Gestiva V2 - Storno documentale legacy disabilitato.
        // Il metodo non garantisce l'isolamento multi-società
        // e non ripristina i livelli FIFO consumati.
        //
        // Non riattivare prima di implementare uno storno V2
        // transazionale, isolato per società e magazzino.

        throw new BusinessException(
                "Storno documentale temporaneamente non disponibile: " +
                        "ripristino FIFO V2 in fase di completamento."
        );
    }

    private boolean isZeroOrNegative(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) <= 0;
    }
}