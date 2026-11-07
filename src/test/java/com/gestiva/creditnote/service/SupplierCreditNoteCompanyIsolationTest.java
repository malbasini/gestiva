package com.gestiva.creditnote.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.repository.CreditNoteLineRepository;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceLineRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.settings.sequence.service.DocumentSequenceService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierCreditNoteCompanyIsolationTest {

    private static final Long TENANT = 2L;
    private static final Long OTHER_TENANT = 8L;
    private static final Long COMPANY = 3L;
    private static final Long OTHER_COMPANY = 7L;
    private static final Long INVOICE = 35L;
    private static final Long NOTE = 100L;
    private static final Long LINE = 501L;

    private CompanyContext companies;
    private SupplierInvoiceRepository invoices;
    private SupplierInvoiceLineRepository invoiceLines;
    private CreditNoteRepository notes;
    private CreditNoteLineRepository noteLines;
    private DocumentSequenceService sequences;
    private SupplierCreditNoteRegisteredCreditsService credits;
    private SupplierCreditNoteRegistrationPreparationService preparation;
    private SupplierCreditNoteAccountingService accounting;
    private SupplierCreditNoteFinancialPositionService finance;

    private SupplierCreditNoteDraftService drafts;
    private SupplierCreditNoteRegistrationService registration;

    @BeforeEach
    void setup() {
        companies = mock(CompanyContext.class);
        invoices = mock(SupplierInvoiceRepository.class);
        invoiceLines = mock(SupplierInvoiceLineRepository.class);
        notes = mock(CreditNoteRepository.class);
        noteLines = mock(CreditNoteLineRepository.class);
        sequences = mock(DocumentSequenceService.class);
        credits = mock(SupplierCreditNoteRegisteredCreditsService.class);
        accounting = mock(SupplierCreditNoteAccountingService.class);
        finance = mock(SupplierCreditNoteFinancialPositionService.class);

        drafts = new SupplierCreditNoteDraftService(
                invoices, invoiceLines, notes, noteLines,
                sequences, companies);

        preparation = new SupplierCreditNoteRegistrationPreparationService(
                companies, invoices, invoiceLines, notes, noteLines, credits);

        registration = new SupplierCreditNoteRegistrationService(
                companies, notes, preparation, accounting, finance);
    }

    private void rejectFullDraft(Long tenant, Long activeCompany) {
        when(companies.resolveCurrentCompanyId()).thenReturn(activeCompany);

        when(invoices.findByTenantIdAndCompanyIdAndId(
                tenant, activeCompany, INVOICE))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> drafts.createFullDraft(
                        tenant, INVOICE, "Rettifica test isolamento"));

        verify(invoices).findByTenantIdAndCompanyIdAndId(
                tenant, activeCompany, INVOICE);

        verifyNoInteractions(
                invoiceLines, notes, noteLines, sequences);
    }

    private void rejectPartialDraft(Long tenant, Long activeCompany) {
        when(companies.resolveCurrentCompanyId()).thenReturn(activeCompany);

        when(invoices.findByTenantIdAndCompanyIdAndId(
                tenant, activeCompany, INVOICE))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> drafts.createPartialDraft(
                        tenant,
                        INVOICE,
                        Map.of(LINE, new BigDecimal("1.000")),
                        "Rettifica test isolamento"));

        verify(invoices).findByTenantIdAndCompanyIdAndId(
                tenant, activeCompany, INVOICE);

        verifyNoInteractions(
                invoiceLines, notes, noteLines, sequences);
    }

    private void rejectPreparation(Long tenant, Long activeCompany) {
        when(companies.resolveCurrentCompanyId()).thenReturn(activeCompany);

        when(notes.findByTenantIdAndCompanyIdAndId(
                tenant, activeCompany, NOTE))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> preparation.validateUnderLock(tenant, NOTE));

        verify(notes).findByTenantIdAndCompanyIdAndId(
                tenant, activeCompany, NOTE);

        verifyNoMoreInteractions(notes);
        verifyNoInteractions(invoices, invoiceLines, noteLines, credits);
    }

    private void rejectRegistrationAtPreparation(
            Long tenant, Long activeCompany) {

        when(companies.resolveCurrentCompanyId()).thenReturn(activeCompany);

        // Simula il rifiuto ottenuto dalla preparazione sotto lock.
        // Non sostituisce un test di integrazione del repository reale.
        doThrow(new BusinessException("Nota non accessibile"))
                .when(preparation).validateUnderLock(tenant, NOTE);

        assertThrows(BusinessException.class,
                () -> registration.register(tenant, NOTE));

        verify(preparation).validateUnderLock(tenant, NOTE);

        verifyNoInteractions(notes, accounting, finance);
    }

    private void rejectRegistrationAfterPreparation(
            Long tenant, Long activeCompany) {

        when(companies.resolveCurrentCompanyId()).thenReturn(activeCompany);

        // In questa prova la validazione simulata termina senza errori.
        // La seconda lettura protetta deve impedire le scritture.
        when(notes.findByTenantIdAndCompanyIdAndId(
                tenant, activeCompany, NOTE))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> registration.register(tenant, NOTE));

        verify(preparation).validateUnderLock(tenant, NOTE);

        verify(notes).findByTenantIdAndCompanyIdAndId(
                tenant, activeCompany, NOTE);

        verifyNoMoreInteractions(notes);
        verifyNoInteractions(accounting, finance);
    }

    @Test
    void fullDraftRejectsForeignCompany() {
        rejectFullDraft(TENANT, OTHER_COMPANY);
    }

    @Test
    void fullDraftRejectsForeignTenant() {
        rejectFullDraft(OTHER_TENANT, COMPANY);
    }

    @Test
    void partialDraftRejectsForeignCompany() {
        rejectPartialDraft(TENANT, OTHER_COMPANY);
    }

    @Test
    void partialDraftRejectsForeignTenant() {
        rejectPartialDraft(OTHER_TENANT, COMPANY);
    }

    @Test
    void preparationRejectsForeignCompany() {
        rejectPreparation(TENANT, OTHER_COMPANY);
    }

    @Test
    void preparationRejectsForeignTenant() {
        rejectPreparation(OTHER_TENANT, COMPANY);
    }

    @Test
    void registrationStopsWhenForeignCompanyRejectedByPreparation() {
        rejectRegistrationAtPreparation(TENANT, OTHER_COMPANY);
    }

    @Test
    void registrationStopsWhenForeignTenantRejectedByPreparation() {
        rejectRegistrationAtPreparation(OTHER_TENANT, COMPANY);
    }

    @Test
    void registrationRejectsForeignCompanyAfterPreparation() {
        rejectRegistrationAfterPreparation(TENANT, OTHER_COMPANY);
    }

    @Test
    void registrationRejectsForeignTenantAfterPreparation() {
        rejectRegistrationAfterPreparation(OTHER_TENANT, COMPANY);
    }
}
