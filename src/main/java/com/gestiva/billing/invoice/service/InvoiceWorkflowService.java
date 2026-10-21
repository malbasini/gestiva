package com.gestiva.billing.invoice.service;

import com.gestiva.billing.invoice.entity.Invoice;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gestiva.security.company.context.CompanyContext;

@Service
@Transactional
public class InvoiceWorkflowService {

    private final InvoiceRepository invoiceRepository;
    private final CompanyContext companyContext;

    public InvoiceWorkflowService(
            InvoiceRepository invoiceRepository,
            CompanyContext companyContext) {

        this.invoiceRepository = invoiceRepository;
        this.companyContext = companyContext;
    }

    public void cancel(Long tenantId, Long invoiceId) {

        Long companyId = companyContext.requireCurrentCompanyId();
        Invoice invoice = invoiceRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        invoiceId
                )
                .orElseThrow(() -> new NotFoundException(
                        "Fattura non trovata nella società attiva."
                ));

        if ("CANCELLED".equals(invoice.getStatus())) {
            throw new BusinessException("La fattura è già annullata.");
        }

        if (!"ISSUED".equals(invoice.getStatus())) {
            throw new BusinessException(
                    "Solo una fattura ISSUED può essere annullata."
            );
        }

        throw new BusinessException(
                "Annullamento non disponibile: la fattura è già contabilizzata. "
                        + "È necessaria una procedura di rettifica contabile "
                        + "e, ove previsto, l'emissione di una nota di credito."
        );
    }
}