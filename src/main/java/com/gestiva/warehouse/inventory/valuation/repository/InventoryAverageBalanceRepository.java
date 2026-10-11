package com.gestiva.warehouse.inventory.valuation.repository;

import com.gestiva.warehouse.inventory.valuation.entity.InventoryAverageBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InventoryAverageBalanceRepository extends JpaRepository<InventoryAverageBalance, Long> {

    Optional<InventoryAverageBalance> findByTenantIdAndItemId(Long tenantId, Long itemId);
}