package com.gestiva.inventory.movement.repository;

import com.gestiva.inventory.movement.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
}