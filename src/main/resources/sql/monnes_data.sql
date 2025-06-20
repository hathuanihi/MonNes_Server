-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: localhost    Database: quan_ly_so_tiet_kiem_db
-- ------------------------------------------------------
-- Server version	8.0.42

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
-- Table structure for table `dangnhap`
--

DROP TABLE IF EXISTS `dangnhap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dangnhap` (
  `madn` int NOT NULL AUTO_INCREMENT,
  `login_time` datetime(6) NOT NULL,
  `mand` int NOT NULL,
  PRIMARY KEY (`madn`),
  KEY `FKex5ljvbtxrul9m2j6x9ufha70` (`mand`),
  CONSTRAINT `FKex5ljvbtxrul9m2j6x9ufha70` FOREIGN KEY (`mand`) REFERENCES `nguoidung` (`mand`)
) ENGINE=InnoDB AUTO_INCREMENT=147 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dangnhap`
--

LOCK TABLES `dangnhap` WRITE;
/*!40000 ALTER TABLE `dangnhap` DISABLE KEYS */;
INSERT INTO `dangnhap` VALUES (1,'2025-06-02 14:11:11.719000',1),(2,'2025-06-02 14:21:21.217000',1),(3,'2025-06-02 14:35:20.536000',1),(4,'2025-06-02 14:46:13.511000',1),(5,'2025-06-02 15:39:13.031000',1),(6,'2025-06-02 15:56:39.368000',1),(7,'2025-06-02 15:57:10.762000',1),(8,'2025-06-02 16:12:23.628000',1),(9,'2025-06-02 23:25:53.246000',1),(10,'2025-06-03 00:38:31.880000',1),(11,'2025-06-03 03:13:43.821000',1),(12,'2025-06-03 03:47:57.325000',1),(18,'2025-06-03 22:17:33.468000',1),(21,'2025-06-04 12:17:57.474000',1),(22,'2025-06-04 13:19:47.934000',1),(24,'2025-06-04 14:44:56.777000',1),(25,'2025-06-04 22:02:37.219000',1),(26,'2025-06-04 22:16:37.297000',1),(27,'2025-06-04 22:21:32.450000',1),(29,'2025-06-05 00:23:37.468000',1),(30,'2025-06-05 14:21:22.740000',1),(53,'2025-06-06 20:46:08.307000',1),(55,'2025-06-06 21:26:47.458000',1),(57,'2025-06-06 22:26:54.558000',1),(61,'2025-06-08 15:43:34.997000',1),(63,'2025-06-09 16:06:17.151000',1),(64,'2025-06-09 16:40:10.189000',1),(65,'2025-06-09 16:56:08.555000',1),(66,'2025-06-09 20:44:42.200000',1),(68,'2025-06-09 20:54:57.380000',1),(75,'2025-06-09 21:27:47.145000',1),(83,'2025-06-13 14:14:07.070000',1),(84,'2025-06-13 14:56:44.178000',1),(86,'2025-06-13 14:59:51.637000',1),(87,'2025-06-13 15:14:12.437000',1),(88,'2025-06-13 15:14:58.171000',1),(97,'2025-06-13 18:07:13.909000',1),(98,'2025-06-14 10:44:05.543000',1),(99,'2025-06-14 10:50:19.707000',1),(100,'2025-06-14 10:52:10.482000',1),(101,'2025-06-14 10:54:23.502000',1),(102,'2025-06-14 10:56:05.860000',1),(103,'2025-06-14 10:56:15.921000',1),(104,'2025-06-14 11:00:53.247000',1),(105,'2025-06-14 11:07:34.438000',1),(106,'2025-06-14 11:15:35.668000',1),(108,'2025-06-14 17:08:00.319000',1),(109,'2025-06-14 17:23:45.343000',1),(110,'2025-06-16 11:52:52.125000',1),(111,'2025-06-16 14:31:14.871000',1),(112,'2025-06-16 14:38:04.733000',1),(113,'2025-06-16 14:41:29.381000',1),(114,'2025-06-16 14:43:00.157000',1),(116,'2025-06-17 11:52:52.059000',1),(117,'2025-06-18 11:58:06.943000',1),(119,'2025-06-18 12:34:13.596000',1),(123,'2025-06-18 23:11:57.937000',1),(125,'2025-06-20 09:27:48.209000',1),(127,'2025-06-20 10:26:15.147000',1),(128,'2025-06-20 10:49:55.611000',10),(129,'2025-06-20 10:50:03.536000',10),(130,'2025-06-20 10:50:16.707000',10),(131,'2025-06-20 10:51:29.087000',10),(132,'2025-06-20 10:53:22.917000',10),(133,'2025-06-20 10:53:38.016000',10),(134,'2025-06-20 11:00:16.828000',10),(135,'2025-06-20 11:26:36.978000',11),(136,'2025-06-20 11:59:13.027000',12),(137,'2025-06-20 12:28:13.015000',13),(138,'2025-06-20 14:01:05.763000',1),(139,'2025-06-20 14:46:49.148000',1),(140,'2025-06-20 15:21:39.747000',10),(141,'2025-06-20 15:24:46.407000',11),(142,'2025-06-20 15:27:53.200000',1),(143,'2025-06-20 15:29:14.040000',10),(144,'2025-06-20 15:31:57.653000',13),(145,'2025-06-20 15:38:18.085000',1),(146,'2025-06-20 15:45:25.271000',11);
/*!40000 ALTER TABLE `dangnhap` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `giao_dich`
--

DROP TABLE IF EXISTS `giao_dich`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `giao_dich` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `loai_giao_dich` enum('DEPOSIT','WITHDRAW','INTEREST','INTEREST_ACCRUAL','INTEREST_PAYMENT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `ngay_thuc_hien` date NOT NULL,
  `so_tien` decimal(19,4) NOT NULL,
  `mo_so_tiet_kiem_id` int NOT NULL,
  `san_pham_so_tiet_kiem_id` int NOT NULL COMMENT 'FK ??n SOTIETKIEM(mastk) t?i th?i ?i?m giao d?ch',
  PRIMARY KEY (`id`),
  KEY `FK373tyx3tffs0vjgejjnvy7rv` (`mo_so_tiet_kiem_id`),
  KEY `fk_giaodich_sanphamsotk_final` (`san_pham_so_tiet_kiem_id`),
  CONSTRAINT `FK373tyx3tffs0vjgejjnvy7rv` FOREIGN KEY (`mo_so_tiet_kiem_id`) REFERENCES `mosotietkiem` (`mamstk`),
  CONSTRAINT `FK3mu07uq289svv3yj5vjfbl1dg` FOREIGN KEY (`san_pham_so_tiet_kiem_id`) REFERENCES `sotietkiem` (`mastk`),
  CONSTRAINT `fk_giaodich_sanphamsotk_final` FOREIGN KEY (`san_pham_so_tiet_kiem_id`) REFERENCES `sotietkiem` (`mastk`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=235 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `giao_dich`
--

LOCK TABLES `giao_dich` WRITE;
/*!40000 ALTER TABLE `giao_dich` DISABLE KEYS */;
INSERT INTO `giao_dich` VALUES (118,'DEPOSIT','2025-01-02',5000000.0000,35,7),(119,'DEPOSIT','2025-01-15',10000000.0000,36,8),(120,'DEPOSIT','2025-01-15',3000000.0000,37,9),(121,'INTEREST','2025-02-15',3013.7000,35,7),(122,'INTEREST','2025-02-15',42465.7500,36,8),(123,'INTEREST','2025-02-15',14013.7000,37,9),(124,'DEPOSIT','2025-02-15',500000.0000,35,7),(125,'INTEREST','2025-03-15',2110.7400,35,7),(126,'INTEREST','2025-03-15',38519.0500,36,8),(127,'INTEREST','2025-03-15',12716.6600,37,9),(128,'WITHDRAW','2025-03-21',500000.0000,35,7),(129,'WITHDRAW','2025-04-15',10080984.8000,36,8),(130,'INTEREST','2025-05-15',3770.9800,35,7),(131,'INTEREST','2025-05-15',27821.0400,37,9),(132,'INTEREST','2025-06-15',2127.0700,35,7),(133,'INTEREST','2025-06-15',14268.5200,37,9),(134,'DEPOSIT','2025-01-02',7500000.0000,38,7),(135,'DEPOSIT','2025-01-11',8000000.0000,39,8),(136,'INTEREST','2025-02-02',3184.9300,38,7),(137,'INTEREST','2025-03-02',2877.9300,38,7),(138,'INTEREST','2025-03-02',54794.5200,39,8),(139,'INTEREST','2025-04-02',3187.5100,38,7),(140,'INTEREST','2025-04-02',34205.2900,39,8),(141,'INTEREST','2025-05-02',3085.9900,38,7),(142,'INTEREST','2025-06-02',3190.1700,38,7),(143,'INTEREST','2025-07-02',1166.9500,35,7),(144,'INTEREST','2025-07-02',3088.5700,38,7),(145,'INTEREST','2025-07-02',7861.2200,37,9),(146,'INTEREST','2025-04-11',99727.3900,39,8),(147,'INTEREST','2025-05-11',3365.2300,39,8),(148,'INTEREST','2025-06-11',3478.8300,39,8),(149,'DEPOSIT','2025-01-08',10000000.0000,40,8),(150,'INTEREST','2025-02-08',42465.7500,40,8),(151,'INTEREST','2025-03-08',38519.0500,40,8),(152,'INTEREST','2025-04-08',124286.1100,40,8),(153,'DEPOSIT','2025-06-08',10000000.0000,40,8),(154,'DEPOSIT','2025-02-11',50000000.0000,41,9),(155,'INTEREST','2025-03-11',210958.9000,41,9),(156,'INTEREST','2025-04-11',830.3500,40,8),(157,'INTEREST','2025-04-11',234547.0800,41,9),(158,'INTEREST','2025-05-11',8303.8800,40,8),(159,'INTEREST','2025-05-11',228041.3300,41,9),(160,'INTEREST','2025-06-11',8584.2000,40,8),(161,'INTEREST','2025-06-11',236707.9400,41,9),(162,'DEPOSIT','2025-06-11',40000000.0000,42,7),(163,'DEPOSIT','2025-01-04',20000000.0000,43,8),(164,'INTEREST','2025-02-04',84931.5100,43,8),(165,'INTEREST','2025-03-04',77038.0900,43,8),(166,'INTEREST','2025-04-04',248572.2300,43,8),(167,'INTEREST','2025-05-04',8387.8900,43,8),(168,'INTEREST','2025-06-04',8671.0500,43,8),(169,'INTEREST','2025-07-04',2582.1700,39,8),(170,'INTEREST','2025-07-04',6371.6300,40,8),(171,'INTEREST','2025-07-04',8394.9000,43,8),(172,'INTEREST','2025-07-04',137.3200,35,7),(173,'INTEREST','2025-07-04',205.9900,38,7),(174,'INTEREST','2025-07-04',12602.7400,42,7),(175,'INTEREST','2025-07-04',927.2200,37,9),(176,'INTEREST','2025-07-04',176442.3900,41,9),(177,'DEPOSIT','2025-07-05',50000000.0000,43,8),(178,'DEPOSIT','2024-06-02',100000000.0000,44,9),(179,'INTEREST','2024-07-02',452054.7900,44,9),(180,'INTEREST','2024-08-02',469234.9400,44,9),(181,'INTEREST','2024-09-02',471426.8500,44,9),(182,'INTEREST','2024-10-02',458350.6400,44,9),(183,'INTEREST','2024-11-02',475770.0500,44,9),(184,'INTEREST','2024-12-02',2821697.5800,44,9),(185,'DEPOSIT','2024-07-21',150000000.0000,45,9),(186,'INTEREST','2024-08-21',700684.9300,45,9),(187,'INTEREST','2024-09-21',703957.9900,45,9),(188,'INTEREST','2024-10-21',684431.9500,45,9),(189,'INTEREST','2024-11-21',710443.4900,45,9),(190,'INTEREST','2024-12-21',27367.4300,44,9),(191,'INTEREST','2024-12-21',690737.5500,45,9),(192,'INTEREST','2025-01-21',4255675.0400,45,9),(193,'INTEREST','2025-01-21',44663.7400,44,9),(194,'DEPOSIT','2025-01-21',40000000.0000,45,9),(195,'DEPOSIT','2025-01-21',300000000.0000,46,8),(196,'INTEREST','2025-02-21',44682.7100,44,9),(197,'INTEREST','2025-02-21',83974.3000,45,9),(198,'INTEREST','2025-02-21',1273972.6000,46,8),(199,'INTEREST','2025-03-21',40375.7100,44,9),(200,'INTEREST','2025-03-21',75879.9600,45,9),(201,'INTEREST','2025-03-21',1155571.4000,46,8),(202,'INTEREST','2025-04-21',3728583.4200,46,8),(203,'INTEREST','2025-04-21',44718.8300,44,9),(204,'INTEREST','2025-04-21',84042.1800,45,9),(205,'WITHDRAW','2025-04-21',306158127.4200,46,8),(206,'DEPOSIT','2024-07-16',10000000.0000,47,7),(207,'INTEREST','2024-08-16',4246.5800,47,7),(208,'DEPOSIT','2024-08-16',7000000.0000,47,7),(209,'WITHDRAW','2024-09-16',3000000.0000,47,7),(210,'INTEREST','2024-10-16',5755.1700,47,7),(211,'DEPOSIT','2024-10-16',100000000.0000,47,7),(212,'INTEREST','2024-11-16',48415.2100,47,7),(213,'DEPOSIT','2024-12-02',20000000.0000,47,7),(214,'INTEREST','2024-12-16',55092.5000,47,7),(215,'INTEREST','2025-01-16',56952.3100,47,7),(216,'INTEREST','2025-02-16',56976.5000,47,7),(217,'INTEREST','2025-03-16',51484.5000,47,7),(218,'INTEREST','2025-04-16',57022.5600,47,7),(219,'INTEREST','2025-05-16',36078.8800,44,9),(220,'INTEREST','2025-05-16',67804.7400,45,9),(221,'INTEREST','2025-05-16',55206.5500,47,7),(222,'INTEREST','2025-06-16',44753.1400,44,9),(223,'INTEREST','2025-06-16',84106.6700,45,9),(224,'INTEREST','2025-06-16',57070.2200,47,7),(225,'INTEREST','2025-07-16',1347.6400,39,8),(226,'INTEREST','2025-07-16',3325.3700,40,8),(227,'INTEREST','2025-07-16',11578.5200,43,8),(228,'INTEREST','2025-07-16',43327.8800,44,9),(229,'INTEREST','2025-07-16',81428.1100,45,9),(230,'INTEREST','2025-07-16',823.9400,35,7),(231,'INTEREST','2025-07-16',1235.9700,38,7),(232,'INTEREST','2025-07-16',6577.4100,42,7),(233,'INTEREST','2025-07-16',55252.6900,47,7),(234,'INTEREST','2025-07-16',92375.9500,41,9);
/*!40000 ALTER TABLE `giao_dich` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loaisotietkiem`
--

