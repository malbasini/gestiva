package com.gestiva.sales.quote.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.sales.quote.dto.QuoteCreateRequest;
import com.gestiva.sales.quote.dto.QuoteLineRequest;
import com.gestiva.sales.quote.dto.QuoteResponse;
import com.gestiva.sales.quote.dto.QuoteUpdateRequest;
import com.gestiva.sales.quote.entity.Quote;
import com.gestiva.sales.quote.entity.QuoteLine;
import com.gestiva.sales.quote.mapper.QuoteMapper;
import com.gestiva.sales.quote.repository.QuoteLineRepository;
import com.gestiva.sales.quote.repository.QuoteRepository;
import com.gestiva.sales.sequence.entity.DocumentSequence;
import com.gestiva.sales.sequence.repository.DocumentSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class QuoteService {

    private static final String SEQUENCE_CODE = "QUOTE";
    private static final String DEFAULT_STATUS = "DRAFT";
    private static final String DEFAULT_CURRENCY = "EUR";
    private static final Set<String> ALLOWED_STATUSES =
            Set.of("DRAFT", "SENT", "ACCEPTED", "REJECTED");

    private final QuoteRepository quoteRepository;
    private final QuoteLineRepository quoteLineRepository;
    private final CustomerRepository customerRepository;
    private final DocumentSequenceRepository documentSequenceRepository;
    private final QuoteMapper quoteMapper;

    public QuoteService(QuoteRepository quoteRepository,
                        QuoteLineRepository quoteLineRepository,
                        CustomerRepository customerRepository,
                        DocumentSequenceRepository documentSequenceRepository,
                        QuoteMapper quoteMapper) {
        this.quoteRepository = quoteRepository;
        this.quoteLineRepository = quoteLineRepository;
        this.customerRepository = customerRepository;
        this.documentSequenceRepository = documentSequenceRepository;
        this.quoteMapper = quoteMapper;
    }

    @Transactional(readOnly = true)
    public List<QuoteResponse> findAll(Long tenantId) {
        List<Quote> quotes = quoteRepository.findByTenantIdOrderByQuoteDateDescIdDesc(tenantId);
        List<QuoteResponse> responses = new ArrayList<>();

        for (Quote quote : quotes) {
            List<QuoteLine> lines = quoteLineRepository.findByTenantIdAndQuoteIdOrderByLineNoAsc(tenantId, quote.getId());
            responses.add(quoteMapper.toResponse(quote, lines));
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public QuoteResponse findById(Long tenantId, Long id) {
        Quote quote = quoteRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Preventivo non trovato"));

        List<QuoteLine> lines = quoteLineRepository.findByTenantIdAndQuoteIdOrderByLineNoAsc(tenantId, id);
        return quoteMapper.toResponse(quote, lines);
    }

    public QuoteResponse create(Long tenantId, QuoteCreateRequest request) {
        validateCustomerExists(tenantId, request.getCustomerId());
        validateLines(request.getLines());

        String quoteNumber = nextQuoteNumber(tenantId, request.getQuoteDate());
        Totals totals = calculateTotals(request.getLines());

        Quote quote = new Quote();
        quote.setTenantId(tenantId);
        quote.setCustomerId(request.getCustomerId());
        quote.setQuoteNumber(quoteNumber);
        quote.setQuoteDate(request.getQuoteDate());
        quote.setValidUntil(request.getValidUntil());
        quote.setStatus(DEFAULT_STATUS);
        quote.setCurrencyCode(defaultIfBlank(request.getCurrencyCode(), DEFAULT_CURRENCY));
        quote.setSubtotalAmount(totals.subtotal());
        quote.setTaxAmount(totals.tax());
        quote.setTotalAmount(totals.total());
        quote.setNotes(request.getNotes());

        Quote savedQuote = quoteRepository.save(quote);
        List<QuoteLine> savedLines = saveQuoteLines(tenantId, savedQuote.getId(), request.getLines());

        return quoteMapper.toResponse(savedQuote, savedLines);
    }

    public QuoteResponse update(Long tenantId, Long id, QuoteUpdateRequest request) {
        Quote quote = quoteRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Preventivo non trovato"));

        validateCustomerExists(tenantId, request.getCustomerId());
        validateLines(request.getLines());
        validateStatus(request.getStatus());

        Totals totals = calculateTotals(request.getLines());

        quote.setCustomerId(request.getCustomerId());
        quote.setQuoteDate(request.getQuoteDate());
        quote.setValidUntil(request.getValidUntil());
        quote.setStatus(defaultIfBlank(request.getStatus(), DEFAULT_STATUS));
        quote.setCurrencyCode(defaultIfBlank(request.getCurrencyCode(), DEFAULT_CURRENCY));
        quote.setSubtotalAmount(totals.subtotal());
        quote.setTaxAmount(totals.tax());
        quote.setTotalAmount(totals.total());
        quote.setNotes(request.getNotes());

        Quote savedQuote = quoteRepository.save(quote);

        quoteLineRepository.deleteByTenantIdAndQuoteId(tenantId, savedQuote.getId());
        List<QuoteLine> savedLines = saveQuoteLines(tenantId, savedQuote.getId(), request.getLines());

        return quoteMapper.toResponse(savedQuote, savedLines);
    }

    public void delete(Long tenantId, Long id) {
        Quote quote = quoteRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Preventivo non trovato"));

        quoteLineRepository.deleteByTenantIdAndQuoteId(tenantId, quote.getId());
        quoteRepository.delete(quote);
    }

    private void validateCustomerExists(Long tenantId, Long customerId) {
        customerRepository.findByTenantIdAndId(tenantId, customerId)
                .orElseThrow(() -> new BusinessException("Cliente non valido per il tenant corrente"));
    }

    private void validateStatus(String status) {
        if (status == null || status.isBlank()) {
            return;
        }

        if (!ALLOWED_STATUSES.contains(status)) {
            throw new BusinessException("Stato preventivo non valido: " + status);
        }
    }

    private void validateLines(List<QuoteLineRequest> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException("Il preventivo deve contenere almeno una riga");
        }

        for (int i = 0; i < lines.size(); i++) {
            QuoteLineRequest line = lines.get(i);

            if (line.getDescription() == null || line.getDescription().isBlank()) {
                throw new BusinessException("La descrizione della riga " + (i + 1) + " è obbligatoria");
            }

            if (line.getQuantity() == null || line.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("La quantità della riga " + (i + 1) + " deve essere maggiore di zero");
            }

            if (line.getUnitPrice() == null || line.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("Il prezzo unitario della riga " + (i + 1) + " non può essere negativo");
            }

            if (line.getDiscountPct() != null &&
                    (line.getDiscountPct().compareTo(BigDecimal.ZERO) < 0 ||
                            line.getDiscountPct().compareTo(new BigDecimal("100")) > 0)) {
                throw new BusinessException("Lo sconto % della riga " + (i + 1) + " deve essere tra 0 e 100");
            }

            if (line.getTaxPct() != null &&
                    (line.getTaxPct().compareTo(BigDecimal.ZERO) < 0 ||
                            line.getTaxPct().compareTo(new BigDecimal("100")) > 0)) {
                throw new BusinessException("L'aliquota IVA della riga " + (i + 1) + " deve essere tra 0 e 100");
            }
        }
    }

    private Totals calculateTotals(List<QuoteLineRequest> lines) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;

        for (QuoteLineRequest line : lines) {
            BigDecimal quantity = scale(line.getQuantity(), 3);
            BigDecimal unitPrice = scale(line.getUnitPrice(), 2);
            BigDecimal discountPct = percentOrZero(line.getDiscountPct());
            BigDecimal taxPct = percentOrZero(line.getTaxPct());

            BigDecimal gross = quantity.multiply(unitPrice);
            BigDecimal discountAmount = gross.multiply(discountPct)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            BigDecimal netLine = gross.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

            BigDecimal taxAmount = netLine.multiply(taxPct)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            subtotal = subtotal.add(netLine);
            tax = tax.add(taxAmount);
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        tax = tax.setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);

        return new Totals(subtotal, tax, total);
    }

    private List<QuoteLine> saveQuoteLines(Long tenantId, Long quoteId, List<QuoteLineRequest> requests) {
        List<QuoteLine> entities = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            QuoteLineRequest request = requests.get(i);

            BigDecimal quantity = scale(request.getQuantity(), 3);
            BigDecimal unitPrice = scale(request.getUnitPrice(), 2);
            BigDecimal discountPct = percentOrZero(request.getDiscountPct());
            BigDecimal taxPct = percentOrZero(request.getTaxPct());

            BigDecimal gross = quantity.multiply(unitPrice);
            BigDecimal discountAmount = gross.multiply(discountPct)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal netLine = gross.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

            QuoteLine line = new QuoteLine();
            line.setTenantId(tenantId);
            line.setQuoteId(quoteId);
            line.setLineNo(i + 1);
            line.setDescription(request.getDescription().trim());
            line.setQuantity(quantity);
            line.setUnitPrice(unitPrice);
            line.setDiscountPct(discountPct);
            line.setTaxPct(taxPct);
            line.setLineTotal(netLine);

            entities.add(line);
        }

        return quoteLineRepository.saveAll(entities);
    }

    private String nextQuoteNumber(Long tenantId, LocalDate quoteDate) {
        int year = quoteDate.getYear();

        DocumentSequence sequence = documentSequenceRepository
                .findByTenantIdAndSequenceCodeAndYearValue(tenantId, SEQUENCE_CODE, year)
                .orElseGet(() -> {
                    DocumentSequence seq = new DocumentSequence();
                    seq.setTenantId(tenantId);
                    seq.setSequenceCode(SEQUENCE_CODE);
                    seq.setYearValue(year);
                    seq.setNextNumber(1);
                    seq.setPrefix("PRE");
                    return seq;
                });

        int progressive = sequence.getNextNumber();
        String prefix = defaultIfBlank(sequence.getPrefix(), "PRE");

        sequence.setNextNumber(progressive + 1);
        documentSequenceRepository.save(sequence);

        return prefix + "-" + year + "-" + String.format("%05d", progressive);
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    private BigDecimal percentOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scale(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP);
    }

    private record Totals(BigDecimal subtotal, BigDecimal tax, BigDecimal total) {
    }

    public void validateDates(LocalDate quoteDate, LocalDate validUntil) {
        if (validUntil.isBefore(quoteDate)) {
            throw new BusinessException("La data di validità non può essere precedente alla data del preventivo");
        }
    }
}