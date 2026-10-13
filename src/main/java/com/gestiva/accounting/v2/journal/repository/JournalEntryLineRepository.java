package com.gestiva.accounting.v2.journal.repository;

import com.gestiva.accounting.v2.journal.entity.JournalEntryLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, Long> {

    // =====================================================
    // METODI ESISTENTI - COMPATIBILITÀ
    // =====================================================

    List<JournalEntryLine> findByTenantIdAndJournalEntryIdOrderByLineNoAsc(
            Long tenantId,
            Long journalEntryId
    );

    List<JournalEntryLine> findByTenantIdAndAccountIdOrderByJournalEntryIdAscLineNoAsc(
            Long tenantId,
            Long accountId
    );

    List<JournalEntryLine> findByTenantIdAndJournalEntryIdIn(
            Long tenantId,
            List<Long> journalEntryIds
    );

    List<JournalEntryLine> findByTenantIdAndAccountIdAndJournalEntryIdInOrderByJournalEntryIdAscLineNoAsc(
            Long tenantId,
            Long accountId,
            List<Long> entryIds
    );

    // =====================================================
    // GESTIVA V2 - METODI MULTI-SOCIETÀ
    // =====================================================

    // Righe appartenenti a una registrazione specifica
    List<JournalEntryLine> findByTenantIdAndCompanyIdAndJournalEntryIdOrderByLineNoAsc(
            Long tenantId,
            Long companyId,
            Long journalEntryId
    );

    // Movimenti di un conto appartenente a una società
    List<JournalEntryLine> findByTenantIdAndCompanyIdAndAccountIdOrderByJournalEntryIdAscLineNoAsc(
            Long tenantId,
            Long companyId,
            Long accountId
    );

    // Righe di più registrazioni della stessa società
    List<JournalEntryLine> findByTenantIdAndCompanyIdAndJournalEntryIdIn(
            Long tenantId,
            Long companyId,
            List<Long> journalEntryIds
    );

    // Movimenti di un conto relativi a un insieme di registrazioni
    List<JournalEntryLine> findByTenantIdAndCompanyIdAndAccountIdAndJournalEntryIdInOrderByJournalEntryIdAscLineNoAsc(
            Long tenantId,
            Long companyId,
            Long accountId,
            List<Long> entryIds
    );
}