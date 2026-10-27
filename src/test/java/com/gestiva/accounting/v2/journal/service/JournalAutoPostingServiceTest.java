package com.gestiva.accounting.v2.journal.service;

import com.gestiva.accounting.v2.account.repository.AccountRepository;
import com.gestiva.accounting.v2.journal.repository.JournalEntryLineRepository;
import com.gestiva.accounting.v2.journal.repository.JournalEntryRepository;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.settings.sequence.service.DocumentSequenceService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JournalAutoPostingServiceTest {

    private static final Long TENANT_ID = 2L;
    private static final Long COMPANY_ID = 5L;
    private static final Long SUPPLIER_INVOICE_ID = 100L;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private JournalEntryLineRepository journalEntryLineRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private DocumentSequenceService documentSequenceService;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private SupplierInvoiceRepository supplierInvoiceRepository;

    @Mock
    private CompanyContext companyContext;

    @InjectMocks
    private JournalAutoPostingService service;

    @Test
    void postSupplierInvoice_shouldRejectInvoiceOutsideCurrentCompany() {

        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(supplierInvoiceRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID,
                COMPANY_ID,
                SUPPLIER_INVOICE_ID
        )).thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> service.postSupplierInvoice(
                        TENANT_ID,
                        LocalDate.of(2026, 10, 27),
                        "SF-TEST-001",
                        new BigDecimal("100.00"),
                        new BigDecimal("22.00"),
                        new BigDecimal("122.00"),
                        "EUR",
                        SUPPLIER_INVOICE_ID,
                        "Test isolamento multi-company"
                )
        );

        verify(supplierInvoiceRepository)
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID,
                        COMPANY_ID,
                        SUPPLIER_INVOICE_ID
                );

        verifyNoInteractions(
                journalEntryRepository,
                journalEntryLineRepository,
                accountRepository,
                documentSequenceService
        );
    }

    private SupplierInvoice createInvoiceFixture() {
        SupplierInvoice invoice = new SupplierInvoice();
        invoice.setInvoiceNumber("SF-TEST-001");
        invoice.setInvoiceDate(LocalDate.of(2026, 10, 27));
        invoice.setCurrencyCode("EUR");
        invoice.setSubtotalAmount(new BigDecimal("100.00"));
        invoice.setTaxAmount(new BigDecimal("22.00"));
        invoice.setTotalAmount(new BigDecimal("122.00"));
        return invoice;
    }

    private void configureExistingInvoice() {
        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(supplierInvoiceRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID,
                COMPANY_ID,
                SUPPLIER_INVOICE_ID
        )).thenReturn(Optional.of(createInvoiceFixture()));
    }

    private void assertRejectedWithoutAccountingWrites(
            LocalDate date,
            String number,
            BigDecimal subtotal,
            BigDecimal tax,
            BigDecimal total,
            String currency) {

        assertThrows(
                BusinessException.class,
                () -> service.postSupplierInvoice(
                        TENANT_ID,
                        date,
                        number,
                        subtotal,
                        tax,
                        total,
                        currency,
                        SUPPLIER_INVOICE_ID,
                        "Test coerenza contabile"
                )
        );

        verifyNoInteractions(
                journalEntryRepository,
                journalEntryLineRepository,
                accountRepository,
                documentSequenceService
        );
    }

    @Test
    void postSupplierInvoice_shouldRejectDifferentDocumentNumber() {
        configureExistingInvoice();

        assertRejectedWithoutAccountingWrites(
                LocalDate.of(2026, 10, 27),
                "SF-ERRATA",
                new BigDecimal("100.00"),
                new BigDecimal("22.00"),
                new BigDecimal("122.00"),
                "EUR"
        );
    }

    @Test
    void postSupplierInvoice_shouldRejectDifferentDocumentDate() {
        configureExistingInvoice();

        assertRejectedWithoutAccountingWrites(
                LocalDate.of(2026, 10, 26),
                "SF-TEST-001",
                new BigDecimal("100.00"),
                new BigDecimal("22.00"),
                new BigDecimal("122.00"),
                "EUR"
        );
    }

    @Test
    void postSupplierInvoice_shouldRejectDifferentCurrency() {
        configureExistingInvoice();

        assertRejectedWithoutAccountingWrites(
                LocalDate.of(2026, 10, 27),
                "SF-TEST-001",
                new BigDecimal("100.00"),
                new BigDecimal("22.00"),
                new BigDecimal("122.00"),
                "USD"
        );
    }

    @Test
    void postSupplierInvoice_shouldRejectDifferentSubtotal() {
        configureExistingInvoice();

        assertRejectedWithoutAccountingWrites(
                LocalDate.of(2026, 10, 27),
                "SF-TEST-001",
                new BigDecimal("100.01"),
                new BigDecimal("22.00"),
                new BigDecimal("122.00"),
                "EUR"
        );
    }

    @Test
    void postSupplierInvoice_shouldRejectDifferentTax() {
        configureExistingInvoice();

        assertRejectedWithoutAccountingWrites(
                LocalDate.of(2026, 10, 27),
                "SF-TEST-001",
                new BigDecimal("100.00"),
                new BigDecimal("22.01"),
                new BigDecimal("122.00"),
                "EUR"
        );
    }

    @Test
    void postSupplierInvoice_shouldRejectDifferentTotal() {
        configureExistingInvoice();

        assertRejectedWithoutAccountingWrites(
                LocalDate.of(2026, 10, 27),
                "SF-TEST-001",
                new BigDecimal("100.00"),
                new BigDecimal("22.00"),
                new BigDecimal("122.01"),
                "EUR"
        );
    }


    @Test
    void postSupplierInvoice_shouldRejectNullInvoiceId() {

        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        assertThrows(
                BusinessException.class,
                () -> service.postSupplierInvoice(
                        TENANT_ID,
                        LocalDate.of(2026, 10, 27),
                        "SF-TEST-001",
                        new BigDecimal("100.00"),
                        new BigDecimal("22.00"),
                        new BigDecimal("122.00"),
                        "EUR",
                        null,
                        "Test identificativo nullo"
                )
        );

        verifyNoInteractions(
                supplierInvoiceRepository,
                journalEntryRepository,
                journalEntryLineRepository,
                accountRepository,
                documentSequenceService
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5})
    void postSupplierInvoice_shouldRejectNullAmounts(int scenario) {

        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        var invoice = createInvoiceFixture();

        BigDecimal subtotal = new BigDecimal("100.00");
        BigDecimal tax = new BigDecimal("22.00");
        BigDecimal total = new BigDecimal("122.00");

        switch (scenario) {
            case 0 -> subtotal = null;
            case 1 -> tax = null;
            case 2 -> total = null;
            case 3 -> invoice.setSubtotalAmount(null);
            case 4 -> invoice.setTaxAmount(null);
            case 5 -> invoice.setTotalAmount(null);
            default -> throw new IllegalArgumentException(
                    "Scenario non previsto"
            );
        }

        when(supplierInvoiceRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID,
                COMPANY_ID,
                SUPPLIER_INVOICE_ID
        )).thenReturn(Optional.of(invoice));

        assertRejectedWithoutAccountingWrites(
                LocalDate.of(2026, 10, 27),
                "SF-TEST-001",
                subtotal,
                tax,
                total,
                "EUR"
        );
    }


    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void postSupplierInvoice_shouldRejectInvalidSignedAmounts(
            int scenario) {

        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        var invoice = createInvoiceFixture();

        BigDecimal subtotal = new BigDecimal("100.00");
        BigDecimal tax = new BigDecimal("22.00");
        BigDecimal total = new BigDecimal("122.00");

        switch (scenario) {
            case 0 -> {
                subtotal = new BigDecimal("-100.00");
                invoice.setSubtotalAmount(subtotal);
            }
            case 1 -> {
                tax = new BigDecimal("-22.00");
                invoice.setTaxAmount(tax);
            }
            case 2 -> {
                total = new BigDecimal("-122.00");
                invoice.setTotalAmount(total);
            }
            case 3 -> {
                total = BigDecimal.ZERO;
                invoice.setTotalAmount(total);
            }
            default -> throw new IllegalArgumentException(
                    "Scenario non previsto"
            );
        }

        when(supplierInvoiceRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID,
                COMPANY_ID,
                SUPPLIER_INVOICE_ID
        )).thenReturn(Optional.of(invoice));

        final BigDecimal checkedSubtotal = subtotal;
        final BigDecimal checkedTax = tax;
        final BigDecimal checkedTotal = total;

        assertThrows(
                BusinessException.class,
                () -> service.postSupplierInvoice(
                        TENANT_ID,
                        LocalDate.of(2026, 10, 27),
                        "SF-TEST-001",
                        checkedSubtotal,
                        checkedTax,
                        checkedTotal,
                        "EUR",
                        SUPPLIER_INVOICE_ID,
                        "Test importi non validi"
                )
        );

        verify(journalEntryRepository)
                .existsByTenantIdAndCompanyIdAndReferenceTypeAndReferenceId(
                        TENANT_ID,
                        COMPANY_ID,
                        "SUPPLIER_INVOICE",
                        SUPPLIER_INVOICE_ID
                );

        verifyNoMoreInteractions(journalEntryRepository);

        verifyNoInteractions(
                journalEntryLineRepository,
                accountRepository,
                documentSequenceService
        );
    }

}
