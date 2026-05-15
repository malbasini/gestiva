package com.gestiva.accounting.entry.repository;

import com.gestiva.accounting.entry.entity.AccountingEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountingEntryRepository extends JpaRepository<AccountingEntry, Long>, JpaSpecificationExecutor<AccountingEntry> {

    Optional<AccountingEntry> findByTenantIdAndId(Long tenantId, Long id);
    List<AccountingEntry> findByTenantIdOrderByEntryDateDescIdDesc(Long tenantId);
    boolean existsByTenantIdAndEntryNumber(Long tenantId, String entryNumber);
    @Query("""
       select coalesce(sum(a.totalAmount), 0)
       from AccountingEntry a
       where a.tenantId = :tenantId
         and a.causalCode = :causalCode
         and a.entryDate between :startDate and :endDate
       """)
    java.math.BigDecimal sumTotalByCausalCodeAndPeriod(Long tenantId, String causalCode, java.time.LocalDate startDate, java.time.LocalDate endDate);
    java.util.List<AccountingEntry> findTop10ByTenantIdOrderByEntryDateDescIdDesc(Long tenantId);






    @Query("""
       select distinct a.causalCode
       from AccountingEntry a
       where a.tenantId = :tenantId
         and a.causalCode is not null
         order by a.causalCode
       """)
    List<String> findDistinctCausalCodesByTenantId(Long tenantId);
}