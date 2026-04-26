package com.gestiva.sales.quote.pdf;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.platform.company.entity.CompanyProfile;
import com.gestiva.platform.company.repository.CompanyProfileRepository;
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

    public QuotePdfDataService(QuoteRepository quoteRepository,
                               QuoteLineRepository quoteLineRepository,
                               CustomerRepository customerRepository,
                               CompanyProfileRepository companyProfileRepository) {
        this.quoteRepository = quoteRepository;
        this.quoteLineRepository = quoteLineRepository;
        this.customerRepository = customerRepository;
        this.companyProfileRepository = companyProfileRepository;
    }

    public QuotePdfView buildView(Long tenantId, Long quoteId) {
        Quote quote = quoteRepository.findByTenantIdAndId(tenantId, quoteId)
                .orElseThrow(() -> new NotFoundException("Preventivo non trovato"));

        List<QuoteLine> lines = quoteLineRepository.findByTenantIdAndQuoteIdOrderByLineNoAsc(tenantId, quoteId);

        Customer customer = customerRepository.findByTenantIdAndId(tenantId, quote.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Cliente non trovato"));

        CompanyProfile company = companyProfileRepository.findByTenantId(tenantId)
                .orElse(null);

        QuotePdfView view = new QuotePdfView();

        if (company != null) {
            view.setCompanyName(company.getLegalName());
            view.setCompanyEmail(company.getEmail());
            view.setCompanyPhone(company.getPhone());
            view.setCompanyVatNumber(company.getVatNumber());
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

        view.setLines(lines.stream().map(line -> {
            QuotePdfLineView l = new QuotePdfLineView();
            l.setLineNo(line.getLineNo());
            l.setDescription(line.getDescription());
            l.setQuantity(line.getQuantity());
            l.setUnitPrice(line.getUnitPrice());
            l.setDiscountPct(line.getDiscountPct());
            l.setTaxPct(line.getTaxPct());
            l.setLineTotal(line.getLineTotal());
            return l;
        }).toList());

        return view;
    }
}