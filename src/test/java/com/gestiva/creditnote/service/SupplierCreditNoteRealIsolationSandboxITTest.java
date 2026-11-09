package com.gestiva.creditnote.service;

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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

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
class SupplierCreditNoteRealIsolationSandboxITTest {

    private static final String DATABASE = "gestiva_v9_sandbox";

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
    @Autowired PlatformTransactionManager manager;
    @Autowired CompanyBootstrapService bootstrap;
    @Autowired SupplierCreditNoteDraftService drafts;
    @Autowired SupplierCreditNoteRegistrationService registrations;

    @MockitoBean CompanyContext companyContext;


    record Fixture(
            Long tenantId,
            Long companyId,
            Long supplierId,
            Long orderId,
            Long receiptId,
            Long invoiceId,
            Long noteId) {
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        String user = System.getenv("GESTIVA_V9_USER");
        String password = System.getenv("GESTIVA_V9_PASSWORD");

        if (!"gestiva_v9_runner".equals(user)
                || password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Credenziali MySQL sandbox non valide.");
        }

        registry.add("spring.datasource.url", () ->
                "jdbc:mysql://127.0.0.1:33307/"
                + DATABASE
                + "?sslMode=DISABLED&allowPublicKeyRetrieval=true"
        );
        registry.add("spring.datasource.username", () -> user);
        registry.add("spring.datasource.password", () -> password);
        registry.add("spring.flyway.enabled", () -> false);
        registry.add("spring.jpa.hibernate.ddl-auto",
                () -> "validate");
    }

    private void verifySandbox() throws Exception {
        assertEquals(
                DATABASE,
                jdbc.queryForObject("SELECT DATABASE()", String.class)
        );

        assertEquals(
                33307,
                jdbc.queryForObject("SELECT @@port", Integer.class)
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
                        .toRealPath(),
                Path.of(jdbc.queryForObject(
                        "SELECT @@datadir", String.class))
                        .toRealPath()
        );
    }

    private long count(String table) {
        Long result = jdbc.queryForObject(
                "SELECT COUNT(*) FROM `" + table + "`",
                Long.class
        );
        assertNotNull(result);
        return result;
    }

    private long countSql(String sql, Object... args) {
        Long result = jdbc.queryForObject(
                sql, Long.class, args
        );
        assertNotNull(result);
        return result;
    }

    private void assertAllEmpty() {
        for (String table : TABLES) {
            assertEquals(
                    0L, count(table),
                    "Residui nella tabella " + table
            );
        }
    }

    private long journalSequence(
            Long tenantId, Long companyId) {
        Long result = jdbc.queryForObject(
                """
                SELECT next_number
                FROM document_sequence
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND document_type = 'JOURNAL_ENTRY'
                """,
                Long.class, tenantId, companyId
        );
        assertNotNull(result);
        return result;
    }

    private Fixture createCommittedFixture() {
        TransactionTemplate tx = new TransactionTemplate(manager);

        Fixture fixture = tx.execute(status -> {
            LocalDate date = LocalDate.of(2026, 11, 7);
            BigDecimal subtotal = new BigDecimal("100.00");
            BigDecimal tax = new BigDecimal("22.00");
            BigDecimal total = new BigDecimal("122.00");

            Tenant tenant = new Tenant();
            tenant.setName("SCN failure rollback sandbox");
            tenant.setSlug("scn-failure-sandbox-2240");
            tenant.setEmail("scn-failure@example.invalid");
            tenant.setStatus("ACTIVE");
            tenant.setDefaultLocale("it");
            tenant.setDefaultCurrency("EUR");
            em.persist(tenant);
            em.flush();

            Long tenantId = tenant.getId();

            Company company = new Company();
            company.setTenantId(tenantId);
            company.setCode("SCN-RB-2240");
            company.setLegalName("SCN Rollback Sandbox");
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
            supplier.setCode("SUP-SCN-RB-2240");
            supplier.setName("Fornitore SCN rollback");
            supplier.setActive(true);
            em.persist(supplier);
            em.flush();

            PurchaseOrder order = new PurchaseOrder();
            order.setTenantId(tenantId);
            order.setCompanyId(companyId);
            order.setOrderNumber("PO-SCN-RB-2240");
            order.setOrderDate(date);
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
            receipt.setReceiptNumber("GR-SCN-RB-2240");
            receipt.setReceiptDate(date);
            receipt.setPurchaseOrderId(order.getId());
            receipt.setSupplierId(supplier.getId());
            em.persist(receipt);
            em.flush();

            SupplierInvoice invoice = new SupplierInvoice();
            invoice.setTenantId(tenantId);
            invoice.setCompanyId(companyId);
            invoice.setInvoiceNumber("SI-SCN-RB-2240");
            invoice.setInvoiceDate(date);
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
            line.setDescription("Merce sandbox SCN rollback");
            line.setQuantity(new BigDecimal("10.000"));
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

            Long noteId = drafts.createPartialDraft(
                    tenantId,
                    invoice.getId(),
                    java.util.Map.of(
                            line.getId(), new BigDecimal("4.000")),
                    "Prima rettifica cumulativa confermata"
            );

            em.flush();

            return new Fixture(
                    tenantId,
                    companyId,
                    supplier.getId(),
                    order.getId(),
                    receipt.getId(),
                    invoice.getId(),
                    noteId
            );
        });

        assertNotNull(fixture);

        return fixture;
    }


