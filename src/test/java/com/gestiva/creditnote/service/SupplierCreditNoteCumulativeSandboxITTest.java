package com.gestiva.creditnote.service;

import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.platform.company.entity.Company;
import com.gestiva.platform.company.service.CompanyBootstrapService;
import com.gestiva.purchasing.supplier.entity.Supplier;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import com.gestiva.purchasing.invoice.entity.SupplierInvoiceLine;
import com.gestiva.security.tenant.entity.Tenant;
import com.gestiva.security.company.context.CompanyContext;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnabledIfEnvironmentVariable(
        named = "GESTIVA_V9_SANDBOX_TEST",
        matches = "enabled"
)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.flyway.enabled=false",
                "spring.jpa.hibernate.ddl-auto=validate",
                "spring.sql.init.mode=never",
                "spring.task.scheduling.enabled=false",
                "spring.batch.job.enabled=false"
        }
)
@ActiveProfiles("test")
class SupplierCreditNoteCumulativeSandboxITTest {

    private static final String DATABASE =
            "gestiva_v9_sandbox";

    private static final List<String> TABLES = List.of(
            "tenant",
            "company",
            "company_settings",
            "supplier",
            "purchase_order",
            "goods_receipt",
            "supplier_invoice",
            "supplier_invoice_line",
            "document_sequence",
            "account",
            "credit_note",
            "credit_note_line",
            "credit_note_financial_position",
            "journal_entry",
            "journal_entry_line"
    );

    @Autowired EntityManager em;
    @Autowired JdbcTemplate jdbc;
    @Autowired CompanyBootstrapService bootstrap;
    @Autowired SupplierCreditNoteDraftService drafts;
    @Autowired SupplierCreditNoteRegistrationService registrations;
    @Autowired CreditNoteRepository notes;

    @MockitoBean
    CompanyContext companyContext;

    private Map<String, Long> baseline;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        String user = System.getenv("GESTIVA_V9_USER");
        String password = System.getenv("GESTIVA_V9_PASSWORD");

        if (!"gestiva_v9_runner".equals(user)
                || password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Credenziali sandbox mancanti.");
        }

