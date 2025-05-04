CREATE DATABASE  IF NOT EXISTS `milan_store` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `milan_store`;
-- MySQL dump 10.13  Distrib 8.0.29, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: milan_store
-- ------------------------------------------------------
-- Server version	8.0.29

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account_status`
--

DROP TABLE IF EXISTS `account_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account_status` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_account_active` bit(1) DEFAULT NULL,
  `password_reset_token` varchar(255) DEFAULT NULL,
  `password_reset_token_expiry` datetime(6) DEFAULT NULL,
  `verification_token` varchar(255) DEFAULT NULL,
  `verification_token_expiry` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_status`
--

LOCK TABLES `account_status` WRITE;
/*!40000 ALTER TABLE `account_status` DISABLE KEYS */;
INSERT INTO `account_status` VALUES (3,_binary '',NULL,NULL,NULL,NULL),(4,_binary '',NULL,NULL,NULL,NULL),(6,_binary '',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `account_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart`
--

DROP TABLE IF EXISTS `cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart` (
  `cart_id` int NOT NULL AUTO_INCREMENT,
  `created_on` datetime(6) DEFAULT NULL,
  `updated_on` datetime(6) DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  `total_cart_price` double DEFAULT NULL,
  PRIMARY KEY (`cart_id`),
  UNIQUE KEY `UK9emlp6m95v5er2bcqkjsw48he` (`user_id`),
  CONSTRAINT `FKmoevlua5kup94qth8tsmpcqc2` FOREIGN KEY (`user_id`) REFERENCES `site_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart`
--

LOCK TABLES `cart` WRITE;
/*!40000 ALTER TABLE `cart` DISABLE KEYS */;
INSERT INTO `cart` VALUES (4,'2025-04-23 18:36:41.419387','2025-04-24 22:48:51.822440',2,0);
/*!40000 ALTER TABLE `cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_items`
--

DROP TABLE IF EXISTS `cart_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_items` (
  `cart_item_id` int NOT NULL AUTO_INCREMENT,
  `created_on` datetime(6) DEFAULT NULL,
  `updated_on` datetime(6) DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `sub_total_price` double DEFAULT NULL,
  `cart_id` int DEFAULT NULL,
  `product_id` int DEFAULT NULL,
  PRIMARY KEY (`cart_item_id`),
  KEY `FK99e0am9jpriwxcm6is7xfedy3` (`cart_id`),
  KEY `FK1re40cjegsfvw58xrkdp6bac6` (`product_id`),
  CONSTRAINT `FK1re40cjegsfvw58xrkdp6bac6` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FK99e0am9jpriwxcm6is7xfedy3` FOREIGN KEY (`cart_id`) REFERENCES `cart` (`cart_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_items`
--

LOCK TABLES `cart_items` WRITE;
/*!40000 ALTER TABLE `cart_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `cart_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` int NOT NULL AUTO_INCREMENT,
  `category_name` varchar(60) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `category_image` varchar(255) DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `created_on` datetime(6) DEFAULT NULL,
  `updated_on` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'Electronics updated','A wide range of electronic products including mobile phones, laptops, and accessories.','ced7388a-7280-4f9f-ab53-b4a16ed1fab1.jpg',_binary '','2025-04-20 23:11:52.823270','2025-04-21 22:43:01.005674'),(2,'Home Appliances','Essential appliances for home including refrigerators, washing machines, and microwaves.','50e0baf2-4902-4f51-ae65-285eaee5ecca.jpg',_binary '','2025-04-21 22:26:30.318195','2025-04-21 22:45:23.409523'),(3,'Fashion','Clothing, shoes, and accessories for men, women, and children.',NULL,_binary '','2025-04-21 22:26:37.428312',NULL),(4,'Books','Books across genres including fiction, non-fiction, education, and self-help.',NULL,_binary '','2025-04-21 22:26:48.473875',NULL),(5,'Health & Beauty','Personal care products, skincare, wellness, and beauty tools.',NULL,_binary '','2025-04-21 22:26:53.679949',NULL),(6,'Toys & Games','Toys, board games, and learning kits for kids of all ages.',NULL,_binary '','2025-04-21 22:27:00.195649',NULL),(7,'Sports & Outdoors','Equipment and accessories for sports, fitness, and outdoor adventures.',NULL,_binary '','2025-04-21 22:27:05.883262',NULL),(8,'Groceries','Everyday grocery items including food, beverages, and household essentials.',NULL,_binary '','2025-04-21 22:27:10.366160',NULL),(9,'Automotive','Car accessories, tools, and parts for maintenance and performance.',NULL,_binary '','2025-04-21 22:27:14.884663',NULL),(10,'Furniture','Home and office furniture including chairs, tables, and storage solutions.',NULL,_binary '','2025-04-21 22:27:20.901085',NULL),(11,'Test Category - To Be Deleted','This category is created for testing the soft delete functionality using isActive = false.',NULL,_binary '\0','2025-04-21 22:27:46.388465','2025-04-21 22:42:12.912470'),(12,'Computers','Desktops, laptops, accessories, and peripherals for all your computing needs.',NULL,_binary '','2025-04-21 22:44:31.263493',NULL);
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `price_at_purchase` double NOT NULL,
  `quantity` int NOT NULL,
  `order_id` int NOT NULL,
  `product_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbioxgbv59vetrxe0ejfubep1w` (`order_id`),
  KEY `FKocimc7dtr037rh4ls4l95nlfi` (`product_id`),
  CONSTRAINT `FKbioxgbv59vetrxe0ejfubep1w` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `FKocimc7dtr037rh4ls4l95nlfi` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (1,47.490500000000004,1,1,2),(2,53.982,2,1,1),(3,220.983,2,2,3),(4,220.983,2,3,3),(5,53.982,2,3,1);
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_on` datetime(6) DEFAULT NULL,
  `updated_on` datetime(6) DEFAULT NULL,
  `estimated_delivery_date` datetime(6) DEFAULT NULL,
  `order_date` datetime(6) DEFAULT NULL,
  `order_identifier` varchar(255) DEFAULT NULL,
  `payment_method` varchar(255) DEFAULT NULL,
  `shipping_address` varchar(255) NOT NULL,
  `shipping_phone_number` varchar(255) NOT NULL,
  `shipping_province` varchar(255) NOT NULL,
  `shipping_zip_code` varchar(6) NOT NULL,
  `status` enum('CANCELLED','DELIVERED','IN_PROGRESS','RECEIVED','SHIPPED','OUT_FOR_DELIVERY','REFUNDED') DEFAULT NULL,
  `total_order_amount` double DEFAULT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgc0ejftqimbjw2w7mu9ur9msj` (`user_id`),
  CONSTRAINT `FKgc0ejftqimbjw2w7mu9ur9msj` FOREIGN KEY (`user_id`) REFERENCES `site_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,'2025-04-23 23:11:15.490346','2025-04-25 20:35:56.853751','2025-04-26 23:11:15.337370','2025-04-23 23:11:15.337370','1c2052b0-557e-45b5-979b-090a8123ba7a','Cash On Delivery','Balkhu, Kathmandu','9841234567','Bagmati','44600','REFUNDED',101.4725,2),(2,'2025-04-24 14:17:07.454365','2025-04-25 17:32:40.041234','2025-04-27 14:17:07.418813','2025-04-24 14:17:07.418813','de7e7bc8-d39f-4c7e-b7e3-aa2e20bf900c','Cash On Delivery','Balkhu, Kathmandu','9841234567','Bagmati','44600','CANCELLED',220.983,2),(3,'2025-04-24 15:12:54.011394','2025-04-25 22:20:07.773382','2025-04-27 15:12:53.958188','2025-04-24 15:12:53.958188','948105d5-d2d3-4e5d-b9fe-d591a5fc932e','Cash On Delivery','Balkhu, Kathmandu','9841234567','Bagmati','44600','DELIVERED',274.96500000000003,2);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_name` varchar(250) NOT NULL,
  `short_description` varchar(160) NOT NULL,
  `description` varchar(8000) NOT NULL,
  `unit_price` double NOT NULL,
  `discount_percent` int NOT NULL,
  `discounted_price` double DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `product_image` varchar(255) DEFAULT NULL,
  `stock` int NOT NULL,
  `category_id` int DEFAULT NULL,
  `created_on` datetime(6) DEFAULT NULL,
  `updated_on` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  `deleted_on` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1cf90etcu98x1e6n9aks3tel3` (`category_id`),
  CONSTRAINT `FK1cf90etcu98x1e6n9aks3tel3` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'Logitech GPROX SuperLight 2','Best esports wireless mouse','A high-precision ergonomic wireless mouse with adjustable DPI and long battery life.',29.99,10,26.991,_binary '','b70de8eb-5c37-47d8-bcc2-a750d152b04a.jpg',6,1,'2025-04-22 16:42:41.046063','2025-04-24 15:12:53.978390',NULL,NULL,NULL),(2,'Wireless Keyboard','Full-size wireless keyboard','Comfortable full-size wireless keyboard with multimedia keys and quiet typing experience.',49.99,5,47.490500000000004,_binary '',NULL,14,12,'2025-04-22 19:38:43.431808','2025-04-23 23:11:15.393322',NULL,NULL,NULL),(3,'Bluetooth Headphones','Over-ear noise cancelling headphones','Premium over-ear headphones with active noise cancellation and 30-hour battery life.',129.99,15,110.4915,_binary '',NULL,4,12,'2025-04-22 19:38:54.286045','2025-04-24 15:12:53.964390',NULL,NULL,NULL),(4,'USB-C Hub','7-in-1 USB-C adapter','Compact 7-in-1 USB-C hub with HDMI, USB 3.0, SD card reader, and power delivery.',39.99,0,39.99,_binary '',NULL,20,12,'2025-04-22 19:39:04.007069',NULL,NULL,NULL,NULL),(5,'Mechanical Keyboard','RGB mechanical gaming keyboard','Tactile mechanical gaming keyboard with customizable RGB lighting and programmable macros.',89.99,10,80.991,_binary '',NULL,12,12,'2025-04-22 19:39:10.930896',NULL,NULL,NULL,NULL),(6,'External SSD','500GB portable SSD','Ultra-fast 500GB external SSD with USB 3.2 interface and shock-resistant design.',79.99,5,75.9905,_binary '',NULL,7,12,'2025-04-22 19:39:18.239743',NULL,NULL,NULL,NULL),(7,'Monitor','27-inch 4K monitor','Professional 27-inch 4K monitor with IPS panel, 99% sRGB color accuracy, and adjustable stand.',349.99,15,297.4915,_binary '',NULL,5,12,'2025-04-22 19:39:27.469438',NULL,NULL,NULL,NULL),(8,'Espresso Machine','Semi-automatic home espresso maker','15-bar semi-automatic espresso machine with milk frother and programmable temperature control.',249.99,10,224.991,_binary '',NULL,6,2,'2025-04-22 19:39:56.949560',NULL,NULL,NULL,NULL),(9,'Smart Watch','Fitness tracking smart watch','Water-resistant smart watch with heart rate monitor, sleep tracking, and 7-day battery life.',149.99,15,127.4915,_binary '',NULL,12,1,'2025-04-22 19:40:17.651164',NULL,NULL,NULL,NULL),(10,'Air Purifier','HEPA air purifier for medium rooms','True HEPA air purifier that removes 99.97% of allergens, dust, and pollutants in rooms up to 300 sq ft.',119.99,5,113.9905,_binary '',NULL,8,1,'2025-04-22 19:40:27.680857',NULL,NULL,NULL,NULL),(11,'Yoga Mat','Non-slip exercise yoga mat','Eco-friendly TPE yoga mat with non-slip texture, extra thickness for joint protection, and carrying strap.',32.99,0,32.99,_binary '',NULL,25,5,'2025-04-22 19:40:39.822467',NULL,NULL,NULL,NULL),(12,'Dutch Oven','5.5qt enameled cast iron dutch oven','Versatile enameled cast iron dutch oven perfect for slow cooking, braising, baking, and roasting.',89.99,20,71.99199999999999,_binary '',NULL,10,2,'2025-04-22 19:40:51.261067',NULL,NULL,NULL,NULL),(13,'Resistance Bands Set','5-piece exercise resistance bands','Set of 5 resistance bands of varying strengths with handles, ankle straps, and door anchor for full-body workouts.',28.99,5,27.540499999999998,_binary '\0',NULL,18,5,'2025-04-22 19:41:07.874856','2025-04-28 23:44:36.981929',NULL,_binary '','2025-04-28 23:44:36.948934'),(14,'Adjustable Dumbbell Set','Versatile strength training','Adjustable dumbbell set ranging from 5 to 52.5 pounds, ideal for a variety of strength training exercises at home or gym.',149.99,10,134.991,_binary '',NULL,12,7,'2025-04-28 22:48:18.632305',NULL,NULL,NULL,NULL),(15,'UltraBook Pro 15','Lightweight and powerful','Sleek and lightweight laptop featuring a 15.6-inch Full HD display, Intel i7 processor, 16GB RAM, 512GB SSD, and long battery life for professionals and students.',1099.99,7,1022.9907000000001,_binary '',NULL,25,7,'2025-04-28 23:37:43.871385',NULL,NULL,_binary '\0',NULL);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `refunds`
--

DROP TABLE IF EXISTS `refunds`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refunds` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_on` datetime(6) DEFAULT NULL,
  `updated_on` datetime(6) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `resolved_date` datetime(6) DEFAULT NULL,
  `seller_comment` varchar(255) DEFAULT NULL,
  `status` enum('APPROVED','PENDING','REJECTED') DEFAULT NULL,
  `order_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKgotjk25w6sr9rf3ikc0mrove5` (`order_id`),
  CONSTRAINT `FKsk9rqm7f6y8b1g0qob018hdm7` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `refunds`
--

LOCK TABLES `refunds` WRITE;
/*!40000 ALTER TABLE `refunds` DISABLE KEYS */;
INSERT INTO `refunds` VALUES (1,'2025-04-25 20:20:35.392516','2025-04-25 20:35:56.871752','Damaged','2025-04-25 20:35:56.810751','Ok, Will contact you and refund or exchange item','APPROVED',1),(2,'2025-04-25 22:20:27.035476','2025-04-25 22:21:23.915007','Product is already opened.','2025-04-25 22:21:23.913005','Sorry, cant refund','REJECTED',3);
/*!40000 ALTER TABLE `refunds` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reviews`
--

DROP TABLE IF EXISTS `reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_on` datetime(6) DEFAULT NULL,
  `updated_on` datetime(6) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `rating` int NOT NULL,
  `product_id` int NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpl51cejpw4gy5swfar8br9ngi` (`product_id`),
  KEY `FKtfcvgdgygpq2wrv83p437kfo5` (`user_id`),
  CONSTRAINT `FKpl51cejpw4gy5swfar8br9ngi` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKtfcvgdgygpq2wrv83p437kfo5` FOREIGN KEY (`user_id`) REFERENCES `site_user` (`id`),
  CONSTRAINT `reviews_chk_1` CHECK (((`rating` <= 5) and (`rating` >= 1)))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reviews`
--

LOCK TABLES `reviews` WRITE;
/*!40000 ALTER TABLE `reviews` DISABLE KEYS */;
INSERT INTO `reviews` VALUES (1,'2025-04-26 03:29:52.939033','2025-04-26 12:38:18.993724','Best product',5,1,2);
/*!40000 ALTER TABLE `reviews` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (1,'ADMIN'),(2,'USER');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `site_user`
--

DROP TABLE IF EXISTS `site_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `site_user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `mobile_number` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `profile_image` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `zip_code` varchar(255) DEFAULT NULL,
  `account_status_id` int DEFAULT NULL,
  `created_on` datetime(6) DEFAULT NULL,
  `updated_on` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK1sj7pf2cwh7sf37qmwv2q1tq3` (`account_status_id`),
  CONSTRAINT `FKisikkiak9gakttuc9a2kllor6` FOREIGN KEY (`account_status_id`) REFERENCES `account_status` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `site_user`
--

LOCK TABLES `site_user` WRITE;
/*!40000 ALTER TABLE `site_user` DISABLE KEYS */;
INSERT INTO `site_user` VALUES (2,'Kathmandu','Gokarneshowr-2','lamameelan32@gmail.com','Meelan','Tamang','9863837952','$2a$12$rwZN8FNrI5fuItLQPWKbgezYC2f7mk7d9xESe7f9bxRikQIbQS4Uu','7f515a03-6763-43da-bec7-585b7b48fa1b.png','Bagmati','44600',4,'2025-04-20 21:56:00.443900','2025-04-28 18:35:42.920458'),(3,'Kathmandu','Gokarneshowr-2','lmilan667@gmail.com','Admin','Lama','9860973075','$2a$10$cuFzIg9Uaousu1n0CkpTSePdkXKq2yOnVR6nYkrHbyw3QH/b8g.hW',NULL,'Bagmati','44600',3,'2025-04-20 22:17:34.407021',NULL),(5,'Nayapati','Gokarneshowr-2','milan.2021205@nami.edu.np','Mbappe','Lama','9849352272','$2a$12$fMAvdYF48WFIFinzKgHEXuk/e5/hoRAfyiZ.apWZclB6r1T28R85a',NULL,'Bagmati','44600',6,'2025-04-28 19:53:57.977092',NULL);
/*!40000 ALTER TABLE `site_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_role` (
  `user_id` int NOT NULL,
  `role_id` int NOT NULL,
  KEY `FKa68196081fvovjhkek5m97n3y` (`role_id`),
  KEY `FKisluplr06apg2qi71axkoquye` (`user_id`),
  CONSTRAINT `FKa68196081fvovjhkek5m97n3y` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`),
  CONSTRAINT `FKisluplr06apg2qi71axkoquye` FOREIGN KEY (`user_id`) REFERENCES `site_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES (2,2),(3,1),(5,1);
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

-- Dump completed on 2025-05-03 23:50:55
