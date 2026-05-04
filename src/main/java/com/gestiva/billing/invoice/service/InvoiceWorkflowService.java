package com.gestiva.billing.invoice.service;

import com.gestiva.billing.invoice.entity.Invoice;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InvoiceWorkflowService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceWorkflowService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public void cancel(Long tenantId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findByTenantIdAndId(tenantId, invoiceId)
                .orElseThrow(() -> new NotFoundException("Fattura non trovata"));

        if ("CANCELLED".equals(invoice.getStatus())) {
            throw new BusinessException("La fattura è già annullata.");
        }

        if (!"ISSUED".equals(invoice.getStatus())) {
            throw new BusinessException("Solo una fattura ISSUED può essere annullata.");
        }

        invoice.setStatus("CANCELLED");
        invoiceRepository.save(invoice);
    }
}