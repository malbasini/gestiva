package com.gestiva.accounting.vat.web;

import com.gestiva.billing.invoice.entity.Invoice;
import com.gestiva.billing.invoice.entity.InvoiceLine;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.billing.invoice.repository.InvoiceLineRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import com.gestiva.purchasing.invoice.entity.SupplierInvoiceLine;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceLineRepository;

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
class VatSettlementWebServiceTest {

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
    private SupplierInvoiceRepository supplierInvoiceRepository;

    @Mock
    private SupplierInvoiceLineRepository supplierInvoiceLineRepository;

    @InjectMocks
    private VatSettlementWebService service;

    private InvoiceLine salesLine(
            String quantity,
            String unitPrice,
            String discount,
            String tax,
            String lineTotal) {

        InvoiceLine line = new InvoiceLine();

        line.setQuantity(new BigDecimal(quantity));
        line.setUnitPrice(new BigDecimal(unitPrice));
        line.setDiscountPct(new BigDecimal(discount));
        line.setTaxAmount(new BigDecimal(tax));
        line.setLineTotal(new BigDecimal(lineTotal));

        return line;
    }

    private SupplierInvoiceLine purchaseLine(
            String taxable,
            String tax) {

        SupplierInvoiceLine line = new SupplierInvoiceLine();

        line.setLineSubtotal(new BigDecimal(taxable));
        line.setTaxAmount(new BigDecimal(tax));

        return line;
    }

    private void configureSales(List<Invoice> invoices) {

        when(invoiceRepository
                .findByTenantIdAndCompanyIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(
                        TENANT, COMPANY, FROM, TO
                ))
                .thenReturn(invoices);
    }

    private void configurePurchases(List<SupplierInvoice> invoices) {

        when(supplierInvoiceRepository
                .findByTenantIdAndCompanyIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(
                        TENANT, COMPANY, FROM, TO
                ))
                .thenReturn(invoices);
    }

    private void assertMoney(String expected, String actual) {

        assertEquals(
                PdfFormatUtils.formatMoney(new BigDecimal(expected)),
                actual
        );
    }

    @Test
    void calculateSettlement_shouldHandleNetAndGrossLineTotal() {

        Invoice netInvoice = mock(Invoice.class);
        Invoice grossInvoice = mock(Invoice.class);

        when(netInvoice.getId()).thenReturn(101L);
        when(netInvoice.getStatus()).thenReturn("ISSUED");
        when(grossInvoice.getId()).thenReturn(102L);
        when(grossInvoice.getStatus()).thenReturn("ISSUED");

        configureSales(List.of(netInvoice, grossInvoice));

        // Percorso preventivo: lineTotal è imponibile.
        when(invoiceLineRepository
                .findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                        TENANT, 101L
                ))
                .thenReturn(List.of(
                        salesLine(
                                "1.000", "100.00",
                                "0.00", "22.00", "100.00"
                        )
                ));

