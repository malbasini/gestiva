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
) ENGINE=InnoDB AUTO_INCREMENT=191 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,0,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(2,0,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(3,0,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(4,0,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(5,0,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(6,0,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(7,0,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(8,0,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(9,0,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(10,0,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(11,0,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(12,0,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(13,0,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(14,0,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(15,0,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(16,0,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(17,0,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(18,0,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(19,0,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-09 21:00:20','2026-05-09 21:00:20'),(20,91,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:54:51','2026-05-14 17:54:51'),(21,91,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:03','2026-05-14 17:55:03'),(22,91,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(23,91,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(24,91,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(25,91,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(26,91,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(27,91,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(28,91,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(29,91,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(30,91,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(31,91,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(32,91,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(33,91,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(34,91,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(35,91,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(36,91,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(37,91,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(38,91,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-14 17:55:15','2026-05-14 17:55:15'),(39,92,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(40,92,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(41,92,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(42,92,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(43,92,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(44,92,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(45,92,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(46,92,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(47,92,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(48,92,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(49,92,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(50,92,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(51,92,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(52,92,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(53,92,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(54,92,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(55,92,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(56,92,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(57,92,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-16 21:45:51','2026-05-16 21:45:51'),(58,93,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(59,93,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(60,93,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(61,93,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(62,93,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(63,93,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(64,93,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(65,93,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(66,93,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(67,93,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(68,93,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(69,93,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(70,93,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(71,93,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(72,93,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(73,93,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(74,93,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(75,93,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(76,93,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-05-20 09:49:39','2026-05-20 09:49:39'),(77,94,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(78,94,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(79,94,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(80,94,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(81,94,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(82,94,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(83,94,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(84,94,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(85,94,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(86,94,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(87,94,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(88,94,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(89,94,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(90,94,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(91,94,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(92,94,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(93,94,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(94,94,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(95,94,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-02 16:31:36','2026-06-02 16:31:36'),(96,95,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(97,95,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(98,95,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(99,95,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(100,95,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(101,95,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(102,95,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(103,95,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(104,95,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(105,95,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(106,95,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(107,95,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(108,95,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(109,95,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(110,95,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(111,95,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(112,95,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(113,95,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(114,95,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 11:17:26','2026-06-03 11:17:26'),(115,96,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(116,96,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(117,96,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(118,96,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(119,96,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(120,96,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(121,96,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(122,96,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(123,96,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(124,96,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(125,96,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(126,96,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(127,96,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(128,96,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(129,96,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(130,96,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(131,96,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(132,96,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(133,96,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-03 19:37:41','2026-06-03 19:37:41'),(134,97,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(135,97,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(136,97,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(137,97,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(138,97,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(139,97,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(140,97,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(141,97,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(142,97,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(143,97,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(144,97,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(145,97,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(146,97,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(147,97,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(148,97,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(149,97,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(150,97,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(151,97,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(152,97,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-04 10:51:45','2026-06-04 10:51:45'),(153,98,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(154,98,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(155,98,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(156,98,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(157,98,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(158,98,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(159,98,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(160,98,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(161,98,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(162,98,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(163,98,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(164,98,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(165,98,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(166,98,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(167,98,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(168,98,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(169,98,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(170,98,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(171,98,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-05 19:48:01','2026-06-05 19:48:01'),(172,99,'1000','Attività','ASSET','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(173,99,'1100','Disponibilità liquide','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(174,99,'1110','Cassa','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(175,99,'1120','Banca','ASSET','DEBIT',2,3,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(176,99,'1200','Crediti commerciali','ASSET','DEBIT',1,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(177,99,'1210','Crediti verso clienti','ASSET','DEBIT',5,3,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(178,99,'2000','Passività','LIABILITY','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(179,99,'2100','Debiti commerciali','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(180,99,'2110','Debiti verso fornitori','LIABILITY','CREDIT',8,3,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(181,99,'2200','Debiti tributari','LIABILITY','CREDIT',7,2,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(182,99,'2210','IVA a debito','LIABILITY','CREDIT',10,3,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(183,99,'2220','IVA a credito','ASSET','DEBIT',1,2,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(184,99,'3000','Patrimonio netto','EQUITY','CREDIT',NULL,1,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(185,99,'4000','Ricavi','REVENUE','CREDIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(186,99,'4100','Ricavi da vendite','REVENUE','CREDIT',14,2,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(187,99,'5000','Costi','COST','DEBIT',NULL,1,_binary '\0',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(188,99,'5100','Acquisti merci','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(189,99,'5200','Costi per servizi','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49'),(190,99,'5300','Spese bancarie','COST','DEBIT',16,2,_binary '',_binary '',_binary '',NULL,'2026-06-07 01:41:49','2026-06-07 01:41:49');
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `accounting_entry`
--

LOCK TABLES `accounting_entry` WRITE;
/*!40000 ALTER TABLE `accounting_entry` DISABLE KEYS */;
truncate table `accounting_entry`;
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
truncate table `accounting_entry_line`;
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
truncate table `ai_conversation_log`;
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
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_user`
--

LOCK TABLES `app_user` WRITE;
/*!40000 ALTER TABLE `app_user` DISABLE KEYS */;
truncate table `app_user`;
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
truncate table `attachment`;
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
truncate table `audit_log`;
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
truncate table `company_profile`;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `company_settings`
--

LOCK TABLES `company_settings` WRITE;
/*!40000 ALTER TABLE `company_settings` DISABLE KEYS */;
truncate table `company_settings`;
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
truncate table `contact_person`;
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
truncate table `crm_activity`;
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
truncate table `crm_lead`;
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
truncate table `crm_note`;
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
) ENGINE=InnoDB AUTO_INCREMENT=91 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
truncate table `customer`;
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
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `delivery_note`
--

LOCK TABLES `delivery_note` WRITE;
/*!40000 ALTER TABLE `delivery_note` DISABLE KEYS */;
truncate table `delivery_note`;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `delivery_note_line`
--

LOCK TABLES `delivery_note_line` WRITE;
/*!40000 ALTER TABLE `delivery_note_line` DISABLE KEYS */;
truncate table `delivery_note_line`;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `document_sequence`
--

LOCK TABLES `document_sequence` WRITE;
/*!40000 ALTER TABLE `document_sequence` DISABLE KEYS */;
truncate table `document_sequence`;
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
truncate table `document_template`;
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
) ENGINE=InnoDB AUTO_INCREMENT=85 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `goods_receipt`
--

LOCK TABLES `goods_receipt` WRITE;
/*!40000 ALTER TABLE `goods_receipt` DISABLE KEYS */;
truncate table `goods_receipt`;
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
truncate table `goods_receipt_line`;
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
truncate table `inventory_average_balance`;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory_layer`
--

LOCK TABLES `inventory_layer` WRITE;
/*!40000 ALTER TABLE `inventory_layer` DISABLE KEYS */;
truncate table `inventory_layer`;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory_layer_consumption`
--

LOCK TABLES `inventory_layer_consumption` WRITE;
/*!40000 ALTER TABLE `inventory_layer_consumption` DISABLE KEYS */;
truncate table `inventory_layer_consumption`;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory_movement`
--

LOCK TABLES `inventory_movement` WRITE;
/*!40000 ALTER TABLE `inventory_movement` DISABLE KEYS */;
truncate table `inventory_movement`;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invoice`
--

LOCK TABLES `invoice` WRITE;
/*!40000 ALTER TABLE `invoice` DISABLE KEYS */;
truncate table `invoice`;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invoice_line`
--

LOCK TABLES `invoice_line` WRITE;
/*!40000 ALTER TABLE `invoice_line` DISABLE KEYS */;
truncate table `invoice_line`;
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `item`
--

LOCK TABLES `item` WRITE;
/*!40000 ALTER TABLE `item` DISABLE KEYS */;
truncate table `item`;
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
truncate table `journal_entry`;
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
truncate table `journal_entry_line`;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_due`
--

LOCK TABLES `payment_due` WRITE;
/*!40000 ALTER TABLE `payment_due` DISABLE KEYS */;
truncate table `payment_due`;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_due_transaction`
--

LOCK TABLES `payment_due_transaction` WRITE;
/*!40000 ALTER TABLE `payment_due_transaction` DISABLE KEYS */;
truncate table `payment_due_transaction`;
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
truncate table `payment_reminder`;
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
truncate table `payment_schedule`;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_transaction`
--

LOCK TABLES `payment_transaction` WRITE;
/*!40000 ALTER TABLE `payment_transaction` DISABLE KEYS */;
truncate table `payment_transaction`;
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
truncate table `permission`;
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
truncate table `persistent_logins`;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchase_order`
--

LOCK TABLES `purchase_order` WRITE;
/*!40000 ALTER TABLE `purchase_order` DISABLE KEYS */;
truncate table `purchase_order`;
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
truncate table `purchase_order_line`;
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
) ENGINE=InnoDB AUTO_INCREMENT=149 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quote`
--

LOCK TABLES `quote` WRITE;
/*!40000 ALTER TABLE `quote` DISABLE KEYS */;
truncate table `quote`;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quote_line`
--

LOCK TABLES `quote_line` WRITE;
/*!40000 ALTER TABLE `quote_line` DISABLE KEYS */;
truncate table `quote_line`;
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
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (15,'2026-06-05 21:48:00.664185','2026-06-05 21:48:00.664188',0,'ROLE_WAREHOUSE','ROLE_WAREHOUSE',_binary ''),(16,'2026-06-05 21:48:00.664185','2026-06-05 21:48:00.664185',0,'ROLE_SALES','ROLE_SALES',_binary ''),(17,'2026-06-05 21:48:00.664185','2026-06-05 21:48:00.664185',0,'ROLE_PURCHASING','ROLE_PURCHASING',_binary ''),(18,'2026-06-05 21:48:00.664185','2026-06-05 21:48:00.664185',0,'ROLE_ACCOUNTING','ROLE_ACCOUNTING',_binary '');
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
truncate table `role_permission`;
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
) ENGINE=InnoDB AUTO_INCREMENT=89 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sales_order`
--

LOCK TABLES `sales_order` WRITE;
/*!40000 ALTER TABLE `sales_order` DISABLE KEYS */;
truncate table `sales_order`;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sales_order_line`
--

LOCK TABLES `sales_order_line` WRITE;
/*!40000 ALTER TABLE `sales_order_line` DISABLE KEYS */;
truncate table `sales_order_line`;
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplier`
--

LOCK TABLES `supplier` WRITE;
/*!40000 ALTER TABLE `supplier` DISABLE KEYS */;
truncate table `supplier`;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplier_invoice`
--

LOCK TABLES `supplier_invoice` WRITE;
/*!40000 ALTER TABLE `supplier_invoice` DISABLE KEYS */;
truncate table `supplier_invoice`;
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
truncate table `supplier_invoice_line`;
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
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tenant`
--

LOCK TABLES `tenant` WRITE;
/*!40000 ALTER TABLE `tenant` DISABLE KEYS */;
truncate table `tenant`;
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
truncate table `tenant_module`;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
truncate table `user_role`;
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

-- Dump completed on 2026-06-07 20:12:41
