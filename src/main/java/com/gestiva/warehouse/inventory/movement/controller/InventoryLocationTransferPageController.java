package com.gestiva.warehouse.inventory.movement.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.warehouse.item.web.ItemWebService;
import com.gestiva.warehouse.location.service.WarehouseService;
import com.gestiva.warehouse.location.service.WarehouseLocationService;
import com.gestiva.warehouse.inventory.movement.service.InventoryLocationTransferAtomicPostingCoordinator;

import jakarta.servlet.http.HttpSession;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Controller
@RequestMapping("/inventory/transfers")
@PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
public class InventoryLocationTransferPageController {

    private static final String SESSION_KEY =
            "gestiva.inventory.transfer.operationKey";

    private static final String SESSION_TENANT =
            "gestiva.inventory.transfer.tenantId";

    private static final String SESSION_COMPANY =
            "gestiva.inventory.transfer.companyId";

    private static final String VIEW =
            "warehouse/inventory/inventory-location-transfer-form";

    private final ItemWebService itemWebService;
    private final WarehouseService warehouseService;
    private final WarehouseLocationService locationService;
    private final TenantContext tenantContext;
    private final CompanyContext companyContext;
    private final TenantRepository tenantRepository;
    private final InventoryLocationTransferAtomicPostingCoordinator coordinator;

    public InventoryLocationTransferPageController(
            ItemWebService itemWebService,
            WarehouseService warehouseService,
            WarehouseLocationService locationService,
            TenantContext tenantContext,
            CompanyContext companyContext,
            TenantRepository tenantRepository,
            InventoryLocationTransferAtomicPostingCoordinator coordinator) {

        this.itemWebService = Objects.requireNonNull(itemWebService);
        this.warehouseService = Objects.requireNonNull(warehouseService);
        this.locationService = Objects.requireNonNull(locationService);
        this.tenantContext = Objects.requireNonNull(tenantContext);
        this.companyContext = Objects.requireNonNull(companyContext);
        this.tenantRepository = Objects.requireNonNull(tenantRepository);
        this.coordinator = Objects.requireNonNull(coordinator);
    }

    @GetMapping("/new")
    public String newTransfer(
            Model model,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        Long tenantId = tenantContext.getCurrentTenantId();
        requireProfessional(tenantId);

        Long companyId;

        try {
            companyId = companyContext.requireCurrentCompanyId();
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Seleziona una società prima di trasferire le scorte."
            );
            return "redirect:/company/select";
        }

        // Una nuova apertura del form crea una nuova
        // operazione. I POST ripetuti dello stesso form
        // conservano invece la medesima chiave.
        String operationKey = UUID.randomUUID().toString();

        session.setAttribute(SESSION_KEY, operationKey);
        session.setAttribute(SESSION_TENANT, tenantId);
        session.setAttribute(SESSION_COMPANY, companyId);

        populateOptions(model, tenantId, companyId);

        model.addAttribute("operationKey", operationKey);
        model.addAttribute("movementDate", LocalDate.now());