    private void cleanup(Fixture f) {
        // Il database deve essere già stato verificato come sandbox
        // vuota prima di creare la fixture.
        //
        // Cancellazioni limitate a tenant e società creati dal test.
        // FK rispettate: figli prima delle rispettive testate.
        TransactionTemplate tx = new TransactionTemplate(manager);

        tx.executeWithoutResult(status -> {
            Long tenant = f.tenantId();
            Long company = f.companyId();

            jdbc.update(
                    """
                    DELETE FROM journal_entry_line
                    WHERE tenant_id = ? AND company_id = ?
                      AND journal_entry_id IN (
                          SELECT id FROM journal_entry
                          WHERE tenant_id = ? AND company_id = ?
                            AND reference_type = 'SUPPLIER_CREDIT_NOTE'
                      )
                    """,
                    tenant, company, tenant, company
            );

            jdbc.update(
                    """
                    DELETE FROM journal_entry
                    WHERE tenant_id = ? AND company_id = ?
                      AND reference_type = 'SUPPLIER_CREDIT_NOTE'
                    """,
                    tenant, company
            );

            jdbc.update(
                    """
                    DELETE FROM credit_note_financial_position
                    WHERE tenant_id = ? AND company_id = ?
                      AND credit_note_id IN (
                          SELECT id FROM credit_note
                          WHERE tenant_id = ? AND company_id = ?
                            AND original_invoice_id = ?
                      )
                    """,
                    tenant, company,
                    tenant, company, f.invoiceId()
            );

            jdbc.update(
                    """
                    DELETE FROM credit_note_line
                    WHERE tenant_id = ? AND company_id = ?
                      AND credit_note_id IN (
                          SELECT id FROM credit_note
                          WHERE tenant_id = ? AND company_id = ?
                            AND original_invoice_id = ?
                      )
                    """,
                    tenant, company,
                    tenant, company, f.invoiceId()
            );

            jdbc.update(
                    """
                    DELETE FROM credit_note
                    WHERE tenant_id = ? AND company_id = ?
                      AND original_invoice_id = ?
                    """,
                    tenant, company, f.invoiceId()
            );

            jdbc.update(
                    """
                    DELETE FROM supplier_invoice_line
                    WHERE tenant_id = ? AND supplier_invoice_id = ?
                    """,
                    tenant, f.invoiceId()
            );

            jdbc.update(
                    """
                    DELETE FROM supplier_invoice
                    WHERE tenant_id = ? AND company_id = ? AND id = ?
                    """,
                    tenant, company, f.invoiceId()
            );

            jdbc.update(
                    """
                    DELETE FROM goods_receipt
                    WHERE tenant_id = ? AND company_id = ? AND id = ?
                    """,
                    tenant, company, f.receiptId()
            );

            jdbc.update(
                    """
                    DELETE FROM purchase_order
                    WHERE tenant_id = ? AND company_id = ? AND id = ?
                    """,
                    tenant, company, f.orderId()
            );

            jdbc.update(
                    "DELETE FROM supplier WHERE tenant_id = ? AND id = ?",
                    tenant, f.supplierId()
            );

            jdbc.update(
                    "DELETE FROM account WHERE tenant_id = ? AND company_id = ?",
                    tenant, company
            );

            jdbc.update(
                    """
                    DELETE FROM document_sequence
                    WHERE tenant_id = ? AND company_id = ?
                    """,
                    tenant, company
            );

            jdbc.update(
                    "DELETE FROM company_settings WHERE company_id = ?",
                    company
            );

            jdbc.update(
                    "DELETE FROM company WHERE tenant_id = ? AND id = ?",
                    tenant, company
            );

            jdbc.update(
                    "DELETE FROM tenant WHERE id = ?",
                    tenant
            );
        });
    }

