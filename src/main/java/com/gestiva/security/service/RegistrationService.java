package com.gestiva.security.service;

import com.gestiva.accounting.v2.account.entity.Account;
import com.gestiva.accounting.v2.account.repository.AccountRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.role.entity.Role;
import com.gestiva.security.role.entity.UserRole;
import com.gestiva.security.role.repository.RoleRepository;
import com.gestiva.security.role.repository.UserRoleRepository;
import com.gestiva.security.tenant.entity.AppUser;
import com.gestiva.security.tenant.entity.Tenant;
import com.gestiva.security.tenant.repository.AppUserRepository;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.security.web.RegistrationForm;
import com.gestiva.security.web.RegistrationResult;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.List;
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
    private final AccountRepository accountRepository;




    public RegistrationService(TenantRepository tenantRepository,
                               AppUserRepository appUserRepository,
                               RoleRepository roleRepository,
                               UserRoleRepository userRoleRepository,
                               PasswordEncoder passwordEncoder,
                               AccountRepository accountRepository) {

        this.tenantRepository = tenantRepository;
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
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
    @Transactional
    public void registerAccount(Long tenantId){
        if(!accountRepository.findByTenantId(tenantId).isEmpty())
            return;
        List<Account> accounts = accountRepository.findByTenantIdOrderByCodeAsc(0L);
        if(!accounts.isEmpty()){
            for (Account account : accounts) {
                Account a = new Account();
                a.setTenantId(tenantId);
                a.setCode(account.getCode());
                a.setName(account.getName());
                a.setAccountType(account.getAccountType());
                a.setNature(account.getNature());
                a.setLevelNo(account.getLevelNo());
                a.setParentId(account.getParentId());
                a.setLeafAccount(account.isLeafAccount());
                a.setSystemAccount(account.isSystemAccount());
                a.setActive(account.isActive());
                a.setDescription(account.getDescription());
                accountRepository.save(a);
            }
        }
    }
}
