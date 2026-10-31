package com.gestiva.accounting.vat.web;

import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import com.gestiva.purchasing.invoice.entity.SupplierInvoiceLine;
import com.gestiva.documents.pdf.PdfFormatUtils;
import java.math.BigDecimal;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceLineRepository;
import com.gestiva.purchasing.supplier.repository.SupplierRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VatPurchaseRegisterWebServiceTest {

    private static final Long TENANT_ID = 2L;
    private static final Long COMPANY_ID = 5L;
    private static final Long INVOICE_ID = 100L;
    private static final Long SUPPLIER_ID = 20L;

    private static final LocalDate FROM =
            LocalDate.of(2026, 10, 1);

    private static final LocalDate TO =
            LocalDate.of(2026, 10, 31);

    @Mock
    private SupplierInvoiceRepository supplierInvoiceRepository;

    @Mock
    private SupplierInvoiceLineRepository supplierInvoiceLineRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private VatPurchaseRegisterWebService service;

    private SupplierInvoice createInvoice(String status) {

        SupplierInvoice invoice = new SupplierInvoice();

        ReflectionTestUtils.setField(
                invoice, "id", INVOICE_ID
        );

        invoice.setTenantId(TENANT_ID);
        invoice.setCompanyId(COMPANY_ID);
        invoice.setSupplierId(SUPPLIER_ID);
        invoice.setStatus(status);
        invoice.setInvoiceDate(
                LocalDate.of(2026, 10, 15)
        );
        invoice.setInvoiceNumber("SF-TEST-001");

        return invoice;
    }

    @Test
    void findRows_shouldRejectLegacyMethod() {

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.findRows(TENANT_ID, FROM, TO)
        );

        assertTrue(
                exception.getMessage().contains(
                        "companyId obbligatorio"
                )
        );

        verifyNoInteractions(
                supplierInvoiceRepository,
                supplierInvoiceLineRepository,
                supplierRepository
        );
    }

    @Test
    void findRows_shouldRejectMissingCompany() {

        assertThrows(
                IllegalStateException.class,
                () -> service.findRows(
                        TENANT_ID,
                        null,
                        FROM,
                        TO
                )
        );

        verifyNoInteractions(
                supplierInvoiceRepository,
                supplierInvoiceLineRepository,
                supplierRepository
        );
    }

    @Test
    void findRows_shouldUseCompanyScopedQuery() {

        when(supplierInvoiceRepository
                .findByTenantIdAndCompanyIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(
                        TENANT_ID,
                        COMPANY_ID,
                        FROM,
                        TO
                ))
                .thenReturn(List.of());

        var rows = service.findRows(
                TENANT_ID,
                COMPANY_ID,
                FROM,
                TO
        );

        assertTrue(rows.isEmpty());

        verify(supplierInvoiceRepository)
                .findByTenantIdAndCompanyIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(
                        TENANT_ID,
                        COMPANY_ID,
                        FROM,
                        TO
                );

        verifyNoMoreInteractions(supplierInvoiceRepository);

        verifyNoInteractions(
                supplierInvoiceLineRepository,
                supplierRepository
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"DRAFT", "CANCELLED"})
    void findRows_shouldExcludeNonRegisteredInvoices(
            String status) {

        SupplierInvoice invoice = createInvoice(status);

        when(supplierInvoiceRepository
                .findByTenantIdAndCompanyIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(
                        TENANT_ID,
                        COMPANY_ID,
                        FROM,
                        TO
                ))
                .thenReturn(List.of(invoice));

        var rows = service.findRows(
                TENANT_ID,
                COMPANY_ID,
                FROM,
                TO
        );

        assertTrue(rows.isEmpty());

        verifyNoInteractions(
                supplierInvoiceLineRepository,
                supplierRepository
        );
    }

    @Test
    void findRows_shouldProcessRegisteredInvoice() {

        SupplierInvoice invoice = createInvoice("REGISTERED");

        when(supplierInvoiceRepository
                .findByTenantIdAndCompanyIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(
                        TENANT_ID,
                        COMPANY_ID,
                        FROM,
                        TO
                ))
                .thenReturn(List.of(invoice));

        when(supplierInvoiceLineRepository
                .findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                        TENANT_ID,
                        INVOICE_ID
                ))
                .thenReturn(List.of());

        var rows = service.findRows(
                TENANT_ID,
                COMPANY_ID,
                FROM,
                TO
        );

        // Senza righe IVA non vengono prodotti bucket.
        assertTrue(rows.isEmpty());

        verify(supplierInvoiceLineRepository)
                .findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                        TENANT_ID,
                        INVOICE_ID
                );
    }

    private SupplierInvoiceLine createVatLine(
            String taxPct,
            String taxable,
            String tax) {

        SupplierInvoiceLine line = new SupplierInvoiceLine();

        line.setSupplierInvoiceId(INVOICE_ID);
        line.setTaxPct(new BigDecimal(taxPct));
        line.setLineSubtotal(new BigDecimal(taxable));
        line.setTaxAmount(new BigDecimal(tax));

        return line;
    }

    @Test
    void findRows_shouldAggregateAmountsByTaxRate() {

        SupplierInvoice invoice = createInvoice("REGISTERED");

        when(supplierInvoiceRepository
                .findByTenantIdAndCompanyIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(
                        TENANT_ID, COMPANY_ID, FROM, TO
                ))
                .thenReturn(List.of(invoice));

        SupplierInvoiceLine line1 =
                createVatLine("22.00", "100.00", "22.00");

        SupplierInvoiceLine line2 =
                createVatLine("22.00", "50.00", "11.00");

        SupplierInvoiceLine line3 =
                createVatLine("10.00", "80.00", "8.00");

        when(supplierInvoiceLineRepository
                .findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                        TENANT_ID, INVOICE_ID
                ))
                .thenReturn(List.of(line1, line2, line3));

        var rows = service.findRows(
                TENANT_ID, COMPANY_ID, FROM, TO
        );

        assertEquals(2, rows.size());

        // TreeMap: aliquota 10% prima del 22%.
        var row10 = rows.get(0);
        var row22 = rows.get(1);

        assertEquals(
                PdfFormatUtils.formatDecimal(
                        new BigDecimal("10.00"), 2
                ) + "%",
                row10.getFormattedTaxPct()
        );

        assertEquals(
                PdfFormatUtils.formatMoney(
                        new BigDecimal("80.00")
                ),
                row10.getFormattedTaxableAmount()
        );

        assertEquals(
                PdfFormatUtils.formatMoney(
                        new BigDecimal("8.00")
                ),
                row10.getFormattedTaxAmount()
        );

        assertEquals(
                PdfFormatUtils.formatMoney(
                        new BigDecimal("88.00")
                ),
                row10.getFormattedTotalAmount()
        );

        assertEquals(
                PdfFormatUtils.formatDecimal(
                        new BigDecimal("22.00"), 2
                ) + "%",
                row22.getFormattedTaxPct()
        );

        assertEquals(
                PdfFormatUtils.formatMoney(
                        new BigDecimal("150.00")
                ),
                row22.getFormattedTaxableAmount()
        );

        assertEquals(
                PdfFormatUtils.formatMoney(
                        new BigDecimal("33.00")
                ),
                row22.getFormattedTaxAmount()
        );

        assertEquals(
                PdfFormatUtils.formatMoney(
                        new BigDecimal("183.00")
                ),
                row22.getFormattedTotalAmount()
        );

        for (var row : rows) {
            assertEquals(
                    INVOICE_ID,
                    row.getSupplierInvoiceId()
            );

            assertEquals(
                    "SF-TEST-001",
                    row.getInvoiceNumber()
            );
        }

        verify(supplierInvoiceLineRepository)
                .findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                        TENANT_ID, INVOICE_ID
                );
    }

    @Test
    void findRows_shouldIncludeOnlyRegisteredAmounts() {

        SupplierInvoice draft = createInvoice("DRAFT");
        SupplierInvoice registered = createInvoice("REGISTERED");
        SupplierInvoice cancelled = createInvoice("CANCELLED");

        ReflectionTestUtils.setField(
                draft, "id", 101L
        );

        ReflectionTestUtils.setField(
                cancelled, "id", 102L
        );

        when(supplierInvoiceRepository
                .findByTenantIdAndCompanyIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(
                        TENANT_ID, COMPANY_ID, FROM, TO
                ))
                .thenReturn(List.of(
                        draft,
                        registered,
                        cancelled
                ));

        SupplierInvoiceLine registeredLine =
                createVatLine("22.00", "100.00", "22.00");

        when(supplierInvoiceLineRepository
                .findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                        TENANT_ID, INVOICE_ID
                ))
                .thenReturn(List.of(registeredLine));

        var rows = service.findRows(
                TENANT_ID, COMPANY_ID, FROM, TO
        );

        assertEquals(1, rows.size());

        var row = rows.get(0);

        assertEquals(
                INVOICE_ID,
                row.getSupplierInvoiceId()
        );

        assertEquals(
                PdfFormatUtils.formatMoney(
                        new BigDecimal("100.00")
                ),
                row.getFormattedTaxableAmount()
        );

        assertEquals(
                PdfFormatUtils.formatMoney(
                        new BigDecimal("22.00")
                ),
                row.getFormattedTaxAmount()
        );

        assertEquals(
                PdfFormatUtils.formatMoney(
                        new BigDecimal("122.00")
                ),
                row.getFormattedTotalAmount()
        );

        verify(supplierInvoiceLineRepository, never())
                .findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                        TENANT_ID, 101L
                );

        verify(supplierInvoiceLineRepository, never())
                .findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                        TENANT_ID, 102L
                );

        verify(supplierInvoiceLineRepository)
                .findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                        TENANT_ID, INVOICE_ID
                );
    }

}
