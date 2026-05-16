package com.gestiva.logistics.ddt.repository;

import com.gestiva.logistics.ddt.entity.DeliveryNoteLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DeliveryNoteLineRepository extends JpaRepository<DeliveryNoteLine, Long> {


    @Query("""
       select j
       from DeliveryNoteLine j
       where j.tenantId = :tenantId
       and j.id = :id
       """)

    List<DeliveryNoteLine> findByTenantIdAndDeliveryNoteIdOrderByLineNoAsc(Long tenantId, Long id);
}