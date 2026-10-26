-- Gestiva V2
-- PASSO 5.13.249
--
-- Unita di misura commerciale e fattore storico
-- nelle righe degli ordini fornitori.
--
-- Le colonne sono nullable per compatibilita
-- con gli ordini creati prima della migrazione.
--
-- Nessun backfill automatico.
-- Nessuna modifica ai prezzi o alle quantita storiche.

ALTER TABLE purchase_order_line
    ADD COLUMN uom_id BIGINT NULL AFTER item_id,
    ADD COLUMN conversion_factor DECIMAL(19,6) NULL AFTER uom_id;
