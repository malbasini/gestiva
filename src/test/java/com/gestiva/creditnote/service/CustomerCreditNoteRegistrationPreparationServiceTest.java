
package com.gestiva.creditnote.service;

import com.gestiva.billing.invoice.entity.Invoice;
import com.gestiva.billing.invoice.entity.InvoiceLine;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.billing.invoice.repository.InvoiceLineRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteLine;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.creditnote.repository.CreditNoteLineRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerCreditNoteRegistrationPreparationServiceTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 3L;
    private static final Long INVOICE = 10L;
    private static final Long NOTE = 50L;
    private static final Long LINE = 100L;

    @Mock
    CompanyContext companyContext;

    @Mock
    InvoiceRepository invoices;

    @Mock
    InvoiceLineRepository invoiceLines;

    @Mock
    CreditNoteRepository notes;

    @Mock
    CreditNoteLineRepository noteLines;

    @Mock
    CustomerCreditNoteRegisteredCreditsService registeredCredits;

    @InjectMocks
    CustomerCreditNoteRegistrationPreparationService service;

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private Invoice invoice() {
        Invoice value = new Invoice();
        value.setId(INVOICE);
        value.setTenantId(TENANT);
        value.setCompanyId(COMPANY);
        value.setCustomerId(20L);
        value.setCurrencyCode("EUR");
        value.setStatus("ISSUED");
        return value;
    }

    private CreditNote note(String status) {
        CreditNote value = new CreditNote();
        value.setId(NOTE);
        value.setTenantId(TENANT);
        value.setCompanyId(COMPANY);
        value.setOriginalInvoiceId(INVOICE);
        value.setPartyId(20L);
        value.setCurrencyCode("EUR");
        value.setCreditNoteType("CUSTOMER");
        value.setStatus(status);
        value.setSubtotalAmount(bd("50.00"));
        value.setTaxAmount(bd("11.00"));
        value.setTotalAmount(bd("61.00"));
        return value;
    }

    private InvoiceLine originalLine() {
        InvoiceLine value = new InvoiceLine();
        value.setId(LINE);
        value.setTenantId(TENANT);
        value.setInvoiceId(INVOICE);
        value.setLineNo(1);
        value.setDescription("Prodotto test");
        value.setQuantity(bd("2.000"));
        value.setUnitOfMeasure("PZ");
        value.setUnitPrice(bd("50.00"));
        value.setDiscountPct(bd("0.00"));
        value.setTaxPct(bd("22.00"));
        value.setTaxAmount(bd("22.00"));
        value.setLineTotal(bd("122.00"));
        return value;
    }

    private CreditNoteLine draftLine() {
        CreditNoteLine value = new CreditNoteLine();
        value.setTenantId(TENANT);
        value.setCompanyId(COMPANY);
        value.setCreditNoteId(NOTE);
        value.setOriginalInvoiceLineId(LINE);
        value.setLineNo(1);
        value.setDescription("Prodotto test");
        value.setQuantity(bd("1.000"));
        value.setUnitOfMeasure("PZ");
        value.setUomCode("PZ");
        value.setUnitPrice(bd("50.00"));
        value.setDiscountPct(bd("0.00"));
        value.setTaxPct(bd("22.00"));
        value.setLineSubtotal(bd("50.00"));
        value.setTaxAmount(bd("11.00"));
        value.setLineTotal(bd("61.00"));
        return value;
    }

    private void mockInitialNote() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        when(notes.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, NOTE))
                .thenReturn(Optional.of(note("DRAFT")));
    }

    @Test
    void validatesDraftWithCorrectLockOrder() {
        mockInitialNote();

        when(invoices.findByTenantIdAndCompanyIdAndIdForUpdate(
                TENANT, COMPANY, INVOICE))
                .thenReturn(Optional.of(invoice()));

        when(notes.findByTenantIdAndCompanyIdAndIdForUpdate(
                TENANT, COMPANY, NOTE))
                .thenReturn(Optional.of(note("DRAFT")));

        when(registeredCredits.loadRegisteredCredits(
                TENANT, COMPANY, INVOICE))
                .thenReturn(Map.of());

        when(invoiceLines.findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                TENANT, INVOICE))
                .thenReturn(List.of(originalLine()));

        when(noteLines
                .findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
                        TENANT, COMPANY, NOTE))
                .thenReturn(List.of(draftLine()));

        assertDoesNotThrow(() ->
                service.validateUnderLock(TENANT, NOTE));

        InOrder order = inOrder(invoices, notes, registeredCredits);

        order.verify(notes).findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, NOTE);

        order.verify(invoices)
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        TENANT, COMPANY, INVOICE);

        order.verify(notes)
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        TENANT, COMPANY, NOTE);

        order.verify(registeredCredits)
                .loadRegisteredCredits(
                        TENANT, COMPANY, INVOICE);

        verify(notes, never()).save(any());
        verify(notes, never()).saveAndFlush(any());
        verify(noteLines, never()).saveAll(any());
    }

    @Test
    void rejectsAlreadyRegisteredNote() {
        mockInitialNote();

        when(invoices.findByTenantIdAndCompanyIdAndIdForUpdate(
                TENANT, COMPANY, INVOICE))
                .thenReturn(Optional.of(invoice()));

        when(notes.findByTenantIdAndCompanyIdAndIdForUpdate(
                TENANT, COMPANY, NOTE))
                .thenReturn(Optional.of(note("REGISTERED")));

        assertThrows(BusinessException.class,
                () -> service.validateUnderLock(TENANT, NOTE));

        verifyNoInteractions(registeredCredits);
        verify(notes, never()).saveAndFlush(any());
    }

    @Test
    void rejectsInvoiceOutsideActiveCompany() {
        mockInitialNote();

        when(invoices.findByTenantIdAndCompanyIdAndIdForUpdate(
                TENANT, COMPANY, INVOICE))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> service.validateUnderLock(TENANT, NOTE));

        verify(notes, never())
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        anyLong(), anyLong(), anyLong());

        verifyNoInteractions(registeredCredits);
    }

    @Test
    void rejectsDraftWithChangedOriginalInvoice() {
        mockInitialNote();

        when(invoices.findByTenantIdAndCompanyIdAndIdForUpdate(
                TENANT, COMPANY, INVOICE))
                .thenReturn(Optional.of(invoice()));

        CreditNote changed = note("DRAFT");
        changed.setOriginalInvoiceId(999L);

        when(notes.findByTenantIdAndCompanyIdAndIdForUpdate(
                TENANT, COMPANY, NOTE))
                .thenReturn(Optional.of(changed));

        assertThrows(BusinessException.class,
                () -> service.validateUnderLock(TENANT, NOTE));

        verifyNoInteractions(registeredCredits);
        verify(notes, never()).saveAndFlush(any());
    }
}
