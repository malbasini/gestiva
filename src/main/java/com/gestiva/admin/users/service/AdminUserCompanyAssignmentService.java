package com.gestiva.admin.users.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.platform.company.entity.Company;
import com.gestiva.platform.company.repository.CompanyRepository;
import com.gestiva.security.company.entity.UserCompanyAccess;
import com.gestiva.security.company.repository.UserCompanyAccessRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminUserCompanyAssignmentService {

    private final CompanyRepository companyRepository;
    private final UserCompanyAccessRepository accessRepository;

    public AdminUserCompanyAssignmentService(
            CompanyRepository companyRepository,
            UserCompanyAccessRepository accessRepository) {

        this.companyRepository = companyRepository;
        this.accessRepository = accessRepository;
    }

    @Transactional(readOnly = true)
    public List<Long> findAssignedCompanyIds(
            Long tenantId,
            Long userId) {

        if (tenantId == null || tenantId <= 0
                || userId == null || userId <= 0) {
            throw new BusinessException(
                    "Tenant o utente non valido."
            );
        }

        List<UserCompanyAccess> accesses = accessRepository
                .findByTenantIdAndUserId(tenantId, userId);

        for (UserCompanyAccess access : accesses) {
            if (!tenantId.equals(access.getTenantId())
                    || !userId.equals(access.getUserId())) {
                throw new BusinessException(
                        "Associazione società incoerente."
                );
            }
        }

        return accesses.stream()
                .map(UserCompanyAccess::getCompanyId)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }

    @Transactional
    public void replaceAccess(
            Long tenantId,
            Long userId,
            List<Long> requestedCompanyIds,
            boolean admin) {

        if (tenantId == null || tenantId <= 0
                || userId == null || userId <= 0) {
            throw new BusinessException(
                    "Tenant o utente non valido."
            );
        }

        if (requestedCompanyIds != null
                && requestedCompanyIds.stream()
                        .anyMatch(java.util.Objects::isNull)) {
            throw new BusinessException(
                    "Selezione società non valida."
            );
        }

        Set<Long> selected = requestedCompanyIds == null
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(requestedCompanyIds);

        // Un utente ordinario deve avere almeno una
        // società esplicitamente autorizzata.
        if (!admin && selected.isEmpty()) {
            throw new BusinessException(
                    "Selezionare almeno una società per l'utente."
            );
        }

        // Prima validiamo TUTTE le società.
        // Nessuna scrittura finché non abbiamo concluso.
        for (Long companyId : selected) {

            Company company = companyRepository
                    .findByTenantIdAndId(tenantId, companyId)
                    .orElseThrow(() -> new BusinessException(
                            "Società non disponibile per il tenant."
                    ));

            if (!tenantId.equals(company.getTenantId())
                    || !companyId.equals(company.getId())
                    || !company.isActive()) {

                throw new BusinessException(
                        "Società non autorizzata o non attiva."
                );
            }
        }

        // Lettura delle associazioni del solo utente corrente.
        List<UserCompanyAccess> existing = accessRepository
                .findByTenantIdAndUserId(tenantId, userId);

        // Verifica difensiva anche delle associazioni lette.
        for (UserCompanyAccess access : existing) {
            if (!tenantId.equals(access.getTenantId())
                    || !userId.equals(access.getUserId())) {
                throw new BusinessException(
                        "Associazione società incoerente con il tenant."
                );
            }
        }

        Set<Long> existingIds = existing.stream()
                .map(UserCompanyAccess::getCompanyId)
                .collect(Collectors.toSet());

        // Le società attive deselezionate vengono revocate.
        // Le società inattive, non presenti nelle checkbox,
        // conservano invece le autorizzazioni preesistenti.
        List<UserCompanyAccess> toDelete = new ArrayList<>();

        for (UserCompanyAccess access : existing) {

            Long companyId = access.getCompanyId();

            if (selected.contains(companyId)) {
                continue;
            }

            // Recuperiamo la società nel perimetro del tenant.
            var existingCompany = companyId == null
                    ? java.util.Optional
                            .<com.gestiva.platform.company.entity.Company>empty()
                    : companyRepository.findByTenantIdAndId(
                            tenantId, companyId
                    );

            if (existingCompany.isPresent()) {

                Company company = existingCompany.get();

                // Solo una società realmente inattiva,
                // con identificativi coerenti, è preservata.
                if (tenantId.equals(company.getTenantId())
                        && companyId.equals(company.getId())
                        && !company.isActive()) {
                    continue;
                }
            }

            // Società attiva deselezionata oppure
            // associazione non più riconducibile a una
            // società valida del tenant.
            toDelete.add(access);
        }

        List<UserCompanyAccess> toCreate = new ArrayList<>();

        for (Long companyId : selected) {
            if (existingIds.contains(companyId)) {
                continue;
            }

            UserCompanyAccess access = new UserCompanyAccess();
            access.setTenantId(tenantId);
            access.setUserId(userId);
            access.setCompanyId(companyId);

            toCreate.add(access);
        }

        if (!toDelete.isEmpty()) {
            accessRepository.deleteAll(toDelete);
        }

        for (UserCompanyAccess access : toCreate) {
            accessRepository.save(access);
        }
    }
}
