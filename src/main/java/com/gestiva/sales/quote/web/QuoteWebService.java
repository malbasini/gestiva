package com.gestiva.sales.quote.web;

import com.gestiva.common.dto.PageResponse;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.sales.quote.dto.QuoteSearchRequest;
import com.gestiva.sales.quote.entity.Quote;
import com.gestiva.sales.quote.repository.QuoteRepository;
import com.gestiva.sales.quote.repository.QuoteSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class QuoteWebService {

    private final QuoteRepository quoteRepository;
    private final CustomerRepository customerRepository;

    public QuoteWebService(QuoteRepository quoteRepository,
                           CustomerRepository customerRepository) {

        this.quoteRepository = quoteRepository;
        this.customerRepository = customerRepository;
    }

    public PageResponse<QuoteListItemView> search(Long tenantId,
                                                  QuoteSearchRequest request,
                                                  Pageable pageable) {

        var specification = QuoteSpecifications.hasTenantId(tenantId)
                .and(QuoteSpecifications.hasStatus(request.getStatus()))
                .and(QuoteSpecifications.hasCustomerId(request.getCustomerId()))
                .and(QuoteSpecifications.matchesSearch(request.getSearch()));

        var page = quoteRepository.findAll(specification, pageable);

        var content = page.getContent().stream().map(quote -> {
            QuoteListItemView item = new QuoteListItemView();
            item.setId(quote.getId());
            item.setCustomerId(quote.getCustomerId());
            item.setQuoteNumber(quote.getQuoteNumber());
            item.setQuoteDate(quote.getQuoteDate());
            item.setValidUntil(quote.getValidUntil());
            item.setStatus(quote.getStatus());
            item.setCurrencyCode(quote.getCurrencyCode());
            item.setTotalAmount(quote.getTotalAmount());

            item.setFormattedQuoteDate(PdfFormatUtils.formatDate(quote.getQuoteDate()));
            item.setFormattedValidUntil(PdfFormatUtils.formatDate(quote.getValidUntil()));
            item.setFormattedTotalAmount(PdfFormatUtils.formatMoney(quote.getTotalAmount()));
            return item;
        }).toList();

        PageResponse<QuoteListItemView> response = new PageResponse<>();
        response.setContent(content);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());

        return response;
    }

    public Page<QuoteListItemView> findPage(Long tenantId,
                                            int page,
                                            int size,
                                            String q,
                                            String status,
                                            LocalDate dateFrom,
                                            LocalDate dateTo) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("quoteDate"), Sort.Order.desc("id")));
        Specification<Quote> spec = Specification.where(byTenant(tenantId))
                .and(bySearch(q))
                .and(byStatus(status))
                .and(byDateFrom(dateFrom))
                .and(byDateTo(dateTo));
        return quoteRepository.findAll(spec, pageable).map(this::toListItemView);
    }

    private Specification<Quote> byTenant(Long tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
    }

    private Specification<Quote> bySearch(String q) {
        if (q == null || q.trim().isEmpty()) {
            return null;
        }
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("quoteNumber")), like)
        );
    }
    private Specification<Quote> byStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
    private Specification<Quote> byDateFrom(LocalDate dateFrom) {
        if (dateFrom == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("quoteDate"), dateFrom);
    }

    private Specification<Quote> byDateTo(LocalDate dateTo) {
        if (dateTo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("validUntil"), dateTo);
    }
    private QuoteListItemView toListItemView(Quote quote) {
        QuoteListItemView v = new QuoteListItemView();
        v.setId(quote.getId());
        v.setQuoteNumber(quote.getQuoteNumber());
        v.setFormattedQuoteDate(PdfFormatUtils.formatDate(quote.getQuoteDate()));
        v.setFormattedValidUntil(PdfFormatUtils.formatDate(quote.getValidUntil()));
        v.setStatus(quote.getStatus());
        v.setCurrencyCode(quote.getCurrencyCode());
        v.setFormattedTotalAmount(PdfFormatUtils.formatMoney(quote.getTotalAmount()));
        return v;

    }

}