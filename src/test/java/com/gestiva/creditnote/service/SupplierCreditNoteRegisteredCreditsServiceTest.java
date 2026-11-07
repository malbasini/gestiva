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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierCreditNoteRegisteredCreditsServiceTest {

    private SupplierInvoiceRepository invoices;
    private SupplierInvoiceLineRepository originalLines;
    private CreditNoteRepository notes;
    private CreditNoteLineRepository noteLines;
    private SupplierCreditNoteRegisteredCreditsService service;

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    @BeforeEach
    void setup() {
        invoices = mock(SupplierInvoiceRepository.class);
        originalLines = mock(SupplierInvoiceLineRepository.class);
        notes = mock(CreditNoteRepository.class);
        noteLines = mock(CreditNoteLineRepository.class);

        service = new SupplierCreditNoteRegisteredCreditsService(
                invoices, originalLines, notes, noteLines);
    }

    private void originalInvoiceWithOneLine() {
        SupplierInvoice invoice = mock(SupplierInvoice.class);
        when(invoice.getStatus()).thenReturn("REGISTERED");

        SupplierInvoiceLine line = mock(SupplierInvoiceLine.class);
        when(line.getId()).thenReturn(501L);
        when(line.getQuantity()).thenReturn(bd("3.000"));
        when(line.getLineSubtotal()).thenReturn(bd("10.00"));
        when(line.getTaxAmount()).thenReturn(bd("2.20"));

        when(invoices.findByTenantIdAndCompanyIdAndIdForUpdate(
                2L, 3L, 35L))
                .thenReturn(Optional.of(invoice));

        when(originalLines
                .findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                        2L, 35L))
                .thenReturn(List.of(line));
    }

    private CreditNote note(Long id, String status,
                            String subtotal, String tax) {
        CreditNote note = mock(CreditNote.class);
        when(note.getStatus()).thenReturn(status);

        if ("REGISTERED".equals(status)) {
            when(note.getId()).thenReturn(id);
            when(note.getSubtotalAmount()).thenReturn(bd(subtotal));
            when(note.getTaxAmount()).thenReturn(bd(tax));
            when(note.getTotalAmount()).thenReturn(
                    bd(subtotal).add(bd(tax)));
        }

        return note;
    }

    private CreditNoteLine creditedLine(
            Long originalLineId,
            String quantity,
            String subtotal,
            String tax) {

        CreditNoteLine line = mock(CreditNoteLine.class);
        when(line.getOriginalInvoiceLineId())
                .thenReturn(originalLineId);
        when(line.getQuantity()).thenReturn(bd(quantity));
        when(line.getLineSubtotal()).thenReturn(bd(subtotal));
        when(line.getTaxAmount()).thenReturn(bd(tax));
        when(line.getLineTotal()).thenReturn(
                bd(subtotal).add(bd(tax)));
        return line;
    }

    private void assertNumber(String expected, BigDecimal actual) {
        assertEquals(0, bd(expected).compareTo(actual));
    }

    @Test
    void aggregatesRegisteredCreditsUnderInvoiceLock() {
        originalInvoiceWithOneLine();

        CreditNote first = note(100L, "REGISTERED", "3.33", "0.73");
        CreditNote second = note(101L, "REGISTERED", "3.33", "0.73");

        when(notes
                .findByTenantIdAndCompanyIdAndCreditNoteTypeAndOriginalInvoiceId(
                        2L, 3L, "SUPPLIER", 35L))
                .thenReturn(List.of(first, second));

        CreditNoteLine firstLine =
                creditedLine(501L, "1.000", "3.33", "0.73");

        when(noteLines
                .findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
                        2L, 3L, 100L))
                .thenReturn(List.of(firstLine));

        CreditNoteLine secondLine =
                creditedLine(501L, "1.000", "3.33", "0.73");

        when(noteLines
                .findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
                        2L, 3L, 101L))
                .thenReturn(List.of(secondLine));

        var result = service.loadRegisteredCredits(2L, 3L, 35L);

        assertEquals(1, result.size());
        var amounts = result.get(501L);
        assertNotNull(amounts);
        assertNumber("2.000", amounts.quantity());
        assertNumber("6.66", amounts.subtotal());
        assertNumber("1.46", amounts.tax());

        verify(invoices).findByTenantIdAndCompanyIdAndIdForUpdate(
                2L, 3L, 35L);
        verify(noteLines, times(2))
                .findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
                        eq(2L), eq(3L), anyLong());
    }

    @Test
    void ignoresDraftAndCancelledNotes() {
        originalInvoiceWithOneLine();

        CreditNote draft = note(100L, "DRAFT", "0", "0");
        CreditNote cancelled = note(101L, "CANCELLED", "0", "0");

        when(notes
                .findByTenantIdAndCompanyIdAndCreditNoteTypeAndOriginalInvoiceId(
                        2L, 3L, "SUPPLIER", 35L))
                .thenReturn(List.of(draft, cancelled));

        var result = service.loadRegisteredCredits(2L, 3L, 35L);

        assertNumber("0", result.get(501L).quantity());
        assertNumber("0", result.get(501L).subtotal());
        assertNumber("0", result.get(501L).tax());

        verifyNoInteractions(noteLines);
    }

    @Test
    void rejectsCumulativeQuantityOverOriginal() {
        originalInvoiceWithOneLine();

        CreditNote first = note(100L, "REGISTERED", "10.00", "2.20");
        CreditNote second = note(101L, "REGISTERED", "0.00", "0.00");

        when(notes
                .findByTenantIdAndCompanyIdAndCreditNoteTypeAndOriginalInvoiceId(
                        2L, 3L, "SUPPLIER", 35L))
                .thenReturn(List.of(first, second));

        CreditNoteLine fullyCreditedLine =
                creditedLine(501L, "3.000", "10.00", "2.20");

        when(noteLines
                .findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
                        2L, 3L, 100L))
                .thenReturn(List.of(fullyCreditedLine));

        CreditNoteLine excessLine =
                creditedLine(501L, "0.100", "0.00", "0.00");

        when(noteLines
                .findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
                        2L, 3L, 101L))
                .thenReturn(List.of(excessLine));

        assertThrows(BusinessException.class,
                () -> service.loadRegisteredCredits(2L, 3L, 35L));
    }

    @Test
    void rejectsCreditLineFromDifferentInvoice() {
        originalInvoiceWithOneLine();

        CreditNote registered = note(
                100L, "REGISTERED", "3.33", "0.73");

        when(notes
                .findByTenantIdAndCompanyIdAndCreditNoteTypeAndOriginalInvoiceId(
                        2L, 3L, "SUPPLIER", 35L))
                .thenReturn(List.of(registered));

        CreditNoteLine foreignLine =
                creditedLine(999L, "1.000", "3.33", "0.73");

        when(noteLines
                .findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
                        2L, 3L, 100L))
                .thenReturn(List.of(foreignLine));

        assertThrows(BusinessException.class,
                () -> service.loadRegisteredCredits(2L, 3L, 35L));
    }
}
