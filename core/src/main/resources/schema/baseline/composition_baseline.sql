-- MySQL dump 10.13  Distrib 5.7.24, for osx10.14 (x86_64)
--
-- Host: 127.0.0.1    Database: comp2
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
-- Table structure for table `Composition`
--

DROP TABLE IF EXISTS `Composition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Composition` (
  `oid` bigint(20) NOT NULL,
  `areaOid` bigint(20) NOT NULL,
  `body` mediumtext NOT NULL,
  `compositionType` int(11) DEFAULT NULL,
  `editDatetime` datetime DEFAULT NULL,
  `editorUserOid` bigint(20) DEFAULT NULL,
  `guestName` varchar(40) DEFAULT NULL,
  `userOid` bigint(20) DEFAULT NULL,
  `filePointerSet_oid` bigint(20) DEFAULT NULL,
  `mentions_oid` bigint(20) DEFAULT NULL,
  `canonicalUrl` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `composition_area_oid_idx` (`areaOid`),
  KEY `fk_composition_filePointerSet` (`filePointerSet_oid`),
  CONSTRAINT `fk_composition_filePointerSet` FOREIGN KEY (`filePointerSet_oid`) REFERENCES `FilePointerSet` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CompositionMentions`
--

DROP TABLE IF EXISTS `CompositionMentions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CompositionMentions` (
  `oid` bigint(20) NOT NULL,
  `mentionedMemberOids` longtext,
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CompositionStats`
--

DROP TABLE IF EXISTS `CompositionStats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CompositionStats` (
  `oid` bigint(20) NOT NULL,
  `lastUpdateDatetime` datetime DEFAULT NULL,
  `likeFields_likeCount` int(11) NOT NULL,
  `pageViews` int(11) NOT NULL,
  `replyCount` int(11) NOT NULL,
  `watchedContentCount` int(11) NOT NULL,
  `lastReply_oid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `fk_compositionStats_lastReply` (`lastReply_oid`),
  CONSTRAINT `fk_compositionStats_lastReply` FOREIGN KEY (`lastReply_oid`) REFERENCES `Reply` (`oid`),
  CONSTRAINT `fk_compositionstats_composition` FOREIGN KEY (`oid`) REFERENCES `Composition` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `FilePointer`
--

DROP TABLE IF EXISTS `FilePointer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FilePointer` (
  `fileType` varchar(31) NOT NULL,
  `oid` bigint(20) NOT NULL,
  `byteSize` int(11) NOT NULL,
  `filename` varchar(255) NOT NULL,
  `metaData` longblob,
  `mimeType` varchar(80) NOT NULL,
  `fileOnDiskOid` bigint(20) NOT NULL,
  `status` tinyint(4) NOT NULL,
  `threadingOrder` int(11) NOT NULL,
  `filePointerSet_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  KEY `filePointer_fileOnDiskOid_idx` (`fileOnDiskOid`),
  KEY `fk_filePointer_filePointerSet` (`filePointerSet_oid`),
  CONSTRAINT `fk_filePointer_filePointerSet` FOREIGN KEY (`filePointerSet_oid`) REFERENCES `FilePointerSet` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `FilePointerSet`
--

DROP TABLE IF EXISTS `FilePointerSet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FilePointerSet` (
  `oid` bigint(20) NOT NULL,
  `fileCount` int(11) NOT NULL,
  `totalByteSize` int(11) NOT NULL,
  `composition_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `NarrativePostContent`
--

DROP TABLE IF EXISTS `NarrativePostContent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NarrativePostContent` (
  `oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  CONSTRAINT `fk_narrativePostContent_composition` FOREIGN KEY (`oid`) REFERENCES `Composition` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Reply`
--

DROP TABLE IF EXISTS `Reply`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Reply` (
  `oid` bigint(20) NOT NULL,
  `body` mediumtext NOT NULL,
  `editDatetime` datetime DEFAULT NULL,
  `editorUserOid` bigint(20) DEFAULT NULL,
  `guestName` varchar(40) DEFAULT NULL,
  `liveDatetime` datetime NOT NULL,
  `moderationStatus` enum('APPROVED','PENDING_APPROVAL','MODERATED') NOT NULL,
  `threadingOrder` int(11) NOT NULL AUTO_INCREMENT,
  `userOid` bigint(20) DEFAULT NULL,
  `composition_oid` bigint(20) NOT NULL,
  `filePointerSet_oid` bigint(20) DEFAULT NULL,
  `mentions_oid` bigint(20) DEFAULT NULL,
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
  UNIQUE KEY `threadingOrder` (`threadingOrder`),
  UNIQUE KEY `filePointerSet_oid` (`filePointerSet_oid`),
  KEY `reply_composition_liveDatetime_idx` (`composition_oid`,`liveDatetime`),
  KEY `reply_composition_threadingOrder_idx` (`composition_oid`,`threadingOrder`),
  KEY `fk_reply_composition` (`composition_oid`),
  KEY `fk_reply_filePointerSet` (`filePointerSet_oid`),
  CONSTRAINT `fk_reply_composition` FOREIGN KEY (`composition_oid`) REFERENCES `Composition` (`oid`),
  CONSTRAINT `fk_reply_filePointerSet` FOREIGN KEY (`filePointerSet_oid`) REFERENCES `FilePointerSet` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReplyMentions`
--

DROP TABLE IF EXISTS `ReplyMentions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ReplyMentions` (
  `oid` bigint(20) NOT NULL,
  `mentionedMemberOids` longtext,
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReplyStats`
--

DROP TABLE IF EXISTS `ReplyStats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ReplyStats` (
  `oid` bigint(20) NOT NULL,
  `likeCount` int(11) NOT NULL,
  `reportCount` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  CONSTRAINT `fk_replystats_reply` FOREIGN KEY (`oid`) REFERENCES `Reply` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserAgeRatedComposition`
--

DROP TABLE IF EXISTS `UserAgeRatedComposition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserAgeRatedComposition` (
  `oid` bigint(20) NOT NULL,
  `pointValue` int(11) NOT NULL,
  `ratingDatetime` datetime(6) NOT NULL,
  `userOid` bigint(20) NOT NULL,
  `ageRating` int(11) NOT NULL,
  `composition_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `userAgeRatedComposition_composition_userOid_uidx` (`composition_oid`,`userOid`),
  CONSTRAINT `fk_userAgeRatedComposition_composition` FOREIGN KEY (`composition_oid`) REFERENCES `Composition` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserQualityRatedComposition`
--

DROP TABLE IF EXISTS `UserQualityRatedComposition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserQualityRatedComposition` (
  `oid` bigint(20) NOT NULL,
  `pointValue` int(11) NOT NULL,
  `ratingDatetime` datetime(6) NOT NULL,
  `userOid` bigint(20) NOT NULL,
  `qualityRating` int(11) NOT NULL,
  `composition_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `userQualityRatedComposition_composition_userOid_uidx` (`composition_oid`,`userOid`),
  CONSTRAINT `fk_userQualityRatedComposition_composition` FOREIGN KEY (`composition_oid`) REFERENCES `Composition` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserQualityRatedReply`
--

DROP TABLE IF EXISTS `UserQualityRatedReply`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserQualityRatedReply` (
  `oid` bigint(20) NOT NULL,
  `pointValue` int(11) NOT NULL,
  `ratingDatetime` datetime(6) NOT NULL,
  `userOid` bigint(20) NOT NULL,
  `qualityRating` int(11) NOT NULL,
  `reply_oid` bigint(20) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `userQualityRatedReply_reply_userOid_uidx` (`reply_oid`,`userOid`),
  CONSTRAINT `fk_userQualityRatedReply_reply` FOREIGN KEY (`reply_oid`) REFERENCES `Reply` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-01-06  8:01:02
