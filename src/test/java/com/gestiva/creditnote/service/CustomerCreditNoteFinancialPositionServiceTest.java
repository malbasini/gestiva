
package com.gestiva.creditnote.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteFinancialPosition;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.creditnote.repository.CreditNoteFinancialPositionRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerCreditNoteFinancialPositionServiceTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 3L;
    private static final Long NOTE_ID = 50L;

    @Mock
    private CompanyContext companyContext;

    @Mock
    private CreditNoteRepository creditNotes;

    @Mock
    private CreditNoteFinancialPositionRepository positions;

    @InjectMocks
    private CustomerCreditNoteFinancialPositionService service;

    @BeforeEach
    void setup() {
        lenient().when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);
    }

    private CreditNote note(String status, String amount) {
        CreditNote note = new CreditNote();

        note.setId(NOTE_ID);
        note.setTenantId(TENANT);
        note.setCompanyId(COMPANY);
        note.setCreditNoteType("CUSTOMER");
        note.setStatus(status);
        note.setOriginalInvoiceId(10L);
        note.setPartyId(20L);
        note.setCurrencyCode("EUR");
        note.setTotalAmount(new BigDecimal(amount));

        return note;
    }

    private void mockNote(CreditNote note) {
        when(creditNotes.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, NOTE_ID))
                .thenReturn(Optional.of(note));
    }

    @Test
    void createsOpenCustomerFinancialPosition() {
        mockNote(note("REGISTERED", "122.00"));

        when(positions.existsByTenantIdAndCompanyIdAndCreditNoteId(
                TENANT, COMPANY, NOTE_ID))
                .thenReturn(false);

        when(positions.save(any(CreditNoteFinancialPosition.class)))
                .thenAnswer(invocation -> {
                    CreditNoteFinancialPosition position =
                            invocation.getArgument(0);

                    ReflectionTestUtils.setField(
                            position, "id", 80L);

                    return position;
                });

        Long id = service.createForRegisteredCustomerCreditNote(
                TENANT, NOTE_ID);

        assertEquals(80L, id);

        ArgumentCaptor<CreditNoteFinancialPosition> captor =
                ArgumentCaptor.forClass(
                        CreditNoteFinancialPosition.class);

        verify(positions).save(captor.capture());

        CreditNoteFinancialPosition saved = captor.getValue();

        assertEquals(TENANT, saved.getTenantId());
        assertEquals(COMPANY, saved.getCompanyId());
        assertEquals(NOTE_ID, saved.getCreditNoteId());
        assertEquals("CUSTOMER", saved.getPositionType());
        assertEquals("EUR", saved.getCurrencyCode());
        assertEquals("OPEN", saved.getStatus());

        assertEquals(0, saved.getOriginalAmount()
                .compareTo(new BigDecimal("122.00")));

        assertEquals(0, saved.getAvailableAmount()
                .compareTo(new BigDecimal("122.00")));

        assertEquals(0, saved.getCompensatedAmount()
                .compareTo(BigDecimal.ZERO));

        assertEquals(0, saved.getRefundedAmount()
                .compareTo(BigDecimal.ZERO));
    }

    @Test
    void rejectsDraftNote() {
        mockNote(note("DRAFT", "122.00"));

        assertThrows(BusinessException.class,
                () -> service.createForRegisteredCustomerCreditNote(
                        TENANT, NOTE_ID));

        verifyNoInteractions(positions);
    }

    @Test
    void rejectsDuplicateFinancialPosition() {
        mockNote(note("REGISTERED", "122.00"));

        when(positions.existsByTenantIdAndCompanyIdAndCreditNoteId(
                TENANT, COMPANY, NOTE_ID))
                .thenReturn(true);

        assertThrows(BusinessException.class,
                () -> service.createForRegisteredCustomerCreditNote(
                        TENANT, NOTE_ID));

        verify(positions, never()).save(any());
    }

    @Test
    void rejectsNoteOutsideActiveCompany() {
        when(creditNotes.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, NOTE_ID))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> service.createForRegisteredCustomerCreditNote(
                        TENANT, NOTE_ID));

        verifyNoInteractions(positions);
    }

    @Test
    void rejectsInvalidAmount() {
        mockNote(note("REGISTERED", "0.00"));

        assertThrows(BusinessException.class,
                () -> service.createForRegisteredCustomerCreditNote(
                        TENANT, NOTE_ID));

        verifyNoInteractions(positions);
    }
}
