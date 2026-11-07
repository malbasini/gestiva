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
class SupplierCreditNoteSandboxITTest {

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
    void shouldRegisterSupplierCreditNoteAndRollbackFixture() throws java.io.IOException {
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

        Long noteId = drafts.createFullDraft(
                tenantId,
                invoice.getId(),
                "Rettifica integrale fattura sandbox"
        );

        em.flush();
        em.clear();

        CreditNote draft = notes
                .findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, noteId)
                .orElseThrow();

        assertEquals("DRAFT", draft.getStatus());
        assertEquals("SUPPLIER", draft.getCreditNoteType());
        assertEquals("SCN-00001", draft.getCreditNoteNumber());

        System.out.println("SCN_REAL_DRAFT: OK");

        var result = registrations.register(
                tenantId, noteId);

        assertEquals(noteId, result.creditNoteId());
        assertNotNull(result.journalEntryId());
        assertNotNull(result.financialPositionId());

        em.flush();
        em.clear();

        CreditNote registered = notes
                .findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, noteId)
                .orElseThrow();

        assertEquals("REGISTERED", registered.getStatus());

        Integer journalCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM journal_entry "
                + "WHERE id = ? AND tenant_id = ? "
                + "AND company_id = ? "
                + "AND reference_type = 'SUPPLIER_CREDIT_NOTE' "
                + "AND reference_id = ? AND posted = 1",
                Integer.class,
                result.journalEntryId(),
                tenantId,
                companyId,
                noteId
        );

        assertEquals(1, journalCount);

        checkAmount(
                "SELECT total_debit FROM journal_entry WHERE id = ?",
                total, result.journalEntryId()
        );

        checkAmount(
                "SELECT total_credit FROM journal_entry WHERE id = ?",
                total, result.journalEntryId()
        );

        Integer lineCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM journal_entry_line "
                + "WHERE journal_entry_id = ?",
                Integer.class,
                result.journalEntryId()
        );

        assertEquals(3, lineCount);

        checkAmount(
                "SELECT SUM(debit_amount) "
                + "FROM journal_entry_line WHERE journal_entry_id = ?",
                total, result.journalEntryId()
        );

        checkAmount(
                "SELECT SUM(credit_amount) "
                + "FROM journal_entry_line WHERE journal_entry_id = ?",
                total, result.journalEntryId()
        );

        System.out.println("SCN_REAL_JOURNAL_POSTED: OK");

        Integer positionCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM credit_note_financial_position "
                + "WHERE id = ? AND tenant_id = ? "
                + "AND company_id = ? AND credit_note_id = ? "
                + "AND position_type = 'SUPPLIER' "
                + "AND status = 'OPEN'",
                Integer.class,
                result.financialPositionId(),
                tenantId,
                companyId,
                noteId
        );

        assertEquals(1, positionCount);

        checkAmount(
                "SELECT original_amount "
                + "FROM credit_note_financial_position WHERE id = ?",
                total, result.financialPositionId()
        );

        checkAmount(
                "SELECT available_amount "
                + "FROM credit_note_financial_position WHERE id = ?",
                total, result.financialPositionId()
        );

        checkAmount(
                "SELECT compensated_amount "
                + "FROM credit_note_financial_position WHERE id = ?",
                BigDecimal.ZERO, result.financialPositionId()
        );

        checkAmount(
                "SELECT refunded_amount "
                + "FROM credit_note_financial_position WHERE id = ?",
                BigDecimal.ZERO, result.financialPositionId()
        );

        System.out.println("SCN_REAL_FINANCIAL_OPEN: OK");
        System.out.println("SCN_REAL_REGISTRATION: OK");
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
