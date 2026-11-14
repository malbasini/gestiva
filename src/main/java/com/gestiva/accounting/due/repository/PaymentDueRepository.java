package com.gestiva.accounting.due.repository;

import com.gestiva.accounting.due.entity.PaymentDue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface PaymentDueRepository extends JpaRepository<PaymentDue, Long>, JpaSpecificationExecutor<PaymentDue> {

    Optional<PaymentDue> findByTenantIdAndId(Long tenantId, Long id);

    Optional<PaymentDue> findByTenantIdAndCompanyIdAndId(
            Long tenantId,
            Long companyId,
            Long id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select p
    from PaymentDue p
    where p.tenantId = :tenantId
      and p.companyId = :companyId
      and p.id = :id
    """)
    Optional<PaymentDue> findByTenantIdAndCompanyIdAndIdForUpdate(
            Long tenantId,
            Long companyId,
            Long id
    );

    Optional<PaymentDue> findByTenantIdAndCompanyIdAndReferenceTypeAndReferenceId(
            Long tenantId,
            Long companyId,
            String referenceType,
            Long referenceId
    );

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

    // Gestiva V2 - Conteggio scadenze isolato per società.
    long countByTenantIdAndCompanyIdAndDirectionAndStatusIn(
            Long tenantId,
            Long companyId,
            String direction,
            java.util.Collection<String> statuses
    );

    @Query("""
    select coalesce(sum(p.openAmount), 0)
    from PaymentDue p
    where p.tenantId = :tenantId
      and p.companyId = :companyId
      and p.direction = :direction
      and p.status in :statuses
    """)
    java.math.BigDecimal sumOpenAmountByCompanyAndDirectionAndStatuses(
            Long tenantId,
            Long companyId,
            String direction,
            java.util.List<String> statuses
    );


    @Query("""
    select count(p)
    from PaymentDue p
    where p.tenantId = :tenantId
      and p.companyId = :companyId
      and p.status in :statuses
      and p.dueDate < :today
    """)
    long countOverdueByCompany(
            Long tenantId,
            Long companyId,
            java.util.List<String> statuses,
            java.time.LocalDate today
    );


    @Query("""
    select coalesce(sum(p.openAmount), 0)
    from PaymentDue p
    where p.tenantId = :tenantId
      and p.companyId = :companyId
      and p.status in :statuses
      and p.dueDate < :today
    """)
    java.math.BigDecimal sumOverdueOpenAmountByCompany(
            Long tenantId,
            Long companyId,
            java.util.List<String> statuses,
            java.time.LocalDate today
    );


    // GESTIVA V2 - PASSO 5.13.2392G
    // Scadenze fornitore eleggibili per compensazione V10.
    // Lettura filtrata per tenant, societa, fornitore e valuta.

    @org.springframework.data.jpa.repository.Query("""
        select p
        from PaymentDue p
        where p.tenantId = :tenantId
          and p.companyId = :companyId
          and p.partyType = 'SUPPLIER'
          and p.partyId = :supplierId
          and p.direction = 'PAYABLE'
          and p.referenceType = 'SUPPLIER_INVOICE'
          and p.currencyCode = :currencyCode
          and p.status in ('OPEN', 'PARTIALLY_PAID')
          and p.openAmount > 0
        order by p.dueDate asc, p.id asc
        """)
    java.util.List<PaymentDue> findCompensableSupplierDues(
            @org.springframework.data.repository.query.Param("tenantId")
            Long tenantId,
            @org.springframework.data.repository.query.Param("companyId")
            Long companyId,
            @org.springframework.data.repository.query.Param("supplierId")
            Long supplierId,
            @org.springframework.data.repository.query.Param("currencyCode")
            String currencyCode
    );


    // GESTIVA V2 - PASSO 5.13.2395KN
    // Scadenze cliente eleggibili per compensazione V11.
    // Lettura isolata per tenant, societa, cliente e valuta.
    @Query("""
        select p
        from PaymentDue p
        where p.tenantId = :tenantId
          and p.companyId = :companyId
          and p.partyType = 'CUSTOMER'
          and p.partyId = :customerId
          and p.direction = 'RECEIVABLE'
          and p.referenceType = 'CUSTOMER_INVOICE'
          and p.currencyCode = :currencyCode
          and p.status in ('OPEN', 'PARTIALLY_PAID')
          and p.openAmount > 0
        order by p.dueDate asc, p.id asc
        """)
    List<PaymentDue> findCompensableCustomerDues(
            @org.springframework.data.repository.query.Param("tenantId")
            Long tenantId,
            @org.springframework.data.repository.query.Param("companyId")
            Long companyId,
            @org.springframework.data.repository.query.Param("customerId")
            Long customerId,
            @org.springframework.data.repository.query.Param("currencyCode")
            String currencyCode
    );

}
