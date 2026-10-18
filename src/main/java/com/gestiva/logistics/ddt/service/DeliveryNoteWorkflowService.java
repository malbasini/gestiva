package com.gestiva.logistics.ddt.service;

import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.warehouse.inventory.movement.service.InventoryDocumentPostingService;
import com.gestiva.logistics.ddt.entity.DeliveryNote;
import com.gestiva.logistics.ddt.repository.DeliveryNoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import com.gestiva.security.company.context.CompanyContext;

@Service
@Transactional
public class DeliveryNoteWorkflowService {

    private final DeliveryNoteRepository deliveryNoteRepository;
    private final InvoiceRepository invoiceRepository;
    private final InventoryDocumentPostingService inventoryDocumentPostingService;
    private final CompanyContext companyContext;

    public DeliveryNoteWorkflowService(
            DeliveryNoteRepository deliveryNoteRepository,
            InvoiceRepository invoiceRepository,
            InventoryDocumentPostingService inventoryDocumentPostingService,
            CompanyContext companyContext) {

        this.deliveryNoteRepository = deliveryNoteRepository;
        this.invoiceRepository = invoiceRepository;
        this.inventoryDocumentPostingService = inventoryDocumentPostingService;
        this.companyContext = companyContext;
    }

    public void cancel(Long tenantId, Long deliveryNoteId) {

        Long companyId = companyContext.resolveCurrentCompanyId();

        if (companyId == null) {
            throw new BusinessException(
                    "Nessuna società attiva selezionata."
            );
        }

        DeliveryNote deliveryNote = deliveryNoteRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        deliveryNoteId
                )
                .orElseThrow(() -> new NotFoundException(
                        "DDT non trovato nella società attiva."
                ));

        if ("CANCELLED".equals(deliveryNote.getStatus())) {
            throw new BusinessException(
                    "Il DDT è già annullato."
            );
        }

        if (!"ISSUED".equals(deliveryNote.getStatus())) {
            throw new BusinessException(
                    "Solo un DDT ISSUED può essere annullato."
            );
        }

        boolean invoiceExists = invoiceRepository
                .existsByTenantIdAndCompanyIdAndDeliveryNoteId(
                        tenantId,
                        companyId,
                        deliveryNoteId
                );

        if (invoiceExists) {
            throw new BusinessException(
                    "Il DDT non può essere annullato perché esiste già una fattura associata."
            );
        }

        // BLOCCO TEMPORANEO GESTIVA V2:
        // Lo storno FIFO deve ancora essere completato.
        // Non modificare lo stato del DDT e non generare movimenti.

        throw new BusinessException(
                "Annullamento DDT temporaneamente non disponibile: "
                        + "ripristino FIFO in fase di completamento."
        );
    }
}