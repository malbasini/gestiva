package com.gestiva.creditnote.repository;

import com.gestiva.creditnote.entity.CreditNoteFinancialPosition;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CreditNoteFinancialPositionRepository
        extends JpaRepository<CreditNoteFinancialPosition, Long> {

    Optional<CreditNoteFinancialPosition>
    findByTenantIdAndCompanyIdAndCreditNoteId(
            Long tenantId,
            Long companyId,
            Long creditNoteId);

    boolean existsByTenantIdAndCompanyIdAndCreditNoteId(
            Long tenantId,
            Long companyId,
            Long creditNoteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select p
        from CreditNoteFinancialPosition p
        where p.tenantId = :tenantId
          and p.companyId = :companyId
          and p.id = :id
        """)
    Optional<CreditNoteFinancialPosition>
    findByTenantIdAndCompanyIdAndIdForUpdate(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId,
            @Param("id") Long id);
}
