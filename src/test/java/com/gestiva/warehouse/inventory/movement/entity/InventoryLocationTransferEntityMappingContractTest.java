package com.gestiva.warehouse.inventory.movement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferEntityMappingContractTest {

    @Test
    void transferEntitiesMustMatchPersistenceSchema()
            throws Exception {

        Class<?> transfer = requireClass(
                "InventoryLocationTransfer"
        );

        Class<?> allocation = requireClass(
                "InventoryLocationTransferAllocation"
        );

        verifyEntity(
                transfer,
                "inventory_location_transfer"
        );

        verifyEntity(
                allocation,
                "inventory_location_transfer_allocation"
        );

        verifyColumn(transfer, "companyId", "company_id");
        verifyColumn(transfer, "warehouseId", "warehouse_id");
        verifyColumn(transfer, "itemId", "item_id");

        verifyColumn(
                transfer, "sourceLocationId", "source_location_id"
        );

        verifyColumn(
                transfer, "targetLocationId", "target_location_id"
        );

        verifyColumn(transfer, "quantity", "quantity");
        verifyColumn(transfer, "movementDate", "movement_date");
        verifyColumn(transfer, "operationKey", "operation_key");
        verifyColumn(transfer, "status", "status");
        verifyColumn(transfer, "outMovementId", "out_movement_id");
        verifyColumn(transfer, "inMovementId", "in_movement_id");
        verifyColumn(transfer, "notes", "notes");

        verifyColumn(allocation, "companyId", "company_id");
        verifyColumn(allocation, "warehouseId", "warehouse_id");
        verifyColumn(allocation, "transferId", "transfer_id");

        verifyColumn(
                allocation, "sourceLayerId", "source_layer_id"
        );

        verifyColumn(
                allocation, "targetLayerId", "target_layer_id"
        );

        verifyColumn(allocation, "allocatedQty", "allocated_qty");
        verifyColumn(allocation, "unitCost", "unit_cost");
        verifyColumn(allocation, "layerDate", "layer_date");
    }

    private static Class<?> requireClass(String simpleName)
            throws Exception {

        String name =
                "com.gestiva.warehouse.inventory.movement.entity."
                + simpleName;

        try {
            return Class.forName(name);
        } catch (ClassNotFoundException exception) {
            fail("STOP-ANY-JPA-ENTITY-NON-IMPLEMENTATA: "
                    + simpleName);
            throw exception;
        }
    }

    private static void verifyEntity(
            Class<?> type,
            String tableName
    ) {

        boolean tenantAware = false;

        Class<?> superclass = type.getSuperclass();

        while (superclass != null) {
            if ("TenantAwareEntity".equals(
                    superclass.getSimpleName())) {
                tenantAware = true;
                break;
            }

            superclass = superclass.getSuperclass();
        }

        assertTrue(
                tenantAware,
                "L'entità deve ereditare l'isolamento tenant: "
                        + type.getSimpleName()
        );

        assertNotNull(
                type.getAnnotation(Entity.class),
                "@Entity mancante: " + type.getSimpleName()
        );

        Table table = type.getAnnotation(Table.class);

        assertNotNull(
                table,
                "@Table mancante: " + type.getSimpleName()
        );

        assertEquals(tableName, table.name());
    }

    private static void verifyColumn(
            Class<?> type,
            String fieldName,
            String columnName
    ) throws Exception {

        Field field = type.getDeclaredField(fieldName);
        Column column = field.getAnnotation(Column.class);

        assertNotNull(
                column,
                "@Column mancante: " + fieldName
        );

        assertEquals(
                columnName,
                column.name(),
                "Colonna non conforme: " + fieldName
        );
    }
}
