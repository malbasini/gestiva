package com.gestiva.sales.quote.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.sales.quote.dto.QuoteLineRequest;
import com.gestiva.sales.quote.entity.QuoteLine;
import com.gestiva.sales.quote.mapper.QuoteMapper;
import com.gestiva.sales.quote.repository.QuoteLineRepository;
import com.gestiva.sales.quote.repository.QuoteRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.settings.sequence.repository.DocumentSequenceRepository;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import com.gestiva.warehouse.item.entity.Item;
import com.gestiva.warehouse.item.entity.ItemUom;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.item.repository.ItemUomRepository;
import com.gestiva.warehouse.uom.entity.UnitOfMeasure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteUomSnapshotTest {

    private static final Long TENANT = 2L;
    private static final Long ITEM_ID = 20L;
    private static final Long BASE_UOM_ID = 1L;
    private static final Long BOX_UOM_ID = 2L;

    @Mock private QuoteRepository quoteRepository;
    @Mock private QuoteLineRepository quoteLineRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private DocumentSequenceRepository documentSequenceRepository;
    @Mock private QuoteMapper quoteMapper;
    @Mock private DocumentSequenceService documentSequenceService;
    @Mock private CompanyContext companyContext;
    @Mock private ItemRepository itemRepository;
    @Mock private ItemUomRepository itemUomRepository;

    @InjectMocks
    private QuoteService service;

    private Item item;
    private UnitOfMeasure baseUom;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(
                service, "itemRepository", itemRepository
        );
        ReflectionTestUtils.setField(
                service, "itemUomRepository", itemUomRepository
        );

        item = mock(Item.class);
        baseUom = mock(UnitOfMeasure.class);

        when(itemRepository.findByTenantIdAndId(TENANT, ITEM_ID))
                .thenReturn(Optional.of(item));

        when(item.getBaseUom()).thenReturn(baseUom);
        when(baseUom.isActive()).thenReturn(true);
        when(baseUom.getId()).thenReturn(BASE_UOM_ID);
    }

    private QuoteLineRequest request(Long uomId) {
        QuoteLineRequest request = new QuoteLineRequest();
        request.setItemId(ITEM_ID);
        request.setUomId(uomId);
        return request;
    }

    private QuoteLine line() {
        QuoteLine line = new QuoteLine();
        line.setQuantity(new BigDecimal("2.000"));
        return line;
    }

    private void apply(QuoteLineRequest request, QuoteLine line) {
        ReflectionTestUtils.invokeMethod(
                service, "applyUomSnapshot", TENANT, request, line
        );
    }

    @Test
    void unitaBaseMemorizzaFattoreUno() {
        when(baseUom.getCode()).thenReturn("PZ");
        QuoteLine line = line();

        apply(request(BASE_UOM_ID), line);

        assertEquals(BASE_UOM_ID, line.getUomId());
        assertEquals("PZ", line.getUnitOfMeasure());
        assertEquals("PZ", line.getBaseUomCode());
        assertEquals(0, line.getConversionFactor()
                .compareTo(BigDecimal.ONE));
    }

    @Test
    void boxAbilitatoAllaVenditaMemorizzaFattoreDieci() {
        when(baseUom.getCode()).thenReturn("PZ");
        ItemUom association = mock(ItemUom.class);
        UnitOfMeasure box = mock(UnitOfMeasure.class);

        when(item.getId()).thenReturn(ITEM_ID);
        when(itemUomRepository.findByTenantIdAndItemIdAndUomId(
                TENANT, ITEM_ID, BOX_UOM_ID
        )).thenReturn(Optional.of(association));

        when(association.getUom()).thenReturn(box);
        when(association.isActive()).thenReturn(true);
        when(association.isSalesUom()).thenReturn(true);
        when(association.getConversionFactor())
                .thenReturn(new BigDecimal("10"));

        when(box.isActive()).thenReturn(true);
        when(box.getCode()).thenReturn("BOX");

        QuoteLine line = line();

        apply(request(BOX_UOM_ID), line);

        assertEquals(BOX_UOM_ID, line.getUomId());
        assertEquals("BOX", line.getUnitOfMeasure());
        assertEquals("PZ", line.getBaseUomCode());
        assertEquals(0, line.getConversionFactor()
                .compareTo(new BigDecimal("10")));
        assertEquals(0, line.getQuantity()
                .compareTo(new BigDecimal("2")));
    }

    @Test
    void uomNonAbilitataAllaVenditaVieneRifiutata() {
        ItemUom association = mock(ItemUom.class);

        when(item.getId()).thenReturn(ITEM_ID);
        when(itemUomRepository.findByTenantIdAndItemIdAndUomId(
                TENANT, ITEM_ID, BOX_UOM_ID
        )).thenReturn(Optional.of(association));

        when(association.getUom()).thenReturn(mock(UnitOfMeasure.class));
        when(association.isActive()).thenReturn(true);
        when(association.isSalesUom()).thenReturn(false);

        assertThrows(
                BusinessException.class,
                () -> apply(request(BOX_UOM_ID), line())
        );
    }
}
