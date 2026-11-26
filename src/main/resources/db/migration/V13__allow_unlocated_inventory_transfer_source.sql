-- GESTIVA ERP V2
-- V13: assegnazione delle scorte senza ubicazione.
--
-- NULL identifica esclusivamente una sorgente
-- priva di ubicazione fisica.
--
-- La destinazione deve rimanere obbligatoria.
-- Nessuna modifica ai dati storici o ai layer FIFO.

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
                    AND `source_location_id` <> `target_location_id`
                )
            )
        );
