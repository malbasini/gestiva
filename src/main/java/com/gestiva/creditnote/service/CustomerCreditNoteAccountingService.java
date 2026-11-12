
package com.gestiva.creditnote.service;

import com.gestiva.accounting.v2.account.entity.Account;
import com.gestiva.accounting.v2.account.repository.AccountRepository;
import com.gestiva.accounting.v2.journal.entity.JournalEntry;
import com.gestiva.accounting.v2.journal.entity.JournalEntryLine;
import com.gestiva.accounting.v2.journal.repository.JournalEntryRepository;
import com.gestiva.accounting.v2.journal.repository.JournalEntryLineRepository;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.billing.invoice.repository.InvoiceLineRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNoteLine;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.creditnote.repository.CreditNoteLineRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.settings.sequence.service.DocumentSequenceService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class CustomerCreditNoteAccountingService {

    private static final String REFERENCE_TYPE =
            "CUSTOMER_CREDIT_NOTE";

    private static final BigDecimal ZERO =
            new BigDecimal("0.00");

    private final CompanyContext companyContext;
    private final CreditNoteRepository notes;
    private final CreditNoteLineRepository noteLines;
    private final InvoiceRepository invoices;
    private final InvoiceLineRepository invoiceLines;
    private final AccountRepository accounts;
    private final JournalEntryRepository entries;
    private final JournalEntryLineRepository entryLines;
    private final DocumentSequenceService sequences;

    public CustomerCreditNoteAccountingService(
            CompanyContext companyContext,
            CreditNoteRepository notes,
            CreditNoteLineRepository noteLines,
            InvoiceRepository invoices,
            InvoiceLineRepository invoiceLines,
            AccountRepository accounts,
            JournalEntryRepository entries,
            JournalEntryLineRepository entryLines,
            DocumentSequenceService sequences) {

        this.companyContext = companyContext;
        this.notes = notes;
        this.noteLines = noteLines;
        this.invoices = invoices;
        this.invoiceLines = invoiceLines;
        this.accounts = accounts;
        this.entries = entries;
        this.entryLines = entryLines;
        this.sequences = sequences;
    }

    /**
     * GESTIVA V2 - PASSO 5.13.2395E
     *
     * Contabilizza una nota di credito CUSTOMER
     * già REGISTERED nella transazione corrente.
     *
     * La validazione cumulativa e i lock devono
     * essere stati eseguiti dal coordinatore.
     *
     * Non modifica fatture, scadenze o pagamenti.
     * Non effettua commit autonomi.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Long postRegisteredNote(
            Long tenantId,
            Long creditNoteId) {

        if (tenantId == null || creditNoteId == null) {
            throw new BusinessException(
                    "Tenant o nota cliente mancanti.");
        }

        Long companyId =
                companyContext.requireCurrentCompanyId();

        var note = notes
                .findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, creditNoteId)
                .orElseThrow(() -> new BusinessException(
                        "Nota cliente non trovata nella società attiva."));

        if (!"CUSTOMER".equals(note.getCreditNoteType())
                || !"REGISTERED".equals(note.getStatus())) {
            throw new BusinessException(
                    "La nota cliente deve essere REGISTERED.");
        }

        if (note.getOriginalInvoiceId() == null
                || note.getPartyId() == null
                || note.getCreditNoteNumber() == null
                || note.getCreditNoteNumber().isBlank()
                || note.getCreditNoteDate() == null
                || note.getCurrencyCode() == null
                || note.getCurrencyCode().isBlank()) {
            throw new BusinessException(
                    "Dati documentali della nota cliente incompleti.");
        }

        var invoice = invoices
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        note.getOriginalInvoiceId())
                .orElseThrow(() -> new BusinessException(
                        "Fattura cliente originale non trovata."));

        if (!"ISSUED".equals(invoice.getStatus())
                || !Objects.equals(
                invoice.getCustomerId(),
                note.getPartyId())
                || !Objects.equals(
                invoice.getCurrencyCode(),
                note.getCurrencyCode())) {
            throw new BusinessException(
                    "Nota cliente incoerente con la fattura originale.");
        }

        BigDecimal subtotal = money(note.getSubtotalAmount());
        BigDecimal tax = money(note.getTaxAmount());
        BigDecimal total = money(note.getTotalAmount());

        if (subtotal.signum() < 0
                || tax.signum() < 0
                || total.signum() <= 0
                || subtotal.add(tax).compareTo(total) != 0) {
            throw new BusinessException(
                    "Importi della nota cliente non validi.");
        }

        List<CreditNoteLine> lines = noteLines
                .findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
                        tenantId, companyId, creditNoteId);

        if (lines.isEmpty()) {
            throw new BusinessException(
                    "Nota cliente registrata senza righe.");
        }

        var originals = invoiceLines
                .findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                        tenantId, invoice.getId());

        Set<Long> originalIds = new HashSet<>();

        for (var original : originals) {
            if (original.getId() == null
                    || !Objects.equals(
                    original.getTenantId(), tenantId)
                    || !Objects.equals(
                    original.getInvoiceId(), invoice.getId())
                    || !originalIds.add(original.getId())) {
                throw new BusinessException(
                        "Righe fattura originale non coerenti.");
            }
        }

        BigDecimal sumSubtotal = ZERO;
        BigDecimal sumTax = ZERO;
        BigDecimal sumTotal = ZERO;

        Set<Long> selectedIds = new HashSet<>();

        for (var line : lines) {

            if (!Objects.equals(line.getTenantId(), tenantId)
                    || !Objects.equals(line.getCompanyId(), companyId)
                    || !Objects.equals(
                    line.getCreditNoteId(), creditNoteId)
                    || line.getOriginalInvoiceLineId() == null
                    || !originalIds.contains(
                    line.getOriginalInvoiceLineId())
                    || !selectedIds.add(
                    line.getOriginalInvoiceLineId())
                    || line.getQuantity() == null
                    || line.getQuantity().signum() <= 0
                    || line.getQuantity().scale() > 3) {
                throw new BusinessException(
                        "Riga nota cliente non valida.");
            }

            BigDecimal rowSubtotal =
                    money(line.getLineSubtotal());

            BigDecimal rowTax =
                    money(line.getTaxAmount());

            BigDecimal rowTotal =
                    money(line.getLineTotal());

            if (rowSubtotal.signum() < 0
                    || rowTax.signum() < 0
                    || rowTotal.signum() <= 0
                    || rowSubtotal.add(rowTax)
                    .compareTo(rowTotal) != 0) {
                throw new BusinessException(
                        "Importi della riga cliente non quadrati.");
            }

            sumSubtotal = sumSubtotal.add(rowSubtotal);
            sumTax = sumTax.add(rowTax);
            sumTotal = sumTotal.add(rowTotal);
        }

        if (sumSubtotal.compareTo(subtotal) != 0
                || sumTax.compareTo(tax) != 0
                || sumTotal.compareTo(total) != 0) {
            throw new BusinessException(
                    "Totali delle righe diversi dalla nota cliente.");
        }

        // Protezione applicativa dalle doppie scritture.
        // La V9 aggiunge anche un vincolo SQL UNIQUE.
        if (entries
                .existsByTenantIdAndCompanyIdAndReferenceTypeAndReferenceId(
                        tenantId,
                        companyId,
                        REFERENCE_TYPE,
                        creditNoteId)) {
            throw new BusinessException(
                    "Nota cliente già contabilizzata.");
        }

        // Conti coerenti con le fatture clienti Gestiva.
        Account receivables =
                requireAccount(tenantId, companyId, "1210");

        Account revenue =
                requireAccount(tenantId, companyId, "4100");

        Account vatOutput =
                requireAccount(tenantId, companyId, "2210");

        JournalEntry entry = new JournalEntry();

        entry.setTenantId(tenantId);
        entry.setCompanyId(companyId);
        entry.setEntryNumber(
                sequences.nextJournalEntryNumber(
                        tenantId, companyId));
        entry.setEntryDate(note.getCreditNoteDate());
        entry.setCausalCode(REFERENCE_TYPE);
        entry.setDescription(
                "Nota di credito cliente "
                        + note.getCreditNoteNumber());
        entry.setReferenceType(REFERENCE_TYPE);
        entry.setReferenceId(creditNoteId);
        entry.setCurrencyCode(note.getCurrencyCode());
        entry.setTotalDebit(total);
        entry.setTotalCredit(total);
        entry.setPosted(true);
        entry.setNotes(note.getReason());

        JournalEntry saved = entries.save(entry);

        if (saved == null || saved.getId() == null) {
            throw new BusinessException(
                    "Scrittura contabile cliente non salvata.");
        }

        entryLines.saveAll(List.of(

                journalLine(
                        tenantId, companyId, saved.getId(),
                        1, revenue.getId(),
                        "Rettifica ricavi da vendite",
                        subtotal, ZERO),

                journalLine(
                        tenantId, companyId, saved.getId(),
                        2, vatOutput.getId(),
                        "Rettifica IVA a debito",
                        tax, ZERO),

                journalLine(
                        tenantId, companyId, saved.getId(),
                        3, receivables.getId(),
                        "Riduzione crediti verso clienti",
                        ZERO, total)
        ));

        return saved.getId();
    }

    private Account requireAccount(
            Long tenantId,
            Long companyId,
            String code) {

        Account account = accounts
                .findByTenantIdAndCompanyIdAndCode(
                        tenantId, companyId, code)
                .orElseThrow(() -> new BusinessException(
                        "Conto non trovato nella società: " + code));

        if (!account.isActive()
                || !account.isLeafAccount()) {
            throw new BusinessException(
                    "Conto non attivo o non movimentabile: " + code);
        }

        return account;
    }

    private static BigDecimal money(BigDecimal value) {

        if (value == null || value.scale() > 2) {
            throw new BusinessException(
                    "Importo contabile mancante o con scala non valida.");
        }

        return value.setScale(2);
    }

    private static JournalEntryLine journalLine(
            Long tenantId,
            Long companyId,
            Long journalEntryId,
            int lineNo,
            Long accountId,
            String description,
            BigDecimal debit,
            BigDecimal credit) {

        JournalEntryLine line = new JournalEntryLine();

        line.setTenantId(tenantId);
        line.setCompanyId(companyId);
        line.setJournalEntryId(journalEntryId);
        line.setLineNo(lineNo);
        line.setAccountId(accountId);
        line.setDescription(description);
        line.setDebitAmount(debit);
        line.setCreditAmount(credit);

        return line;
    }
}
