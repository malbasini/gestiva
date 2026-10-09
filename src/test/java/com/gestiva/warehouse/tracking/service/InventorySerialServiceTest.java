package com.gestiva.warehouse.tracking.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.item.entity.InventoryTrackingType;
import com.gestiva.warehouse.item.entity.Item;
import com.gestiva.warehouse.item.entity.ItemType;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.tracking.entity.InventorySerial;
import com.gestiva.warehouse.tracking.repository.InventorySerialRepository;
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
class InventorySerialServiceTest {

    private static final Long TENANT_ID = 2L;
    private static final Long ITEM_ID = 10L;
    private static final Long SERIAL_ID = 20L;

    private static final LocalDate MANUFACTURING_DATE =
            LocalDate.of(2026, 1, 10);

    private static final LocalDate EXPIRATION_DATE =
            LocalDate.of(2027, 1, 10);

    @Mock
    private InventorySerialRepository serialRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private InventorySerialService service;

    private Item item;

    @BeforeEach
    void setUp() {

        item = new Item();

        item.setTenantId(TENANT_ID);
        item.setCode("NOTEBOOK-001");
        item.setName("Notebook di prova");

        item.setItemType(ItemType.PRODUCT);
        item.setTrackingType(InventoryTrackingType.SERIAL);

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

        when(serialRepository.save(any(InventorySerial.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private InventorySerial createSerialFixture() {

        InventorySerial serial = new InventorySerial();

        serial.setTenantId(TENANT_ID);
        serial.setItem(item);
        serial.setSerialNumber("SN10001");
        serial.setManufacturingDate(MANUFACTURING_DATE);
        serial.setExpirationDate(EXPIRATION_DATE);
        serial.setActive(true);

        return serial;
    }

    // =========================================================
    // CREAZIONE SERIALE
    // =========================================================

    @Test
    void create_shouldSaveValidSerial() {

        configureItem();
        configureSave();

        InventorySerial result = service.create(
                TENANT_ID,
                ITEM_ID,
                " sn10001 ",
                MANUFACTURING_DATE,
                EXPIRATION_DATE,
                " Prima fornitura "
        );

        assertNotNull(result);

        assertEquals(TENANT_ID, result.getTenantId());
        assertSame(item, result.getItem());

        assertEquals("SN10001", result.getSerialNumber());
        assertEquals(MANUFACTURING_DATE, result.getManufacturingDate());
        assertEquals(EXPIRATION_DATE, result.getExpirationDate());

        assertEquals("Prima fornitura", result.getNotes());
        assertTrue(result.isActive());

        verify(serialRepository).save(any(InventorySerial.class));
    }

    @Test
    void create_shouldAllowNullExpirationWhenNotRequired() {

        configureItem();
        configureSave();

        InventorySerial result = service.create(
                TENANT_ID,
                ITEM_ID,
                "SN10001",
                MANUFACTURING_DATE,
                null,
                null
        );

        assertNull(result.getExpirationDate());
    }

    @Test
    void create_shouldAllowNullManufacturingDate() {

        configureItem();
        configureSave();

        InventorySerial result = service.create(
                TENANT_ID,
                ITEM_ID,
                "SN10001",
                null,
                EXPIRATION_DATE,
                null
        );

        assertNull(result.getManufacturingDate());
    }

    @Test
    void create_shouldNormalizeBlankNotesToNull() {

        configureItem();
        configureSave();

        InventorySerial result = service.create(
                TENANT_ID,
                ITEM_ID,
                "SN10001",
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
                        "SN10001",
                        null,
                        null,
                        null
                )
        );

        verifyNoInteractions(itemRepository, serialRepository);
    }

    @Test
    void create_shouldRejectNullItemId() {

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        null,
                        "SN10001",
                        null,
                        null,
                        null
                )
        );

        verifyNoInteractions(itemRepository, serialRepository);
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
                        "SN10001",
                        null,
                        null,
                        null
                )
        );

