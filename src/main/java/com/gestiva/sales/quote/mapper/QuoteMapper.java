package com.gestiva.sales.quote.mapper;

import com.gestiva.sales.quote.dto.QuoteLineResponse;
import com.gestiva.sales.quote.dto.QuoteResponse;
import com.gestiva.sales.quote.entity.Quote;
import com.gestiva.sales.quote.entity.QuoteLine;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuoteMapper {

    public QuoteResponse toResponse(Quote quote, List<QuoteLine> lines) {
        QuoteResponse response = new QuoteResponse();
        response.setId(quote.getId());
        response.setCustomerId(quote.getCustomerId());
        response.setQuoteNumber(quote.getQuoteNumber());
        response.setQuoteDate(quote.getQuoteDate());
        response.setValidUntil(quote.getValidUntil());
        response.setStatus(quote.getStatus());
        response.setCurrencyCode(quote.getCurrencyCode());
        response.setSubtotalAmount(quote.getSubtotalAmount());
        response.setTaxAmount(quote.getTaxAmount());
        response.setTotalAmount(quote.getTotalAmount());
        response.setNotes(quote.getNotes());
        response.setCreatedAt(quote.getCreatedAt());
        response.setUpdatedAt(quote.getUpdatedAt());
        response.setLines(lines.stream().map(this::toLineResponse).toList());
        return response;
    }

    public QuoteLineResponse toLineResponse(QuoteLine line) {
        QuoteLineResponse response = new QuoteLineResponse();
        response.setId(line.getId());
        response.setLineNo(line.getLineNo());
        response.setDescription(line.getDescription());
        response.setQuantity(line.getQuantity());
        response.setUnitPrice(line.getUnitPrice());
        response.setDiscountPct(line.getDiscountPct());
        response.setTaxPct(line.getTaxPct());
        response.setLineTotal(line.getLineTotal());
        return response;
    }
}
