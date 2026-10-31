package com.gestiva.accounting.vat.web;

import com.gestiva.billing.invoice.entity.Invoice;
import com.gestiva.billing.invoice.entity.InvoiceLine;
import com.gestiva.billing.invoice.repository.InvoiceLineRepository;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class VatSalesRegisterWebService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final CustomerRepository customerRepository;

    public VatSalesRegisterWebService(InvoiceRepository invoiceRepository,
                                      InvoiceLineRepository invoiceLineRepository,
                                      CustomerRepository customerRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.customerRepository = customerRepository;
    }

    @Deprecated
    public List<VatSalesRegisterRowView> findRows(
            Long tenantId,
            LocalDate dateFrom,
            LocalDate dateTo) {

        throw new IllegalStateException(
                "Registro IVA vendite: companyId obbligatorio."
        );
    }

    public List<VatSalesRegisterRowView> findRows(
            Long tenantId,
            Long companyId,
            LocalDate dateFrom,
            LocalDate dateTo) {

        if (tenantId == null || companyId == null) {
            throw new IllegalStateException(
                    "Tenant e società obbligatori per il registro IVA vendite."
            );
        }
        List<Invoice> invoices = invoiceRepository
                .findByTenantIdAndCompanyIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(tenantId, companyId, dateFrom, dateTo);

        List<VatSalesRegisterRowView> rows = new ArrayList<>();

        for (Invoice invoice : invoices) {
            if (!"ISSUED".equals(invoice.getStatus())) {
                continue;
            }
            String customerName = customerRepository.findByTenantIdAndId(tenantId, invoice.getCustomerId())
                    .map(c -> c.getName() != null ? c.getName() : "Cliente #" + invoice.getCustomerId())
                    .orElse("Cliente #" + invoice.getCustomerId());

            List<InvoiceLine> lines = invoiceLineRepository.findByTenantIdAndInvoiceIdOrderByLineNoAsc(tenantId, invoice.getId());

            Map<BigDecimal, VatBucket> bucketsByTaxPct = new TreeMap<>();
            for (InvoiceLine line : lines) {
                BigDecimal taxPct = pct(line.getTaxPct());
                VatBucket bucket = bucketsByTaxPct.computeIfAbsent(taxPct, k -> new VatBucket());

                // lineTotal non è omogeneo tra i percorsi di vendita.
                // Ricostruiamo l'imponibile da quantità, prezzo e sconto.
                BigDecimal gross = line.getQuantity()
                        .multiply(line.getUnitPrice());

                BigDecimal discountPct =
                        line.getDiscountPct() == null
                                ? BigDecimal.ZERO
                                : line.getDiscountPct();

                BigDecimal discountAmount = gross
                        .multiply(discountPct)
                        .divide(
                                new BigDecimal("100"),
                                2,
                                RoundingMode.HALF_UP
                        );

                BigDecimal taxable = money(
                        gross.subtract(discountAmount)
                );
                BigDecimal tax = money(line.getTaxAmount());

                bucket.taxableAmount = money(bucket.taxableAmount.add(taxable));
                bucket.taxAmount = money(bucket.taxAmount.add(tax));
            }

            for (Map.Entry<BigDecimal, VatBucket> entry : bucketsByTaxPct.entrySet()) {
                BigDecimal taxPct = entry.getKey();
                VatBucket bucket = entry.getValue();

                VatSalesRegisterRowView row = new VatSalesRegisterRowView();
                row.setInvoiceId(invoice.getId());
                row.setFormattedInvoiceDate(PdfFormatUtils.formatDate(invoice.getInvoiceDate()));
                row.setInvoiceNumber(invoice.getInvoiceNumber());
                row.setCustomerName(customerName);
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
