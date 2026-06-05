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
) ENGINE=InnoDB AUTO_INCREMENT=153 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,0,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(2,0,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(3,0,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(4,0,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(5,0,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(6,0,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(7,0,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(8,0,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(9,0,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(10,0,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(11,0,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(12,0,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(13,0,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(14,0,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(15,0,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(16,0,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(17,0,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(18,0,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(19,0,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(20,91,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:54:51','2026-05-14 17:54:51'),(21,91,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:03','2026-05-14 17:55:03'),(22,91,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(23,91,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(24,91,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(25,91,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(26,91,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(27,91,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(28,91,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(29,91,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(30,91,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(31,91,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(32,91,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(33,91,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(34,91,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(35,91,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(36,91,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(37,91,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(38,91,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(39,92,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(40,92,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(41,92,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(42,92,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(43,92,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(44,92,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(45,92,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(46,92,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(47,92,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(48,92,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(49,92,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(50,92,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(51,92,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(52,92,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(53,92,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(54,92,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(55,92,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(56,92,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(57,92,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(58,93,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(59,93,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(60,93,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(61,93,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(62,93,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(63,93,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(64,93,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(65,93,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(66,93,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(67,93,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(68,93,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(69,93,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(70,93,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(71,93,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(72,93,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(73,93,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(74,93,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(75,93,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(76,93,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(77,94,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(78,94,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(79,94,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(80,94,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(81,94,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(82,94,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(83,94,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(84,94,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(85,94,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(86,94,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(87,94,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(88,94,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(89,94,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(90,94,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(91,94,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(92,94,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(93,94,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(94,94,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(95,94,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(96,95,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(97,95,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(98,95,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(99,95,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(100,95,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(101,95,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(102,95,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(103,95,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(104,95,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(105,95,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(106,95,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(107,95,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(108,95,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(109,95,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(110,95,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(111,95,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(112,95,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(113,95,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(114,95,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(115,96,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(116,96,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(117,96,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(118,96,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(119,96,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(120,96,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(121,96,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(122,96,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(123,96,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(124,96,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(125,96,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(126,96,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(127,96,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(128,96,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(129,96,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(130,96,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(131,96,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(132,96,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(133,96,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(134,97,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(135,97,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(136,97,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(137,97,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(138,97,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(139,97,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(140,97,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(141,97,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(142,97,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(143,97,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(144,97,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(145,97,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(146,97,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(147,97,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(148,97,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(149,97,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(150,97,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(151,97,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(152,97,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45');
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
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `accounting_entry`
--

LOCK TABLES `accounting_entry` WRITE;
/*!40000 ALTER TABLE `accounting_entry` DISABLE KEYS */;
INSERT INTO `accounting_entry` VALUES (1,87,'PN-00001','2026-05-09','CUSTOMER_RECEIPT','Incasso su scadenza INV-2026-00001','PAYMENT_DUE',1,'EUR',3478.00,'prova prima nota incasso','2026-05-09 05:45:45','2026-05-09 05:45:45'),(2,87,'PN-00002','2026-05-23','CUSTOMER_RECEIPT','Incasso su scadenza INV-2026-00001','PAYMENT_DUE',1,'EUR',60.00,'PROVA PRIMA NOTA INCASSO','2026-05-09 05:46:21','2026-05-09 05:46:21'),(3,87,'PN-00003','2026-05-09','SUPPLIER_PAYMENT','Pagamento su scadenza SI-00004','PAYMENT_DUE',5,'EUR',707.56,'PROVA PRIMA NOTA PAGAMENTO A FORNITORE','2026-05-09 05:47:42','2026-05-09 05:47:42'),(4,87,'PN-00004','2026-05-09','SUPPLIER_PAYMENT','Pagamento su scadenza SI-00004','PAYMENT_DUE',5,'EUR',0.04,'','2026-05-09 05:48:06','2026-05-09 05:48:06'),(5,88,'PN-00005','2026-05-09','MANUAL_INCOME','Rimborso assicurazione',NULL,NULL,'EUR',149.96,'PROVA ENTRATA MANUALE','2026-05-09 19:07:05','2026-05-09 19:07:05'),(6,88,'PN-00006','2026-05-09','MANUAL_EXPENSE','Spese bancarie',NULL,NULL,'EUR',12.50,'USCITA MANUALE','2026-05-09 19:10:06','2026-05-09 19:10:06'),(7,88,'PN-00007','2026-05-09','CUSTOMER_RECEIPT','Incasso su scadenza INV-2026-00001','PAYMENT_DUE',6,'EUR',700.00,'PROVA DASHBOARD','2026-05-09 19:38:13','2026-05-09 19:38:13'),(8,88,'PN-00008','2026-05-09','CUSTOMER_RECEIPT','Incasso su scadenza INV-2026-00001','PAYMENT_DUE',6,'EUR',7.60,'PROVA DASHBOARD','2026-05-09 19:39:22','2026-05-09 19:39:22'),(9,88,'PN-00009','2026-05-09','SUPPLIER_PAYMENT','Pagamento su scadenza SI-00005','PAYMENT_DUE',8,'EUR',1400.00,'PROVA DASHBOARD','2026-05-09 20:16:15','2026-05-09 20:16:15'),(10,88,'PN-00010','2026-05-09','CUSTOMER_RECEIPT','Incasso su scadenza INV-2026-00002','PAYMENT_DUE',7,'EUR',707.60,'PROVA DASHBOARD','2026-05-09 20:17:16','2026-05-09 20:17:16'),(11,88,'PN-00011','2026-05-10','CUSTOMER_RECEIPT','Incasso su scadenza INV-2026-00003','PAYMENT_DUE',9,'EUR',706.70,'PROVA INCASSO','2026-05-10 10:16:38','2026-05-10 10:16:38'),(12,88,'PN-00012','2026-05-10','CUSTOMER_RECEIPT','Incasso su scadenza INV-2026-00004','PAYMENT_DUE',10,'EUR',707.60,'PROVA JOURNAL','2026-05-10 10:38:04','2026-05-10 10:38:04'),(13,88,'PN-00013','2026-05-10','CUSTOMER_RECEIPT','Incasso su scadenza INV-2026-00005','PAYMENT_DUE',11,'EUR',707.60,'PROVA LIBRO JOURNAL','2026-05-10 11:12:38','2026-05-10 11:12:38'),(14,88,'PN-00014','2026-05-10','SUPPLIER_PAYMENT','Pagamento su scadenza SI-00006','PAYMENT_DUE',12,'EUR',707.60,'PROVA JOURNAL','2026-05-10 11:21:52','2026-05-10 11:21:52'),(15,91,'PN-00015','2026-05-15','MANUAL_EXPENSE','PROVA DUE',NULL,NULL,'EUR',100.00,'REGISTRAZIONE MANUALE','2026-05-15 04:35:52','2026-05-15 04:35:52'),(16,91,'PN-00016','2026-05-15','MANUAL_EXPENSE','PROVA',NULL,NULL,'EUR',120.00,'PROVA USCITA MANUALE','2026-05-15 04:42:12','2026-05-15 04:42:12'),(17,91,'PN-00017','2026-05-15','CUSTOMER_RECEIPT','Incasso su scadenza INV-2026-00001','PAYMENT_DUE',22,'EUR',707.60,'REGISTRO INCASSO','2026-05-15 05:05:46','2026-05-15 05:05:46');
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
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `accounting_entry_line`
--

LOCK TABLES `accounting_entry_line` WRITE;
/*!40000 ALTER TABLE `accounting_entry_line` DISABLE KEYS */;
INSERT INTO `accounting_entry_line` VALUES (1,87,1,1,'INCOME','Incasso su scadenza INV-2026-00001',3478.00,'2026-05-09 05:45:45','2026-05-09 05:45:45'),(2,87,2,1,'INCOME','Incasso su scadenza INV-2026-00001',60.00,'2026-05-09 05:46:21','2026-05-09 05:46:21'),(3,87,3,1,'EXPENSE','Pagamento su scadenza SI-00004',707.56,'2026-05-09 05:47:42','2026-05-09 05:47:42'),(4,87,4,1,'EXPENSE','Pagamento su scadenza SI-00004',0.04,'2026-05-09 05:48:06','2026-05-09 05:48:06'),(5,88,5,1,'INCOME','Rimborso assicurazione',149.96,'2026-05-09 19:07:05','2026-05-09 19:07:05'),(6,88,6,1,'EXPENSE','Spese bancarie',12.50,'2026-05-09 19:10:06','2026-05-09 19:10:06'),(7,88,7,1,'INCOME','Incasso su scadenza INV-2026-00001',700.00,'2026-05-09 19:38:13','2026-05-09 19:38:13'),(8,88,8,1,'INCOME','Incasso su scadenza INV-2026-00001',7.60,'2026-05-09 19:39:22','2026-05-09 19:39:22'),(9,88,9,1,'EXPENSE','Pagamento su scadenza SI-00005',1400.00,'2026-05-09 20:16:15','2026-05-09 20:16:15'),(10,88,10,1,'INCOME','Incasso su scadenza INV-2026-00002',707.60,'2026-05-09 20:17:16','2026-05-09 20:17:16'),(11,88,11,1,'INCOME','Incasso su scadenza INV-2026-00003',706.70,'2026-05-10 10:16:38','2026-05-10 10:16:38'),(12,88,12,1,'INCOME','Incasso su scadenza INV-2026-00004',707.60,'2026-05-10 10:38:04','2026-05-10 10:38:04'),(13,88,13,1,'INCOME','Incasso su scadenza INV-2026-00005',707.60,'2026-05-10 11:12:38','2026-05-10 11:12:38'),(14,88,14,1,'EXPENSE','Pagamento su scadenza SI-00006',707.60,'2026-05-10 11:21:52','2026-05-10 11:21:52'),(15,91,15,1,'EXPENSE','PROVA DUE',100.00,'2026-05-15 04:35:52','2026-05-15 04:35:52'),(16,91,16,1,'EXPENSE','PROVA',120.00,'2026-05-15 04:42:12','2026-05-15 04:42:12'),(17,91,17,1,'INCOME','Incasso su scadenza INV-2026-00001',707.60,'2026-05-15 05:05:46','2026-05-15 05:05:46');
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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_user`
--

LOCK TABLES `app_user` WRITE;
/*!40000 ALTER TABLE `app_user` DISABLE KEYS */;
INSERT INTO `app_user` VALUES (1,85,'Marco','Albasini','admin@example.com','$2a$10$oM12UOzwk.bsSbbmqEPNt.E0ES1NiKll1Iy4UCcytXE.I2CZyueAK','ACTIVE',_binary '','it','2026-05-05 05:42:49','2026-05-05 05:42:49'),(2,86,'Marco','Albasini','admin@example.com','$2a$10$8jjasueMe.Tz.3v.k9gajOSJRg1NYyyBkBTEldsvaxeDVwvY17frK','ACTIVE',_binary '','it','2026-05-07 01:20:11','2026-05-07 01:20:11'),(3,87,'Alessandra','Albasini','malbasini@outlook.it','$2a$10$ex96en3m4jmBXbyI0ZZcEO0zEJdHZIyLKhYrJHi72Af52Viur3wBS','ACTIVE',_binary '','it','2026-05-08 02:46:05','2026-05-08 02:46:05'),(4,88,'Mario','Rossi','admin@rossi.it','$2a$10$EBeSnF3NL9JnaTpwtP8K1ex6EbWNM3fRCkkdhTkkzD7E.cd4IdB1a','ACTIVE',_binary '','it','2026-05-09 13:26:17','2026-05-09 13:26:17'),(5,89,'Marco','Albasini','admin@rossi.com','$2a$10$xcjjM5.3Jvt09Qr7SVOp3.zIlMzyvNlIkBz.Uy5vNIAGUNSxLsQfa','ACTIVE',_binary '','it','2026-05-13 02:01:15','2026-05-13 02:01:15'),(6,90,'Marco','Albasini','admin@verdi.it','$2a$10$.D6/mebXsQAuS7W4EP5NvOzdEmc3eI9ORvgb8hC2Ur5qA1q00T4eq','ACTIVE',_binary '','it','2026-05-13 02:04:33','2026-05-13 02:04:33'),(7,91,'Marco','Albasini','admin@rossi.it','$2a$10$VlGQN9FuPfSjX5v6XTxRwugBApi4PILKodqx.ddnhQY6Ayrbs7cQG','ACTIVE',_binary '','it','2026-05-14 19:54:09','2026-05-14 19:54:09'),(8,92,'Marco','Albasini','malbasini@outlook.it','$2a$10$ZmS52cOIbYhbRZ.zcszEMO2m7tYJZ9LaoNCrc1JMTqQyCoMJd8zv6','ACTIVE',_binary '','it','2026-05-16 23:45:51','2026-05-16 23:45:51'),(9,93,'Marco','Albasini','example@rossi.it','$2a$10$Ckn5Uhst.i1f9/E8JcVU1.nn6mUwxZK9YZ7FjjY11SHhKxTEQI.eW','ACTIVE',_binary '','it','2026-05-20 11:49:39','2026-05-20 11:49:39'),(10,94,'Alessandra','Albasini','alessandra.albasini@hotmail.it','$2a$10$BmIl6hz0uKwk9Y2vZjCCZ.2zxffvJI30zXsdhFEWodJa6MaSxGc2e','ACTIVE',_binary '','it','2026-06-02 18:31:36','2026-06-02 18:31:36'),(11,95,'Marco','Albasini','admin@info.com','$2a$10$g0FO19NWmWWkRNxWac0sSu9Zd8tDKDWefxgg84owN8EhvLUWGyf/i','ACTIVE',_binary '','it','2026-06-03 13:17:26','2026-06-03 13:17:26'),(12,96,'Marco','Albasini','admin@info.gov','$2a$10$KsqTTThaFvzEUK.aps8CFuhGfA91FALDfKurVRMy5geMUjRter8V.','ACTIVE',_binary '','it','2026-06-03 21:37:41','2026-06-03 21:37:41'),(13,97,'Alessandra','Albasini','admin@neri.it','$2a$10$jjIkXXJvuz9opdYEEQgDE.cm152KHbA/LmrgteR7pvVmJlICl6k8m','ACTIVE',_binary '','it','2026-06-04 12:51:45','2026-06-04 12:51:45');
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
-- Table structure for table `company_settings`
--

DROP TABLE IF EXISTS `company_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `company_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `company_name` varchar(180) COLLATE utf8mb4_unicode_ci NOT NULL,
  `trade_name` varchar(180) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `vat_number` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tax_code` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(180) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `website` varchar(180) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address_line_1` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `postal_code` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `province` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `country_code` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `default_currency_code` varchar(3) COLLATE utf8mb4_unicode_ci NOT NULL,
  `default_vat_pct` decimal(5,2) DEFAULT NULL,
  `default_customer_due_days` int DEFAULT NULL,
  `default_supplier_due_days` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_settings_tenant` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `company_settings`
--

LOCK TABLES `company_settings` WRITE;
/*!40000 ALTER TABLE `company_settings` DISABLE KEYS */;
INSERT INTO `company_settings` VALUES (1,93,'Azienda',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'EUR',22.00,30,30,'2026-06-02 12:35:24','2026-06-02 14:35:23.588778'),(2,94,'Azienda Demo','Azienda Demo S.P.A.','IT0984327898','LBSMRC69A04F457F','marco.albasini@pec.it','3311675741','https://www.acme.it','Via della fontana 24, prima voc. fontana 7','05026','Montecastrilli','TR','Italia','EUR',22.00,30,30,'2026-06-02 16:40:00','2026-06-02 18:47:14.149526'),(3,95,'Azienda Demo','Azienda Demo S.P.A.','IT0984327898','LBSMRC69A04F457F','marco.albasini@pec.it','3311675741','https://www.acme.it','Via della fontana 24, prima voc. fontana 7','05026','Montecastrilli','TR','Italia','EUR',22.00,30,30,'2026-06-03 12:01:14','2026-06-03 14:01:34.188490'),(4,96,'Azienda Demo','Azienda Demo S.P.A.','IT0984327898','LBSMRC69A04F457F','marco.albasini@pec.it','3311675741','https://www.acme.it','Via della fontana 24, prima voc. fontana 7','05026','Montecastrilli','TR','Italia','EUR',22.00,30,30,'2026-06-03 19:41:20','2026-06-03 21:41:40.619255');
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
INSERT INTO `customer` VALUES (70,70,'Rossi Srl',NULL,NULL,'customer-57014888715666@example.com',NULL,'COMPANY','ACTIVE','2026-04-27 01:29:26','2026-04-27 01:29:26',NULL,NULL,NULL,NULL),(71,71,'Bianchi Srl',NULL,NULL,'customer-57014890156416@example.com',NULL,'COMPANY','ACTIVE','2026-04-27 01:29:26','2026-04-27 01:29:26',NULL,NULL,NULL,NULL),(72,72,'Rossi Srl',NULL,NULL,'customer-6346986438791@example.com',NULL,'COMPANY','ACTIVE','2026-04-27 19:58:16','2026-04-27 19:58:16',NULL,NULL,NULL,NULL),(73,73,'Rossi Srl',NULL,NULL,'customer-23734473434583@example.com',NULL,'COMPANY','ACTIVE','2026-04-28 18:46:25','2026-04-28 18:46:25',NULL,NULL,NULL,NULL),(74,74,'Rossi Srl',NULL,NULL,'customer-28677535989791@example.com',NULL,'COMPANY','ACTIVE','2026-04-28 20:08:48','2026-04-28 20:08:48',NULL,NULL,NULL,NULL),(75,96,'Rossi Srl','IT0984327898',NULL,'customer-4009228697458@example.com','+13311675741','COMPANY','ACTIVE','2026-04-29 04:12:04','2026-05-11 16:26:04','Via della fontana 24, prima voc. fontana 7','Montecastrilli','05026','IT'),(76,96,'Bianchi Srl','908798543',NULL,'customer-4009231593291@example.com','+393476681161','COMPANY','ACTIVE','2026-04-29 04:12:04','2026-05-11 16:27:44','VIA GIAN DOMENICO ROMAGNOSI 11','TERNI','05100','IT'),(77,76,'Alfa Srl',NULL,NULL,'customer-4009233142791@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(78,77,'Rossi Srl',NULL,NULL,'customer-4009399204375@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(79,77,'Bianchi Srl',NULL,NULL,'customer-4009400848541@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(80,78,'Alfa Srl',NULL,NULL,'customer-4009402706125@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(81,79,'Rossi Srl',NULL,NULL,'customer-4009474103125@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(82,79,'Bianchi Srl',NULL,NULL,'customer-4009475728583@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(83,80,'Alfa Srl',NULL,NULL,'customer-4009477193208@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(84,81,'Rossi Srl','125TYHBN','YT67UIKM','customer-4009542552500@example.com','3311675741','COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04','VIA DELLA FONTANA 24','MONTECASTRILLI','05026','IT'),(85,81,'Bianchi Srl','1POLK98UI',NULL,'customer-4009544162458@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(86,82,'Alfa Srl',NULL,NULL,'customer-4009545695791@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(87,83,'Rossi Srl',NULL,NULL,'customer-4009609751375@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(88,83,'Bianchi Srl',NULL,NULL,'customer-4009611152708@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(89,84,'Alfa Srl',NULL,NULL,'customer-4009612564916@example.com',NULL,'COMPANY','ACTIVE','2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `delivery_note`
--

LOCK TABLES `delivery_note` WRITE;
/*!40000 ALTER TABLE `delivery_note` DISABLE KEYS */;
INSERT INTO `delivery_note` VALUES (1,75,29,76,'DDT-2026-00001','2026-05-02','ISSUED','Vendita','Franco',NULL,'EUR',140.00,30.80,170.80,NULL,'2026-05-02 19:26:05','2026-05-02 19:26:05'),(2,75,10,76,'DDT-2026-00002','2026-05-02','CANCELLED','Vendita','Franco',NULL,'EUR',200.00,44.00,244.00,NULL,'2026-05-02 19:38:59','2026-05-02 19:38:59'),(3,75,9,75,'DDT-2026-00003','2026-05-03','ISSUED','Vendita','Franco',NULL,'EUR',1000.00,220.00,1220.00,NULL,'2026-05-02 20:24:16','2026-05-02 20:24:16'),(4,85,30,75,'DDT-2026-00001','2026-05-06','ISSUED','Vendita','Franco',NULL,'EUR',580.00,127.60,707.60,NULL,'2026-05-06 17:32:42','2026-05-06 17:32:42'),(5,85,31,75,'DDT-2026-00002','2026-05-07','CANCELLED','Vendita','Franco',NULL,'EUR',464.00,102.08,566.08,NULL,'2026-05-06 20:24:49','2026-05-06 20:24:49'),(6,86,32,76,'DDT-2026-00001','2026-05-07','ISSUED','Vendita','Franco',NULL,'EUR',5220.00,1148.40,6368.40,NULL,'2026-05-06 21:32:42','2026-05-06 21:32:42'),(7,86,33,75,'DDT-2026-00002','2026-05-07','ISSUED','Vendita','Franco',NULL,'EUR',1080.00,237.60,1317.60,NULL,'2026-05-06 21:54:30','2026-05-06 21:54:30'),(8,87,34,76,'DDT-2026-00001','2026-05-08','CANCELLED','Vendita','Franco',NULL,'EUR',11600.00,2552.00,14152.00,NULL,'2026-05-07 22:55:41','2026-05-07 22:55:41'),(9,87,35,75,'DDT-2026-00002','2026-05-08','ISSUED','Vendita','Franco',NULL,'EUR',2900.00,638.00,3538.00,NULL,'2026-05-08 12:18:24','2026-05-08 12:18:24'),(10,87,36,76,'DDT-2026-00003','2026-05-08','ISSUED','Vendita','Franco',NULL,'EUR',100.00,22.00,122.00,NULL,'2026-05-08 15:02:12','2026-05-08 15:02:12'),(11,87,37,76,'DDT-2026-00004','2026-05-09','ISSUED','Vendita','Franco',NULL,'EUR',580.00,127.60,707.60,NULL,'2026-05-09 03:34:14','2026-05-09 03:34:14'),(12,87,38,76,'DDT-2026-00005','2026-05-09','CANCELLED','Vendita','Franco',NULL,'EUR',580.00,127.60,707.60,NULL,'2026-05-09 08:22:53','2026-05-09 08:22:53'),(15,88,39,75,'DDT-2026-00001','2026-05-09','ISSUED','Vendita','Franco',NULL,'EUR',580.00,127.60,707.60,NULL,'2026-05-09 09:34:00','2026-05-09 09:34:00'),(16,88,40,75,'DDT-2026-00002','2026-05-09','ISSUED','Vendita','Franco',NULL,'EUR',580.00,127.60,707.60,NULL,'2026-05-09 12:01:05','2026-05-09 12:01:05'),(17,88,41,76,'DDT-2026-00003','2026-05-10','ISSUED','Vendita','Franco',NULL,'EUR',580.00,127.60,707.60,NULL,'2026-05-10 08:03:18','2026-05-10 08:03:18'),(18,88,42,75,'DDT-2026-00004','2026-05-10','ISSUED','Vendita','Franco',NULL,'EUR',580.00,127.60,707.60,NULL,'2026-05-10 08:36:03','2026-05-10 08:36:03'),(19,88,43,76,'DDT-2026-00005','2026-05-10','ISSUED','Vendita','Franco',NULL,'EUR',580.00,127.60,707.60,NULL,'2026-05-10 09:11:47','2026-05-10 09:11:47'),(20,88,44,75,'DDT-2026-00006','2026-05-10','ISSUED','Vendita','Franco',NULL,'EUR',580.00,127.60,707.60,NULL,'2026-05-10 14:06:30','2026-05-10 14:06:30'),(22,90,46,75,'DDT-2026-00001','2026-05-13','ISSUED','Vendita','Franco',NULL,'EUR',580.00,127.60,707.60,NULL,'2026-05-13 15:07:44','2026-05-13 15:07:44'),(23,90,45,76,'DDT-2026-00002','2026-05-13','ISSUED','Vendita','Franco',NULL,'EUR',1200.00,264.00,1464.00,NULL,'2026-05-13 15:08:33','2026-05-13 15:08:33'),(24,91,47,76,'DDT-2026-00001','2026-05-15','ISSUED','Vendita','Franco',NULL,'EUR',580.00,127.60,707.60,NULL,'2026-05-15 02:31:33','2026-05-15 02:31:33'),(25,91,48,75,'DDT-2026-00002','2026-05-16','CANCELLED','Vendita','Franco',NULL,'EUR',5800.00,1276.00,7076.00,NULL,'2026-05-15 22:15:48','2026-05-15 22:15:48'),(27,91,49,76,'DDT-2026-00003','2026-05-16','ISSUED','Vendita','Franco',NULL,'EUR',120.00,26.40,146.40,NULL,'2026-05-16 17:58:24','2026-05-16 17:58:24'),(28,92,50,76,'DDT-2026-00001','2026-05-17','ISSUED','Vendita','Franco',NULL,'EUR',120.00,26.40,146.40,NULL,'2026-05-16 21:10:25','2026-05-16 21:10:25'),(29,92,51,76,'DDT-2026-00002','2026-05-17','ISSUED','Vendita','Franco',NULL,'EUR',120.00,26.40,146.40,NULL,'2026-05-16 21:28:56','2026-05-16 21:28:56'),(30,92,52,76,'DDT-2026-00003','2026-05-17','ISSUED','Vendita','Franco',NULL,'EUR',120.00,26.40,146.40,NULL,'2026-05-16 21:33:01','2026-05-16 21:33:01'),(31,92,53,76,'DDT-2026-00004','2026-05-17','ISSUED','Vendita','Franco',NULL,'EUR',120.00,26.40,146.40,NULL,'2026-05-16 22:04:45','2026-05-16 22:04:45'),(32,92,54,76,'DDT-2026-00005','2026-05-17','ISSUED','Vendita','Franco',NULL,'EUR',120.00,26.40,146.40,NULL,'2026-05-16 22:13:05','2026-05-16 22:13:05'),(33,92,55,76,'DDT-2026-00006','2026-05-17','ISSUED','Vendita','Franco',NULL,'EUR',120.00,26.40,146.40,NULL,'2026-05-17 07:46:05','2026-05-17 07:46:05'),(34,92,56,76,'DDT-2026-00007','2026-05-17','ISSUED','Vendita','Franco',NULL,'EUR',20.00,4.40,24.40,NULL,'2026-05-17 11:05:40','2026-05-17 11:05:40'),(35,92,57,76,'DDT-2026-00008','2026-05-18','ISSUED','Vendita','Franco',NULL,'EUR',522.00,114.84,636.84,NULL,'2026-05-18 10:13:40','2026-05-18 10:13:40'),(38,92,59,76,'DDT-2026-00009','2026-05-19','ISSUED','Vendita','Franco',NULL,'EUR',120.00,26.40,146.40,NULL,'2026-05-19 15:21:00','2026-05-19 15:21:00'),(39,92,60,76,'DDT-2026-00010','2026-05-19','ISSUED','Vendita','Franco',NULL,'EUR',120.00,26.40,146.40,NULL,'2026-05-19 15:35:23','2026-05-19 15:35:23'),(40,93,61,76,'DDT-2026-00001','2026-05-20','ISSUED','Vendita','Franco',NULL,'EUR',120.00,26.40,146.40,NULL,'2026-05-20 07:58:02','2026-05-20 07:58:02'),(41,93,62,76,'DDT-2026-00002','2026-05-20','ISSUED','Vendita','Franco',NULL,'EUR',120.00,26.40,146.40,NULL,'2026-05-20 08:15:36','2026-05-20 08:15:36'),(42,93,64,76,'DDT-2026-00003','2026-05-21','ISSUED','Vendita','Franco',NULL,'EUR',70.00,15.40,85.40,NULL,'2026-05-21 16:10:46','2026-05-21 16:10:46'),(43,93,65,76,'DDT-2026-00004','2026-05-22','ISSUED','Vendita','Franco',NULL,'EUR',590.00,129.80,719.80,NULL,'2026-05-22 02:45:23','2026-05-22 02:45:23'),(44,93,66,76,'DDT-2026-00005','2026-05-23','ISSUED','Vendita','Franco',NULL,'EUR',590.00,129.80,719.80,NULL,'2026-05-23 06:01:33','2026-05-23 06:01:33'),(45,93,67,76,'DDT-2026-00006','2026-05-23','ISSUED','Vendita','Franco',NULL,'EUR',590.00,129.80,719.80,NULL,'2026-05-23 11:31:17','2026-05-23 11:31:17'),(46,93,68,76,'DDT-2026-00007','2026-05-24','ISSUED','Vendita','Franco',NULL,'EUR',590.00,129.80,719.80,NULL,'2026-05-23 23:52:31','2026-05-23 23:52:31'),(47,93,69,75,'DDT-2026-00008','2026-05-25','ISSUED','Vendita','Franco',NULL,'EUR',100.00,22.00,122.00,NULL,'2026-05-25 04:06:34','2026-05-25 04:06:34'),(52,93,73,75,'DDT-2026-00009','2026-05-25','ISSUED','Vendita','Franco',NULL,'EUR',150.00,33.00,183.00,NULL,'2026-05-25 04:54:53','2026-05-25 04:54:53'),(53,93,74,76,'DDT-2026-00010','2026-05-25','ISSUED','Vendita','Franco',NULL,'EUR',150.00,27.00,177.00,NULL,'2026-05-25 05:21:17','2026-05-25 05:21:17'),(65,94,76,75,'DDT-00001','2026-06-03','ISSUED','Vendita','Franco',NULL,'EUR',10.00,2.20,12.20,NULL,'2026-06-03 01:45:00','2026-06-03 01:45:00'),(66,94,75,76,'DDT-00002','2026-06-03','ISSUED','Vendita','Franco',NULL,'EUR',10.00,2.20,12.20,NULL,'2026-06-03 01:45:47','2026-06-03 01:45:47'),(67,95,77,76,'DDT-00001','2026-06-03','ISSUED','Vendita','Franco',NULL,'EUR',28.00,6.16,34.16,NULL,'2026-06-03 10:11:59','2026-06-03 10:11:59'),(69,96,78,76,'DDT-00001','2026-06-03','ISSUED','Vendita','Franco',NULL,'EUR',20.00,4.40,24.40,NULL,'2026-06-03 17:43:30','2026-06-03 17:43:30'),(70,96,79,76,'DDT-00002','2026-06-03','ISSUED','Vendita','Franco',NULL,'EUR',20.00,4.40,24.40,NULL,'2026-06-03 18:58:59','2026-06-03 18:58:59');
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
  KEY `idx_delivery_note_line_item` (`tenant_id`,`item_id`),
  KEY `FK4fjlrvqba0f3g072eubjtba8j` (`delivery_note_id`),
  CONSTRAINT `FK4fjlrvqba0f3g072eubjtba8j` FOREIGN KEY (`delivery_note_id`) REFERENCES `delivery_note` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `delivery_note_line`
--

LOCK TABLES `delivery_note_line` WRITE;
/*!40000 ALTER TABLE `delivery_note_line` DISABLE KEYS */;
INSERT INTO `delivery_note_line` VALUES (1,75,1,44,1,'RIGA 1',1.000,'pz',100.00,10.00,22.00,19.80,109.80,NULL),(2,75,1,45,2,'RIGA 2',1.000,'pz',50.00,0.00,22.00,11.00,61.00,NULL),(3,75,2,18,1,'Setup',1.000,'pz',200.00,0.00,22.00,44.00,200.00,NULL),(4,75,3,17,1,'Modulo CRM',1.000,'pz',1000.00,0.00,22.00,220.00,1000.00,NULL),(5,85,4,46,1,'RIGA 1',1.000,'pz',580.00,0.00,22.00,127.60,580.00,NULL),(6,85,5,47,1,'RIGA 1',1.000,'pz',580.00,20.00,22.00,102.08,464.00,2),(7,86,6,48,1,'RIGA 1',10.000,'pz',580.00,10.00,22.00,1148.40,5220.00,2),(8,86,7,49,1,'RIGA 1',1.000,'pz',1200.00,10.00,22.00,237.60,1080.00,1),(9,87,8,50,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',20.000,'pz',580.00,0.00,22.00,2552.00,11600.00,2),(10,87,9,51,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',5.000,'pz',580.00,0.00,22.00,638.00,2900.00,2),(11,87,10,52,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',100.00,0.00,22.00,22.00,100.00,2),(12,87,11,53,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(13,87,12,54,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(16,88,15,55,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(17,88,16,56,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(18,88,17,57,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(19,88,18,58,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(20,88,19,59,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(21,88,20,60,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(23,90,22,62,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(24,90,23,61,1,'Modulo CRM',1.000,'pz',1200.00,0.00,22.00,264.00,1200.00,1),(25,91,24,63,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(26,91,25,64,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'pz',580.00,0.00,22.00,1276.00,5800.00,2),(27,91,27,65,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,'pz',10.00,0.00,22.00,26.40,120.00,2),(28,92,28,66,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,'pz',10.00,0.00,22.00,26.40,120.00,2),(29,92,29,67,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,'pz',10.00,0.00,22.00,26.40,120.00,2),(30,92,30,68,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,'pz',10.00,0.00,22.00,26.40,120.00,2),(31,92,31,69,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,'pz',10.00,0.00,22.00,26.40,120.00,2),(32,92,32,70,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,'pz',10.00,0.00,22.00,26.40,120.00,2),(33,92,33,71,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,'pz',10.00,0.00,22.00,26.40,120.00,2),(34,92,34,72,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',2.000,'pz',10.00,0.00,22.00,4.40,20.00,2),(35,92,35,73,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,10.00,22.00,114.84,522.00,2),(38,92,38,75,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,'pz',10.00,0.00,22.00,26.40,120.00,2),(39,92,39,76,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,'pz',10.00,0.00,22.00,26.40,120.00,2),(40,93,40,77,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,'pz',10.00,0.00,22.00,26.40,120.00,2),(41,93,41,78,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,'pz',10.00,0.00,22.00,26.40,120.00,2),(42,93,42,80,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',70.00,0.00,22.00,15.40,70.00,2),(43,93,43,81,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',590.00,0.00,22.00,129.80,590.00,2),(44,93,44,82,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',590.00,0.00,22.00,129.80,590.00,2),(45,93,45,83,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',590.00,0.00,22.00,129.80,590.00,2),(46,93,46,84,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',590.00,0.00,22.00,129.80,590.00,2),(47,93,47,85,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',100.00,0.00,22.00,22.00,100.00,2),(56,93,52,92,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',100.00,0.00,22.00,22.00,100.00,2),(57,93,52,93,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',50.00,0.00,22.00,11.00,50.00,2),(58,93,53,94,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',100.00,0.00,22.00,22.00,100.00,2),(59,93,53,95,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',50.00,0.00,10.00,5.00,50.00,2),(71,94,65,97,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',10.00,0.00,22.00,2.20,10.00,2),(72,94,66,96,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',10.00,0.00,22.00,2.20,10.00,2),(73,95,67,98,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',20.00,10.00,22.00,3.96,18.00,2),(74,95,67,99,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',10.00,0.00,22.00,2.20,10.00,2),(76,96,69,100,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',20.00,0.00,22.00,4.40,20.00,2),(77,96,70,101,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',2.000,'pz',10.00,0.00,22.00,4.40,20.00,2);
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
  `document_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `prefix` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `next_number` bigint NOT NULL,
  `padding_size` int NOT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_document_sequence_tenant_type` (`tenant_id`,`document_type`),
  KEY `idx_document_sequence_tenant` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `document_sequence`
--

LOCK TABLES `document_sequence` WRITE;
/*!40000 ALTER TABLE `document_sequence` DISABLE KEYS */;
INSERT INTO `document_sequence` VALUES (1,1,'QUOTE','QUO-',1,5,'2026-06-02 12:31:23','2026-06-02 12:32:25'),(2,1,'SALES_ORDER','SO-',1,5,'2026-06-02 12:31:23','2026-06-02 12:32:25'),(3,1,'DELIVERY_NOTE','DDT-',1,5,'2026-06-02 12:31:23','2026-06-02 12:32:25'),(4,1,'SALES_INVOICE','INV-',1,5,'2026-06-02 12:31:23','2026-06-02 12:32:25'),(5,1,'PURCHASE_ORDER','PO-',1,5,'2026-06-02 12:31:23','2026-06-02 12:32:25'),(6,1,'GOODS_RECEIPT','GR-',1,5,'2026-06-02 12:31:23','2026-06-02 12:32:25'),(7,1,'SUPPLIER_INVOICE','SI-',1,5,'2026-06-02 12:31:23','2026-06-02 12:32:25'),(8,1,'JOURNAL_ENTRY','JE-',1,5,'2026-06-02 12:31:23','2026-06-02 12:32:25'),(9,93,'QUOTE','QUO-',1,5,'2026-06-02 12:35:24','2026-06-02 12:35:24'),(10,93,'SALES_ORDER','SO-',1,5,'2026-06-02 12:35:24','2026-06-02 12:35:24'),(11,93,'DELIVERY_NOTE','DDT-',1,5,'2026-06-02 12:35:24','2026-06-02 12:35:24'),(12,93,'SALES_INVOICE','INV-',1,5,'2026-06-02 12:35:24','2026-06-02 12:35:24'),(13,93,'PURCHASE_ORDER','PO-',1,5,'2026-06-02 12:35:24','2026-06-02 12:35:24'),(14,93,'GOODS_RECEIPT','GR-',1,5,'2026-06-02 12:35:24','2026-06-02 12:35:24'),(15,93,'SUPPLIER_INVOICE','SI-',1,5,'2026-06-02 12:35:24','2026-06-02 12:35:24'),(16,93,'JOURNAL_ENTRY','JE-',1,5,'2026-06-02 12:35:24','2026-06-02 12:35:24'),(17,94,'QUOTE','QUO-',3,5,'2026-06-03 01:32:06','2026-06-02 16:40:00'),(18,94,'SALES_ORDER','SO-',3,5,'2026-06-03 01:32:15','2026-06-02 16:40:00'),(19,94,'DELIVERY_NOTE','DDT-',3,5,'2026-06-03 03:45:47','2026-06-02 16:40:00'),(20,94,'SALES_INVOICE','INV-',3,5,'2026-06-03 04:45:11','2026-06-02 16:40:00'),(21,94,'PURCHASE_ORDER','PO-',2,5,'2026-06-03 11:04:02','2026-06-02 16:40:00'),(22,94,'GOODS_RECEIPT','GR-',2,5,'2026-06-03 11:04:17','2026-06-02 16:40:00'),(23,94,'SUPPLIER_INVOICE','SI-',2,5,'2026-06-03 11:04:40','2026-06-02 16:40:00'),(24,94,'JOURNAL_ENTRY','JE-',1,5,'2026-06-02 16:40:00','2026-06-02 16:40:00'),(25,95,'QUOTE','QUO-',2,5,'2026-06-03 12:02:08','2026-06-03 12:01:14'),(26,95,'SALES_ORDER','SO-',2,5,'2026-06-03 12:06:16','2026-06-03 12:01:14'),(27,95,'DELIVERY_NOTE','DDT-',2,5,'2026-06-03 12:11:59','2026-06-03 12:01:14'),(28,95,'SALES_INVOICE','INV-',2,5,'2026-06-03 12:17:06','2026-06-03 12:01:14'),(29,95,'PURCHASE_ORDER','PO-',3,5,'2026-06-03 15:20:58','2026-06-03 12:01:14'),(30,95,'GOODS_RECEIPT','GR-',7,5,'2026-06-03 15:21:06','2026-06-03 12:01:14'),(31,95,'SUPPLIER_INVOICE','SI-',3,5,'2026-06-03 15:21:25','2026-06-03 12:01:15'),(32,95,'JOURNAL_ENTRY','JE-',1,5,'2026-06-03 12:01:15','2026-06-03 12:01:15'),(33,96,'QUOTE','QUO-',3,5,'2026-06-03 20:58:41','2026-06-03 19:41:20'),(34,96,'SALES_ORDER','SO-',3,5,'2026-06-03 20:58:45','2026-06-03 19:41:20'),(35,96,'DELIVERY_NOTE','DDT-',3,5,'2026-06-03 20:58:59','2026-06-03 19:41:20'),(36,96,'SALES_INVOICE','INV-',3,5,'2026-06-04 06:11:39','2026-06-03 19:41:20'),(37,96,'PURCHASE_ORDER','PO-',4,5,'2026-06-03 20:31:03','2026-06-03 19:41:20'),(38,96,'GOODS_RECEIPT','GR-',4,5,'2026-06-03 20:31:10','2026-06-03 19:41:20'),(39,96,'SUPPLIER_INVOICE','SI-',1,5,'2026-06-03 19:41:20','2026-06-03 19:41:20'),(40,96,'JOURNAL_ENTRY','JE-',1,5,'2026-06-03 19:41:20','2026-06-03 19:41:20');
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
) ENGINE=InnoDB AUTO_INCREMENT=74 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `goods_receipt`
--

LOCK TABLES `goods_receipt` WRITE;
/*!40000 ALTER TABLE `goods_receipt` DISABLE KEYS */;
INSERT INTO `goods_receipt` VALUES (1,85,'GR-00001','2026-05-07',1,1,'Ricezione automatica da ordine fornitore PO-00001','2026-05-07 19:42:02','2026-05-07 19:42:02'),(2,87,'GR-00002','2026-05-08',2,1,'Ricezione automatica da ordine fornitore PO-00002','2026-05-08 01:03:27','2026-05-08 01:03:27'),(3,87,'GR-00003','2026-05-08',3,1,'Ricezione automatica da ordine fornitore PO-00003','2026-05-08 03:06:28','2026-05-08 03:06:28'),(4,87,'GR-00004','2026-05-08',4,1,'Ricezione automatica da ordine fornitore PO-00004','2026-05-08 14:21:15','2026-05-08 14:21:15'),(5,87,'GR-00005','2026-05-09',5,1,'Ricezione automatica da ordine fornitore PO-00005','2026-05-09 05:35:08','2026-05-09 05:35:08'),(6,88,'GR-00006','2026-05-09',6,1,'Ricezione automatica da ordine fornitore PO-00006','2026-05-09 20:14:52','2026-05-09 20:14:52'),(7,88,'GR-00007','2026-05-10',7,1,'Ricezione automatica da ordine fornitore PO-00007','2026-05-10 11:20:37','2026-05-10 11:20:37'),(8,88,'GR-00008','2026-05-10',8,1,'Ricezione automatica da ordine fornitore PO-00008','2026-05-10 16:14:54','2026-05-10 16:14:54'),(9,90,'GR-00009','2026-05-13',10,1,'Ricezione automatica da ordine fornitore PO-00010','2026-05-13 21:35:46','2026-05-13 21:35:46'),(10,90,'GR-00010','2026-05-13',9,2,'Ricezione automatica da ordine fornitore PO-00009','2026-05-13 21:40:38','2026-05-13 21:40:38'),(11,91,'GR-00011','2026-05-15',11,2,'Ricezione automatica da ordine fornitore PO-00011','2026-05-15 04:37:41','2026-05-15 04:37:41'),(12,91,'GR-00012','2026-05-16',12,2,'Ricezione automatica da ordine fornitore PO-00012','2026-05-15 22:18:50','2026-05-15 22:18:50'),(13,91,'GR-00013','2026-05-16',13,2,'Ricezione automatica da ordine fornitore PO-00013','2026-05-15 23:02:36','2026-05-15 23:02:36'),(14,91,'GR-00014','2026-05-16',14,1,'Ricezione automatica da ordine fornitore PO-00014','2026-05-15 23:04:24','2026-05-15 23:04:24'),(15,91,'GR-00015','2026-05-16',15,2,'Ricezione automatica da ordine fornitore PO-00015','2026-05-15 23:36:38','2026-05-15 23:36:38'),(16,91,'GR-00016','2026-05-16',16,2,'Ricezione automatica da ordine fornitore PO-00016','2026-05-16 00:13:21','2026-05-16 00:13:21'),(18,91,'GR-00017','2026-05-16',17,2,'Ricezione automatica da ordine fornitore PO-00017','2026-05-16 19:46:52','2026-05-16 19:46:52'),(19,91,'GR-00018','2026-05-16',18,2,'Ricezione automatica da ordine fornitore PO-00018','2026-05-16 19:50:35','2026-05-16 19:50:35'),(20,91,'GR-00019','2026-05-16',19,2,'Ricezione automatica da ordine fornitore PO-00019','2026-05-16 20:19:06','2026-05-16 20:19:06'),(21,91,'GR-00020','2026-05-16',20,2,'Ricezione automatica da ordine fornitore PO-00020','2026-05-16 20:23:32','2026-05-16 20:23:32'),(22,91,'GR-00021','2026-05-16',21,2,'Ricezione automatica da ordine fornitore PO-00021','2026-05-16 20:26:13','2026-05-16 20:26:13'),(23,91,'GR-00022','2026-05-16',22,2,'Ricezione automatica da ordine fornitore PO-00022','2026-05-16 20:34:04','2026-05-16 20:34:04'),(24,91,'GR-00023','2026-05-16',23,2,'Ricezione automatica da ordine fornitore PO-00023','2026-05-16 20:35:59','2026-05-16 20:35:59'),(25,91,'GR-00024','2026-05-16',24,2,'Ricezione automatica da ordine fornitore PO-00024','2026-05-16 20:46:40','2026-05-16 20:46:40'),(26,91,'GR-00025','2026-05-16',25,2,'Ricezione automatica da ordine fornitore PO-00025','2026-05-16 20:49:27','2026-05-16 20:49:27'),(27,91,'GR-00026','2026-05-16',26,2,'Ricezione automatica da ordine fornitore PO-00026','2026-05-16 20:54:56','2026-05-16 20:54:56'),(28,91,'GR-00027','2026-05-16',27,2,'Ricezione automatica da ordine fornitore PO-00027','2026-05-16 20:56:19','2026-05-16 20:56:19'),(29,91,'GR-00028','2026-05-16',28,2,'Ricezione automatica da ordine fornitore PO-00028','2026-05-16 21:07:46','2026-05-16 21:07:46'),(30,91,'GR-00029','2026-05-16',29,2,'Ricezione automatica da ordine fornitore PO-00029','2026-05-16 21:26:03','2026-05-16 21:26:03'),(31,91,'GR-00030','2026-05-16',30,1,'Ricezione automatica da ordine fornitore PO-00030','2026-05-16 21:31:57','2026-05-16 21:31:57'),(32,91,'GR-00031','2026-05-16',31,2,'Ricezione automatica da ordine fornitore PO-00031','2026-05-16 21:34:33','2026-05-16 21:34:33'),(37,92,'GR-00032','2026-05-17',36,2,'Ricezione automatica da ordine fornitore PO-00036','2026-05-16 22:39:02','2026-05-16 22:39:02'),(38,92,'GR-00033','2026-05-17',37,2,'Ricezione automatica da ordine fornitore PO-00037','2026-05-16 23:06:47','2026-05-16 23:06:47'),(39,92,'GR-00034','2026-05-17',38,2,'Ricezione automatica da ordine fornitore PO-00038','2026-05-16 23:08:28','2026-05-16 23:08:28'),(40,92,'GR-00035','2026-05-17',39,2,'Ricezione automatica da ordine fornitore PO-00039','2026-05-17 00:02:35','2026-05-17 00:02:35'),(41,92,'GR-00036','2026-05-17',40,2,'Ricezione automatica da ordine fornitore PO-00040','2026-05-17 00:03:40','2026-05-17 00:03:40'),(42,92,'GR-00037','2026-05-17',41,2,'Ricezione automatica da ordine fornitore PO-00041','2026-05-17 00:11:18','2026-05-17 00:11:18'),(43,92,'GR-00038','2026-05-17',42,2,'Ricezione automatica da ordine fornitore PO-00042','2026-05-17 00:12:11','2026-05-17 00:12:11'),(44,92,'GR-00039','2026-05-17',43,2,'Ricezione automatica da ordine fornitore PO-00043','2026-05-17 09:44:12','2026-05-17 09:44:12'),(45,92,'GR-00040','2026-05-17',44,2,'Ricezione automatica da ordine fornitore PO-00044','2026-05-17 09:45:04','2026-05-17 09:45:04'),(46,92,'GR-00041','2026-05-18',45,2,'Ricezione automatica da ordine fornitore PO-00045','2026-05-18 12:23:51','2026-05-18 12:23:51'),(47,92,'GR-00042','2026-05-19',46,2,'Ricezione automatica da ordine fornitore PO-00046','2026-05-19 16:47:08','2026-05-19 16:47:08'),(48,92,'GR-00043','2026-05-19',47,2,'Ricezione automatica da ordine fornitore PO-00047','2026-05-19 16:48:21','2026-05-19 16:48:21'),(49,92,'GR-00044','2026-05-19',49,2,'Ricezione automatica da ordine fornitore PO-00049','2026-05-19 17:18:26','2026-05-19 17:18:26'),(50,92,'GR-00045','2026-05-19',50,2,'Ricezione automatica da ordine fornitore PO-00050','2026-05-19 17:25:21','2026-05-19 17:25:21'),(51,92,'GR-00046','2026-05-19',51,2,'Ricezione automatica da ordine fornitore PO-00051','2026-05-19 17:26:26','2026-05-19 17:26:26'),(52,93,'GR-00047','2026-05-20',52,2,'Ricezione automatica da ordine fornitore PO-00052','2026-05-20 09:53:33','2026-05-20 09:53:33'),(53,93,'GR-00048','2026-05-20',53,2,'Ricezione automatica da ordine fornitore PO-00053','2026-05-20 09:56:12','2026-05-20 09:56:12'),(54,93,'GR-00049','2026-05-20',54,2,'Ricezione automatica da ordine fornitore PO-00054','2026-05-20 09:56:49','2026-05-20 09:56:49'),(55,93,'GR-00050','2026-05-20',55,2,'Ricezione automatica da ordine fornitore PO-00055','2026-05-20 10:13:49','2026-05-20 10:13:49'),(56,93,'GR-00051','2026-05-20',56,2,'Ricezione automatica da ordine fornitore PO-00056','2026-05-20 10:14:35','2026-05-20 10:14:35'),(57,93,'GR-00052','2026-05-23',65,2,'Ricezione automatica da ordine fornitore PO-00065','2026-05-22 23:30:00','2026-05-22 23:30:00'),(58,93,'GR-00053','2026-05-23',66,2,'Ricezione automatica da ordine fornitore PO-00066','2026-05-22 23:33:35','2026-05-22 23:33:35'),(59,93,'GR-00054','2026-05-23',67,2,'Ricezione automatica da ordine fornitore PO-00067','2026-05-23 13:53:59','2026-05-23 13:53:59'),(60,93,'GR-00055','2026-05-24',68,2,'Ricezione automatica da ordine fornitore PO-00068','2026-05-24 04:12:45','2026-05-24 04:12:45'),(61,93,'GR-00056','2026-05-25',69,2,'Ricezione automatica da ordine fornitore PO-00069','2026-05-25 07:26:44','2026-05-25 07:26:44'),(62,93,'GR-00057','2026-05-25',70,2,'Ricezione automatica da ordine fornitore PO-00070','2026-05-25 07:29:02','2026-05-25 07:29:02'),(63,93,'GR-00058','2026-05-25',71,2,'Ricezione automatica da ordine fornitore PO-00071','2026-05-25 07:30:48','2026-05-25 07:30:48'),(64,94,'GR-00001','2026-06-03',72,2,'Ricezione automatica da ordine fornitore PO-00001','2026-06-03 11:04:17','2026-06-03 11:04:17'),(69,95,'GR-00005','2026-06-03',73,2,'Ricezione automatica da ordine fornitore PO-00001','2026-06-03 15:01:30','2026-06-03 15:01:30'),(70,95,'GR-00006','2026-06-03',74,1,'Ricezione automatica da ordine fornitore PO-00002','2026-06-03 15:21:06','2026-06-03 15:21:06'),(71,96,'GR-00001','2026-06-03',75,2,'Ricezione automatica da ordine fornitore PO-00001','2026-06-03 19:55:04','2026-06-03 19:55:04'),(72,96,'GR-00002','2026-06-03',76,1,'Ricezione automatica da ordine fornitore PO-00002','2026-06-03 20:04:51','2026-06-03 20:04:51'),(73,96,'GR-00003','2026-06-03',77,2,'Ricezione automatica da ordine fornitore PO-00003','2026-06-03 20:31:10','2026-06-03 20:31:10');
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
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `goods_receipt_line`
--

LOCK TABLES `goods_receipt_line` WRITE;
/*!40000 ALTER TABLE `goods_receipt_line` DISABLE KEYS */;
INSERT INTO `goods_receipt_line` VALUES (1,85,1,1,2,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-07 19:42:02','2026-05-07 19:42:02',NULL,NULL),(2,87,2,1,4,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',50.000,'2026-05-08 01:03:27','2026-05-08 01:03:27',NULL,NULL),(3,87,3,1,6,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-08 03:06:28','2026-05-08 03:06:28',NULL,NULL),(4,87,4,1,8,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',9.998,'2026-05-08 14:21:15','2026-05-08 14:21:15',NULL,NULL),(5,87,5,1,9,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-09 05:35:08','2026-05-09 05:35:08',NULL,NULL),(6,88,6,1,10,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',2.000,'2026-05-09 20:14:52','2026-05-09 20:14:52',NULL,NULL),(7,88,7,1,11,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-10 11:20:37','2026-05-10 11:20:37',NULL,NULL),(8,88,8,1,12,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-10 16:14:54','2026-05-10 16:14:54',NULL,NULL),(9,90,9,1,18,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-13 21:35:46','2026-05-13 21:35:46',NULL,NULL),(10,90,10,1,15,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-13 21:40:38','2026-05-13 21:40:38',NULL,NULL),(11,90,10,2,16,1,'Modulo CRM',1.000,'2026-05-13 21:40:38','2026-05-13 21:40:38',NULL,NULL),(12,91,11,1,19,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-15 04:37:41','2026-05-15 04:37:41',NULL,NULL),(13,91,12,1,20,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',11.000,'2026-05-15 22:18:50','2026-05-15 22:18:50',NULL,NULL),(14,91,13,1,21,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-15 23:02:36','2026-05-15 23:02:36',NULL,NULL),(15,91,14,1,22,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-15 23:11:24','2026-05-15 23:11:24',NULL,NULL),(16,91,15,1,23,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-15 23:37:14','2026-05-15 23:37:14',NULL,NULL),(17,91,16,1,24,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',100.000,'2026-05-16 00:13:21','2026-05-16 00:13:21',NULL,NULL),(18,91,18,1,25,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-16 19:47:23','2026-05-16 19:47:23',NULL,NULL),(19,91,19,1,26,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-16 19:50:35','2026-05-16 19:50:35',NULL,NULL),(20,91,20,1,27,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-16 20:19:06','2026-05-16 20:19:06',NULL,NULL),(21,91,21,1,29,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-16 20:25:16','2026-05-16 20:25:16',NULL,NULL),(22,91,22,1,30,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-16 20:33:20','2026-05-16 20:33:20',NULL,NULL),(23,91,23,1,31,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-16 20:34:04','2026-05-16 20:34:04',NULL,NULL),(24,91,24,1,33,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-16 20:45:40','2026-05-16 20:45:40',NULL,NULL),(25,91,25,1,34,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-16 20:46:40','2026-05-16 20:46:40',NULL,NULL),(26,91,26,1,35,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-16 20:50:06','2026-05-16 20:50:06',NULL,NULL),(27,91,27,1,36,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-16 20:54:56','2026-05-16 20:54:56',NULL,NULL),(28,91,28,1,37,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,'2026-05-16 20:59:29','2026-05-16 20:59:29',NULL,NULL),(29,91,29,1,38,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-16 21:11:28','2026-05-16 21:11:28',NULL,NULL),(30,91,30,1,39,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-16 21:26:03','2026-05-16 21:26:03',NULL,NULL),(31,91,31,1,40,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-16 21:33:34','2026-05-16 21:33:34',NULL,NULL),(32,91,32,1,41,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-16 21:34:47','2026-05-16 21:34:47',NULL,NULL),(37,92,37,1,48,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-16 22:39:05','2026-05-16 22:39:05',580.0000,580.0000),(38,92,38,1,49,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-16 23:06:47','2026-05-16 23:06:47',5.0000,50.0000),(39,92,39,1,50,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-16 23:08:28','2026-05-16 23:08:28',7.0000,70.0000),(40,92,40,1,51,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-17 00:02:35','2026-05-17 00:02:35',5.0000,50.0000),(41,92,41,1,52,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-17 00:03:40','2026-05-17 00:03:40',7.0000,70.0000),(42,92,42,1,53,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-17 00:11:18','2026-05-17 00:11:18',5.0000,50.0000),(43,92,43,1,54,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-17 00:12:11','2026-05-17 00:12:11',7.0000,70.0000),(44,92,44,1,55,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-17 09:44:12','2026-05-17 09:44:12',5.0000,50.0000),(45,92,45,1,56,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-17 09:45:04','2026-05-17 09:45:04',7.0000,70.0000),(46,92,46,1,57,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',2.000,'2026-05-18 12:23:51','2026-05-18 12:23:51',580.0000,1160.0000),(47,92,47,1,58,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-19 16:47:08','2026-05-19 16:47:08',5.0000,50.0000),(48,92,48,1,59,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-19 16:48:21','2026-05-19 16:48:21',7.0000,70.0000),(49,92,49,1,62,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-19 17:18:26','2026-05-19 17:18:26',7.0000,70.0000),(50,92,50,1,63,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-19 17:25:21','2026-05-19 17:25:21',5.0000,50.0000),(51,92,51,1,65,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-19 17:26:26','2026-05-19 17:26:26',7.0000,70.0000),(52,93,52,1,66,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-20 09:53:33','2026-05-20 09:53:33',5.0000,50.0000),(53,93,53,1,67,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-20 09:56:12','2026-05-20 09:56:12',5.0000,50.0000),(54,93,54,1,68,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-20 09:56:49','2026-05-20 09:56:49',7.0000,70.0000),(55,93,55,1,69,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-20 10:13:49','2026-05-20 10:13:49',5.0000,50.0000),(56,93,56,1,70,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,'2026-05-20 10:14:35','2026-05-20 10:14:35',7.0000,70.0000),(57,93,57,1,80,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-22 23:30:01','2026-05-22 23:30:01',590.0000,590.0000),(58,93,58,1,81,1,'Modulo CRM',1.000,'2026-05-22 23:33:36','2026-05-22 23:33:36',1200.0000,1200.0000),(59,93,59,1,82,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-23 13:53:59','2026-05-23 13:53:59',590.0000,590.0000),(60,93,60,1,83,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-24 04:12:46','2026-05-24 04:12:46',590.0000,590.0000),(61,93,61,1,85,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-25 07:26:44','2026-05-25 07:26:44',100.0000,100.0000),(62,93,62,1,86,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-25 07:29:02','2026-05-25 07:29:02',100.0000,100.0000),(63,93,62,2,87,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-25 07:29:02','2026-05-25 07:29:02',50.0000,50.0000),(64,93,63,1,88,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-25 07:30:48','2026-05-25 07:30:48',100.0000,100.0000),(65,93,63,2,89,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-05-25 07:30:48','2026-05-25 07:30:48',50.0000,50.0000),(66,94,64,1,90,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-06-03 11:04:17','2026-06-03 11:04:17',590.0000,590.0000),(75,95,69,1,93,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-06-03 15:01:30','2026-06-03 15:01:30',20.0000,20.0000),(76,95,69,2,94,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-06-03 15:01:31','2026-06-03 15:01:31',10.0000,10.0000),(77,95,70,1,95,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-06-03 15:21:06','2026-06-03 15:21:06',590.0000,590.0000),(78,96,71,1,96,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-06-03 19:55:04','2026-06-03 19:55:04',10.0000,10.0000),(79,96,72,1,97,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-06-03 20:04:51','2026-06-03 20:04:51',50.0000,50.0000),(80,96,73,1,98,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'2026-06-03 20:31:10','2026-06-03 20:31:10',100.0000,100.0000);
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
  KEY `idx_inv_layer_source_movement` (`tenant_id`,`source_movement_id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory_layer`
--

LOCK TABLES `inventory_layer` WRITE;
/*!40000 ALTER TABLE `inventory_layer` DISABLE KEYS */;
INSERT INTO `inventory_layer` VALUES (1,93,2,1,'2026-05-20',10.000,1.000,5.0000,_binary '\0','2026-05-20 10:13:49','2026-05-25 07:21:17'),(2,93,2,2,'2026-05-20',10.000,0.000,7.0000,_binary '','2026-05-20 10:14:35','2026-05-20 10:15:36'),(3,93,2,6,'2026-05-23',1.000,0.000,590.0000,_binary '','2026-05-22 23:30:03','2026-05-23 08:01:33'),(4,93,2,9,'2026-05-23',1.000,0.000,590.0000,_binary '','2026-05-23 13:54:00','2026-05-24 01:52:31'),(5,93,2,11,'2026-05-24',1.000,0.000,590.0000,_binary '','2026-05-24 04:12:46','2026-05-25 06:06:34'),(6,93,2,17,'2026-05-25',1.000,1.000,100.0000,_binary '\0','2026-05-25 07:26:44','2026-05-25 07:26:44'),(7,93,2,18,'2026-05-25',1.000,1.000,100.0000,_binary '\0','2026-05-25 07:29:03','2026-05-25 07:29:03'),(8,93,2,19,'2026-05-25',1.000,1.000,50.0000,_binary '\0','2026-05-25 07:29:03','2026-05-25 07:29:03'),(9,93,2,20,'2026-05-25',1.000,1.000,100.0000,_binary '\0','2026-05-25 07:30:48','2026-05-25 07:30:48'),(10,93,2,21,'2026-05-25',1.000,1.000,50.0000,_binary '\0','2026-05-25 07:30:48','2026-05-25 07:30:48'),(11,94,2,22,'2026-06-03',10.000,10.000,590.0000,_binary '\0','2026-06-03 00:25:49','2026-06-03 00:25:49'),(12,94,2,23,'2026-06-03',10.000,8.000,590.0000,_binary '\0','2026-06-03 01:54:22','2026-06-03 03:45:47'),(13,94,2,27,'2026-06-03',1.000,1.000,590.0000,_binary '\0','2026-06-03 11:04:17','2026-06-03 11:04:17'),(14,95,2,28,'2026-06-03',10.000,8.000,590.0000,_binary '\0','2026-06-03 12:08:49','2026-06-03 12:11:59'),(15,95,2,33,'2026-06-03',1.000,1.000,20.0000,_binary '\0','2026-06-03 14:40:55','2026-06-03 14:40:55'),(16,95,2,34,'2026-06-03',1.000,1.000,10.0000,_binary '\0','2026-06-03 14:40:55','2026-06-03 14:40:55'),(17,95,2,37,'2026-06-03',1.000,1.000,20.0000,_binary '\0','2026-06-03 14:45:28','2026-06-03 14:45:28'),(18,95,2,38,'2026-06-03',1.000,1.000,10.0000,_binary '\0','2026-06-03 14:45:28','2026-06-03 14:45:28'),(19,95,2,41,'2026-06-03',1.000,1.000,20.0000,_binary '\0','2026-06-03 14:50:24','2026-06-03 14:50:24'),(20,95,2,42,'2026-06-03',1.000,1.000,10.0000,_binary '\0','2026-06-03 14:50:24','2026-06-03 14:50:24'),(21,95,2,45,'2026-06-03',1.000,1.000,20.0000,_binary '\0','2026-06-03 14:52:43','2026-06-03 14:52:43'),(22,95,2,46,'2026-06-03',1.000,1.000,10.0000,_binary '\0','2026-06-03 14:52:43','2026-06-03 14:52:43'),(23,95,2,49,'2026-06-03',1.000,1.000,20.0000,_binary '\0','2026-06-03 15:01:31','2026-06-03 15:01:31'),(24,95,2,50,'2026-06-03',1.000,1.000,10.0000,_binary '\0','2026-06-03 15:01:31','2026-06-03 15:01:31'),(25,95,2,52,'2026-06-03',1.000,1.000,590.0000,_binary '\0','2026-06-03 15:21:06','2026-06-03 15:21:06'),(26,95,2,53,'2026-06-03',11.000,11.000,590.0000,_binary '\0','2026-06-03 19:06:45','2026-06-03 19:06:45'),(27,96,2,54,'2026-06-03',10.000,9.000,590.0000,_binary '\0','2026-06-03 19:43:12','2026-06-03 19:43:30'),(28,96,2,57,'2026-06-03',1.000,1.000,10.0000,_binary '\0','2026-06-03 19:55:04','2026-06-03 19:55:04'),(29,96,2,59,'2026-06-03',1.000,0.000,50.0000,_binary '','2026-06-03 20:04:51','2026-06-03 20:58:59'),(30,96,2,60,'2026-06-03',1.000,0.000,100.0000,_binary '','2026-06-03 20:31:10','2026-06-03 20:58:59');
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
  `out_movement_id` bigint NOT NULL,
  `layer_id` bigint NOT NULL,
  `consumed_qty` decimal(15,3) NOT NULL,
  `unit_cost` decimal(15,4) NOT NULL,
  `total_cost` decimal(15,4) NOT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_inv_layer_cons_out` (`tenant_id`,`out_movement_id`),
  KEY `idx_inv_layer_cons_layer` (`tenant_id`,`layer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory_layer_consumption`
--

LOCK TABLES `inventory_layer_consumption` WRITE;
/*!40000 ALTER TABLE `inventory_layer_consumption` DISABLE KEYS */;
INSERT INTO `inventory_layer_consumption` VALUES (1,93,3,2,10.000,7.0000,70.0000,'2026-05-20 10:15:36','2026-05-20 10:15:36'),(2,93,3,1,2.000,5.0000,10.0000,'2026-05-20 10:15:36','2026-05-20 10:15:36'),(3,93,4,1,1.000,5.0000,5.0000,'2026-05-21 18:10:46','2026-05-21 18:10:46'),(4,93,5,1,1.000,5.0000,5.0000,'2026-05-22 04:45:23','2026-05-22 04:45:23'),(5,93,7,3,1.000,590.0000,590.0000,'2026-05-23 08:01:33','2026-05-23 08:01:33'),(6,93,8,1,1.000,5.0000,5.0000,'2026-05-23 13:31:17','2026-05-23 13:31:17'),(7,93,10,4,1.000,590.0000,590.0000,'2026-05-24 01:52:31','2026-05-24 01:52:31'),(8,93,12,5,1.000,590.0000,590.0000,'2026-05-25 06:06:34','2026-05-25 06:06:34'),(9,93,13,1,1.000,5.0000,5.0000,'2026-05-25 06:54:53','2026-05-25 06:54:53'),(10,93,14,1,1.000,5.0000,5.0000,'2026-05-25 06:54:53','2026-05-25 06:54:53'),(11,93,15,1,1.000,5.0000,5.0000,'2026-05-25 07:21:17','2026-05-25 07:21:17'),(12,93,16,1,1.000,5.0000,5.0000,'2026-05-25 07:21:17','2026-05-25 07:21:17'),(13,94,24,12,1.000,590.0000,590.0000,'2026-06-03 03:45:00','2026-06-03 03:45:00'),(14,94,25,12,1.000,590.0000,590.0000,'2026-06-03 03:45:47','2026-06-03 03:45:47'),(15,95,29,14,1.000,590.0000,590.0000,'2026-06-03 12:11:59','2026-06-03 12:11:59'),(16,95,30,14,1.000,590.0000,590.0000,'2026-06-03 12:11:59','2026-06-03 12:11:59'),(17,96,55,27,1.000,590.0000,590.0000,'2026-06-03 19:43:30','2026-06-03 19:43:30'),(18,96,61,30,1.000,100.0000,100.0000,'2026-06-03 20:58:59','2026-06-03 20:58:59'),(19,96,61,29,1.000,50.0000,50.0000,'2026-06-03 20:58:59','2026-06-03 20:58:59');
/*!40000 ALTER TABLE `inventory_layer_consumption` ENABLE KEYS */;
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
  PRIMARY KEY (`id`),
  KEY `idx_inv_mov_tenant_item_date` (`tenant_id`,`item_id`,`movement_date`),
  KEY `idx_inv_mov_tenant_ref` (`tenant_id`,`reference_type`,`reference_id`),
  KEY `idx_inv_mov_tenant_causal` (`tenant_id`,`causal_code`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory_movement`
--

LOCK TABLES `inventory_movement` WRITE;
/*!40000 ALTER TABLE `inventory_movement` DISABLE KEYS */;
INSERT INTO `inventory_movement` VALUES (1,93,2,'2026-05-20','IN','PURCHASE_RECEIPT',10.000,5.0000,50.0000,'GOODS_RECEIPT',55,'Carico da ricezione merci GR-00050',_binary '\0',NULL,'2026-05-20 10:13:49','2026-05-20 10:13:49'),(2,93,2,'2026-05-20','IN','PURCHASE_RECEIPT',10.000,7.0000,70.0000,'GOODS_RECEIPT',56,'Carico da ricezione merci GR-00051',_binary '\0',NULL,'2026-05-20 10:14:35','2026-05-20 10:14:35'),(3,93,2,'2026-05-20','OUT','SALES_DELIVERY',12.000,6.6667,80.0000,'DELIVERY_NOTE',41,'Scarico da DDT DDT-2026-00002',_binary '\0',NULL,'2026-05-20 10:15:36','2026-05-20 10:15:36'),(4,93,2,'2026-05-21','OUT','SALES_DELIVERY',1.000,5.0000,5.0000,'DELIVERY_NOTE',42,'Scarico da DDT DDT-2026-00003',_binary '\0',NULL,'2026-05-21 18:10:46','2026-05-21 18:10:46'),(5,93,2,'2026-05-22','OUT','SALES_DELIVERY',1.000,5.0000,5.0000,'DELIVERY_NOTE',43,'Scarico da DDT DDT-2026-00004',_binary '\0',NULL,'2026-05-22 04:45:23','2026-05-22 04:45:23'),(6,93,2,'2026-05-23','IN','PURCHASE_RECEIPT',1.000,590.0000,590.0000,'GOODS_RECEIPT',57,'Carico da ricezione merci GR-00052',_binary '\0',NULL,'2026-05-22 23:30:02','2026-05-22 23:30:02'),(7,93,2,'2026-05-23','OUT','SALES_DELIVERY',1.000,590.0000,590.0000,'DELIVERY_NOTE',44,'Scarico da DDT DDT-2026-00005',_binary '\0',NULL,'2026-05-23 08:01:33','2026-05-23 08:01:33'),(8,93,2,'2026-05-23','OUT','SALES_DELIVERY',1.000,5.0000,5.0000,'DELIVERY_NOTE',45,'Scarico da DDT DDT-2026-00006',_binary '\0',NULL,'2026-05-23 13:31:17','2026-05-23 13:31:17'),(9,93,2,'2026-05-23','IN','PURCHASE_RECEIPT',1.000,590.0000,590.0000,'GOODS_RECEIPT',59,'Carico da ricezione merci GR-00054',_binary '\0',NULL,'2026-05-23 13:53:59','2026-05-23 13:53:59'),(10,93,2,'2026-05-24','OUT','SALES_DELIVERY',1.000,590.0000,590.0000,'DELIVERY_NOTE',46,'Scarico da DDT DDT-2026-00007',_binary '\0',NULL,'2026-05-24 01:52:31','2026-05-24 01:52:31'),(11,93,2,'2026-05-24','IN','PURCHASE_RECEIPT',1.000,590.0000,590.0000,'GOODS_RECEIPT',60,'Carico da ricezione merci GR-00055',_binary '\0',NULL,'2026-05-24 04:12:46','2026-05-24 04:12:46'),(12,93,2,'2026-05-25','OUT','SALES_DELIVERY',1.000,590.0000,590.0000,'DELIVERY_NOTE',47,'Scarico da DDT DDT-2026-00008',_binary '\0',NULL,'2026-05-25 06:06:34','2026-05-25 06:06:34'),(13,93,2,'2026-05-25','OUT','SALES_DELIVERY',1.000,5.0000,5.0000,'DELIVERY_NOTE',52,'Scarico da DDT DDT-2026-00009',_binary '\0',NULL,'2026-05-25 06:54:53','2026-05-25 06:54:53'),(14,93,2,'2026-05-25','OUT','SALES_DELIVERY',1.000,5.0000,5.0000,'DELIVERY_NOTE',52,'Scarico da DDT DDT-2026-00009',_binary '\0',NULL,'2026-05-25 06:54:53','2026-05-25 06:54:53'),(15,93,2,'2026-05-25','OUT','SALES_DELIVERY',1.000,5.0000,5.0000,'DELIVERY_NOTE',53,'Scarico da DDT DDT-2026-00010',_binary '\0',NULL,'2026-05-25 07:21:17','2026-05-25 07:21:17'),(16,93,2,'2026-05-25','OUT','SALES_DELIVERY',1.000,5.0000,5.0000,'DELIVERY_NOTE',53,'Scarico da DDT DDT-2026-00010',_binary '\0',NULL,'2026-05-25 07:21:17','2026-05-25 07:21:17'),(17,93,2,'2026-05-25','IN','PURCHASE_RECEIPT',1.000,100.0000,100.0000,'GOODS_RECEIPT',61,'Carico da ricezione merci GR-00056',_binary '\0',NULL,'2026-05-25 07:26:44','2026-05-25 07:26:44'),(18,93,2,'2026-05-25','IN','PURCHASE_RECEIPT',1.000,100.0000,100.0000,'GOODS_RECEIPT',62,'Carico da ricezione merci GR-00057',_binary '\0',NULL,'2026-05-25 07:29:03','2026-05-25 07:29:03'),(19,93,2,'2026-05-25','IN','PURCHASE_RECEIPT',1.000,50.0000,50.0000,'GOODS_RECEIPT',62,'Carico da ricezione merci GR-00057',_binary '\0',NULL,'2026-05-25 07:29:03','2026-05-25 07:29:03'),(20,93,2,'2026-05-25','IN','PURCHASE_RECEIPT',1.000,100.0000,100.0000,'GOODS_RECEIPT',63,'Carico da ricezione merci GR-00058',_binary '\0',NULL,'2026-05-25 07:30:48','2026-05-25 07:30:48'),(21,93,2,'2026-05-25','IN','PURCHASE_RECEIPT',1.000,50.0000,50.0000,'GOODS_RECEIPT',63,'Carico da ricezione merci GR-00058',_binary '\0',NULL,'2026-05-25 07:30:48','2026-05-25 07:30:48'),(22,94,2,'2026-06-03','ADJUSTMENT_IN','MANUAL_ADJUSTMENT_IN',10.000,590.0000,5900.0000,'MANUAL_INVENTORY',NULL,'',_binary '\0',NULL,'2026-06-03 00:25:49','2026-06-03 00:25:49'),(23,94,2,'2026-06-03','ADJUSTMENT_IN','MANUAL_ADJUSTMENT_IN',10.000,590.0000,5900.0000,'MANUAL_INVENTORY',NULL,'',_binary '\0',NULL,'2026-06-03 01:54:22','2026-06-03 01:54:22'),(24,94,2,'2026-06-03','OUT','SALES_DELIVERY',1.000,590.0000,590.0000,'DELIVERY_NOTE',65,'Scarico da DDT DDT-00001',_binary '\0',NULL,'2026-06-03 03:45:00','2026-06-03 03:45:00'),(25,94,2,'2026-06-03','OUT','SALES_DELIVERY',1.000,590.0000,590.0000,'DELIVERY_NOTE',66,'Scarico da DDT DDT-00002',_binary '\0',NULL,'2026-06-03 03:45:47','2026-06-03 03:45:47'),(26,94,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,NULL,NULL,'GOODS_RECEIPT',64,'Carico automatico da ricezione merci GR-00001',_binary '\0',NULL,'2026-06-03 11:04:17','2026-06-03 11:04:17'),(27,94,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,590.0000,590.0000,'GOODS_RECEIPT',64,'Carico da ricezione merci GR-00001',_binary '\0',NULL,'2026-06-03 11:04:17','2026-06-03 11:04:17'),(28,95,2,'2026-06-03','ADJUSTMENT_IN','MANUAL_ADJUSTMENT_IN',10.000,590.0000,5900.0000,'MANUAL_INVENTORY',NULL,'RETTIFICA AUMENTO GIACENZA 10 PEZZI',_binary '\0',NULL,'2026-06-03 12:08:49','2026-06-03 12:08:49'),(29,95,2,'2026-06-03','OUT','SALES_DELIVERY',1.000,590.0000,590.0000,'DELIVERY_NOTE',67,'Scarico da DDT DDT-00001',_binary '\0',NULL,'2026-06-03 12:11:59','2026-06-03 12:11:59'),(30,95,2,'2026-06-03','OUT','SALES_DELIVERY',1.000,590.0000,590.0000,'DELIVERY_NOTE',67,'Scarico da DDT DDT-00001',_binary '\0',NULL,'2026-06-03 12:11:59','2026-06-03 12:11:59'),(31,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,NULL,NULL,'GOODS_RECEIPT',65,'Carico automatico da ricezione merci GR-00001',_binary '\0',NULL,'2026-06-03 14:40:55','2026-06-03 14:40:55'),(32,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,NULL,NULL,'GOODS_RECEIPT',65,'Carico automatico da ricezione merci GR-00001',_binary '\0',NULL,'2026-06-03 14:40:55','2026-06-03 14:40:55'),(33,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,20.0000,20.0000,'GOODS_RECEIPT',65,'Carico da ricezione merci GR-00001',_binary '\0',NULL,'2026-06-03 14:40:55','2026-06-03 14:40:55'),(34,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,10.0000,10.0000,'GOODS_RECEIPT',65,'Carico da ricezione merci GR-00001',_binary '\0',NULL,'2026-06-03 14:40:55','2026-06-03 14:40:55'),(35,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,NULL,NULL,'GOODS_RECEIPT',66,'Carico automatico da ricezione merci GR-00002',_binary '\0',NULL,'2026-06-03 14:45:28','2026-06-03 14:45:28'),(36,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,NULL,NULL,'GOODS_RECEIPT',66,'Carico automatico da ricezione merci GR-00002',_binary '\0',NULL,'2026-06-03 14:45:28','2026-06-03 14:45:28'),(37,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,20.0000,20.0000,'GOODS_RECEIPT',66,'Carico da ricezione merci GR-00002',_binary '\0',NULL,'2026-06-03 14:45:28','2026-06-03 14:45:28'),(38,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,10.0000,10.0000,'GOODS_RECEIPT',66,'Carico da ricezione merci GR-00002',_binary '\0',NULL,'2026-06-03 14:45:28','2026-06-03 14:45:28'),(39,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,NULL,NULL,'GOODS_RECEIPT',67,'Carico automatico da ricezione merci GR-00003',_binary '\0',NULL,'2026-06-03 14:50:24','2026-06-03 14:50:24'),(40,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,NULL,NULL,'GOODS_RECEIPT',67,'Carico automatico da ricezione merci GR-00003',_binary '\0',NULL,'2026-06-03 14:50:24','2026-06-03 14:50:24'),(41,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,20.0000,20.0000,'GOODS_RECEIPT',67,'Carico da ricezione merci GR-00003',_binary '\0',NULL,'2026-06-03 14:50:24','2026-06-03 14:50:24'),(42,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,10.0000,10.0000,'GOODS_RECEIPT',67,'Carico da ricezione merci GR-00003',_binary '\0',NULL,'2026-06-03 14:50:24','2026-06-03 14:50:24'),(43,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,NULL,NULL,'GOODS_RECEIPT',68,'Carico automatico da ricezione merci GR-00004',_binary '\0',NULL,'2026-06-03 14:52:43','2026-06-03 14:52:43'),(44,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,NULL,NULL,'GOODS_RECEIPT',68,'Carico automatico da ricezione merci GR-00004',_binary '\0',NULL,'2026-06-03 14:52:43','2026-06-03 14:52:43'),(45,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,20.0000,20.0000,'GOODS_RECEIPT',68,'Carico da ricezione merci GR-00004',_binary '\0',NULL,'2026-06-03 14:52:43','2026-06-03 14:52:43'),(46,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,10.0000,10.0000,'GOODS_RECEIPT',68,'Carico da ricezione merci GR-00004',_binary '\0',NULL,'2026-06-03 14:52:43','2026-06-03 14:52:43'),(47,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,NULL,NULL,'GOODS_RECEIPT',69,'Carico automatico da ricezione merci GR-00005',_binary '\0',NULL,'2026-06-03 15:01:31','2026-06-03 15:01:31'),(48,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,NULL,NULL,'GOODS_RECEIPT',69,'Carico automatico da ricezione merci GR-00005',_binary '\0',NULL,'2026-06-03 15:01:31','2026-06-03 15:01:31'),(49,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,20.0000,20.0000,'GOODS_RECEIPT',69,'Carico da ricezione merci GR-00005',_binary '\0',NULL,'2026-06-03 15:01:31','2026-06-03 15:01:31'),(50,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,10.0000,10.0000,'GOODS_RECEIPT',69,'Carico da ricezione merci GR-00005',_binary '\0',NULL,'2026-06-03 15:01:31','2026-06-03 15:01:31'),(51,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,NULL,NULL,'GOODS_RECEIPT',70,'Carico automatico da ricezione merci GR-00006',_binary '\0',NULL,'2026-06-03 15:21:06','2026-06-03 15:21:06'),(52,95,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,590.0000,590.0000,'GOODS_RECEIPT',70,'Carico da ricezione merci GR-00006',_binary '\0',NULL,'2026-06-03 15:21:06','2026-06-03 15:21:06'),(53,95,2,'2026-06-03','ADJUSTMENT_IN','MANUAL_ADJUSTMENT_IN',11.000,590.0000,6490.0000,'MANUAL_INVENTORY',NULL,'',_binary '\0',NULL,'2026-06-03 19:06:45','2026-06-03 19:06:45'),(54,96,2,'2026-06-03','ADJUSTMENT_IN','MANUAL_ADJUSTMENT_IN',10.000,590.0000,5900.0000,'MANUAL_INVENTORY',NULL,'',_binary '\0',NULL,'2026-06-03 19:43:12','2026-06-03 19:43:12'),(55,96,2,'2026-06-03','OUT','SALES_DELIVERY',1.000,590.0000,590.0000,'DELIVERY_NOTE',69,'Scarico da DDT DDT-00001',_binary '\0',NULL,'2026-06-03 19:43:30','2026-06-03 19:43:30'),(57,96,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,10.0000,10.0000,'GOODS_RECEIPT',71,'Carico da ricezione merci GR-00001',_binary '\0',NULL,'2026-06-03 19:55:04','2026-06-03 19:55:04'),(59,96,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,50.0000,50.0000,'GOODS_RECEIPT',72,'Carico da ricezione merci GR-00002',_binary '\0',NULL,'2026-06-03 20:04:51','2026-06-03 20:04:51'),(60,96,2,'2026-06-03','IN','PURCHASE_RECEIPT',1.000,100.0000,100.0000,'GOODS_RECEIPT',73,'Carico da ricezione merci GR-00003',_binary '\0',NULL,'2026-06-03 20:31:10','2026-06-03 20:31:10'),(61,96,2,'2026-06-03','OUT','SALES_DELIVERY',2.000,75.0000,150.0000,'DELIVERY_NOTE',70,'Scarico da DDT DDT-00002',_binary '\0',NULL,'2026-06-03 20:58:59','2026-06-03 20:58:59');
/*!40000 ALTER TABLE `inventory_movement` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invoice`
--

LOCK TABLES `invoice` WRITE;
/*!40000 ALTER TABLE `invoice` DISABLE KEYS */;
INSERT INTO `invoice` VALUES (1,75,1,29,76,'INV-2026-00001','2026-05-04','ISSUED','EUR',140.00,30.80,170.80,NULL,'2026-05-03 22:10:38','2026-05-03 22:10:38'),(2,75,3,9,75,'INV-2026-00002','2026-05-04','CANCELLED','EUR',1000.00,220.00,1220.00,NULL,'2026-05-03 22:12:39','2026-05-03 22:12:39'),(3,86,6,32,76,'INV-2026-00001','2026-05-07','ISSUED','EUR',5220.00,1148.40,6368.40,NULL,'2026-05-06 21:45:03','2026-05-06 21:45:03'),(4,86,7,33,75,'INV-2026-00002','2026-05-07','ISSUED','EUR',1080.00,237.60,1317.60,NULL,'2026-05-06 21:54:38','2026-05-06 21:54:38'),(5,87,9,35,75,'INV-2026-00001','2026-05-08','ISSUED','EUR',2900.00,638.00,3538.00,NULL,'2026-05-08 12:18:31','2026-05-08 12:18:31'),(6,87,10,36,76,'INV-2026-00002','2026-05-08','ISSUED','EUR',100.00,22.00,122.00,NULL,'2026-05-08 15:02:21','2026-05-08 15:02:21'),(7,87,11,37,76,'INV-2026-00003','2026-05-09','ISSUED','EUR',580.00,127.60,707.60,NULL,'2026-05-09 03:34:18','2026-05-09 03:34:18'),(8,88,15,39,75,'INV-2026-00001','2026-05-09','CANCELLED','EUR',580.00,127.60,707.60,NULL,'2026-05-09 09:34:24','2026-05-09 09:34:24'),(9,88,16,40,75,'INV-2026-00002','2026-05-09','ISSUED','EUR',580.00,127.60,707.60,NULL,'2026-05-09 12:02:58','2026-05-09 12:02:58'),(10,88,17,41,76,'INV-2026-00003','2026-05-10','ISSUED','EUR',580.00,127.60,707.60,NULL,'2026-05-10 08:04:21','2026-05-10 08:04:21'),(11,88,18,42,75,'INV-2026-00004','2026-05-10','ISSUED','EUR',580.00,127.60,707.60,NULL,'2026-05-10 08:36:14','2026-05-10 08:36:14'),(12,88,19,43,76,'INV-2026-00005','2026-05-10','ISSUED','EUR',580.00,127.60,707.60,NULL,'2026-05-10 09:11:52','2026-05-10 09:11:52'),(13,88,20,44,75,'INV-2026-00006','2026-05-10','ISSUED','EUR',580.00,127.60,707.60,NULL,'2026-05-10 14:06:40','2026-05-10 14:06:40'),(17,90,22,46,75,'INV-2026-00001','2026-05-13','ISSUED','EUR',580.00,127.60,707.60,NULL,'2026-05-13 17:08:47','2026-05-13 17:08:47'),(18,90,23,45,76,'INV-2026-00002','2026-05-13','ISSUED','EUR',1200.00,264.00,1464.00,NULL,'2026-05-13 17:33:07','2026-05-13 17:33:07'),(19,91,24,47,76,'INV-2026-00001','2026-05-15','ISSUED','EUR',580.00,127.60,707.60,NULL,'2026-05-15 02:32:08','2026-05-15 02:32:08'),(20,92,29,51,76,'INV-2026-00001','2026-05-17','ISSUED','EUR',120.00,26.40,146.40,NULL,'2026-05-16 21:31:20','2026-05-16 21:31:20'),(21,92,30,52,76,'INV-2026-00002','2026-05-17','ISSUED','EUR',120.00,26.40,146.40,NULL,'2026-05-16 21:34:35','2026-05-16 21:34:35'),(22,92,31,53,76,'INV-2026-00003','2026-05-17','ISSUED','EUR',120.00,26.40,146.40,NULL,'2026-05-16 22:04:50','2026-05-16 22:04:50'),(23,92,32,54,76,'INV-2026-00004','2026-05-17','ISSUED','EUR',120.00,26.40,146.40,NULL,'2026-05-16 22:13:09','2026-05-16 22:13:09'),(24,92,33,55,76,'INV-2026-00005','2026-05-17','ISSUED','EUR',120.00,26.40,146.40,NULL,'2026-05-17 07:46:09','2026-05-17 07:46:09'),(25,92,35,57,76,'INV-2026-00006','2026-05-18','ISSUED','EUR',522.00,114.84,636.84,NULL,'2026-05-18 10:14:14','2026-05-18 10:14:14'),(26,92,38,59,76,'INV-2026-00007','2026-05-19','ISSUED','EUR',120.00,26.40,146.40,NULL,'2026-05-19 15:21:06','2026-05-19 15:21:06'),(27,93,42,64,76,'INV-2026-00001','2026-05-21','ISSUED','EUR',70.00,15.40,85.40,NULL,'2026-05-21 16:11:05','2026-05-21 16:11:05'),(28,93,43,65,76,'INV-2026-00002','2026-05-22','CANCELLED','EUR',590.00,129.80,719.80,NULL,'2026-05-22 02:45:28','2026-05-22 02:45:28'),(29,93,44,66,76,'INV-2026-00003','2026-05-23','ISSUED','EUR',590.00,129.80,719.80,NULL,'2026-05-23 06:01:37','2026-05-23 06:01:37'),(30,93,45,67,76,'INV-2026-00004','2026-05-23','ISSUED','EUR',590.00,129.80,719.80,NULL,'2026-05-23 11:31:21','2026-05-23 11:31:21'),(31,93,46,68,76,'INV-2026-00005','2026-05-24','ISSUED','EUR',590.00,129.80,719.80,NULL,'2026-05-23 23:52:35','2026-05-23 23:52:35'),(32,93,47,69,75,'INV-2026-00006','2026-05-25','ISSUED','EUR',100.00,22.00,122.00,NULL,'2026-05-25 04:06:38','2026-05-25 04:06:38'),(33,93,52,73,75,'INV-2026-00007','2026-05-25','ISSUED','EUR',150.00,33.00,183.00,NULL,'2026-05-25 04:55:00','2026-05-25 04:55:00'),(34,93,53,74,76,'INV-2026-00008','2026-05-25','ISSUED','EUR',150.00,27.00,177.00,NULL,'2026-05-25 05:21:21','2026-05-25 05:21:21'),(35,94,66,75,76,'INV-00001','2026-06-03','ISSUED','EUR',10.00,2.20,12.20,NULL,'2026-06-03 02:43:52','2026-06-03 02:43:52'),(36,94,65,76,75,'INV-00002','2026-06-03','ISSUED','EUR',10.00,2.20,12.20,NULL,'2026-06-03 02:45:11','2026-06-03 02:45:11'),(37,95,67,77,76,'INV-00001','2026-06-03','ISSUED','EUR',28.00,6.16,34.16,NULL,'2026-06-03 10:17:06','2026-06-03 10:17:06'),(38,96,70,79,76,'INV-00001','2026-06-04','ISSUED','EUR',20.00,4.40,24.40,NULL,'2026-06-04 04:11:13','2026-06-04 04:11:13'),(39,96,69,78,76,'INV-00002','2026-06-04','ISSUED','EUR',20.00,4.40,24.40,NULL,'2026-06-04 04:11:39','2026-06-04 04:11:39');
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
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invoice_line`
--

LOCK TABLES `invoice_line` WRITE;
/*!40000 ALTER TABLE `invoice_line` DISABLE KEYS */;
INSERT INTO `invoice_line` VALUES (1,75,1,1,1,'RIGA 1',1.000,'pz',100.00,10.00,22.00,19.80,109.80,NULL),(2,75,1,2,2,'RIGA 2',1.000,'pz',50.00,0.00,22.00,11.00,61.00,NULL),(3,75,2,4,1,'Modulo CRM',1.000,'pz',1000.00,0.00,22.00,220.00,1000.00,NULL),(4,86,3,7,1,'RIGA 1',10.000,'pz',580.00,10.00,22.00,1148.40,5220.00,2),(5,86,4,8,1,'RIGA 1',1.000,'pz',1200.00,10.00,22.00,237.60,1080.00,1),(6,87,5,10,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',5.000,'pz',580.00,0.00,22.00,638.00,2900.00,2),(7,87,6,11,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',100.00,0.00,22.00,22.00,100.00,2),(8,87,7,12,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(9,88,8,16,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(10,88,9,17,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(11,88,10,18,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(12,88,11,19,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(13,88,12,20,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(14,88,13,21,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(18,90,17,23,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(19,90,18,24,1,'Modulo CRM',1.000,'pz',1200.00,0.00,22.00,264.00,1200.00,1),(20,91,19,25,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,0.00,22.00,127.60,580.00,2),(21,92,20,29,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,'pz',10.00,0.00,22.00,26.40,120.00,2),(22,92,21,30,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,'pz',10.00,0.00,22.00,26.40,120.00,2),(23,92,22,31,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,'pz',10.00,0.00,22.00,26.40,120.00,2),(24,92,23,32,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,'pz',10.00,0.00,22.00,26.40,120.00,2),(25,92,24,33,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,'pz',10.00,0.00,22.00,26.40,120.00,2),(26,92,25,35,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',580.00,10.00,22.00,114.84,522.00,2),(27,92,26,38,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,'pz',10.00,0.00,22.00,26.40,120.00,2),(28,93,27,42,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',70.00,0.00,22.00,15.40,70.00,2),(29,93,28,43,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',590.00,0.00,22.00,129.80,590.00,2),(30,93,29,44,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',590.00,0.00,22.00,129.80,590.00,2),(31,93,30,45,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',590.00,0.00,22.00,129.80,590.00,2),(32,93,31,46,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',590.00,0.00,22.00,129.80,590.00,2),(33,93,32,47,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',100.00,0.00,22.00,22.00,100.00,2),(34,93,33,56,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',100.00,0.00,22.00,22.00,100.00,2),(35,93,33,57,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',50.00,0.00,22.00,11.00,50.00,2),(36,93,34,58,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',100.00,0.00,22.00,22.00,100.00,2),(37,93,34,59,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',50.00,0.00,10.00,5.00,50.00,2),(38,94,35,72,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',10.00,0.00,22.00,2.20,10.00,2),(39,94,36,71,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',10.00,0.00,22.00,2.20,10.00,2),(40,95,37,73,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',20.00,10.00,22.00,3.96,18.00,2),(41,95,37,74,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',10.00,0.00,22.00,2.20,10.00,2),(42,96,38,77,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',2.000,'pz',10.00,0.00,22.00,4.40,20.00,2),(43,96,39,76,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,'pz',20.00,0.00,22.00,4.40,20.00,2);
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
INSERT INTO `item` VALUES (1,96,'ART-001','Modulo CRM','Modulo CRM','SERVICE','pz',1,0,1200.00,22.00,'2026-05-06 18:10:31','2026-05-06 19:03:38'),(2,96,'ART-002','FORNO ELETTRICO','FORNO ELETTRICO ALIMENTATO A 220V-230V','PRODUCT','pz',1,1,590.00,22.00,'2026-05-06 18:16:26','2026-05-20 06:31:30');
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
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `journal_entry`
--

LOCK TABLES `journal_entry` WRITE;
/*!40000 ALTER TABLE `journal_entry` DISABLE KEYS */;
INSERT INTO `journal_entry` VALUES (3,88,'JE-00001','2026-05-10','MANUAL_JOURNAL','PROVA JOURNAL',NULL,NULL,'EUR',100.00,100.00,_binary '','TEST JOURNAL','2026-05-10 00:53:28','2026-05-10 00:53:28'),(4,88,'JE-00002','2026-05-10','MANUAL_JOURNAL','TEST INCASSO',NULL,NULL,'EUR',580.00,580.00,_binary '','PROVA INCASSO','2026-05-10 10:12:18','2026-05-10 10:12:18'),(5,88,'JE-00003','2026-05-10','MANUAL_JOURNAL','PROVA DUE',NULL,NULL,'EUR',780.00,780.00,_binary '','TEST INCASSO','2026-05-10 10:19:39','2026-05-10 10:19:39'),(6,88,'JE-00004','2026-05-10','MANUAL_JOURNAL','TEST INCASSO',NULL,NULL,'EUR',707.60,707.60,_binary '','TEST INCASSO','2026-05-10 10:45:02','2026-05-10 10:45:02'),(7,88,'JE-00005','2026-05-10','CUSTOMER_RECEIPT','Incasso cliente su scadenza INV-2026-00005','PAYMENT_DUE',11,'EUR',707.60,707.60,_binary '','PROVA LIBRO JOURNAL','2026-05-10 11:12:38','2026-05-10 11:12:38'),(8,88,'JE-00006','2026-05-10','SUPPLIER_PAYMENT','Pagamento fornitore su scadenza SI-00006','PAYMENT_DUE',12,'EUR',707.60,707.60,_binary '','PROVA JOURNAL','2026-05-10 11:21:52','2026-05-10 11:21:52'),(9,88,'JE-00007','2026-05-10','SALES_INVOICE','Fattura cliente INV-2026-00006','CUSTOMER_INVOICE',13,'EUR',707.60,707.60,_binary '',NULL,'2026-05-10 16:07:32','2026-05-10 16:07:32'),(10,88,'JE-00008','2026-05-10','PURCHASE_INVOICE','Fattura fornitore SI-00007','SUPPLIER_INVOICE',7,'EUR',707.60,707.60,_binary '','Fattura fornitore generata da ricezione merci GR-00008','2026-05-10 16:15:01','2026-05-10 16:15:01'),(11,90,'JE-00009','2026-05-13','SALES_INVOICE','Fattura cliente INV-2026-00001','CUSTOMER_INVOICE',17,'EUR',707.60,707.60,_binary '',NULL,'2026-05-13 19:08:47','2026-05-13 19:08:47'),(12,90,'JE-00010','2026-05-13','SALES_INVOICE','Fattura cliente INV-2026-00002','CUSTOMER_INVOICE',18,'EUR',1464.00,1464.00,_binary '',NULL,'2026-05-13 19:33:07','2026-05-13 19:33:07'),(13,90,'JE-00011','2026-05-14','PURCHASE_INVOICE','Fattura fornitore SI-00008','SUPPLIER_INVOICE',8,'EUR',2171.60,2171.60,_binary '','Fattura fornitore generata da ricezione merci GR-00010','2026-05-13 22:22:30','2026-05-13 22:22:30'),(14,90,'JE-00012','2026-05-14','PURCHASE_INVOICE','Fattura fornitore SI-00009','SUPPLIER_INVOICE',9,'EUR',707.60,707.60,_binary '','Fattura fornitore generata da ricezione merci GR-00009','2026-05-13 22:24:36','2026-05-13 22:24:36'),(15,91,'JE-00013','2026-05-15','SALES_INVOICE','Fattura cliente INV-2026-00001','CUSTOMER_INVOICE',19,'EUR',707.60,707.60,_binary '',NULL,'2026-05-15 04:32:08','2026-05-15 04:32:08'),(16,91,'JE-00014','2026-05-15','PURCHASE_INVOICE','Fattura fornitore SI-00010','SUPPLIER_INVOICE',10,'EUR',7076.00,7076.00,_binary '','Fattura fornitore generata da ricezione merci GR-00011','2026-05-15 04:37:53','2026-05-15 04:37:53'),(17,91,'JE-00015','2026-05-15','CUSTOMER_RECEIPT','Incasso cliente su scadenza INV-2026-00001','PAYMENT_DUE',22,'EUR',707.60,707.60,_binary '','REGISTRO INCASSO','2026-05-15 05:05:46','2026-05-15 05:05:46'),(18,91,'JE-00016','2026-05-16','PURCHASE_INVOICE','Fattura fornitore SI-00011','SUPPLIER_INVOICE',11,'EUR',7783.60,7783.60,_binary '','Fattura fornitore generata da ricezione merci GR-00012','2026-05-15 22:19:10','2026-05-15 22:19:10'),(19,91,'JE-00017','2026-05-16','PURCHASE_INVOICE','Fattura fornitore SI-00012','SUPPLIER_INVOICE',12,'EUR',7076.00,7076.00,_binary '','Fattura fornitore generata da ricezione merci GR-00013','2026-05-15 23:02:40','2026-05-15 23:02:40'),(20,91,'JE-00018','2026-05-16','PURCHASE_INVOICE','Fattura fornitore SI-00013','SUPPLIER_INVOICE',13,'EUR',7076.00,7076.00,_binary '','Fattura fornitore generata da ricezione merci GR-00016','2026-05-16 00:13:25','2026-05-16 00:13:25'),(21,92,'JE-00019','2026-05-17','PURCHASE_INVOICE','Fattura fornitore SI-00014','SUPPLIER_INVOICE',14,'EUR',61.00,61.00,_binary '','Fattura fornitore generata da ricezione merci GR-00033','2026-05-16 23:07:28','2026-05-16 23:07:28'),(22,92,'JE-00020','2026-05-17','PURCHASE_INVOICE','Fattura fornitore SI-00015','SUPPLIER_INVOICE',15,'EUR',85.40,85.40,_binary '','Fattura fornitore generata da ricezione merci GR-00034','2026-05-16 23:08:30','2026-05-16 23:08:30'),(23,92,'JE-00021','2026-05-17','SALES_INVOICE','Fattura cliente INV-2026-00001','CUSTOMER_INVOICE',20,'EUR',146.40,146.40,_binary '',NULL,'2026-05-16 23:31:25','2026-05-16 23:31:25'),(24,92,'JE-00022','2026-05-17','SALES_INVOICE','Fattura cliente INV-2026-00002','CUSTOMER_INVOICE',21,'EUR',146.40,146.40,_binary '',NULL,'2026-05-16 23:34:41','2026-05-16 23:34:41'),(25,92,'JE-00023','2026-05-17','PURCHASE_INVOICE','Fattura fornitore SI-00016','SUPPLIER_INVOICE',16,'EUR',61.00,61.00,_binary '','Fattura fornitore generata da ricezione merci GR-00035','2026-05-17 00:02:40','2026-05-17 00:02:40'),(26,92,'JE-00024','2026-05-17','PURCHASE_INVOICE','Fattura fornitore SI-00017','SUPPLIER_INVOICE',17,'EUR',85.40,85.40,_binary '','Fattura fornitore generata da ricezione merci GR-00036','2026-05-17 00:03:42','2026-05-17 00:03:42'),(27,92,'JE-00025','2026-05-17','SALES_INVOICE','Fattura cliente INV-2026-00003','CUSTOMER_INVOICE',22,'EUR',146.40,146.40,_binary '',NULL,'2026-05-17 00:04:50','2026-05-17 00:04:50'),(28,92,'JE-00026','2026-05-17','PURCHASE_INVOICE','Fattura fornitore SI-00018','SUPPLIER_INVOICE',18,'EUR',85.40,85.40,_binary '','Fattura fornitore generata da ricezione merci GR-00038','2026-05-17 00:12:13','2026-05-17 00:12:13'),(29,92,'JE-00027','2026-05-17','SALES_INVOICE','Fattura cliente INV-2026-00004','CUSTOMER_INVOICE',23,'EUR',146.40,146.40,_binary '',NULL,'2026-05-17 00:13:09','2026-05-17 00:13:09'),(30,92,'JE-00028','2026-05-17','PURCHASE_INVOICE','Fattura fornitore SI-00019','SUPPLIER_INVOICE',19,'EUR',61.00,61.00,_binary '','Fattura fornitore generata da ricezione merci GR-00039','2026-05-17 09:44:15','2026-05-17 09:44:15'),(31,92,'JE-00029','2026-05-17','PURCHASE_INVOICE','Fattura fornitore SI-00020','SUPPLIER_INVOICE',20,'EUR',85.40,85.40,_binary '','Fattura fornitore generata da ricezione merci GR-00040','2026-05-17 09:45:07','2026-05-17 09:45:07'),(32,92,'JE-00030','2026-05-17','SALES_INVOICE','Fattura cliente INV-2026-00005','CUSTOMER_INVOICE',24,'EUR',146.40,146.40,_binary '',NULL,'2026-05-17 09:46:09','2026-05-17 09:46:09'),(33,92,'JE-00031','2026-05-18','SALES_INVOICE','Fattura cliente INV-2026-00006','CUSTOMER_INVOICE',25,'EUR',636.84,636.84,_binary '',NULL,'2026-05-18 12:14:14','2026-05-18 12:14:14'),(34,92,'JE-00032','2026-05-18','PURCHASE_INVOICE','Fattura fornitore SI-00021','SUPPLIER_INVOICE',21,'EUR',1273.68,1273.68,_binary '','Fattura fornitore generata da ricezione merci GR-00041','2026-05-18 12:24:41','2026-05-18 12:24:41'),(35,92,'JE-00033','2026-05-19','SALES_INVOICE','Fattura cliente INV-2026-00007','CUSTOMER_INVOICE',26,'EUR',146.40,146.40,_binary '',NULL,'2026-05-19 17:21:06','2026-05-19 17:21:06'),(36,93,'JE-00034','2026-05-21','SALES_INVOICE','Fattura cliente INV-2026-00001','CUSTOMER_INVOICE',27,'EUR',85.40,85.40,_binary '',NULL,'2026-05-21 18:11:05','2026-05-21 18:11:05'),(37,93,'JE-00035','2026-05-22','SALES_INVOICE','Fattura cliente INV-2026-00002','CUSTOMER_INVOICE',28,'EUR',719.80,719.80,_binary '',NULL,'2026-05-22 04:45:28','2026-05-22 04:45:28'),(38,93,'JE-00036','2026-05-23','PURCHASE_INVOICE','Fattura fornitore SI-00022','SUPPLIER_INVOICE',22,'EUR',719.80,719.80,_binary '','Fattura fornitore generata da ricezione merci GR-00052','2026-05-22 23:30:12','2026-05-22 23:30:12'),(39,93,'JE-00037','2026-05-23','PURCHASE_INVOICE','Fattura fornitore SI-00023','SUPPLIER_INVOICE',23,'EUR',1464.00,1464.00,_binary '','Fattura fornitore generata da ricezione merci GR-00053','2026-05-22 23:33:44','2026-05-22 23:33:44'),(40,93,'JE-00038','2026-05-23','SALES_INVOICE','Fattura cliente INV-2026-00003','CUSTOMER_INVOICE',29,'EUR',719.80,719.80,_binary '',NULL,'2026-05-23 08:01:37','2026-05-23 08:01:37'),(41,93,'JE-00039','2026-05-23','CUSTOMER_RECEIPT','Incasso scadenza INV-2026-00003 - Rif. 987YTR56UY','CUSTOMER_INVOICE',29,'EUR',719.80,719.80,_binary '','','2026-05-23 09:38:31','2026-05-23 09:38:31'),(42,93,'JE-00040','2026-05-23','SALES_INVOICE','Fattura cliente INV-2026-00004','CUSTOMER_INVOICE',30,'EUR',719.80,719.80,_binary '',NULL,'2026-05-23 13:31:21','2026-05-23 13:31:21'),(43,93,'JE-00041','2026-05-23','CUSTOMER_RECEIPT','Incasso scadenza INV-2026-00004 - Rif. 987YTR56UY','CUSTOMER_INVOICE',30,'EUR',12.00,12.00,_binary '','','2026-05-23 13:48:48','2026-05-23 13:48:48'),(44,93,'JE-00042','2026-05-23','CUSTOMER_RECEIPT','Incasso scadenza INV-2026-00004 - Rif. 987YTR56UY','CUSTOMER_INVOICE',30,'EUR',707.80,707.80,_binary '','','2026-05-23 13:51:48','2026-05-23 13:51:48'),(45,93,'JE-00043','2026-05-23','PURCHASE_INVOICE','Fattura fornitore SI-00024','SUPPLIER_INVOICE',24,'EUR',719.80,719.80,_binary '','Fattura fornitore generata da ricezione merci GR-00054','2026-05-23 13:54:07','2026-05-23 13:54:07'),(46,93,'JE-00044','2026-05-23','SUPPLIER_PAYMENT','Pagamento scadenza SI-00024 - Rif. 987YTR56UY','SUPPLIER_INVOICE',24,'EUR',100.00,100.00,_binary '','','2026-05-23 13:54:51','2026-05-23 13:54:51'),(47,93,'JE-00045','2026-05-23','SUPPLIER_PAYMENT','Pagamento scadenza SI-00024 - Rif. 987YTR56UY','SUPPLIER_INVOICE',24,'EUR',619.80,619.80,_binary '','','2026-05-23 13:56:50','2026-05-23 13:56:50'),(48,93,'JE-00046','2026-05-24','SALES_INVOICE','Fattura cliente INV-2026-00005','CUSTOMER_INVOICE',31,'EUR',719.80,719.80,_binary '',NULL,'2026-05-24 01:52:35','2026-05-24 01:52:35'),(49,93,'JE-00047','2026-05-24','CUSTOMER_RECEIPT','Incasso scadenza INV-2026-00005 - Rif. 987YTR56UY','CUSTOMER_INVOICE',31,'EUR',100.00,100.00,_binary '','','2026-05-24 02:34:35','2026-05-24 02:34:35'),(50,93,'JE-00048','2026-05-24','PURCHASE_INVOICE','Fattura fornitore SI-00025','SUPPLIER_INVOICE',25,'EUR',719.80,719.80,_binary '','Fattura fornitore generata da ricezione merci GR-00055','2026-05-24 04:12:48','2026-05-24 04:12:48'),(51,93,'JE-00049','2026-05-24','SUPPLIER_PAYMENT','Pagamento scadenza SI-00025 - Rif. 987YTR56UY','SUPPLIER_INVOICE',25,'EUR',100.00,100.00,_binary '','','2026-05-24 04:13:25','2026-05-24 04:13:25'),(52,93,'JE-00050','2026-05-25','SALES_INVOICE','Fattura cliente INV-2026-00006','CUSTOMER_INVOICE',32,'EUR',122.00,122.00,_binary '',NULL,'2026-05-25 06:06:38','2026-05-25 06:06:38'),(53,93,'JE-00051','2026-05-25','SALES_INVOICE','Fattura cliente INV-2026-00007','CUSTOMER_INVOICE',33,'EUR',183.00,183.00,_binary '',NULL,'2026-05-25 06:55:00','2026-05-25 06:55:00'),(54,93,'JE-00052','2026-05-25','SALES_INVOICE','Fattura cliente INV-2026-00008','CUSTOMER_INVOICE',34,'EUR',177.00,177.00,_binary '',NULL,'2026-05-25 07:21:21','2026-05-25 07:21:21'),(55,93,'JE-00053','2026-05-25','PURCHASE_INVOICE','Fattura fornitore SI-00026','SUPPLIER_INVOICE',26,'EUR',122.00,122.00,_binary '','Fattura fornitore generata da ricezione merci GR-00056','2026-05-25 07:26:54','2026-05-25 07:26:54'),(56,93,'JE-00054','2026-05-25','PURCHASE_INVOICE','Fattura fornitore SI-00027','SUPPLIER_INVOICE',27,'EUR',183.00,183.00,_binary '','Fattura fornitore generata da ricezione merci GR-00057','2026-05-25 07:29:06','2026-05-25 07:29:06'),(57,93,'JE-00055','2026-05-25','PURCHASE_INVOICE','Fattura fornitore SI-00028','SUPPLIER_INVOICE',28,'EUR',174.50,174.50,_binary '','Fattura fornitore generata da ricezione merci GR-00058','2026-05-25 07:30:50','2026-05-25 07:30:50'),(58,94,'JE-00056','2026-06-03','SALES_INVOICE','Fattura cliente INV-00001','CUSTOMER_INVOICE',35,'EUR',12.20,12.20,_binary '',NULL,'2026-06-03 04:43:52','2026-06-03 04:43:52'),(59,94,'JE-00057','2026-06-03','SALES_INVOICE','Fattura cliente INV-00002','CUSTOMER_INVOICE',36,'EUR',12.20,12.20,_binary '',NULL,'2026-06-03 04:45:11','2026-06-03 04:45:11'),(60,94,'JE-00058','2026-06-03','PURCHASE_INVOICE','Fattura fornitore SI-00001','SUPPLIER_INVOICE',29,'EUR',719.80,719.80,_binary '','Fattura fornitore generata da ricezione merci GR-00001','2026-06-03 11:04:40','2026-06-03 11:04:40'),(61,95,'JE-00059','2026-06-03','SALES_INVOICE','Fattura cliente INV-00001','CUSTOMER_INVOICE',37,'EUR',34.16,34.16,_binary '',NULL,'2026-06-03 12:17:06','2026-06-03 12:17:06'),(62,95,'JE-00060','2026-06-03','PURCHASE_INVOICE','Fattura fornitore SI-00001','SUPPLIER_INVOICE',30,'EUR',34.16,34.16,_binary '','Fattura fornitore generata da ricezione merci GR-00005','2026-06-03 15:14:13','2026-06-03 15:14:13'),(63,95,'JE-00061','2026-06-03','PURCHASE_INVOICE','Fattura fornitore SI-00002','SUPPLIER_INVOICE',31,'EUR',719.80,719.80,_binary '','Fattura fornitore generata da ricezione merci GR-00006','2026-06-03 15:21:25','2026-06-03 15:21:25'),(64,96,'JE-00062','2026-06-04','SALES_INVOICE','Fattura cliente INV-00001','CUSTOMER_INVOICE',38,'EUR',24.40,24.40,_binary '',NULL,'2026-06-04 06:11:13','2026-06-04 06:11:13'),(65,96,'JE-00063','2026-06-04','SALES_INVOICE','Fattura cliente INV-00002','CUSTOMER_INVOICE',39,'EUR',24.40,24.40,_binary '',NULL,'2026-06-04 06:11:39','2026-06-04 06:11:39');
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
) ENGINE=InnoDB AUTO_INCREMENT=180 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `journal_entry_line`
--

LOCK TABLES `journal_entry_line` WRITE;
/*!40000 ALTER TABLE `journal_entry_line` DISABLE KEYS */;
INSERT INTO `journal_entry_line` VALUES (5,88,3,1,3,'RIGA 1',100.00,0.00,'2026-05-10 00:53:28','2026-05-10 00:53:28'),(6,88,3,2,15,'RIGA 2',0.00,100.00,'2026-05-10 00:53:28','2026-05-10 00:53:28'),(7,88,4,1,4,'RIGA 1',580.00,0.00,'2026-05-10 10:12:18','2026-05-10 10:12:18'),(8,88,4,2,6,'RIGA 2',0.00,580.00,'2026-05-10 10:12:18','2026-05-10 10:12:18'),(9,88,5,1,4,'RIGA 1',780.00,0.00,'2026-05-10 10:19:39','2026-05-10 10:19:39'),(10,88,5,2,6,'RIGA 2',0.00,780.00,'2026-05-10 10:19:39','2026-05-10 10:19:39'),(11,88,6,1,4,'RIGA 1',707.60,0.00,'2026-05-10 10:45:02','2026-05-10 10:45:02'),(12,88,6,2,6,'RIGA 2',0.00,707.60,'2026-05-10 10:45:02','2026-05-10 10:45:02'),(13,88,7,1,4,'Incasso su banca',707.60,0.00,'2026-05-10 11:12:38','2026-05-10 11:12:38'),(14,88,7,2,6,'Chiusura credito cliente',0.00,707.60,'2026-05-10 11:12:38','2026-05-10 11:12:38'),(15,88,8,1,9,'Chiusura debito fornitore',707.60,0.00,'2026-05-10 11:21:52','2026-05-10 11:21:52'),(16,88,8,2,4,'Pagamento da banca',0.00,707.60,'2026-05-10 11:21:52','2026-05-10 11:21:52'),(17,88,9,1,6,'Rilevazione credito cliente',707.60,0.00,'2026-05-10 16:07:32','2026-05-10 16:07:32'),(18,88,9,2,15,'Ricavi da vendite',0.00,580.00,'2026-05-10 16:07:32','2026-05-10 16:07:32'),(19,88,9,3,11,'IVA a debito',0.00,127.60,'2026-05-10 16:07:32','2026-05-10 16:07:32'),(20,88,10,1,17,'Rilevazione costo acquisto',580.00,0.00,'2026-05-10 16:15:01','2026-05-10 16:15:01'),(21,88,10,2,12,'IVA a credito',127.60,0.00,'2026-05-10 16:15:01','2026-05-10 16:15:01'),(22,88,10,3,9,'Rilevazione debito fornitore',0.00,707.60,'2026-05-10 16:15:01','2026-05-10 16:15:01'),(23,90,11,1,6,'Rilevazione credito cliente',707.60,0.00,'2026-05-13 19:08:47','2026-05-13 19:08:47'),(24,90,11,2,15,'Ricavi da vendite',0.00,580.00,'2026-05-13 19:08:47','2026-05-13 19:08:47'),(25,90,11,3,11,'IVA a debito',0.00,127.60,'2026-05-13 19:08:47','2026-05-13 19:08:47'),(26,90,12,1,6,'Rilevazione credito cliente',1464.00,0.00,'2026-05-13 19:33:07','2026-05-13 19:33:07'),(27,90,12,2,15,'Ricavi da vendite',0.00,1200.00,'2026-05-13 19:33:07','2026-05-13 19:33:07'),(28,90,12,3,11,'IVA a debito',0.00,264.00,'2026-05-13 19:33:07','2026-05-13 19:33:07'),(29,90,13,1,17,'Rilevazione costo acquisto',1780.00,0.00,'2026-05-13 22:22:30','2026-05-13 22:22:30'),(30,90,13,2,12,'IVA a credito',391.60,0.00,'2026-05-13 22:22:30','2026-05-13 22:22:30'),(31,90,13,3,9,'Rilevazione debito fornitore',0.00,2171.60,'2026-05-13 22:22:30','2026-05-13 22:22:30'),(32,90,14,1,17,'Rilevazione costo acquisto',580.00,0.00,'2026-05-13 22:24:36','2026-05-13 22:24:36'),(33,90,14,2,12,'IVA a credito',127.60,0.00,'2026-05-13 22:24:36','2026-05-13 22:24:36'),(34,90,14,3,9,'Rilevazione debito fornitore',0.00,707.60,'2026-05-13 22:24:36','2026-05-13 22:24:36'),(35,91,15,1,25,'Rilevazione credito cliente',707.60,0.00,'2026-05-15 04:32:08','2026-05-15 04:32:08'),(36,91,15,2,34,'Ricavi da vendite',0.00,580.00,'2026-05-15 04:32:08','2026-05-15 04:32:08'),(37,91,15,3,30,'IVA a debito',0.00,127.60,'2026-05-15 04:32:08','2026-05-15 04:32:08'),(38,91,16,1,36,'Rilevazione costo acquisto',5800.00,0.00,'2026-05-15 04:37:53','2026-05-15 04:37:53'),(39,91,16,2,31,'IVA a credito',1276.00,0.00,'2026-05-15 04:37:53','2026-05-15 04:37:53'),(40,91,16,3,28,'Rilevazione debito fornitore',0.00,7076.00,'2026-05-15 04:37:53','2026-05-15 04:37:53'),(41,91,17,1,23,'Incasso su banca',707.60,0.00,'2026-05-15 05:05:46','2026-05-15 05:05:46'),(42,91,17,2,25,'Chiusura credito cliente',0.00,707.60,'2026-05-15 05:05:46','2026-05-15 05:05:46'),(43,91,18,1,36,'Rilevazione costo acquisto',6380.00,0.00,'2026-05-15 22:19:10','2026-05-15 22:19:10'),(44,91,18,2,31,'IVA a credito',1403.60,0.00,'2026-05-15 22:19:10','2026-05-15 22:19:10'),(45,91,18,3,28,'Rilevazione debito fornitore',0.00,7783.60,'2026-05-15 22:19:10','2026-05-15 22:19:10'),(46,91,19,1,36,'Rilevazione costo acquisto',5800.00,0.00,'2026-05-15 23:02:40','2026-05-15 23:02:40'),(47,91,19,2,31,'IVA a credito',1276.00,0.00,'2026-05-15 23:02:40','2026-05-15 23:02:40'),(48,91,19,3,28,'Rilevazione debito fornitore',0.00,7076.00,'2026-05-15 23:02:40','2026-05-15 23:02:40'),(49,91,20,1,36,'Rilevazione costo acquisto',5800.00,0.00,'2026-05-16 00:13:25','2026-05-16 00:13:25'),(50,91,20,2,31,'IVA a credito',1276.00,0.00,'2026-05-16 00:13:25','2026-05-16 00:13:25'),(51,91,20,3,28,'Rilevazione debito fornitore',0.00,7076.00,'2026-05-16 00:13:25','2026-05-16 00:13:25'),(52,92,21,1,55,'Rilevazione costo acquisto',50.00,0.00,'2026-05-16 23:07:28','2026-05-16 23:07:28'),(53,92,21,2,50,'IVA a credito',11.00,0.00,'2026-05-16 23:07:28','2026-05-16 23:07:28'),(54,92,21,3,47,'Rilevazione debito fornitore',0.00,61.00,'2026-05-16 23:07:28','2026-05-16 23:07:28'),(55,92,22,1,55,'Rilevazione costo acquisto',70.00,0.00,'2026-05-16 23:08:30','2026-05-16 23:08:30'),(56,92,22,2,50,'IVA a credito',15.40,0.00,'2026-05-16 23:08:30','2026-05-16 23:08:30'),(57,92,22,3,47,'Rilevazione debito fornitore',0.00,85.40,'2026-05-16 23:08:30','2026-05-16 23:08:30'),(58,92,23,1,44,'Rilevazione credito cliente',146.40,0.00,'2026-05-16 23:31:25','2026-05-16 23:31:25'),(59,92,23,2,53,'Ricavi da vendite',0.00,120.00,'2026-05-16 23:31:25','2026-05-16 23:31:25'),(60,92,23,3,49,'IVA a debito',0.00,26.40,'2026-05-16 23:31:25','2026-05-16 23:31:25'),(61,92,24,1,44,'Rilevazione credito cliente',146.40,0.00,'2026-05-16 23:34:41','2026-05-16 23:34:41'),(62,92,24,2,53,'Ricavi da vendite',0.00,120.00,'2026-05-16 23:34:41','2026-05-16 23:34:41'),(63,92,24,3,49,'IVA a debito',0.00,26.40,'2026-05-16 23:34:41','2026-05-16 23:34:41'),(64,92,25,1,55,'Rilevazione costo acquisto',50.00,0.00,'2026-05-17 00:02:40','2026-05-17 00:02:40'),(65,92,25,2,50,'IVA a credito',11.00,0.00,'2026-05-17 00:02:40','2026-05-17 00:02:40'),(66,92,25,3,47,'Rilevazione debito fornitore',0.00,61.00,'2026-05-17 00:02:40','2026-05-17 00:02:40'),(67,92,26,1,55,'Rilevazione costo acquisto',70.00,0.00,'2026-05-17 00:03:42','2026-05-17 00:03:42'),(68,92,26,2,50,'IVA a credito',15.40,0.00,'2026-05-17 00:03:42','2026-05-17 00:03:42'),(69,92,26,3,47,'Rilevazione debito fornitore',0.00,85.40,'2026-05-17 00:03:42','2026-05-17 00:03:42'),(70,92,27,1,44,'Rilevazione credito cliente',146.40,0.00,'2026-05-17 00:04:50','2026-05-17 00:04:50'),(71,92,27,2,53,'Ricavi da vendite',0.00,120.00,'2026-05-17 00:04:50','2026-05-17 00:04:50'),(72,92,27,3,49,'IVA a debito',0.00,26.40,'2026-05-17 00:04:50','2026-05-17 00:04:50'),(73,92,28,1,55,'Rilevazione costo acquisto',70.00,0.00,'2026-05-17 00:12:13','2026-05-17 00:12:13'),(74,92,28,2,50,'IVA a credito',15.40,0.00,'2026-05-17 00:12:13','2026-05-17 00:12:13'),(75,92,28,3,47,'Rilevazione debito fornitore',0.00,85.40,'2026-05-17 00:12:13','2026-05-17 00:12:13'),(76,92,29,1,44,'Rilevazione credito cliente',146.40,0.00,'2026-05-17 00:13:09','2026-05-17 00:13:09'),(77,92,29,2,53,'Ricavi da vendite',0.00,120.00,'2026-05-17 00:13:09','2026-05-17 00:13:09'),(78,92,29,3,49,'IVA a debito',0.00,26.40,'2026-05-17 00:13:09','2026-05-17 00:13:09'),(79,92,30,1,55,'Rilevazione costo acquisto',50.00,0.00,'2026-05-17 09:44:15','2026-05-17 09:44:15'),(80,92,30,2,50,'IVA a credito',11.00,0.00,'2026-05-17 09:44:15','2026-05-17 09:44:15'),(81,92,30,3,47,'Rilevazione debito fornitore',0.00,61.00,'2026-05-17 09:44:15','2026-05-17 09:44:15'),(82,92,31,1,55,'Rilevazione costo acquisto',70.00,0.00,'2026-05-17 09:45:07','2026-05-17 09:45:07'),(83,92,31,2,50,'IVA a credito',15.40,0.00,'2026-05-17 09:45:07','2026-05-17 09:45:07'),(84,92,31,3,47,'Rilevazione debito fornitore',0.00,85.40,'2026-05-17 09:45:07','2026-05-17 09:45:07'),(85,92,32,1,44,'Rilevazione credito cliente',146.40,0.00,'2026-05-17 09:46:09','2026-05-17 09:46:09'),(86,92,32,2,53,'Ricavi da vendite',0.00,120.00,'2026-05-17 09:46:09','2026-05-17 09:46:09'),(87,92,32,3,49,'IVA a debito',0.00,26.40,'2026-05-17 09:46:09','2026-05-17 09:46:09'),(88,92,33,1,44,'Rilevazione credito cliente',636.84,0.00,'2026-05-18 12:14:14','2026-05-18 12:14:14'),(89,92,33,2,53,'Ricavi da vendite',0.00,522.00,'2026-05-18 12:14:14','2026-05-18 12:14:14'),(90,92,33,3,49,'IVA a debito',0.00,114.84,'2026-05-18 12:14:14','2026-05-18 12:14:14'),(91,92,34,1,55,'Rilevazione costo acquisto',1044.00,0.00,'2026-05-18 12:24:41','2026-05-18 12:24:41'),(92,92,34,2,50,'IVA a credito',229.68,0.00,'2026-05-18 12:24:41','2026-05-18 12:24:41'),(93,92,34,3,47,'Rilevazione debito fornitore',0.00,1273.68,'2026-05-18 12:24:41','2026-05-18 12:24:41'),(94,92,35,1,44,'Rilevazione credito cliente',146.40,0.00,'2026-05-19 17:21:06','2026-05-19 17:21:06'),(95,92,35,2,53,'Ricavi da vendite',0.00,120.00,'2026-05-19 17:21:06','2026-05-19 17:21:06'),(96,92,35,3,49,'IVA a debito',0.00,26.40,'2026-05-19 17:21:06','2026-05-19 17:21:06'),(97,93,36,1,63,'Rilevazione credito cliente',85.40,0.00,'2026-05-21 18:11:05','2026-05-21 18:11:05'),(98,93,36,2,72,'Ricavi da vendite',0.00,70.00,'2026-05-21 18:11:05','2026-05-21 18:11:05'),(99,93,36,3,68,'IVA a debito',0.00,15.40,'2026-05-21 18:11:05','2026-05-21 18:11:05'),(100,93,37,1,63,'Rilevazione credito cliente',719.80,0.00,'2026-05-22 04:45:28','2026-05-22 04:45:28'),(101,93,37,2,72,'Ricavi da vendite',0.00,590.00,'2026-05-22 04:45:28','2026-05-22 04:45:28'),(102,93,37,3,68,'IVA a debito',0.00,129.80,'2026-05-22 04:45:28','2026-05-22 04:45:28'),(103,93,38,1,74,'Rilevazione costo acquisto',590.00,0.00,'2026-05-22 23:30:12','2026-05-22 23:30:12'),(104,93,38,2,69,'IVA a credito',129.80,0.00,'2026-05-22 23:30:12','2026-05-22 23:30:12'),(105,93,38,3,66,'Rilevazione debito fornitore',0.00,719.80,'2026-05-22 23:30:12','2026-05-22 23:30:12'),(106,93,39,1,74,'Rilevazione costo acquisto',1200.00,0.00,'2026-05-22 23:33:44','2026-05-22 23:33:44'),(107,93,39,2,69,'IVA a credito',264.00,0.00,'2026-05-22 23:33:44','2026-05-22 23:33:44'),(108,93,39,3,66,'Rilevazione debito fornitore',0.00,1464.00,'2026-05-22 23:33:44','2026-05-22 23:33:44'),(109,93,40,1,63,'Rilevazione credito cliente',719.80,0.00,'2026-05-23 08:01:37','2026-05-23 08:01:37'),(110,93,40,2,72,'Ricavi da vendite',0.00,590.00,'2026-05-23 08:01:37','2026-05-23 08:01:37'),(111,93,40,3,68,'IVA a debito',0.00,129.80,'2026-05-23 08:01:37','2026-05-23 08:01:37'),(112,93,41,1,63,'Incasso scadenza INV-2026-00003 - Rif. 987YTR56UY',719.80,0.00,'2026-05-23 09:38:31','2026-05-23 09:38:31'),(113,93,41,2,63,'Incasso scadenza INV-2026-00003 - Rif. 987YTR56UY',0.00,719.80,'2026-05-23 09:38:31','2026-05-23 09:38:31'),(114,93,42,1,63,'Rilevazione credito cliente',719.80,0.00,'2026-05-23 13:31:21','2026-05-23 13:31:21'),(115,93,42,2,72,'Ricavi da vendite',0.00,590.00,'2026-05-23 13:31:21','2026-05-23 13:31:21'),(116,93,42,3,68,'IVA a debito',0.00,129.80,'2026-05-23 13:31:21','2026-05-23 13:31:21'),(117,93,43,1,63,'Incasso scadenza INV-2026-00004 - Rif. 987YTR56UY',12.00,0.00,'2026-05-23 13:48:51','2026-05-23 13:48:51'),(118,93,43,2,63,'Incasso scadenza INV-2026-00004 - Rif. 987YTR56UY',0.00,12.00,'2026-05-23 13:48:54','2026-05-23 13:48:54'),(119,93,44,1,63,'Incasso scadenza INV-2026-00004 - Rif. 987YTR56UY',707.80,0.00,'2026-05-23 13:51:48','2026-05-23 13:51:48'),(120,93,44,2,63,'Incasso scadenza INV-2026-00004 - Rif. 987YTR56UY',0.00,707.80,'2026-05-23 13:51:48','2026-05-23 13:51:48'),(121,93,45,1,74,'Rilevazione costo acquisto',590.00,0.00,'2026-05-23 13:54:07','2026-05-23 13:54:07'),(122,93,45,2,69,'IVA a credito',129.80,0.00,'2026-05-23 13:54:07','2026-05-23 13:54:07'),(123,93,45,3,66,'Rilevazione debito fornitore',0.00,719.80,'2026-05-23 13:54:07','2026-05-23 13:54:07'),(124,93,46,1,66,'Pagamento scadenza SI-00024 - Rif. 987YTR56UY',100.00,0.00,'2026-05-23 13:54:51','2026-05-23 13:54:51'),(125,93,46,2,66,'Pagamento scadenza SI-00024 - Rif. 987YTR56UY',0.00,100.00,'2026-05-23 13:54:51','2026-05-23 13:54:51'),(126,93,47,1,66,'Pagamento scadenza SI-00024 - Rif. 987YTR56UY',619.80,0.00,'2026-05-23 13:56:50','2026-05-23 13:56:50'),(127,93,47,2,66,'Pagamento scadenza SI-00024 - Rif. 987YTR56UY',0.00,619.80,'2026-05-23 13:56:50','2026-05-23 13:56:50'),(128,93,48,1,63,'Rilevazione credito cliente',719.80,0.00,'2026-05-24 01:52:35','2026-05-24 01:52:35'),(129,93,48,2,72,'Ricavi da vendite',0.00,590.00,'2026-05-24 01:52:35','2026-05-24 01:52:35'),(130,93,48,3,68,'IVA a debito',0.00,129.80,'2026-05-24 01:52:35','2026-05-24 01:52:35'),(131,93,49,1,61,'Incasso scadenza INV-2026-00005 - Rif. 987YTR56UY',100.00,0.00,'2026-05-24 02:34:35','2026-05-24 02:34:35'),(132,93,49,2,63,'Incasso scadenza INV-2026-00005 - Rif. 987YTR56UY',0.00,100.00,'2026-05-24 02:34:35','2026-05-24 02:34:35'),(133,93,50,1,74,'Rilevazione costo acquisto',590.00,0.00,'2026-05-24 04:12:48','2026-05-24 04:12:48'),(134,93,50,2,69,'IVA a credito',129.80,0.00,'2026-05-24 04:12:48','2026-05-24 04:12:48'),(135,93,50,3,66,'Rilevazione debito fornitore',0.00,719.80,'2026-05-24 04:12:48','2026-05-24 04:12:48'),(136,93,51,1,66,'Pagamento scadenza SI-00025 - Rif. 987YTR56UY',100.00,0.00,'2026-05-24 04:13:25','2026-05-24 04:13:25'),(137,93,51,2,60,'Pagamento scadenza SI-00025 - Rif. 987YTR56UY',0.00,100.00,'2026-05-24 04:13:25','2026-05-24 04:13:25'),(138,93,52,1,63,'Rilevazione credito cliente',122.00,0.00,'2026-05-25 06:06:38','2026-05-25 06:06:38'),(139,93,52,2,72,'Ricavi da vendite',0.00,100.00,'2026-05-25 06:06:38','2026-05-25 06:06:38'),(140,93,52,3,68,'IVA a debito',0.00,22.00,'2026-05-25 06:06:38','2026-05-25 06:06:38'),(141,93,53,1,63,'Rilevazione credito cliente',183.00,0.00,'2026-05-25 06:55:00','2026-05-25 06:55:00'),(142,93,53,2,72,'Ricavi da vendite',0.00,150.00,'2026-05-25 06:55:00','2026-05-25 06:55:00'),(143,93,53,3,68,'IVA a debito',0.00,33.00,'2026-05-25 06:55:00','2026-05-25 06:55:00'),(144,93,54,1,63,'Rilevazione credito cliente',177.00,0.00,'2026-05-25 07:21:21','2026-05-25 07:21:21'),(145,93,54,2,72,'Ricavi da vendite',0.00,150.00,'2026-05-25 07:21:21','2026-05-25 07:21:21'),(146,93,54,3,68,'IVA a debito',0.00,27.00,'2026-05-25 07:21:21','2026-05-25 07:21:21'),(147,93,55,1,74,'Rilevazione costo acquisto',100.00,0.00,'2026-05-25 07:26:54','2026-05-25 07:26:54'),(148,93,55,2,69,'IVA a credito',22.00,0.00,'2026-05-25 07:26:54','2026-05-25 07:26:54'),(149,93,55,3,66,'Rilevazione debito fornitore',0.00,122.00,'2026-05-25 07:26:54','2026-05-25 07:26:54'),(150,93,56,1,74,'Rilevazione costo acquisto',150.00,0.00,'2026-05-25 07:29:06','2026-05-25 07:29:06'),(151,93,56,2,69,'IVA a credito',33.00,0.00,'2026-05-25 07:29:06','2026-05-25 07:29:06'),(152,93,56,3,66,'Rilevazione debito fornitore',0.00,183.00,'2026-05-25 07:29:06','2026-05-25 07:29:06'),(153,93,57,1,74,'Rilevazione costo acquisto',150.00,0.00,'2026-05-25 07:30:50','2026-05-25 07:30:50'),(154,93,57,2,69,'IVA a credito',24.50,0.00,'2026-05-25 07:30:50','2026-05-25 07:30:50'),(155,93,57,3,66,'Rilevazione debito fornitore',0.00,174.50,'2026-05-25 07:30:50','2026-05-25 07:30:50'),(156,94,58,1,82,'Rilevazione credito cliente',12.20,0.00,'2026-06-03 04:43:52','2026-06-03 04:43:52'),(157,94,58,2,91,'Ricavi da vendite',0.00,10.00,'2026-06-03 04:43:52','2026-06-03 04:43:52'),(158,94,58,3,87,'IVA a debito',0.00,2.20,'2026-06-03 04:43:52','2026-06-03 04:43:52'),(159,94,59,1,82,'Rilevazione credito cliente',12.20,0.00,'2026-06-03 04:45:11','2026-06-03 04:45:11'),(160,94,59,2,91,'Ricavi da vendite',0.00,10.00,'2026-06-03 04:45:11','2026-06-03 04:45:11'),(161,94,59,3,87,'IVA a debito',0.00,2.20,'2026-06-03 04:45:11','2026-06-03 04:45:11'),(162,94,60,1,93,'Rilevazione costo acquisto',590.00,0.00,'2026-06-03 11:04:40','2026-06-03 11:04:40'),(163,94,60,2,88,'IVA a credito',129.80,0.00,'2026-06-03 11:04:40','2026-06-03 11:04:40'),(164,94,60,3,85,'Rilevazione debito fornitore',0.00,719.80,'2026-06-03 11:04:40','2026-06-03 11:04:40'),(165,95,61,1,101,'Rilevazione credito cliente',34.16,0.00,'2026-06-03 12:17:06','2026-06-03 12:17:06'),(166,95,61,2,110,'Ricavi da vendite',0.00,28.00,'2026-06-03 12:17:06','2026-06-03 12:17:06'),(167,95,61,3,106,'IVA a debito',0.00,6.16,'2026-06-03 12:17:06','2026-06-03 12:17:06'),(168,95,62,1,112,'Rilevazione costo acquisto',28.00,0.00,'2026-06-03 15:14:13','2026-06-03 15:14:13'),(169,95,62,2,107,'IVA a credito',6.16,0.00,'2026-06-03 15:14:13','2026-06-03 15:14:13'),(170,95,62,3,104,'Rilevazione debito fornitore',0.00,34.16,'2026-06-03 15:14:13','2026-06-03 15:14:13'),(171,95,63,1,112,'Rilevazione costo acquisto',590.00,0.00,'2026-06-03 15:21:25','2026-06-03 15:21:25'),(172,95,63,2,107,'IVA a credito',129.80,0.00,'2026-06-03 15:21:25','2026-06-03 15:21:25'),(173,95,63,3,104,'Rilevazione debito fornitore',0.00,719.80,'2026-06-03 15:21:25','2026-06-03 15:21:25'),(174,96,64,1,120,'Rilevazione credito cliente',24.40,0.00,'2026-06-04 06:11:13','2026-06-04 06:11:13'),(175,96,64,2,129,'Ricavi da vendite',0.00,20.00,'2026-06-04 06:11:13','2026-06-04 06:11:13'),(176,96,64,3,125,'IVA a debito',0.00,4.40,'2026-06-04 06:11:13','2026-06-04 06:11:13'),(177,96,65,1,120,'Rilevazione credito cliente',24.40,0.00,'2026-06-04 06:11:39','2026-06-04 06:11:39'),(178,96,65,2,129,'Ricavi da vendite',0.00,20.00,'2026-06-04 06:11:39','2026-06-04 06:11:39'),(179,96,65,3,125,'IVA a debito',0.00,4.40,'2026-06-04 06:11:39','2026-06-04 06:11:39');
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
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_due`
--

LOCK TABLES `payment_due` WRITE;
/*!40000 ALTER TABLE `payment_due` DISABLE KEYS */;
INSERT INTO `payment_due` VALUES (1,87,'RECEIVABLE','CUSTOMER',75,'INV-2026-00001','2026-05-08','2026-06-07','CUSTOMER_INVOICE',5,'EUR',3538.00,3538.00,0.00,'PAID','Scadenza generata da fattura cliente','2026-05-08 14:18:31','2026-05-09 05:46:21'),(2,87,'PAYABLE','SUPPLIER',1,'SI-00003','2026-05-08','2026-06-07','SUPPLIER_INVOICE',3,'EUR',7074.58,7074.58,0.00,'PAID','Scadenza generata da fattura fornitore','2026-05-08 14:21:23','2026-05-08 17:11:02'),(3,87,'RECEIVABLE','CUSTOMER',76,'INV-2026-00002','2026-05-08','2026-06-07','CUSTOMER_INVOICE',6,'EUR',122.00,122.00,0.00,'PAID','Scadenza generata da fattura cliente','2026-05-08 17:02:21','2026-05-08 17:05:37'),(4,87,'RECEIVABLE','CUSTOMER',76,'INV-2026-00003','2026-05-09','2026-06-08','CUSTOMER_INVOICE',7,'EUR',707.60,0.00,707.60,'OPEN','Scadenza generata da fattura cliente','2026-05-09 05:34:18','2026-05-09 05:34:18'),(5,87,'PAYABLE','SUPPLIER',1,'SI-00004','2026-05-09','2026-06-08','SUPPLIER_INVOICE',4,'EUR',707.60,707.60,0.00,'PAID','Scadenza generata da fattura fornitore','2026-05-09 05:35:11','2026-05-09 05:48:06'),(6,88,'RECEIVABLE','CUSTOMER',75,'INV-2026-00001','2026-05-09','2026-06-08','CUSTOMER_INVOICE',8,'EUR',707.60,707.60,0.00,'PAID','Scadenza generata da fattura cliente','2026-05-09 11:34:24','2026-05-09 19:39:22'),(7,88,'RECEIVABLE','CUSTOMER',75,'INV-2026-00002','2026-05-09','2026-06-08','CUSTOMER_INVOICE',9,'EUR',707.60,707.60,0.00,'PAID','Scadenza generata da fattura cliente','2026-05-09 14:02:58','2026-05-09 20:17:16'),(8,88,'PAYABLE','SUPPLIER',1,'SI-00005','2026-05-09','2026-06-08','SUPPLIER_INVOICE',5,'EUR',1415.20,1400.00,15.20,'PARTIALLY_PAID','Scadenza generata da fattura fornitore','2026-05-09 20:15:04','2026-05-09 20:16:15'),(9,88,'RECEIVABLE','CUSTOMER',76,'INV-2026-00003','2026-05-10','2026-06-09','CUSTOMER_INVOICE',10,'EUR',707.60,706.70,0.90,'PARTIALLY_PAID','Scadenza generata da fattura cliente','2026-05-10 10:04:21','2026-05-10 10:16:38'),(10,88,'RECEIVABLE','CUSTOMER',75,'INV-2026-00004','2026-05-10','2026-06-09','CUSTOMER_INVOICE',11,'EUR',707.60,707.60,0.00,'PAID','Scadenza generata da fattura cliente','2026-05-10 10:36:14','2026-05-10 10:38:04'),(11,88,'RECEIVABLE','CUSTOMER',76,'INV-2026-00005','2026-05-10','2026-06-09','CUSTOMER_INVOICE',12,'EUR',707.60,707.60,0.00,'PAID','Scadenza generata da fattura cliente','2026-05-10 11:11:52','2026-05-10 11:12:38'),(12,88,'PAYABLE','SUPPLIER',1,'SI-00006','2026-05-10','2026-06-09','SUPPLIER_INVOICE',6,'EUR',707.60,707.60,0.00,'PAID','Scadenza generata da fattura fornitore','2026-05-10 11:20:53','2026-05-10 11:21:52'),(13,88,'RECEIVABLE','CUSTOMER',75,'INV-2026-00006','2026-05-10','2026-06-09','CUSTOMER_INVOICE',13,'EUR',707.60,0.00,707.60,'OPEN','Scadenza generata da fattura cliente','2026-05-10 16:06:40','2026-05-10 16:06:40'),(14,88,'PAYABLE','SUPPLIER',1,'SI-00007','2026-05-10','2026-06-09','SUPPLIER_INVOICE',7,'EUR',707.60,0.00,707.60,'OPEN','Scadenza generata da fattura fornitore','2026-05-10 16:15:01','2026-05-10 16:15:01'),(18,90,'RECEIVABLE','CUSTOMER',75,'INV-2026-00001','2026-05-13','2026-06-12','CUSTOMER_INVOICE',17,'EUR',707.60,0.00,707.60,'OPEN','Scadenza generata da fattura cliente','2026-05-13 19:08:47','2026-05-13 19:08:47'),(19,90,'RECEIVABLE','CUSTOMER',76,'INV-2026-00002','2026-05-13','2026-06-12','CUSTOMER_INVOICE',18,'EUR',1464.00,0.00,1464.00,'OPEN','Scadenza generata da fattura cliente','2026-05-13 19:33:07','2026-05-13 19:33:07'),(20,90,'PAYABLE','SUPPLIER',2,'SI-00008','2026-05-14','2026-06-13','SUPPLIER_INVOICE',8,'EUR',2171.60,0.00,2171.60,'OPEN','Scadenza generata da fattura fornitore','2026-05-13 22:22:29','2026-05-13 22:22:29'),(21,90,'PAYABLE','SUPPLIER',1,'SI-00009','2026-05-14','2026-06-13','SUPPLIER_INVOICE',9,'EUR',707.60,0.00,707.60,'OPEN','Scadenza generata da fattura fornitore','2026-05-13 22:24:36','2026-05-13 22:24:36'),(22,91,'RECEIVABLE','CUSTOMER',76,'INV-2026-00001','2026-05-15','2026-06-14','CUSTOMER_INVOICE',19,'EUR',707.60,707.60,0.00,'PAID','Scadenza generata da fattura cliente','2026-05-15 04:32:08','2026-05-15 05:05:46'),(23,91,'PAYABLE','SUPPLIER',2,'SI-00010','2026-05-15','2026-06-14','SUPPLIER_INVOICE',10,'EUR',7076.00,0.00,7076.00,'OPEN','Scadenza generata da fattura fornitore','2026-05-15 04:37:53','2026-05-15 04:37:53'),(24,91,'PAYABLE','SUPPLIER',2,'SI-00011','2026-05-16','2026-06-15','SUPPLIER_INVOICE',11,'EUR',7783.60,0.00,7783.60,'OPEN','Scadenza generata da fattura fornitore','2026-05-15 22:19:10','2026-05-15 22:19:10'),(25,91,'PAYABLE','SUPPLIER',2,'SI-00012','2026-05-16','2026-06-15','SUPPLIER_INVOICE',12,'EUR',7076.00,0.00,7076.00,'OPEN','Scadenza generata da fattura fornitore','2026-05-15 23:02:40','2026-05-15 23:02:40'),(26,91,'PAYABLE','SUPPLIER',2,'SI-00013','2026-05-16','2026-06-15','SUPPLIER_INVOICE',13,'EUR',7076.00,0.00,7076.00,'OPEN','Scadenza generata da fattura fornitore','2026-05-16 00:13:25','2026-05-16 00:13:25'),(27,92,'PAYABLE','SUPPLIER',2,'SI-00014','2026-05-17','2026-06-16','SUPPLIER_INVOICE',14,'EUR',61.00,0.00,61.00,'OPEN','Scadenza generata da fattura fornitore','2026-05-16 23:07:28','2026-05-16 23:07:28'),(28,92,'PAYABLE','SUPPLIER',2,'SI-00015','2026-05-17','2026-06-16','SUPPLIER_INVOICE',15,'EUR',85.40,0.00,85.40,'OPEN','Scadenza generata da fattura fornitore','2026-05-16 23:08:30','2026-05-16 23:08:30'),(29,92,'RECEIVABLE','CUSTOMER',76,'INV-2026-00001','2026-05-17','2026-06-16','CUSTOMER_INVOICE',20,'EUR',146.40,0.00,146.40,'OPEN','Scadenza generata da fattura cliente','2026-05-16 23:31:20','2026-05-16 23:31:20'),(30,92,'RECEIVABLE','CUSTOMER',76,'INV-2026-00002','2026-05-17','2026-06-16','CUSTOMER_INVOICE',21,'EUR',146.40,0.00,146.40,'OPEN','Scadenza generata da fattura cliente','2026-05-16 23:34:35','2026-05-16 23:34:35'),(31,92,'PAYABLE','SUPPLIER',2,'SI-00016','2026-05-17','2026-06-16','SUPPLIER_INVOICE',16,'EUR',61.00,0.00,61.00,'OPEN','Scadenza generata da fattura fornitore','2026-05-17 00:02:40','2026-05-17 00:02:40'),(32,92,'PAYABLE','SUPPLIER',2,'SI-00017','2026-05-17','2026-06-16','SUPPLIER_INVOICE',17,'EUR',85.40,0.00,85.40,'OPEN','Scadenza generata da fattura fornitore','2026-05-17 00:03:42','2026-05-17 00:03:42'),(33,92,'RECEIVABLE','CUSTOMER',76,'INV-2026-00003','2026-05-17','2026-06-16','CUSTOMER_INVOICE',22,'EUR',146.40,0.00,146.40,'OPEN','Scadenza generata da fattura cliente','2026-05-17 00:04:50','2026-05-17 00:04:50'),(34,92,'PAYABLE','SUPPLIER',2,'SI-00018','2026-05-17','2026-06-16','SUPPLIER_INVOICE',18,'EUR',85.40,0.00,85.40,'OPEN','Scadenza generata da fattura fornitore','2026-05-17 00:12:13','2026-05-17 00:12:13'),(35,92,'RECEIVABLE','CUSTOMER',76,'INV-2026-00004','2026-05-17','2026-06-16','CUSTOMER_INVOICE',23,'EUR',146.40,0.00,146.40,'OPEN','Scadenza generata da fattura cliente','2026-05-17 00:13:09','2026-05-17 00:13:09'),(36,92,'PAYABLE','SUPPLIER',2,'SI-00019','2026-05-17','2026-06-16','SUPPLIER_INVOICE',19,'EUR',61.00,0.00,61.00,'OPEN','Scadenza generata da fattura fornitore','2026-05-17 09:44:15','2026-05-17 09:44:15'),(37,92,'PAYABLE','SUPPLIER',2,'SI-00020','2026-05-17','2026-06-16','SUPPLIER_INVOICE',20,'EUR',85.40,0.00,85.40,'OPEN','Scadenza generata da fattura fornitore','2026-05-17 09:45:06','2026-05-17 09:45:06'),(38,92,'RECEIVABLE','CUSTOMER',76,'INV-2026-00005','2026-05-17','2026-06-16','CUSTOMER_INVOICE',24,'EUR',146.40,0.00,146.40,'OPEN','Scadenza generata da fattura cliente','2026-05-17 09:46:09','2026-05-17 09:46:09'),(39,92,'RECEIVABLE','CUSTOMER',76,'INV-2026-00006','2026-05-18','2026-06-17','CUSTOMER_INVOICE',25,'EUR',636.84,0.00,636.84,'OPEN','Scadenza generata da fattura cliente','2026-05-18 12:14:14','2026-05-18 12:14:14'),(40,92,'PAYABLE','SUPPLIER',2,'SI-00021','2026-05-18','2026-06-17','SUPPLIER_INVOICE',21,'EUR',1273.68,0.00,1273.68,'OPEN','Scadenza generata da fattura fornitore','2026-05-18 12:24:41','2026-05-18 12:24:41'),(41,92,'RECEIVABLE','CUSTOMER',76,'INV-2026-00007','2026-05-19','2026-06-18','CUSTOMER_INVOICE',26,'EUR',146.40,0.00,146.40,'OPEN','Scadenza generata da fattura cliente','2026-05-19 17:21:06','2026-05-19 17:21:06'),(42,93,'RECEIVABLE','CUSTOMER',76,'INV-2026-00001','2026-05-21','2026-06-20','CUSTOMER_INVOICE',27,'EUR',85.40,85.40,0.00,'PAID','Scadenza generata da fattura cliente','2026-05-21 18:11:05','2026-05-22 23:24:10'),(43,93,'RECEIVABLE','CUSTOMER',76,'INV-2026-00002','2026-05-22','2026-06-21','CUSTOMER_INVOICE',28,'EUR',719.80,719.80,0.00,'PAID','Scadenza generata da fattura cliente','2026-05-22 04:45:28','2026-05-22 22:31:12'),(44,93,'PAYABLE','SUPPLIER',2,'SI-00022','2026-05-23','2026-06-22','SUPPLIER_INVOICE',22,'EUR',719.80,719.80,0.00,'PAID','Scadenza generata da fattura fornitore','2026-05-22 23:30:12','2026-05-22 23:31:24'),(45,93,'PAYABLE','SUPPLIER',2,'SI-00023','2026-05-23','2026-06-22','SUPPLIER_INVOICE',23,'EUR',1464.00,1464.00,0.00,'PAID','Scadenza generata da fattura fornitore','2026-05-22 23:33:44','2026-05-22 23:34:56'),(46,93,'RECEIVABLE','CUSTOMER',76,'INV-2026-00003','2026-05-23','2026-06-22','CUSTOMER_INVOICE',29,'EUR',719.80,719.80,0.00,'PAID','Scadenza generata da fattura cliente','2026-05-23 08:01:37','2026-05-23 09:38:31'),(47,93,'RECEIVABLE','CUSTOMER',76,'INV-2026-00004','2026-05-23','2026-06-22','CUSTOMER_INVOICE',30,'EUR',719.80,719.80,0.00,'PAID','Scadenza generata da fattura cliente','2026-05-23 13:31:21','2026-05-23 13:51:48'),(48,93,'PAYABLE','SUPPLIER',2,'SI-00024','2026-05-23','2026-06-22','SUPPLIER_INVOICE',24,'EUR',719.80,719.80,0.00,'PAID','Scadenza generata da fattura fornitore','2026-05-23 13:54:07','2026-05-23 13:56:50'),(49,93,'RECEIVABLE','CUSTOMER',76,'INV-2026-00005','2026-05-24','2026-06-23','CUSTOMER_INVOICE',31,'EUR',719.80,100.00,619.80,'PARTIALLY_PAID','Scadenza generata da fattura cliente','2026-05-24 01:52:35','2026-05-24 02:34:36'),(50,93,'PAYABLE','SUPPLIER',2,'SI-00025','2026-05-24','2026-06-23','SUPPLIER_INVOICE',25,'EUR',719.80,100.00,619.80,'PARTIALLY_PAID','Scadenza generata da fattura fornitore','2026-05-24 04:12:48','2026-05-24 04:13:25'),(51,93,'RECEIVABLE','CUSTOMER',75,'INV-2026-00006','2026-05-25','2026-06-24','CUSTOMER_INVOICE',32,'EUR',122.00,0.00,122.00,'OPEN','Scadenza generata da fattura cliente','2026-05-25 06:06:38','2026-05-25 06:06:38'),(52,93,'RECEIVABLE','CUSTOMER',75,'INV-2026-00007','2026-05-25','2026-06-24','CUSTOMER_INVOICE',33,'EUR',183.00,0.00,183.00,'OPEN','Scadenza generata da fattura cliente','2026-05-25 06:55:00','2026-05-25 06:55:00'),(53,93,'RECEIVABLE','CUSTOMER',76,'INV-2026-00008','2026-05-25','2026-06-24','CUSTOMER_INVOICE',34,'EUR',177.00,0.00,177.00,'OPEN','Scadenza generata da fattura cliente','2026-05-25 07:21:21','2026-05-25 07:21:21'),(54,93,'PAYABLE','SUPPLIER',2,'SI-00026','2026-05-25','2026-06-24','SUPPLIER_INVOICE',26,'EUR',122.00,0.00,122.00,'OPEN','Scadenza generata da fattura fornitore','2026-05-25 07:26:54','2026-05-25 07:26:54'),(55,93,'PAYABLE','SUPPLIER',2,'SI-00027','2026-05-25','2026-06-24','SUPPLIER_INVOICE',27,'EUR',183.00,0.00,183.00,'OPEN','Scadenza generata da fattura fornitore','2026-05-25 07:29:06','2026-05-25 07:29:06'),(56,93,'PAYABLE','SUPPLIER',2,'SI-00028','2026-05-25','2026-06-24','SUPPLIER_INVOICE',28,'EUR',174.50,0.00,174.50,'OPEN','Scadenza generata da fattura fornitore','2026-05-25 07:30:50','2026-05-25 07:30:50'),(57,94,'RECEIVABLE','CUSTOMER',76,'INV-00001','2026-06-03','2026-07-03','CUSTOMER_INVOICE',35,'EUR',12.20,0.00,12.20,'OPEN','Scadenza generata da fattura cliente','2026-06-03 04:43:52','2026-06-03 04:43:52'),(58,94,'RECEIVABLE','CUSTOMER',75,'INV-00002','2026-06-03','2026-07-03','CUSTOMER_INVOICE',36,'EUR',12.20,0.00,12.20,'OPEN','Scadenza generata da fattura cliente','2026-06-03 04:45:11','2026-06-03 04:45:11'),(59,94,'PAYABLE','SUPPLIER',2,'SI-00001','2026-06-03','2026-07-03','SUPPLIER_INVOICE',29,'EUR',719.80,0.00,719.80,'OPEN','Scadenza generata da fattura fornitore','2026-06-03 11:04:40','2026-06-03 11:04:40'),(60,95,'RECEIVABLE','CUSTOMER',76,'INV-00001','2026-06-03','2026-07-03','CUSTOMER_INVOICE',37,'EUR',34.16,0.00,34.16,'OPEN','Scadenza generata da fattura cliente','2026-06-03 12:17:06','2026-06-03 12:17:06'),(61,95,'PAYABLE','SUPPLIER',2,'SI-00001','2026-06-03','2026-07-03','SUPPLIER_INVOICE',30,'EUR',34.16,0.00,34.16,'OPEN','Scadenza generata da fattura fornitore','2026-06-03 15:14:12','2026-06-03 15:14:12'),(62,95,'PAYABLE','SUPPLIER',1,'SI-00002','2026-06-03','2026-07-03','SUPPLIER_INVOICE',31,'EUR',719.80,0.00,719.80,'OPEN','Scadenza generata da fattura fornitore','2026-06-03 15:21:25','2026-06-03 15:21:25'),(63,96,'RECEIVABLE','CUSTOMER',76,'INV-00001','2026-06-04','2026-07-04','CUSTOMER_INVOICE',38,'EUR',24.40,0.00,24.40,'OPEN','Scadenza generata da fattura cliente','2026-06-04 06:11:13','2026-06-04 06:11:13'),(64,96,'RECEIVABLE','CUSTOMER',76,'INV-00002','2026-06-04','2026-07-04','CUSTOMER_INVOICE',39,'EUR',24.40,0.00,24.40,'OPEN','Scadenza generata da fattura cliente','2026-06-04 06:11:39','2026-06-04 06:11:39');
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
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_due_transaction`
--

LOCK TABLES `payment_due_transaction` WRITE;
/*!40000 ALTER TABLE `payment_due_transaction` DISABLE KEYS */;
INSERT INTO `payment_due_transaction` VALUES (1,87,3,'2026-05-08',82.00,'RECEIPT','PROVA INCASSO','2026-05-08 17:04:36','2026-05-08 17:04:36'),(2,87,3,'2026-05-22',40.00,'RECEIPT','PROVA INCASSO','2026-05-08 17:05:37','2026-05-08 17:05:37'),(3,87,2,'2026-05-08',7014.58,'PAYMENT','PROVA PAGAMENTO FATTURA A FORNITOR','2026-05-08 17:10:04','2026-05-08 17:10:04'),(4,87,2,'2026-05-22',60.00,'PAYMENT','PAGAMENTO A FORNITORE','2026-05-08 17:11:02','2026-05-08 17:11:02'),(5,87,1,'2026-05-09',3478.00,'RECEIPT','prova prima nota incasso','2026-05-09 05:45:45','2026-05-09 05:45:45'),(6,87,1,'2026-05-23',60.00,'RECEIPT','PROVA PRIMA NOTA INCASSO','2026-05-09 05:46:21','2026-05-09 05:46:21'),(7,87,5,'2026-05-09',707.56,'PAYMENT','PROVA PRIMA NOTA PAGAMENTO A FORNITORE','2026-05-09 05:47:42','2026-05-09 05:47:42'),(8,87,5,'2026-05-09',0.04,'PAYMENT','','2026-05-09 05:48:06','2026-05-09 05:48:06'),(9,88,6,'2026-05-09',700.00,'RECEIPT','PROVA DASHBOARD','2026-05-09 19:38:13','2026-05-09 19:38:13'),(10,88,6,'2026-05-09',7.60,'RECEIPT','PROVA DASHBOARD','2026-05-09 19:39:22','2026-05-09 19:39:22'),(11,88,8,'2026-05-09',1400.00,'PAYMENT','PROVA DASHBOARD','2026-05-09 20:16:15','2026-05-09 20:16:15'),(12,88,7,'2026-05-09',707.60,'RECEIPT','PROVA DASHBOARD','2026-05-09 20:17:16','2026-05-09 20:17:16'),(13,88,9,'2026-05-10',706.70,'RECEIPT','PROVA INCASSO','2026-05-10 10:16:38','2026-05-10 10:16:38'),(14,88,10,'2026-05-10',707.60,'RECEIPT','PROVA JOURNAL','2026-05-10 10:38:04','2026-05-10 10:38:04'),(15,88,11,'2026-05-10',707.60,'RECEIPT','PROVA LIBRO JOURNAL','2026-05-10 11:12:38','2026-05-10 11:12:38'),(16,88,12,'2026-05-10',707.60,'PAYMENT','PROVA JOURNAL','2026-05-10 11:21:52','2026-05-10 11:21:52'),(17,91,22,'2026-05-15',707.60,'RECEIPT','REGISTRO INCASSO','2026-05-15 05:05:46','2026-05-15 05:05:46');
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
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_transaction`
--

LOCK TABLES `payment_transaction` WRITE;
/*!40000 ALTER TABLE `payment_transaction` DISABLE KEYS */;
INSERT INTO `payment_transaction` VALUES (1,93,'IN','CUSTOMER',76,43,'CUSTOMER_INVOICE',28,'2026-05-23',719.80,'Assegno','987YTR56UY','','2026-05-22 22:31:12','2026-05-22 22:31:12',NULL),(2,93,'IN','CUSTOMER',76,42,'CUSTOMER_INVOICE',27,'2026-05-23',40.00,'Assegno','987YTR56UY','','2026-05-22 23:23:26','2026-05-22 23:23:26',NULL),(3,93,'IN','CUSTOMER',76,42,'CUSTOMER_INVOICE',27,'2026-05-23',45.40,'Assegno','987YTR56UY','','2026-05-22 23:24:10','2026-05-22 23:24:10',NULL),(4,93,'OUT','SUPPLIER',2,44,'SUPPLIER_INVOICE',22,'2026-05-23',719.80,'Bonifico','987YTR56UY','','2026-05-22 23:31:24','2026-05-22 23:31:24',NULL),(5,93,'OUT','SUPPLIER',2,45,'SUPPLIER_INVOICE',23,'2026-05-23',1000.00,'Bonifico','987YTR56UY','','2026-05-22 23:34:14','2026-05-22 23:34:14',NULL),(6,93,'OUT','SUPPLIER',2,45,'SUPPLIER_INVOICE',23,'2026-05-23',464.00,'Bonifico','987YTR56UY','','2026-05-22 23:34:56','2026-05-22 23:34:56',NULL),(15,93,'IN','CUSTOMER',76,46,'CUSTOMER_INVOICE',29,'2026-05-23',719.80,'Bonifico','987YTR56UY','','2026-05-23 09:38:31','2026-05-23 09:38:31',41),(19,93,'IN','CUSTOMER',76,47,'CUSTOMER_INVOICE',30,'2026-05-23',12.00,'','987YTR56UY','','2026-05-23 13:47:56','2026-05-23 13:48:58',43),(20,93,'IN','CUSTOMER',76,47,'CUSTOMER_INVOICE',30,'2026-05-23',707.80,'','987YTR56UY','','2026-05-23 13:51:44','2026-05-23 13:51:48',44),(21,93,'OUT','SUPPLIER',2,48,'SUPPLIER_INVOICE',24,'2026-05-23',100.00,'Bonifico','987YTR56UY','','2026-05-23 13:54:48','2026-05-23 13:54:51',46),(22,93,'OUT','SUPPLIER',2,48,'SUPPLIER_INVOICE',24,'2026-05-23',619.80,'Bonifico','987YTR56UY','','2026-05-23 13:56:32','2026-05-23 13:56:50',47),(27,93,'IN','CUSTOMER',76,49,'CUSTOMER_INVOICE',31,'2026-05-24',100.00,'Bonifico','987YTR56UY','','2026-05-24 02:33:41','2026-05-24 02:34:36',49),(28,93,'OUT','SUPPLIER',2,50,'SUPPLIER_INVOICE',25,'2026-05-24',100.00,'Bonifico','987YTR56UY','','2026-05-24 04:13:25','2026-05-24 04:13:25',51);
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
INSERT INTO `persistent_logins` VALUES ('neri-spa|admin@neri.it','ZaIviIdwULt0GN2k35YzwQ==','SLg73gtXI/plfGCwINYOZQ==','2026-06-04 10:52:15');
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
  KEY `idx_purchase_order_tenant_status` (`tenant_id`,`status`),
  KEY `FK4traogu3jriq9u7e8rvm86k7i` (`supplier_id`),
  CONSTRAINT `FK4traogu3jriq9u7e8rvm86k7i` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchase_order`
--

LOCK TABLES `purchase_order` WRITE;
/*!40000 ALTER TABLE `purchase_order` DISABLE KEYS */;
INSERT INTO `purchase_order` VALUES (1,85,'PO-00001','2026-05-07','2026-05-22',1,'CONFIRMED','EUR','ORDINE 10 PEZZI FORNO ELETTRICO',5800.00,1276.00,7076.00,'2026-05-07 16:59:48','2026-05-07 17:05:37'),(2,87,'PO-00002','2026-05-08','2026-05-15',1,'CONFIRMED','EUR','ORDINE A FORNITORE DI 50 FORNI ELETTRICI',23200.00,5104.00,28304.00,'2026-05-08 01:02:46','2026-05-08 01:03:15'),(3,87,'PO-00003','2026-05-08','2026-05-15',1,'CONFIRMED','EUR','PROVA',4640.00,1020.80,5660.80,'2026-05-08 03:05:48','2026-05-08 03:06:22'),(4,87,'PO-00004','2026-05-08','2026-05-15',1,'CONFIRMED','EUR','PROVA ADUE',5798.84,1275.74,7074.58,'2026-05-08 14:20:52','2026-05-08 14:21:10'),(5,87,'PO-00005','2026-05-09','2026-05-16',1,'CONFIRMED','EUR','PROVA PRIMA NOTA FORNITORE',580.00,127.60,707.60,'2026-05-09 05:35:04','2026-05-09 05:35:04'),(6,88,'PO-00006','2026-05-09','2026-05-16',1,'CONFIRMED','EUR','PROVA DASHBOARD',1160.00,255.20,1415.20,'2026-05-09 20:14:41','2026-05-09 20:14:41'),(7,88,'PO-00007','2026-05-10','2026-05-17',1,'CONFIRMED','EUR','PAGAMENTO A FORNITORE',580.00,127.60,707.60,'2026-05-10 11:20:17','2026-05-10 11:20:17'),(8,88,'PO-00008','2026-05-10','2026-05-17',1,'CONFIRMED','EUR','PROVA CONTABILE',580.00,127.60,707.60,'2026-05-10 16:14:47','2026-05-10 16:14:47'),(9,90,'PO-00009','2026-05-13','2026-05-22',2,'CONFIRMED','EUR','PROVA INSERIMENTO',1780.00,391.60,2171.60,'2026-05-13 21:20:21','2026-05-13 21:20:56'),(10,90,'PO-00010','2026-05-15','2026-05-28',1,'CONFIRMED','EUR','',580.00,127.60,707.60,'2026-05-13 21:22:04','2026-05-13 21:23:44'),(11,91,'PO-00011','2026-05-15','2026-05-22',2,'CONFIRMED','EUR','ORDINAZIONE DI 10 FORNI ELETTRICI',5800.00,1276.00,7076.00,'2026-05-15 04:37:25','2026-05-15 04:37:25'),(12,91,'PO-00012','2026-05-16','2026-05-23',2,'CONFIRMED','EUR','PROVA MOVIMENTAZIONE MAGAZZINO',6380.00,1403.60,7783.60,'2026-05-15 22:18:38','2026-05-15 22:18:38'),(13,91,'PO-00013','2026-05-16','2026-05-23',2,'CONFIRMED','EUR','PROVA MOVIMENTAZIONE',5800.00,1276.00,7076.00,'2026-05-15 23:02:31','2026-05-15 23:02:31'),(14,91,'PO-00014','2026-05-14','2026-05-23',1,'CONFIRMED','EUR','',580.00,127.60,707.60,'2026-05-15 23:04:21','2026-05-15 23:04:21'),(15,91,'PO-00015','2026-05-20','2026-05-23',2,'CONFIRMED','EUR','',5800.00,1276.00,7076.00,'2026-05-15 23:36:32','2026-05-15 23:36:32'),(16,91,'PO-00016','2026-05-16','2026-05-23',2,'CONFIRMED','EUR','PROVA MOVIMENTAZIONE',58000.00,12760.00,70760.00,'2026-05-16 00:13:17','2026-05-16 00:13:17'),(17,91,'PO-00017','2026-05-16','2026-05-23',2,'CONFIRMED','EUR','CARICAMENTO 12 PEZZI',50.00,11.00,61.00,'2026-05-16 19:27:34','2026-05-16 19:27:34'),(18,91,'PO-00018','2026-05-16','2026-05-23',2,'CONFIRMED','EUR','',70.00,15.40,85.40,'2026-05-16 19:50:26','2026-05-16 19:50:26'),(19,91,'PO-00019','2026-05-16','2026-05-23',2,'CONFIRMED','EUR','',50.00,11.00,61.00,'2026-05-16 20:18:55','2026-05-16 20:18:55'),(20,91,'PO-00020','2026-05-16','2026-05-23',2,'CONFIRMED','EUR','',50.00,11.00,61.00,'2026-05-16 20:23:07','2026-05-16 20:23:24'),(21,91,'PO-00021','2026-05-16','2026-05-23',2,'CONFIRMED','EUR','',50.00,11.00,61.00,'2026-05-16 20:26:04','2026-05-16 20:26:04'),(22,91,'PO-00022','2026-05-16','2026-05-23',2,'CONFIRMED','EUR','',50.00,11.00,61.00,'2026-05-16 20:33:59','2026-05-16 20:33:59'),(23,91,'PO-00023','2026-05-16','2026-05-23',2,'CONFIRMED','EUR','',50.00,11.00,61.00,'2026-05-16 20:35:43','2026-05-16 20:35:53'),(24,91,'PO-00024','2026-05-16','2026-05-23',2,'CONFIRMED','EUR','',50.00,11.00,61.00,'2026-05-16 20:46:32','2026-05-16 20:46:32'),(25,91,'PO-00025','2026-05-16','2026-05-23',2,'CONFIRMED','EUR','',580.00,127.60,707.60,'2026-05-16 20:49:21','2026-05-16 20:49:21'),(26,91,'PO-00026','2026-05-16','2026-05-23',2,'CONFIRMED','EUR','',580.00,127.60,707.60,'2026-05-16 20:54:50','2026-05-16 20:54:50'),(27,91,'PO-00027','2026-05-16','2026-05-23',2,'CONFIRMED','EUR','',60.00,13.20,73.20,'2026-05-16 20:56:13','2026-05-16 20:56:13'),(28,91,'PO-00028','2026-05-16','2026-05-23',2,'CONFIRMED','EUR','',580.00,127.60,707.60,'2026-05-16 21:07:40','2026-05-16 21:07:40'),(29,91,'PO-00029','2026-05-16','2026-05-23',2,'CONFIRMED','EUR','',580.00,127.60,707.60,'2026-05-16 21:24:13','2026-05-16 21:24:13'),(30,91,'PO-00030','2026-05-16','2026-05-23',1,'CONFIRMED','EUR','',580.00,127.60,707.60,'2026-05-16 21:31:16','2026-05-16 21:31:16'),(31,91,'PO-00031','2026-05-16','2026-05-23',2,'CONFIRMED','EUR','',580.00,127.60,707.60,'2026-05-16 21:34:17','2026-05-16 21:34:17'),(32,92,'PO-00032','2026-05-16','2026-05-23',2,'CONFIRMED','EUR','CARICO 10 PEZZI A 5',50.00,11.00,61.00,'2026-05-16 21:50:40','2026-05-16 21:50:40'),(33,92,'PO-00033','2026-05-16','2026-05-23',2,'CONFIRMED','EUR','',50.00,11.00,61.00,'2026-05-16 21:54:35','2026-05-16 21:54:45'),(34,92,'PO-00034','2026-05-17','2026-05-24',2,'CONFIRMED','EUR','',580.00,127.60,707.60,'2026-05-16 22:05:26','2026-05-16 22:05:26'),(35,92,'PO-00035','2026-05-17','2026-05-24',2,'CONFIRMED','EUR','CARICO 10 PEZZI DA 5',50.00,11.00,61.00,'2026-05-16 22:35:41','2026-05-16 22:35:41'),(36,92,'PO-00036','2026-05-17','2026-05-24',2,'CONFIRMED','EUR','',580.00,127.60,707.60,'2026-05-16 22:38:42','2026-05-16 22:38:52'),(37,92,'PO-00037','2026-05-17','2026-05-24',2,'CONFIRMED','EUR','CARICO 10 PEZZI A 5',50.00,11.00,61.00,'2026-05-16 23:06:44','2026-05-16 23:06:44'),(38,92,'PO-00038','2026-05-17','2026-05-24',2,'CONFIRMED','EUR','CARICO 10 PEZZI A 7',70.00,15.40,85.40,'2026-05-16 23:08:24','2026-05-16 23:08:24'),(39,92,'PO-00039','2026-05-17','2026-05-24',2,'CONFIRMED','EUR','LIFO',50.00,11.00,61.00,'2026-05-17 00:02:29','2026-05-17 00:02:29'),(40,92,'PO-00040','2026-05-17','2026-05-24',2,'CONFIRMED','EUR','LIFO',70.00,15.40,85.40,'2026-05-17 00:03:35','2026-05-17 00:03:35'),(41,92,'PO-00041','2026-05-17','2026-05-24',2,'CONFIRMED','EUR','LIFO',50.00,11.00,61.00,'2026-05-17 00:11:15','2026-05-17 00:11:15'),(42,92,'PO-00042','2026-05-17','2026-05-24',2,'CONFIRMED','EUR','LIFO',70.00,15.40,85.40,'2026-05-17 00:12:08','2026-05-17 00:12:08'),(43,92,'PO-00043','2026-05-17','2026-05-24',2,'CONFIRMED','EUR','LIFO',50.00,11.00,61.00,'2026-05-17 09:44:07','2026-05-17 09:44:07'),(44,92,'PO-00044','2026-05-17','2026-05-24',2,'CONFIRMED','EUR','LIFO',70.00,15.40,85.40,'2026-05-17 09:45:00','2026-05-17 09:45:00'),(45,92,'PO-00045','2026-05-18','2026-05-25',2,'CONFIRMED','EUR','',1044.00,229.68,1273.68,'2026-05-18 12:23:27','2026-05-18 12:23:27'),(46,92,'PO-00046','2026-05-19','2026-05-26',2,'CONFIRMED','EUR','',50.00,11.00,61.00,'2026-05-19 16:47:02','2026-05-19 16:47:02'),(47,92,'PO-00047','2026-05-19','2026-05-26',2,'CONFIRMED','EUR','',70.00,15.40,85.40,'2026-05-19 16:48:16','2026-05-19 16:48:16'),(48,92,'PO-00048','2026-05-19','2026-05-26',2,'CONFIRMED','EUR','',50.00,11.00,61.00,'2026-05-19 17:17:06','2026-05-19 17:17:18'),(49,92,'PO-00049','2026-05-19','2026-05-26',2,'CONFIRMED','EUR','',70.00,15.40,85.40,'2026-05-19 17:18:22','2026-05-19 17:18:22'),(50,92,'PO-00050','2026-05-19','2026-05-26',2,'CONFIRMED','EUR','',50.00,11.00,61.00,'2026-05-19 17:25:17','2026-05-19 17:25:17'),(51,92,'PO-00051','2026-05-19','2026-05-26',2,'CONFIRMED','EUR','',70.00,15.40,85.40,'2026-05-19 17:25:51','2026-05-19 17:26:20'),(52,93,'PO-00052','2026-05-20','2026-05-27',2,'CONFIRMED','EUR','',50.00,11.00,61.00,'2026-05-20 09:53:26','2026-05-20 09:53:26'),(53,93,'PO-00053','2026-05-20','2026-05-27',2,'CONFIRMED','EUR','',50.00,11.00,61.00,'2026-05-20 09:56:08','2026-05-20 09:56:08'),(54,93,'PO-00054','2026-05-20','2026-05-27',2,'CONFIRMED','EUR','',70.00,15.40,85.40,'2026-05-20 09:56:45','2026-05-20 09:56:45'),(55,93,'PO-00055','2026-05-20','2026-05-27',2,'CONFIRMED','EUR','',50.00,11.00,61.00,'2026-05-20 10:13:45','2026-05-20 10:13:45'),(56,93,'PO-00056','2026-05-20','2026-05-27',2,'CONFIRMED','EUR','',70.00,15.40,85.40,'2026-05-20 10:14:29','2026-05-20 10:14:29'),(57,93,'PO-00057','2026-05-22','2026-05-29',2,'DRAFT','EUR','',0.00,0.00,0.00,'2026-05-22 03:09:50','2026-05-22 03:09:50'),(58,93,'PO-00058','2026-05-22','2026-05-29',2,'DRAFT','EUR','',10.00,2.20,12.20,'2026-05-22 03:25:58','2026-05-22 03:26:38'),(59,93,'PO-00059','2026-05-22','2026-05-29',2,'DRAFT','','',0.00,0.00,0.00,'2026-05-22 04:19:54','2026-05-22 04:19:54'),(60,93,'PO-00060','2026-05-22','2026-05-29',2,'DRAFT','EUR','',0.00,0.00,0.00,'2026-05-22 04:27:03','2026-05-22 04:27:03'),(61,93,'PO-00061','2026-05-22','2026-05-29',2,'DRAFT','EUR','',-590.00,-129.80,-719.80,'2026-05-22 04:27:41','2026-05-22 04:27:41'),(62,93,'PO-00062','2026-05-22','2026-05-29',2,'DRAFT','EUR','',0.00,0.00,0.00,'2026-05-22 04:28:17','2026-05-22 04:28:17'),(63,93,'PO-00063','2026-05-22',NULL,2,'DRAFT','EUR','',0.00,0.00,0.00,'2026-05-22 04:32:07','2026-05-22 04:32:07'),(64,93,'PO-00064','2026-05-22',NULL,2,'DRAFT','EUR','',590.00,129.80,719.80,'2026-05-22 04:33:02','2026-05-22 04:33:02'),(65,93,'PO-00065','2026-05-23','2026-05-30',2,'CONFIRMED','EUR','',590.00,129.80,719.80,'2026-05-22 23:29:41','2026-05-22 23:29:41'),(66,93,'PO-00066','2026-05-23','2026-05-30',2,'CONFIRMED','EUR','',1200.00,264.00,1464.00,'2026-05-22 23:33:23','2026-05-22 23:33:23'),(67,93,'PO-00067','2026-05-23','2026-05-30',2,'CONFIRMED','EUR','',590.00,129.80,719.80,'2026-05-23 13:53:51','2026-05-23 13:53:51'),(68,93,'PO-00068','2026-05-24','2026-05-31',2,'CONFIRMED','EUR','',590.00,129.80,719.80,'2026-05-24 04:12:43','2026-05-24 04:12:43'),(69,93,'PO-00069','2026-06-04','2026-06-24',2,'CONFIRMED','EUR','',100.00,22.00,122.00,'2026-05-25 07:26:30','2026-05-25 07:26:42'),(70,93,'PO-00070','2026-05-25','2026-06-01',2,'CONFIRMED','EUR','',150.00,33.00,183.00,'2026-05-25 07:29:00','2026-05-25 07:29:00'),(71,93,'PO-00071','2026-05-25','2026-06-01',2,'CONFIRMED','EUR','',150.00,24.50,174.50,'2026-05-25 07:30:35','2026-05-25 07:30:35'),(72,94,'PO-00001','2026-06-03','2026-06-10',2,'CONFIRMED','EUR','',590.00,129.80,719.80,'2026-06-03 11:04:02','2026-06-03 11:04:02'),(73,95,'PO-00001','2026-06-03','2026-06-10',2,'CONFIRMED','EUR','',28.00,6.16,34.16,'2026-06-03 14:19:15','2026-06-03 14:37:15'),(74,95,'PO-00002','2026-06-03','2026-06-10',1,'CONFIRMED','EUR','',590.00,129.80,719.80,'2026-06-03 15:20:58','2026-06-03 15:20:58'),(75,96,'PO-00001','2026-06-03','2026-06-10',2,'CONFIRMED','EUR','',10.00,2.20,12.20,'2026-06-03 19:54:43','2026-06-03 19:54:43'),(76,96,'PO-00002','2026-06-03','2026-06-10',1,'CONFIRMED','EUR','PROVA VALORIZZAZIONE E GIACENZA.',50.00,11.00,61.00,'2026-06-03 20:04:40','2026-06-03 20:04:40'),(77,96,'PO-00003','2026-06-03','2026-06-10',2,'CONFIRMED','EUR','',90.00,19.80,109.80,'2026-06-03 20:31:03','2026-06-03 20:31:03');
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
) ENGINE=InnoDB AUTO_INCREMENT=99 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchase_order_line`
--

LOCK TABLES `purchase_order_line` WRITE;
/*!40000 ALTER TABLE `purchase_order_line` DISABLE KEYS */;
INSERT INTO `purchase_order_line` VALUES (2,85,1,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,580.00,0.00,22.00,5800.00,1276.00,7076.00,'2026-05-07 17:05:37','2026-05-07 17:05:37'),(4,87,2,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',50.000,580.00,20.00,22.00,23200.00,5104.00,28304.00,'2026-05-08 01:03:15','2026-05-08 01:03:15'),(6,87,3,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,580.00,20.00,22.00,4640.00,1020.80,5660.80,'2026-05-08 03:06:22','2026-05-08 03:06:22'),(8,87,4,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',9.998,580.00,0.00,22.00,5798.84,1275.74,7074.58,'2026-05-08 14:21:10','2026-05-08 14:21:10'),(9,87,5,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,127.60,707.60,'2026-05-09 05:35:04','2026-05-09 05:35:04'),(10,88,6,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',2.000,580.00,0.00,22.00,1160.00,255.20,1415.20,'2026-05-09 20:14:41','2026-05-09 20:14:41'),(11,88,7,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,127.60,707.60,'2026-05-10 11:20:17','2026-05-10 11:20:17'),(12,88,8,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,127.60,707.60,'2026-05-10 16:14:47','2026-05-10 16:14:47'),(15,90,9,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,127.60,707.60,'2026-05-13 21:20:56','2026-05-13 21:20:56'),(16,90,9,2,1,'Modulo CRM',1.000,1200.00,0.00,22.00,1200.00,264.00,1464.00,'2026-05-13 21:20:56','2026-05-13 21:20:56'),(18,90,10,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,127.60,707.60,'2026-05-13 21:23:44','2026-05-13 21:23:44'),(19,91,11,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,580.00,0.00,22.00,5800.00,1276.00,7076.00,'2026-05-15 04:37:25','2026-05-15 04:37:25'),(20,91,12,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',11.000,580.00,0.00,22.00,6380.00,1403.60,7783.60,'2026-05-15 22:18:38','2026-05-15 22:18:38'),(21,91,13,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,580.00,0.00,22.00,5800.00,1276.00,7076.00,'2026-05-15 23:02:31','2026-05-15 23:02:31'),(22,91,14,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,127.60,707.60,'2026-05-15 23:04:21','2026-05-15 23:04:21'),(23,91,15,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,580.00,0.00,22.00,5800.00,1276.00,7076.00,'2026-05-15 23:36:32','2026-05-15 23:36:32'),(24,91,16,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',100.000,580.00,0.00,22.00,58000.00,12760.00,70760.00,'2026-05-16 00:13:17','2026-05-16 00:13:17'),(25,91,17,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-16 19:27:34','2026-05-16 19:27:34'),(26,91,18,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,7.00,0.00,22.00,70.00,15.40,85.40,'2026-05-16 19:50:26','2026-05-16 19:50:26'),(27,91,19,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-16 20:18:55','2026-05-16 20:18:55'),(29,91,20,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-16 20:23:24','2026-05-16 20:23:24'),(30,91,21,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-16 20:26:04','2026-05-16 20:26:04'),(31,91,22,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-16 20:33:59','2026-05-16 20:33:59'),(33,91,23,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-16 20:35:53','2026-05-16 20:35:53'),(34,91,24,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-16 20:46:32','2026-05-16 20:46:32'),(35,91,25,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,127.60,707.60,'2026-05-16 20:49:21','2026-05-16 20:49:21'),(36,91,26,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,127.60,707.60,'2026-05-16 20:54:50','2026-05-16 20:54:50'),(37,91,27,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,5.00,0.00,22.00,60.00,13.20,73.20,'2026-05-16 20:56:13','2026-05-16 20:56:13'),(38,91,28,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,127.60,707.60,'2026-05-16 21:07:40','2026-05-16 21:07:40'),(39,91,29,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,127.60,707.60,'2026-05-16 21:24:13','2026-05-16 21:24:13'),(40,91,30,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,127.60,707.60,'2026-05-16 21:31:16','2026-05-16 21:31:16'),(41,91,31,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,127.60,707.60,'2026-05-16 21:34:17','2026-05-16 21:34:17'),(42,92,32,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-16 21:50:40','2026-05-16 21:50:40'),(44,92,33,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-16 21:54:45','2026-05-16 21:54:45'),(45,92,34,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,127.60,707.60,'2026-05-16 22:05:26','2026-05-16 22:05:26'),(46,92,35,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-16 22:35:41','2026-05-16 22:35:41'),(48,92,36,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,127.60,707.60,'2026-05-16 22:38:52','2026-05-16 22:38:52'),(49,92,37,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-16 23:06:44','2026-05-16 23:06:44'),(50,92,38,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,7.00,0.00,22.00,70.00,15.40,85.40,'2026-05-16 23:08:24','2026-05-16 23:08:24'),(51,92,39,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-17 00:02:29','2026-05-17 00:02:29'),(52,92,40,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,7.00,0.00,22.00,70.00,15.40,85.40,'2026-05-17 00:03:35','2026-05-17 00:03:35'),(53,92,41,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-17 00:11:15','2026-05-17 00:11:15'),(54,92,42,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,7.00,0.00,22.00,70.00,15.40,85.40,'2026-05-17 00:12:08','2026-05-17 00:12:08'),(55,92,43,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-17 09:44:07','2026-05-17 09:44:07'),(56,92,44,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,7.00,0.00,22.00,70.00,15.40,85.40,'2026-05-17 09:45:00','2026-05-17 09:45:00'),(57,92,45,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',2.000,580.00,10.00,22.00,1044.00,229.68,1273.68,'2026-05-18 12:23:27','2026-05-18 12:23:27'),(58,92,46,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-19 16:47:02','2026-05-19 16:47:02'),(59,92,47,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,7.00,0.00,22.00,70.00,15.40,85.40,'2026-05-19 16:48:16','2026-05-19 16:48:16'),(61,92,48,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-19 17:17:18','2026-05-19 17:17:18'),(62,92,49,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,7.00,0.00,22.00,70.00,15.40,85.40,'2026-05-19 17:18:22','2026-05-19 17:18:22'),(63,92,50,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-19 17:25:17','2026-05-19 17:25:17'),(65,92,51,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,7.00,0.00,22.00,70.00,15.40,85.40,'2026-05-19 17:26:20','2026-05-19 17:26:20'),(66,93,52,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-20 09:53:26','2026-05-20 09:53:26'),(67,93,53,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-20 09:56:08','2026-05-20 09:56:08'),(68,93,54,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,7.00,0.00,22.00,70.00,15.40,85.40,'2026-05-20 09:56:45','2026-05-20 09:56:45'),(69,93,55,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-20 10:13:45','2026-05-20 10:13:45'),(70,93,56,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,7.00,0.00,22.00,70.00,15.40,85.40,'2026-05-20 10:14:29','2026-05-20 10:14:29'),(71,93,57,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',0.000,590.00,0.00,22.00,0.00,0.00,0.00,'2026-05-22 03:09:50','2026-05-22 03:09:50'),(73,93,58,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,10.00,0.00,22.00,10.00,2.20,12.20,'2026-05-22 03:26:38','2026-05-22 03:26:38'),(74,93,59,1,NULL,'',1.000,0.00,0.00,22.00,0.00,0.00,0.00,'2026-05-22 04:19:54','2026-05-22 04:19:54'),(75,93,60,1,NULL,'',1.000,0.00,0.00,22.00,0.00,0.00,0.00,'2026-05-22 04:27:04','2026-05-22 04:27:04'),(76,93,61,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',-1.000,590.00,0.00,22.00,-590.00,-129.80,-719.80,'2026-05-22 04:27:41','2026-05-22 04:27:41'),(77,93,62,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,0.00,0.00,22.00,0.00,0.00,0.00,'2026-05-22 04:28:17','2026-05-22 04:28:17'),(78,93,63,1,NULL,'',1.000,0.00,0.00,22.00,0.00,0.00,0.00,'2026-05-22 04:32:08','2026-05-22 04:32:08'),(79,93,64,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,590.00,0.00,22.00,590.00,129.80,719.80,'2026-05-22 04:33:02','2026-05-22 04:33:02'),(80,93,65,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,590.00,0.00,22.00,590.00,129.80,719.80,'2026-05-22 23:29:41','2026-05-22 23:29:41'),(81,93,66,1,1,'Modulo CRM',1.000,1200.00,0.00,22.00,1200.00,264.00,1464.00,'2026-05-22 23:33:23','2026-05-22 23:33:23'),(82,93,67,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,590.00,0.00,22.00,590.00,129.80,719.80,'2026-05-23 13:53:51','2026-05-23 13:53:51'),(83,93,68,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,590.00,0.00,22.00,590.00,129.80,719.80,'2026-05-24 04:12:43','2026-05-24 04:12:43'),(85,93,69,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,0.00,22.00,100.00,22.00,122.00,'2026-05-25 07:26:42','2026-05-25 07:26:42'),(86,93,70,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,0.00,22.00,100.00,22.00,122.00,'2026-05-25 07:29:00','2026-05-25 07:29:00'),(87,93,70,2,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,50.00,0.00,22.00,50.00,11.00,61.00,'2026-05-25 07:29:00','2026-05-25 07:29:00'),(88,93,71,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,0.00,22.00,100.00,22.00,122.00,'2026-05-25 07:30:35','2026-05-25 07:30:35'),(89,93,71,2,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,50.00,0.00,5.00,50.00,2.50,52.50,'2026-05-25 07:30:35','2026-05-25 07:30:35'),(90,94,72,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,590.00,0.00,22.00,590.00,129.80,719.80,'2026-06-03 11:04:02','2026-06-03 11:04:02'),(93,95,73,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,20.00,10.00,22.00,18.00,3.96,21.96,'2026-06-03 14:37:15','2026-06-03 14:37:15'),(94,95,73,2,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,10.00,0.00,22.00,10.00,2.20,12.20,'2026-06-03 14:37:15','2026-06-03 14:37:15'),(95,95,74,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,590.00,0.00,22.00,590.00,129.80,719.80,'2026-06-03 15:20:58','2026-06-03 15:20:58'),(96,96,75,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,10.00,0.00,22.00,10.00,2.20,12.20,'2026-06-03 19:54:43','2026-06-03 19:54:43'),(97,96,76,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,50.00,0.00,22.00,50.00,11.00,61.00,'2026-06-03 20:04:40','2026-06-03 20:04:40'),(98,96,77,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,10.00,22.00,90.00,19.80,109.80,'2026-06-03 20:31:03','2026-06-03 20:31:03');
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
) ENGINE=InnoDB AUTO_INCREMENT=140 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quote`
--

LOCK TABLES `quote` WRITE;
/*!40000 ALTER TABLE `quote` DISABLE KEYS */;
INSERT INTO `quote` VALUES (56,70,70,'PRE-2026-00001','2026-04-26','2026-05-26','ACCEPTED','EUR',1100.00,242.00,1342.00,'Preventivo tenant A','2026-04-27 01:29:26','2026-04-29 20:08:14'),(57,72,72,'PRE-2026-00001','2026-04-26','2026-05-26','DRAFT','EUR',900.00,198.00,1098.00,'Preview HTML test','2026-04-27 19:58:16','2026-04-27 19:58:16'),(58,73,73,'PRE-2026-00001','2026-04-28','2026-05-28','ACCEPTED','EUR',1100.00,242.00,1342.00,NULL,'2026-04-28 18:46:25','2026-04-28 18:46:25'),(59,74,74,'PRE-2026-00001','2026-04-28','2026-05-28','ACCEPTED','EUR',1100.00,242.00,1342.00,NULL,'2026-04-28 20:08:48','2026-04-28 20:08:48'),(60,75,75,'PRE-2026-00001','2026-04-29','2026-05-29','ACCEPTED','EUR',1000.00,220.00,1220.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(61,75,76,'PRE-2026-00002','2026-04-29','2026-05-29','ACCEPTED','EUR',200.00,44.00,244.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(62,76,77,'PRE-2026-00001','2026-04-29','2026-05-29','ACCEPTED','EUR',500.00,110.00,610.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(63,77,78,'PRE-2026-00001','2026-04-29','2026-05-29','ACCEPTED','EUR',1000.00,220.00,1220.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(64,77,79,'PRE-2026-00002','2026-04-29','2026-05-29','ACCEPTED','EUR',200.00,44.00,244.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(65,78,80,'PRE-2026-00001','2026-04-29','2026-05-29','ACCEPTED','EUR',500.00,110.00,610.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(66,79,81,'PRE-2026-00001','2026-04-29','2026-05-29','ACCEPTED','EUR',1000.00,220.00,1220.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(67,79,82,'PRE-2026-00002','2026-04-29','2026-05-29','ACCEPTED','EUR',200.00,44.00,244.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(68,80,83,'PRE-2026-00001','2026-04-29','2026-05-29','ACCEPTED','EUR',500.00,110.00,610.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(69,81,84,'PRE-2026-00001','2026-04-29','2026-05-29','ACCEPTED','EUR',1000.00,220.00,1220.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(70,81,85,'PRE-2026-00002','2026-04-29','2026-05-29','ACCEPTED','EUR',200.00,44.00,244.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(71,82,86,'PRE-2026-00001','2026-04-29','2026-05-29','ACCEPTED','EUR',500.00,110.00,610.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:04'),(72,83,87,'PRE-2026-00001','2026-04-29','2026-05-29','ACCEPTED','EUR',1000.00,220.00,1220.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:05'),(73,83,88,'PRE-2026-00002','2026-04-29','2026-05-29','ACCEPTED','EUR',200.00,44.00,244.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:05'),(74,84,89,'PRE-2026-00001','2026-04-29','2026-05-29','ACCEPTED','EUR',500.00,110.00,610.00,NULL,'2026-04-29 04:12:04','2026-04-29 04:12:05'),(75,84,89,'PRE-2026-00002','2026-05-02','2026-06-30','DRAFT','EUR',1900.00,418.00,2318.00,'Prova preventivo','2026-05-01 14:05:51','2026-05-01 14:14:17'),(76,84,89,'PRE-2026-00003','2026-05-02','2026-06-30','ACCEPTED','EUR',2190.00,481.80,2671.80,'PROVA','2026-05-01 15:17:07','2026-05-01 15:56:10'),(77,75,76,'PRE-2026-00003','2026-05-01','2026-05-31','ACCEPTED','EUR',90.00,19.80,109.80,'PROVA CLIENTE','2026-05-01 17:59:05','2026-05-01 18:05:01'),(78,75,76,'PRE-2026-00004','2026-05-02','2026-06-01','ACCEPTED','EUR',140.00,30.80,170.80,'PROVA RICALCOLO TOTALI','2026-05-02 03:24:46','2026-05-02 03:25:13'),(79,85,75,'PRE-2026-00001','2026-05-06','2026-06-05','ACCEPTED','EUR',580.00,127.60,707.60,'PREVENTIVO PER IL CLIENTE ROSSI S.R.L','2026-05-06 21:18:38','2026-05-06 21:24:39'),(80,85,76,'PRE-2026-00002','2026-05-06','2026-06-05','ACCEPTED','EUR',1200.00,264.00,1464.00,'PREVENTIVO PER IL CLIENTE BIANCHI S.R.L','2026-05-06 21:23:17','2026-05-06 21:23:17'),(81,85,76,'PRE-2026-00003','2026-05-06','2026-06-05','DRAFT','EUR',580.00,127.60,707.60,'prova','2026-05-06 23:03:08','2026-05-06 23:03:08'),(82,85,76,'PRE-2026-00004','2026-05-06','2026-06-05','DRAFT','EUR',800.00,176.00,976.00,'prova itemId','2026-05-06 23:12:44','2026-05-06 23:12:44'),(83,85,75,'PRE-2026-00005','2026-05-06','2026-06-05','DRAFT','EUR',600.00,132.00,732.00,'prova','2026-05-06 23:25:10','2026-05-06 23:25:10'),(84,85,75,'PRE-2026-00006','2026-05-06','2026-06-05','ACCEPTED','EUR',464.00,102.08,566.08,'PREVENTIVO PER L\'AZIENDA ROSSI S.R.L','2026-05-06 23:28:27','2026-05-06 23:35:02'),(85,86,76,'PRE-2026-00001','2026-05-07','2026-06-06','ACCEPTED','EUR',5220.00,1148.40,6368.40,'PREVENTIVO PER IL CLIENTE BIANCHI S.R.L','2026-05-07 01:28:49','2026-05-07 01:29:52'),(86,86,75,'PRE-2026-00003','2026-05-07','2026-06-06','ACCEPTED','EUR',1080.00,237.60,1317.60,'PROVA ARTICOLO SERVICE','2026-05-07 01:50:01','2026-05-07 01:54:10'),(91,85,76,'PRE-2026-00007','2026-05-07','2026-06-06','SENT','EUR',1200.00,264.00,1464.00,'','2026-05-07 10:30:24','2026-05-07 10:30:44'),(92,87,76,'PRE-2026-00001','2026-05-08','2026-06-07','ACCEPTED','EUR',11600.00,2552.00,14152.00,'ACQUISTO DI 20 FORNI ELETTRICI','2026-05-08 02:51:30','2026-05-08 02:51:51'),(93,87,75,'PRE-2026-00002','2026-05-08','2026-06-07','ACCEPTED','EUR',2900.00,638.00,3538.00,'PROVA ADUE','2026-05-08 16:17:53','2026-05-08 16:17:53'),(94,87,76,'PRE-2026-00003','2026-05-08','2026-06-07','ACCEPTED','EUR',100.00,22.00,122.00,'PROVA INCASSO','2026-05-08 19:01:50','2026-05-08 19:01:50'),(95,87,76,'PRE-2026-00004','2026-05-09','2026-06-08','ACCEPTED','EUR',580.00,127.60,707.60,'PROVA PRIMA NOTA','2026-05-09 07:34:02','2026-05-09 07:34:02'),(96,87,76,'PRE-2026-00005','2026-05-09','2026-06-08','ACCEPTED','EUR',580.00,127.60,707.60,'PROVA MODALE','2026-05-09 12:22:40','2026-05-09 12:22:40'),(97,88,75,'PRE-2026-00001','2026-05-09','2026-06-08','ACCEPTED','EUR',580.00,127.60,707.60,'PROVA MODALE','2026-05-09 13:29:47','2026-05-09 13:29:47'),(98,88,75,'PRE-2026-00002','2026-05-09','2026-06-08','ACCEPTED','EUR',580.00,127.60,707.60,'PROVA MODALE','2026-05-09 14:13:50','2026-05-09 14:13:50'),(99,88,76,'PRE-2026-00003','2026-05-10','2026-06-09','ACCEPTED','EUR',580.00,127.60,707.60,'TEST MOVIMENTO DI INCASSO','2026-05-10 12:02:56','2026-05-10 12:02:56'),(100,88,75,'PRE-2026-00004','2026-05-10','2026-06-09','ACCEPTED','EUR',580.00,127.60,707.60,'TEST JOURNAL','2026-05-10 12:35:37','2026-05-10 12:35:37'),(101,88,76,'PRE-2026-00005','2026-05-10','2026-06-09','ACCEPTED','EUR',580.00,127.60,707.60,'PROVA LIBRO GIORNALE','2026-05-10 13:11:35','2026-05-10 13:11:35'),(102,88,75,'PRE-2026-00006','2026-05-10','2026-06-09','ACCEPTED','EUR',580.00,127.60,707.60,'PROVA CONTABILE','2026-05-10 18:06:09','2026-05-10 18:06:09'),(103,90,75,'PRE-2026-00001','2026-05-13','2026-06-12','ACCEPTED','EUR',580.00,127.60,707.60,'PROVA FILTRI','2026-05-13 07:28:19','2026-05-13 18:48:57'),(104,90,76,'PRE-2026-00002','2026-05-04','2026-06-26','ACCEPTED','EUR',1200.00,264.00,1464.00,'PROVA FILTRI','2026-05-13 07:46:54','2026-05-13 07:48:42'),(105,91,76,'PRE-2026-00001','2026-05-15','2026-06-14','ACCEPTED','EUR',580.00,127.60,707.60,'PROVA PREVENTIVO','2026-05-15 06:29:01','2026-05-15 06:29:24'),(106,91,75,'PRE-2026-00002','2026-05-16','2026-06-15','ACCEPTED','EUR',5800.00,1276.00,7076.00,'PROVA MOVIMENTAZIONE','2026-05-16 02:15:32','2026-05-16 02:15:32'),(107,91,76,'PRE-2026-00003','2026-05-16','2026-06-15','ACCEPTED','EUR',120.00,26.40,146.40,'SCARICO 12 PEZZI','2026-05-16 21:53:04','2026-05-16 21:53:04'),(108,92,76,'PRE-2026-00001','2026-05-17','2026-06-16','ACCEPTED','EUR',120.00,26.40,146.40,'SCARICO 12 PEZZI','2026-05-17 01:10:13','2026-05-17 01:10:13'),(109,92,76,'PRE-2026-00002','2026-05-17','2026-06-16','ACCEPTED','EUR',120.00,26.40,146.40,'SCARICO 12 PEZZI','2026-05-17 01:28:31','2026-05-17 01:28:31'),(110,92,76,'PRE-2026-00003','2026-05-17','2026-06-16','ACCEPTED','EUR',120.00,26.40,146.40,'SCARICO 12 PEZZI','2026-05-17 01:32:34','2026-05-17 01:32:34'),(111,92,76,'PRE-2026-00004','2026-05-17','2026-06-16','ACCEPTED','EUR',120.00,26.40,146.40,'LIFO','2026-05-17 02:04:33','2026-05-17 02:04:33'),(112,92,76,'PRE-2026-00005','2026-05-17','2026-06-16','ACCEPTED','EUR',120.00,26.40,146.40,'LIFO','2026-05-17 02:12:55','2026-05-17 02:12:55'),(113,92,76,'PRE-2026-00006','2026-05-17','2026-06-16','ACCEPTED','EUR',120.00,26.40,146.40,'LIFO','2026-05-17 11:45:53','2026-05-17 11:45:53'),(114,92,76,'PRE-2026-00007','2026-05-17','2026-06-16','ACCEPTED','EUR',20.00,4.40,24.40,'','2026-05-17 15:05:27','2026-05-17 15:05:27'),(115,92,76,'PRE-2026-00008','2026-05-18','2026-06-17','ACCEPTED','EUR',522.00,114.84,636.84,'PROVA FORMATTAZIONE','2026-05-18 14:12:28','2026-05-18 14:12:28'),(116,92,76,'PRE-2026-00009','2026-05-18','2026-06-17','ACCEPTED','EUR',120.00,26.40,146.40,'','2026-05-18 20:16:47','2026-05-18 20:16:47'),(117,92,76,'PRE-2026-00010','2026-05-19','2026-06-18','ACCEPTED','EUR',120.00,26.40,146.40,'','2026-05-19 19:19:18','2026-05-19 19:19:18'),(118,92,76,'PRE-2026-00011','2026-05-19','2026-06-18','ACCEPTED','EUR',120.00,26.40,146.40,'','2026-05-19 19:35:03','2026-05-19 19:35:03'),(119,93,76,'PRE-2026-00001','2026-05-20','2026-06-19','ACCEPTED','EUR',120.00,26.40,146.40,'','2026-05-20 11:57:46','2026-05-20 11:57:46'),(120,93,76,'PRE-2026-00002','2026-05-20','2026-06-19','ACCEPTED','EUR',120.00,26.40,146.40,'','2026-05-20 12:15:23','2026-05-20 12:15:23'),(121,93,76,'PRE-2026-00003','2026-05-21','2026-06-20','DRAFT','EUR',531.00,116.82,647.82,'','2026-05-21 05:00:02','2026-05-21 05:00:02'),(122,93,76,'PRE-2026-00004','2026-05-21','2026-06-20','ACCEPTED','EUR',590.00,129.80,719.80,'','2026-05-21 12:05:52','2026-05-23 10:01:22'),(123,93,76,'PRE-2026-00005','2026-05-21','2026-06-20','ACCEPTED','EUR',10.00,2.20,12.20,'','2026-05-21 14:18:01','2026-05-21 14:21:21'),(124,93,76,'PRE-2026-00006','2026-05-21','2026-06-20','ACCEPTED','EUR',70.00,15.40,85.40,'','2026-05-21 15:13:19','2026-05-21 20:09:37'),(125,93,76,'PRE-2026-00007','2026-05-21','2026-06-20','ACCEPTED','EUR',10.00,2.20,12.20,'','2026-05-21 18:33:39','2026-05-21 19:20:28'),(126,93,76,'PRE-2026-00008','2026-05-22','2026-05-30','ACCEPTED','EUR',590.00,129.80,719.80,'','2026-05-22 06:44:51','2026-05-22 06:45:12'),(127,93,76,'PRE-2026-00009','2026-05-23','2026-06-22','ACCEPTED','EUR',590.00,129.80,719.80,'','2026-05-23 15:30:52','2026-05-23 15:30:52'),(128,93,76,'PRE-2026-00010','2026-05-24','2026-06-23','ACCEPTED','EUR',590.00,129.80,719.80,'','2026-05-24 03:52:18','2026-05-24 03:52:18'),(129,93,75,'PRE-2026-00011','2026-06-04','2026-06-24','ACCEPTED','EUR',100.00,22.00,122.00,'','2026-05-25 08:06:10','2026-05-25 08:06:10'),(130,93,76,'PRE-2026-00012','2026-05-25','2026-06-24','ACCEPTED','EUR',150.00,33.00,183.00,'','2026-05-25 08:12:46','2026-05-25 08:12:46'),(131,93,76,'PRE-2026-00013','2026-05-25','2026-06-24','ACCEPTED','EUR',150.00,33.00,183.00,'','2026-05-25 08:24:25','2026-05-25 08:24:25'),(132,93,76,'PRE-2026-00014','2026-05-25','2026-06-24','ACCEPTED','EUR',150.00,33.00,183.00,'','2026-05-25 08:45:12','2026-05-25 08:45:12'),(133,93,75,'PRE-2026-00015','2026-05-25','2026-06-24','ACCEPTED','EUR',150.00,33.00,183.00,'','2026-05-25 08:54:43','2026-05-25 08:54:43'),(134,93,76,'PRE-2026-00016','2026-05-30','2026-06-24','ACCEPTED','EUR',150.00,27.00,177.00,'','2026-05-25 09:21:03','2026-05-25 09:21:03'),(135,94,76,'QUO-00001','2026-06-02','2026-07-02','ACCEPTED','EUR',10.00,2.20,12.20,'PROVA INSERIMENTO','2026-06-02 19:00:48','2026-06-02 19:05:42'),(136,94,75,'QUO-00002','2026-06-03','2026-07-03','ACCEPTED','EUR',10.00,2.20,12.20,'','2026-06-03 03:32:06','2026-06-03 03:32:06'),(137,95,76,'QUO-00001','2026-06-03','2026-07-03','ACCEPTED','EUR',28.00,6.16,34.16,'','2026-06-03 14:02:08','2026-06-03 14:06:16'),(138,96,76,'QUO-00001','2026-06-03','2026-07-03','ACCEPTED','EUR',20.00,4.40,24.40,'','2026-06-03 21:42:12','2026-06-03 21:42:12'),(139,96,76,'QUO-00002','2026-06-03','2026-07-03','ACCEPTED','EUR',20.00,4.40,24.40,'','2026-06-03 22:58:41','2026-06-03 22:58:41');
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
) ENGINE=InnoDB AUTO_INCREMENT=332 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quote_line`
--

LOCK TABLES `quote_line` WRITE;
/*!40000 ALTER TABLE `quote_line` DISABLE KEYS */;
INSERT INTO `quote_line` VALUES (127,70,56,1,'Modulo CRM',1.000,1000.00,10.00,22.00,900.00,'2026-04-27 01:29:26','2026-04-27 01:29:26',NULL,NULL,NULL,NULL),(128,70,56,2,'Setup',1.000,200.00,0.00,22.00,200.00,'2026-04-27 01:29:26','2026-04-27 01:29:26',NULL,NULL,NULL,NULL),(129,72,57,1,'Modulo CRM',1.000,1000.00,10.00,22.00,900.00,'2026-04-27 19:58:16','2026-04-27 19:58:16',NULL,NULL,NULL,NULL),(132,73,58,1,'Modulo CRM',1.000,1000.00,10.00,22.00,900.00,'2026-04-28 18:46:25','2026-04-28 18:46:25',NULL,NULL,NULL,NULL),(133,73,58,2,'Setup',1.000,200.00,0.00,22.00,200.00,'2026-04-28 18:46:25','2026-04-28 18:46:25',NULL,NULL,NULL,NULL),(136,74,59,1,'Modulo CRM',1.000,1000.00,10.00,22.00,900.00,'2026-04-28 20:08:48','2026-04-28 20:08:48',NULL,NULL,NULL,NULL),(137,74,59,2,'Setup',1.000,200.00,0.00,22.00,200.00,'2026-04-28 20:08:48','2026-04-28 20:08:48',NULL,NULL,NULL,NULL),(139,75,60,1,'Modulo CRM',1.000,1000.00,0.00,22.00,1000.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(141,75,61,1,'Setup',1.000,200.00,0.00,22.00,200.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(143,76,62,1,'Servizio Altro Tenant',1.000,500.00,0.00,22.00,500.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(145,77,63,1,'Modulo CRM',1.000,1000.00,0.00,22.00,1000.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(147,77,64,1,'Setup',1.000,200.00,0.00,22.00,200.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(149,78,65,1,'Servizio Altro Tenant',1.000,500.00,0.00,22.00,500.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(151,79,66,1,'Modulo CRM',1.000,1000.00,0.00,22.00,1000.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(153,79,67,1,'Setup',1.000,200.00,0.00,22.00,200.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(155,80,68,1,'Servizio Altro Tenant',1.000,500.00,0.00,22.00,500.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(157,81,69,1,'Modulo CRM',1.000,1000.00,0.00,22.00,1000.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(159,81,70,1,'Setup',1.000,200.00,0.00,22.00,200.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(161,82,71,1,'Servizio Altro Tenant',1.000,500.00,0.00,22.00,500.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(163,83,72,1,'Modulo CRM',1.000,1000.00,0.00,22.00,1000.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(165,83,73,1,'Setup',1.000,200.00,0.00,22.00,200.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',NULL,NULL,NULL,NULL),(167,84,74,1,'Servizio Altro Tenant',1.000,500.00,0.00,22.00,500.00,'2026-04-29 04:12:05','2026-04-29 04:12:05',NULL,NULL,NULL,NULL),(182,84,75,1,'RIGA 1',1.000,1000.00,0.00,22.00,1000.00,'2026-05-01 14:14:53','2026-05-01 14:14:53',NULL,NULL,NULL,NULL),(183,84,75,2,'RIGA 2',2.000,500.00,10.00,22.00,900.00,'2026-05-01 14:14:53','2026-05-01 14:14:53',NULL,NULL,NULL,NULL),(190,84,76,1,'RIGA 1',1.000,100.00,10.00,22.00,90.00,'2026-05-01 15:56:10','2026-05-01 15:56:10',NULL,NULL,NULL,NULL),(191,84,76,2,'RIGA 2',1.000,500.00,0.00,22.00,500.00,'2026-05-01 15:56:10','2026-05-01 15:56:10',NULL,NULL,NULL,NULL),(192,84,76,3,'RIGA 3',1.000,2000.00,20.00,22.00,1600.00,'2026-05-01 15:56:10','2026-05-01 15:56:10',NULL,NULL,NULL,NULL),(194,75,77,1,'RIGA 1',1.000,100.00,10.00,22.00,90.00,'2026-05-01 18:05:01','2026-05-01 18:05:01',NULL,NULL,NULL,NULL),(197,75,78,1,'RIGA 1',1.000,100.00,10.00,22.00,90.00,'2026-05-02 03:25:13','2026-05-02 03:25:13',NULL,NULL,NULL,NULL),(198,75,78,2,'RIGA 2',1.000,50.00,0.00,22.00,50.00,'2026-05-02 03:25:13','2026-05-02 03:25:13',NULL,NULL,NULL,NULL),(204,85,79,1,'RIGA 1',1.000,580.00,0.00,22.00,580.00,'2026-05-06 21:24:39','2026-05-06 21:24:39',NULL,NULL,NULL,NULL),(206,85,80,1,'RIGA 1',1.000,1200.00,0.00,22.00,1200.00,'2026-05-06 22:44:38','2026-05-06 22:44:38',NULL,NULL,NULL,NULL),(207,85,81,1,'RIGA 1',1.000,580.00,0.00,22.00,580.00,'2026-05-06 23:03:08','2026-05-06 23:03:08',NULL,NULL,NULL,NULL),(208,85,82,1,'RIGA 1',1.000,800.00,0.00,22.00,800.00,'2026-05-06 23:12:44','2026-05-06 23:12:44',NULL,NULL,NULL,2),(209,85,83,1,'RIGA 1',1.000,600.00,0.00,22.00,600.00,'2026-05-06 23:25:10','2026-05-06 23:25:10',NULL,NULL,NULL,2),(212,85,84,1,'RIGA 1',1.000,580.00,20.00,22.00,464.00,'2026-05-06 23:35:02','2026-05-06 23:35:02',NULL,NULL,NULL,2),(214,86,85,1,'RIGA 1',10.000,580.00,10.00,22.00,5220.00,'2026-05-07 01:29:48','2026-05-07 01:29:48',NULL,NULL,NULL,2),(216,86,86,1,'RIGA 1',1.000,1200.00,10.00,22.00,1080.00,'2026-05-07 01:50:29','2026-05-07 01:50:29',NULL,NULL,NULL,1),(219,85,91,1,'Modulo CRM',1.000,1200.00,0.00,22.00,1200.00,'2026-05-07 10:30:44','2026-05-07 10:30:44',NULL,NULL,NULL,1),(222,87,92,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',20.000,580.00,0.00,22.00,11600.00,'2026-05-08 02:51:51','2026-05-08 02:51:51',NULL,NULL,NULL,2),(224,87,93,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',5.000,580.00,0.00,22.00,2900.00,'2026-05-08 16:17:53','2026-05-08 16:17:53',NULL,NULL,NULL,2),(226,87,94,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,0.00,22.00,100.00,'2026-05-08 19:01:50','2026-05-08 19:01:50',NULL,NULL,NULL,2),(228,87,95,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,'2026-05-09 07:34:02','2026-05-09 07:34:02',NULL,NULL,NULL,2),(230,87,96,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,'2026-05-09 12:22:40','2026-05-09 12:22:40',NULL,NULL,NULL,2),(232,88,97,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,'2026-05-09 13:29:47','2026-05-09 13:29:47',NULL,NULL,NULL,2),(234,88,98,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,'2026-05-09 14:13:50','2026-05-09 14:13:50',NULL,NULL,NULL,2),(236,88,99,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,'2026-05-10 12:02:56','2026-05-10 12:02:56',NULL,NULL,NULL,2),(238,88,100,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,'2026-05-10 12:35:37','2026-05-10 12:35:37',NULL,NULL,NULL,2),(240,88,101,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,'2026-05-10 13:11:35','2026-05-10 13:11:35',NULL,NULL,NULL,2),(242,88,102,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,'2026-05-10 18:06:09','2026-05-10 18:06:09',NULL,NULL,NULL,2),(246,90,104,1,'Modulo CRM',1.000,1200.00,0.00,22.00,1200.00,'2026-05-13 07:46:54','2026-05-13 07:46:54',NULL,NULL,NULL,1),(247,90,103,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,'2026-05-13 18:48:57','2026-05-13 18:48:57',NULL,NULL,NULL,2),(249,91,105,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,'2026-05-15 06:29:24','2026-05-15 06:29:24',NULL,NULL,NULL,2),(251,91,106,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,580.00,0.00,22.00,5800.00,'2026-05-16 02:15:32','2026-05-16 02:15:32',NULL,NULL,NULL,2),(253,91,107,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,0.00,22.00,120.00,'2026-05-16 21:53:04','2026-05-16 21:53:04',NULL,NULL,NULL,2),(255,92,108,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,0.00,22.00,120.00,'2026-05-17 01:10:13','2026-05-17 01:10:13',NULL,NULL,NULL,2),(257,92,109,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,0.00,22.00,120.00,'2026-05-17 01:28:31','2026-05-17 01:28:31',NULL,NULL,NULL,2),(259,92,110,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,0.00,22.00,120.00,'2026-05-17 01:32:34','2026-05-17 01:32:34',NULL,NULL,NULL,2),(261,92,111,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,0.00,22.00,120.00,'2026-05-17 02:04:33','2026-05-17 02:04:33',NULL,NULL,NULL,2),(263,92,112,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,0.00,22.00,120.00,'2026-05-17 02:12:55','2026-05-17 02:12:55',NULL,NULL,NULL,2),(265,92,113,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,0.00,22.00,120.00,'2026-05-17 11:45:53','2026-05-17 11:45:53',NULL,NULL,NULL,2),(267,92,114,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',2.000,10.00,0.00,22.00,20.00,'2026-05-17 15:05:27','2026-05-17 15:05:27',NULL,NULL,NULL,2),(269,92,115,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,10.00,22.00,522.00,'2026-05-18 14:12:28','2026-05-18 14:12:28',NULL,NULL,NULL,2),(271,92,116,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,0.00,22.00,120.00,'2026-05-18 20:16:47','2026-05-18 20:16:47',NULL,NULL,NULL,2),(273,92,117,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,0.00,22.00,120.00,'2026-05-19 19:19:18','2026-05-19 19:19:18',NULL,NULL,NULL,2),(275,92,118,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,0.00,22.00,120.00,'2026-05-19 19:35:03','2026-05-19 19:35:03',NULL,NULL,NULL,2),(277,93,119,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,0.00,22.00,120.00,'2026-05-20 11:57:46','2026-05-20 11:57:46',NULL,NULL,NULL,2),(279,93,120,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,0.00,22.00,120.00,'2026-05-20 12:15:23','2026-05-20 12:15:23',NULL,NULL,NULL,2),(280,93,121,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,590.00,10.00,22.00,531.00,'2026-05-21 05:00:02','2026-05-21 05:00:02',NULL,NULL,NULL,2),(284,93,123,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,10.00,0.00,22.00,10.00,'2026-05-21 14:21:21','2026-05-21 14:21:21',NULL,NULL,NULL,2),(289,93,125,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,10.00,0.00,22.00,10.00,'2026-05-21 19:20:28','2026-05-21 19:20:28',NULL,NULL,NULL,2),(290,93,124,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,70.00,0.00,22.00,70.00,'2026-05-21 20:09:37','2026-05-21 20:09:37',NULL,NULL,NULL,2),(292,93,126,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,590.00,0.00,22.00,590.00,'2026-05-22 06:45:12','2026-05-22 06:45:12',NULL,NULL,NULL,2),(293,93,122,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,590.00,0.00,22.00,590.00,'2026-05-23 10:01:22','2026-05-23 10:01:22',NULL,NULL,NULL,2),(295,93,127,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,590.00,0.00,22.00,590.00,'2026-05-23 15:30:52','2026-05-23 15:30:52',NULL,NULL,NULL,2),(297,93,128,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,590.00,0.00,22.00,590.00,'2026-05-24 03:52:18','2026-05-24 03:52:18',NULL,NULL,NULL,2),(299,93,129,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,0.00,22.00,100.00,'2026-05-25 08:06:10','2026-05-25 08:06:10',NULL,NULL,NULL,2),(302,93,130,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,0.00,22.00,100.00,'2026-05-25 08:12:46','2026-05-25 08:12:46',NULL,NULL,NULL,2),(303,93,130,2,'Modulo CRM',1.000,50.00,0.00,22.00,50.00,'2026-05-25 08:12:46','2026-05-25 08:12:46',NULL,NULL,NULL,1),(306,93,131,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,0.00,22.00,100.00,'2026-05-25 08:24:25','2026-05-25 08:24:25',NULL,NULL,NULL,2),(307,93,131,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,50.00,0.00,22.00,50.00,'2026-05-25 08:24:25','2026-05-25 08:24:25',NULL,NULL,NULL,2),(310,93,132,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,0.00,22.00,100.00,'2026-05-25 08:45:12','2026-05-25 08:45:12',NULL,NULL,NULL,2),(311,93,132,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,50.00,0.00,22.00,50.00,'2026-05-25 08:45:12','2026-05-25 08:45:12',NULL,NULL,NULL,2),(314,93,133,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,0.00,22.00,100.00,'2026-05-25 08:54:43','2026-05-25 08:54:43',NULL,NULL,NULL,2),(315,93,133,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,50.00,0.00,22.00,50.00,'2026-05-25 08:54:43','2026-05-25 08:54:43',NULL,NULL,NULL,2),(318,93,134,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,0.00,22.00,100.00,'2026-05-25 09:21:03','2026-05-25 09:21:03',NULL,NULL,NULL,2),(319,93,134,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,50.00,0.00,10.00,50.00,'2026-05-25 09:21:03','2026-05-25 09:21:03',NULL,NULL,NULL,2),(321,94,135,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,10.00,0.00,22.00,10.00,'2026-06-02 19:05:42','2026-06-02 19:05:42',NULL,NULL,NULL,2),(323,94,136,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,10.00,0.00,22.00,10.00,'2026-06-03 03:32:06','2026-06-03 03:32:06',NULL,NULL,NULL,2),(326,95,137,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,20.00,10.00,22.00,18.00,'2026-06-03 14:05:44','2026-06-03 14:05:44',NULL,NULL,NULL,2),(327,95,137,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,10.00,0.00,22.00,10.00,'2026-06-03 14:05:44','2026-06-03 14:05:44',NULL,NULL,NULL,2),(329,96,138,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,20.00,0.00,22.00,20.00,'2026-06-03 21:42:12','2026-06-03 21:42:12',NULL,NULL,NULL,2),(331,96,139,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',2.000,10.00,0.00,22.00,20.00,'2026-06-03 22:58:41','2026-06-03 22:58:41',NULL,NULL,NULL,2);
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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (1,'2026-05-05 05:42:48.719345','2026-05-05 05:42:48.719349',85,'ROLE_ADMIN','Amministratore',_binary ''),(2,'2026-05-07 01:20:10.839427','2026-05-07 01:20:10.839431',86,'ROLE_ADMIN','Amministratore',_binary ''),(3,'2026-05-08 02:46:04.641656','2026-05-08 02:46:04.641660',87,'ROLE_ADMIN','Amministratore',_binary ''),(4,'2026-05-09 13:26:16.983978','2026-05-09 13:26:16.983981',88,'ROLE_ADMIN','Amministratore',_binary ''),(5,'2026-05-13 02:01:14.570266','2026-05-13 02:01:14.570270',89,'ROLE_ADMIN','Amministratore',_binary ''),(6,'2026-05-13 02:04:33.388429','2026-05-13 02:04:33.388439',90,'ROLE_ADMIN','Amministratore',_binary ''),(7,'2026-05-14 19:54:08.866208','2026-05-14 19:54:08.866213',91,'ROLE_ADMIN','Amministratore',_binary ''),(8,'2026-05-16 23:45:51.436400','2026-05-16 23:45:51.436404',92,'ROLE_ADMIN','Amministratore',_binary ''),(9,'2026-05-20 11:49:38.637633','2026-05-20 11:49:38.637638',93,'ROLE_ADMIN','Amministratore',_binary ''),(10,'2026-06-02 18:31:35.714693','2026-06-02 18:31:35.714703',94,'ROLE_ADMIN','Amministratore',_binary ''),(11,'2026-06-03 13:17:25.845415','2026-06-03 13:17:25.845419',95,'ROLE_ADMIN','Amministratore',_binary ''),(12,'2026-06-03 21:37:41.459615','2026-06-03 21:37:41.459619',96,'ROLE_ADMIN','Amministratore',_binary ''),(13,'2026-06-04 12:51:45.459520','2026-06-04 12:51:45.459524',97,'ROLE_ADMIN','Amministratore',_binary '');
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
) ENGINE=InnoDB AUTO_INCREMENT=80 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sales_order`
--

LOCK TABLES `sales_order` WRITE;
/*!40000 ALTER TABLE `sales_order` DISABLE KEYS */;
INSERT INTO `sales_order` VALUES (8,74,74,59,'ORD-2026-00001','2026-04-28','CONFIRMED','EUR',1342.00,'2026-04-28 20:08:48','2026-04-28 20:08:48',1100.00,242.00),(9,75,75,60,'ORD-2026-00001','2026-04-29','FULFILLED','EUR',1220.00,'2026-04-29 04:12:04','2026-05-02 05:27:45',1000.00,220.00),(10,75,76,61,'ORD-2026-00002','2026-04-29','FULFILLED','EUR',244.00,'2026-04-29 04:12:04','2026-05-02 23:38:48',200.00,44.00),(11,76,77,62,'ORD-2026-00001','2026-04-29','CONFIRMED','EUR',610.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',500.00,110.00),(12,77,78,63,'ORD-2026-00001','2026-04-29','CONFIRMED','EUR',1220.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',1000.00,220.00),(13,77,79,64,'ORD-2026-00002','2026-04-29','CONFIRMED','EUR',244.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',200.00,44.00),(14,78,80,65,'ORD-2026-00001','2026-04-29','CONFIRMED','EUR',610.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',500.00,110.00),(15,79,81,66,'ORD-2026-00001','2026-04-29','CONFIRMED','EUR',1220.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',1000.00,220.00),(16,79,82,67,'ORD-2026-00002','2026-04-29','CONFIRMED','EUR',244.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',200.00,44.00),(17,80,83,68,'ORD-2026-00001','2026-04-29','CONFIRMED','EUR',610.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',500.00,110.00),(18,81,84,69,'ORD-2026-00001','2026-04-29','CONFIRMED','EUR',1220.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',1000.00,220.00),(19,81,85,70,'ORD-2026-00002','2026-04-29','CONFIRMED','EUR',244.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',200.00,44.00),(20,82,86,71,'ORD-2026-00001','2026-04-29','CONFIRMED','EUR',610.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',500.00,110.00),(21,83,87,72,'ORD-2026-00001','2026-04-29','CONFIRMED','EUR',1220.00,'2026-04-29 04:12:05','2026-04-29 04:12:05',1000.00,220.00),(22,83,88,73,'ORD-2026-00002','2026-04-29','CONFIRMED','EUR',244.00,'2026-04-29 04:12:05','2026-04-29 04:12:05',200.00,44.00),(23,84,89,74,'ORD-2026-00001','2026-04-29','CONFIRMED','EUR',610.00,'2026-04-29 04:12:05','2026-04-29 04:12:05',500.00,110.00),(24,70,70,56,'ORD-2026-00001','2026-04-29','CONFIRMED','EUR',1342.00,'2026-04-29 20:08:14','2026-04-29 20:08:14',1100.00,242.00),(25,73,73,58,'ORD-2026-00002','2026-04-29','CONFIRMED','EUR',1342.00,'2026-04-29 20:26:18','2026-04-29 20:26:18',1100.00,242.00),(27,84,89,76,'ORD-2026-00003','2026-05-01','CONFIRMED','EUR',2671.80,'2026-05-01 15:56:24','2026-05-01 15:56:24',2190.00,481.80),(28,75,76,77,'ORD-2026-00003','2026-05-01','CANCELLED','EUR',109.80,'2026-05-01 18:05:04','2026-05-02 05:25:37',90.00,19.80),(29,75,76,78,'ORD-2026-00004','2026-05-02','FULFILLED','EUR',170.80,'2026-05-02 03:26:21','2026-05-02 05:20:53',140.00,30.80),(30,85,75,79,'ORD-2026-00001','2026-05-06','FULFILLED','EUR',707.60,'2026-05-06 21:25:11','2026-05-06 21:28:33',580.00,127.60),(31,85,75,84,'ORD-2026-00002','2026-05-07','FULFILLED','EUR',566.08,'2026-05-07 00:10:05','2026-05-07 00:11:54',464.00,102.08),(32,86,76,85,'ORD-2026-00001','2026-05-07','FULFILLED','EUR',6368.40,'2026-05-07 01:29:52','2026-05-07 01:31:09',5220.00,1148.40),(33,86,75,86,'ORD-2026-00002','2026-05-07','FULFILLED','EUR',1317.60,'2026-05-07 01:54:10','2026-05-07 01:54:17',1080.00,237.60),(34,87,76,92,'ORD-2026-00001','2026-05-08','FULFILLED','EUR',14152.00,'2026-05-08 02:52:17','2026-05-08 02:52:36',11600.00,2552.00),(35,87,75,93,'ORD-2026-00002','2026-05-08','FULFILLED','EUR',3538.00,'2026-05-08 16:18:01','2026-05-08 16:18:12',2900.00,638.00),(36,87,76,94,'ORD-2026-00003','2026-05-08','FULFILLED','EUR',122.00,'2026-05-08 19:02:01','2026-05-08 19:02:06',100.00,22.00),(37,87,76,95,'ORD-2026-00004','2026-05-09','FULFILLED','EUR',707.60,'2026-05-09 07:34:07','2026-05-09 07:34:10',580.00,127.60),(38,87,76,96,'ORD-2026-00005','2026-05-09','FULFILLED','EUR',707.60,'2026-05-09 12:22:43','2026-05-09 12:22:49',580.00,127.60),(39,88,75,97,'ORD-2026-00001','2026-05-09','FULFILLED','EUR',707.60,'2026-05-09 13:29:56','2026-05-09 13:30:02',580.00,127.60),(40,88,75,98,'ORD-2026-00002','2026-05-09','FULFILLED','EUR',707.60,'2026-05-09 14:14:39','2026-05-09 15:52:40',580.00,127.60),(41,88,76,99,'ORD-2026-00003','2026-05-10','FULFILLED','EUR',707.60,'2026-05-10 12:03:03','2026-05-10 12:03:08',580.00,127.60),(42,88,75,100,'ORD-2026-00004','2026-05-10','FULFILLED','EUR',707.60,'2026-05-10 12:35:47','2026-05-10 12:35:54',580.00,127.60),(43,88,76,101,'ORD-2026-00005','2026-05-10','FULFILLED','EUR',707.60,'2026-05-10 13:11:40','2026-05-10 13:11:43',580.00,127.60),(44,88,75,102,'ORD-2026-00006','2026-05-10','FULFILLED','EUR',707.60,'2026-05-10 18:06:19','2026-05-10 18:06:24',580.00,127.60),(45,90,76,104,'ORD-2026-00001','2026-05-13','FULFILLED','EUR',1464.00,'2026-05-13 07:48:42','2026-05-13 07:56:04',1200.00,264.00),(46,90,75,103,'ORD-2026-00002','2026-05-13','FULFILLED','EUR',707.60,'2026-05-13 18:49:04','2026-05-13 18:49:12',580.00,127.60),(47,91,76,105,'ORD-2026-00001','2026-05-15','FULFILLED','EUR',707.60,'2026-05-15 06:29:31','2026-05-15 06:29:40',580.00,127.60),(48,91,75,106,'ORD-2026-00002','2026-05-16','FULFILLED','EUR',7076.00,'2026-05-16 02:15:37','2026-05-16 02:15:43',5800.00,1276.00),(49,91,76,107,'ORD-2026-00003','2026-05-16','FULFILLED','EUR',146.40,'2026-05-16 21:53:11','2026-05-16 21:53:22',120.00,26.40),(50,92,76,108,'ORD-2026-00001','2026-05-17','FULFILLED','EUR',146.40,'2026-05-17 01:10:17','2026-05-17 01:10:21',120.00,26.40),(51,92,76,109,'ORD-2026-00002','2026-05-17','FULFILLED','EUR',146.40,'2026-05-17 01:28:35','2026-05-17 01:28:46',120.00,26.40),(52,92,76,110,'ORD-2026-00003','2026-05-17','FULFILLED','EUR',146.40,'2026-05-17 01:32:41','2026-05-17 01:32:50',120.00,26.40),(53,92,76,111,'ORD-2026-00004','2026-05-17','FULFILLED','EUR',146.40,'2026-05-17 02:04:38','2026-05-17 02:04:41',120.00,26.40),(54,92,76,112,'ORD-2026-00005','2026-05-17','FULFILLED','EUR',146.40,'2026-05-17 02:12:58','2026-05-17 02:13:02',120.00,26.40),(55,92,76,113,'ORD-2026-00006','2026-05-17','FULFILLED','EUR',146.40,'2026-05-17 11:45:57','2026-05-17 11:46:01',120.00,26.40),(56,92,76,114,'ORD-2026-00007','2026-05-17','FULFILLED','EUR',24.40,'2026-05-17 15:05:32','2026-05-17 15:05:36',20.00,4.40),(57,92,76,115,'ORD-2026-00008','2026-05-18','FULFILLED','EUR',636.84,'2026-05-18 14:13:15','2026-05-18 14:13:24',522.00,114.84),(58,92,76,116,'ORD-2026-00009','2026-05-18','FULFILLED','EUR',146.40,'2026-05-18 20:17:00','2026-05-18 20:17:03',120.00,26.40),(59,92,76,117,'ORD-2026-00010','2026-05-19','FULFILLED','EUR',146.40,'2026-05-19 19:19:22','2026-05-19 19:19:26',120.00,26.40),(60,92,76,118,'ORD-2026-00011','2026-05-19','FULFILLED','EUR',146.40,'2026-05-19 19:35:08','2026-05-19 19:35:14',120.00,26.40),(61,93,76,119,'ORD-2026-00001','2026-05-20','FULFILLED','EUR',146.40,'2026-05-20 11:57:51','2026-05-20 11:57:57',120.00,26.40),(62,93,76,120,'ORD-2026-00002','2026-05-20','FULFILLED','EUR',146.40,'2026-05-20 12:15:27','2026-05-20 12:15:31',120.00,26.40),(63,93,76,125,'ORD-2026-00003','2026-05-21','FULFILLED','EUR',12.20,'2026-05-21 19:33:22','2026-05-21 19:34:00',10.00,2.20),(64,93,76,124,'ORD-2026-00004','2026-05-21','FULFILLED','EUR',85.40,'2026-05-21 20:09:42','2026-05-21 20:09:51',70.00,15.40),(65,93,76,126,'ORD-2026-00005','2026-05-22','FULFILLED','EUR',719.80,'2026-05-22 06:45:14','2026-05-22 06:45:19',590.00,129.80),(66,93,76,122,'ORD-2026-00006','2026-05-23','FULFILLED','EUR',719.80,'2026-05-23 10:01:24','2026-05-23 10:01:29',590.00,129.80),(67,93,76,127,'ORD-2026-00007','2026-05-23','FULFILLED','EUR',719.80,'2026-05-23 15:30:55','2026-05-23 15:31:06',590.00,129.80),(68,93,76,128,'ORD-2026-00008','2026-05-24','FULFILLED','EUR',719.80,'2026-05-24 03:52:22','2026-05-24 03:52:27',590.00,129.80),(69,93,75,129,'ORD-2026-00009','2026-05-25','FULFILLED','EUR',122.00,'2026-05-25 08:06:20','2026-05-25 08:06:29',100.00,22.00),(70,93,76,130,'ORD-2026-00010','2026-05-25','FULFILLED','EUR',183.00,'2026-05-25 08:12:56','2026-05-25 08:13:00',150.00,33.00),(71,93,76,131,'ORD-2026-00011','2026-05-25','FULFILLED','EUR',183.00,'2026-05-25 08:24:30','2026-05-25 08:24:34',150.00,33.00),(72,93,76,132,'ORD-2026-00012','2026-05-25','FULFILLED','EUR',183.00,'2026-05-25 08:45:15','2026-05-25 08:45:19',150.00,33.00),(73,93,75,133,'ORD-2026-00013','2026-05-25','FULFILLED','EUR',183.00,'2026-05-25 08:54:46','2026-05-25 08:54:49',150.00,33.00),(74,93,76,134,'ORD-2026-00014','2026-05-25','FULFILLED','EUR',177.00,'2026-05-25 09:21:11','2026-05-25 09:21:14',150.00,27.00),(75,94,76,135,'SO-00001','2026-06-02','FULFILLED','EUR',12.20,'2026-06-02 19:07:08','2026-06-02 19:07:45',10.00,2.20),(76,94,75,136,'SO-00002','2026-06-03','FULFILLED','EUR',12.20,'2026-06-03 03:32:15','2026-06-03 03:33:31',10.00,2.20),(77,95,76,137,'SO-00001','2026-06-03','FULFILLED','EUR',34.16,'2026-06-03 14:06:16','2026-06-03 14:10:36',28.00,6.16),(78,96,76,138,'SO-00001','2026-06-03','FULFILLED','EUR',24.40,'2026-06-03 21:42:25','2026-06-03 21:42:35',20.00,4.40),(79,96,76,139,'SO-00002','2026-06-03','FULFILLED','EUR',24.40,'2026-06-03 22:58:45','2026-06-03 22:58:48',20.00,4.40);
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
) ENGINE=InnoDB AUTO_INCREMENT=102 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sales_order_line`
--

LOCK TABLES `sales_order_line` WRITE;
/*!40000 ALTER TABLE `sales_order_line` DISABLE KEYS */;
INSERT INTO `sales_order_line` VALUES (15,74,8,1,'Modulo CRM',1.000,1000.00,900.00,'2026-04-28 20:08:48','2026-04-28 20:08:48',10.00,22.00,198.00,NULL),(16,74,8,2,'Setup',1.000,200.00,200.00,'2026-04-28 20:08:48','2026-04-28 20:08:48',0.00,22.00,44.00,NULL),(17,75,9,1,'Modulo CRM',1.000,1000.00,1000.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,220.00,NULL),(18,75,10,1,'Setup',1.000,200.00,200.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,44.00,NULL),(19,76,11,1,'Servizio Altro Tenant',1.000,500.00,500.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,110.00,NULL),(20,77,12,1,'Modulo CRM',1.000,1000.00,1000.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,220.00,NULL),(21,77,13,1,'Setup',1.000,200.00,200.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,44.00,NULL),(22,78,14,1,'Servizio Altro Tenant',1.000,500.00,500.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,110.00,NULL),(23,79,15,1,'Modulo CRM',1.000,1000.00,1000.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,220.00,NULL),(24,79,16,1,'Setup',1.000,200.00,200.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,44.00,NULL),(25,80,17,1,'Servizio Altro Tenant',1.000,500.00,500.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,110.00,NULL),(26,81,18,1,'Modulo CRM',1.000,1000.00,1000.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,220.00,NULL),(27,81,19,1,'Setup',1.000,200.00,200.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,44.00,NULL),(28,82,20,1,'Servizio Altro Tenant',1.000,500.00,500.00,'2026-04-29 04:12:04','2026-04-29 04:12:04',0.00,22.00,110.00,NULL),(29,83,21,1,'Modulo CRM',1.000,1000.00,1000.00,'2026-04-29 04:12:05','2026-04-29 04:12:05',0.00,22.00,220.00,NULL),(30,83,22,1,'Setup',1.000,200.00,200.00,'2026-04-29 04:12:05','2026-04-29 04:12:05',0.00,22.00,44.00,NULL),(31,84,23,1,'Servizio Altro Tenant',1.000,500.00,500.00,'2026-04-29 04:12:05','2026-04-29 04:12:05',0.00,22.00,110.00,NULL),(32,70,24,1,'Modulo CRM',1.000,1000.00,900.00,'2026-04-29 20:08:14','2026-04-29 20:08:14',10.00,22.00,198.00,NULL),(33,70,24,2,'Setup',1.000,200.00,200.00,'2026-04-29 20:08:14','2026-04-29 20:08:14',0.00,22.00,44.00,NULL),(34,73,25,1,'Modulo CRM',1.000,1000.00,900.00,'2026-04-29 20:26:18','2026-04-29 20:26:18',10.00,22.00,198.00,NULL),(35,73,25,2,'Setup',1.000,200.00,200.00,'2026-04-29 20:26:18','2026-04-29 20:26:18',0.00,22.00,44.00,NULL),(38,84,27,1,'RIGA 1',1.000,100.00,90.00,'2026-05-01 15:56:24','2026-05-01 15:56:24',10.00,22.00,19.80,NULL),(39,84,27,2,'RIGA 2',1.000,500.00,500.00,'2026-05-01 15:56:24','2026-05-01 15:56:24',0.00,22.00,110.00,NULL),(40,84,27,3,'RIGA 3',1.000,2000.00,1600.00,'2026-05-01 15:56:24','2026-05-01 15:56:24',20.00,22.00,352.00,NULL),(41,75,28,1,'RIGA 1',1.000,100.00,90.00,'2026-05-01 18:05:04','2026-05-01 18:05:04',10.00,22.00,19.80,NULL),(44,75,29,1,'RIGA 1',1.000,100.00,109.80,'2026-05-02 04:47:40','2026-05-02 04:47:40',10.00,22.00,19.80,NULL),(45,75,29,2,'RIGA 2',1.000,50.00,61.00,'2026-05-02 04:47:40','2026-05-02 04:47:40',0.00,22.00,11.00,NULL),(46,85,30,1,'RIGA 1',1.000,580.00,580.00,'2026-05-06 21:25:11','2026-05-06 21:25:11',0.00,22.00,127.60,NULL),(47,85,31,1,'RIGA 1',1.000,580.00,464.00,'2026-05-07 00:10:48','2026-05-07 00:10:48',20.00,22.00,102.08,2),(48,86,32,1,'RIGA 1',10.000,580.00,5220.00,'2026-05-07 01:29:52','2026-05-07 01:29:52',10.00,22.00,1148.40,2),(49,86,33,1,'RIGA 1',1.000,1200.00,1080.00,'2026-05-07 01:54:10','2026-05-07 01:54:10',10.00,22.00,237.60,1),(50,87,34,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',20.000,580.00,11600.00,'2026-05-08 02:52:17','2026-05-08 02:52:17',0.00,22.00,2552.00,2),(51,87,35,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',5.000,580.00,2900.00,'2026-05-08 16:18:01','2026-05-08 16:18:01',0.00,22.00,638.00,2),(52,87,36,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,100.00,'2026-05-08 19:02:01','2026-05-08 19:02:01',0.00,22.00,22.00,2),(53,87,37,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,580.00,'2026-05-09 07:34:07','2026-05-09 07:34:07',0.00,22.00,127.60,2),(54,87,38,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,580.00,'2026-05-09 12:22:43','2026-05-09 12:22:43',0.00,22.00,127.60,2),(55,88,39,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,580.00,'2026-05-09 13:29:56','2026-05-09 13:29:56',0.00,22.00,127.60,2),(56,88,40,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,580.00,'2026-05-09 14:14:39','2026-05-09 14:14:39',0.00,22.00,127.60,2),(57,88,41,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,580.00,'2026-05-10 12:03:03','2026-05-10 12:03:03',0.00,22.00,127.60,2),(58,88,42,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,580.00,'2026-05-10 12:35:47','2026-05-10 12:35:47',0.00,22.00,127.60,2),(59,88,43,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,580.00,'2026-05-10 13:11:40','2026-05-10 13:11:40',0.00,22.00,127.60,2),(60,88,44,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,580.00,'2026-05-10 18:06:19','2026-05-10 18:06:19',0.00,22.00,127.60,2),(61,90,45,1,'Modulo CRM',1.000,1200.00,1200.00,'2026-05-13 07:48:42','2026-05-13 07:48:42',0.00,22.00,264.00,1),(62,90,46,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,580.00,'2026-05-13 18:49:04','2026-05-13 18:49:04',0.00,22.00,127.60,2),(63,91,47,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,580.00,'2026-05-15 06:29:31','2026-05-15 06:29:31',0.00,22.00,127.60,2),(64,91,48,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,580.00,5800.00,'2026-05-16 02:15:37','2026-05-16 02:15:37',0.00,22.00,1276.00,2),(65,91,49,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,120.00,'2026-05-16 21:53:11','2026-05-16 21:53:11',0.00,22.00,26.40,2),(66,92,50,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,120.00,'2026-05-17 01:10:17','2026-05-17 01:10:17',0.00,22.00,26.40,2),(67,92,51,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,120.00,'2026-05-17 01:28:35','2026-05-17 01:28:35',0.00,22.00,26.40,2),(68,92,52,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,120.00,'2026-05-17 01:32:41','2026-05-17 01:32:41',0.00,22.00,26.40,2),(69,92,53,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,120.00,'2026-05-17 02:04:38','2026-05-17 02:04:38',0.00,22.00,26.40,2),(70,92,54,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,120.00,'2026-05-17 02:12:58','2026-05-17 02:12:58',0.00,22.00,26.40,2),(71,92,55,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,120.00,'2026-05-17 11:45:57','2026-05-17 11:45:57',0.00,22.00,26.40,2),(72,92,56,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',2.000,10.00,20.00,'2026-05-17 15:05:32','2026-05-17 15:05:32',0.00,22.00,4.40,2),(73,92,57,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,522.00,'2026-05-18 14:13:15','2026-05-18 14:13:15',10.00,22.00,114.84,2),(74,92,58,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,120.00,'2026-05-18 20:17:00','2026-05-18 20:17:00',0.00,22.00,26.40,2),(75,92,59,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,120.00,'2026-05-19 19:19:22','2026-05-19 19:19:22',0.00,22.00,26.40,2),(76,92,60,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,120.00,'2026-05-19 19:35:08','2026-05-19 19:35:08',0.00,22.00,26.40,2),(77,93,61,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,120.00,'2026-05-20 11:57:51','2026-05-20 11:57:51',0.00,22.00,26.40,2),(78,93,62,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',12.000,10.00,120.00,'2026-05-20 12:15:27','2026-05-20 12:15:27',0.00,22.00,26.40,2),(79,93,63,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,10.00,10.00,'2026-05-21 19:33:22','2026-05-21 19:33:22',0.00,22.00,2.20,2),(80,93,64,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,70.00,70.00,'2026-05-21 20:09:42','2026-05-21 20:09:42',0.00,22.00,15.40,2),(81,93,65,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,590.00,590.00,'2026-05-22 06:45:14','2026-05-22 06:45:14',0.00,22.00,129.80,2),(82,93,66,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,590.00,590.00,'2026-05-23 10:01:24','2026-05-23 10:01:24',0.00,22.00,129.80,2),(83,93,67,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,590.00,590.00,'2026-05-23 15:30:55','2026-05-23 15:30:55',0.00,22.00,129.80,2),(84,93,68,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,590.00,590.00,'2026-05-24 03:52:22','2026-05-24 03:52:22',0.00,22.00,129.80,2),(85,93,69,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,100.00,'2026-05-25 08:06:20','2026-05-25 08:06:20',0.00,22.00,22.00,2),(86,93,70,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,100.00,'2026-05-25 08:12:56','2026-05-25 08:12:56',0.00,22.00,22.00,2),(87,93,70,2,'Modulo CRM',1.000,50.00,50.00,'2026-05-25 08:12:56','2026-05-25 08:12:56',0.00,22.00,11.00,1),(88,93,71,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,100.00,'2026-05-25 08:24:30','2026-05-25 08:24:30',0.00,22.00,22.00,2),(89,93,71,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,50.00,50.00,'2026-05-25 08:24:30','2026-05-25 08:24:30',0.00,22.00,11.00,2),(90,93,72,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,100.00,'2026-05-25 08:45:15','2026-05-25 08:45:15',0.00,22.00,22.00,2),(91,93,72,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,50.00,50.00,'2026-05-25 08:45:15','2026-05-25 08:45:15',0.00,22.00,11.00,2),(92,93,73,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,100.00,'2026-05-25 08:54:46','2026-05-25 08:54:46',0.00,22.00,22.00,2),(93,93,73,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,50.00,50.00,'2026-05-25 08:54:46','2026-05-25 08:54:46',0.00,22.00,11.00,2),(94,93,74,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,100.00,'2026-05-25 09:21:11','2026-05-25 09:21:11',0.00,22.00,22.00,2),(95,93,74,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,50.00,50.00,'2026-05-25 09:21:11','2026-05-25 09:21:11',0.00,10.00,5.00,2),(96,94,75,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,10.00,10.00,'2026-06-02 19:07:08','2026-06-02 19:07:08',0.00,22.00,2.20,2),(97,94,76,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,10.00,10.00,'2026-06-03 03:32:15','2026-06-03 03:32:15',0.00,22.00,2.20,2),(98,95,77,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,20.00,18.00,'2026-06-03 14:06:16','2026-06-03 14:06:16',10.00,22.00,3.96,2),(99,95,77,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,10.00,10.00,'2026-06-03 14:06:16','2026-06-03 14:06:16',0.00,22.00,2.20,2),(100,96,78,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,20.00,20.00,'2026-06-03 21:42:25','2026-06-03 21:42:25',0.00,22.00,4.40,2),(101,96,79,1,'FORNO ELETTRICO ALIMENTATO A 220V-230V',2.000,10.00,20.00,'2026-06-03 22:58:45','2026-06-03 22:58:45',0.00,22.00,4.40,2);
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplier`
--

LOCK TABLES `supplier` WRITE;
/*!40000 ALTER TABLE `supplier` DISABLE KEYS */;
INSERT INTO `supplier` VALUES (1,96,'SUP-001','Fornitore Demo S.R.L','IT0984327898','LBSMRC69A04F457F','fornitore@example.com','+393476681161','VIA GIAN DOMENICO ROMAGNOSI 11','TERNI','05100','TR','IT','PROVA FORNITORE DEMO',1,'2026-05-07 09:57:56','2026-05-08 01:00:29'),(2,96,'SUP-002','Fornitore Demo','IT0984329756','LBSMRC69A04F457F','supplier@example.com','+393311675741','VIA GIAN DOMENICO ROMAGNOSI 11','TERNI','05100','TR','IT','DEMO',1,'2026-05-11 13:08:12','2026-05-11 13:09:49');
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
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplier_invoice`
--

LOCK TABLES `supplier_invoice` WRITE;
/*!40000 ALTER TABLE `supplier_invoice` DISABLE KEYS */;
INSERT INTO `supplier_invoice` VALUES (1,87,'SI-00001','2026-05-08',1,2,2,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00002',23200.00,5104.00,28304.00,'2026-05-08 01:17:28','2026-05-08 01:17:53'),(2,87,'SI-00002','2026-05-08',1,3,3,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00003',4640.00,1020.80,5660.80,'2026-05-08 03:06:42','2026-05-08 03:06:42'),(3,87,'SI-00003','2026-05-08',1,4,4,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00004',5798.84,1275.74,7074.58,'2026-05-08 14:21:23','2026-05-08 14:21:23'),(4,87,'SI-00004','2026-05-09',1,5,5,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00005',580.00,127.60,707.60,'2026-05-09 05:35:11','2026-05-09 05:35:11'),(5,88,'SI-00005','2026-05-09',1,6,6,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00006',1160.00,255.20,1415.20,'2026-05-09 20:15:03','2026-05-09 20:15:04'),(6,88,'SI-00006','2026-05-10',1,7,7,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00007',580.00,127.60,707.60,'2026-05-10 11:20:53','2026-05-10 11:20:53'),(7,88,'SI-00007','2026-05-10',1,8,8,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00008',580.00,127.60,707.60,'2026-05-10 16:15:01','2026-05-10 16:15:01'),(8,90,'SI-00008','2026-05-14',2,10,9,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00010',1780.00,391.60,2171.60,'2026-05-13 22:22:29','2026-05-13 22:22:30'),(9,90,'SI-00009','2026-05-14',1,9,10,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00009',580.00,127.60,707.60,'2026-05-13 22:24:35','2026-05-13 22:24:36'),(10,91,'SI-00010','2026-05-15',2,11,11,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00011',5800.00,1276.00,7076.00,'2026-05-15 04:37:53','2026-05-15 04:37:53'),(11,91,'SI-00011','2026-05-16',2,12,12,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00012',6380.00,1403.60,7783.60,'2026-05-15 22:19:10','2026-05-15 22:19:10'),(12,91,'SI-00012','2026-05-16',2,13,13,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00013',5800.00,1276.00,7076.00,'2026-05-15 23:02:40','2026-05-15 23:02:40'),(13,91,'SI-00013','2026-05-16',2,16,16,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00016',5800.00,1276.00,7076.00,'2026-05-16 00:13:25','2026-05-16 00:13:25'),(14,92,'SI-00014','2026-05-17',2,38,37,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00033',50.00,11.00,61.00,'2026-05-16 23:07:28','2026-05-16 23:07:28'),(15,92,'SI-00015','2026-05-17',2,39,38,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00034',70.00,15.40,85.40,'2026-05-16 23:08:30','2026-05-16 23:08:30'),(16,92,'SI-00016','2026-05-17',2,40,39,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00035',50.00,11.00,61.00,'2026-05-17 00:02:40','2026-05-17 00:02:40'),(17,92,'SI-00017','2026-05-17',2,41,40,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00036',70.00,15.40,85.40,'2026-05-17 00:03:42','2026-05-17 00:03:42'),(18,92,'SI-00018','2026-05-17',2,43,42,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00038',70.00,15.40,85.40,'2026-05-17 00:12:13','2026-05-17 00:12:13'),(19,92,'SI-00019','2026-05-17',2,44,43,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00039',50.00,11.00,61.00,'2026-05-17 09:44:15','2026-05-17 09:44:15'),(20,92,'SI-00020','2026-05-17',2,45,44,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00040',70.00,15.40,85.40,'2026-05-17 09:45:06','2026-05-17 09:45:07'),(21,92,'SI-00021','2026-05-18',2,46,45,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00041',1044.00,229.68,1273.68,'2026-05-18 12:24:41','2026-05-18 12:24:41'),(22,93,'SI-00022','2026-05-23',2,57,65,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00052',590.00,129.80,719.80,'2026-05-22 23:30:12','2026-05-22 23:30:12'),(23,93,'SI-00023','2026-05-23',2,58,66,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00053',1200.00,264.00,1464.00,'2026-05-22 23:33:44','2026-05-22 23:33:44'),(24,93,'SI-00024','2026-05-23',2,59,67,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00054',590.00,129.80,719.80,'2026-05-23 13:54:07','2026-05-23 13:54:07'),(25,93,'SI-00025','2026-05-24',2,60,68,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00055',590.00,129.80,719.80,'2026-05-24 04:12:48','2026-05-24 04:12:48'),(26,93,'SI-00026','2026-05-25',2,61,69,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00056',100.00,22.00,122.00,'2026-05-25 07:26:54','2026-05-25 07:26:54'),(27,93,'SI-00027','2026-05-25',2,62,70,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00057',150.00,33.00,183.00,'2026-05-25 07:29:06','2026-05-25 07:29:06'),(28,93,'SI-00028','2026-05-25',2,63,71,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00058',150.00,24.50,174.50,'2026-05-25 07:30:50','2026-05-25 07:30:50'),(29,94,'SI-00001','2026-06-03',2,64,72,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00001',590.00,129.80,719.80,'2026-06-03 11:04:40','2026-06-03 11:04:40'),(30,95,'SI-00001','2026-06-03',2,69,73,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00005',28.00,6.16,34.16,'2026-06-03 15:14:12','2026-06-03 15:14:13'),(31,95,'SI-00002','2026-06-03',1,70,74,'DRAFT','EUR','Fattura fornitore generata da ricezione merci GR-00006',590.00,129.80,719.80,'2026-06-03 15:21:25','2026-06-03 15:21:25');
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
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplier_invoice_line`
--

LOCK TABLES `supplier_invoice_line` WRITE;
/*!40000 ALTER TABLE `supplier_invoice_line` DISABLE KEYS */;
INSERT INTO `supplier_invoice_line` VALUES (1,87,1,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',50.000,580.00,20.00,22.00,23200.00,5104.00,28304.00,'2026-05-08 01:17:39','2026-05-08 01:17:39'),(2,87,2,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,580.00,20.00,22.00,4640.00,1020.80,5660.80,'2026-05-08 03:06:42','2026-05-08 03:06:42'),(3,87,3,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',9.998,580.00,0.00,22.00,5798.84,1275.74,7074.58,'2026-05-08 14:21:23','2026-05-08 14:21:23'),(4,87,4,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,127.60,707.60,'2026-05-09 05:35:11','2026-05-09 05:35:11'),(5,88,5,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',2.000,580.00,0.00,22.00,1160.00,255.20,1415.20,'2026-05-09 20:15:03','2026-05-09 20:15:03'),(6,88,6,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,127.60,707.60,'2026-05-10 11:20:53','2026-05-10 11:20:53'),(7,88,7,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,127.60,707.60,'2026-05-10 16:15:01','2026-05-10 16:15:01'),(8,90,8,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,127.60,707.60,'2026-05-13 22:22:29','2026-05-13 22:22:29'),(9,90,8,2,1,'Modulo CRM',1.000,1200.00,0.00,22.00,1200.00,264.00,1464.00,'2026-05-13 22:22:29','2026-05-13 22:22:29'),(10,90,9,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,580.00,0.00,22.00,580.00,127.60,707.60,'2026-05-13 22:24:35','2026-05-13 22:24:36'),(11,91,10,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,580.00,0.00,22.00,5800.00,1276.00,7076.00,'2026-05-15 04:37:53','2026-05-15 04:37:53'),(12,91,11,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',11.000,580.00,0.00,22.00,6380.00,1403.60,7783.60,'2026-05-15 22:19:10','2026-05-15 22:19:10'),(13,91,12,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,580.00,0.00,22.00,5800.00,1276.00,7076.00,'2026-05-15 23:02:40','2026-05-15 23:02:40'),(14,91,13,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,580.00,0.00,22.00,5800.00,1276.00,7076.00,'2026-05-16 00:13:25','2026-05-16 00:13:25'),(15,92,14,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-16 23:07:28','2026-05-16 23:07:28'),(16,92,15,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,7.00,0.00,22.00,70.00,15.40,85.40,'2026-05-16 23:08:30','2026-05-16 23:08:30'),(17,92,16,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-17 00:02:40','2026-05-17 00:02:40'),(18,92,17,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,7.00,0.00,22.00,70.00,15.40,85.40,'2026-05-17 00:03:42','2026-05-17 00:03:42'),(19,92,18,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,7.00,0.00,22.00,70.00,15.40,85.40,'2026-05-17 00:12:13','2026-05-17 00:12:13'),(20,92,19,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,5.00,0.00,22.00,50.00,11.00,61.00,'2026-05-17 09:44:15','2026-05-17 09:44:15'),(21,92,20,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',10.000,7.00,0.00,22.00,70.00,15.40,85.40,'2026-05-17 09:45:06','2026-05-17 09:45:06'),(22,92,21,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',2.000,580.00,10.00,22.00,1044.00,229.68,1273.68,'2026-05-18 12:24:41','2026-05-18 12:24:41'),(23,93,22,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,590.00,0.00,22.00,590.00,129.80,719.80,'2026-05-22 23:30:12','2026-05-22 23:30:12'),(24,93,23,1,1,'Modulo CRM',1.000,1200.00,0.00,22.00,1200.00,264.00,1464.00,'2026-05-22 23:33:44','2026-05-22 23:33:44'),(25,93,24,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,590.00,0.00,22.00,590.00,129.80,719.80,'2026-05-23 13:54:07','2026-05-23 13:54:07'),(26,93,25,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,590.00,0.00,22.00,590.00,129.80,719.80,'2026-05-24 04:12:48','2026-05-24 04:12:48'),(27,93,26,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,0.00,22.00,100.00,22.00,122.00,'2026-05-25 07:26:54','2026-05-25 07:26:54'),(28,93,27,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,0.00,22.00,100.00,22.00,122.00,'2026-05-25 07:29:06','2026-05-25 07:29:06'),(29,93,27,2,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,50.00,0.00,22.00,50.00,11.00,61.00,'2026-05-25 07:29:06','2026-05-25 07:29:06'),(30,93,28,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,100.00,0.00,22.00,100.00,22.00,122.00,'2026-05-25 07:30:50','2026-05-25 07:30:50'),(31,93,28,2,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,50.00,0.00,5.00,50.00,2.50,52.50,'2026-05-25 07:30:50','2026-05-25 07:30:50'),(32,94,29,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,590.00,0.00,22.00,590.00,129.80,719.80,'2026-06-03 11:04:40','2026-06-03 11:04:40'),(33,95,30,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,20.00,10.00,22.00,18.00,3.96,21.96,'2026-06-03 15:14:12','2026-06-03 15:14:12'),(34,95,30,2,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,10.00,0.00,22.00,10.00,2.20,12.20,'2026-06-03 15:14:12','2026-06-03 15:14:12'),(35,95,31,1,2,'FORNO ELETTRICO ALIMENTATO A 220V-230V',1.000,590.00,0.00,22.00,590.00,129.80,719.80,'2026-06-03 15:21:25','2026-06-03 15:21:25');
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `uc_tenant_email` (`email`),
  UNIQUE KEY `uc_tenant_slug` (`slug`)
) ENGINE=InnoDB AUTO_INCREMENT=98 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tenant`
--

LOCK TABLES `tenant` WRITE;
/*!40000 ALTER TABLE `tenant` DISABLE KEYS */;
INSERT INTO `tenant` VALUES (70,'Tenant Test','tenant-test-57014885898041','tenant57014885906083@example.com','ACTIVE','it','EUR','2026-04-27 01:29:26','2026-04-27 01:29:26','FIFO'),(71,'Tenant Test','tenant-test-57014887438916','tenant57014887442708@example.com','ACTIVE','it','EUR','2026-04-27 01:29:26','2026-04-27 01:29:26','FIFO'),(72,'Tenant Test','tenant-test-6346922231416','tenant6346922301541@example.com','ACTIVE','it','EUR','2026-04-27 19:58:16','2026-04-27 19:58:16','FIFO'),(73,'Tenant Test','tenant-test-23734427062791','tenant23734427125416@example.com','ACTIVE','it','EUR','2026-04-28 18:46:25','2026-04-28 18:46:25','FIFO'),(74,'Tenant Test','tenant-test-28677502495958','tenant28677502542916@example.com','ACTIVE','it','EUR','2026-04-28 20:08:48','2026-04-28 20:08:48','FIFO'),(75,'Tenant Test','tenant-test-4009166646958','tenant4009166698708@example.com','ACTIVE','it','EUR','2026-04-29 04:12:04','2026-04-29 04:12:04','FIFO'),(76,'Tenant Test','tenant-test-4009227100916','tenant4009227105833@example.com','ACTIVE','it','EUR','2026-04-29 04:12:04','2026-04-29 04:12:04','FIFO'),(77,'Tenant Test','tenant-test-4009395190166','tenant4009395198375@example.com','ACTIVE','it','EUR','2026-04-29 04:12:04','2026-04-29 04:12:04','FIFO'),(78,'Tenant Test','tenant-test-4009397500458','tenant4009397505041@example.com','ACTIVE','it','EUR','2026-04-29 04:12:04','2026-04-29 04:12:04','FIFO'),(79,'Tenant Test','tenant-test-4009470057416','tenant4009470064750@example.com','ACTIVE','it','EUR','2026-04-29 04:12:04','2026-04-29 04:12:04','FIFO'),(80,'Tenant Test','tenant-test-4009472465500','tenant4009472469666@example.com','ACTIVE','it','EUR','2026-04-29 04:12:04','2026-04-29 04:12:04','FIFO'),(81,'Tenant Test','tenant-test-4009539012041','tenant4009539019958@example.com','ACTIVE','it','EUR','2026-04-29 04:12:04','2026-04-29 04:12:04','FIFO'),(82,'Tenant Test','tenant-test-4009541103708','tenant4009541107791@example.com','ACTIVE','it','EUR','2026-04-29 04:12:04','2026-04-29 04:12:04','FIFO'),(83,'Tenant Test','tenant-test-4009606512333','tenant4009606520250@example.com','ACTIVE','it','EUR','2026-04-29 04:12:04','2026-04-29 04:12:04','FIFO'),(84,'Tenant Test','tenant-test-4009608435583','tenant4009608439958@example.com','ACTIVE','it','EUR','2026-04-29 04:12:04','2026-04-29 04:12:04','FIFO'),(85,'Azienda Demo','azienda-demo','malbasini@gmail.com','ACTIVE','it','EUR','2026-05-05 05:42:49','2026-05-05 05:42:49','FIFO'),(86,'Azienda Demo S.P.A.','azienda-demo-s-p-a','malbasini@outlook.it','ACTIVE','it','EUR','2026-05-07 01:20:11','2026-05-07 01:20:11','FIFO'),(87,'Verdi Spa','verdi-spa','info@verdi.com','ACTIVE','it','EUR','2026-05-08 02:46:05','2026-05-08 02:46:05','FIFO'),(88,'Bianchi SRL','bianchi-srl','info@bianchi.com','ACTIVE','it','EUR','2026-05-09 13:26:17','2026-05-09 13:26:17','FIFO'),(89,'Other','other','marco.albasini@pec.it','ACTIVE','it','EUR','2026-05-13 02:01:14','2026-05-13 02:01:14','FIFO'),(90,'Verdi Spa','verdi-spa-2','info@verdi.it','ACTIVE','it','EUR','2026-05-13 02:04:33','2026-05-13 02:04:33','FIFO'),(91,'Azienda Prova','azienda-prova','info@prova.it','ACTIVE','it','EUR','2026-05-14 19:54:09','2026-05-14 19:54:09','FIFO'),(92,'Rossi Spa','rossi-spa','info@rossi.gov','ACTIVE','it','EUR','2026-05-16 23:45:51','2026-05-19 18:45:37','AVERAGE'),(93,'Rossi Snc','rossi-snc','example@rossi.it','ACTIVE','it','EUR','2026-05-20 11:49:39','2026-05-20 12:12:14','LIFO'),(94,'Rossi Srl','rossi-srl','info@roddi.edu','ACTIVE','it','EUR','2026-06-02 18:31:35','2026-06-02 18:31:35','LIFO'),(95,'Bianchi SPA','bianchi-spa','info@bianchi.it','ACTIVE','it','EUR','2026-06-03 13:17:26','2026-06-03 13:17:26','LIFO'),(96,'Bianchi SNC','bianchi-snc','info@example.com','ACTIVE','it','EUR','2026-06-03 21:37:41','2026-06-03 21:37:41','LIFO'),(97,'Neri SPA','neri-spa','info@neri.it','ACTIVE','it','EUR','2026-06-04 12:51:45','2026-06-04 12:51:45','LIFO');
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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES (1,'2026-05-05 05:42:48.723456','2026-05-05 05:42:48.723462',85,1,1),(2,'2026-05-07 01:20:10.841537','2026-05-07 01:20:10.841544',86,2,2),(3,'2026-05-08 02:46:04.646724','2026-05-08 02:46:04.646729',87,3,3),(4,'2026-05-09 13:26:16.986104','2026-05-09 13:26:16.986109',88,4,4),(5,'2026-05-13 02:01:14.575065','2026-05-13 02:01:14.575070',89,5,5),(6,'2026-05-13 02:04:33.390213','2026-05-13 02:04:33.390218',90,6,6),(7,'2026-05-14 19:54:08.868494','2026-05-14 19:54:08.868499',91,7,7),(8,'2026-05-16 23:45:51.438322','2026-05-16 23:45:51.438327',92,8,8),(9,'2026-05-20 11:49:38.639693','2026-05-20 11:49:38.639697',93,9,9),(10,'2026-06-02 18:31:35.719162','2026-06-02 18:31:35.719176',94,10,10),(11,'2026-06-03 13:17:25.847143','2026-06-03 13:17:25.847147',95,11,11),(12,'2026-06-03 21:37:41.461544','2026-06-03 21:37:41.461550',96,12,12),(13,'2026-06-04 12:51:45.461738','2026-06-04 12:51:45.461742',97,13,13);
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

-- Dump completed on 2026-06-04 12:55:48
