package com.gestiva.purchasing.receipt.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.receipt.service.GoodsReceiptPartialApplicationService;
import com.gestiva.security.usercontext.TenantContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GoodsReceiptPartialMvcBindingTest {

    private GoodsReceiptPartialApplicationService service;
    private TenantContext tenantContext;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(GoodsReceiptPartialApplicationService.class);
        tenantContext = mock(TenantContext.class);

        GoodsReceiptPartialPageController controller =
                new GoodsReceiptPartialPageController(
                        service, tenantContext
                );

        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void validPostBindsParametersAndRedirectsToReceipt()
            throws Exception {

        when(tenantContext.getCurrentTenantId())
                .thenReturn(2L);

        when(service.create(
                eq(2L),
                eq(100L),
                eq(50L),
                anyMap()
        )).thenReturn(301L);

        mvc.perform(post("/purchase-orders/100/receive-partial")
                        .param("warehouseId", "50")
                        .param("quantity_200", "8.000")
                        .param("quantity_201", "7,500"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/goods-receipts/301"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "Ricevimento parziale creato con successo."
                ));

        verify(service).create(
                2L,
                100L,
                50L,
                Map.of(
                        200L, new BigDecimal("8.000"),
                        201L, new BigDecimal("7.500")
                )
        );

        verifyNoMoreInteractions(service);
    }

    @Test
    void invalidQuantityRedirectsWithoutCallingService()
            throws Exception {

        mvc.perform(post("/purchase-orders/100/receive-partial")
                        .param("warehouseId", "50")
                        .param("quantity_200", "-4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/purchase-orders/100"))
                .andExpect(flash().attributeExists("errorMessage"));

        verifyNoInteractions(service, tenantContext);
    }

    @Test
    void applicationRejectionRedirectsWithError()
            throws Exception {

        when(tenantContext.getCurrentTenantId())
                .thenReturn(2L);

        when(service.create(
                eq(2L),
                eq(100L),
                eq(50L),
                anyMap()
        )).thenThrow(new BusinessException(
                "Ricevimenti parziali temporaneamente non disponibili."
        ));

        mvc.perform(post("/purchase-orders/100/receive-partial")
                        .param("warehouseId", "50")
                        .param("quantity_200", "8"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/purchase-orders/100"))
                .andExpect(flash().attribute(
                        "errorMessage",
                        "Ricevimenti parziali temporaneamente non disponibili."
                ));

        verify(service).create(
                2L,
                100L,
                50L,
                Map.of(200L, new BigDecimal("8"))
        );
    }
}
