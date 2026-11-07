package com.gestiva.platform.company.service;

import com.gestiva.platform.company.entity.Company;
import com.gestiva.platform.company.entity.CompanySettings;
import com.gestiva.platform.company.repository.CompanyRepository;
import com.gestiva.security.tenant.entity.Tenant;
import com.gestiva.settings.company.repository.CompanySettingsRepository;
import com.gestiva.settings.sequence.entity.DocumentSequence;
import com.gestiva.settings.sequence.repository.DocumentSequenceRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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
class CompanyBootstrapSandboxITTest {

    private static final String DATABASE =
            "gestiva_v9_sandbox";

    private static final List<String> CHECKED_TABLES = List.of(
            "tenant",
            "company",
            "company_settings",
            "document_sequence",
            "account"
    );

    private static final Map<String, String> PREFIXES = Map.of(
            "QUOTE", "QUO-",
            "SALES_ORDER", "SO-",
            "DELIVERY_NOTE", "DDT-",
            "SALES_INVOICE", "INV-",
            "PURCHASE_ORDER", "PO-",
            "GOODS_RECEIPT", "GR-",
            "SUPPLIER_INVOICE", "SI-",
            "JOURNAL_ENTRY", "JE-",
            "SUPPLIER_CREDIT_NOTE", "SCN-",
            "CUSTOMER_CREDIT_NOTE", "CCN-"
    );

    private Map<String, Long> baseline;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private CompanyBootstrapService bootstrap;

    @Autowired
    private DocumentSequenceRepository sequences;

    @Autowired
    private CompanySettingsRepository settings;

