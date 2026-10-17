package com.gestiva.billing.invoice.repository;

import com.gestiva.billing.invoice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository
        extends JpaRepository<Invoice, Long>,
        JpaSpecificationExecutor<Invoice> {

    // =====================================================
    // METODI LEGACY - DA MIGRARE PROGRESSIVAMENTE
    // =====================================================

    Optional<Invoice> findByTenantIdAndId(
            Long tenantId,
            Long id
    );

    Optional<Invoice> findByTenantIdAndDeliveryNoteId(
            Long tenantId,
            Long deliveryNoteId
    );

    boolean existsByTenantIdAndDeliveryNoteId(
            Long tenantId,
            Long deliveryNoteId
    );

    List<Invoice> findByTenantIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(
            Long tenantId,
            LocalDate dateFrom,
            LocalDate dateTo
    );

    long countByTenantIdAndStatusIn(
            Long tenantId,
            Collection<String> statuses
    );

    List<Invoice> findTop5ByTenantIdOrderByInvoiceDateDescIdDesc(
            Long tenantId
    );

    // =====================================================
    // METODI V2 - MULTI-SOCIETÀ
    // =====================================================

    Optional<Invoice> findByTenantIdAndCompanyIdAndId(
            Long tenantId,
            Long companyId,
            Long id
    );

    Optional<Invoice> findByTenantIdAndCompanyIdAndDeliveryNoteId(
            Long tenantId,
            Long companyId,
            Long deliveryNoteId
    );

    boolean existsByTenantIdAndCompanyIdAndDeliveryNoteId(
            Long tenantId,
            Long companyId,
            Long deliveryNoteId
    );

    boolean existsByTenantIdAndCompanyIdAndInvoiceNumber(
            Long tenantId,
            Long companyId,
            String invoiceNumber
    );

    List<Invoice> findByTenantIdAndCompanyIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(
            Long tenantId,
            Long companyId,
            LocalDate dateFrom,
            LocalDate dateTo
    );

    long countByTenantIdAndCompanyIdAndStatusIn(
            Long tenantId,
            Long companyId,
            Collection<String> statuses
    );

    List<Invoice> findTop5ByTenantIdAndCompanyIdOrderByInvoiceDateDescIdDesc(
            Long tenantId,
            Long companyId
    );

    List<Invoice> findByTenantIdAndCompanyIdAndCustomerIdOrderByInvoiceDateDescIdDesc(
            Long tenantId,
            Long companyId,
            Long customerId
    );

    List<Invoice> findByTenantIdAndCompanyIdAndSalesOrderIdOrderByInvoiceDateDescIdDesc(
            Long tenantId,
            Long companyId,
            Long salesOrderId
    );
}