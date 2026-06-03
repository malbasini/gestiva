package com.gestiva.logistics.ddt.service;

import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.inventory.movement.service.InventoryDocumentPostingService;
import com.gestiva.logistics.ddt.entity.DeliveryNote;
import com.gestiva.logistics.ddt.repository.DeliveryNoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class DeliveryNoteWorkflowService {

    private final DeliveryNoteRepository deliveryNoteRepository;
    private final InvoiceRepository invoiceRepository;
    private final InventoryDocumentPostingService inventoryDocumentPostingService;

    public DeliveryNoteWorkflowService(DeliveryNoteRepository deliveryNoteRepository,
                                       InvoiceRepository invoiceRepository,
                                       InventoryDocumentPostingService inventoryDocumentPostingService) {

        this.deliveryNoteRepository = deliveryNoteRepository;
        this.invoiceRepository = invoiceRepository;
        this.inventoryDocumentPostingService = inventoryDocumentPostingService;
    }

    public void cancel(Long tenantId, Long deliveryNoteId) {
        DeliveryNote deliveryNote = deliveryNoteRepository.findByTenantIdAndId(tenantId, deliveryNoteId)
                .orElseThrow(() -> new NotFoundException("DDT non trovato"));

        if ("CANCELLED".equals(deliveryNote.getStatus())) {
            throw new BusinessException("Il DDT è già annullato.");
        }

        if (!"ISSUED".equals(deliveryNote.getStatus())) {
            throw new BusinessException("Solo un DDT ISSUED può essere annullato.");
        }

        boolean invoiceExists = invoiceRepository.existsByTenantIdAndDeliveryNoteId(tenantId, deliveryNoteId);
        if (invoiceExists) {
            throw new BusinessException("Il DDT non può essere annullato perché esiste già una fattura associata.");
        }
        deliveryNote.setStatus("CANCELLED");
        deliveryNoteRepository.save(deliveryNote);
        inventoryDocumentPostingService.reverseDocumentMovements(
                tenantId,
                "DELIVERY_NOTE",
                deliveryNote.getId(),
                LocalDate.now(),
                "Ripristino da annullamento DDT " + deliveryNote.getDdtNumber()
        );
    }
}