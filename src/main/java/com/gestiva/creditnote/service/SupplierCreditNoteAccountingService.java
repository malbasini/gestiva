package com.gestiva.creditnote.service;

import com.gestiva.accounting.v2.account.entity.Account;
import com.gestiva.accounting.v2.account.repository.AccountRepository;
import com.gestiva.accounting.v2.journal.entity.JournalEntry;
import com.gestiva.accounting.v2.journal.entity.JournalEntryLine;
import com.gestiva.accounting.v2.journal.repository.JournalEntryLineRepository;
import com.gestiva.accounting.v2.journal.repository.JournalEntryRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.repository.CreditNoteLineRepository;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class SupplierCreditNoteAccountingService {

    private static final String REFERENCE_TYPE = "SUPPLIER_CREDIT_NOTE";
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final CompanyContext companyContext;
    private final CreditNoteRepository creditNotes;
    private final CreditNoteLineRepository creditNoteLines;
    private final SupplierInvoiceRepository supplierInvoices;
    private final AccountRepository accounts;
    private final JournalEntryRepository entries;
    private final JournalEntryLineRepository entryLines;
    private final DocumentSequenceService sequences;

    public SupplierCreditNoteAccountingService(
            CompanyContext companyContext,
            CreditNoteRepository creditNotes,
            CreditNoteLineRepository creditNoteLines,
            SupplierInvoiceRepository supplierInvoices,
            AccountRepository accounts,
            JournalEntryRepository entries,
            JournalEntryLineRepository entryLines,
            DocumentSequenceService sequences) {

        this.companyContext = companyContext;
        this.creditNotes = creditNotes;
        this.creditNoteLines = creditNoteLines;
        this.supplierInvoices = supplierInvoices;
        this.accounts = accounts;
        this.entries = entries;
        this.entryLines = entryLines;
        this.sequences = sequences;
    }

    /**
     * Componente interno del futuro coordinatore di registrazione.
     *
     * MANDATORY: la transazione deve essere già attiva.
     * La nota deve essere stata validata sotto lock e portata
     * a REGISTERED nella medesima transazione.
     *
     * Non invocare direttamente da controller o interfaccia.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Long postRegisteredNote(Long tenantId, Long creditNoteId) {

        if (tenantId == null || creditNoteId == null) {
            throw new BusinessException(
                    "Tenant o nota di credito mancanti.");
        }

        Long companyId = companyContext.resolveCurrentCompanyId();

        if (companyId == null) {
            throw new BusinessException(
                    "Nessuna società attiva selezionata.");
        }

        var note = creditNotes
                .findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, creditNoteId)
                .orElseThrow(() -> new BusinessException(
                        "Nota di credito non trovata nella società attiva."));

        if (!"SUPPLIER".equals(note.getCreditNoteType())
                || !"REGISTERED".equals(note.getStatus())) {
            throw new BusinessException(
                    "La nota fornitore deve essere REGISTERED.");
        }

        if (note.getOriginalInvoiceId() == null
                || note.getPartyId() == null
                || note.getCreditNoteNumber() == null
                || note.getCreditNoteNumber().isBlank()
                || note.getCreditNoteDate() == null
                || note.getCurrencyCode() == null
                || note.getCurrencyCode().isBlank()) {
            throw new BusinessException(
                    "Dati documentali della nota incompleti.");
        }

        var invoice = supplierInvoices
                .findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, note.getOriginalInvoiceId())
                .orElseThrow(() -> new BusinessException(
                        "Fattura originale non trovata nella società."));

        if (!"REGISTERED".equals(invoice.getStatus())
                || !Objects.equals(
                        invoice.getSupplierId(), note.getPartyId())
                || !Objects.equals(
                        invoice.getCurrencyCode(), note.getCurrencyCode())) {
            throw new BusinessException(
                    "Nota non coerente con la fattura fornitore.");
        }

        BigDecimal subtotal = money(note.getSubtotalAmount());
        BigDecimal tax = money(note.getTaxAmount());
        BigDecimal total = money(note.getTotalAmount());

        if (subtotal.signum() < 0
                || tax.signum() < 0
                || total.signum() <= 0
                || subtotal.add(tax).compareTo(total) != 0) {
            throw new BusinessException(
                    "Importi della nota non validi.");
        }

        List<com.gestiva.creditnote.entity.CreditNoteLine> lines =
                creditNoteLines
                        .findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
                                tenantId, companyId, creditNoteId);

        if (lines.isEmpty()) {
            throw new BusinessException(
                    "Nota di credito registrata senza righe.");
        }

        BigDecimal sumSubtotal = ZERO;
        BigDecimal sumTax = ZERO;
        BigDecimal sumTotal = ZERO;

        for (var line : lines) {
            if (!Objects.equals(line.getTenantId(), tenantId)
                    || !Objects.equals(line.getCompanyId(), companyId)
                    || !Objects.equals(line.getCreditNoteId(), creditNoteId)
                    || line.getOriginalInvoiceLineId() == null
                    || line.getQuantity() == null
                    || line.getQuantity().signum() <= 0) {
                throw new BusinessException(
                        "Riga nota di credito non valida.");
            }

            BigDecimal rowSubtotal = money(line.getLineSubtotal());
            BigDecimal rowTax = money(line.getTaxAmount());
            BigDecimal rowTotal = money(line.getLineTotal());

            if (rowSubtotal.signum() < 0
                    || rowTax.signum() < 0
                    || rowTotal.signum() < 0
                    || rowSubtotal.add(rowTax).compareTo(rowTotal) != 0) {
                throw new BusinessException(
                        "Importi riga nota di credito non quadrati.");
            }

            sumSubtotal = sumSubtotal.add(rowSubtotal);
            sumTax = sumTax.add(rowTax);
            sumTotal = sumTotal.add(rowTotal);
        }

        if (sumSubtotal.compareTo(subtotal) != 0
                || sumTax.compareTo(tax) != 0
                || sumTotal.compareTo(total) != 0) {
            throw new BusinessException(
                    "Totali righe diversi dalla testata nota.");
        }

        if (entries
                .existsByTenantIdAndCompanyIdAndReferenceTypeAndReferenceId(
                        tenantId, companyId,
                        REFERENCE_TYPE, creditNoteId)) {
            throw new BusinessException(
                    "Nota di credito già contabilizzata.");
        }

        Account payable = requireAccount(
                tenantId, companyId, "2110");
        Account purchases = requireAccount(
                tenantId, companyId, "5100");
        Account vatInput = requireAccount(
                tenantId, companyId, "2220");

        JournalEntry entry = new JournalEntry();
        entry.setTenantId(tenantId);
        entry.setCompanyId(companyId);
        entry.setEntryNumber(
                sequences.nextJournalEntryNumber(tenantId, companyId));
        entry.setEntryDate(note.getCreditNoteDate());
        entry.setCausalCode(REFERENCE_TYPE);
        entry.setDescription(
                "Nota di credito fornitore " + note.getCreditNoteNumber());
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
                    "Scrittura contabile non salvata correttamente.");
        }

        entryLines.saveAll(List.of(
                journalLine(
                        tenantId, companyId, saved.getId(),
                        1, payable.getId(),
                        "Storno debito verso fornitore",
                        total, ZERO),
                journalLine(
                        tenantId, companyId, saved.getId(),
                        2, purchases.getId(),
                        "Rettifica costi di acquisto",
                        ZERO, subtotal),
                journalLine(
                        tenantId, companyId, saved.getId(),
                        3, vatInput.getId(),
                        "Rettifica IVA a credito",
                        ZERO, tax)
        ));

        return saved.getId();
    }

    private Account requireAccount(
            Long tenantId, Long companyId, String code) {

        Account account = accounts
                .findByTenantIdAndCompanyIdAndCode(
                        tenantId, companyId, code)
                .orElseThrow(() -> new BusinessException(
                        "Conto non trovato nella società attiva: " + code));

        if (!account.isActive() || !account.isLeafAccount()) {
            throw new BusinessException(
                    "Conto non attivo o non movimentabile: " + code);
        }

        return account;
    }

    private static BigDecimal money(BigDecimal value) {
        if (value == null || value.scale() > 2) {
            throw new BusinessException(
                    "Importo monetario nullo o con precisione non valida.");
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
