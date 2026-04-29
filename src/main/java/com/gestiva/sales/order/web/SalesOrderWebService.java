package com.gestiva.sales.order.web;

import com.gestiva.common.dto.PageResponse;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.sales.order.dto.SalesOrderSearchRequest;
import com.gestiva.sales.order.repository.SalesOrderRepository;
import com.gestiva.sales.order.repository.SalesOrderSpecifications;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SalesOrderWebService {

    private final SalesOrderRepository salesOrderRepository;

    public SalesOrderWebService(SalesOrderRepository salesOrderRepository) {
        this.salesOrderRepository = salesOrderRepository;
    }

    public PageResponse<SalesOrderListItemView> search(Long tenantId,
                                                       SalesOrderSearchRequest request,
                                                       Pageable pageable) {

        var specification = SalesOrderSpecifications.hasTenantId(tenantId)
                .and(SalesOrderSpecifications.hasStatus(request.getStatus()))
                .and(SalesOrderSpecifications.hasCustomerId(request.getCustomerId()))
                .and(SalesOrderSpecifications.hasQuoteId(request.getQuoteId()))
                .and(SalesOrderSpecifications.matchesSearch(request.getSearch()));

        var page = salesOrderRepository.findAll(specification, pageable);

        var content = page.getContent().stream().map(order -> {
            SalesOrderListItemView item = new SalesOrderListItemView();
            item.setId(order.getId());
            item.setCustomerId(order.getCustomerId());
            item.setQuoteId(order.getQuoteId());
            item.setOrderNumber(order.getOrderNumber());
            item.setOrderDate(order.getOrderDate());
            item.setStatus(order.getStatus());
            item.setCurrencyCode(order.getCurrencyCode());
            item.setTotalAmount(order.getTotalAmount());

            item.setFormattedOrderDate(PdfFormatUtils.formatDate(order.getOrderDate()));
            item.setFormattedTotalAmount(PdfFormatUtils.formatMoney(order.getTotalAmount()));
            return item;
        }).toList();

        PageResponse<SalesOrderListItemView> response = new PageResponse<>();
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