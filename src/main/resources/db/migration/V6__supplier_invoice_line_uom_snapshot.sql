-- Gestiva V2: snapshot UOM commerciale e base
-- delle righe delle fatture fornitori.
-- Colonne nullable per compatibilita con documenti storici.

ALTER TABLE supplier_invoice_line
    ADD COLUMN uom_id BIGINT NULL,
    ADD COLUMN uom_code VARCHAR(20) NULL,
    ADD COLUMN base_uom_code VARCHAR(20) NULL,
    ADD COLUMN conversion_factor DECIMAL(19,6) NULL;
