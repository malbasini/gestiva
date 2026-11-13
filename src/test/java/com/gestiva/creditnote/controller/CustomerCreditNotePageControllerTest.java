
package com.gestiva.creditnote.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.service.CustomerCreditNoteDraftService;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerCreditNotePageControllerTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 3L;
    private static final Long INVOICE = 10L;

    @Mock TenantContext tenantContext;
    @Mock CompanyContext companyContext;
    @Mock CustomerCreditNoteDraftService drafts;
    @Mock RedirectAttributes redirectAttributes;

    @InjectMocks
    CustomerCreditNotePageController controller;

    private void authenticatedContext() {
        when(tenantContext.getCurrentTenantId())
                .thenReturn(TENANT);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);
    }

    @Test
    void createsFullDraftAndRedirectsToInvoice() {
        authenticatedContext();

        when(drafts.createFullDraft(
                TENANT, INVOICE, "Rettifica completa"))
                .thenReturn(50L);

        String result = controller.createFullDraft(
                INVOICE,
                "Rettifica completa",
                redirectAttributes);

        assertEquals("redirect:/customer-credit-notes/50", result);

        verify(drafts).createFullDraft(
                TENANT, INVOICE, "Rettifica completa");

        verify(redirectAttributes).addFlashAttribute(
                eq("successMessage"), anyString());
    }

    @Test
    void handlesBusinessErrorDuringFullDraftCreation() {
        authenticatedContext();

        when(drafts.createFullDraft(
                TENANT, INVOICE, "Rettifica"))
                .thenThrow(new BusinessException(
                        "Fattura non rettificabile"));

        String result = controller.createFullDraft(
                INVOICE, "Rettifica", redirectAttributes);

        assertEquals("redirect:/invoices/10", result);

        verify(redirectAttributes).addFlashAttribute(
                "errorMessage", "Fattura non rettificabile");
    }

    @Test
    void rejectsInvalidInvoiceIdBeforeCreatingFullDraft() {
        authenticatedContext();

        assertThrows(
                BusinessException.class,
                () -> controller.createFullDraft(
                        0L, "Rettifica", redirectAttributes));

        verifyNoInteractions(drafts);
    }

    @Test
    void createsPartialDraftWithSelectedQuantities() {
        authenticatedContext();

        when(drafts.createPartialDraft(
                eq(TENANT),
                eq(INVOICE),
                eq(Map.of(
                        100L, new BigDecimal("2.000"),
                        101L, new BigDecimal("1.500"))),
                eq("Rettifica parziale")))
                .thenReturn(51L);

        String result = controller.createPartialDraft(
                INVOICE,
                "Rettifica parziale",
                List.of(100L, 101L),
                List.of(
                        new BigDecimal("2.000"),
                        new BigDecimal("1.500")),
                redirectAttributes);

        assertEquals("redirect:/customer-credit-notes/51", result);

        verify(drafts).createPartialDraft(
                eq(TENANT),
                eq(INVOICE),
                eq(Map.of(
                        100L, new BigDecimal("2.000"),
                        101L, new BigDecimal("1.500"))),
                eq("Rettifica parziale"));

        verify(redirectAttributes).addFlashAttribute(
                eq("successMessage"), anyString());
    }

    @Test
    void rejectsDuplicateLineIds() {
        authenticatedContext();

        String result = controller.createPartialDraft(
                INVOICE,
                "Rettifica parziale",
                List.of(100L, 100L),
                List.of(
                        new BigDecimal("1.000"),
                        new BigDecimal("1.000")),
                redirectAttributes);

        assertEquals("redirect:/invoices/10", result);

        verifyNoInteractions(drafts);

        verify(redirectAttributes).addFlashAttribute(
                eq("errorMessage"), anyString());
    }

    @Test
    void rejectsInvalidPartialQuantities() {
        authenticatedContext();

        String result = controller.createPartialDraft(
                INVOICE,
                "Rettifica parziale",
                List.of(100L),
                List.of(BigDecimal.ZERO),
                redirectAttributes);

        assertEquals("redirect:/invoices/10", result);

        verifyNoInteractions(drafts);

        verify(redirectAttributes).addFlashAttribute(
                eq("errorMessage"), anyString());
    }

    @Test
    void rejectsMismatchedPartialLineLists() {
        authenticatedContext();

        String result = controller.createPartialDraft(
                INVOICE,
                "Rettifica parziale",
                List.of(100L, 101L),
                List.of(new BigDecimal("1.000")),
                redirectAttributes);

        assertEquals("redirect:/invoices/10", result);

        verifyNoInteractions(drafts);

        verify(redirectAttributes).addFlashAttribute(
                eq("errorMessage"), anyString());
    }

    @Test
    void propagatesCompanyAuthorizationFailure() {
        when(tenantContext.getCurrentTenantId())
                .thenReturn(TENANT);

        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new SecurityException(
                        "Società non autorizzata"));

        assertThrows(
                SecurityException.class,
                () -> controller.createFullDraft(
                        INVOICE,
                        "Rettifica",
                        redirectAttributes));

        verifyNoInteractions(drafts);
    }
}
