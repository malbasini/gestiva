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


    @Query("""
       select distinct j.causalCode
       from JournalEntry j
       where j.tenantId = :tenantId
         and j.causalCode is not null
       order by j.causalCode
       """)
    List<String> findDistinctCausalCodesByTenantId(Long tenantId);


    List<JournalEntry> findByTenantIdAndEntryDateBetweenOrderByEntryDateAscIdAsc(
            Long tenantId,
            java.time.LocalDate dateFrom,
            java.time.LocalDate dateTo
    );




}
