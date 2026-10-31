package com.gestiva.accounting.vat.web;

import com.gestiva.billing.invoice.entity.Invoice;
import com.gestiva.billing.invoice.entity.InvoiceLine;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.billing.invoice.repository.InvoiceLineRepository;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VatSalesRegisterWebServiceTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 5L;

    private static final LocalDate FROM =
            LocalDate.of(2026, 10, 1);

    private static final LocalDate TO =
            LocalDate.of(2026, 10, 31);

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceLineRepository invoiceLineRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private VatSalesRegisterWebService service;

    private Invoice invoice(Long id, String status) {
        Invoice invoice = mock(Invoice.class);

        when(invoice.getStatus()).thenReturn(status);

        if ("ISSUED".equals(status)) {
            when(invoice.getId()).thenReturn(id);
            when(invoice.getCustomerId()).thenReturn(77L);
            when(invoice.getInvoiceDate()).thenReturn(FROM);
            when(invoice.getInvoiceNumber())
                    .thenReturn("INV-" + id);
        }

        return invoice;
    }

    private InvoiceLine line(
            String quantity,
            String price,
            String discount,
            String taxPct,
            String tax,
            String lineTotal) {

        InvoiceLine line = new InvoiceLine();

        line.setQuantity(new BigDecimal(quantity));
        line.setUnitPrice(new BigDecimal(price));
        line.setDiscountPct(new BigDecimal(discount));
        line.setTaxPct(new BigDecimal(taxPct));
        line.setTaxAmount(new BigDecimal(tax));
        line.setLineTotal(new BigDecimal(lineTotal));

        return line;
    }

    private void assertMoney(String expected, String actual) {
        assertEquals(
                PdfFormatUtils.formatMoney(new BigDecimal(expected)),
                actual
        );
    }

    @Test
    void findRows_shouldRejectLegacyMethod() {

        assertThrows(
                IllegalStateException.class,
                () -> service.findRows(TENANT, FROM, TO)
        );

        verifyNoInteractions(
                invoiceRepository,
                invoiceLineRepository,
                customerRepository
        );
    }

    @Test
    void findRows_shouldRequireTenantAndCompany() {

        assertThrows(
                IllegalStateException.class,
                () -> service.findRows(
                        null, COMPANY, FROM, TO
                )
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.findRows(
                        TENANT, null, FROM, TO
                )
        );

        verifyNoInteractions(
                invoiceRepository,
                invoiceLineRepository,
                customerRepository
        );
    }

    @Test
    void findRows_shouldUseCompanyAndExcludeNonIssuedInvoices() {

        Invoice draft = invoice(101L, "DRAFT");
        Invoice cancelled = invoice(102L, "CANCELLED");
        Invoice issued = invoice(103L, "ISSUED");

        when(invoiceRepository
                .findByTenantIdAndCompanyIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(
                        TENANT, COMPANY, FROM, TO
                ))
                .thenReturn(List.of(
                        draft, cancelled, issued
                ));

        when(invoiceLineRepository
                .findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                        TENANT, 103L
                ))
                .thenReturn(List.of(
                        line(
                                "1.000", "100.00",
                                "0.00", "22.00",
                                "22.00", "122.00"
                        )
                ));

        List<VatSalesRegisterRowView> rows =
                service.findRows(
                        TENANT, COMPANY, FROM, TO
                );

        assertEquals(1, rows.size());

        assertEquals(
                "INV-103",
                rows.get(0).getInvoiceNumber()
        );

        assertMoney(
                "100.00",
                rows.get(0).getFormattedTaxableAmount()
        );

        assertMoney(
                "22.00",
                rows.get(0).getFormattedTaxAmount()
        );

        assertMoney(
                "122.00",
                rows.get(0).getFormattedTotalAmount()
        );

        verify(invoiceRepository)
                .findByTenantIdAndCompanyIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(
                        TENANT, COMPANY, FROM, TO
                );

        verifyNoMoreInteractions(invoiceRepository);

        verify(invoiceLineRepository, times(1))
                .findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                        TENANT, 103L
                );

        verifyNoMoreInteractions(invoiceLineRepository);
    }

    @Test
    void findRows_shouldCalculateTaxableAndGroupByTaxRate() {

        Invoice first = invoice(201L, "ISSUED");
        Invoice second = invoice(202L, "ISSUED");

        when(invoiceRepository
                .findByTenantIdAndCompanyIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(
                        TENANT, COMPANY, FROM, TO
                ))
                .thenReturn(List.of(first, second));

        // Da preventivo: lineTotal netto.
        when(invoiceLineRepository
                .findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                        TENANT, 201L
                ))
                .thenReturn(List.of(
                        line(
                                "1.000", "100.00",
                                "0.00", "22.00",
                                "22.00", "100.00"
                        )
                ));

        // Da ordine manuale: lineTotal comprensivo di IVA.
        when(invoiceLineRepository
                .findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                        TENANT, 202L
                ))
                .thenReturn(List.of(
                        line(
                                "2.000", "40.00",
                                "0.00", "10.00",
                                "8.00", "88.00"
                        ),
                        line(
                                "3.000", "19.99",
                                "12.50", "22.00",
                                "11.54", "64.01"
                        )
                ));

        List<VatSalesRegisterRowView> rows =
                service.findRows(
                        TENANT, COMPANY, FROM, TO
                );

        assertEquals(3, rows.size());

        // Prima fattura: imponibile netto, aliquota 22%.
        assertEquals(
                "INV-201",
                rows.get(0).getInvoiceNumber()
        );

        assertMoney(
                "100.00",
                rows.get(0).getFormattedTaxableAmount()
        );

        assertMoney(
                "22.00",
                rows.get(0).getFormattedTaxAmount()
        );

        assertMoney(
                "122.00",
                rows.get(0).getFormattedTotalAmount()
        );

        // Seconda fattura: aliquota 10%.
        assertMoney(
                "80.00",
                rows.get(1).getFormattedTaxableAmount()
        );

        assertMoney(
                "8.00",
                rows.get(1).getFormattedTaxAmount()
        );

        assertMoney(
                "88.00",
                rows.get(1).getFormattedTotalAmount()
        );

        // Seconda fattura: aliquota 22%, con sconto.
        assertMoney(
                "52.47",
                rows.get(2).getFormattedTaxableAmount()
        );

        assertMoney(
                "11.54",
                rows.get(2).getFormattedTaxAmount()
        );

        assertMoney(
                "64.01",
                rows.get(2).getFormattedTotalAmount()
        );

        verify(invoiceRepository)
                .findByTenantIdAndCompanyIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(
                        TENANT, COMPANY, FROM, TO
                );

        verifyNoMoreInteractions(invoiceRepository);
    }
}
