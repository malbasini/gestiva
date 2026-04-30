package com.gestiva.dashboard.web;

import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.sales.order.repository.SalesOrderRepository;
import com.gestiva.sales.quote.repository.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class DashboardWebService {

    private final CustomerRepository customerRepository;
    private final QuoteRepository quoteRepository;
    private final SalesOrderRepository salesOrderRepository;

    public DashboardWebService(CustomerRepository customerRepository,
                               QuoteRepository quoteRepository,
                               SalesOrderRepository salesOrderRepository) {
        this.customerRepository = customerRepository;
        this.quoteRepository = quoteRepository;
        this.salesOrderRepository = salesOrderRepository;
    }

    public DashboardView build(Long tenantId) {
        DashboardView view = new DashboardView();

        view.setCustomersCount(customerRepository.countByTenantId(tenantId));
        view.setQuotesCount(quoteRepository.countByTenantId(tenantId));
        view.setOrdersCount(salesOrderRepository.countByTenantId(tenantId));

        var latestQuotes = quoteRepository.findTop5ByTenantIdOrderByQuoteDateDescIdDesc(tenantId);
        var latestOrders = salesOrderRepository.findTop5ByTenantIdOrderByOrderDateDescIdDesc(tenantId);

        BigDecimal quotesTotal = latestQuotes.stream()
                .map(q -> q.getTotalAmount() == null ? BigDecimal.ZERO : q.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ordersTotal = latestOrders.stream()
                .map(o -> o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        view.setFormattedQuotesTotal(PdfFormatUtils.formatMoney(quotesTotal));
        view.setFormattedOrdersTotal(PdfFormatUtils.formatMoney(ordersTotal));

        view.setLatestQuotes(latestQuotes.stream().map(q -> {
            DashboardQuoteItemView item = new DashboardQuoteItemView();
            item.setId(q.getId());
            item.setQuoteNumber(q.getQuoteNumber());
            item.setStatus(q.getStatus());
            item.setFormattedQuoteDate(PdfFormatUtils.formatDate(q.getQuoteDate()));
            item.setFormattedTotalAmount(PdfFormatUtils.formatMoney(q.getTotalAmount()));
            return item;
        }).toList());

        view.setLatestOrders(latestOrders.stream().map(o -> {
            DashboardOrderItemView item = new DashboardOrderItemView();
            item.setId(o.getId());
            item.setOrderNumber(o.getOrderNumber());
            item.setStatus(o.getStatus());
            item.setFormattedOrderDate(PdfFormatUtils.formatDate(o.getOrderDate()));
            item.setFormattedTotalAmount(PdfFormatUtils.formatMoney(o.getTotalAmount()));
            return item;
        }).toList());

        return view;
    }
}