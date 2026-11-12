
package com.gestiva.creditnote.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerCreditNoteRegistrationServiceTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 3L;
    private static final Long NOTE_ID = 50L;
    private static final Long JOURNAL_ID = 90L;
    private static final Long POSITION_ID = 80L;

    @Mock
    CompanyContext companyContext;

    @Mock
    CreditNoteRepository notes;

    @Mock
    CustomerCreditNoteRegistrationPreparationService preparation;

    @Mock
    CustomerCreditNoteAccountingService accounting;

    @Mock
    CustomerCreditNoteFinancialPositionService financialPositions;

    @InjectMocks
    CustomerCreditNoteRegistrationService service;

    @BeforeEach
    void setup() {
        lenient()
                .when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);
    }

    private CreditNote note(String status) {
        CreditNote value = new CreditNote();

        ReflectionTestUtils.setField(
                value, "id", NOTE_ID);

        value.setTenantId(TENANT);
        value.setCompanyId(COMPANY);
        value.setCreditNoteType("CUSTOMER");
        value.setStatus(status);

        return value;
    }

    private void mockDraft() {
        when(notes.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, NOTE_ID))
                .thenReturn(Optional.of(note("DRAFT")));

        when(notes.saveAndFlush(any(CreditNote.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));
    }

    @Test
    void registersCustomerCreditNoteInCorrectOrder() {
        mockDraft();

        when(accounting.postRegisteredNote(
                TENANT, NOTE_ID))
                .thenReturn(JOURNAL_ID);

        when(financialPositions
                .createForRegisteredCustomerCreditNote(
                        TENANT, NOTE_ID))
                .thenReturn(POSITION_ID);

        var result = service.register(TENANT, NOTE_ID);

        assertEquals(NOTE_ID, result.creditNoteId());
        assertEquals(JOURNAL_ID, result.journalEntryId());
        assertEquals(POSITION_ID, result.financialPositionId());

        InOrder order = inOrder(
                preparation, notes, accounting, financialPositions);

        order.verify(preparation)
                .validateUnderLock(TENANT, NOTE_ID);

        order.verify(notes)
                .findByTenantIdAndCompanyIdAndId(
                        TENANT, COMPANY, NOTE_ID);

        order.verify(notes).saveAndFlush(
                argThat(n ->
                        "REGISTERED".equals(n.getStatus())));

        order.verify(accounting)
                .postRegisteredNote(TENANT, NOTE_ID);

        order.verify(financialPositions)
                .createForRegisteredCustomerCreditNote(
                        TENANT, NOTE_ID);

        order.verifyNoMoreInteractions();
    }

    @Test
    void rejectsAlreadyRegisteredNote() {
        when(notes.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, NOTE_ID))
                .thenReturn(Optional.of(note("REGISTERED")));

        assertThrows(BusinessException.class,
                () -> service.register(TENANT, NOTE_ID));

        verify(notes, never()).saveAndFlush(any());
        verifyNoInteractions(accounting, financialPositions);
    }

    @Test
    void stopsWhenPreparationFails() {
        doThrow(new BusinessException(
                "Rettifiche cumulative non valide"))
                .when(preparation)
                .validateUnderLock(TENANT, NOTE_ID);

        assertThrows(BusinessException.class,
                () -> service.register(TENANT, NOTE_ID));

        verifyNoInteractions(notes, accounting, financialPositions);
    }

    @Test
    void stopsWhenAccountingFails() {
        mockDraft();

        when(accounting.postRegisteredNote(
                TENANT, NOTE_ID))
                .thenThrow(new BusinessException(
                        "Errore contabilizzazione"));

        assertThrows(BusinessException.class,
                () -> service.register(TENANT, NOTE_ID));

        verify(notes).saveAndFlush(
                argThat(n ->
                        "REGISTERED".equals(n.getStatus())));

        verifyNoInteractions(financialPositions);
    }

    @Test
    void propagatesFinancialPositionFailure() {
        mockDraft();

        when(accounting.postRegisteredNote(
                TENANT, NOTE_ID))
                .thenReturn(JOURNAL_ID);

        when(financialPositions
                .createForRegisteredCustomerCreditNote(
                        TENANT, NOTE_ID))
                .thenThrow(new BusinessException(
                        "Errore posizione finanziaria"));

        assertThrows(BusinessException.class,
                () -> service.register(TENANT, NOTE_ID));

        verify(accounting)
                .postRegisteredNote(TENANT, NOTE_ID);

        verify(financialPositions)
                .createForRegisteredCustomerCreditNote(
                        TENANT, NOTE_ID);
    }
}
