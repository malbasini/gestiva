package com.gestiva.warehouse.inventory.movement.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.warehouse.location.service.WarehouseService;
import com.gestiva.warehouse.location.service.WarehouseLocationService;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Controller
@RequestMapping("/inventory/transfers")
@PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
public class InventoryLocationTransferAvailabilityController {

    private final JdbcTemplate jdbc;
    private final TenantContext tenantContext;
    private final CompanyContext companyContext;
    private final TenantRepository tenantRepository;
    private final WarehouseService warehouseService;
    private final WarehouseLocationService locationService;

    public InventoryLocationTransferAvailabilityController(
            JdbcTemplate jdbc,
            TenantContext tenantContext,
            CompanyContext companyContext,
            TenantRepository tenantRepository,
            WarehouseService warehouseService,
            WarehouseLocationService locationService) {

        this.jdbc = jdbc;
        this.tenantContext = tenantContext;
        this.companyContext = companyContext;
        this.tenantRepository = tenantRepository;
        this.warehouseService = warehouseService;
        this.locationService = locationService;
    }

    @GetMapping("/availability")
    @ResponseBody
    @Transactional(readOnly = true)
    public Map<String, String> availability(
            @RequestParam Long warehouseId,
            @RequestParam Long itemId,
            @RequestParam(name = "sourceLocationId", required = false)
            String sourceSelection) {

        Long tenantId = tenantContext.getCurrentTenantId();

        boolean professional = tenantId != null
                && tenantRepository.findById(tenantId)
                .map(t -> t.isSubscriptionActive()
                        && "PROFESSIONAL".equalsIgnoreCase(
                                t.getSubscriptionPlan()))
                .orElse(false);

        if (!professional) {
            throw new AccessDeniedException(
                    "Disponibilità FIFO riservata al piano Professional."
            );
        }

        Long companyId = companyContext.requireCurrentCompanyId();

        if (warehouseId == null || warehouseId <= 0
                || itemId == null || itemId <= 0) {
            throw new ResponseStatusException(
                    BAD_REQUEST, "Selezione non valida."
            );
        }

        final Long sourceLocationId;

        try {
            sourceLocationId =
                    InventoryLocationTransferSourceSelection
                            .parse(sourceSelection);
        } catch (BusinessException ex) {
            throw new ResponseStatusException(
                    BAD_REQUEST, ex.getMessage(), ex
            );
        }

        boolean validWarehouse = warehouseService.findActive(
                tenantId, companyId
        ).stream().anyMatch(w ->
                warehouseId.equals(w.getId())
        );

        if (!validWarehouse) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Magazzino non disponibile nella società attiva."
            );
        }

        if (sourceLocationId == null) {
            return unlocatedAvailability(
                    tenantId, companyId, warehouseId, itemId
            );
        }

        boolean validLocation = locationService.findAll(
                tenantId, companyId, warehouseId
        ).stream().anyMatch(l ->
                l.isActive()
                        && sourceLocationId.equals(l.getId())
        );

        if (!validLocation) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Ubicazione non disponibile nel magazzino."
            );
        }

        String sql = """
            SELECT COALESCE(SUM(l.remaining_qty), 0)
            FROM inventory_layer l
            JOIN warehouse w
              ON w.id = l.warehouse_id
             AND w.tenant_id = l.tenant_id
             AND w.company_id = l.company_id
            JOIN warehouse_location loc
              ON loc.id = l.location_id
             AND loc.warehouse_id = w.id
             AND loc.tenant_id = l.tenant_id
            JOIN item i
              ON i.id = l.item_id
             AND i.tenant_id = l.tenant_id
            WHERE l.tenant_id = ?
              AND l.company_id = ?
              AND l.warehouse_id = ?
              AND l.item_id = ?
              AND l.location_id = ?
              AND l.remaining_qty > 0
            """;

        BigDecimal available = jdbc.queryForObject(
                sql,
                BigDecimal.class,
                tenantId,
                companyId,
                warehouseId,
                itemId,
                sourceLocationId
        );

        if (available == null) {
            available = BigDecimal.ZERO;
        }

        return Map.of(
                "availableQty",
                available.toPlainString()
        );
    }

    /**
     * Disponibilita FIFO limitata esclusivamente ai layer
     * privi di ubicazione fisica.
     *
     * Il contesto tenant/company/warehouse e gia stato
     * verificato dall'endpoint chiamante.
     */
    private Map<String, String> unlocatedAvailability(
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long itemId) {

        String sql = """
            SELECT COALESCE(SUM(l.remaining_qty), 0)
            FROM inventory_layer l
            JOIN warehouse w
              ON w.id = l.warehouse_id
             AND w.tenant_id = l.tenant_id
             AND w.company_id = l.company_id
            JOIN item i
              ON i.id = l.item_id
             AND i.tenant_id = l.tenant_id
            WHERE l.tenant_id = ?
              AND l.company_id = ?
              AND l.warehouse_id = ?
              AND l.item_id = ?
              AND l.location_id IS NULL
              AND l.closed = b'0'
              AND l.remaining_qty > 0
            """;

        BigDecimal available = jdbc.queryForObject(
                sql,
                BigDecimal.class,
                tenantId,
                companyId,
                warehouseId,
                itemId
        );

        if (available == null) {
            available = BigDecimal.ZERO;
        }

        return Map.of(
                "availableQty",
                available.toPlainString()
        );
    }
}
