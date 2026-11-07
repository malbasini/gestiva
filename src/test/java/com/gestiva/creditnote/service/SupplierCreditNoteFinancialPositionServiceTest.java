package com.gestiva.creditnote.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteFinancialPosition;
import com.gestiva.creditnote.repository.CreditNoteFinancialPositionRepository;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SupplierCreditNoteFinancialPositionServiceTest {

    private CompanyContext companyContext;
    private CreditNoteRepository creditNotes;
    private CreditNoteFinancialPositionRepository positions;

    private SupplierCreditNoteFinancialPositionService service;

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    @BeforeEach
    void setUp() {

        companyContext = mock(CompanyContext.class);
        creditNotes = mock(CreditNoteRepository.class);
        positions = mock(CreditNoteFinancialPositionRepository.class);

        service = new SupplierCreditNoteFinancialPositionService(
                companyContext,
                creditNotes,
                positions
        );

        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(3L);
    }

    private CreditNote note(
            String type,
            String status,
            BigDecimal amount) {

        CreditNote note = new CreditNote();

        note.setTenantId(2L);
        note.setCompanyId(3L);
        note.setCreditNoteType(type);
        note.setStatus(status);
        note.setCurrencyCode("EUR");
        note.setTotalAmount(amount);

        return note;
    }

    private void configureNote(CreditNote note) {

        when(creditNotes.findByTenantIdAndCompanyIdAndId(
                2L, 3L, 100L))
                .thenReturn(Optional.of(note));
    }

    private void assertMoney(
            String expected,
            BigDecimal actual) {

        assertNotNull(actual);
        assertEquals(
                0,
                money(expected).compareTo(actual)
        );
    }

    @Test
    void createsOpenFinancialPositionWithCorrectAmounts() {

        configureNote(
                note("SUPPLIER", "REGISTERED", money("12.20"))
        );

        CreditNoteFinancialPosition saved =
                new CreditNoteFinancialPosition();

        saved.setId(900L);

        when(positions.save(
                any(CreditNoteFinancialPosition.class)))
                .thenReturn(saved);

        Long result =
                service.createForRegisteredSupplierCreditNote(
                        2L, 100L);

        assertEquals(900L, result);

        ArgumentCaptor<CreditNoteFinancialPosition> captor =
                ArgumentCaptor.forClass(
                        CreditNoteFinancialPosition.class);

        verify(positions).save(captor.capture());

        CreditNoteFinancialPosition position =
                captor.getValue();

        assertEquals(2L, position.getTenantId());
        assertEquals(3L, position.getCompanyId());
        assertEquals(100L, position.getCreditNoteId());

        assertEquals("SUPPLIER", position.getPositionType());
        assertEquals("EUR", position.getCurrencyCode());
        assertEquals("OPEN", position.getStatus());

        assertMoney("12.20", position.getOriginalAmount());
        assertMoney("0.00", position.getCompensatedAmount());
        assertMoney("0.00", position.getRefundedAmount());
        assertMoney("12.20", position.getAvailableAmount());

        verify(positions)
                .existsByTenantIdAndCompanyIdAndCreditNoteId(
                        2L, 3L, 100L);

        verify(positions, times(1))
                .save(any(CreditNoteFinancialPosition.class));
    }

    @Test
    void rejectsDuplicateFinancialPosition() {

        configureNote(
                note("SUPPLIER", "REGISTERED", money("12.20"))
        );

        when(positions
                .existsByTenantIdAndCompanyIdAndCreditNoteId(
                        2L, 3L, 100L))
                .thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> service
                        .createForRegisteredSupplierCreditNote(
                                2L, 100L)
        );

        verify(positions, never()).save(any());
    }

    @Test
    void rejectsDraftCreditNote() {

        configureNote(
                note("SUPPLIER", "DRAFT", money("12.20"))
        );

        assertThrows(
                BusinessException.class,
                () -> service
                        .createForRegisteredSupplierCreditNote(
                                2L, 100L)
        );

        verifyNoInteractions(positions);
    }

    @Test
    void rejectsInvalidAmounts() {

        BigDecimal[] invalidAmounts = {
                null,
                money("0.00"),
                money("-1.00"),
                money("12.205")
        };

        for (BigDecimal amount : invalidAmounts) {

            reset(creditNotes, positions);

            configureNote(
                    note("SUPPLIER", "REGISTERED", amount)
            );

            assertThrows(
                    BusinessException.class,
                    () -> service
                            .createForRegisteredSupplierCreditNote(
                                    2L, 100L)
            );

            verify(positions, never()).save(any());
        }
    }

    @Test
    void rejectsNoteOutsideActiveCompany() {

        when(creditNotes.findByTenantIdAndCompanyIdAndId(
                2L, 3L, 100L))
                .thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> service
                        .createForRegisteredSupplierCreditNote(
                                2L, 100L)
        );

        verify(creditNotes)
                .findByTenantIdAndCompanyIdAndId(
                        2L, 3L, 100L);

        verifyNoInteractions(positions);
    }

    @Test
    void rejectsCustomerCreditNote() {

        configureNote(
                note("CUSTOMER", "REGISTERED", money("12.20"))
        );

        assertThrows(
                BusinessException.class,
                () -> service
                        .createForRegisteredSupplierCreditNote(
                                2L, 100L)
        );

        verifyNoInteractions(positions);
    }
}
