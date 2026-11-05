package com.gestiva.purchasing.receipt.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.receipt.service.GoodsReceiptPartialApplicationService;
import com.gestiva.security.usercontext.TenantContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoodsReceiptPartialPageControllerTest {

    private static final Long TENANT_ID = 2L;
    private static final Long ORDER_ID = 100L;

    @Mock
    GoodsReceiptPartialApplicationService service;

    @Mock
    TenantContext tenantContext;

    private GoodsReceiptPartialPageController controller() {
        return new GoodsReceiptPartialPageController(
                service, tenantContext
        );
    }

    private MultiValueMap<String, String> parameters(
            String warehouseId,
            String quantity) {

        MultiValueMap<String, String> params =
                new LinkedMultiValueMap<>();

        params.add("warehouseId", warehouseId);
        params.add("quantity_200", quantity);

        return params;
    }

    @Test
    void validRequestDelegatesQuantitiesAndRedirectsToReceipt() {
        var params = parameters("50", "8.000");
        params.add("quantity_201", "7,500");

        when(tenantContext.getCurrentTenantId())
                .thenReturn(TENANT_ID);

        when(service.create(
                eq(TENANT_ID),
                eq(ORDER_ID),
                eq(50L),
                anyMap()
        )).thenReturn(301L);

        var redirect = new RedirectAttributesModelMap();

        String result = controller().receivePartial(
                ORDER_ID, params, redirect
        );

        assertEquals(
                "redirect:/goods-receipts/301",
                result
        );

        verify(service).create(
                TENANT_ID,
                ORDER_ID,
                50L,
                Map.of(
                        200L, new BigDecimal("8.000"),
                        201L, new BigDecimal("7.500")
                )
        );

        assertEquals(
                "Ricevimento parziale creato con successo.",
                redirect.getFlashAttributes().get("successMessage")
        );

        assertFalse(
                redirect.getFlashAttributes().containsKey("errorMessage")
        );
    }

    @Test
    void duplicatedWarehouseParameterIsRejected() {
        var params = parameters("50", "8");
        params.add("warehouseId", "51");

        var redirect = new RedirectAttributesModelMap();

        String result = controller().receivePartial(
                ORDER_ID, params, redirect
        );

        assertEquals(
                "redirect:/purchase-orders/100",
                result
        );

        assertTrue(
                redirect.getFlashAttributes().containsKey("errorMessage")
        );

        verifyNoInteractions(tenantContext, service);
    }

    @Test
    void negativeQuantityIsRejected() {
        var redirect = new RedirectAttributesModelMap();

        String result = controller().receivePartial(
                ORDER_ID,
                parameters("50", "-1"),
                redirect
        );

        assertEquals(
                "redirect:/purchase-orders/100",
                result
        );

        assertTrue(
                redirect.getFlashAttributes().containsKey("errorMessage")
        );

        verifyNoInteractions(tenantContext, service);
    }

    @Test
    void nonNumericQuantityIsRejected() {
        var redirect = new RedirectAttributesModelMap();

        String result = controller().receivePartial(
                ORDER_ID,
                parameters("50", "non-valido"),
                redirect
        );

        assertEquals(
                "redirect:/purchase-orders/100",
                result
        );

        assertTrue(
                redirect.getFlashAttributes().containsKey("errorMessage")
        );

        verifyNoInteractions(tenantContext, service);
    }

    @Test
    void zeroQuantitiesAreRejected() {
        var params = parameters("50", "0.000");
        params.add("quantity_201", "0");

        var redirect = new RedirectAttributesModelMap();

        String result = controller().receivePartial(
                ORDER_ID, params, redirect
        );

        assertEquals(
                "redirect:/purchase-orders/100",
                result
        );

        assertTrue(
                redirect.getFlashAttributes().containsKey("errorMessage")
        );

        verifyNoInteractions(tenantContext, service);
    }

    @Test
    void applicationErrorRedirectsBackWithoutSuccess() {
        when(tenantContext.getCurrentTenantId())
                .thenReturn(TENANT_ID);

        when(service.create(
                eq(TENANT_ID),
                eq(ORDER_ID),
                eq(50L),
                anyMap()
        )).thenThrow(
                new BusinessException(
                        "Ricevimenti parziali temporaneamente non disponibili."
                )
        );

        var redirect = new RedirectAttributesModelMap();

        String result = controller().receivePartial(
                ORDER_ID,
                parameters("50", "8"),
                redirect
        );

        assertEquals(
                "redirect:/purchase-orders/100",
                result
        );

        assertEquals(
                "Ricevimenti parziali temporaneamente non disponibili.",
                redirect.getFlashAttributes().get("errorMessage")
        );

        assertFalse(
                redirect.getFlashAttributes().containsKey("successMessage")
        );

        verify(service).create(
                TENANT_ID,
                ORDER_ID,
                50L,
                Map.of(200L, new BigDecimal("8"))
        );
    }
}
