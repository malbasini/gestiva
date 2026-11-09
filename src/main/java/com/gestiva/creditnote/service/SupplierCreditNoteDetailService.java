package com.gestiva.creditnote.service;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteLine;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.creditnote.repository.CreditNoteLineRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SupplierCreditNoteDetailService {

    private final CreditNoteRepository noteRepository;
    private final CreditNoteLineRepository lineRepository;
    private final CompanyContext companyContext;

    public SupplierCreditNoteDetailService(
            CreditNoteRepository noteRepository,
            CreditNoteLineRepository lineRepository,
            CompanyContext companyContext) {
        this.noteRepository = noteRepository;
        this.lineRepository = lineRepository;
        this.companyContext = companyContext;
    }

    public record Detail(
            CreditNote note,
            List<CreditNoteLine> lines) {
    }

    public List<CreditNote> findSupplierNotesForInvoice(
            Long tenantId,
            Long supplierInvoiceId) {

        if (tenantId == null || tenantId <= 0
                || supplierInvoiceId == null
                || supplierInvoiceId <= 0) {
            throw new NotFoundException(
                    "Fattura fornitore non valida.");
        }

        Long companyId = companyContext.requireCurrentCompanyId();

        List<CreditNote> notes = noteRepository
                .findByTenantIdAndCompanyIdAndCreditNoteTypeAndOriginalInvoiceId(
                        tenantId,
                        companyId,
                        "SUPPLIER",
                        supplierInvoiceId
                );

        return notes.stream()
                .sorted(
                        java.util.Comparator
                                .comparing(
                                        CreditNote::getCreditNoteDate,
                                        java.util.Comparator.nullsLast(
                                                java.util.Comparator.reverseOrder()))
                                .thenComparing(
                                        CreditNote::getId,
                                        java.util.Comparator.nullsLast(
                                                java.util.Comparator.reverseOrder()))
                )
                .toList();
    }

    public Detail getDetail(Long tenantId, Long noteId) {

        if (tenantId == null || noteId == null
                || tenantId <= 0 || noteId <= 0) {
            throw new NotFoundException(
                    "Nota di credito fornitore non trovata.");
        }

        Long companyId = companyContext.requireCurrentCompanyId();

        CreditNote note = noteRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, noteId)
                .orElseThrow(() -> new NotFoundException(
                        "Nota di credito fornitore non trovata."));

        if (!"SUPPLIER".equals(note.getCreditNoteType())) {
            throw new NotFoundException(
                    "Nota di credito fornitore non trovata.");
        }

        List<CreditNoteLine> lines = lineRepository
                .findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
                        tenantId, companyId, noteId);

        return new Detail(note, List.copyOf(lines));
    }
}
