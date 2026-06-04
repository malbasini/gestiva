package com.gestiva.accounting.due.repository;

import com.gestiva.accounting.due.entity.PaymentDue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
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

    @Query("""
       select coalesce(sum(p.openAmount), 0)
       from PaymentDue p
       where p.tenantId = :tenantId
         and p.direction = :direction
         and p.status in :statuses
       """)
    java.math.BigDecimal sumOpenAmountByDirectionAndStatuses(Long tenantId, String direction, java.util.List<String> statuses);

    @Query("""
       select count(p)
       from PaymentDue p
       where p.tenantId = :tenantId
         and p.status in :statuses
         and p.dueDate < :today
       """)
    long countOverdue(Long tenantId, java.util.List<String> statuses, java.time.LocalDate today);

    @Query("""
       select coalesce(sum(p.openAmount), 0)
       from PaymentDue p
       where p.tenantId = :tenantId
         and p.status in :statuses
         and p.dueDate < :today
       """)
    java.math.BigDecimal sumOverdueOpenAmount(Long tenantId, java.util.List<String> statuses, java.time.LocalDate today);
    long countByTenantIdAndDirectionAndStatusIn(Long tenantId, String direction, java.util.Collection<String> statuses);


}
