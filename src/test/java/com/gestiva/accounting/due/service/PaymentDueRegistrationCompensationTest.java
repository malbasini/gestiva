package com.gestiva.accounting.due.service;

import com.gestiva.accounting.due.entity.PaymentDue;
import com.gestiva.accounting.due.entity.PaymentDueTransaction;
import com.gestiva.accounting.due.repository.PaymentDueRepository;
import com.gestiva.accounting.due.repository.PaymentDueTransactionRepository;
import com.gestiva.accounting.due.web.PaymentDueRegistrationForm;
import com.gestiva.accounting.v2.journal.service.JournalAutoPostingService;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentDueRegistrationCompensationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 3L;
    private static final Long DUE_ID = 9L;
    private static final LocalDate DATE =
            LocalDate.of(2026, 11, 14);

    @Mock
    private PaymentDueRepository dueRepository;

    @Mock
    private PaymentDueTransactionRepository txRepository;

    @Mock
    private JournalAutoPostingService journalService;

    @Mock
    private CompanyContext companyContext;

    private PaymentDueRegistrationService service;

    @BeforeEach
    void setup() {
        service = new PaymentDueRegistrationService(
                dueRepository,
                txRepository,
                journalService,
                companyContext
        );
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    private PaymentDue due(
            String direction,
            String gross,
            String paid,
            String compensated,
            String open) {

        PaymentDue due = mock(PaymentDue.class);

        lenient().when(due.getId()).thenReturn(DUE_ID);
        lenient().when(due.getCompanyId()).thenReturn(COMPANY);
        lenient().when(due.getStatus()).thenReturn("PARTIALLY_PAID");
        lenient().when(due.getDirection()).thenReturn(direction);
        lenient().when(due.getDocumentNumber())
                .thenReturn("INV-00001");
        lenient().when(due.getCurrencyCode()).thenReturn("EUR");

        lenient().when(due.getGrossAmount())
                .thenReturn(money(gross));
        lenient().when(due.getPaidAmount())
                .thenReturn(money(paid));
        lenient().when(due.getCompensatedAmount())
                .thenReturn(money(compensated));
        lenient().when(due.getOpenAmount())
                .thenReturn(money(open));

        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(COMPANY);

        when(dueRepository.findByTenantIdAndCompanyIdAndIdForUpdate(
                TENANT, COMPANY, DUE_ID))
                .thenReturn(Optional.of(due));

        return due;
    }

    private static PaymentDueRegistrationForm form(String amount) {
        PaymentDueRegistrationForm form =
                new PaymentDueRegistrationForm();

        form.setTransactionDate(DATE);
        form.setAmount(money(amount));
        form.setNotes("Collaudo compensazioni V10/V11");

        return form;
    }

    private void verifyNoFinancialWrites() {
        verify(dueRepository, never()).save(any());
        verifyNoInteractions(txRepository, journalService);
    }

    @Test
    void customerReceiptAboveCompensatedResidualIsRejected() {
        due("RECEIVABLE",
                "186.66", "0.00", "20.00", "166.66");

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.registerMovement(
                        TENANT, DUE_ID, form("186.66"))
        );

        assertTrue(ex.getMessage().contains("residuo"));

        verify(dueRepository)
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        TENANT, COMPANY, DUE_ID);

        verifyNoFinancialWrites();
    }

    @Test
    void supplierPaymentAboveCompensatedResidualIsRejected() {
        due("PAYABLE",
                "100.00", "10.00", "30.00", "60.00");

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.registerMovement(
                        TENANT, DUE_ID, form("90.00"))
        );

        assertTrue(ex.getMessage().contains("residuo"));
        verifyNoFinancialWrites();
    }

    @Test
    void customerPartialReceiptPreservesCompensation() {
        PaymentDue due = due(
                "RECEIVABLE",
                "186.66", "0.00", "20.00", "166.66");

        service.registerMovement(
                TENANT, DUE_ID, form("100.00"));

        verify(due).setPaidAmount(money("100.00"));
        verify(due).setOpenAmount(money("66.66"));
        verify(due).setStatus("PARTIALLY_PAID");
        verify(dueRepository).save(due);

        ArgumentCaptor<PaymentDueTransaction> captor =
                ArgumentCaptor.forClass(PaymentDueTransaction.class);

        verify(txRepository).save(captor.capture());

        PaymentDueTransaction transaction = captor.getValue();

        assertEquals(money("100.00"), transaction.getAmount());
        assertEquals("RECEIPT", transaction.getDirection());
        assertEquals(TENANT, transaction.getTenantId());
        assertEquals(COMPANY, transaction.getCompanyId());
        assertEquals(DUE_ID, transaction.getPaymentDueId());

        verify(journalService).postCustomerReceipt(
                eq(TENANT),
                eq(DATE),
                eq("INV-00001"),
                eq(money("100.00")),
                eq("EUR"),
                eq(DUE_ID),
                anyString()
        );
    }

    @Test
    void supplierPaymentAtResidualClosesDue() {
        PaymentDue due = due(
                "PAYABLE",
                "100.00", "10.00", "30.00", "60.00");

        service.registerMovement(
                TENANT, DUE_ID, form("60.00"));

        verify(due).setPaidAmount(money("70.00"));
        verify(due).setOpenAmount(money("0.00"));
        verify(due).setStatus("PAID");
        verify(dueRepository).save(due);

        ArgumentCaptor<PaymentDueTransaction> captor =
                ArgumentCaptor.forClass(PaymentDueTransaction.class);

        verify(txRepository).save(captor.capture());

        assertEquals("PAYMENT", captor.getValue().getDirection());
        assertEquals(money("60.00"), captor.getValue().getAmount());

        verify(journalService).postSupplierPayment(
                eq(TENANT),
                eq(DATE),
                eq("INV-00001"),
                eq(money("60.00")),
                eq("EUR"),
                eq(DUE_ID),
                anyString()
        );
    }

    @Test
    void inconsistentFinancialBalanceIsRejectedWithoutWrites() {
        due("RECEIVABLE",
                "186.66", "0.00", "20.00", "186.66");

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.registerMovement(
                        TENANT, DUE_ID, form("50.00"))
        );

        assertTrue(ex.getMessage().contains("non quadrata"));
        verifyNoFinancialWrites();
    }
}
