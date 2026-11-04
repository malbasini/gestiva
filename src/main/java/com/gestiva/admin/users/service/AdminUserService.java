package com.gestiva.admin.users.service;

import com.gestiva.admin.users.web.AdminUserEditForm;
import com.gestiva.admin.users.web.AdminUserCompanyOptionView;
import com.gestiva.platform.company.repository.CompanyRepository;
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
    private final CompanyRepository companyRepository;
    private final AdminUserCompanyAssignmentService companyAssignmentService;

    public AdminUserService(AppUserRepository appUserRepository,
                            UserRoleRepository userRoleRepository,
                            RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder,
                            AdminUserCompanyAssignmentService companyAssignmentService,
                            CompanyRepository companyRepository) {
        this.appUserRepository = appUserRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.companyRepository = java.util.Objects.requireNonNull(
                companyRepository,
                "CompanyRepository obbligatorio"
        );
        this.companyAssignmentService = java.util.Objects.requireNonNull(
                companyAssignmentService,
                "Company assignment service obbligatorio"
        );
    }

    @Transactional(readOnly = true)
    public AdminUserPageView buildPage(Long tenantId) {
        AdminUserPageView page = new AdminUserPageView();
        page.setAvailableRoles(AVAILABLE_ROLES);

        // Recuperiamo soltanto le società attive del tenant.
        // La seconda verifica protegge anche da risultati
        // incoerenti restituiti dal livello di persistenza.
        var availableCompanies = companyRepository
                .findByTenantIdAndActiveTrueOrderByLegalNameAsc(tenantId)
                .stream()
                .filter(company ->
                        tenantId.equals(company.getTenantId())
                        && company.isActive()
                )
                .map(company -> new AdminUserCompanyOptionView(
                        company.getId(),
                        company.getLegalName()
                ))
                .toList();

        page.setAvailableCompanies(availableCompanies);

        List<AppUser> users = appUserRepository.findByTenantIdOrderByLastNameAscFirstNameAsc(tenantId);

        Map<Long, List<UserRole>> rolesByUserId = userRoleRepository.findByTenantId(tenantId).stream()
                .collect(Collectors.groupingBy(UserRole::getUserId));

        // Recupera esclusivamente i ruoli del tenant corrente.
        Map<Long, Role> roleById = roleRepository
                .findByTenantId(tenantId)
                .stream()
                // Protezione aggiuntiva contro ruoli di altri tenant.
                .filter(role -> tenantId.equals(role.getTenantId()))
                .collect(Collectors.toMap(
                        Role::getId,
                        role -> role
                ));

        List<AdminUserRowView> rows = users.stream().map(user -> {
            AdminUserRowView row = new AdminUserRowView();
            row.setId(user.getId());
            row.setFullName(buildFullName(user.getFirstName(), user.getLastName()));
            row.setEmail(user.getEmail());
            row.setActive("ACTIVE".equalsIgnoreCase(user.getStatus()));
            row.setAdmin(user.isAdmin());

            // Le associazioni sono tenant-scoped.
            // Anche le entita Role sono filtrate per tenant.
            List<Role> assignedRoles = rolesByUserId
                    .getOrDefault(user.getId(), List.of())
                    .stream()
                    .map(UserRole::getRoleId)
                    .map(roleById::get)
                    .filter(Objects::nonNull)
                    .toList();

            // Nell'interfaccia mostriamo i nomi descrittivi.
            List<String> roleNames = assignedRoles.stream()
                    .map(Role::getName)
                    .filter(Objects::nonNull)
                    .sorted()
                    .toList();

            row.setRoles(roleNames);

            // I privilegi amministrativi dipendono dal codice
            // tecnico, mai dal nome descrittivo del ruolo.
            boolean hasAdminRole = assignedRoles.stream()
                    .anyMatch(role ->
                            "ROLE_ADMIN".equals(role.getCode())
                    );

            row.setAdmin(user.isAdmin() || hasAdminRole);
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

        companyAssignmentService.replaceAccess(
                tenantId,
                saved.getId(),
                form.getCompanyIds(),
                form.isAdmin()
        );
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

        // Sincronizza soltanto i ruoli effettivamente cambiati.
        // Le associazioni confermate rimangono inalterate.
        syncUserRoles(
                tenantId,
                user.getId(),
                form.getRoles(),
                form.isAdmin()
        );

        companyAssignmentService.replaceAccess(
                tenantId,
                user.getId(),
                form.getCompanyIds(),
                form.isAdmin()
        );
    }

    @Transactional(readOnly = true)
    public AdminUserEditForm loadEditForm(Long tenantId, Long userId) {
        AppUser user = appUserRepository.findByTenantIdAndId(tenantId, userId)
                .orElseThrow(() -> new BusinessException("Utente non trovato."));

        Map<Long, String> roleCodeById = roleRepository
                .findByTenantId(tenantId)
                .stream()
                .collect(Collectors.toMap(
                        Role::getId,
                        Role::getCode
                ));

        List<String> roles = userRoleRepository.findByTenantIdAndUserId(tenantId, userId).stream()
                .map(UserRole::getRoleId)
                .map(roleCodeById::get)
                .filter(Objects::nonNull)
                .sorted()
                .toList();

        AdminUserEditForm form = new AdminUserEditForm();
        form.setId(user.getId());
        form.setFirstName(user.getFirstName());
        form.setLastName(user.getLastName());
        form.setEmail(user.getEmail());
        form.setActive("ACTIVE".equalsIgnoreCase(user.getStatus()));
        // Il form deve rappresentare i privilegi effettivi:
        // flag admin oppure associazione al ruolo ROLE_ADMIN.
        form.setAdmin(
                user.isAdmin() || roles.contains("ROLE_ADMIN")
        );
        form.setRoles(new ArrayList<>(roles));

        // Precarica le società già autorizzate.
        // Il setter del form gestisce anche un elenco nullo.
        form.setCompanyIds(
                companyAssignmentService.findAssignedCompanyIds(
                        tenantId,
                        userId
                )
        );

        return form;
    }

    private List<Role> resolveRequestedRoles(
            Long tenantId,
            List<String> roles,
            boolean adminFlag) {

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

        List<Role> roleEntities = roleRepository
                .findByTenantIdAndCodeInOrderByCodeAsc(
                        tenantId,
                        normalizedRoles
                );

        Set<String> foundCodes = new HashSet<>();

        for (Role role : roleEntities) {

            if (!tenantId.equals(role.getTenantId())) {
                throw new BusinessException(
                        "Ruolo non autorizzato per il tenant corrente."
                );
            }

            String code = role.getCode();

            if (code == null
                    || !normalizedRoles.contains(code)
                    || !foundCodes.add(code)
                    || role.getId() == null) {
                throw new BusinessException(
                        "Risultato non valido nella ricerca dei ruoli."
                );
            }
        }

        if (!foundCodes.equals(normalizedRoles)) {
            throw new BusinessException(
                    "Uno o più ruoli selezionati non sono disponibili "
                    + "per il tenant corrente."
            );
        }

        return roleEntities;
    }

    private void saveUserRoles(
            Long tenantId,
            Long userId,
            List<String> roles,
            boolean adminFlag) {

        // La creazione di un nuovo utente non ha ruoli precedenti.
        for (Role role :
                resolveRequestedRoles(tenantId, roles, adminFlag)) {

            UserRole association = new UserRole();
            association.setTenantId(tenantId);
            association.setUserId(userId);
            association.setRoleId(role.getId());

            userRoleRepository.save(association);
        }
    }

    private void syncUserRoles(
            Long tenantId,
            Long userId,
            List<String> roles,
            boolean adminFlag) {

        // Validiamo tutti i ruoli richiesti prima delle
        // operazioni di cancellazione e inserimento.
        List<Role> requested = resolveRequestedRoles(
                tenantId,
                roles,
                adminFlag
        );

        Set<Long> selectedIds = new HashSet<>();

        for (Role role : requested) {
            if (!selectedIds.add(role.getId())) {
                throw new BusinessException(
                        "Ruoli richiesti duplicati."
                );
            }
        }

        List<UserRole> existing = userRoleRepository
                .findByTenantIdAndUserId(tenantId, userId);

        Set<Long> existingIds = new HashSet<>();

        // Difesa contro associazioni incoerenti.
        for (UserRole association : existing) {

            if (!tenantId.equals(association.getTenantId())
                    || !userId.equals(association.getUserId())
                    || association.getRoleId() == null
                    || !existingIds.add(association.getRoleId())) {

                throw new BusinessException(
                        "Associazione ruolo incoerente."
                );
            }
        }

        List<UserRole> toDelete = new ArrayList<>();

        for (UserRole association : existing) {
            if (!selectedIds.contains(association.getRoleId())) {
                toDelete.add(association);
            }
        }

        List<Role> toCreate = new ArrayList<>();

        for (Role role : requested) {
            if (!existingIds.contains(role.getId())) {
                toCreate.add(role);
            }
        }

        // Nessuna cancellazione per i ruoli conservati.
        if (!toDelete.isEmpty()) {
            userRoleRepository.deleteAll(toDelete);
        }

        // Inseriamo soltanto associazioni realmente nuove.
        for (Role role : toCreate) {

            UserRole association = new UserRole();
            association.setTenantId(tenantId);
            association.setUserId(userId);
            association.setRoleId(role.getId());

            userRoleRepository.save(association);
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