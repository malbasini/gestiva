package com.gestiva.sales.quote.web;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.sales.order.repository.SalesOrderRepository;
import com.gestiva.sales.quote.repository.QuoteLineRepository;
import com.gestiva.sales.quote.repository.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class QuoteDetailWebService {

    private final QuoteRepository quoteRepository;
    private final QuoteLineRepository quoteLineRepository;
    private final CustomerRepository customerRepository;
    private final SalesOrderRepository salesOrderRepository;

    public QuoteDetailWebService(QuoteRepository quoteRepository,
                                 QuoteLineRepository quoteLineRepository,
                                 CustomerRepository customerRepository,
                                 SalesOrderRepository salesOrderRepository) {
        this.quoteRepository = quoteRepository;
        this.quoteLineRepository = quoteLineRepository;
        this.customerRepository = customerRepository;
        this.salesOrderRepository = salesOrderRepository;
    }

    public QuoteDetailView getDetail(Long tenantId, Long quoteId) {
        var quote = quoteRepository.findByTenantIdAndId(tenantId, quoteId)
                .orElseThrow(() -> new NotFoundException("Preventivo non trovato"));

        var customer = customerRepository.findByTenantIdAndId(tenantId, quote.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Cliente non trovato"));

        var lines = quoteLineRepository.findByTenantIdAndQuoteIdOrderByLineNoAsc(tenantId, quoteId);

        QuoteDetailView view = new QuoteDetailView();
        view.setId(quote.getId());
        view.setCustomerId(customer.getId());
        view.setCustomerName(customer.getName());
        view.setQuoteNumber(quote.getQuoteNumber());
        view.setStatus(quote.getStatus());
        view.setCurrencyCode(quote.getCurrencyCode());
        view.setNotes(quote.getNotes());

        view.setFormattedQuoteDate(PdfFormatUtils.formatDate(quote.getQuoteDate()));
        view.setFormattedValidUntil(PdfFormatUtils.formatDate(quote.getValidUntil()));
        view.setFormattedSubtotalAmount(PdfFormatUtils.formatMoney(quote.getSubtotalAmount()));
        view.setFormattedTaxAmount(PdfFormatUtils.formatMoney(quote.getTaxAmount()));
        view.setFormattedTotalAmount(PdfFormatUtils.formatMoney(quote.getTotalAmount()));

        view.setLines(lines.stream().map(line -> {
            QuoteDetailLineView l = new QuoteDetailLineView();
            l.setLineNo(line.getLineNo());
            l.setDescription(line.getDescription());
            l.setFormattedQuantity(PdfFormatUtils.formatDecimal(line.getQuantity()));
            l.setFormattedUnitPrice(PdfFormatUtils.formatMoney(line.getUnitPrice()));
            l.setFormattedDiscountPct(PdfFormatUtils.formatDecimal(line.getDiscountPct()));
            l.setFormattedTaxPct(PdfFormatUtils.formatDecimal(line.getTaxPct()));
            l.setFormattedLineTotal(PdfFormatUtils.formatMoney(line.getLineTotal()));
            return l;
        }).toList());

        boolean converted = salesOrderRepository.existsByTenantIdAndQuoteId(tenantId, quoteId);
        view.setLocked("ACCEPTED".equals(quote.getStatus()) && converted);
        
        return view;
    }
}