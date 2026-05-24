package com.gestiva.accounting.vat.web;

import com.gestiva.billing.invoice.entity.Invoice;
import com.gestiva.billing.invoice.entity.InvoiceLine;
import com.gestiva.billing.invoice.repository.InvoiceLineRepository;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import com.gestiva.purchasing.invoice.entity.SupplierInvoiceLine;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceLineRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class VatSettlementWebService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final SupplierInvoiceRepository supplierInvoiceRepository;
    private final SupplierInvoiceLineRepository supplierInvoiceLineRepository;

    public VatSettlementWebService(InvoiceRepository invoiceRepository,
                                   InvoiceLineRepository invoiceLineRepository,
                                   SupplierInvoiceRepository supplierInvoiceRepository,
                                   SupplierInvoiceLineRepository supplierInvoiceLineRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.supplierInvoiceRepository = supplierInvoiceRepository;
        this.supplierInvoiceLineRepository = supplierInvoiceLineRepository;
    }

    public VatSettlementView calculateMonthlySettlement(Long tenantId, int year, int month) {
        LocalDate dateFrom = LocalDate.of(year, month, 1);
        LocalDate dateTo = dateFrom.withDayOfMonth(dateFrom.lengthOfMonth());

        BigDecimal salesTaxable = zero();
        BigDecimal salesTax = zero();

        List<Invoice> salesInvoices = invoiceRepository
                .findByTenantIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(tenantId, dateFrom, dateTo);

        for (Invoice invoice : salesInvoices) {
            List<InvoiceLine> lines = invoiceLineRepository.findByTenantIdAndInvoiceIdOrderByLineNoAsc(tenantId, invoice.getId());

            for (InvoiceLine line : lines) {
                salesTaxable = money(salesTaxable.add(money(line.getLineTotal())));
                salesTax = money(salesTax.add(money(line.getTaxAmount())));
            }
        }

        BigDecimal purchaseTaxable = zero();
        BigDecimal purchaseTax = zero();

        List<SupplierInvoice> purchaseInvoices = supplierInvoiceRepository
                .findByTenantIdAndInvoiceDateBetweenOrderByInvoiceDateAscInvoiceNumberAsc(tenantId, dateFrom, dateTo);

        for (SupplierInvoice invoice : purchaseInvoices) {
            List<SupplierInvoiceLine> lines =
                    supplierInvoiceLineRepository.findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(tenantId, invoice.getId());

            for (SupplierInvoiceLine line : lines) {
                purchaseTaxable = money(purchaseTaxable.add(money(line.getLineSubtotal())));
                purchaseTax = money(purchaseTax.add(money(line.getTaxAmount())));
            }
        }

        BigDecimal vatBalance = money(salesTax.subtract(purchaseTax));

        VatSettlementView view = new VatSettlementView();
        view.setYear(year);
        view.setMonth(month);
        view.setFormattedSalesTaxableAmount(PdfFormatUtils.formatMoney(salesTaxable));
        view.setFormattedSalesTaxAmount(PdfFormatUtils.formatMoney(salesTax));
        view.setFormattedPurchaseTaxableAmount(PdfFormatUtils.formatMoney(purchaseTaxable));
        view.setFormattedPurchaseTaxAmount(PdfFormatUtils.formatMoney(purchaseTax));
        view.setFormattedVatBalance(PdfFormatUtils.formatMoney(vatBalance));
        view.setBalanceTypeLabel(resolveBalanceType(vatBalance));

        return view;
    }

    private String resolveBalanceType(BigDecimal vatBalance) {
        int cmp = vatBalance.compareTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        if (cmp > 0) {
            return "A DEBITO";
        }
        if (cmp < 0) {
            return "A CREDITO";
        }
        return "ZERO";
    }

    private BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal money(BigDecimal value) {
        return value == null
                ? zero()
                : value.setScale(2, RoundingMode.HALF_UP);
    }
}