package com.gestiva.admin.users.controller;

import com.gestiva.admin.users.service.AdminUserService;
import com.gestiva.admin.users.web.AdminUserEditForm;
import com.gestiva.admin.users.web.AdminUserPageView;
import com.gestiva.security.usercontext.TenantContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminUserCompanyHttpBindingTest {

    private static final Long TENANT_ID = 2L;
    private static final Long USER_ID = 100L;

    private AdminUserService service;
    private MockMvc mvc;

    @BeforeEach
    void setup() {

        TenantContext tenantContext = mock(TenantContext.class);
        service = mock(AdminUserService.class);

        when(tenantContext.getCurrentTenantId())
                .thenReturn(TENANT_ID);

        AdminUserPageController controller =
                new AdminUserPageController(
                        tenantContext,
                        service
                );

        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void createShouldBindMultipleCompaniesAndRoles()
            throws Exception {

        mvc.perform(post("/admin/users/create")
                        .param("firstName", "Mario")
                        .param("lastName", "Rossi")
                        .param("email", "mario@example.invalid")
                        .param("password", "Password123!")
                        .param("active", "true")
                        .param("companyIds", "10", "20")
                        .param(
                                "roles",
                                "ROLE_SALES",
                                "ROLE_WAREHOUSE"
                        ))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        ArgumentCaptor<AdminUserEditForm> captor =
                ArgumentCaptor.forClass(
                        AdminUserEditForm.class
                );

        verify(service).createUser(
                eq(TENANT_ID),
                captor.capture()
        );

        AdminUserEditForm form = captor.getValue();

        assertEquals(
                List.of(10L, 20L),
                form.getCompanyIds()
        );

        assertEquals(
                List.of("ROLE_SALES", "ROLE_WAREHOUSE"),
                form.getRoles()
        );

        assertFalse(form.isAdmin());
    }

    @Test
    void updateShouldBindCompaniesAndRoles()
            throws Exception {

        AdminUserEditForm existing = new AdminUserEditForm();
        existing.setId(USER_ID);
        existing.setAdmin(false);

        when(service.loadEditForm(TENANT_ID, USER_ID))
                .thenReturn(existing);

        mvc.perform(post("/admin/users/{id}/edit", USER_ID)
                        .param("firstName", "Mario")
                        .param("lastName", "Aggiornato")
                        .param("email", "mario@example.invalid")
                        .param("password", "")
                        .param("active", "true")
                        .param("companyIds", "20", "30")
                        .param(
                                "roles",
                                "ROLE_PURCHASING",
                                "ROLE_ACCOUNTING"
                        ))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        ArgumentCaptor<AdminUserEditForm> captor =
                ArgumentCaptor.forClass(
                        AdminUserEditForm.class
                );

        verify(service).updateUser(
                eq(TENANT_ID),
                captor.capture()
        );

        AdminUserEditForm form = captor.getValue();

        assertEquals(USER_ID, form.getId());

        assertEquals(
                List.of(20L, 30L),
                form.getCompanyIds()
        );

        assertEquals(
                List.of(
                        "ROLE_PURCHASING",
                        "ROLE_ACCOUNTING"
                ),
                form.getRoles()
        );
    }

    @Test
    void editPageShouldExposeAssignedCompanies()
            throws Exception {

        AdminUserEditForm form =
                new AdminUserEditForm();

        form.setId(USER_ID);
        form.setCompanyIds(List.of(10L, 20L));
        form.setRoles(List.of("ROLE_SALES"));

        when(service.loadEditForm(TENANT_ID, USER_ID))
                .thenReturn(form);

        when(service.buildPage(TENANT_ID))
                .thenReturn(new AdminUserPageView());

        mvc.perform(get("/admin/users/{id}/edit", USER_ID))
                .andExpect(status().isOk())
                .andExpect(view().name(
                        "admin/users/user-list"
                ))
                .andExpect(model().attribute(
                        "editMode", true
                ))
                .andExpect(model().attribute(
                        "userForm", form
                ));

        verify(service).loadEditForm(
                TENANT_ID, USER_ID
        );

        assertEquals(
                List.of(10L, 20L),
                form.getCompanyIds()
        );
    }
}
