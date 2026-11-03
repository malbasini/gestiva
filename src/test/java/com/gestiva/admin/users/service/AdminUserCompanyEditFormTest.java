package com.gestiva.admin.users.service;

import com.gestiva.admin.users.web.AdminUserEditForm;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.platform.company.repository.CompanyRepository;
import com.gestiva.security.company.entity.UserCompanyAccess;
import com.gestiva.security.company.repository.UserCompanyAccessRepository;
import com.gestiva.security.role.repository.RoleRepository;
import com.gestiva.security.role.repository.UserRoleRepository;
import com.gestiva.security.tenant.entity.AppUser;
import com.gestiva.security.tenant.repository.AppUserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminUserCompanyEditFormTest {

    private static final Long TENANT = 2L;
    private static final Long USER = 100L;

    private UserCompanyAccess access(
            Long tenantId,
            Long userId,
            Long companyId) {

        UserCompanyAccess access = new UserCompanyAccess();

        access.setTenantId(tenantId);
        access.setUserId(userId);
        access.setCompanyId(companyId);

        return access;
    }

    @Test
    void shouldPreloadAssignedCompaniesInEditForm() {

        AppUserRepository users =
                mock(AppUserRepository.class);

        UserCompanyAccessRepository accesses =
                mock(UserCompanyAccessRepository.class);

        AppUser user = mock(AppUser.class);

        when(user.getId()).thenReturn(USER);
        when(user.getStatus()).thenReturn("ACTIVE");

        when(users.findByTenantIdAndId(TENANT, USER))
                .thenReturn(Optional.of(user));

        when(accesses.findByTenantIdAndUserId(TENANT, USER))
                .thenReturn(List.of(
                        access(TENANT, USER, 20L),
                        access(TENANT, USER, 10L)
                ));

        AdminUserCompanyAssignmentService assignments =
                new AdminUserCompanyAssignmentService(
                        mock(CompanyRepository.class),
                        accesses
                );

        AdminUserService service = new AdminUserService(
                users,
                mock(UserRoleRepository.class),
                mock(RoleRepository.class),
                mock(PasswordEncoder.class),
                assignments,
                mock(CompanyRepository.class)
        );

        AdminUserEditForm form =
                service.loadEditForm(TENANT, USER);

        assertEquals(
                List.of(10L, 20L),
                form.getCompanyIds()
        );

        verify(accesses).findByTenantIdAndUserId(
                TENANT, USER
        );
    }

    @Test
    void shouldRejectForeignTenantAssociation() {

        UserCompanyAccessRepository accesses =
                mock(UserCompanyAccessRepository.class);

        when(accesses.findByTenantIdAndUserId(TENANT, USER))
                .thenReturn(List.of(
                        access(3L, USER, 99L)
                ));

        AdminUserCompanyAssignmentService assignments =
                new AdminUserCompanyAssignmentService(
                        mock(CompanyRepository.class),
                        accesses
                );

        assertThrows(
                BusinessException.class,
                () -> assignments.findAssignedCompanyIds(
                        TENANT, USER
                )
        );
    }

    @Test
    void shouldRejectAssociationOfAnotherUser() {

        UserCompanyAccessRepository accesses =
                mock(UserCompanyAccessRepository.class);

        when(accesses.findByTenantIdAndUserId(TENANT, USER))
                .thenReturn(List.of(
                        access(TENANT, 999L, 10L)
                ));

        AdminUserCompanyAssignmentService assignments =
                new AdminUserCompanyAssignmentService(
                        mock(CompanyRepository.class),
                        accesses
                );

        assertThrows(
                BusinessException.class,
                () -> assignments.findAssignedCompanyIds(
                        TENANT, USER
                )
        );
    }
}
