package com.gestiva.sales.quote.web;

import com.gestiva.common.dto.PageResponse;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.sales.quote.dto.QuoteSearchRequest;
import com.gestiva.sales.quote.repository.QuoteRepository;
import com.gestiva.sales.quote.repository.QuoteSpecifications;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class QuoteWebService {

    private final QuoteRepository quoteRepository;

    public QuoteWebService(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
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
}