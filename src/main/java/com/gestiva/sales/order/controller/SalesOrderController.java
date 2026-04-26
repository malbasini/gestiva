package com.gestiva.sales.order.controller;

import com.gestiva.sales.order.dto.SalesOrderResponse;
import com.gestiva.sales.order.service.SalesOrderService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;
    private final TenantContext tenantContext;

    public SalesOrderController(SalesOrderService salesOrderService,
                                TenantContext tenantContext) {
        this.salesOrderService = salesOrderService;
        this.tenantContext = tenantContext;
    }

    // TODO: sostituire con tenant context reale
    private Long getTenantId() {
        return tenantContext.getCurrentTenantId();
    }

    @GetMapping
    public List<SalesOrderResponse> findAll() {
        return salesOrderService.findAll(getTenantId());
    }

    @GetMapping("/{id}")
    public SalesOrderResponse findById(@PathVariable Long id) {
        return salesOrderService.findById(getTenantId(), id);
    }

    @PostMapping("/from-quote/{quoteId}")
    @ResponseStatus(HttpStatus.CREATED)
    public SalesOrderResponse convertFromQuote(@PathVariable Long quoteId) {
        return salesOrderService.convertFromQuote(getTenantId(), quoteId);
    }
}