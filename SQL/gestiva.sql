-- MySQL dump 10.13  Distrib 8.0.45, for macos15 (arm64)
--
-- Host: localhost    Database: gestiva
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
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
  UNIQUE KEY `uk_account_tenant_code` (`tenant_id`,`code`),
  KEY `idx_account_tenant_type` (`tenant_id`,`account_type`),
  KEY `idx_account_tenant_active` (`tenant_id`,`active`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,88,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(2,88,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(3,88,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(4,88,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(5,88,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(6,88,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(7,88,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(8,88,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(9,88,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(10,88,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(11,88,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(12,88,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(13,88,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(14,88,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(15,88,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(16,88,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(17,88,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(18,88,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(19,88,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20');
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `accounting_entry`
--

DROP TABLE IF EXISTS `accounting_entry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `accounting_entry`
--

LOCK TABLES `accounting_entry` WRITE;
/*!40000 ALTER TABLE `accounting_entry` DISABLE KEYS */;
INSERT INTO `accounting_entry` VALUES (1,87,'PN-00001','2026-05-09','CUSTOMER_RECEIPT','Incasso su scadenza INV-2026-00001','PAYMENT_DUE',1,'EUR',3478.00,'prova prima nota incasso','2026-05-09 05:45:45','2026-05-09 05:45:45'),(2,87,'PN-00002','2026-05-23','CUSTOMER_RECEIPT','Incasso su scadenza INV-2026-00001','PAYMENT_DUE',1,'EUR',60.00,'PROVA PRIMA NOTA INCASSO','2026-05-09 05:46:21','2026-05-09 05:46:21'),(3,87,'PN-00003','2026-05-09','SUPPLIER_PAYMENT','Pagamento su scadenza SI-00004','PAYMENT_DUE',5,'EUR',707.56,'PROVA PRIMA NOTA PAGAMENTO A FORNITORE','2026-05-09 05:47:42','2026-05-09 05:47:42'),(4,87,'PN-00004','2026-05-09','SUPPLIER_PAYMENT','Pagamento su scadenza SI-00004','PAYMENT_DUE',5,'EUR',0.04,'','2026-05-09 05:48:06','2026-05-09 05:48:06'),(5,88,'PN-00005','2026-05-09','MANUAL_INCOME','Rimborso assicurazione',NULL,NULL,'EUR',149.96,'PROVA ENTRATA MANUALE','2026-05-09 19:07:05','2026-05-09 19:07:05'),(6,88,'PN-00006','2026-05-09','MANUAL_EXPENSE','Spese bancarie',NULL,NULL,'EUR',12.50,'USCITA MANUALE','2026-05-09 19:10:06','2026-05-09 19:10:06'),(7,88,'PN-00007','2026-05-09','CUSTOMER_RECEIPT','Incasso su scadenza INV-2026-00001','PAYMENT_DUE',6,'EUR',700.00,'PROVA DASHBOARD','2026-05-09 19:38:13','2026-05-09 19:38:13'),(8,88,'PN-00008','2026-05-09','CUSTOMER_RECEIPT','Incasso su scadenza INV-2026-00001','PAYMENT_DUE',6,'EUR',7.60,'PROVA DASHBOARD','2026-05-09 19:39:22','2026-05-09 19:39:22'),(9,88,'PN-00009','2026-05-09','SUPPLIER_PAYMENT','Pagamento su scadenza SI-00005','PAYMENT_DUE',8,'EUR',1400.00,'PROVA DASHBOARD','2026-05-09 20:16:15','2026-05-09 20:16:15'),(10,88,'PN-00010','2026-05-09','CUSTOMER_RECEIPT','Incasso su scadenza INV-2026-00002','PAYMENT_DUE',7,'EUR',707.60,'PROVA DASHBOARD','2026-05-09 20:17:16','2026-05-09 20:17:16');
/*!40000 ALTER TABLE `accounting_entry` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `accounting_entry_line`
--

DROP TABLE IF EXISTS `accounting_entry_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `accounting_entry_line`
--

LOCK TABLES `accounting_entry_line` WRITE;
/*!40000 ALTER TABLE `accounting_entry_line` DISABLE KEYS */;
INSERT INTO `accounting_entry_line` VALUES (1,87,1,1,'INCOME','Incasso su scadenza INV-2026-00001',3478.00,'2026-05-09 05:45:45','2026-05-09 05:45:45'),(2,87,2,1,'INCOME','Incasso su scadenza INV-2026-00001',60.00,'2026-05-09 05:46:21','2026-05-09 05:46:21'),(3,87,3,1,'EXPENSE','Pagamento su scadenza SI-00004',707.56,'2026-05-09 05:47:42','2026-05-09 05:47:42'),(4,87,4,1,'EXPENSE','Pagamento su scadenza SI-00004',0.04,'2026-05-09 05:48:06','2026-05-09 05:48:06'),(5,88,5,1,'INCOME','Rimborso assicurazione',149.96,'2026-05-09 19:07:05','2026-05-09 19:07:05'),(6,88,6,1,'EXPENSE','Spese bancarie',12.50,'2026-05-09 19:10:06','2026-05-09 19:10:06'),(7,88,7,1,'INCOME','Incasso su scadenza INV-2026-00001',700.00,'2026-05-09 19:38:13','2026-05-09 19:38:13'),(8,88,8,1,'INCOME','Incasso su scadenza INV-2026-00001',7.60,'2026-05-09 19:39:22','2026-05-09 19:39:22'),(9,88,9,1,'EXPENSE','Pagamento su scadenza SI-00005',1400.00,'2026-05-09 20:16:15','2026-05-09 20:16:15'),(10,88,10,1,'INCOME','Incasso su scadenza INV-2026-00002',707.60,'2026-05-09 20:17:16','2026-05-09 20:17:16');
/*!40000 ALTER TABLE `accounting_entry_line` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_conversation_log`
--

DROP TABLE IF EXISTS `ai_conversation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_conversation_log`
--

LOCK TABLES `ai_conversation_log` WRITE;
/*!40000 ALTER TABLE `ai_conversation_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_conversation_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_user`
--

DROP TABLE IF EXISTS `app_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_user`
--

LOCK TABLES `app_user` WRITE;
/*!40000 ALTER TABLE `app_user` DISABLE KEYS */;
INSERT INTO `app_user` VALUES (1,85,'Marco','Albasini','admin@example.com','$2a$10$oM12UOzwk.bsSbbmqEPNt.E0ES1NiKll1Iy4UCcytXE.I2CZyueAK','ACTIVE',_binary '','it','2026-05-05 05:42:49','2026-05-05 05:42:49'),(2,86,'Marco','Albasini','admin@example.com','$2a$10$8jjasueMe.Tz.3v.k9gajOSJRg1NYyyBkBTEldsvaxeDVwvY17frK','ACTIVE',_binary '','it','2026-05-07 01:20:11','2026-05-07 01:20:11'),(3,87,'Alessandra','Albasini','malbasini@outlook.it','$2a$10$ex96en3m4jmBXbyI0ZZcEO0zEJdHZIyLKhYrJHi72Af52Viur3wBS','ACTIVE',_binary '','it','2026-05-08 02:46:05','2026-05-08 02:46:05'),(4,88,'Mario','Rossi','admin@rossi.it','$2a$10$EBeSnF3NL9JnaTpwtP8K1ex6EbWNM3fRCkkdhTkkzD7E.cd4IdB1a','ACTIVE',_binary '','it','2026-05-09 13:26:17','2026-05-09 13:26:17');
/*!40000 ALTER TABLE `app_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attachment`
--

DROP TABLE IF EXISTS `attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attachment`
--

LOCK TABLES `attachment` WRITE;
/*!40000 ALTER TABLE `attachment` DISABLE KEYS */;
/*!40000 ALTER TABLE `attachment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_log`
--

DROP TABLE IF EXISTS `audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_log`
--

LOCK TABLES `audit_log` WRITE;
/*!40000 ALTER TABLE `audit_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `audit_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `company_profile`
--

DROP TABLE IF EXISTS `company_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `company_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `currency_code` varchar(3) DEFAULT NULL,
  `email` varchar(180) DEFAULT NULL,
  `legal_name` varchar(180) NOT NULL,
  `locale_code` varchar(10) DEFAULT NULL,
  `phone` varchar(50) DEFAULT NULL,
  `tax_code` varchar(30) DEFAULT NULL,
  `trade_name` varchar(180) DEFAULT NULL,
  `vat_number` varchar(30) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `company_profile`
--

LOCK TABLES `company_profile` WRITE;
/*!40000 ALTER TABLE `company_profile` DISABLE KEYS */;
/*!40000 ALTER TABLE `company_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contact_person`
--

DROP TABLE IF EXISTS `contact_person`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contact_person`
--

LOCK TABLES `contact_person` WRITE;
/*!40000 ALTER TABLE `contact_person` DISABLE KEYS */;
/*!40000 ALTER TABLE `contact_person` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `crm_activity`
--

DROP TABLE IF EXISTS `crm_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `crm_activity`
--

LOCK TABLES `crm_activity` WRITE;
/*!40000 ALTER TABLE `crm_activity` DISABLE KEYS */;
/*!40000 ALTER TABLE `crm_activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `crm_lead`
--

DROP TABLE IF EXISTS `crm_lead`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `crm_lead`
--

LOCK TABLES `crm_lead` WRITE;
/*!40000 ALTER TABLE `crm_lead` DISABLE KEYS */;
/*!40000 ALTER TABLE `crm_lead` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `crm_note`
--

DROP TABLE IF EXISTS `crm_note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `crm_note`
--

LOCK TABLES `crm_note` WRITE;
/*!40000 ALTER TABLE `crm_note` DISABLE KEYS */;
/*!40000 ALTER TABLE `crm_note` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=90 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
INSERT INTO `customer` VALUES (70,70,'Rossi Srl',NULL,NULL,'customer-57014888715666@example.com',NULL,'COMPANY','ACTIVE','2026-04-27 01:29:26','2026-04-27 01:29:26',NULL,NULL,NULL,NULL),(71,71,'Bianchi Srl',NULL,NULL,'customer-57014890156416@example.com',NULL,'COMPANY','ACTIVE','2026-04-27 01:29:26','2026-04-27 01:29:26',NULL,NULL,NULL,NULL),(72,72,'Rossi Srl',NULL,NULL,'customer-6346986438791@example.com',NULL,'COMPANY','ACTIVE','2026-04-27 19:58:16','2026-04-27 19:58:16',NULL,NULL,NULL,NULL),(73,73,'Rossi Srl',NULL,NULL,'customer-23734473434583@example.com',NULL,'COMPANY','ACTIVE','2026-04-28 18:46:25','2026-04-28 18:46:25',NULL,NULL,NULL,NULL),(74,74,'Rossi Srl',NULL,NULL,'customer-28677535989791@example.com',NULL,'COMPANY','ACTIVE','2026-04-28 20:08:48','2026-04-28 20:08:48',NULL,NULL,NULL,NULL),(75,88,'Rossi Srl',NULL,NULL,'customer-4009228697458@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(76,88,'Bianchi Srl',NULL,NULL,'customer-4009231593291@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(77,76,'Alfa Srl',NULL,NULL,'customer-4009233142791@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(78,77,'Rossi Srl',NULL,NULL,'customer-4009399204375@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(79,77,'Bianchi Srl',NULL,NULL,'customer-4009400848541@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(80,78,'Alfa Srl',NULL,NULL,'customer-4009402706125@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(81,79,'Rossi Srl',NULL,NULL,'customer-4009474103125@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(82,79,'Bianchi Srl',NULL,NULL,'customer-4009475728583@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(83,80,'Alfa Srl',NULL,NULL,'customer-4009477193208@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(84,81,'Rossi Srl','125TYHBN','YT67UIKM','customer-4009542552500@example.com','3311675741','COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04','VIA DELLA FONTANA 24','MONTECASTRILLI','05026','IT'),(85,81,'Bianchi Srl','1POLK98UI',NULL,'customer-4009544162458@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(86,82,'Alfa Srl',NULL,NULL,'customer-4009545695791@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(87,83,'Rossi Srl',NULL,NULL,'customer-4009609751375@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(88,83,'Bianchi Srl',NULL,NULL,'customer-4009611152708@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(89,84,'Alfa Srl',NULL,NULL,'customer-4009612564916@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `delivery_note`
--

DROP TABLE IF EXISTS `delivery_note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `delivery_note` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_delivery_note_tenant_number` (`tenant_id`,`ddt_number`),
  KEY `idx_delivery_note_tenant` (`tenant_id`),
  KEY `idx_delivery_note_sales_order` (`tenant_id`,`sales_order_id`),
  KEY `idx_delivery_note_customer` (`tenant_id`,`customer_id`),
  KEY `idx_delivery_note_date` (`tenant_id`,`ddt_date`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `delivery_note`
--

LOCK TABLES `delivery_note` WRITE;
/*!40000 ALTER TABLE `delivery_note` DISABLE KEYS */;
INSERT INTO `delivery_note` VALUES (1,75,29,76,'DDT-2026-00001','2026-05-02','ISSUED','Vendita','Franco',NULL,'EUR',140.00,30.80,170.80,NULL,'2026-05-02 19:26:05','2026-05-02 19:26:05'),(2,75,10,76,'DDT-2026-00002','2026-05-02','CANCELLED','Vendita','Franco',NULL,'EUR',200.00,44.00,244.00,NULL,'2026-05-02 19:38:59','2026-05-02 19:38:59'),(3,75,9,75,'DDT-2026-00003','2026-05-03','ISSUED','Vendita','Franco',NULL,'EUR',1000.00,220.00,1220.00,NULL,'2026-05-02 20:24:16','2026-05-02 20:24:16'),(4,85,30,75,'DDT-2026-00001','2026-05-06','ISSUED','Vendita','Franco',NULL,'EUR',580.00,127.60,707.60,NULL,'2026-05-06 17:32:42','2026-05-06 17:32:42'),(5,85,31,75,'DDT-2026-00002','2026-05-07','CANCELLED','Vendita','Franco',NULL,'EUR',464.00,102.08,566.08,NULL,'2026-05-06 20:24:49','2026-05-06 20:24:49'),(6,86,32,76,'DDT-2026-00001','2026-05-07','ISSUED','Vendita','Franco',NULL,'EUR',5220.00,1148.40,6368.40,NULL,'2026-05-06 21:32:42','2026-05-06 21:32:42'),(7,86,33,75,'DDT-2026-00002','2026-05-07','ISSUED','Vendita','Franco',NULL,'EUR',1080.00,237.60,1317.60,NULL,'2026-05-06 21:54:30','2026-05-06 21:54:30'),(8,87,34,76,'DDT-2026-00001','2026-05-08','CANCELLED','Vendita','Franco',NULL,'EUR',11600.00,2552.00,14152.00,NULL,'2026-05-07 22:55:41','2026-05-07 22:55:41'),(9,87,35,75,'DDT-2026-00002','2026-05-08','ISSUED','Vendita','Franco',NULL,'EUR',2900.00,638.00,3538.00,NULL,'2026-05-08 12:18:24','2026-05-08 12:18:24'),(10,87,36,76,'DDT-2026-00003','2026-05-08','ISSUED','Vendita','Franco',NULL,'EUR',100.00,22.00,122.00,NULL,'2026-05-08 15:02:12','2026-05-08 15:02:12'),(11,87,37,76,'DDT-2026-00004','2026-05-09','ISSUED','Vendita','Franco',NULL,'EUR',580.00,127.60,707.60,NULL,'2026-05-09 03:34:14','2026-05-09 03:34:14'),(12,87,38,76,'DDT-2026-00005','2026-05-09','CANCELLED','Vendita','Franco',NULL,'EUR',580.00,127.60,707.60,NULL,'2026-05-09 08:22:53','2026-05-09 08:22:53'),(15,88,39,75,'DDT-2026-00001','2026-05-09','ISSUED','Vendita','Franco',NULL,'EUR',580.00,127.60,707.60,NULL,'2026-05-09 09:34:00','2026-05-09 09:34:00'),(16,88,40,75,'DDT-2026-00002','2026-05-09','ISSUED','Vendita','Franco',NULL,'EUR',580.00,127.60,707.60,NULL,'2026-05-09 12:01:05','2026-05-09 12:01:05');
/*!40000 ALTER TABLE `delivery_note` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `delivery_note_line`
--

DROP TABLE IF EXISTS `delivery_note_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `delivery_note_line` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
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
  KEY `idx_delivery_note_line_item` (`tenant_id`,`item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `delivery_note_line`
--

LOCK TABLES `delivery_note_line` WRITE;
/*!40000 ALTER TABLE `delivery_note_line` DISABLE KEYS */;
INSERT INTO `delivery_note_line` VALUES (1,75,1,44,1,'RIGA 1',1.000,'pz',100.00,10.00,22.00,19.80,109.80,NULL),(2,75,1,45,2,'RIGA 2',1.000,'pz',50.00,0.00,22.00,11.00,61.00,NULL),(3,75,2,18,1,'Setup',1.000,'pz',200.00,0.00,22.00,44.00,200.00,NULL),(4,75,3,17,1,'Modulo CRM',1.000,'pz',1000.00,0.00,22.00,220.00,1000.00,NULL),(5,85,4,46,1,'RIGA 1',1.000,'pz',580.00,0.00,22.00,127.60,580.00,NULL),(6,85,5,47,1,'RIGA 1',1.000,'pz',580.00,20.00,22.00,102.08,464.00,2),(7,86,6,48,1,'RIGA 1',10.000,'pz',580.00,10.00,22.00,1148.40,5220.00,2),(8,86,7,49,1,'RIGA 1',1.000,'pz',1200.00,10.00,22.00,237.60,1080.00,1),(9,87,8,50,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',20.000,'pz',580.00,0.00,22.00,2552.00,11600.00,2),(10,87,9,51,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',5.000,'pz',580.00,0.00,22.00,638.00,2900.00,2),(11,87,10,52,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',100.00,0.00,22.00,22.00,100.00,2),(12,87,11,53,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(13,87,12,54,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(16,88,15,55,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(17,88,16,56,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2);
/*!40000 ALTER TABLE `delivery_note_line` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `document_sequence`
--

DROP TABLE IF EXISTS `document_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `document_sequence` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `sequence_code` varchar(40) NOT NULL,
  `year_value` int NOT NULL,
  `next_number` int NOT NULL,
  `prefix` varchar(30) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_doc_sequence` (`tenant_id`,`sequence_code`,`year_value`),
  UNIQUE KEY `uc_755af292e5dcfc1e3a6a0b3a4` (`tenant_id`,`sequence_code`,`year_value`),
  UNIQUE KEY `UKbwv5mb3gm4hya280mofhcqh27` (`tenant_id`,`sequence_code`,`year_value`),
  CONSTRAINT `fk_doc_sequence_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=97 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `document_sequence`
--

LOCK TABLES `document_sequence` WRITE;
/*!40000 ALTER TABLE `document_sequence` DISABLE KEYS */;
INSERT INTO `document_sequence` VALUES (62,70,'QUOTE',2026,2,'PRE','2026-04-27 01:29:26','2026-04-27 01:29:26'),(63,72,'QUOTE',2026,2,'PRE','2026-04-27 19:58:16','2026-04-27 19:58:16'),(64,73,'QUOTE',2026,2,'PRE','2026-04-28 18:46:25','2026-04-28 18:46:25'),(65,73,'ORDER',2026,3,'ORD','2026-04-28 18:46:25','2026-04-29 20:26:18'),(66,74,'QUOTE',2026,2,'PRE','2026-04-28 20:08:48','2026-04-28 20:08:48'),(67,74,'ORDER',2026,2,'ORD','2026-04-28 20:08:48','2026-04-28 20:08:48'),(68,75,'QUOTE',2026,5,'PRE','2026-04-29 04:12:04','2026-05-02 03:24:46'),(69,76,'QUOTE',2026,2,'PRE','2026-04-29 04:12:04','2026-04-29 04:12:04'),(70,75,'ORDER',2026,5,'ORD','2026-04-29 04:12:04','2026-05-02 03:26:21'),(71,76,'ORDER',2026,2,'ORD','2026-04-29 04:12:04','2026-04-29 04:12:04'),(72,77,'QUOTE',2026,3,'PRE','2026-04-29 04:12:04','2026-04-29 04:12:04'),(73,78,'QUOTE',2026,2,'PRE','2026-04-29 04:12:04','2026-04-29 04:12:04'),(74,77,'ORDER',2026,3,'ORD','2026-04-29 04:12:04','2026-04-29 04:12:04'),(75,78,'ORDER',2026,2,'ORD','2026-04-29 04:12:04','2026-04-29 04:12:04'),(76,79,'QUOTE',2026,3,'PRE','2026-04-29 04:12:04','2026-04-29 04:12:04'),(77,80,'QUOTE',2026,2,'PRE','2026-04-29 04:12:04','2026-04-29 04:12:04'),(78,79,'ORDER',2026,3,'ORD','2026-04-29 04:12:04','2026-04-29 04:12:04'),(79,80,'ORDER',2026,2,'ORD','2026-04-29 04:12:04','2026-04-29 04:12:04'),(80,81,'QUOTE',2026,3,'PRE','2026-04-29 04:12:04','2026-04-29 04:12:04'),(81,82,'QUOTE',2026,2,'PRE','2026-04-29 04:12:04','2026-04-29 04:12:04'),(82,81,'ORDER',2026,3,'ORD','2026-04-29 04:12:04','2026-04-29 04:12:04'),(83,82,'ORDER',2026,2,'ORD','2026-04-29 04:12:04','2026-04-29 04:12:04'),(84,83,'QUOTE',2026,3,'PRE','2026-04-29 04:12:04','2026-04-29 04:12:04'),(85,84,'QUOTE',2026,4,'PRE','2026-04-29 04:12:04','2026-05-01 15:17:07'),(86,83,'ORDER',2026,3,'ORD','2026-04-29 04:12:05','2026-04-29 04:12:05'),(87,84,'ORDER',2026,4,'ORD','2026-04-29 04:12:05','2026-05-01 15:56:24'),(88,70,'ORDER',2026,2,'ORD','2026-04-29 20:08:14','2026-04-29 20:08:14'),(89,85,'QUOTE',2026,8,'PRE','2026-05-06 21:18:38','2026-05-07 10:30:24'),(90,85,'ORDER',2026,3,'ORD','2026-05-06 21:25:11','2026-05-07 00:10:48'),(91,86,'QUOTE',2026,3,'PRE','2026-05-07 01:28:49','2026-05-07 01:50:29'),(92,86,'ORDER',2026,3,'ORD','2026-05-07 01:29:52','2026-05-07 01:54:10'),(93,87,'QUOTE',2026,6,'PRE','2026-05-08 02:51:30','2026-05-09 12:22:40'),(94,87,'ORDER',2026,6,'ORD','2026-05-08 02:52:17','2026-05-09 12:22:43'),(95,88,'QUOTE',2026,3,'PRE','2026-05-09 13:29:47','2026-05-09 14:13:50'),(96,88,'ORDER',2026,3,'ORD','2026-05-09 13:29:56','2026-05-09 14:14:39');
/*!40000 ALTER TABLE `document_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `document_template`
--

DROP TABLE IF EXISTS `document_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `document_template`
--

LOCK TABLES `document_template` WRITE;
/*!40000 ALTER TABLE `document_template` DISABLE KEYS */;
/*!40000 ALTER TABLE `document_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` VALUES (1,'1','<< Flyway Baseline >>','BASELINE','<< Flyway Baseline >>',NULL,'root','2026-04-26 03:02:54',0,1);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `goods_receipt`
--

DROP TABLE IF EXISTS `goods_receipt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_receipt` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `receipt_number` varchar(50) NOT NULL,
  `receipt_date` date NOT NULL,
  `purchase_order_id` bigint NOT NULL,
  `supplier_id` bigint NOT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_goods_receipt_tenant_number` (`tenant_id`,`receipt_number`),
  KEY `idx_goods_receipt_order` (`tenant_id`,`purchase_order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `goods_receipt`
--

LOCK TABLES `goods_receipt` WRITE;
/*!40000 ALTER TABLE `goods_receipt` DISABLE KEYS */;
INSERT INTO `goods_receipt` VALUES (1,85,'GR-00001','2026-05-07',1,1,'Ricezione automatica da ordine fornitore PO-00001','2026-05-07 19:42:02','2026-05-07 19:42:02'),(2,87,'GR-00002','2026-05-08',2,1,'Ricezione automatica da ordine fornitore PO-00002','2026-05-08 01:03:27','2026-05-08 01:03:27'),(3,87,'GR-00003','2026-05-08',3,1,'Ricezione automatica da ordine fornitore PO-00003','2026-05-08 03:06:28','2026-05-08 03:06:28'),(4,87,'GR-00004','2026-05-08',4,1,'Ricezione automatica da ordine fornitore PO-00004','2026-05-08 14:21:15','2026-05-08 14:21:15'),(5,87,'GR-00005','2026-05-09',5,1,'Ricezione automatica da ordine fornitore PO-00005','2026-05-09 05:35:08','2026-05-09 05:35:08'),(6,88,'GR-00006','2026-05-09',6,1,'Ricezione automatica da ordine fornitore PO-00006','2026-05-09 20:14:52','2026-05-09 20:14:52');
/*!40000 ALTER TABLE `goods_receipt` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `goods_receipt_line`
--

DROP TABLE IF EXISTS `goods_receipt_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
  PRIMARY KEY (`id`),
  KEY `idx_goods_receipt_line_receipt` (`tenant_id`,`goods_receipt_id`),
  KEY `idx_goods_receipt_line_item` (`tenant_id`,`item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `goods_receipt_line`
--

LOCK TABLES `goods_receipt_line` WRITE;
/*!40000 ALTER TABLE `goods_receipt_line` DISABLE KEYS */;
INSERT INTO `goods_receipt_line` VALUES (1,85,1,1,2,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-07 19:42:02','2026-05-07 19:42:02'),(2,87,2,1,4,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',50.000,'2026-05-08 01:03:27','2026-05-08 01:03:27'),(3,87,3,1,6,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-08 03:06:28','2026-05-08 03:06:28'),(4,87,4,1,8,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',9.998,'2026-05-08 14:21:15','2026-05-08 14:21:15'),(5,87,5,1,9,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-09 05:35:08','2026-05-09 05:35:08'),(6,88,6,1,10,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',2.000,'2026-05-09 20:14:52','2026-05-09 20:14:52');
/*!40000 ALTER TABLE `goods_receipt_line` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `invoice`
--

DROP TABLE IF EXISTS `invoice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invoice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `delivery_note_id` bigint NOT NULL,
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
  UNIQUE KEY `uk_invoice_tenant_number` (`tenant_id`,`invoice_number`),
  UNIQUE KEY `uk_invoice_tenant_delivery_note` (`tenant_id`,`delivery_note_id`),
  KEY `idx_invoice_tenant` (`tenant_id`),
  KEY `idx_invoice_delivery_note` (`tenant_id`,`delivery_note_id`),
  KEY `idx_invoice_sales_order` (`tenant_id`,`sales_order_id`),
  KEY `idx_invoice_customer` (`tenant_id`,`customer_id`),
  KEY `idx_invoice_date` (`tenant_id`,`invoice_date`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invoice`
--

LOCK TABLES `invoice` WRITE;
/*!40000 ALTER TABLE `invoice` DISABLE KEYS */;
INSERT INTO `invoice` VALUES (1,75,1,29,76,'INV-2026-00001','2026-05-04','ISSUED','EUR',140.00,30.80,170.80,NULL,'2026-05-03 22:10:38','2026-05-03 22:10:38'),(2,75,3,9,75,'INV-2026-00002','2026-05-04','CANCELLED','EUR',1000.00,220.00,1220.00,NULL,'2026-05-03 22:12:39','2026-05-03 22:12:39'),(3,86,6,32,76,'INV-2026-00001','2026-05-07','ISSUED','EUR',5220.00,1148.40,6368.40,NULL,'2026-05-06 21:45:03','2026-05-06 21:45:03'),(4,86,7,33,75,'INV-2026-00002','2026-05-07','ISSUED','EUR',1080.00,237.60,1317.60,NULL,'2026-05-06 21:54:38','2026-05-06 21:54:38'),(5,87,9,35,75,'INV-2026-00001','2026-05-08','ISSUED','EUR',2900.00,638.00,3538.00,NULL,'2026-05-08 12:18:31','2026-05-08 12:18:31'),(6,87,10,36,76,'INV-2026-00002','2026-05-08','ISSUED','EUR',100.00,22.00,122.00,NULL,'2026-05-08 15:02:21','2026-05-08 15:02:21'),(7,87,11,37,76,'INV-2026-00003','2026-05-09','ISSUED','EUR',580.00,127.60,707.60,NULL,'2026-05-09 03:34:18','2026-05-09 03:34:18'),(8,88,15,39,75,'INV-2026-00001','2026-05-09','CANCELLED','EUR',580.00,127.60,707.60,NULL,'2026-05-09 09:34:24','2026-05-09 09:34:24'),(9,88,16,40,75,'INV-2026-00002','2026-05-09','ISSUED','EUR',580.00,127.60,707.60,NULL,'2026-05-09 12:02:58','2026-05-09 12:02:58');
/*!40000 ALTER TABLE `invoice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `invoice_line`
--

DROP TABLE IF EXISTS `invoice_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invoice_line` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `invoice_id` bigint NOT NULL,
  `delivery_note_line_id` bigint DEFAULT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invoice_line`
--

LOCK TABLES `invoice_line` WRITE;
/*!40000 ALTER TABLE `invoice_line` DISABLE KEYS */;
INSERT INTO `invoice_line` VALUES (1,75,1,1,1,'RIGA 1',1.000,'pz',100.00,10.00,22.00,19.80,109.80,NULL),(2,75,1,2,2,'RIGA 2',1.000,'pz',50.00,0.00,22.00,11.00,61.00,NULL),(3,75,2,4,1,'Modulo CRM',1.000,'pz',1000.00,0.00,22.00,220.00,1000.00,NULL),(4,86,3,7,1,'RIGA 1',10.000,'pz',580.00,10.00,22.00,1148.40,5220.00,2),(5,86,4,8,1,'RIGA 1',1.000,'pz',1200.00,10.00,22.00,237.60,1080.00,1),(6,87,5,10,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',5.000,'pz',580.00,0.00,22.00,638.00,2900.00,2),(7,87,6,11,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',100.00,0.00,22.00,22.00,100.00,2),(8,87,7,12,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(9,88,8,16,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(10,88,9,17,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2);
/*!40000 ALTER TABLE `invoice_line` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `item`
--

DROP TABLE IF EXISTS `item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `code` varchar(60) NOT NULL,
  `name` varchar(180) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `item_type` varchar(20) NOT NULL,
  `unit_of_measure` varchar(20) NOT NULL,
  `active` tinyint(1) NOT NULL,
  `track_stock` tinyint(1) NOT NULL,
  `base_price` decimal(15,2) DEFAULT NULL,
  `default_tax_pct` decimal(6,2) DEFAULT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_tenant_code` (`tenant_id`,`code`),
  KEY `idx_item_tenant_active` (`tenant_id`,`active`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `item`
--

LOCK TABLES `item` WRITE;
/*!40000 ALTER TABLE `item` DISABLE KEYS */;
INSERT INTO `item` VALUES (1,88,'ART-001','Modulo CRM','Modulo CRM','SERVICE','pz',1,0,1200.00,22.00,'2026-05-06 18:10:31','2026-05-06 19:03:38'),(2,88,'ART-002','FORNO ELETTRICO','FORNO ELETTRICO ALIMENTATO A 220V-230V','PRODUCT','pz',1,1,580.00,22.00,'2026-05-06 18:16:26','2026-05-06 18:16:26');
/*!40000 ALTER TABLE `item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `journal_entry`
--

DROP TABLE IF EXISTS `journal_entry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `journal_entry` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_journal_entry_tenant_number` (`tenant_id`,`entry_number`),
  KEY `idx_journal_entry_tenant_date` (`tenant_id`,`entry_date`),
  KEY `idx_journal_entry_tenant_causal` (`tenant_id`,`causal_code`),
  KEY `idx_journal_entry_tenant_ref` (`tenant_id`,`reference_type`,`reference_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `journal_entry`
--

LOCK TABLES `journal_entry` WRITE;
/*!40000 ALTER TABLE `journal_entry` DISABLE KEYS */;
/*!40000 ALTER TABLE `journal_entry` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `journal_entry_line`
--

DROP TABLE IF EXISTS `journal_entry_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `journal_entry_line` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
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
  KEY `idx_journal_entry_line_tenant_account` (`tenant_id`,`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `journal_entry_line`
--

LOCK TABLES `journal_entry_line` WRITE;
/*!40000 ALTER TABLE `journal_entry_line` DISABLE KEYS */;
/*!40000 ALTER TABLE `journal_entry_line` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `opportunity`
--

DROP TABLE IF EXISTS `opportunity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `opportunity`
--

LOCK TABLES `opportunity` WRITE;
/*!40000 ALTER TABLE `opportunity` DISABLE KEYS */;
/*!40000 ALTER TABLE `opportunity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_due`
--

DROP TABLE IF EXISTS `payment_due`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_due` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
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
  KEY `idx_payment_due_tenant_due_date` (`tenant_id`,`due_date`),
  KEY `idx_payment_due_tenant_direction_status` (`tenant_id`,`direction`,`status`),
  KEY `idx_payment_due_tenant_ref` (`tenant_id`,`reference_type`,`reference_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_due`
--

LOCK TABLES `payment_due` WRITE;
/*!40000 ALTER TABLE `payment_due` DISABLE KEYS */;
INSERT INTO `payment_due` VALUES (1,87,'RECEIVABLE','CUSTOMER',75,'INV-2026-00001','2026-05-08','2026-06-07','CUSTOMER_INVOICE',5,'EUR',3538.00,3538.00,0.00,'PAID','Scadenza generata da fattura cliente','2026-05-08 14:18:31','2026-05-09 05:46:21'),(2,87,'PAYABLE','SUPPLIER',1,'SI-00003','2026-05-08','2026-06-07','SUPPLIER_INVOICE',3,'EUR',7074.58,7074.58,0.00,'PAID','Scadenza generata da fattura fornitore','2026-05-08 14:21:23','2026-05-08 17:11:02'),(3,87,'RECEIVABLE','CUSTOMER',76,'INV-2026-00002','2026-05-08','2026-06-07','CUSTOMER_INVOICE',6,'EUR',122.00,122.00,0.00,'PAID','Scadenza generata da fattura cliente','2026-05-08 17:02:21','2026-05-08 17:05:37'),(4,87,'RECEIVABLE','CUSTOMER',76,'INV-2026-00003','2026-05-09','2026-06-08','CUSTOMER_INVOICE',7,'EUR',707.60,0.00,707.60,'OPEN','Scadenza generata da fattura cliente','2026-05-09 05:34:18','2026-05-09 05:34:18'),(5,87,'PAYABLE','SUPPLIER',1,'SI-00004','2026-05-09','2026-06-08','SUPPLIER_INVOICE',4,'EUR',707.60,707.60,0.00,'PAID','Scadenza generata da fattura fornitore','2026-05-09 05:35:11','2026-05-09 05:48:06'),(6,88,'RECEIVABLE','CUSTOMER',75,'INV-2026-00001','2026-05-09','2026-06-08','CUSTOMER_INVOICE',8,'EUR',707.60,707.60,0.00,'PAID','Scadenza generata da fattura cliente','2026-05-09 11:34:24','2026-05-09 19:39:22'),(7,88,'RECEIVABLE','CUSTOMER',75,'INV-2026-00002','2026-05-09','2026-06-08','CUSTOMER_INVOICE',9,'EUR',707.60,707.60,0.00,'PAID','Scadenza generata da fattura cliente','2026-05-09 14:02:58','2026-05-09 20:17:16'),(8,88,'PAYABLE','SUPPLIER',1,'SI-00005','2026-05-09','2026-06-08','SUPPLIER_INVOICE',5,'EUR',1415.20,1400.00,15.20,'PARTIALLY_PAID','Scadenza generata da fattura fornitore','2026-05-09 20:15:04','2026-05-09 20:16:15');
/*!40000 ALTER TABLE `payment_due` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_due_transaction`
--

DROP TABLE IF EXISTS `payment_due_transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_due_transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_due_transaction`
--

LOCK TABLES `payment_due_transaction` WRITE;
/*!40000 ALTER TABLE `payment_due_transaction` DISABLE KEYS */;
INSERT INTO `payment_due_transaction` VALUES (1,87,3,'2026-05-08',82.00,'RECEIPT','PROVA INCASSO','2026-05-08 17:04:36','2026-05-08 17:04:36'),(2,87,3,'2026-05-22',40.00,'RECEIPT','PROVA INCASSO','2026-05-08 17:05:37','2026-05-08 17:05:37'),(3,87,2,'2026-05-08',7014.58,'PAYMENT','PROVA PAGAMENTO FATTURA A FORNITOR','2026-05-08 17:10:04','2026-05-08 17:10:04'),(4,87,2,'2026-05-22',60.00,'PAYMENT','PAGAMENTO A FORNITORE','2026-05-08 17:11:02','2026-05-08 17:11:02'),(5,87,1,'2026-05-09',3478.00,'RECEIPT','prova prima nota incasso','2026-05-09 05:45:45','2026-05-09 05:45:45'),(6,87,1,'2026-05-23',60.00,'RECEIPT','PROVA PRIMA NOTA INCASSO','2026-05-09 05:46:21','2026-05-09 05:46:21'),(7,87,5,'2026-05-09',707.56,'PAYMENT','PROVA PRIMA NOTA PAGAMENTO A FORNITORE','2026-05-09 05:47:42','2026-05-09 05:47:42'),(8,87,5,'2026-05-09',0.04,'PAYMENT','','2026-05-09 05:48:06','2026-05-09 05:48:06'),(9,88,6,'2026-05-09',700.00,'RECEIPT','PROVA DASHBOARD','2026-05-09 19:38:13','2026-05-09 19:38:13'),(10,88,6,'2026-05-09',7.60,'RECEIPT','PROVA DASHBOARD','2026-05-09 19:39:22','2026-05-09 19:39:22'),(11,88,8,'2026-05-09',1400.00,'PAYMENT','PROVA DASHBOARD','2026-05-09 20:16:15','2026-05-09 20:16:15'),(12,88,7,'2026-05-09',707.60,'RECEIPT','PROVA DASHBOARD','2026-05-09 20:17:16','2026-05-09 20:17:16');
/*!40000 ALTER TABLE `payment_due_transaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_reminder`
--

DROP TABLE IF EXISTS `payment_reminder`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_reminder`
--

LOCK TABLES `payment_reminder` WRITE;
/*!40000 ALTER TABLE `payment_reminder` DISABLE KEYS */;
/*!40000 ALTER TABLE `payment_reminder` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_schedule`
--

DROP TABLE IF EXISTS `payment_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_schedule`
--

LOCK TABLES `payment_schedule` WRITE;
/*!40000 ALTER TABLE `payment_schedule` DISABLE KEYS */;
/*!40000 ALTER TABLE `payment_schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permission`
--

DROP TABLE IF EXISTS `permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permission`
--

LOCK TABLES `permission` WRITE;
/*!40000 ALTER TABLE `permission` DISABLE KEYS */;
/*!40000 ALTER TABLE `permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `persistent_logins`
--

DROP TABLE IF EXISTS `persistent_logins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `persistent_logins` (
  `username` varchar(64) NOT NULL,
  `series` varchar(64) NOT NULL,
  `token` varchar(64) NOT NULL,
  `last_used` timestamp NOT NULL,
  PRIMARY KEY (`series`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `persistent_logins`
--

LOCK TABLES `persistent_logins` WRITE;
/*!40000 ALTER TABLE `persistent_logins` DISABLE KEYS */;
/*!40000 ALTER TABLE `persistent_logins` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `purchase_order`
--

DROP TABLE IF EXISTS `purchase_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
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
  UNIQUE KEY `uk_purchase_order_tenant_number` (`tenant_id`,`order_number`),
  KEY `idx_purchase_order_tenant_supplier` (`tenant_id`,`supplier_id`),
  KEY `idx_purchase_order_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchase_order`
--

LOCK TABLES `purchase_order` WRITE;
/*!40000 ALTER TABLE `purchase_order` DISABLE KEYS */;
INSERT INTO `purchase_order` VALUES (1,85,'PO-00001','2026-05-07','2026-05-22',1,'CONFIRMED','EUR','ORDINE 10 PEZZI FORNO ELETTRICO',5800.00,1276.00,7076.00,'2026-05-07 16:59:48','2026-05-07 17:05:37'),(2,87,'PO-00002','2026-05-08','2026-05-15',1,'CONFIRMED','EUR','ORDINE A FORNITORE DI 50 FORNI ELETTRICI',23200.00,5104.00,28304.00,'2026-05-08 01:02:46','2026-05-08 01:03:15'),(3,87,'PO-00003','2026-05-08','2026-05-15',1,'CONFIRMED','EUR','PROVA',4640.00,1020.80,5660.80,'2026-05-08 03:05:48','2026-05-08 03:06:22'),(4,87,'PO-00004','2026-05-08','2026-05-15',1,'CONFIRMED','EUR','PROVA ADUE',5798.84,1275.74,7074.58,'2026-05-08 14:20:52','2026-05-08 14:21:10'),(5,87,'PO-00005','2026-05-09','2026-05-16',1,'CONFIRMED','EUR','PROVA PRIMA NOTA FORNITORE',580.00,127.60,707.60,'2026-05-09 05:35:04','2026-05-09 05:35:04'),(6,88,'PO-00006','2026-05-09','2026-05-16',1,'CONFIRMED','EUR','PROVA DASHBOARD',1160.00,255.20,1415.20,'2026-05-09 20:14:41','2026-05-09 20:14:41');
/*!40000 ALTER TABLE `purchase_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `purchase_order_line`
--

DROP TABLE IF EXISTS `purchase_order_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchase_order_line`
--

LOCK TABLES `purchase_order_line` WRITE;
/*!40000 ALTER TABLE `purchase_order_line` DISABLE KEYS */;
INSERT INTO `purchase_order_line` VALUES (2,85,1,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,580.00,0.00,22.00,5800.00,1276.00,7076.00,'2026-05-07 17:05:37','2026-05-07 17:05:37'),(4,87,2,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',50.000,580.00,20.00,22.00,23200.00,5104.00,28304.00,'2026-05-08 01:03:15','2026-05-08 01:03:15'),(6,87,3,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,580.00,20.00,22.00,4640.00,1020.80,5660.80,'2026-05-08 03:06:22','2026-05-08 03:06:22'),(8,87,4,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',9.998,580.00,0.00,22.00,5798.84,1275.74,7074.58,'2026-05-08 14:21:10','2026-05-08 14:21:10'),(9,87,5,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,127.60,707.60,'2026-05-09 05:35:04','2026-05-09 05:35:04'),(10,88,6,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',2.000,580.00,0.00,22.00,1160.00,255.20,1415.20,'2026-05-09 20:14:41','2026-05-09 20:14:41');
/*!40000 ALTER TABLE `purchase_order_line` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quote`
--

DROP TABLE IF EXISTS `quote`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_quote_number` (`tenant_id`,`quote_number`),
  UNIQUE KEY `uc_e78bafd14f0ef376c44e492d8` (`tenant_id`,`quote_number`),
  UNIQUE KEY `UKlma9ds8gfmx9xqhv4tpjh6goo` (`tenant_id`,`quote_number`),
  KEY `fk_quote_customer` (`customer_id`),
  CONSTRAINT `fk_quote_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`),
  CONSTRAINT `fk_quote_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=99 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quote`
--

LOCK TABLES `quote` WRITE;
/*!40000 ALTER TABLE `quote` DISABLE KEYS */;
INSERT INTO `quote` VALUES (56,70,70,'PRE-2026-00001','2026-04-26','2026-05-26','ACCEPTED','EUR',1100.00,242.00,1342.00,'Preventivo tenant A','2026-04-27 01:29:26','2026-04-29 20:08:14'),(57,72,72,'PRE-2026-00001','2026-04-26','2026-05-26','DRAFT','EUR',900.00,198.00,1098.00,'Preview HTML test','2026-04-27 19:58:16','2026-04-27 19:58:16'),(58,73,73,'PRE-2026-00001','2026-04-28','2026-05-28','ACCEPTED','EUR',1100.00,242.00,1342.00,NULL,'2026-04-28 18:46:25','2026-04-28 18:46:25'),(59,74,74,'PRE-2026-00001','2026-04-28','2026-05-28','ACCEPTED','EUR',1100.00,242.00,1342.00,NULL,'2026-04-28 20:08:48','2026-04-28 20:08:48'),(60,75,75,'PRE-2026-00001','2026-04-29','2026-05-29','ACCEPTED','EUR',1000.00,220.00,1220.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(61,75,76,'PRE-2026-00002','2026-04-29','2026-05-29','ACCEPTED','EUR',200.00,44.00,244.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(62,76,77,'PRE-2026-00001','2026-04-29','2026-05-29','ACCEPTED','EUR',500.00,110.00,610.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(63,77,78,'PRE-2026-00001','2026-04-29','2026-05-29','ACCEPTED','EUR',1000.00,220.00,1220.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(64,77,79,'PRE-2026-00002','2026-04-29','2026-05-29','ACCEPTED','EUR',200.00,44.00,244.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(65,78,80,'PRE-2026-00001','2026-04-29','2026-05-29','ACCEPTED','EUR',500.00,110.00,610.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(66,79,81,'PRE-2026-00001','2026-04-29','2026-05-29','ACCEPTED','EUR',1000.00,220.00,1220.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(67,79,82,'PRE-2026-00002','2026-04-29','2026-05-29','ACCEPTED','EUR',200.00,44.00,244.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(68,80,83,'PRE-2026-00001','2026-04-29','2026-05-29','ACCEPTED','EUR',500.00,110.00,610.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(69,81,84,'PRE-2026-00001','2026-04-29','2026-05-29','ACCEPTED','EUR',1000.00,220.00,1220.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(70,81,85,'PRE-2026-00002','2026-04-29','2026-05-29','ACCEPTED','EUR',200.00,44.00,244.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(71,82,86,'PRE-2026-00001','2026-04-29','2026-05-29','ACCEPTED','EUR',500.00,110.00,610.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(72,83,87,'PRE-2026-00001','2026-04-29','2026-05-29','ACCEPTED','EUR',1000.00,220.00,1220.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:05'),(73,83,88,'PRE-2026-00002','2026-04-29','2026-05-29','ACCEPTED','EUR',200.00,44.00,244.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:05'),(74,84,89,'PRE-2026-00001','2026-04-29','2026-05-29','ACCEPTED','EUR',500.00,110.00,610.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:05'),(75,84,89,'PRE-2026-00002','2026-05-02','2026-06-30','DRAFT','EUR',1900.00,418.00,2318.00,'Prova preventivo','2026-05-01 14:05:51','2026-05-01 14:14:17'),(76,84,89,'PRE-2026-00003','2026-05-02','2026-06-30','ACCEPTED','EUR',2190.00,481.80,2671.80,'PROVA','2026-05-01 15:17:07','2026-05-01 15:56:10'),(77,75,76,'PRE-2026-00003','2026-05-01','2026-05-31','ACCEPTED','EUR',90.00,19.80,109.80,'PROVA CLIENTE','2026-05-01 17:59:05','2026-05-01 18:05:01'),(78,75,76,'PRE-2026-00004','2026-05-02','2026-06-01','ACCEPTED','EUR',140.00,30.80,170.80,'PROVA RICALCOLO TOTALI','2026-05-02 03:24:46','2026-05-02 03:25:13'),(79,85,75,'PRE-2026-00001','2026-05-06','2026-06-05','ACCEPTED','EUR',580.00,127.60,707.60,'PREVENTIVO PER IL CLIENTE ROSSI S.R.L','2026-05-06 21:18:38','2026-05-06 21:24:39'),(80,85,76,'PRE-2026-00002','2026-05-06','2026-06-05','ACCEPTED','EUR',1200.00,264.00,1464.00,'PREVENTIVO PER IL CLIENTE BIANCHI S.R.L','2026-05-06 21:23:17','2026-05-06 21:23:17'),(81,85,76,'PRE-2026-00003','2026-05-06','2026-06-05','DRAFT','EUR',580.00,127.60,707.60,'prova','2026-05-06 23:03:08','2026-05-06 23:03:08'),(82,85,76,'PRE-2026-00004','2026-05-06','2026-06-05','DRAFT','EUR',800.00,176.00,976.00,'prova itemId','2026-05-06 23:12:44','2026-05-06 23:12:44'),(83,85,75,'PRE-2026-00005','2026-05-06','2026-06-05','DRAFT','EUR',600.00,132.00,732.00,'prova','2026-05-06 23:25:10','2026-05-06 23:25:10'),(84,85,75,'PRE-2026-00006','2026-05-06','2026-06-05','ACCEPTED','EUR',464.00,102.08,566.08,'PREVENTIVO PER L\'AZIENDA ROSSI S.R.L','2026-05-06 23:28:27','2026-05-06 23:35:02'),(85,86,76,'PRE-2026-00001','2026-05-07','2026-06-06','ACCEPTED','EUR',5220.00,1148.40,6368.40,'PREVENTIVO PER IL CLIENTE BIANCHI S.R.L','2026-05-07 01:28:49','2026-05-07 01:29:52'),(86,86,75,'PRE-2026-00003','2026-05-07','2026-06-06','ACCEPTED','EUR',1080.00,237.60,1317.60,'PROVA ARTICOLO SERVICE','2026-05-07 01:50:01','2026-05-07 01:54:10'),(91,85,76,'PRE-2026-00007','2026-05-07','2026-06-06','SENT','EUR',1200.00,264.00,1464.00,'','2026-05-07 10:30:24','2026-05-07 10:30:44'),(92,87,76,'PRE-2026-00001','2026-05-08','2026-06-07','ACCEPTED','EUR',11600.00,2552.00,14152.00,'ACQUISTO DI 20 FORNI ELETTRICI','2026-05-08 02:51:30','2026-05-08 02:51:51'),(93,87,75,'PRE-2026-00002','2026-05-08','2026-06-07','ACCEPTED','EUR',2900.00,638.00,3538.00,'PROVA ADUE','2026-05-08 16:17:53','2026-05-08 16:17:53'),(94,87,76,'PRE-2026-00003','2026-05-08','2026-06-07','ACCEPTED','EUR',100.00,22.00,122.00,'PROVA INCASSO','2026-05-08 19:01:50','2026-05-08 19:01:50'),(95,87,76,'PRE-2026-00004','2026-05-09','2026-06-08','ACCEPTED','EUR',580.00,127.60,707.60,'PROVA PRIMA NOTA','2026-05-09 07:34:02','2026-05-09 07:34:02'),(96,87,76,'PRE-2026-00005','2026-05-09','2026-06-08','ACCEPTED','EUR',580.00,127.60,707.60,'PROVA MODALE','2026-05-09 12:22:40','2026-05-09 12:22:40'),(97,88,75,'PRE-2026-00001','2026-05-09','2026-06-08','ACCEPTED','EUR',580.00,127.60,707.60,'PROVA MODALE','2026-05-09 13:29:47','2026-05-09 13:29:47'),(98,88,75,'PRE-2026-00002','2026-05-09','2026-06-08','ACCEPTED','EUR',580.00,127.60,707.60,'PROVA MODALE','2026-05-09 14:13:50','2026-05-09 14:13:50');
/*!40000 ALTER TABLE `quote` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quote_line`
--

DROP TABLE IF EXISTS `quote_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quote_line` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `quote_id` bigint NOT NULL,
  `line_no` int NOT NULL,
  `description` varchar(255) NOT NULL,
  `quantity` decimal(15,3) NOT NULL,
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
  CONSTRAINT `fk_quote_line_quote` FOREIGN KEY (`quote_id`) REFERENCES `quote` (`id`),
  CONSTRAINT `fk_quote_line_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=235 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quote_line`
--

LOCK TABLES `quote_line` WRITE;
/*!40000 ALTER TABLE `quote_line` DISABLE KEYS */;
INSERT INTO `quote_line` VALUES (127,70,56,1,'Modulo CRM',1.000,1000.00,10.00,22.00,900.00,'2026-04-27 01:29:26','2026-04-27 01:29:26',NULL,NULL,NULL,NULL),(128,70,56,2,'Setup',1.000,200.00,0.00,22.00,200.00,'2026-04-27 01:29:26','2026-04-27 01:29:26',NULL,NULL,NULL,NULL),(129,72,57,1,'Modulo CRM',1.000,1000.00,10.00,22.00,900.00,'2026-04-27 19:58:16','2026-04-27 19:58:16',NULL,NULL,NULL,NULL),(132,73,58,1,'Modulo CRM',1.000,1000.00,10.00,22.00,900.00,'2026-04-28 18:46:25','2026-04-28 18:46:25',NULL,NULL,NULL,NULL),(133,73,58,2,'Setup',1.000,200.00,0.00,22.00,200.00,'2026-04-28 18:46:25','2026-04-28 18:46:25',NULL,NULL,NULL,NULL),(136,74,59,1,'Modulo CRM',1.000,1000.00,10.00,22.00,900.00,'2026-04-28 20:08:48','2026-04-28 20:08:48',NULL,NULL,NULL,NULL),(137,74,59,2,'Setup',1.000,200.00,0.00,22.00,200.00,'2026-04-28 20:08:48','2026-04-28 20:08:48',NULL,NULL,NULL,NULL),(139,75,60,1,'Modulo CRM',1.000,1000.00,0.00,22.00,1000.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(141,75,61,1,'Setup',1.000,200.00,0.00,22.00,200.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(143,76,62,1,'Servizio Altro Tenant',1.000,500.00,0.00,22.00,500.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(145,77,63,1,'Modulo CRM',1.000,1000.00,0.00,22.00,1000.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(147,77,64,1,'Setup',1.000,200.00,0.00,22.00,200.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(149,78,65,1,'Servizio Altro Tenant',1.000,500.00,0.00,22.00,500.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(151,79,66,1,'Modulo CRM',1.000,1000.00,0.00,22.00,1000.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(153,79,67,1,'Setup',1.000,200.00,0.00,22.00,200.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(155,80,68,1,'Servizio Altro Tenant',1.000,500.00,0.00,22.00,500.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(157,81,69,1,'Modulo CRM',1.000,1000.00,0.00,22.00,1000.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(159,81,70,1,'Setup',1.000,200.00,0.00,22.00,200.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(161,82,71,1,'Servizio Altro Tenant',1.000,500.00,0.00,22.00,500.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(163,83,72,1,'Modulo CRM',1.000,1000.00,0.00,22.00,1000.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(165,83,73,1,'Setup',1.000,200.00,0.00,22.00,200.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(167,84,74,1,'Servizio Altro Tenant',1.000,500.00,0.00,22.00,500.00,'2026-04-29 04:12:05','2026-04-29 04:12:05',NULL,NULL,NULL,NULL),(182,84,75,1,'RIGA 1',1.000,1000.00,0.00,22.00,1000.00,'2026-05-01 14:14:53','2026-05-01 14:14:53',NULL,NULL,NULL,NULL),(183,84,75,2,'RIGA 2',2.000,500.00,10.00,22.00,900.00,'2026-05-01 14:14:53','2026-05-01 14:14:53',NULL,NULL,NULL,NULL),(190,84,76,1,'RIGA 1',1.000,100.00,10.00,22.00,90.00,'2026-05-01 15:56:10','2026-05-01 15:56:10',NULL,NULL,NULL,NULL),(191,84,76,2,'RIGA 2',1.000,500.00,0.00,22.00,500.00,'2026-05-01 15:56:10','2026-05-01 15:56:10',NULL,NULL,NULL,NULL),(192,84,76,3,'RIGA 3',1.000,2000.00,20.00,22.00,1600.00,'2026-05-01 15:56:10','2026-05-01 15:56:10',NULL,NULL,NULL,NULL),(194,75,77,1,'RIGA 1',1.000,100.00,10.00,22.00,90.00,'2026-05-01 18:05:01','2026-05-01 18:05:01',NULL,NULL,NULL,NULL),(197,75,78,1,'RIGA 1',1.000,100.00,10.00,22.00,90.00,'2026-05-02 03:25:13','2026-05-02 03:25:13',NULL,NULL,NULL,NULL),(198,75,78,2,'RIGA 2',1.000,50.00,0.00,22.00,50.00,'2026-05-02 03:25:13','2026-05-02 03:25:13',NULL,NULL,NULL,NULL),(204,85,79,1,'RIGA 1',1.000,580.00,0.00,22.00,580.00,'2026-05-06 21:24:39','2026-05-06 21:24:39',NULL,NULL,NULL,NULL),(206,85,80,1,'RIGA 1',1.000,1200.00,0.00,22.00,1200.00,'2026-05-06 22:44:38','2026-05-06 22:44:38',NULL,NULL,NULL,NULL),(207,85,81,1,'RIGA 1',1.000,580.00,0.00,22.00,580.00,'2026-05-06 23:03:08','2026-05-06 23:03:08',NULL,NULL,NULL,NULL),(208,85,82,1,'RIGA 1',1.000,800.00,0.00,22.00,800.00,'2026-05-06 23:12:44','2026-05-06 23:12:44',NULL,NULL,NULL,2),(209,85,83,1,'RIGA 1',1.000,600.00,0.00,22.00,600.00,'2026-05-06 23:25:10','2026-05-06 23:25:10',NULL,NULL,NULL,2),(212,85,84,1,'RIGA 1',1.000,580.00,20.00,22.00,464.00,'2026-05-06 23:35:02','2026-05-06 23:35:02',NULL,NULL,NULL,2),(214,86,85,1,'RIGA 1',10.000,580.00,10.00,22.00,5220.00,'2026-05-07 01:29:48','2026-05-07 01:29:48',NULL,NULL,NULL,2),(216,86,86,1,'RIGA 1',1.000,1200.00,10.00,22.00,1080.00,'2026-05-07 01:50:29','2026-05-07 01:50:29',NULL,NULL,NULL,1),(219,85,91,1,'Modulo CRM',1.000,1200.00,0.00,22.00,1200.00,'2026-05-07 10:30:44','2026-05-07 10:30:44',NULL,NULL,NULL,1),(222,87,92,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',20.000,580.00,0.00,22.00,11600.00,'2026-05-08 02:51:51','2026-05-08 02:51:51',NULL,NULL,NULL,2),(224,87,93,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',5.000,580.00,0.00,22.00,2900.00,'2026-05-08 16:17:53','2026-05-08 16:17:53',NULL,NULL,NULL,2),(226,87,94,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,0.00,22.00,100.00,'2026-05-08 19:01:50','2026-05-08 19:01:50',NULL,NULL,NULL,2),(228,87,95,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,'2026-05-09 07:34:02','2026-05-09 07:34:02',NULL,NULL,NULL,2),(230,87,96,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,'2026-05-09 12:22:40','2026-05-09 12:22:40',NULL,NULL,NULL,2),(232,88,97,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,'2026-05-09 13:29:47','2026-05-09 13:29:47',NULL,NULL,NULL,2),(234,88,98,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,'2026-05-09 14:13:50','2026-05-09 14:13:50',NULL,NULL,NULL,2);
/*!40000 ALTER TABLE `quote_line` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (1,'2026-05-05 05:42:48.719345','2026-05-05 05:42:48.719349',85,'ROLE_ADMIN','Amministratore',_binary ''),(2,'2026-05-07 01:20:10.839427','2026-05-07 01:20:10.839431',86,'ROLE_ADMIN','Amministratore',_binary ''),(3,'2026-05-08 02:46:04.641656','2026-05-08 02:46:04.641660',87,'ROLE_ADMIN','Amministratore',_binary ''),(4,'2026-05-09 13:26:16.983978','2026-05-09 13:26:16.983981',88,'ROLE_ADMIN','Amministratore',_binary '');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role_permission`
--

DROP TABLE IF EXISTS `role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_permission`
--

LOCK TABLES `role_permission` WRITE;
/*!40000 ALTER TABLE `role_permission` DISABLE KEYS */;
/*!40000 ALTER TABLE `role_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sales_order`
--

DROP TABLE IF EXISTS `sales_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
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
  UNIQUE KEY `uk_order_number` (`tenant_id`,`order_number`),
  UNIQUE KEY `uc_a990dfe94f127c1be31f33265` (`tenant_id`,`order_number`),
  UNIQUE KEY `UKfaxftiydq8v568gv4k264yoc6` (`tenant_id`,`order_number`),
  KEY `fk_order_customer` (`customer_id`),
  KEY `fk_order_quote` (`quote_id`),
  CONSTRAINT `fk_order_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`),
  CONSTRAINT `fk_order_quote` FOREIGN KEY (`quote_id`) REFERENCES `quote` (`id`),
  CONSTRAINT `fk_order_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sales_order`
--

LOCK TABLES `sales_order` WRITE;
/*!40000 ALTER TABLE `sales_order` DISABLE KEYS */;
INSERT INTO `sales_order` VALUES (8,74,74,59,'ORD-2026-00001','2026-04-28','CONFIRMED','EUR',1342.00,'2026-04-28 20:08:48','2026-04-28 20:08:48',1100.00,242.00),(9,75,75,60,'ORD-2026-00001','2026-04-29','FULFILLED','EUR',1220.00,'2026-04-29 04:12:04','2026-05-02 05:27:45',1000.00,220.00),(10,75,76,61,'ORD-2026-00002','2026-04-29','FULFILLED','EUR',244.00,'2026-04-29 04:12:04','2026-05-02 23:38:48',200.00,44.00),(11,76,77,62,'ORD-2026-00001','2026-04-29','CONFIRMED','EUR',610.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',500.00,110.00),(12,77,78,63,'ORD-2026-00001','2026-04-29','CONFIRMED','EUR',1220.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',1000.00,220.00),(13,77,79,64,'ORD-2026-00002','2026-04-29','CONFIRMED','EUR',244.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',200.00,44.00),(14,78,80,65,'ORD-2026-00001','2026-04-29','CONFIRMED','EUR',610.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',500.00,110.00),(15,79,81,66,'ORD-2026-00001','2026-04-29','CONFIRMED','EUR',1220.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',1000.00,220.00),(16,79,82,67,'ORD-2026-00002','2026-04-29','CONFIRMED','EUR',244.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',200.00,44.00),(17,80,83,68,'ORD-2026-00001','2026-04-29','CONFIRMED','EUR',610.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',500.00,110.00),(18,81,84,69,'ORD-2026-00001','2026-04-29','CONFIRMED','EUR',1220.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',1000.00,220.00),(19,81,85,70,'ORD-2026-00002','2026-04-29','CONFIRMED','EUR',244.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',200.00,44.00),(20,82,86,71,'ORD-2026-00001','2026-04-29','CONFIRMED','EUR',610.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',500.00,110.00),(21,83,87,72,'ORD-2026-00001','2026-04-29','CONFIRMED','EUR',1220.00,'2026-04-29 04:12:05','2026-04-29 04:12:05',1000.00,220.00),(22,83,88,73,'ORD-2026-00002','2026-04-29','CONFIRMED','EUR',244.00,'2026-04-29 04:12:05','2026-04-29 04:12:05',200.00,44.00),(23,84,89,74,'ORD-2026-00001','2026-04-29','CONFIRMED','EUR',610.00,'2026-04-29 04:12:05','2026-04-29 04:12:05',500.00,110.00),(24,70,70,56,'ORD-2026-00001','2026-04-29','CONFIRMED','EUR',1342.00,'2026-04-29 20:08:14','2026-04-29 20:08:14',1100.00,242.00),(25,73,73,58,'ORD-2026-00002','2026-04-29','CONFIRMED','EUR',1342.00,'2026-04-29 20:26:18','2026-04-29 20:26:18',1100.00,242.00),(27,84,89,76,'ORD-2026-00003','2026-05-01','CONFIRMED','EUR',2671.80,'2026-05-01 15:56:24','2026-05-01 15:56:24',2190.00,481.80),(28,75,76,77,'ORD-2026-00003','2026-05-01','CANCELLED','EUR',109.80,'2026-05-01 18:05:04','2026-05-02 05:25:37',90.00,19.80),(29,75,76,78,'ORD-2026-00004','2026-05-02','FULFILLED','EUR',170.80,'2026-05-02 03:26:21','2026-05-02 05:20:53',140.00,30.80),(30,85,75,79,'ORD-2026-00001','2026-05-06','FULFILLED','EUR',707.60,'2026-05-06 21:25:11','2026-05-06 21:28:33',580.00,127.60),(31,85,75,84,'ORD-2026-00002','2026-05-07','FULFILLED','EUR',566.08,'2026-05-07 00:10:05','2026-05-07 00:11:54',464.00,102.08),(32,86,76,85,'ORD-2026-00001','2026-05-07','FULFILLED','EUR',6368.40,'2026-05-07 01:29:52','2026-05-07 01:31:09',5220.00,1148.40),(33,86,75,86,'ORD-2026-00002','2026-05-07','FULFILLED','EUR',1317.60,'2026-05-07 01:54:10','2026-05-07 01:54:17',1080.00,237.60),(34,87,76,92,'ORD-2026-00001','2026-05-08','FULFILLED','EUR',14152.00,'2026-05-08 02:52:17','2026-05-08 02:52:36',11600.00,2552.00),(35,87,75,93,'ORD-2026-00002','2026-05-08','FULFILLED','EUR',3538.00,'2026-05-08 16:18:01','2026-05-08 16:18:12',2900.00,638.00),(36,87,76,94,'ORD-2026-00003','2026-05-08','FULFILLED','EUR',122.00,'2026-05-08 19:02:01','2026-05-08 19:02:06',100.00,22.00),(37,87,76,95,'ORD-2026-00004','2026-05-09','FULFILLED','EUR',707.60,'2026-05-09 07:34:07','2026-05-09 07:34:10',580.00,127.60),(38,87,76,96,'ORD-2026-00005','2026-05-09','FULFILLED','EUR',707.60,'2026-05-09 12:22:43','2026-05-09 12:22:49',580.00,127.60),(39,88,75,97,'ORD-2026-00001','2026-05-09','FULFILLED','EUR',707.60,'2026-05-09 13:29:56','2026-05-09 13:30:02',580.00,127.60),(40,88,75,98,'ORD-2026-00002','2026-05-09','FULFILLED','EUR',707.60,'2026-05-09 14:14:39','2026-05-09 15:52:40',580.00,127.60);
/*!40000 ALTER TABLE `sales_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sales_order_line`
--

DROP TABLE IF EXISTS `sales_order_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_order_line` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `sales_order_id` bigint NOT NULL,
  `line_no` int NOT NULL,
  `description` varchar(255) NOT NULL,
  `quantity` decimal(15,3) NOT NULL,
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
  CONSTRAINT `fk_order_line_order` FOREIGN KEY (`sales_order_id`) REFERENCES `sales_order` (`id`),
  CONSTRAINT `fk_order_line_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sales_order_line`
--

LOCK TABLES `sales_order_line` WRITE;
/*!40000 ALTER TABLE `sales_order_line` DISABLE KEYS */;
INSERT INTO `sales_order_line` VALUES (15,74,8,1,'Modulo CRM',1.000,1000.00,900.00,'2026-04-28 20:08:48','2026-04-28 20:08:48',10.00,22.00,198.00,NULL),(16,74,8,2,'Setup',1.000,200.00,200.00,'2026-04-28 20:08:48','2026-04-28 20:08:48',0.00,22.00,44.00,NULL),(17,75,9,1,'Modulo CRM',1.000,1000.00,1000.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,220.00,NULL),(18,75,10,1,'Setup',1.000,200.00,200.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,44.00,NULL),(19,76,11,1,'Servizio Altro Tenant',1.000,500.00,500.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,110.00,NULL),(20,77,12,1,'Modulo CRM',1.000,1000.00,1000.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,220.00,NULL),(21,77,13,1,'Setup',1.000,200.00,200.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,44.00,NULL),(22,78,14,1,'Servizio Altro Tenant',1.000,500.00,500.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,110.00,NULL),(23,79,15,1,'Modulo CRM',1.000,1000.00,1000.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,220.00,NULL),(24,79,16,1,'Setup',1.000,200.00,200.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,44.00,NULL),(25,80,17,1,'Servizio Altro Tenant',1.000,500.00,500.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,110.00,NULL),(26,81,18,1,'Modulo CRM',1.000,1000.00,1000.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,220.00,NULL),(27,81,19,1,'Setup',1.000,200.00,200.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,44.00,NULL),(28,82,20,1,'Servizio Altro Tenant',1.000,500.00,500.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,110.00,NULL),(29,83,21,1,'Modulo CRM',1.000,1000.00,1000.00,'2026-04-29 04:12:05','2026-04-29 04:12:05',0.00,22.00,220.00,NULL),(30,83,22,1,'Setup',1.000,200.00,200.00,'2026-04-29 04:12:05','2026-04-29 04:12:05',0.00,22.00,44.00,NULL),(31,84,23,1,'Servizio Altro Tenant',1.000,500.00,500.00,'2026-04-29 04:12:05','2026-04-29 04:12:05',0.00,22.00,110.00,NULL),(32,70,24,1,'Modulo CRM',1.000,1000.00,900.00,'2026-04-29 20:08:14','2026-04-29 20:08:14',10.00,22.00,198.00,NULL),(33,70,24,2,'Setup',1.000,200.00,200.00,'2026-04-29 20:08:14','2026-04-29 20:08:14',0.00,22.00,44.00,NULL),(34,73,25,1,'Modulo CRM',1.000,1000.00,900.00,'2026-04-29 20:26:18','2026-04-29 20:26:18',10.00,22.00,198.00,NULL),(35,73,25,2,'Setup',1.000,200.00,200.00,'2026-04-29 20:26:18','2026-04-29 20:26:18',0.00,22.00,44.00,NULL),(38,84,27,1,'RIGA 1',1.000,100.00,90.00,'2026-05-01 15:56:24','2026-05-01 15:56:24',10.00,22.00,19.80,NULL),(39,84,27,2,'RIGA 2',1.000,500.00,500.00,'2026-05-01 15:56:24','2026-05-01 15:56:24',0.00,22.00,110.00,NULL),(40,84,27,3,'RIGA 3',1.000,2000.00,1600.00,'2026-05-01 15:56:24','2026-05-01 15:56:24',20.00,22.00,352.00,NULL),(41,75,28,1,'RIGA 1',1.000,100.00,90.00,'2026-05-01 18:05:04','2026-05-01 18:05:04',10.00,22.00,19.80,NULL),(44,75,29,1,'RIGA 1',1.000,100.00,109.80,'2026-05-02 04:47:40','2026-05-02 04:47:40',10.00,22.00,19.80,NULL),(45,75,29,2,'RIGA 2',1.000,50.00,61.00,'2026-05-02 04:47:40','2026-05-02 04:47:40',0.00,22.00,11.00,NULL),(46,85,30,1,'RIGA 1',1.000,580.00,580.00,'2026-05-06 21:25:11','2026-05-06 21:25:11',0.00,22.00,127.60,NULL),(47,85,31,1,'RIGA 1',1.000,580.00,464.00,'2026-05-07 00:10:48','2026-05-07 00:10:48',20.00,22.00,102.08,2),(48,86,32,1,'RIGA 1',10.000,580.00,5220.00,'2026-05-07 01:29:52','2026-05-07 01:29:52',10.00,22.00,1148.40,2),(49,86,33,1,'RIGA 1',1.000,1200.00,1080.00,'2026-05-07 01:54:10','2026-05-07 01:54:10',10.00,22.00,237.60,1),(50,87,34,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',20.000,580.00,11600.00,'2026-05-08 02:52:17','2026-05-08 02:52:17',0.00,22.00,2552.00,2),(51,87,35,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',5.000,580.00,2900.00,'2026-05-08 16:18:01','2026-05-08 16:18:01',0.00,22.00,638.00,2),(52,87,36,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,100.00,'2026-05-08 19:02:01','2026-05-08 19:02:01',0.00,22.00,22.00,2),(53,87,37,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,580.00,'2026-05-09 07:34:07','2026-05-09 07:34:07',0.00,22.00,127.60,2),(54,87,38,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,580.00,'2026-05-09 12:22:43','2026-05-09 12:22:43',0.00,22.00,127.60,2),(55,88,39,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,580.00,'2026-05-09 13:29:56','2026-05-09 13:29:56',0.00,22.00,127.60,2),(56,88,40,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,580.00,'2026-05-09 14:14:39','2026-05-09 14:14:39',0.00,22.00,127.60,2);
/*!40000 ALTER TABLE `sales_order_line` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stock_movement`
--

DROP TABLE IF EXISTS `stock_movement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_movement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `item_id` bigint NOT NULL,
  `movement_date` date NOT NULL,
  `direction` varchar(10) NOT NULL,
  `reason_code` varchar(40) NOT NULL,
  `quantity` decimal(15,3) NOT NULL,
  `notes` varchar(500) DEFAULT NULL,
  `reference_type` varchar(40) DEFAULT NULL,
  `reference_id` bigint DEFAULT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_stock_movement_item` (`tenant_id`,`item_id`),
  KEY `idx_stock_movement_reference` (`tenant_id`,`reference_type`,`reference_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock_movement`
--

LOCK TABLES `stock_movement` WRITE;
/*!40000 ALTER TABLE `stock_movement` DISABLE KEYS */;
INSERT INTO `stock_movement` VALUES (1,85,2,'2026-05-06','IN','MANUAL_LOAD',10.000,'CARICO MANUALE','MANUAL',NULL,'2026-05-06 18:28:01','2026-05-06 18:28:01'),(2,85,2,'2026-05-07','OUT','DDT_ISSUE',1.000,'Scarico automatico da DDT DDT-2026-00002','DDT',5,'2026-05-06 22:25:24','2026-05-06 22:25:24'),(3,85,2,'2026-05-07','IN','DDT_CANCEL',1.000,'Ripristino automatico da annullamento DDT null','DDT_CANCEL',5,'2026-05-06 22:31:31','2026-05-06 22:31:31'),(4,86,2,'2026-05-07','IN','MANUAL_LOAD',20.000,'CARICO MANUALE','MANUAL',NULL,'2026-05-06 23:24:45','2026-05-06 23:24:45'),(5,86,2,'2026-05-07','OUT','DDT_ISSUE',10.000,'Scarico automatico da DDT DDT-2026-00001','DDT',6,'2026-05-06 23:32:42','2026-05-06 23:32:42'),(6,86,2,'2026-05-08','IN','MANUAL_LOAD',10.000,'CARICO','MANUAL',NULL,'2026-05-06 23:48:17','2026-05-06 23:48:17'),(7,85,2,'2026-05-07','IN','PURCHASE_RECEIPT',10.000,'Carico automatico da ricezione merci GR-00001','GOODS_RECEIPT',1,'2026-05-07 19:42:02','2026-05-07 19:42:02'),(8,87,2,'2026-05-08','IN','MANUAL_LOAD',100.000,'CARCI MANUALE DI MAGAZZINO DI 100 PEZZI','MANUAL',NULL,'2026-05-08 00:49:55','2026-05-08 00:49:55'),(9,87,2,'2026-05-08','OUT','DDT_ISSUE',20.000,'Scarico automatico da DDT DDT-2026-00001','DDT',8,'2026-05-08 00:55:41','2026-05-08 00:55:41'),(10,87,2,'2026-05-08','IN','DDT_CANCEL',20.000,'Ripristino automatico da annullamento DDT DDT-2026-00001','DDT_CANCEL',8,'2026-05-08 00:56:24','2026-05-08 00:56:24'),(11,87,2,'2026-05-08','IN','PURCHASE_RECEIPT',50.000,'Carico automatico da ricezione merci GR-00002','GOODS_RECEIPT',2,'2026-05-08 01:03:27','2026-05-08 01:03:27'),(12,87,2,'2026-05-08','IN','PURCHASE_RECEIPT',10.000,'Carico automatico da ricezione merci GR-00003','GOODS_RECEIPT',3,'2026-05-08 03:06:28','2026-05-08 03:06:28'),(13,87,2,'2026-05-08','OUT','DDT_ISSUE',5.000,'Scarico automatico da DDT DDT-2026-00002','DDT',9,'2026-05-08 14:18:24','2026-05-08 14:18:24'),(14,87,2,'2026-05-08','IN','PURCHASE_RECEIPT',9.998,'Carico automatico da ricezione merci GR-00004','GOODS_RECEIPT',4,'2026-05-08 14:21:16','2026-05-08 14:21:16'),(15,87,2,'2026-05-08','OUT','DDT_ISSUE',1.000,'Scarico automatico da DDT DDT-2026-00003','DDT',10,'2026-05-08 17:02:12','2026-05-08 17:02:12'),(16,87,2,'2026-05-09','OUT','DDT_ISSUE',1.000,'Scarico automatico da DDT DDT-2026-00004','DDT',11,'2026-05-09 05:34:14','2026-05-09 05:34:14'),(17,87,2,'2026-05-09','IN','PURCHASE_RECEIPT',1.000,'Carico automatico da ricezione merci GR-00005','GOODS_RECEIPT',5,'2026-05-09 05:35:08','2026-05-09 05:35:08'),(18,87,2,'2026-05-09','OUT','DDT_ISSUE',1.000,'Scarico automatico da DDT DDT-2026-00005','DDT',12,'2026-05-09 10:22:53','2026-05-09 10:22:53'),(19,87,2,'2026-05-09','IN','DDT_CANCEL',1.000,'Ripristino automatico da annullamento DDT DDT-2026-00005','DDT_CANCEL',12,'2026-05-09 11:00:29','2026-05-09 11:00:29'),(20,88,2,'2026-05-09','IN','MANUAL_LOAD',100.000,'','MANUAL',NULL,'2026-05-09 11:33:43','2026-05-09 11:33:43'),(21,88,2,'2026-05-09','OUT','DDT_ISSUE',1.000,'Scarico automatico da DDT DDT-2026-00001','DDT',15,'2026-05-09 11:34:00','2026-05-09 11:34:00'),(22,88,2,'2026-05-09','OUT','DDT_ISSUE',1.000,'Scarico automatico da DDT DDT-2026-00002','DDT',16,'2026-05-09 14:01:05','2026-05-09 14:01:05'),(23,88,2,'2026-05-09','IN','PURCHASE_RECEIPT',2.000,'Carico automatico da ricezione merci GR-00006','GOODS_RECEIPT',6,'2026-05-09 20:14:52','2026-05-09 20:14:52');
/*!40000 ALTER TABLE `stock_movement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `supplier`
--

DROP TABLE IF EXISTS `supplier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplier`
--

LOCK TABLES `supplier` WRITE;
/*!40000 ALTER TABLE `supplier` DISABLE KEYS */;
INSERT INTO `supplier` VALUES (1,88,'SUP-001','Fornitore Demo S.R.L','IT0984327898','LBSMRC69A04F457F','fornitore@example.com','+393476681161','VIA GIAN DOMENICO ROMAGNOSI 11','TERNI','05100','TR','IT','PROVA FORNITORE DEMO',1,'2026-05-07 09:57:56','2026-05-08 01:00:29');
/*!40000 ALTER TABLE `supplier` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `supplier_invoice`
--

DROP TABLE IF EXISTS `supplier_invoice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier_invoice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
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
  UNIQUE KEY `uk_supplier_invoice_tenant_number` (`tenant_id`,`invoice_number`),
  KEY `idx_supplier_invoice_supplier` (`tenant_id`,`supplier_id`),
  KEY `idx_supplier_invoice_receipt` (`tenant_id`,`goods_receipt_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplier_invoice`
--

LOCK TABLES `supplier_invoice` WRITE;
/*!40000 ALTER TABLE `supplier_invoice` DISABLE KEYS */;
INSERT INTO `supplier_invoice` VALUES (1,87,'SI-00001','2026-05-08',1,2,2,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00002',23200.00,5104.00,28304.00,'2026-05-08 01:17:28','2026-05-08 01:17:53'),(2,87,'SI-00002','2026-05-08',1,3,3,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00003',4640.00,1020.80,5660.80,'2026-05-08 03:06:42','2026-05-08 03:06:42'),(3,87,'SI-00003','2026-05-08',1,4,4,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00004',5798.84,1275.74,7074.58,'2026-05-08 14:21:23','2026-05-08 14:21:23'),(4,87,'SI-00004','2026-05-09',1,5,5,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00005',580.00,127.60,707.60,'2026-05-09 05:35:11','2026-05-09 05:35:11'),(5,88,'SI-00005','2026-05-09',1,6,6,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00006',1160.00,255.20,1415.20,'2026-05-09 20:15:03','2026-05-09 20:15:04');
/*!40000 ALTER TABLE `supplier_invoice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `supplier_invoice_line`
--

DROP TABLE IF EXISTS `supplier_invoice_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplier_invoice_line`
--

LOCK TABLES `supplier_invoice_line` WRITE;
/*!40000 ALTER TABLE `supplier_invoice_line` DISABLE KEYS */;
INSERT INTO `supplier_invoice_line` VALUES (1,87,1,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',50.000,580.00,20.00,22.00,23200.00,5104.00,28304.00,'2026-05-08 01:17:39','2026-05-08 01:17:39'),(2,87,2,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,580.00,20.00,22.00,4640.00,1020.80,5660.80,'2026-05-08 03:06:42','2026-05-08 03:06:42'),(3,87,3,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',9.998,580.00,0.00,22.00,5798.84,1275.74,7074.58,'2026-05-08 14:21:23','2026-05-08 14:21:23'),(4,87,4,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,127.60,707.60,'2026-05-09 05:35:11','2026-05-09 05:35:11'),(5,88,5,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',2.000,580.00,0.00,22.00,1160.00,255.20,1415.20,'2026-05-09 20:15:03','2026-05-09 20:15:03');
/*!40000 ALTER TABLE `supplier_invoice_line` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tenant`
--

DROP TABLE IF EXISTS `tenant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `uc_tenant_email` (`email`),
  UNIQUE KEY `uc_tenant_slug` (`slug`)
) ENGINE=InnoDB AUTO_INCREMENT=89 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tenant`
--

LOCK TABLES `tenant` WRITE;
/*!40000 ALTER TABLE `tenant` DISABLE KEYS */;
INSERT INTO `tenant` VALUES (70,'Tenant Test','tenant-test-57014885898041','tenant57014885906083@example.com','ACTIVE','it','EUR','2026-04-27 01:29:26','2026-04-27 01:29:26'),(71,'Tenant Test','tenant-test-57014887438916','tenant57014887442708@example.com','ACTIVE','it','EUR','2026-04-27 01:29:26','2026-04-27 01:29:26'),(72,'Tenant Test','tenant-test-6346922231416','tenant6346922301541@example.com','ACTIVE','it','EUR','2026-04-27 19:58:16','2026-04-27 19:58:16'),(73,'Tenant Test','tenant-test-23734427062791','tenant23734427125416@example.com','ACTIVE','it','EUR','2026-04-28 18:46:25','2026-04-28 18:46:25'),(74,'Tenant Test','tenant-test-28677502495958','tenant28677502542916@example.com','ACTIVE','it','EUR','2026-04-28 20:08:48','2026-04-28 20:08:48'),(75,'Tenant Test','tenant-test-4009166646958','tenant4009166698708@example.com','ACTIVE','it','EUR','2026-04-29 04:12:04','2026-04-29 04:12:04'),(76,'Tenant Test','tenant-test-4009227100916','tenant4009227105833@example.com','ACTIVE','it','EUR','2026-04-29 04:12:04','2026-04-29 04:12:04'),(77,'Tenant Test','tenant-test-4009395190166','tenant4009395198375@example.com','ACTIVE','it','EUR','2026-04-29 04:12:04','2026-04-29 04:12:04'),(78,'Tenant Test','tenant-test-4009397500458','tenant4009397505041@example.com','ACTIVE','it','EUR','2026-04-29 04:12:04','2026-04-29 04:12:04'),(79,'Tenant Test','tenant-test-4009470057416','tenant4009470064750@example.com','ACTIVE','it','EUR','2026-04-29 04:12:04','2026-04-29 04:12:04'),(80,'Tenant Test','tenant-test-4009472465500','tenant4009472469666@example.com','ACTIVE','it','EUR','2026-04-29 04:12:04','2026-04-29 04:12:04'),(81,'Tenant Test','tenant-test-4009539012041','tenant4009539019958@example.com','ACTIVE','it','EUR','2026-04-29 04:12:04','2026-04-29 04:12:04'),(82,'Tenant Test','tenant-test-4009541103708','tenant4009541107791@example.com','ACTIVE','it','EUR','2026-04-29 04:12:04','2026-04-29 04:12:04'),(83,'Tenant Test','tenant-test-4009606512333','tenant4009606520250@example.com','ACTIVE','it','EUR','2026-04-29 04:12:04','2026-04-29 04:12:04'),(84,'Tenant Test','tenant-test-4009608435583','tenant4009608439958@example.com','ACTIVE','it','EUR','2026-04-29 04:12:04','2026-04-29 04:12:04'),(85,'Azienda Demo','azienda-demo','malbasini@gmail.com','ACTIVE','it','EUR','2026-05-05 05:42:49','2026-05-05 05:42:49'),(86,'Azienda Demo S.P.A.','azienda-demo-s-p-a','malbasini@outlook.it','ACTIVE','it','EUR','2026-05-07 01:20:11','2026-05-07 01:20:11'),(87,'Verdi Spa','verdi-spa','info@verdi.com','ACTIVE','it','EUR','2026-05-08 02:46:05','2026-05-08 02:46:05'),(88,'Bianchi SRL','bianchi-srl','info@bianchi.com','ACTIVE','it','EUR','2026-05-09 13:26:17','2026-05-09 13:26:17');
/*!40000 ALTER TABLE `tenant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tenant_module`
--

DROP TABLE IF EXISTS `tenant_module`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tenant_module`
--

LOCK TABLES `tenant_module` WRITE;
/*!40000 ALTER TABLE `tenant_module` DISABLE KEYS */;
/*!40000 ALTER TABLE `tenant_module` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqrde1ctbh1u3xtshpr1sxouuq` (`tenant_id`,`user_id`,`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES (1,'2026-05-05 05:42:48.723456','2026-05-05 05:42:48.723462',85,1,1),(2,'2026-05-07 01:20:10.841537','2026-05-07 01:20:10.841544',86,2,2),(3,'2026-05-08 02:46:04.646724','2026-05-08 02:46:04.646729',87,3,3),(4,'2026-05-09 13:26:16.986104','2026-05-09 13:26:16.986109',88,4,4);
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-10  1:07:12
