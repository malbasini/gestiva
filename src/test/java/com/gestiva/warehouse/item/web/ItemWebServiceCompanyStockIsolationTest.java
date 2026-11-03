package com.gestiva.warehouse.item.web;

import com.gestiva.warehouse.inventory.movement.web.InventoryLedgerWebService;
import com.gestiva.warehouse.item.entity.Item;
import com.gestiva.warehouse.item.entity.ItemType;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemWebServiceCompanyStockIsolationTest {

    @Mock
    ItemRepository itemRepository;

    @Mock
    InventoryMovementRepository inventoryMovementRepository;

    @Mock
    InventoryLedgerWebService inventoryLedgerWebService;

    @Test
    void shouldReadCompanyStockThroughInventoryLedger() {
        ItemWebService service = mock(ItemWebService.class, CALLS_REAL_METHODS);

        ReflectionTestUtils.setField(
                service, "itemRepository", itemRepository
        );
        ReflectionTestUtils.setField(
                service, "inventoryMovementRepository",
                inventoryMovementRepository
        );
        ReflectionTestUtils.setField(
                service, "inventoryLedgerWebService",
                inventoryLedgerWebService
        );

        Item item = mock(Item.class);

        when(item.getTenantId()).thenReturn(999L);
        when(item.getId()).thenReturn(77L);
        when(item.isTrackStock()).thenReturn(true);
        when(item.getItemType()).thenReturn(ItemType.PRODUCT);

        var baseUom = mock(
                com.gestiva.warehouse.uom.entity.UnitOfMeasure.class
        );
        when(baseUom.getCode()).thenReturn("PZ");
        when(item.getBaseUom()).thenReturn(baseUom);


        when(itemRepository.findByTenantIdAndId(999L, 77L))
                .thenReturn(Optional.of(item));

        when(inventoryLedgerWebService.calculateCurrentStock(999L, 77L))
                .thenReturn(new BigDecimal("13.000"));

        service.getDetail(999L, 77L);

        verify(inventoryLedgerWebService)
                .calculateCurrentStock(999L, 77L);

        verify(inventoryMovementRepository, never())
                .calculateInventoryBalance(anyLong(), anyLong());
    }
}