DROP TABLE IF EXISTS `loaisotietkiem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loaisotietkiem` (
  `ma_loai` int NOT NULL AUTO_INCREMENT,
  `ten_loai` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ky_han` int NOT NULL,
  `lai_suat` decimal(5,2) NOT NULL,
  `so_ngay_gui_toi_thieu` int NOT NULL,
  `tien_gui_toi_thieu` bigint NOT NULL,
  `tien_gui_them_toi_thieu` bigint NOT NULL,
  PRIMARY KEY (`ma_loai`),
  UNIQUE KEY `UK_ten_loai` (`ten_loai`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loaisotietkiem`
--

LOCK TABLES `loaisotietkiem` WRITE;
/*!40000 ALTER TABLE `loaisotietkiem` DISABLE KEYS */;
INSERT INTO `loaisotietkiem` VALUES (1,'Không kỳ hạn',0,0.20,15,100000,100000),(2,'Tiết kiệm 3 tháng',3,3.50,90,1000000,100000),(3,'Tiết kiệm 6 tháng',6,5.50,180,1000000,100000);
/*!40000 ALTER TABLE `loaisotietkiem` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loaisotietkiem_danhmuc`
--

DROP TABLE IF EXISTS `loaisotietkiem_danhmuc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loaisotietkiem_danhmuc` (
  `ma_loai_danh_muc` int NOT NULL AUTO_INCREMENT,
  `ten_loai_danh_muc` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`ma_loai_danh_muc`),
  UNIQUE KEY `UKdiji8cxjqbc370oe9eyp4y7ui` (`ten_loai_danh_muc`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loaisotietkiem_danhmuc`
--

LOCK TABLES `loaisotietkiem_danhmuc` WRITE;
/*!40000 ALTER TABLE `loaisotietkiem_danhmuc` DISABLE KEYS */;
INSERT INTO `loaisotietkiem_danhmuc` VALUES (2,'Có Kỳ Hạn'),(1,'Không Kỳ Hạn');
/*!40000 ALTER TABLE `loaisotietkiem_danhmuc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mosotietkiem`
--

DROP TABLE IF EXISTS `mosotietkiem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mosotietkiem` (
  `mamstk` int NOT NULL AUTO_INCREMENT,
  `lai_suat_ap_dung` decimal(5,2) NOT NULL,
  `ngay_dao_han` date DEFAULT NULL,
  `ngay_mo` date NOT NULL,
  `so_du` decimal(19,4) NOT NULL,
  `ten_so_mo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `trang_thai` enum('DANG_HOAT_DONG','DA_DONG','DA_DAO_HAN') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ma_nd` int NOT NULL COMMENT 'FK ??n NGUOIDUNG(mand)',
  `ma_stk_san_pham` int NOT NULL,
  `ngay_tra_lai_cuoi_cung` date DEFAULT NULL,
  `ngay_tra_lai_ke_tiep` date DEFAULT NULL,
  PRIMARY KEY (`mamstk`),
  KEY `FK6fu5u61qlk1j6h7wobel6ybm6` (`ma_nd`),
  KEY `FK255nlyx9kmsx7r328knsh3xpb` (`ma_stk_san_pham`),
  CONSTRAINT `FK255nlyx9kmsx7r328knsh3xpb` FOREIGN KEY (`ma_stk_san_pham`) REFERENCES `sotietkiem` (`mastk`),
  CONSTRAINT `FK6fu5u61qlk1j6h7wobel6ybm6` FOREIGN KEY (`ma_nd`) REFERENCES `nguoidung` (`mand`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mosotietkiem`
--

LOCK TABLES `mosotietkiem` WRITE;
/*!40000 ALTER TABLE `mosotietkiem` DISABLE KEYS */;
INSERT INTO `mosotietkiem` VALUES (35,0.50,NULL,'2025-01-02',5013150.7000,'Không Kỳ Hạn','DANG_HOAT_DONG',10,7,'2025-07-16','2025-01-18'),(36,5.00,'2025-04-15','2025-01-15',0.0000,'Tiết Kiệm 3 Tháng ','DA_DONG',10,8,'2025-04-15','2025-02-15'),(37,5.50,'2025-07-15','2025-01-15',3077608.3600,'Tiết Kiệm 6 Tháng','DANG_HOAT_DONG',10,9,'2025-07-04','2025-02-15'),(38,0.50,NULL,'2025-01-02',7520057.0600,'Không Kỳ Hạn','DANG_HOAT_DONG',11,7,'2025-07-16','2025-01-18'),(39,5.00,'2025-04-11','2025-01-11',8199501.0700,'Tiết Kiệm 3 Tháng ','DA_DAO_HAN',11,8,'2025-07-16','2025-02-11'),(40,5.00,'2025-04-08','2025-01-08',20232686.3400,'Tiết Kiệm 3 Tháng Mua Điện Thoại','DA_DAO_HAN',12,8,'2025-07-16','2025-02-08'),(41,5.50,'2025-08-11','2025-02-11',51179073.5900,'Tiết Kiệm Mua Ô Tô','DANG_HOAT_DONG',12,9,'2025-07-16','2025-03-11'),(42,0.50,NULL,'2025-06-11',40019180.1500,'Tiền Cưới Vợ','DANG_HOAT_DONG',13,7,'2025-07-16','2025-06-27'),(43,5.00,'2025-04-04','2025-01-04',70447574.1900,'Đi Du Lịch','DA_DAO_HAN',13,8,'2025-07-16','2025-02-04'),(44,5.50,'2024-12-02','2024-06-02',105474503.1700,'Tiết Kiệm 6 Tháng','DA_DAO_HAN',10,9,'2025-07-16','2024-07-02'),(45,5.50,'2025-01-21','2024-07-21',198223166.9100,'Mua Vàng','DA_DAO_HAN',11,9,'2025-07-16','2024-08-21'),(46,5.00,'2025-04-21','2025-01-21',0.0000,'Quà Cho Ba Mẹ','DA_DONG',10,8,'2025-04-21','2025-02-21'),(47,0.50,NULL,'2024-07-16',134503474.7900,'Nạp Game','DANG_HOAT_DONG',13,7,'2025-07-16','2024-08-01');
/*!40000 ALTER TABLE `mosotietkiem` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nguoidung`
--

DROP TABLE IF EXISTS `nguoidung`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nguoidung` (
  `mand` int NOT NULL AUTO_INCREMENT,
  `cccd` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dia_chi` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `mat_khau` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ngay_sinh` date DEFAULT NULL,
  `sdt` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tennd` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `vai_tro` int NOT NULL,
  PRIMARY KEY (`mand`),
  UNIQUE KEY `UKfypgm4q7hwro4gipdmwew5f33` (`email`),
  UNIQUE KEY `UKrgns1h6wkx003pk178662ppe6` (`cccd`),
  UNIQUE KEY `UK4l8n9aabwqxy7h5jlege70qgi` (`sdt`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nguoidung`
--

LOCK TABLES `nguoidung` WRITE;
/*!40000 ALTER TABLE `nguoidung` DISABLE KEYS */;
INSERT INTO `nguoidung` VALUES (1,'056254482653','HCM','admin@gmail.com','$2a$10$iM4h2aMcizkcdEBpoNKaj.eRph8//gfbHtine3nSDJ5TiuF6YzONq','2005-09-20','0957354658','admin',0),(10,'051305000889','Tp. Hồ Chí Minh','hathu20905@gmail.com','$2a$10$3YxbragnhgeDRuj1ug0SUOSH0m.p.KR.UK5GoaLXEkUIHcD3HxnVq','2005-09-20','0945771705','Phạm Hà Anh Thư',1),(11,'051305000564','An Giang','ai.bic.trui@gmail.com','$2a$10$gH7XYu66JZQXFCAAL1D1kui9Y5Op8y/heiA9TLFB0PexdMqXgcgW.','2005-07-15','0905706806','Nguyễn Thị Thanh Phương',1),(12,'051305000111','Tp. Hồ Chí Minh','nhoawmnhoawm@gmail.com','$2a$10$NHX1cO3DeZRLbLUx3ceX4u/cKxrEmdrhupZ6VDmdN6H1KOQWgRILq','2005-04-07','0935572657','Nguyễn Hoàng Minh',1),(13,'051305000765','Tp. Đà Nẵng','dangvanvy112@gmail.com','$2a$10$le3LTrCifvz.zRPc12H8pee7SOhw8v.w5ODLL4KIzECC8ncalAiGi','2005-07-15','0954376896','Đặng Văn Vỹ',1);
/*!40000 ALTER TABLE `nguoidung` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phieuguitien`
--

DROP TABLE IF EXISTS `phieuguitien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phieuguitien` (
  `ma_phieu` int NOT NULL AUTO_INCREMENT,
  `ma_mostk` int NOT NULL,
  `ngay_gui` date NOT NULL,
  `so_tien_gui` decimal(15,2) NOT NULL,
  PRIMARY KEY (`ma_phieu`),
  KEY `ma_mostk` (`ma_mostk`),
  CONSTRAINT `phieuguitien_ibfk_1` FOREIGN KEY (`ma_mostk`) REFERENCES `mosotietkiem` (`mamstk`)
) ENGINE=InnoDB AUTO_INCREMENT=58 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieuguitien`
--

LOCK TABLES `phieuguitien` WRITE;
/*!40000 ALTER TABLE `phieuguitien` DISABLE KEYS */;
INSERT INTO `phieuguitien` VALUES (38,35,'2025-01-02',5000000.00),(39,36,'2025-01-15',10000000.00),(40,37,'2025-01-15',3000000.00),(41,35,'2025-02-15',500000.00),(42,38,'2025-01-02',7500000.00),(43,39,'2025-01-11',8000000.00),(44,40,'2025-01-08',10000000.00),(45,40,'2025-06-08',10000000.00),(46,41,'2025-02-11',50000000.00),(47,42,'2025-06-11',40000000.00),(48,43,'2025-01-04',20000000.00),(49,43,'2025-07-05',50000000.00),(50,44,'2024-06-02',100000000.00),(51,45,'2024-07-21',150000000.00),(52,45,'2025-01-21',40000000.00),(53,46,'2025-01-21',300000000.00),(54,47,'2024-07-16',10000000.00),(55,47,'2024-08-16',7000000.00),(56,47,'2024-10-16',100000000.00),(57,47,'2024-12-02',20000000.00);
/*!40000 ALTER TABLE `phieuguitien` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phieurutien`
--

DROP TABLE IF EXISTS `phieurutien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phieurutien` (
  `ma_phieu` int NOT NULL AUTO_INCREMENT,
  `ma_mostk` int NOT NULL,
  `ngay_rut` date NOT NULL,
  `so_tien_rut` decimal(15,2) NOT NULL,
  PRIMARY KEY (`ma_phieu`),
  KEY `ma_mostk` (`ma_mostk`),
  CONSTRAINT `phieurutien_ibfk_1` FOREIGN KEY (`ma_mostk`) REFERENCES `mosotietkiem` (`mamstk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieurutien`
--

LOCK TABLES `phieurutien` WRITE;
/*!40000 ALTER TABLE `phieurutien` DISABLE KEYS */;
/*!40000 ALTER TABLE `phieurutien` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phieuruttien`
--

DROP TABLE IF EXISTS `phieuruttien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phieuruttien` (
  `maprt` int NOT NULL AUTO_INCREMENT,
  `lai_suat_khi_rut` decimal(5,2) DEFAULT NULL,
  `ngay_rut` datetime(6) NOT NULL,
  `so_tien_rut` decimal(19,4) NOT NULL,
  `tien_lai_thuc_nhan` decimal(19,4) DEFAULT NULL,
  `mamstk` int NOT NULL,
  PRIMARY KEY (`maprt`),
  KEY `FKatxl2qlrmq33ljbqbdpqa0ye1` (`mamstk`),
  CONSTRAINT `FKatxl2qlrmq33ljbqbdpqa0ye1` FOREIGN KEY (`mamstk`) REFERENCES `mosotietkiem` (`mamstk`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieuruttien`
--

LOCK TABLES `phieuruttien` WRITE;
/*!40000 ALTER TABLE `phieuruttien` DISABLE KEYS */;
INSERT INTO `phieuruttien` VALUES (21,0.50,'2025-03-21 00:00:00.000000',500000.0000,452.4800,35),(22,5.00,'2025-04-15 00:00:00.000000',10080984.8000,42809.6600,36),(23,5.00,'2025-04-21 00:00:00.000000',306158127.4200,0.0000,46),(24,0.50,'2024-09-16 00:00:00.000000',3000000.0000,7220.9800,47);
/*!40000 ALTER TABLE `phieuruttien` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sotietkiem`
--

DROP TABLE IF EXISTS `sotietkiem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sotietkiem` (
  `mastk` int NOT NULL AUTO_INCREMENT,
  `ky_han` int NOT NULL,
  `lai_suat` decimal(5,2) NOT NULL,
  `so_ngay_gui_toi_thieu_de_rut` int DEFAULT NULL,
  `ten_so` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tien_gui_ban_dau_toi_thieu` bigint NOT NULL,
  `tien_gui_them_toi_thieu` bigint NOT NULL,
  `ma_loai_danh_muc` int DEFAULT NULL,
  PRIMARY KEY (`mastk`),
  UNIQUE KEY `UKpd4sl1sxiiucbfqv09q023py1` (`ten_so`),
  KEY `FK52eh8ysitp66q6u0giwyx4f62` (`ma_loai_danh_muc`),
  CONSTRAINT `FK52eh8ysitp66q6u0giwyx4f62` FOREIGN KEY (`ma_loai_danh_muc`) REFERENCES `loaisotietkiem_danhmuc` (`ma_loai_danh_muc`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sotietkiem`
--

LOCK TABLES `sotietkiem` WRITE;
/*!40000 ALTER TABLE `sotietkiem` DISABLE KEYS */;
INSERT INTO `sotietkiem` VALUES (7,0,0.50,15,'Tiết Kiệm Không Kỳ Hạn',1000000,100000,1),(8,3,5.00,90,'Tiết Kiệm 3 Tháng ',1000000,100000,2),(9,6,5.50,180,'Tiết Kiệm 6 Tháng ',1000000,100000,2);
/*!40000 ALTER TABLE `sotietkiem` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thaydoi_quydinh_sotietkiem`
--

DROP TABLE IF EXISTS `thaydoi_quydinh_sotietkiem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `thaydoi_quydinh_sotietkiem` (
  `matd` int NOT NULL AUTO_INCREMENT,
  `ghi_chu` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ky_han_cu` int DEFAULT NULL,
  `ky_han_moi` int DEFAULT NULL,
  `lai_suat_cu` decimal(5,2) DEFAULT NULL,
  `lai_suat_moi` decimal(5,2) DEFAULT NULL,
  `ngay_thay_doi` date NOT NULL,
  `so_ngay_gui_toi_thieu_de_rut_cu` int DEFAULT NULL,
  `so_ngay_gui_toi_thieu_de_rut_moi` int DEFAULT NULL,
  `ten_so_cu` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ten_so_moi` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tien_gui_ban_dau_toi_thieu_cu` bigint DEFAULT NULL,
  `tien_gui_ban_dau_toi_thieu_moi` bigint DEFAULT NULL,
  `tien_gui_them_toi_thieu_cu` bigint DEFAULT NULL,
  `tien_gui_them_toi_thieu_moi` bigint DEFAULT NULL,
  `ma_nd_admin` int NOT NULL,
  `ma_stk_san_pham` int NOT NULL,
  PRIMARY KEY (`matd`),
  KEY `FK3bdxh29dck5fun17pp4nu229e` (`ma_nd_admin`),
  KEY `FK3ee6bgfpor2yjc7d59ohl193e` (`ma_stk_san_pham`),
  CONSTRAINT `FK3bdxh29dck5fun17pp4nu229e` FOREIGN KEY (`ma_nd_admin`) REFERENCES `nguoidung` (`mand`),
  CONSTRAINT `FK3ee6bgfpor2yjc7d59ohl193e` FOREIGN KEY (`ma_stk_san_pham`) REFERENCES `sotietkiem` (`mastk`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thaydoi_quydinh_sotietkiem`
--

LOCK TABLES `thaydoi_quydinh_sotietkiem` WRITE;
/*!40000 ALTER TABLE `thaydoi_quydinh_sotietkiem` DISABLE KEYS */;
INSERT INTO `thaydoi_quydinh_sotietkiem` VALUES (19,'Tạo mới sản phẩm sổ tiết kiệm: Tiết Kiệm Không Kỳ Hạn',NULL,0,NULL,0.50,'2025-06-18',NULL,16,NULL,'Tiết Kiệm Không Kỳ Hạn',NULL,999999,NULL,100000,1,7),(20,'Cập nhật sản phẩm sổ tiết kiệm ID: 7',0,0,0.50,0.50,'2025-06-18',16,15,'Tiết Kiệm Không Kỳ Hạn','Tiết Kiệm Không Kỳ Hạn',999999,1000000,100000,100000,1,7),(21,'Tạo mới sản phẩm sổ tiết kiệm: Tiết Kiệm 3 Tháng ',NULL,3,NULL,5.00,'2025-06-18',NULL,90,NULL,'Tiết Kiệm 3 Tháng ',NULL,1000000,NULL,100000,1,8),(22,'Tạo mới sản phẩm sổ tiết kiệm: Tiết Kiệm 6 Tháng ',NULL,6,NULL,5.50,'2025-06-18',NULL,180,NULL,'Tiết Kiệm 6 Tháng ',NULL,1000000,NULL,100000,1,9),(24,'Cập nhật sản phẩm sổ tiết kiệm ID: 7',0,0,0.50,0.50,'2025-06-18',15,15,'Tiết Kiệm Không Kỳ Hạn','Tiết Kiệm Không Kỳ Hạn',1000000,1000000,100000,100000,1,7),(25,'Cập nhật sản phẩm sổ tiết kiệm ID: 8',3,3,5.00,5.00,'2025-06-18',90,90,'Tiết Kiệm 3 Tháng ','Tiết Kiệm 3 Tháng ',1000000,1000000,100000,100000,1,8),(26,'Cập nhật sản phẩm sổ tiết kiệm ID: 9',6,6,5.50,5.50,'2025-06-18',180,180,'Tiết Kiệm 6 Tháng ','Tiết Kiệm 6 Tháng ',1000000,1000000,100000,100000,1,9);
/*!40000 ALTER TABLE `thaydoi_quydinh_sotietkiem` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-06-20 16:07:39
