
package com.gestiva.creditnote.controller;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteLine;
import com.gestiva.creditnote.service.CustomerCreditNoteDetailService;
import com.gestiva.security.usercontext.TenantContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerCreditNoteViewControllerTest {

    private TenantContext tenantContext;
    private CustomerCreditNoteDetailService detailService;
    private CustomerCreditNoteViewController controller;

    @BeforeEach
    void setUp() {
        tenantContext = mock(TenantContext.class);
        detailService = mock(CustomerCreditNoteDetailService.class);

        controller = new CustomerCreditNoteViewController(
                tenantContext,
                detailService
        );

        when(tenantContext.getCurrentTenantId())
                .thenReturn(2L);
    }

    private TestingAuthenticationToken user(String role) {
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(
                        "test-user",
                        "unused",
                        role
                );

        authentication.setAuthenticated(true);
        return authentication;
    }

    private CreditNote note(String status) {
        CreditNote note = mock(CreditNote.class);
        when(note.getStatus()).thenReturn(status);
        return note;
    }

    private void givenDetail(CreditNote note) {
        when(detailService.getDetail(2L, 50L))
                .thenReturn(
                        new CustomerCreditNoteDetailService.Detail(
                                note,
                                List.<CreditNoteLine>of()
                        )
                );
    }

    @Test
    void adminCanRegisterDraft() {
        CreditNote note = note("DRAFT");
        givenDetail(note);

        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.detail(
                50L,
                user("ROLE_ADMIN"),
                model
        );

        assertEquals(
                "creditnote/customer-credit-note-detail",
                view
        );
        assertSame(note, model.get("creditNote"));
        assertEquals(List.of(), model.get("creditNoteLines"));
        assertEquals(
                Boolean.TRUE,
                model.get("canRegisterCreditNote")
        );
        assertEquals("invoices", model.get("activeMenu"));

        verify(detailService).getDetail(2L, 50L);
    }

    @Test
    void accountingCanRegisterDraft() {
        CreditNote note = note("DRAFT");
        givenDetail(note);

        ExtendedModelMap model = new ExtendedModelMap();

        controller.detail(
                50L,
                user("ROLE_ACCOUNTING"),
                model
        );

        assertEquals(
                Boolean.TRUE,
                model.get("canRegisterCreditNote")
        );
    }

    @Test
    void salesCanViewButCannotRegister() {
        CreditNote note = note("DRAFT");
        givenDetail(note);

        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.detail(
                50L,
                user("ROLE_SALES"),
                model
        );

        assertEquals(
                "creditnote/customer-credit-note-detail",
                view
        );
        assertSame(note, model.get("creditNote"));
        assertEquals(
                Boolean.FALSE,
                model.get("canRegisterCreditNote")
        );

        verify(detailService).getDetail(2L, 50L);
    }

    @Test
    void registeredNoteCannotBeRegisteredAgain() {
        CreditNote note = note("REGISTERED");
        givenDetail(note);

        ExtendedModelMap model = new ExtendedModelMap();

        controller.detail(
                50L,
                user("ROLE_ADMIN"),
                model
        );

        assertEquals(
                Boolean.FALSE,
                model.get("canRegisterCreditNote")
        );
    }

    @Test
    void isolationErrorPropagatesWithoutRendering() {
        when(detailService.getDetail(2L, 50L))
                .thenThrow(
                        new NotFoundException(
                                "Nota di credito cliente non trovata."
                        )
                );

        ExtendedModelMap model = new ExtendedModelMap();

        assertThrows(
                NotFoundException.class,
                () -> controller.detail(
                        50L,
                        user("ROLE_ADMIN"),
                        model
                )
        );

        assertFalse(model.containsKey("creditNote"));
        assertFalse(model.containsKey("creditNoteLines"));
        assertFalse(model.containsKey("canRegisterCreditNote"));

        verify(detailService).getDetail(2L, 50L);
    }
}
