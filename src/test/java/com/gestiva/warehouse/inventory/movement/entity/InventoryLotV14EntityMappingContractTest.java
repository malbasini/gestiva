package com.gestiva.warehouse.inventory.movement.entity;

import com.gestiva.common.model.BaseEntity;
import com.gestiva.common.model.TenantAwareEntity;
import com.gestiva.logistics.ddt.entity.DeliveryNoteLineLot;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLineLot;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLotV14EntityMappingContractTest {

    private static void assertEntity(
            Class<?> type,
            String tableName
    ) throws Exception {

        assertNotNull(type.getAnnotation(Entity.class));

        Table table = type.getAnnotation(Table.class);
        assertNotNull(table);
        assertEquals(tableName, table.name());

        assertEquals(
                TenantAwareEntity.class,
                type.getSuperclass()
        );

        assertNotNull(type.getDeclaredConstructor());
    }

    private static Column assertColumn(
            Class<?> owner,
            String fieldName,
            Class<?> fieldType,
            String columnName,
            boolean nullable
    ) throws Exception {

        Field field = owner.getDeclaredField(fieldName);

        assertEquals(fieldType, field.getType());
        assertTrue(Modifier.isPrivate(field.getModifiers()));

        Column column = field.getAnnotation(Column.class);

        assertNotNull(column);
        assertEquals(columnName, column.name());
        assertEquals(nullable, column.nullable());

        return column;
    }

    private static void assertDecimal(
            Class<?> owner,
            String fieldName,
            String columnName,
            boolean nullable,
            int scale
    ) throws Exception {

        Column column = assertColumn(
                owner,
                fieldName,
                BigDecimal.class,
                columnName,
                nullable
        );

        assertEquals(15, column.precision());
        assertEquals(scale, column.scale());
    }

    private static void assertLotMapping(
            Class<?> type,
            boolean nullable
    ) throws Exception {

        assertColumn(
                type,
                "lotId",
                Long.class,
                "lot_id",
                nullable
        );

        assertEquals(
                Long.class,
                type.getMethod("getLotId").getReturnType()
        );

        assertEquals(
                void.class,
                type.getMethod(
                        "setLotId",
                        Long.class
                ).getReturnType()
        );
    }

    private static void assertLotAccessor(
            Class<?> type
    ) throws Exception {

        Object entity = type.getDeclaredConstructor()
                .newInstance();

        var getter = type.getMethod("getLotId");

        var setter = type.getMethod(
                "setLotId",
                Long.class
        );

        assertNull(getter.invoke(entity));

        setter.invoke(entity, 25L);
        assertEquals(25L, getter.invoke(entity));

        setter.invoke(entity, 26L);
        assertEquals(26L, getter.invoke(entity));

        setter.invoke(entity, (Object) null);
        assertNull(getter.invoke(entity));
    }

    private static void assertUniqueConstraints(
            Class<?> type,
            String lineConstraint,
            String lineColumn,
            String movementConstraint
    ) {

        UniqueConstraint[] constraints =
                type.getAnnotation(Table.class)
                        .uniqueConstraints();

        assertEquals(2, constraints.length);

        UniqueConstraint line = Arrays.stream(constraints)
                .filter(c -> c.name().equals(lineConstraint))
                .findFirst()
                .orElseThrow();

        assertArrayEquals(
                new String[]{lineColumn, "lot_id"},
                line.columnNames()
        );

        UniqueConstraint movement = Arrays.stream(constraints)
                .filter(c -> c.name().equals(movementConstraint))
                .findFirst()
                .orElseThrow();

        assertArrayEquals(
                new String[]{"movement_id"},
                movement.columnNames()
        );
    }

    private static void assertTraceIndex(
            Class<?> type,
            String expectedName
    ) {

        Index[] indexes = type.getAnnotation(Table.class)
                .indexes();

        assertEquals(1, indexes.length);
        assertEquals(expectedName, indexes[0].name());

        assertEquals(
                "tenant_id,item_id,lot_id,company_id",
                indexes[0].columnList().replace(" ", "")
        );

        assertFalse(indexes[0].unique());
    }

    // 01-05: Identita delle entita JPA

    @Test
    void movementEntityIdentity() throws Exception {
        assertEntity(
                InventoryMovement.class,
                "inventory_movement"
        );
    }

    @Test
    void layerEntityIdentity() throws Exception {
        assertEntity(
                InventoryLayer.class,
                "inventory_layer"
        );
    }

    @Test
    void transferEntityIdentity() throws Exception {
        assertEntity(
                InventoryLocationTransfer.class,
                "inventory_location_transfer"
        );
    }

    @Test
    void goodsReceiptLotEntityIdentity() throws Exception {
        assertEntity(
                GoodsReceiptLineLot.class,
                "goods_receipt_line_lot"
        );
    }

    @Test
    void deliveryNoteLotEntityIdentity() throws Exception {
        assertEntity(
                DeliveryNoteLineLot.class,
                "delivery_note_line_lot"
        );
    }

    // 06-10: Mapping lotId

    @Test
    void movementLotMappingIsNullable() throws Exception {
        assertLotMapping(InventoryMovement.class, true);
    }

    @Test
    void layerLotMappingIsNullable() throws Exception {
        assertLotMapping(InventoryLayer.class, true);
    }

    @Test
    void transferLotMappingIsNullable() throws Exception {
        assertLotMapping(
                InventoryLocationTransfer.class,
                true
        );
    }

    @Test
    void goodsReceiptLotMappingIsRequired() throws Exception {
        assertLotMapping(GoodsReceiptLineLot.class, false);
    }

    @Test
    void deliveryNoteLotMappingIsRequired() throws Exception {
        assertLotMapping(DeliveryNoteLineLot.class, false);
    }

    // 11-15: Comportamento degli accessor

    @Test
    void movementLotAccessor() throws Exception {
        assertLotAccessor(InventoryMovement.class);
    }

    @Test
    void layerLotAccessor() throws Exception {
        assertLotAccessor(InventoryLayer.class);
    }

    @Test
    void transferLotAccessor() throws Exception {
        assertLotAccessor(InventoryLocationTransfer.class);
    }

    @Test
    void goodsReceiptLotAccessor() throws Exception {
        assertLotAccessor(GoodsReceiptLineLot.class);
    }

    @Test
    void deliveryNoteLotAccessor() throws Exception {
        assertLotAccessor(DeliveryNoteLineLot.class);
    }

    // 16-18: Quantita inventariali

    @Test
    void movementQuantityMapping() throws Exception {

        assertDecimal(
                InventoryMovement.class,
                "quantity",
                "quantity",
                false,
                3
        );
    }

    @Test
    void layerQuantityMapping() throws Exception {

        assertDecimal(
                InventoryLayer.class,
                "originalQty",
                "original_qty",
                false,
                3
        );

        assertDecimal(
                InventoryLayer.class,
                "remainingQty",
                "remaining_qty",
                false,
                3
        );
    }

    @Test
    void transferQuantityMapping() throws Exception {

        assertDecimal(
                InventoryLocationTransfer.class,
                "quantity",
                "quantity",
                false,
                3
        );
    }

    // 19-20: Costi inventariali

    @Test
    void movementCostMapping() throws Exception {

        assertDecimal(
                InventoryMovement.class,
                "unitCost",
                "unit_cost",
                true,
                4
        );

        assertDecimal(
                InventoryMovement.class,
                "totalCost",
                "total_cost",
                true,
                4
        );
    }

    @Test
    void layerCostMapping() throws Exception {

        assertDecimal(
                InventoryLayer.class,
                "unitCost",
                "unit_cost",
                false,
                4
        );
    }

    // 21-22: Mapping completo documentale

    @Test
    void goodsReceiptLotCompleteMapping() throws Exception {

        Class<?> type = GoodsReceiptLineLot.class;

        assertEquals(
                8,
                type.getDeclaredFields().length
        );

        assertColumn(
                type, "companyId", Long.class,
                "company_id", false
        );

        assertColumn(
                type, "goodsReceiptLineId", Long.class,
                "goods_receipt_line_id", false
        );

        assertColumn(
                type, "itemId", Long.class,
                "item_id", false
        );

        assertLotMapping(type, false);

        assertColumn(
                type, "movementId", Long.class,
                "movement_id", true
        );

        assertDecimal(
                type, "quantity",
                "quantity", false, 3
        );

        assertDecimal(
                type, "unitCost",
                "unit_cost", false, 4
        );

        assertDecimal(
                type, "totalCost",
                "total_cost", false, 4
        );
    }

    @Test
    void deliveryNoteLotCompleteMapping() throws Exception {

        Class<?> type = DeliveryNoteLineLot.class;

        assertEquals(
                6,
                type.getDeclaredFields().length
        );

        assertColumn(
                type, "companyId", Long.class,
                "company_id", false
        );

        assertColumn(
                type, "deliveryNoteLineId", Long.class,
                "delivery_note_line_id", false
        );

        assertColumn(
                type, "itemId", Long.class,
                "item_id", false
        );

        assertLotMapping(type, false);

        assertColumn(
                type, "movementId", Long.class,
                "movement_id", true
        );

        assertDecimal(
                type, "quantity",
                "quantity", false, 3
        );
    }

    // 23-24: Vincoli UNIQUE

    @Test
    void goodsReceiptLotUniqueConstraints() {

        assertUniqueConstraints(
                GoodsReceiptLineLot.class,
                "uk_gr_line_lot",
                "goods_receipt_line_id",
                "uk_gr_line_lot_movement"
        );
    }

    @Test
    void deliveryNoteLotUniqueConstraints() {

        assertUniqueConstraints(
                DeliveryNoteLineLot.class,
                "uk_ddt_line_lot",
                "delivery_note_line_id",
                "uk_ddt_line_lot_movement"
        );
    }

    // 25-26: Indici di tracciabilita

    @Test
    void goodsReceiptLotTraceIndex() {

        assertTraceIndex(
                GoodsReceiptLineLot.class,
                "idx_gr_line_lot_trace"
        );
    }

    @Test
    void deliveryNoteLotTraceIndex() {

        assertTraceIndex(
                DeliveryNoteLineLot.class,
                "idx_ddt_line_lot_trace"
        );
    }

    // 27: Assenza duplicazione costi

    @Test
    void deliveryNoteLotDoesNotDuplicateFifoCosts() {

        Class<?> type = DeliveryNoteLineLot.class;

        assertThrows(
                NoSuchFieldException.class,
                () -> type.getDeclaredField("unitCost")
        );

        assertThrows(
                NoSuchFieldException.class,
                () -> type.getDeclaredField("totalCost")
        );

        assertThrows(
                NoSuchMethodException.class,
                () -> type.getMethod("getUnitCost")
        );

        assertThrows(
                NoSuchMethodException.class,
                () -> type.getMethod("getTotalCost")
        );
    }

    // 28: Gerarchia comune e timestamp

    @Test
    void inheritedTenantIdentityAndTimestampMapping()
            throws Exception {

        assertNotNull(
                BaseEntity.class.getAnnotation(
                        MappedSuperclass.class
                )
        );

        assertNotNull(
                TenantAwareEntity.class.getAnnotation(
                        MappedSuperclass.class
                )
        );

        assertEquals(
                BaseEntity.class,
                TenantAwareEntity.class.getSuperclass()
        );

        assertEquals(
                AccessType.FIELD,
                BaseEntity.class.getAnnotation(Access.class)
                        .value()
        );

        assertEquals(
                AccessType.FIELD,
                TenantAwareEntity.class
                        .getAnnotation(Access.class)
                        .value()
        );

        Field id = BaseEntity.class.getDeclaredField("id");

        assertEquals(Long.class, id.getType());
        assertNotNull(id.getAnnotation(Id.class));

        assertEquals(
                GenerationType.IDENTITY,
                id.getAnnotation(GeneratedValue.class)
                        .strategy()
        );

        assertColumn(
                TenantAwareEntity.class,
                "tenantId",
                Long.class,
                "tenant_id",
                false
        );

        Column created = assertColumn(
                BaseEntity.class,
                "createdAt",
                LocalDateTime.class,
                "created_at",
                false
        );

        assertFalse(created.updatable());

        assertColumn(
                BaseEntity.class,
                "updatedAt",
                LocalDateTime.class,
                "updated_at",
                false
        );

        assertNotNull(
                BaseEntity.class.getDeclaredMethod("onCreate")
                        .getAnnotation(PrePersist.class)
        );

        assertNotNull(
                BaseEntity.class.getDeclaredMethod("onUpdate")
                        .getAnnotation(PreUpdate.class)
        );
    }

    // 29-30: Comportamento oggetti documentali

    @Test
    void goodsReceiptLotRetainsAssignedValues() {

        GoodsReceiptLineLot lot =
                new GoodsReceiptLineLot();

        lot.setTenantId(2L);
        lot.setCompanyId(1L);
        lot.setGoodsReceiptLineId(100L);
        lot.setItemId(10L);
        lot.setLotId(25L);
        lot.setQuantity(new BigDecimal("40.000"));
        lot.setUnitCost(new BigDecimal("7.2000"));
        lot.setTotalCost(new BigDecimal("288.0000"));

        assertNull(lot.getMovementId());

        lot.setMovementId(500L);

        assertEquals(2L, lot.getTenantId());
        assertEquals(1L, lot.getCompanyId());
        assertEquals(100L, lot.getGoodsReceiptLineId());
        assertEquals(10L, lot.getItemId());
        assertEquals(25L, lot.getLotId());

        assertEquals(
                new BigDecimal("40.000"),
                lot.getQuantity()
        );

        assertEquals(
                new BigDecimal("7.2000"),
                lot.getUnitCost()
        );

        assertEquals(
                new BigDecimal("288.0000"),
                lot.getTotalCost()
        );

        assertEquals(500L, lot.getMovementId());
    }

    @Test
    void deliveryNoteLotRetainsAssignedValues() {

        DeliveryNoteLineLot lot =
                new DeliveryNoteLineLot();

        lot.setTenantId(2L);
        lot.setCompanyId(1L);
        lot.setDeliveryNoteLineId(200L);
        lot.setItemId(10L);
        lot.setLotId(25L);
        lot.setQuantity(new BigDecimal("20.000"));

        assertNull(lot.getMovementId());

        lot.setMovementId(501L);

        assertEquals(2L, lot.getTenantId());
        assertEquals(1L, lot.getCompanyId());
        assertEquals(200L, lot.getDeliveryNoteLineId());
        assertEquals(10L, lot.getItemId());
        assertEquals(25L, lot.getLotId());

        assertEquals(
                new BigDecimal("20.000"),
                lot.getQuantity()
        );

        assertEquals(501L, lot.getMovementId());
    }
}
