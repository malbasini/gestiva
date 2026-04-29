package com.gestiva.sales.order.web;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.sales.order.repository.SalesOrderLineRepository;
import com.gestiva.sales.order.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SalesOrderDetailWebService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final CustomerRepository customerRepository;

    public SalesOrderDetailWebService(SalesOrderRepository salesOrderRepository,
                                      SalesOrderLineRepository salesOrderLineRepository,
                                      CustomerRepository customerRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderLineRepository = salesOrderLineRepository;
        this.customerRepository = customerRepository;
    }

    public SalesOrderDetailView getDetail(Long tenantId, Long orderId) {
        var order = salesOrderRepository.findByTenantIdAndId(tenantId, orderId)
                .orElseThrow(() -> new NotFoundException("Ordine non trovato"));

        var customer = customerRepository.findByTenantIdAndId(tenantId, order.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Cliente non trovato"));

        var lines = salesOrderLineRepository.findByTenantIdAndSalesOrderIdOrderByLineNoAsc(tenantId, orderId);

        SalesOrderDetailView view = new SalesOrderDetailView();
        view.setId(order.getId());
        view.setCustomerId(customer.getId());
        view.setCustomerName(customer.getName());
        view.setQuoteId(order.getQuoteId());
        view.setOrderNumber(order.getOrderNumber());
        view.setStatus(order.getStatus());
        view.setCurrencyCode(order.getCurrencyCode());

        view.setFormattedOrderDate(PdfFormatUtils.formatDate(order.getOrderDate()));
        view.setFormattedSubtotalAmount(PdfFormatUtils.formatMoney(order.getSubtotalAmount()));
        view.setFormattedTaxAmount(PdfFormatUtils.formatMoney(order.getTaxAmount()));
        view.setFormattedTotalAmount(PdfFormatUtils.formatMoney(order.getTotalAmount()));

        view.setLines(lines.stream().map(line -> {
            SalesOrderDetailLineView l = new SalesOrderDetailLineView();
            l.setLineNo(line.getLineNo());
            l.setDescription(line.getDescription());
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