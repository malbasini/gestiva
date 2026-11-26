package com.gestiva.warehouse.inventory.movement.controller;

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
import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
@RequestMapping("/inventory/transfers")
@PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
public class InventoryLocationTransferDetailPageController {

    private final JdbcTemplate jdbc;
    private final TenantContext tenantContext;
    private final CompanyContext companyContext;
    private final TenantRepository tenantRepository;

    public InventoryLocationTransferDetailPageController(
            JdbcTemplate jdbc,
            TenantContext tenantContext,
            CompanyContext companyContext,
            TenantRepository tenantRepository) {

        this.jdbc = jdbc;
        this.tenantContext = tenantContext;
        this.companyContext = companyContext;
        this.tenantRepository = tenantRepository;
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String detail(@PathVariable Long id, Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();

        boolean professional = tenantId != null
                && tenantRepository.findById(tenantId)
                .map(t -> t.isSubscriptionActive()
                        && "PROFESSIONAL".equalsIgnoreCase(
                                t.getSubscriptionPlan()))
                .orElse(false);

        if (!professional) {
            throw new AccessDeniedException(
                    "Dettaglio riservato al piano Professional attivo."
            );
        }

        Long companyId = companyContext.requireCurrentCompanyId();

        String transferSql = """
            SELECT
                t.id,
                t.movement_date,
                w.code AS warehouse_code,
                i.code AS item_code,
                CASE
                    WHEN t.source_location_id IS NULL
                        THEN 'Senza ubicazione'
                    ELSE src.code
                END AS source_code,
                dst.code AS target_code,
                t.quantity,
                t.status,
                t.out_movement_id,
                t.in_movement_id,
                t.notes
            FROM inventory_location_transfer t
            JOIN warehouse w
              ON w.id = t.warehouse_id
             AND w.tenant_id = t.tenant_id
             AND w.company_id = t.company_id
            JOIN item i
              ON i.id = t.item_id
             AND i.tenant_id = t.tenant_id
            LEFT JOIN warehouse_location src
              ON src.id = t.source_location_id
             AND src.tenant_id = t.tenant_id
             AND src.warehouse_id = t.warehouse_id
            JOIN warehouse_location dst
              ON dst.id = t.target_location_id
             AND dst.tenant_id = t.tenant_id
             AND dst.warehouse_id = t.warehouse_id
            WHERE t.id = ?
              AND t.tenant_id = ?
              AND t.company_id = ?
              AND (
                  t.source_location_id IS NULL
                  OR src.id IS NOT NULL
              )
            """;

        List<TransferDetail> found = jdbc.query(
                transferSql,
                (rs, rowNum) -> new TransferDetail(
                        rs.getLong("id"),
                        rs.getDate("movement_date").toLocalDate(),
                        rs.getString("warehouse_code"),
                        rs.getString("item_code"),
                        rs.getString("source_code"),
                        rs.getString("target_code"),
                        rs.getBigDecimal("quantity"),
                        rs.getString("status"),
                        rs.getObject("out_movement_id", Long.class),
                        rs.getObject("in_movement_id", Long.class),
                        rs.getString("notes")
                ),
                id, tenantId, companyId
        );

        if (found.isEmpty()) {
            throw new ResponseStatusException(
                    NOT_FOUND,
                    "Trasferimento non trovato nella società attiva."
            );
        }

        String allocationSql = """
            SELECT
                a.id,
                a.source_layer_id,
                a.target_layer_id,
                a.layer_date,
                a.allocated_qty,
                a.unit_cost
            FROM inventory_location_transfer_allocation a
            JOIN inventory_location_transfer t
              ON t.id = a.transfer_id
             AND t.tenant_id = a.tenant_id
             AND t.company_id = a.company_id
             AND t.warehouse_id = a.warehouse_id
            WHERE a.transfer_id = ?
              AND a.tenant_id = ?
              AND a.company_id = ?
            ORDER BY a.id ASC
            """;

        List<AllocationRow> allocations = jdbc.query(
                allocationSql,
                (rs, rowNum) -> {
                    BigDecimal qty = rs.getBigDecimal("allocated_qty");
                    BigDecimal cost = rs.getBigDecimal("unit_cost");

                    return new AllocationRow(
                            rs.getLong("id"),
                            rs.getLong("source_layer_id"),
                            rs.getObject("target_layer_id", Long.class),
                            rs.getDate("layer_date").toLocalDate(),
                            qty,
                            cost,
                            qty.multiply(cost)
                    );
                },
                id, tenantId, companyId
        );

        BigDecimal allocatedQty = allocations.stream()
                .map(AllocationRow::quantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalValue = allocations.stream()
                .map(AllocationRow::value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("transfer", found.get(0));
        model.addAttribute("allocations", allocations);
        model.addAttribute("allocatedQty", allocatedQty);
        model.addAttribute("totalValue", totalValue);
        model.addAttribute("activeMenu", "inventoryTransfers");

        return "warehouse/inventory/inventory-transfer-detail";
    }

    public record TransferDetail(
            Long id,
            LocalDate movementDate,
            String warehouseCode,
            String itemCode,
            String sourceCode,
            String targetCode,
            BigDecimal quantity,
            String status,
            Long outMovementId,
            Long inMovementId,
            String notes) {
    }

    public record AllocationRow(
            Long id,
            Long sourceLayerId,
            Long targetLayerId,
            LocalDate layerDate,
            BigDecimal quantity,
            BigDecimal unitCost,
            BigDecimal value) {
    }
}
