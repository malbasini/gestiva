package com.gestiva.admin.users.service;

import com.gestiva.admin.users.web.AdminUserEditForm;
import com.gestiva.admin.users.web.AdminUserPageView;
import com.gestiva.admin.users.web.AdminUserRowView;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.role.entity.Role;
import com.gestiva.security.role.repository.RoleRepository;
import com.gestiva.security.tenant.entity.AppUser;
import com.gestiva.security.tenant.repository.AppUserRepository;
import com.gestiva.security.role.entity.UserRole;
import com.gestiva.security.role.repository.UserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminUserService {

    public static final List<String> AVAILABLE_ROLES = List.of(
            "ROLE_SALES",
            "ROLE_PURCHASING",
            "ROLE_WAREHOUSE",
            "ROLE_ACCOUNTING"
    );
    private final AppUserRepository appUserRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(AppUserRepository appUserRepository,
                            UserRoleRepository userRoleRepository,
                            RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public AdminUserPageView buildPage(Long tenantId) {
        AdminUserPageView page = new AdminUserPageView();
        page.setAvailableRoles(AVAILABLE_ROLES);

        List<AppUser> users = appUserRepository.findByTenantIdOrderByLastNameAscFirstNameAsc(tenantId);

        Map<Long, List<UserRole>> rolesByUserId = userRoleRepository.findByTenantId(tenantId).stream()
                .collect(Collectors.groupingBy(UserRole::getUserId));

        Map<Long, String> roleNameById = roleRepository.findAll().stream()
                .collect(Collectors.toMap(Role::getId, Role::getName));

        List<AdminUserRowView> rows = users.stream().map(user -> {
            AdminUserRowView row = new AdminUserRowView();
            row.setId(user.getId());
            row.setFullName(buildFullName(user.getFirstName(), user.getLastName()));
            row.setEmail(user.getEmail());
            row.setActive("ACTIVE".equalsIgnoreCase(user.getStatus()));
            row.setAdmin(user.isAdmin());

            List<String> roleNames = rolesByUserId.getOrDefault(user.getId(), List.of()).stream()
                    .map(UserRole::getRoleId)
                    .map(roleNameById::get)
                    .filter(Objects::nonNull)
                    .sorted()
                    .toList();

            row.setRoles(roleNames);
            if(roleNames.contains("Amministratore"))
                row.setAdmin(true);
            return row;
        }).toList();

        page.setUsers(rows);
        return page;
    }

    public void createUser(Long tenantId, AdminUserEditForm form) {
        String email = normalizeEmail(form.getEmail());

        if (appUserRepository.existsByTenantIdAndEmail(tenantId, email)) {
            throw new BusinessException("Esiste già un utente con questa email.");
        }

        if (form.getPassword() == null || form.getPassword().isBlank()) {
            throw new BusinessException("La password è obbligatoria per il nuovo utente.");
        }

        AppUser user = new AppUser();
        user.setTenantId(tenantId);
        user.setFirstName(trimToNull(form.getFirstName()));
        user.setLastName(trimToNull(form.getLastName()));
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setStatus(form.isActive() ? "ACTIVE" : "DISABLED");
        user.setAdmin(form.isAdmin());
        user.setLocaleCode("it");

        AppUser saved = appUserRepository.save(user);

        saveUserRoles(tenantId, saved.getId(), form.getRoles(), form.isAdmin());
    }

    public void updateUser(Long tenantId, AdminUserEditForm form) {
        AppUser user = appUserRepository.findByTenantIdAndId(tenantId, form.getId())
                .orElseThrow(() -> new BusinessException("Utente non trovato."));

        user.setFirstName(trimToNull(form.getFirstName()));
        user.setLastName(trimToNull(form.getLastName()));
        user.setEmail(normalizeEmail(form.getEmail()));
        user.setStatus(form.isActive() ? "ACTIVE" : "DISABLED");
        user.setAdmin(form.isAdmin());

        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        }

        appUserRepository.save(user);

        List<UserRole> roleNames = userRoleRepository.findByTenantIdAndUserId(tenantId,user.getId());
        for(UserRole ur:roleNames){
            userRoleRepository.deleteByTenantIdAndUserId(tenantId, ur.getUserId());
        }
        userRoleRepository.deleteByTenantIdAndUserId(tenantId, user.getId());
        saveUserRoles(tenantId, user.getId(), form.getRoles(), form.isAdmin());
    }

    @Transactional(readOnly = true)
    public AdminUserEditForm loadEditForm(Long tenantId, Long userId) {
        AppUser user = appUserRepository.findByTenantIdAndId(tenantId, userId)
                .orElseThrow(() -> new BusinessException("Utente non trovato."));

        Map<Long, String> roleNameById = roleRepository.findAll().stream()
                .collect(Collectors.toMap(Role::getId, Role::getName));

        List<String> roles = userRoleRepository.findByTenantIdAndUserId(tenantId, userId).stream()
                .map(UserRole::getRoleId)
                .map(roleNameById::get)
                .filter(Objects::nonNull)
                .sorted()
                .toList();

        AdminUserEditForm form = new AdminUserEditForm();
        form.setId(user.getId());
        form.setFirstName(user.getFirstName());
        form.setLastName(user.getLastName());
        form.setEmail(user.getEmail());
        form.setActive("ACTIVE".equalsIgnoreCase(user.getStatus()));
        form.setAdmin(user.isAdmin());
        form.setRoles(new ArrayList<>(roles));

        return form;
    }

    private void saveUserRoles(Long tenantId, Long userId, List<String> roles, boolean adminFlag) {
        Set<String> normalizedRoles = new LinkedHashSet<>();
        if (roles != null) {
            normalizedRoles.addAll(
                    roles.stream()
                            .filter(AVAILABLE_ROLES::contains)
                            .collect(Collectors.toSet())
            );
        }

        if (adminFlag) {
            normalizedRoles.add("ROLE_ADMIN");
        }

        List<Role> roleEntities = roleRepository.findByNameInOrderByNameAsc(normalizedRoles);

        for (Role role : roleEntities) {
            UserRole ur = new UserRole();
            ur.setTenantId(tenantId);
            ur.setUserId(userId);
            ur.setRoleId(role.getId());
            userRoleRepository.save(ur);
        }
    }

    private String buildFullName(String firstName, String lastName) {
        String f = firstName == null ? "" : firstName.trim();
        String l = lastName == null ? "" : lastName.trim();
        return (f + " " + l).trim();
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("L'email è obbligatoria.");
        }
        return email.trim().toLowerCase();
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }
}