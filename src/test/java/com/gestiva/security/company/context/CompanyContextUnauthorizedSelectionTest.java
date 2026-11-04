package com.gestiva.security.company.context;

import com.gestiva.platform.company.entity.Company;
import com.gestiva.security.company.service.CompanyAccessService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompanyContextUnauthorizedSelectionTest {

    @Test
    void unauthorizedSelectionMustPreserveActiveCompany() {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        HttpSession session =
                mock(HttpSession.class);

        CompanyAccessService accessService =
                mock(CompanyAccessService.class);

        Company authorizedCompany =
                mock(Company.class);

        when(authorizedCompany.getId())
                .thenReturn(10L);

        when(request.getSession(true))
                .thenReturn(session);

        when(request.getSession(false))
                .thenReturn(session);

        when(accessService.requireAccess(10L))
                .thenReturn(authorizedCompany);

        when(accessService.requireAccess(20L))
                .thenThrow(new AccessDeniedException(
                        "Accesso alla società non consentito"
                ));

        CompanyContext context =
                new CompanyContext(request, accessService);

        // Prima selezione: società autorizzata.
        Company selected = context.selectCompany(10L);

        assertSame(authorizedCompany, selected);

        verify(session).setAttribute(
                "GESTIVA_ACTIVE_COMPANY_ID",
                10L
        );

        // Il mock restituisce il valore già selezionato.
        when(session.getAttribute(
                "GESTIVA_ACTIVE_COMPANY_ID"
        )).thenReturn(10L);

        // Seconda selezione: società non autorizzata.
        assertThrows(
                AccessDeniedException.class,
                () -> context.selectCompany(20L)
        );

        // Nessuna scrittura relativa alla società vietata.
        verify(session, never()).setAttribute(
                "GESTIVA_ACTIVE_COMPANY_ID",
                20L
        );

        // Una sola scrittura complessiva nella sessione.
        verify(session, times(1)).setAttribute(
                eq("GESTIVA_ACTIVE_COMPANY_ID"),
                any()
        );

        // La società autorizzata rimane quella attiva.
        assertEquals(
                10L,
                context.getCurrentCompanyIdOrNull()
        );

        verify(accessService).requireAccess(20L);

        System.out.println(
                "ACCESSO NEGATO OK: società non autorizzata."
        );

        System.out.println(
                "SESSIONE INVARIATA OK: società 10 conservata."
        );
    }
}