        registry.add("spring.datasource.url", () ->
                "jdbc:mysql://127.0.0.1:33307/"
                + DATABASE
                + "?sslMode=DISABLED"
                + "&allowPublicKeyRetrieval=true"
        );
        registry.add("spring.datasource.username", () -> user);
        registry.add("spring.datasource.password", () -> password);
        registry.add("spring.flyway.enabled", () -> false);
        registry.add("spring.jpa.hibernate.ddl-auto",
                () -> "validate");
    }

    private void verifySandbox() throws java.io.IOException {
        assertEquals(
                DATABASE,
                jdbc.queryForObject(
                        "SELECT DATABASE()", String.class)
        );

        assertEquals(
                33307,
                jdbc.queryForObject(
                        "SELECT @@port", Integer.class)
        );

        assertEquals(
                "127.0.0.1",
                jdbc.queryForObject(
                        "SELECT @@bind_address", String.class)
        );

        assertEquals(
                "gestiva_v9_runner@127.0.0.1",
                jdbc.queryForObject(
                        "SELECT CURRENT_USER()", String.class)
        );

        assertEquals(
                Path.of(System.getenv("GESTIVA_V9_DATADIR"))
                        .toRealPath().toString(),
                Path.of(jdbc.queryForObject(
                        "SELECT @@datadir", String.class))
                        .toRealPath().toString()
        );
    }

    private Map<String, Long> counts() {
        Map<String, Long> result = new HashMap<>();

        for (String table : TABLES) {
            result.put(
                    table,
                    jdbc.queryForObject(
                            "SELECT COUNT(*) FROM `" + table + "`",
                            Long.class
                    )
            );
        }

        return result;
    }

    private void checkAmount(
            String sql, BigDecimal expected, Object... args) {
        BigDecimal actual = jdbc.queryForObject(
                sql, BigDecimal.class, args);
        assertNotNull(actual);
        assertEquals(
                0,
                expected.compareTo(actual),
                "Importo inatteso: " + sql
        );
    }

    @BeforeTransaction
    void beforeTransaction() throws java.io.IOException {
        verifySandbox();
        baseline = counts();

        for (String table : TABLES) {
            assertEquals(
                    0L, baseline.get(table),
                    "Sandbox non vuota: " + table
            );
        }

        System.out.println("SCN_SANDBOX_BASELINE: OK");
    }

    @Test
    @Transactional
    void shouldRejectExcessiveCumulativeSupplierCreditNotes() throws java.io.IOException {
        verifySandbox();

        LocalDate today = LocalDate.of(2026, 11, 7);

        BigDecimal subtotal = new BigDecimal("100.00");
        BigDecimal tax = new BigDecimal("22.00");
        BigDecimal total = new BigDecimal("122.00");

        Tenant tenant = new Tenant();
        tenant.setName("Gestiva SCN Sandbox");
        tenant.setSlug("gestiva-scn-sandbox-2236");
        tenant.setEmail("scn-sandbox@example.invalid");
        tenant.setStatus("ACTIVE");
        tenant.setDefaultLocale("it");
        tenant.setDefaultCurrency("EUR");
        em.persist(tenant);
        em.flush();

        Long tenantId = tenant.getId();

        Company company = new Company();
        company.setTenantId(tenantId);
        company.setCode("SCN-2236");
        company.setLegalName("Gestiva SCN Sandbox Company");
        company.setCountryCode("IT");
        company.setCurrencyCode("EUR");
        company.setLocaleCode("it");
        company.setActive(true);
        em.persist(company);
        em.flush();

        Long companyId = company.getId();

        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(companyId);

        bootstrap.initializeCompany(tenantId, companyId);

        Supplier supplier = new Supplier();
        supplier.setTenantId(tenantId);
        supplier.setCode("SUP-SCN-2236");
        supplier.setName("Fornitore test note credito");
        supplier.setActive(true);
        em.persist(supplier);
        em.flush();

        PurchaseOrder order = new PurchaseOrder();
        order.setTenantId(tenantId);
        order.setCompanyId(companyId);
        order.setOrderNumber("PO-SCN-2236");
        order.setOrderDate(today);
        order.setSupplierId(supplier.getId());
        order.setStatus("CONFIRMED");
        order.setCurrencyCode("EUR");
        order.setSubtotalAmount(subtotal);
        order.setTaxAmount(tax);
        order.setTotalAmount(total);
        em.persist(order);
        em.flush();

        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setTenantId(tenantId);
        receipt.setCompanyId(companyId);
        receipt.setReceiptNumber("GR-SCN-2236");
        receipt.setReceiptDate(today);
        receipt.setPurchaseOrderId(order.getId());
        receipt.setSupplierId(supplier.getId());
        em.persist(receipt);
        em.flush();

        SupplierInvoice invoice = new SupplierInvoice();
        invoice.setTenantId(tenantId);
        invoice.setCompanyId(companyId);
        invoice.setInvoiceNumber("SI-SCN-2236");
        invoice.setInvoiceDate(today);
        invoice.setSupplierId(supplier.getId());
        invoice.setGoodsReceiptId(receipt.getId());
        invoice.setPurchaseOrderId(order.getId());
        invoice.setStatus("REGISTERED");
        invoice.setCurrencyCode("EUR");
        invoice.setSubtotalAmount(subtotal);
        invoice.setTaxAmount(tax);
        invoice.setTotalAmount(total);
        em.persist(invoice);
        em.flush();

        SupplierInvoiceLine line = new SupplierInvoiceLine();
        line.setTenantId(tenantId);
        line.setSupplierInvoiceId(invoice.getId());
        line.setLineNo(1);
        line.setDescription("Merce rettificabile sandbox");
        line.setQuantity(new BigDecimal("1.000"));
        line.setUnitPrice(subtotal);
        line.setDiscountPct(new BigDecimal("0.00"));
        line.setTaxPct(new BigDecimal("22.00"));
        line.setLineSubtotal(subtotal);
        line.setTaxAmount(tax);
        line.setLineTotal(total);
        line.setUomCode("PZ");
        line.setBaseUomCode("PZ");
        line.setConversionFactor(new BigDecimal("1.000000"));
        em.persist(line);
        em.flush();

        // La fattura originale contiene 10 PZ.
        // Le due note valide rettificano 4 + 6 PZ.
        line.setQuantity(new BigDecimal("10.000"));
        em.flush();

        Long originalLineId = line.getId();
        assertNotNull(originalLineId);

        Long firstId = drafts.createPartialDraft(
                tenantId,
                invoice.getId(),
                java.util.Map.of(
                        originalLineId, new BigDecimal("4.000")),
                "Prima rettifica cumulativa"
        );

        var firstResult = registrations.register(
                tenantId, firstId);

        assertNotNull(firstResult.journalEntryId());
        assertNotNull(firstResult.financialPositionId());

        em.flush();

        assertEquals(
                "REGISTERED",
                notes.findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, firstId)
                        .orElseThrow().getStatus()
        );

        checkAmount(
                "SELECT total_debit FROM journal_entry WHERE id = ?",
                new BigDecimal("48.80"),
                firstResult.journalEntryId()
        );

        checkAmount(
                "SELECT total_credit FROM journal_entry WHERE id = ?",
                new BigDecimal("48.80"),
                firstResult.journalEntryId()
        );

        checkAmount(
                "SELECT original_amount "
                + "FROM credit_note_financial_position WHERE id = ?",
                new BigDecimal("48.80"),
                firstResult.financialPositionId()
        );

        System.out.println("SCN_CUMULATIVE_FIRST_4_PZ: OK");

        Long secondId = drafts.createPartialDraft(
                tenantId,
                invoice.getId(),
                java.util.Map.of(
                        originalLineId, new BigDecimal("6.000")),
                "Seconda rettifica cumulativa"
        );

        var secondResult = registrations.register(
                tenantId, secondId);

        assertNotNull(secondResult.journalEntryId());
        assertNotNull(secondResult.financialPositionId());

        em.flush();

        assertEquals(
                "REGISTERED",
                notes.findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, secondId)
                        .orElseThrow().getStatus()
        );

        checkAmount(
                "SELECT total_debit FROM journal_entry WHERE id = ?",
                new BigDecimal("73.20"),
                secondResult.journalEntryId()
        );

        checkAmount(
                "SELECT total_credit FROM journal_entry WHERE id = ?",
                new BigDecimal("73.20"),
                secondResult.journalEntryId()
        );

        checkAmount(
                "SELECT original_amount "
                + "FROM credit_note_financial_position WHERE id = ?",
                new BigDecimal("73.20"),
                secondResult.financialPositionId()
        );

        System.out.println("SCN_CUMULATIVE_SECOND_6_PZ: OK");

        BigDecimal quantityRegistered = jdbc.queryForObject(
                """
                SELECT SUM(cl.quantity)
                FROM credit_note_line cl
                JOIN credit_note cn
                  ON cn.id = cl.credit_note_id
                 AND cn.tenant_id = cl.tenant_id
                 AND cn.company_id = cl.company_id
                WHERE cn.tenant_id = ?
                  AND cn.company_id = ?
                  AND cn.original_invoice_id = ?
                  AND cn.credit_note_type = 'SUPPLIER'
                  AND cn.status = 'REGISTERED'
                  AND cl.original_invoice_line_id = ?
                """,
                BigDecimal.class,
                tenantId, companyId, invoice.getId(), originalLineId
        );

        assertNotNull(quantityRegistered);
        assertEquals(
                0,
                new BigDecimal("10.000")
                        .compareTo(quantityRegistered)
        );

        checkAmount(
                """
                SELECT SUM(cn.subtotal_amount)
                FROM credit_note cn
                WHERE cn.tenant_id = ?
                  AND cn.company_id = ?
                  AND cn.original_invoice_id = ?
                  AND cn.credit_note_type = 'SUPPLIER'
                  AND cn.status = 'REGISTERED'
                """,
                new BigDecimal("100.00"),
                tenantId, companyId, invoice.getId()
        );

        checkAmount(
                """
                SELECT SUM(cn.tax_amount)
                FROM credit_note cn
                WHERE cn.tenant_id = ?
                  AND cn.company_id = ?
                  AND cn.original_invoice_id = ?
                  AND cn.credit_note_type = 'SUPPLIER'
                  AND cn.status = 'REGISTERED'
                """,
                new BigDecimal("22.00"),
                tenantId, companyId, invoice.getId()
        );

        checkAmount(
                """
                SELECT SUM(cn.total_amount)
                FROM credit_note cn
                WHERE cn.tenant_id = ?
                  AND cn.company_id = ?
                  AND cn.original_invoice_id = ?
                  AND cn.credit_note_type = 'SUPPLIER'
                  AND cn.status = 'REGISTERED'
                """,
                new BigDecimal("122.00"),
                tenantId, companyId, invoice.getId()
        );

        System.out.println("SCN_CUMULATIVE_EXACT_RECONCILIATION: OK");

        Long thirdId = drafts.createPartialDraft(
                tenantId,
                invoice.getId(),
                java.util.Map.of(
                        originalLineId, new BigDecimal("1.000")),
                "Terza rettifica oltre residuo"
        );

        em.flush();

        assertEquals(
                "DRAFT",
                notes.findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, thirdId)
                        .orElseThrow().getStatus()
        );

        Long journalSequenceBefore = jdbc.queryForObject(
                """
                SELECT next_number
                FROM document_sequence
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND document_type = 'JOURNAL_ENTRY'
                """,
                Long.class, tenantId, companyId
        );

        assertNotNull(journalSequenceBefore);

        var rejection = assertThrows(
                com.gestiva.common.exception.BusinessException.class,
                () -> registrations.register(tenantId, thirdId)
        );

        assertNotNull(rejection.getMessage());

        assertEquals(
                "DRAFT",
                jdbc.queryForObject(
                        "SELECT status FROM credit_note WHERE id = ?",
                        String.class, thirdId
                )
        );

        assertEquals(
                2L,
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM journal_entry
                        WHERE tenant_id = ?
                          AND company_id = ?
                          AND reference_type = 'SUPPLIER_CREDIT_NOTE'
                        """,
                        Long.class, tenantId, companyId
                )
        );

        assertEquals(
                2L,
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM credit_note_financial_position
                        WHERE tenant_id = ?
                          AND company_id = ?
                          AND position_type = 'SUPPLIER'
                        """,
                        Long.class, tenantId, companyId
                )
        );

        assertEquals(
                journalSequenceBefore,
                jdbc.queryForObject(
                        """
                        SELECT next_number
                        FROM document_sequence
                        WHERE tenant_id = ?
                          AND company_id = ?
                          AND document_type = 'JOURNAL_ENTRY'
                        """,
                        Long.class, tenantId, companyId
                )
        );

        System.out.println("SCN_CUMULATIVE_EXCESS_REJECTED: OK");
        System.out.println("SCN_CUMULATIVE_REGISTRATION: OK");
    }


    @AfterTransaction
    void afterRollback() throws java.io.IOException {
        verifySandbox();

        assertNotNull(baseline);

        assertEquals(
                baseline,
                counts(),
                "Residui nelle tabelle dopo rollback."
        );

        System.out.println("SCN_REAL_FIXTURE_ROLLBACK: OK");
    }
}
