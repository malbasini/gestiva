package com.gestiva.warehouse.inventory.movement.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.tenant.entity.Tenant;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.warehouse.inventory.movement.service
        .InventoryLocationTransferAtomicPostingCoordinator;
import com.gestiva.warehouse.item.web.ItemWebService;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.entity.WarehouseLocation;
import com.gestiva.warehouse.location.service.WarehouseService;
import com.gestiva.warehouse.location.service.WarehouseLocationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support
        .RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryLocationTransferPageControllerTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 3L;
    private static final Long WAREHOUSE = 2L;
    private static final Long ITEM = 1L;

    private static final Long SOURCE_LOCATION = 1L;
    private static final Long TARGET_LOCATION = 2L;
    private static final Long INACTIVE_LOCATION = 4L;
    private static final Long FOREIGN_LOCATION = 999L;

    private static final Long TRANSFER_ID = 101L;

    private static final BigDecimal QUANTITY =
            new BigDecimal("12.000");

    private static final LocalDate DATE =
            LocalDate.of(2026, 11, 26);

    private static final String NOTES =
            "Collaudo controller V13";

    private static final String VIEW =
            "warehouse/inventory/inventory-location-transfer-form";

    private static final String REDIRECT =
            "redirect:/inventory/transfers/new";

    private static final String SESSION_KEY =
            "gestiva.inventory.transfer.operationKey";

    private static final String SESSION_TENANT =
            "gestiva.inventory.transfer.tenantId";

    private static final String SESSION_COMPANY =
            "gestiva.inventory.transfer.companyId";

    private ItemWebService itemWebService;
    private WarehouseService warehouseService;
    private WarehouseLocationService locationService;
    private TenantContext tenantContext;
    private CompanyContext companyContext;
    private TenantRepository tenantRepository;

    private InventoryLocationTransferAtomicPostingCoordinator
            coordinator;

    private InventoryLocationTransferPageController controller;

    private Tenant tenant;

    private MockHttpSession session;

    private ExtendedModelMap model;

    private RedirectAttributesModelMap redirect;

    private String operationKey;

    @BeforeEach
    void setup() {

        itemWebService = mock(ItemWebService.class);
        warehouseService = mock(WarehouseService.class);
        locationService = mock(WarehouseLocationService.class);
        tenantContext = mock(TenantContext.class);
        companyContext = mock(CompanyContext.class);
        tenantRepository = mock(TenantRepository.class);

        coordinator = mock(
                InventoryLocationTransferAtomicPostingCoordinator.class
        );

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

        Warehouse warehouse = mock(Warehouse.class);

        when(warehouse.getId()).thenReturn(WAREHOUSE);

        when(warehouseService.findActive(TENANT, COMPANY))
                .thenReturn(List.of(warehouse));

        WarehouseLocation sourceLocation =
                location(SOURCE_LOCATION, true);

        WarehouseLocation targetLocation =
                location(TARGET_LOCATION, true);

        WarehouseLocation inactiveLocation =
                location(INACTIVE_LOCATION, false);

        when(locationService.findAll(
                TENANT, COMPANY, WAREHOUSE))
                .thenReturn(List.of(
                        sourceLocation,
                        targetLocation,
                        inactiveLocation
                ));

        when(itemWebService.findStockManagedOptions(TENANT))
                .thenReturn(List.of());

        controller = new InventoryLocationTransferPageController(
                itemWebService,
                warehouseService,
                locationService,
                tenantContext,
                companyContext,
                tenantRepository,
                coordinator
        );

        session = new MockHttpSession();
        model = new ExtendedModelMap();
        redirect = new RedirectAttributesModelMap();

        // Apertura reale del form: il controller genera
        // e associa la operationKey alla sessione.

        assertEquals(
                VIEW,
                controller.newTransfer(
                        new ExtendedModelMap(),
                        new RedirectAttributesModelMap(),
                        session
                )
        );

        operationKey = (String)
                session.getAttribute(SESSION_KEY);

        assertNotNull(operationKey);

        assertDoesNotThrow(
                () -> UUID.fromString(operationKey));

        assertEquals(
                TENANT,
                session.getAttribute(SESSION_TENANT));

        assertEquals(
                COMPANY,
                session.getAttribute(SESSION_COMPANY));

        // Le verifiche successive riguardano il solo POST.
        clearInvocations(
                itemWebService,
                warehouseService,
                locationService,
                tenantContext,
                companyContext,
                tenantRepository,
                coordinator
        );
    }

    private WarehouseLocation location(
            Long id, boolean active) {

        WarehouseLocation location =
                mock(WarehouseLocation.class);

        when(location.getId()).thenReturn(id);
        when(location.isActive()).thenReturn(active);

        when(location.getCode()).thenReturn("LOC-" + id);
        when(location.getName()).thenReturn("Ubicazione " + id);

        return location;
    }

    private String post(
            String source, Long target) {

        return post(
                WAREHOUSE, source, target, operationKey);
    }

    private String post(
            Long warehouse,
            String source,
            Long target,
            String key) {

        return controller.postTransfer(
                warehouse,
                ITEM,
                source,
                target,
                QUANTITY,
                DATE,
                NOTES,
                key,
                session,
                model,
                redirect
        );
    }

    private void stubSuccessfulPosting(Long source) {

        when(coordinator.post(
                TENANT,
                COMPANY,
                WAREHOUSE,
                ITEM,
                source,
                TARGET_LOCATION,
                QUANTITY,
                DATE,
                NOTES,
                operationKey
        )).thenReturn(TRANSFER_ID);
    }

    private void verifyPosting(Long source) {

        verify(coordinator).post(
                TENANT,
                COMPANY,
                WAREHOUSE,
                ITEM,
                source,
                TARGET_LOCATION,
                QUANTITY,
                DATE,
                NOTES,
                operationKey
        );

        verifyNoMoreInteractions(coordinator);
    }

    private void assertRejected(
            String source, Long target) {

        assertEquals(VIEW, post(source, target));

        assertNotNull(model.get("errorMessage"));

        assertFalse(
                redirect.getFlashAttributes()
                        .containsKey("successMessage"));

        assertEquals(
                operationKey,
                model.get("operationKey"));

        verifyNoInteractions(coordinator);
    }

    @Test
    void explicitUnlocatedMustDelegateNullOrigin() {

        stubSuccessfulPosting(null);

        assertEquals(
                REDIRECT,
                post("UNLOCATED", TARGET_LOCATION));

        verifyPosting(null);

        assertEquals(
                "Trasferimento registrato correttamente. "
                        + "Identificativo n. " + TRANSFER_ID,
                redirect.getFlashAttributes()
                        .get("successMessage"));
    }

    @Test
    void physicalOriginMustDelegateItsIdentifier() {

        stubSuccessfulPosting(SOURCE_LOCATION);

        assertEquals(
                REDIRECT,
                post("1", TARGET_LOCATION));

        verifyPosting(SOURCE_LOCATION);
    }

    @Test
    void missingOriginMustNotReachCoordinator() {

        assertRejected(null, TARGET_LOCATION);

        assertEquals(
                "Seleziona l'ubicazione di origine.",
                model.get("errorMessage"));
    }

    @Test
    void emptyOriginMustNotReachCoordinator() {

        assertRejected("", TARGET_LOCATION);
    }

    @Test
    void invalidOriginMustNotReachCoordinator() {

        assertRejected("null", TARGET_LOCATION);

        assertEquals(
                "Selezione dell'ubicazione di origine non valida.",
                model.get("errorMessage"));
    }

    @Test
    void foreignPhysicalOriginMustBeRejected() {

        assertRejected(
                FOREIGN_LOCATION.toString(),
                TARGET_LOCATION);
    }

    @Test
    void inactivePhysicalOriginMustBeRejected() {

        assertRejected(
                INACTIVE_LOCATION.toString(),
                TARGET_LOCATION);
    }

    @Test
    void unlocatedWithMissingTargetMustBeRejected() {

        assertRejected("UNLOCATED", null);
    }

    @Test
    void unlocatedWithZeroTargetMustBeRejected() {

        assertRejected("UNLOCATED", 0L);
    }

    @Test
    void unlocatedWithForeignTargetMustBeRejected() {

        assertRejected(
                "UNLOCATED", FOREIGN_LOCATION);
    }

    @Test
    void unlocatedWithInactiveTargetMustBeRejected() {

        assertRejected(
                "UNLOCATED", INACTIVE_LOCATION);
    }

    @Test
    void unlocatedWithForeignWarehouseMustBeRejected() {

        assertEquals(
                VIEW,
                post(
                        999L,
                        "UNLOCATED",
                        TARGET_LOCATION,
                        operationKey
                ));

        assertEquals(
                "Magazzino non disponibile per la società attiva.",
                model.get("errorMessage"));

        verifyNoInteractions(coordinator);
    }

    @Test
    void discordantOperationKeyMustBeRejected() {

        assertEquals(
                VIEW,
                post(
                        WAREHOUSE,
                        "UNLOCATED",
                        TARGET_LOCATION,
                        UUID.randomUUID().toString()
                ));

        assertTrue(
                model.get("errorMessage")
                        .toString().contains("sessione scaduta"));

        verifyNoInteractions(coordinator);
    }

    @Test
    void discordantSessionCompanyMustBeRejected() {

        session.setAttribute(SESSION_COMPANY, 999L);

        assertRejected("UNLOCATED", TARGET_LOCATION);

        assertTrue(
                model.get("errorMessage")
                        .toString().contains("sessione scaduta"));
    }

    @Test
    void discordantSessionTenantMustBeRejected() {

        session.setAttribute(SESSION_TENANT, 999L);

        assertRejected("UNLOCATED", TARGET_LOCATION);

        assertTrue(
                model.get("errorMessage")
                        .toString().contains("sessione scaduta"));
    }

    @Test
    void inactiveProfessionalSubscriptionMustBeRejected() {

        when(tenant.isSubscriptionActive())
                .thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> post("UNLOCATED", TARGET_LOCATION)
        );

        verifyNoInteractions(coordinator);
        verifyNoInteractions(warehouseService);
        verifyNoInteractions(locationService);
    }

    private void assertPostingFailurePreservesSelection(
            String selection,
            Long resolvedSource) {

        String message =
                "Strati FIFO insufficienti nell'ubicazione di origine.";

        when(coordinator.post(
                TENANT,
                COMPANY,
                WAREHOUSE,
                ITEM,
                resolvedSource,
                TARGET_LOCATION,
                QUANTITY,
                DATE,
                NOTES,
                operationKey
        )).thenThrow(new BusinessException(message));

        assertEquals(
                VIEW,
                post(selection, TARGET_LOCATION));

        verifyPosting(resolvedSource);

        assertEquals(message, model.get("errorMessage"));

        assertEquals(
                selection,
                model.get("sourceLocationId"));

        assertEquals(
                WAREHOUSE,
                model.get("warehouseId"));

        assertEquals(
                ITEM,
                model.get("itemId"));

        assertEquals(
                TARGET_LOCATION,
                model.get("targetLocationId"));

        assertEquals(QUANTITY, model.get("quantity"));

        assertEquals(DATE, model.get("movementDate"));

        assertEquals(NOTES, model.get("notes"));

        assertEquals(
                operationKey,
                model.get("operationKey"));

        assertEquals(
                operationKey,
                session.getAttribute(SESSION_KEY));

        assertNotNull(model.get("warehouseOptions"));
        assertNotNull(model.get("locationOptions"));
        assertNotNull(model.get("itemOptions"));

        assertFalse(
                redirect.getFlashAttributes()
                        .containsKey("successMessage"));
    }

    @Test
    void postingFailureMustPreserveUnlocatedSelection() {

        assertPostingFailurePreservesSelection(
                "UNLOCATED", null);
    }

    @Test
    void postingFailureMustPreservePhysicalSelection() {

        assertPostingFailurePreservesSelection(
                "1", SOURCE_LOCATION);
    }
}
