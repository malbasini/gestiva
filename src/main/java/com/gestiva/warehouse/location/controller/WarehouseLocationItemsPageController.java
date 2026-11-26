package com.gestiva.warehouse.location.controller;

import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;

import org.springframework.jdbc.core.JdbcTemplate;
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
@RequestMapping("/warehouse/locations")
@PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
public class WarehouseLocationItemsPageController {

    private final JdbcTemplate jdbc;
    private final TenantContext tenantContext;
    private final CompanyContext companyContext;

    public WarehouseLocationItemsPageController(
            JdbcTemplate jdbc,
            TenantContext tenantContext,
            CompanyContext companyContext) {

        this.jdbc = jdbc;
        this.tenantContext = tenantContext;
        this.companyContext = companyContext;
    }

    @GetMapping("/{locationId}/items")
    @Transactional(readOnly = true)
    public String items(
            @PathVariable Long locationId,
            Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();
        Long companyId = companyContext.requireCurrentCompanyId();

        // La posizione deve appartenere alla società attiva.
        // Non basta conoscere il suo identificativo.
        String locationSql = """
            SELECT
                loc.id,
                loc.code AS location_code,
                loc.name AS location_name,
                loc.active,
                w.id AS warehouse_id,
                w.code AS warehouse_code,
                w.name AS warehouse_name
            FROM warehouse_location loc
            JOIN warehouse w
              ON w.id = loc.warehouse_id
             AND w.tenant_id = loc.tenant_id
            WHERE loc.id = ?
              AND loc.tenant_id = ?
              AND w.tenant_id = ?
              AND w.company_id = ?
            """;

        List<LocationHeader> locations = jdbc.query(
                locationSql,
                (rs, n) -> new LocationHeader(
                        rs.getLong("id"),
                        rs.getString("location_code"),
                        rs.getString("location_name"),
                        rs.getBoolean("active"),
                        rs.getLong("warehouse_id"),
                        rs.getString("warehouse_code"),
                        rs.getString("warehouse_name")
                ),
                locationId, tenantId, tenantId, companyId
        );

        if (locations.isEmpty()) {
            throw new ResponseStatusException(
                    NOT_FOUND,
                    "Ubicazione non trovata nella società attiva."
            );
        }

        LocationHeader location = locations.get(0);

        // Le quantità provengono dai layer FIFO residui.
        // Nessuna modifica alle scorte.
        //
        // Include soltanto gli articoli con disponibilità
        // positiva nell'ubicazione selezionata.
        String stockSql = """
            SELECT
                i.id AS item_id,
                i.code AS item_code,
                i.name AS item_name,
                COALESCE(SUM(l.remaining_qty), 0)
                    AS available_qty
            FROM inventory_layer l
            JOIN item i
              ON i.id = l.item_id
             AND i.tenant_id = l.tenant_id
            WHERE l.tenant_id = ?
              AND l.company_id = ?
              AND l.warehouse_id = ?
              AND l.location_id = ?
              AND l.remaining_qty > 0
            GROUP BY i.id, i.code, i.name
            ORDER BY i.code, i.id
            """;

        List<ItemStockRow> rows = jdbc.query(
                stockSql,
                (rs, n) -> new ItemStockRow(
                        rs.getLong("item_id"),
                        rs.getString("item_code"),
                        rs.getString("item_name"),
                        rs.getBigDecimal("available_qty")
                ),
                tenantId,
                companyId,
                location.warehouseId(),
                locationId
        );

        BigDecimal totalQty = rows.stream()
                .map(ItemStockRow::availableQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("location", location);
        model.addAttribute("rows", rows);
        model.addAttribute("itemCount", rows.size());
        model.addAttribute("totalQty", totalQty);
        model.addAttribute("activeMenu", "locations");

        return "warehouse/location/location-items";
    }

    public record LocationHeader(
            Long id,
            String code,
            String name,
            boolean active,
            Long warehouseId,
            String warehouseCode,
            String warehouseName) {
    }

    public record ItemStockRow(
            Long itemId,
            String itemCode,
            String itemName,
            BigDecimal availableQty) {
    }
}
