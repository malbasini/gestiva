package com.gestiva.security.auth;

import com.gestiva.security.permission.repository.PermissionRepository;
import com.gestiva.security.role.repository.RolePermissionRepository;
import com.gestiva.security.role.repository.RoleRepository;
import com.gestiva.security.role.repository.UserRoleRepository;
import com.gestiva.security.tenant.repository.AppUserRepository;
import com.gestiva.security.tenant.repository.TenantRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final TenantRepository tenantRepository;
    private final AppUserRepository appUserRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    public CustomUserDetailsService(TenantRepository tenantRepository,
                                    AppUserRepository appUserRepository,
                                    UserRoleRepository userRoleRepository,
                                    RoleRepository roleRepository,
                                    RolePermissionRepository rolePermissionRepository,
                                    PermissionRepository permissionRepository) {
        this.tenantRepository = tenantRepository;
        this.appUserRepository = appUserRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LoginIdentifier loginIdentifier = LoginIdentifier.parse(username);

        var tenant = tenantRepository.findBySlug(loginIdentifier.tenantSlug())
                .orElseThrow(() -> new UsernameNotFoundException("Tenant non trovato"));

        var user = appUserRepository.findByTenantIdAndEmail(tenant.getId(), loginIdentifier.email())
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new UsernameNotFoundException("Utente non attivo");
        }

        var userRoles = userRoleRepository.findByTenantIdAndUserId(tenant.getId(), user.getId());

        Set<GrantedAuthority> authorities = new LinkedHashSet<>();

        var roleIds = userRoles.stream()
                .map(ur -> ur.getRoleId())
                .collect(Collectors.toSet());

        if (!roleIds.isEmpty()) {
            var roles = roleRepository.findAllById(roleIds);

            for (var role : roles) {
                authorities.add(new SimpleGrantedAuthority(role.getCode()));

                var rolePermissions = rolePermissionRepository.findByTenantIdAndRoleId(tenant.getId(), role.getId());
                var permissionIds = rolePermissions.stream()
                        .map(rp -> rp.getPermissionId())
                        .collect(Collectors.toSet());

                if (!permissionIds.isEmpty()) {
                    var permissions = permissionRepository.findAllById(permissionIds);
                    for (var permission : permissions) {
                        authorities.add(new SimpleGrantedAuthority(permission.getCode()));
                    }
                }
            }
        }

        if (user.isAdmin()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return new AuthenticatedUser(
                user.getId(),
                tenant.getId(),
                tenant.getSlug(),
                user.getEmail(),
                user.getPasswordHash(),
                "ACTIVE".equalsIgnoreCase(user.getStatus()),
                authorities
        );
    }

    private record LoginIdentifier(String tenantSlug, String email) {

        static LoginIdentifier parse(String raw) {
            if (raw == null || raw.isBlank() || !raw.contains("|")) {
                throw new UsernameNotFoundException("Credenziali non valide");
            }

            String[] parts = raw.split("\\|", 2);
            String tenantSlug = parts[0].trim().toLowerCase(Locale.ROOT);
            String email = parts[1].trim().toLowerCase(Locale.ROOT);

            if (tenantSlug.isBlank() || email.isBlank()) {
                throw new UsernameNotFoundException("Credenziali non valide");
            }

            return new LoginIdentifier(tenantSlug, email);
        }
    }
}