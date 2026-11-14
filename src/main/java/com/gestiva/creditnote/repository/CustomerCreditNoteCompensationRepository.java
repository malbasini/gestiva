package com.gestiva.creditnote.repository;

import com.gestiva.creditnote.entity.CustomerCreditNoteCompensation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerCreditNoteCompensationRepository
        extends JpaRepository<CustomerCreditNoteCompensation, Long> {

    Optional<CustomerCreditNoteCompensation>
    findByTenantIdAndCompanyIdAndOperationKey(
            Long tenantId,
            Long companyId,
            String operationKey);

    List<CustomerCreditNoteCompensation>
    findByTenantIdAndCompanyIdAndCreditNoteFinancialPositionIdOrderByIdAsc(
            Long tenantId,
            Long companyId,
            Long creditNoteFinancialPositionId);

    List<CustomerCreditNoteCompensation>
    findByTenantIdAndCompanyIdAndPaymentDueIdOrderByIdAsc(
            Long tenantId,
            Long companyId,
            Long paymentDueId);
}
