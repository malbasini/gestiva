package com.gestiva.accounting.entry.repository;

import com.gestiva.accounting.entry.entity.AccountingEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AccountingEntryRepository extends JpaRepository<AccountingEntry, Long>, JpaSpecificationExecutor<AccountingEntry> {

    Optional<AccountingEntry> findByTenantIdAndId(Long tenantId, Long id);
    List<AccountingEntry> findByTenantIdOrderByEntryDateDescIdDesc(Long tenantId);
    boolean existsByTenantIdAndEntryNumber(Long tenantId, String entryNumber);
}