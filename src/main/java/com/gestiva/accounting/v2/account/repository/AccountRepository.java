package com.gestiva.accounting.v2.account.repository;

import com.gestiva.accounting.v2.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {

    Optional<Account> findByTenantIdAndId(Long tenantId, Long id);

    Optional<Account> findByTenantIdAndCode(Long tenantId, String code);

    boolean existsByTenantIdAndCode(Long tenantId, String code);

    List<Account> findByTenantIdOrderByCodeAsc(Long tenantId);

    List<Account> findByTenantIdAndActiveTrueOrderByCodeAsc(Long tenantId);

    List<Account> findByTenantIdAndLeafAccountTrueAndActiveTrueOrderByCodeAsc(Long tenantId);

    List<Account> findByTenantId(Long tenantId);

    Optional<Account> findByTenantIdAndCompanyIdAndId(
            Long tenantId,
            Long companyId,
            Long id
    );

    Optional<Account> findByTenantIdAndCompanyIdAndCode(
            Long tenantId,
            Long companyId,
            String code
    );

    boolean existsByTenantIdAndCompanyIdAndCode(
            Long tenantId,
            Long companyId,
            String code
    );

    List<Account> findByTenantIdAndCompanyIdOrderByCodeAsc(
            Long tenantId,
            Long companyId
    );

    List<Account> findByTenantIdAndCompanyIdAndActiveTrueOrderByCodeAsc(
            Long tenantId,
            Long companyId
    );

    List<Account> findByTenantIdAndCompanyIdAndLeafAccountTrueAndActiveTrueOrderByCodeAsc(
            Long tenantId,
            Long companyId
    );








}
