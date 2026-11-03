package com.gestiva.warehouse.inventory.valuation.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.warehouse.inventory.movement.entity.InventoryMovement;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayerConsumption;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerConsumptionRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.item.entity.Item;
import com.gestiva.warehouse.item.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryConsumptionWebServiceCompanyIsolationTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;
    private static final Long MOVEMENT_ID = 77L;
    private static final Long WAREHOUSE_ID = 55L;

    @Mock
    private CompanyContext companyContext;

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private InventoryLayerConsumptionRepository inventoryLayerConsumptionRepository;

    @Mock
    private InventoryLayerRepository inventoryLayerRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private InventoryConsumptionWebService service;

    @Test
    void shouldRejectMissingActiveCompany() {
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new IllegalStateException(
                        "Nessuna società attiva selezionata"
                ));

        assertThrows(
                IllegalStateException.class,
                () -> service.getOutboundValuationDetail(TENANT_ID, MOVEMENT_ID)
        );

        verifyNoInteractions(
                inventoryMovementRepository,
                inventoryLayerConsumptionRepository,
                inventoryLayerRepository,
                itemRepository
        );
    }

    @Test
    void shouldNotReadMovementOfAnotherCompany() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(inventoryMovementRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID, COMPANY_ID, MOVEMENT_ID
        )).thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> service.getOutboundValuationDetail(TENANT_ID, MOVEMENT_ID)
        );

        verify(inventoryMovementRepository)
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, MOVEMENT_ID
                );

        verifyNoInteractions(
                inventoryLayerConsumptionRepository,
                inventoryLayerRepository,
                itemRepository
        );
    }

    @Test
    void shouldFilterMovementConsumptionsAndLayersByCompanyAndWarehouse() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        InventoryMovement movement = mock(InventoryMovement.class);
        when(movement.getMovementType()).thenReturn("OUT");
        when(movement.getWarehouseId()).thenReturn(WAREHOUSE_ID);
        when(movement.getItemId()).thenReturn(88L);

        when(inventoryMovementRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID, COMPANY_ID, MOVEMENT_ID
        )).thenReturn(Optional.of(movement));

        Item item = mock(Item.class);
        when(itemRepository.findByTenantIdAndId(TENANT_ID, 88L))
                .thenReturn(Optional.of(item));

        when(inventoryLayerConsumptionRepository
                .findByTenantIdAndCompanyIdAndWarehouseIdAndOutMovementIdOrderByIdAsc(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID, MOVEMENT_ID
                )).thenReturn(List.of());

        service.getOutboundValuationDetail(TENANT_ID, MOVEMENT_ID);

        verify(inventoryMovementRepository)
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, MOVEMENT_ID
                );

        verify(inventoryLayerConsumptionRepository)
                .findByTenantIdAndCompanyIdAndWarehouseIdAndOutMovementIdOrderByIdAsc(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID, MOVEMENT_ID
                );

        verifyNoInteractions(inventoryLayerRepository);
    }

    @Test
    void shouldRejectLayerFromDifferentWarehouse() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        InventoryMovement movement = mock(InventoryMovement.class);
        when(movement.getMovementType()).thenReturn("OUT");
        when(movement.getWarehouseId()).thenReturn(WAREHOUSE_ID);
        when(movement.getItemId()).thenReturn(88L);

        when(inventoryMovementRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID, COMPANY_ID, MOVEMENT_ID
        )).thenReturn(Optional.of(movement));

        Item item = mock(Item.class);
        when(itemRepository.findByTenantIdAndId(TENANT_ID, 88L))
                .thenReturn(Optional.of(item));

        InventoryLayerConsumption consumption = mock(InventoryLayerConsumption.class);
        when(consumption.getLayerId()).thenReturn(999L);

        when(inventoryLayerConsumptionRepository
                .findByTenantIdAndCompanyIdAndWarehouseIdAndOutMovementIdOrderByIdAsc(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID, MOVEMENT_ID
                )).thenReturn(List.of(consumption));

        InventoryLayer layer = mock(InventoryLayer.class);
        when(layer.getWarehouseId()).thenReturn(66L);

        when(inventoryLayerRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID, COMPANY_ID, 999L
        )).thenReturn(Optional.of(layer));

        assertThrows(
                BusinessException.class,
                () -> service.getOutboundValuationDetail(TENANT_ID, MOVEMENT_ID)
        );
    }
}
