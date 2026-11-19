package com.gestiva.warehouse.inventory.valuation.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryAverageBalanceRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.item.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryAvailabilityLocationIsolationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 3L;
    private static final Long WAREHOUSE = 10L;
    private static final Long ITEM = 20L;

    @Mock InventoryLayerRepository inventoryLayerRepository;
    @Mock ItemRepository itemRepository;
    @Mock TenantRepository tenantRepository;
    @Mock InventoryAverageBalanceRepository inventoryAverageBalanceRepository;
    @Mock InventoryMovementRepository inventoryMovementRepository;

    @InjectMocks
    InventoryAvailabilityService service;

    @Test
    void aggregateAvailabilityMustPreserveWarehouseWideBalance() {

        when(inventoryMovementRepository
                .calculateInventoryBalanceByCompanyAndWarehouse(
                        TENANT, COMPANY, WAREHOUSE, ITEM, null
                )).thenReturn(new BigDecimal("8.000"));

        BigDecimal available =
                service.getAvailableQtyByCompanyAndWarehouse(
                        TENANT, COMPANY, WAREHOUSE, ITEM, null
                );

        assertEquals(0, available.compareTo(new BigDecimal("8.000")));
    }

    @Test
    void outboundWithoutLocationMustNotUseAggregateAvailability() {

        // Il riepilogo aggregato contiene 8 PZ ubicati.
        // Non è dimostrata alcuna disponibilità non ubicata.
        // Uno scarico senza ubicazione non può usare il totale.
        lenient().when(inventoryMovementRepository
                .calculateInventoryBalanceByCompanyAndWarehouse(
                        TENANT, COMPANY, WAREHOUSE, ITEM, null
                )).thenReturn(new BigDecimal("8.000"));

        assertThrows(
                BusinessException.class,
                () -> service.validateAvailabilityByCompanyAndWarehouse(
                        TENANT, COMPANY, WAREHOUSE, ITEM,
                        null, new BigDecimal("3.000")
                )
        );

        // La validazione operativa NULL non deve usare
        // la query aggregata come autorizzazione allo scarico.
        verify(inventoryMovementRepository, never())
                .calculateInventoryBalanceByCompanyAndWarehouse(
                        TENANT, COMPANY, WAREHOUSE, ITEM, null
                );
    }

    @Test
    void outboundWithoutLocationUsesOnlyUnlocatedAvailability() {

        when(inventoryMovementRepository
                .calculateUnlocatedInventoryBalanceByCompanyAndWarehouse(
                        TENANT, COMPANY, WAREHOUSE, ITEM
                )).thenReturn(new BigDecimal("5.000"));

        assertDoesNotThrow(
                () -> service.validateAvailabilityByCompanyAndWarehouse(
                        TENANT,
                        COMPANY,
                        WAREHOUSE,
                        ITEM,
                        null,
                        new BigDecimal("3.000")
                )
        );

        verify(inventoryMovementRepository, times(1))
                .calculateUnlocatedInventoryBalanceByCompanyAndWarehouse(
                        TENANT, COMPANY, WAREHOUSE, ITEM
                );

        verify(inventoryMovementRepository, never())
                .calculateInventoryBalanceByCompanyAndWarehouse(
                        TENANT, COMPANY, WAREHOUSE, ITEM, null
                );

        verifyNoMoreInteractions(inventoryMovementRepository);
    }


    @Test
    void outboundFromSpecificLocationUsesOnlyThatLocation() {

        Long locationId = 101L;

        when(inventoryMovementRepository
                .calculateInventoryBalanceByCompanyAndWarehouse(
                        TENANT, COMPANY, WAREHOUSE, ITEM, locationId
                )).thenReturn(new BigDecimal("5.000"));

        assertDoesNotThrow(
                () -> service.validateAvailabilityByCompanyAndWarehouse(
                        TENANT,
                        COMPANY,
                        WAREHOUSE,
                        ITEM,
                        locationId,
                        new BigDecimal("3.000")
                )
        );

        verify(inventoryMovementRepository, times(1))
                .calculateInventoryBalanceByCompanyAndWarehouse(
                        TENANT, COMPANY, WAREHOUSE, ITEM, locationId
                );

        verify(inventoryMovementRepository, never())
                .calculateUnlocatedInventoryBalanceByCompanyAndWarehouse(
                        any(), any(), any(), any()
                );

        verifyNoMoreInteractions(inventoryMovementRepository);
    }


    @Test
    void outboundFromSpecificLocationRejectsInsufficientStock() {

        Long locationId = 101L;

        when(inventoryMovementRepository
                .calculateInventoryBalanceByCompanyAndWarehouse(
                        TENANT, COMPANY, WAREHOUSE, ITEM, locationId
                )).thenReturn(new BigDecimal("2.000"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.validateAvailabilityByCompanyAndWarehouse(
                        TENANT,
                        COMPANY,
                        WAREHOUSE,
                        ITEM,
                        locationId,
                        new BigDecimal("3.000")
                )
        );

        assertTrue(exception.getMessage()
                .contains("Disponibilità insufficiente"));

        verify(inventoryMovementRepository, times(1))
                .calculateInventoryBalanceByCompanyAndWarehouse(
                        TENANT, COMPANY, WAREHOUSE, ITEM, locationId
                );

        verify(inventoryMovementRepository, never())
                .calculateUnlocatedInventoryBalanceByCompanyAndWarehouse(
                        any(), any(), any(), any()
                );

        verifyNoMoreInteractions(inventoryMovementRepository);
    }


    @Test
    void unlocatedAvailabilityMustPreserveTenantCompanyScope() {

        when(inventoryMovementRepository
                .calculateUnlocatedInventoryBalanceByCompanyAndWarehouse(
                        TENANT, COMPANY, WAREHOUSE, ITEM
                )).thenReturn(new BigDecimal("4.000"));

        assertDoesNotThrow(
                () -> service.validateAvailabilityByCompanyAndWarehouse(
                        TENANT,
                        COMPANY,
                        WAREHOUSE,
                        ITEM,
                        null,
                        new BigDecimal("2.000")
                )
        );

        verify(inventoryMovementRepository, times(1))
                .calculateUnlocatedInventoryBalanceByCompanyAndWarehouse(
                        eq(TENANT),
                        eq(COMPANY),
                        eq(WAREHOUSE),
                        eq(ITEM)
                );

        verifyNoMoreInteractions(inventoryMovementRepository);
    }

}
