package com.gestiva.accounting.payment.repository;

import com.gestiva.accounting.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.util.List;

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
}