        return VIEW;
    }

    @PostMapping
    public String postTransfer(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long itemId,
            @RequestParam(name = "sourceLocationId", required = false)
            String sourceSelection,
            @RequestParam(required = false) Long targetLocationId,
            @RequestParam(required = false) BigDecimal quantity,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate movementDate,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) String operationKey,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long tenantId = tenantContext.getCurrentTenantId();
        requireProfessional(tenantId);

        Long companyId;

        try {
            companyId = companyContext.requireCurrentCompanyId();
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Seleziona una società prima di registrare il trasferimento."
            );
            return "redirect:/company/select";
        }

        try {
            requireSessionOperation(
                    session, tenantId, companyId, operationKey
            );

            if (warehouseId == null
                    || warehouseId <= 0
                    || itemId == null
                    || itemId <= 0
                    || targetLocationId == null
                    || targetLocationId <= 0) {
                throw new BusinessException(
                        "Magazzino, articolo e ubicazioni sono obbligatori."
                );
            }

            Long sourceLocationId =
                    InventoryLocationTransferSourceSelection
                            .parse(sourceSelection);

            var warehouses = warehouseService.findActive(
                    tenantId, companyId
            );

            boolean allowedWarehouse = warehouses.stream()
                    .anyMatch(w -> warehouseId.equals(w.getId()));

            if (!allowedWarehouse) {
                throw new BusinessException(
                        "Magazzino non disponibile per la società attiva."
                );
            }

            // Controllo server-side: non ci fidiamo delle
            // ubicazioni indicate dal browser.
            var allowedLocations = locationService.findAll(
                    tenantId, companyId, warehouseId
            ).stream().filter(l -> l.isActive()).toList();

            boolean validSource = sourceLocationId == null
                    || allowedLocations.stream()
                            .anyMatch(l -> sourceLocationId.equals(l.getId()));

            boolean validTarget = allowedLocations.stream()
                    .anyMatch(l -> targetLocationId.equals(l.getId()));

            if (!validSource || !validTarget) {
                throw new BusinessException(
                        "Le ubicazioni devono essere attive "
                        + "e appartenere al magazzino selezionato."
                );
            }

            // Il coordinatore verifica nuovamente contesto,
            // lock, disponibilità FIFO e idempotenza.
            Long transferId = coordinator.post(
                    tenantId,
                    companyId,
                    warehouseId,
                    itemId,
                    sourceLocationId,
                    targetLocationId,
                    quantity,
                    movementDate,
                    notes,
                    operationKey
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Trasferimento registrato correttamente. "
                    + "Identificativo n. " + transferId
            );

            return "redirect:/inventory/transfers/new";

        } catch (BusinessException ex) {

            populateOptions(model, tenantId, companyId);

            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("warehouseId", warehouseId);
            model.addAttribute("itemId", itemId);
            model.addAttribute("sourceLocationId", sourceSelection);
            model.addAttribute("targetLocationId", targetLocationId);
            model.addAttribute("quantity", quantity);
            model.addAttribute("movementDate", movementDate);
            model.addAttribute("notes", notes);
            model.addAttribute("operationKey", operationKey);

            return VIEW;
        }
    }

    private void requireProfessional(Long tenantId) {
        boolean allowed = tenantId != null
                && tenantRepository.findById(tenantId)
                .map(t -> t.isSubscriptionActive()
                        && "PROFESSIONAL".equalsIgnoreCase(
                                t.getSubscriptionPlan()))
                .orElse(false);

        if (!allowed) {
            throw new AccessDeniedException(
                    "Funzionalità riservata al piano Professional attivo."
            );
        }
    }

    private void requireSessionOperation(
            HttpSession session,
            Long tenantId,
            Long companyId,
            String operationKey) {

        Object expected = session.getAttribute(SESSION_KEY);

        if (!(expected instanceof String)
                || operationKey == null
                || !expected.equals(operationKey)
                || !Objects.equals(
                        session.getAttribute(SESSION_TENANT),
                        tenantId)
                || !Objects.equals(
                        session.getAttribute(SESSION_COMPANY),
                        companyId)) {

            throw new BusinessException(
                    "Operazione non valida o sessione scaduta. "
                    + "Apri nuovamente il modulo di trasferimento."
            );
        }

        try {
            UUID.fromString(operationKey);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(
                    "Chiave operazione non valida."
            );
        }
    }

    private void populateOptions(
            Model model,
            Long tenantId,
            Long companyId) {

        var warehouses = warehouseService.findActive(
                tenantId, companyId
        );

        var locations = warehouses.stream()
                .flatMap(warehouse ->
                        locationService.findAll(
                                tenantId,
                                companyId,
                                warehouse.getId()
                        ).stream()
                        .filter(location -> location.isActive())
                        .map(location -> new LocationOption(
                                location.getId(),
                                warehouse.getId(),
                                location.getCode(),
                                location.getName()
                        ))
                ).toList();

        model.addAttribute(
                "itemOptions",
                itemWebService.findStockManagedOptions(tenantId)
        );

        model.addAttribute("warehouseOptions", warehouses);
        model.addAttribute("locationOptions", locations);
        model.addAttribute("activeMenu", "inventoryTransfers");
    }

    public record LocationOption(
            Long id,
            Long warehouseId,
            String code,
            String name) {
    }
}
