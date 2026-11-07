package com.gestiva.creditnote.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SupplierCreditNoteRegistrationServiceTest {

    private CompanyContext companies;
    private CreditNoteRepository notes;
    private SupplierCreditNoteRegistrationPreparationService preparation;
    private SupplierCreditNoteAccountingService accounting;
    private SupplierCreditNoteFinancialPositionService finance;
    private SupplierCreditNoteRegistrationService service;

    @BeforeEach
    void setUp() {
        companies = mock(CompanyContext.class);
        notes = mock(CreditNoteRepository.class);
        preparation = mock(
                SupplierCreditNoteRegistrationPreparationService.class);
        accounting = mock(SupplierCreditNoteAccountingService.class);
        finance = mock(
                SupplierCreditNoteFinancialPositionService.class);

        service = new SupplierCreditNoteRegistrationService(
                companies, notes, preparation, accounting, finance);

        when(companies.resolveCurrentCompanyId())
                .thenReturn(3L);
    }

    private CreditNote draft() {
        CreditNote note = new CreditNote();
        note.setId(100L);
        note.setTenantId(2L);
        note.setCompanyId(3L);
        note.setCreditNoteType("SUPPLIER");
        note.setStatus("DRAFT");
        return note;
    }

    private CreditNote configureDraft() {
        CreditNote note = draft();

        when(notes.findByTenantIdAndCompanyIdAndId(
                2L, 3L, 100L))
                .thenReturn(Optional.of(note));

        when(notes.saveAndFlush(note))
                .thenReturn(note);

        return note;
    }

    @Test
    void registersInCorrectOrderAndReturnsAllIds() {
        CreditNote note = configureDraft();

        when(accounting.postRegisteredNote(2L, 100L))
                .thenReturn(900L);

        when(finance.createForRegisteredSupplierCreditNote(
                2L, 100L))
                .thenReturn(800L);

        var result = service.register(2L, 100L);

        assertEquals(100L, result.creditNoteId());
        assertEquals(900L, result.journalEntryId());
        assertEquals(800L, result.financialPositionId());
        assertEquals("REGISTERED", note.getStatus());

        InOrder order = inOrder(
                preparation, notes, accounting, finance);

        order.verify(preparation)
                .validateUnderLock(2L, 100L);

        order.verify(notes)
                .findByTenantIdAndCompanyIdAndId(
                        2L, 3L, 100L);

        order.verify(notes)
                .saveAndFlush(note);

        order.verify(accounting)
                .postRegisteredNote(2L, 100L);

        order.verify(finance)
                .createForRegisteredSupplierCreditNote(
                        2L, 100L);

        order.verifyNoMoreInteractions();
    }

    @Test
    void validationFailurePreventsAllWrites() {
        doThrow(new BusinessException(
                "Bozza non valida"))
                .when(preparation)
                .validateUnderLock(2L, 100L);

        assertThrows(
                BusinessException.class,
                () -> service.register(2L, 100L));

        verifyNoInteractions(
                notes, accounting, finance);
    }

    @Test
    void refusesDocumentNoLongerDraft() {
        CreditNote note = draft();
        note.setStatus("REGISTERED");

        when(notes.findByTenantIdAndCompanyIdAndId(
                2L, 3L, 100L))
                .thenReturn(Optional.of(note));

        assertThrows(
                BusinessException.class,
                () -> service.register(2L, 100L));

        verify(notes, never())
                .saveAndFlush(any(CreditNote.class));

        verifyNoInteractions(accounting, finance);
    }

    @Test
    void accountingFailurePreventsFinancialPosition() {
        CreditNote note = configureDraft();

        doThrow(new BusinessException(
                "Errore contabilizzazione"))
                .when(accounting)
                .postRegisteredNote(2L, 100L);

        assertThrows(
                BusinessException.class,
                () -> service.register(2L, 100L));

        assertEquals("REGISTERED", note.getStatus());

        verify(notes).saveAndFlush(note);
        verifyNoInteractions(finance);
    }

    @Test
    void financialFailurePropagatesException() {
        CreditNote note = configureDraft();

        when(accounting.postRegisteredNote(2L, 100L))
                .thenReturn(900L);

        doThrow(new BusinessException(
                "Errore posizione finanziaria"))
                .when(finance)
                .createForRegisteredSupplierCreditNote(
                        2L, 100L);

        assertThrows(
                BusinessException.class,
                () -> service.register(2L, 100L));

        assertEquals("REGISTERED", note.getStatus());

        verify(accounting)
                .postRegisteredNote(2L, 100L);

        verify(finance)
                .createForRegisteredSupplierCreditNote(
                        2L, 100L);
    }

    @Test
    void rejectsMissingIdentifiersWithoutWrites() {
        assertThrows(
                BusinessException.class,
                () -> service.register(null, 100L));

        assertThrows(
                BusinessException.class,
                () -> service.register(2L, null));

        verifyNoInteractions(
                notes, preparation, accounting, finance);
    }
}