        verify(serialRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectInactiveItem() {

        item.setActive(false);
        configureItem();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        ITEM_ID,
                        "SN10001",
                        null,
                        null,
                        null
                )
        );

        verify(serialRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectServiceItem() {

        item.setItemType(ItemType.SERVICE);
        configureItem();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        ITEM_ID,
                        "SN10001",
                        null,
                        null,
                        null
                )
        );

        verify(serialRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectTrackingNone() {

        item.setTrackingType(InventoryTrackingType.NONE);
        configureItem();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        ITEM_ID,
                        "SN10001",
                        null,
                        null,
                        null
                )
        );

        verify(serialRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectTrackingLot() {

        item.setTrackingType(InventoryTrackingType.LOT);
        configureItem();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        ITEM_ID,
                        "SN10001",
                        null,
                        null,
                        null
                )
        );

        verify(serialRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectBlankSerialNumber() {

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

        verify(serialRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectNullSerialNumber() {

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

        verify(serialRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectSerialNumberLongerThan150() {

        configureItem();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        ITEM_ID,
                        "A".repeat(151),
                        null,
                        null,
                        null
                )
        );

        verify(serialRepository, never()).save(any());
    }

    @Test
    void create_shouldAcceptSerialNumberOf150Characters() {

        configureItem();
        configureSave();

        String serialNumber = "A".repeat(150);

        InventorySerial result = service.create(
                TENANT_ID,
                ITEM_ID,
                serialNumber,
                null,
                null,
                null
        );

        assertEquals(150, result.getSerialNumber().length());
    }

    @Test
    void create_shouldRejectExpirationBeforeManufacturing() {

        configureItem();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        ITEM_ID,
                        "SN10001",
                        EXPIRATION_DATE,
                        MANUFACTURING_DATE,
                        null
                )
        );

        verify(serialRepository, never()).save(any());
    }

    @Test
    void create_shouldRequireExpirationWhenTrackingEnabled() {

        item.setTrackExpiration(true);
        configureItem();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        ITEM_ID,
                        "SN10001",
                        MANUFACTURING_DATE,
                        null,
                        null
                )
        );

