-- MySQL dump 10.13  Distrib 5.7.24, for osx10.14 (x86_64)
--
-- Host: 127.0.0.1    Database: global
-- ------------------------------------------------------
-- Server version	5.7.22-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `AppVersion`
--

DROP TABLE IF EXISTS `AppVersion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AppVersion` (
  `oid` bigint(20) NOT NULL,
  `completeDatetime` datetime DEFAULT NULL,
  `startDatetime` datetime NOT NULL,
  `version` varchar(255) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `version` (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AppliedPatch`
--

DROP TABLE IF EXISTS `AppliedPatch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AppliedPatch` (
  `oid` bigint(20) NOT NULL,
  `completeDatetime` datetime DEFAULT NULL,
  `data` longtext,
  `errorText` longtext,
  `name` varchar(255) NOT NULL,
  `runTimeMs` bigint(20) NOT NULL,
  `version` varchar(100) DEFAULT NULL,
  `partition_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `name` (`name`,`partition_oid`),
  KEY `FKAE9419EB6D6C7C2C` (`partition_oid`),
  CONSTRAINT `FKAE9419EB6D6C7C2C` FOREIGN KEY (`partition_oid`) REFERENCES `DBPartition` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Area`
--

DROP TABLE IF EXISTS `Area`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Area` (
  `oid` bigint(20) NOT NULL,
  `areaName` varchar(80) NOT NULL,
  `creationDate` datetime NOT NULL,
  `expirationDate` datetime DEFAULT NULL,
  `publication` bit(1) NOT NULL,
  `areaPicture_oid` bigint(20) DEFAULT NULL,
  `areaResource_oid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `area_creation_date_idx` (`creationDate`),
  KEY `FK1F44AD8130E1C6` (`areaPicture_oid`),
  CONSTRAINT `FK1F44AD8130E1C6` FOREIGN KEY (`areaPicture_oid`) REFERENCES `FileOnDisk` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AreaCircle`
--

DROP TABLE IF EXISTS `AreaCircle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AreaCircle` (
  `oid` bigint(20) NOT NULL,
  `label` varchar(20) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `viewableByAdminsOnly` bit(1) NOT NULL,
  `area_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  KEY `fk_areaCircle_area` (`area_oid`),
  CONSTRAINT `fk_areaCircle_area` FOREIGN KEY (`area_oid`) REFERENCES `Area` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AreaCirclePermission`
--

DROP TABLE IF EXISTS `AreaCirclePermission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AreaCirclePermission` (
  `oid` bigint(20) NOT NULL,
  `securableType` int(11) NOT NULL,
  `areaCircle_oid` bigint(20) NOT NULL,
  `areaResource_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `areaCircle_oid` (`areaCircle_oid`,`areaResource_oid`,`securableType`),
  KEY `fk_areaCirclePermission_group` (`areaCircle_oid`),
  KEY `fk_areaCirclePermission_resource` (`areaResource_oid`),
  CONSTRAINT `fk_areaCirclePermission_group` FOREIGN KEY (`areaCircle_oid`) REFERENCES `AreaCircle` (`oid`),
  CONSTRAINT `fk_areaCirclePermission_resource` FOREIGN KEY (`areaResource_oid`) REFERENCES `AreaResource` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AreaCircleUser`
--

DROP TABLE IF EXISTS `AreaCircleUser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AreaCircleUser` (
  `oid` bigint(20) NOT NULL,
  `areaCircle_oid` bigint(20) NOT NULL,
  `areaUser_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `areaCircle_oid` (`areaCircle_oid`,`areaUser_oid`),
  KEY `fk_areaCircleUser_areaUser` (`areaUser_oid`),
  KEY `fk_areaCircleUser_areaCircle` (`areaCircle_oid`),
  CONSTRAINT `fk_areaCircleUser_areaCircle` FOREIGN KEY (`areaCircle_oid`) REFERENCES `AreaCircle` (`oid`),
  CONSTRAINT `fk_areaCircleUser_areaUser` FOREIGN KEY (`areaUser_oid`) REFERENCES `AreaUser` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AreaCredentials`
--

DROP TABLE IF EXISTS `AreaCredentials`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AreaCredentials` (
  `oid` bigint(20) NOT NULL,
  `emailAddress` varchar(255) NOT NULL,
  `passwordFields_hashedPassword` varchar(255) NOT NULL,
  `areaRlm_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `areaRlm_oid` (`areaRlm_oid`,`emailAddress`),
  KEY `fk_areaCredentials_areaRlm` (`areaRlm_oid`),
  CONSTRAINT `fk_areaCredentials_areaRlm` FOREIGN KEY (`areaRlm_oid`) REFERENCES `AreaRlm` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AreaPropertyOverride`
--

DROP TABLE IF EXISTS `AreaPropertyOverride`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AreaPropertyOverride` (
  `oid` bigint(20) NOT NULL,
  `propertyType` varchar(255) NOT NULL,
  `value` longtext,
  `areaPropertySet_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `areaPropertySet_oid` (`areaPropertySet_oid`,`propertyType`),
  KEY `FK8DA9478E1A5DBDD` (`areaPropertySet_oid`),
  CONSTRAINT `FK8DA9478E1A5DBDD` FOREIGN KEY (`areaPropertySet_oid`) REFERENCES `AreaPropertySet` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AreaPropertySet`
--

DROP TABLE IF EXISTS `AreaPropertySet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AreaPropertySet` (
  `oid` bigint(20) NOT NULL,
  `lastModificationDate` datetime NOT NULL,
  `propertySetType` varchar(40) NOT NULL,
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AreaResource`
--

DROP TABLE IF EXISTS `AreaResource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AreaResource` (
  `oid` bigint(20) NOT NULL,
  `areaResourceType` int(11) NOT NULL,
  `area_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  KEY `fk_areaResource_area` (`area_oid`),
  CONSTRAINT `fk_areaResource_area` FOREIGN KEY (`area_oid`) REFERENCES `Area` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AreaRlm`
--

DROP TABLE IF EXISTS `AreaRlm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AreaRlm` (
  `oid` bigint(20) NOT NULL,
  `authZone` bigint(20) NOT NULL,
  `defaultPortfolio_oid` bigint(20) DEFAULT NULL,
  `settingsSet_oid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `FK36BA2186282785FC` (`settingsSet_oid`),
  KEY `fk_areaRlm_portfolio` (`defaultPortfolio_oid`),
  CONSTRAINT `FK36BA2186282785FC` FOREIGN KEY (`settingsSet_oid`) REFERENCES `AreaPropertySet` (`oid`),
  CONSTRAINT `fk_areaRlm_portfolio` FOREIGN KEY (`defaultPortfolio_oid`) REFERENCES `Portfolio` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AreaStats`
--

DROP TABLE IF EXISTS `AreaStats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AreaStats` (
  `oid` bigint(20) NOT NULL,
  `memberCount` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  CONSTRAINT `fk_areastats_area` FOREIGN KEY (`oid`) REFERENCES `Area` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AreaUser`
--

DROP TABLE IF EXISTS `AreaUser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AreaUser` (
  `oid` bigint(20) NOT NULL,
  `createdDatetime` datetime NOT NULL,
  `displayName` varchar(40) NOT NULL,
  `followerCount` int(11) NOT NULL,
  `preferences_notificationSettings` bigint(20) NOT NULL,
  `area_oid` bigint(20) NOT NULL,
  `user_oid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `user_oid` (`user_oid`,`area_oid`),
  KEY `area_user_created_datetime_idx` (`createdDatetime`),
  KEY `areaUser_displayName_idx` (`displayName`),
  KEY `FKA08B86187EB27D35` (`user_oid`),
  KEY `FKA08B861810FFDA37` (`area_oid`),
  CONSTRAINT `FKA08B861810FFDA37` FOREIGN KEY (`area_oid`) REFERENCES `Area` (`oid`),
  CONSTRAINT `FKA08B86187EB27D35` FOREIGN KEY (`user_oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AreaUserRlm`
--

DROP TABLE IF EXISTS `AreaUserRlm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AreaUserRlm` (
  `oid` bigint(20) NOT NULL,
  `areaRlm_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  KEY `FKCC8AE43B2603C9A5` (`areaRlm_oid`),
  CONSTRAINT `FKCC8AE43B2603C9A5` FOREIGN KEY (`areaRlm_oid`) REFERENCES `AreaRlm` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AreaUserStats`
--

DROP TABLE IF EXISTS `AreaUserStats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AreaUserStats` (
  `oid` bigint(20) NOT NULL,
  `cumulativeContentCounts_typeCounts` varchar(255) DEFAULT NULL,
  `cumulativeReplyCounts_typeCounts` varchar(255) DEFAULT NULL,
  `lastLoginDatetime` datetime DEFAULT NULL,
  `previousLoginDatetime` datetime DEFAULT NULL,
  PRIMARY KEY (`oid`),
  CONSTRAINT `fk_areauserstats_areauser` FOREIGN KEY (`oid`) REFERENCES `AreaUser` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Channel`
--

DROP TABLE IF EXISTS `Channel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Channel` (
  `oid` bigint(20) NOT NULL,
  `type` int(11) NOT NULL,
  `primaryDomain_oid` bigint(20) DEFAULT NULL,
  `purchaseInvoice_oid` bigint(20) DEFAULT NULL,
  `statusRelatedTribunalIssuesLockedUntilDatetime` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `fk_channel_primaryDomain` (`primaryDomain_oid`),
  KEY `fk_channel_purchaseInvoice` (`purchaseInvoice_oid`),
  CONSTRAINT `fk_channel_primaryDomain` FOREIGN KEY (`primaryDomain_oid`) REFERENCES `ChannelDomain` (`oid`),
  CONSTRAINT `fk_channel_purchaseInvoice` FOREIGN KEY (`purchaseInvoice_oid`) REFERENCES `Invoice` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ChannelContent`
--

DROP TABLE IF EXISTS `ChannelContent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ChannelContent` (
  `oid` bigint(20) NOT NULL,
  `status` int(11) NOT NULL,
  `channel_oid` bigint(20) NOT NULL,
  `content_oid` bigint(20) NOT NULL,
  `featuredDatetime` bigint(20) DEFAULT NULL,
  `featuredUntilDatetime` bigint(20) DEFAULT NULL,
  `moderationDatetime` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `uidx_channelContent_content_channel` (`content_oid`,`channel_oid`),
  KEY `fk_channelContent_channel` (`channel_oid`),
  CONSTRAINT `fk_channelContent_channel` FOREIGN KEY (`channel_oid`) REFERENCES `Channel` (`oid`),
  CONSTRAINT `fk_channelContent_content` FOREIGN KEY (`content_oid`) REFERENCES `Content` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ChannelDomain`
--

DROP TABLE IF EXISTS `ChannelDomain`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ChannelDomain` (
  `oid` bigint(20) NOT NULL,
  `domainName` varchar(255) NOT NULL,
  `channel_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `channelDomain_domainName_uidx` (`domainName`),
  KEY `fk_channelDomain_channel` (`channel_oid`),
  CONSTRAINT `fk_channelDomain_channel` FOREIGN KEY (`channel_oid`) REFERENCES `Channel` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ChannelUser`
--

DROP TABLE IF EXISTS `ChannelUser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ChannelUser` (
  `oid` bigint(20) NOT NULL,
  `roles` bigint(20) NOT NULL,
  `channel_oid` bigint(20) NOT NULL,
  `user_oid` bigint(20) NOT NULL,
  `invitedRoles` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `channelUser_channel_user_uidx` (`channel_oid`,`user_oid`),
  KEY `fk_channelUser_user` (`user_oid`),
  CONSTRAINT `fk_channelUser_channel` FOREIGN KEY (`channel_oid`) REFERENCES `Channel` (`oid`),
  CONSTRAINT `fk_channelUser_user` FOREIGN KEY (`user_oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Content`
--

DROP TABLE IF EXISTS `Content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Content` (
  `oid` bigint(20) NOT NULL,
  `allowReplies` bit(1) NOT NULL,
  `avatarImageOnDisk_oid` bigint(20) DEFAULT NULL,
  `compositionPartitionOid` bigint(20) NOT NULL,
  `contentStatus` int(11) NOT NULL,
  `contentType` int(11) NOT NULL,
  `extract` varchar(500) NOT NULL,
  `guestName` varchar(40) DEFAULT NULL,
  `lastUpdateDatetime` datetime NOT NULL,
  `liveDatetime` datetime NOT NULL,
  `moderationStatus` enum('APPROVED','PENDING_APPROVAL','MODERATED') NOT NULL,
  `prettyUrlString` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `subject` varchar(255) NOT NULL,
  `areaUserRlm_oid` bigint(20) DEFAULT NULL,
  `futureContent_oid` bigint(20) DEFAULT NULL,
  `portfolio_oid` bigint(20) NOT NULL,
  `authorAgeRating` tinyint(4) NOT NULL,
  `subTitle` varchar(255) DEFAULT NULL,
  `ageRating` tinyint(4) NOT NULL,
  `ageRatingFields_generalCount` int(11) NOT NULL,
  `ageRatingFields_generalPoints` int(11) NOT NULL,
  `ageRatingFields_restrictedCount` int(11) NOT NULL,
  `ageRatingFields_restrictedPoints` int(11) NOT NULL,
  `qualityRatingFields_dislikeContentViolatesAupCount` int(11) NOT NULL,
  `qualityRatingFields_dislikeContentViolatesAupPoints` int(11) NOT NULL,
  `qualityRatingFields_dislikeDisagreeWithViewpointCount` int(11) NOT NULL,
  `qualityRatingFields_dislikeLowQualityContentCount` int(11) NOT NULL,
  `qualityRatingFields_dislikeLowQualityContentPoints` int(11) NOT NULL,
  `qualityRatingFields_likeCount` int(11) NOT NULL,
  `qualityRatingFields_likePoints` int(11) NOT NULL,
  `qualityRatingFields_score` int(11) NOT NULL,
  `qualityRatingFields_dislikeDisagreeWithViewpointPoints` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `portfolio_oid` (`portfolio_oid`,`contentType`,`prettyUrlString`),
  KEY `content_live_datetime_idx` (`liveDatetime`),
  KEY `content_last_update_datetime_idx` (`lastUpdateDatetime`),
  KEY `fk_content_futureContent` (`futureContent_oid`),
  KEY `FK9BEFCC59A04A123C` (`areaUserRlm_oid`),
  KEY `fk_content_portfolio` (`portfolio_oid`),
  KEY `fk_content_avatarImageOnDisk` (`avatarImageOnDisk_oid`),
  CONSTRAINT `FK9BEFCC59A04A123C` FOREIGN KEY (`areaUserRlm_oid`) REFERENCES `AreaUserRlm` (`oid`),
  CONSTRAINT `fk_content_avatarImageOnDisk` FOREIGN KEY (`avatarImageOnDisk_oid`) REFERENCES `FileOnDisk` (`oid`),
  CONSTRAINT `fk_content_futureContent` FOREIGN KEY (`futureContent_oid`) REFERENCES `FutureContent` (`oid`),
  CONSTRAINT `fk_content_portfolio` FOREIGN KEY (`portfolio_oid`) REFERENCES `Portfolio` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ContentReward`
--

DROP TABLE IF EXISTS `ContentReward`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ContentReward` (
  `oid` bigint(20) NOT NULL AUTO_INCREMENT,
  `contentOid` bigint(20) NOT NULL,
  `points` bigint(20) NOT NULL,
  `period_oid` bigint(20) NOT NULL,
  `user_oid` bigint(20) NOT NULL,
  `publicationReward_oid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `contentReward_contentOid_period_uidx` (`contentOid`,`period_oid`),
  KEY `fk_contentReward_period` (`period_oid`),
  KEY `fk_contentReward_user` (`user_oid`),
  KEY `fk_contentReward_publicationReward` (`publicationReward_oid`),
  CONSTRAINT `fk_contentReward_period` FOREIGN KEY (`period_oid`) REFERENCES `RewardPeriod` (`oid`),
  CONSTRAINT `fk_contentReward_publicationReward` FOREIGN KEY (`publicationReward_oid`) REFERENCES `PublicationReward` (`oid`),
  CONSTRAINT `fk_contentReward_user` FOREIGN KEY (`user_oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ContentStats`
--

DROP TABLE IF EXISTS `ContentStats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ContentStats` (
  `oid` bigint(20) NOT NULL,
  `areaLikeFields_likeCount` int(11) NOT NULL,
  `areaPageViews` int(11) NOT NULL,
  `globalLikeFields_likeCount` int(11) NOT NULL,
  `lastReplyGuestName` varchar(255) DEFAULT NULL,
  `lastReplyUserOid` bigint(20) DEFAULT NULL,
  `moderatedReplyCount` int(11) NOT NULL,
  `replyCount` int(11) NOT NULL,
  `reportCount` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  CONSTRAINT `fk_contentstats_content` FOREIGN KEY (`oid`) REFERENCES `Content` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DBPartition`
--

DROP TABLE IF EXISTS `DBPartition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DBPartition` (
  `oid` bigint(20) NOT NULL,
  `databaseName` varchar(50) NOT NULL,
  `parameters` varchar(255) DEFAULT NULL,
  `partitionType` enum('GLOBAL','COMPOSITION','UTILITY') NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `rootPassword` varchar(255) DEFAULT NULL,
  `server` varchar(100) NOT NULL,
  `username` varchar(255) DEFAULT NULL,
  `weight` int(11) NOT NULL,
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DeletedChannel`
--

DROP TABLE IF EXISTS `DeletedChannel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DeletedChannel` (
  `oid` bigint(20) NOT NULL,
  `name` varchar(60) NOT NULL,
  `type` int(11) NOT NULL,
  `owner_oid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `fk_deletedChannel_owner` (`owner_oid`),
  CONSTRAINT `fk_deletedChannel_owner` FOREIGN KEY (`owner_oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Election`
--

DROP TABLE IF EXISTS `Election`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Election` (
  `oid` bigint(20) NOT NULL,
  `availableSlots` int(11) NOT NULL,
  `nominationStartDatetime` bigint(20) NOT NULL,
  `status` int(11) NOT NULL,
  `type` int(11) NOT NULL,
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ElectionNominee`
--

DROP TABLE IF EXISTS `ElectionNominee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ElectionNominee` (
  `oid` bigint(20) NOT NULL,
  `nominationConfirmedDatetime` bigint(20) DEFAULT NULL,
  `personalStatement` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` int(11) NOT NULL,
  `election_oid` bigint(20) NOT NULL,
  `nominee_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `uk_electionNominee_nominee_election` (`nominee_oid`,`election_oid`),
  KEY `fk_electionNominee_election` (`election_oid`),
  CONSTRAINT `fk_electionNominee_election` FOREIGN KEY (`election_oid`) REFERENCES `Election` (`oid`),
  CONSTRAINT `fk_electionNominee_nominee` FOREIGN KEY (`nominee_oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EmailAddress`
--

DROP TABLE IF EXISTS `EmailAddress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EmailAddress` (
  `oid` bigint(20) NOT NULL,
  `creationDatetime` bigint(20) NOT NULL,
  `emailAddress` varchar(255) NOT NULL,
  `type` int(11) NOT NULL,
  `verifiedSteps` bigint(20) NOT NULL,
  `user_oid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `emailAddress_emailAddress_uidx` (`emailAddress`),
  UNIQUE KEY `emailAddress_user_type_uidx` (`user_oid`,`type`),
  CONSTRAINT `fk_emailAddress_user` FOREIGN KEY (`user_oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EventMessage`
--

DROP TABLE IF EXISTS `EventMessage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventMessage` (
  `eventId` binary(16) NOT NULL,
  `creationDatetime` bigint(20) NOT NULL,
  `eventJson` mediumtext NOT NULL,
  `failCount` int(11) NOT NULL,
  `lastSentDatetime` bigint(20) DEFAULT NULL,
  `sendCount` int(11) NOT NULL,
  `sendDatetime` bigint(20) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `retryCount` int(11) NOT NULL,
  PRIMARY KEY (`eventId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `FiatPayment`
--

DROP TABLE IF EXISTS `FiatPayment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FiatPayment` (
  `oid` bigint(20) NOT NULL,
  `nrveAmount` bigint(20) DEFAULT NULL,
  `transactionDate` datetime(6) DEFAULT NULL,
  `transactionId` varchar(64) DEFAULT NULL,
  `feeUsdAmount` decimal(19,2) NOT NULL,
  `status` int(11) NOT NULL,
  `usdAmount` decimal(19,2) NOT NULL,
  `processorType` int(11) DEFAULT NULL,
  `paymentWalletTransaction_oid` bigint(20) DEFAULT NULL,
  `refundWalletTransaction_oid` bigint(20) DEFAULT NULL,
  `reversalWalletTransaction_oid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `uk_fiatPayment_transactionId` (`transactionId`),
  KEY `fk_fiatPayment_paymentWalletTransaction` (`paymentWalletTransaction_oid`),
  KEY `fk_fiatPayment_refundWalletTransaction` (`refundWalletTransaction_oid`),
  KEY `fk_fiatPayment_reversalWalletTransaction` (`reversalWalletTransaction_oid`),
  CONSTRAINT `fk_fiatPayment_invoice` FOREIGN KEY (`oid`) REFERENCES `Invoice` (`oid`),
  CONSTRAINT `fk_fiatPayment_paymentWalletTransaction` FOREIGN KEY (`paymentWalletTransaction_oid`) REFERENCES `WalletTransaction` (`oid`),
  CONSTRAINT `fk_fiatPayment_refundWalletTransaction` FOREIGN KEY (`refundWalletTransaction_oid`) REFERENCES `WalletTransaction` (`oid`),
  CONSTRAINT `fk_fiatPayment_reversalWalletTransaction` FOREIGN KEY (`reversalWalletTransaction_oid`) REFERENCES `WalletTransaction` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `FileOnDisk`
--

DROP TABLE IF EXISTS `FileOnDisk`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FileOnDisk` (
  `fileType` varchar(31) NOT NULL,
  `oid` bigint(20) NOT NULL,
  `byteSize` int(11) NOT NULL,
  `filename` varchar(255) NOT NULL,
  `metaData` longblob,
  `mimeType` varchar(80) NOT NULL,
  `authZone` bigint(20) NOT NULL,
  `creationDatetime` datetime NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `fileUsageType` int(11) NOT NULL,
  `status` tinyint(4) NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `user_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  KEY `fileOnDisk_authZone_fileUsageType_idx` (`authZone`,`fileUsageType`),
  KEY `FKDAF838D87EB27D35` (`user_oid`),
  CONSTRAINT `FKDAF838D87EB27D35` FOREIGN KEY (`user_oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `FileOnDiskStats`
--

DROP TABLE IF EXISTS `FileOnDiskStats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FileOnDiskStats` (
  `oid` bigint(20) NOT NULL,
  `downloadCount` int(11) NOT NULL,
  `streamCount` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  CONSTRAINT `fk_fileondiskstats_fileondisk` FOREIGN KEY (`oid`) REFERENCES `FileOnDisk` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `FollowedChannel`
--

DROP TABLE IF EXISTS `FollowedChannel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FollowedChannel` (
  `oid` bigint(20) NOT NULL,
  `channel_oid` bigint(20) DEFAULT NULL,
  `follower_oid` bigint(20) DEFAULT NULL,
  `followDatetime` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `follower_channel_uidx` (`follower_oid`,`channel_oid`),
  KEY `fk_followedChannel_channel` (`channel_oid`),
  CONSTRAINT `fk_followedChannel_channel` FOREIGN KEY (`channel_oid`) REFERENCES `Channel` (`oid`),
  CONSTRAINT `fk_followedChannel_follower` FOREIGN KEY (`follower_oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `FutureContent`
--

DROP TABLE IF EXISTS `FutureContent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FutureContent` (
  `oid` bigint(20) NOT NULL,
  `draft` bit(1) NOT NULL,
  `saveDatetime` datetime NOT NULL,
  `userOid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Invoice`
--

DROP TABLE IF EXISTS `Invoice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Invoice` (
  `oid` bigint(20) NOT NULL,
  `invoiceDatetime` datetime(6) NOT NULL,
  `nrveAmount` bigint(20) DEFAULT NULL,
  `paymentDueDatetime` datetime(6) NOT NULL,
  `status` int(11) NOT NULL,
  `type` int(11) NOT NULL,
  `updateDatetime` datetime(6) DEFAULT NULL,
  `usdAmount` decimal(19,2) DEFAULT NULL,
  `fiatPayment_oid` bigint(20) DEFAULT NULL,
  `nrvePayment_oid` bigint(20) DEFAULT NULL,
  `user_oid` bigint(20) NOT NULL,
  `refundDatetime` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `fk_invoice_fiatPayment` (`fiatPayment_oid`),
  KEY `fk_invoice_nrvePayment` (`nrvePayment_oid`),
  KEY `fk_invoice_user` (`user_oid`),
  CONSTRAINT `fk_invoice_fiatPayment` FOREIGN KEY (`fiatPayment_oid`) REFERENCES `FiatPayment` (`oid`),
  CONSTRAINT `fk_invoice_nrvePayment` FOREIGN KEY (`nrvePayment_oid`) REFERENCES `NrvePayment` (`oid`),
  CONSTRAINT `fk_invoice_user` FOREIGN KEY (`user_oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ItemHourTrendingStats`
--

DROP TABLE IF EXISTS `ItemHourTrendingStats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ItemHourTrendingStats` (
  `oid` bigint(20) NOT NULL,
  `hoursSinceEpoch` bigint(20) NOT NULL,
  `objectOid` bigint(20) NOT NULL,
  `replyPoints` bigint(20) NOT NULL,
  `viewPoints` bigint(20) NOT NULL,
  `likePoints` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `objectOid` (`objectOid`,`hoursSinceEpoch`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `LedgerEntry`
--

DROP TABLE IF EXISTS `LedgerEntry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `LedgerEntry` (
  `oid` bigint(20) NOT NULL,
  `eventDatetime` bigint(20) NOT NULL,
  `properties` longtext,
  `type` int(11) NOT NULL,
  `actor_oid` bigint(20) DEFAULT NULL,
  `auction_oid` bigint(20) DEFAULT NULL,
  `auctionBid_oid` bigint(20) DEFAULT NULL,
  `issue_oid` bigint(20) DEFAULT NULL,
  `issueReport_oid` bigint(20) DEFAULT NULL,
  `channel_oid` bigint(20) DEFAULT NULL,
  `referendum_oid` bigint(20) DEFAULT NULL,
  `election_oid` bigint(20) DEFAULT NULL,
  `invoice_oid` bigint(20) DEFAULT NULL,
  `contentOid` bigint(20) DEFAULT NULL,
  `commentOid` bigint(20) DEFAULT NULL,
  `author_oid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `fk_ledgerEntry_niche` (`channel_oid`),
  KEY `fk_ledgerEntry_actor` (`actor_oid`),
  KEY `fk_ledgerEntry_issueReport` (`issueReport_oid`),
  KEY `fk_ledgerEntry_auction` (`auction_oid`),
  KEY `fk_ledgerEntry_auctionBid` (`auctionBid_oid`),
  KEY `fk_ledgerEntry_issue` (`issue_oid`),
  KEY `fk_ledgerEntry_referendum` (`referendum_oid`),
  KEY `fk_ledgerEntry_election` (`election_oid`),
  KEY `fk_ledgerEntry_invoice` (`invoice_oid`),
  KEY `fk_ledgerEntry_author` (`author_oid`),
  CONSTRAINT `fk_ledgerEntry_actor` FOREIGN KEY (`actor_oid`) REFERENCES `AreaUserRlm` (`oid`),
  CONSTRAINT `fk_ledgerEntry_auction` FOREIGN KEY (`auction_oid`) REFERENCES `NicheAuction` (`oid`),
  CONSTRAINT `fk_ledgerEntry_auctionBid` FOREIGN KEY (`auctionBid_oid`) REFERENCES `NicheAuctionBid` (`oid`),
  CONSTRAINT `fk_ledgerEntry_author` FOREIGN KEY (`author_oid`) REFERENCES `User` (`oid`),
  CONSTRAINT `fk_ledgerEntry_channel` FOREIGN KEY (`channel_oid`) REFERENCES `Channel` (`oid`),
  CONSTRAINT `fk_ledgerEntry_election` FOREIGN KEY (`election_oid`) REFERENCES `Election` (`oid`),
  CONSTRAINT `fk_ledgerEntry_invoice` FOREIGN KEY (`invoice_oid`) REFERENCES `Invoice` (`oid`),
  CONSTRAINT `fk_ledgerEntry_issue` FOREIGN KEY (`issue_oid`) REFERENCES `TribunalIssue` (`oid`),
  CONSTRAINT `fk_ledgerEntry_issueReport` FOREIGN KEY (`issueReport_oid`) REFERENCES `TribunalIssueReport` (`oid`),
  CONSTRAINT `fk_ledgerEntry_referendum` FOREIGN KEY (`referendum_oid`) REFERENCES `Referendum` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ModeratedContent`
--

DROP TABLE IF EXISTS `ModeratedContent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ModeratedContent` (
  `oid` bigint(20) NOT NULL,
  `moderationDatetime` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  CONSTRAINT `fk_moderatedContent_content` FOREIGN KEY (`oid`) REFERENCES `Content` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `NarrativeCompanyReward`
--

DROP TABLE IF EXISTS `NarrativeCompanyReward`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NarrativeCompanyReward` (
  `oid` bigint(20) NOT NULL,
  `period_oid` bigint(20) NOT NULL,
  `transaction_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `narrativeCompanyReward_period_uidx` (`period_oid`),
  KEY `fk_narrativeCompanyReward_transaction` (`transaction_oid`),
  CONSTRAINT `fk_narrativeCompanyReward_period` FOREIGN KEY (`period_oid`) REFERENCES `RewardPeriod` (`oid`),
  CONSTRAINT `fk_narrativeCompanyReward_transaction` FOREIGN KEY (`transaction_oid`) REFERENCES `WalletTransaction` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `NeoTransaction`
--

DROP TABLE IF EXISTS `NeoTransaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NeoTransaction` (
  `oid` bigint(20) NOT NULL,
  `type` int(11) NOT NULL,
  `fromNeoWallet_oid` bigint(20) NOT NULL,
  `toNeoWallet_oid` bigint(20) NOT NULL,
  `nrveAmount` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  KEY `fk_neoTransaction_fromNeoWallet` (`fromNeoWallet_oid`),
  KEY `fk_neoTransaction_toNeoWallet` (`toNeoWallet_oid`),
  CONSTRAINT `fk_neoTransaction_fromNeoWallet` FOREIGN KEY (`fromNeoWallet_oid`) REFERENCES `NeoWallet` (`oid`),
  CONSTRAINT `fk_neoTransaction_toNeoWallet` FOREIGN KEY (`toNeoWallet_oid`) REFERENCES `NeoWallet` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `NeoTransactionId`
--

DROP TABLE IF EXISTS `NeoTransactionId`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NeoTransactionId` (
  `oid` bigint(20) NOT NULL,
  `blockNumber` bigint(20) NOT NULL,
  `nrveAmount` bigint(20) NOT NULL,
  `transactionDatetime` bigint(20) NOT NULL,
  `transactionId` varchar(64) NOT NULL,
  `neoTransaction_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `uidx_neoTransactionId_transactionId` (`transactionId`),
  KEY `fk_neoTransactionId_neoTransaction` (`neoTransaction_oid`),
  CONSTRAINT `fk_neoTransactionId_neoTransaction` FOREIGN KEY (`neoTransaction_oid`) REFERENCES `NeoTransaction` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `NeoWallet`
--

DROP TABLE IF EXISTS `NeoWallet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NeoWallet` (
  `oid` bigint(20) NOT NULL,
  `neoAddress` varchar(255) DEFAULT NULL,
  `singleton` bit(1) DEFAULT NULL,
  `type` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `uidx_neoWallet_type` (`type`,`singleton`),
  UNIQUE KEY `uidx_neoWallet_neoAddress` (`neoAddress`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Niche`
--

DROP TABLE IF EXISTS `Niche`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Niche` (
  `oid` bigint(20) NOT NULL,
  `description` varchar(256) DEFAULT NULL,
  `lastStatusChangeDatetime` datetime NOT NULL,
  `name` varchar(100) NOT NULL,
  `prettyUrlString` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `status` int(11) NOT NULL,
  `suggestedDatetime` datetime NOT NULL,
  `activeAuction_oid` bigint(20) DEFAULT NULL,
  `owner_oid` bigint(20) DEFAULT NULL,
  `portfolio_oid` bigint(20) NOT NULL,
  `suggester_oid` bigint(20) NOT NULL,
  `moderatorSlots` int(11) NOT NULL,
  `activeModeratorElection_oid` bigint(20) DEFAULT NULL,
  `renewalDatetime` bigint(20) DEFAULT NULL,
  `reservedName` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `prettyUrlString` (`prettyUrlString`,`portfolio_oid`),
  UNIQUE KEY `niche_reservedName_uidx` (`reservedName`,`portfolio_oid`),
  KEY `fk_niche_portfolio` (`portfolio_oid`),
  KEY `fk_niche_activeAuction` (`activeAuction_oid`),
  KEY `fk_niche_owner` (`owner_oid`),
  KEY `fk_niche_suggester` (`suggester_oid`),
  KEY `fk_niche_activeModeratorElection` (`activeModeratorElection_oid`),
  CONSTRAINT `fk_niche_activeAuction` FOREIGN KEY (`activeAuction_oid`) REFERENCES `NicheAuction` (`oid`),
  CONSTRAINT `fk_niche_activeModeratorElection` FOREIGN KEY (`activeModeratorElection_oid`) REFERENCES `NicheModeratorElection` (`oid`),
  CONSTRAINT `fk_niche_owner` FOREIGN KEY (`owner_oid`) REFERENCES `AreaUserRlm` (`oid`),
  CONSTRAINT `fk_niche_portfolio` FOREIGN KEY (`portfolio_oid`) REFERENCES `Portfolio` (`oid`),
  CONSTRAINT `fk_niche_suggester` FOREIGN KEY (`suggester_oid`) REFERENCES `AreaUserRlm` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `NicheAuction`
--

DROP TABLE IF EXISTS `NicheAuction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NicheAuction` (
  `oid` bigint(20) NOT NULL,
  `endDatetime` datetime DEFAULT NULL,
  `startDatetime` datetime NOT NULL,
  `activeInvoice_oid` bigint(20) DEFAULT NULL,
  `leadingBid_oid` bigint(20) DEFAULT NULL,
  `niche_oid` bigint(20) NOT NULL,
  `nrveUsdPrice` decimal(19,8) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `fk_nicheAuction_niche` (`niche_oid`),
  KEY `fk_nicheAuction_leadingBid` (`leadingBid_oid`),
  KEY `fk_nicheAuction_activeInvoice` (`activeInvoice_oid`),
  CONSTRAINT `fk_nicheAuction_activeInvoice` FOREIGN KEY (`activeInvoice_oid`) REFERENCES `NicheAuctionInvoice` (`oid`),
  CONSTRAINT `fk_nicheAuction_leadingBid` FOREIGN KEY (`leadingBid_oid`) REFERENCES `NicheAuctionBid` (`oid`),
  CONSTRAINT `fk_nicheAuction_niche` FOREIGN KEY (`niche_oid`) REFERENCES `Niche` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `NicheAuctionBid`
--

DROP TABLE IF EXISTS `NicheAuctionBid`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NicheAuctionBid` (
  `oid` bigint(20) NOT NULL,
  `bidDatetime` bigint(20) NOT NULL,
  `maxNrveBid` bigint(20) NOT NULL,
  `nrveBid` bigint(20) NOT NULL,
  `status` int(11) NOT NULL,
  `auction_oid` bigint(20) NOT NULL,
  `bidder_oid` bigint(20) NOT NULL,
  `createdFromBid_oid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `fk_nicheAuctionBid_auction` (`auction_oid`),
  KEY `fk_nicheAuctionBid_bidder` (`bidder_oid`),
  CONSTRAINT `fk_nicheAuctionBid_auction` FOREIGN KEY (`auction_oid`) REFERENCES `NicheAuction` (`oid`),
  CONSTRAINT `fk_nicheAuctionBid_bidder` FOREIGN KEY (`bidder_oid`) REFERENCES `AreaUserRlm` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `NicheAuctionInvoice`
--

DROP TABLE IF EXISTS `NicheAuctionInvoice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NicheAuctionInvoice` (
  `oid` bigint(20) NOT NULL,
  `auction_oid` bigint(20) NOT NULL,
  `bid_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `bid_oid` (`bid_oid`,`auction_oid`),
  KEY `fk_nicheAuctionInvoice_auction` (`auction_oid`),
  KEY `fk_nicheAuctionInvoice_bid` (`bid_oid`),
  CONSTRAINT `fk_nicheAuctionInvoice_auction` FOREIGN KEY (`auction_oid`) REFERENCES `NicheAuction` (`oid`),
  CONSTRAINT `fk_nicheAuctionInvoice_bid` FOREIGN KEY (`bid_oid`) REFERENCES `NicheAuctionBid` (`oid`),
  CONSTRAINT `fk_nicheAuctionInvoice_invoice` FOREIGN KEY (`oid`) REFERENCES `Invoice` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `NicheAuctionSecurityDeposit`
--

DROP TABLE IF EXISTS `NicheAuctionSecurityDeposit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NicheAuctionSecurityDeposit` (
  `oid` bigint(20) NOT NULL,
  `auction_oid` bigint(20) NOT NULL,
  `user_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `uidx_nicheAuctionSecurityDeposit_user_auction` (`user_oid`,`auction_oid`),
  KEY `fk_nicheAuctionSecurityDeposit_auction` (`auction_oid`),
  CONSTRAINT `fk_nicheAuctionSecurityDeposit_auction` FOREIGN KEY (`auction_oid`) REFERENCES `NicheAuction` (`oid`),
  CONSTRAINT `fk_nicheAuctionSecurityDeposit_invoice` FOREIGN KEY (`oid`) REFERENCES `Invoice` (`oid`),
  CONSTRAINT `fk_nicheAuctionSecurityDeposit_user` FOREIGN KEY (`user_oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `NicheContentReward`
--

DROP TABLE IF EXISTS `NicheContentReward`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NicheContentReward` (
  `oid` bigint(20) NOT NULL AUTO_INCREMENT,
  `reward` bigint(20) DEFAULT NULL,
  `contentReward_oid` bigint(20) NOT NULL,
  `nicheReward_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `nicheContentReward_nicheReward_contentReward_uidx` (`nicheReward_oid`,`contentReward_oid`),
  KEY `fk_nicheContentReward_contentReward` (`contentReward_oid`),
  CONSTRAINT `fk_nicheContentReward_contentReward` FOREIGN KEY (`contentReward_oid`) REFERENCES `ContentReward` (`oid`),
  CONSTRAINT `fk_nicheContentReward_nicheReward` FOREIGN KEY (`nicheReward_oid`) REFERENCES `NicheReward` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `NicheModeratorElection`
--

DROP TABLE IF EXISTS `NicheModeratorElection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NicheModeratorElection` (
  `oid` bigint(20) NOT NULL,
  `niche_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  KEY `fk_nicheModeratorElection_niche` (`niche_oid`),
  CONSTRAINT `fk_nicheModeratorElection_election` FOREIGN KEY (`oid`) REFERENCES `Election` (`oid`),
  CONSTRAINT `fk_nicheModeratorElection_niche` FOREIGN KEY (`niche_oid`) REFERENCES `Niche` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `NicheModeratorReward`
--

DROP TABLE IF EXISTS `NicheModeratorReward`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NicheModeratorReward` (
  `oid` bigint(20) NOT NULL AUTO_INCREMENT,
  `nicheReward_oid` bigint(20) NOT NULL,
  `transaction_oid` bigint(20) DEFAULT NULL,
  `user_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `nicheModeratorReward_user_nicheReward_uidx` (`user_oid`,`nicheReward_oid`),
  KEY `fk_nicheModeratorReward_nicheReward` (`nicheReward_oid`),
  KEY `fk_nicheModeratorReward_transaction` (`transaction_oid`),
  CONSTRAINT `fk_nicheModeratorReward_nicheReward` FOREIGN KEY (`nicheReward_oid`) REFERENCES `NicheReward` (`oid`),
  CONSTRAINT `fk_nicheModeratorReward_transaction` FOREIGN KEY (`transaction_oid`) REFERENCES `WalletTransaction` (`oid`),
  CONSTRAINT `fk_nicheModeratorReward_user` FOREIGN KEY (`user_oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `NicheOfInterest`
--

DROP TABLE IF EXISTS `NicheOfInterest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NicheOfInterest` (
  `niche_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`niche_oid`),
  CONSTRAINT `fk_nicheOfInterest_niche` FOREIGN KEY (`niche_oid`) REFERENCES `Niche` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `NicheOwnerReward`
--

DROP TABLE IF EXISTS `NicheOwnerReward`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NicheOwnerReward` (
  `oid` bigint(20) NOT NULL AUTO_INCREMENT,
  `nicheReward_oid` bigint(20) NOT NULL,
  `transaction_oid` bigint(20) DEFAULT NULL,
  `user_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `nicheOwnerReward_nicheReward_uidx` (`nicheReward_oid`),
  KEY `fk_nicheOwnerReward_transaction` (`transaction_oid`),
  KEY `fk_nicheOwnerReward_user` (`user_oid`),
  CONSTRAINT `fk_nicheOwnerReward_nicheReward` FOREIGN KEY (`nicheReward_oid`) REFERENCES `NicheReward` (`oid`),
  CONSTRAINT `fk_nicheOwnerReward_transaction` FOREIGN KEY (`transaction_oid`) REFERENCES `WalletTransaction` (`oid`),
  CONSTRAINT `fk_nicheOwnerReward_user` FOREIGN KEY (`user_oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `NicheReward`
--

DROP TABLE IF EXISTS `NicheReward`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NicheReward` (
  `oid` bigint(20) NOT NULL AUTO_INCREMENT,
  `niche_oid` bigint(20) NOT NULL,
  `period_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `nicheReward_niche_period_uidx` (`niche_oid`,`period_oid`),
  KEY `fk_nicheReward_period` (`period_oid`),
  CONSTRAINT `fk_nicheReward_niche` FOREIGN KEY (`niche_oid`) REFERENCES `Niche` (`oid`),
  CONSTRAINT `fk_nicheReward_period` FOREIGN KEY (`period_oid`) REFERENCES `RewardPeriod` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `NicheUserAssociation`
--

DROP TABLE IF EXISTS `NicheUserAssociation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NicheUserAssociation` (
  `oid` bigint(20) NOT NULL,
  `associationDatetime` datetime NOT NULL,
  `associationSlot` int(11) NOT NULL,
  `type` int(11) NOT NULL,
  `areaUserRlm_oid` bigint(20) NOT NULL,
  `niche_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `areaUserRlm_oid` (`areaUserRlm_oid`,`associationSlot`),
  UNIQUE KEY `niche_oid` (`niche_oid`,`areaUserRlm_oid`),
  KEY `fk_nicheUserAssociation_niche` (`niche_oid`),
  KEY `fk_nicheUserAssociation_areaUserRlm` (`areaUserRlm_oid`),
  CONSTRAINT `fk_nicheUserAssociation_areaUserRlm` FOREIGN KEY (`areaUserRlm_oid`) REFERENCES `AreaUserRlm` (`oid`),
  CONSTRAINT `fk_nicheUserAssociation_niche` FOREIGN KEY (`niche_oid`) REFERENCES `Niche` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `NrvePayment`
--

DROP TABLE IF EXISTS `NrvePayment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NrvePayment` (
  `oid` bigint(20) NOT NULL,
  `nrveAmount` bigint(20) NOT NULL,
  `transactionDate` datetime(6) DEFAULT NULL,
  `transactionId` varchar(64) DEFAULT NULL,
  `foundByExternalApi` bit(1) NOT NULL,
  `fromNeoAddress` varchar(34) NOT NULL,
  `paymentStatus` bit(1) DEFAULT NULL,
  `paymentWalletTransaction_oid` bigint(20) DEFAULT NULL,
  `refundWalletTransaction_oid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `uidx_nrvePayment_fromNeoAddress_nrveAmount_paymentStatus` (`fromNeoAddress`,`nrveAmount`,`paymentStatus`),
  UNIQUE KEY `uk_nrvePayment_transactionId` (`transactionId`),
  KEY `fk_nrvePayment_paymentWalletTransaction` (`paymentWalletTransaction_oid`),
  KEY `fk_nrvePayment_refundWalletTransaction` (`refundWalletTransaction_oid`),
  CONSTRAINT `fk_nrvePayment_invoice` FOREIGN KEY (`oid`) REFERENCES `Invoice` (`oid`),
  CONSTRAINT `fk_nrvePayment_paymentWalletTransaction` FOREIGN KEY (`paymentWalletTransaction_oid`) REFERENCES `WalletTransaction` (`oid`),
  CONSTRAINT `fk_nrvePayment_refundWalletTransaction` FOREIGN KEY (`refundWalletTransaction_oid`) REFERENCES `WalletTransaction` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PatchRunnerLock`
--

DROP TABLE IF EXISTS `PatchRunnerLock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PatchRunnerLock` (
  `oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PersonalJournal`
--

DROP TABLE IF EXISTS `PersonalJournal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PersonalJournal` (
  `oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  CONSTRAINT `fk_personalJournal_user` FOREIGN KEY (`oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Portfolio`
--

DROP TABLE IF EXISTS `Portfolio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Portfolio` (
  `oid` bigint(20) NOT NULL,
  `areaRlm_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `areaRlm_oid` (`areaRlm_oid`),
  KEY `fk_portfolio_areaRlm` (`areaRlm_oid`),
  CONSTRAINT `fk_portfolio_areaRlm` FOREIGN KEY (`areaRlm_oid`) REFERENCES `AreaRlm` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Property`
--

DROP TABLE IF EXISTS `Property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Property` (
  `oid` bigint(20) NOT NULL,
  `propertyType` varchar(255) NOT NULL,
  `value` longtext NOT NULL,
  `propertySet_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  KEY `FKC8A841F5A2033515` (`propertySet_oid`),
  CONSTRAINT `FKC8A841F5A2033515` FOREIGN KEY (`propertySet_oid`) REFERENCES `PropertySet` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PropertySet`
--

DROP TABLE IF EXISTS `PropertySet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PropertySet` (
  `oid` bigint(20) NOT NULL,
  `propertySetType` varchar(40) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `propertySetType` (`propertySetType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ProratedMonthRevenue`
--

DROP TABLE IF EXISTS `ProratedMonthRevenue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ProratedMonthRevenue` (
  `oid` bigint(20) NOT NULL,
  `captures` int(11) NOT NULL,
  `month` int(11) NOT NULL,
  `totalNrve` bigint(20) NOT NULL,
  `type` int(11) NOT NULL,
  `wallet_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `proratedMonthRevenue_type_month_uidx` (`type`,`month`),
  KEY `fk_proratedMonthRevenue_wallet` (`wallet_oid`),
  CONSTRAINT `fk_proratedMonthRevenue_wallet` FOREIGN KEY (`wallet_oid`) REFERENCES `Wallet` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Publication`
--

DROP TABLE IF EXISTS `Publication`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Publication` (
  `oid` bigint(20) NOT NULL,
  `creationDatetime` bigint(20) NOT NULL,
  `description` varchar(256) DEFAULT NULL,
  `endDatetime` bigint(20) NOT NULL,
  `name` varchar(60) NOT NULL,
  `plan` int(11) NOT NULL,
  `prettyUrlString` varchar(255) NOT NULL,
  `status` int(11) NOT NULL,
  `logo_oid` bigint(20) DEFAULT NULL,
  `owner_oid` bigint(20) DEFAULT NULL,
  `settingsSet_oid` bigint(20) NOT NULL,
  `headerImage_oid` bigint(20) DEFAULT NULL,
  `contentRewardRecipient` int(11) DEFAULT NULL,
  `contentRewardWriterShare` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `publication_prettyUrlString_uidx` (`prettyUrlString`),
  KEY `fk_publication_logo` (`logo_oid`),
  KEY `fk_publication_owner` (`owner_oid`),
  KEY `fk_publication_settingsSet` (`settingsSet_oid`),
  KEY `fk_publication_headerImage` (`headerImage_oid`),
  CONSTRAINT `fk_publication_headerImage` FOREIGN KEY (`headerImage_oid`) REFERENCES `FileOnDisk` (`oid`),
  CONSTRAINT `fk_publication_logo` FOREIGN KEY (`logo_oid`) REFERENCES `FileOnDisk` (`oid`),
  CONSTRAINT `fk_publication_owner` FOREIGN KEY (`owner_oid`) REFERENCES `User` (`oid`),
  CONSTRAINT `fk_publication_settingsSet` FOREIGN KEY (`settingsSet_oid`) REFERENCES `AreaPropertySet` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PublicationInvoice`
--

DROP TABLE IF EXISTS `PublicationInvoice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PublicationInvoice` (
  `oid` bigint(20) NOT NULL,
  `plan` int(11) NOT NULL,
  `publication_oid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `fk_publicationInvoice_publication` (`publication_oid`),
  CONSTRAINT `fk_publicationInvoice_invoice` FOREIGN KEY (`oid`) REFERENCES `Invoice` (`oid`),
  CONSTRAINT `fk_publicationInvoice_publication` FOREIGN KEY (`publication_oid`) REFERENCES `Publication` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PublicationReward`
--

DROP TABLE IF EXISTS `PublicationReward`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PublicationReward` (
  `oid` bigint(20) NOT NULL AUTO_INCREMENT,
  `contentRewardRecipient` int(11) DEFAULT NULL,
  `contentRewardWriterShare` int(11) NOT NULL,
  `period_oid` bigint(20) NOT NULL,
  `publicationOid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `publicationReward_publicationOid_period_uidx` (`publicationOid`,`period_oid`),
  KEY `fk_publicationReward_period` (`period_oid`),
  CONSTRAINT `fk_publicationReward_period` FOREIGN KEY (`period_oid`) REFERENCES `RewardPeriod` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PublicationWaitListEntry`
--

DROP TABLE IF EXISTS `PublicationWaitListEntry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PublicationWaitListEntry` (
  `oid` bigint(20) NOT NULL,
  `emailAddress` varchar(255) NOT NULL,
  `used` bit(1) NOT NULL,
  `claimer_oid` bigint(20) DEFAULT NULL,
  `publication_oid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `publicationWaitListEntry_emailAddress_uidx` (`emailAddress`),
  UNIQUE KEY `publicationWaitListEntry_publication_uidx` (`publication_oid`),
  KEY `fk_publicationWaitListEntry_claimer` (`claimer_oid`),
  CONSTRAINT `fk_publicationWaitListEntry_claimer` FOREIGN KEY (`claimer_oid`) REFERENCES `User` (`oid`),
  CONSTRAINT `fk_publicationWaitListEntry_publication` FOREIGN KEY (`publication_oid`) REFERENCES `Publication` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_BLOB_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_BLOB_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_BLOB_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `BLOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_BLOB_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_CALENDARS`
--

DROP TABLE IF EXISTS `QRTZ_CALENDARS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_CALENDARS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `CALENDAR_NAME` varchar(200) NOT NULL,
  `CALENDAR` blob NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`CALENDAR_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_CRON_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_CRON_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_CRON_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `CRON_EXPRESSION` varchar(120) NOT NULL,
  `TIME_ZONE_ID` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_CRON_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_FIRED_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_FIRED_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_FIRED_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `ENTRY_ID` varchar(95) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `INSTANCE_NAME` varchar(200) NOT NULL,
  `FIRED_TIME` bigint(13) NOT NULL,
  `SCHED_TIME` bigint(13) NOT NULL,
  `PRIORITY` int(11) NOT NULL,
  `STATE` varchar(16) NOT NULL,
  `JOB_NAME` varchar(200) DEFAULT NULL,
  `JOB_GROUP` varchar(200) DEFAULT NULL,
  `IS_NONCONCURRENT` varchar(1) DEFAULT NULL,
  `REQUESTS_RECOVERY` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`ENTRY_ID`),
  KEY `IDX_QRTZ_FT_TRIG_INST_NAME` (`SCHED_NAME`,`INSTANCE_NAME`),
  KEY `IDX_QRTZ_FT_INST_JOB_REQ_RCVRY` (`SCHED_NAME`,`INSTANCE_NAME`,`REQUESTS_RECOVERY`),
  KEY `IDX_QRTZ_FT_J_G` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_FT_JG` (`SCHED_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_FT_T_G` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `IDX_QRTZ_FT_TG` (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_JOB_DETAILS`
--

DROP TABLE IF EXISTS `QRTZ_JOB_DETAILS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_JOB_DETAILS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `JOB_NAME` varchar(200) NOT NULL,
  `JOB_GROUP` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(250) DEFAULT NULL,
  `JOB_CLASS_NAME` varchar(250) NOT NULL,
  `IS_DURABLE` varchar(1) NOT NULL,
  `IS_NONCONCURRENT` varchar(1) NOT NULL,
  `IS_UPDATE_DATA` varchar(1) NOT NULL,
  `REQUESTS_RECOVERY` varchar(1) NOT NULL,
  `JOB_DATA` mediumblob,
  PRIMARY KEY (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_J_REQ_RECOVERY` (`SCHED_NAME`,`REQUESTS_RECOVERY`),
  KEY `IDX_QRTZ_J_GRP` (`SCHED_NAME`,`JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_LOCKS`
--

DROP TABLE IF EXISTS `QRTZ_LOCKS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_LOCKS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `LOCK_NAME` varchar(40) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`LOCK_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_PAUSED_TRIGGER_GRPS`
--

DROP TABLE IF EXISTS `QRTZ_PAUSED_TRIGGER_GRPS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_PAUSED_TRIGGER_GRPS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_SCHEDULER_STATE`
--

DROP TABLE IF EXISTS `QRTZ_SCHEDULER_STATE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_SCHEDULER_STATE` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `INSTANCE_NAME` varchar(200) NOT NULL,
  `LAST_CHECKIN_TIME` bigint(13) NOT NULL,
  `CHECKIN_INTERVAL` bigint(13) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`INSTANCE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_SIMPLE_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_SIMPLE_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_SIMPLE_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `REPEAT_COUNT` bigint(7) NOT NULL,
  `REPEAT_INTERVAL` bigint(12) NOT NULL,
  `TIMES_TRIGGERED` bigint(10) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_SIMPLE_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_SIMPROP_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_SIMPROP_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_SIMPROP_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `STR_PROP_1` varchar(512) DEFAULT NULL,
  `STR_PROP_2` varchar(512) DEFAULT NULL,
  `STR_PROP_3` varchar(512) DEFAULT NULL,
  `INT_PROP_1` int(11) DEFAULT NULL,
  `INT_PROP_2` int(11) DEFAULT NULL,
  `LONG_PROP_1` bigint(20) DEFAULT NULL,
  `LONG_PROP_2` bigint(20) DEFAULT NULL,
  `DEC_PROP_1` decimal(13,4) DEFAULT NULL,
  `DEC_PROP_2` decimal(13,4) DEFAULT NULL,
  `BOOL_PROP_1` varchar(1) DEFAULT NULL,
  `BOOL_PROP_2` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_SIMPROP_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `JOB_NAME` varchar(200) NOT NULL,
  `JOB_GROUP` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(250) DEFAULT NULL,
  `NEXT_FIRE_TIME` bigint(13) DEFAULT NULL,
  `PREV_FIRE_TIME` bigint(13) DEFAULT NULL,
  `PRIORITY` int(11) DEFAULT NULL,
  `TRIGGER_STATE` varchar(16) NOT NULL,
  `TRIGGER_TYPE` varchar(8) NOT NULL,
  `START_TIME` bigint(13) NOT NULL,
  `END_TIME` bigint(13) DEFAULT NULL,
  `CALENDAR_NAME` varchar(200) DEFAULT NULL,
  `MISFIRE_INSTR` smallint(2) DEFAULT NULL,
  `JOB_DATA` mediumblob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `IDX_QRTZ_T_J` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_T_JG` (`SCHED_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_T_C` (`SCHED_NAME`,`CALENDAR_NAME`),
  KEY `IDX_QRTZ_T_G` (`SCHED_NAME`,`TRIGGER_GROUP`),
  KEY `IDX_QRTZ_T_STATE` (`SCHED_NAME`,`TRIGGER_STATE`),
  KEY `IDX_QRTZ_T_N_STATE` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
  KEY `IDX_QRTZ_T_N_G_STATE` (`SCHED_NAME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
  KEY `IDX_QRTZ_T_NEXT_FIRE_TIME` (`SCHED_NAME`,`NEXT_FIRE_TIME`),
  KEY `IDX_QRTZ_T_NFT_ST` (`SCHED_NAME`,`TRIGGER_STATE`,`NEXT_FIRE_TIME`),
  KEY `IDX_QRTZ_T_NFT_MISFIRE` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`),
  KEY `IDX_QRTZ_T_NFT_ST_MISFIRE` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`,`TRIGGER_STATE`),
  KEY `IDX_QRTZ_T_NFT_ST_MISFIRE_GRP` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
  CONSTRAINT `QRTZ_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) REFERENCES `QRTZ_JOB_DETAILS` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Referendum`
--

DROP TABLE IF EXISTS `Referendum`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Referendum` (
  `oid` bigint(20) NOT NULL,
  `compositionPartitionOid` bigint(20) NOT NULL,
  `endDatetime` datetime NOT NULL,
  `open` bit(1) NOT NULL,
  `properties` longtext,
  `startDatetime` datetime NOT NULL,
  `type` int(11) NOT NULL,
  `votePointsAgainst` int(11) NOT NULL,
  `votePointsFor` int(11) NOT NULL,
  `niche_oid` bigint(20) DEFAULT NULL,
  `tribunalIssue_oid` bigint(20) DEFAULT NULL,
  `publication_oid` bigint(20) DEFAULT NULL,
  `deletedChannel_oid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `fk_referendum_niche` (`niche_oid`),
  KEY `fk_referendum_tribunalIssue` (`tribunalIssue_oid`),
  KEY `fk_referendum_publication` (`publication_oid`),
  KEY `fk_referendum_deletedChannel` (`deletedChannel_oid`),
  CONSTRAINT `fk_referendum_deletedChannel` FOREIGN KEY (`deletedChannel_oid`) REFERENCES `DeletedChannel` (`oid`),
  CONSTRAINT `fk_referendum_niche` FOREIGN KEY (`niche_oid`) REFERENCES `Niche` (`oid`),
  CONSTRAINT `fk_referendum_publication` FOREIGN KEY (`publication_oid`) REFERENCES `Publication` (`oid`),
  CONSTRAINT `fk_referendum_tribunalIssue` FOREIGN KEY (`tribunalIssue_oid`) REFERENCES `TribunalIssue` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReferendumVote`
--

DROP TABLE IF EXISTS `ReferendumVote`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ReferendumVote` (
  `oid` bigint(20) NOT NULL,
  `commentReplyOid` bigint(20) DEFAULT NULL,
  `reason` int(11) DEFAULT NULL,
  `voteDatetime` datetime NOT NULL,
  `votedFor` bit(1) DEFAULT NULL,
  `referendum_oid` bigint(20) NOT NULL,
  `voter_oid` bigint(20) NOT NULL,
  `votePoints` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `voter_oid` (`voter_oid`,`referendum_oid`),
  KEY `fk_referendumVote_referendum` (`referendum_oid`),
  KEY `fk_referendumVote_voter` (`voter_oid`),
  CONSTRAINT `fk_referendumVote_referendum` FOREIGN KEY (`referendum_oid`) REFERENCES `Referendum` (`oid`),
  CONSTRAINT `fk_referendumVote_voter` FOREIGN KEY (`voter_oid`) REFERENCES `AreaUserRlm` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `RewardPeriod`
--

DROP TABLE IF EXISTS `RewardPeriod`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RewardPeriod` (
  `oid` bigint(20) NOT NULL,
  `completedDatetime` bigint(20) DEFAULT NULL,
  `completedSteps` bigint(20) NOT NULL,
  `mintMonth` tinyint(4) DEFAULT NULL,
  `mintYear` int(11) DEFAULT NULL,
  `period` int(11) NOT NULL,
  `totalRewards` bigint(20) NOT NULL,
  `totalRewardsDisbursed` bigint(20) NOT NULL,
  `wallet_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `rewardPeriod_period_uidx` (`period`),
  KEY `fk_rewardPeriod_wallet` (`wallet_oid`),
  CONSTRAINT `fk_rewardPeriod_wallet` FOREIGN KEY (`wallet_oid`) REFERENCES `Wallet` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `RoleContentPageView`
--

DROP TABLE IF EXISTS `RoleContentPageView`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RoleContentPageView` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `contentOid` bigint(20) NOT NULL,
  `roleId` varchar(128) NOT NULL,
  `viewDatetime` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `RoleContentReward`
--

DROP TABLE IF EXISTS `RoleContentReward`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RoleContentReward` (
  `oid` bigint(20) NOT NULL AUTO_INCREMENT,
  `role` int(11) NOT NULL,
  `contentReward_oid` bigint(20) NOT NULL,
  `transaction_oid` bigint(20) DEFAULT NULL,
  `user_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `roleContentReward_contentReward_role_user_uidx` (`contentReward_oid`,`role`,`user_oid`),
  KEY `fk_roleContentReward_transaction` (`transaction_oid`),
  KEY `fk_roleContentReward_user` (`user_oid`),
  CONSTRAINT `fk_roleContentReward_contentReward` FOREIGN KEY (`contentReward_oid`) REFERENCES `ContentReward` (`oid`),
  CONSTRAINT `fk_roleContentReward_transaction` FOREIGN KEY (`transaction_oid`) REFERENCES `WalletTransaction` (`oid`),
  CONSTRAINT `fk_roleContentReward_user` FOREIGN KEY (`user_oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SandboxedAreaUser`
--

DROP TABLE IF EXISTS `SandboxedAreaUser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SandboxedAreaUser` (
  `oid` bigint(20) NOT NULL,
  `disableNewDialogs` bit(1) NOT NULL,
  `displayName` varchar(40) NOT NULL,
  `areaRlm_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  KEY `fk_sandboxedAreaUser_areaRlm` (`areaRlm_oid`),
  CONSTRAINT `fk_sandboxedAreaUser_areaRlm` FOREIGN KEY (`areaRlm_oid`) REFERENCES `AreaRlm` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `TrendingContent`
--

DROP TABLE IF EXISTS `TrendingContent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TrendingContent` (
  `oid` bigint(20) NOT NULL AUTO_INCREMENT,
  `buildTime` bigint(20) NOT NULL,
  `score` bigint(20) NOT NULL,
  `content_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `uidx_trendingContent_content_buildTime` (`content_oid`,`buildTime`),
  CONSTRAINT `fk_trendingContent_content` FOREIGN KEY (`content_oid`) REFERENCES `Content` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `TribunalIssue`
--

DROP TABLE IF EXISTS `TribunalIssue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TribunalIssue` (
  `oid` bigint(20) NOT NULL,
  `creationDatetime` datetime NOT NULL,
  `status` bit(1) DEFAULT NULL,
  `type` int(11) NOT NULL,
  `lastReport_oid` bigint(20) DEFAULT NULL,
  `lastReport2_oid` bigint(20) DEFAULT NULL,
  `lastReport3_oid` bigint(20) DEFAULT NULL,
  `channel_oid` bigint(20) DEFAULT NULL,
  `referendum_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `tribunalIssue_channel_type_status_uidx` (`channel_oid`,`type`,`status`),
  KEY `fk_tribunalIssue_niche` (`channel_oid`),
  KEY `fk_tribunalIssue_lastReport3` (`lastReport3_oid`),
  KEY `fk_tribunalIssue_lastReport2` (`lastReport2_oid`),
  KEY `fk_tribunalIssue_referendumForTribunal` (`referendum_oid`),
  KEY `fk_tribunalIssue_lastReport` (`lastReport_oid`),
  CONSTRAINT `fk_tribunalIssue_channel` FOREIGN KEY (`channel_oid`) REFERENCES `Channel` (`oid`),
  CONSTRAINT `fk_tribunalIssue_lastReport` FOREIGN KEY (`lastReport_oid`) REFERENCES `TribunalIssueReport` (`oid`),
  CONSTRAINT `fk_tribunalIssue_lastReport2` FOREIGN KEY (`lastReport2_oid`) REFERENCES `TribunalIssueReport` (`oid`),
  CONSTRAINT `fk_tribunalIssue_lastReport3` FOREIGN KEY (`lastReport3_oid`) REFERENCES `TribunalIssueReport` (`oid`),
  CONSTRAINT `fk_tribunalIssue_referendum` FOREIGN KEY (`referendum_oid`) REFERENCES `Referendum` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `TribunalIssueReport`
--

DROP TABLE IF EXISTS `TribunalIssueReport`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TribunalIssueReport` (
  `oid` bigint(20) NOT NULL,
  `comments` longtext COLLATE utf8mb4_unicode_ci,
  `creationDatetime` datetime NOT NULL,
  `reporter_oid` bigint(20) NOT NULL,
  `tribunalIssue_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  KEY `fk_tribunalIssueReport_reporter` (`reporter_oid`),
  KEY `fk_tribunalIssueReport_tribunalIssue` (`tribunalIssue_oid`),
  CONSTRAINT `fk_tribunalIssueReport_reporter` FOREIGN KEY (`reporter_oid`) REFERENCES `AreaUserRlm` (`oid`),
  CONSTRAINT `fk_tribunalIssueReport_tribunalIssue` FOREIGN KEY (`tribunalIssue_oid`) REFERENCES `TribunalIssue` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `User`
--

DROP TABLE IF EXISTS `User`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `User` (
  `oid` bigint(20) NOT NULL,
  `authZone` bigint(20) NOT NULL,
  `confirmedWaitListInviteCount` int(11) NOT NULL,
  `displayName` varchar(40) NOT NULL,
  `formatPreferences_languageLocale` int(11) DEFAULT NULL,
  `formatPreferences_locale` varchar(8) NOT NULL,
  `formatPreferences_timeZone` varchar(255) NOT NULL,
  `lastConfirmedWaitListInviteDatetime` bigint(20) DEFAULT NULL,
  `preferences_lastDeactivationDatetime` datetime DEFAULT NULL,
  `preferences_suspendAllEmails` bit(1) NOT NULL,
  `twoFactorAuthenticationSecretKey` tinytext,
  `userFields_agreedToTosDatetime` datetime DEFAULT NULL,
  `userFields_registrationDate` datetime NOT NULL,
  `userStatus` int(11) NOT NULL,
  `username` varchar(20) DEFAULT NULL,
  `avatar_oid` bigint(20) DEFAULT NULL,
  `lastPaymentChargebackDatetime` bigint(20) DEFAULT NULL,
  `preferences_contentQualityFilter` tinyint(4) NOT NULL,
  `preferences_displayAgeRestrictedContent` bit(1) NOT NULL,
  `preferences_hideMyFollowers` bit(1) NOT NULL,
  `preferences_hideMyFollows` bit(1) NOT NULL,
  `wallet_oid` bigint(20) NOT NULL,
  `firstAvatarUploadDatetime` bigint(20) DEFAULT NULL,
  `usedTwoFactorAuthenticationBackupCodes` bigint(20) NOT NULL,
  `lastWalletAddressChangeDatetime` bigint(20) DEFAULT NULL,
  `userFields_emailAddress_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `user_wallet_uidx` (`wallet_oid`),
  UNIQUE KEY `user_emailAddress_uidx` (`authZone`,`userFields_emailAddress_oid`),
  UNIQUE KEY `authZone` (`authZone`,`username`),
  KEY `FK285FEB101B5B4E` (`avatar_oid`),
  KEY `fk_user_emailAddress` (`userFields_emailAddress_oid`),
  CONSTRAINT `FK285FEB101B5B4E` FOREIGN KEY (`avatar_oid`) REFERENCES `FileOnDisk` (`oid`),
  CONSTRAINT `fk_user_emailAddress` FOREIGN KEY (`userFields_emailAddress_oid`) REFERENCES `EmailAddress` (`oid`),
  CONSTRAINT `fk_user_wallet` FOREIGN KEY (`wallet_oid`) REFERENCES `Wallet` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserActivityReward`
--

DROP TABLE IF EXISTS `UserActivityReward`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserActivityReward` (
  `oid` bigint(20) NOT NULL AUTO_INCREMENT,
  `bonus` int(11) NOT NULL,
  `points` bigint(20) NOT NULL,
  `period_oid` bigint(20) NOT NULL,
  `transaction_oid` bigint(20) DEFAULT NULL,
  `user_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `userActivityReward_user_period_uidx` (`user_oid`,`period_oid`),
  KEY `fk_userActivityReward_period` (`period_oid`),
  KEY `fk_userActivityReward_transaction` (`transaction_oid`),
  CONSTRAINT `fk_userActivityReward_period` FOREIGN KEY (`period_oid`) REFERENCES `RewardPeriod` (`oid`),
  CONSTRAINT `fk_userActivityReward_transaction` FOREIGN KEY (`transaction_oid`) REFERENCES `WalletTransaction` (`oid`),
  CONSTRAINT `fk_userActivityReward_user` FOREIGN KEY (`user_oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserActivityRewardEvent`
--

DROP TABLE IF EXISTS `UserActivityRewardEvent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserActivityRewardEvent` (
  `oid` bigint(20) NOT NULL,
  `eventDatetime` bigint(20) NOT NULL,
  `type` int(11) NOT NULL,
  `user_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  KEY `fk_userRewardActivityEvent_user` (`user_oid`),
  CONSTRAINT `fk_userRewardActivityEvent_user` FOREIGN KEY (`user_oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserAuth`
--

DROP TABLE IF EXISTS `UserAuth`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserAuth` (
  `oid` bigint(20) NOT NULL,
  `authProvider` int(11) NOT NULL,
  `authZone` bigint(20) NOT NULL,
  `identifier` varchar(255) NOT NULL,
  `user_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `authZone` (`authZone`,`authProvider`,`identifier`),
  UNIQUE KEY `user_oid` (`user_oid`,`authProvider`),
  KEY `fk_userAuth_user` (`user_oid`),
  CONSTRAINT `fk_userAuth_user` FOREIGN KEY (`user_oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserDemographics`
--

DROP TABLE IF EXISTS `UserDemographics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserDemographics` (
  `oid` bigint(20) unsigned NOT NULL,
  `country` char(2) DEFAULT NULL,
  `postalCode` varchar(20) DEFAULT NULL,
  `gender` varchar(6) DEFAULT NULL,
  `dateOfBirth` datetime DEFAULT NULL,
  `region` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserElectorateReward`
--

DROP TABLE IF EXISTS `UserElectorateReward`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserElectorateReward` (
  `oid` bigint(20) NOT NULL AUTO_INCREMENT,
  `points` bigint(20) NOT NULL,
  `period_oid` bigint(20) NOT NULL,
  `transaction_oid` bigint(20) DEFAULT NULL,
  `user_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `userElectorateReward_user_period_uidx` (`user_oid`,`period_oid`),
  KEY `fk_userElectorateReward_period` (`period_oid`),
  KEY `fk_userElectorateReward_transaction` (`transaction_oid`),
  CONSTRAINT `fk_userElectorateReward_period` FOREIGN KEY (`period_oid`) REFERENCES `RewardPeriod` (`oid`),
  CONSTRAINT `fk_userElectorateReward_transaction` FOREIGN KEY (`transaction_oid`) REFERENCES `WalletTransaction` (`oid`),
  CONSTRAINT `fk_userElectorateReward_user` FOREIGN KEY (`user_oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserKyc`
--

DROP TABLE IF EXISTS `UserKyc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserKyc` (
  `oid` bigint(20) NOT NULL,
  `kycStatus` tinyint(4) DEFAULT NULL,
  `userDetailHash` varchar(128) DEFAULT NULL,
  `birthMonth` tinyint(4) DEFAULT NULL,
  `birthYear` smallint(6) DEFAULT NULL,
  `country` char(2) DEFAULT NULL,
  `lastUpdated` datetime(6) DEFAULT NULL,
  `encryptionSalt` tinyblob,
  `submissionCount` int(11) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `userKyc_userDetailHash_uidx` (`userDetailHash`),
  CONSTRAINT `fk_userkyc_user` FOREIGN KEY (`oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserKycEvent`
--

DROP TABLE IF EXISTS `UserKycEvent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserKycEvent` (
  `oid` bigint(20) NOT NULL,
  `created` datetime(6) DEFAULT NULL,
  `type` int(11) NOT NULL,
  `actorDisplayName` varchar(255) NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  `userKyc_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  KEY `fk_userKycEvent_userKyc` (`userKyc_oid`),
  CONSTRAINT `fk_userKycEvent_userKyc` FOREIGN KEY (`userKyc_oid`) REFERENCES `UserKyc` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserMembership`
--

DROP TABLE IF EXISTS `UserMembership`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserMembership` (
  `oid` bigint(20) NOT NULL,
  `userDemographics_oid` bigint(20) DEFAULT NULL,
  `areaOid` bigint(20) NOT NULL,
  `createdDatetime` datetime NOT NULL,
  `lastLoginDatetime` datetime DEFAULT NULL,
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserStats`
--

DROP TABLE IF EXISTS `UserStats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserStats` (
  `oid` bigint(20) NOT NULL,
  `lastLoginDatetime` datetime DEFAULT NULL,
  PRIMARY KEY (`oid`),
  CONSTRAINT `fk_userstats_user` FOREIGN KEY (`oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserTribunalReward`
--

DROP TABLE IF EXISTS `UserTribunalReward`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserTribunalReward` (
  `oid` bigint(20) NOT NULL AUTO_INCREMENT,
  `period_oid` bigint(20) NOT NULL,
  `transaction_oid` bigint(20) NOT NULL,
  `user_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `userTribunalReward_user_period_uidx` (`user_oid`,`period_oid`),
  KEY `fk_userTribunalReward_period` (`period_oid`),
  KEY `fk_userTribunalReward_transaction` (`transaction_oid`),
  CONSTRAINT `fk_userTribunalReward_period` FOREIGN KEY (`period_oid`) REFERENCES `RewardPeriod` (`oid`),
  CONSTRAINT `fk_userTribunalReward_transaction` FOREIGN KEY (`transaction_oid`) REFERENCES `WalletTransaction` (`oid`),
  CONSTRAINT `fk_userTribunalReward_user` FOREIGN KEY (`user_oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Wallet`
--

DROP TABLE IF EXISTS `Wallet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Wallet` (
  `oid` bigint(20) NOT NULL,
  `balance` bigint(20) NOT NULL,
  `type` int(11) NOT NULL,
  `singleton` bit(1) DEFAULT NULL,
  `neoWallet_oid` bigint(20) DEFAULT NULL,
  `allowsSharedNeoWallets` bit(1) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `uidx_wallet_type` (`type`,`singleton`),
  UNIQUE KEY `uidx_wallet_neoWallet` (`neoWallet_oid`,`allowsSharedNeoWallets`),
  CONSTRAINT `fk_wallet_neoWallet` FOREIGN KEY (`neoWallet_oid`) REFERENCES `NeoWallet` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `WalletTransaction`
--

DROP TABLE IF EXISTS `WalletTransaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `WalletTransaction` (
  `oid` bigint(20) NOT NULL,
  `memo` varchar(255) DEFAULT NULL,
  `nrveAmount` bigint(20) NOT NULL,
  `transactionDatetime` bigint(20) NOT NULL,
  `type` int(11) NOT NULL,
  `fromWallet_oid` bigint(20) DEFAULT NULL,
  `toWallet_oid` bigint(20) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `neoTransaction_oid` bigint(20) DEFAULT NULL,
  `usdAmount` decimal(19,2) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `uidx_walletTransaction_neoTransaction` (`neoTransaction_oid`),
  KEY `fk_walletTransaction_fromWallet` (`fromWallet_oid`),
  KEY `fk_walletTransaction_toWallet` (`toWallet_oid`),
  CONSTRAINT `fk_walletTransaction_fromWallet` FOREIGN KEY (`fromWallet_oid`) REFERENCES `Wallet` (`oid`),
  CONSTRAINT `fk_walletTransaction_neoTransaction` FOREIGN KEY (`neoTransaction_oid`) REFERENCES `NeoTransaction` (`oid`),
  CONSTRAINT `fk_walletTransaction_toWallet` FOREIGN KEY (`toWallet_oid`) REFERENCES `Wallet` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `WatchedUser`
--

DROP TABLE IF EXISTS `WatchedUser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `WatchedUser` (
  `oid` bigint(20) NOT NULL,
  `blocked` bit(1) NOT NULL,
  `watchDatetime` datetime NOT NULL,
  `watchedUser_oid` bigint(20) NOT NULL,
  `watcherUser_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `watcherUser_oid` (`watcherUser_oid`,`watchedUser_oid`),
  KEY `fk_watchedUser_watcherUser` (`watcherUser_oid`),
  KEY `fk_watchedUser_watchedUser` (`watchedUser_oid`),
  CONSTRAINT `fk_watchedUser_watchedUser` FOREIGN KEY (`watchedUser_oid`) REFERENCES `User` (`oid`),
  CONSTRAINT `fk_watchedUser_watcherUser` FOREIGN KEY (`watcherUser_oid`) REFERENCES `User` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `highbit`
--

DROP TABLE IF EXISTS `highbit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `highbit` (
  `high` smallint(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mysqloid`
--

DROP TABLE IF EXISTS `mysqloid`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mysqloid` (
  `nextOid` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-01-06  8:01:01
