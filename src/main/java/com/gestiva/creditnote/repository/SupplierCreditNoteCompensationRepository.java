package com.gestiva.creditnote.repository;

import com.gestiva.creditnote.entity.SupplierCreditNoteCompensation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierCreditNoteCompensationRepository
        extends JpaRepository<SupplierCreditNoteCompensation, Long> {

    Optional<SupplierCreditNoteCompensation>
    findByTenantIdAndCompanyIdAndOperationKey(
            Long tenantId,
            Long companyId,
            String operationKey);

    List<SupplierCreditNoteCompensation>
    findByTenantIdAndCompanyIdAndCreditNoteFinancialPositionIdOrderByIdAsc(
            Long tenantId,
            Long companyId,
            Long creditNoteFinancialPositionId);

    List<SupplierCreditNoteCompensation>
    findByTenantIdAndCompanyIdAndPaymentDueIdOrderByIdAsc(
            Long tenantId,
            Long companyId,
            Long paymentDueId);
}
