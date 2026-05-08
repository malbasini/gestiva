package com.gestiva.purchasing.invoice.repository;

import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface SupplierInvoiceRepository extends JpaRepository<SupplierInvoice, Long>, JpaSpecificationExecutor<SupplierInvoice> {

    Optional<SupplierInvoice> findByTenantIdAndId(Long tenantId, Long id);

    boolean existsByTenantIdAndInvoiceNumber(Long tenantId, String invoiceNumber);

    boolean existsByTenantIdAndGoodsReceiptId(Long tenantId, Long goodsReceiptId);
}