package com.gestiva.security.role.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.role.entity.Role;
import com.gestiva.security.role.repository.RoleRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class StandardRoleBootstrapService {

    private static final Map<String, String> STANDARD_ROLES =
            standardRoles();

    private final RoleRepository roleRepository;

    public StandardRoleBootstrapService(
            RoleRepository roleRepository
    ) {
        this.roleRepository = roleRepository;
    }

    @Transactional
    public void initializeForTenant(Long tenantId) {

        if (tenantId == null || tenantId <= 0) {
            throw new BusinessException(
                    "Tenant non valido per inizializzazione ruoli."
            );
        }

        // FASE 1: verifica completa, senza scritture.
        // Se un ruolo è incoerente, interrompiamo tutto
        // prima di creare qualsiasi nuovo ruolo.
        Map<String, String> missingRoles = new LinkedHashMap<>();

        for (var entry : STANDARD_ROLES.entrySet()) {

            String code = entry.getKey();
            String name = entry.getValue();

            var existing = roleRepository
                    .findByTenantIdAndCode(tenantId, code);

            if (existing.isPresent()) {

                Role role = existing.get();

                if (!tenantId.equals(role.getTenantId())
                        || !code.equals(role.getCode())) {
                    throw new BusinessException(
                            "Ruolo non coerente con il tenant."
                    );
                }

                // Conserviamo integralmente il ruolo esistente.
                continue;
            }

            missingRoles.put(code, name);
        }

        // FASE 2: scritture consentite solo dopo
        // il completamento di tutte le verifiche.
        for (var entry : missingRoles.entrySet()) {

            Role role = new Role();
            role.setTenantId(tenantId);
            role.setCode(entry.getKey());
            role.setName(entry.getValue());
            role.setSystemRole(true);

            roleRepository.save(role);
        }
    }

    private static Map<String, String> standardRoles() {

        Map<String, String> roles = new LinkedHashMap<>();

        roles.put("ROLE_ADMIN", "Amministratore");
        roles.put("ROLE_SALES", "Vendite");
        roles.put("ROLE_PURCHASING", "Acquisti");
        roles.put("ROLE_WAREHOUSE", "Magazzino");
        roles.put("ROLE_ACCOUNTING", "Contabilità");

        return Map.copyOf(roles);
    }
}
