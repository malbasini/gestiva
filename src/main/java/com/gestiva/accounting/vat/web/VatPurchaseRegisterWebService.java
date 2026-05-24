package com.gestiva.accounting.vat.web;

import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import com.gestiva.purchasing.invoice.entity.SupplierInvoiceLine;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceLineRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.supplier.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class VatPurchaseRegisterWebService {

    private final SupplierInvoiceRepository supplierInvoiceRepository;
    private final SupplierInvoiceLineRepository supplierInvoiceLineRepository;
    private final SupplierRepository supplierRepository;

    public VatPurchaseRegisterWebService(SupplierInvoiceRepository supplierInvoiceRepository,
                                         SupplierInvoiceLineRepository supplierInvoiceLineRepository,
                                         SupplierRepository supplierRepository) {
        this.supplierInvoiceRepository = supplierInvoiceRepository;
        this.supplierInvoiceLineRepository = supplierInvoiceLineRepository;
        this.supplierRepository = supplierRepository;
    }

    public List<VatPurchaseRegisterRowView> findRows(Long tenantId, LocalDate dateFrom, LocalDate dateTo) {
        List<SupplierInvoice> invoices = supplierInvoiceRepository
                .findByTenantIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(tenantId, dateFrom, dateTo);

        List<VatPurchaseRegisterRowView> rows = new ArrayList<>();

        for (SupplierInvoice invoice : invoices) {
            String supplierName = supplierRepository.findByTenantIdAndId(tenantId, invoice.getSupplierId())
                    .map(s -> s.getName() != null ? s.getName() : "Fornitore #" + invoice.getSupplierId())
                    .orElse("Fornitore #" + invoice.getSupplierId());

            List<SupplierInvoiceLine> lines =
                    supplierInvoiceLineRepository.findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(tenantId, invoice.getId());

            Map<BigDecimal, VatBucket> bucketsByTaxPct = new TreeMap<>();
            for (SupplierInvoiceLine line : lines) {
                BigDecimal taxPct = pct(line.getTaxPct());
                VatBucket bucket = bucketsByTaxPct.computeIfAbsent(taxPct, k -> new VatBucket());

                BigDecimal taxable = money(line.getLineSubtotal());
                BigDecimal tax = money(line.getTaxAmount());

                bucket.taxableAmount = money(bucket.taxableAmount.add(taxable));
                bucket.taxAmount = money(bucket.taxAmount.add(tax));
            }

            for (Map.Entry<BigDecimal, VatBucket> entry : bucketsByTaxPct.entrySet()) {
                BigDecimal taxPct = entry.getKey();
                VatBucket bucket = entry.getValue();

                VatPurchaseRegisterRowView row = new VatPurchaseRegisterRowView();
                row.setSupplierInvoiceId(invoice.getId());
                row.setFormattedInvoiceDate(PdfFormatUtils.formatDate(invoice.getInvoiceDate()));
                row.setInvoiceNumber(invoice.getInvoiceNumber());
                row.setSupplierName(supplierName);
                row.setFormattedTaxPct(PdfFormatUtils.formatDecimal(taxPct, 2) + "%");
                row.setFormattedTaxableAmount(PdfFormatUtils.formatMoney(bucket.taxableAmount));
                row.setFormattedTaxAmount(PdfFormatUtils.formatMoney(bucket.taxAmount));
                row.setFormattedTotalAmount(PdfFormatUtils.formatMoney(money(bucket.taxableAmount.add(bucket.taxAmount))));

                rows.add(row);
            }
        }

        return rows;
    }

    private BigDecimal money(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal pct(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : value.setScale(2, RoundingMode.HALF_UP);
    }

    private static class VatBucket {
        private BigDecimal taxableAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        private BigDecimal taxAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}