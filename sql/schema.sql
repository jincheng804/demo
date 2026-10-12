-- Fresh installation only: recreates existing tables. Use upgrade_review_20261011.sql for an existing database.
CREATE DATABASE IF NOT EXISTS bee_farm CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE bee_farm;
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
DROP TABLE IF EXISTS `biz_apiary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_apiary` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `apiary_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '分区名称',
  `location` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '位置',
  `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
  `manager` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '负责人',
  `dept_id` bigint DEFAULT NULL COMMENT '所属部门',
  `status` tinyint DEFAULT '1',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='蜂场分区表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_breeding`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_breeding` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `hive_id` bigint DEFAULT NULL COMMENT '主蜂箱',
  `breed_type` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '类型(自然分蜂/人工分蜂/并群/蜂王培育)',
  `breed_date` date NOT NULL COMMENT '作业日期',
  `source_hive_id` bigint DEFAULT NULL COMMENT '源蜂箱',
  `target_hive_id` bigint DEFAULT NULL COMMENT '目标蜂箱',
  `quantity` int DEFAULT NULL COMMENT '数量(脾/只)',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_breed_hive` (`hive_id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='分蜂繁育表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_colony`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_colony` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `hive_id` bigint NOT NULL COMMENT '蜂箱',
  `record_date` date NOT NULL COMMENT '记录日期',
  `colony_strength` int DEFAULT NULL COMMENT '群势(框)',
  `brood_frame` int DEFAULT NULL COMMENT '子脾数(框)',
  `honey_frame` int DEFAULT NULL COMMENT '蜜脾数(框)',
  `bee_count` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '蜂量(足框/足脾)',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_colony_hive` (`hive_id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='蜂群日常记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_customer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `customer_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '客户名称',
  `contact` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '电话',
  `address` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '地址',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='客户表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_disease`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_disease` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `hive_id` bigint NOT NULL COMMENT '蜂箱',
  `disease_type` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '病害类型(蜂螨/白垩病/囊状幼虫病)',
  `disease_date` date NOT NULL COMMENT '发现日期',
  `level` tinyint DEFAULT '1' COMMENT '严重程度(1轻 2中 3重)',
  `handle_status` tinyint DEFAULT '0' COMMENT '处理状态(0未处理 1已处理)',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_disease_hive` (`hive_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='病害记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_expense`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_expense` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `apiary_id` bigint DEFAULT NULL COMMENT '蜂场/区域',
  `expense_date` date NOT NULL COMMENT '发生日期',
  `expense_type` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '类型(人工/饲料/药品/蜂具/包装/其他)',
  `amount` decimal(12,2) DEFAULT '0.00' COMMENT '金额',
  `summary` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '摘要',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_expense_date` (`expense_date`),
  KEY `idx_expense_type` (`expense_type`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='成本费用表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_feeding`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_feeding` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `hive_id` bigint NOT NULL COMMENT '蜂箱',
  `feed_date` date NOT NULL COMMENT '饲喂日期',
  `feed_type` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '饲料类型(白糖水/花粉/代用花粉)',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '用量',
  `unit` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '单位',
  `operator` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_feed_hive` (`hive_id`)
) ENGINE=InnoDB AUTO_INCREMENT=140 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='饲喂记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_harvest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_harvest` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `batch_no` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '采收批次号',
  `hive_id` bigint NOT NULL COMMENT '蜂箱',
  `source_id` bigint DEFAULT NULL COMMENT '蜜源地',
  `harvest_date` date NOT NULL COMMENT '摇蜜日期',
  `honey_type` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '蜂蜜种类(洋槐蜜/荆条蜜/百花蜜)',
  `weight` decimal(10,2) DEFAULT '0.00' COMMENT '采收重量(kg)',
  `moisture` decimal(5,2) DEFAULT NULL COMMENT '波美度/含水量',
  `operator` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人',
  `status` tinyint DEFAULT '1' COMMENT '状态(1待加工 2已装瓶)',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_harvest_batch` (`batch_no`),
  KEY `idx_harvest_hive` (`hive_id`)
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='摇蜜采收记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_hive`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_hive` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `hive_code` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '蜂箱编号',
  `apiary_id` bigint DEFAULT NULL COMMENT '所属分区',
  `box_type` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '蜂箱型号(朗式/意标/中蜂)',
  `location` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '摆放位置',
  `entry_date` date DEFAULT NULL COMMENT '入栏日期',
  `status` tinyint DEFAULT '1' COMMENT '状态(1正常 2空箱 3维修 4淘汰)',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hive_code` (`hive_code`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='蜂箱档案表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_inspection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_inspection` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `hive_id` bigint NOT NULL COMMENT '蜂箱',
  `inspect_date` date NOT NULL COMMENT '巡检日期',
  `temperature` decimal(5,2) DEFAULT NULL COMMENT '温度(℃)',
  `humidity` decimal(5,2) DEFAULT NULL COMMENT '湿度(%)',
  `colony_potential` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '蜂群势(强/中/弱)',
  `queen_status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '蜂王状态(正常/停产/失王)',
  `feed_status` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '饲料情况',
  `abnormal` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '异常情况',
  `inspector` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '巡检人',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_insp_hive` (`hive_id`),
  KEY `idx_insp_date` (`inspect_date`)
) ENGINE=InnoDB AUTO_INCREMENT=328 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='蜂箱巡检表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_inventory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_inventory` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `warehouse_id` bigint DEFAULT NULL COMMENT '所属仓库',
  `item_type` tinyint NOT NULL COMMENT '类型(1蜂具耗材 2产品)',
  `item_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `spec` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '规格',
  `unit` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '单位',
  `stock` decimal(10,2) DEFAULT '0.00' COMMENT '库存数量',
  `warn_stock` decimal(10,2) DEFAULT '0.00' COMMENT '预警库存',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_inv_warehouse` (`warehouse_id`)
) ENGINE=InnoDB AUTO_INCREMENT=120 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='库存台账表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_medication`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_medication` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `hive_id` bigint NOT NULL COMMENT '蜂箱',
  `disease_id` bigint DEFAULT NULL COMMENT '关联病害',
  `drug_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '药品名称',
  `dose` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '剂量',
  `use_date` date NOT NULL COMMENT '用药日期',
  `withdrawal_days` int DEFAULT '0' COMMENT '休药期(天)',
  `safe_date` date DEFAULT NULL COMMENT '安全采收日期',
  `operator` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_med_hive` (`hive_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用药记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_nectar_source`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_nectar_source` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `source_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '蜜源地名称',
  `plant` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '蜜源植物(洋槐/荆条/油菜)',
  `location` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '地点',
  `longitude` decimal(10,6) DEFAULT NULL,
  `latitude` decimal(10,6) DEFAULT NULL,
  `bloom_start` date DEFAULT NULL COMMENT '花期开始',
  `bloom_end` date DEFAULT NULL COMMENT '花期结束',
  `status` tinyint DEFAULT '1',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='蜜源地表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_outbound`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_outbound` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `outbound_no` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '出库单号',
  `customer_id` bigint DEFAULT NULL COMMENT '客户',
  `total_qty` int DEFAULT '0' COMMENT '总数量',
  `total_amount` decimal(12,2) DEFAULT '0.00' COMMENT '总金额',
  `outbound_date` date DEFAULT NULL COMMENT '出库日期',
  `status` tinyint DEFAULT '0' COMMENT '状态(0待审核 1已出库 2已取消)',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_outbound_no` (`outbound_no`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='出库单表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_outbound_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_outbound_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `outbound_id` bigint NOT NULL COMMENT '出库单',
  `product_batch_id` bigint NOT NULL COMMENT '产品批次',
  `quantity` int DEFAULT '0' COMMENT '数量',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '单价',
  PRIMARY KEY (`id`),
  KEY `idx_oi_outbound` (`outbound_id`)
) ENGINE=InnoDB AUTO_INCREMENT=92 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='出库明细表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_product_batch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_product_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_batch_no` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '产品批次号',
  `harvest_id` bigint NOT NULL COMMENT '来源采收批次',
  `product_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '产品名称',
  `spec` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '规格(500g/1000g)',
  `bottle_count` int DEFAULT '0' COMMENT '装瓶数量',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '定价(元)',
  `produce_date` date DEFAULT NULL COMMENT '生产日期',
  `shelf_life` int DEFAULT '24' COMMENT '保质期(月)',
  `qc_status` tinyint DEFAULT '0' COMMENT '质检状态(0未检 1合格 2不合格)',
  `status` tinyint DEFAULT '1' COMMENT '状态(1在库 2已出库)',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_batch` (`product_batch_no`),
  KEY `idx_pb_harvest` (`harvest_id`)
) ENGINE=InnoDB AUTO_INCREMENT=75 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='产品批次表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_purchase`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_purchase` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `purchase_no` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '采购单号',
  `supplier_id` bigint DEFAULT NULL COMMENT '供应商',
  `warehouse_id` bigint DEFAULT NULL COMMENT '入库仓库',
  `purchase_date` date DEFAULT NULL COMMENT '采购日期',
  `total_qty` decimal(12,2) DEFAULT '0' COMMENT '总数量',
  `total_amount` decimal(12,2) DEFAULT '0.00' COMMENT '总金额',
  `status` tinyint DEFAULT '0' COMMENT '状态(0待审核 1已入库 2已取消)',
  `operator` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '经办人',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_purchase_no` (`purchase_no`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购单表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_purchase_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_purchase_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `purchase_id` bigint NOT NULL COMMENT '采购单',
  `material_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '物料名称',
  `spec` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '规格',
  `quantity` decimal(10,2) DEFAULT '0.00' COMMENT '数量',
  `unit` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '单位',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '单价',
  `amount` decimal(12,2) DEFAULT '0.00' COMMENT '金额',
  PRIMARY KEY (`id`),
  KEY `idx_pi_purchase` (`purchase_id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购明细表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_qc_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_qc_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `qc_id` bigint NOT NULL COMMENT '质检记录ID',
  `field_name` varchar(50) NOT NULL COMMENT '修改字段',
  `field_label` varchar(50) DEFAULT NULL COMMENT '字段中文名',
  `old_value` text COMMENT '旧值',
  `new_value` text COMMENT '新值',
  `operator` varchar(50) DEFAULT NULL COMMENT '操作人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_qc_id` (`qc_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='质检修改历史';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_qc_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_qc_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `batch_id` bigint NOT NULL COMMENT '产品批次',
  `qc_items` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '检测项目',
  `qc_result` tinyint DEFAULT '1' COMMENT '检测结果(1合格 2不合格)',
  `qc_value` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '检测指标值',
  `inspector` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '检测人',
  `qc_date` datetime DEFAULT NULL,
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_qc_batch` (`batch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='质检记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_queen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_queen` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `queen_code` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '蜂王编号',
  `hive_id` bigint DEFAULT NULL COMMENT '所在蜂箱',
  `breed` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '品种(意蜂/中蜂/喀尔巴阡)',
  `birth_date` date DEFAULT NULL COMMENT '出房日期',
  `status` tinyint DEFAULT '1' COMMENT '状态(1正常 2停产 3淘汰)',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_queen_code` (`queen_code`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='蜂王档案表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_scan_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_scan_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `trace_code` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '溯源码',
  `scan_time` datetime NOT NULL COMMENT '扫码时间',
  `ip` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'IP地址',
  `device_type` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '设备类型(手机/微信/浏览器)',
  `user_agent` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'User-Agent',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_scanlog_code` (`trace_code`),
  KEY `idx_scanlog_time` (`scan_time`)
) ENGINE=InnoDB AUTO_INCREMENT=461 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='扫码日志表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_stock_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_stock_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `item_id` bigint DEFAULT NULL COMMENT '库存物品',
  `warehouse_id` bigint DEFAULT NULL COMMENT '所属仓库',
  `batch_id` bigint DEFAULT NULL COMMENT '关联产品批次',
  `biz_type` tinyint NOT NULL COMMENT '类型(1入库 2出库 3盘盈 4盘亏)',
  `quantity` decimal(10,2) DEFAULT '0.00' COMMENT '数量',
  `stock_after` decimal(10,2) DEFAULT NULL COMMENT '操作后库存',
  `operator` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=207 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='出入库流水表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_supplier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_supplier` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `supplier_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '供应商名称',
  `supplier_type` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '类型(蜂具/饲料/药品/包装)',
  `contact` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '电话',
  `address` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '地址',
  `status` tinyint DEFAULT '1' COMMENT '状态(1正常 0停用)',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='供应商表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_trace_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_trace_code` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `trace_code` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '溯源码',
  `product_batch_id` bigint NOT NULL COMMENT '产品批次',
  `qr_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '二维码内容URL',
  `scan_count` int DEFAULT '0' COMMENT '扫码次数',
  `last_scan_time` datetime DEFAULT NULL COMMENT '最后扫码时间',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trace_code` (`trace_code`),
  KEY `idx_trace_batch` (`product_batch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='溯源码表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_warehouse`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_warehouse` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `wh_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '仓库名称',
  `wh_type` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '仓库类型(原料库/成品库/蜂具库)',
  `location` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '仓库位置',
  `manager` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '负责人',
  `phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系电话',
  `status` tinyint DEFAULT '1' COMMENT '状态(1正常 0停用)',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='仓库表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biz_warning`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_warning` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `warning_type` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '类型(休药期/库存不足/蜂箱异常/待质检/待审核)',
  `biz_id` bigint DEFAULT NULL COMMENT '业务ID',
  `content` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '预警内容',
  `level` tinyint DEFAULT '1' COMMENT '级别(1低 2中 3高)',
  `status` tinyint DEFAULT '0' COMMENT '状态(0未处理 1已处理)',
  `generate_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  `handle_by` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '处理人',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_warning_status` (`status`),
  KEY `idx_warning_type` (`warning_type`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='预警表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_dept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门ID',
  `ancestors` varchar(200) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '部门名称',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `leader` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '负责人',
  `phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系电话',
  `status` tinyint DEFAULT '1' COMMENT '状态(1正常 0停用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='部门(蜂场)表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_dict_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_data` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dict_type` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典类型',
  `dict_label` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典标签',
  `dict_value` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典键值',
  `css_class` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `order_num` int DEFAULT '0',
  `status` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典数据表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_dict_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_type` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dict_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典名称',
  `dict_type` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典类型',
  `status` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典类型表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_login_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `ip` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status` tinyint DEFAULT '1' COMMENT '1成功 0失败',
  `msg` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `login_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='登录日志表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID',
  `menu_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  `menu_type` char(1) COLLATE utf8mb4_general_ci DEFAULT 'M' COMMENT '类型(M目录 C菜单 F按钮)',
  `perms` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '权限标识',
  `path` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '路由地址',
  `component` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '菜单图标',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `visible` tinyint DEFAULT '1' COMMENT '是否可见',
  `status` tinyint DEFAULT '1' COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=95205 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单/权限表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_oper_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_oper_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '模块标题',
  `method` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '请求方式',
  `oper_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '请求URL',
  `oper_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人员',
  `oper_param` text COLLATE utf8mb4_general_ci COMMENT '请求参数',
  `ip` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '主机地址',
  `status` tinyint DEFAULT '1' COMMENT '状态(1正常 0异常)',
  `error_msg` text COLLATE utf8mb4_general_ci COMMENT '错误消息',
  `oper_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色编码',
  `data_scope` tinyint DEFAULT '1' COMMENT '数据范围(1全部 2本部门 3本人)',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `status` tinyint DEFAULT '1' COMMENT '状态(1正常 0停用)',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_role_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_menu` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色-菜单关联表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录账号',
  `auth_version` int NOT NULL DEFAULT 0 COMMENT '登录令牌版本',
  `password` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码(BCrypt)',
  `real_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '手机号',
  `email` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱',
  `gender` tinyint DEFAULT '0' COMMENT '性别(0未知 1男 2女)',
  `avatar` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '头像',
  `status` tinyint DEFAULT '1' COMMENT '状态(1正常 0停用)',
  `login_ip` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0' COMMENT '删除标志(0存在 1删除)',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户-角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


-- Internal transaction guard: serializes the small farm's stock-changing transactions.
CREATE TABLE IF NOT EXISTS sys_business_lock (id INT PRIMARY KEY) ENGINE=InnoDB;
INSERT IGNORE INTO sys_business_lock(id) VALUES (1);
