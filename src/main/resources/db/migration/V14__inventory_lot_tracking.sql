-- =========================================================
-- GESTIVA ERP V2
-- V14 - Inventory lot tracking
--
-- Estensione additiva delle strutture inventariali V13.
-- Conservazione integrale delle movimentazioni storiche.
-- Applicazione manuale: SANDBOX prima di TEST.
-- =========================================================


-- =========================================================
-- 1. INVENTORY LOT
-- =========================================================

ALTER TABLE inventory_lot

    ADD CONSTRAINT uk_inventory_lot_context
        UNIQUE (tenant_id, item_id, id),

    ADD CONSTRAINT chk_inventory_lot_dates
        CHECK (
            production_date IS NULL
            OR expiration_date IS NULL
            OR production_date <= expiration_date
        );


-- =========================================================
-- 2. INVENTORY MOVEMENT
-- =========================================================

ALTER TABLE inventory_movement

    ADD COLUMN lot_id BIGINT DEFAULT NULL
        AFTER item_id,

    ADD KEY idx_inv_mov_lot_stock (
        tenant_id,
        company_id,
        warehouse_id,
        item_id,
        location_id,
        lot_id
    ),

    ADD KEY idx_inv_mov_lot_history (
        tenant_id,
        item_id,
        lot_id,
        company_id,
        movement_date,
        id
    ),

    ADD CONSTRAINT fk_inv_movement_lot_context
        FOREIGN KEY (tenant_id, item_id, lot_id)
        REFERENCES inventory_lot (tenant_id, item_id, id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT;


-- =========================================================
-- 3. INVENTORY LAYER
-- =========================================================

ALTER TABLE inventory_layer

    ADD COLUMN lot_id BIGINT DEFAULT NULL
        AFTER item_id,

    ADD KEY idx_inv_layer_lot_fifo (
        tenant_id,
        company_id,
        warehouse_id,
        item_id,
        location_id,
        lot_id,
        closed,
        layer_date,
        id
    ),

    ADD KEY idx_inv_layer_lot_context (
        tenant_id,
        item_id,
        lot_id,
        company_id,
        closed
    ),

    ADD CONSTRAINT fk_inv_layer_lot_context
        FOREIGN KEY (tenant_id, item_id, lot_id)
        REFERENCES inventory_lot (tenant_id, item_id, id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT;


-- =========================================================
-- 4. INVENTORY LOCATION TRANSFER
-- =========================================================

ALTER TABLE inventory_location_transfer

    ADD COLUMN lot_id BIGINT DEFAULT NULL
        AFTER item_id,

    ADD KEY idx_inv_transfer_lot_context (
        tenant_id,
        item_id,
        lot_id,
        company_id,
        warehouse_id,
        movement_date,
        id
    ),

    ADD CONSTRAINT fk_inv_transfer_lot_context
        FOREIGN KEY (tenant_id, item_id, lot_id)
        REFERENCES inventory_lot (tenant_id, item_id, id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT;


-- =========================================================
-- 5. GOODS RECEIPT LINE LOT
-- =========================================================

CREATE TABLE goods_receipt_line_lot (

    id BIGINT NOT NULL AUTO_INCREMENT,

    tenant_id BIGINT NOT NULL,

    company_id BIGINT NOT NULL,

    goods_receipt_line_id BIGINT NOT NULL,

    item_id BIGINT NOT NULL,

    lot_id BIGINT NOT NULL,

    quantity DECIMAL(15,3) NOT NULL,

    unit_cost DECIMAL(15,4) NOT NULL,

    total_cost DECIMAL(15,4) NOT NULL,

    movement_id BIGINT DEFAULT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    PRIMARY KEY (id),

    UNIQUE KEY uk_gr_line_lot (
        goods_receipt_line_id,
        lot_id
    ),

    UNIQUE KEY uk_gr_line_lot_movement (
        movement_id
    ),

    KEY idx_gr_line_lot_trace (
        tenant_id,
        item_id,
        lot_id,
        company_id
    ),

    CONSTRAINT fk_gr_line_lot_receipt_line
        FOREIGN KEY (goods_receipt_line_id)
        REFERENCES goods_receipt_line (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_gr_line_lot_context
        FOREIGN KEY (tenant_id, item_id, lot_id)
        REFERENCES inventory_lot (tenant_id, item_id, id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_gr_line_lot_movement
        FOREIGN KEY (movement_id)
        REFERENCES inventory_movement (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT chk_gr_line_lot_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_gr_line_lot_unit_cost
        CHECK (unit_cost > 0),

    CONSTRAINT chk_gr_line_lot_total_cost
        CHECK (total_cost > 0)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


-- =========================================================
-- 6. DELIVERY NOTE LINE LOT
-- =========================================================

CREATE TABLE delivery_note_line_lot (

    id BIGINT NOT NULL AUTO_INCREMENT,

    tenant_id BIGINT NOT NULL,

    company_id BIGINT NOT NULL,

    delivery_note_line_id BIGINT NOT NULL,

    item_id BIGINT NOT NULL,

    lot_id BIGINT NOT NULL,

    quantity DECIMAL(15,3) NOT NULL,

    movement_id BIGINT DEFAULT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    PRIMARY KEY (id),

    UNIQUE KEY uk_ddt_line_lot (
        delivery_note_line_id,
        lot_id
    ),

    UNIQUE KEY uk_ddt_line_lot_movement (
        movement_id
    ),

    KEY idx_ddt_line_lot_trace (
        tenant_id,
        item_id,
        lot_id,
        company_id
    ),

    CONSTRAINT fk_ddt_line_lot_delivery_line
        FOREIGN KEY (delivery_note_line_id)
        REFERENCES delivery_note_line (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_ddt_line_lot_context
        FOREIGN KEY (tenant_id, item_id, lot_id)
        REFERENCES inventory_lot (tenant_id, item_id, id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_ddt_line_lot_movement
        FOREIGN KEY (movement_id)
        REFERENCES inventory_movement (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT chk_ddt_line_lot_quantity
        CHECK (quantity > 0)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


-- =========================================================
-- END V14
-- =========================================================
