package com.gestiva.sales.order.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.sales.order.dto.SalesOrderResponse;
import com.gestiva.sales.order.entity.SalesOrder;
import com.gestiva.sales.order.entity.SalesOrderLine;
import com.gestiva.sales.order.mapper.SalesOrderMapper;
import com.gestiva.sales.order.repository.SalesOrderLineRepository;
import com.gestiva.sales.order.repository.SalesOrderRepository;
import com.gestiva.sales.quote.entity.Quote;
import com.gestiva.sales.quote.entity.QuoteLine;
import com.gestiva.sales.quote.repository.QuoteLineRepository;
import com.gestiva.sales.quote.repository.QuoteRepository;
import com.gestiva.sales.sequence.entity.DocumentSequence;
import com.gestiva.sales.sequence.repository.DocumentSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class SalesOrderService {

    private static final String ORDER_SEQUENCE_CODE = "ORDER";
    private static final String DEFAULT_ORDER_STATUS = "CONFIRMED";
    private static final Set<String> CONVERTIBLE_QUOTE_STATUSES =
            Set.of("SENT", "ACCEPTED");

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final QuoteRepository quoteRepository;
    private final QuoteLineRepository quoteLineRepository;
    private final DocumentSequenceRepository documentSequenceRepository;
    private final SalesOrderMapper salesOrderMapper;

    public SalesOrderService(SalesOrderRepository salesOrderRepository,
                             SalesOrderLineRepository salesOrderLineRepository,
                             QuoteRepository quoteRepository,
                             QuoteLineRepository quoteLineRepository,
                             DocumentSequenceRepository documentSequenceRepository,
                             SalesOrderMapper salesOrderMapper) {
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderLineRepository = salesOrderLineRepository;
        this.quoteRepository = quoteRepository;
        this.quoteLineRepository = quoteLineRepository;
        this.documentSequenceRepository = documentSequenceRepository;
        this.salesOrderMapper = salesOrderMapper;
    }

    public SalesOrderResponse convertFromQuote(Long tenantId, Long quoteId) {
        Quote quote = quoteRepository.findByTenantIdAndId(tenantId, quoteId)
                .orElseThrow(() -> new NotFoundException("Preventivo non trovato"));

        validateQuoteConvertible(tenantId, quote);

        List<QuoteLine> quoteLines = quoteLineRepository.findByTenantIdAndQuoteIdOrderByLineNoAsc(tenantId, quoteId);
        if (quoteLines.isEmpty()) {
            throw new BusinessException("Il preventivo non contiene righe convertibili");
        }

        String orderNumber = nextOrderNumber(tenantId, LocalDate.now());

        SalesOrder order = new SalesOrder();
        order.setTenantId(tenantId);
        order.setCustomerId(quote.getCustomerId());
        order.setQuoteId(quote.getId());
        order.setOrderNumber(orderNumber);
        order.setOrderDate(LocalDate.now());
        order.setStatus(DEFAULT_ORDER_STATUS);
        order.setCurrencyCode(quote.getCurrencyCode());
        order.setTotalAmount(quote.getTotalAmount());

        SalesOrder savedOrder = salesOrderRepository.save(order);

        List<SalesOrderLine> savedLines = copyQuoteLinesToOrder(tenantId, savedOrder.getId(), quoteLines);

        if (!"ACCEPTED".equals(quote.getStatus())) {
            quote.setStatus("ACCEPTED");
            quoteRepository.save(quote);
        }

        return salesOrderMapper.toResponse(savedOrder, savedLines);
    }

    @Transactional(readOnly = true)
    public SalesOrderResponse findById(Long tenantId, Long orderId) {
        SalesOrder order = salesOrderRepository.findByTenantIdAndId(tenantId, orderId)
                .orElseThrow(() -> new NotFoundException("Ordine non trovato"));

        List<SalesOrderLine> lines =
                salesOrderLineRepository.findByTenantIdAndSalesOrderIdOrderByLineNoAsc(tenantId, orderId);

        return salesOrderMapper.toResponse(order, lines);
    }

    @Transactional(readOnly = true)
    public List<SalesOrderResponse> findAll(Long tenantId) {
        List<SalesOrder> orders = salesOrderRepository.findByTenantIdOrderByOrderDateDescIdDesc(tenantId);
        List<SalesOrderResponse> responses = new ArrayList<>();

        for (SalesOrder order : orders) {
            List<SalesOrderLine> lines =
                    salesOrderLineRepository.findByTenantIdAndSalesOrderIdOrderByLineNoAsc(tenantId, order.getId());
            responses.add(salesOrderMapper.toResponse(order, lines));
        }

        return responses;
    }

    private void validateQuoteConvertible(Long tenantId, Quote quote) {
        if (!CONVERTIBLE_QUOTE_STATUSES.contains(quote.getStatus())) {
            throw new BusinessException(
                    "Il preventivo con stato '" + quote.getStatus() + "' non è convertibile in ordine");
        }

        if (salesOrderRepository.existsByTenantIdAndQuoteId(tenantId, quote.getId())) {
            throw new BusinessException("Esiste già un ordine generato da questo preventivo");
        }
    }

    private List<SalesOrderLine> copyQuoteLinesToOrder(Long tenantId,
                                                       Long salesOrderId,
                                                       List<QuoteLine> quoteLines) {
        List<SalesOrderLine> orderLines = new ArrayList<>();

        for (QuoteLine quoteLine : quoteLines) {
            SalesOrderLine orderLine = new SalesOrderLine();
            orderLine.setTenantId(tenantId);
            orderLine.setSalesOrderId(salesOrderId);
            orderLine.setLineNo(quoteLine.getLineNo());
            orderLine.setDescription(quoteLine.getDescription());
            orderLine.setQuantity(quoteLine.getQuantity());
            orderLine.setUnitPrice(quoteLine.getUnitPrice());
            orderLine.setLineTotal(quoteLine.getLineTotal());
            orderLine.setDiscountPct(quoteLine.getDiscountPct());
            orderLine.setTaxPct(quoteLine.getTaxPct());

            orderLines.add(orderLine);
        }

        return salesOrderLineRepository.saveAll(orderLines);
    }

    private String nextOrderNumber(Long tenantId, LocalDate orderDate) {
        int year = orderDate.getYear();

        DocumentSequence sequence = documentSequenceRepository
                .findByTenantIdAndSequenceCodeAndYearValue(tenantId, ORDER_SEQUENCE_CODE, year)
                .orElseGet(() -> {
                    DocumentSequence seq = new DocumentSequence();
                    seq.setTenantId(tenantId);
                    seq.setSequenceCode(ORDER_SEQUENCE_CODE);
                    seq.setYearValue(year);
                    seq.setNextNumber(1);
                    seq.setPrefix("ORD");
                    return seq;
                });

        int progressive = sequence.getNextNumber();
        String prefix = (sequence.getPrefix() == null || sequence.getPrefix().isBlank())
                ? "ORD"
                : sequence.getPrefix();

        sequence.setNextNumber(progressive + 1);
        documentSequenceRepository.save(sequence);

        return prefix + "-" + year + "-" + String.format("%05d", progressive);
    }
}