package com.gestiva.warehouse.inventory.valuation.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.warehouse.inventory.movement.entity.InventoryMovement;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryAverageBalanceRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerConsumptionRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.repository.WarehouseRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryFifoReversalSafetyTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;
    private static final Long WAREHOUSE_ID = 45L;
    private static final Long ITEM_ID = 67L;
    private static final Long MOVEMENT_ID = 89L;

    @Mock TenantRepository tenantRepository;
    @Mock InventoryMovementRepository inventoryMovementRepository;
    @Mock InventoryLayerRepository inventoryLayerRepository;
    @Mock InventoryLayerConsumptionRepository inventoryLayerConsumptionRepository;
    @Mock InventoryAverageBalanceRepository inventoryAverageBalanceRepository;
    @Mock WarehouseRepository warehouseRepository;

    @InjectMocks InventoryValuationService service;

    @Test
    void shouldRejectFifoReversalWhenSubsequentMovementsExist() {

        Warehouse warehouse = mock(Warehouse.class);

        when(warehouseRepository
                .findByTenantAndCompanyAndIdForUpdate(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(warehouse));

        InventoryMovement original = mock(InventoryMovement.class);

        when(original.getWarehouseId()).thenReturn(WAREHOUSE_ID);
        when(original.getMovementType()).thenReturn("OUT");
        when(original.getItemId()).thenReturn(ITEM_ID);

        when(inventoryMovementRepository
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, MOVEMENT_ID))
                .thenReturn(Optional.of(original));

        when(inventoryMovementRepository
                .existsSubsequentActiveOriginalMovements(
                        TENANT_ID,
                        COMPANY_ID,
                        WAREHOUSE_ID,
                        ITEM_ID,
                        MOVEMENT_ID))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.reverseFifoValuation(
                        TENANT_ID,
                        COMPANY_ID,
                        WAREHOUSE_ID,
                        MOVEMENT_ID));

        assertTrue(
                exception.getMessage().contains("movimenti successivi"),
                "Deve essere segnalata la presenza di movimenti successivi"
        );

        verify(warehouseRepository)
                .findByTenantAndCompanyAndIdForUpdate(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID);

        verify(inventoryMovementRepository)
                .existsSubsequentActiveOriginalMovements(
                        TENANT_ID,
                        COMPANY_ID,
                        WAREHOUSE_ID,
                        ITEM_ID,
                        MOVEMENT_ID);

        // Non deve nemmeno iniziare il recupero dei consumi FIFO.
        verifyNoInteractions(
                inventoryLayerRepository,
                inventoryLayerConsumptionRepository,
                inventoryAverageBalanceRepository
        );

        verify(inventoryMovementRepository, never()).save(any());
    }

    @Test
    void shouldRestoreTwoFifoLayersWithCorrectQuantityAndValue() {

        when(warehouseRepository
                .findByTenantAndCompanyAndIdForUpdate(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(mock(Warehouse.class)));

        InventoryMovement original = new InventoryMovement();
        original.setWarehouseId(WAREHOUSE_ID);
        original.setItemId(ITEM_ID);
        original.setMovementType("OUT");
        original.setQuantity(new java.math.BigDecimal("10.000"));
        original.setTotalCost(new java.math.BigDecimal("108.0000"));

        when(inventoryMovementRepository
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, MOVEMENT_ID))
                .thenReturn(Optional.of(original));

        // Non esistono movimenti originali successivi attivi.
        // Il repository mock restituisce false per impostazione predefinita.

        com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer layerA =
                new com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer();

        layerA.setWarehouseId(WAREHOUSE_ID);
        layerA.setItemId(ITEM_ID);
        layerA.setOriginalQty(new java.math.BigDecimal("10.000"));
        layerA.setRemainingQty(new java.math.BigDecimal("4.000"));
        layerA.setUnitCost(new java.math.BigDecimal("10.0000"));
        layerA.setClosed(false);

        com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer layerB =
                new com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer();

        layerB.setWarehouseId(WAREHOUSE_ID);
        layerB.setItemId(ITEM_ID);
        layerB.setOriginalQty(new java.math.BigDecimal("4.000"));
        layerB.setRemainingQty(new java.math.BigDecimal("0.000"));
        layerB.setUnitCost(new java.math.BigDecimal("12.0000"));
        layerB.setClosed(true);

        var consumptionA = mock(
                com.gestiva.warehouse.inventory.valuation.entity
                        .InventoryLayerConsumption.class);

        when(consumptionA.getLayerId()).thenReturn(201L);
        when(consumptionA.getConsumedQty())
                .thenReturn(new java.math.BigDecimal("6.000"));
        when(consumptionA.getTotalCost())
                .thenReturn(new java.math.BigDecimal("60.0000"));

        var consumptionB = mock(
                com.gestiva.warehouse.inventory.valuation.entity
                        .InventoryLayerConsumption.class);

        when(consumptionB.getLayerId()).thenReturn(202L);
        when(consumptionB.getConsumedQty())
                .thenReturn(new java.math.BigDecimal("4.000"));
        when(consumptionB.getTotalCost())
                .thenReturn(new java.math.BigDecimal("48.0000"));

        when(inventoryLayerConsumptionRepository
                .findByTenantIdAndCompanyIdAndWarehouseIdAndOutMovementIdOrderByIdAsc(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID, MOVEMENT_ID))
                .thenReturn(java.util.List.of(consumptionA, consumptionB));

        when(inventoryLayerRepository
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, 201L))
                .thenReturn(Optional.of(layerA));

        when(inventoryLayerRepository
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, 202L))
                .thenReturn(Optional.of(layerB));

        // Esegue realmente la logica Java di ripristino FIFO.
        // Sono simulati soltanto i repository.
        assertDoesNotThrow(
                () -> service.reverseFifoValuation(
                        TENANT_ID,
                        COMPANY_ID,
                        WAREHOUSE_ID,
                        MOVEMENT_ID));

        // Layer A: 4 + 6 = 10.
        assertEquals(
                0,
                layerA.getRemainingQty().compareTo(
                        new java.math.BigDecimal("10.000")));

        // Layer B: 0 + 4 = 4.
        assertEquals(
                0,
                layerB.getRemainingQty().compareTo(
                        new java.math.BigDecimal("4.000")));

        assertFalse(layerA.isClosed());
        assertFalse(layerB.isClosed());

        // Entrambi i layer devono essere salvati.
        verify(inventoryLayerRepository).save(same(layerA));
        verify(inventoryLayerRepository).save(same(layerB));

        // Verifica che i consumi utilizzati totalizzino
        // esattamente 10 pezzi e 108 euro.
        java.math.BigDecimal totalQuantity =
                consumptionA.getConsumedQty()
                        .add(consumptionB.getConsumedQty());

        java.math.BigDecimal totalValue =
                consumptionA.getTotalCost()
                        .add(consumptionB.getTotalCost());

        assertEquals(
                0,
                totalQuantity.compareTo(original.getQuantity()));

        assertEquals(
                0,
                totalValue.compareTo(original.getTotalCost()));

        verifyNoInteractions(inventoryAverageBalanceRepository);
    }


    @Test
    void shouldRejectFifoReversalWhenCostsDoNotMatch() {

        when(warehouseRepository
                .findByTenantAndCompanyAndIdForUpdate(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(mock(Warehouse.class)));

        InventoryMovement originalMovement = new InventoryMovement();
        originalMovement.setWarehouseId(WAREHOUSE_ID);
        originalMovement.setItemId(ITEM_ID);
        originalMovement.setMovementType("OUT");
        originalMovement.setQuantity(
                new java.math.BigDecimal("6.000"));
        originalMovement.setTotalCost(
                new java.math.BigDecimal("60.0000"));

        when(inventoryMovementRepository
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, MOVEMENT_ID))
                .thenReturn(Optional.of(originalMovement));

        var layer =
                new com.gestiva.warehouse.inventory.valuation.entity
                        .InventoryLayer();

        layer.setWarehouseId(WAREHOUSE_ID);
        layer.setItemId(ITEM_ID);
        layer.setOriginalQty(
                new java.math.BigDecimal("10.000"));
        layer.setRemainingQty(
                new java.math.BigDecimal("4.000"));
        layer.setUnitCost(
                new java.math.BigDecimal("10.0000"));
        layer.setClosed(false);

        var consumption =
                new com.gestiva.warehouse.inventory.valuation.entity
                        .InventoryLayerConsumption();

        consumption.setLayerId(201L);
        consumption.setConsumedQty(
                new java.math.BigDecimal("6.000"));

        // Anomalia intenzionale:
        // il consumo vale 59 euro,
        // ma lo scarico originale vale 60 euro.
        consumption.setTotalCost(
                new java.math.BigDecimal("59.0000"));

        when(inventoryLayerConsumptionRepository
                .findByTenantIdAndCompanyIdAndWarehouseIdAndOutMovementIdOrderByIdAsc(
                        TENANT_ID,
                        COMPANY_ID,
                        WAREHOUSE_ID,
                        MOVEMENT_ID))
                .thenReturn(java.util.List.of(consumption));

        when(inventoryLayerRepository
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, 201L))
                .thenReturn(Optional.of(layer));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.reverseFifoValuation(
                        TENANT_ID,
                        COMPANY_ID,
                        WAREHOUSE_ID,
                        MOVEMENT_ID));

        assertTrue(
                exception.getMessage().contains(
                        "valore dei consumi FIFO"),
                "Deve essere segnalata la mancata quadratura FIFO"
        );

        // Il controllo quantitativo è stato superato,
        // ma quello economico deve bloccare l'operazione.
        assertEquals(
                0,
                consumption.getConsumedQty().compareTo(
                        originalMovement.getQuantity()));

        assertNotEquals(
                0,
                consumption.getTotalCost().compareTo(
                        originalMovement.getTotalCost()));

        // La logica attuale salva il layer prima
        // del controllo finale dei valori.
        // Mockito NON effettua rollback dei suoi oggetti.
        verify(inventoryLayerRepository).save(same(layer));

        // Il movimento originale non deve essere salvato
        // da questo servizio.
        verify(inventoryMovementRepository, never()).save(any());
    }

}
