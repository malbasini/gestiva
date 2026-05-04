package com.gestiva.logistics.ddt.service;

import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.logistics.ddt.entity.DeliveryNote;
import com.gestiva.logistics.ddt.repository.DeliveryNoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeliveryNoteWorkflowService {

    private final DeliveryNoteRepository deliveryNoteRepository;
    private final InvoiceRepository invoiceRepository;

    public DeliveryNoteWorkflowService(DeliveryNoteRepository deliveryNoteRepository,
                                       InvoiceRepository invoiceRepository) {
        this.deliveryNoteRepository = deliveryNoteRepository;
        this.invoiceRepository = invoiceRepository;
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
    }
}