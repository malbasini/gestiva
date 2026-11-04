package com.gestiva.security.company.controller;

import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.company.service.CompanyAccessService;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class CompanySelectionUnauthorizedHttpTest {

    @Test
    void unauthorizedSelectionMustNotSucceed() throws Exception {

        CompanyContext context = mock(CompanyContext.class);
        CompanyAccessService access =
                mock(CompanyAccessService.class);

        CompanySelectionController controller =
                new CompanySelectionController(context, access);

        MockMvc mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        doThrow(new AccessDeniedException(
                "Accesso alla società non consentito"
        )).when(context).selectCompany(20L);

        Exception error = assertThrows(
                Exception.class,
                () -> mvc.perform(
                        post("/company/select")
                                .param("companyId", "20")
                )
        );

        Throwable cause = error;

        boolean denied = false;

        while (cause != null) {
            if (cause instanceof AccessDeniedException) {
                denied = true;
                break;
            }
            cause = cause.getCause();
        }

        assertTrue(
                denied,
                "Il controller deve propagare il diniego"
        );

        verify(context).selectCompany(20L);

        verify(context, never()).selectCompany(10L);

        verifyNoInteractions(access);

        System.out.println(
                "HTTP SELEZIONE NEGATA OK."
        );

        System.out.println(
                "NESSUN REDIRECT DI SUCCESSO."
        );
    }
}