    @DynamicPropertySource
    static void sandboxProperties(DynamicPropertyRegistry registry) {
        String user = System.getenv("GESTIVA_V9_USER");
        String password = System.getenv("GESTIVA_V9_PASSWORD");

        if (!"gestiva_v9_runner".equals(user)
                || password == null
                || password.isBlank()) {
            throw new IllegalStateException(
                    "Credenziali sandbox non configurate.");
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

    private void verifySandboxIdentity() {
        String actualData = jdbc.queryForObject(
                "SELECT @@datadir", String.class);

        String expectedData =
                System.getenv("GESTIVA_V9_DATADIR");

        assertNotNull(expectedData);
        assertEquals(
                Path.of(expectedData).toAbsolutePath().normalize(),
                Path.of(actualData).toAbsolutePath().normalize()
        );

        assertEquals(33307, jdbc.queryForObject(
                "SELECT @@port", Integer.class));

        assertEquals("127.0.0.1", jdbc.queryForObject(
                "SELECT @@bind_address", String.class));

        assertEquals(DATABASE, jdbc.queryForObject(
                "SELECT DATABASE()", String.class));

        assertEquals(
                "gestiva_v9_runner@127.0.0.1",
                jdbc.queryForObject(
                        "SELECT CURRENT_USER()", String.class)
        );
    }

    private Map<String, Long> counts() {
        Map<String, Long> result = new HashMap<>();

        for (String table : CHECKED_TABLES) {
            Long count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM `" + table + "`",
                    Long.class
            );
            result.put(table, count);
        }

        return result;
    }

    @BeforeTransaction
    void captureBaseline() {
        verifySandboxIdentity();
        baseline = counts();

        for (String table : CHECKED_TABLES) {
            assertEquals(
                    0L,
                    baseline.get(table),
                    "La tabella sandbox deve essere vuota: " + table
            );
        }

        System.out.println("SANDBOX_BASELINE: OK");
    }

    @Test
    @Transactional
    void bootstrapShouldCreateTenSequencesAndRollback() {
        verifySandboxIdentity();

        Tenant tenant = new Tenant();
        tenant.setName("GESTIVA V9 SANDBOX TENANT");
        tenant.setSlug("gestiva-v9-sandbox-bootstrap");
        tenant.setEmail("sandbox-bootstrap@example.invalid");
        tenant.setStatus("ACTIVE");
        tenant.setDefaultLocale("it");
        tenant.setDefaultCurrency("EUR");

        entityManager.persist(tenant);
        entityManager.flush();

        Company company = new Company();
        company.setTenantId(tenant.getId());
        company.setCode("V9-SANDBOX");
        company.setLegalName("Gestiva V9 Sandbox Company");
        company.setCountryCode("IT");
        company.setCurrencyCode("EUR");
        company.setLocaleCode("it");
        company.setActive(true);

        entityManager.persist(company);
        entityManager.flush();

        Long tenantId = tenant.getId();
        Long companyId = company.getId();

        assertNotNull(tenantId);
        assertNotNull(companyId);

        bootstrap.initializeCompany(tenantId, companyId);
        entityManager.flush();
        entityManager.clear();

        List<DocumentSequence> first =
                sequences.findByTenantIdAndCompanyIdOrderByDocumentTypeAsc(
                        tenantId, companyId);

        assertEquals(10, first.size());

        Set<String> foundTypes = new HashSet<>();

        for (DocumentSequence sequence : first) {
            foundTypes.add(sequence.getDocumentType());

            assertEquals(
                    PREFIXES.get(sequence.getDocumentType()),
                    sequence.getPrefix()
            );
            assertEquals(1L, sequence.getNextNumber());
            assertEquals(5, sequence.getPaddingSize());
        }

        assertEquals(PREFIXES.keySet(), foundTypes);

        CompanySettings companySettings = settings
                .findByTenantIdAndCompanyId(tenantId, companyId)
                .orElseThrow();

        assertNotNull(companySettings);

        Long accountCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM account "
                + "WHERE tenant_id = ? AND company_id = ?",
                Long.class,
                tenantId, companyId
        );

        assertNotNull(accountCount);
        assertTrue(accountCount > 0);

        DocumentSequence supplier = first.stream()
                .filter(s -> "SUPPLIER_CREDIT_NOTE"
                        .equals(s.getDocumentType()))
                .findFirst()
                .orElseThrow();

        supplier.setPrefix("CUSTOM-SCN-");
        supplier.setNextNumber(17L);

        sequences.save(supplier);
        entityManager.flush();

        bootstrap.initializeCompany(tenantId, companyId);
        entityManager.flush();
        entityManager.clear();

        List<DocumentSequence> second =
                sequences.findByTenantIdAndCompanyIdOrderByDocumentTypeAsc(
                        tenantId, companyId);

        assertEquals(10, second.size());

        DocumentSequence supplierAfter = second.stream()
                .filter(s -> "SUPPLIER_CREDIT_NOTE"
                        .equals(s.getDocumentType()))
                .findFirst()
                .orElseThrow();

        assertEquals("CUSTOM-SCN-", supplierAfter.getPrefix());
        assertEquals(17L, supplierAfter.getNextNumber());

        DocumentSequence customerAfter = second.stream()
                .filter(s -> "CUSTOMER_CREDIT_NOTE"
                        .equals(s.getDocumentType()))
                .findFirst()
                .orElseThrow();

        assertEquals("CCN-", customerAfter.getPrefix());
        assertEquals(1L, customerAfter.getNextNumber());

        System.out.println(
                "SANDBOX_BOOTSTRAP_10_SEQUENCES: OK");
        System.out.println(
                "SANDBOX_BOOTSTRAP_IDEMPOTENCE: OK");
        System.out.println(
                "SANDBOX_BOOTSTRAP_CUSTOM_VALUES: OK");
    }

    @AfterTransaction
    void verifyRollback() {
        verifySandboxIdentity();

        assertNotNull(baseline);

        Map<String, Long> after = counts();

        assertEquals(
                baseline,
                after,
                "Dati residui dopo il rollback nella sandbox."
        );

        System.out.println(
                "SANDBOX_BOOTSTRAP_REAL_ROLLBACK: OK");
    }
}
