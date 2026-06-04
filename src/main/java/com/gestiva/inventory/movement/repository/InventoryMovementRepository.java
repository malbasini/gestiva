package com.gestiva.inventory.movement.repository;

import com.gestiva.inventory.movement.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long>, JpaSpecificationExecutor<InventoryMovement> {
    Optional<InventoryMovement> findByTenantIdAndId(Long tenantId, Long id);

    List<InventoryMovement> findByTenantIdAndItemIdOrderByMovementDateAscIdAsc(Long tenantId, Long itemId);

    List<InventoryMovement> findByTenantIdAndReferenceTypeAndReferenceIdOrderByIdAsc(Long tenantId, String referenceType, Long referenceId);

    boolean existsByTenantIdAndReferenceTypeAndReferenceIdAndCausalCode(
            Long tenantId,
            String referenceType,
            Long referenceId,
            String causalCode
    );
    @Query("""
       select distinct m.causalCode
       from InventoryMovement m
       where m.tenantId = :tenantId
         and (upper(m.movementType) = 'OUT' or upper(m.movementType) = 'ADJUSTMENT_OUT')
         and m.causalCode is not null
       order by m.causalCode
       """)
    List<String> findDistinctOutboundCausalCodesByTenantId(@Param("tenantId") Long tenantId);

    @Query("""
       select new com.gestiva.inventory.valuation.web.CostOfGoodsSoldSummaryRow(
           m.itemId,
           coalesce(sum(m.quantity), 0),
           coalesce(sum(m.totalCost), 0)
       )
       from InventoryMovement m
       where m.tenantId = :tenantId
         and (upper(m.movementType) = 'OUT' or upper(m.movementType) = 'ADJUSTMENT_OUT')
         and (:itemId is null or m.itemId = :itemId)
         and (:dateFrom is null or m.movementDate >= :dateFrom)
         and (:dateTo is null or m.movementDate <= :dateTo)
       group by m.itemId
       order by m.itemId
       """)
    List<com.gestiva.inventory.valuation.web.CostOfGoodsSoldSummaryRow> summarizeCostOfGoodsSoldByItem(
            @Param("tenantId") Long tenantId,
            @Param("itemId") Long itemId,
            @Param("dateFrom") java.time.LocalDate dateFrom,
            @Param("dateTo") java.time.LocalDate dateTo
    );
    @Query("""
   select coalesce(
       sum(
           case
               when upper(m.movementType) in ('IN', 'ADJUSTMENT_IN') then m.quantity
               when upper(m.movementType) in ('OUT', 'ADJUSTMENT_OUT') then -m.quantity
               else 0
           end
       ), 0
   )
   from InventoryMovement m
   where m.tenantId = :tenantId
     and m.itemId = :itemId
     and m.reversed = false
   """)
    BigDecimal calculateInventoryBalance(@Param("tenantId") Long tenantId,
                                         @Param("itemId") Long itemId);


    List<InventoryMovement> findTop10ByTenantIdAndItemIdOrderByMovementDateDescIdDesc(Long tenantId, Long itemId);
    java.util.List<InventoryMovement> findTop10ByTenantIdOrderByMovementDateDescIdDesc(Long tenantId);

}