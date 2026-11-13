
package com.gestiva.creditnote.service;

import com.gestiva.billing.invoice.entity.Invoice;
import com.gestiva.billing.invoice.entity.InvoiceLine;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteLine;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CustomerCreditNoteRegistrationValidatorTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 3L;
    private static final Long INVOICE_ID = 10L;
    private static final Long NOTE_ID = 50L;
    private static final Long ORIGINAL_LINE_ID = 100L;

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private Invoice invoice() {
        Invoice value = new Invoice();
        value.setId(INVOICE_ID);
        value.setTenantId(TENANT);
        value.setCompanyId(COMPANY);
        value.setCustomerId(20L);
        value.setStatus("ISSUED");
        value.setCurrencyCode("EUR");
        return value;
    }

    private InvoiceLine original() {
        InvoiceLine value = new InvoiceLine();

        value.setId(ORIGINAL_LINE_ID);
        value.setTenantId(TENANT);
        value.setInvoiceId(INVOICE_ID);

        value.setLineNo(1);
        value.setItemId(5L);
        value.setDescription("Prodotto test");

        value.setQuantity(bd("3.000"));
        value.setUnitOfMeasure("PZ");
        value.setUnitPrice(bd("33.33"));
        value.setDiscountPct(bd("0.00"));
        value.setTaxPct(bd("22.00"));

        value.setTaxAmount(bd("22.00"));
        value.setLineTotal(bd("100.00"));

        return value;
    }

    private CreditNote draft() {
        CreditNote value = new CreditNote();

        value.setId(NOTE_ID);
        value.setTenantId(TENANT);
        value.setCompanyId(COMPANY);
        value.setOriginalInvoiceId(INVOICE_ID);
        value.setPartyId(20L);

        value.setCreditNoteType("CUSTOMER");
        value.setStatus("DRAFT");
        value.setCurrencyCode("EUR");

        value.setSubtotalAmount(bd("33.33"));
        value.setTaxAmount(bd("7.33"));
        value.setTotalAmount(bd("40.66"));

        return value;
    }

    private CreditNoteLine draftLine() {
        CreditNoteLine value = new CreditNoteLine();

        value.setTenantId(TENANT);
        value.setCompanyId(COMPANY);
        value.setCreditNoteId(NOTE_ID);
        value.setOriginalInvoiceLineId(ORIGINAL_LINE_ID);

        value.setLineNo(1);
        value.setItemId(5L);
        value.setDescription("Prodotto test");

        value.setQuantity(bd("1.000"));
        value.setUnitOfMeasure("PZ");
        value.setUomCode("PZ");
        value.setUnitPrice(bd("33.33"));
        value.setDiscountPct(bd("0.00"));
        value.setTaxPct(bd("22.00"));

        value.setLineSubtotal(bd("33.33"));
        value.setTaxAmount(bd("7.33"));
        value.setLineTotal(bd("40.66"));

        return value;
    }

    private Map<Long,
            CustomerCreditNoteCumulativeCalculator.PreviousCredits>
    previous(String qty, String subtotal, String tax) {

        return Map.of(
                ORIGINAL_LINE_ID,
                new CustomerCreditNoteCumulativeCalculator.PreviousCredits(
                        bd(qty), bd(subtotal), bd(tax))
        );
    }

    private void validate(
            Invoice invoice,
            CreditNote draft,
            List<CreditNoteLine> lines,
            Map<Long,
                    CustomerCreditNoteCumulativeCalculator.PreviousCredits>
                    previousCredits) {

        CustomerCreditNoteRegistrationValidator.validate(
                invoice,
                draft,
                List.of(original()),
                lines,
                previousCredits
        );
    }

    @Test
    void rejectsLegacyDraftAmountsAndAcceptsCorrectRealInvoiceAmounts() {
        InvoiceLine invoiceLine = original();
        invoiceLine.setQuantity(bd("2.000"));
        invoiceLine.setUnitPrice(bd("85.00"));
        invoiceLine.setDiscountPct(bd("10.00"));
        invoiceLine.setLineTotal(bd("153.00"));
        invoiceLine.setTaxAmount(bd("33.66"));

        CreditNote note = draft();
        CreditNoteLine line = draftLine();

        line.setUnitPrice(bd("85.00"));
        line.setDiscountPct(bd("10.00"));
        line.setQuantity(bd("1.000"));

        var previousCredits =
                previous("0.000", "0.00", "0.00");

        // Prima verifica: la rettifica corretta deve essere accettata.
        note.setSubtotalAmount(bd("76.50"));
        note.setTaxAmount(bd("16.83"));
        note.setTotalAmount(bd("93.33"));

        line.setLineSubtotal(bd("76.50"));
        line.setTaxAmount(bd("16.83"));
        line.setLineTotal(bd("93.33"));

        assertDoesNotThrow(() ->
                CustomerCreditNoteRegistrationValidator.validate(
                        invoice(),
                        note,
                        List.of(invoiceLine),
                        List.of(line),
                        previousCredits));

        // Seconda verifica: gli importi del vecchio DRAFT
        // CCN-00001 devono essere rifiutati.
        note.setSubtotalAmount(bd("59.67"));
        note.setTaxAmount(bd("16.83"));
        note.setTotalAmount(bd("76.50"));

        line.setLineSubtotal(bd("59.67"));
        line.setTaxAmount(bd("16.83"));
        line.setLineTotal(bd("76.50"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> CustomerCreditNoteRegistrationValidator.validate(
                        invoice(),
                        note,
                        List.of(invoiceLine),
                        List.of(line),
                        previousCredits));

        assertTrue(exception.getMessage().contains(
                "Importi della nota cliente non più compatibili"));
    }

    @Test
    void acceptsValidPartialDraft() {
        assertDoesNotThrow(() -> validate(
                invoice(),
                draft(),
                List.of(draftLine()),
                previous("0.000", "0.00", "0.00")
        ));
    }

    @Test
    void rejectsDraftWhenRemainingQuantityIsInsufficient() {
        assertThrows(BusinessException.class, () -> validate(
                invoice(),
                draft(),
                List.of(draftLine()),
                previous("3.000", "100.00", "22.00")
        ));
    }

    @Test
    void rejectsChangedDraftAmounts() {
        CreditNoteLine line = draftLine();

        line.setLineSubtotal(bd("34.00"));
        line.setLineTotal(bd("41.33"));

        assertThrows(BusinessException.class, () -> validate(
                invoice(),
                draft(),
                List.of(line),
                previous("0.000", "0.00", "0.00")
        ));
    }

    @Test
    void rejectsForeignOriginalLine() {
        CreditNoteLine line = draftLine();
        line.setOriginalInvoiceLineId(999L);

        assertThrows(BusinessException.class, () -> validate(
                invoice(),
                draft(),
                List.of(line),
                previous("0.000", "0.00", "0.00")
        ));
    }

    @Test
    void rejectsDifferentCompany() {
        CreditNote note = draft();
        note.setCompanyId(999L);

        assertThrows(BusinessException.class, () -> validate(
                invoice(),
                note,
                List.of(draftLine()),
                previous("0.000", "0.00", "0.00")
        ));
    }

    @Test
    void rejectsDuplicateOriginalLines() {
        CreditNoteLine first = draftLine();
        CreditNoteLine second = draftLine();

        assertThrows(BusinessException.class, () -> validate(
                invoice(),
                draft(),
                List.of(first, second),
                previous("0.000", "0.00", "0.00")
        ));
    }

    @Test
    void rejectsAlreadyRegisteredNote() {
        CreditNote note = draft();
        note.setStatus("REGISTERED");

        assertThrows(BusinessException.class, () -> validate(
                invoice(),
                note,
                List.of(draftLine()),
                previous("0.000", "0.00", "0.00")
        ));
    }
}
