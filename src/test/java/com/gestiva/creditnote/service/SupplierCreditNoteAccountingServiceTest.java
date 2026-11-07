package com.gestiva.creditnote.service;

import com.gestiva.accounting.v2.account.entity.Account;
import com.gestiva.accounting.v2.account.repository.AccountRepository;
import com.gestiva.accounting.v2.journal.entity.JournalEntry;
import com.gestiva.accounting.v2.journal.entity.JournalEntryLine;
import com.gestiva.accounting.v2.journal.repository.JournalEntryLineRepository;
import com.gestiva.accounting.v2.journal.repository.JournalEntryRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteLine;
import com.gestiva.creditnote.repository.CreditNoteLineRepository;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SupplierCreditNoteAccountingServiceTest {

    private CompanyContext companies;
    private CreditNoteRepository notes;
    private CreditNoteLineRepository noteLines;
    private SupplierInvoiceRepository invoices;
    private AccountRepository accounts;
    private JournalEntryRepository entries;
    private JournalEntryLineRepository entryLines;
    private DocumentSequenceService sequences;
    private SupplierCreditNoteAccountingService service;

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    @BeforeEach
    void setup() {
        companies = mock(CompanyContext.class);
        notes = mock(CreditNoteRepository.class);
        noteLines = mock(CreditNoteLineRepository.class);
        invoices = mock(SupplierInvoiceRepository.class);
        accounts = mock(AccountRepository.class);
        entries = mock(JournalEntryRepository.class);
        entryLines = mock(JournalEntryLineRepository.class);
        sequences = mock(DocumentSequenceService.class);

        service = new SupplierCreditNoteAccountingService(
                companies, notes, noteLines, invoices,
                accounts, entries, entryLines, sequences);

        when(companies.resolveCurrentCompanyId()).thenReturn(3L);
    }

    private CreditNote note(String status) {
        CreditNote note = new CreditNote();
        note.setTenantId(2L);
        note.setCompanyId(3L);
        note.setCreditNoteType("SUPPLIER");
        note.setCreditNoteNumber("SCN-00001");
        note.setCreditNoteDate(LocalDate.of(2026, 11, 7));
        note.setOriginalInvoiceId(35L);
        note.setPartyId(11L);
        note.setStatus(status);
        note.setCurrencyCode("EUR");
        note.setReason("Rettifica acquisto");
        note.setSubtotalAmount(bd("10.00"));
        note.setTaxAmount(bd("2.20"));
        note.setTotalAmount(bd("12.20"));
        return note;
    }

    private CreditNoteLine noteLine() {
        CreditNoteLine line = new CreditNoteLine();
        line.setTenantId(2L);
        line.setCompanyId(3L);
        line.setCreditNoteId(100L);
        line.setOriginalInvoiceLineId(501L);
        line.setQuantity(bd("1.000"));
        line.setLineSubtotal(bd("10.00"));
        line.setTaxAmount(bd("2.20"));
        line.setLineTotal(bd("12.20"));
        return line;
    }

    private void setupInvoiceAndNote(CreditNote note) {
        SupplierInvoice invoice = new SupplierInvoice();
        invoice.setTenantId(2L);
        invoice.setCompanyId(3L);
        invoice.setSupplierId(11L);
        invoice.setStatus("REGISTERED");
        invoice.setCurrencyCode("EUR");

        when(notes.findByTenantIdAndCompanyIdAndId(2L, 3L, 100L))
                .thenReturn(Optional.of(note));

        when(invoices.findByTenantIdAndCompanyIdAndId(2L, 3L, 35L))
                .thenReturn(Optional.of(invoice));
    }

    private void setupAccounts() {
        for (String code : List.of("2110", "5100", "2220")) {
            Account account = mock(Account.class);

            Long id = switch (code) {
                case "2110" -> 21L;
                case "5100" -> 51L;
                default -> 22L;
            };

            when(account.getId()).thenReturn(id);
            when(account.isActive()).thenReturn(true);
            when(account.isLeafAccount()).thenReturn(true);

            when(accounts.findByTenantIdAndCompanyIdAndCode(
                    2L, 3L, code))
                    .thenReturn(Optional.of(account));
        }
    }

    private void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, bd(expected).compareTo(actual));
    }

    @Test
    void postsBalancedReversalWithCorrectAccounts() {
        setupInvoiceAndNote(note("REGISTERED"));

        when(noteLines
                .findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
                        2L, 3L, 100L))
                .thenReturn(List.of(noteLine()));

        setupAccounts();

        when(sequences.nextJournalEntryNumber(2L, 3L))
                .thenReturn("JE-00008");

        JournalEntry saved = mock(JournalEntry.class);
        when(saved.getId()).thenReturn(900L);
        when(entries.save(any(JournalEntry.class))).thenReturn(saved);

        Long result = service.postRegisteredNote(2L, 100L);
        assertEquals(900L, result);

        ArgumentCaptor<JournalEntry> entryCaptor =
                ArgumentCaptor.forClass(JournalEntry.class);

        verify(entries).save(entryCaptor.capture());

        JournalEntry entry = entryCaptor.getValue();

        assertEquals("SUPPLIER_CREDIT_NOTE", entry.getCausalCode());
        assertEquals("SUPPLIER_CREDIT_NOTE", entry.getReferenceType());
        assertEquals(100L, entry.getReferenceId());
        assertEquals("JE-00008", entry.getEntryNumber());
        assertEquals(2L, entry.getTenantId());
        assertEquals(3L, entry.getCompanyId());
        assertTrue(entry.isPosted());

        assertMoney("12.20", entry.getTotalDebit());
        assertMoney("12.20", entry.getTotalCredit());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<JournalEntryLine>> lineCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(entryLines).saveAll(lineCaptor.capture());

        var rows = lineCaptor.getValue();
        assertEquals(3, rows.size());

        assertEquals(21L, rows.get(0).getAccountId());
        assertMoney("12.20", rows.get(0).getDebitAmount());
        assertMoney("0.00", rows.get(0).getCreditAmount());

        assertEquals(51L, rows.get(1).getAccountId());
        assertMoney("0.00", rows.get(1).getDebitAmount());
        assertMoney("10.00", rows.get(1).getCreditAmount());

        assertEquals(22L, rows.get(2).getAccountId());
        assertMoney("0.00", rows.get(2).getDebitAmount());
        assertMoney("2.20", rows.get(2).getCreditAmount());

        for (var row : rows) {
            assertEquals(2L, row.getTenantId());
            assertEquals(3L, row.getCompanyId());
            assertEquals(900L, row.getJournalEntryId());
        }
    }

    @Test
    void rejectsDuplicateAccountingEntry() {
        setupInvoiceAndNote(note("REGISTERED"));

        when(noteLines
                .findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
                        2L, 3L, 100L))
                .thenReturn(List.of(noteLine()));

        when(entries
                .existsByTenantIdAndCompanyIdAndReferenceTypeAndReferenceId(
                        2L, 3L, "SUPPLIER_CREDIT_NOTE", 100L))
                .thenReturn(true);

        assertThrows(BusinessException.class,
                () -> service.postRegisteredNote(2L, 100L));

        verify(entries, never()).save(any(JournalEntry.class));
        verifyNoInteractions(entryLines, sequences);
    }

    @Test
    void rejectsDraftWithoutAccounting() {
        setupInvoiceAndNote(note("DRAFT"));

        assertThrows(BusinessException.class,
                () -> service.postRegisteredNote(2L, 100L));

        verifyNoInteractions(
                noteLines, accounts, entries, entryLines, sequences);
    }

    @Test
    void rejectsUnbalancedNoteWithoutAccounting() {
        CreditNote invalid = note("REGISTERED");
        invalid.setTotalAmount(bd("12.19"));

        setupInvoiceAndNote(invalid);

        assertThrows(BusinessException.class,
                () -> service.postRegisteredNote(2L, 100L));

        verifyNoInteractions(
                noteLines, accounts, entries, entryLines, sequences);
    }

    @Test
    void rejectsNoteOutsideActiveCompany() {
        when(notes.findByTenantIdAndCompanyIdAndId(
                2L, 3L, 100L))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> service.postRegisteredNote(2L, 100L));

        verifyNoInteractions(
                invoices, noteLines, accounts,
                entries, entryLines, sequences);
    }
}
