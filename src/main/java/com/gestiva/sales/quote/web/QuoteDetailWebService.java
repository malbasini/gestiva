package com.gestiva.sales.quote.web;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.sales.order.repository.SalesOrderRepository;
import com.gestiva.sales.quote.repository.QuoteLineRepository;
import com.gestiva.sales.quote.repository.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;

@Service
@Transactional(readOnly = true)
public class QuoteDetailWebService {

    private final QuoteRepository quoteRepository;
    private final QuoteLineRepository quoteLineRepository;
    private final CustomerRepository customerRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final CompanyContext companyContext;

    public QuoteDetailWebService(
            QuoteRepository quoteRepository,
            QuoteLineRepository quoteLineRepository,
            CustomerRepository customerRepository,
            SalesOrderRepository salesOrderRepository,
            CompanyContext companyContext) {

        this.quoteRepository = quoteRepository;
        this.quoteLineRepository = quoteLineRepository;
        this.customerRepository = customerRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.companyContext = companyContext;
    }

    private Long requireCompanyId() {

        Long companyId = companyContext.resolveCurrentCompanyId();
        if (companyId == null) {
            throw new BusinessException("Nessuna società attiva selezionata.");
        }
        return companyId;
    }

    public QuoteDetailView getDetail(Long tenantId, Long quoteId) {

        Long companyId = requireCompanyId();
        var quote = quoteRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        quoteId
                )
                .orElseThrow(() -> new NotFoundException(
                        "Preventivo non trovato nella società attiva"
                ));

        var customer = customerRepository
                .findByTenantIdAndId(
                        tenantId,
                        quote.getCustomerId()
                )
                .orElseThrow(() -> new NotFoundException(
                        "Cliente non trovato nel tenant"
                ));

        var lines = quoteLineRepository
                .findByTenantIdAndCompanyIdAndQuoteIdOrderByLineNoAsc(
                        tenantId,
                        companyId,
                        quoteId
                );

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
            l.setFormattedQuantity(PdfFormatUtils.formatDecimalTrimmed(line.getQuantity(),2));
            l.setFormattedUnitPrice(PdfFormatUtils.formatMoney(line.getUnitPrice()));
            l.setFormattedDiscountPct(PdfFormatUtils.formatDecimal(line.getDiscountPct()));
            l.setFormattedTaxPct(PdfFormatUtils.formatDecimalTrimmed(line.getTaxPct(),2));
            java.math.BigDecimal gross = line.getQuantity().multiply(line.getUnitPrice());
            java.math.BigDecimal discountAmount = gross
                    .multiply(line.getDiscountPct())
                    .divide(new java.math.BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            java.math.BigDecimal taxable = gross.subtract(discountAmount);
            l.setFormattedLineTotal(PdfFormatUtils.formatMoney(taxable));
            return l;
        }).toList());

        boolean converted = salesOrderRepository
                .existsByTenantIdAndCompanyIdAndQuoteId(
                        tenantId,
                        companyId,
                        quoteId
                );
        view.setLocked("ACCEPTED".equals(quote.getStatus()) && converted);
        
        return view;
    }
}