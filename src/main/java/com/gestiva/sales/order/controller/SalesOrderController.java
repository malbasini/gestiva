package com.gestiva.sales.order.controller;

import com.gestiva.common.dto.PageResponse;
import com.gestiva.sales.order.dto.SalesOrderResponse;
import com.gestiva.sales.order.dto.SalesOrderSearchRequest;
import com.gestiva.sales.order.service.SalesOrderService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public PageResponse<SalesOrderResponse> search(@RequestParam(required = false) String status,
                                                   @RequestParam(required = false) Long customerId,
                                                   @RequestParam(required = false) Long quoteId,
                                                   @RequestParam(required = false) String search,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(defaultValue = "orderDate") String sortBy,
                                                   @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        SalesOrderSearchRequest request = new SalesOrderSearchRequest();
        request.setStatus(status);
        request.setCustomerId(customerId);
        request.setQuoteId(quoteId);
        request.setSearch(search);

        return salesOrderService.search(tenantContext.getCurrentTenantId(), request, pageable);
    }

    @GetMapping("/{id}")
    public SalesOrderResponse findById(@PathVariable Long id) {
        return salesOrderService.findById(tenantContext.getCurrentTenantId(), id);
    }

    @PostMapping("/from-quote/{quoteId}")
    @ResponseStatus(HttpStatus.CREATED)
    public SalesOrderResponse convertFromQuote(@PathVariable Long quoteId) {
        return salesOrderService.convertFromQuote(tenantContext.getCurrentTenantId(), quoteId);
    }
}