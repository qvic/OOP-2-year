-- ---
-- Table 'Schedule'
-- 
-- ---

DROP TABLE IF EXISTS `Schedule`;
		
CREATE TABLE `Schedule` (
  `TrainID` INTEGER NULL DEFAULT NULL,
  `LastStation` MEDIUMTEXT NULL DEFAULT NULL,
  `Arrival` TIME NULL DEFAULT NULL,
  PRIMARY KEY (`TrainID`)
);

-- ---
-- Table 'Ads'
-- 
-- ---

DROP TABLE IF EXISTS `Ads`;
		
CREATE TABLE `Ads` (
  `AdID` INTEGER NULL AUTO_INCREMENT DEFAULT NULL,
  `Text` MEDIUMTEXT NULL DEFAULT NULL,
  PRIMARY KEY (`AdID`)
);

-- ---
-- Table 'Violations'
-- 
-- ---

DROP TABLE IF EXISTS `Violations`;
		
CREATE TABLE `Violations` (
  `ViolationID` INTEGER NULL AUTO_INCREMENT DEFAULT NULL,
  `Delta` TIME NULL DEFAULT NULL,
  `TrainID` INTEGER NULL DEFAULT NULL,
  PRIMARY KEY (`ViolationID`)
);

-- ---
-- Foreign Keys 
-- ---

ALTER TABLE `Violations` ADD FOREIGN KEY (TrainID) REFERENCES `Schedule` (`TrainID`);