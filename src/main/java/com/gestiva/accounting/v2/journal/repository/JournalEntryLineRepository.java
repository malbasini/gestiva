package com.gestiva.accounting.v2.journal.repository;

import com.gestiva.accounting.v2.journal.entity.JournalEntryLine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, Long> {

    List<JournalEntryLine> findByTenantIdAndJournalEntryIdOrderByLineNoAsc(Long tenantId, Long journalEntryId);
    List<JournalEntryLine> findByTenantIdAndAccountIdOrderByJournalEntryIdAscLineNoAsc(Long tenantId, Long accountId);
}