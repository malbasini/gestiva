package com.gestiva.warehouse.inventory.movement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(
        named = "GESTIVA_FIFO_SANDBOX_TEST",
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
class InventoryLocationTransferAtomicPostingSandboxITTest {

    private static final String DATABASE = "gestiva_scn_collaudo";
    private static final String URL =
            "jdbc:mysql://127.0.0.1:33308/" + DATABASE
            + "?sslMode=DISABLED&allowPublicKeyRetrieval=true";

    private static final long TENANT = 2L;
    private static final long COMPANY = 1L;

    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired InventoryLocationTransferAtomicPostingWriter writer;

    private static String env(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("STOP: variabile assente: " + name);
        }
        return value;
    }

    @DynamicPropertySource
    static void sandbox(DynamicPropertyRegistry registry) throws Exception {
        String host = env("MYSQL_COLLAUDO_HOST");
        String port = env("MYSQL_COLLAUDO_PORT");
        String database = env("MYSQL_COLLAUDO_DATABASE");
        String user = env("MYSQL_COLLAUDO_USER");
        String password = env("MYSQL_COLLAUDO_PASSWORD");

        if (!"127.0.0.1".equals(host)
                || !"33308".equals(port)
                || !DATABASE.equals(database)
                || !"gestiva_scn_migrator".equals(user)) {
            throw new IllegalStateException("STOP: datasource non autorizzato");
        }

        try (var connection = DriverManager.getConnection(
                URL, user, password)) {
            connection.setReadOnly(true);

            try (var statement = connection.createStatement();
                 var rs = statement.executeQuery(
                         "SELECT VERSION(), @@port, DATABASE(), "
                         + "CURRENT_USER(), @@datadir, @@socket")) {

                if (!rs.next()) {
                    throw new IllegalStateException("STOP: identità assente");
                }

                Path base = Path.of(
                        System.getProperty("user.home"),
                        ".gestiva/mysql-v9-restore"
                );

                boolean valid =
                        "8.0.45".equals(rs.getString(1))
                        && rs.getInt(2) == 33308
                        && DATABASE.equals(rs.getString(3))
                        && "gestiva_scn_migrator@127.0.0.1"
                                .equals(rs.getString(4))
                        && base.resolve("data").toRealPath()
                                .equals(Path.of(rs.getString(5)).toRealPath())
                        && base.resolve("mysql.sock").toAbsolutePath()
                                .normalize()
                                .equals(Path.of(rs.getString(6))
                                        .toAbsolutePath().normalize());

                if (!valid) {
                    throw new IllegalStateException(
                            "STOP: identità fisica MySQL non conforme");
                }
            }
        }

        registry.add("spring.datasource.url", () -> URL);
        registry.add("spring.datasource.username", () -> user);
        registry.add("spring.datasource.password", () -> password);
    }

    private record Fixture(
            long itemId,
            long warehouseId,
            long sourceId,
            long targetId,
            long movement1,
            long movement2,
            long layer1,
            long layer2,
            String operationKey
    ) {}

    private long id(String sql, Object... params) {
        Long result = jdbc.queryForObject(sql, Long.class, params);
        assertNotNull(result);
        return result;
    }

    private long count(String sql, Object... params) {
        return id(sql, params);
    }

    private void amount(String expected, String sql, Object... params) {
        BigDecimal actual = jdbc.queryForObject(
                sql, BigDecimal.class, params);
        assertNotNull(actual);
        assertEquals(
                0,
                new BigDecimal(expected).compareTo(actual),
                sql
        );
    }

    private Fixture createFixture(String suffix) {
        jdbc.update("""
                INSERT INTO item (
                    tenant_id, code, name, item_type,
                    base_uom_id, tracking_type,
                    track_expiration, active,
                    created_at, updated_at
                )
                VALUES (?, ?, ?, 'PRODUCT', 1, 'NONE',
                        0, 1, NOW(), NOW())
                """,
                TENANT, "FIFO-" + suffix, "Prodotto test FIFO " + suffix);

        long itemId = id(
                "SELECT id FROM item WHERE tenant_id=? AND code=?",
                TENANT, "FIFO-" + suffix);

        jdbc.update("""
                INSERT INTO warehouse (
                    tenant_id, company_id, code, name,
                    warehouse_type, allow_negative_stock,
                    active, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, 'PHYSICAL', 0, 1, NOW(), NOW())
                """,
                TENANT, COMPANY,
                "WF-" + suffix, "Magazzino test FIFO " + suffix);

        long warehouseId = id(
                "SELECT id FROM warehouse WHERE tenant_id=? AND code=?",
                TENANT, "WF-" + suffix);

        jdbc.update("""
                INSERT INTO warehouse_location (
                    tenant_id, warehouse_id, code, name,
                    location_type, active, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, 'STORAGE', 1, NOW(), NOW())
                """,
                TENANT, warehouseId,
                "A-" + suffix, "Origine FIFO");

        jdbc.update("""
                INSERT INTO warehouse_location (
                    tenant_id, warehouse_id, code, name,
                    location_type, active, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, 'STORAGE', 1, NOW(), NOW())
                """,
                TENANT, warehouseId,
                "B-" + suffix, "Destinazione FIFO");

        long sourceId = id(
                "SELECT id FROM warehouse_location "
                + "WHERE warehouse_id=? AND code=?",
                warehouseId, "A-" + suffix);

        long targetId = id(
                "SELECT id FROM warehouse_location "
                + "WHERE warehouse_id=? AND code=?",
                warehouseId, "B-" + suffix);

        long movement1 = createInitialMovement(
                warehouseId, sourceId, itemId,
                LocalDate.of(2026, 10, 1),
                "8.000", "7.2000", "57.6000");

        long movement2 = createInitialMovement(
                warehouseId, sourceId, itemId,
                LocalDate.of(2026, 10, 10),
                "7.000", "9.5000", "66.5000");

        long layer1 = createLayer(
                warehouseId, sourceId, itemId, movement1,
                LocalDate.of(2026, 10, 1),
                "8.000", "7.2000");

        long layer2 = createLayer(
                warehouseId, sourceId, itemId, movement2,
                LocalDate.of(2026, 10, 10),
                "7.000", "9.5000");

        return new Fixture(
                itemId, warehouseId, sourceId, targetId,
                movement1, movement2, layer1, layer2,
                "FIFO-TRANSFER-" + suffix
        );
    }

    private long createInitialMovement(
            long warehouse, long location, long item,
            LocalDate date, String qty,
            String unitCost, String totalCost) {

        jdbc.update("""
                INSERT INTO inventory_movement (
                    tenant_id, company_id, warehouse_id,
                    location_id, item_id, movement_date,
                    movement_type, causal_code, quantity,
                    unit_cost, total_cost, reference_type,
                    reversed, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?,
                        'IN', 'PURCHASE_RECEIPT', ?, ?, ?,
                        'FIFO_SANDBOX_TEST', b'0', NOW(), NOW())
                """,
                TENANT, COMPANY, warehouse, location, item,
                date, new BigDecimal(qty),
                new BigDecimal(unitCost), new BigDecimal(totalCost));

        return id("""
                SELECT id FROM inventory_movement
                WHERE tenant_id=? AND company_id=?
                  AND warehouse_id=? AND location_id=?
                  AND item_id=? AND movement_date=?
                  AND reference_type='FIFO_SANDBOX_TEST'
                """,
                TENANT, COMPANY, warehouse, location, item, date);
    }

    private long createLayer(
            long warehouse, long location, long item,
            long sourceMovement, LocalDate date,
            String qty, String cost) {

        jdbc.update("""
                INSERT INTO inventory_layer (
                    tenant_id, company_id, warehouse_id,
                    location_id, item_id, source_movement_id,
                    layer_date, original_qty, remaining_qty,
                    unit_cost, closed, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                        b'0', NOW(), NOW())
                """,
                TENANT, COMPANY, warehouse, location, item,
                sourceMovement, date,
                new BigDecimal(qty), new BigDecimal(qty),
                new BigDecimal(cost));

        return id(
                "SELECT id FROM inventory_layer "
                + "WHERE source_movement_id=?",
                sourceMovement);
    }

    @Test
    void twoFifoLayersSurviveRealMysqlCommit() {
        assertEquals(
                33308,
                jdbc.queryForObject("SELECT @@port", Integer.class)
        );
        assertEquals(
                DATABASE,
                jdbc.queryForObject("SELECT DATABASE()", String.class)
        );

        String suffix = UUID.randomUUID()
                .toString().replace("-", "").substring(0, 12);

        Fixture fixture = null;
        Long transferId = null;

        try {
            TransactionTemplate tx =
                    new TransactionTemplate(transactionManager);

            fixture = tx.execute(
                    status -> createFixture(suffix)
            );
            assertNotNull(fixture);

            Fixture f = fixture;

            transferId = tx.execute(status ->
                    writer.write(
                            TENANT,
                            COMPANY,
                            f.warehouseId(),
                            f.itemId(),
                            f.sourceId(),
                            f.targetId(),
                            new BigDecimal("12.000"),
                            LocalDate.of(2026, 11, 25),
                            "Commit reale FIFO sandbox",
                            f.operationKey()
                    )
            );

            assertNotNull(transferId);

            assertEquals("POSTED", jdbc.queryForObject(
                    "SELECT status FROM inventory_location_transfer "
                    + "WHERE id=?", String.class, transferId));

            assertEquals(2L, count(
                    "SELECT COUNT(*) FROM "
                    + "inventory_location_transfer_allocation "
                    + "WHERE transfer_id=?", transferId));

            amount("0.000",
                    "SELECT remaining_qty FROM inventory_layer WHERE id=?",
                    f.layer1());

            amount("3.000",
                    "SELECT remaining_qty FROM inventory_layer WHERE id=?",
                    f.layer2());

            long outId = id(
                    "SELECT out_movement_id "
                    + "FROM inventory_location_transfer WHERE id=?",
                    transferId);

            long inId = id(
                    "SELECT in_movement_id "
                    + "FROM inventory_location_transfer WHERE id=?",
                    transferId);

            assertNotEquals(outId, inId);

            amount("95.6000",
                    "SELECT total_cost FROM inventory_movement WHERE id=?",
                    outId);
            amount("95.6000",
                    "SELECT total_cost FROM inventory_movement WHERE id=?",
                    inId);

            amount("12.000",
                    "SELECT SUM(allocated_qty) FROM "
                    + "inventory_location_transfer_allocation "
                    + "WHERE transfer_id=?", transferId);

            amount("95.6000",
                    "SELECT SUM(allocated_qty * unit_cost) "
                    + "FROM inventory_location_transfer_allocation "
                    + "WHERE transfer_id=?", transferId);

            assertEquals(2L, count(
                    "SELECT COUNT(*) FROM inventory_layer "
                    + "WHERE source_movement_id=?", inId));

            assertEquals(2L, count(
                    "SELECT COUNT(*) FROM inventory_layer_consumption "
                    + "WHERE out_movement_id=?", outId));

            assertEquals(2L, count("""
                    SELECT COUNT(*)
                    FROM inventory_location_transfer_allocation a
                    JOIN inventory_layer l ON l.id=a.target_layer_id
                    WHERE a.transfer_id=?
                      AND a.layer_date=l.layer_date
                      AND a.unit_cost=l.unit_cost
                      AND a.allocated_qty=l.original_qty
                      AND l.location_id=?
                    """, transferId, f.targetId()));

            assertEquals(2L, count("""
                    SELECT COUNT(*)
                    FROM inventory_location_transfer_allocation a
                    JOIN inventory_layer_consumption c
                      ON c.layer_id=a.source_layer_id
                    WHERE a.transfer_id=?
                      AND c.out_movement_id=?
                      AND c.consumed_qty=a.allocated_qty
                      AND c.unit_cost=a.unit_cost
                    """, transferId, outId));

            System.out.println(
                    "FIFO_REAL_COMMIT: PASS transferId=" + transferId);

        } finally {
            if (fixture != null) {
                Fixture f = fixture;
                TransactionTemplate cleanup =
                        new TransactionTemplate(transactionManager);

                cleanup.execute(status -> {
                    jdbc.update("""
                            DELETE a
                            FROM inventory_location_transfer_allocation a
                            JOIN inventory_location_transfer t
                              ON t.id=a.transfer_id
                            WHERE t.operation_key=?
                              AND t.tenant_id=?
                              AND t.company_id=?
                            """, f.operationKey(), TENANT, COMPANY);

                    jdbc.update("""
                            DELETE c
                            FROM inventory_layer_consumption c
                            JOIN inventory_movement m
                              ON m.id=c.out_movement_id
                            WHERE m.reference_type='LOCATION_TRANSFER'
                              AND m.tenant_id=?
                              AND m.company_id=?
                              AND m.warehouse_id=?
                            """, TENANT, COMPANY, f.warehouseId());

                    jdbc.update(
                            "DELETE FROM inventory_layer "
                            + "WHERE warehouse_id=? AND location_id=?",
                            f.warehouseId(), f.targetId());

                    jdbc.update(
                            "DELETE FROM inventory_location_transfer "
                            + "WHERE operation_key=? AND tenant_id=? "
                            + "AND company_id=?",
                            f.operationKey(), TENANT, COMPANY);

                    jdbc.update(
                            "DELETE FROM inventory_layer "
                            + "WHERE id IN (?,?)",
                            f.layer1(), f.layer2());

                    jdbc.update(
                            "DELETE FROM inventory_movement "
                            + "WHERE warehouse_id=? AND tenant_id=? "
                            + "AND company_id=?",
                            f.warehouseId(), TENANT, COMPANY);

                    jdbc.update(
                            "DELETE FROM warehouse_location "
                            + "WHERE warehouse_id=?", f.warehouseId());

                    jdbc.update(
                            "DELETE FROM warehouse WHERE id=?",
                            f.warehouseId());

                    jdbc.update(
                            "DELETE FROM item WHERE id=?",
                            f.itemId());

                    return null;
                });
            }
        }
    }

    @Test
    void forcedFailureRollsBackRealMysqlWrites() {
        assertEquals(
                33308,
                jdbc.queryForObject("SELECT @@port", Integer.class)
        );

        String suffix = UUID.randomUUID()
                .toString().replace("-", "").substring(0, 12);

        TransactionTemplate tx =
                new TransactionTemplate(transactionManager);

        Fixture fixture = tx.execute(
                status -> createFixture(suffix)
        );

        assertNotNull(fixture);

        String marker = "FIFO_FORCED_ROLLBACK_" + suffix;

        try {
            IllegalStateException failure = assertThrows(
                    IllegalStateException.class,
                    () -> tx.execute(status -> {

                        Long transferId = writer.write(
                                TENANT,
                                COMPANY,
                                fixture.warehouseId(),
                                fixture.itemId(),
                                fixture.sourceId(),
                                fixture.targetId(),
                                new BigDecimal("12.000"),
                                LocalDate.of(2026, 11, 25),
                                "Rollback reale FIFO sandbox",
                                fixture.operationKey()
                        );

                        assertNotNull(transferId);

                        // Il writer ha scritto su MySQL nella
                        // transazione corrente.
                        assertEquals(1L, count(
                                "SELECT COUNT(*) FROM "
                                + "inventory_location_transfer "
                                + "WHERE operation_key=?",
                                fixture.operationKey()
                        ));

                        assertEquals(2L, count(
                                "SELECT COUNT(*) FROM "
                                + "inventory_movement "
                                + "WHERE reference_type='LOCATION_TRANSFER' "
                                + "AND reference_id=?",
                                transferId
                        ));

                        // Visibili nella stessa transazione:
                        // queste allocazioni verranno annullate.
                        assertEquals(2L, count(
                                "SELECT COUNT(*) FROM "
                                + "inventory_location_transfer_allocation "
                                + "WHERE transfer_id=?",
                                transferId
                        ));

                        throw new IllegalStateException(marker);
                    })
            );

            assertEquals(marker, failure.getMessage());

            // Le query seguenti sono esterne alla
            // transazione che ha subito il rollback.
            assertEquals(0L, count(
                    "SELECT COUNT(*) FROM "
                    + "inventory_location_transfer "
                    + "WHERE operation_key=?",
                    fixture.operationKey()
            ));

            assertEquals(0L, count(
                    "SELECT COUNT(*) FROM "
                    + "inventory_location_transfer_allocation "
                    + "WHERE warehouse_id=? AND tenant_id=? "
                    + "AND company_id=?",
                    fixture.warehouseId(), TENANT, COMPANY
            ));

            assertEquals(0L, count(
                    "SELECT COUNT(*) FROM "
                    + "inventory_layer_consumption "
                    + "WHERE warehouse_id=? AND tenant_id=? "
                    + "AND company_id=?",
                    fixture.warehouseId(), TENANT, COMPANY
            ));

            assertEquals(0L, count(
                    "SELECT COUNT(*) FROM inventory_movement "
                    + "WHERE warehouse_id=? "
                    + "AND reference_type='LOCATION_TRANSFER'",
                    fixture.warehouseId()
            ));

            assertEquals(0L, count(
                    "SELECT COUNT(*) FROM inventory_layer "
                    + "WHERE warehouse_id=? AND location_id=?",
                    fixture.warehouseId(),
                    fixture.targetId()
            ));

            assertEquals(2L, count(
                    "SELECT COUNT(*) FROM inventory_layer "
                    + "WHERE warehouse_id=? AND location_id=?",
                    fixture.warehouseId(),
                    fixture.sourceId()
            ));

            amount("8.000",
                    "SELECT remaining_qty FROM inventory_layer "
                    + "WHERE id=?", fixture.layer1());

            amount("7.000",
                    "SELECT remaining_qty FROM inventory_layer "
                    + "WHERE id=?", fixture.layer2());

            assertEquals(2L, count(
                    "SELECT COUNT(*) FROM inventory_movement "
                    + "WHERE warehouse_id=?",
                    fixture.warehouseId()
            ));

            System.out.println(
                    "FIFO_REAL_ROLLBACK: PASS operationKey="
                    + fixture.operationKey()
            );

        } finally {
            // Prima di eliminare le fixture, accertiamo
            // che non esistano scritture del trasferimento.
            // Se il rollback fallisce, non mascheriamo
            // il problema con una pulizia distruttiva.
            long transfers = count(
                    "SELECT COUNT(*) FROM "
                    + "inventory_location_transfer "
                    + "WHERE warehouse_id=?",
                    fixture.warehouseId()
            );

            long allocations = count(
                    "SELECT COUNT(*) FROM "
                    + "inventory_location_transfer_allocation "
                    + "WHERE warehouse_id=?",
                    fixture.warehouseId()
            );

            long consumptions = count(
                    "SELECT COUNT(*) FROM "
                    + "inventory_layer_consumption "
                    + "WHERE warehouse_id=?",
                    fixture.warehouseId()
            );

            long transferMovements = count(
                    "SELECT COUNT(*) FROM inventory_movement "
                    + "WHERE warehouse_id=? "
                    + "AND reference_type='LOCATION_TRANSFER'",
                    fixture.warehouseId()
            );

            long targetLayers = count(
                    "SELECT COUNT(*) FROM inventory_layer "
                    + "WHERE warehouse_id=? AND location_id=?",
                    fixture.warehouseId(),
                    fixture.targetId()
            );

            if (transfers != 0 || allocations != 0
                    || consumptions != 0
                    || transferMovements != 0
                    || targetLayers != 0) {
                throw new AssertionError(
                        "STOP: rollback incompleto; "
                        + "fixture conservate per diagnosi."
                );
            }

            tx.execute(status -> {
                jdbc.update(
                        "DELETE FROM inventory_layer "
                        + "WHERE id IN (?,?)",
                        fixture.layer1(),
                        fixture.layer2()
                );

                jdbc.update(
                        "DELETE FROM inventory_movement "
                        + "WHERE id IN (?,?)",
                        fixture.movement1(),
                        fixture.movement2()
                );

                jdbc.update(
                        "DELETE FROM warehouse_location "
                        + "WHERE id IN (?,?)",
                        fixture.sourceId(),
                        fixture.targetId()
                );

                jdbc.update(
                        "DELETE FROM warehouse WHERE id=?",
                        fixture.warehouseId()
                );

                jdbc.update(
                        "DELETE FROM item WHERE id=?",
                        fixture.itemId()
                );

                return null;
            });
        }
    }


    @Test
    void inactiveDestinationRollsBackWriterInternalFailure() {
        assertEquals(33308,
                jdbc.queryForObject("SELECT @@port", Integer.class));
        assertEquals(DATABASE,
                jdbc.queryForObject("SELECT DATABASE()", String.class));

        String suffix = UUID.randomUUID()
                .toString().replace("-", "").substring(0, 12);

        TransactionTemplate tx =
                new TransactionTemplate(transactionManager);

        Fixture fixture = tx.execute(
                status -> createFixture(suffix)
        );

        assertNotNull(fixture);

        try {
            // Stato di test commitato prima del posting:
            // sorgente attiva, destinazione inattiva.
            tx.execute(status -> {
                assertEquals(1, jdbc.update(
                        "UPDATE warehouse_location SET active=0 "
                        + "WHERE id=? AND warehouse_id=?",
                        fixture.targetId(),
                        fixture.warehouseId()
                ));
                return null;
            });

            assertEquals(0, jdbc.queryForObject(
                    "SELECT active FROM warehouse_location WHERE id=?",
                    Integer.class, fixture.targetId()));

            // L'eccezione nasce durante la chiamata IN.
            // Il writer ha già salvato il trasferimento
            // PENDING e registrato il movimento OUT.
            com.gestiva.common.exception.BusinessException failure =
                    assertThrows(
                        com.gestiva.common.exception.BusinessException.class,
                        () -> tx.execute(status -> {
                            writer.write(
                                TENANT,
                                COMPANY,
                                fixture.warehouseId(),
                                fixture.itemId(),
                                fixture.sourceId(),
                                fixture.targetId(),
                                new BigDecimal("12.000"),
                                LocalDate.of(2026, 11, 25),
                                "Errore interno FIFO sandbox",
                                fixture.operationKey()
                            );
                            fail("Writer doveva rifiutare ubicazione inattiva.");
                            return null;
                        })
                    );

            assertTrue(
                    failure.getMessage().contains(
                            "ubicazione selezionata non è attiva"),
                    "Eccezione inattesa: " + failure.getMessage()
            );

            // Queste query sono effettuate fuori dalla
            // transazione del writer, ormai terminata.
            assertEquals(0L, count(
                    "SELECT COUNT(*) FROM inventory_location_transfer "
                    + "WHERE operation_key=? AND tenant_id=? "
                    + "AND company_id=?",
                    fixture.operationKey(), TENANT, COMPANY));

            assertEquals(0L, count(
                    "SELECT COUNT(*) FROM "
                    + "inventory_location_transfer_allocation "
                    + "WHERE warehouse_id=?",
                    fixture.warehouseId()));

            assertEquals(0L, count(
                    "SELECT COUNT(*) FROM "
                    + "inventory_layer_consumption "
                    + "WHERE warehouse_id=?",
                    fixture.warehouseId()));

            assertEquals(0L, count(
                    "SELECT COUNT(*) FROM inventory_movement "
                    + "WHERE warehouse_id=? "
                    + "AND reference_type='LOCATION_TRANSFER'",
                    fixture.warehouseId()));

            assertEquals(0L, count(
                    "SELECT COUNT(*) FROM inventory_layer "
                    + "WHERE warehouse_id=? AND location_id=?",
                    fixture.warehouseId(), fixture.targetId()));

            assertEquals(2L, count(
                    "SELECT COUNT(*) FROM inventory_movement "
                    + "WHERE warehouse_id=?",
                    fixture.warehouseId()));

            assertEquals(2L, count(
                    "SELECT COUNT(*) FROM inventory_layer "
                    + "WHERE warehouse_id=?",
                    fixture.warehouseId()));

            amount("8.000",
                    "SELECT remaining_qty FROM inventory_layer WHERE id=?",
                    fixture.layer1());

            amount("7.000",
                    "SELECT remaining_qty FROM inventory_layer WHERE id=?",
                    fixture.layer2());

            amount("124.1000",
                    "SELECT SUM(remaining_qty * unit_cost) "
                    + "FROM inventory_layer WHERE warehouse_id=?",
                    fixture.warehouseId());

            System.out.println("FIFO_INTERNAL_ROLLBACK: PASS");

        } finally {
            // Se rimangono scritture del trasferimento,
            // preserviamo le evidenze per la diagnosi.
            long transfers = count(
                    "SELECT COUNT(*) FROM inventory_location_transfer "
                    + "WHERE warehouse_id=?", fixture.warehouseId());

            long allocations = count(
                    "SELECT COUNT(*) FROM "
                    + "inventory_location_transfer_allocation "
                    + "WHERE warehouse_id=?", fixture.warehouseId());

            long consumptions = count(
                    "SELECT COUNT(*) FROM "
                    + "inventory_layer_consumption "
                    + "WHERE warehouse_id=?", fixture.warehouseId());

            long transferMovements = count(
                    "SELECT COUNT(*) FROM inventory_movement "
                    + "WHERE warehouse_id=? "
                    + "AND reference_type='LOCATION_TRANSFER'",
                    fixture.warehouseId());

            long targetLayers = count(
                    "SELECT COUNT(*) FROM inventory_layer "
                    + "WHERE warehouse_id=? AND location_id=?",
                    fixture.warehouseId(), fixture.targetId());

            if (transfers != 0 || allocations != 0 ||
                    consumptions != 0 || transferMovements != 0 ||
                    targetLayers != 0) {
                throw new AssertionError(
                        "STOP: rollback interno incompleto. "
                        + "Fixture conservate per diagnosi.");
            }

            tx.execute(status -> {
                jdbc.update(
                        "DELETE FROM inventory_layer "
                        + "WHERE id IN (?,?)",
                        fixture.layer1(), fixture.layer2());

                jdbc.update(
                        "DELETE FROM inventory_movement "
                        + "WHERE id IN (?,?)",
                        fixture.movement1(), fixture.movement2());

                jdbc.update(
                        "DELETE FROM warehouse_location "
                        + "WHERE id IN (?,?)",
                        fixture.sourceId(), fixture.targetId());

                jdbc.update(
                        "DELETE FROM warehouse WHERE id=?",
                        fixture.warehouseId());

                jdbc.update(
                        "DELETE FROM item WHERE id=?",
                        fixture.itemId());

                return null;
            });
        }
    }


    @Test
    void otherCompanyCannotTransferDemoWarehouseStock() {
        assertEquals(33308,
                jdbc.queryForObject("SELECT @@port", Integer.class));
        assertEquals(DATABASE,
                jdbc.queryForObject("SELECT DATABASE()", String.class));

        // Entrambe le società appartengono al tenant 2.
        assertEquals(2L, count(
                "SELECT COUNT(*) FROM company "
                + "WHERE tenant_id=? AND id IN (1,2) AND active=1",
                TENANT));

        String suffix = UUID.randomUUID()
                .toString().replace("-", "").substring(0, 12);

        TransactionTemplate tx =
                new TransactionTemplate(transactionManager);

        Fixture fixture = tx.execute(
                status -> createFixture(suffix)
        );

        assertNotNull(fixture);

        try {
            // Il magazzino e le scorte appartengono a
            // COMPANY=1, non alla società TEST2=2.
            com.gestiva.common.exception.BusinessException failure =
                    assertThrows(
                        com.gestiva.common.exception.BusinessException.class,
                        () -> tx.execute(status -> {
                            writer.write(
                                TENANT,
                                2L,
                                fixture.warehouseId(),
                                fixture.itemId(),
                                fixture.sourceId(),
                                fixture.targetId(),
                                new BigDecimal("12.000"),
                                LocalDate.of(2026, 11, 25),
                                "Tentativo cross-company FIFO",
                                fixture.operationKey()
                            );
                            return null;
                        })
                    );

            assertTrue(
                    failure.getMessage().contains(
                            "Magazzino non trovato"),
                    "Eccezione inattesa: " + failure.getMessage());

            // Controlli dopo la transazione fallita.
            assertEquals(0L, count(
                    "SELECT COUNT(*) FROM inventory_location_transfer "
                    + "WHERE operation_key=?",
                    fixture.operationKey()));

            assertEquals(0L, count(
                    "SELECT COUNT(*) FROM "
                    + "inventory_location_transfer_allocation "
                    + "WHERE warehouse_id=?",
                    fixture.warehouseId()));

            assertEquals(0L, count(
                    "SELECT COUNT(*) FROM "
                    + "inventory_layer_consumption "
                    + "WHERE warehouse_id=?",
                    fixture.warehouseId()));

            assertEquals(0L, count(
                    "SELECT COUNT(*) FROM inventory_movement "
                    + "WHERE warehouse_id=? "
                    + "AND reference_type='LOCATION_TRANSFER'",
                    fixture.warehouseId()));

            assertEquals(0L, count(
                    "SELECT COUNT(*) FROM inventory_layer "
                    + "WHERE warehouse_id=? AND location_id=?",
                    fixture.warehouseId(),
                    fixture.targetId()));

            assertEquals(2L, count(
                    "SELECT COUNT(*) FROM inventory_movement "
                    + "WHERE warehouse_id=?",
                    fixture.warehouseId()));

            amount("8.000",
                    "SELECT remaining_qty FROM inventory_layer "
                    + "WHERE id=?", fixture.layer1());

            amount("7.000",
                    "SELECT remaining_qty FROM inventory_layer "
                    + "WHERE id=?", fixture.layer2());

            amount("124.1000",
                    "SELECT SUM(remaining_qty * unit_cost) "
                    + "FROM inventory_layer WHERE warehouse_id=?",
                    fixture.warehouseId());

            System.out.println("FIFO_COMPANY_ISOLATION: PASS");

        } finally {
            // Non cancelliamo eventuali evidenze
            // di una violazione dell'isolamento.
            long transfers = count(
                    "SELECT COUNT(*) FROM inventory_location_transfer "
                    + "WHERE warehouse_id=?",
                    fixture.warehouseId());

            long allocations = count(
                    "SELECT COUNT(*) FROM "
                    + "inventory_location_transfer_allocation "
                    + "WHERE warehouse_id=?",
                    fixture.warehouseId());

            long consumptions = count(
                    "SELECT COUNT(*) FROM "
                    + "inventory_layer_consumption "
                    + "WHERE warehouse_id=?",
                    fixture.warehouseId());

            long transferMovements = count(
                    "SELECT COUNT(*) FROM inventory_movement "
                    + "WHERE warehouse_id=? "
                    + "AND reference_type='LOCATION_TRANSFER'",
                    fixture.warehouseId());

            long targetLayers = count(
                    "SELECT COUNT(*) FROM inventory_layer "
                    + "WHERE warehouse_id=? AND location_id=?",
                    fixture.warehouseId(),
                    fixture.targetId());

            if (transfers != 0 || allocations != 0
                    || consumptions != 0
                    || transferMovements != 0
                    || targetLayers != 0) {
                throw new AssertionError(
                        "STOP: violazione isolamento. "
                        + "Fixture conservate per diagnosi.");
            }

            tx.execute(status -> {
                jdbc.update(
                        "DELETE FROM inventory_layer "
                        + "WHERE id IN (?,?)",
                        fixture.layer1(),
                        fixture.layer2());

                jdbc.update(
                        "DELETE FROM inventory_movement "
                        + "WHERE id IN (?,?)",
                        fixture.movement1(),
                        fixture.movement2());

                jdbc.update(
                        "DELETE FROM warehouse_location "
                        + "WHERE id IN (?,?)",
                        fixture.sourceId(),
                        fixture.targetId());

                jdbc.update(
                        "DELETE FROM warehouse WHERE id=?",
                        fixture.warehouseId());

                jdbc.update(
                        "DELETE FROM item WHERE id=?",
                        fixture.itemId());

                return null;
            });
        }
    }


    @Test
    void concurrentTransfersCannotConsumeSameFifoStockTwice()
            throws Exception {

        assertEquals(33308,
                jdbc.queryForObject("SELECT @@port", Integer.class));

        String suffix = UUID.randomUUID()
                .toString().replace("-", "").substring(0, 12);

        TransactionTemplate setupTx =
                new TransactionTemplate(transactionManager);

        Fixture fixture = setupTx.execute(
                status -> createFixture(suffix)
        );

        assertNotNull(fixture);

        String firstKey = fixture.operationKey() + "-A";
        String secondKey = fixture.operationKey() + "-B";

        java.util.concurrent.ExecutorService pool =
                java.util.concurrent.Executors.newFixedThreadPool(2);

        java.util.concurrent.CountDownLatch ready =
                new java.util.concurrent.CountDownLatch(2);

        java.util.concurrent.CountDownLatch start =
                new java.util.concurrent.CountDownLatch(1);

        boolean verified = false;

        try {
            java.util.function.Function<String,
                    java.util.concurrent.Callable<Object>> taskForKey =
                    operationKey -> () -> {

                        ready.countDown();

                        if (!start.await(
                                15,
                                java.util.concurrent.TimeUnit.SECONDS)) {
                            throw new IllegalStateException(
                                    "Timeout barriera concorrenza.");
                        }

                        TransactionTemplate workerTx =
                                new TransactionTemplate(
                                        transactionManager);

                        try {
                            return workerTx.execute(status ->
                                    writer.write(
                                            TENANT,
                                            COMPANY,
                                            fixture.warehouseId(),
                                            fixture.itemId(),
                                            fixture.sourceId(),
                                            fixture.targetId(),
                                            new BigDecimal("12.000"),
                                            LocalDate.of(2026, 11, 25),
                                            "Concorrenza FIFO " + operationKey,
                                            operationKey
                                    )
                            );
                        } catch (RuntimeException failure) {
                            return failure;
                        }
                    };

            java.util.concurrent.Future<Object> first =
                    pool.submit(taskForKey.apply(firstKey));

            java.util.concurrent.Future<Object> second =
                    pool.submit(taskForKey.apply(secondKey));

            assertTrue(
                    ready.await(
                            15,
                            java.util.concurrent.TimeUnit.SECONDS),
                    "Entrambi i thread devono raggiungere la barriera.");

            // Le due chiamate partono dopo che entrambi
            // i thread hanno raggiunto la stessa barriera.
            start.countDown();

            Object resultA = first.get(
                    45,
                    java.util.concurrent.TimeUnit.SECONDS);

            Object resultB = second.get(
                    45,
                    java.util.concurrent.TimeUnit.SECONDS);

            long successes =
                    (resultA instanceof Long ? 1 : 0)
                    + (resultB instanceof Long ? 1 : 0);

            long failures =
                    (resultA instanceof RuntimeException ? 1 : 0)
                    + (resultB instanceof RuntimeException ? 1 : 0);

            assertEquals(1L, successes,
                    "Deve riuscire un solo trasferimento.");
            assertEquals(1L, failures,
                    "Il secondo trasferimento deve fallire.");

            Object failure = resultA instanceof RuntimeException
                    ? resultA : resultB;

            assertInstanceOf(
                    com.gestiva.common.exception.BusinessException.class,
                    failure,
                    "Fallimento atteso: disponibilità FIFO insufficiente.");

            Object success = resultA instanceof Long
                    ? resultA : resultB;

            Long successfulTransferId = (Long) success;

            assertNotNull(successfulTransferId);

            // Verifiche dopo il commit delle due transazioni.
            assertEquals(1L, count(
                    "SELECT COUNT(*) FROM inventory_location_transfer "
                    + "WHERE warehouse_id=? AND status='POSTED'",
                    fixture.warehouseId()));

            assertEquals(0L, count(
                    "SELECT COUNT(*) FROM inventory_location_transfer "
                    + "WHERE warehouse_id=? AND status<>'POSTED'",
                    fixture.warehouseId()));

            assertEquals(1L, count(
                    "SELECT COUNT(*) FROM inventory_location_transfer "
                    + "WHERE warehouse_id=? AND id=?",
                    fixture.warehouseId(),
                    successfulTransferId));

            assertEquals(2L, count(
                    "SELECT COUNT(*) FROM "
                    + "inventory_location_transfer_allocation "
                    + "WHERE transfer_id=?",
                    successfulTransferId));

            assertEquals(2L, count(
                    "SELECT COUNT(*) FROM "
                    + "inventory_layer_consumption "
                    + "WHERE warehouse_id=?",
                    fixture.warehouseId()));

            assertEquals(2L, count(
                    "SELECT COUNT(*) FROM inventory_movement "
                    + "WHERE warehouse_id=? "
                    + "AND reference_type='LOCATION_TRANSFER'",
                    fixture.warehouseId()));

            amount("0.000",
                    "SELECT remaining_qty FROM inventory_layer "
                    + "WHERE id=?", fixture.layer1());

            amount("3.000",
                    "SELECT remaining_qty FROM inventory_layer "
                    + "WHERE id=?", fixture.layer2());

            amount("3.000",
                    "SELECT SUM(remaining_qty) FROM inventory_layer "
                    + "WHERE warehouse_id=? AND location_id=?",
                    fixture.warehouseId(),
                    fixture.sourceId());

            amount("12.000",
                    "SELECT SUM(remaining_qty) FROM inventory_layer "
                    + "WHERE warehouse_id=? AND location_id=?",
                    fixture.warehouseId(),
                    fixture.targetId());

            amount("15.000",
                    "SELECT SUM(remaining_qty) FROM inventory_layer "
                    + "WHERE warehouse_id=?",
                    fixture.warehouseId());

            assertEquals(0L, count(
                    "SELECT COUNT(*) FROM inventory_layer "
                    + "WHERE warehouse_id=? AND remaining_qty<0",
                    fixture.warehouseId()));

            amount("95.6000",
                    "SELECT SUM(allocated_qty * unit_cost) "
                    + "FROM inventory_location_transfer_allocation "
                    + "WHERE transfer_id=?",
                    successfulTransferId);

            verified = true;

            System.out.println(
                    "FIFO_REAL_CONCURRENCY: PASS transferId="
                    + successfulTransferId);

        } finally {
            start.countDown();
            pool.shutdownNow();

            // In caso di anomalia manteniamo le fixture,
            // così non cancelliamo le prove del problema.
            if (verified) {
                TransactionTemplate cleanup =
                        new TransactionTemplate(transactionManager);

                cleanup.execute(status -> {
                    jdbc.update("""
                            DELETE a
                            FROM inventory_location_transfer_allocation a
                            JOIN inventory_location_transfer t
                              ON t.id=a.transfer_id
                            WHERE t.warehouse_id=?
                              AND t.tenant_id=?
                              AND t.company_id=?
                            """,
                            fixture.warehouseId(), TENANT, COMPANY);

                    jdbc.update("""
                            DELETE c
                            FROM inventory_layer_consumption c
                            JOIN inventory_movement m
                              ON m.id=c.out_movement_id
                            WHERE m.reference_type='LOCATION_TRANSFER'
                              AND m.warehouse_id=?
                              AND m.tenant_id=?
                              AND m.company_id=?
                            """,
                            fixture.warehouseId(), TENANT, COMPANY);

                    jdbc.update(
                            "DELETE FROM inventory_layer "
                            + "WHERE warehouse_id=? AND location_id=?",
                            fixture.warehouseId(), fixture.targetId());

                    jdbc.update(
                            "DELETE FROM inventory_location_transfer "
                            + "WHERE warehouse_id=? AND tenant_id=? "
                            + "AND company_id=?",
                            fixture.warehouseId(), TENANT, COMPANY);

                    jdbc.update(
                            "DELETE FROM inventory_layer "
                            + "WHERE id IN (?,?)",
                            fixture.layer1(), fixture.layer2());

                    jdbc.update(
                            "DELETE FROM inventory_movement "
                            + "WHERE warehouse_id=? AND tenant_id=? "
                            + "AND company_id=?",
                            fixture.warehouseId(), TENANT, COMPANY);

                    jdbc.update(
                            "DELETE FROM warehouse_location "
                            + "WHERE id IN (?,?)",
                            fixture.sourceId(), fixture.targetId());

                    jdbc.update(
                            "DELETE FROM warehouse WHERE id=?",
                            fixture.warehouseId());

                    jdbc.update(
                            "DELETE FROM item WHERE id=?",
                            fixture.itemId());

                    return null;
                });
            }
        }
    }
}
