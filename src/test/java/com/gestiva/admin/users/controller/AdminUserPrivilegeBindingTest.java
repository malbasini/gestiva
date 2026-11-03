package com.gestiva.admin.users.controller;

import com.gestiva.admin.users.service.AdminUserService;
import com.gestiva.admin.users.web.AdminUserEditForm;
import com.gestiva.security.usercontext.TenantContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminUserPrivilegeBindingTest {

    private static final Long TENANT = 2L;
    private static final Long USER = 100L;

    private AdminUserService service;
    private MockMvc mvc;

    @BeforeEach
    void setup() {

        TenantContext tenantContext = mock(TenantContext.class);
        service = mock(AdminUserService.class);

        when(tenantContext.getCurrentTenantId())
                .thenReturn(TENANT);

        mvc = MockMvcBuilders.standaloneSetup(
                new AdminUserPageController(
                        tenantContext,
                        service
                )
        ).build();
    }

    @Test
    void createMustIgnoreForgedAdminParameter() throws Exception {

        mvc.perform(post("/admin/users/create")
                        .param("firstName", "Mario")
                        .param("lastName", "Rossi")
                        .param("email", "mario@example.invalid")
                        .param("password", "Password123!")
                        .param("admin", "true")
                        .param("companyIds", "10")
                        .param("roles", "ROLE_SALES"))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<AdminUserEditForm> captor =
                ArgumentCaptor.forClass(AdminUserEditForm.class);

        verify(service).createUser(
                eq(TENANT), captor.capture()
        );

        assertFalse(
                captor.getValue().isAdmin(),
                "Il parametro HTTP admin=true non deve "
                + "promuovere il nuovo utente"
        );
    }

    @Test
    void updateMustRejectExistingAdminWithoutFormField()
            throws Exception {

        AdminUserEditForm existing = new AdminUserEditForm();
        existing.setId(USER);
        existing.setAdmin(true);

        when(service.loadEditForm(TENANT, USER))
                .thenReturn(existing);

        Exception error = assertThrows(
                Exception.class,
                () -> mvc.perform(
                        post("/admin/users/{id}/edit", USER)
                                .param("firstName", "Amministratore")
                                .param("lastName", "Esistente")
                                .param("email", "admin@example.invalid")
                                .param("companyIds", "10")
                                .param("roles", "ROLE_SALES")
                )
        );

        assertTrue(
                error.getCause() instanceof
                        com.gestiva.common.exception.BusinessException,
                "Attesa BusinessException dal controller"
        );

        verify(service).loadEditForm(TENANT, USER);

        verify(service, never()).updateUser(
                anyLong(),
                any(AdminUserEditForm.class)
        );
    }

    @Test
    void updateMustIgnoreForgedAdminPromotion()
            throws Exception {

        AdminUserEditForm existing = new AdminUserEditForm();
        existing.setId(USER);
        existing.setAdmin(false);

        when(service.loadEditForm(TENANT, USER))
                .thenReturn(existing);

        mvc.perform(post("/admin/users/{id}/edit", USER)
                        .param("firstName", "Utente")
                        .param("lastName", "Ordinario")
                        .param("email", "utente@example.invalid")
                        .param("admin", "true")
                        .param("companyIds", "10")
                        .param("roles", "ROLE_WAREHOUSE"))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<AdminUserEditForm> captor =
                ArgumentCaptor.forClass(AdminUserEditForm.class);

        verify(service).updateUser(
                eq(TENANT), captor.capture()
        );

        assertFalse(
                captor.getValue().isAdmin(),
                "Un parametro manipolato non deve "
                + "promuovere un utente ordinario"
        );
    }
}
