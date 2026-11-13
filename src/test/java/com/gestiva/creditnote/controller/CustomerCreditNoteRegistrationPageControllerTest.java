
package com.gestiva.creditnote.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.service.CustomerCreditNoteRegistrationService;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerCreditNoteRegistrationPageControllerTest {

    private TenantContext tenantContext;
    private CompanyContext companyContext;
    private CustomerCreditNoteRegistrationService registrationService;
    private CustomerCreditNoteRegistrationPageController controller;

    @BeforeEach
    void setUp() {

        tenantContext = mock(TenantContext.class);
        companyContext = mock(CompanyContext.class);

        registrationService = mock(
                CustomerCreditNoteRegistrationService.class
        );

        controller =
                new CustomerCreditNoteRegistrationPageController(
                        tenantContext,
                        companyContext,
                        registrationService
                );

        when(tenantContext.getCurrentTenantId())
                .thenReturn(2L);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(3L);
    }

    @Test
    void successfulRegistrationRedirectsToDetail() {

        when(registrationService.register(2L, 50L))
                .thenReturn(
                        new CustomerCreditNoteRegistrationService
                                .RegistrationResult(
                                50L,
                                100L,
                                200L
                        )
                );

        RedirectAttributesModelMap attributes =
                new RedirectAttributesModelMap();

        String redirect = controller.register(
                50L,
                attributes
        );

        assertEquals(
                "redirect:/customer-credit-notes/50",
                redirect
        );

        assertNotNull(
                attributes.getFlashAttributes()
                        .get("successMessage")
        );

        assertFalse(
                attributes.getFlashAttributes()
                        .containsKey("errorMessage")
        );

        verify(companyContext)
                .requireCurrentCompanyId();

        verify(registrationService, times(1))
                .register(2L, 50L);
    }

    @Test
    void businessErrorRedirectsWithErrorMessage() {

        when(registrationService.register(2L, 50L))
                .thenThrow(
                        new BusinessException(
                                "Nota cliente non più registrabile."
                        )
                );

        RedirectAttributesModelMap attributes =
                new RedirectAttributesModelMap();

        String redirect = controller.register(
                50L,
                attributes
        );

        assertEquals(
                "redirect:/customer-credit-notes/50",
                redirect
        );

        assertEquals(
                "Nota cliente non più registrabile.",
                attributes.getFlashAttributes()
                        .get("errorMessage")
        );

        assertFalse(
                attributes.getFlashAttributes()
                        .containsKey("successMessage")
        );

        verify(registrationService, times(1))
                .register(2L, 50L);
    }

    @Test
    void invalidCreditNoteIdDoesNotCallService() {

        RedirectAttributesModelMap attributes =
                new RedirectAttributesModelMap();

        assertThrows(
                BusinessException.class,
                () -> controller.register(
                        0L,
                        attributes
                )
        );

        verifyNoInteractions(registrationService);
    }

    @Test
    void invalidTenantDoesNotCallService() {

        when(tenantContext.getCurrentTenantId())
                .thenReturn(null);

        RedirectAttributesModelMap attributes =
                new RedirectAttributesModelMap();

        assertThrows(
                BusinessException.class,
                () -> controller.register(
                        50L,
                        attributes
                )
        );

        verifyNoInteractions(registrationService);
    }

    @Test
    void unauthorizedCompanyErrorPropagates() {

        when(companyContext.requireCurrentCompanyId())
                .thenThrow(
                        new AccessDeniedException(
                                "Società non autorizzata."
                        )
                );

        RedirectAttributesModelMap attributes =
                new RedirectAttributesModelMap();

        assertThrows(
                AccessDeniedException.class,
                () -> controller.register(
                        50L,
                        attributes
                )
        );

        verifyNoInteractions(registrationService);

        assertTrue(
                attributes.getFlashAttributes().isEmpty()
        );
    }

    @Test
    void nullRegistrationResultDoesNotReportSuccess() {

        when(registrationService.register(2L, 50L))
                .thenReturn(null);

        RedirectAttributesModelMap attributes =
                new RedirectAttributesModelMap();

        assertThrows(
                IllegalStateException.class,
                () -> controller.register(
                        50L,
                        attributes
                )
        );

        assertFalse(
                attributes.getFlashAttributes()
                        .containsKey("successMessage")
        );

        verify(registrationService, times(1))
                .register(2L, 50L);
    }

    @Test
    void mismatchedRegistrationResultDoesNotReportSuccess() {

        when(registrationService.register(2L, 50L))
                .thenReturn(
                        new CustomerCreditNoteRegistrationService
                                .RegistrationResult(
                                99L,
                                100L,
                                200L
                        )
                );

        RedirectAttributesModelMap attributes =
                new RedirectAttributesModelMap();

        assertThrows(
                IllegalStateException.class,
                () -> controller.register(
                        50L,
                        attributes
                )
        );

        assertFalse(
                attributes.getFlashAttributes()
                        .containsKey("successMessage")
        );

        verify(registrationService, times(1))
                .register(2L, 50L);
    }
}
