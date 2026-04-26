package com.gestiva.sales.quote.controller;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.platform.tenant.entity.Tenant;
import com.gestiva.sales.quote.dto.QuoteCreateRequest;
import com.gestiva.sales.quote.dto.QuoteLineRequest;
import com.gestiva.sales.quote.dto.QuoteResponse;
import com.gestiva.sales.quote.service.QuoteService;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.support.AbstractMySqlIntegrationTest;
import com.gestiva.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

    @AutoConfigureMockMvc
    class QuotePreviewControllerTest extends AbstractMySqlIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private QuoteService quoteService;

        @Autowired
        private TestDataFactory testDataFactory;

        private Long tenantId;
        private Long quoteId;

        @BeforeEach
        void setUp() {
            Tenant tenant = testDataFactory.createTenant();
            Customer customer = testDataFactory.createCustomer(tenant.getId(), "Rossi Srl");
            this.tenantId = tenant.getId();
            Long customerId = customer.getId();

            QuoteCreateRequest request = new QuoteCreateRequest();
            request.setCustomerId(customerId);
            request.setQuoteDate(LocalDate.of(2026, 4, 26));
            request.setValidUntil(LocalDate.of(2026, 5, 26));
            request.setCurrencyCode("EUR");
            request.setNotes("Preview HTML test");

            QuoteLineRequest line = new QuoteLineRequest();
            line.setDescription("Modulo CRM");
            line.setQuantity(new BigDecimal("1.000"));
            line.setUnitPrice(new BigDecimal("1000.00"));
            line.setDiscountPct(new BigDecimal("10.00"));
            line.setTaxPct(new BigDecimal("22.00"));

            request.setLines(List.of(line));

            QuoteResponse quote = quoteService.create(tenantId, request);
            this.quoteId = quote.getId();
        }

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        void preview_shouldReturnHtmlPage() throws Exception {
            mockMvc.perform(get("/quotes/{id}/preview", quoteId)
                            .header("X-Tenant-Id", tenantId))
                    .andExpect(status().isOk())
                    .andExpect(view().name("quote/quote-preview"))
                    .andExpect(model().attributeExists("quote"))
                    .andExpect(model().attributeExists("previewMode"));
        }
    }