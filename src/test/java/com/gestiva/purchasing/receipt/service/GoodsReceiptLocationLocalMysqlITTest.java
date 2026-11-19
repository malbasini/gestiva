package com.gestiva.purchasing.receipt.service;

import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("integration")
@EnabledIfEnvironmentVariable(
        named = "GESTIVA_INVENTORY_LOCATION_LOCAL_IT",
        matches = "enabled"
)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
        "gestiva.purchasing.partial-goods-receipt.enabled=true"
})
class GoodsReceiptLocationLocalMysqlITTest {

    private static final String SCHEMA =
            "gestiva_inventory_location_it";

    private static final String JDBC_URL =
            "jdbc:mysql://127.0.0.1:33309/"
            + SCHEMA + "?sslMode=REQUIRED";

    private static final String DB_USER = "gestiva_jpql_it";

    private static final Path WORKSPACE = Path.of(
            "/private/tmp/gestiva_inventory_jpql_it_33309"
    );

    private static final String[] TABLES = {
            "tenant", "company", "supplier", "unit_of_measure",
            "item", "warehouse", "warehouse_location",
            "purchase_order", "purchase_order_line",
            "goods_receipt", "goods_receipt_line",
            "inventory_movement", "inventory_layer"
    };

    private static String readPassword() {
        try {
            Path file = WORKSPACE.resolve("app-password.local");
            if (Files.isSymbolicLink(file)
                    || !Files.isRegularFile(file)
                    || !Files.getPosixFilePermissions(file).equals(
                            PosixFilePermissions.fromString("rw-------"))) {
                throw new IllegalStateException(
                        "STOP: credenziali sandbox non sicure");
            }
            String value = Files.readString(file).strip();
            if (value.isEmpty()) {
                throw new IllegalStateException(
                        "STOP: credenziali sandbox vuote");
            }
            return value;
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "STOP: credenziali sandbox non disponibili",
                    exception);
        }
    }

    private static void verifyIdentity(Connection connection)
            throws Exception {
        String expectedDatadir =
                WORKSPACE.resolve("data").toRealPath().toString();

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT @@datadir, @@port, @@bind_address, "
                     + "@@skip_networking, CURRENT_USER(), DATABASE(), "
                     + "@@version")) {

            if (!result.next()) {
                throw new IllegalStateException(
                        "STOP: identificazione MySQL vuota");
            }

            String actualDatadir = Path.of(
                    result.getString(1)).toRealPath().toString();

            boolean valid =
                    expectedDatadir.equals(actualDatadir)
                    && result.getInt(2) == 33309
                    && "127.0.0.1".equals(result.getString(3))
                    && result.getInt(4) == 0
                    && "gestiva_jpql_it@localhost".equals(
                            result.getString(5))
                    && SCHEMA.equals(result.getString(6))
                    && result.getString(7).startsWith("8.0.");

            if (!valid) {
                throw new IllegalStateException(
                        "STOP: identità MySQL sandbox non conforme");
            }
        }

        if (!SCHEMA.equals(connection.getCatalog())) {
            throw new IllegalStateException(
                    "STOP: catalogo database non conforme");
        }
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry)
            throws Exception {

        if (!"enabled".equals(System.getenv(
                "GESTIVA_INVENTORY_LOCATION_LOCAL_IT"))) {
            throw new IllegalStateException(
                    "STOP: abilitazione esplicita assente");
        }

        String password = readPassword();

        // Verifica indipendente PRIMA che Spring o Flyway si avviino.
        try (Connection connection = DriverManager.getConnection(
                JDBC_URL, DB_USER, password)) {
            verifyIdentity(connection);
        }

        registry.add("spring.datasource.url", () -> JDBC_URL);
        registry.add("spring.datasource.username", () -> DB_USER);
        registry.add("spring.datasource.password", () -> password);

        registry.add("spring.flyway.url", () -> JDBC_URL);
        registry.add("spring.flyway.user", () -> DB_USER);
        registry.add("spring.flyway.password", () -> password);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.flyway.clean-disabled", () -> true);
        registry.add("spring.flyway.baseline-on-migrate", () -> false);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired private DataSource dataSource;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private EntityManager entityManager;
    @Autowired private PlatformTransactionManager manager;
    @Autowired private GoodsReceiptPartialPersistenceWriter writer;

    @BeforeEach
    void verifyEnvironmentBeforeWriting() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            verifyIdentity(connection);
        }

        for (String table : TABLES) {
            Integer count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM `" + table + "`",
                    Integer.class);
            assertEquals(0, count, "Sandbox non vuota: " + table);
        }
    }

    private BigDecimal bd(String text) {
        return new BigDecimal(text);
    }

    private void sameNumber(String expected, BigDecimal actual) {
        assertNotNull(actual);
        assertEquals(0, bd(expected).compareTo(actual));
    }

    private int count(String sql, Object... parameters) {
        Integer value = jdbc.queryForObject(
                sql, Integer.class, parameters);
        assertNotNull(value);
        return value;
    }

    private long insert(String sql, Object... parameters) {
        KeyHolder holder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            return statement;
        }, holder);

        assertNotNull(holder.getKey());
        return holder.getKey().longValue();
    }

    private void assertNoResidues() {
        for (String table : TABLES) {
            assertEquals(
                    0,
                    count("SELECT COUNT(*) FROM `" + table + "`"),
                    "Residui dopo rollback: " + table
            );
        }
    }

    @Test
    void realLocatedReceiptMovementAndFifoAreRolledBack()
            throws Exception {

        String marker = UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12);

        AtomicLong orderId = new AtomicLong();
        AtomicLong receiptId = new AtomicLong();
        AtomicLong movementId = new AtomicLong();
        AtomicLong layerId = new AtomicLong();

        TransactionTemplate transaction = new TransactionTemplate(manager);
        transaction.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRED);
        transaction.setIsolationLevel(
                TransactionDefinition.ISOLATION_READ_COMMITTED);
        transaction.setReadOnly(false);

        String rollbackMarker = "ROLLBACK_LOCATION_FIFO_INTENZIONALE";

        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> transaction.execute(status -> {

                    // Le anagrafiche sono create nella STESSA transazione.
                    long tenant = insert("""
                            INSERT INTO tenant (
                                name, slug, email, status,
                                inventory_valuation_method,
                                created_at, updated_at
                            ) VALUES (?, ?, ?, 'ACTIVE', 'FIFO',
                                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                            """,
                            "Tenant test " + marker,
                            "location-it-" + marker,
                            "location-" + marker + "@example.invalid");

                    long company = insert("""
                            INSERT INTO company (
                                tenant_id, code, legal_name,
                                country_code, currency_code, locale_code,
                                active, created_at, updated_at
                            ) VALUES (?, ?, ?, 'IT', 'EUR', 'it_IT',
                                1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                            """,
                            tenant,
                            "LOC" + marker,
                            "Societa test ubicazione");

                    long supplier = insert("""
                            INSERT INTO supplier (
                                tenant_id, code, name, active,
                                created_at, updated_at
                            ) VALUES (?, ?, ?, 1,
                                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                            """,
                            tenant,
                            "SUP" + marker,
                            "Fornitore fixture ubicazione");

                    long uom = insert("""
                            INSERT INTO unit_of_measure (
                                tenant_id, code, name, symbol,
                                dimension, decimal_places, active,
                                created_at, updated_at
                            ) VALUES (?, 'PZ', 'Pezzo', 'PZ',
                                'QUANTITY', 3, 1,
                                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                            """, tenant);

                    long item = insert("""
                            INSERT INTO item (
                                tenant_id, code, name, item_type,
                                base_uom_id, tracking_type,
                                active, created_at, updated_at
                            ) VALUES (?, ?, ?, 'PRODUCT', ?, 'NONE',
                                1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                            """,
                            tenant,
                            "ITEM" + marker,
                            "Articolo fixture ricevimento",
                            uom);

                    long warehouse = insert("""
                            INSERT INTO warehouse (
                                tenant_id, company_id, code, name,
                                warehouse_type, active,
                                created_at, updated_at
                            ) VALUES (?, ?, ?, ?, 'PHYSICAL', 1,
                                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                            """,
                            tenant, company,
                            "WH" + marker, "Magazzino fixture");

                    long location = insert("""
                            INSERT INTO warehouse_location (
                                tenant_id, warehouse_id, code, name,
                                location_type, active,
                                created_at, updated_at
                            ) VALUES (?, ?, ?, ?, 'STORAGE', 1,
                                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                            """,
                            tenant, warehouse,
                            "LOC" + marker, "Ubicazione fixture");

                    long order = insert("""
                            INSERT INTO purchase_order (
                                tenant_id, company_id, order_number,
                                order_date, supplier_id, status,
                                currency_code, subtotal_amount,
                                tax_amount, total_amount,
                                created_at, updated_at
                            ) VALUES (?, ?, ?, CURRENT_DATE, ?,
                                'CONFIRMED', 'EUR', 7.20, 1.58, 8.78,
                                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                            """,
                            tenant, company, "PO-LOC-" + marker, supplier);

                    orderId.set(order);

                    long orderLine = insert("""
                            INSERT INTO purchase_order_line (
                                tenant_id, purchase_order_id, line_no,
                                item_id, uom_id, conversion_factor,
                                description, quantity, unit_price,
                                discount_pct, tax_pct, line_subtotal,
                                tax_amount, line_total,
                                created_at, updated_at
                            ) VALUES (?, ?, 1, ?, ?, 1.000000, ?,
                                1.000, 7.20, 0.00, 22.00, 7.20,
                                1.58, 8.78,
                                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                            """,
                            tenant, order, item, uom,
                            "Fixture ricevimento 1 PZ");

                    GoodsReceipt header = new GoodsReceipt();
                    header.setTenantId(tenant);
                    header.setCompanyId(company);
                    header.setPurchaseOrderId(order);
                    header.setSupplierId(supplier);
                    header.setWarehouseId(warehouse);
                    header.setLocationId(location);
                    header.setReceiptNumber("GR-LOC-" + marker);
                    header.setReceiptDate(LocalDate.of(2026, 11, 19));

                    PurchaseOrderLine line = mock(PurchaseOrderLine.class);
                    when(line.getId()).thenReturn(orderLine);
                    when(line.getTenantId()).thenReturn(tenant);
                    when(line.getPurchaseOrderId()).thenReturn(order);
                    when(line.getLineNo()).thenReturn(1);
                    when(line.getItemId()).thenReturn(item);
                    when(line.getDescription()).thenReturn(
                            "Fixture ricevimento 1 PZ");

                    var planned =
                            GoodsReceiptPartialFifoLineCalculator.calculate(
                                    bd("1.000"), bd("0.000"),
                                    bd("1.000"), bd("7.20"));

                    var plan =
                            GoodsReceiptPartialDocumentAssembler.prepare(
                                    header,
                                    List.of(
                                            new GoodsReceiptPartialDocumentAssembler.LineInput(
                                                    line, planned
                                            )
                                    )
                            );

                    long receipt = writer.write(plan);
                    receiptId.set(receipt);

                    entityManager.flush();

                    assertEquals(
                            1,
                            count("""
                                    SELECT COUNT(*) FROM goods_receipt
                                    WHERE id = ? AND tenant_id = ?
                                      AND company_id = ?
                                      AND warehouse_id = ?
                                      AND location_id = ?
                                    """,
                                    receipt, tenant, company,
                                    warehouse, location)
                    );

                    sameNumber(
                            "1.000",
                            jdbc.queryForObject("""
                                    SELECT quantity_received
                                    FROM goods_receipt_line
                                    WHERE goods_receipt_id = ?
                                    """,
                                    BigDecimal.class, receipt)
                    );

                    long movement = jdbc.queryForObject("""
                            SELECT id FROM inventory_movement
                            WHERE tenant_id = ?
                              AND company_id = ?
                              AND warehouse_id = ?
                              AND location_id = ?
                              AND item_id = ?
                              AND reference_type = 'GOODS_RECEIPT'
                              AND reference_id = ?
                              AND causal_code = 'PURCHASE_RECEIPT'
                              AND movement_type = 'IN'
                            """, Long.class,
                            tenant, company, warehouse,
                            location, item, receipt);

                    movementId.set(movement);

                    sameNumber(
                            "1.000",
                            jdbc.queryForObject("""
                                    SELECT quantity
                                    FROM inventory_movement
                                    WHERE id = ?
                                    """,
                                    BigDecimal.class, movement)
                    );

                    sameNumber(
                            "7.2000",
                            jdbc.queryForObject("""
                                    SELECT unit_cost
                                    FROM inventory_movement
                                    WHERE id = ?
                                    """,
                                    BigDecimal.class, movement)
                    );

                    sameNumber(
                            "7.2000",
                            jdbc.queryForObject("""
                                    SELECT total_cost
                                    FROM inventory_movement
                                    WHERE id = ?
                                    """,
                                    BigDecimal.class, movement)
                    );

                    long layer = jdbc.queryForObject("""
                            SELECT id FROM inventory_layer
                            WHERE tenant_id = ?
                              AND company_id = ?
                              AND warehouse_id = ?
                              AND location_id = ?
                              AND item_id = ?
                              AND source_movement_id = ?
                            """, Long.class,
                            tenant, company, warehouse,
                            location, item, movement);

                    layerId.set(layer);

                    sameNumber(
                            "1.000",
                            jdbc.queryForObject("""
                                    SELECT original_qty
                                    FROM inventory_layer
                                    WHERE id = ?
                                    """,
                                    BigDecimal.class, layer)
                    );

                    sameNumber(
                            "1.000",
                            jdbc.queryForObject("""
                                    SELECT remaining_qty
                                    FROM inventory_layer
                                    WHERE id = ?
                                    """,
                                    BigDecimal.class, layer)
                    );

                    sameNumber(
                            "7.2000",
                            jdbc.queryForObject("""
                                    SELECT unit_cost
                                    FROM inventory_layer
                                    WHERE id = ?
                                    """,
                                    BigDecimal.class, layer)
                    );

                    System.out.println(
                            "FIFO LOCATION: ricevimento 1 PZ verificato");
                    System.out.println(
                            "FIFO LOCATION: ubicazione propagata a movimento e layer");
                    System.out.println(
                            "FIFO LOCATION: costo unitario e valore 7.2000");

                    // Forza il rollback dell'intera fixture.
                    throw new IllegalStateException(rollbackMarker);
                })
        );

        assertEquals(rollbackMarker, failure.getMessage());

        assertTrue(orderId.get() > 0);
        assertTrue(receiptId.get() > 0);
        assertTrue(movementId.get() > 0);
        assertTrue(layerId.get() > 0);

        // Fuori dalla transazione: tutte le tabelle devono essere vuote.
        assertNoResidues();

        System.out.println(
                "FIFO LOCATION: rollback verificato, nessun residuo");
    }
}
