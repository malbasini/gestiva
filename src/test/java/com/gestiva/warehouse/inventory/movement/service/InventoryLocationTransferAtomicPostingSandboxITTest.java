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

    @Autowired
    InventoryLocationTransferAtomicPostingCoordinator coordinator;


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


    @Test
    void warehouseLockBlocksTransferUntilRelease() throws Exception {
        assertEquals(33308,
                jdbc.queryForObject("SELECT @@port", Integer.class));
        assertEquals(DATABASE,
                jdbc.queryForObject("SELECT DATABASE()", String.class));

        String suffix = UUID.randomUUID()
                .toString().replace("-", "").substring(0, 12);

        TransactionTemplate setupTx =
                new TransactionTemplate(transactionManager);

        Fixture fixture = setupTx.execute(
                status -> createFixture(suffix));

        assertNotNull(fixture);

        java.util.concurrent.ExecutorService pool =
                java.util.concurrent.Executors.newFixedThreadPool(2);

        java.util.concurrent.CountDownLatch locked =
                new java.util.concurrent.CountDownLatch(1);

        java.util.concurrent.CountDownLatch release =
                new java.util.concurrent.CountDownLatch(1);

        java.util.concurrent.CountDownLatch posting =
                new java.util.concurrent.CountDownLatch(1);

        java.util.concurrent.Future<Long> holder = null;
        java.util.concurrent.Future<Long> poster = null;
        boolean verified = false;

        try {
            // Transazione A: mantiene il lock esclusivo
            // del magazzino senza effettuare il posting.
            holder = pool.submit(() -> {
                TransactionTemplate tx =
                        new TransactionTemplate(transactionManager);

                return tx.execute(status -> {
                    Long warehouseId = jdbc.queryForObject(
                        "SELECT id FROM warehouse "
                        + "WHERE id=? AND tenant_id=? "
                        + "AND company_id=? FOR UPDATE",
                        Long.class,
                        fixture.warehouseId(), TENANT, COMPANY);

                    assertEquals(
                            fixture.warehouseId(),
                            warehouseId.longValue());

                    locked.countDown();

                    try {
                        if (!release.await(
                                20,
                                java.util.concurrent.TimeUnit.SECONDS)) {
                            throw new IllegalStateException(
                                    "Timeout rilascio lock.");
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(
                                "Holder interrotto.", ex);
                    }

                    return warehouseId;
                });
            });

            assertTrue(
                locked.await(
                    10, java.util.concurrent.TimeUnit.SECONDS),
                "Il lock iniziale non è stato acquisito.");

            // Prova indipendente sul database:
            // NOWAIT deve fallire perché A detiene il lock.
            org.springframework.dao.DataAccessException nowait =
                assertThrows(
                    org.springframework.dao.DataAccessException.class,
                    () -> jdbc.queryForObject(
                        "SELECT id FROM warehouse "
                        + "WHERE id=? FOR UPDATE NOWAIT",
                        Long.class, fixture.warehouseId())
                );

            Throwable cause = nowait.getMostSpecificCause();

            assertInstanceOf(
                java.sql.SQLException.class,
                cause,
                "Atteso errore MySQL sul lock NOWAIT.");

            assertEquals(
                3572,
                ((java.sql.SQLException) cause).getErrorCode(),
                "Errore MySQL inatteso: non è lock NOWAIT.");

            // Transazione B: tenta il posting completo
            // mentre A trattiene il lock sul magazzino.
            poster = pool.submit(() -> {
                TransactionTemplate tx =
                        new TransactionTemplate(transactionManager);

                posting.countDown();

                return tx.execute(status ->
                    writer.write(
                        TENANT,
                        COMPANY,
                        fixture.warehouseId(),
                        fixture.itemId(),
                        fixture.sourceId(),
                        fixture.targetId(),
                        new BigDecimal("12.000"),
                        LocalDate.of(2026, 11, 25),
                        "Contesa lock MySQL FIFO sandbox",
                        fixture.operationKey()
                    )
                );
            });

            assertTrue(
                posting.await(
                    10, java.util.concurrent.TimeUnit.SECONDS),
                "Il secondo thread non è partito.");

            java.util.concurrent.Future<Long> pendingPoster = poster;

            // Per almeno un secondo B non deve riuscire
            // a terminare mentre A mantiene il lock.
            assertThrows(
                java.util.concurrent.TimeoutException.class,
                () -> pendingPoster.get(
                    1, java.util.concurrent.TimeUnit.SECONDS),
                "Il writer ha terminato con lock ancora detenuto.");

            // Nessun trasferimento committato prima
            // che A rilasci il lock.
            assertEquals(0L, count(
                "SELECT COUNT(*) FROM inventory_location_transfer "
                + "WHERE operation_key=?",
                fixture.operationKey()));

            release.countDown();

            assertEquals(
                fixture.warehouseId(),
                holder.get(
                    15, java.util.concurrent.TimeUnit.SECONDS)
                    .longValue());

            Long transferId = poster.get(
                25, java.util.concurrent.TimeUnit.SECONDS);

            assertNotNull(transferId);

            assertEquals("POSTED",
                jdbc.queryForObject(
                    "SELECT status FROM inventory_location_transfer "
                    + "WHERE id=?",
                    String.class, transferId));

            assertEquals(2L, count(
                "SELECT COUNT(*) FROM "
                + "inventory_location_transfer_allocation "
                + "WHERE transfer_id=?",
                transferId));

            amount("0.000",
                "SELECT remaining_qty FROM inventory_layer WHERE id=?",
                fixture.layer1());

            amount("3.000",
                "SELECT remaining_qty FROM inventory_layer WHERE id=?",
                fixture.layer2());

            amount("12.000",
                "SELECT SUM(remaining_qty) FROM inventory_layer "
                + "WHERE warehouse_id=? AND location_id=?",
                fixture.warehouseId(), fixture.targetId());

            amount("15.000",
                "SELECT SUM(remaining_qty) FROM inventory_layer "
                + "WHERE warehouse_id=?",
                fixture.warehouseId());

            amount("95.6000",
                "SELECT SUM(allocated_qty * unit_cost) "
                + "FROM inventory_location_transfer_allocation "
                + "WHERE transfer_id=?",
                transferId);

            verified = true;

            System.out.println(
                "FIFO_REAL_LOCK_WAIT: PASS transferId=" + transferId);

        } finally {
            release.countDown();

            // Non cancellare fixture mentre un thread
            // potrebbe ancora avere una transazione attiva.
            if (holder != null && !holder.isDone()) {
                try {
                    holder.get(
                        10, java.util.concurrent.TimeUnit.SECONDS);
                } catch (Exception ignored) {
                    // Anomalia: preserviamo le fixture.
                }
            }

            if (poster != null && !poster.isDone()) {
                try {
                    poster.get(
                        10, java.util.concurrent.TimeUnit.SECONDS);
                } catch (Exception ignored) {
                    // Anomalia: preserviamo le fixture.
                }
            }

            pool.shutdown();

            boolean terminated = pool.awaitTermination(
                10, java.util.concurrent.TimeUnit.SECONDS);

            if (!terminated) {
                pool.shutdownNow();
            }

            // Si pulisce soltanto in caso di verifica
            // completa e assenza di transazioni attive.
            if (verified && terminated) {
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
                        """, fixture.warehouseId(), TENANT, COMPANY);

                    jdbc.update("""
                        DELETE c
                        FROM inventory_layer_consumption c
                        JOIN inventory_movement m
                          ON m.id=c.out_movement_id
                        WHERE m.reference_type='LOCATION_TRANSFER'
                          AND m.warehouse_id=?
                          AND m.tenant_id=?
                          AND m.company_id=?
                        """, fixture.warehouseId(), TENANT, COMPANY);

                    jdbc.update(
                        "DELETE FROM inventory_layer "
                        + "WHERE warehouse_id=? AND location_id=?",
                        fixture.warehouseId(), fixture.targetId());

                    jdbc.update(
                        "DELETE FROM inventory_location_transfer "
                        + "WHERE operation_key=? AND tenant_id=? "
                        + "AND company_id=?",
                        fixture.operationKey(), TENANT, COMPANY);

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

            if (!terminated) {
                throw new AssertionError(
                    "STOP: worker ancora attivi; "
                    + "fixture conservate per diagnosi.");
            }
        }
    }


    @Test
    void duplicateOperationKeyCannotPostTwice() {
        assertEquals(33308,
                jdbc.queryForObject("SELECT @@port", Integer.class));
        assertEquals(DATABASE,
                jdbc.queryForObject("SELECT DATABASE()", String.class));

        String suffix = UUID.randomUUID()
                .toString().replace("-", "").substring(0, 12);

        TransactionTemplate tx =
                new TransactionTemplate(transactionManager);

        Fixture fixture = tx.execute(
                status -> createFixture(suffix));

        assertNotNull(fixture);

        boolean verified = false;

        try {
            // Prima richiesta: viene registrata e committata.
            Long transferId = tx.execute(status ->
                writer.write(
                    TENANT,
                    COMPANY,
                    fixture.warehouseId(),
                    fixture.itemId(),
                    fixture.sourceId(),
                    fixture.targetId(),
                    new BigDecimal("12.000"),
                    LocalDate.of(2026, 11, 25),
                    "Idempotenza FIFO - prima richiesta",
                    fixture.operationKey()
                )
            );

            assertNotNull(transferId);

            assertEquals("POSTED",
                jdbc.queryForObject(
                    "SELECT status FROM inventory_location_transfer "
                    + "WHERE id=?",
                    String.class, transferId));

            // Seconda richiesta: stessa chiave, stessi parametri.
            // Il writer deve rifiutarla senza creare un
            // secondo trasferimento.
            com.gestiva.common.exception.BusinessException failure =
                assertThrows(
                    com.gestiva.common.exception.BusinessException.class,
                    () -> tx.execute(status ->
                        writer.write(
                            TENANT,
                            COMPANY,
                            fixture.warehouseId(),
                            fixture.itemId(),
                            fixture.sourceId(),
                            fixture.targetId(),
                            new BigDecimal("12.000"),
                            LocalDate.of(2026, 11, 25),
                            "Idempotenza FIFO - prima richiesta",
                            fixture.operationKey()
                        )
                    )
                );

            assertTrue(
                failure.getMessage().contains(
                    "Chiave di trasferimento già utilizzata"),
                "Eccezione inattesa: " + failure.getMessage());

            // Stato persistito dopo entrambi i tentativi.
            assertEquals(1L, count(
                "SELECT COUNT(*) FROM inventory_location_transfer "
                + "WHERE tenant_id=? AND company_id=? "
                + "AND operation_key=?",
                TENANT, COMPANY, fixture.operationKey()));

            assertEquals(1L, count(
                "SELECT COUNT(*) FROM inventory_location_transfer "
                + "WHERE warehouse_id=? AND status='POSTED'",
                fixture.warehouseId()));

            assertEquals(2L, count(
                "SELECT COUNT(*) FROM "
                + "inventory_location_transfer_allocation "
                + "WHERE transfer_id=?",
                transferId));

            assertEquals(2L, count(
                "SELECT COUNT(*) FROM inventory_layer_consumption "
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
                fixture.warehouseId(), fixture.sourceId());

            amount("12.000",
                "SELECT SUM(remaining_qty) FROM inventory_layer "
                + "WHERE warehouse_id=? AND location_id=?",
                fixture.warehouseId(), fixture.targetId());

            amount("15.000",
                "SELECT SUM(remaining_qty) FROM inventory_layer "
                + "WHERE warehouse_id=?",
                fixture.warehouseId());

            amount("95.6000",
                "SELECT SUM(allocated_qty * unit_cost) "
                + "FROM inventory_location_transfer_allocation "
                + "WHERE transfer_id=?",
                transferId);

            assertEquals(0L, count(
                "SELECT COUNT(*) FROM inventory_layer "
                + "WHERE warehouse_id=? AND remaining_qty<0",
                fixture.warehouseId()));

            verified = true;

            System.out.println(
                "FIFO_REAL_IDEMPOTENCY: PASS transferId=" + transferId);

        } finally {
            // Se il test rivela una violazione, conserviamo
            // tutte le evidenze per la diagnosi.
            if (verified) {
                tx.execute(status -> {
                    jdbc.update("""
                        DELETE a
                        FROM inventory_location_transfer_allocation a
                        JOIN inventory_location_transfer t
                          ON t.id=a.transfer_id
                        WHERE t.operation_key=?
                          AND t.tenant_id=?
                          AND t.company_id=?
                        """,
                        fixture.operationKey(), TENANT, COMPANY);

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
                        + "WHERE operation_key=? AND tenant_id=? "
                        + "AND company_id=?",
                        fixture.operationKey(), TENANT, COMPANY);

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


    @Test
    void concurrentDuplicateOperationKeyPostsOnlyOnce()
            throws Exception {

        assertEquals(33308,
                jdbc.queryForObject("SELECT @@port", Integer.class));
        assertEquals(DATABASE,
                jdbc.queryForObject("SELECT DATABASE()", String.class));

        String suffix = UUID.randomUUID()
                .toString().replace("-", "").substring(0, 12);

        TransactionTemplate setup =
                new TransactionTemplate(transactionManager);

        Fixture fixture = setup.execute(
                status -> createFixture(suffix));

        assertNotNull(fixture);

        java.util.concurrent.ExecutorService pool =
                java.util.concurrent.Executors.newFixedThreadPool(2);

        java.util.concurrent.CountDownLatch ready =
                new java.util.concurrent.CountDownLatch(2);

        java.util.concurrent.CountDownLatch start =
                new java.util.concurrent.CountDownLatch(1);

        java.util.concurrent.Future<Object> first = null;
        java.util.concurrent.Future<Object> second = null;

        boolean verified = false;

        try {
            java.util.concurrent.Callable<Object> task = () -> {
                ready.countDown();

                if (!start.await(
                        15,
                        java.util.concurrent.TimeUnit.SECONDS)) {
                    throw new IllegalStateException(
                            "Timeout barriera idempotenza.");
                }

                TransactionTemplate tx =
                        new TransactionTemplate(transactionManager);

                try {
                    return tx.execute(status ->
                        writer.write(
                            TENANT,
                            COMPANY,
                            fixture.warehouseId(),
                            fixture.itemId(),
                            fixture.sourceId(),
                            fixture.targetId(),
                            new BigDecimal("12.000"),
                            LocalDate.of(2026, 11, 25),
                            "Idempotenza concorrente FIFO",
                            fixture.operationKey()
                        )
                    );
                } catch (RuntimeException exception) {
                    return exception;
                }
            };

            first = pool.submit(task);
            second = pool.submit(task);

            assertTrue(
                ready.await(
                    15, java.util.concurrent.TimeUnit.SECONDS),
                "I due worker non hanno raggiunto la barriera.");

            start.countDown();

            Object resultA = first.get(
                45, java.util.concurrent.TimeUnit.SECONDS);

            Object resultB = second.get(
                45, java.util.concurrent.TimeUnit.SECONDS);

            long completed =
                    (resultA instanceof Long ? 1 : 0)
                    + (resultB instanceof Long ? 1 : 0);

            long rejected =
                    (resultA instanceof RuntimeException ? 1 : 0)
                    + (resultB instanceof RuntimeException ? 1 : 0);

            assertEquals(1L, completed,
                    "Deve essere registrato un solo trasferimento.");

            assertEquals(1L, rejected,
                    "Una richiesta duplicata deve essere respinta.");

            Object rejection =
                    resultA instanceof RuntimeException
                        ? resultA : resultB;

            assertInstanceOf(
                com.gestiva.common.exception.BusinessException.class,
                rejection,
                "Atteso rifiuto per chiave duplicata.");

            assertTrue(
                ((RuntimeException) rejection).getMessage()
                    .contains("Chiave di trasferimento già utilizzata"),
                "Motivo del rifiuto diverso dall'idempotenza.");

            Long transferId =
                    (Long) (resultA instanceof Long ? resultA : resultB);

            assertNotNull(transferId);

            // Verifiche indipendenti dopo entrambi i commit/rollback.
            assertEquals(1L, count(
                "SELECT COUNT(*) FROM inventory_location_transfer "
                + "WHERE tenant_id=? AND company_id=? "
                + "AND operation_key=?",
                TENANT, COMPANY, fixture.operationKey()));

            assertEquals(1L, count(
                "SELECT COUNT(*) FROM inventory_location_transfer "
                + "WHERE warehouse_id=? AND status='POSTED'",
                fixture.warehouseId()));

            assertEquals(0L, count(
                "SELECT COUNT(*) FROM inventory_location_transfer "
                + "WHERE warehouse_id=? AND status<>'POSTED'",
                fixture.warehouseId()));

            assertEquals("POSTED",
                jdbc.queryForObject(
                    "SELECT status FROM inventory_location_transfer "
                    + "WHERE id=?",
                    String.class, transferId));

            assertEquals(2L, count(
                "SELECT COUNT(*) FROM "
                + "inventory_location_transfer_allocation "
                + "WHERE transfer_id=?",
                transferId));

            assertEquals(2L, count(
                "SELECT COUNT(*) FROM inventory_layer_consumption "
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
                fixture.warehouseId(), fixture.sourceId());

            amount("12.000",
                "SELECT SUM(remaining_qty) FROM inventory_layer "
                + "WHERE warehouse_id=? AND location_id=?",
                fixture.warehouseId(), fixture.targetId());

            amount("15.000",
                "SELECT SUM(remaining_qty) FROM inventory_layer "
                + "WHERE warehouse_id=?",
                fixture.warehouseId());

            amount("95.6000",
                "SELECT SUM(allocated_qty * unit_cost) "
                + "FROM inventory_location_transfer_allocation "
                + "WHERE transfer_id=?",
                transferId);

            assertEquals(0L, count(
                "SELECT COUNT(*) FROM inventory_layer "
                + "WHERE warehouse_id=? AND remaining_qty<0",
                fixture.warehouseId()));

            verified = true;

            System.out.println(
                "FIFO_CONCURRENT_IDEMPOTENCY: PASS transferId="
                + transferId);

        } finally {
            start.countDown();

            // Nessuna pulizia finché i worker non
            // hanno terminato le rispettive transazioni.
            pool.shutdown();

            boolean terminated = pool.awaitTermination(
                30, java.util.concurrent.TimeUnit.SECONDS);

            if (!terminated) {
                pool.shutdownNow();
            }

            // Se il test fallisce, preserviamo
            // le evidenze per la diagnosi.
            if (verified && terminated) {
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
                        """,
                        fixture.operationKey(), TENANT, COMPANY);

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
                        + "WHERE operation_key=? AND tenant_id=? "
                        + "AND company_id=?",
                        fixture.operationKey(), TENANT, COMPANY);

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

            if (!terminated) {
                throw new AssertionError(
                    "STOP: worker ancora attivi. "
                    + "Fixture conservate per diagnosi.");
            }
        }
    }


    @Test
    void validSecondCompanyWarehouseCannotConsumeDemoStock() {
        assertEquals(33308,
                jdbc.queryForObject("SELECT @@port", Integer.class));
        assertEquals(DATABASE,
                jdbc.queryForObject("SELECT DATABASE()", String.class));

        assertEquals(2L, count(
                "SELECT COUNT(*) FROM company "
                + "WHERE tenant_id=? AND id IN (1,2) AND active=1",
                TENANT));

        String suffix = UUID.randomUUID()
                .toString().replace("-", "").substring(0, 12);

        TransactionTemplate tx =
                new TransactionTemplate(transactionManager);

        // Le scorte iniziali appartengono SOLO alla DEMO.
        Fixture demo = tx.execute(
                status -> createFixture(suffix));

        assertNotNull(demo);

        final String warehouseCode = "W2-" + suffix;
        long[] test2 = null;
        boolean verified = false;

        try {
            // Un magazzino reale, attivo e valido per TEST2.
            test2 = tx.execute(status -> {
                jdbc.update("""
                    INSERT INTO warehouse (
                        tenant_id, company_id, code, name,
                        warehouse_type, allow_negative_stock,
                        active, created_at, updated_at
                    )
                    VALUES (?, 2, ?, ?, 'PHYSICAL',
                            0, 1, NOW(), NOW())
                    """, TENANT, warehouseCode,
                    "Magazzino TEST2 isolamento FIFO");

                long warehouseId = id(
                    "SELECT id FROM warehouse "
                    + "WHERE tenant_id=? AND company_id=2 AND code=?",
                    TENANT, warehouseCode);

                jdbc.update("""
                    INSERT INTO warehouse_location (
                        tenant_id, warehouse_id, code, name,
                        location_type, active,
                        created_at, updated_at
                    )
                    VALUES (?, ?, ?, 'TEST2 Origine',
                            'STORAGE', 1, NOW(), NOW())
                    """, TENANT, warehouseId, "A-" + suffix);

                jdbc.update("""
                    INSERT INTO warehouse_location (
                        tenant_id, warehouse_id, code, name,
                        location_type, active,
                        created_at, updated_at
                    )
                    VALUES (?, ?, ?, 'TEST2 Destinazione',
                            'STORAGE', 1, NOW(), NOW())
                    """, TENANT, warehouseId, "B-" + suffix);

                long sourceId = id(
                    "SELECT id FROM warehouse_location "
                    + "WHERE warehouse_id=? AND code=?",
                    warehouseId, "A-" + suffix);

                long targetId = id(
                    "SELECT id FROM warehouse_location "
                    + "WHERE warehouse_id=? AND code=?",
                    warehouseId, "B-" + suffix);

                return new long[]{
                    warehouseId, sourceId, targetId
                };
            });

            assertNotNull(test2);

            final long warehouse2 = test2[0];
            final long source2 = test2[1];
            final long target2 = test2[2];

            assertEquals(1L, count(
                "SELECT COUNT(*) FROM warehouse "
                + "WHERE id=? AND tenant_id=? AND company_id=2",
                warehouse2, TENANT));

            assertEquals(0L, count(
                "SELECT COUNT(*) FROM inventory_layer "
                + "WHERE warehouse_id=?",
                warehouse2));

            // Tentativo 1: magazzino e ubicazioni TEST2
            // corretti, ma nessuna scorta TEST2 disponibile.
            assertThrows(
                com.gestiva.common.exception.BusinessException.class,
                () -> tx.execute(status ->
                    writer.write(
                        TENANT, 2L,
                        warehouse2,
                        demo.itemId(),
                        source2,
                        target2,
                        new BigDecimal("12.000"),
                        LocalDate.of(2026, 11, 25),
                        "TEST2 senza scorte",
                        demo.operationKey() + "-T2A"
                    )
                )
            );

            // Tentativo 2: magazzino valido TEST2,
            // ma ubicazione sorgente appartenente a DEMO.
            // I layer DEMO non devono essere recuperati.
            assertThrows(
                com.gestiva.common.exception.BusinessException.class,
                () -> tx.execute(status ->
                    writer.write(
                        TENANT, 2L,
                        warehouse2,
                        demo.itemId(),
                        demo.sourceId(),
                        target2,
                        new BigDecimal("12.000"),
                        LocalDate.of(2026, 11, 25),
                        "TEST2 con sorgente DEMO",
                        demo.operationKey() + "-T2B"
                    )
                )
            );

            // Nessuna scrittura di trasferimento per
            // nessuna delle due società o dei due magazzini.
            assertEquals(0L, count(
                "SELECT COUNT(*) FROM inventory_location_transfer "
                + "WHERE warehouse_id IN (?,?)",
                demo.warehouseId(), warehouse2));

            assertEquals(0L, count(
                "SELECT COUNT(*) FROM "
                + "inventory_location_transfer_allocation "
                + "WHERE warehouse_id IN (?,?)",
                demo.warehouseId(), warehouse2));

            assertEquals(0L, count(
                "SELECT COUNT(*) FROM "
                + "inventory_layer_consumption "
                + "WHERE warehouse_id IN (?,?)",
                demo.warehouseId(), warehouse2));

            assertEquals(0L, count(
                "SELECT COUNT(*) FROM inventory_movement "
                + "WHERE warehouse_id IN (?,?) "
                + "AND reference_type='LOCATION_TRANSFER'",
                demo.warehouseId(), warehouse2));

            assertEquals(0L, count(
                "SELECT COUNT(*) FROM inventory_layer "
                + "WHERE warehouse_id=?",
                warehouse2));

            assertEquals(2L, count(
                "SELECT COUNT(*) FROM inventory_layer "
                + "WHERE warehouse_id=? AND company_id=1",
                demo.warehouseId()));

            amount("8.000",
                "SELECT remaining_qty FROM inventory_layer "
                + "WHERE id=?", demo.layer1());

            amount("7.000",
                "SELECT remaining_qty FROM inventory_layer "
                + "WHERE id=?", demo.layer2());

            amount("15.000",
                "SELECT SUM(remaining_qty) FROM inventory_layer "
                + "WHERE warehouse_id=?",
                demo.warehouseId());

            amount("124.1000",
                "SELECT SUM(remaining_qty * unit_cost) "
                + "FROM inventory_layer WHERE warehouse_id=?",
                demo.warehouseId());

            verified = true;

            System.out.println(
                "FIFO_DEEP_COMPANY_ISOLATION: PASS");

        } finally {
            // Un eventuale fallimento conserva entrambe
            // le fixture per la diagnosi.
            if (verified && test2 != null) {
                long[] company2 = test2;

                tx.execute(status -> {
                    // Solo ID appartenenti alle fixture
                    // appena create da questo test.
                    jdbc.update(
                        "DELETE FROM warehouse_location "
                        + "WHERE id IN (?,?) AND warehouse_id=?",
                        company2[1], company2[2],
                        company2[0]);

                    jdbc.update(
                        "DELETE FROM warehouse "
                        + "WHERE id=? AND company_id=2",
                        company2[0]);

                    jdbc.update(
                        "DELETE FROM inventory_layer "
                        + "WHERE id IN (?,?)",
                        demo.layer1(), demo.layer2());

                    jdbc.update(
                        "DELETE FROM inventory_movement "
                        + "WHERE id IN (?,?)",
                        demo.movement1(), demo.movement2());

                    jdbc.update(
                        "DELETE FROM warehouse_location "
                        + "WHERE id IN (?,?)",
                        demo.sourceId(), demo.targetId());

                    jdbc.update(
                        "DELETE FROM warehouse WHERE id=?",
                        demo.warehouseId());

                    jdbc.update(
                        "DELETE FROM item WHERE id=?",
                        demo.itemId());

                    return null;
                });
            }
        }
    }


    @Test
    void foreignDestinationLocationRollsBackCompanyTransfer() {
        assertEquals(33308,
            jdbc.queryForObject("SELECT @@port", Integer.class));
        assertEquals(DATABASE,
            jdbc.queryForObject("SELECT DATABASE()", String.class));

        assertEquals(2L, count(
            "SELECT COUNT(*) FROM company "
            + "WHERE tenant_id=? AND id IN (1,2) AND active=1",
            TENANT));

        String suffix = UUID.randomUUID()
            .toString().replace("-", "").substring(0, 12);

        TransactionTemplate tx =
            new TransactionTemplate(transactionManager);

        // DEMO possiede un magazzino e due layer propri.
        Fixture demo = tx.execute(
            status -> createFixture(suffix));

        assertNotNull(demo);

        long[] test2 = null;
        boolean verified = false;

        try {
            // TEST2 possiede un magazzino distinto,
            // due ubicazioni valide e scorte FIFO reali.
            test2 = tx.execute(status -> {
                String code = "W2-" + suffix;

                jdbc.update("""
                    INSERT INTO warehouse (
                        tenant_id, company_id, code, name,
                        warehouse_type, allow_negative_stock,
                        active, created_at, updated_at
                    )
                    VALUES (?, 2, ?, ?, 'PHYSICAL',
                            0, 1, NOW(), NOW())
                    """,
                    TENANT, code, "TEST2 FIFO rollback destinazione");

                long warehouse2 = id(
                    "SELECT id FROM warehouse "
                    + "WHERE tenant_id=? AND company_id=2 AND code=?",
                    TENANT, code);

                jdbc.update("""
                    INSERT INTO warehouse_location (
                        tenant_id, warehouse_id, code, name,
                        location_type, active,
                        created_at, updated_at
                    )
                    VALUES (?, ?, ?, 'Origine TEST2',
                            'STORAGE', 1, NOW(), NOW())
                    """,
                    TENANT, warehouse2, "A-" + suffix);

                jdbc.update("""
                    INSERT INTO warehouse_location (
                        tenant_id, warehouse_id, code, name,
                        location_type, active,
                        created_at, updated_at
                    )
                    VALUES (?, ?, ?, 'Destinazione TEST2',
                            'STORAGE', 1, NOW(), NOW())
                    """,
                    TENANT, warehouse2, "B-" + suffix);

                long source2 = id(
                    "SELECT id FROM warehouse_location "
                    + "WHERE warehouse_id=? AND code=?",
                    warehouse2, "A-" + suffix);

                long target2 = id(
                    "SELECT id FROM warehouse_location "
                    + "WHERE warehouse_id=? AND code=?",
                    warehouse2, "B-" + suffix);

                long[] movements = new long[2];
                long[] layers = new long[2];

                String[] quantities = {"8.000", "7.000"};
                String[] unitCosts = {"7.2000", "9.5000"};
                String[] totalCosts = {"57.6000", "66.5000"};
                LocalDate[] dates = {
                    LocalDate.of(2026, 10, 1),
                    LocalDate.of(2026, 10, 10)
                };

                for (int i = 0; i < 2; i++) {
                    jdbc.update("""
                        INSERT INTO inventory_movement (
                            tenant_id, company_id, warehouse_id,
                            location_id, item_id, movement_date,
                            movement_type, causal_code, quantity,
                            unit_cost, total_cost, reference_type,
                            reversed, created_at, updated_at
                        )
                        VALUES (?, 2, ?, ?, ?, ?,
                                'IN', 'PURCHASE_RECEIPT',
                                ?, ?, ?, 'FIFO_SANDBOX_TEST',
                                b'0', NOW(), NOW())
                        """,
                        TENANT, warehouse2, source2,
                        demo.itemId(), dates[i],
                        new BigDecimal(quantities[i]),
                        new BigDecimal(unitCosts[i]),
                        new BigDecimal(totalCosts[i]));

                    movements[i] = id(
                        "SELECT id FROM inventory_movement "
                        + "WHERE tenant_id=? AND company_id=2 "
                        + "AND warehouse_id=? AND item_id=? "
                        + "AND movement_date=? "
                        + "AND reference_type='FIFO_SANDBOX_TEST'",
                        TENANT, warehouse2, demo.itemId(), dates[i]);

                    jdbc.update("""
                        INSERT INTO inventory_layer (
                            tenant_id, company_id, warehouse_id,
                            location_id, item_id, source_movement_id,
                            layer_date, original_qty, remaining_qty,
                            unit_cost, closed, created_at, updated_at
                        )
                        VALUES (?, 2, ?, ?, ?, ?, ?, ?, ?, ?,
                                b'0', NOW(), NOW())
                        """,
                        TENANT, warehouse2, source2,
                        demo.itemId(), movements[i], dates[i],
                        new BigDecimal(quantities[i]),
                        new BigDecimal(quantities[i]),
                        new BigDecimal(unitCosts[i]));

                    layers[i] = id(
                        "SELECT id FROM inventory_layer "
                        + "WHERE source_movement_id=?",
                        movements[i]);
                }

                return new long[]{
                    warehouse2, source2, target2,
                    movements[0], movements[1],
                    layers[0], layers[1]
                };
            });

            assertNotNull(test2);

            final long warehouse2 = test2[0];
            final long source2 = test2[1];
            final long target2 = test2[2];
            final long layerA = test2[5];
            final long layerB = test2[6];

            assertEquals(1L, count(
                "SELECT COUNT(*) FROM warehouse "
                + "WHERE id=? AND company_id=2 AND tenant_id=?",
                warehouse2, TENANT));

            assertEquals(1L, count(
                "SELECT COUNT(*) FROM warehouse_location "
                + "WHERE id=? AND warehouse_id=?",
                source2, warehouse2));

            assertEquals(0L, count(
                "SELECT COUNT(*) FROM warehouse_location "
                + "WHERE id=? AND warehouse_id=?",
                demo.targetId(), warehouse2));

            amount("15.000",
                "SELECT SUM(remaining_qty) FROM inventory_layer "
                + "WHERE warehouse_id=? AND company_id=2",
                warehouse2);

            amount("124.1000",
                "SELECT SUM(remaining_qty * unit_cost) "
                + "FROM inventory_layer WHERE warehouse_id=?",
                warehouse2);

            // Origine TEST2 valida e con scorte sufficienti.
            // Destinazione DEMO estranea al magazzino TEST2.
            String key = demo.operationKey() + "-FOREIGN-DEST";

            com.gestiva.common.exception.BusinessException failure =
                assertThrows(
                    com.gestiva.common.exception.BusinessException.class,
                    () -> tx.execute(status ->
                        writer.write(
                            TENANT,
                            2L,
                            warehouse2,
                            demo.itemId(),
                            source2,
                            demo.targetId(),
                            new BigDecimal("12.000"),
                            LocalDate.of(2026, 11, 25),
                            "TEST2 verso ubicazione DEMO",
                            key
                        )
                    )
                );

            assertTrue(
                failure.getMessage().contains(
                    "Ubicazione non trovata nel magazzino"),
                "Eccezione inattesa: " + failure.getMessage());

            // Il trasferimento PENDING e il movimento OUT
            // devono risultare annullati dopo il rollback.
            assertEquals(0L, count(
                "SELECT COUNT(*) FROM inventory_location_transfer "
                + "WHERE warehouse_id IN (?,?)",
                demo.warehouseId(), warehouse2));

            assertEquals(0L, count(
                "SELECT COUNT(*) FROM "
                + "inventory_location_transfer_allocation "
                + "WHERE warehouse_id IN (?,?)",
                demo.warehouseId(), warehouse2));

            assertEquals(0L, count(
                "SELECT COUNT(*) FROM "
                + "inventory_layer_consumption "
                + "WHERE warehouse_id IN (?,?)",
                demo.warehouseId(), warehouse2));

            assertEquals(0L, count(
                "SELECT COUNT(*) FROM inventory_movement "
                + "WHERE warehouse_id IN (?,?) "
                + "AND reference_type='LOCATION_TRANSFER'",
                demo.warehouseId(), warehouse2));

            assertEquals(2L, count(
                "SELECT COUNT(*) FROM inventory_movement "
                + "WHERE warehouse_id=?",
                warehouse2));

            assertEquals(2L, count(
                "SELECT COUNT(*) FROM inventory_layer "
                + "WHERE warehouse_id=? AND company_id=2",
                warehouse2));

            assertEquals(0L, count(
                "SELECT COUNT(*) FROM inventory_layer "
                + "WHERE warehouse_id=? AND location_id=?",
                warehouse2, target2));

            amount("8.000",
                "SELECT remaining_qty FROM inventory_layer "
                + "WHERE id=?", layerA);

            amount("7.000",
                "SELECT remaining_qty FROM inventory_layer "
                + "WHERE id=?", layerB);

            amount("15.000",
                "SELECT SUM(remaining_qty) FROM inventory_layer "
                + "WHERE warehouse_id=?",
                warehouse2);

            amount("124.1000",
                "SELECT SUM(remaining_qty * unit_cost) "
                + "FROM inventory_layer WHERE warehouse_id=?",
                warehouse2);

            // Anche le scorte DEMO devono restare intatte.
            amount("8.000",
                "SELECT remaining_qty FROM inventory_layer "
                + "WHERE id=?", demo.layer1());

            amount("7.000",
                "SELECT remaining_qty FROM inventory_layer "
                + "WHERE id=?", demo.layer2());

            amount("124.1000",
                "SELECT SUM(remaining_qty * unit_cost) "
                + "FROM inventory_layer WHERE warehouse_id=?",
                demo.warehouseId());

            verified = true;

            System.out.println(
                "FIFO_FOREIGN_DESTINATION_ROLLBACK: PASS");

        } finally {
            // In caso di errore lasciamo le fixture:
            // non cancelliamo le evidenze di un
            // eventuale difetto di isolamento.
            if (verified && test2 != null) {
                long[] company2 = test2;

                tx.execute(status -> {
                    jdbc.update(
                        "DELETE FROM inventory_layer "
                        + "WHERE id IN (?,?)",
                        company2[5], company2[6]);

                    jdbc.update(
                        "DELETE FROM inventory_movement "
                        + "WHERE id IN (?,?)",
                        company2[3], company2[4]);

                    jdbc.update(
                        "DELETE FROM warehouse_location "
                        + "WHERE id IN (?,?) AND warehouse_id=?",
                        company2[1], company2[2], company2[0]);

                    jdbc.update(
                        "DELETE FROM warehouse "
                        + "WHERE id=? AND company_id=2",
                        company2[0]);

                    jdbc.update(
                        "DELETE FROM inventory_layer "
                        + "WHERE id IN (?,?)",
                        demo.layer1(), demo.layer2());

                    jdbc.update(
                        "DELETE FROM inventory_movement "
                        + "WHERE id IN (?,?)",
                        demo.movement1(), demo.movement2());

                    jdbc.update(
                        "DELETE FROM warehouse_location "
                        + "WHERE id IN (?,?)",
                        demo.sourceId(), demo.targetId());

                    jdbc.update(
                        "DELETE FROM warehouse WHERE id=?",
                        demo.warehouseId());

                    jdbc.update(
                        "DELETE FROM item WHERE id=?",
                        demo.itemId());

                    return null;
                });
            }
        }
    }


    @Test
    void springCoordinatorPostsThroughTransactionalWriter() {
        assertEquals(33308,
            jdbc.queryForObject("SELECT @@port", Integer.class));
        assertEquals(DATABASE,
            jdbc.queryForObject("SELECT DATABASE()", String.class));

        assertNotNull(coordinator);

        String suffix = UUID.randomUUID()
            .toString().replace("-", "").substring(0, 12);

        TransactionTemplate tx =
            new TransactionTemplate(transactionManager);

        Fixture fixture = tx.execute(
            status -> createFixture(suffix));

        assertNotNull(fixture);
        boolean verified = false;

        try {
            // Nessuna transazione esterna alla chiamata:
            // Spring deve aprire REQUIRED nel coordinator.
            // Il writer partecipa tramite MANDATORY.
            Long transferId = coordinator.post(
                TENANT,
                COMPANY,
                fixture.warehouseId(),
                fixture.itemId(),
                fixture.sourceId(),
                fixture.targetId(),
                new BigDecimal("12.000"),
                LocalDate.of(2026, 11, 25),
                "Collaudo coordinatore Spring",
                fixture.operationKey()
            );

            assertNotNull(transferId);

            assertEquals("POSTED",
                jdbc.queryForObject(
                    "SELECT status "
                    + "FROM inventory_location_transfer "
                    + "WHERE id=? AND tenant_id=? AND company_id=?",
                    String.class,
                    transferId, TENANT, COMPANY));

            assertEquals(1L, count(
                "SELECT COUNT(*) "
                + "FROM inventory_location_transfer "
                + "WHERE tenant_id=? AND company_id=? "
                + "AND operation_key=?",
                TENANT, COMPANY, fixture.operationKey()));

            assertEquals(2L, count(
                "SELECT COUNT(*) FROM inventory_movement "
                + "WHERE warehouse_id=? "
                + "AND reference_type='LOCATION_TRANSFER'",
                fixture.warehouseId()));

            assertEquals(2L, count(
                "SELECT COUNT(*) "
                + "FROM inventory_location_transfer_allocation "
                + "WHERE transfer_id=?",
                transferId));

            assertEquals(2L, count(
                "SELECT COUNT(*) FROM inventory_layer_consumption "
                + "WHERE warehouse_id=?",
                fixture.warehouseId()));

            amount("0.000",
                "SELECT remaining_qty FROM inventory_layer "
                + "WHERE id=?",
                fixture.layer1());

            amount("3.000",
                "SELECT remaining_qty FROM inventory_layer "
                + "WHERE id=?",
                fixture.layer2());

            amount("3.000",
                "SELECT SUM(remaining_qty) FROM inventory_layer "
                + "WHERE warehouse_id=? AND location_id=?",
                fixture.warehouseId(), fixture.sourceId());

            amount("12.000",
                "SELECT SUM(remaining_qty) FROM inventory_layer "
                + "WHERE warehouse_id=? AND location_id=?",
                fixture.warehouseId(), fixture.targetId());

            amount("15.000",
                "SELECT SUM(remaining_qty) FROM inventory_layer "
                + "WHERE warehouse_id=?",
                fixture.warehouseId());

            amount("95.6000",
                "SELECT SUM(allocated_qty * unit_cost) "
                + "FROM inventory_location_transfer_allocation "
                + "WHERE transfer_id=?",
                transferId);

            verified = true;

            System.out.println(
                "FIFO_SPRING_COORDINATOR_POSTING: PASS transferId="
                + transferId);

        } finally {
            // In caso di fallimento le fixture restano
            // disponibili per la diagnosi.
            if (verified) {
                tx.execute(status -> {
                    jdbc.update(
                        "DELETE FROM "
                        + "inventory_location_transfer_allocation "
                        + "WHERE warehouse_id=?",
                        fixture.warehouseId());

                    jdbc.update(
                        "DELETE FROM inventory_layer_consumption "
                        + "WHERE warehouse_id=?",
                        fixture.warehouseId());

                    jdbc.update(
                        "DELETE FROM inventory_layer "
                        + "WHERE warehouse_id=? AND location_id=?",
                        fixture.warehouseId(), fixture.targetId());

                    jdbc.update(
                        "DELETE FROM inventory_location_transfer "
                        + "WHERE warehouse_id=?",
                        fixture.warehouseId());

                    jdbc.update(
                        "DELETE FROM inventory_layer "
                        + "WHERE id IN (?,?)",
                        fixture.layer1(), fixture.layer2());

                    jdbc.update(
                        "DELETE FROM inventory_movement "
                        + "WHERE warehouse_id=?",
                        fixture.warehouseId());

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


    @Test
    void springCoordinatorRollsBackWhenWriterFails() {
        assertEquals(33308,
            jdbc.queryForObject("SELECT @@port", Integer.class));

        assertEquals(DATABASE,
            jdbc.queryForObject("SELECT DATABASE()", String.class));

        assertNotNull(coordinator);

        String suffix = UUID.randomUUID()
            .toString().replace("-", "").substring(0, 12);

        TransactionTemplate tx =
            new TransactionTemplate(transactionManager);

        Fixture fixture = tx.execute(
            status -> createFixture(suffix));

        assertNotNull(fixture);

        boolean verified = false;

        try {
            // La fixture iniziale dispone di 15 PZ.
            amount("8.000",
                "SELECT remaining_qty FROM inventory_layer "
                + "WHERE id=?", fixture.layer1());

            amount("7.000",
                "SELECT remaining_qty FROM inventory_layer "
                + "WHERE id=?", fixture.layer2());

            // Il target viene disattivato in una
            // transazione indipendente e già committata.
            // L'origine rimane valida e disponibile.
            tx.execute(status -> {
                int changed = jdbc.update(
                    "UPDATE warehouse_location SET active=0 "
                    + "WHERE id=? AND warehouse_id=? "
                    + "AND tenant_id=? AND active=1",
                    fixture.targetId(),
                    fixture.warehouseId(),
                    TENANT
                );

                assertEquals(1, changed);
                return null;
            });

            assertEquals(1L, count(
                "SELECT COUNT(*) FROM warehouse_location "
                + "WHERE id=? AND active=0",
                fixture.targetId()));

            // NESSUNA TransactionTemplate intorno a post:
            // la transazione deve essere aperta dal
            // proxy Spring del coordinatore.
            com.gestiva.common.exception.BusinessException failure =
                assertThrows(
                    com.gestiva.common.exception.BusinessException.class,
                    () -> coordinator.post(
                        TENANT,
                        COMPANY,
                        fixture.warehouseId(),
                        fixture.itemId(),
                        fixture.sourceId(),
                        fixture.targetId(),
                        new BigDecimal("12.000"),
                        LocalDate.of(2026, 11, 25),
                        "Rollback coordinatore Spring",
                        fixture.operationKey()
                    )
                );

            assertNotNull(failure.getMessage());

            // Il writer deve aver incontrato la
            // destinazione non più utilizzabile.
            assertTrue(
                failure.getMessage().toLowerCase(
                    java.util.Locale.ROOT
                ).contains("ubicazione"),
                "Errore inatteso: " + failure.getMessage());

            // Nessun trasferimento PENDING o POSTED.
            assertEquals(0L, count(
                "SELECT COUNT(*) "
                + "FROM inventory_location_transfer "
                + "WHERE warehouse_id=?",
                fixture.warehouseId()));

            // Nessuna allocazione persistita.
            assertEquals(0L, count(
                "SELECT COUNT(*) "
                + "FROM inventory_location_transfer_allocation "
                + "WHERE warehouse_id=?",
                fixture.warehouseId()));

            // Nessun consumo persistito.
            assertEquals(0L, count(
                "SELECT COUNT(*) "
                + "FROM inventory_layer_consumption "
                + "WHERE warehouse_id=?",
                fixture.warehouseId()));

            // Gli eventuali movimenti OUT/IN
            // del tentativo devono essere annullati.
            assertEquals(0L, count(
                "SELECT COUNT(*) FROM inventory_movement "
                + "WHERE warehouse_id=? "
                + "AND reference_type='LOCATION_TRANSFER'",
                fixture.warehouseId()));

            // Restano solo i due movimenti
            // iniziali della fixture.
            assertEquals(2L, count(
                "SELECT COUNT(*) FROM inventory_movement "
                + "WHERE warehouse_id=?",
                fixture.warehouseId()));

            // Nessun nuovo layer a destinazione.
            assertEquals(0L, count(
                "SELECT COUNT(*) FROM inventory_layer "
                + "WHERE warehouse_id=? AND location_id=?",
                fixture.warehouseId(),
                fixture.targetId()));

            assertEquals(2L, count(
                "SELECT COUNT(*) FROM inventory_layer "
                + "WHERE warehouse_id=?",
                fixture.warehouseId()));

            // Le giacenze originali non devono cambiare.
            amount("8.000",
                "SELECT remaining_qty FROM inventory_layer "
                + "WHERE id=?", fixture.layer1());

            amount("7.000",
                "SELECT remaining_qty FROM inventory_layer "
                + "WHERE id=?", fixture.layer2());

            amount("15.000",
                "SELECT SUM(remaining_qty) "
                + "FROM inventory_layer WHERE warehouse_id=?",
                fixture.warehouseId());

            amount("124.1000",
                "SELECT SUM(remaining_qty * unit_cost) "
                + "FROM inventory_layer WHERE warehouse_id=?",
                fixture.warehouseId());

            verified = true;

            System.out.println(
                "FIFO_SPRING_COORDINATOR_ROLLBACK: PASS");

        } finally {
            // Se una verifica fallisce, conserviamo
            // la fixture per analizzare il problema.
            if (verified) {
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
    }


    @Test
    void springCoordinatorReusesPostedTransferWithoutDuplicateWrites() {
        assertEquals(33308,
            jdbc.queryForObject("SELECT @@port", Integer.class));

        assertEquals(DATABASE,
            jdbc.queryForObject("SELECT DATABASE()", String.class));

        assertNotNull(coordinator);

        String suffix = UUID.randomUUID()
            .toString().replace("-", "").substring(0, 12);

        TransactionTemplate setup =
            new TransactionTemplate(transactionManager);

        Fixture fixture = setup.execute(
            status -> createFixture(suffix));

        assertNotNull(fixture);

        boolean verified = false;

        try {
            // Prima chiamata: nuova transazione REQUIRED
            // e scritture effettive del writer MANDATORY.
            Long firstId = coordinator.post(
                TENANT,
                COMPANY,
                fixture.warehouseId(),
                fixture.itemId(),
                fixture.sourceId(),
                fixture.targetId(),
                new BigDecimal("12.000"),
                LocalDate.of(2026, 11, 25),
                "Test riutilizzo POSTED Spring",
                fixture.operationKey()
            );

            assertNotNull(firstId);

            // Seconda chiamata: altra transazione Spring.
            // Deve rileggere e verificare il POSTED
            // senza eseguire nuove scritture FIFO.
            Long secondId = coordinator.post(
                TENANT,
                COMPANY,
                fixture.warehouseId(),
                fixture.itemId(),
                fixture.sourceId(),
                fixture.targetId(),
                new BigDecimal("12.000"),
                LocalDate.of(2026, 11, 25),
                "Test riutilizzo POSTED Spring",
                fixture.operationKey()
            );

            assertEquals(firstId, secondId,
                "La richiesta ripetuta deve restituire "
                + "lo stesso ID del trasferimento.");

            assertEquals("POSTED",
                jdbc.queryForObject(
                    "SELECT status FROM inventory_location_transfer "
                    + "WHERE id=? AND tenant_id=? AND company_id=?",
                    String.class,
                    firstId, TENANT, COMPANY));

            assertEquals(1L, count(
                "SELECT COUNT(*) FROM inventory_location_transfer "
                + "WHERE tenant_id=? AND company_id=? "
                + "AND operation_key=?",
                TENANT, COMPANY, fixture.operationKey()));

            assertEquals(1L, count(
                "SELECT COUNT(*) FROM inventory_location_transfer "
                + "WHERE warehouse_id=?",
                fixture.warehouseId()));

            assertEquals(2L, count(
                "SELECT COUNT(*) FROM inventory_movement "
                + "WHERE warehouse_id=? "
                + "AND reference_type='LOCATION_TRANSFER'",
                fixture.warehouseId()));

            assertEquals(4L, count(
                "SELECT COUNT(*) FROM inventory_movement "
                + "WHERE warehouse_id=?",
                fixture.warehouseId()));

            assertEquals(2L, count(
                "SELECT COUNT(*) FROM "
                + "inventory_location_transfer_allocation "
                + "WHERE transfer_id=?",
                firstId));

            assertEquals(2L, count(
                "SELECT COUNT(*) FROM inventory_layer_consumption "
                + "WHERE warehouse_id=?",
                fixture.warehouseId()));

            assertEquals(2L, count(
                "SELECT COUNT(*) FROM inventory_layer "
                + "WHERE warehouse_id=? AND location_id=?",
                fixture.warehouseId(), fixture.targetId()));

            amount("0.000",
                "SELECT remaining_qty FROM inventory_layer "
                + "WHERE id=?", fixture.layer1());

            amount("3.000",
                "SELECT remaining_qty FROM inventory_layer "
                + "WHERE id=?", fixture.layer2());

            amount("3.000",
                "SELECT SUM(remaining_qty) FROM inventory_layer "
                + "WHERE warehouse_id=? AND location_id=?",
                fixture.warehouseId(), fixture.sourceId());

            amount("12.000",
                "SELECT SUM(remaining_qty) FROM inventory_layer "
                + "WHERE warehouse_id=? AND location_id=?",
                fixture.warehouseId(), fixture.targetId());

            amount("15.000",
                "SELECT SUM(remaining_qty) FROM inventory_layer "
                + "WHERE warehouse_id=?",
                fixture.warehouseId());

            amount("95.6000",
                "SELECT SUM(allocated_qty * unit_cost) "
                + "FROM inventory_location_transfer_allocation "
                + "WHERE transfer_id=?",
                firstId);

            assertEquals(0L, count(
                "SELECT COUNT(*) FROM inventory_layer "
                + "WHERE warehouse_id=? AND remaining_qty<0",
                fixture.warehouseId()));

            verified = true;

            System.out.println(
                "FIFO_SPRING_POSTED_ID_REUSE: PASS transferId="
                + firstId);

        } finally {
            // Una verifica fallita conserva le
            // fixture per la diagnosi.
            if (verified) {
                setup.execute(status -> {
                    jdbc.update(
                        "DELETE FROM "
                        + "inventory_location_transfer_allocation "
                        + "WHERE warehouse_id=?",
                        fixture.warehouseId());

                    jdbc.update(
                        "DELETE FROM inventory_layer_consumption "
                        + "WHERE warehouse_id=?",
                        fixture.warehouseId());

                    jdbc.update(
                        "DELETE FROM inventory_layer "
                        + "WHERE warehouse_id=? AND location_id=?",
                        fixture.warehouseId(), fixture.targetId());

                    jdbc.update(
                        "DELETE FROM inventory_location_transfer "
                        + "WHERE warehouse_id=?",
                        fixture.warehouseId());

                    jdbc.update(
                        "DELETE FROM inventory_layer "
                        + "WHERE id IN (?,?)",
                        fixture.layer1(), fixture.layer2());

                    jdbc.update(
                        "DELETE FROM inventory_movement "
                        + "WHERE warehouse_id=?",
                        fixture.warehouseId());

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
