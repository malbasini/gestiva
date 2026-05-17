package com.gestiva.inventory.valuation.repository;

import com.gestiva.inventory.movement.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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






}