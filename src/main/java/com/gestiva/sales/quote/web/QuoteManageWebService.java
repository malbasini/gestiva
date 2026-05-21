package com.gestiva.sales.quote.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.common.util.NumberInputUtils;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.sales.order.repository.SalesOrderRepository;
import com.gestiva.sales.quote.dto.QuoteCreateRequest;
import com.gestiva.sales.quote.dto.QuoteLineRequest;
import com.gestiva.sales.quote.dto.QuoteResponse;
import com.gestiva.sales.quote.dto.QuoteUpdateRequest;
import com.gestiva.sales.quote.repository.QuoteLineRepository;
import com.gestiva.sales.quote.repository.QuoteRepository;
import com.gestiva.sales.quote.service.QuoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class QuoteManageWebService {

    private final QuoteService quoteService;
    private final QuoteRepository quoteRepository;
    private final QuoteLineRepository quoteLineRepository;
    private final SalesOrderRepository salesOrderRepository;

    public QuoteManageWebService(QuoteService quoteService,
                                 QuoteRepository quoteRepository,
                                 QuoteLineRepository quoteLineRepository,
                                 SalesOrderRepository salesOrderRepository) {
        this.quoteService = quoteService;
        this.quoteRepository = quoteRepository;
        this.quoteLineRepository = quoteLineRepository;
        this.salesOrderRepository = salesOrderRepository;
    }

    public QuoteForm buildCreateForm() {
        QuoteForm form = new QuoteForm();
        form.setQuoteDate(LocalDate.now());
        form.setValidUntil(LocalDate.now().plusDays(30));
        form.setStatus("DRAFT");
        form.setCurrencyCode("EUR");
        form.getLines().add(defaultLine());
        return form;
    }

    public QuoteForm buildEditForm(Long tenantId, Long quoteId) {
        validateEditable(tenantId, quoteId);
        var quote = quoteRepository.findByTenantIdAndId(tenantId, quoteId)
                .orElseThrow(() -> new NotFoundException("Preventivo non trovato"));

        var lines = quoteLineRepository.findByTenantIdAndQuoteIdOrderByLineNoAsc(tenantId, quoteId);

        QuoteForm form = new QuoteForm();
        form.setCustomerId(quote.getCustomerId());
        form.setQuoteDate(quote.getQuoteDate());
        form.setValidUntil(quote.getValidUntil());
        form.setStatus(quote.getStatus());
        form.setCurrencyCode(quote.getCurrencyCode());
        form.setNotes(quote.getNotes());
        form.setLines(lines.stream().map(line -> {
            QuoteLineForm lf = new QuoteLineForm();
            lf.setDescription(line.getDescription());
            lf.setQuantity(PdfFormatUtils.formatDecimal(line.getQuantity(),0));
            lf.setUnitPrice(PdfFormatUtils.formatMoney(line.getUnitPrice()));
            lf.setDiscountPct(PdfFormatUtils.formatDecimal(line.getDiscountPct(),0));
            lf.setTaxPct(PdfFormatUtils.formatDecimal(line.getTaxPct(),0));
            lf.setItemId(line.getItemId());
            return lf;
        }).toList());

        if (form.getLines().isEmpty()) {
            form.getLines().add(defaultLine());
        }

        return form;
    }

    public Long create(Long tenantId, QuoteForm form) {
        QuoteCreateRequest request = new QuoteCreateRequest();
        request.setCustomerId(form.getCustomerId());
        request.setQuoteDate(form.getQuoteDate());
        request.setValidUntil(form.getValidUntil());
        request.setCurrencyCode(form.getCurrencyCode());
        request.setNotes(form.getNotes());
        request.setLines(mapLines(form));
        QuoteResponse created = quoteService.create(tenantId, request);

        if (form.getStatus() != null && !form.getStatus().equals(created.getStatus())) {
            return update(tenantId, created.getId(), form);
        }

        return created.getId();
    }

    public Long update(Long tenantId, Long quoteId, QuoteForm form) {
        validateEditable(tenantId, quoteId);
        QuoteUpdateRequest request = new QuoteUpdateRequest();
        request.setCustomerId(form.getCustomerId());
        request.setQuoteDate(form.getQuoteDate());
        request.setValidUntil(form.getValidUntil());
        request.setStatus(form.getStatus());
        request.setCurrencyCode(form.getCurrencyCode());
        request.setNotes(form.getNotes());
        request.setLines(mapLines(form));

        QuoteResponse updated = quoteService.update(tenantId, quoteId, request);
        return updated.getId();
    }

    public void addLine(QuoteForm form) {
        form.getLines().add(defaultLine());
    }

    public void removeLine(QuoteForm form, int index) {
        if (form.getLines() == null || form.getLines().isEmpty()) {
            form.getLines().add(defaultLine());
            return;
        }

        if (index >= 0 && index < form.getLines().size()) {
            form.getLines().remove(index);
        }

        if (form.getLines().isEmpty()) {
            form.getLines().add(defaultLine());
        }
    }

    private List<QuoteLineRequest> mapLines(QuoteForm form) {
        return form.getLines().stream().map(line -> {
            QuoteLineRequest req = new QuoteLineRequest();
            req.setDescription(line.getDescription());
            req.setQuantity(NumberInputUtils.parseDecimal(line.getQuantity(),"quantity"));
            req.setUnitPrice(NumberInputUtils.parseDecimal(line.getUnitPrice(),"unitPrice"));
            req.setDiscountPct(defaultZero(NumberInputUtils.parseDecimal(line.getDiscountPct(),"discountPct")));
            req.setTaxPct(defaultZero(NumberInputUtils.parseDecimal(line.getTaxPct(),"taxPct")));
            req.setItemId(line.getItemId());
            return req;
        }).toList();
    }

    private QuoteLineForm defaultLine() {
        QuoteLineForm line = new QuoteLineForm();
        line.setQuantity(PdfFormatUtils.formatDecimal(new BigDecimal("1"),0));
        line.setUnitPrice(PdfFormatUtils.formatMoney(BigDecimal.ZERO));
        line.setDiscountPct(PdfFormatUtils.formatDecimal(BigDecimal.ZERO,0));
        line.setTaxPct(PdfFormatUtils.formatDecimal(new BigDecimal("22"),0));
        line.setItemId(null);
        return line;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
    private void validateEditable(Long tenantId, Long quoteId) {
        var quote = quoteRepository.findByTenantIdAndId(tenantId, quoteId)
                .orElseThrow(() -> new NotFoundException("Preventivo non trovato"));

        boolean converted = salesOrderRepository.existsByTenantIdAndQuoteId(tenantId, quoteId);

        if ("ACCEPTED".equals(quote.getStatus()) && converted) {
            throw new BusinessException("Il preventivo è già stato convertito in ordine e non può più essere modificato.");
        }
    }
    public QuoteTotalsPreviewView calculatePreviewTotals(QuoteForm form) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;

        if (form.getLines() != null) {
            for (QuoteLineForm line : form.getLines()) {
                if (line == null) {
                    continue;
                }

                BigDecimal quantity = defaultZero(NumberInputUtils.parseDecimal(line.getQuantity(),"quantity"));
                BigDecimal unitPrice = defaultZero(NumberInputUtils.parseDecimal(line.getUnitPrice(),"unitPrice"));
                BigDecimal discountPct = defaultZero(NumberInputUtils.parseDecimal(line.getDiscountPct(),"discount"));
                BigDecimal taxPct = defaultZero(NumberInputUtils.parseDecimal(line.getTaxPct(),"tax pct"));

                BigDecimal gross = quantity.multiply(unitPrice);

                BigDecimal discountAmount = gross
                        .multiply(discountPct)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                BigDecimal taxableLine = gross.subtract(discountAmount);

                BigDecimal taxAmount = taxableLine
                        .multiply(taxPct)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                subtotal = subtotal.add(taxableLine);
                tax = tax.add(taxAmount);
            }
        }

        BigDecimal total = subtotal.add(tax);

        QuoteTotalsPreviewView view = new QuoteTotalsPreviewView();
        view.setFormattedSubtotalAmount(PdfFormatUtils.formatMoney(subtotal));
        view.setFormattedTaxAmount(PdfFormatUtils.formatMoney(tax));
        view.setFormattedTotalAmount(PdfFormatUtils.formatMoney(total));
        return view;
    }
}