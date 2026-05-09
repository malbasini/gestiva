package com.gestiva.accounting.v2.journal.repository;

import com.gestiva.accounting.v2.journal.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long>, JpaSpecificationExecutor<JournalEntry> {

    Optional<JournalEntry> findByTenantIdAndId(Long tenantId, Long id);
    Optional<JournalEntry> findByTenantIdAndEntryNumber(Long tenantId, String entryNumber);
    boolean existsByTenantIdAndEntryNumber(Long tenantId, String entryNumber);
    List<JournalEntry> findByTenantIdOrderByEntryDateDescIdDesc(Long tenantId);
}
