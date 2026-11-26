package com.gestiva.warehouse.item.controller;

import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.security.tenant.repository.TenantRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
@RequestMapping("/items")
@PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
public class ItemLocationAvailabilityPageController {

    private final JdbcTemplate jdbc;
    private final TenantContext tenantContext;
    private final CompanyContext companyContext;
    private final TenantRepository tenantRepository;

    public ItemLocationAvailabilityPageController(
            JdbcTemplate jdbc,
            TenantContext tenantContext,
            CompanyContext companyContext,
            TenantRepository tenantRepository) {

        this.jdbc = jdbc;
        this.tenantContext = tenantContext;
        this.companyContext = companyContext;
        this.tenantRepository = tenantRepository;
    }

    @GetMapping("/{id}/locations")
    @Transactional(readOnly = true)
    public String locations(
            @PathVariable Long id,
            Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();

        boolean professional = tenantId != null
                && tenantRepository.findById(tenantId)
                .map(t -> t.isSubscriptionActive()
                        && "PROFESSIONAL".equalsIgnoreCase(
                                t.getSubscriptionPlan()))
                .orElse(false);

        if (!professional) {
            throw new AccessDeniedException(
                    "Consultazione ubicazioni riservata "
                    + "al piano Professional attivo."
            );
        }

        Long companyId = companyContext.requireCurrentCompanyId();

        // Verifica articolo nello stesso tenant.
        List<ItemHeader> items = jdbc.query(
                """
                SELECT id, code, name
                FROM item
                WHERE id = ?
                  AND tenant_id = ?
                """,
                (rs, n) -> new ItemHeader(
                        rs.getLong("id"),
                        rs.getString("code"),
                        rs.getString("name")
                ),
                id, tenantId
        );

        if (items.isEmpty()) {
            throw new ResponseStatusException(
                    NOT_FOUND, "Articolo non trovato."
            );
        }

        // Disponibilità derivata ESCLUSIVAMENTE
        // dai layer FIFO residui.
        // I layer con location_id NULL vengono
        // esposti come scorte senza ubicazione.
        String sql = """
            SELECT
                w.id AS warehouse_id,
                w.code AS warehouse_code,
                w.name AS warehouse_name,
                loc.id AS location_id,
                loc.code AS location_code,
                loc.name AS location_name,
                COALESCE(SUM(l.remaining_qty), 0)
                    AS available_qty
            FROM inventory_layer l
            JOIN warehouse w
              ON w.id = l.warehouse_id
             AND w.tenant_id = l.tenant_id
             AND w.company_id = l.company_id
            LEFT JOIN warehouse_location loc
              ON loc.id = l.location_id
             AND loc.tenant_id = l.tenant_id
             AND loc.warehouse_id = l.warehouse_id
            WHERE l.tenant_id = ?
              AND l.company_id = ?
              AND l.item_id = ?
              AND l.remaining_qty > 0
            GROUP BY
                w.id, w.code, w.name,
                loc.id, loc.code, loc.name
            ORDER BY
                w.code,
                CASE WHEN loc.id IS NULL THEN 1 ELSE 0 END,
                loc.code
            """;

        List<LocationStockRow> rows = jdbc.query(
                sql,
                (rs, n) -> new LocationStockRow(
                        rs.getLong("warehouse_id"),
                        rs.getString("warehouse_code"),
                        rs.getString("warehouse_name"),
                        rs.getObject("location_id", Long.class),
                        rs.getString("location_code"),
                        rs.getString("location_name"),
                        rs.getBigDecimal("available_qty")
                ),
                tenantId, companyId, id
        );

        BigDecimal totalQty = rows.stream()
                .map(LocationStockRow::availableQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal unassignedQty = rows.stream()
                .filter(r -> r.locationId() == null)
                .map(LocationStockRow::availableQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("item", items.get(0));
        model.addAttribute("rows", rows);
        model.addAttribute("totalQty", totalQty);
        model.addAttribute("unassignedQty", unassignedQty);
        model.addAttribute("activeMenu", "items");

        return "warehouse/item/item-location-availability";
    }

    public record ItemHeader(
            Long id,
            String code,
            String name) {
    }

    public record LocationStockRow(
            Long warehouseId,
            String warehouseCode,
            String warehouseName,
            Long locationId,
            String locationCode,
            String locationName,
            BigDecimal availableQty) {
    }
}
