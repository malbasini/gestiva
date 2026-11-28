package com.gestiva.warehouse.inventory.movement.entity;

import com.gestiva.common.model.BaseEntity;
import com.gestiva.logistics.ddt.entity.DeliveryNoteLineLot;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLineLot;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

import java.nio.file.Path;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(
        named = "GESTIVA_LOT_V14_SANDBOX_TEST",
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
class InventoryLotV14JpaSandboxITTest {

    private static final String DATABASE =
            "gestiva_scn_collaudo";

    private static final String USER =
            "gestiva_scn_migrator";

    private static final String URL =
            "jdbc:mysql://127.0.0.1:33308/" + DATABASE
                    + "?sslMode=DISABLED"
                    + "&allowPublicKeyRetrieval=true";

    private static final long TENANT = 2L;
    private static final long COMPANY = 1L;
    private static final long BASE_UOM = 1L;

    private static final List<Class<? extends BaseEntity>>
            V14_ENTITIES = List.of(
                    InventoryMovement.class,
                    InventoryLayer.class,
                    InventoryLocationTransfer.class,
                    GoodsReceiptLineLot.class,
                    DeliveryNoteLineLot.class
            );

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PlatformTransactionManager transactionManager;

    private static String env(String name) {

        String value = System.getenv(name);

        if (value == null || value.isBlank()) {

            throw new IllegalStateException(
                    "STOP: variabile assente: " + name
            );
        }

        return value;
    }

    private static Path sandboxBase() {

        return Path.of(
                System.getProperty("user.home"),
                ".gestiva/mysql-v9-restore"
        );
    }

    private static void assertPhysicalIdentity(
            Object[] row
    ) throws Exception {

        assertEquals(6, row.length);

        assertEquals("8.0.45", row[0]);

        assertEquals(
                33308,
                ((Number) row[1]).intValue()
        );

        assertEquals(DATABASE, row[2]);

        assertEquals(
                USER + "@127.0.0.1",
                row[3]
        );

        Path base = sandboxBase();

        assertEquals(
                base.resolve("data").toRealPath(),
                Path.of(row[4].toString()).toRealPath()
        );

        assertEquals(
                base.resolve("mysql.sock")
                        .toAbsolutePath().normalize(),
                Path.of(row[5].toString())
                        .toAbsolutePath().normalize()
        );
    }

    @DynamicPropertySource
    static void sandbox(
            DynamicPropertyRegistry registry
    ) throws Exception {

        String host = env("MYSQL_COLLAUDO_HOST");
        String port = env("MYSQL_COLLAUDO_PORT");
        String database = env("MYSQL_COLLAUDO_DATABASE");
        String user = env("MYSQL_COLLAUDO_USER");
        String password = env("MYSQL_COLLAUDO_PASSWORD");

        if (!"127.0.0.1".equals(host)
                || !"33308".equals(port)
                || !DATABASE.equals(database)
                || !USER.equals(user)) {

            throw new IllegalStateException(
                    "STOP: datasource non autorizzato"
            );
        }

        // Identita fisica verificata prima del contesto Spring.
        try (var connection = DriverManager.getConnection(
                URL, user, password)) {

            connection.setReadOnly(true);

            try (var statement =
                         connection.createStatement();
                 var rs = statement.executeQuery(
                         "SELECT VERSION(), @@port, "
                                 + "DATABASE(), CURRENT_USER(), "
                                 + "@@datadir, @@socket"
                 )) {

                if (!rs.next()) {

                    throw new IllegalStateException(
                            "STOP: identita SANDBOX assente"
                    );
                }

                Object[] identity = new Object[6];

                for (int i = 0; i < identity.length; i++) {

                    identity[i] = rs.getObject(i + 1);
                }

                assertPhysicalIdentity(identity);
            }
        }

        registry.add(
                "spring.datasource.url",
                () -> URL
        );

        registry.add(
                "spring.datasource.username",
                () -> user
        );

        registry.add(
                "spring.datasource.password",
                () -> password
        );

        registry.add(
                "spring.flyway.enabled",
                () -> false
        );

        registry.add(
                "spring.jpa.hibernate.ddl-auto",
                () -> "validate"
        );

        registry.add(
                "spring.sql.init.mode",
                () -> "never"
        );

        registry.add(
                "spring.datasource.hikari.maximum-pool-size",
                () -> 3
        );
    }

    private TransactionTemplate readOnlyTransaction() {

        TransactionTemplate tx =
                new TransactionTemplate(transactionManager);

        tx.setReadOnly(true);

        return tx;
    }

    private void assertSandboxConnection() {

        Object[] identity = (Object[]) entityManager
                .createNativeQuery(
                        "SELECT VERSION(), @@port, "
                                + "DATABASE(), CURRENT_USER(), "
                                + "@@datadir, @@socket"
                )
                .getSingleResult();

        assertDoesNotThrow(
                () -> assertPhysicalIdentity(identity)
        );

        Number fkChecks = (Number) entityManager
                .createNativeQuery(
                        "SELECT @@session.foreign_key_checks"
                )
                .getSingleResult();

        Number uniqueChecks = (Number) entityManager
                .createNativeQuery(
                        "SELECT @@session.unique_checks"
                )
                .getSingleResult();

        assertEquals(1, fkChecks.intValue());
        assertEquals(1, uniqueChecks.intValue());
    }

    @Test
    void hibernateDatasourceMustBePhysicalSandbox() {

        readOnlyTransaction().executeWithoutResult(
                status -> {

                    assertSandboxConnection();

                    status.setRollbackOnly();
                }
        );
    }

    @Test
    void allFiveV14EntitiesMustBeManagedByJpa() {

        assertEquals(5, V14_ENTITIES.size());

        for (Class<? extends BaseEntity> type
                : V14_ENTITIES) {

            var entity = entityManager.getMetamodel()
                    .entity(type);

            assertEquals(
                    type,
                    entity.getJavaType()
            );

            assertEquals(
                    Long.class,
                    entity.getAttribute("lotId")
                            .getJavaType()
            );

            assertEquals(
                    Long.class,
                    entity.getAttribute("id")
                            .getJavaType()
            );

            assertEquals(
                    Long.class,
                    entity.getAttribute("tenantId")
                            .getJavaType()
            );
        }
    }

    @Test
    void allFiveV14EntitiesMustBeQueryableInSandbox() {

        readOnlyTransaction().executeWithoutResult(
                status -> {

                    assertSandboxConnection();

                    for (Class<? extends BaseEntity> type
                            : V14_ENTITIES) {

                        String name = entityManager
                                .getMetamodel()
                                .entity(type)
                                .getName();

                        Long count = entityManager.createQuery(
                                "select count(e) from "
                                        + name + " e",
                                Long.class
                        ).getSingleResult();

                        assertEquals(
                                0L,
                                count.longValue(),
                                name + " must be empty"
                        );

                        var rows = entityManager.createQuery(
                                "select e from " + name + " e",
                                type
                        ).setMaxResults(1).getResultList();

                        assertTrue(
                                rows.isEmpty(),
                                name + " must be empty"
                        );
                    }

                    status.setRollbackOnly();
                }
        );
    }

    @Test
    void inheritedTimestampsMustBeManagedByJpa() {

        for (Class<? extends BaseEntity> type
                : V14_ENTITIES) {

            var entity = entityManager.getMetamodel()
                    .entity(type);

            assertEquals(
                    LocalDateTime.class,
                    entity.getAttribute("createdAt")
                            .getJavaType()
            );

            assertEquals(
                    LocalDateTime.class,
                    entity.getAttribute("updatedAt")
                            .getJavaType()
            );
        }
    }

    @Test
    void sandboxTenantCompanyAndUomMustBeAvailable() {

        readOnlyTransaction().executeWithoutResult(
                status -> {

                    assertSandboxConnection();

                    assertEquals(
                            "FIFO",
                            jdbc.queryForObject(
                                    "SELECT inventory_valuation_method "
                                            + "FROM tenant WHERE id=?",
                                    String.class,
                                    TENANT
                            )
                    );

                    assertEquals(
                            1L,
                            jdbc.queryForObject(
                                    "SELECT COUNT(*) FROM company "
                                            + "WHERE id=? AND tenant_id=? "
                                            + "AND active=1",
                                    Long.class,
                                    COMPANY, TENANT
                            ).longValue()
                    );

                    assertEquals(
                            1L,
                            jdbc.queryForObject(
                                    "SELECT COUNT(*) FROM unit_of_measure "
                                            + "WHERE id=? AND tenant_id=? "
                                            + "AND code='PZ' AND active=1",
                                    Long.class,
                                    BASE_UOM, TENANT
                            ).longValue()
                    );

                    status.setRollbackOnly();
                }
        );
    }


    // =================================================
    // V14: fixture inventariali nella stessa transazione
    // =================================================

    private record InventoryFixture(
            String marker,
            long itemId,
            Long lotId,
            long warehouseId,
            long sourceLocationId,
            long targetLocationId
    ) {}

    private record InventoryIds(
            Long movementId,
            Long layerId,
            Long transferId
    ) {}

    private static BigDecimal decimal(String value) {

        return new BigDecimal(value);
    }

    private static String marker() {

        return "V14JPA-" + UUID.randomUUID()
                .toString().replace("-", "")
                .substring(0, 12);
    }

    private long requiredId(
            String sql,
            Object... arguments
    ) {

        Long id = jdbc.queryForObject(
                sql, Long.class, arguments
        );

        assertNotNull(id);
        assertTrue(id > 0);

        return id;
    }

    private TransactionTemplate writableTransaction() {

        TransactionTemplate tx =
                new TransactionTemplate(transactionManager);

        tx.setReadOnly(false);

        tx.setPropagationBehavior(
                org.springframework.transaction
                        .TransactionDefinition
                        .PROPAGATION_REQUIRES_NEW
        );

        return tx;
    }

    private void assertWritableSandbox() {

        assertSandboxConnection();

        Number readOnly = (Number) entityManager
                .createNativeQuery(
                        "SELECT @@session.transaction_read_only"
                )
                .getSingleResult();

        assertEquals(0, readOnly.intValue());

        Number jpaConnection = (Number) entityManager
                .createNativeQuery("SELECT CONNECTION_ID()")
                .getSingleResult();

        Long jdbcConnection = jdbc.queryForObject(
                "SELECT CONNECTION_ID()", Long.class
        );

        assertNotNull(jdbcConnection);

        assertEquals(
                jpaConnection.longValue(),
                jdbcConnection.longValue(),
                "JPA e JDBC devono condividere la transazione"
        );
    }

    private InventoryFixture createInventoryFixture(
            String marker,
            boolean tracked
    ) {

        assertWritableSandbox();

        String tracking = tracked ? "LOT" : "NONE";

        assertEquals(1, jdbc.update("""
                INSERT INTO item (
                    tenant_id, code, name, item_type,
                    base_uom_id, tracking_type,
                    track_expiration, active,
                    created_at, updated_at
                )
                VALUES (?, ?, ?, 'PRODUCT', ?, ?,
                        0, 1, NOW(), NOW())
                """,
                TENANT, marker, marker,
                BASE_UOM, tracking
        ));

        long itemId = requiredId("""
                SELECT id FROM item
                WHERE tenant_id=? AND code=?
                """, TENANT, marker);

        Long lotId = null;

        if (tracked) {

            assertEquals(1, jdbc.update("""
                    INSERT INTO inventory_lot (
                        tenant_id, item_id, lot_code,
                        production_date, expiration_date,
                        notes, active, created_at, updated_at
                    )
                    VALUES (?, ?, ?, ?, ?, ?, 1, NOW(), NOW())
                    """,
                    TENANT, itemId, marker,
                    LocalDate.of(2026, 11, 1),
                    LocalDate.of(2027, 11, 1),
                    marker
            ));

            lotId = requiredId("""
                    SELECT id FROM inventory_lot
                    WHERE tenant_id=? AND item_id=?
                      AND lot_code=?
                    """, TENANT, itemId, marker);
        }

        assertEquals(1, jdbc.update("""
                INSERT INTO warehouse (
                    tenant_id, company_id, code, name,
                    warehouse_type, allow_negative_stock,
                    active, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, 'PHYSICAL',
                        0, 1, NOW(), NOW())
                """, TENANT, COMPANY, marker, marker));

        long warehouseId = requiredId("""
                SELECT id FROM warehouse
                WHERE tenant_id=? AND code=?
                """, TENANT, marker);

        assertEquals(1, jdbc.update("""
                INSERT INTO warehouse_location (
                    tenant_id, warehouse_id, code, name,
                    location_type, active,
                    created_at, updated_at
                )
                VALUES (?, ?, ?, ?, 'STORAGE',
                        1, NOW(), NOW())
                """,
                TENANT, warehouseId,
                "A-" + marker, "Origine V14"
        ));

        assertEquals(1, jdbc.update("""
                INSERT INTO warehouse_location (
                    tenant_id, warehouse_id, code, name,
                    location_type, active,
                    created_at, updated_at
                )
                VALUES (?, ?, ?, ?, 'STORAGE',
                        1, NOW(), NOW())
                """,
                TENANT, warehouseId,
                "B-" + marker, "Destinazione V14"
        ));

        long source = requiredId("""
                SELECT id FROM warehouse_location
                WHERE tenant_id=? AND warehouse_id=?
                  AND code=?
                """, TENANT, warehouseId, "A-" + marker);

        long destination = requiredId("""
                SELECT id FROM warehouse_location
                WHERE tenant_id=? AND warehouse_id=?
                  AND code=?
                """, TENANT, warehouseId, "B-" + marker);

        assertNotEquals(source, destination);

        return new InventoryFixture(
                marker, itemId, lotId,
                warehouseId, source, destination
        );
    }

    private static void assertDecimalValue(
            String expected,
            BigDecimal actual,
            int scale
    ) {

        assertNotNull(actual);

        assertEquals(
                0,
                decimal(expected).compareTo(actual)
        );

        assertEquals(scale, actual.scale());
    }

    private static void assertGeneratedIdentity(
            BaseEntity entity
    ) {

        assertNotNull(entity.getId());
        assertTrue(entity.getId() > 0);
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    private static void assertStoredTimestamps(
            BaseEntity entity,
            LocalDateTime lower,
            LocalDateTime upper
    ) {

        assertGeneratedIdentity(entity);

        LocalDateTime min = lower.minusSeconds(2);
        LocalDateTime max = upper.plusSeconds(2);

        assertFalse(entity.getCreatedAt().isBefore(min));
        assertFalse(entity.getCreatedAt().isAfter(max));

        assertFalse(entity.getUpdatedAt().isBefore(min));
        assertFalse(entity.getUpdatedAt().isAfter(max));

        assertFalse(
                entity.getUpdatedAt()
                        .isBefore(entity.getCreatedAt())
        );
    }

    private InventoryIds persistInventoryFixture(
            InventoryFixture fixture
    ) {

        assertWritableSandbox();

        LocalDate movementDate =
                LocalDate.of(2026, 11, 27);

        InventoryMovement movement =
                new InventoryMovement();

        movement.setTenantId(TENANT);
        movement.setCompanyId(COMPANY);
        movement.setItemId(fixture.itemId());
        movement.setWarehouseId(fixture.warehouseId());
        movement.setLocationId(fixture.sourceLocationId());
        movement.setLotId(fixture.lotId());

        movement.setMovementDate(movementDate);
        movement.setMovementType("IN");
        movement.setCausalCode("PURCHASE_RECEIPT");

        movement.setQuantity(decimal("8.125"));
        movement.setUnitCost(decimal("7.2000"));
        movement.setTotalCost(decimal("58.5000"));

        movement.setReferenceType("V14_JPA_TEST");
        movement.setNotes(fixture.marker());

        entityManager.persist(movement);
        entityManager.flush();

        assertGeneratedIdentity(movement);

        InventoryLayer layer = new InventoryLayer();

        layer.setTenantId(TENANT);
        layer.setCompanyId(COMPANY);
        layer.setWarehouseId(fixture.warehouseId());
        layer.setLocationId(fixture.sourceLocationId());
        layer.setItemId(fixture.itemId());
        layer.setLotId(fixture.lotId());
        layer.setSourceMovementId(movement.getId());

        layer.setLayerDate(movementDate);
        layer.setOriginalQty(decimal("8.125"));
        layer.setRemainingQty(decimal("8.125"));
        layer.setUnitCost(decimal("7.2000"));
        layer.setClosed(false);

        entityManager.persist(layer);
        entityManager.flush();

        assertGeneratedIdentity(layer);

        InventoryLocationTransfer transfer =
                new InventoryLocationTransfer();

        transfer.setTenantId(TENANT);
        transfer.setCompanyId(COMPANY);
        transfer.setWarehouseId(fixture.warehouseId());
        transfer.setItemId(fixture.itemId());
        transfer.setLotId(fixture.lotId());

        transfer.setSourceLocationId(
                fixture.sourceLocationId()
        );

        transfer.setTargetLocationId(
                fixture.targetLocationId()
        );

        transfer.setQuantity(decimal("3.125"));
        transfer.setMovementDate(movementDate);
        transfer.setOperationKey(fixture.marker());
        transfer.setStatus("PENDING");

        entityManager.persist(transfer);
        entityManager.flush();

        assertGeneratedIdentity(transfer);

        return new InventoryIds(
                movement.getId(),
                layer.getId(),
                transfer.getId()
        );
    }

    private void assertInventoryRoundTrip(
            InventoryFixture fixture,
            InventoryIds ids,
            LocalDateTime lower
    ) {

        entityManager.clear();

        InventoryMovement movement = entityManager.find(
                InventoryMovement.class,
                ids.movementId()
        );

        InventoryLayer layer = entityManager.find(
                InventoryLayer.class,
                ids.layerId()
        );

        InventoryLocationTransfer transfer =
                entityManager.find(
                        InventoryLocationTransfer.class,
                        ids.transferId()
                );

        assertNotNull(movement);
        assertNotNull(layer);
        assertNotNull(transfer);

        assertEquals(TENANT, movement.getTenantId());
        assertEquals(TENANT, layer.getTenantId());
        assertEquals(TENANT, transfer.getTenantId());

        assertEquals(COMPANY, movement.getCompanyId());
        assertEquals(COMPANY, layer.getCompanyId());
        assertEquals(COMPANY, transfer.getCompanyId());

        assertEquals(fixture.itemId(), movement.getItemId());
        assertEquals(fixture.itemId(), layer.getItemId());
        assertEquals(fixture.itemId(), transfer.getItemId());

        assertEquals(fixture.lotId(), movement.getLotId());
        assertEquals(fixture.lotId(), layer.getLotId());
        assertEquals(fixture.lotId(), transfer.getLotId());

        assertEquals(
                fixture.warehouseId(),
                movement.getWarehouseId()
        );

        assertEquals(
                fixture.warehouseId(),
                layer.getWarehouseId()
        );

        assertEquals(
                fixture.warehouseId(),
                transfer.getWarehouseId()
        );

        assertEquals(
                fixture.sourceLocationId(),
                movement.getLocationId()
        );

        assertEquals(
                fixture.sourceLocationId(),
                layer.getLocationId()
        );

        assertEquals(
                fixture.sourceLocationId(),
                transfer.getSourceLocationId()
        );

        assertEquals(
                fixture.targetLocationId(),
                transfer.getTargetLocationId()
        );

        assertEquals(
                ids.movementId(),
                layer.getSourceMovementId()
        );

        assertEquals("IN", movement.getMovementType());
        assertEquals("PENDING", transfer.getStatus());

        assertEquals(
                fixture.marker(),
                transfer.getOperationKey()
        );

        assertDecimalValue(
                "8.125", movement.getQuantity(), 3
        );

        assertDecimalValue(
                "7.2000", movement.getUnitCost(), 4
        );

        assertDecimalValue(
                "58.5000", movement.getTotalCost(), 4
        );

        assertDecimalValue(
                "8.125", layer.getOriginalQty(), 3
        );

        assertDecimalValue(
                "8.125", layer.getRemainingQty(), 3
        );

        assertDecimalValue(
                "7.2000", layer.getUnitCost(), 4
        );

        assertDecimalValue(
                "3.125", transfer.getQuantity(), 3
        );

        LocalDateTime upper = LocalDateTime.now();

        assertStoredTimestamps(movement, lower, upper);
        assertStoredTimestamps(layer, lower, upper);
        assertStoredTimestamps(transfer, lower, upper);
    }

    private void assertFixtureAbsent(
            String marker
    ) {

        readOnlyTransaction().executeWithoutResult(
                status -> {

                    assertSandboxConnection();

                    String[] tables = {
                            "item",
                            "inventory_lot",
                            "warehouse",
                            "warehouse_location",
                            "inventory_movement",
                            "inventory_layer",
                            "inventory_location_transfer"
                    };

                    for (String table : tables) {

                        Long count = jdbc.queryForObject(
                                "SELECT COUNT(*) FROM " + table,
                                Long.class
                        );

                        assertNotNull(count);

                        assertEquals(
                                0L,
                                count.longValue(),
                                "Fixture residua: "
                                        + marker + " / " + table
                        );
                    }

                    status.setRollbackOnly();
                }
        );
    }

    private void verifyInventoryPersistence(
            boolean tracked
    ) {

        String marker = marker();

        assertFixtureAbsent(marker);

        try {

            writableTransaction().executeWithoutResult(
                    status -> {

                        status.setRollbackOnly();

                        LocalDateTime lower =
                                LocalDateTime.now();

                        InventoryFixture fixture =
                                createInventoryFixture(
                                        marker, tracked
                                );

                        if (tracked) {

                            assertNotNull(fixture.lotId());

                        } else {

                            assertNull(fixture.lotId());
                        }

                        InventoryIds ids =
                                persistInventoryFixture(
                                        fixture
                                );

                        assertInventoryRoundTrip(
                                fixture, ids, lower
                        );
                    }
            );

        } finally {

            assertFixtureAbsent(marker);
        }
    }

    @Test
    void trackedInventoryEntitiesMustSurviveJpaRoundTrip() {

        verifyInventoryPersistence(true);
    }

    @Test
    void untrackedInventoryEntitiesMustPreserveNullLotId() {

        verifyInventoryPersistence(false);
    }

    @Test
    void inventoryMovementUpdateMustPreserveCreationTimestamp() {

        String marker = marker();

        assertFixtureAbsent(marker);

        try {

            writableTransaction().executeWithoutResult(
                    status -> {

                        status.setRollbackOnly();

                        InventoryFixture fixture =
                                createInventoryFixture(
                                        marker, true
                                );

                        InventoryIds ids =
                                persistInventoryFixture(
                                        fixture
                                );

                        entityManager.clear();

                        InventoryMovement movement =
                                entityManager.find(
                                        InventoryMovement.class,
                                        ids.movementId()
                                );

                        assertNotNull(movement);

                        LocalDateTime created =
                                movement.getCreatedAt();

                        LocalDateTime previousUpdate =
                                movement.getUpdatedAt();

                        assertNotNull(created);
                        assertNotNull(previousUpdate);

                        // TIMESTAMP SQL ha precisione al secondo.
                        // Attendere il secondo successivo consente
                        // di osservare un vero cambio di updatedAt.
                        try {

                            Thread.sleep(1200L);

                        } catch (InterruptedException ex) {

                            Thread.currentThread().interrupt();

                            throw new IllegalStateException(ex);
                        }

                        movement.setNotes(
                                marker + "-UPDATED"
                        );

                        entityManager.flush();
                        entityManager.clear();

                        InventoryMovement reloaded =
                                entityManager.find(
                                        InventoryMovement.class,
                                        ids.movementId()
                                );

                        assertNotNull(reloaded);

                        assertEquals(
                                marker + "-UPDATED",
                                reloaded.getNotes()
                        );

                        assertEquals(
                                created,
                                reloaded.getCreatedAt()
                        );

                        assertTrue(
                                reloaded.getUpdatedAt()
                                        .isAfter(previousUpdate)
                        );

                        assertEquals(
                                fixture.lotId(),
                                reloaded.getLotId()
                        );

                        assertDecimalValue(
                                "8.125",
                                reloaded.getQuantity(),
                                3
                        );

                        assertDecimalValue(
                                "58.5000",
                                reloaded.getTotalCost(),
                                4
                        );
                    }
            );

        } finally {

            assertFixtureAbsent(marker);
        }
    }

    private static final class ExpectedRollback
            extends RuntimeException {

        ExpectedRollback() {

            super("V14 intentional rollback");
        }
    }

    @Test
    void forcedFailureMustRollbackAllInventoryFixtures() {

        String marker = marker();

        assertFixtureAbsent(marker);

        try {

            assertThrows(
                    ExpectedRollback.class,
                    () -> writableTransaction()
                            .executeWithoutResult(
                                    status -> {

                                        InventoryFixture fixture =
                                                createInventoryFixture(
                                                        marker, true
                                                );

                                        InventoryIds ids =
                                                persistInventoryFixture(
                                                        fixture
                                                );

                                        entityManager.clear();

                                        assertNotNull(
                                                entityManager.find(
                                                        InventoryMovement.class,
                                                        ids.movementId()
                                                )
                                        );

                                        assertNotNull(
                                                entityManager.find(
                                                        InventoryLayer.class,
                                                        ids.layerId()
                                                )
                                        );

                                        assertNotNull(
                                                entityManager.find(
                                                        InventoryLocationTransfer.class,
                                                        ids.transferId()
                                                )
                                        );

                                        throw new ExpectedRollback();
                                    }
                            )
            );

        } finally {

            assertFixtureAbsent(marker);
        }
    }


    // =================================================
    // V14: fixture documentali GR e DDT
    // =================================================

    private record DocumentFixture(
            InventoryFixture inventory,
            long secondLotId,
            long receiptId,
            long receiptLineId,
            long deliveryId,
            long deliveryLineId
    ) {}

    private static final String[] DOCUMENT_TEST_TABLES = {
            "supplier",
            "purchase_order",
            "purchase_order_line",
            "goods_receipt",
            "goods_receipt_line",
            "goods_receipt_line_lot",
            "customer",
            "sales_order",
            "sales_order_line",
            "delivery_note",
            "delivery_note_line",
            "delivery_note_line_lot",
            "item",
            "inventory_lot",
            "inventory_serial",
            "inventory_movement",
            "inventory_layer",
            "inventory_layer_consumption",
            "inventory_average_balance",
            "inventory_location_transfer",
            "inventory_location_transfer_allocation",
            "warehouse",
            "warehouse_location",
            "invoice",
            "invoice_line",
            "quote"
    };

    private void assertDocumentFixturesAbsent() {

        readOnlyTransaction().executeWithoutResult(
                status -> {

                    assertSandboxConnection();

                    for (String table : DOCUMENT_TEST_TABLES) {

                        Long count = jdbc.queryForObject(
                                "SELECT COUNT(*) FROM " + table,
                                Long.class
                        );

                        assertNotNull(count);

                        assertEquals(
                                0L,
                                count.longValue(),
                                "Fixture residua: " + table
                        );
                    }

                    status.setRollbackOnly();
                }
        );
    }

    private DocumentFixture createDocumentFixture(
            String marker
    ) {

        assertWritableSandbox();

        InventoryFixture inventory =
                createInventoryFixture(marker, true);

        assertNotNull(inventory.lotId());

        String secondCode = marker + "-B";

        assertEquals(1, jdbc.update("""
                INSERT INTO inventory_lot (
                    tenant_id, item_id, lot_code,
                    production_date, expiration_date,
                    notes, active, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, 1, NOW(), NOW())
                """,
                TENANT, inventory.itemId(), secondCode,
                LocalDate.of(2026, 11, 2),
                LocalDate.of(2027, 11, 2),
                marker
        ));

        long secondLotId = requiredId("""
                SELECT id FROM inventory_lot
                WHERE tenant_id=? AND item_id=?
                  AND lot_code=?
                """,
                TENANT, inventory.itemId(), secondCode
        );

        assertNotEquals(
                inventory.lotId().longValue(),
                secondLotId
        );

        LocalDate date = LocalDate.of(2026, 11, 27);

        // ---------------------------------------------
        // Fornitore e ordine di acquisto
        // ---------------------------------------------

        assertEquals(1, jdbc.update("""
                INSERT INTO supplier (
                    tenant_id, code, name, active,
                    created_at, updated_at
                )
                VALUES (?, ?, ?, 1, NOW(), NOW())
                """, TENANT, marker, marker));

        long supplierId = requiredId("""
                SELECT id FROM supplier
                WHERE tenant_id=? AND code=?
                """, TENANT, marker);

        assertEquals(1, jdbc.update("""
                INSERT INTO purchase_order (
                    tenant_id, company_id, order_number,
                    order_date, supplier_id, status,
                    currency_code, subtotal_amount,
                    tax_amount, total_amount,
                    created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, 'CONFIRMED',
                        'EUR', 720.00, 158.40, 878.40,
                        NOW(), NOW())
                """,
                TENANT, COMPANY, marker, date, supplierId
        ));

        long purchaseId = requiredId("""
                SELECT id FROM purchase_order
                WHERE tenant_id=? AND company_id=?
                  AND order_number=?
                """, TENANT, COMPANY, marker);

        assertEquals(1, jdbc.update("""
                INSERT INTO purchase_order_line (
                    tenant_id, purchase_order_id, line_no,
                    item_id, uom_id, conversion_factor,
                    description, quantity, unit_price,
                    discount_pct, tax_pct, line_subtotal,
                    tax_amount, line_total,
                    created_at, updated_at
                )
                VALUES (?, ?, 1, ?, ?, 1.000000,
                        ?, 100.000, 7.20,
                        0.00, 22.00, 720.00,
                        158.40, 878.40, NOW(), NOW())
                """,
                TENANT, purchaseId,
                inventory.itemId(), BASE_UOM, marker
        ));

        long purchaseLineId = requiredId("""
                SELECT id FROM purchase_order_line
                WHERE tenant_id=? AND purchase_order_id=?
                  AND line_no=1
                """, TENANT, purchaseId);

        // ---------------------------------------------
        // Ricevimento merci e riga commerciale
        // ---------------------------------------------

        assertEquals(1, jdbc.update("""
                INSERT INTO goods_receipt (
                    tenant_id, company_id, receipt_number,
                    receipt_date, purchase_order_id,
                    supplier_id, warehouse_id, location_id,
                    created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                """,
                TENANT, COMPANY, marker, date,
                purchaseId, supplierId,
                inventory.warehouseId(),
                inventory.sourceLocationId()
        ));

        long receiptId = requiredId("""
                SELECT id FROM goods_receipt
                WHERE tenant_id=? AND company_id=?
                  AND receipt_number=?
                """, TENANT, COMPANY, marker);

        assertEquals(1, jdbc.update("""
                INSERT INTO goods_receipt_line (
                    tenant_id, goods_receipt_id, line_no,
                    purchase_order_line_id, item_id,
                    description, quantity_received,
                    unit_cost, total_cost,
                    created_at, updated_at
                )
                VALUES (?, ?, 1, ?, ?, ?, 100.000,
                        7.2000, 720.0000, NOW(), NOW())
                """,
                TENANT, receiptId, purchaseLineId,
                inventory.itemId(), marker
        ));

        long receiptLineId = requiredId("""
                SELECT id FROM goods_receipt_line
                WHERE tenant_id=? AND goods_receipt_id=?
                  AND line_no=1
                """, TENANT, receiptId);

        // ---------------------------------------------
        // Cliente e ordine di vendita
        // ---------------------------------------------

        assertEquals(1, jdbc.update("""
                INSERT INTO customer (
                    tenant_id, name,
                    created_at, updated_at
                )
                VALUES (?, ?, NOW(), NOW())
                """, TENANT, marker));

        long customerId = requiredId("""
                SELECT id FROM customer
                WHERE tenant_id=? AND name=?
                """, TENANT, marker);

        assertEquals(1, jdbc.update("""
                INSERT INTO sales_order (
                    tenant_id, company_id, customer_id,
                    order_number, order_date, status,
                    currency_code, subtotal_amount,
                    tax_amount, total_amount,
                    created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, 'FULFILLED',
                        'EUR', 840.00, 184.80, 1024.80,
                        NOW(), NOW())
                """,
                TENANT, COMPANY, customerId, marker, date
        ));

        long salesId = requiredId("""
                SELECT id FROM sales_order
                WHERE tenant_id=? AND company_id=?
                  AND order_number=?
                """, TENANT, COMPANY, marker);

        assertEquals(1, jdbc.update("""
                INSERT INTO sales_order_line (
                    tenant_id, company_id, sales_order_id,
                    line_no, description, item_id,
                    quantity, unit_of_measure, uom_id,
                    base_uom_code, conversion_factor,
                    unit_price, discount_pct, tax_pct,
                    tax_amount, line_total,
                    created_at, updated_at
                )
                VALUES (?, ?, ?, 1, ?, ?,
                        70.000, 'PZ', ?,
                        'PZ', 1.000000,
                        12.00, 0.00, 22.00,
                        184.80, 1024.80, NOW(), NOW())
                """,
                TENANT, COMPANY, salesId, marker,
                inventory.itemId(), BASE_UOM
        ));

        long salesLineId = requiredId("""
                SELECT id FROM sales_order_line
                WHERE tenant_id=? AND company_id=?
                  AND sales_order_id=? AND line_no=1
                """, TENANT, COMPANY, salesId);

        // ---------------------------------------------
        // DDT e riga commerciale
        // ---------------------------------------------

        assertEquals(1, jdbc.update("""
                INSERT INTO delivery_note (
                    tenant_id, company_id, sales_order_id,
                    customer_id, ddt_number, ddt_date,
                    status, currency_code,
                    subtotal_amount, tax_amount, total_amount,
                    warehouse_id, location_id,
                    created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, 'ISSUED',
                        'EUR', 840.00, 184.80, 1024.80,
                        ?, ?, NOW(), NOW())
                """,
                TENANT, COMPANY, salesId, customerId,
                marker, date, inventory.warehouseId(),
                inventory.sourceLocationId()
        ));

        long deliveryId = requiredId("""
                SELECT id FROM delivery_note
                WHERE tenant_id=? AND company_id=?
                  AND ddt_number=?
                """, TENANT, COMPANY, marker);

        assertEquals(1, jdbc.update("""
                INSERT INTO delivery_note_line (
                    tenant_id, company_id, delivery_note_id,
                    sales_order_line_id, line_no, description,
                    item_id, quantity, unit_of_measure,
                    uom_id, base_uom_code, conversion_factor,
                    unit_price, discount_pct, tax_pct,
                    tax_amount, line_total
                )
                VALUES (?, ?, ?, ?, 1, ?, ?,
                        70.000, 'PZ', ?, 'PZ', 1.000000,
                        12.00, 0.00, 22.00, 184.80, 1024.80)
                """,
                TENANT, COMPANY, deliveryId, salesLineId,
                marker, inventory.itemId(), BASE_UOM
        ));

        long deliveryLineId = requiredId("""
                SELECT id FROM delivery_note_line
                WHERE tenant_id=? AND company_id=?
                  AND delivery_note_id=? AND line_no=1
                """, TENANT, COMPANY, deliveryId);

        return new DocumentFixture(
                inventory, secondLotId,
                receiptId, receiptLineId,
                deliveryId, deliveryLineId
        );
    }

    private Long persistDocumentMovement(
            DocumentFixture fixture,
            long lotId,
            String quantity,
            boolean inbound
    ) {

        InventoryFixture inventory = fixture.inventory();

        InventoryMovement movement =
                new InventoryMovement();

        movement.setTenantId(TENANT);
        movement.setCompanyId(COMPANY);
        movement.setItemId(inventory.itemId());
        movement.setWarehouseId(inventory.warehouseId());
        movement.setLocationId(inventory.sourceLocationId());
        movement.setLotId(lotId);

        movement.setMovementDate(
                LocalDate.of(2026, 11, 27)
        );

        movement.setMovementType(
                inbound ? "IN" : "OUT"
        );

        movement.setCausalCode(
                inbound ? "PURCHASE_RECEIPT" : "SALES_DELIVERY"
        );

        movement.setReferenceType(
                inbound ? "GOODS_RECEIPT" : "DELIVERY_NOTE"
        );

        movement.setReferenceId(
                inbound
                        ? fixture.receiptId()
                        : fixture.deliveryId()
        );

        movement.setQuantity(decimal(quantity));
        movement.setUnitCost(decimal("7.2000"));

        movement.setTotalCost(
                decimal(quantity)
                        .multiply(decimal("7.2000"))
                        .setScale(4)
        );

        movement.setNotes(inventory.marker());

        entityManager.persist(movement);
        entityManager.flush();

        assertGeneratedIdentity(movement);

        return movement.getId();
    }

    private Long persistReceiptLot(
            DocumentFixture fixture,
            long lotId,
            String quantity,
            Long movementId
    ) {

        GoodsReceiptLineLot allocation =
                new GoodsReceiptLineLot();

        allocation.setTenantId(TENANT);
        allocation.setCompanyId(COMPANY);

        allocation.setGoodsReceiptLineId(
                fixture.receiptLineId()
        );

        allocation.setItemId(
                fixture.inventory().itemId()
        );

        allocation.setLotId(lotId);
        allocation.setQuantity(decimal(quantity));
        allocation.setUnitCost(decimal("7.2000"));

        allocation.setTotalCost(
                decimal(quantity)
                        .multiply(decimal("7.2000"))
                        .setScale(4)
        );

        allocation.setMovementId(movementId);

        entityManager.persist(allocation);
        entityManager.flush();

        assertGeneratedIdentity(allocation);

        return allocation.getId();
    }

    private Long persistDeliveryLot(
            DocumentFixture fixture,
            long lotId,
            String quantity,
            Long movementId
    ) {

        DeliveryNoteLineLot allocation =
                new DeliveryNoteLineLot();

        allocation.setTenantId(TENANT);
        allocation.setCompanyId(COMPANY);

        allocation.setDeliveryNoteLineId(
                fixture.deliveryLineId()
        );

        allocation.setItemId(
                fixture.inventory().itemId()
        );

        allocation.setLotId(lotId);
        allocation.setQuantity(decimal(quantity));
        allocation.setMovementId(movementId);

        entityManager.persist(allocation);
        entityManager.flush();

        assertGeneratedIdentity(allocation);

        return allocation.getId();
    }

    private void assertDocumentMovement(
            DocumentFixture fixture,
            Long movementId,
            long lotId,
            String quantity,
            boolean inbound
    ) {

        if (movementId == null) {
            return;
        }

        InventoryMovement movement =
                entityManager.find(
                        InventoryMovement.class, movementId
                );

        assertNotNull(movement);
        assertEquals(TENANT, movement.getTenantId());
        assertEquals(COMPANY, movement.getCompanyId());

        assertEquals(
                fixture.inventory().itemId(),
                movement.getItemId()
        );

        assertEquals(lotId, movement.getLotId());

        assertEquals(
                fixture.inventory().warehouseId(),
                movement.getWarehouseId()
        );

        assertEquals(
                fixture.inventory().sourceLocationId(),
                movement.getLocationId()
        );

        assertEquals(
                inbound ? "IN" : "OUT",
                movement.getMovementType()
        );

        assertEquals(
                inbound
                        ? "PURCHASE_RECEIPT"
                        : "SALES_DELIVERY",
                movement.getCausalCode()
        );

        assertEquals(
                inbound
                        ? "GOODS_RECEIPT"
                        : "DELIVERY_NOTE",
                movement.getReferenceType()
        );

        assertEquals(
                inbound
                        ? fixture.receiptId()
                        : fixture.deliveryId(),
                movement.getReferenceId()
        );

        assertDecimalValue(
                quantity, movement.getQuantity(), 3
        );
    }

    private GoodsReceiptLineLot assertReceiptLotRoundTrip(
            DocumentFixture fixture,
            Long id,
            long lotId,
            String quantity,
            Long movementId,
            LocalDateTime lower
    ) {

        entityManager.clear();

        GoodsReceiptLineLot allocation =
                entityManager.find(
                        GoodsReceiptLineLot.class, id
                );

        assertNotNull(allocation);

        assertEquals(id, allocation.getId());
        assertEquals(TENANT, allocation.getTenantId());
        assertEquals(COMPANY, allocation.getCompanyId());

        assertEquals(
                fixture.receiptLineId(),
                allocation.getGoodsReceiptLineId()
        );

        assertEquals(
                fixture.inventory().itemId(),
                allocation.getItemId()
        );

        assertEquals(lotId, allocation.getLotId());
        assertEquals(movementId, allocation.getMovementId());

        assertDecimalValue(
                quantity, allocation.getQuantity(), 3
        );

        assertDecimalValue(
                "7.2000", allocation.getUnitCost(), 4
        );

        assertDecimalValue(
                decimal(quantity)
                        .multiply(decimal("7.2000"))
                        .setScale(4).toPlainString(),
                allocation.getTotalCost(),
                4
        );

        assertStoredTimestamps(
                allocation, lower, LocalDateTime.now()
        );

        assertDocumentMovement(
                fixture, movementId, lotId, quantity, true
        );

        return allocation;
    }

    private DeliveryNoteLineLot assertDeliveryLotRoundTrip(
            DocumentFixture fixture,
            Long id,
            long lotId,
            String quantity,
            Long movementId,
            LocalDateTime lower
    ) {

        entityManager.clear();

        DeliveryNoteLineLot allocation =
                entityManager.find(
                        DeliveryNoteLineLot.class, id
                );

        assertNotNull(allocation);

        assertEquals(id, allocation.getId());
        assertEquals(TENANT, allocation.getTenantId());
        assertEquals(COMPANY, allocation.getCompanyId());

        assertEquals(
                fixture.deliveryLineId(),
                allocation.getDeliveryNoteLineId()
        );

        assertEquals(
                fixture.inventory().itemId(),
                allocation.getItemId()
        );

        assertEquals(lotId, allocation.getLotId());
        assertEquals(movementId, allocation.getMovementId());

        assertDecimalValue(
                quantity, allocation.getQuantity(), 3
        );

        assertStoredTimestamps(
                allocation, lower, LocalDateTime.now()
        );

        assertDocumentMovement(
                fixture, movementId, lotId, quantity, false
        );

        return allocation;
    }

    @FunctionalInterface
    private interface DocumentScenario {

        void execute(
                DocumentFixture fixture,
                LocalDateTime lower
        );
    }

    private void runDocumentScenario(
            DocumentScenario scenario
    ) {

        assertDocumentFixturesAbsent();

        try {

            writableTransaction().executeWithoutResult(
                    status -> {

                        status.setRollbackOnly();

                        LocalDateTime lower =
                                LocalDateTime.now();

                        DocumentFixture fixture =
                                createDocumentFixture(marker());

                        scenario.execute(fixture, lower);

                        entityManager.flush();
                    }
            );

        } finally {

            assertDocumentFixturesAbsent();
        }
    }

    // TEST 10

    @Test
    void receiptLotAllocationMustSurviveJpaRoundTrip() {

        runDocumentScenario((fixture, lower) -> {

            long lotId = fixture.inventory().lotId();

            Long movementId = persistDocumentMovement(
                    fixture, lotId, "100.000", true
            );

            Long allocationId = persistReceiptLot(
                    fixture, lotId, "100.000", movementId
            );

            assertReceiptLotRoundTrip(
                    fixture, allocationId, lotId,
                    "100.000", movementId, lower
            );
        });
    }

    // TEST 11

    @Test
    void deliveryLotAllocationMustSurviveJpaRoundTrip() {

        runDocumentScenario((fixture, lower) -> {

            long lotId = fixture.inventory().lotId();

            Long movementId = persistDocumentMovement(
                    fixture, lotId, "70.000", false
            );

            Long allocationId = persistDeliveryLot(
                    fixture, lotId, "70.000", movementId
            );

            assertDeliveryLotRoundTrip(
                    fixture, allocationId, lotId,
                    "70.000", movementId, lower
            );
        });
    }

    // TEST 12

    @Test
    void receiptLineMustSupportMultipleLotAllocations() {

        runDocumentScenario((fixture, lower) -> {

            long firstLot = fixture.inventory().lotId();
            long secondLot = fixture.secondLotId();

            Long firstMovement = persistDocumentMovement(
                    fixture, firstLot, "60.000", true
            );

            Long secondMovement = persistDocumentMovement(
                    fixture, secondLot, "40.000", true
            );

            Long firstId = persistReceiptLot(
                    fixture, firstLot, "60.000", firstMovement
            );

            Long secondId = persistReceiptLot(
                    fixture, secondLot, "40.000", secondMovement
            );

            assertNotEquals(firstId, secondId);
            assertNotEquals(firstMovement, secondMovement);

            var first = assertReceiptLotRoundTrip(
                    fixture, firstId, firstLot,
                    "60.000", firstMovement, lower
            );

            var second = assertReceiptLotRoundTrip(
                    fixture, secondId, secondLot,
                    "40.000", secondMovement, lower
            );

            assertDecimalValue(
                    "100.000",
                    first.getQuantity()
                            .add(second.getQuantity()),
                    3
            );

            assertDecimalValue(
                    "720.0000",
                    first.getTotalCost()
                            .add(second.getTotalCost()),
                    4
            );

            Number allocations = (Number) entityManager
                    .createNativeQuery("""
                        SELECT COUNT(*)
                        FROM goods_receipt_line_lot
                        WHERE tenant_id=:tenant
                          AND company_id=:company
                          AND goods_receipt_line_id=:line
                        """)
                    .setParameter("tenant", TENANT)
                    .setParameter("company", COMPANY)
                    .setParameter(
                            "line", fixture.receiptLineId()
                    )
                    .getSingleResult();

            assertEquals(2L, allocations.longValue());

            assertEquals(
                    1L,
                    jdbc.queryForObject("""
                        SELECT COUNT(*)
                        FROM goods_receipt_line
                        WHERE tenant_id=?
                          AND goods_receipt_id=?
                        """,
                        Long.class,
                        TENANT, fixture.receiptId()
                    ).longValue()
            );

            assertDecimalValue(
                    "100.000",
                    jdbc.queryForObject("""
                        SELECT quantity_received
                        FROM goods_receipt_line
                        WHERE id=?
                        """,
                        BigDecimal.class,
                        fixture.receiptLineId()
                    ),
                    3
            );

            assertDecimalValue(
                    "720.0000",
                    jdbc.queryForObject("""
                        SELECT total_cost
                        FROM goods_receipt_line
                        WHERE id=?
                        """,
                        BigDecimal.class,
                        fixture.receiptLineId()
                    ),
                    4
            );
        });
    }

    // TEST 13

    @Test
    void deliveryLineMustSupportMultipleLotAllocations() {

        runDocumentScenario((fixture, lower) -> {

            long firstLot = fixture.inventory().lotId();
            long secondLot = fixture.secondLotId();

            Long firstMovement = persistDocumentMovement(
                    fixture, firstLot, "50.000", false
            );

            Long secondMovement = persistDocumentMovement(
                    fixture, secondLot, "20.000", false
            );

            Long firstId = persistDeliveryLot(
                    fixture, firstLot, "50.000", firstMovement
            );

            Long secondId = persistDeliveryLot(
                    fixture, secondLot, "20.000", secondMovement
            );

            assertNotEquals(firstId, secondId);
            assertNotEquals(firstMovement, secondMovement);

            var first = assertDeliveryLotRoundTrip(
                    fixture, firstId, firstLot,
                    "50.000", firstMovement, lower
            );

            var second = assertDeliveryLotRoundTrip(
                    fixture, secondId, secondLot,
                    "20.000", secondMovement, lower
            );

            assertDecimalValue(
                    "70.000",
                    first.getQuantity()
                            .add(second.getQuantity()),
                    3
            );

            Number allocations = (Number) entityManager
                    .createNativeQuery("""
                        SELECT COUNT(*)
                        FROM delivery_note_line_lot
                        WHERE tenant_id=:tenant
                          AND company_id=:company
                          AND delivery_note_line_id=:line
                        """)
                    .setParameter("tenant", TENANT)
                    .setParameter("company", COMPANY)
                    .setParameter(
                            "line", fixture.deliveryLineId()
                    )
                    .getSingleResult();

            assertEquals(2L, allocations.longValue());

            assertEquals(
                    1L,
                    jdbc.queryForObject("""
                        SELECT COUNT(*)
                        FROM delivery_note_line
                        WHERE tenant_id=? AND company_id=?
                          AND delivery_note_id=?
                        """,
                        Long.class,
                        TENANT, COMPANY, fixture.deliveryId()
                    ).longValue()
            );

            assertDecimalValue(
                    "70.000",
                    jdbc.queryForObject("""
                        SELECT quantity
                        FROM delivery_note_line
                        WHERE id=?
                        """,
                        BigDecimal.class,
                        fixture.deliveryLineId()
                    ),
                    3
            );
        });
    }

    // TEST 14

    @Test
    void documentAllocationsMustAcceptDeferredMovementLink() {

        runDocumentScenario((fixture, lower) -> {

            long lotId = fixture.inventory().lotId();

            Long receiptAllocationId = persistReceiptLot(
                    fixture, lotId, "100.000", null
            );

            Long deliveryAllocationId = persistDeliveryLot(
                    fixture, lotId, "70.000", null
            );

            var initialReceipt = assertReceiptLotRoundTrip(
                    fixture, receiptAllocationId, lotId,
                    "100.000", null, lower
            );

            LocalDateTime receiptCreatedAt =
                    initialReceipt.getCreatedAt();

            var initialDelivery = assertDeliveryLotRoundTrip(
                    fixture, deliveryAllocationId, lotId,
                    "70.000", null, lower
            );

            LocalDateTime deliveryCreatedAt =
                    initialDelivery.getCreatedAt();

            Long in = persistDocumentMovement(
                    fixture, lotId, "100.000", true
            );

            Long out = persistDocumentMovement(
                    fixture, lotId, "70.000", false
            );

            assertNotEquals(in, out);

            GoodsReceiptLineLot receipt =
                    entityManager.find(
                            GoodsReceiptLineLot.class,
                            receiptAllocationId
                    );

            DeliveryNoteLineLot delivery =
                    entityManager.find(
                            DeliveryNoteLineLot.class,
                            deliveryAllocationId
                    );

            assertNotNull(receipt);
            assertNotNull(delivery);

            receipt.setMovementId(in);
            delivery.setMovementId(out);

            entityManager.flush();
            entityManager.clear();

            var finalReceipt = assertReceiptLotRoundTrip(
                    fixture, receiptAllocationId, lotId,
                    "100.000", in, lower
            );

            assertEquals(
                    receiptCreatedAt,
                    finalReceipt.getCreatedAt()
            );

            var finalDelivery = assertDeliveryLotRoundTrip(
                    fixture, deliveryAllocationId, lotId,
                    "70.000", out, lower
            );

            assertEquals(
                    deliveryCreatedAt,
                    finalDelivery.getCreatedAt()
            );
        });
    }

    // TEST 15

    @Test
    void forcedFailureMustRollbackAllDocumentFixtures() {

        assertDocumentFixturesAbsent();

        try {

            assertThrows(
                    ExpectedRollback.class,
                    () -> writableTransaction()
                            .executeWithoutResult(status -> {

                                LocalDateTime lower =
                                        LocalDateTime.now();

                                DocumentFixture fixture =
                                        createDocumentFixture(marker());

                                long lotId =
                                        fixture.inventory().lotId();

                                Long in = persistDocumentMovement(
                                        fixture, lotId,
                                        "100.000", true
                                );

                                Long out = persistDocumentMovement(
                                        fixture, lotId,
                                        "70.000", false
                                );

                                Long receiptAllocation =
                                        persistReceiptLot(
                                                fixture, lotId,
                                                "100.000", in
                                        );

                                Long deliveryAllocation =
                                        persistDeliveryLot(
                                                fixture, lotId,
                                                "70.000", out
                                        );

                                assertReceiptLotRoundTrip(
                                        fixture, receiptAllocation,
                                        lotId, "100.000",
                                        in, lower
                                );

                                assertDeliveryLotRoundTrip(
                                        fixture, deliveryAllocation,
                                        lotId, "70.000",
                                        out, lower
                                );

                                entityManager.flush();

                                throw new ExpectedRollback();
                            })
            );

        } finally {

            assertDocumentFixturesAbsent();
        }
    }
}