    private void checkMoney(
            String expected, String sql, Object... parameters) {

        BigDecimal actual = jdbc.queryForObject(
                sql, BigDecimal.class, parameters);

        assertNotNull(actual);
        assertEquals(
                0,
                new BigDecimal(expected).compareTo(actual),
                "Importo non corrispondente: " + sql
        );
    }

    private Long createPartialCommitted(
            Fixture f,
            Long lineId,
            String quantity,
            String reason) {

        Long result = drafts.createPartialDraft(
                f.tenantId(),
                f.invoiceId(),
                java.util.Map.of(lineId, new BigDecimal(quantity)),
                reason
        );

        assertNotNull(result);
        return result;
    }


    @Autowired
    com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository
            invoiceRepository;

    @Autowired
    com.gestiva.creditnote.repository.CreditNoteRepository
            creditNoteRepository;

    record ForeignContext(
            Long companyB,
            Long tenantB,
            Long companyC) {
    }

    private ForeignContext createForeignContexts(Long tenantA) {
        TransactionTemplate tx = new TransactionTemplate(manager);

        ForeignContext result = tx.execute(status -> {
            Company companyB = new Company();
            companyB.setTenantId(tenantA);
            companyB.setCode("SCN-ISO-B-2263");
            companyB.setLegalName("SCN Isolamento Societa B");
            companyB.setCountryCode("IT");
            companyB.setCurrencyCode("EUR");
            companyB.setLocaleCode("it");
            companyB.setActive(true);
            em.persist(companyB);
            em.flush();

            Tenant tenantB = new Tenant();
            tenantB.setName("SCN Isolamento Tenant B");
            tenantB.setSlug("scn-isolation-tenant-b-2263");
            tenantB.setEmail("scn-isolation-b@example.invalid");
            tenantB.setStatus("ACTIVE");
            tenantB.setDefaultLocale("it");
            tenantB.setDefaultCurrency("EUR");
            em.persist(tenantB);
            em.flush();

            Company companyC = new Company();
            companyC.setTenantId(tenantB.getId());
            companyC.setCode("SCN-ISO-C-2263");
            companyC.setLegalName("SCN Isolamento Societa C");
            companyC.setCountryCode("IT");
            companyC.setCurrencyCode("EUR");
            companyC.setLocaleCode("it");
            companyC.setActive(true);
            em.persist(companyC);
            em.flush();

            return new ForeignContext(
                    companyB.getId(),
                    tenantB.getId(),
                    companyC.getId()
            );
        });

        assertNotNull(result);
        return result;
    }

    private void cleanupForeign(Fixture f, ForeignContext foreign) {
        TransactionTemplate tx = new TransactionTemplate(manager);

        tx.executeWithoutResult(status -> {
            int removedC = jdbc.update(
                    "DELETE FROM company WHERE tenant_id = ? AND id = ?",
                    foreign.tenantB(), foreign.companyC()
            );
            int removedB = jdbc.update(
                    "DELETE FROM company WHERE tenant_id = ? AND id = ?",
                    f.tenantId(), foreign.companyB()
            );
            int removedTenantB = jdbc.update(
                    "DELETE FROM tenant WHERE id = ?",
                    foreign.tenantB()
            );

            assertEquals(1, removedC);
            assertEquals(1, removedB);
            assertEquals(1, removedTenantB);
        });
    }

