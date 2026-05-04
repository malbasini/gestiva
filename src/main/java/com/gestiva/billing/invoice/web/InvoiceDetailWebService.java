package com.gestiva.billing.invoice.web;

import com.gestiva.billing.invoice.repository.InvoiceLineRepository;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional(readOnly = true)
public class InvoiceDetailWebService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final CustomerRepository customerRepository;

    public InvoiceDetailWebService(InvoiceRepository invoiceRepository,
                                   InvoiceLineRepository invoiceLineRepository,
                                   CustomerRepository customerRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.customerRepository = customerRepository;
    }

    public InvoiceDetailView getDetail(Long tenantId, Long invoiceId) {
        var invoice = invoiceRepository.findByTenantIdAndId(tenantId, invoiceId)
                .orElseThrow(() -> new NotFoundException("Fattura non trovata"));

        var customer = customerRepository.findByTenantIdAndId(tenantId, invoice.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Cliente non trovato"));

        var lines = invoiceLineRepository.findByTenantIdAndInvoiceIdOrderByLineNoAsc(tenantId, invoiceId);

        InvoiceDetailView view = new InvoiceDetailView();
        view.setId(invoice.getId());
        view.setCustomerId(invoice.getCustomerId());
        view.setDeliveryNoteId(invoice.getDeliveryNoteId());
        view.setSalesOrderId(invoice.getSalesOrderId());
        view.setCustomerName(customer.getName());
        view.setInvoiceNumber(invoice.getInvoiceNumber());
        view.setStatus(invoice.getStatus());
        view.setCurrencyCode(invoice.getCurrencyCode());
        view.setFormattedInvoiceDate(PdfFormatUtils.formatDate(invoice.getInvoiceDate()));
        view.setFormattedSubtotalAmount(PdfFormatUtils.formatMoney(invoice.getSubtotalAmount()));
        view.setFormattedTaxAmount(PdfFormatUtils.formatMoney(invoice.getTaxAmount()));
        view.setFormattedTotalAmount(PdfFormatUtils.formatMoney(invoice.getTotalAmount()));
        view.setNotes(invoice.getNotes());
        view.setCancelable("ISSUED".equals(invoice.getStatus()));

        view.setLines(lines.stream().map(line -> {
            InvoiceDetailLineView l = new InvoiceDetailLineView();
            l.setLineNo(line.getLineNo());
            l.setDescription(line.getDescription());
            l.setFormattedQuantity(PdfFormatUtils.formatDecimal(line.getQuantity()));
            l.setUnitOfMeasure(line.getUnitOfMeasure());
            l.setFormattedUnitPrice(PdfFormatUtils.formatMoney(line.getUnitPrice()));
            l.setFormattedDiscountPct(PdfFormatUtils.formatDecimal(line.getDiscountPct()));
            l.setFormattedTaxPct(PdfFormatUtils.formatDecimal(line.getTaxPct()));

            BigDecimal gross = line.getQuantity().multiply(line.getUnitPrice());
            BigDecimal discountAmount = gross
                    .multiply(line.getDiscountPct())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal taxable = gross.subtract(discountAmount);

            l.setFormattedLineTotal(PdfFormatUtils.formatMoney(taxable));
            return l;
        }).toList());

        return view;
    }
}