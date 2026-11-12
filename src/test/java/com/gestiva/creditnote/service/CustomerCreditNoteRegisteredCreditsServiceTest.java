
package com.gestiva.creditnote.service;

import com.gestiva.billing.invoice.entity.Invoice;
import com.gestiva.billing.invoice.entity.InvoiceLine;
import com.gestiva.billing.invoice.repository.InvoiceLineRepository;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteLine;
import com.gestiva.creditnote.repository.CreditNoteLineRepository;
import com.gestiva.creditnote.repository.CreditNoteRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerCreditNoteRegisteredCreditsServiceTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 3L;
    private static final Long INVOICE = 10L;
    private static final Long LINE = 100L;

    @Mock
    InvoiceRepository invoices;

    @Mock
    InvoiceLineRepository invoiceLines;

    @Mock
    CreditNoteRepository notes;

    @Mock
    CreditNoteLineRepository noteLines;

    @InjectMocks
    CustomerCreditNoteRegisteredCreditsService service;

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    @BeforeEach
    void setup() {
        Invoice invoice = new Invoice();
        invoice.setId(INVOICE);
        invoice.setTenantId(TENANT);
        invoice.setCompanyId(COMPANY);
        invoice.setCustomerId(20L);
        invoice.setCurrencyCode("EUR");
        invoice.setStatus("ISSUED");

        InvoiceLine original = new InvoiceLine();
        original.setId(LINE);
        original.setTenantId(TENANT);
        original.setInvoiceId(INVOICE);
        original.setQuantity(bd("3.000"));
        original.setLineTotal(bd("122.00"));
        original.setTaxAmount(bd("22.00"));

        when(invoices.findByTenantIdAndCompanyIdAndIdForUpdate(
                TENANT, COMPANY, INVOICE))
                .thenReturn(Optional.of(invoice));

        when(invoiceLines.findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                TENANT, INVOICE))
                .thenReturn(List.of(original));
    }

    private CreditNote note(Long id, String status) {
        CreditNote note = new CreditNote();
        note.setId(id);
        note.setTenantId(TENANT);
        note.setCompanyId(COMPANY);
        note.setOriginalInvoiceId(INVOICE);
        note.setCreditNoteType("CUSTOMER");
        note.setPartyId(20L);
        note.setCurrencyCode("EUR");
        note.setStatus(status);
        return note;
    }

    private CreditNoteLine line(
            Long noteId,
            Long originalLineId,
            String qty,
            String subtotal,
            String tax) {

        CreditNoteLine line = new CreditNoteLine();
        line.setTenantId(TENANT);
        line.setCompanyId(COMPANY);
        line.setCreditNoteId(noteId);
        line.setOriginalInvoiceLineId(originalLineId);
        line.setQuantity(bd(qty));
        line.setLineSubtotal(bd(subtotal));
        line.setTaxAmount(bd(tax));
        line.setLineTotal(
                bd(subtotal).add(bd(tax)));
        return line;
    }

    private void mockNoteLines(
            Long noteId,
            CreditNoteLine line) {

        when(noteLines
                .findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
                        TENANT, COMPANY, noteId))
                .thenReturn(List.of(line));
    }

    @Test
    void ignoresDraftNotes() {
        when(notes
                .findByTenantIdAndCompanyIdAndCreditNoteTypeAndOriginalInvoiceId(
                        TENANT, COMPANY, "CUSTOMER", INVOICE))
                .thenReturn(List.of(note(1L, "DRAFT")));

        var result = service.loadRegisteredCredits(
                TENANT, COMPANY, INVOICE);

        var used = result.get(LINE);

        assertNotNull(used);
        assertEquals(0, used.quantity().compareTo(bd("0")));
        assertEquals(0, used.subtotal().compareTo(bd("0")));
        assertEquals(0, used.tax().compareTo(bd("0")));

        verifyNoInteractions(noteLines);
    }

    @Test
    void aggregatesRegisteredNotes() {
        CreditNote first = note(1L, "REGISTERED");
        first.setSubtotalAmount(bd("33.33"));
        first.setTaxAmount(bd("7.33"));
        first.setTotalAmount(bd("40.66"));

        CreditNote second = note(2L, "REGISTERED");
        second.setSubtotalAmount(bd("33.33"));
        second.setTaxAmount(bd("7.33"));
        second.setTotalAmount(bd("40.66"));

        when(notes
                .findByTenantIdAndCompanyIdAndCreditNoteTypeAndOriginalInvoiceId(
                        TENANT, COMPANY, "CUSTOMER", INVOICE))
                .thenReturn(List.of(first, second));

        mockNoteLines(1L, line(
                1L, LINE, "1.000", "33.33", "7.33"));

        mockNoteLines(2L, line(
                2L, LINE, "1.000", "33.33", "7.33"));

        var result = service.loadRegisteredCredits(
                TENANT, COMPANY, INVOICE);

        var used = result.get(LINE);

        assertEquals(
                0, used.quantity().compareTo(bd("2.000")));

        assertEquals(
                0, used.subtotal().compareTo(bd("66.66")));

        assertEquals(
                0, used.tax().compareTo(bd("14.66")));
    }

    @Test
    void rejectsForeignInvoiceLine() {
        CreditNote registered = note(1L, "REGISTERED");
        registered.setSubtotalAmount(bd("33.33"));
        registered.setTaxAmount(bd("7.33"));
        registered.setTotalAmount(bd("40.66"));

        when(notes
                .findByTenantIdAndCompanyIdAndCreditNoteTypeAndOriginalInvoiceId(
                        TENANT, COMPANY, "CUSTOMER", INVOICE))
                .thenReturn(List.of(registered));

        mockNoteLines(1L, line(
                1L, 999L, "1.000", "33.33", "7.33"));

        assertThrows(
                BusinessException.class,
                () -> service.loadRegisteredCredits(
                        TENANT, COMPANY, INVOICE));
    }
}