        // Percorso ordine manuale: lineTotal comprende IVA.
        when(invoiceLineRepository
                .findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                        TENANT, 102L
                ))
                .thenReturn(List.of(
                        salesLine(
                                "1.000", "100.00",
                                "0.00", "22.00", "122.00"
                        )
                ));

        SupplierInvoice registered = mock(SupplierInvoice.class);
        SupplierInvoice draft = mock(SupplierInvoice.class);
        SupplierInvoice cancelled = mock(SupplierInvoice.class);

        when(registered.getStatus()).thenReturn("REGISTERED");
        when(registered.getId()).thenReturn(201L);

        when(draft.getStatus()).thenReturn("DRAFT");
        when(cancelled.getStatus()).thenReturn("CANCELLED");

        configurePurchases(List.of(
                draft, registered, cancelled
        ));

        when(supplierInvoiceLineRepository
                .findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                        TENANT, 201L
                ))
                .thenReturn(List.of(
                        purchaseLine("80.00", "8.00")
                ));

        VatSettlementView view = service.calculateSettlement(
                TENANT, COMPANY, FROM, TO
        );

        assertMoney(
                "200.00",
                view.getFormattedSalesTaxableAmount()
        );

        assertMoney(
                "44.00",
                view.getFormattedSalesTaxAmount()
        );

        assertMoney(
                "80.00",
                view.getFormattedPurchaseTaxableAmount()
        );

        assertMoney(
                "8.00",
                view.getFormattedPurchaseTaxAmount()
        );

        assertMoney(
                "36.00",
                view.getFormattedVatBalance()
        );

        assertEquals("A DEBITO", view.getBalanceTypeLabel());

        verify(invoiceRepository)
                .findByTenantIdAndCompanyIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(
                        TENANT, COMPANY, FROM, TO
                );

        verify(supplierInvoiceRepository)
                .findByTenantIdAndCompanyIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(
                        TENANT, COMPANY, FROM, TO
                );

        verifyNoMoreInteractions(
                invoiceRepository,
                supplierInvoiceRepository
        );

        verify(supplierInvoiceLineRepository, times(1))
                .findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                        TENANT, 201L
                );

        verifyNoMoreInteractions(supplierInvoiceLineRepository);
    }

    @Test
    void calculateSettlement_shouldApplyDiscountAndRounding() {

        Invoice invoice = mock(Invoice.class);
        when(invoice.getId()).thenReturn(301L);
        when(invoice.getStatus()).thenReturn("ISSUED");

        configureSales(List.of(invoice));
        configurePurchases(List.of());

        when(invoiceLineRepository
                .findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                        TENANT, 301L
                ))
                .thenReturn(List.of(
                        salesLine(
                                "3.000", "19.99",
                                "12.50", "11.54", "64.01"
                        )
                ));

        VatSettlementView view = service.calculateSettlement(
                TENANT, COMPANY, FROM, TO
        );

        // Lordo: 59,97
        // Sconto 12,5%: 7,50
        // Imponibile: 52,47
        assertMoney(
                "52.47",
                view.getFormattedSalesTaxableAmount()
        );

        assertMoney(
                "11.54",
                view.getFormattedSalesTaxAmount()
        );

        assertMoney(
                "11.54",
                view.getFormattedVatBalance()
        );

        assertEquals("A DEBITO", view.getBalanceTypeLabel());
    }

    @Test
    void calculateSettlement_shouldExcludeUnregisteredPurchases() {

        configureSales(List.of());

        SupplierInvoice draft = mock(SupplierInvoice.class);
        SupplierInvoice cancelled = mock(SupplierInvoice.class);

        when(draft.getStatus()).thenReturn("DRAFT");
        when(cancelled.getStatus()).thenReturn("CANCELLED");

        configurePurchases(List.of(draft, cancelled));

        VatSettlementView view = service.calculateSettlement(
                TENANT, COMPANY, FROM, TO
        );

        assertMoney(
                "0.00",
                view.getFormattedPurchaseTaxableAmount()
        );

        assertMoney(
                "0.00",
                view.getFormattedPurchaseTaxAmount()
        );

        assertMoney(
                "0.00",
                view.getFormattedVatBalance()
        );

        assertEquals("ZERO", view.getBalanceTypeLabel());

        verifyNoInteractions(supplierInvoiceLineRepository);
    }

    @Test
    void calculateSettlement_shouldExcludeDraftAndCancelledSalesInvoices() {

        Invoice draft = mock(Invoice.class);
        Invoice cancelled = mock(Invoice.class);
        Invoice issued = mock(Invoice.class);

        when(draft.getStatus()).thenReturn("DRAFT");
        when(cancelled.getStatus()).thenReturn("CANCELLED");
        when(issued.getStatus()).thenReturn("ISSUED");

        when(issued.getId()).thenReturn(403L);

        configureSales(List.of(draft, cancelled, issued));
        configurePurchases(List.of());

        when(invoiceLineRepository
                .findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                        TENANT, 403L
                ))
                .thenReturn(List.of(
                        salesLine(
                                "1.000",
                                "100.00",
                                "0.00",
                                "22.00",
                                "122.00"
                        )
                ));

        VatSettlementView view = service.calculateSettlement(
                TENANT, COMPANY, FROM, TO
        );

        // Solo la fattura ISSUED contribuisce alla liquidazione.
        assertMoney(
                "100.00",
                view.getFormattedSalesTaxableAmount()
        );

        assertMoney(
                "22.00",
                view.getFormattedSalesTaxAmount()
        );

        assertMoney(
                "0.00",
                view.getFormattedPurchaseTaxAmount()
        );

        assertMoney(
                "22.00",
                view.getFormattedVatBalance()
        );

        assertEquals(
                "A DEBITO",
                view.getBalanceTypeLabel()
        );

        // Le righe DRAFT e CANCELLED non devono essere caricate.
        verify(invoiceLineRepository, times(1))
                .findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                        TENANT, 403L
                );

        verifyNoMoreInteractions(invoiceLineRepository);

        verifyNoInteractions(supplierInvoiceLineRepository);

        verify(invoiceRepository)
                .findByTenantIdAndCompanyIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(
                        TENANT, COMPANY, FROM, TO
                );

        verify(supplierInvoiceRepository)
                .findByTenantIdAndCompanyIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(
                        TENANT, COMPANY, FROM, TO
                );
    }

}
