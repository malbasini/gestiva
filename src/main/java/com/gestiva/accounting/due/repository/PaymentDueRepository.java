package com.gestiva.accounting.due.repository;

import com.gestiva.accounting.due.entity.PaymentDue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface PaymentDueRepository extends JpaRepository<PaymentDue, Long>, JpaSpecificationExecutor<PaymentDue> {

    Optional<PaymentDue> findByTenantIdAndId(Long tenantId, Long id);

    Optional<PaymentDue> findByTenantIdAndReferenceTypeAndReferenceId(Long tenantId, String referenceType, Long referenceId);

    List<PaymentDue> findByTenantIdAndDirectionAndStatusInOrderByDueDateAsc(
            Long tenantId,
            String direction,
            List<String> statuses
    );

    List<PaymentDue> findByTenantIdOrderByDueDateAscIdAsc(Long tenantId);

    List<PaymentDue> findByTenantIdAndDirectionOrderByDueDateAscIdAsc(Long tenantId, String direction);

    List<PaymentDue> findByTenantIdAndStatusInOrderByDueDateAscIdAsc(Long tenantId, List<String> statuses);

    List<PaymentDue> findByTenantIdAndDirectionAndStatusInOrderByDueDateAscIdAsc(
            Long tenantId,
            String direction,
            List<String> statuses
    );
}
