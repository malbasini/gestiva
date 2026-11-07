package com.gestiva.creditnote.repository;

import com.gestiva.creditnote.entity.CreditNote;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CreditNoteRepository
        extends JpaRepository<CreditNote, Long> {

    Optional<CreditNote> findByTenantIdAndCompanyIdAndId(
            Long tenantId, Long companyId, Long id);

    List<CreditNote>
    findByTenantIdAndCompanyIdAndCreditNoteTypeAndOriginalInvoiceId(
            Long tenantId,
            Long companyId,
            String creditNoteType,
            Long originalInvoiceId);

    boolean existsByTenantIdAndCompanyIdAndCreditNoteTypeAndCreditNoteNumber(
            Long tenantId,
            Long companyId,
            String creditNoteType,
            String creditNoteNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select cn from CreditNote cn
        where cn.tenantId = :tenantId
          and cn.companyId = :companyId
          and cn.id = :id
        """)
    Optional<CreditNote> findByTenantIdAndCompanyIdAndIdForUpdate(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId,
            @Param("id") Long id);
}
