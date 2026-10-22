package com.gestiva.sales.quote.pdf;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.platform.company.entity.CompanyProfile;
import com.gestiva.platform.company.repository.CompanyProfileRepository;
import com.gestiva.sales.quote.entity.Quote;
import com.gestiva.sales.quote.entity.QuoteLine;
import com.gestiva.sales.quote.repository.QuoteLineRepository;
import com.gestiva.sales.quote.repository.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import java.util.List;
import com.gestiva.platform.company.entity.Company;
import com.gestiva.platform.company.repository.CompanyRepository;

@Service
@Transactional(readOnly = true)
public class QuotePdfDataService {

    private final QuoteRepository quoteRepository;
    private final QuoteLineRepository quoteLineRepository;
    private final CustomerRepository customerRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final CompanyRepository companyRepository;
    private final CompanyContext companyContext;

    public QuotePdfDataService(
            QuoteRepository quoteRepository,
            QuoteLineRepository quoteLineRepository,
            CustomerRepository customerRepository,
            CompanyProfileRepository companyProfileRepository,
            CompanyRepository companyRepository,
            CompanyContext companyContext) {

        this.quoteRepository = quoteRepository;
        this.quoteLineRepository = quoteLineRepository;
        this.customerRepository = customerRepository;
        this.companyProfileRepository = companyProfileRepository;
        this.companyRepository = companyRepository;
        this.companyContext = companyContext;
    }

    private Long requireCompanyId() {

        Long companyId = companyContext.resolveCurrentCompanyId();
        if (companyId == null) {
            throw new BusinessException(
                    "Nessuna società attiva selezionata."
            );
        }
        return companyId;
    }

    public QuotePdfView buildView(Long tenantId, Long quoteId) {

        Long companyId = requireCompanyId();
        Company issuingCompany = companyRepository
                .findByTenantIdAndId(tenantId, companyId)
                .orElseThrow(() -> new NotFoundException(
                        "Società emittente non trovata"
                ));

        if (!issuingCompany.isActive()) {
            throw new BusinessException(
                    "La società emittente non è attiva."
            );
        }
        Quote quote = quoteRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        quoteId
                )
                .orElseThrow(() -> new NotFoundException(
                        "Preventivo non trovato nella società attiva"
                ));

        List<QuoteLine> lines = quoteLineRepository
                .findByTenantIdAndCompanyIdAndQuoteIdOrderByLineNoAsc(
                        tenantId,
                        companyId,
                        quoteId
                );

        Customer customer = customerRepository
                .findByTenantIdAndId(
                        tenantId,
                        quote.getCustomerId()
                )
                .orElseThrow(() -> new NotFoundException(
                        "Cliente non trovato nel tenant"
                ));

        CompanyProfile company = companyProfileRepository
                .findByTenantIdAndCompanyId(
                        tenantId,
                        companyId
                )
                .orElse(null);

        QuotePdfView view = new QuotePdfView();

        view.setCompanyName(
                firstNonBlank(
                        company != null ? company.getLegalName() : null,
                        issuingCompany.getLegalName()
                )
        );

        view.setCompanyEmail(
                firstNonBlank(
                        company != null ? company.getEmail() : null,
                        issuingCompany.getEmail()
                )
        );

        view.setCompanyPhone(
                firstNonBlank(
                        company != null ? company.getPhone() : null,
                        issuingCompany.getPhone()
                )
        );

        view.setCompanyVatNumber(
                firstNonBlank(
                        company != null ? company.getVatNumber() : null,
                        issuingCompany.getVatNumber()
                )
        );

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

        return view;
    }

    private String firstNonBlank(String preferred, String fallback) {

        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }

        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }

        return "";
    }
}