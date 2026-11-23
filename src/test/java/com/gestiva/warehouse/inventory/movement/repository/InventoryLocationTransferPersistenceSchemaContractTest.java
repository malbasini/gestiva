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

    private static void require(String sql, String fragment) {
        assertTrue(
                sql.contains(fragment),
                "STOP-ANW-SCHEMA-CONTRATTO-INCOMPLETO: "
                + fragment
        );
    }
}
