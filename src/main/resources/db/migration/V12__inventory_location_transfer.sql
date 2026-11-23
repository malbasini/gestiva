-- GESTIVA ERP V2
-- V12: trasferimenti FIFO tra ubicazioni.
--
-- Schema predisposto, non abilita il posting.
-- OUT, IN, consumi FIFO e layer di destinazione
-- dovranno essere persistiti nella stessa transazione.
--
-- L'operation_key identifica una singola richiesta
-- nell'ambito di tenant e societa'.

CREATE TABLE `inventory_location_transfer` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `tenant_id` bigint NOT NULL,
    `company_id` bigint NOT NULL,
    `warehouse_id` bigint NOT NULL,
    `item_id` bigint NOT NULL,
    `source_location_id` bigint NOT NULL,
    `target_location_id` bigint NOT NULL,
    `quantity` decimal(15,3) NOT NULL,
    `movement_date` date NOT NULL,
    `operation_key` varchar(120) NOT NULL,
    `status` varchar(20) NOT NULL,
    `out_movement_id` bigint DEFAULT NULL,
    `in_movement_id` bigint DEFAULT NULL,
    `notes` varchar(1000) DEFAULT NULL,
    `created_at` timestamp NOT NULL,
    `updated_at` timestamp NOT NULL,

    PRIMARY KEY (`id`),

    UNIQUE KEY `uk_location_transfer_operation`
        (`tenant_id`,`company_id`,`operation_key`),

    UNIQUE KEY `uk_location_transfer_out_movement`
        (`out_movement_id`),

    UNIQUE KEY `uk_location_transfer_in_movement`
        (`in_movement_id`),

    KEY `idx_location_transfer_context`
        (`tenant_id`,`company_id`,`warehouse_id`,`item_id`),

    KEY `idx_location_transfer_date`
        (`tenant_id`,`company_id`,`movement_date`),

    CONSTRAINT `chk_location_transfer_quantity`
        CHECK (`quantity` > 0),

    CONSTRAINT `chk_location_transfer_locations`
        CHECK (`source_location_id` <> `target_location_id`),

    CONSTRAINT `chk_location_transfer_status`
        CHECK (`status` IN ('PENDING', 'POSTED'))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `inventory_location_transfer_allocation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `tenant_id` bigint NOT NULL,
    `company_id` bigint NOT NULL,
    `warehouse_id` bigint NOT NULL,
    `transfer_id` bigint NOT NULL,
    `source_layer_id` bigint NOT NULL,
    `target_layer_id` bigint DEFAULT NULL,
    `allocated_qty` decimal(15,3) NOT NULL,
    `unit_cost` decimal(15,4) NOT NULL,
    `layer_date` date NOT NULL,
    `created_at` timestamp NOT NULL,
    `updated_at` timestamp NOT NULL,

    PRIMARY KEY (`id`),

    UNIQUE KEY `uk_location_transfer_source_layer`
        (`transfer_id`,`source_layer_id`),

    UNIQUE KEY `uk_location_transfer_target_layer`
        (`target_layer_id`),

    KEY `idx_location_transfer_allocation_context`
        (`tenant_id`,`company_id`,`warehouse_id`,`transfer_id`),

    KEY `idx_location_transfer_allocation_source`
        (`tenant_id`,`company_id`,`source_layer_id`),

    CONSTRAINT `fk_location_transfer_allocation_transfer`
        FOREIGN KEY (`transfer_id`)
        REFERENCES `inventory_location_transfer` (`id`),

    CONSTRAINT `chk_location_transfer_allocated_qty`
        CHECK (`allocated_qty` > 0),

    CONSTRAINT `chk_location_transfer_unit_cost`
        CHECK (`unit_cost` > 0)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;
