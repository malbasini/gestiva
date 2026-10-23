package com.gestiva.purchasing.order.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.supplier.entity.Supplier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.purchasing.supplier.repository.SupplierRepository;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import com.gestiva.security.company.context.CompanyContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderWebServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private PurchaseOrderLineRepository purchaseOrderLineRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private GoodsReceiptRepository goodsReceiptRepository;

    @Mock
    private DocumentSequenceService documentSequenceService;

    @Mock
    private CompanyContext companyContext;

    @InjectMocks
    private PurchaseOrderWebService service;

    private PurchaseOrderLineForm validLine;

    @BeforeEach
    void setUp() {
        validLine = new PurchaseOrderLineForm();
        validLine.setDescription("Prodotto di test");
        validLine.setQuantity("2");
        validLine.setUnitPrice("9,50");
        validLine.setDiscountPct("10");
        validLine.setTaxPct("22");
    }

    private void validate() {
        service.validateLines(List.of(validLine));
    }

    @ParameterizedTest
    @CsvSource({
        "'5.50', '5,5'",
        "'2.25', '2,25'",
        "'0.75', '0,75'",
        "'10.00', '10'"
    })
    void shouldPreserveDiscountPrecisionWhenReopeningOrder(
            String storedDiscount,
            String expectedDiscount
    ) {
        Long tenantId = 2L;
        Long companyId = 5L;
        Long orderId = 100L;

        PurchaseOrder order = new PurchaseOrder();
        order.setSupplierId(10L);
        order.setOrderDate(LocalDate.of(2026, 10, 23));
        order.setStatus("DRAFT");
        order.setCurrencyCode("EUR");

        PurchaseOrderLine line = new PurchaseOrderLine();
        line.setDescription("Prodotto di test");
        line.setQuantity(new BigDecimal("1.000"));
        line.setUnitPrice(new BigDecimal("100.00"));
        line.setDiscountPct(new BigDecimal(storedDiscount));
        line.setTaxPct(new BigDecimal("22.00"));

        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(companyId);

        when(purchaseOrderRepository.findByTenantIdAndCompanyIdAndId(
                tenantId, companyId, orderId
        )).thenReturn(Optional.of(order));

        when(purchaseOrderLineRepository
                .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        tenantId, orderId
                )).thenReturn(List.of(line));

        PurchaseOrderForm form = service.getForm(tenantId, orderId);

        assertEquals(1, form.getLines().size());

        String actualDiscount = form.getLines()
                .get(0)
                .getDiscountPct();

        assertEquals(
                0,
                new BigDecimal(storedDiscount).compareTo(
                        new BigDecimal(
                                actualDiscount.replace(",", ".")
                        )
                )
        );

        assertEquals(expectedDiscount, actualDiscount);
    }

    private void verifyVatCalculation(
            String taxPct,
            String expectedTax,
            String expectedTotal
    ) {
        Long tenantId = 2L;
        Long companyId = 5L;
        Long supplierId = 10L;

        PurchaseOrderForm form = new PurchaseOrderForm();
        form.setSupplierId(supplierId);
        form.setOrderDate(LocalDate.of(2026, 10, 23));
        form.setStatus("DRAFT");
        form.setCurrencyCode("EUR");

        validLine.setQuantity("1");
        validLine.setUnitPrice("1000");
        validLine.setDiscountPct("0");
        validLine.setTaxPct(taxPct);

        form.getLines().add(validLine);

        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(companyId);

        when(supplierRepository.findByTenantIdAndId(
                tenantId, supplierId
        )).thenReturn(Optional.of(new Supplier()));

        when(documentSequenceService.nextNumber(
                eq(tenantId),
                eq(companyId),
                any()
        )).thenReturn("PO-TEST-001");

        when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.create(tenantId, form);

        ArgumentCaptor<PurchaseOrder> orderCaptor =
                ArgumentCaptor.forClass(PurchaseOrder.class);

        verify(purchaseOrderRepository).save(orderCaptor.capture());

        PurchaseOrder savedOrder = orderCaptor.getValue();

        assertEquals(
                0,
                new BigDecimal("1000.00").compareTo(
                        savedOrder.getSubtotalAmount()
                )
        );

        assertEquals(
                0,
                new BigDecimal(expectedTax).compareTo(
                        savedOrder.getTaxAmount()
                )
        );

        assertEquals(
                0,
                new BigDecimal(expectedTotal).compareTo(
                        savedOrder.getTotalAmount()
                )
        );

        ArgumentCaptor<PurchaseOrderLine> lineCaptor =
                ArgumentCaptor.forClass(PurchaseOrderLine.class);

        verify(purchaseOrderLineRepository).save(lineCaptor.capture());

        PurchaseOrderLine savedLine = lineCaptor.getValue();

        assertEquals(
                0,
                new BigDecimal(taxPct.replace(",", ".")).compareTo(
                        savedLine.getTaxPct()
                )
        );

        assertEquals(
                0,
                new BigDecimal(expectedTax).compareTo(
                        savedLine.getTaxAmount()
                )
        );
    }

    @Test
    void shouldCalculateVatAtTenPointFivePercent() {
        verifyVatCalculation("10,50", "105.00", "1105.00");
    }

    @Test
    void shouldCalculateVatAtFourPointTwentyFivePercent() {
        verifyVatCalculation("4,25", "42.50", "1042.50");
    }

    @Test
    void shouldAcceptValidEconomicValues() {
        assertDoesNotThrow(this::validate);
    }

    @Test
    void shouldAcceptDecimalComma() {
        validLine.setQuantity("2,500");
        validLine.setUnitPrice("15,75");
        validLine.setDiscountPct("5,50");

        assertDoesNotThrow(this::validate);
    }

    @Test
    void shouldAcceptThreeDecimalQuantity() {
        validLine.setQuantity("1,123");

        assertDoesNotThrow(this::validate);
    }

    @Test
    void shouldRejectFourDecimalQuantity() {
        validLine.setQuantity("1,1234");

        assertThrows(BusinessException.class, this::validate);
    }

    @Test
    void shouldAcceptTrailingZeroQuantity() {
        validLine.setQuantity("1,2300");

        assertDoesNotThrow(this::validate);
    }

    @Test
    void shouldRejectZeroQuantity() {
        validLine.setQuantity("0");

        assertThrows(BusinessException.class, this::validate);
    }

    @Test
    void shouldRejectNegativeQuantity() {
        validLine.setQuantity("-2");

        assertThrows(BusinessException.class, this::validate);
    }

    @Test
    void shouldRejectEmptyQuantity() {
        validLine.setQuantity("");

        assertThrows(BusinessException.class, this::validate);
    }

    @Test
    void shouldRejectNegativeUnitPrice() {
        validLine.setUnitPrice("-9,50");

        assertThrows(BusinessException.class, this::validate);
    }

    @Test
    void shouldRejectNegativeDiscount() {
        validLine.setDiscountPct("-1");

        assertThrows(BusinessException.class, this::validate);
    }

    @Test
    void shouldRejectDiscountAbove100() {
        validLine.setDiscountPct("101");

        assertThrows(BusinessException.class, this::validate);
    }

    @Test
    void shouldAcceptTaxWithOneDecimal() {
        validLine.setTaxPct("10,50");

        assertDoesNotThrow(this::validate);
    }

    @Test
    void shouldAcceptTaxWithTwoDecimals() {
        validLine.setTaxPct("4,25");

        assertDoesNotThrow(this::validate);
    }

    @Test
    void shouldRejectTaxWithMoreThanTwoDecimals() {
        validLine.setTaxPct("10,555");

        assertThrows(BusinessException.class, this::validate);
    }

    @Test
    void shouldRejectNegativeTax() {
        validLine.setTaxPct("-1");

        assertThrows(BusinessException.class, this::validate);
    }

    @Test
    void shouldRejectTaxAbove100() {
        validLine.setTaxPct("101");

        assertThrows(BusinessException.class, this::validate);
    }

    @Test
    void shouldRejectEmptyUnitPrice() {
        validLine.setUnitPrice("");

        assertThrows(BusinessException.class, this::validate);
    }

    @Test
    void shouldRejectEmptyDiscount() {
        validLine.setDiscountPct("");

        assertThrows(BusinessException.class, this::validate);
    }

    @Test
    void shouldRejectEmptyTax() {
        validLine.setTaxPct("");

        assertThrows(BusinessException.class, this::validate);
    }

    @Test
    void shouldRejectInvalidNumericValue() {
        validLine.setUnitPrice("abc");

        assertThrows(BusinessException.class, this::validate);
    }

    @Test
    void shouldRejectMissingDescription() {
        validLine.setDescription("");

        assertThrows(BusinessException.class, this::validate);
    }

    @Test
    void shouldRejectEmptyLines() {
        assertThrows(
            BusinessException.class,
            () -> service.validateLines(List.of())
        );
    }

    @Test
    void shouldRejectNullLines() {
        assertThrows(
            BusinessException.class,
            () -> service.validateLines(null)
        );
    }
}
