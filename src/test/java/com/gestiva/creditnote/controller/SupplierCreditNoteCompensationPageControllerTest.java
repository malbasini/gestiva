
package com.gestiva.creditnote.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNoteFinancialPosition;
import com.gestiva.creditnote.repository.CreditNoteFinancialPositionRepository;
import com.gestiva.creditnote.service.SupplierCreditNoteCompensationService;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierCreditNoteCompensationPageControllerTest {

    private TenantContext tenantContext;
    private CompanyContext companyContext;
    private CreditNoteFinancialPositionRepository positions;
    private SupplierCreditNoteCompensationService service;
    private SupplierCreditNoteCompensationPageController controller;

    private static final LocalDate DATE =
            LocalDate.of(2026, 11, 12);

    @BeforeEach
    void setUp() {
        tenantContext = mock(TenantContext.class);
        companyContext = mock(CompanyContext.class);
        positions = mock(CreditNoteFinancialPositionRepository.class);
        service = mock(SupplierCreditNoteCompensationService.class);

        controller = new SupplierCreditNoteCompensationPageController(
                tenantContext, companyContext, positions, service);
    }

    private Authentication auth(String role) {
        return new UsernamePasswordAuthenticationToken(
                "tester",
                "unused",
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    private RedirectAttributes flash() {
        return new RedirectAttributesModelMap();
    }

    private void prepareValidContext() {
        when(tenantContext.getCurrentTenantId()).thenReturn(2L);
        when(companyContext.requireCurrentCompanyId()).thenReturn(3L);
    }

    @Test
    void adminCompensatesUsingServerResolvedPosition() {
        prepareValidContext();

        CreditNoteFinancialPosition position =
                mock(CreditNoteFinancialPosition.class);

        when(position.getId()).thenReturn(19L);

        when(positions.findByTenantIdAndCompanyIdAndCreditNoteId(
                2L, 3L, 10L))
                .thenReturn(Optional.of(position));

        when(service.compensate(
                2L, 19L, 7L, new BigDecimal("12.50"),
                DATE, "op-123", "test"))
                .thenReturn(501L);

        RedirectAttributes attributes = flash();

        String redirect = controller.compensate(
                10L, 7L, new BigDecimal("12.50"), "op-123",
                DATE, "test", auth("ADMIN"), attributes);

        assertEquals("redirect:/supplier-credit-notes/10", redirect);
        assertTrue(attributes.getFlashAttributes()
                .containsKey("successMessage"));

        verify(service).compensate(
                2L, 19L, 7L, new BigDecimal("12.50"),
                DATE, "op-123", "test");
    }

    @Test
    void accountingIsAuthorized() {
        prepareValidContext();

        CreditNoteFinancialPosition position =
                mock(CreditNoteFinancialPosition.class);
        when(position.getId()).thenReturn(19L);

        when(positions.findByTenantIdAndCompanyIdAndCreditNoteId(
                2L, 3L, 10L))
                .thenReturn(Optional.of(position));

        when(service.compensate(
                anyLong(), anyLong(), anyLong(), any(),
                any(), anyString(), any()))
                .thenReturn(502L);

        RedirectAttributes attributes = flash();

        controller.compensate(
                10L, 7L, new BigDecimal("1.00"), "op-456",
                DATE, null, auth("ACCOUNTING"), attributes);

        assertTrue(attributes.getFlashAttributes()
                .containsKey("successMessage"));
        verify(service, times(1)).compensate(
                eq(2L), eq(19L), eq(7L), any(),
                eq(DATE), eq("op-456"), isNull());
    }

    @Test
    void purchasingCannotCompensate() {
        assertThrows(AccessDeniedException.class,
                () -> controller.compensate(
                        10L, 7L, new BigDecimal("1.00"), "op",
                        DATE, null, auth("PURCHASING"), flash()));

        verifyNoInteractions(tenantContext, companyContext, positions, service);
    }

    @Test
    void missingPositionDoesNotInvokeService() {
        prepareValidContext();

        when(positions.findByTenantIdAndCompanyIdAndCreditNoteId(
                2L, 3L, 10L))
                .thenReturn(Optional.empty());

        RedirectAttributes attributes = flash();

        controller.compensate(
                10L, 7L, new BigDecimal("1.00"), "op-789",
                DATE, null, auth("ADMIN"), attributes);

        assertTrue(attributes.getFlashAttributes()
                .containsKey("errorMessage"));
        verifyNoInteractions(service);
    }

    @Test
    void invalidAmountDoesNotInvokeService() {
        prepareValidContext();

        RedirectAttributes attributes = flash();

        controller.compensate(
                10L, 7L, new BigDecimal("0.00"), "op-789",
                DATE, null, auth("ADMIN"), attributes);

        assertTrue(attributes.getFlashAttributes()
                .containsKey("errorMessage"));
        verifyNoInteractions(positions, service);
    }
}

