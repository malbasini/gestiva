package com.gestiva.sales.order.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.sales.order.web.SalesOrderDetailWebService;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import com.gestiva.warehouse.location.repository.WarehouseLocationRepository;
import java.util.ArrayList;
import java.util.List;
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
    private final WarehouseLocationRepository warehouseLocationRepository;

    public record LocationOption(
            Long id,
            Long warehouseId,
            String code,
            String name
    ) {}

    public SalesOrderDetailPageController(
            SalesOrderDetailWebService salesOrderDetailWebService,
            TenantContext tenantContext,
            CompanyContext companyContext,
            WarehouseRepository warehouseRepository,
            WarehouseLocationRepository warehouseLocationRepository) {

        this.salesOrderDetailWebService = salesOrderDetailWebService;
        this.tenantContext = tenantContext;
        this.companyContext = companyContext;
        this.warehouseRepository = warehouseRepository;
        this.warehouseLocationRepository = warehouseLocationRepository;
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

            List<LocationOption> locationOptions = new ArrayList<>();

            for (var warehouse : warehouses) {
                var locations = warehouseLocationRepository
                        .findByTenantIdAndWarehouseIdAndActiveTrueOrderByCodeAsc(
                                resolvedTenantId,
                                warehouse.getId()
                        );

                for (var location : locations) {
                    locationOptions.add(new LocationOption(
                            location.getId(),
                            warehouse.getId(),
                            location.getCode(),
                            location.getName()
                    ));
                }
            }

            model.addAttribute(
                    "locationOptions",
                    List.copyOf(locationOptions)
            );
        }
        return "order/order-detail";
    }
}