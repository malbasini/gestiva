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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/inventory/transfers")
@PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
public class InventoryLocationTransferHistoryPageController {

    private final JdbcTemplate jdbc;
    private final TenantContext tenantContext;
    private final CompanyContext companyContext;
    private final TenantRepository tenantRepository;

    public InventoryLocationTransferHistoryPageController(
            JdbcTemplate jdbc,
            TenantContext tenantContext,
            CompanyContext companyContext,
            TenantRepository tenantRepository) {

        this.jdbc = jdbc;
        this.tenantContext = tenantContext;
        this.companyContext = companyContext;
        this.tenantRepository = tenantRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String history(
            Model model,
            RedirectAttributes redirectAttributes) {

        Long tenantId = tenantContext.getCurrentTenantId();

        boolean professional = tenantId != null
                && tenantRepository.findById(tenantId)
                .map(t -> t.isSubscriptionActive()
                        && "PROFESSIONAL".equalsIgnoreCase(
                                t.getSubscriptionPlan()))
                .orElse(false);

        if (!professional) {
            throw new AccessDeniedException(
                    "Storico riservato al piano Professional attivo."
            );
        }

        Long companyId;

        try {
            companyId = companyContext.requireCurrentCompanyId();
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Seleziona una società prima di consultare i trasferimenti."
            );
            return "redirect:/company/select";
        }

        String sql = """
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
                t.in_movement_id
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
            WHERE t.tenant_id = ?
              AND t.company_id = ?
              AND (
                  t.source_location_id IS NULL
                  OR src.id IS NOT NULL
              )
            ORDER BY t.movement_date DESC, t.id DESC
            LIMIT 100
            """;

        List<TransferRow> rows = jdbc.query(
                sql,
                (rs, rowNum) -> new TransferRow(
                        rs.getLong("id"),
                        rs.getDate("movement_date").toLocalDate(),
                        rs.getString("warehouse_code"),
                        rs.getString("item_code"),
                        rs.getString("source_code"),
                        rs.getString("target_code"),
                        rs.getBigDecimal("quantity"),
                        rs.getString("status"),
                        rs.getObject("out_movement_id", Long.class),
                        rs.getObject("in_movement_id", Long.class)
                ),
                tenantId,
                companyId
        );

        model.addAttribute("rows", rows);
        model.addAttribute("activeMenu", "inventoryTransfers");

        return "warehouse/inventory/inventory-transfer-history";
    }

    public record TransferRow(
            Long id,
            LocalDate movementDate,
            String warehouseCode,
            String itemCode,
            String sourceCode,
            String targetCode,
            BigDecimal quantity,
            String status,
            Long outMovementId,
            Long inMovementId) {
    }
}
