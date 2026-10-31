-- Gestiva V2 - UOM snapshot ciclo attivo
-- Solo struttura: nessuna trasformazione dei dati.

ALTER TABLE quote_line
    ADD COLUMN uom_id BIGINT NULL,
    ADD COLUMN base_uom_code VARCHAR(20) NULL,
    ADD COLUMN conversion_factor DECIMAL(19,6) NULL;

ALTER TABLE sales_order_line
    ADD COLUMN uom_id BIGINT NULL,
    ADD COLUMN base_uom_code VARCHAR(20) NULL,
    ADD COLUMN conversion_factor DECIMAL(19,6) NULL;

ALTER TABLE delivery_note_line
    ADD COLUMN uom_id BIGINT NULL,
    ADD COLUMN base_uom_code VARCHAR(20) NULL,
    ADD COLUMN conversion_factor DECIMAL(19,6) NULL;

ALTER TABLE invoice_line
    ADD COLUMN uom_id BIGINT NULL,
    ADD COLUMN base_uom_code VARCHAR(20) NULL,
    ADD COLUMN conversion_factor DECIMAL(19,6) NULL;
