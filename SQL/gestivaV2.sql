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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,0,NULL,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(2,0,NULL,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(3,0,NULL,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(4,0,NULL,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(5,0,NULL,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(6,0,NULL,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(7,0,NULL,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(8,0,NULL,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(9,0,NULL,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(10,0,NULL,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(11,0,NULL,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(12,0,NULL,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(13,0,NULL,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(14,0,NULL,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(15,0,NULL,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(16,0,NULL,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(17,0,NULL,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(18,0,NULL,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(19,0,NULL,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(20,91,NULL,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:54:51','2026-05-14 17:54:51'),(21,91,NULL,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:03','2026-05-14 17:55:03'),(22,91,NULL,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(23,91,NULL,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(24,91,NULL,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(25,91,NULL,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(26,91,NULL,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(27,91,NULL,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(28,91,NULL,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(29,91,NULL,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(30,91,NULL,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(31,91,NULL,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(32,91,NULL,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(33,91,NULL,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(34,91,NULL,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(35,91,NULL,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(36,91,NULL,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(37,91,NULL,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(38,91,NULL,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(39,92,NULL,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(40,92,NULL,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(41,92,NULL,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(42,92,NULL,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(43,92,NULL,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(44,92,NULL,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(45,92,NULL,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(46,92,NULL,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(47,92,NULL,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(48,92,NULL,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(49,92,NULL,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(50,92,NULL,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(51,92,NULL,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(52,92,NULL,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(53,92,NULL,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(54,92,NULL,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(55,92,NULL,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(56,92,NULL,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(57,92,NULL,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(58,93,NULL,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(59,93,NULL,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(60,93,NULL,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(61,93,NULL,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(62,93,NULL,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(63,93,NULL,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(64,93,NULL,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(65,93,NULL,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(66,93,NULL,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(67,93,NULL,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(68,93,NULL,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(69,93,NULL,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(70,93,NULL,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(71,93,NULL,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(72,93,NULL,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(73,93,NULL,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(74,93,NULL,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(75,93,NULL,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(76,93,NULL,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(77,94,NULL,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(78,94,NULL,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(79,94,NULL,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(80,94,NULL,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(81,94,NULL,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(82,94,NULL,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(83,94,NULL,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(84,94,NULL,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(85,94,NULL,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(86,94,NULL,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(87,94,NULL,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(88,94,NULL,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(89,94,NULL,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(90,94,NULL,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(91,94,NULL,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(92,94,NULL,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(93,94,NULL,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(94,94,NULL,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(95,94,NULL,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(96,95,NULL,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(97,95,NULL,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(98,95,NULL,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(99,95,NULL,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(100,95,NULL,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(101,95,NULL,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(102,95,NULL,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(103,95,NULL,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(104,95,NULL,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(105,95,NULL,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(106,95,NULL,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(107,95,NULL,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(108,95,NULL,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(109,95,NULL,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(110,95,NULL,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(111,95,NULL,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(112,95,NULL,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(113,95,NULL,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(114,95,NULL,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(115,96,NULL,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(116,96,NULL,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(117,96,NULL,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(118,96,NULL,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(119,96,NULL,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(120,96,NULL,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(121,96,NULL,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(122,96,NULL,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(123,96,NULL,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(124,96,NULL,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(125,96,NULL,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(126,96,NULL,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(127,96,NULL,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(128,96,NULL,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(129,96,NULL,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(130,96,NULL,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(131,96,NULL,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(132,96,NULL,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(133,96,NULL,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(134,97,NULL,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(135,97,NULL,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(136,97,NULL,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(137,97,NULL,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(138,97,NULL,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(139,97,NULL,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(140,97,NULL,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(141,97,NULL,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(142,97,NULL,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(143,97,NULL,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(144,97,NULL,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(145,97,NULL,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(146,97,NULL,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(147,97,NULL,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(148,97,NULL,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(149,97,NULL,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(150,97,NULL,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(151,97,NULL,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(152,97,NULL,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(153,98,NULL,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(154,98,NULL,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(155,98,NULL,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(156,98,NULL,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(157,98,NULL,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(158,98,NULL,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(159,98,NULL,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(160,98,NULL,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(161,98,NULL,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(162,98,NULL,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(163,98,NULL,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(164,98,NULL,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(165,98,NULL,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(166,98,NULL,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(167,98,NULL,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(168,98,NULL,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(169,98,NULL,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(170,98,NULL,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(171,98,NULL,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(172,99,NULL,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(173,99,NULL,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(174,99,NULL,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(175,99,NULL,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(176,99,NULL,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(177,99,NULL,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(178,99,NULL,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(179,99,NULL,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(180,99,NULL,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(181,99,NULL,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(182,99,NULL,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(183,99,NULL,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(184,99,NULL,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(185,99,NULL,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(186,99,NULL,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(187,99,NULL,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(188,99,NULL,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(189,99,NULL,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(190,99,NULL,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(191,2,1,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(192,2,1,'1100','Disponibilità liquide','ASSET','DEBIT',191,2,_binary '\0',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(193,2,1,'1110','Cassa','ASSET','DEBIT',192,3,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(194,2,1,'1120','Banca','ASSET','DEBIT',192,3,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(195,2,1,'1200','Crediti commerciali','ASSET','DEBIT',191,2,_binary '\0',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(196,2,1,'1210','Crediti verso clienti','ASSET','DEBIT',195,3,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(197,2,1,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(198,2,1,'2100','Debiti commerciali','LIABILITY','CREDIT',197,2,_binary '\0',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(199,2,1,'2110','Debiti verso fornitori','LIABILITY','CREDIT',198,3,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(200,2,1,'2200','Debiti tributari','LIABILITY','CREDIT',197,2,_binary '\0',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(201,2,1,'2210','IVA a debito','LIABILITY','CREDIT',200,3,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(202,2,1,'2220','IVA a credito','ASSET','DEBIT',191,2,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(203,2,1,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(204,2,1,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(205,2,1,'4100','Ricavi da vendite','REVENUE','CREDIT',204,2,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(206,2,1,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(207,2,1,'5100','Acquisti merci','COST','DEBIT',206,2,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(208,2,1,'5200','Costi per servizi','COST','DEBIT',206,2,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(209,2,1,'5300','Spese bancarie','COST','DEBIT',206,2,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38');
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `account_backup_pre_multicompany`
--

DROP TABLE IF EXISTS `account_backup_pre_multicompany`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account_backup_pre_multicompany` (
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
) ENGINE=InnoDB AUTO_INCREMENT=210 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_backup_pre_multicompany`
--

LOCK TABLES `account_backup_pre_multicompany` WRITE;
/*!40000 ALTER TABLE `account_backup_pre_multicompany` DISABLE KEYS */;
INSERT INTO `account_backup_pre_multicompany` VALUES (1,0,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(2,0,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(3,0,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(4,0,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(5,0,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(6,0,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(7,0,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(8,0,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(9,0,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(10,0,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(11,0,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(12,0,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(13,0,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(14,0,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(15,0,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(16,0,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(17,0,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(18,0,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(19,0,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(20,91,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:54:51','2026-05-14 17:54:51'),(21,91,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:03','2026-05-14 17:55:03'),(22,91,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(23,91,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(24,91,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(25,91,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(26,91,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(27,91,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(28,91,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(29,91,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(30,91,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(31,91,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(32,91,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(33,91,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(34,91,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(35,91,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(36,91,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(37,91,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(38,91,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(39,92,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(40,92,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(41,92,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(42,92,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(43,92,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(44,92,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(45,92,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(46,92,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(47,92,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(48,92,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(49,92,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(50,92,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(51,92,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(52,92,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(53,92,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(54,92,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(55,92,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(56,92,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(57,92,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(58,93,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(59,93,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(60,93,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(61,93,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(62,93,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(63,93,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(64,93,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(65,93,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(66,93,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(67,93,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(68,93,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(69,93,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(70,93,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(71,93,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(72,93,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(73,93,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(74,93,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(75,93,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(76,93,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(77,94,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(78,94,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(79,94,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(80,94,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(81,94,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(82,94,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(83,94,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(84,94,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(85,94,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(86,94,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(87,94,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(88,94,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(89,94,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(90,94,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(91,94,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(92,94,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(93,94,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(94,94,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(95,94,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(96,95,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(97,95,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(98,95,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(99,95,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(100,95,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(101,95,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(102,95,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(103,95,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(104,95,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(105,95,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(106,95,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(107,95,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(108,95,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(109,95,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(110,95,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(111,95,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(112,95,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(113,95,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(114,95,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(115,96,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(116,96,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(117,96,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(118,96,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(119,96,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(120,96,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(121,96,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(122,96,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(123,96,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(124,96,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(125,96,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(126,96,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(127,96,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(128,96,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(129,96,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(130,96,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(131,96,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(132,96,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(133,96,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(134,97,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(135,97,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(136,97,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(137,97,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(138,97,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(139,97,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(140,97,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(141,97,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(142,97,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(143,97,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(144,97,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(145,97,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(146,97,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(147,97,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(148,97,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(149,97,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(150,97,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(151,97,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(152,97,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(153,98,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(154,98,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(155,98,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(156,98,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(157,98,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(158,98,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(159,98,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(160,98,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(161,98,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(162,98,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(163,98,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(164,98,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(165,98,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(166,98,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(167,98,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(168,98,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(169,98,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(170,98,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(171,98,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(172,99,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(173,99,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(174,99,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(175,99,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(176,99,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(177,99,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(178,99,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(179,99,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(180,99,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(181,99,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(182,99,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(183,99,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(184,99,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(185,99,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(186,99,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(187,99,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(188,99,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(189,99,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(190,99,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(191,2,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(192,2,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(193,2,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(194,2,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(195,2,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(196,2,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(197,2,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(198,2,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(199,2,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(200,2,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(201,2,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(202,2,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(203,2,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(204,2,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(205,2,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(206,2,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(207,2,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(208,2,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38'),(209,2,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-10-07 23:14:38','2026-10-07 23:14:38');
/*!40000 ALTER TABLE `account_backup_pre_multicompany` ENABLE KEYS */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `accounting_entry`
--

LOCK TABLES `accounting_entry` WRITE;
/*!40000 ALTER TABLE `accounting_entry` DISABLE KEYS */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `accounting_entry_line`
--

LOCK TABLES `accounting_entry_line` WRITE;
/*!40000 ALTER TABLE `accounting_entry_line` DISABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_user`
--

LOCK TABLES `app_user` WRITE;
/*!40000 ALTER TABLE `app_user` DISABLE KEYS */;
INSERT INTO `app_user` VALUES (1,2,'Marco','Albasini','malbasini@gmail.com','$2a$10$mmbEQyI0be4kSy23m5uqzu1T/NA9iOHSVJcIbJA7eG6EAIORlfiDy','ACTIVE',_binary '','it','2026-10-08 01:14:38','2026-10-08 01:14:38');
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
-- Table structure for table `billing_order`
--

DROP TABLE IF EXISTS `billing_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `billing_order`
--

LOCK TABLES `billing_order` WRITE;
/*!40000 ALTER TABLE `billing_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `billing_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `company`
--

DROP TABLE IF EXISTS `company`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `company`
--

LOCK TABLES `company` WRITE;
/*!40000 ALTER TABLE `company` DISABLE KEYS */;
INSERT INTO `company` VALUES (1,2,'DEMO','Azienda Demo','Azienda Demo',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'IT','EUR','it',1,'2026-10-12 15:01:57','2026-10-12 15:01:57'),(2,2,'TEST2','Azienda Test 2',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'IT','EUR','it',1,'2026-10-13 20:27:44','2026-10-13 20:27:44');
/*!40000 ALTER TABLE `company` ENABLE KEYS */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `company_profile`
--

LOCK TABLES `company_profile` WRITE;
/*!40000 ALTER TABLE `company_profile` DISABLE KEYS */;
/*!40000 ALTER TABLE `company_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `company_settings`
--

DROP TABLE IF EXISTS `company_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `company_settings`
--

LOCK TABLES `company_settings` WRITE;
/*!40000 ALTER TABLE `company_settings` DISABLE KEYS */;
INSERT INTO `company_settings` VALUES (1,2,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-10-12 13:28:36','2026-10-12 15:28:36.000000',1),(2,2,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,22.00,30,30,'2026-10-14 17:31:33','2026-10-14 19:31:32.561316',2);
/*!40000 ALTER TABLE `company_settings` ENABLE KEYS */;
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
  `company_id` bigint NOT NULL,
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
  KEY `idx_customer_company_name` (`tenant_id`,`company_id`,`name`),
  KEY `idx_customer_company_id` (`tenant_id`,`company_id`,`id`),
  CONSTRAINT `fk_customer_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`),
  CONSTRAINT `fk_customer_tenant_company` FOREIGN KEY (`tenant_id`, `company_id`) REFERENCES `company` (`tenant_id`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `delivery_note`
--

LOCK TABLES `delivery_note` WRITE;
/*!40000 ALTER TABLE `delivery_note` DISABLE KEYS */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `delivery_note_line`
--

LOCK TABLES `delivery_note_line` WRITE;
/*!40000 ALTER TABLE `delivery_note_line` DISABLE KEYS */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `document_sequence`
--

LOCK TABLES `document_sequence` WRITE;
/*!40000 ALTER TABLE `document_sequence` DISABLE KEYS */;
INSERT INTO `document_sequence` VALUES (1,2,1,'QUOTE','QUO-',1,5,'2026-10-14 17:24:39','2026-10-14 17:24:39'),(2,2,1,'SALES_ORDER','SO-',1,5,'2026-10-14 17:24:40','2026-10-14 17:24:40'),(3,2,1,'DELIVERY_NOTE','DDT-',1,5,'2026-10-14 17:24:40','2026-10-14 17:24:40'),(4,2,1,'SALES_INVOICE','INV-',1,5,'2026-10-14 17:24:40','2026-10-14 17:24:40'),(5,2,1,'PURCHASE_ORDER','PO-',1,5,'2026-10-14 17:24:40','2026-10-14 17:24:40'),(6,2,1,'GOODS_RECEIPT','GR-',1,5,'2026-10-14 17:24:40','2026-10-14 17:24:40'),(7,2,1,'SUPPLIER_INVOICE','SI-',1,5,'2026-10-14 17:24:40','2026-10-14 17:24:40'),(8,2,1,'JOURNAL_ENTRY','JE-',1,5,'2026-10-14 17:24:40','2026-10-14 17:24:40'),(9,2,2,'QUOTE','QUO-',1,5,'2026-10-14 17:31:33','2026-10-14 17:31:33'),(10,2,2,'SALES_ORDER','SO-',1,5,'2026-10-14 17:31:33','2026-10-14 17:31:33'),(11,2,2,'DELIVERY_NOTE','DDT-',1,5,'2026-10-14 17:31:33','2026-10-14 17:31:33'),(12,2,2,'SALES_INVOICE','TEST-INV-',1,5,'2026-10-14 17:34:31','2026-10-14 17:31:33'),(13,2,2,'PURCHASE_ORDER','PO-',1,5,'2026-10-14 17:31:33','2026-10-14 17:31:33'),(14,2,2,'GOODS_RECEIPT','GR-',1,5,'2026-10-14 17:31:33','2026-10-14 17:31:33'),(15,2,2,'SUPPLIER_INVOICE','SI-',1,5,'2026-10-14 17:31:33','2026-10-14 17:31:33'),(16,2,2,'JOURNAL_ENTRY','JE-',1,5,'2026-10-14 17:31:33','2026-10-14 17:31:33');
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
INSERT INTO `flyway_schema_history` VALUES (1,'1','init','SQL','V1__init.sql',1547801324,'root','2026-06-07 18:03:25',315,0),(2,'1','<< Flyway Baseline >>','BASELINE','<< Flyway Baseline >>',NULL,'root','2026-04-26 01:02:54',0,1);
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `goods_receipt`
--

LOCK TABLES `goods_receipt` WRITE;
/*!40000 ALTER TABLE `goods_receipt` DISABLE KEYS */;
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
  `unit_cost` decimal(15,4) DEFAULT NULL,
  `total_cost` decimal(15,4) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_goods_receipt_line_receipt` (`tenant_id`,`goods_receipt_id`),
  KEY `idx_goods_receipt_line_item` (`tenant_id`,`item_id`),
  KEY `FKff5scxf5vgristhdleqkhh0p` (`goods_receipt_id`),
  CONSTRAINT `FKff5scxf5vgristhdleqkhh0p` FOREIGN KEY (`goods_receipt_id`) REFERENCES `goods_receipt` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `goods_receipt_line`
--

LOCK TABLES `goods_receipt_line` WRITE;
/*!40000 ALTER TABLE `goods_receipt_line` DISABLE KEYS */;
/*!40000 ALTER TABLE `goods_receipt_line` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory_average_balance`
--

DROP TABLE IF EXISTS `inventory_average_balance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory_average_balance`
--

LOCK TABLES `inventory_average_balance` WRITE;
/*!40000 ALTER TABLE `inventory_average_balance` DISABLE KEYS */;
/*!40000 ALTER TABLE `inventory_average_balance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory_layer`
--

DROP TABLE IF EXISTS `inventory_layer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory_layer`
--

LOCK TABLES `inventory_layer` WRITE;
/*!40000 ALTER TABLE `inventory_layer` DISABLE KEYS */;
/*!40000 ALTER TABLE `inventory_layer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory_layer_consumption`
--

DROP TABLE IF EXISTS `inventory_layer_consumption`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory_layer_consumption`
--

LOCK TABLES `inventory_layer_consumption` WRITE;
/*!40000 ALTER TABLE `inventory_layer_consumption` DISABLE KEYS */;
/*!40000 ALTER TABLE `inventory_layer_consumption` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory_lot`
--

DROP TABLE IF EXISTS `inventory_lot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory_lot`
--

LOCK TABLES `inventory_lot` WRITE;
/*!40000 ALTER TABLE `inventory_lot` DISABLE KEYS */;
/*!40000 ALTER TABLE `inventory_lot` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory_movement`
--

DROP TABLE IF EXISTS `inventory_movement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory_movement`
--

LOCK TABLES `inventory_movement` WRITE;
/*!40000 ALTER TABLE `inventory_movement` DISABLE KEYS */;
/*!40000 ALTER TABLE `inventory_movement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory_serial`
--

DROP TABLE IF EXISTS `inventory_serial`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory_serial`
--

LOCK TABLES `inventory_serial` WRITE;
/*!40000 ALTER TABLE `inventory_serial` DISABLE KEYS */;
/*!40000 ALTER TABLE `inventory_serial` ENABLE KEYS */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invoice`
--

LOCK TABLES `invoice` WRITE;
/*!40000 ALTER TABLE `invoice` DISABLE KEYS */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invoice_line`
--

LOCK TABLES `invoice_line` WRITE;
/*!40000 ALTER TABLE `invoice_line` DISABLE KEYS */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `item`
--

LOCK TABLES `item` WRITE;
/*!40000 ALTER TABLE `item` DISABLE KEYS */;
/*!40000 ALTER TABLE `item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `item_uom`
--

DROP TABLE IF EXISTS `item_uom`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `item_uom`
--

LOCK TABLES `item_uom` WRITE;
/*!40000 ALTER TABLE `item_uom` DISABLE KEYS */;
/*!40000 ALTER TABLE `item_uom` ENABLE KEYS */;
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_journal_entry_tenant_company_number` (`tenant_id`,`company_id`,`entry_number`),
  KEY `idx_journal_entry_tenant_date` (`tenant_id`,`entry_date`),
  KEY `idx_journal_entry_tenant_causal` (`tenant_id`,`causal_code`),
  KEY `idx_journal_entry_tenant_ref` (`tenant_id`,`reference_type`,`reference_id`),
  KEY `idx_journal_entry_company_date` (`tenant_id`,`company_id`,`entry_date`),
  KEY `idx_journal_entry_company_causal` (`tenant_id`,`company_id`,`causal_code`),
  KEY `idx_journal_entry_company_ref` (`tenant_id`,`company_id`,`reference_type`,`reference_id`)
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
  KEY `idx_payment_due_tenant_due_date` (`tenant_id`,`due_date`),
  KEY `idx_payment_due_tenant_direction_status` (`tenant_id`,`direction`,`status`),
  KEY `idx_payment_due_tenant_ref` (`tenant_id`,`reference_type`,`reference_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_due`
--

LOCK TABLES `payment_due` WRITE;
/*!40000 ALTER TABLE `payment_due` DISABLE KEYS */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_due_transaction`
--

LOCK TABLES `payment_due_transaction` WRITE;
/*!40000 ALTER TABLE `payment_due_transaction` DISABLE KEYS */;
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
-- Table structure for table `payment_transaction`
--

DROP TABLE IF EXISTS `payment_transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_transaction`
--

LOCK TABLES `payment_transaction` WRITE;
/*!40000 ALTER TABLE `payment_transaction` DISABLE KEYS */;
/*!40000 ALTER TABLE `payment_transaction` ENABLE KEYS */;
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
INSERT INTO `persistent_logins` VALUES ('azienda-demo|malbasini@gmail.com','cCHkVyMf1uu07Ghk49aBAg==','oUs1zZ+x8NXCIGt37+gsvQ==','2026-10-18 14:58:34'),('azienda-demo|malbasini@gmail.com','Jmn/ST1/OIZnXfumMM9Cew==','NVHRXEJxAZgr1adQHn3hkQ==','2026-10-14 18:03:39');
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchase_order`
--

LOCK TABLES `purchase_order` WRITE;
/*!40000 ALTER TABLE `purchase_order` DISABLE KEYS */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchase_order_line`
--

LOCK TABLES `purchase_order_line` WRITE;
/*!40000 ALTER TABLE `purchase_order_line` DISABLE KEYS */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quote`
--

LOCK TABLES `quote` WRITE;
/*!40000 ALTER TABLE `quote` DISABLE KEYS */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quote_line`
--

LOCK TABLES `quote_line` WRITE;
/*!40000 ALTER TABLE `quote_line` DISABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (15,'2026-06-05 21:48:00.664185','2026-06-05 21:48:00.664188',0,'ROLE_WAREHOUSE','ROLE_WAREHOUSE',_binary ''),(16,'2026-06-05 21:48:00.664185','2026-06-05 21:48:00.664185',0,'ROLE_SALES','ROLE_SALES',_binary ''),(17,'2026-06-05 21:48:00.664185','2026-06-05 21:48:00.664185',0,'ROLE_PURCHASING','ROLE_PURCHASING',_binary ''),(18,'2026-06-05 21:48:00.664185','2026-06-05 21:48:00.664185',0,'ROLE_ACCOUNTING','ROLE_ACCOUNTING',_binary ''),(20,'2026-10-08 01:14:37.572569','2026-10-08 01:14:37.572573',2,'ROLE_ADMIN','Amministratore',_binary '');
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sales_order`
--

LOCK TABLES `sales_order` WRITE;
/*!40000 ALTER TABLE `sales_order` DISABLE KEYS */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sales_order_line`
--

LOCK TABLES `sales_order_line` WRITE;
/*!40000 ALTER TABLE `sales_order_line` DISABLE KEYS */;
/*!40000 ALTER TABLE `sales_order_line` ENABLE KEYS */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplier`
--

LOCK TABLES `supplier` WRITE;
/*!40000 ALTER TABLE `supplier` DISABLE KEYS */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplier_invoice`
--

LOCK TABLES `supplier_invoice` WRITE;
/*!40000 ALTER TABLE `supplier_invoice` DISABLE KEYS */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplier_invoice_line`
--

LOCK TABLES `supplier_invoice_line` WRITE;
/*!40000 ALTER TABLE `supplier_invoice_line` DISABLE KEYS */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tenant`
--

LOCK TABLES `tenant` WRITE;
/*!40000 ALTER TABLE `tenant` DISABLE KEYS */;
INSERT INTO `tenant` VALUES (2,'Azienda Demo','azienda-demo','marco.albasini@pec.it','ACTIVE','it','EUR','2026-10-08 01:14:37','2026-10-08 01:14:37','FIFO',1,'ACTIVE','PROFESSIONAL');
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
-- Table structure for table `unit_of_measure`
--

DROP TABLE IF EXISTS `unit_of_measure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `unit_of_measure`
--

LOCK TABLES `unit_of_measure` WRITE;
/*!40000 ALTER TABLE `unit_of_measure` DISABLE KEYS */;
INSERT INTO `unit_of_measure` VALUES (1,2,'PZ','Pezzo','pz','UNIT',0,1,'2026-10-08 01:14:37','2026-10-08 01:14:37'),(2,2,'KG','Chilogrammo','kg','MASS',3,1,'2026-10-08 01:14:37','2026-10-08 01:14:37'),(3,2,'G','Grammo','g','MASS',3,1,'2026-10-08 01:14:37','2026-10-08 01:14:37'),(4,2,'L','Litro','l','VOLUME',3,1,'2026-10-08 01:14:37','2026-10-08 01:14:37'),(5,2,'ML','Millilitro','ml','VOLUME',3,1,'2026-10-08 01:14:37','2026-10-08 01:14:37'),(6,2,'M','Metro','m','LENGTH',3,1,'2026-10-08 01:14:37','2026-10-08 01:14:37'),(7,2,'M2','Metro quadrato','m²','AREA',3,1,'2026-10-08 01:14:37','2026-10-08 01:14:37'),(8,2,'H','Ora','h','TIME',2,1,'2026-10-08 01:14:37','2026-10-08 01:14:37');
/*!40000 ALTER TABLE `unit_of_measure` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_company_access`
--

DROP TABLE IF EXISTS `user_company_access`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_company_access`
--

LOCK TABLES `user_company_access` WRITE;
/*!40000 ALTER TABLE `user_company_access` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_company_access` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES (1,'2026-10-08 01:14:37.577688','2026-10-08 01:14:37.577694',2,20,1);
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `warehouse`
--

DROP TABLE IF EXISTS `warehouse`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `warehouse`
--

LOCK TABLES `warehouse` WRITE;
/*!40000 ALTER TABLE `warehouse` DISABLE KEYS */;
/*!40000 ALTER TABLE `warehouse` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `warehouse_location`
--

DROP TABLE IF EXISTS `warehouse_location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `warehouse_location`
--

LOCK TABLES `warehouse_location` WRITE;
/*!40000 ALTER TABLE `warehouse_location` DISABLE KEYS */;
/*!40000 ALTER TABLE `warehouse_location` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-22  2:26:12
