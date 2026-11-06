package com.gestiva.purchasing.invoice.service;

import com.gestiva.purchasing.invoice.controller.SupplierInvoicePartialPageController;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SupplierInvoicePartialHttpFeatureGateTest {

    private static final String PATH =
            "/goods-receipts/302/invoice-partial";

    private static final String GENERAL_FLAG =
            "gestiva.purchasing.supplier-invoice.enabled";

    private static final String PARTIAL_FLAG =
            "gestiva.purchasing.supplier-invoice.partial.enabled";

    private MockEnvironment environment;
    private CompanyContext companyContext;
    private TenantContext tenantContext;
    private SupplierInvoicePartialDraftCreationService creation;
    private MockMvc mvc;

    @BeforeEach
    void setup() {

        environment = new MockEnvironment();

        companyContext = mock(CompanyContext.class);
        tenantContext = mock(TenantContext.class);

        creation = mock(
                SupplierInvoicePartialDraftCreationService.class
        );

        SupplierInvoicePartialGenerationGuard guard =
                new SupplierInvoicePartialGenerationGuard(
                        environment
                );

        SupplierInvoicePartialApplicationService application =
                new SupplierInvoicePartialApplicationService(
                        guard,
                        companyContext,
                        creation
                );

        SupplierInvoicePartialPageController controller =
                new SupplierInvoicePartialPageController(
                        application,
                        tenantContext
                );

        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        when(tenantContext.getCurrentTenantId())
                .thenReturn(2L);
    }

    private void expectBlocked() throws Exception {

        mvc.perform(post(PATH))
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

        verify(tenantContext, times(1))
                .getCurrentTenantId();

        verifyNoInteractions(
                companyContext,
                creation
        );
    }

    @Test
    void productionProfileBlocksEvenWithBothFlagsTrue()
            throws Exception {

        environment.setActiveProfiles("prod");
        environment.setProperty(GENERAL_FLAG, "true");
        environment.setProperty(PARTIAL_FLAG, "true");

        expectBlocked();
    }

    @Test
    void testProfileWithMissingFlagsBlocks()
            throws Exception {

        environment.setActiveProfiles("test");

        expectBlocked();
    }

    @Test
    void generalFlagEnabledButPartialMissingBlocks()
            throws Exception {

        environment.setActiveProfiles("test");
        environment.setProperty(GENERAL_FLAG, "true");

        expectBlocked();
    }

    @Test
    void partialFlagEnabledButGeneralDisabledBlocks()
            throws Exception {

        environment.setActiveProfiles("test");
        environment.setProperty(GENERAL_FLAG, "false");
        environment.setProperty(PARTIAL_FLAG, "true");

        expectBlocked();
    }

    @Test
    void explicitPartialFlagFalseBlocks()
            throws Exception {

        environment.setActiveProfiles("test");
        environment.setProperty(GENERAL_FLAG, "true");
        environment.setProperty(PARTIAL_FLAG, "false");

        expectBlocked();
    }

    @Test
    void testProfileWithBothFlagsDelegates()
            throws Exception {

        environment.setActiveProfiles("test");
        environment.setProperty(GENERAL_FLAG, "true");
        environment.setProperty(PARTIAL_FLAG, "true");

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(1L);

        when(creation.create(2L, 302L))
                .thenReturn(501L);

        mvc.perform(post(PATH))
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

        verify(companyContext, times(1))
                .requireCurrentCompanyId();

        verify(creation, times(1))
                .create(2L, 302L);

        verifyNoMoreInteractions(creation);
    }
}
