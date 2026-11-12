
package com.gestiva.creditnote.service;

import com.gestiva.billing.invoice.entity.Invoice;
import com.gestiva.billing.invoice.entity.InvoiceLine;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.billing.invoice.repository.InvoiceLineRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.creditnote.repository.CreditNoteLineRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerCreditNoteDraftServiceTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 3L;
    private static final Long INVOICE_ID = 10L;

    @Mock
    private InvoiceRepository invoices;

    @Mock
    private InvoiceLineRepository invoiceLines;

    @Mock
    private CreditNoteRepository notes;

    @Mock
    private CreditNoteLineRepository noteLines;

    @Mock
    private DocumentSequenceService sequences;

    @Mock
    private CompanyContext companyContext;

    @Mock
    private CustomerCreditNoteRegisteredCreditsService registeredCredits;

    @InjectMocks
    private CustomerCreditNoteDraftService service;

    @BeforeEach
    void setup() {
        lenient().when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);
    }

    private Invoice invoice(String status) {
        Invoice invoice = new Invoice();

        invoice.setId(INVOICE_ID);
        invoice.setTenantId(TENANT);
        invoice.setCompanyId(COMPANY);
        invoice.setCustomerId(20L);
        invoice.setStatus(status);
        invoice.setCurrencyCode("EUR");
        invoice.setSubtotalAmount(new BigDecimal("100.00"));
        invoice.setTaxAmount(new BigDecimal("22.00"));
        invoice.setTotalAmount(new BigDecimal("122.00"));

        return invoice;
    }

    private InvoiceLine line() {
        InvoiceLine line = new InvoiceLine();

        line.setId(100L);
        line.setTenantId(TENANT);
        line.setInvoiceId(INVOICE_ID);
        line.setLineNo(1);
        line.setDescription("Prodotto di prova");
        line.setQuantity(new BigDecimal("2.000"));
        line.setUnitPrice(new BigDecimal("50.00"));
        line.setDiscountPct(BigDecimal.ZERO);
        line.setTaxPct(new BigDecimal("22.00"));
        line.setTaxAmount(new BigDecimal("22.00"));
        line.setLineTotal(new BigDecimal("122.00"));

        return line;
    }

    @Test
    void createsFullDraft() {
        when(invoices.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, INVOICE_ID))
                .thenReturn(Optional.of(invoice("ISSUED")));

        when(invoiceLines.findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                TENANT, INVOICE_ID))
                .thenReturn(List.of(line()));

        when(sequences.nextCustomerCreditNoteNumber(
                TENANT, COMPANY))
                .thenReturn("CCN-00001");

        when(notes.save(any(CreditNote.class)))
                .thenAnswer(invocation -> {
                    CreditNote note = invocation.getArgument(0);
                    ReflectionTestUtils.setField(note, "id", 50L);
                    return note;
                });

        Long id = service.createFullDraft(
                TENANT, INVOICE_ID, "Rettifica completa");

        assertEquals(50L, id);

        verify(notes).save(argThat(note ->
                "CUSTOMER".equals(note.getCreditNoteType())
                        && "DRAFT".equals(note.getStatus())
                        && "CCN-00001".equals(note.getCreditNoteNumber())
                        && note.getTotalAmount()
                        .compareTo(new BigDecimal("122.00")) == 0
        ));

        verify(noteLines).saveAll(argThat(lines -> {
            var iterator = lines.iterator();

            if (!iterator.hasNext()) {
                return false;
            }

            var creditLine = iterator.next();

            return !iterator.hasNext()
                    && Long.valueOf(100L).equals(
                    creditLine.getOriginalInvoiceLineId())
                    && Long.valueOf(50L).equals(
                    creditLine.getCreditNoteId())
                    && creditLine.getLineSubtotal()
                    .compareTo(new BigDecimal("100.00")) == 0;
        }));
    }

    @Test
    void rejectsNotIssuedInvoice() {
        when(invoices.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, INVOICE_ID))
                .thenReturn(Optional.of(invoice("DRAFT")));

        assertThrows(BusinessException.class,
                () -> service.createFullDraft(
                        TENANT, INVOICE_ID, "Rettifica"));

        verify(notes, never()).save(any());
        verify(noteLines, never()).saveAll(any());
    }

    @Test
    void rejectsInvoiceOutsideCurrentCompany() {
        when(invoices.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, INVOICE_ID))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> service.createFullDraft(
                        TENANT, INVOICE_ID, "Rettifica"));

        verify(notes, never()).save(any());
    }

    @Test
    void rejectsInconsistentTotals() {
        Invoice invoice = invoice("ISSUED");
        invoice.setTotalAmount(new BigDecimal("123.00"));

        when(invoices.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, INVOICE_ID))
                .thenReturn(Optional.of(invoice));

        when(invoiceLines.findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                TENANT, INVOICE_ID))
                .thenReturn(List.of(line()));

        assertThrows(BusinessException.class,
                () -> service.createFullDraft(
                        TENANT, INVOICE_ID, "Rettifica"));

        verify(notes, never()).save(any());
        verify(noteLines, never()).saveAll(any());
        verify(sequences, never())
                .nextCustomerCreditNoteNumber(anyLong(), anyLong());
    }


    // PASSO 5.13.2394V
    // Test creazione note credito clienti parziali.

    @Test
    void createsPartialDraftWithPreviousCredits() {
        when(invoices.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, INVOICE_ID))
                .thenReturn(Optional.of(invoice("ISSUED")));

        when(invoiceLines.findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                TENANT, INVOICE_ID))
                .thenReturn(List.of(line()));

        when(registeredCredits.loadRegisteredCredits(
                TENANT, COMPANY, INVOICE_ID))
                .thenReturn(Map.of(
                        100L,
                        new CustomerCreditNoteCumulativeCalculator.PreviousCredits(
                                new BigDecimal("1.000"),
                                new BigDecimal("50.00"),
                                new BigDecimal("11.00"))));

        when(sequences.nextCustomerCreditNoteNumber(
                TENANT, COMPANY))
                .thenReturn("CCN-00002");

        when(notes.save(any(CreditNote.class)))
                .thenAnswer(invocation -> {
                    CreditNote note = invocation.getArgument(0);
                    ReflectionTestUtils.setField(note, "id", 51L);
                    return note;
                });

        Long id = service.createPartialDraft(
                TENANT,
                INVOICE_ID,
                Map.of(100L, new BigDecimal("1.000")),
                "Rettifica parziale");

        assertEquals(51L, id);

        verify(notes).save(argThat(note ->
                "CUSTOMER".equals(note.getCreditNoteType())
                        && "DRAFT".equals(note.getStatus())
                        && "CCN-00002".equals(note.getCreditNoteNumber())
                        && note.getSubtotalAmount()
                        .compareTo(new BigDecimal("50.00")) == 0
                        && note.getTaxAmount()
                        .compareTo(new BigDecimal("11.00")) == 0
                        && note.getTotalAmount()
                        .compareTo(new BigDecimal("61.00")) == 0
        ));

        verify(noteLines).saveAll(argThat(lines -> {
            var iterator = lines.iterator();

            if (!iterator.hasNext()) {
                return false;
            }

            var creditLine = iterator.next();

            return !iterator.hasNext()
                    && Long.valueOf(51L).equals(
                    creditLine.getCreditNoteId())
                    && Long.valueOf(100L).equals(
                    creditLine.getOriginalInvoiceLineId())
                    && creditLine.getQuantity()
                    .compareTo(new BigDecimal("1.000")) == 0
                    && creditLine.getLineTotal()
                    .compareTo(new BigDecimal("61.00")) == 0;
        }));
    }

    @Test
    void rejectsPartialQuantityAboveRemaining() {
        when(invoices.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, INVOICE_ID))
                .thenReturn(Optional.of(invoice("ISSUED")));

        when(invoiceLines.findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                TENANT, INVOICE_ID))
                .thenReturn(List.of(line()));

        when(registeredCredits.loadRegisteredCredits(
                TENANT, COMPANY, INVOICE_ID))
                .thenReturn(Map.of(
                        100L,
                        new CustomerCreditNoteCumulativeCalculator.PreviousCredits(
                                new BigDecimal("1.500"),
                                new BigDecimal("75.00"),
                                new BigDecimal("16.50"))));

        assertThrows(BusinessException.class,
                () -> service.createPartialDraft(
                        TENANT,
                        INVOICE_ID,
                        Map.of(100L, new BigDecimal("1.000")),
                        "Rettifica eccessiva"));

        verify(notes, never()).save(any());
        verify(noteLines, never()).saveAll(any());
        verify(sequences, never())
                .nextCustomerCreditNoteNumber(anyLong(), anyLong());
    }

    @Test
    void rejectsForeignLineInPartialDraft() {
        when(invoices.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, INVOICE_ID))
                .thenReturn(Optional.of(invoice("ISSUED")));

        when(invoiceLines.findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                TENANT, INVOICE_ID))
                .thenReturn(List.of(line()));

        when(registeredCredits.loadRegisteredCredits(
                TENANT, COMPANY, INVOICE_ID))
                .thenReturn(Map.of());

        assertThrows(BusinessException.class,
                () -> service.createPartialDraft(
                        TENANT,
                        INVOICE_ID,
                        Map.of(999L, new BigDecimal("1.000")),
                        "Riga estranea"));

        verify(notes, never()).save(any());
        verify(noteLines, never()).saveAll(any());
    }

    @Test
    void rejectsPartialDraftOutsideCurrentCompany() {
        when(invoices.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, INVOICE_ID))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> service.createPartialDraft(
                        TENANT,
                        INVOICE_ID,
                        Map.of(100L, new BigDecimal("1.000")),
                        "Società estranea"));

        verifyNoInteractions(registeredCredits);
        verify(notes, never()).save(any());
        verify(noteLines, never()).saveAll(any());
    }

}
