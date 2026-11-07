package com.gestiva.creditnote.repository;

import com.gestiva.creditnote.entity.CreditNoteLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreditNoteLineRepository
        extends JpaRepository<CreditNoteLine, Long> {

    List<CreditNoteLine>
    findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
            Long tenantId, Long companyId, Long creditNoteId);
}
