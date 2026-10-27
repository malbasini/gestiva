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

import com.gestiva.accounting.v2.account.entity.Account;
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


    @Test
    void postSupplierInvoice_shouldRejectUnbalancedAmounts() {

        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        var invoice = createInvoiceFixture();

        invoice.setTotalAmount(new BigDecimal("123.00"));

        when(supplierInvoiceRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID,
                COMPANY_ID,
                SUPPLIER_INVOICE_ID
        )).thenReturn(Optional.of(invoice));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.postSupplierInvoice(
                        TENANT_ID,
                        LocalDate.of(2026, 10, 27),
                        "SF-TEST-001",
                        new BigDecimal("100.00"),
                        new BigDecimal("22.00"),
                        new BigDecimal("123.00"),
                        "EUR",
                        SUPPLIER_INVOICE_ID,
                        "Test quadratura contabile"
                )
        );

        org.junit.jupiter.api.Assertions.assertTrue(
                exception.getMessage().contains(
                        "non è quadrata"
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


    @Test
    void postSupplierInvoice_shouldAcceptEquivalentDecimalScale() {

        configureExistingInvoice();

        com.gestiva.accounting.v2.account.entity.Account purchase =
                new com.gestiva.accounting.v2.account.entity.Account();
        purchase.setId(5100L);
        purchase.setTenantId(TENANT_ID);
        purchase.setCompanyId(COMPANY_ID);
        purchase.setCode("5100");
        purchase.setActive(true);
        purchase.setLeafAccount(true);

        com.gestiva.accounting.v2.account.entity.Account vat =
                new com.gestiva.accounting.v2.account.entity.Account();
        vat.setId(2220L);
        vat.setTenantId(TENANT_ID);
        vat.setCompanyId(COMPANY_ID);
        vat.setCode("2220");
        vat.setActive(true);
        vat.setLeafAccount(true);

        com.gestiva.accounting.v2.account.entity.Account payable =
                new com.gestiva.accounting.v2.account.entity.Account();
        payable.setId(2110L);
        payable.setTenantId(TENANT_ID);
        payable.setCompanyId(COMPANY_ID);
        payable.setCode("2110");
        payable.setActive(true);
        payable.setLeafAccount(true);

        when(accountRepository.findByTenantIdAndCompanyIdAndCode(
                TENANT_ID, COMPANY_ID, "5100"
        )).thenReturn(Optional.of(purchase));

        when(accountRepository.findByTenantIdAndCompanyIdAndCode(
                TENANT_ID, COMPANY_ID, "2220"
        )).thenReturn(Optional.of(vat));

        when(accountRepository.findByTenantIdAndCompanyIdAndCode(
                TENANT_ID, COMPANY_ID, "2110"
        )).thenReturn(Optional.of(payable));

        when(documentSequenceService.nextJournalEntryNumber(
                TENANT_ID, COMPANY_ID
        )).thenReturn("JE-TEST-001");

        when(journalEntryRepository.save(
                any(com.gestiva.accounting.v2.journal.entity.JournalEntry.class)
        )).thenAnswer(invocation -> {
            com.gestiva.accounting.v2.journal.entity.JournalEntry entry =
                    invocation.getArgument(0);
            entry.setId(900L);
            return entry;
        });

        Long result = service.postSupplierInvoice(
                TENANT_ID,
                LocalDate.of(2026, 10, 27),
                "SF-TEST-001",
                new BigDecimal("100.0"),
                new BigDecimal("22.0"),
                new BigDecimal("122.0"),
                "EUR",
                SUPPLIER_INVOICE_ID,
                "Test scale decimali differenti"
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                900L, result
        );

        org.mockito.ArgumentCaptor<
                com.gestiva.accounting.v2.journal.entity.JournalEntry
        > entryCaptor = org.mockito.ArgumentCaptor.forClass(
                com.gestiva.accounting.v2.journal.entity.JournalEntry.class
        );

        verify(journalEntryRepository).save(entryCaptor.capture());

        var entry = entryCaptor.getValue();

        org.junit.jupiter.api.Assertions.assertEquals(
                TENANT_ID, entry.getTenantId()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                COMPANY_ID, entry.getCompanyId()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "SUPPLIER_INVOICE", entry.getReferenceType()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                SUPPLIER_INVOICE_ID, entry.getReferenceId()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "JE-TEST-001", entry.getEntryNumber()
        );

        org.junit.jupiter.api.Assertions.assertTrue(
                entry.isPosted()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                new BigDecimal("122.00"), entry.getTotalDebit()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                new BigDecimal("122.00"), entry.getTotalCredit()
        );

        org.mockito.ArgumentCaptor<
                com.gestiva.accounting.v2.journal.entity.JournalEntryLine
        > lineCaptor = org.mockito.ArgumentCaptor.forClass(
                com.gestiva.accounting.v2.journal.entity.JournalEntryLine.class
        );

        verify(journalEntryLineRepository, times(3))
                .save(lineCaptor.capture());

        var lines = lineCaptor.getAllValues();

        org.junit.jupiter.api.Assertions.assertEquals(
                3, lines.size()
        );

        Long[] accountIds = {5100L, 2220L, 2110L};

        BigDecimal[] debits = {
                new BigDecimal("100.00"),
                new BigDecimal("22.00"),
                new BigDecimal("0.00")
        };

        BigDecimal[] credits = {
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("122.00")
        };

        for (int i = 0; i < 3; i++) {

            var line = lines.get(i);

            org.junit.jupiter.api.Assertions.assertEquals(
                    TENANT_ID, line.getTenantId()
            );

            org.junit.jupiter.api.Assertions.assertEquals(
                    COMPANY_ID, line.getCompanyId()
            );

            org.junit.jupiter.api.Assertions.assertEquals(
                    900L, line.getJournalEntryId()
            );

            org.junit.jupiter.api.Assertions.assertEquals(
                    i + 1, line.getLineNo()
            );

            org.junit.jupiter.api.Assertions.assertEquals(
                    accountIds[i], line.getAccountId()
            );

            org.junit.jupiter.api.Assertions.assertEquals(
                    debits[i], line.getDebitAmount()
            );

            org.junit.jupiter.api.Assertions.assertEquals(
                    credits[i], line.getCreditAmount()
            );
        }

        verify(journalEntryRepository)
                .existsByTenantIdAndCompanyIdAndReferenceTypeAndReferenceId(
                        TENANT_ID,
                        COMPANY_ID,
                        "SUPPLIER_INVOICE",
                        SUPPLIER_INVOICE_ID
                );

        verify(accountRepository)
                .findByTenantIdAndCompanyIdAndCode(
                        TENANT_ID, COMPANY_ID, "5100"
                );

        verify(accountRepository)
                .findByTenantIdAndCompanyIdAndCode(
                        TENANT_ID, COMPANY_ID, "2220"
                );

        verify(accountRepository)
                .findByTenantIdAndCompanyIdAndCode(
                        TENANT_ID, COMPANY_ID, "2110"
                );

        verify(documentSequenceService)
                .nextJournalEntryNumber(TENANT_ID, COMPANY_ID);

        verifyNoMoreInteractions(
                journalEntryRepository,
                journalEntryLineRepository,
                accountRepository,
                documentSequenceService
        );
    }


    @Test
    void postSupplierInvoice_shouldRejectAlreadyPostedInvoice() {

        configureExistingInvoice();

        when(journalEntryRepository
                .existsByTenantIdAndCompanyIdAndReferenceTypeAndReferenceId(
                        TENANT_ID,
                        COMPANY_ID,
                        "SUPPLIER_INVOICE",
                        SUPPLIER_INVOICE_ID
                )).thenReturn(true);

        BusinessException exception = assertThrows(
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
                        "Test doppia contabilizzazione"
                )
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "La fattura fornitore è già stata contabilizzata.",
                exception.getMessage()
        );

        verify(supplierInvoiceRepository)
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID,
                        COMPANY_ID,
                        SUPPLIER_INVOICE_ID
                );

        verify(journalEntryRepository)
                .existsByTenantIdAndCompanyIdAndReferenceTypeAndReferenceId(
                        TENANT_ID,
                        COMPANY_ID,
                        "SUPPLIER_INVOICE",
                        SUPPLIER_INVOICE_ID
                );

        verifyNoMoreInteractions(
                journalEntryRepository,
                supplierInvoiceRepository
        );

        verifyNoInteractions(
                journalEntryLineRepository,
                accountRepository,
                documentSequenceService
        );
    }


    @Test
    void postSupplierInvoice_shouldRejectMissingAccount() {

        configureExistingInvoice();

        when(accountRepository.findByTenantIdAndCompanyIdAndCode(
                TENANT_ID,
                COMPANY_ID,
                "5100"
        )).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
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
                        "Test conto contabile inesistente"
                )
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "Conto contabile non trovato nella società attiva: 5100",
                exception.getMessage()
        );

        verify(supplierInvoiceRepository)
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID,
                        COMPANY_ID,
                        SUPPLIER_INVOICE_ID
                );

        verify(journalEntryRepository)
                .existsByTenantIdAndCompanyIdAndReferenceTypeAndReferenceId(
                        TENANT_ID,
                        COMPANY_ID,
                        "SUPPLIER_INVOICE",
                        SUPPLIER_INVOICE_ID
                );

        verify(accountRepository)
                .findByTenantIdAndCompanyIdAndCode(
                        TENANT_ID,
                        COMPANY_ID,
                        "5100"
                );

        verifyNoMoreInteractions(
                supplierInvoiceRepository,
                journalEntryRepository,
                accountRepository
        );

        verifyNoInteractions(
                journalEntryLineRepository,
                documentSequenceService
        );
    }


    @Test
    void postSupplierInvoice_shouldRejectInactiveAccount() {

        configureExistingInvoice();

        Account inactiveAccount = new Account();
        inactiveAccount.setId(5100L);
        inactiveAccount.setTenantId(TENANT_ID);
        inactiveAccount.setCompanyId(COMPANY_ID);
        inactiveAccount.setCode("5100");
        inactiveAccount.setActive(false);
        inactiveAccount.setLeafAccount(true);

        when(accountRepository.findByTenantIdAndCompanyIdAndCode(
                TENANT_ID,
                COMPANY_ID,
                "5100"
        )).thenReturn(Optional.of(inactiveAccount));

        BusinessException exception = assertThrows(
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
                        "Test conto contabile disattivato"
                )
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "Il conto contabile 5100 è disattivato.",
                exception.getMessage()
        );

        verify(supplierInvoiceRepository)
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID,
                        COMPANY_ID,
                        SUPPLIER_INVOICE_ID
                );

        verify(journalEntryRepository)
                .existsByTenantIdAndCompanyIdAndReferenceTypeAndReferenceId(
                        TENANT_ID,
                        COMPANY_ID,
                        "SUPPLIER_INVOICE",
                        SUPPLIER_INVOICE_ID
                );

        verify(accountRepository)
                .findByTenantIdAndCompanyIdAndCode(
                        TENANT_ID,
                        COMPANY_ID,
                        "5100"
                );

        verifyNoMoreInteractions(
                supplierInvoiceRepository,
                journalEntryRepository,
                accountRepository
        );

        verifyNoInteractions(
                journalEntryLineRepository,
                documentSequenceService
        );
    }

}
