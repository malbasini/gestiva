package com.gestiva.warehouse.inventory.valuation.service;

import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.warehouse.inventory.movement.entity.InventoryMovement;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayerConsumption;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryAverageBalanceRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerConsumptionRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryFifoLocationIsolationTest {

    private static final Long TENANT = 999L;
    private static final Long COMPANY = 123L;
    private static final Long WAREHOUSE = 45L;
    private static final Long ITEM = 67L;
    private static final Long MOVEMENT = 89L;

    @Mock TenantRepository tenantRepository;
    @Mock InventoryMovementRepository inventoryMovementRepository;
    @Mock InventoryLayerRepository inventoryLayerRepository;
    @Mock InventoryLayerConsumptionRepository consumptionRepository;
    @Mock InventoryAverageBalanceRepository averageBalanceRepository;
    @Mock WarehouseRepository warehouseRepository;

    @InjectMocks InventoryValuationService service;

    @Test
    void outboundFromLocationBMustNotConsumeOlderLayerFromLocationA() {

        when(warehouseRepository.findByTenantAndCompanyAndIdForUpdate(
                TENANT, COMPANY, WAREHOUSE
        )).thenReturn(Optional.of(mock(Warehouse.class)));

        InventoryMovement movement = new InventoryMovement();
        movement.setWarehouseId(WAREHOUSE);
        movement.setItemId(ITEM);
        movement.setLocationId(102L);
        movement.setMovementType("OUT");
        movement.setQuantity(new BigDecimal("3.000"));

        when(inventoryMovementRepository.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, MOVEMENT
        )).thenReturn(Optional.of(movement));

        InventoryLayer layerA = mock(InventoryLayer.class);
        lenient().when(layerA.getId()).thenReturn(201L);
        lenient().when(layerA.getRemainingQty())
                .thenReturn(new BigDecimal("5.000"));
        lenient().when(layerA.getUnitCost())
                .thenReturn(new BigDecimal("8.0000"));
        when(layerA.getLocationId()).thenReturn(101L);

        InventoryLayer layerB = mock(InventoryLayer.class);
        when(layerB.getId()).thenReturn(202L);
        when(layerB.getRemainingQty())
                .thenReturn(new BigDecimal("5.000"));
        when(layerB.getUnitCost())
                .thenReturn(new BigDecimal("10.0000"));
        when(layerB.getLocationId()).thenReturn(102L);

        when(inventoryLayerRepository
                .findByTenantIdAndCompanyIdAndWarehouseIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
                        TENANT, COMPANY, WAREHOUSE, ITEM
                )).thenReturn(List.of(layerA, layerB));

        when(consumptionRepository
                .findByTenantIdAndCompanyIdAndWarehouseIdAndOutMovementIdOrderByIdAsc(
                        TENANT, COMPANY, WAREHOUSE, MOVEMENT
                )).thenReturn(List.of());

        service.applyOutboundValuation(
                TENANT, COMPANY, WAREHOUSE, MOVEMENT
        );

        ArgumentCaptor<InventoryLayerConsumption> captured =
                ArgumentCaptor.forClass(InventoryLayerConsumption.class);

        verify(consumptionRepository, times(1)).save(captured.capture());

        InventoryLayerConsumption consumption = captured.getValue();

        assertEquals(202L, consumption.getLayerId(),
                "Lo scarico da ubicazione 102 deve consumare il layer B");
        assertEquals(0, new BigDecimal("3.000")
                .compareTo(consumption.getConsumedQty()));
        assertEquals(0, new BigDecimal("30.0000")
                .compareTo(consumption.getTotalCost()));

        verify(layerA, never()).setRemainingQty(any());
        verify(layerB).setRemainingQty(new BigDecimal("2.000"));
    }

    @Test
    void outboundWithoutLocationPreservesLegacyWarehouseWideFifo() {

        when(warehouseRepository.findByTenantAndCompanyAndIdForUpdate(
                TENANT, COMPANY, WAREHOUSE
        )).thenReturn(Optional.of(mock(Warehouse.class)));

        InventoryMovement movement = new InventoryMovement();
        movement.setWarehouseId(WAREHOUSE);
        movement.setItemId(ITEM);
        movement.setLocationId(null);
        movement.setMovementType("OUT");
        movement.setQuantity(new BigDecimal("3.000"));

        when(inventoryMovementRepository.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, MOVEMENT
        )).thenReturn(Optional.of(movement));

        InventoryLayer olderLayer = mock(InventoryLayer.class);
        when(olderLayer.getLocationId()).thenReturn(null);
        when(olderLayer.getId()).thenReturn(201L);
        when(olderLayer.getRemainingQty())
                .thenReturn(new BigDecimal("5.000"));
        when(olderLayer.getUnitCost())
                .thenReturn(new BigDecimal("8.0000"));

        InventoryLayer newerLayer = mock(InventoryLayer.class);

        when(inventoryLayerRepository
                .findByTenantIdAndCompanyIdAndWarehouseIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
                        TENANT, COMPANY, WAREHOUSE, ITEM
                )).thenReturn(List.of(olderLayer, newerLayer));

        when(consumptionRepository
                .findByTenantIdAndCompanyIdAndWarehouseIdAndOutMovementIdOrderByIdAsc(
                        TENANT, COMPANY, WAREHOUSE, MOVEMENT
                )).thenReturn(List.of());

        service.applyOutboundValuation(
                TENANT, COMPANY, WAREHOUSE, MOVEMENT
        );

        ArgumentCaptor<InventoryLayerConsumption> captured =
                ArgumentCaptor.forClass(InventoryLayerConsumption.class);

        verify(consumptionRepository, times(1)).save(captured.capture());

        assertEquals(201L, captured.getValue().getLayerId());
        assertEquals(0, new BigDecimal("24.0000")
                .compareTo(captured.getValue().getTotalCost()));

        verify(olderLayer).setRemainingQty(new BigDecimal("2.000"));
        verifyNoInteractions(newerLayer);
    }


    @Test
    void outboundMustRejectStockAvailableOnlyInAnotherLocation() {

        when(warehouseRepository.findByTenantAndCompanyAndIdForUpdate(
                TENANT, COMPANY, WAREHOUSE
        )).thenReturn(Optional.of(mock(Warehouse.class)));

        InventoryMovement movement = new InventoryMovement();
        movement.setWarehouseId(WAREHOUSE);
        movement.setItemId(ITEM);
        movement.setLocationId(102L);
        movement.setMovementType("OUT");
        movement.setQuantity(new BigDecimal("3.000"));

        when(inventoryMovementRepository.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, MOVEMENT
        )).thenReturn(Optional.of(movement));

        InventoryLayer otherLocationLayer = mock(InventoryLayer.class);

        when(otherLocationLayer.getLocationId()).thenReturn(101L);

        when(inventoryLayerRepository
                .findByTenantIdAndCompanyIdAndWarehouseIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
                        TENANT, COMPANY, WAREHOUSE, ITEM
                )).thenReturn(List.of(otherLocationLayer));

        when(consumptionRepository
                .findByTenantIdAndCompanyIdAndWarehouseIdAndOutMovementIdOrderByIdAsc(
                        TENANT, COMPANY, WAREHOUSE, MOVEMENT
                )).thenReturn(List.of());

        org.junit.jupiter.api.Assertions.assertThrows(
                com.gestiva.common.exception.BusinessException.class,
                () -> service.applyOutboundValuation(
                        TENANT, COMPANY, WAREHOUSE, MOVEMENT
                )
        );

        verify(consumptionRepository, never()).save(any());
        verify(inventoryLayerRepository, never()).save(any());
        verify(inventoryMovementRepository, never()).save(any());
        verify(otherLocationLayer, never()).setRemainingQty(any());
    }


    @Test
    void outboundFromLocationMustSkipOlderUnlocatedLayer() {

        when(warehouseRepository.findByTenantAndCompanyAndIdForUpdate(
                TENANT, COMPANY, WAREHOUSE
        )).thenReturn(Optional.of(mock(Warehouse.class)));

        InventoryMovement movement = new InventoryMovement();
        movement.setWarehouseId(WAREHOUSE);
        movement.setItemId(ITEM);
        movement.setLocationId(102L);
        movement.setMovementType("OUT");
        movement.setQuantity(new BigDecimal("3.000"));

        when(inventoryMovementRepository.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, MOVEMENT
        )).thenReturn(Optional.of(movement));

        InventoryLayer legacyLayer = mock(InventoryLayer.class);
        // getLocationId() restituisce null sul mock non configurato.

        InventoryLayer locatedLayer = mock(InventoryLayer.class);
        when(locatedLayer.getLocationId()).thenReturn(102L);
        when(locatedLayer.getId()).thenReturn(202L);
        when(locatedLayer.getRemainingQty())
                .thenReturn(new BigDecimal("5.000"));
        when(locatedLayer.getUnitCost())
                .thenReturn(new BigDecimal("10.0000"));

        when(inventoryLayerRepository
                .findByTenantIdAndCompanyIdAndWarehouseIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
                        TENANT, COMPANY, WAREHOUSE, ITEM
                )).thenReturn(List.of(legacyLayer, locatedLayer));

        when(consumptionRepository
                .findByTenantIdAndCompanyIdAndWarehouseIdAndOutMovementIdOrderByIdAsc(
                        TENANT, COMPANY, WAREHOUSE, MOVEMENT
                )).thenReturn(List.of());

        service.applyOutboundValuation(
                TENANT, COMPANY, WAREHOUSE, MOVEMENT
        );

        ArgumentCaptor<InventoryLayerConsumption> captured =
                ArgumentCaptor.forClass(InventoryLayerConsumption.class);

        verify(consumptionRepository, times(1)).save(captured.capture());

        assertEquals(202L, captured.getValue().getLayerId());
        assertEquals(0, new BigDecimal("3.000")
                .compareTo(captured.getValue().getConsumedQty()));
        assertEquals(0, new BigDecimal("30.0000")
                .compareTo(captured.getValue().getTotalCost()));

        verify(legacyLayer, never()).getRemainingQty();
        verify(legacyLayer, never()).setRemainingQty(any());
        verify(locatedLayer).setRemainingQty(new BigDecimal("2.000"));
    }


    @Test
    void outboundWithoutLocationMustSkipLocatedLayer() {

        when(warehouseRepository.findByTenantAndCompanyAndIdForUpdate(
                TENANT, COMPANY, WAREHOUSE
        )).thenReturn(Optional.of(mock(Warehouse.class)));

        InventoryMovement movement = new InventoryMovement();
        movement.setWarehouseId(WAREHOUSE);
        movement.setItemId(ITEM);
        movement.setLocationId(null);
        movement.setMovementType("OUT");
        movement.setQuantity(new BigDecimal("3.000"));

        when(inventoryMovementRepository.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, MOVEMENT
        )).thenReturn(Optional.of(movement));

        InventoryLayer locatedLayer = mock(InventoryLayer.class);
        when(locatedLayer.getLocationId()).thenReturn(101L);

        InventoryLayer unlocatedLayer = mock(InventoryLayer.class);
        when(unlocatedLayer.getLocationId()).thenReturn(null);
        lenient().when(unlocatedLayer.getId()).thenReturn(202L);
        lenient().when(unlocatedLayer.getRemainingQty())
                .thenReturn(new BigDecimal("5.000"));
        lenient().when(unlocatedLayer.getUnitCost())
                .thenReturn(new BigDecimal("10.0000"));

        when(inventoryLayerRepository
                .findByTenantIdAndCompanyIdAndWarehouseIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
                        TENANT, COMPANY, WAREHOUSE, ITEM
                )).thenReturn(List.of(locatedLayer, unlocatedLayer));

        when(consumptionRepository
                .findByTenantIdAndCompanyIdAndWarehouseIdAndOutMovementIdOrderByIdAsc(
                        TENANT, COMPANY, WAREHOUSE, MOVEMENT
                )).thenReturn(List.of());

        service.applyOutboundValuation(
                TENANT, COMPANY, WAREHOUSE, MOVEMENT
        );

        ArgumentCaptor<InventoryLayerConsumption> captured =
                ArgumentCaptor.forClass(InventoryLayerConsumption.class);

        verify(consumptionRepository, times(1)).save(captured.capture());

        assertEquals(202L, captured.getValue().getLayerId(),
                "Lo scarico NULL non deve consumare layer ubicati");
        assertEquals(0, new BigDecimal("30.0000")
                .compareTo(captured.getValue().getTotalCost()));

        verify(locatedLayer, never()).getRemainingQty();
        verify(locatedLayer, never()).setRemainingQty(any());
        verify(unlocatedLayer).setRemainingQty(new BigDecimal("2.000"));
    }

}
