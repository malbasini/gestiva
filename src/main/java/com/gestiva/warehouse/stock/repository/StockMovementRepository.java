package com.gestiva.warehouse.stock.repository;

import com.gestiva.warehouse.stock.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByTenantIdAndItemIdOrderByMovementDateDescIdDesc(Long tenantId, Long itemId);
    List<StockMovement> findByTenantIdAndReferenceTypeAndReferenceId(Long tenantId, String referenceType, Long referenceId);

    @Query("""
        select coalesce(sum(
            case
                when sm.direction = 'IN' then sm.quantity
                when sm.direction = 'OUT' then -sm.quantity
                else 0
            end
        ), 0)
        from StockMovement sm
        where sm.tenantId = :tenantId
          and sm.itemId = :itemId
    """)
    BigDecimal calculateStockBalance(Long tenantId, Long itemId);
}
