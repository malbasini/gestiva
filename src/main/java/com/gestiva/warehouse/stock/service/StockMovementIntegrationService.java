package com.gestiva.warehouse.stock.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.logistics.ddt.repository.DeliveryNoteLineRepository;
import com.gestiva.logistics.ddt.repository.DeliveryNoteRepository;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.stock.entity.StockMovement;
import com.gestiva.warehouse.stock.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class StockMovementIntegrationService {

    private final DeliveryNoteRepository deliveryNoteRepository;
    private final DeliveryNoteLineRepository deliveryNoteLineRepository;
    private final ItemRepository itemRepository;
    private final StockMovementRepository stockMovementRepository;

    public StockMovementIntegrationService(DeliveryNoteRepository deliveryNoteRepository,
                                           DeliveryNoteLineRepository deliveryNoteLineRepository,
                                           ItemRepository itemRepository,
                                           StockMovementRepository stockMovementRepository) {
        this.deliveryNoteRepository = deliveryNoteRepository;
        this.deliveryNoteLineRepository = deliveryNoteLineRepository;
        this.itemRepository = itemRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public void createOutboundMovementsFromDeliveryNote(Long tenantId, Long deliveryNoteId) {
        var deliveryNote = deliveryNoteRepository.findByTenantIdAndId(tenantId, deliveryNoteId)
                .orElseThrow(() -> new BusinessException("DDT non trovato"));

        var existing = stockMovementRepository.findByTenantIdAndReferenceTypeAndReferenceId(tenantId, "DDT", deliveryNoteId);
        if (!existing.isEmpty()) {
            throw new BusinessException("Movimenti di magazzino già generati per questo DDT.");
        }

        var lines = deliveryNoteLineRepository.findByTenantIdAndDeliveryNoteIdOrderByLineNoAsc(tenantId, deliveryNoteId);

        for (var line : lines) {
            if (line.getItemId() == null) {
                continue;
            }

            var item = itemRepository.findByTenantIdAndId(tenantId, line.getItemId())
                    .orElse(null);

            if (item == null || !item.isTrackStock()) {
                continue;
            }

            BigDecimal currentBalance = stockMovementRepository.calculateStockBalance(tenantId, item.getId());
            if (currentBalance.compareTo(line.getQuantity()) < 0) {
                throw new BusinessException("Giacenza insufficiente per l'articolo " + item.getCode());
            }

            StockMovement movement = new StockMovement();
            movement.setTenantId(tenantId);
            movement.setItemId(item.getId());
            movement.setMovementDate(deliveryNote.getDdtDate());
            movement.setDirection("OUT");
            movement.setReasonCode("DDT_ISSUE");
            movement.setQuantity(line.getQuantity());
            movement.setNotes("Scarico automatico da DDT " + deliveryNote.getDdtNumber());
            movement.setReferenceType("DDT");
            movement.setReferenceId(deliveryNoteId);

            stockMovementRepository.save(movement);
        }
    }

    public void createInboundReversalFromCancelledDeliveryNote(Long tenantId, Long deliveryNoteId) {
        var deliveryNote = deliveryNoteRepository.findByTenantIdAndId(tenantId, deliveryNoteId)
                .orElseThrow(() -> new BusinessException("DDT non trovato"));

        var lines = deliveryNoteLineRepository.findByTenantIdAndDeliveryNoteIdOrderByLineNoAsc(tenantId, deliveryNoteId);

        for (var line : lines) {
            if (line.getItemId() == null) {
                continue;
            }

            var item = itemRepository.findByTenantIdAndId(tenantId, line.getItemId())
                    .orElse(null);

            if (item == null || !item.isTrackStock()) {
                continue;
            }

            StockMovement movement = new StockMovement();
            movement.setTenantId(tenantId);
            movement.setItemId(item.getId());
            movement.setMovementDate(deliveryNote.getDdtDate());
            movement.setDirection("IN");
            movement.setReasonCode("DDT_CANCEL");
            movement.setQuantity(line.getQuantity());
            movement.setNotes("Ripristino automatico da annullamento DDT " + deliveryNote.getDdtNumber());
            movement.setReferenceType("DDT_CANCEL");
            movement.setReferenceId(deliveryNoteId);

            stockMovementRepository.save(movement);
        }
    }
}
