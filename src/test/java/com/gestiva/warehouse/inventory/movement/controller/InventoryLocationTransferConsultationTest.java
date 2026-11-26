package com.gestiva.warehouse.inventory.movement.controller;

import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.tenant.entity.Tenant;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.security.usercontext.TenantContext;

import com.gestiva.warehouse.inventory.movement.controller
        .InventoryLocationTransferHistoryPageController.TransferRow;

import com.gestiva.warehouse.inventory.movement.controller
        .InventoryLocationTransferDetailPageController.TransferDetail;

import com.gestiva.warehouse.inventory.movement.controller
        .InventoryLocationTransferDetailPageController.AllocationRow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support
        .RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;

class InventoryLocationTransferConsultationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 3L;
    private static final Long TRANSFER_ID = 101L;

    private static final Long OUT_ID = 501L;
    private static final Long IN_ID = 502L;

    private static final LocalDate MOVEMENT_DATE =
            LocalDate.of(2026, 11, 26);

    private static final String UNLOCATED =
            "Senza ubicazione";

    private static final String PHYSICAL_SOURCE =
            "U1-TEST-A01";

    private static final String PHYSICAL_TARGET =
            "U1-TEST-A01-01";

    private static final String HISTORY_VIEW =
            "warehouse/inventory/inventory-transfer-history";

    private static final String DETAIL_VIEW =
            "warehouse/inventory/inventory-transfer-detail";

    private JdbcTemplate jdbc;
    private TenantContext tenantContext;
    private CompanyContext companyContext;
    private TenantRepository tenantRepository;

    private Tenant tenant;

    private InventoryLocationTransferHistoryPageController
            historyController;

    private InventoryLocationTransferDetailPageController
            detailController;

    private ExtendedModelMap model;

    private RedirectAttributesModelMap redirect;

    @BeforeEach
    void setup() {

        jdbc = mock(JdbcTemplate.class);

        tenantContext = mock(TenantContext.class);
        companyContext = mock(CompanyContext.class);
        tenantRepository = mock(TenantRepository.class);
        tenant = mock(Tenant.class);

        when(tenantContext.getCurrentTenantId())
                .thenReturn(TENANT);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        when(tenantRepository.findById(TENANT))
                .thenReturn(Optional.of(tenant));

        when(tenant.isSubscriptionActive())
                .thenReturn(true);

        when(tenant.getSubscriptionPlan())
                .thenReturn("PROFESSIONAL");

        historyController =
                new InventoryLocationTransferHistoryPageController(
                        jdbc,
                        tenantContext,
                        companyContext,
                        tenantRepository
                );

        detailController =
                new InventoryLocationTransferDetailPageController(
                        jdbc,
                        tenantContext,
                        companyContext,
                        tenantRepository
                );

        model = new ExtendedModelMap();

        redirect = new RedirectAttributesModelMap();
    }

    private ResultSet transferResult(
            Long id,
            String sourceCode) throws SQLException {

        ResultSet rs = mock(ResultSet.class);

        when(rs.getLong("id")).thenReturn(id);

        when(rs.getDate("movement_date"))
                .thenReturn(Date.valueOf(MOVEMENT_DATE));

        when(rs.getString("warehouse_code"))
                .thenReturn("MAG-TEST-001");

        when(rs.getString("item_code"))
                .thenReturn("TEST-ART-001");

        when(rs.getString("source_code"))
                .thenReturn(sourceCode);

        when(rs.getString("target_code"))
                .thenReturn(PHYSICAL_TARGET);

        when(rs.getBigDecimal("quantity"))
                .thenReturn(new BigDecimal("12.000"));

        when(rs.getString("status"))
                .thenReturn("POSTED");

        when(rs.getObject("out_movement_id", Long.class))
                .thenReturn(OUT_ID);

        when(rs.getObject("in_movement_id", Long.class))
                .thenReturn(IN_ID);

        when(rs.getString("notes"))
                .thenReturn("Collaudo V13");

        return rs;
    }

    private ResultSet allocationResult(
            long id,
            long sourceLayer,
            long targetLayer,
            LocalDate date,
            String quantity,
            String unitCost) throws SQLException {

        ResultSet rs = mock(ResultSet.class);

        when(rs.getLong("id")).thenReturn(id);

        when(rs.getLong("source_layer_id"))
                .thenReturn(sourceLayer);

        when(rs.getObject("target_layer_id", Long.class))
                .thenReturn(targetLayer);

        when(rs.getDate("layer_date"))
                .thenReturn(Date.valueOf(date));

        when(rs.getBigDecimal("allocated_qty"))
                .thenReturn(new BigDecimal(quantity));

        when(rs.getBigDecimal("unit_cost"))
                .thenReturn(new BigDecimal(unitCost));

        return rs;
    }

    private void stubHistory(ResultSet... results) {

        when(jdbc.query(
                anyString(),
                ArgumentMatchers.<RowMapper<TransferRow>>any(),
                eq(TENANT),
                eq(COMPANY)
        )).thenAnswer(invocation -> {

            RowMapper<TransferRow> mapper =
                    invocation.getArgument(1);

            List<TransferRow> rows = new ArrayList<>();

            for (int i = 0; i < results.length; i++) {
                rows.add(mapper.mapRow(results[i], i));
            }

            return rows;
        });
    }

    private void stubDetail(ResultSet... results) {

        when(jdbc.query(
                contains("FROM inventory_location_transfer t"),
                ArgumentMatchers.<RowMapper<TransferDetail>>any(),
                eq(TRANSFER_ID),
                eq(TENANT),
                eq(COMPANY)
        )).thenAnswer(invocation -> {

            RowMapper<TransferDetail> mapper =
                    invocation.getArgument(1);

            List<TransferDetail> rows = new ArrayList<>();

            for (int i = 0; i < results.length; i++) {
                rows.add(mapper.mapRow(results[i], i));
            }

            return rows;
        });
    }

    private void stubAllocations(ResultSet... results) {

        when(jdbc.query(
                contains(
                        "FROM inventory_location_transfer_allocation a"
                ),
                ArgumentMatchers.<RowMapper<AllocationRow>>any(),
                eq(TRANSFER_ID),
                eq(TENANT),
                eq(COMPANY)
        )).thenAnswer(invocation -> {

            RowMapper<AllocationRow> mapper =
                    invocation.getArgument(1);

            List<AllocationRow> rows = new ArrayList<>();

            for (int i = 0; i < results.length; i++) {
                rows.add(mapper.mapRow(results[i], i));
            }

            return rows;
        });
    }

    @SuppressWarnings("unchecked")
    private List<TransferRow> historyRows() {

        assertEquals(
                HISTORY_VIEW,
                historyController.history(model, redirect));

        assertEquals(
                "inventoryTransfers",
                model.get("activeMenu"));

        return (List<TransferRow>) model.get("rows");
    }

    private TransferDetail loadDetail() {

        assertEquals(
                DETAIL_VIEW,
                detailController.detail(TRANSFER_ID, model));

        assertEquals(
                "inventoryTransfers",
                model.get("activeMenu"));

        return (TransferDetail) model.get("transfer");
    }

    private static String normalizeSql(String sql) {

        return sql.toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private void assertCommonTransferSql(String sql) {

        assertTrue(sql.contains(
                "case when t.source_location_id is null"
                + " then 'senza ubicazione'"
                + " else src.code end as source_code"));

        assertTrue(sql.contains(
                "join warehouse w"
                + " on w.id = t.warehouse_id"
                + " and w.tenant_id = t.tenant_id"
                + " and w.company_id = t.company_id"));

        assertTrue(sql.contains(
                "join item i"
                + " on i.id = t.item_id"
                + " and i.tenant_id = t.tenant_id"));

        assertTrue(sql.contains(
                "left join warehouse_location src"
                + " on src.id = t.source_location_id"
                + " and src.tenant_id = t.tenant_id"
                + " and src.warehouse_id = t.warehouse_id"));

        assertTrue(sql.contains(
                "join warehouse_location dst"
                + " on dst.id = t.target_location_id"
                + " and dst.tenant_id = t.tenant_id"
                + " and dst.warehouse_id = t.warehouse_id"));

        assertFalse(sql.contains(
                "left join warehouse_location dst"));

        assertTrue(sql.contains(
                "and ( t.source_location_id is null"
                + " or src.id is not null )"));
    }

    @Test
    void historyMustMapUnlocatedTransfer()
            throws SQLException {

        ResultSet rs = transferResult(
                TRANSFER_ID, UNLOCATED);

        stubHistory(rs);

        var rows = historyRows();

        assertEquals(1, rows.size());

        TransferRow row = rows.get(0);

        assertEquals(TRANSFER_ID, row.id());
        assertEquals(UNLOCATED, row.sourceCode());
        assertEquals(PHYSICAL_TARGET, row.targetCode());
        assertEquals(MOVEMENT_DATE, row.movementDate());
        assertEquals("MAG-TEST-001", row.warehouseCode());
        assertEquals("TEST-ART-001", row.itemCode());

        assertEquals(
                new BigDecimal("12.000"),
                row.quantity());

        assertEquals("POSTED", row.status());
        assertEquals(OUT_ID, row.outMovementId());
        assertEquals(IN_ID, row.inMovementId());
    }

    @Test
    void historyMustPreservePhysicalOrigin()
            throws SQLException {

        ResultSet rs = transferResult(
                TRANSFER_ID, PHYSICAL_SOURCE);

        stubHistory(rs);

        var rows = historyRows();

        assertEquals(1, rows.size());

        assertEquals(
                PHYSICAL_SOURCE,
                rows.get(0).sourceCode());

        assertEquals(
                PHYSICAL_TARGET,
                rows.get(0).targetCode());
    }

    @Test
    void historyMustSupportBothOriginTypes()
            throws SQLException {

        ResultSet first = transferResult(
                102L, UNLOCATED);

        ResultSet second = transferResult(
                101L, PHYSICAL_SOURCE);

        stubHistory(first, second);

        var rows = historyRows();

        assertEquals(2, rows.size());

        assertEquals(102L, rows.get(0).id());
        assertEquals(UNLOCATED, rows.get(0).sourceCode());

        assertEquals(101L, rows.get(1).id());

        assertEquals(
                PHYSICAL_SOURCE,
                rows.get(1).sourceCode());
    }

    @Test
    void emptyHistoryMustReturnEmptyRows() {

        stubHistory();

        assertTrue(historyRows().isEmpty());
    }

    @Test
    void historySqlMustPreserveScopeAndNullOrigin() {

        stubHistory();

        historyRows();

        ArgumentCaptor<String> captor =
                ArgumentCaptor.forClass(String.class);

        verify(jdbc).query(
                captor.capture(),
                ArgumentMatchers.<RowMapper<TransferRow>>any(),
                eq(TENANT),
                eq(COMPANY)
        );

        String sql = normalizeSql(captor.getValue());

        assertCommonTransferSql(sql);

        assertTrue(sql.contains(
                "where t.tenant_id = ?"
                + " and t.company_id = ?"));

        assertTrue(sql.contains(
                "order by t.movement_date desc, t.id desc"
                + " limit 100"));

        assertEquals(
                2L,
                sql.chars().filter(c -> c == '?').count());

        verifyNoMoreInteractions(jdbc);
    }

    @Test
    void missingCompanyMustRedirectHistory() {

        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new IllegalStateException());

        assertEquals(
                "redirect:/company/select",
                historyController.history(model, redirect));

        assertEquals(
                "Seleziona una società prima di consultare i trasferimenti.",
                redirect.getFlashAttributes()
                        .get("errorMessage"));

        verifyNoInteractions(jdbc);
    }

    @Test
    void inactiveSubscriptionMustRejectHistory() {

        when(tenant.isSubscriptionActive())
                .thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> historyController.history(model, redirect));

        verifyNoInteractions(jdbc, companyContext);
    }

    @Test
    void nonProfessionalPlanMustRejectHistory() {

        when(tenant.getSubscriptionPlan())
                .thenReturn("STANDARD");

        assertThrows(
                AccessDeniedException.class,
                () -> historyController.history(model, redirect));

        verifyNoInteractions(jdbc, companyContext);
    }

    @Test
    void detailMustMapUnlocatedTransfer()
            throws SQLException {

        ResultSet rs = transferResult(
                TRANSFER_ID, UNLOCATED);

        stubDetail(rs);
        stubAllocations();

        TransferDetail detail = loadDetail();

        assertEquals(TRANSFER_ID, detail.id());
        assertEquals(UNLOCATED, detail.sourceCode());
        assertEquals(PHYSICAL_TARGET, detail.targetCode());

        assertEquals(
                MOVEMENT_DATE,
                detail.movementDate());

        assertEquals("MAG-TEST-001", detail.warehouseCode());
        assertEquals("TEST-ART-001", detail.itemCode());

        assertEquals(
                new BigDecimal("12.000"),
                detail.quantity());

        assertEquals("POSTED", detail.status());
        assertEquals(OUT_ID, detail.outMovementId());
        assertEquals(IN_ID, detail.inMovementId());
        assertEquals("Collaudo V13", detail.notes());
    }

    @Test
    void detailMustPreservePhysicalOrigin()
            throws SQLException {

        ResultSet rs = transferResult(
                TRANSFER_ID, PHYSICAL_SOURCE);

        stubDetail(rs);
        stubAllocations();

        TransferDetail detail = loadDetail();

        assertEquals(
                PHYSICAL_SOURCE,
                detail.sourceCode());

        assertEquals(
                PHYSICAL_TARGET,
                detail.targetCode());
    }

    @Test
    void missingTransferMustReturnNotFound() {

        stubDetail();

        ResponseStatusException failure =
                assertThrows(
                        ResponseStatusException.class,
                        () -> detailController.detail(
                                TRANSFER_ID, model)
                );

        assertEquals(NOT_FOUND, failure.getStatusCode());

        verify(jdbc).query(
                contains("FROM inventory_location_transfer t"),
                ArgumentMatchers.<RowMapper<TransferDetail>>any(),
                eq(TRANSFER_ID),
                eq(TENANT),
                eq(COMPANY)
        );

        // Nessuna consultazione delle allocazioni.
        verifyNoMoreInteractions(jdbc);
    }

    @Test
    void detailSqlMustPreserveScopeAndNullOrigin()
            throws SQLException {

        ResultSet rs = transferResult(
                TRANSFER_ID, UNLOCATED);

        stubDetail(rs);
        stubAllocations();

        loadDetail();

        ArgumentCaptor<String> captor =
                ArgumentCaptor.forClass(String.class);

        verify(jdbc, times(2)).query(
                captor.capture(),
                ArgumentMatchers.<RowMapper<?>>any(),
                eq(TRANSFER_ID),
                eq(TENANT),
                eq(COMPANY)
        );

        String sql = normalizeSql(
                captor.getAllValues().get(0));

        assertCommonTransferSql(sql);

        assertTrue(sql.contains(
                "where t.id = ?"
                + " and t.tenant_id = ?"
                + " and t.company_id = ?"));

        assertEquals(
                3L,
                sql.chars().filter(c -> c == '?').count());

        verifyNoMoreInteractions(jdbc);
    }

    @Test
    void allocationSqlMustPreserveFifoEvidenceScope()
            throws SQLException {

        ResultSet rs = transferResult(
                TRANSFER_ID, UNLOCATED);

        stubDetail(rs);
        stubAllocations();

        loadDetail();

        ArgumentCaptor<String> captor =
                ArgumentCaptor.forClass(String.class);

        verify(jdbc, times(2)).query(
                captor.capture(),
                ArgumentMatchers.<RowMapper<?>>any(),
                eq(TRANSFER_ID),
                eq(TENANT),
                eq(COMPANY)
        );

        String sql = normalizeSql(
                captor.getAllValues().get(1));

        assertTrue(sql.contains(
                "select a.id, a.source_layer_id,"
                + " a.target_layer_id, a.layer_date,"
                + " a.allocated_qty, a.unit_cost"));

        assertTrue(sql.contains(
                "from inventory_location_transfer_allocation a"));

        assertTrue(sql.contains(
                "join inventory_location_transfer t"
                + " on t.id = a.transfer_id"
                + " and t.tenant_id = a.tenant_id"
                + " and t.company_id = a.company_id"
                + " and t.warehouse_id = a.warehouse_id"));

        assertTrue(sql.contains(
                "where a.transfer_id = ?"
                + " and a.tenant_id = ?"
                + " and a.company_id = ?"
                + " order by a.id asc"));

        assertEquals(
                3L,
                sql.chars().filter(c -> c == '?').count());

        verifyNoMoreInteractions(jdbc);
    }

    @Test
    @SuppressWarnings("unchecked")
    void detailMustPreserveFifoAllocationValues()
            throws SQLException {

        ResultSet transfer = transferResult(
                TRANSFER_ID, UNLOCATED);

        ResultSet first = allocationResult(
                701L,
                15L,
                25L,
                LocalDate.of(2026, 10, 1),
                "8.000",
                "7.2000"
        );

        ResultSet second = allocationResult(
                702L,
                16L,
                26L,
                LocalDate.of(2026, 10, 10),
                "4.000",
                "9.5000"
        );

        stubDetail(transfer);
        stubAllocations(first, second);

        loadDetail();

        List<AllocationRow> allocations =
                (List<AllocationRow>)
                        model.get("allocations");

        assertEquals(2, allocations.size());

        AllocationRow a = allocations.get(0);

        assertEquals(701L, a.id());
        assertEquals(15L, a.sourceLayerId());
        assertEquals(25L, a.targetLayerId());

        assertEquals(
                LocalDate.of(2026, 10, 1),
                a.layerDate());

        assertEquals(
                new BigDecimal("8.000"),
                a.quantity());

        assertEquals(
                new BigDecimal("7.2000"),
                a.unitCost());

        assertEquals(
                0,
                new BigDecimal("57.6000")
                        .compareTo(a.value()));

        AllocationRow b = allocations.get(1);

        assertEquals(702L, b.id());
        assertEquals(16L, b.sourceLayerId());
        assertEquals(26L, b.targetLayerId());

        assertEquals(
                LocalDate.of(2026, 10, 10),
                b.layerDate());

        assertEquals(
                new BigDecimal("4.000"),
                b.quantity());

        assertEquals(
                new BigDecimal("9.5000"),
                b.unitCost());

        assertEquals(
                0,
                new BigDecimal("38.0000")
                        .compareTo(b.value()));

        assertEquals(
                new BigDecimal("12.000"),
                model.get("allocatedQty"));

        assertEquals(
                0,
                new BigDecimal("95.6000")
                        .compareTo(
                                (BigDecimal)
                                        model.get("totalValue")));
    }

    @Test
    void inactiveSubscriptionMustRejectDetail() {

        when(tenant.isSubscriptionActive())
                .thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> detailController.detail(
                        TRANSFER_ID, model));

        verifyNoInteractions(jdbc, companyContext);
    }

    @Test
    void nonProfessionalPlanMustRejectDetail() {

        when(tenant.getSubscriptionPlan())
                .thenReturn("STANDARD");

        assertThrows(
                AccessDeniedException.class,
                () -> detailController.detail(
                        TRANSFER_ID, model));

        verifyNoInteractions(jdbc, companyContext);
    }
}
