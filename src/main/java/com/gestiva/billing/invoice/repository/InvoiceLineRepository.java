package com.gestiva.billing.invoice.repository;

import com.gestiva.billing.invoice.entity.InvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceLineRepository extends JpaRepository<InvoiceLine, Long> {

    List<InvoiceLine> findByTenantIdAndInvoiceIdOrderByLineNoAsc(Long tenantId, Long invoiceId);
}