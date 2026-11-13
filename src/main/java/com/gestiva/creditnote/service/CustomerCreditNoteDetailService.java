
package com.gestiva.creditnote.service;

import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteLine;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.creditnote.repository.CreditNoteLineRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CustomerCreditNoteDetailService {

    private final InvoiceRepository invoiceRepository;
    private final CreditNoteRepository noteRepository;
    private final CreditNoteLineRepository lineRepository;
    private final CompanyContext companyContext;

    public CustomerCreditNoteDetailService(
            InvoiceRepository invoiceRepository,
            CreditNoteRepository noteRepository,
            CreditNoteLineRepository lineRepository,
            CompanyContext companyContext) {

        this.invoiceRepository = invoiceRepository;
        this.noteRepository = noteRepository;
        this.lineRepository = lineRepository;
        this.companyContext = companyContext;
    }

    public record Detail(
            CreditNote note,
            List<CreditNoteLine> lines) {
    }

    /**
     * GESTIVA V2 - PASSO 5.13.2395BI
     *
     * Elenco delle note di credito CUSTOMER
     * associate a una fattura della società attiva.
     *
     * Operazione esclusivamente in lettura.
     */
    public List<CreditNote> findCustomerNotesForInvoice(
            Long tenantId,
            Long customerInvoiceId) {

        if (tenantId == null || tenantId <= 0
                || customerInvoiceId == null
                || customerInvoiceId <= 0) {

            throw new NotFoundException(
                    "Fattura cliente non valida.");
        }

        Long companyId =
                companyContext.requireCurrentCompanyId();

        // Verifica che la fattura appartenga
        // al tenant e alla società attiva.
        invoiceRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        customerInvoiceId)
                .orElseThrow(() -> new NotFoundException(
                        "Fattura cliente non trovata."));

        List<CreditNote> notes = noteRepository
                .findByTenantIdAndCompanyIdAndCreditNoteTypeAndOriginalInvoiceId(
                        tenantId,
                        companyId,
                        "CUSTOMER",
                        customerInvoiceId);

        return notes.stream()
                .sorted(
                        Comparator
                                .comparing(
                                        CreditNote::getCreditNoteDate,
                                        Comparator.nullsLast(
                                                Comparator.reverseOrder()))
                                .thenComparing(
                                        CreditNote::getId,
                                        Comparator.nullsLast(
                                                Comparator.reverseOrder()))
                )
                .toList();
    }

    /**
     * Dettaglio di una singola nota cliente.
     *
     * Non restituisce documenti SUPPLIER
     * e non consente accesso ad altre società.
     */
    public Detail getDetail(
            Long tenantId,
            Long creditNoteId) {

        if (tenantId == null || tenantId <= 0
                || creditNoteId == null
                || creditNoteId <= 0) {

            throw new NotFoundException(
                    "Nota di credito cliente non trovata.");
        }

        Long companyId =
                companyContext.requireCurrentCompanyId();

        CreditNote note = noteRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        creditNoteId)
                .orElseThrow(() -> new NotFoundException(
                        "Nota di credito cliente non trovata."));

        if (!"CUSTOMER".equals(note.getCreditNoteType())) {
            throw new NotFoundException(
                    "Nota di credito cliente non trovata.");
        }

        List<CreditNoteLine> lines = lineRepository
                .findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
                        tenantId,
                        companyId,
                        creditNoteId);

        return new Detail(note, List.copyOf(lines));
    }
}
