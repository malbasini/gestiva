package com.gestiva.logistics.ddt.repository;

import com.gestiva.logistics.ddt.entity.DeliveryNoteLine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeliveryNoteLineRepository extends JpaRepository<DeliveryNoteLine, Long> {

    List<DeliveryNoteLine> findByTenantIdAndDeliveryNoteIdOrderByLineNoAsc(Long tenantId, Long deliveryNoteId);
}