        verify(serialRepository, never()).save(any());
    }

    @Test
    void create_shouldAllowExpirationWhenTrackingEnabled() {

        item.setTrackExpiration(true);

        configureItem();
        configureSave();

        InventorySerial result = service.create(
                TENANT_ID,
                ITEM_ID,
                "SN10001",
                MANUFACTURING_DATE,
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
                        "SN10001",
                        null,
                        null,
                        "A".repeat(501)
                )
        );

        verify(serialRepository, never()).save(any());
    }

    @Test
    void create_shouldAcceptNotesOf500Characters() {

        configureItem();
        configureSave();

        InventorySerial result = service.create(
                TENANT_ID,
                ITEM_ID,
                "SN10001",
                null,
                null,
                "A".repeat(500)
        );

        assertEquals(500, result.getNotes().length());
    }

    @Test
    void create_shouldRejectDuplicateSerialNumber() {

        configureItem();

        when(serialRepository.existsByTenantIdAndItemIdAndSerialNumber(
                TENANT_ID,
                ITEM_ID,
                "SN10001"
        )).thenReturn(true);

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID,
                        ITEM_ID,
                        "sn10001",
                        null,
                        null,
                        null
                )
        );

        verify(serialRepository, never()).save(any());
    }

    // =========================================================
    // RECUPERO SERIALE
    // =========================================================

    @Test
    void getById_shouldReturnSerial() {

        InventorySerial serial = createSerialFixture();

        when(serialRepository.findByTenantIdAndItemIdAndId(
                TENANT_ID,
                ITEM_ID,
                SERIAL_ID
        )).thenReturn(Optional.of(serial));

        InventorySerial result = service.getById(
                TENANT_ID,
                ITEM_ID,
                SERIAL_ID
        );

        assertSame(serial, result);
    }

    @Test
    void getById_shouldRejectMissingSerial() {

        when(serialRepository.findByTenantIdAndItemIdAndId(
                TENANT_ID,
                ITEM_ID,
                SERIAL_ID
        )).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                service.getById(
                        TENANT_ID,
                        ITEM_ID,
                        SERIAL_ID
                )
        );
    }

    @Test
    void getById_shouldRejectNullSerialId() {

        assertThrows(BusinessException.class, () ->
                service.getById(
                        TENANT_ID,
                        ITEM_ID,
                        null
                )
        );

        verifyNoInteractions(serialRepository);
    }

    @Test
    void getById_shouldRejectNullTenant() {

        assertThrows(BusinessException.class, () ->
                service.getById(
                        null,
                        ITEM_ID,
                        SERIAL_ID
                )
        );

        verifyNoInteractions(serialRepository);
    }

    @Test
    void getById_shouldRejectNullItemId() {

        assertThrows(BusinessException.class, () ->
                service.getById(
                        TENANT_ID,
                        null,
                        SERIAL_ID
                )
        );

        verifyNoInteractions(serialRepository);
    }

    // =========================================================
    // RICERCA PER NUMERO DI SERIE
    // =========================================================

    @Test
    void getBySerialNumber_shouldNormalizeAndReturnSerial() {

        InventorySerial serial = createSerialFixture();

        when(serialRepository.findByTenantIdAndItemIdAndSerialNumber(
                TENANT_ID,
                ITEM_ID,
                "SN10001"
        )).thenReturn(Optional.of(serial));

        InventorySerial result = service.getBySerialNumber(
                TENANT_ID,
                ITEM_ID,
                " sn10001 "
        );

        assertSame(serial, result);
    }

    @Test
    void getBySerialNumber_shouldRejectMissingSerial() {

        when(serialRepository.findByTenantIdAndItemIdAndSerialNumber(
                TENANT_ID,
                ITEM_ID,
                "SN99999"
        )).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                service.getBySerialNumber(
                        TENANT_ID,
                        ITEM_ID,
                        "SN99999"
                )
        );
    }

    @Test
    void getBySerialNumber_shouldRejectBlankNumber() {

        assertThrows(BusinessException.class, () ->
                service.getBySerialNumber(
                        TENANT_ID,
                        ITEM_ID,
                        " "
                )
        );

        verifyNoInteractions(serialRepository);
    }

    // =========================================================
    // ELENCHI
    // =========================================================

    @Test
    void findAll_shouldReturnSerials() {

        InventorySerial serial = createSerialFixture();

        when(serialRepository.findByTenantIdAndItemIdOrderBySerialNumberAsc(
                TENANT_ID,
                ITEM_ID
        )).thenReturn(List.of(serial));

        List<InventorySerial> result = service.findAll(
                TENANT_ID,
                ITEM_ID
        );

        assertEquals(1, result.size());
        assertSame(serial, result.get(0));
    }

    @Test
    void findActive_shouldReturnActiveSerials() {

        InventorySerial serial = createSerialFixture();

        when(serialRepository
                .findByTenantIdAndItemIdAndActiveTrueOrderBySerialNumberAsc(
                        TENANT_ID,
                        ITEM_ID
                )).thenReturn(List.of(serial));

        List<InventorySerial> result = service.findActive(
                TENANT_ID,
                ITEM_ID
        );

        assertEquals(1, result.size());
        assertSame(serial, result.get(0));
    }

    @Test
    void findAll_shouldRejectNullItemId() {

        assertThrows(BusinessException.class, () ->
                service.findAll(
                        TENANT_ID,
                        null
                )
        );

        verifyNoInteractions(serialRepository);
    }

    @Test
    void findActive_shouldRejectNullTenant() {

        assertThrows(BusinessException.class, () ->
                service.findActive(
                        null,
                        ITEM_ID
                )
        );

        verifyNoInteractions(serialRepository);
    }
}