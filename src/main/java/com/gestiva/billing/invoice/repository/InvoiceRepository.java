package com.gestiva.billing.invoice.repository;

import com.gestiva.billing.invoice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {

    Optional<Invoice> findByTenantIdAndId(Long tenantId, Long id);

    Optional<Invoice> findByTenantIdAndDeliveryNoteId(Long tenantId, Long deliveryNoteId);

    boolean existsByTenantIdAndDeliveryNoteId(Long tenantId, Long deliveryNoteId);

    long countByTenantId(Long tenantId);

    java.util.List<Invoice> findByTenantIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(
            Long tenantId,
            java.time.LocalDate dateFrom,
            java.time.LocalDate dateTo
    );
    long countByTenantIdAndStatusIn(Long tenantId, java.util.Collection<String> statuses);
    List<Invoice> findTop5ByTenantIdOrderByInvoiceDateDescIdDesc(Long tenantId);
}