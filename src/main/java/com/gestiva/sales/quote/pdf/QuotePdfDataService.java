package com.gestiva.sales.quote.pdf;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.platform.company.entity.CompanyProfile;
import com.gestiva.platform.company.repository.CompanyProfileRepository;
import com.gestiva.platform.tenant.entity.Tenant;
import com.gestiva.platform.tenant.repository.TenantRepository;
import com.gestiva.sales.quote.entity.Quote;
import com.gestiva.sales.quote.entity.QuoteLine;
import com.gestiva.sales.quote.repository.QuoteLineRepository;
import com.gestiva.sales.quote.repository.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class QuotePdfDataService {

    private final QuoteRepository quoteRepository;
    private final QuoteLineRepository quoteLineRepository;
    private final CustomerRepository customerRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final TenantRepository tenantRepository;

    public QuotePdfDataService(QuoteRepository quoteRepository,
                               QuoteLineRepository quoteLineRepository,
                               CustomerRepository customerRepository,
                               CompanyProfileRepository companyProfileRepository,
                               TenantRepository tenantRepository) {
        this.quoteRepository = quoteRepository;
        this.quoteLineRepository = quoteLineRepository;
        this.customerRepository = customerRepository;
        this.companyProfileRepository = companyProfileRepository;
        this.tenantRepository = tenantRepository;
    }

    public QuotePdfView buildView(Long tenantId, Long quoteId) {
        Quote quote = quoteRepository.findByTenantIdAndId(tenantId, quoteId)
                .orElseThrow(() -> new NotFoundException("Preventivo non trovato"));

        List<QuoteLine> lines = quoteLineRepository.findByTenantIdAndQuoteIdOrderByLineNoAsc(tenantId, quoteId);

        Customer customer = customerRepository.findByTenantIdAndId(tenantId, quote.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Cliente non trovato"));

        CompanyProfile company = companyProfileRepository.findByTenantId(tenantId).orElse(null);
        Tenant tenant = tenantRepository.findById(tenantId).orElse(null);

        QuotePdfView view = new QuotePdfView();

        if (company != null) {
            view.setCompanyName(company.getLegalName());
            view.setCompanyEmail(company.getEmail());
            view.setCompanyPhone(company.getPhone());
            view.setCompanyVatNumber(company.getVatNumber());
        } else if (tenant != null) {
            view.setCompanyName(tenant.getName());
            view.setCompanyEmail(tenant.getEmail());
            view.setCompanyPhone("");
            view.setCompanyVatNumber("");
        } else {
            view.setCompanyName("Azienda Demo");
            view.setCompanyEmail("");
            view.setCompanyPhone("");
            view.setCompanyVatNumber("");
        }

        view.setCustomerName(customer.getName());
        view.setCustomerEmail(customer.getEmail());
        view.setCustomerVatNumber(customer.getVatNumber());

        view.setQuoteNumber(quote.getQuoteNumber());
        view.setQuoteDate(quote.getQuoteDate());
        view.setValidUntil(quote.getValidUntil());
        view.setCurrencyCode(quote.getCurrencyCode());
        view.setNotes(quote.getNotes());
        view.setSubtotalAmount(quote.getSubtotalAmount());
        view.setTaxAmount(quote.getTaxAmount());
        view.setTotalAmount(quote.getTotalAmount());

        view.setFormattedQuoteDate(PdfFormatUtils.formatDate(quote.getQuoteDate()));
        view.setFormattedValidUntil(PdfFormatUtils.formatDate(quote.getValidUntil()));
        view.setFormattedSubtotalAmount(PdfFormatUtils.formatMoney(quote.getSubtotalAmount()));
        view.setFormattedTaxAmount(PdfFormatUtils.formatMoney(quote.getTaxAmount()));
        view.setFormattedTotalAmount(PdfFormatUtils.formatMoney(quote.getTotalAmount()));

        view.setLines(lines.stream().map(line -> {
            QuotePdfLineView l = new QuotePdfLineView();
            l.setLineNo(line.getLineNo());
            l.setDescription(line.getDescription());
            l.setQuantity(line.getQuantity());
            l.setUnitPrice(line.getUnitPrice());
            l.setDiscountPct(line.getDiscountPct());
            l.setTaxPct(line.getTaxPct());
            l.setLineTotal(line.getLineTotal());

            l.setFormattedQuantity(PdfFormatUtils.formatDecimal(line.getQuantity()));
            l.setFormattedUnitPrice(PdfFormatUtils.formatMoney(line.getUnitPrice()));
            l.setFormattedDiscountPct(PdfFormatUtils.formatDecimal(line.getDiscountPct()));
            l.setFormattedTaxPct(PdfFormatUtils.formatDecimal(line.getTaxPct()));
            l.setFormattedLineTotal(PdfFormatUtils.formatMoney(line.getLineTotal()));
            return l;
        }).toList());

        return view;
    }
}