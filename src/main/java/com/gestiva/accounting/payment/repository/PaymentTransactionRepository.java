package com.gestiva.accounting.payment.repository;

import com.gestiva.accounting.payment.entity.PaymentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByTenantIdAndPaymentDueIdOrderByPaymentDateAscIdAsc(Long tenantId, Long paymentDueId);

    @org.springframework.data.jpa.repository.Query("""
           select coalesce(sum(p.amount), 0)
           from PaymentTransaction p
           where p.tenantId = :tenantId
             and p.paymentDueId = :paymentDueId
           """)
    BigDecimal calculatePaidAmountByDueId(@org.springframework.data.repository.query.Param("tenantId") Long tenantId,
                                          @org.springframework.data.repository.query.Param("paymentDueId") Long paymentDueId);


    org.springframework.data.domain.Page<PaymentTransaction> findByTenantIdAndDirection(
            Long tenantId,
            String direction,
            org.springframework.data.domain.Pageable pageable
    );


    Page<PaymentTransaction> findByTenantId(Long tenantId, Pageable pageable);

    Optional<PaymentTransaction> findByTenantIdAndId(Long tenantId, Long id);
}
