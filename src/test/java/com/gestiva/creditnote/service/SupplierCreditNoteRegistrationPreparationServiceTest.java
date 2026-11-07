package com.gestiva.creditnote.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteLine;
import com.gestiva.creditnote.repository.CreditNoteLineRepository;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import com.gestiva.purchasing.invoice.entity.SupplierInvoiceLine;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceLineRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.security.company.context.CompanyContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierCreditNoteRegistrationPreparationServiceTest {

    private CompanyContext companies;
    private SupplierInvoiceRepository invoices;
    private SupplierInvoiceLineRepository invoiceLines;
    private CreditNoteRepository notes;
    private CreditNoteLineRepository noteLines;
    private SupplierCreditNoteRegisteredCreditsService credits;
    private SupplierCreditNoteRegistrationPreparationService service;

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    @BeforeEach
    void setup() {
        companies = mock(CompanyContext.class);
        invoices = mock(SupplierInvoiceRepository.class);
        invoiceLines = mock(SupplierInvoiceLineRepository.class);
        notes = mock(CreditNoteRepository.class);
        noteLines = mock(CreditNoteLineRepository.class);
        credits = mock(SupplierCreditNoteRegisteredCreditsService.class);

        service = new SupplierCreditNoteRegistrationPreparationService(
                companies, invoices, invoiceLines,
                notes, noteLines, credits);

        when(companies.resolveCurrentCompanyId()).thenReturn(3L);
    }

    private SupplierInvoice originalInvoice() {
        SupplierInvoice invoice = mock(SupplierInvoice.class);
        when(invoice.getId()).thenReturn(35L);
        when(invoice.getTenantId()).thenReturn(2L);
        when(invoice.getCompanyId()).thenReturn(3L);
        when(invoice.getSupplierId()).thenReturn(11L);
        when(invoice.getStatus()).thenReturn("REGISTERED");
        when(invoice.getCurrencyCode()).thenReturn("EUR");
        return invoice;
    }

    private CreditNote draft(String status) {
        CreditNote note = mock(CreditNote.class);
        when(note.getId()).thenReturn(100L);
        when(note.getTenantId()).thenReturn(2L);
        when(note.getCompanyId()).thenReturn(3L);
        when(note.getOriginalInvoiceId()).thenReturn(35L);
        when(note.getPartyId()).thenReturn(11L);
        when(note.getCreditNoteType()).thenReturn("SUPPLIER");
        when(note.getStatus()).thenReturn(status);
        when(note.getCurrencyCode()).thenReturn("EUR");
        when(note.getSubtotalAmount()).thenReturn(bd("3.33"));
        when(note.getTaxAmount()).thenReturn(bd("0.73"));
        when(note.getTotalAmount()).thenReturn(bd("4.06"));
        return note;
    }

    private SupplierInvoiceLine originalLine() {
        SupplierInvoiceLine line = mock(SupplierInvoiceLine.class);
        when(line.getId()).thenReturn(501L);
        when(line.getQuantity()).thenReturn(bd("3.000"));
        when(line.getLineSubtotal()).thenReturn(bd("10.00"));
        when(line.getTaxAmount()).thenReturn(bd("2.20"));
        when(line.getLineTotal()).thenReturn(bd("12.20"));
        return line;
    }

    private CreditNoteLine draftLine() {
        CreditNoteLine line = mock(CreditNoteLine.class);
        when(line.getOriginalInvoiceLineId()).thenReturn(501L);
        when(line.getCreditNoteId()).thenReturn(100L);
        when(line.getTenantId()).thenReturn(2L);
        when(line.getCompanyId()).thenReturn(3L);
        when(line.getQuantity()).thenReturn(bd("1.000"));
        when(line.getLineSubtotal()).thenReturn(bd("3.33"));
        when(line.getTaxAmount()).thenReturn(bd("0.73"));
        when(line.getLineTotal()).thenReturn(bd("4.06"));
        return line;
    }

    private void configureValidScenario(
            String previousQuantity,
            String previousSubtotal,
            String previousTax) {

        // Prepariamo tutti i mock PRIMA dei thenReturn:
        // evita UnfinishedStubbing di Mockito.
        CreditNote note = draft("DRAFT");
        SupplierInvoice invoice = originalInvoice();
        SupplierInvoiceLine original = originalLine();
        CreditNoteLine row = draftLine();

        var previous = Map.of(
                501L,
                new SupplierCreditNoteCumulativeCalculator.PreviousCredits(
                        bd(previousQuantity),
                        bd(previousSubtotal),
                        bd(previousTax)));

        when(notes.findByTenantIdAndCompanyIdAndId(2L, 3L, 100L))
                .thenReturn(Optional.of(note));

        when(invoices.findByTenantIdAndCompanyIdAndIdForUpdate(
                2L, 3L, 35L))
                .thenReturn(Optional.of(invoice));

        when(notes.findByTenantIdAndCompanyIdAndIdForUpdate(
                2L, 3L, 100L))
                .thenReturn(Optional.of(note));

        when(credits.loadRegisteredCredits(2L, 3L, 35L))
                .thenReturn(previous);

        when(invoiceLines.findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                2L, 35L))
                .thenReturn(List.of(original));

        when(noteLines
                .findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
                        2L, 3L, 100L))
                .thenReturn(List.of(row));
    }

    @Test
    void validatesDraftWithCorrectLockOrderWithoutWriting() {
        configureValidScenario("0.000", "0.00", "0.00");

        assertDoesNotThrow(
                () -> service.validateUnderLock(2L, 100L));

        InOrder order = inOrder(notes, invoices, credits);

        order.verify(notes)
                .findByTenantIdAndCompanyIdAndId(2L, 3L, 100L);

        order.verify(invoices)
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        2L, 3L, 35L);

        order.verify(notes)
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        2L, 3L, 100L);

        order.verify(credits)
                .loadRegisteredCredits(2L, 3L, 35L);

        verify(notes, never()).save(any(CreditNote.class));
        verify(noteLines, never()).saveAll(any());
    }

    @Test
    void rejectsNoteOutsideActiveCompany() {
        when(notes.findByTenantIdAndCompanyIdAndId(2L, 3L, 100L))
                .thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> service.validateUnderLock(2L, 100L));

        verify(invoices, never())
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        anyLong(), anyLong(), anyLong());
        verifyNoInteractions(credits, noteLines);
    }

    @Test
    void rejectsMissingOriginalInvoice() {
        CreditNote note = draft("DRAFT");

        when(notes.findByTenantIdAndCompanyIdAndId(2L, 3L, 100L))
                .thenReturn(Optional.of(note));

        when(invoices.findByTenantIdAndCompanyIdAndIdForUpdate(
                2L, 3L, 35L))
                .thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> service.validateUnderLock(2L, 100L));

        verify(notes, never())
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        anyLong(), anyLong(), anyLong());

        verifyNoInteractions(credits, noteLines);
    }

    @Test
    void rejectsDraftChangedAfterInitialRead() {
        CreditNote initial = draft("DRAFT");
        CreditNote changed = draft("REGISTERED");
        SupplierInvoice invoice = originalInvoice();

        when(notes.findByTenantIdAndCompanyIdAndId(2L, 3L, 100L))
                .thenReturn(Optional.of(initial));

        when(invoices.findByTenantIdAndCompanyIdAndIdForUpdate(
                2L, 3L, 35L))
                .thenReturn(Optional.of(invoice));

        when(notes.findByTenantIdAndCompanyIdAndIdForUpdate(
                2L, 3L, 100L))
                .thenReturn(Optional.of(changed));

        assertThrows(
                BusinessException.class,
                () -> service.validateUnderLock(2L, 100L));

        verifyNoInteractions(credits, invoiceLines, noteLines);
    }

    @Test
    void rejectsStaleDraftAmounts() {
        // Restando 1 unità, gli importi corretti sono
        // 3.34 + 0.74 = 4.08, non quelli della bozza.
        configureValidScenario("2.000", "6.66", "1.46");

        assertThrows(
                BusinessException.class,
                () -> service.validateUnderLock(2L, 100L));

        verify(notes, never()).save(any(CreditNote.class));
        verify(noteLines, never()).saveAll(any());
    }
}
