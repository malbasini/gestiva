package com.gestiva.accounting.v2.journal.repository;

import com.gestiva.accounting.v2.journal.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long>, JpaSpecificationExecutor<JournalEntry> {

    Optional<JournalEntry> findByTenantIdAndId(Long tenantId, Long id);
    Optional<JournalEntry> findByTenantIdAndEntryNumber(Long tenantId, String entryNumber);
    boolean existsByTenantIdAndEntryNumber(Long tenantId, String entryNumber);
    List<JournalEntry> findByTenantIdOrderByEntryDateDescIdDesc(Long tenantId);

    // Metodi multi-società Gestiva V2

    Optional<JournalEntry> findByTenantIdAndCompanyIdAndId(
            Long tenantId,
            Long companyId,
            Long id
    );

    Optional<JournalEntry> findByTenantIdAndCompanyIdAndEntryNumber(
            Long tenantId,
            Long companyId,
            String entryNumber
    );

    boolean existsByTenantIdAndCompanyIdAndEntryNumber(
            Long tenantId,
            Long companyId,
            String entryNumber
    );

    List<JournalEntry> findByTenantIdAndCompanyIdOrderByEntryDateDescIdDesc(
            Long tenantId,
            Long companyId
    );

    List<JournalEntry> findByTenantIdAndCompanyIdAndEntryDateBetweenOrderByEntryDateAscIdAsc(
            Long tenantId,
            Long companyId,
            java.time.LocalDate dateFrom,
            java.time.LocalDate dateTo
    );

    @Query("""
       select distinct j.causalCode
       from JournalEntry j
       where j.tenantId = :tenantId
         and j.causalCode is not null
       order by j.causalCode
       """)
    List<String> findDistinctCausalCodesByTenantId(Long tenantId);

    @Query("""
       select distinct j.causalCode
       from JournalEntry j
       where j.tenantId = :tenantId
         and j.companyId = :companyId
         and j.causalCode is not null
       order by j.causalCode
       """)
    List<String> findDistinctCausalCodesByTenantIdAndCompanyId(
            Long tenantId,
            Long companyId
    );

    List<JournalEntry> findByTenantIdAndEntryDateBetweenOrderByEntryDateAscIdAsc(
            Long tenantId,
            java.time.LocalDate dateFrom,
            java.time.LocalDate dateTo
    );
}
