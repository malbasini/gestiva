package com.gestiva.admin.users.controller;

import com.gestiva.admin.users.service.AdminUserService;
import com.gestiva.admin.users.web.AdminUserEditForm;
import com.gestiva.admin.users.web.AdminUserPageView;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.usercontext.TenantContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminUserProtectedAccountTest {

    private static final Long TENANT = 2L;
    private static final Long USER = 100L;

    private AdminUserService service;
    private AdminUserPageController controller;

    @BeforeEach
    void setup() {

        TenantContext tenantContext =
                mock(TenantContext.class);

        service = mock(AdminUserService.class);

        when(tenantContext.getCurrentTenantId())
                .thenReturn(TENANT);

        controller = new AdminUserPageController(
                tenantContext,
                service
        );

        AdminUserEditForm existing =
                new AdminUserEditForm();

        existing.setId(USER);
        existing.setAdmin(true);

        when(service.loadEditForm(TENANT, USER))
                .thenReturn(existing);

        when(service.buildPage(TENANT))
                .thenReturn(new AdminUserPageView());
    }

    @Test
    void directGetMustRejectEditingAdministrator() {

        assertThrows(
                BusinessException.class,
                () -> controller.edit(
                        USER,
                        new ExtendedModelMap()
                )
        );

        verify(service).loadEditForm(TENANT, USER);
        verify(service, never()).updateUser(
                anyLong(),
                any(AdminUserEditForm.class)
        );
    }

    @Test
    void directPostMustRejectEditingAdministrator() {

        AdminUserEditForm submitted =
                new AdminUserEditForm();

        submitted.setFirstName("Nome modificato");
        submitted.setLastName("Cognome modificato");
        submitted.setEmail("admin@example.invalid");
        submitted.setAdmin(false);
        submitted.setCompanyIds(
                java.util.List.of(10L)
        );

        var errors = new BeanPropertyBindingResult(
                submitted,
                "userForm"
        );

        assertThrows(
                BusinessException.class,
                () -> controller.update(
                        USER,
                        submitted,
                        errors,
                        new ExtendedModelMap(),
                        new RedirectAttributesModelMap()
                )
        );

        verify(service).loadEditForm(TENANT, USER);

        verify(service, never()).updateUser(
                anyLong(),
                any(AdminUserEditForm.class)
        );
    }
}
