package com.gestiva.purchasing.invoice.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.service.SupplierInvoicePartialApplicationService;
import com.gestiva.security.usercontext.TenantContext;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SupplierInvoicePartialPageControllerTest {

    private SupplierInvoicePartialApplicationService service;
    private TenantContext tenantContext;
    private MockMvc mvc;

    @BeforeEach
    void setup() {

        service = mock(
                SupplierInvoicePartialApplicationService.class
        );

        tenantContext = mock(TenantContext.class);

        SupplierInvoicePartialPageController controller =
                new SupplierInvoicePartialPageController(
                        service,
                        tenantContext
                );

        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void successfulCreationRedirectsToReceipt()
            throws Exception {

        when(tenantContext.getCurrentTenantId())
                .thenReturn(2L);

        when(service.create(2L, 302L))
                .thenReturn(501L);

        mvc.perform(
                post("/goods-receipts/302/invoice-partial")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl("/goods-receipts/302")
                )
                .andExpect(
                        flash().attribute(
                                "successMessage",
                                "Fattura fornitore parziale DRAFT "
                                + "creata con successo."
                        )
                );

        verify(tenantContext, times(1))
                .getCurrentTenantId();

        verify(service, times(1))
                .create(2L, 302L);

        verifyNoMoreInteractions(service);
    }

    @Test
    void duplicateInvoiceShowsBusinessError()
            throws Exception {

        when(tenantContext.getCurrentTenantId())
                .thenReturn(2L);

        when(service.create(2L, 302L))
                .thenThrow(new BusinessException(
                        "Esiste già una fattura per questa ricezione."
                ));

        mvc.perform(
                post("/goods-receipts/302/invoice-partial")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl("/goods-receipts/302")
                )
                .andExpect(
                        flash().attribute(
                                "errorMessage",
                                "Esiste già una fattura per questa ricezione."
                        )
                );

        verify(service, times(1))
                .create(2L, 302L);
    }

    @Test
    void disabledFeatureShowsBusinessError()
            throws Exception {

        when(tenantContext.getCurrentTenantId())
                .thenReturn(2L);

        when(service.create(2L, 302L))
                .thenThrow(new BusinessException(
                        "Fatturazione parziale non disponibile."
                ));

        mvc.perform(
                post("/goods-receipts/302/invoice-partial")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl("/goods-receipts/302")
                )
                .andExpect(
                        flash().attribute(
                                "errorMessage",
                                "Fatturazione parziale non disponibile."
                        )
                );

        verify(service, times(1))
                .create(2L, 302L);
    }

    @Test
    void invalidReceiptIdIsRejectedByApplicationService()
            throws Exception {

        when(tenantContext.getCurrentTenantId())
                .thenReturn(2L);

        when(service.create(2L, 0L))
                .thenThrow(new BusinessException(
                        "Identificativi fatturazione parziale non validi."
                ));

        mvc.perform(
                post("/goods-receipts/0/invoice-partial")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl("/goods-receipts/0")
                )
                .andExpect(
                        flash().attributeExists("errorMessage")
                );

        verify(service, times(1))
                .create(2L, 0L);
    }

    @Test
    void unexpectedFailureIsNotSilentlyConvertedToSuccess()
            throws Exception {

        when(tenantContext.getCurrentTenantId())
                .thenReturn(2L);

        when(service.create(2L, 302L))
                .thenThrow(new IllegalStateException(
                        "Errore infrastrutturale simulato."
                ));

        assertThrows(
                ServletException.class,
                () -> mvc.perform(
                        post("/goods-receipts/302/invoice-partial")
                )
        );

        verify(service, times(1))
                .create(2L, 302L);
    }
}
