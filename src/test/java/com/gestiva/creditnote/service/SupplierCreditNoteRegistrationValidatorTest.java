package com.gestiva.creditnote.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteLine;
import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import com.gestiva.purchasing.invoice.entity.SupplierInvoiceLine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierCreditNoteRegistrationValidatorTest {

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
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

    private SupplierInvoiceLine originalLine() {
        SupplierInvoiceLine line = mock(SupplierInvoiceLine.class);
        when(line.getId()).thenReturn(501L);
        when(line.getQuantity()).thenReturn(bd("3.000"));
        when(line.getLineSubtotal()).thenReturn(bd("10.00"));
        when(line.getTaxAmount()).thenReturn(bd("2.20"));
        when(line.getLineTotal()).thenReturn(bd("12.20"));
        return line;
    }

    private CreditNote draft(String subtotal, String tax, Long companyId) {
        CreditNote note = mock(CreditNote.class);
        when(note.getId()).thenReturn(100L);
        when(note.getTenantId()).thenReturn(2L);
        when(note.getCompanyId()).thenReturn(companyId);
        when(note.getOriginalInvoiceId()).thenReturn(35L);
        when(note.getPartyId()).thenReturn(11L);
        when(note.getCreditNoteType()).thenReturn("SUPPLIER");
        when(note.getStatus()).thenReturn("DRAFT");
        when(note.getCurrencyCode()).thenReturn("EUR");
        when(note.getSubtotalAmount()).thenReturn(bd(subtotal));
        when(note.getTaxAmount()).thenReturn(bd(tax));
        when(note.getTotalAmount()).thenReturn(
                bd(subtotal).add(bd(tax)));
        return note;
    }

    private CreditNoteLine draftLine(String subtotal, String tax) {
        CreditNoteLine line = mock(CreditNoteLine.class);
        when(line.getOriginalInvoiceLineId()).thenReturn(501L);
        when(line.getCreditNoteId()).thenReturn(100L);
        when(line.getTenantId()).thenReturn(2L);
        when(line.getCompanyId()).thenReturn(3L);
        when(line.getQuantity()).thenReturn(bd("1.000"));
        when(line.getLineSubtotal()).thenReturn(bd(subtotal));
        when(line.getTaxAmount()).thenReturn(bd(tax));
        when(line.getLineTotal()).thenReturn(
                bd(subtotal).add(bd(tax)));
        return line;
    }

    private Map<Long, SupplierCreditNoteCumulativeCalculator.PreviousCredits>
    previous(String quantity, String subtotal, String tax) {
        return Map.of(
                501L,
                new SupplierCreditNoteCumulativeCalculator.PreviousCredits(
                        bd(quantity), bd(subtotal), bd(tax))
        );
    }

    private void validate(
            CreditNote note,
            CreditNoteLine line,
            Map<Long, SupplierCreditNoteCumulativeCalculator.PreviousCredits>
                    previousCredits) {

        SupplierCreditNoteRegistrationValidator.validate(
                originalInvoice(),
                note,
                List.of(originalLine()),
                List.of(line),
                previousCredits);
    }

    @Test
    void acceptsConsistentDraftWithoutPreviousCredits() {
        assertDoesNotThrow(() -> validate(
                draft("3.33", "0.73", 3L),
                draftLine("3.33", "0.73"),
                previous("0.000", "0.00", "0.00")));
    }

    @Test
    void rejectsDraftFromAnotherCompany() {
        assertThrows(BusinessException.class, () -> validate(
                draft("3.33", "0.73", 4L),
                draftLine("3.33", "0.73"),
                previous("0.000", "0.00", "0.00")));
    }

    @Test
    void rejectsQuantityAlreadyFullyCredited() {
        assertThrows(BusinessException.class, () -> validate(
                draft("3.33", "0.73", 3L),
                draftLine("3.33", "0.73"),
                previous("3.000", "10.00", "2.20")));
    }

    @Test
    void rejectsStaleRoundingAmounts() {
        // Dopo due rettifiche da 1 unità, l'ultima deve
        // avere imponibile 3,34 e IVA 0,74, non 3,33 e 0,73.
        assertThrows(BusinessException.class, () -> validate(
                draft("3.33", "0.73", 3L),
                draftLine("3.33", "0.73"),
                previous("2.000", "6.66", "1.46")));
    }
}
