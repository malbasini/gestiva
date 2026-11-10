package com.gestiva.creditnote.service;

import com.gestiva.accounting.due.entity.PaymentDue;
import com.gestiva.accounting.due.repository.PaymentDueRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteFinancialPosition;
import com.gestiva.creditnote.entity.SupplierCreditNoteCompensation;
import com.gestiva.creditnote.repository.CreditNoteFinancialPositionRepository;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.creditnote.repository.SupplierCreditNoteCompensationRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SupplierCreditNoteCompensationServiceTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 3L;
    private static final Long POSITION = 101L;
    private static final Long DUE = 201L;
    private static final Long NOTE = 301L;
    private static final LocalDate DATE =
            LocalDate.of(2026, 11, 10);

    private CompanyContext companies;
    private CreditNoteFinancialPositionRepository positions;
    private CreditNoteRepository notes;
    private PaymentDueRepository dues;
    private SupplierCreditNoteCompensationRepository compensations;
    private SupplierCreditNoteCompensationService service;

    private CreditNoteFinancialPosition position;
    private PaymentDue due;

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    @BeforeEach
    void setup() {
        companies = mock(CompanyContext.class);
        positions = mock(CreditNoteFinancialPositionRepository.class);
        notes = mock(CreditNoteRepository.class);
        dues = mock(PaymentDueRepository.class);
        compensations = mock(
                SupplierCreditNoteCompensationRepository.class);

        service = new SupplierCreditNoteCompensationService(
                companies, positions, notes, dues, compensations);

        when(companies.requireCurrentCompanyId()).thenReturn(COMPANY);

        position = new CreditNoteFinancialPosition();
        position.setTenantId(TENANT);
        position.setCompanyId(COMPANY);
        position.setCreditNoteId(NOTE);
        position.setPositionType("SUPPLIER");
        position.setCurrencyCode("EUR");
        position.setOriginalAmount(money("63.24"));
        position.setCompensatedAmount(money("0.00"));
        position.setRefundedAmount(money("0.00"));
        position.setAvailableAmount(money("63.24"));
        position.setStatus("OPEN");

        due = new PaymentDue();
        due.setTenantId(TENANT);
        due.setCompanyId(COMPANY);
        due.setDirection("PAYABLE");
        due.setPartyType("SUPPLIER");
        due.setPartyId(11L);
        due.setReferenceType("SUPPLIER_INVOICE");
        due.setReferenceId(401L);
        due.setCurrencyCode("EUR");
        due.setGrossAmount(money("100.00"));
        due.setPaidAmount(money("20.00"));
        due.setCompensatedAmount(money("0.00"));
        due.setOpenAmount(money("80.00"));
        due.setStatus("PARTIALLY_PAID");

        when(positions.findByTenantIdAndCompanyIdAndIdForUpdate(
                TENANT, COMPANY, POSITION))
                .thenReturn(Optional.of(position));

        when(dues.findByTenantIdAndCompanyIdAndIdForUpdate(
                TENANT, COMPANY, DUE))
                .thenReturn(Optional.of(due));

        CreditNote note = mock(CreditNote.class);
        when(note.getCreditNoteType()).thenReturn("SUPPLIER");
        when(note.getStatus()).thenReturn("REGISTERED");
        when(note.getPartyId()).thenReturn(11L);
        when(note.getCurrencyCode()).thenReturn("EUR");

        when(notes.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, NOTE))
                .thenReturn(Optional.of(note));
    }

    private Long compensate(String amount, String key) {
        return service.compensate(
                TENANT, POSITION, DUE, money(amount),
                DATE, key, "Test compensazione");
    }

    private void successfulSave(Long id) {
        SupplierCreditNoteCompensation saved =
                mock(SupplierCreditNoteCompensation.class);
        when(saved.getId()).thenReturn(id);
        when(compensations.saveAndFlush(
                any(SupplierCreditNoteCompensation.class)))
                .thenReturn(saved);
    }

    private void verifyNoWrites() {
        verify(positions, never()).save(any());
        verify(dues, never()).save(any());
        verify(compensations, never()).saveAndFlush(any());
    }

    @Test
    void partialCompensationUpdatesBothBalances() {
        successfulSave(501L);

        Long id = compensate("20.00", "TEST-V10-PARTIAL");

        assertEquals(501L, id);
        assertEquals(0, position.getCompensatedAmount()
                .compareTo(money("20.00")));
        assertEquals(0, position.getAvailableAmount()
                .compareTo(money("43.24")));
        assertEquals("PARTIALLY_SETTLED", position.getStatus());

        assertEquals(0, due.getPaidAmount()
                .compareTo(money("20.00")));
        assertEquals(0, due.getCompensatedAmount()
                .compareTo(money("20.00")));
        assertEquals(0, due.getOpenAmount()
                .compareTo(money("60.00")));
        assertEquals("PARTIALLY_PAID", due.getStatus());

        verify(positions).save(same(position));
        verify(dues).save(same(due));
        verify(compensations).saveAndFlush(argThat(c ->
                c.getAmount().compareTo(money("20.00")) == 0
                && c.getCreditNoteFinancialPositionId()
                        .equals(POSITION)
                && c.getPaymentDueId().equals(DUE)
                && c.getOperationKey().equals("TEST-V10-PARTIAL")));
    }

    @Test
    void fullCreditCompensationSettlesCreditPosition() {
        successfulSave(502L);

        compensate("63.24", "TEST-V10-FULL-CREDIT");

        assertEquals(0, position.getAvailableAmount()
                .compareTo(money("0.00")));
        assertEquals("SETTLED", position.getStatus());
        assertEquals(0, due.getOpenAmount()
                .compareTo(money("16.76")));
        assertEquals("PARTIALLY_PAID", due.getStatus());
    }

    @Test
    void rejectsDifferentSupplierWithoutWriting() {
        due.setPartyId(99L);

        assertThrows(BusinessException.class,
                () -> compensate("10.00", "TEST-V10-SUPPLIER"));

        verifyNoWrites();
    }

    @Test
    void rejectsAmountAboveAvailableCredit() {
        assertThrows(BusinessException.class,
                () -> compensate("63.25", "TEST-V10-CREDIT-LIMIT"));

        verifyNoWrites();
    }

    @Test
    void rejectsAmountAboveOutstandingDue() {
        due.setGrossAmount(money("40.00"));
        due.setPaidAmount(money("20.00"));
        due.setOpenAmount(money("20.00"));

        assertThrows(BusinessException.class,
                () -> compensate("20.01", "TEST-V10-DUE-LIMIT"));

        verifyNoWrites();
    }

    @Test
    void repeatedSameOperationReturnsPreviousId() {
        SupplierCreditNoteCompensation previous =
                mock(SupplierCreditNoteCompensation.class);

        when(previous.getId()).thenReturn(600L);
        when(previous.getCreditNoteFinancialPositionId())
                .thenReturn(POSITION);
        when(previous.getPaymentDueId()).thenReturn(DUE);
        when(previous.getAmount()).thenReturn(money("10.00"));
        when(previous.getCompensationDate()).thenReturn(DATE);
        when(previous.getNotes()).thenReturn("Test compensazione");

        when(compensations.findByTenantIdAndCompanyIdAndOperationKey(
                TENANT, COMPANY, "TEST-V10-REPLAY"))
                .thenReturn(Optional.of(previous));

        Long id = compensate("10.00", "TEST-V10-REPLAY");

        assertEquals(600L, id);
        verifyNoWrites();
    }

    @Test
    void repeatedKeyWithDifferentAmountIsRejected() {
        SupplierCreditNoteCompensation previous =
                mock(SupplierCreditNoteCompensation.class);

        when(previous.getCreditNoteFinancialPositionId())
                .thenReturn(POSITION);
        when(previous.getPaymentDueId()).thenReturn(DUE);
        when(previous.getAmount()).thenReturn(money("5.00"));

        when(compensations.findByTenantIdAndCompanyIdAndOperationKey(
                TENANT, COMPANY, "TEST-V10-CONFLICT"))
                .thenReturn(Optional.of(previous));

        assertThrows(BusinessException.class,
                () -> compensate("10.00", "TEST-V10-CONFLICT"));

        verifyNoWrites();
    }

    @Test
    void foreignCompanyPositionIsRejected() {
        when(positions.findByTenantIdAndCompanyIdAndIdForUpdate(
                TENANT, COMPANY, POSITION))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> compensate("10.00", "TEST-V10-ISOLATION"));

        verify(positions).findByTenantIdAndCompanyIdAndIdForUpdate(
                TENANT, COMPANY, POSITION);
        verify(dues, never())
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        anyLong(), anyLong(), anyLong());
        verifyNoWrites();
    }
}
