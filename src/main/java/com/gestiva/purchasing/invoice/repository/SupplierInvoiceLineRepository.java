package com.gestiva.purchasing.invoice.repository;

import com.gestiva.purchasing.invoice.entity.SupplierInvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupplierInvoiceLineRepository extends JpaRepository<SupplierInvoiceLine, Long> {

    List<SupplierInvoiceLine> findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(Long tenantId, Long supplierInvoiceId);


}