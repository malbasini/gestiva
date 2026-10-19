package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.inventory.movement.entity.InventoryMovement;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import com.gestiva.warehouse.inventory.valuation.service.InventoryValuationService;
import com.gestiva.warehouse.location.repository.WarehouseRepository;

@Service
@Transactional
public class InventoryMovementService {

    private final InventoryMovementRepository inventoryMovementRepository;
    private final ItemRepository itemRepository;
    private final InventoryLocationValidationService inventoryLocationValidationService;
    private final InventoryValuationService inventoryValuationService;
    private final WarehouseRepository warehouseRepository;

    public InventoryMovementService(
            InventoryMovementRepository inventoryMovementRepository,
            ItemRepository itemRepository,
            InventoryLocationValidationService inventoryLocationValidationService,
            InventoryValuationService inventoryValuationService,
            WarehouseRepository warehouseRepository) {

        this.inventoryMovementRepository = inventoryMovementRepository;
        this.itemRepository = itemRepository;
        this.inventoryLocationValidationService = inventoryLocationValidationService;
        this.inventoryValuationService = inventoryValuationService;
        this.warehouseRepository = warehouseRepository;
    }

    // Gestiva V2 - Registrazione movimento per società e magazzino
    public Long registerMovement(
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long locationId,
            Long itemId,
            LocalDate movementDate,
            String movementType,
            String causalCode,
            BigDecimal quantity,
            BigDecimal unitCost,
            String referenceType,
            Long referenceId,
            String notes) {

        if (tenantId == null || companyId == null) {
            throw new BusinessException(
                    "Tenant e società sono obbligatori."
            );
        }

        inventoryLocationValidationService.validate(
                tenantId,
                companyId,
                warehouseId,
                locationId
        );

        var item = itemRepository.findByTenantIdAndId(
                tenantId,
                itemId
        ).orElseThrow(() -> new BusinessException(
                "Articolo non trovato."
        ));

        if (!item.isTrackStock()) {
            throw new BusinessException(
                    "L'articolo non è gestito a magazzino."
            );
        }

        BigDecimal qty = scaleQty(quantity);

        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "La quantità deve essere maggiore di zero."
            );
        }

        boolean outbound = isOutbound(movementType);

        BigDecimal cost = outbound
                ? null
                : (unitCost == null ? null : scaleCost(unitCost));

        BigDecimal totalCost = cost != null
                ? scaleCost(cost.multiply(qty))
                : null;

        InventoryMovement movement = new InventoryMovement();

        movement.setTenantId(tenantId);
        movement.setCompanyId(companyId);
        movement.setWarehouseId(warehouseId);
        movement.setLocationId(locationId);
        movement.setItemId(itemId);
        movement.setMovementDate(movementDate);
        movement.setMovementType(movementType);
        movement.setCausalCode(causalCode);
        movement.setQuantity(qty);
        movement.setUnitCost(cost);
        movement.setTotalCost(totalCost);
        movement.setReferenceType(referenceType);
        movement.setReferenceId(referenceId);
        movement.setNotes(notes);
        movement.setReversed(false);
        movement.setReversalOfMovementId(null);

        return inventoryMovementRepository.save(movement).getId();
    }

    public Long registerMovement(Long tenantId,
                                 Long companyId,
                                 Long itemId,
                                 LocalDate movementDate,
                                 String movementType,
                                 String causalCode,
                                 BigDecimal quantity,
                                 BigDecimal unitCost,
                                 String referenceType,
                                 Long referenceId,
                                 String notes) {

        if (tenantId == null) {
            throw new BusinessException("Tenant non valido.");
        }

        if (companyId == null) {
            throw new BusinessException("Società non selezionata.");
        }

        var item = itemRepository.findByTenantIdAndId(tenantId, itemId)
                .orElseThrow(() -> new BusinessException("Articolo non trovato"));

        if (!item.isTrackStock()) {
            throw new BusinessException("L'articolo non è gestito a magazzino.");
        }

        BigDecimal qty = scaleQty(quantity);

        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("La quantità deve essere maggiore di zero.");
        }

        boolean outbound = isOutbound(movementType);

        BigDecimal cost = outbound
                ? null
                : (unitCost == null ? null : scaleCost(unitCost));

        BigDecimal totalCost = cost != null
                ? scaleCost(cost.multiply(qty))
                : null;

        InventoryMovement movement = new InventoryMovement();

        movement.setTenantId(tenantId);
        movement.setCompanyId(companyId);
        movement.setItemId(itemId);
        movement.setMovementDate(movementDate);
        movement.setMovementType(movementType);
        movement.setCausalCode(causalCode);
        movement.setQuantity(qty);
        movement.setUnitCost(cost);
        movement.setTotalCost(totalCost);
        movement.setReferenceType(referenceType);
        movement.setReferenceId(referenceId);
        movement.setNotes(notes);
        movement.setReversed(false);
        movement.setReversalOfMovementId(null);

        return inventoryMovementRepository.save(movement).getId();
    }

    public Long registerMovement(Long tenantId,
                                 Long itemId,
                                 LocalDate movementDate,
                                 String movementType,
                                 String causalCode,
                                 BigDecimal quantity,
                                 BigDecimal unitCost,
                                 String referenceType,
                                 Long referenceId,
                                 String notes) {

        var item = itemRepository.findByTenantIdAndId(tenantId, itemId)
                .orElseThrow(() -> new BusinessException("Articolo non trovato"));

        if (!item.isTrackStock()) {
            throw new BusinessException("L'articolo non è gestito a magazzino.");
        }

        BigDecimal qty = scaleQty(quantity);
        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("La quantità deve essere maggiore di zero.");
        }

        boolean outbound = isOutbound(movementType);

        BigDecimal cost = outbound
                ? null
                : (unitCost == null ? null : scaleCost(unitCost));

        BigDecimal totalCost = cost != null
                ? scaleCost(cost.multiply(qty))
                : null;

        InventoryMovement movement = new InventoryMovement();
        movement.setTenantId(tenantId);
        movement.setItemId(itemId);
        movement.setMovementDate(movementDate);
        movement.setMovementType(movementType);
        movement.setCausalCode(causalCode);
        movement.setQuantity(qty);
        movement.setUnitCost(cost);
        movement.setTotalCost(totalCost);
        movement.setReferenceType(referenceType);
        movement.setReferenceId(referenceId);
        movement.setNotes(notes);
        movement.setReversed(false);
        movement.setReversalOfMovementId(null);

        return inventoryMovementRepository.save(movement).getId();
    }

    private boolean isOutbound(String movementType) {
        return "OUT".equalsIgnoreCase(movementType)
                || "ADJUSTMENT_OUT".equalsIgnoreCase(movementType);
    }

    // Gestiva V2 - Storno movimento per società
    public Long reverseMovement(
            Long tenantId,
            Long companyId,
            Long movementId,
            LocalDate reversalDate,
            String notes) {

        if (tenantId == null || companyId == null) {
            throw new BusinessException(
                    "Tenant e società sono obbligatori."
            );
        }

        // Gestiva V2 - Recupero preliminare del movimento
// per individuare il magazzino da bloccare.
        InventoryMovement movementToReverse = inventoryMovementRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        movementId
                )
                .orElseThrow(() -> new BusinessException(
                        "Movimento di magazzino non trovato nella società attiva."
                ));

        if (movementToReverse.getWarehouseId() == null) {
            throw new BusinessException(
                    "Impossibile stornare un movimento senza magazzino."
            );
        }

        // Gestiva V2 - Il magazzino viene bloccato prima
        // del movimento originale.
        warehouseRepository.findByTenantAndCompanyAndIdForUpdate(
                tenantId,
                companyId,
                movementToReverse.getWarehouseId()
        ).orElseThrow(() -> new BusinessException(
                "Magazzino non trovato nella società attiva."
        ));

        // Gestiva V2 - Blocco pessimista del movimento originale
        // per impedire storni concorrenti.
        var original = inventoryMovementRepository
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        tenantId,
                        companyId,
                        movementId
                )
                .orElseThrow(() -> new BusinessException(
                        "Movimento di magazzino non trovato nella società attiva."
                ));

        // Gestiva V2 - Verifica della coerenza del magazzino
        // dopo l'acquisizione dei lock.
        if (!movementToReverse.getWarehouseId().equals(original.getWarehouseId())) {
            throw new BusinessException(
                    "Il magazzino del movimento è cambiato durante lo storno. " +
                            "Operazione annullata per garantire la coerenza FIFO."
            );
        }

        if (original.isReversed()) {
            throw new BusinessException(
                    "Il movimento è già stato stornato."
            );
        }

        // Gestiva V2 - Impedisce lo storno di un movimento compensativo
        if (original.getReversalOfMovementId() != null) {
            throw new BusinessException(
                    "Impossibile stornare un movimento che rappresenta già uno storno."
            );
        }

        if (original.getWarehouseId() == null) {
            throw new BusinessException(
                    "Impossibile stornare un movimento senza magazzino."
            );
        }

        // Gestiva V2 - Validazione della data di storno
        if (reversalDate == null) {
            throw new BusinessException(
                    "La data di storno è obbligatoria."
            );
        }

        if (original.getMovementDate() == null) {
            throw new BusinessException(
                    "Il movimento originale non ha una data valida."
            );
        }

        if (reversalDate.isBefore(original.getMovementDate())) {
            throw new BusinessException(
                    "La data di storno non può precedere quella del movimento originale."
            );
        }

        inventoryLocationValidationService.validate(
                tenantId,
                companyId,
                original.getWarehouseId(),
                original.getLocationId()
        );

        // Gestiva V2 - Ripristino FIFO nella stessa transazione
        // del movimento compensativo.
        inventoryValuationService.reverseFifoValuation(
                tenantId,
                companyId,
                original.getWarehouseId(),
                original.getId()
        );

        InventoryMovement reversal = new InventoryMovement();

        reversal.setTenantId(tenantId);
        reversal.setCompanyId(companyId);
        reversal.setWarehouseId(original.getWarehouseId());
        reversal.setLocationId(original.getLocationId());

        reversal.setItemId(original.getItemId());
        reversal.setMovementDate(reversalDate);
        reversal.setMovementType(
                reverseType(original.getMovementType())
        );

        reversal.setCausalCode("DOCUMENT_CANCEL_RESTORE");
        reversal.setQuantity(original.getQuantity());

        reversal.setUnitCost(original.getUnitCost());
        reversal.setTotalCost(original.getTotalCost());

        reversal.setReferenceType(original.getReferenceType());
        reversal.setReferenceId(original.getReferenceId());

        reversal.setNotes(notes);
        reversal.setReversed(false);
        reversal.setReversalOfMovementId(original.getId());

        InventoryMovement savedReversal =
                inventoryMovementRepository.save(reversal);

        original.setReversed(true);
        inventoryMovementRepository.save(original);

        return savedReversal.getId();
    }

    // Gestiva V2 - Storno legacy disabilitato.
    // Non garantisce l'isolamento multi-società
    // e non aggiorna i livelli FIFO.
    @Deprecated
    public Long reverseMovement(
            Long tenantId,
            Long movementId,
            LocalDate reversalDate,
            String notes) {

        throw new BusinessException(
                "Storno movimento legacy non disponibile: " +
                        "utilizzare esclusivamente il percorso FIFO V2 dopo il completamento della migrazione."
        );
    }

    private String reverseType(String movementType) {
        return switch (movementType) {
            case "IN" -> "OUT";
            case "OUT" -> "IN";
            case "ADJUSTMENT_IN" -> "ADJUSTMENT_OUT";
            case "ADJUSTMENT_OUT" -> "ADJUSTMENT_IN";
            default -> throw new BusinessException("Tipo movimento non reversibile: " + movementType);
        };
    }

    private BigDecimal scaleQty(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP)
                : value.setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleCost(BigDecimal value) {
        return value == null ? null : value.setScale(4, RoundingMode.HALF_UP);
    }
}