-- GESTIVA V2 - SCHEMA INIZIALE CANDIDATO
-- Tabelle ordinate secondo le dipendenze delle foreign key.
-- Nessun dato applicativo incluso.
-- File non ancora validato mediante esecuzione su database vuoto.

-- MySQL dump 10.13  Distrib 8.0.45, for macos15 (arm64)
--
-- Host: localhost    Database: gestiva_flyway_test
-- ------------------------------------------------------
-- Server version	8.0.45


--
-- Table structure for table `account`
--

CREATE TABLE `account` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `company_id` bigint DEFAULT NULL,
  `code` varchar(30) NOT NULL,
  `name` varchar(180) NOT NULL,
  `account_type` varchar(30) NOT NULL,
  `nature` varchar(10) NOT NULL,
  `parent_id` bigint DEFAULT NULL,
  `level_no` int NOT NULL,
  `leaf_account` bit(1) NOT NULL,
  `system_account` bit(1) NOT NULL,
  `active` bit(1) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_tenant_company_code` (`tenant_id`,(coalesce(`company_id`,0)),`code`),
  KEY `idx_account_tenant_type` (`tenant_id`,`account_type`),
  KEY `idx_account_tenant_active` (`tenant_id`,`active`)
) ENGINE=InnoDB AUTO_INCREMENT=210 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `accounting_entry`
--

CREATE TABLE `accounting_entry` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `entry_number` varchar(50) NOT NULL,
  `entry_date` date NOT NULL,
  `causal_code` varchar(40) NOT NULL,
  `description` varchar(500) NOT NULL,
  `reference_type` varchar(40) DEFAULT NULL,
  `reference_id` bigint DEFAULT NULL,
  `currency_code` varchar(3) NOT NULL,
  `total_amount` decimal(15,2) NOT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_accounting_entry_tenant_number` (`tenant_id`,`entry_number`),
  KEY `idx_accounting_entry_tenant_date` (`tenant_id`,`entry_date`),
  KEY `idx_accounting_entry_tenant_causal` (`tenant_id`,`causal_code`),
  KEY `idx_accounting_entry_tenant_ref` (`tenant_id`,`reference_type`,`reference_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `accounting_entry_line`
--

CREATE TABLE `accounting_entry_line` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `accounting_entry_id` bigint NOT NULL,
  `line_no` int NOT NULL,
  `line_type` varchar(20) NOT NULL,
  `description` varchar(500) NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_accounting_entry_line_tenant_entry` (`tenant_id`,`accounting_entry_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `attachment`
--

CREATE TABLE `attachment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `content_type` varchar(120) NOT NULL,
  `entity_id` bigint NOT NULL,
  `entity_name` varchar(80) NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `size_bytes` bigint NOT NULL,
  `storage_path` varchar(500) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `billing_order`
--

CREATE TABLE `billing_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `plan_code` varchar(50) NOT NULL,
  `provider` varchar(30) NOT NULL,
  `provider_order_id` varchar(120) DEFAULT NULL,
  `status` varchar(30) NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `currency_code` varchar(10) NOT NULL,
  `approval_url` varchar(1000) DEFAULT NULL,
  `provider_payer_id` varchar(120) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `completed_at` datetime DEFAULT NULL,
  `raw_payload` longtext,
  PRIMARY KEY (`id`),
  KEY `idx_billing_order_tenant` (`tenant_id`),
  KEY `idx_billing_order_provider_order` (`provider_order_id`),
  KEY `idx_billing_order_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `contact_person`
--

CREATE TABLE `contact_person` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `customer_id` bigint DEFAULT NULL,
  `email` varchar(180) DEFAULT NULL,
  `first_name` varchar(120) NOT NULL,
  `last_name` varchar(120) NOT NULL,
  `phone` varchar(50) DEFAULT NULL,
  `role_title` varchar(120) DEFAULT NULL,
  `supplier_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `crm_activity`
--

CREATE TABLE `crm_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `assigned_user_id` bigint DEFAULT NULL,
  `customer_id` bigint DEFAULT NULL,
  `due_at` datetime(6) DEFAULT NULL,
  `lead_id` bigint DEFAULT NULL,
  `notes` longtext,
  `opportunity_id` bigint DEFAULT NULL,
  `status` varchar(30) NOT NULL,
  `subject` varchar(200) NOT NULL,
  `type` varchar(40) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `crm_lead`
--

CREATE TABLE `crm_lead` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `assigned_to` varchar(80) DEFAULT NULL,
  `company_name` varchar(180) NOT NULL,
  `contact_name` varchar(120) DEFAULT NULL,
  `email` varchar(180) DEFAULT NULL,
  `estimated_value` decimal(15,2) DEFAULT NULL,
  `expected_close_date` date DEFAULT NULL,
  `phone` varchar(50) DEFAULT NULL,
  `source` varchar(40) NOT NULL,
  `status` varchar(40) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `crm_note`
--

CREATE TABLE `crm_note` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `author_user_id` bigint NOT NULL,
  `content` tinytext NOT NULL,
  `customer_id` bigint DEFAULT NULL,
  `lead_id` bigint DEFAULT NULL,
  `opportunity_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `delivery_note`
--

CREATE TABLE `delivery_note` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `sales_order_id` bigint NOT NULL,
  `customer_id` bigint NOT NULL,
  `ddt_number` varchar(50) NOT NULL,
  `ddt_date` date NOT NULL,
  `status` varchar(30) NOT NULL,
  `transport_reason` varchar(120) DEFAULT NULL,
  `carriage_condition` varchar(120) DEFAULT NULL,
  `carrier_name` varchar(180) DEFAULT NULL,
  `currency_code` varchar(10) NOT NULL,
  `subtotal_amount` decimal(15,2) NOT NULL,
  `tax_amount` decimal(15,2) NOT NULL,
  `total_amount` decimal(15,2) NOT NULL,
  `notes` varchar(2000) DEFAULT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  `warehouse_id` bigint DEFAULT NULL,
  `location_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_delivery_note_tenant_company_number` (`tenant_id`,`company_id`,`ddt_number`),
  KEY `idx_delivery_note_tenant` (`tenant_id`),
  KEY `idx_delivery_note_sales_order` (`tenant_id`,`sales_order_id`),
  KEY `idx_delivery_note_customer` (`tenant_id`,`customer_id`),
  KEY `idx_delivery_note_date` (`tenant_id`,`ddt_date`),
  KEY `idx_delivery_note_company_date` (`tenant_id`,`company_id`,`ddt_date`),
  KEY `idx_delivery_note_company_sales_order` (`tenant_id`,`company_id`,`sales_order_id`),
  KEY `idx_delivery_note_company_customer` (`tenant_id`,`company_id`,`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `document_sequence`
--

CREATE TABLE `document_sequence` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `document_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `prefix` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `next_number` bigint NOT NULL,
  `padding_size` int NOT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_document_sequence_tenant_company_type` (`tenant_id`,`company_id`,`document_type`),
  KEY `idx_document_sequence_tenant` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--

-- Table structure for table `goods_receipt`
--

CREATE TABLE `goods_receipt` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `receipt_number` varchar(50) NOT NULL,
  `receipt_date` date NOT NULL,
  `purchase_order_id` bigint NOT NULL,
  `supplier_id` bigint NOT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  `warehouse_id` bigint DEFAULT NULL,
  `location_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_goods_receipt_tenant_company_number` (`tenant_id`,`company_id`,`receipt_number`),
  KEY `idx_goods_receipt_order` (`tenant_id`,`purchase_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `inventory_average_balance`
--

CREATE TABLE `inventory_average_balance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `item_id` bigint NOT NULL,
  `current_qty` decimal(15,3) NOT NULL,
  `current_total_value` decimal(15,4) NOT NULL,
  `current_avg_unit_cost` decimal(15,4) NOT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inv_avg_balance_tenant_item` (`tenant_id`,`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `inventory_layer`
--

CREATE TABLE `inventory_layer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `warehouse_id` bigint NOT NULL,
  `location_id` bigint DEFAULT NULL,
  `item_id` bigint NOT NULL,
  `source_movement_id` bigint NOT NULL,
  `layer_date` date NOT NULL,
  `original_qty` decimal(15,3) NOT NULL,
  `remaining_qty` decimal(15,3) NOT NULL,
  `unit_cost` decimal(15,4) NOT NULL,
  `closed` bit(1) NOT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_inv_layer_tenant_item_date` (`tenant_id`,`item_id`,`layer_date`,`id`),
  KEY `idx_inv_layer_tenant_item_open` (`tenant_id`,`item_id`,`closed`),
  KEY `idx_inv_layer_source_movement` (`tenant_id`,`source_movement_id`),
  KEY `idx_inv_layer_company_item_fifo` (`tenant_id`,`company_id`,`warehouse_id`,`item_id`,`closed`,`layer_date`,`id`),
  KEY `idx_inv_layer_company_source` (`tenant_id`,`company_id`,`source_movement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `inventory_layer_consumption`
--

CREATE TABLE `inventory_layer_consumption` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `warehouse_id` bigint NOT NULL,
  `out_movement_id` bigint NOT NULL,
  `layer_id` bigint NOT NULL,
  `consumed_qty` decimal(15,3) NOT NULL,
  `unit_cost` decimal(15,4) NOT NULL,
  `total_cost` decimal(15,4) NOT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_inv_layer_cons_out` (`tenant_id`,`out_movement_id`),
  KEY `idx_inv_layer_cons_layer` (`tenant_id`,`layer_id`),
  KEY `idx_inv_layer_cons_company_out` (`tenant_id`,`company_id`,`out_movement_id`),
  KEY `idx_inv_layer_cons_company_layer` (`tenant_id`,`company_id`,`warehouse_id`,`layer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `inventory_movement`
--

CREATE TABLE `inventory_movement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `item_id` bigint NOT NULL,
  `movement_date` date NOT NULL,
  `movement_type` varchar(30) NOT NULL,
  `causal_code` varchar(50) NOT NULL,
  `quantity` decimal(15,3) NOT NULL,
  `unit_cost` decimal(15,4) DEFAULT NULL,
  `total_cost` decimal(15,4) DEFAULT NULL,
  `reference_type` varchar(50) DEFAULT NULL,
  `reference_id` bigint DEFAULT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `reversed` bit(1) NOT NULL,
  `reversal_of_movement_id` bigint DEFAULT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  `warehouse_id` bigint DEFAULT NULL,
  `location_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_inventory_movement_reversal` (`reversal_of_movement_id`),
  KEY `idx_inv_mov_tenant_item_date` (`tenant_id`,`item_id`,`movement_date`),
  KEY `idx_inv_mov_tenant_ref` (`tenant_id`,`reference_type`,`reference_id`),
  KEY `idx_inv_mov_tenant_causal` (`tenant_id`,`causal_code`),
  KEY `idx_inventory_movement_warehouse` (`tenant_id`,`warehouse_id`,`item_id`),
  KEY `idx_inventory_movement_location` (`tenant_id`,`warehouse_id`,`location_id`),
  KEY `idx_inv_mov_tenant_company_item_date` (`tenant_id`,`company_id`,`item_id`,`movement_date`),
  KEY `idx_inv_mov_tenant_company_ref` (`tenant_id`,`company_id`,`reference_type`,`reference_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `invoice`
--

CREATE TABLE `invoice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `delivery_note_id` bigint DEFAULT NULL,
  `sales_order_id` bigint NOT NULL,
  `customer_id` bigint NOT NULL,
  `invoice_number` varchar(50) NOT NULL,
  `invoice_date` date NOT NULL,
  `status` varchar(30) NOT NULL,
  `currency_code` varchar(10) NOT NULL,
  `subtotal_amount` decimal(15,2) NOT NULL,
  `tax_amount` decimal(15,2) NOT NULL,
  `total_amount` decimal(15,2) NOT NULL,
  `notes` varchar(2000) DEFAULT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invoice_tenant_company_number` (`tenant_id`,`company_id`,`invoice_number`),
  UNIQUE KEY `uk_invoice_tenant_company_sales_order` (`tenant_id`,`company_id`,`sales_order_id`),
  UNIQUE KEY `uk_invoice_tenant_company_delivery_note` (`tenant_id`,`company_id`,`delivery_note_id`),
  KEY `idx_invoice_tenant` (`tenant_id`),
  KEY `idx_invoice_delivery_note` (`tenant_id`,`delivery_note_id`),
  KEY `idx_invoice_sales_order` (`tenant_id`,`sales_order_id`),
  KEY `idx_invoice_customer` (`tenant_id`,`customer_id`),
  KEY `idx_invoice_date` (`tenant_id`,`invoice_date`),
  KEY `idx_invoice_company_date` (`tenant_id`,`company_id`,`invoice_date`),
  KEY `idx_invoice_company_customer` (`tenant_id`,`company_id`,`customer_id`),
  KEY `idx_invoice_company_sales_order` (`tenant_id`,`company_id`,`sales_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `invoice_line`
--

CREATE TABLE `invoice_line` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `invoice_id` bigint NOT NULL,
  `delivery_note_line_id` bigint DEFAULT NULL,
  `sales_order_line_id` bigint DEFAULT NULL,
  `line_no` int NOT NULL,
  `description` varchar(255) NOT NULL,
  `quantity` decimal(15,3) NOT NULL,
  `unit_of_measure` varchar(20) DEFAULT NULL,
  `unit_price` decimal(15,2) NOT NULL,
  `discount_pct` decimal(7,2) NOT NULL,
  `tax_pct` decimal(7,2) NOT NULL,
  `tax_amount` decimal(15,2) NOT NULL,
  `line_total` decimal(15,2) NOT NULL,
  `item_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_invoice_line_invoice` (`tenant_id`,`invoice_id`),
  KEY `idx_invoice_line_item` (`tenant_id`,`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `journal_entry`
--

CREATE TABLE `journal_entry` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `entry_number` varchar(50) NOT NULL,
  `entry_date` date NOT NULL,
  `causal_code` varchar(40) NOT NULL,
  `description` varchar(500) NOT NULL,
  `reference_type` varchar(40) DEFAULT NULL,
  `reference_id` bigint DEFAULT NULL,
  `currency_code` varchar(3) NOT NULL,
  `total_debit` decimal(15,2) NOT NULL,
  `total_credit` decimal(15,2) NOT NULL,
  `posted` bit(1) NOT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  `supplier_invoice_unique_ref` bigint GENERATED ALWAYS AS ((case when (`reference_type` = _utf8mb4'SUPPLIER_INVOICE') then `reference_id` else NULL end)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_journal_entry_tenant_company_number` (`tenant_id`,`company_id`,`entry_number`),
  UNIQUE KEY `uk_journal_entry_supplier_invoice` (`tenant_id`,`company_id`,`supplier_invoice_unique_ref`),
  KEY `idx_journal_entry_tenant_date` (`tenant_id`,`entry_date`),
  KEY `idx_journal_entry_tenant_causal` (`tenant_id`,`causal_code`),
  KEY `idx_journal_entry_tenant_ref` (`tenant_id`,`reference_type`,`reference_id`),
  KEY `idx_journal_entry_company_date` (`tenant_id`,`company_id`,`entry_date`),
  KEY `idx_journal_entry_company_causal` (`tenant_id`,`company_id`,`causal_code`),
  KEY `idx_journal_entry_company_ref` (`tenant_id`,`company_id`,`reference_type`,`reference_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `journal_entry_line`
--

CREATE TABLE `journal_entry_line` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `journal_entry_id` bigint NOT NULL,
  `line_no` int NOT NULL,
  `account_id` bigint NOT NULL,
  `description` varchar(500) NOT NULL,
  `debit_amount` decimal(15,2) NOT NULL,
  `credit_amount` decimal(15,2) NOT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_journal_entry_line_tenant_entry` (`tenant_id`,`journal_entry_id`),
  KEY `idx_journal_entry_line_tenant_account` (`tenant_id`,`account_id`),
  KEY `idx_journal_line_company_entry` (`tenant_id`,`company_id`,`journal_entry_id`),
  KEY `idx_journal_line_company_account` (`tenant_id`,`company_id`,`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `opportunity`
--

CREATE TABLE `opportunity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `amount` decimal(15,2) DEFAULT NULL,
  `customer_id` bigint DEFAULT NULL,
  `expected_close_date` date DEFAULT NULL,
  `lead_id` bigint DEFAULT NULL,
  `probability` int DEFAULT NULL,
  `stage_code` varchar(40) NOT NULL,
  `status` varchar(30) DEFAULT NULL,
  `title` varchar(180) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `payment_due`
--

CREATE TABLE `payment_due` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `direction` varchar(20) NOT NULL,
  `party_type` varchar(20) NOT NULL,
  `party_id` bigint NOT NULL,
  `document_number` varchar(50) NOT NULL,
  `document_date` date NOT NULL,
  `due_date` date NOT NULL,
  `reference_type` varchar(40) NOT NULL,
  `reference_id` bigint NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  `gross_amount` decimal(15,2) NOT NULL,
  `paid_amount` decimal(15,2) NOT NULL,
  `open_amount` decimal(15,2) NOT NULL,
  `status` varchar(30) NOT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_due_company_reference` (`tenant_id`,`company_id`,`reference_type`,`reference_id`),
  KEY `idx_payment_due_tenant_due_date` (`tenant_id`,`due_date`),
  KEY `idx_payment_due_tenant_direction_status` (`tenant_id`,`direction`,`status`),
  KEY `idx_payment_due_tenant_ref` (`tenant_id`,`reference_type`,`reference_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `payment_due_transaction`
--

CREATE TABLE `payment_due_transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `payment_due_id` bigint NOT NULL,
  `transaction_date` date NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `direction` varchar(20) NOT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_due_tx_tenant_due` (`tenant_id`,`payment_due_id`),
  KEY `idx_due_tx_tenant_date` (`tenant_id`,`transaction_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `payment_reminder`
--

CREATE TABLE `payment_reminder` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `channel` varchar(30) NOT NULL,
  `payment_schedule_id` bigint NOT NULL,
  `sent_at` datetime(6) DEFAULT NULL,
  `status` varchar(30) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `payment_schedule`
--

CREATE TABLE `payment_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `customer_id` bigint DEFAULT NULL,
  `direction` varchar(20) NOT NULL,
  `document_id` bigint NOT NULL,
  `document_type` varchar(80) NOT NULL,
  `due_date` date NOT NULL,
  `status` varchar(30) NOT NULL,
  `supplier_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `payment_transaction`
--

CREATE TABLE `payment_transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `direction` varchar(10) NOT NULL,
  `counterparty_type` varchar(20) NOT NULL,
  `counterparty_id` bigint NOT NULL,
  `payment_due_id` bigint NOT NULL,
  `document_type` varchar(50) DEFAULT NULL,
  `document_id` bigint DEFAULT NULL,
  `payment_date` date NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `payment_method` varchar(30) DEFAULT NULL,
  `reference` varchar(100) DEFAULT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  `journal_entry_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_payment_tx_tenant_due` (`tenant_id`,`payment_due_id`),
  KEY `idx_payment_tx_tenant_date` (`tenant_id`,`payment_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `permission`
--

CREATE TABLE `permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(120) NOT NULL,
  `module_code` varchar(120) NOT NULL,
  `description` varchar(200) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  UNIQUE KEY `uc_c533d6f945e3cc91db5f05576` (`code`),
  UNIQUE KEY `UKa7ujv987la0i7a0o91ueevchc` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `persistent_logins`
--

CREATE TABLE `persistent_logins` (
  `username` varchar(64) NOT NULL,
  `series` varchar(64) NOT NULL,
  `token` varchar(64) NOT NULL,
  `last_used` timestamp NOT NULL,
  PRIMARY KEY (`series`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `purchase_order_line`
--

CREATE TABLE `purchase_order_line` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `purchase_order_id` bigint NOT NULL,
  `line_no` int NOT NULL,
  `item_id` bigint DEFAULT NULL,
  `description` varchar(500) NOT NULL,
  `quantity` decimal(15,3) NOT NULL,
  `unit_price` decimal(15,2) NOT NULL,
  `discount_pct` decimal(6,2) NOT NULL,
  `tax_pct` decimal(6,2) NOT NULL,
  `line_subtotal` decimal(15,2) NOT NULL,
  `tax_amount` decimal(15,2) NOT NULL,
  `line_total` decimal(15,2) NOT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_purchase_order_line_order` (`tenant_id`,`purchase_order_id`),
  KEY `idx_purchase_order_line_item` (`tenant_id`,`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `role`
--

CREATE TABLE `role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `code` varchar(80) NOT NULL,
  `name` varchar(120) NOT NULL,
  `system_role` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKovol9h33vyoplw60gaob71gpl` (`tenant_id`,`code`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `role_permission`
--

CREATE TABLE `role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `permission_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKkys381ujcmew5eo3f86ncefps` (`tenant_id`,`role_id`,`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `supplier`
--

CREATE TABLE `supplier` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `code` varchar(60) NOT NULL,
  `name` varchar(180) NOT NULL,
  `vat_number` varchar(40) DEFAULT NULL,
  `tax_code` varchar(40) DEFAULT NULL,
  `email` varchar(180) DEFAULT NULL,
  `phone` varchar(60) DEFAULT NULL,
  `address_line` varchar(255) DEFAULT NULL,
  `city` varchar(120) DEFAULT NULL,
  `postal_code` varchar(20) DEFAULT NULL,
  `province` varchar(20) DEFAULT NULL,
  `country_code` varchar(2) DEFAULT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `active` tinyint(1) NOT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_tenant_code` (`tenant_id`,`code`),
  KEY `idx_supplier_tenant_name` (`tenant_id`,`name`),
  KEY `idx_supplier_tenant_active` (`tenant_id`,`active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `supplier_invoice`
--

CREATE TABLE `supplier_invoice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `invoice_number` varchar(50) NOT NULL,
  `invoice_date` date NOT NULL,
  `supplier_id` bigint NOT NULL,
  `goods_receipt_id` bigint NOT NULL,
  `purchase_order_id` bigint NOT NULL,
  `status` varchar(30) NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `subtotal_amount` decimal(15,2) NOT NULL,
  `tax_amount` decimal(15,2) NOT NULL,
  `total_amount` decimal(15,2) NOT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_invoice_tenant_company_number` (`tenant_id`,`company_id`,`invoice_number`),
  KEY `idx_supplier_invoice_supplier` (`tenant_id`,`supplier_id`),
  KEY `idx_supplier_invoice_receipt` (`tenant_id`,`goods_receipt_id`),
  KEY `idx_supplier_invoice_tenant_company` (`tenant_id`,`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `supplier_invoice_line`
--

CREATE TABLE `supplier_invoice_line` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `supplier_invoice_id` bigint NOT NULL,
  `line_no` int NOT NULL,
  `item_id` bigint DEFAULT NULL,
  `description` varchar(500) NOT NULL,
  `quantity` decimal(15,3) NOT NULL,
  `unit_price` decimal(15,2) NOT NULL,
  `discount_pct` decimal(6,2) NOT NULL,
  `tax_pct` decimal(6,2) NOT NULL,
  `line_subtotal` decimal(15,2) NOT NULL,
  `tax_amount` decimal(15,2) NOT NULL,
  `line_total` decimal(15,2) NOT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_supplier_invoice_line_invoice` (`tenant_id`,`supplier_invoice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `tenant`
--

CREATE TABLE `tenant` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(120) NOT NULL,
  `slug` varchar(120) NOT NULL,
  `email` varchar(180) NOT NULL,
  `status` varchar(30) NOT NULL,
  `default_locale` varchar(10) NOT NULL DEFAULT 'it',
  `default_currency` varchar(3) NOT NULL DEFAULT 'EUR',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `inventory_valuation_method` varchar(20) NOT NULL DEFAULT 'FIFO',
  `subscription_active` tinyint(1) NOT NULL DEFAULT '0',
  `subscription_status` varchar(30) DEFAULT 'PENDING',
  `subscription_plan` varchar(50) DEFAULT 'PROFESSIONAL',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `uc_tenant_email` (`email`),
  UNIQUE KEY `uc_tenant_slug` (`slug`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `tenant_module`
--

CREATE TABLE `tenant_module` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `module_code` varchar(50) NOT NULL,
  `plan_code` varchar(50) DEFAULT NULL,
  `tenant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKal5y9mntq5g5ntoivg1gx4xe1` (`tenant_id`,`module_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `user_role`
--

CREATE TABLE `user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqrde1ctbh1u3xtshpr1sxouuq` (`tenant_id`,`user_id`,`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `app_user`
--

CREATE TABLE `app_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `first_name` varchar(120) NOT NULL,
  `last_name` varchar(120) NOT NULL,
  `email` varchar(180) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `status` varchar(30) NOT NULL,
  `admin` bit(1) NOT NULL,
  `locale_code` varchar(10) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_user_tenant_email` (`tenant_id`,`email`),
  UNIQUE KEY `uc_5bc5805bdb9f459160ff9129f` (`tenant_id`,`email`),
  UNIQUE KEY `UKr06tbvime3j5yvmuu9b8djaaw` (`tenant_id`,`email`),
  CONSTRAINT `fk_app_user_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `company`
--

CREATE TABLE `company` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `code` varchar(30) NOT NULL,
  `legal_name` varchar(180) NOT NULL,
  `trade_name` varchar(180) DEFAULT NULL,
  `vat_number` varchar(30) DEFAULT NULL,
  `tax_code` varchar(30) DEFAULT NULL,
  `email` varchar(180) DEFAULT NULL,
  `phone` varchar(50) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  `address_line_1` varchar(255) DEFAULT NULL,
  `postal_code` varchar(30) DEFAULT NULL,
  `city` varchar(120) DEFAULT NULL,
  `province` varchar(10) DEFAULT NULL,
  `country_code` varchar(2) NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  `locale_code` varchar(10) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_tenant_code` (`tenant_id`,`code`),
  UNIQUE KEY `uk_company_tenant_id` (`tenant_id`,`id`),
  KEY `idx_company_tenant_active` (`tenant_id`,`active`),
  CONSTRAINT `fk_company_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `customer`
--

CREATE TABLE `customer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `name` varchar(180) NOT NULL,
  `vat_number` varchar(30) DEFAULT NULL,
  `tax_code` varchar(30) DEFAULT NULL,
  `email` varchar(180) DEFAULT NULL,
  `phone` varchar(50) DEFAULT NULL,
  `type` varchar(30) DEFAULT NULL,
  `status` varchar(30) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `address_line_1` varchar(2000) DEFAULT NULL,
  `city` varchar(300) DEFAULT NULL,
  `postal_code` varchar(100) DEFAULT NULL,
  `country_code` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_customer_tenant_name` (`tenant_id`,`name`),
  CONSTRAINT `fk_customer_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `delivery_note_line`
--

CREATE TABLE `delivery_note_line` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `delivery_note_id` bigint NOT NULL,
  `sales_order_line_id` bigint DEFAULT NULL,
  `line_no` int NOT NULL,
  `description` varchar(255) NOT NULL,
  `quantity` decimal(15,3) NOT NULL,
  `unit_of_measure` varchar(20) DEFAULT NULL,
  `unit_price` decimal(15,2) NOT NULL,
  `discount_pct` decimal(7,2) NOT NULL,
  `tax_pct` decimal(7,2) NOT NULL,
  `tax_amount` decimal(15,2) NOT NULL,
  `line_total` decimal(15,2) NOT NULL,
  `item_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_delivery_note_line_note` (`tenant_id`,`delivery_note_id`),
  KEY `idx_delivery_note_line_item` (`tenant_id`,`item_id`),
  KEY `FK4fjlrvqba0f3g072eubjtba8j` (`delivery_note_id`),
  KEY `idx_delivery_note_line_company_note` (`tenant_id`,`company_id`,`delivery_note_id`),
  KEY `idx_delivery_note_line_company_item` (`tenant_id`,`company_id`,`item_id`),
  CONSTRAINT `FK4fjlrvqba0f3g072eubjtba8j` FOREIGN KEY (`delivery_note_id`) REFERENCES `delivery_note` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `document_template`
--

CREATE TABLE `document_template` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `code` varchar(80) NOT NULL,
  `name` varchar(180) NOT NULL,
  `document_type` varchar(30) NOT NULL,
  `template_html` tinytext NOT NULL,
  `active` bit(1) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_doc_template_tenant` (`tenant_id`),
  CONSTRAINT `fk_doc_template_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `goods_receipt_line`
--

CREATE TABLE `goods_receipt_line` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `goods_receipt_id` bigint NOT NULL,
  `line_no` int NOT NULL,
  `purchase_order_line_id` bigint NOT NULL,
  `item_id` bigint DEFAULT NULL,
  `description` varchar(500) NOT NULL,
  `quantity_received` decimal(15,3) NOT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  `unit_cost` decimal(15,4) DEFAULT NULL,
  `total_cost` decimal(15,4) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_goods_receipt_line_receipt` (`tenant_id`,`goods_receipt_id`),
  KEY `idx_goods_receipt_line_item` (`tenant_id`,`item_id`),
  KEY `FKff5scxf5vgristhdleqkhh0p` (`goods_receipt_id`),
  CONSTRAINT `FKff5scxf5vgristhdleqkhh0p` FOREIGN KEY (`goods_receipt_id`) REFERENCES `goods_receipt` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `purchase_order`
--

CREATE TABLE `purchase_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `order_number` varchar(50) NOT NULL,
  `order_date` date NOT NULL,
  `expected_delivery_date` date DEFAULT NULL,
  `supplier_id` bigint NOT NULL,
  `status` varchar(30) NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `subtotal_amount` decimal(15,2) NOT NULL,
  `tax_amount` decimal(15,2) NOT NULL,
  `total_amount` decimal(15,2) NOT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_purchase_order_tenant_company_number` (`tenant_id`,`company_id`,`order_number`),
  KEY `idx_purchase_order_tenant_supplier` (`tenant_id`,`supplier_id`),
  KEY `idx_purchase_order_tenant_status` (`tenant_id`,`status`),
  KEY `FK4traogu3jriq9u7e8rvm86k7i` (`supplier_id`),
  CONSTRAINT `FK4traogu3jriq9u7e8rvm86k7i` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `unit_of_measure`
--

CREATE TABLE `unit_of_measure` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `code` varchar(20) NOT NULL,
  `name` varchar(100) NOT NULL,
  `symbol` varchar(20) NOT NULL,
  `dimension` varchar(30) NOT NULL,
  `decimal_places` int NOT NULL DEFAULT '0',
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_uom_tenant_code` (`tenant_id`,`code`),
  KEY `idx_uom_tenant_active` (`tenant_id`,`active`),
  KEY `idx_uom_tenant_dimension` (`tenant_id`,`dimension`),
  CONSTRAINT `fk_uom_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `ai_conversation_log`
--

CREATE TABLE `ai_conversation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `prompt_text` tinytext NOT NULL,
  `response_text` longtext,
  `scope_type` varchar(30) NOT NULL,
  `action_type` varchar(30) DEFAULT NULL,
  `confirmed_by_user` bit(1) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_ai_log_tenant` (`tenant_id`),
  KEY `fk_ai_log_user` (`user_id`),
  CONSTRAINT `fk_ai_log_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`),
  CONSTRAINT `fk_ai_log_user` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `audit_log`
--

CREATE TABLE `audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `entity_name` varchar(100) NOT NULL,
  `entity_id` bigint NOT NULL,
  `action_code` varchar(30) NOT NULL,
  `old_value_json` tinytext,
  `new_value_json` tinytext,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_audit_user` (`user_id`),
  KEY `idx_audit_tenant_entity` (`tenant_id`,`entity_name`,`entity_id`),
  KEY `idx_audit_tenant_created` (`tenant_id`,`created_at`),
  CONSTRAINT `fk_audit_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`),
  CONSTRAINT `fk_audit_user` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `company_profile`
--

CREATE TABLE `company_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `currency_code` varchar(3) DEFAULT NULL,
  `email` varchar(180) DEFAULT NULL,
  `legal_name` varchar(180) NOT NULL,
  `locale_code` varchar(10) DEFAULT NULL,
  `phone` varchar(50) DEFAULT NULL,
  `tax_code` varchar(30) DEFAULT NULL,
  `trade_name` varchar(180) DEFAULT NULL,
  `vat_number` varchar(30) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_profile_tenant_company` (`tenant_id`,`company_id`),
  KEY `fk_company_profile_company` (`company_id`),
  CONSTRAINT `fk_company_profile_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `company_settings`
--

CREATE TABLE `company_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `trade_name` varchar(180) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `vat_number` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tax_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(180) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `website` varchar(180) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address_line_1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `postal_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `province` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `country_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `default_vat_pct` decimal(5,2) DEFAULT NULL,
  `default_customer_due_days` int DEFAULT NULL,
  `default_supplier_due_days` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(6) NOT NULL,
  `company_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_settings_company` (`company_id`),
  CONSTRAINT `fk_company_settings_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--

-- Table structure for table `item`
--

CREATE TABLE `item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `code` varchar(60) NOT NULL,
  `barcode` varchar(100) DEFAULT NULL,
  `name` varchar(180) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `item_type` varchar(20) NOT NULL,
  `base_uom_id` bigint NOT NULL,
  `tracking_type` varchar(20) NOT NULL DEFAULT 'NONE',
  `track_expiration` tinyint(1) NOT NULL DEFAULT '0',
  `active` tinyint(1) NOT NULL,
  `base_price` decimal(15,2) DEFAULT NULL,
  `default_tax_pct` decimal(6,2) DEFAULT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_tenant_code` (`tenant_id`,`code`),
  KEY `idx_item_tenant_active` (`tenant_id`,`active`),
  KEY `fk_item_base_uom` (`base_uom_id`),
  CONSTRAINT `fk_item_base_uom` FOREIGN KEY (`base_uom_id`) REFERENCES `unit_of_measure` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `quote`
--

CREATE TABLE `quote` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `customer_id` bigint NOT NULL,
  `quote_number` varchar(50) NOT NULL,
  `quote_date` date NOT NULL,
  `valid_until` date NOT NULL,
  `status` varchar(30) NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  `subtotal_amount` decimal(15,2) NOT NULL,
  `tax_amount` decimal(15,2) NOT NULL,
  `total_amount` decimal(15,2) NOT NULL,
  `notes` longtext,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `company_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_quote_tenant_company_number` (`tenant_id`,`company_id`,`quote_number`),
  KEY `fk_quote_customer` (`customer_id`),
  KEY `idx_quote_company_date` (`tenant_id`,`company_id`,`quote_date`),
  KEY `idx_quote_company_customer` (`tenant_id`,`company_id`,`customer_id`),
  KEY `idx_quote_company_status` (`tenant_id`,`company_id`,`status`),
  CONSTRAINT `fk_quote_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`),
  CONSTRAINT `fk_quote_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `user_company_access`
--

CREATE TABLE `user_company_access` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_company_access` (`tenant_id`,`user_id`,`company_id`),
  KEY `fk_uca_user` (`user_id`),
  KEY `fk_uca_company` (`company_id`),
  KEY `idx_uca_user` (`tenant_id`,`user_id`),
  KEY `idx_uca_company` (`tenant_id`,`company_id`),
  CONSTRAINT `fk_uca_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`),
  CONSTRAINT `fk_uca_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`),
  CONSTRAINT `fk_uca_user` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `warehouse`
--

CREATE TABLE `warehouse` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `code` varchar(30) NOT NULL,
  `name` varchar(180) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `warehouse_type` varchar(30) NOT NULL DEFAULT 'PHYSICAL',
  `allow_negative_stock` tinyint(1) NOT NULL DEFAULT '0',
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_company_code` (`tenant_id`,`company_id`,`code`),
  KEY `fk_warehouse_company` (`company_id`),
  KEY `idx_warehouse_company_active` (`tenant_id`,`company_id`,`active`),
  CONSTRAINT `fk_warehouse_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`),
  CONSTRAINT `fk_warehouse_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `inventory_lot`
--

CREATE TABLE `inventory_lot` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `item_id` bigint NOT NULL,
  `lot_code` varchar(100) NOT NULL,
  `production_date` date DEFAULT NULL,
  `expiration_date` date DEFAULT NULL,
  `notes` varchar(500) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inventory_lot_item_code` (`tenant_id`,`item_id`,`lot_code`),
  KEY `fk_inventory_lot_item` (`item_id`),
  KEY `idx_inventory_lot_item` (`tenant_id`,`item_id`),
  KEY `idx_inventory_lot_expiration` (`tenant_id`,`expiration_date`),
  CONSTRAINT `fk_inventory_lot_item` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`),
  CONSTRAINT `fk_inventory_lot_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `inventory_serial`
--

CREATE TABLE `inventory_serial` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `item_id` bigint NOT NULL,
  `serial_number` varchar(150) NOT NULL,
  `manufacturing_date` date DEFAULT NULL,
  `expiration_date` date DEFAULT NULL,
  `notes` varchar(500) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inventory_serial_item_number` (`tenant_id`,`item_id`,`serial_number`),
  KEY `fk_inventory_serial_item` (`item_id`),
  KEY `idx_inventory_serial_item` (`tenant_id`,`item_id`),
  KEY `idx_inventory_serial_expiration` (`tenant_id`,`expiration_date`),
  CONSTRAINT `fk_inventory_serial_item` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`),
  CONSTRAINT `fk_inventory_serial_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `item_uom`
--

CREATE TABLE `item_uom` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `item_id` bigint NOT NULL,
  `uom_id` bigint NOT NULL,
  `conversion_factor` decimal(19,6) NOT NULL,
  `purchase_uom` tinyint(1) NOT NULL DEFAULT '0',
  `sales_uom` tinyint(1) NOT NULL DEFAULT '0',
  `barcode` varchar(100) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_uom` (`tenant_id`,`item_id`,`uom_id`),
  KEY `fk_item_uom_item` (`item_id`),
  KEY `fk_item_uom_uom` (`uom_id`),
  KEY `idx_item_uom_item` (`tenant_id`,`item_id`),
  KEY `idx_item_uom_uom` (`tenant_id`,`uom_id`),
  CONSTRAINT `fk_item_uom_item` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`),
  CONSTRAINT `fk_item_uom_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`),
  CONSTRAINT `fk_item_uom_uom` FOREIGN KEY (`uom_id`) REFERENCES `unit_of_measure` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `quote_line`
--

CREATE TABLE `quote_line` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `quote_id` bigint NOT NULL,
  `line_no` int NOT NULL,
  `description` varchar(255) NOT NULL,
  `quantity` decimal(15,3) NOT NULL,
  `unit_of_measure` varchar(20) DEFAULT NULL,
  `unit_price` decimal(15,2) NOT NULL,
  `discount_pct` decimal(5,2) DEFAULT NULL,
  `tax_pct` decimal(5,2) DEFAULT NULL,
  `line_total` decimal(15,2) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `gross_amount` decimal(15,2) DEFAULT NULL,
  `discount_amount` decimal(15,2) DEFAULT NULL,
  `tax_amount` decimal(15,2) DEFAULT NULL,
  `item_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_quote_line_quote` (`quote_id`),
  KEY `idx_quote_line_item` (`tenant_id`,`item_id`),
  KEY `idx_quote_line_company_quote` (`tenant_id`,`company_id`,`quote_id`),
  KEY `idx_quote_line_company_item` (`tenant_id`,`company_id`,`item_id`),
  CONSTRAINT `fk_quote_line_quote` FOREIGN KEY (`quote_id`) REFERENCES `quote` (`id`),
  CONSTRAINT `fk_quote_line_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `sales_order`
--

CREATE TABLE `sales_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `customer_id` bigint NOT NULL,
  `quote_id` bigint DEFAULT NULL,
  `order_number` varchar(50) NOT NULL,
  `order_date` date NOT NULL,
  `status` varchar(30) NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  `total_amount` decimal(15,2) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `subtotal_amount` decimal(15,2) DEFAULT NULL,
  `tax_amount` decimal(15,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sales_order_tenant_company_number` (`tenant_id`,`company_id`,`order_number`),
  KEY `fk_order_customer` (`customer_id`),
  KEY `fk_order_quote` (`quote_id`),
  KEY `idx_sales_order_company_date` (`tenant_id`,`company_id`,`order_date`),
  KEY `idx_sales_order_company_customer` (`tenant_id`,`company_id`,`customer_id`),
  KEY `idx_sales_order_company_quote` (`tenant_id`,`company_id`,`quote_id`),
  CONSTRAINT `fk_order_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`),
  CONSTRAINT `fk_order_quote` FOREIGN KEY (`quote_id`) REFERENCES `quote` (`id`),
  CONSTRAINT `fk_order_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--

-- Table structure for table `warehouse_location`
--

CREATE TABLE `warehouse_location` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `warehouse_id` bigint NOT NULL,
  `parent_location_id` bigint DEFAULT NULL,
  `code` varchar(50) NOT NULL,
  `name` varchar(180) NOT NULL,
  `location_type` varchar(30) NOT NULL DEFAULT 'STORAGE',
  `barcode` varchar(100) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_location_code` (`tenant_id`,`warehouse_id`,`code`),
  KEY `fk_location_warehouse` (`warehouse_id`),
  KEY `fk_location_parent` (`parent_location_id`),
  KEY `idx_warehouse_location_parent` (`tenant_id`,`warehouse_id`,`parent_location_id`),
  KEY `idx_warehouse_location_active` (`tenant_id`,`warehouse_id`,`active`),
  CONSTRAINT `fk_location_parent` FOREIGN KEY (`parent_location_id`) REFERENCES `warehouse_location` (`id`),
  CONSTRAINT `fk_location_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`),
  CONSTRAINT `fk_location_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping events for database 'gestiva_flyway_test'
--

--
-- Dumping routines for database 'gestiva_flyway_test'
--


-- Dump completed on 2026-10-23  2:18:59

-- Table structure for table `sales_order_line`
--

CREATE TABLE `sales_order_line` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `sales_order_id` bigint NOT NULL,
  `line_no` int NOT NULL,
  `description` varchar(255) NOT NULL,
  `quantity` decimal(15,3) NOT NULL,
  `unit_of_measure` varchar(20) DEFAULT NULL,
  `unit_price` decimal(15,2) NOT NULL,
  `line_total` decimal(15,2) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `discount_pct` decimal(15,2) DEFAULT NULL,
  `tax_pct` decimal(15,2) DEFAULT NULL,
  `tax_amount` decimal(15,2) DEFAULT NULL,
  `item_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_order_line_order` (`sales_order_id`),
  KEY `idx_sales_order_line_item` (`tenant_id`,`item_id`),
  KEY `idx_sales_order_line_company_order` (`tenant_id`,`company_id`,`sales_order_id`),
  KEY `idx_sales_order_line_company_item` (`tenant_id`,`company_id`,`item_id`),
  CONSTRAINT `fk_order_line_order` FOREIGN KEY (`sales_order_id`) REFERENCES `sales_order` (`id`),
  CONSTRAINT `fk_order_line_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
