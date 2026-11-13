
package com.gestiva.creditnote.service;

import com.gestiva.billing.invoice.entity.Invoice;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteLine;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.creditnote.repository.CreditNoteLineRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerCreditNoteDetailServiceTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 3L;
    private static final Long INVOICE = 10L;
    private static final Long NOTE = 50L;

    @Mock
    InvoiceRepository invoices;

    @Mock
    CreditNoteRepository notes;

    @Mock
    CreditNoteLineRepository noteLines;

    @Mock
    CompanyContext companyContext;

    @InjectMocks
    CustomerCreditNoteDetailService service;

    private Invoice invoice() {
        Invoice value = new Invoice();
        value.setId(INVOICE);
        value.setTenantId(TENANT);
        value.setCompanyId(COMPANY);
        value.setStatus("ISSUED");
        return value;
    }

    private CreditNote note(
            Long id,
            String type,
            LocalDate date) {

        CreditNote value = new CreditNote();
        value.setId(id);
        value.setTenantId(TENANT);
        value.setCompanyId(COMPANY);
        value.setOriginalInvoiceId(INVOICE);
        value.setCreditNoteType(type);
        value.setCreditNoteDate(date);
        value.setStatus("DRAFT");
        return value;
    }

    private void mockCompany() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);
    }

    @Test
    void listsOnlyCustomerNotesForCurrentInvoice() {
        mockCompany();

        when(invoices.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, INVOICE))
                .thenReturn(Optional.of(invoice()));

        CreditNote first =
                note(50L, "CUSTOMER", LocalDate.of(2026, 11, 10));

        CreditNote second =
                note(51L, "CUSTOMER", LocalDate.of(2026, 11, 12));

        when(notes
                .findByTenantIdAndCompanyIdAndCreditNoteTypeAndOriginalInvoiceId(
                        TENANT, COMPANY, "CUSTOMER", INVOICE))
                .thenReturn(List.of(first, second));

        var result =
                service.findCustomerNotesForInvoice(TENANT, INVOICE);

        assertEquals(2, result.size());
        assertEquals(51L, result.get(0).getId());
        assertEquals(50L, result.get(1).getId());

        verify(notes)
                .findByTenantIdAndCompanyIdAndCreditNoteTypeAndOriginalInvoiceId(
                        TENANT, COMPANY, "CUSTOMER", INVOICE);
    }

    @Test
    void rejectsInvoiceOutsideCurrentCompany() {
        mockCompany();

        when(invoices.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, INVOICE))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.findCustomerNotesForInvoice(
                        TENANT, INVOICE));

        verifyNoInteractions(notes, noteLines);
    }

    @Test
    void rejectsInvalidInvoiceIdentifiers() {
        assertThrows(
                NotFoundException.class,
                () -> service.findCustomerNotesForInvoice(
                        null, INVOICE));

        assertThrows(
                NotFoundException.class,
                () -> service.findCustomerNotesForInvoice(
                        TENANT, 0L));

        verifyNoInteractions(
                companyContext, invoices, notes, noteLines);
    }

    @Test
    void returnsEmptyListWhenInvoiceHasNoCreditNotes() {
        mockCompany();

        when(invoices.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, INVOICE))
                .thenReturn(Optional.of(invoice()));

        when(notes
                .findByTenantIdAndCompanyIdAndCreditNoteTypeAndOriginalInvoiceId(
                        TENANT, COMPANY, "CUSTOMER", INVOICE))
                .thenReturn(List.of());

        var result =
                service.findCustomerNotesForInvoice(TENANT, INVOICE);

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsCustomerNoteWithItsLines() {
        mockCompany();

        CreditNote customerNote =
                note(NOTE, "CUSTOMER", LocalDate.of(2026, 11, 13));

        CreditNoteLine line = new CreditNoteLine();
        line.setId(100L);
        line.setTenantId(TENANT);
        line.setCompanyId(COMPANY);
        line.setCreditNoteId(NOTE);
        line.setLineNo(1);

        when(notes.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, NOTE))
                .thenReturn(Optional.of(customerNote));

        when(noteLines
                .findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
                        TENANT, COMPANY, NOTE))
                .thenReturn(List.of(line));

        var detail = service.getDetail(TENANT, NOTE);

        assertSame(customerNote, detail.note());
        assertEquals(1, detail.lines().size());
        assertSame(line, detail.lines().get(0));
    }

    @Test
    void rejectsSupplierNote() {
        mockCompany();

        when(notes.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, NOTE))
                .thenReturn(Optional.of(
                        note(NOTE, "SUPPLIER", LocalDate.now())));

        assertThrows(
                NotFoundException.class,
                () -> service.getDetail(TENANT, NOTE));

        verifyNoInteractions(noteLines);
    }

    @Test
    void rejectsNoteOutsideCurrentCompany() {
        mockCompany();

        when(notes.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, NOTE))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.getDetail(TENANT, NOTE));

        verifyNoInteractions(noteLines);
    }

    @Test
    void rejectsInvalidNoteIdentifiers() {
        assertThrows(
                NotFoundException.class,
                () -> service.getDetail(TENANT, null));

        assertThrows(
                NotFoundException.class,
                () -> service.getDetail(-1L, NOTE));

        verifyNoInteractions(
                companyContext, invoices, notes, noteLines);
    }
}
