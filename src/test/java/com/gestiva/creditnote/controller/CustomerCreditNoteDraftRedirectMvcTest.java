
package com.gestiva.creditnote.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.service.CustomerCreditNoteDraftService;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CustomerCreditNoteDraftRedirectMvcTest {

    private static final String BASE =
            "/customer-credit-notes";

    private CustomerCreditNoteDraftService draftService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {

        TenantContext tenantContext =
                mock(TenantContext.class);

        CompanyContext companyContext =
                mock(CompanyContext.class);

        draftService =
                mock(CustomerCreditNoteDraftService.class);

        when(tenantContext.getCurrentTenantId())
                .thenReturn(2L);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(3L);

        CustomerCreditNotePageController controller =
                new CustomerCreditNotePageController(
                        tenantContext,
                        companyContext,
                        draftService
                );

        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void fullDraftSuccessRedirectsToNewCreditNote()
            throws Exception {

        when(draftService.createFullDraft(
                2L, 10L, "Rettifica completa"))
                .thenReturn(50L);

        mvc.perform(
                        post(BASE)
                                .param("customerInvoiceId", "10")
                                .param("reason", "Rettifica completa")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl(
                                "/customer-credit-notes/50"
                        )
                )
                .andExpect(
                        flash().attributeExists("successMessage")
                );

        verify(draftService)
                .createFullDraft(
                        2L, 10L, "Rettifica completa");
    }

    @Test
    void partialDraftSuccessRedirectsToNewCreditNote()
            throws Exception {

        when(draftService.createPartialDraft(
                eq(2L),
                eq(10L),
                eq(Map.of(
                        101L, new BigDecimal("2.000"),
                        309L, new BigDecimal("1.000")
                )),
                eq("Rettifica parziale")
        )).thenReturn(51L);

        mvc.perform(
                        post(BASE + "/partial")
                                .param("customerInvoiceId", "10")
                                .param("reason", "Rettifica parziale")
                                .param("lineIds", "101", "309")
                                .param("quantities", "2.000", "1.000")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl(
                                "/customer-credit-notes/51"
                        )
                )
                .andExpect(
                        flash().attributeExists("successMessage")
                );

        verify(draftService)
                .createPartialDraft(
                        eq(2L),
                        eq(10L),
                        eq(Map.of(
                                101L, new BigDecimal("2.000"),
                                309L, new BigDecimal("1.000")
                        )),
                        eq("Rettifica parziale")
                );
    }

    @Test
    void fullDraftBusinessErrorReturnsToInvoice()
            throws Exception {

        when(draftService.createFullDraft(
                2L, 10L, "Rettifica"))
                .thenThrow(new BusinessException(
                        "Fattura non rettificabile."
                ));

        mvc.perform(
                        post(BASE)
                                .param("customerInvoiceId", "10")
                                .param("reason", "Rettifica")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl("/invoices/10")
                )
                .andExpect(
                        flash().attribute(
                                "errorMessage",
                                "Fattura non rettificabile."
                        )
                );
    }

    @Test
    void partialDraftBusinessErrorReturnsToInvoice()
            throws Exception {

        when(draftService.createPartialDraft(
                eq(2L),
                eq(10L),
                anyMap(),
                eq("Rettifica parziale")
        )).thenThrow(new BusinessException(
                "Quantità non rettificabile."
        ));

        mvc.perform(
                        post(BASE + "/partial")
                                .param("customerInvoiceId", "10")
                                .param("reason", "Rettifica parziale")
                                .param("lineIds", "101")
                                .param("quantities", "2.000")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl("/invoices/10")
                )
                .andExpect(
                        flash().attribute(
                                "errorMessage",
                                "Quantità non rettificabile."
                        )
                );

        verify(draftService, times(1))
                .createPartialDraft(
                        eq(2L),
                        eq(10L),
                        anyMap(),
                        eq("Rettifica parziale")
                );
    }
}
