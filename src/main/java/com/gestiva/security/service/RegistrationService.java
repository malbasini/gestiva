package com.gestiva.security.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.platform.role.entity.Role;
import com.gestiva.platform.role.entity.UserRole;
import com.gestiva.platform.role.repository.RoleRepository;
import com.gestiva.platform.role.repository.UserRoleRepository;
import com.gestiva.platform.tenant.entity.AppUser;
import com.gestiva.platform.tenant.entity.Tenant;
import com.gestiva.platform.tenant.repository.AppUserRepository;
import com.gestiva.platform.tenant.repository.TenantRepository;
import com.gestiva.security.web.RegistrationForm;
import com.gestiva.security.web.RegistrationResult;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.Locale;

@Service
@Transactional
public class RegistrationService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final TenantRepository tenantRepository;
    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(TenantRepository tenantRepository,
                               AppUserRepository appUserRepository,
                               RoleRepository roleRepository,
                               UserRoleRepository userRoleRepository,
                               PasswordEncoder passwordEncoder) {
        this.tenantRepository = tenantRepository;
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public RegistrationResult register(RegistrationForm form) {
        validateForm(form);

        String slug = generateUniqueTenantSlug(form.getTenantName());

        OffsetDateTime now = OffsetDateTime.now();

        Tenant tenant = new Tenant();
        tenant.setName(form.getTenantName().trim());
        tenant.setSlug(slug);
        tenant.setEmail(form.getTenantEmail().trim().toLowerCase(Locale.ROOT));
        tenant.setStatus("ACTIVE");
        tenant.setDefaultLocale("it");
        tenant.setDefaultCurrency("EUR");
        tenant.setCreatedAt(now.toLocalDateTime());
        tenant.setUpdatedAt(now.toLocalDateTime());

        Tenant savedTenant = tenantRepository.save(tenant);

        AppUser user = new AppUser();
        user.setTenantId(savedTenant.getId());
        user.setFirstName(form.getFirstName().trim());
        user.setLastName(form.getLastName().trim());
        user.setEmail(form.getUserEmail().trim().toLowerCase(Locale.ROOT));
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setStatus("ACTIVE");
        user.setAdmin(true);
        user.setLocaleCode("it");
        user.setCreatedAt(now.toLocalDateTime());
        user.setUpdatedAt(now.toLocalDateTime());

        if (appUserRepository.existsByTenantIdAndEmail(savedTenant.getId(), user.getEmail())) {
            throw new BusinessException("Esiste già un utente con questa email per il tenant.");
        }

        AppUser savedUser = appUserRepository.save(user);

        Role adminRole = roleRepository.findByTenantIdAndCode(savedTenant.getId(), ROLE_ADMIN)
                .orElseGet(() -> createAdminRole(savedTenant.getId()));

        if (!userRoleRepository.existsByTenantIdAndUserIdAndRoleId(savedTenant.getId(), savedUser.getId(), adminRole.getId())) {
            UserRole userRole = new UserRole();
            userRole.setTenantId(savedTenant.getId());
            userRole.setUserId(savedUser.getId());
            userRole.setRoleId(adminRole.getId());
            userRole.setCreatedAt(now.toLocalDateTime());
            userRole.setUpdatedAt(now.toLocalDateTime());
            userRoleRepository.save(userRole);
        }

        RegistrationResult result = new RegistrationResult();
        result.setTenantId(savedTenant.getId());
        result.setUserId(savedUser.getId());
        result.setTenantSlug(savedTenant.getSlug());
        return result;
    }

    private void validateForm(RegistrationForm form) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new BusinessException("Le password non coincidono.");
        }

        if (tenantRepository.existsByEmail(form.getTenantEmail().trim().toLowerCase(Locale.ROOT))) {
            throw new BusinessException("Esiste già un tenant con questa email.");
        }
    }

    private Role createAdminRole(Long tenantId) {
        OffsetDateTime now = OffsetDateTime.now();

        Role role = new Role();
        role.setTenantId(tenantId);
        role.setCode(ROLE_ADMIN);
        role.setName("Amministratore");
        role.setSystemRole(true);
        role.setCreatedAt(now.toLocalDateTime());
        role.setUpdatedAt(now.toLocalDateTime());

        return roleRepository.save(role);
    }

    private String generateUniqueTenantSlug(String tenantName) {
        String base = slugify(tenantName);
        String candidate = base;
        int counter = 2;

        while (tenantRepository.existsBySlug(candidate)) {
            candidate = base + "-" + counter;
            counter++;
        }

        return candidate;
    }

    private String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        String slug = normalized
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");

        if (slug.isBlank()) {
            return "tenant";
        }

        return slug;
    }
}
