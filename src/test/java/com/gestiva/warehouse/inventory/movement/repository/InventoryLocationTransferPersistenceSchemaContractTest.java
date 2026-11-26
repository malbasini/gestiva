package com.gestiva.warehouse.inventory.movement.repository;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferPersistenceSchemaContractTest {

    @Test
    void migrationMustDefineAtomicTransferAndFifoTraceability()
            throws Exception {

        Path migration = Path.of(
                "src/main/resources/db/migration/"
                + "V12__inventory_location_transfer.sql"
        );

        if (!Files.isRegularFile(migration)) {
            fail("STOP-ANW-V12-TRANSFER-SCHEMA-NON-IMPLEMENTATO");
        }

        String sql = Files.readString(migration)
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");

        // Identificativo e contesto del trasferimento.
        require(sql, "create table `inventory_location_transfer`");
        require(sql, "`tenant_id`");
        require(sql, "`company_id`");
        require(sql, "`warehouse_id`");
        require(sql, "`item_id`");
        require(sql, "`source_location_id`");
        require(sql, "`target_location_id`");
        require(sql, "`quantity`");
        require(sql, "`movement_date`");

        // Collegamento ai due movimenti inventariali.
        require(sql, "`out_movement_id`");
        require(sql, "`in_movement_id`");

        // Protezione contro richieste duplicate.
        require(sql, "`operation_key`");
        require(sql, "unique key");
        require(sql, "(`tenant_id`,`company_id`,`operation_key`)");

        // Tracciabilità dei singoli layer FIFO.
        require(
                sql,
                "create table `inventory_location_transfer_allocation`"
        );
        require(sql, "`transfer_id`");
        require(sql, "`source_layer_id`");
        require(sql, "`target_layer_id`");
        require(sql, "`allocated_qty`");
        require(sql, "`unit_cost`");
        require(sql, "`layer_date`");
    }


    @Test
    void migrationV13MustAllowOnlyUnlocatedTransferSource()
            throws Exception {

        Path migration = Path.of(
                "src/main/resources/db/migration/"
                + "V13__allow_unlocated_inventory_transfer_source.sql"
        );

        assertTrue(
                Files.isRegularFile(migration),
                "Migrazione V13 mancante."
        );

        String sql = Files.readString(migration)
                .replaceAll("(?m)--[^\\r\\n]*", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();

        String expected = """
                ALTER TABLE `inventory_location_transfer`
                    DROP CHECK `chk_location_transfer_locations`,
                    MODIFY COLUMN `source_location_id` BIGINT NULL,
                    ADD CONSTRAINT `chk_location_transfer_locations`
                        CHECK (
                            `target_location_id` > 0
                            AND (
                                `source_location_id` IS NULL
                                OR (
                                    `source_location_id` > 0
                                    AND `source_location_id`
                                        <> `target_location_id`
                                )
                            )
                        );
                """
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();

        assertEquals(
                expected,
                sql,
                "V13 deve modificare esclusivamente il contratto "
                        + "dell'ubicazione di origine e il relativo CHECK."
        );

        Path v12 = Path.of(
                "src/main/resources/db/migration/"
                + "V12__inventory_location_transfer.sql"
        );

        assertTrue(
                Files.isRegularFile(v12),
                "Migrazione storica V12 mancante."
        );

        String v12Sql = Files.readString(v12)
                .replaceAll("(?m)--[^\\r\\n]*", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");

        require(
                v12Sql,
                "`source_location_id` bigint not null"
        );

        require(
                v12Sql,
                "`target_location_id` bigint not null"
        );

        require(
                v12Sql,
                "constraint `chk_location_transfer_locations` "
                        + "check (`source_location_id` "
                        + "<> `target_location_id`)"
        );
    }

    private static void require(String sql, String fragment) {
        assertTrue(
                sql.contains(fragment),
                "STOP-ANW-SCHEMA-CONTRATTO-INCOMPLETO: "
                + fragment
        );
    }
}
