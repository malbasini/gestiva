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
}
