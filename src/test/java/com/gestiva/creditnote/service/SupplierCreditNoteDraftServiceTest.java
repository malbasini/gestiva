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
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SupplierCreditNoteDraftServiceTest {

    private SupplierInvoiceRepository invoices;
    private SupplierInvoiceLineRepository invoiceLines;
    private CreditNoteRepository notes;
    private CreditNoteLineRepository noteLines;
    private DocumentSequenceService sequences;
    private CompanyContext companies;
    private SupplierCreditNoteRegisteredCreditsService registeredCredits;
    private SupplierCreditNoteDraftService service;

    @BeforeEach
    void setup() {
        invoices = mock(SupplierInvoiceRepository.class);
        invoiceLines = mock(SupplierInvoiceLineRepository.class);
        notes = mock(CreditNoteRepository.class);
        noteLines = mock(CreditNoteLineRepository.class);
        sequences = mock(DocumentSequenceService.class);
        companies = mock(CompanyContext.class);
        registeredCredits = mock(
                SupplierCreditNoteRegisteredCreditsService.class);

        service = new SupplierCreditNoteDraftService(
                invoices, invoiceLines, notes, noteLines,
                sequences, companies, registeredCredits);

        when(companies.resolveCurrentCompanyId()).thenReturn(3L);
        lenient().when(registeredCredits.loadRegisteredCredits(
                anyLong(), anyLong(), anyLong()))
                .thenReturn(Map.of());
    }

    private SupplierInvoice invoice(String status) {
        SupplierInvoice value = new SupplierInvoice();
        value.setTenantId(2L);
        value.setCompanyId(3L);
        value.setStatus(status);
        value.setSupplierId(11L);
        value.setInvoiceNumber("SI-00001");
        value.setInvoiceDate(LocalDate.of(2026, 11, 6));
        value.setCurrencyCode("EUR");
        value.setSubtotalAmount(new BigDecimal("51.84"));
        value.setTaxAmount(new BigDecimal("11.40"));
        value.setTotalAmount(new BigDecimal("63.24"));
        return value;
    }

    @Test
    void createsDraftWithOriginalCommercialSnapshot() {
        var original = invoice("REGISTERED");
        var line = new SupplierInvoiceLine();
        line.setLineNo(1);
        line.setDescription("Articolo test");
        line.setQuantity(new BigDecimal("0.800"));
        line.setUomCode("BOX");
        line.setBaseUomCode("PZ");
        line.setConversionFactor(new BigDecimal("10.000000"));
        line.setUnitPrice(new BigDecimal("72.00"));
        line.setDiscountPct(new BigDecimal("10.00"));
        line.setTaxPct(new BigDecimal("22.00"));
        line.setLineSubtotal(new BigDecimal("51.84"));
        line.setTaxAmount(new BigDecimal("11.40"));
        line.setLineTotal(new BigDecimal("63.24"));

        when(invoices.findByTenantIdAndCompanyIdAndId(2L, 3L, 35L))
                .thenReturn(Optional.of(original));
        when(invoiceLines.findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                eq(2L), any()))
                .thenReturn(List.of(line));
        when(sequences.nextNumber(2L, 3L, "SUPPLIER_CREDIT_NOTE"))
                .thenReturn("SCN-00001");

        CreditNote saved = mock(CreditNote.class);
        when(saved.getId()).thenReturn(100L);
        when(notes.save(any(CreditNote.class))).thenReturn(saved);

        Long result = service.createFullDraft(2L, 35L, "Rettifica collaudo");

        assertEquals(100L, result);

        ArgumentCaptor<CreditNote> header =
                ArgumentCaptor.forClass(CreditNote.class);
        verify(notes).save(header.capture());
        assertEquals("DRAFT", header.getValue().getStatus());
        assertEquals("SUPPLIER", header.getValue().getCreditNoteType());
        assertEquals(3L, header.getValue().getCompanyId());
        assertEquals("SCN-00001", header.getValue().getCreditNoteNumber());
        assertEquals(0, header.getValue().getTotalAmount()
                .compareTo(new BigDecimal("63.24")));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CreditNoteLine>> rows =
                ArgumentCaptor.forClass(List.class);
        verify(noteLines).saveAll(rows.capture());

        assertEquals(1, rows.getValue().size());
        CreditNoteLine copy = rows.getValue().getFirst();
        assertEquals(100L, copy.getCreditNoteId());
        assertEquals(2L, copy.getTenantId());
        assertEquals(3L, copy.getCompanyId());
        assertEquals("BOX", copy.getUomCode());
        assertEquals("PZ", copy.getBaseUomCode());
        assertEquals(0, copy.getQuantity()
                .compareTo(new BigDecimal("0.800")));
        assertEquals(0, copy.getConversionFactor()
                .compareTo(new BigDecimal("10.000000")));
        assertEquals(0, copy.getLineTotal()
                .compareTo(new BigDecimal("63.24")));
    }

    @Test
    void rejectsInvoiceNotRegisteredWithoutWriting() {
        when(invoices.findByTenantIdAndCompanyIdAndId(2L, 3L, 35L))
                .thenReturn(Optional.of(invoice("DRAFT")));

        assertThrows(BusinessException.class,
                () -> service.createFullDraft(2L, 35L, "Test"));

        verifyNoInteractions(notes, noteLines, sequences);
    }

    @Test
    void rejectsInvoiceWithoutLinesWithoutWriting() {
        when(invoices.findByTenantIdAndCompanyIdAndId(2L, 3L, 35L))
                .thenReturn(Optional.of(invoice("REGISTERED")));
        when(invoiceLines.findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                eq(2L), any()))
                .thenReturn(List.of());

        assertThrows(BusinessException.class,
                () -> service.createFullDraft(2L, 35L, "Test"));

        verifyNoInteractions(notes, noteLines, sequences);
    }

    @Test
    void rejectsInconsistentLineTotalWithoutWriting() {
        var original = invoice("REGISTERED");

        var line = new SupplierInvoiceLine();
        line.setLineNo(1);
        line.setDescription("Riga con totale errato");
        line.setQuantity(new BigDecimal("1.000"));
        line.setLineSubtotal(new BigDecimal("51.84"));
        line.setTaxAmount(new BigDecimal("11.40"));
        line.setLineTotal(new BigDecimal("63.23"));

        when(invoices.findByTenantIdAndCompanyIdAndId(2L, 3L, 35L))
                .thenReturn(Optional.of(original));

        when(invoiceLines.findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                eq(2L), any()))
                .thenReturn(List.of(line));

        assertThrows(
                BusinessException.class,
                () -> service.createFullDraft(2L, 35L, "Rettifica test")
        );

        verifyNoInteractions(notes, noteLines, sequences);
    }

    @Test
    void rejectsMismatchBetweenLinesAndHeaderWithoutWriting() {
        var original = invoice("REGISTERED");

        var line = new SupplierInvoiceLine();
        line.setLineNo(1);
        line.setDescription("Riga coerente, testata differente");
        line.setQuantity(new BigDecimal("1.000"));
        line.setLineSubtotal(new BigDecimal("50.84"));
        line.setTaxAmount(new BigDecimal("11.40"));
        line.setLineTotal(new BigDecimal("62.24"));

        when(invoices.findByTenantIdAndCompanyIdAndId(2L, 3L, 35L))
                .thenReturn(Optional.of(original));

        when(invoiceLines.findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                eq(2L), any()))
                .thenReturn(List.of(line));

        assertThrows(
                BusinessException.class,
                () -> service.createFullDraft(2L, 35L, "Rettifica test")
        );

        verifyNoInteractions(notes, noteLines, sequences);
    }


    private SupplierInvoiceLine partialOriginalLine(
            Long id, int number, String subtotal,
            String tax, String total) {

        SupplierInvoiceLine line = mock(SupplierInvoiceLine.class);

        when(line.getId()).thenReturn(id);
        when(line.getLineNo()).thenReturn(number);
        when(line.getDescription()).thenReturn("Articolo " + number);
        when(line.getQuantity()).thenReturn(new BigDecimal("2.000"));
        when(line.getUomCode()).thenReturn("BOX");
        when(line.getBaseUomCode()).thenReturn("PZ");
        when(line.getConversionFactor())
                .thenReturn(new BigDecimal("10.000000"));
        when(line.getUnitPrice()).thenReturn(new BigDecimal("50.00"));
        when(line.getDiscountPct()).thenReturn(BigDecimal.ZERO);
        when(line.getTaxPct()).thenReturn(new BigDecimal("22.00"));
        when(line.getLineSubtotal())
                .thenReturn(new BigDecimal(subtotal));
        when(line.getTaxAmount())
                .thenReturn(new BigDecimal(tax));
        when(line.getLineTotal())
                .thenReturn(new BigDecimal(total));

        return line;
    }

    private void setupPartialInvoice() {
        var original = invoice("REGISTERED");

        var first = partialOriginalLine(
                501L, 1, "40.00", "8.80", "48.80");

        var second = partialOriginalLine(
                502L, 2, "11.84", "2.60", "14.44");

        when(invoices.findByTenantIdAndCompanyIdAndId(
                2L, 3L, 35L))
                .thenReturn(Optional.of(original));

        when(invoiceLines
                .findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                        2L, 35L))
                .thenReturn(List.of(first, second));
    }

    @Test
    void createsPartialDraftWithOnlySelectedLine() {
        setupPartialInvoice();

        when(sequences.nextNumber(
                2L, 3L, "SUPPLIER_CREDIT_NOTE"))
                .thenReturn("SCN-00001");

        CreditNote saved = mock(CreditNote.class);
        when(saved.getId()).thenReturn(200L);
        when(notes.save(any(CreditNote.class)))
                .thenReturn(saved);

        Long result = service.createPartialDraft(
                2L, 35L,
                Map.of(501L, new BigDecimal("1.000")),
                "Rettifica parziale");

        assertEquals(200L, result);

        ArgumentCaptor<CreditNote> noteCaptor =
                ArgumentCaptor.forClass(CreditNote.class);

        verify(notes).save(noteCaptor.capture());

        var note = noteCaptor.getValue();
        assertEquals("SUPPLIER", note.getCreditNoteType());
        assertEquals("DRAFT", note.getStatus());
        assertEquals(2L, note.getTenantId());
        assertEquals(3L, note.getCompanyId());
        assertEquals(0, note.getSubtotalAmount()
                .compareTo(new BigDecimal("20.00")));
        assertEquals(0, note.getTaxAmount()
                .compareTo(new BigDecimal("4.40")));
        assertEquals(0, note.getTotalAmount()
                .compareTo(new BigDecimal("24.40")));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CreditNoteLine>> rows =
                ArgumentCaptor.forClass(List.class);

        verify(noteLines).saveAll(rows.capture());

        assertEquals(1, rows.getValue().size());

        var line = rows.getValue().getFirst();
        assertEquals(501L, line.getOriginalInvoiceLineId());
        assertEquals(200L, line.getCreditNoteId());
        assertEquals("BOX", line.getUomCode());
        assertEquals("PZ", line.getBaseUomCode());
        assertEquals(0, line.getQuantity()
                .compareTo(new BigDecimal("1.000")));
        assertEquals(0, line.getLineTotal()
                .compareTo(new BigDecimal("24.40")));

        verify(sequences).nextNumber(
                2L, 3L, "SUPPLIER_CREDIT_NOTE");
    }

    @Test
    void rejectsForeignLineIdWithoutWriting() {
        setupPartialInvoice();

        assertThrows(
                BusinessException.class,
                () -> service.createPartialDraft(
                        2L, 35L,
                        Map.of(999L, new BigDecimal("1.000")),
                        "Rettifica"));

        verifyNoInteractions(notes, noteLines, sequences);
    }

    @Test
    void rejectsExcessivePartialQuantityWithoutWriting() {
        setupPartialInvoice();

        assertThrows(
                BusinessException.class,
                () -> service.createPartialDraft(
                        2L, 35L,
                        Map.of(501L, new BigDecimal("2.001")),
                        "Rettifica"));

        verifyNoInteractions(notes, noteLines, sequences);
    }

    @Test
    void rejectsEmptySelectionWithoutWriting() {
        assertThrows(
                BusinessException.class,
                () -> service.createPartialDraft(
                        2L, 35L,
                        Map.of(),
                        "Rettifica"));

        verifyNoInteractions(
                invoices, invoiceLines, notes,
                noteLines, sequences);
    }

    @Test
    void rejectsNullSelectedQuantityWithoutWriting() {
        setupPartialInvoice();

        java.util.Map<Long, BigDecimal> requested =
                new java.util.HashMap<>();

        requested.put(501L, null);

        assertThrows(
                BusinessException.class,
                () -> service.createPartialDraft(
                        2L, 35L, requested, "Rettifica"));

        verifyNoInteractions(notes, noteLines, sequences);
    }


    private SupplierInvoiceLine cumulativeOriginalLine() {
        SupplierInvoiceLine line = new SupplierInvoiceLine();
        line.setId(501L);
        line.setLineNo(1);
        line.setDescription("Radiatore test");
        line.setQuantity(new BigDecimal("0.700"));
        line.setUomCode("BOX");
        line.setBaseUomCode("PZ");
        line.setConversionFactor(new BigDecimal("10.000000"));
        line.setUnitPrice(new BigDecimal("72.00"));
        line.setDiscountPct(new BigDecimal("10.00"));
        line.setTaxPct(new BigDecimal("22.00"));
        line.setLineSubtotal(new BigDecimal("45.36"));
        line.setTaxAmount(new BigDecimal("9.98"));
        line.setLineTotal(new BigDecimal("55.34"));
        return line;
    }

    private void setupCumulativePartialInvoice() {
        SupplierInvoice original = invoice("REGISTERED");
        original.setId(36L);
        original.setSubtotalAmount(new BigDecimal("45.36"));
        original.setTaxAmount(new BigDecimal("9.98"));
        original.setTotalAmount(new BigDecimal("55.34"));

        when(invoices.findByTenantIdAndCompanyIdAndId(2L, 3L, 36L))
                .thenReturn(Optional.of(original));

        when(invoiceLines.findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                2L, 36L))
                .thenReturn(List.of(cumulativeOriginalLine()));

        when(registeredCredits.loadRegisteredCredits(2L, 3L, 36L))
                .thenReturn(Map.of(
                        501L,
                        new SupplierCreditNoteCumulativeCalculator.PreviousCredits(
                                new BigDecimal("0.300"),
                                new BigDecimal("19.44"),
                                new BigDecimal("4.28"))));
    }

    @Test
    void rejectsCumulativeOverCreditDuringDraftCreation() {
        setupCumulativePartialInvoice();

        BusinessException error = assertThrows(
                BusinessException.class,
                () -> service.createPartialDraft(
                        2L, 36L,
                        Map.of(501L, new BigDecimal("0.500")),
                        "Storno eccedente"));

        assertTrue(error.getMessage().contains(
                "Quantità cumulativa superiore alla fattura."));

        verify(registeredCredits).loadRegisteredCredits(2L, 3L, 36L);
        verifyNoInteractions(notes, noteLines, sequences);
    }

    @Test
    void createsDraftForExactRemainingQuantity() {
        setupCumulativePartialInvoice();

        when(sequences.nextNumber(2L, 3L, "SUPPLIER_CREDIT_NOTE"))
                .thenReturn("SCN-TEST-RESIDUO");

        CreditNote saved = mock(CreditNote.class);
        when(saved.getId()).thenReturn(100L);
        when(notes.save(any(CreditNote.class))).thenReturn(saved);

        Long id = service.createPartialDraft(
                2L, 36L,
                Map.of(501L, new BigDecimal("0.400")),
                "Storno residuo esatto");

        assertEquals(100L, id);

        ArgumentCaptor<CreditNote> header =
                ArgumentCaptor.forClass(CreditNote.class);
        verify(notes).save(header.capture());

        assertEquals("DRAFT", header.getValue().getStatus());
        assertEquals(0, header.getValue().getSubtotalAmount()
                .compareTo(new BigDecimal("25.92")));
        assertEquals(0, header.getValue().getTaxAmount()
                .compareTo(new BigDecimal("5.70")));
        assertEquals(0, header.getValue().getTotalAmount()
                .compareTo(new BigDecimal("31.62")));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CreditNoteLine>> rows =
                ArgumentCaptor.forClass(List.class);

        verify(noteLines).saveAll(rows.capture());
        assertEquals(1, rows.getValue().size());
        assertEquals(0, rows.getValue().getFirst().getQuantity()
                .compareTo(new BigDecimal("0.400")));
        assertEquals(0, rows.getValue().getFirst().getLineTotal()
                .compareTo(new BigDecimal("31.62")));
    }

}
