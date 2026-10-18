package com.gestiva.sales.order.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.sales.order.web.SalesOrderDetailWebService;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/orders")
public class SalesOrderDetailPageController {

    private final SalesOrderDetailWebService salesOrderDetailWebService;
    private final TenantContext tenantContext;
    private final CompanyContext companyContext;
    private final WarehouseRepository warehouseRepository;

    public SalesOrderDetailPageController(
            SalesOrderDetailWebService salesOrderDetailWebService,
            TenantContext tenantContext,
            CompanyContext companyContext,
            WarehouseRepository warehouseRepository) {

        this.salesOrderDetailWebService = salesOrderDetailWebService;
        this.tenantContext = tenantContext;
        this.companyContext = companyContext;
        this.warehouseRepository = warehouseRepository;
    }
    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            @RequestParam(required = false) Long tenantId,
            Model model) {
        Long resolvedTenantId = tenantId != null
                ? tenantId
                : tenantContext.getCurrentTenantId();
        // Il servizio verifica già che l'ordine appartenga
        // alla società attiva.
        var order = salesOrderDetailWebService.getDetail(
                resolvedTenantId,
                id
        );
        model.addAttribute("order", order);
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("activeMenu", "orders");
        // Carichiamo i magazzini soltanto quando
        // è possibile generare un DDT.
        if (order.isDeliveryNoteCreatable()) {
            Long companyId = companyContext.resolveCurrentCompanyId();
            if (companyId == null) {
                throw new BusinessException(
                        "Nessuna società attiva selezionata."
                );
            }
            var warehouses = warehouseRepository
                    .findByTenantIdAndCompanyIdAndActiveTrueOrderByCodeAsc(
                            resolvedTenantId,
                            companyId
                    );
            model.addAttribute("warehouses", warehouses);
        }
        return "order/order-detail";
    }
}