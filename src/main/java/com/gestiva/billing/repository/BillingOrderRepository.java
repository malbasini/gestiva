package com.gestiva.billing.repository;

import com.gestiva.billing.entity.BillingOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillingOrderRepository extends JpaRepository<BillingOrder, Long> {

    Optional<BillingOrder> findByTenantIdAndId(Long tenantId, Long id);
    Optional<BillingOrder> findByTenantIdAndProviderOrderId(Long tenantId, String providerOrderId);
    Optional<BillingOrder> findByProviderOrderId(String providerOrderId);
}