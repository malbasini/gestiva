package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Gestiva ERP V2
 *
 * Trasferimenti FIFO tra ubicazioni dello stesso magazzino.
 *
 * Contratto iniziale.
 * La registrazione effettiva sarà implementata e collaudata
 * nei successivi passi.
 */
@Service
@Transactional
public class InventoryLocationTransferService {

    private final TenantContext tenantContext;
    private final CompanyContext companyContext;
    private final InventoryLocationValidationService locationValidationService;

    public InventoryLocationTransferService(
            TenantContext tenantContext,
            CompanyContext companyContext,
            InventoryLocationValidationService locationValidationService) {
        this.tenantContext = java.util.Objects.requireNonNull(
                tenantContext, "tenantContext");
        this.companyContext = java.util.Objects.requireNonNull(
                companyContext, "companyContext");
        this.locationValidationService = java.util.Objects.requireNonNull(
                locationValidationService, "locationValidationService");
    }

    public Long transferBetweenLocations(
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long itemId,
            Long sourceLocationId,
            Long targetLocationId,
            BigDecimal quantity,
            LocalDate movementDate,
            String notes) {

        if (tenantId == null
                || companyId == null
                || warehouseId == null
                || itemId == null
                || movementDate == null) {
            throw new BusinessException(
                    "Tenant, società, magazzino, articolo e data sono obbligatori."
            );
        }

        if (sourceLocationId == null || targetLocationId == null) {
            throw new BusinessException(
                    "Le ubicazioni di origine e destinazione sono obbligatorie."
            );
        }

        if (sourceLocationId.equals(targetLocationId)) {
            throw new BusinessException(
                    "Le ubicazioni di origine e destinazione devono essere diverse."
            );
        }

        if (quantity == null
                || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "La quantità da trasferire deve essere maggiore di zero."
            );
        }

        try {
            quantity.setScale(3, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw new BusinessException(
                    "La quantità da trasferire non può superare tre decimali."
            );
        }

        Long authenticatedTenantId =
                tenantContext.getCurrentTenantId();

        if (!java.util.Objects.equals(
                tenantId, authenticatedTenantId)) {
            throw new BusinessException(
                    "Il tenant indicato non corrisponde al tenant autenticato."
            );
        }

        Long activeCompanyId = companyContext.requireCurrentCompanyId();

        if (!java.util.Objects.equals(companyId, activeCompanyId)) {
            throw new BusinessException(
                    "La società indicata non corrisponde alla società attiva."
            );
        }

        locationValidationService.validateWarehouse(
                tenantId,
                companyId,
                warehouseId
        );

        locationValidationService.validateLocation(
                tenantId,
                warehouseId,
                sourceLocationId
        );

        locationValidationService.validateLocation(
                tenantId,
                warehouseId,
                targetLocationId
        );

        throw new BusinessException(
                "Trasferimenti tra ubicazioni non ancora abilitati."
        );
    }
}
