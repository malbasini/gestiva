package com.gestiva.warehouse.tracking.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.item.entity.InventoryTrackingType;
import com.gestiva.warehouse.item.entity.Item;
import com.gestiva.warehouse.item.entity.ItemType;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.tracking.entity.InventoryLot;
import com.gestiva.warehouse.tracking.repository.InventoryLotRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryLotServiceTest {

    private static final Long TENANT_ID = 2L;
    private static final Long ITEM_ID = 10L;
    private static final Long LOT_ID = 20L;

    private static final LocalDate PRODUCTION_DATE =
            LocalDate.of(2026, 1, 10);

    private static final LocalDate EXPIRATION_DATE =
            LocalDate.of(2027, 1, 10);

    @Mock
    private InventoryLotRepository lotRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private InventoryLotService service;

    private Item item;

    @BeforeEach
    void setUp() {

        item = new Item();

        item.setTenantId(TENANT_ID);
        item.setCode("PROD-001");
        item.setName("Prodotto di prova");

        item.setItemType(ItemType.PRODUCT);
        item.setTrackingType(InventoryTrackingType.LOT);

        item.setTrackExpiration(false);
        item.setActive(true);
    }

    private void configureItem() {

        when(itemRepository.findByTenantIdAndId(
                TENANT_ID,
                ITEM_ID
        )).thenReturn(Optional.of(item));
    }

    private void configureSave() {

        when(lotRepository.save(any(InventoryLot.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private InventoryLot createLotFixture() {

        InventoryLot lot = new InventoryLot();

        lot.setTenantId(TENANT_ID);
        lot.setItem(item);
        lot.setLotCode("LOT-001");
        lot.setProductionDate(PRODUCTION_DATE);
        lot.setExpirationDate(EXPIRATION_DATE);
        lot.setActive(true);

        return lot;
    }

    // =========================================================
    // CREAZIONE LOTTO
    // =========================================================

    @Test
    void create_shouldSaveValidLot() {

        configureItem();
        configureSave();

        InventoryLot result = service.create(
                TENANT_ID,
                ITEM_ID,
                " lot-001 ",
                PRODUCTION_DATE,
                EXPIRATION_DATE,
                " Prima fornitura "
        );

        assertNotNull(result);

        assertEquals(TENANT_ID, result.getTenantId());
        assertSame(item, result.getItem());

        assertEquals("LOT-001", result.getLotCode());
        assertEquals(PRODUCTION_DATE, result.getProductionDate());
        assertEquals(EXPIRATION_DATE, result.getExpirationDate());

        assertEquals("Prima fornitura", result.getNotes());
        assertTrue(result.isActive());

        verify(lotRepository).save(any(InventoryLot.class));
    }

    @Test
    void create_shouldAllowNullExpirationWhenNotRequired() {

        configureItem();
        configureSave();

        InventoryLot result = service.create(
                TENANT_ID,
                ITEM_ID,
                "LOT-001",
                PRODUCTION_DATE,
                null,
                null
        );

        assertNull(result.getExpirationDate());
    }

    @Test
    void create_shouldAllowNullProductionDate() {

        configureItem();
        configureSave();

        InventoryLot result = service.create(
                TENANT_ID,
                ITEM_ID,
                "LOT-001",
                null,
                EXPIRATION_DATE,
                null
        );

        assertNull(result.getProductionDate());
    }

    @Test
    void create_shouldNormalizeBlankNotesToNull() {

        configureItem();
        configureSave();

        InventoryLot result = service.create(
                TENANT_ID,
                ITEM_ID,
                "LOT-001",
                null,
                null,
                "   "
        );

        assertNull(result.getNotes());
    }

    @Test
    void create_shouldRejectNullTenant() {

        assertThrows(BusinessException.class, () ->
                service.create(
                        null,
                        ITEM_ID,
                        "LOT-001",
                        null,
                        null,
                        null
                )
        );

        verifyNoInteractions(itemRepository, lotRepository);
    }

    @Test
    void create_shouldRejectNullItemId() {

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        null,
                        "LOT-001",
                        null,
                        null,
                        null
                )
        );

        verifyNoInteractions(itemRepository, lotRepository);
    }

    @Test
    void create_shouldRejectItemFromAnotherTenant() {

        when(itemRepository.findByTenantIdAndId(
                TENANT_ID,
                ITEM_ID
        )).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        ITEM_ID,
                        "LOT-001",
                        null,
                        null,
                        null
                )
        );

        verify(lotRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectInactiveItem() {

        item.setActive(false);
        configureItem();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        ITEM_ID,
                        "LOT-001",
                        null,
                        null,
                        null
                )
        );

        verify(lotRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectServiceItem() {

        item.setItemType(ItemType.SERVICE);
        configureItem();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        ITEM_ID,
                        "LOT-001",
                        null,
                        null,
                        null
                )
        );

        verify(lotRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectTrackingNone() {

        item.setTrackingType(InventoryTrackingType.NONE);
        configureItem();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        ITEM_ID,
                        "LOT-001",
                        null,
                        null,
                        null
                )
        );

        verify(lotRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectTrackingSerial() {

        item.setTrackingType(InventoryTrackingType.SERIAL);
        configureItem();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        ITEM_ID,
                        "LOT-001",
                        null,
                        null,
                        null
                )
        );

        verify(lotRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectBlankLotCode() {

        configureItem();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        ITEM_ID,
                        "   ",
                        null,
                        null,
                        null
                )
        );
    }

    @Test
    void create_shouldRejectNullLotCode() {

        configureItem();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        ITEM_ID,
                        null,
                        null,
                        null,
                        null
                )
        );
    }

    @Test
    void create_shouldRejectLotCodeLongerThan100() {

        configureItem();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        ITEM_ID,
                        "A".repeat(101),
                        null,
                        null,
                        null
                )
        );
    }

    @Test
    void create_shouldRejectExpirationBeforeProduction() {

        configureItem();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        ITEM_ID,
                        "LOT-001",
                        EXPIRATION_DATE,
                        PRODUCTION_DATE,
                        null
                )
        );
    }

    @Test
    void create_shouldRequireExpirationWhenTrackingEnabled() {

        item.setTrackExpiration(true);
        configureItem();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        ITEM_ID,
                        "LOT-001",
                        PRODUCTION_DATE,
                        null,
                        null
                )
        );

        verify(lotRepository, never()).save(any());
    }

    @Test
    void create_shouldAllowExpirationWhenTrackingEnabled() {

        item.setTrackExpiration(true);

        configureItem();
        configureSave();

        InventoryLot result = service.create(
                TENANT_ID,
                ITEM_ID,
                "LOT-001",
                PRODUCTION_DATE,
                EXPIRATION_DATE,
                null
        );

        assertEquals(EXPIRATION_DATE, result.getExpirationDate());
    }

    @Test
    void create_shouldRejectNotesLongerThan500() {

        configureItem();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        ITEM_ID,
                        "LOT-001",
                        null,
                        null,
                        "A".repeat(501)
                )
        );
    }

    @Test
    void create_shouldRejectDuplicateLotCode() {

        configureItem();

        when(lotRepository.existsByTenantIdAndItemIdAndLotCode(
                TENANT_ID,
                ITEM_ID,
                "LOT-001"
        )).thenReturn(true);

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        ITEM_ID,
                        "lot-001",
                        null,
                        null,
                        null
                )
        );

        verify(lotRepository, never()).save(any());
    }

    // =========================================================
    // RECUPERO LOTTO
    // =========================================================

    @Test
    void getById_shouldReturnLot() {

        InventoryLot lot = createLotFixture();

        when(lotRepository.findByTenantIdAndItemIdAndId(
                TENANT_ID,
                ITEM_ID,
                LOT_ID
        )).thenReturn(Optional.of(lot));

        InventoryLot result = service.getById(
                TENANT_ID,
                ITEM_ID,
                LOT_ID
        );

        assertSame(lot, result);
    }

    @Test
    void getById_shouldRejectMissingLot() {

        when(lotRepository.findByTenantIdAndItemIdAndId(
                TENANT_ID,
                ITEM_ID,
                LOT_ID
        )).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                service.getById(
                        TENANT_ID,
                        ITEM_ID,
                        LOT_ID
                )
        );
    }

    @Test
    void getById_shouldRejectNullLotId() {

        assertThrows(BusinessException.class, () ->
                service.getById(
                        TENANT_ID,
                        ITEM_ID,
                        null
                )
        );

        verifyNoInteractions(lotRepository);
    }

    @Test
    void getById_shouldRejectNullTenant() {

        assertThrows(BusinessException.class, () ->
                service.getById(
                        null,
                        ITEM_ID,
                        LOT_ID
                )
        );

        verifyNoInteractions(lotRepository);
    }

    // =========================================================
    // RICERCA PER CODICE
    // =========================================================

    @Test
    void getByCode_shouldNormalizeAndReturnLot() {

        InventoryLot lot = createLotFixture();

        when(lotRepository.findByTenantIdAndItemIdAndLotCode(
                TENANT_ID,
                ITEM_ID,
                "LOT-001"
        )).thenReturn(Optional.of(lot));

        InventoryLot result = service.getByCode(
                TENANT_ID,
                ITEM_ID,
                " lot-001 "
        );

        assertSame(lot, result);
    }

    @Test
    void getByCode_shouldRejectMissingLot() {

        when(lotRepository.findByTenantIdAndItemIdAndLotCode(
                TENANT_ID,
                ITEM_ID,
                "LOT-999"
        )).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                service.getByCode(
                        TENANT_ID,
                        ITEM_ID,
                        "LOT-999"
                )
        );
    }

    // =========================================================
    // ELENCHI
    // =========================================================

    @Test
    void findAll_shouldReturnLots() {

        InventoryLot lot = createLotFixture();

        when(lotRepository.findByTenantIdAndItemIdOrderByLotCodeAsc(
                TENANT_ID,
                ITEM_ID
        )).thenReturn(List.of(lot));

        List<InventoryLot> result = service.findAll(
                TENANT_ID,
                ITEM_ID
        );

        assertEquals(1, result.size());
        assertSame(lot, result.get(0));
    }

    @Test
    void findActive_shouldReturnActiveLots() {

        InventoryLot lot = createLotFixture();

        when(lotRepository
                .findByTenantIdAndItemIdAndActiveTrueOrderByLotCodeAsc(
                        TENANT_ID,
                        ITEM_ID
                )).thenReturn(List.of(lot));

        List<InventoryLot> result = service.findActive(
                TENANT_ID,
                ITEM_ID
        );

        assertEquals(1, result.size());
        assertSame(lot, result.get(0));
    }

    @Test
    void findAll_shouldRejectNullItemId() {

        assertThrows(BusinessException.class, () ->
                service.findAll(
                        TENANT_ID,
                        null
                )
        );

        verifyNoInteractions(lotRepository);
    }
}