    private java.util.Map<String, Long> snapshotTables() {
        java.util.Map<String, Long> result =
                new java.util.LinkedHashMap<>();

        for (String table : TABLES) {
            result.put(table, count(table));
        }

        return result;
    }

    private void verifyForeignRejection(
            Fixture f, Long requestedTenant, Long activeCompany,
            Long lineId, String label) {

        org.mockito.Mockito.reset(companyContext);
        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(activeCompany);

        // Le query reali devono nascondere i documenti
        // appartenenti al contesto originale.
        assertTrue(invoiceRepository
                .findByTenantIdAndCompanyIdAndId(
                        requestedTenant, activeCompany, f.invoiceId())
                .isEmpty());

        assertTrue(creditNoteRepository
                .findByTenantIdAndCompanyIdAndId(
                        requestedTenant, activeCompany, f.noteId())
                .isEmpty());

        var before = snapshotTables();

        assertThrows(
                com.gestiva.common.exception.BusinessException.class,
                () -> drafts.createFullDraft(
                        requestedTenant, f.invoiceId(),
                        "Rettifica non autorizzata")
        );

        assertThrows(
                com.gestiva.common.exception.BusinessException.class,
                () -> drafts.createPartialDraft(
                        requestedTenant,
                        f.invoiceId(),
                        java.util.Map.of(
                                lineId, new BigDecimal("1.000")),
                        "Rettifica parziale non autorizzata")
        );

        assertThrows(
                com.gestiva.common.exception.BusinessException.class,
                () -> registrations.register(
                        requestedTenant, f.noteId())
        );

        assertEquals(
                "DRAFT",
                jdbc.queryForObject(
                        "SELECT status FROM credit_note WHERE id = ?",
                        String.class, f.noteId())
        );

        assertEquals(before, snapshotTables(),
                "Scritture inattese nel contesto " + label);

        System.out.println(
                "SCN_REAL_ISOLATION_" + label + ": OK");
    }

    @Test
    void foreignCompanyAndTenantCannotUseOriginalDocuments()
            throws Exception {

        verifySandbox();
        assertAllEmpty();
        System.out.println("SCN_REAL_ISOLATION_BASELINE: OK");

        Fixture fixture = null;
        ForeignContext foreign = null;

        try {
            fixture = createCommittedFixture();
            foreign = createForeignContexts(fixture.tenantId());

            Fixture f = fixture;

            Long lineId = jdbc.queryForObject(
                    """
                    SELECT id FROM supplier_invoice_line
                    WHERE tenant_id = ?
                      AND supplier_invoice_id = ?
                      AND line_no = 1
                    """,
                    Long.class, f.tenantId(), f.invoiceId()
            );

            assertNotNull(lineId);

            System.out.println("SCN_REAL_ISOLATION_FIXTURE: OK");

            verifyForeignRejection(
                    f, f.tenantId(), foreign.companyB(),
                    lineId, "CROSS_COMPANY"
            );

            verifyForeignRejection(
                    f, foreign.tenantB(), foreign.companyC(),
                    lineId, "CROSS_TENANT"
            );

            // Verifica anche il predicato tenant usando
            // l'ID della società originale.
            assertTrue(invoiceRepository
                    .findByTenantIdAndCompanyIdAndId(
                            foreign.tenantB(), f.companyId(),
                            f.invoiceId())
                    .isEmpty());

            assertTrue(creditNoteRepository
                    .findByTenantIdAndCompanyIdAndId(
                            foreign.tenantB(), f.companyId(),
                            f.noteId())
                    .isEmpty());

            System.out.println(
                    "SCN_REAL_ISOLATION_TENANT_PREDICATE: OK");

        } finally {
            if (fixture != null) {
                verifySandbox();

                if (foreign != null) {
                    cleanupForeign(fixture, foreign);
                }

                cleanup(fixture);
            }

            verifySandbox();
            assertAllEmpty();
            System.out.println("SCN_REAL_ISOLATION_CLEANUP: OK");
        }
    }
}
