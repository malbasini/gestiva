package com.gestiva.accounting.due.repository;

import com.gestiva.accounting.due.entity.PaymentDueTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentDueTransactionRepository extends JpaRepository<PaymentDueTransaction, Long> {

    List<PaymentDueTransaction> findByTenantIdAndPaymentDueIdOrderByTransactionDateAscIdAsc(Long tenantId, Long paymentDueId);
}