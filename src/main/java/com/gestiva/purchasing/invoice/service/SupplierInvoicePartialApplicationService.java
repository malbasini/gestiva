package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Punto di ingresso applicativo per la futura creazione
 * delle fatture fornitori da ricevimenti parziali.
 *
 * Non è collegato ad alcun controller.
 *
 * Il coordinatore mantiene la responsabilità di:
 * - isolamento transazionale e lock pessimista;
 * - verifica del ricevimento e dei duplicati;
 * - pianificazione e calcolo degli importi;
 * - numerazione e persistenza della fattura DRAFT.
 *
 * Nessun effetto contabile o sullo scadenzario.
 */
@Service
public class SupplierInvoicePartialApplicationService {

    private final SupplierInvoicePartialGenerationGuard guard;
    private final CompanyContext companyContext;
    private final SupplierInvoicePartialDraftCreationService creation;

    public SupplierInvoicePartialApplicationService(
            SupplierInvoicePartialGenerationGuard guard,
            CompanyContext companyContext,
            SupplierInvoicePartialDraftCreationService creation) {

        this.guard = guard;
        this.companyContext = companyContext;
        this.creation = creation;
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class
    )
    public Long create(Long tenantId, Long goodsReceiptId) {

        // Prima protezione: feature gate prima di
        // qualsiasi consultazione documentale.
        guard.requireEnabled();

        if (tenantId == null || tenantId <= 0
                || goodsReceiptId == null
                || goodsReceiptId <= 0) {

            throw new BusinessException(
                    "Identificativi fatturazione parziale non validi."
            );
        }

        // Verifica dell'azienda attiva e della relativa
        // autorizzazione tramite CompanyAccessService.
        //
        // Non prendiamo mai companyId dal client.
        Long companyId = companyContext.requireCurrentCompanyId();

        if (companyId == null || companyId <= 0) {
            throw new BusinessException(
                    "Selezionare una società attiva valida."
            );
        }

        // Preparazione e writer sono protetti da ulteriori
        // controlli all'interno della stessa transazione.
        return creation.create(tenantId, goodsReceiptId);
    }
}
