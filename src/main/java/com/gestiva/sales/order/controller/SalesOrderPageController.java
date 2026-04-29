package com.gestiva.sales.order.controller;

import com.gestiva.sales.order.dto.SalesOrderSearchRequest;
import com.gestiva.sales.order.web.SalesOrderWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/orders")
public class SalesOrderPageController {

    private final SalesOrderWebService salesOrderWebService;
    private final TenantContext tenantContext;

    public SalesOrderPageController(SalesOrderWebService salesOrderWebService,
                                    TenantContext tenantContext) {
        this.salesOrderWebService = salesOrderWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public String listOrders(@RequestParam(required = false) String status,
                             @RequestParam(required = false) Long customerId,
                             @RequestParam(required = false) Long quoteId,
                             @RequestParam(required = false) String search,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(defaultValue = "orderDate") String sortBy,
                             @RequestParam(defaultValue = "desc") String sortDir,
                             @RequestParam(required = false) Long tenantId,
                             Model model) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        var pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        SalesOrderSearchRequest request = new SalesOrderSearchRequest();
        request.setStatus(status);
        request.setCustomerId(customerId);
        request.setQuoteId(quoteId);
        request.setSearch(search);

        var result = salesOrderWebService.search(resolvedTenantId, request, pageable);

        model.addAttribute("ordersPage", result);
        model.addAttribute("status", status);
        model.addAttribute("customerId", customerId);
        model.addAttribute("quoteId", quoteId);
        model.addAttribute("search", search);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("tenantId", resolvedTenantId);

        return "order/order-list";
    }
}