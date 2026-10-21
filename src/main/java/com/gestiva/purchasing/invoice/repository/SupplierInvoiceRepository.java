package com.gestiva.purchasing.invoice.repository;

import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface SupplierInvoiceRepository extends JpaRepository<SupplierInvoice, Long>, JpaSpecificationExecutor<SupplierInvoice> {

    Optional<SupplierInvoice> findByTenantIdAndId(Long tenantId, Long id);

    boolean existsByTenantIdAndInvoiceNumber(Long tenantId, String invoiceNumber);

    boolean existsByTenantIdAndGoodsReceiptId(Long tenantId, Long goodsReceiptId);

    java.util.Optional<com.gestiva.purchasing.invoice.entity.SupplierInvoice> findFirstByTenantIdAndGoodsReceiptId(Long tenantId, Long goodsReceiptId);


    java.util.List<SupplierInvoice> findByTenantIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(
            Long tenantId,
            java.time.LocalDate dateFrom,
            java.time.LocalDate dateTo
    );
    long countByTenantIdAndStatusIn(Long tenantId, java.util.Collection<String> statuses);

    List<SupplierInvoice> findTop5ByTenantIdOrderByInvoiceDateDescIdDesc(Long tenantId);

    // =====================================================
    // GESTIVA V2 - QUERY MULTI-COMPANY
    // =====================================================

    Optional<SupplierInvoice> findByTenantIdAndCompanyIdAndId(
            Long tenantId,
            Long companyId,
            Long id
    );

    boolean existsByTenantIdAndCompanyIdAndInvoiceNumber(
            Long tenantId,
            Long companyId,
            String invoiceNumber
    );

    boolean existsByTenantIdAndCompanyIdAndGoodsReceiptId(
            Long tenantId,
            Long companyId,
            Long goodsReceiptId
    );

    Optional<SupplierInvoice> findFirstByTenantIdAndCompanyIdAndGoodsReceiptId(
            Long tenantId,
            Long companyId,
            Long goodsReceiptId
    );

    List<SupplierInvoice> findByTenantIdAndCompanyIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(
            Long tenantId,
            Long companyId,
            java.time.LocalDate dateFrom,
            java.time.LocalDate dateTo
    );

    long countByTenantIdAndCompanyIdAndStatusIn(
            Long tenantId,
            Long companyId,
            java.util.Collection<String> statuses
    );

    List<SupplierInvoice> findTop5ByTenantIdAndCompanyIdOrderByInvoiceDateDescIdDesc(
            Long tenantId,
            Long companyId
    );

}