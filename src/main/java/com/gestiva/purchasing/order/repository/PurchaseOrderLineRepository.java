package com.gestiva.purchasing.order.repository;

import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderLineRepository extends JpaRepository<PurchaseOrderLine, Long> {

    List<PurchaseOrderLine> findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(Long tenantId, Long purchaseOrderId);

    Optional<PurchaseOrderLine> findByTenantIdAndPurchaseOrderIdAndId(
            Long tenantId,
            Long purchaseOrderId,
            Long id
    );

    void deleteByTenantIdAndPurchaseOrderId(Long tenantId, Long purchaseOrderId);

    /**
     * Elimina esclusivamente una riga appartenente
     * al tenant e all'ordine specificati.
     *
     * Il chiamante deve verificare preventivamente
     * l'assenza di ricevimenti merci collegati.
     */
    @org.springframework.data.jpa.repository.Modifying(
            flushAutomatically = true,
            clearAutomatically = false
    )
    @org.springframework.data.jpa.repository.Query("""
            delete from PurchaseOrderLine line
            where line.tenantId = :tenantId
              and line.purchaseOrderId = :purchaseOrderId
              and line.id = :id
            """)
    int deleteByTenantIdAndPurchaseOrderIdAndId(
            @org.springframework.data.repository.query.Param("tenantId")
            Long tenantId,
            @org.springframework.data.repository.query.Param("purchaseOrderId")
            Long purchaseOrderId,
            @org.springframework.data.repository.query.Param("id")
            Long id
    );

    boolean existsByTenantIdAndItemId(Long tenantId, Long itemId);
}