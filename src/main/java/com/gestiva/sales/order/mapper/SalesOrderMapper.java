package com.gestiva.sales.order.mapper;

import com.gestiva.sales.order.dto.SalesOrderLineResponse;
import com.gestiva.sales.order.dto.SalesOrderResponse;
import com.gestiva.sales.order.entity.SalesOrder;
import com.gestiva.sales.order.entity.SalesOrderLine;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SalesOrderMapper {

    public SalesOrderResponse toResponse(SalesOrder order, List<SalesOrderLine> lines) {
        SalesOrderResponse response = new SalesOrderResponse();
        response.setId(order.getId());
        response.setCustomerId(order.getCustomerId());
        response.setQuoteId(order.getQuoteId());
        response.setOrderNumber(order.getOrderNumber());
        response.setOrderDate(order.getOrderDate());
        response.setStatus(order.getStatus());
        response.setCurrencyCode(order.getCurrencyCode());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setLines(lines.stream().map(this::toLineResponse).toList());
        return response;
    }

    public SalesOrderLineResponse toLineResponse(SalesOrderLine line) {
        SalesOrderLineResponse response = new SalesOrderLineResponse();
        response.setId(line.getId());
        response.setLineNo(line.getLineNo());
        response.setDescription(line.getDescription());
        response.setQuantity(line.getQuantity());
        response.setUnitPrice(line.getUnitPrice());
        response.setLineTotal(line.getLineTotal());
        return response;
    }
}