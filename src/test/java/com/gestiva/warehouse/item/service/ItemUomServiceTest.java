package com.gestiva.warehouse.item.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.inventory.item.repository.ItemRepository;
import com.gestiva.warehouse.item.entity.Item;
import com.gestiva.warehouse.item.entity.ItemType;
import com.gestiva.warehouse.item.entity.ItemUom;
import com.gestiva.warehouse.item.repository.ItemUomRepository;
import com.gestiva.warehouse.uom.entity.UnitOfMeasure;
import com.gestiva.warehouse.uom.entity.UnitOfMeasureDimension;
import com.gestiva.warehouse.uom.repository.UnitOfMeasureRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemUomServiceTest {

    private static final Long TENANT_ID = 2L;
    private static final Long ITEM_ID = 10L;
    private static final Long BASE_UOM_ID = 1L;
    private static final Long ALTERNATIVE_UOM_ID = 2L;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemUomRepository itemUomRepository;

    @Mock
    private UnitOfMeasureRepository unitOfMeasureRepository;

    @InjectMocks
    private ItemUomService service;

    private Item item;
    private UnitOfMeasure baseUom;
    private UnitOfMeasure alternativeUom;

    @BeforeEach
    void setUp() {

        baseUom = mock(UnitOfMeasure.class);
        lenient().when(baseUom.getId()).thenReturn(BASE_UOM_ID);
        lenient().when(baseUom.getCode()).thenReturn("PZ");
        lenient().when(baseUom.getDimension())
                .thenReturn(UnitOfMeasureDimension.UNIT);
        lenient().when(baseUom.getDecimalPlaces()).thenReturn(0);
        lenient().when(baseUom.isActive()).thenReturn(true);

        alternativeUom = mock(UnitOfMeasure.class);
        lenient().when(alternativeUom.getId())
                .thenReturn(ALTERNATIVE_UOM_ID);
        lenient().when(alternativeUom.getCode()).thenReturn("SC");
        lenient().when(alternativeUom.getDimension())
                .thenReturn(UnitOfMeasureDimension.UNIT);
        lenient().when(alternativeUom.getDecimalPlaces()).thenReturn(2);
        lenient().when(alternativeUom.isActive()).thenReturn(true);

        item = mock(Item.class);
        lenient().when(item.getBaseUom()).thenReturn(baseUom);
        lenient().when(item.isActive()).thenReturn(true);
        lenient().when(item.getItemType()).thenReturn(ItemType.PRODUCT);

        lenient().when(itemRepository.findByTenantIdAndId(
                TENANT_ID, ITEM_ID
        )).thenReturn(Optional.of(item));

        lenient().when(unitOfMeasureRepository.findByTenantIdAndId(
                TENANT_ID, BASE_UOM_ID
        )).thenReturn(Optional.of(baseUom));

        lenient().when(unitOfMeasureRepository.findByTenantIdAndId(
                TENANT_ID, ALTERNATIVE_UOM_ID
        )).thenReturn(Optional.of(alternativeUom));
    }

    private void configureConversion(
            String factor,
            boolean active) {

        ItemUom conversion = new ItemUom();
        conversion.setTenantId(TENANT_ID);
        conversion.setItem(item);
        conversion.setUom(alternativeUom);
        conversion.setConversionFactor(new BigDecimal(factor));
        conversion.setActive(active);

        when(itemUomRepository.findByTenantIdAndItemIdAndUomId(
                TENANT_ID, ITEM_ID, ALTERNATIVE_UOM_ID
        )).thenReturn(Optional.of(conversion));
    }

    // =========================================================
    // CONVERSIONI
    // =========================================================

    @Test
    void convertToBase_shouldConvertThreeBoxesInto300Pieces() {

        configureConversion("100", true);

        BigDecimal result = service.convertToBase(
                TENANT_ID,
                ITEM_ID,
                ALTERNATIVE_UOM_ID,
                new BigDecimal("3")
        );

        assertEquals(
                0,
                new BigDecimal("300").compareTo(result)
        );
    }

    @Test
    void convertFromBase_shouldConvert300PiecesIntoThreeBoxes() {

        configureConversion("100", true);

        BigDecimal result = service.convertFromBase(
                TENANT_ID,
                ITEM_ID,
                ALTERNATIVE_UOM_ID,
                new BigDecimal("300")
        );

        assertEquals(
                0,
                new BigDecimal("3").compareTo(result)
        );
    }

    @Test
    void convertToBase_shouldReturnSameQuantityForBaseUom() {

        BigDecimal result = service.convertToBase(
                TENANT_ID,
                ITEM_ID,
                BASE_UOM_ID,
                new BigDecimal("25")
        );

        assertEquals(
                0,
                new BigDecimal("25").compareTo(result)
        );
    }

    @Test
    void convertFromBase_shouldReturnSameQuantityForBaseUom() {

        BigDecimal result = service.convertFromBase(
                TENANT_ID,
                ITEM_ID,
                BASE_UOM_ID,
                new BigDecimal("25")
        );

        assertEquals(
                0,
                new BigDecimal("25").compareTo(result)
        );
    }

    @Test
    void convertToBase_shouldRejectFractionalPieces() {

        configureConversion("3", true);

        assertThrows(
                BusinessException.class,
                () -> service.convertToBase(
                        TENANT_ID,
                        ITEM_ID,
                        ALTERNATIVE_UOM_ID,
                        new BigDecimal("0.50")
                )
        );
    }
    @Test
    void convertFromBase_shouldRejectNonExactConversion() {

        configureConversion("3", true);

        assertThrows(
                BusinessException.class,
                () -> service.convertFromBase(
                        TENANT_ID,
                        ITEM_ID,
                        ALTERNATIVE_UOM_ID,
                        new BigDecimal("1")
                )
        );
    }

    @Test
    void convertToBase_shouldRejectMissingConversion() {

        when(itemUomRepository.findByTenantIdAndItemIdAndUomId(
                TENANT_ID, ITEM_ID, ALTERNATIVE_UOM_ID
        )).thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> service.convertToBase(
                        TENANT_ID,
                        ITEM_ID,
                        ALTERNATIVE_UOM_ID,
                        new BigDecimal("3")
                )
        );
    }

    @Test
    void convertToBase_shouldRejectInactiveConversion() {

        configureConversion("100", false);

        assertThrows(
                BusinessException.class,
                () -> service.convertToBase(
                        TENANT_ID,
                        ITEM_ID,
                        ALTERNATIVE_UOM_ID,
                        new BigDecimal("3")
                )
        );
    }

    @Test
    void convertToBase_shouldRejectNegativeQuantity() {

        assertThrows(
                BusinessException.class,
                () -> service.convertToBase(
                        TENANT_ID,
                        ITEM_ID,
                        ALTERNATIVE_UOM_ID,
                        new BigDecimal("-1")
                )
        );
    }

    // =========================================================
    // CREAZIONE CONVERSIONI
    // =========================================================

    @Test
    void create_shouldSaveValidConversion() {

        when(itemUomRepository.existsByTenantIdAndItemIdAndUomId(
                TENANT_ID, ITEM_ID, ALTERNATIVE_UOM_ID
        )).thenReturn(false);

        when(itemUomRepository.save(any(ItemUom.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ItemUom result = service.create(
                TENANT_ID,
                ITEM_ID,
                ALTERNATIVE_UOM_ID,
                new BigDecimal("100"),
                true,
                false,
                " 123456789 "
        );

        assertNotNull(result);

        assertEquals(TENANT_ID, result.getTenantId());
        assertSame(item, result.getItem());
        assertSame(alternativeUom, result.getUom());

        assertEquals(
                0,
                new BigDecimal("100")
                        .compareTo(result.getConversionFactor())
        );

        assertTrue(result.isPurchaseUom());
        assertFalse(result.isSalesUom());
        assertTrue(result.isActive());
        assertEquals("123456789", result.getBarcode());
    }

    @Test
    void create_shouldRejectZeroConversionFactor() {

        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT_ID,
                        ITEM_ID,
                        ALTERNATIVE_UOM_ID,
                        BigDecimal.ZERO,
                        true,
                        false,
                        null
                )
        );

        verify(itemUomRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectNegativeConversionFactor() {

        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT_ID,
                        ITEM_ID,
                        ALTERNATIVE_UOM_ID,
                        new BigDecimal("-5"),
                        true,
                        false,
                        null
                )
        );

        verify(itemUomRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectExcessiveFactorDecimals() {

        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT_ID,
                        ITEM_ID,
                        ALTERNATIVE_UOM_ID,
                        new BigDecimal("1.1234567"),
                        true,
                        false,
                        null
                )
        );

        verify(itemUomRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectDifferentDimensions() {

        when(alternativeUom.getDimension())
                .thenReturn(UnitOfMeasureDimension.MASS);

        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT_ID,
                        ITEM_ID,
                        ALTERNATIVE_UOM_ID,
                        new BigDecimal("100"),
                        true,
                        false,
                        null
                )
        );

        verify(itemUomRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectDuplicateConversion() {

        when(itemUomRepository.existsByTenantIdAndItemIdAndUomId(
                TENANT_ID, ITEM_ID, ALTERNATIVE_UOM_ID
        )).thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT_ID,
                        ITEM_ID,
                        ALTERNATIVE_UOM_ID,
                        new BigDecimal("100"),
                        true,
                        false,
                        null
                )
        );

        verify(itemUomRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectInactiveItem() {

        when(item.isActive()).thenReturn(false);

        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT_ID,
                        ITEM_ID,
                        ALTERNATIVE_UOM_ID,
                        new BigDecimal("100"),
                        true,
                        false,
                        null
                )
        );

        verify(itemUomRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectItemFromAnotherTenant() {

        when(itemRepository.findByTenantIdAndId(
                TENANT_ID, ITEM_ID
        )).thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT_ID,
                        ITEM_ID,
                        ALTERNATIVE_UOM_ID,
                        new BigDecimal("100"),
                        true,
                        false,
                        null
                )
        );

        verify(itemUomRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectBaseUomAsAlternative() {

        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT_ID,
                        ITEM_ID,
                        BASE_UOM_ID,
                        BigDecimal.ONE,
                        true,
                        true,
                        null
                )
        );

        verify(itemUomRepository, never()).save(any());
    }
}