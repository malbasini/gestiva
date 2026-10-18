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
            DeliveryNoteLineRepository deliveryNoteLineRepository,
            InventoryValuationService inventoryValuationService,
            InventoryAvailabilityService inventoryAvailabilityService,
            WarehouseRepository warehouseRepository,
            DeliveryNoteRepository deliveryNoteRepository) {

        this.inventoryMovementService = inventoryMovementService;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.itemRepository = itemRepository;
        this.goodsReceiptLineRepository = goodsReceiptLineRepository;
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