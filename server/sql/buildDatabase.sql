DROP DATABASE IF EXISTS `ultraqc_db`;

CREATE SCHEMA `ultraqc_db` DEFAULT CHARACTER SET utf8mb4 ;

CREATE TABLE `ultraqc_db`.`samples` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `SampleName` varchar(45) DEFAULT NULL,
  `Species` varchar(45) DEFAULT NULL,
  `SequencingTechnology` varchar(45) DEFAULT NULL,
  `Coverage` int DEFAULT NULL,
  `Alignment` double DEFAULT NULL,
  `Duplication` double DEFAULT NULL,
  `Date` date NOT NULL,
  `per_sequence_gc_content` double DEFAULT NULL,
  `avg_gc_content` double DEFAULT NULL,
  `per_base_n_content` double DEFAULT NULL,
  `per_base_sequence_quality` double DEFAULT NULL,
  `per_sequence_quality_scores` double DEFAULT NULL,
  `sequence_length_distribution` double DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `id_UNIQUE` (`ID`)
);

CREATE INDEX idx_species
ON `ultraqc_db`.`samples`(Species);

CREATE INDEX idx_speciesAndDate
ON `ultraqc_db`.`samples`(Date, Species);

CREATE TABLE `ultraqc_db`.`metric_gc_content` (
  `SampleID` INT NOT NULL AUTO_INCREMENT,
  `Count` INT NOT NULL,
  `GCcontent` DOUBLE NOT NULL,
  `RelContent` double DEFAULT NULL,
  PRIMARY KEY (`SampleID`, `Count`),
  CONSTRAINT `ID`
    FOREIGN KEY (`SampleID`)
    REFERENCES `ultraqc_db`.`samples` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);	
	
CREATE TABLE `ultraqc_db`.`metric_per_base_n_content` (
  `SampleID` INT NOT NULL,
  `Base` INT NOT NULL,
  `N-Count` DOUBLE NOT NULL,
  PRIMARY KEY (`SampleID`, `Base`),
  CONSTRAINT `NID`
    FOREIGN KEY (`SampleID`)
    REFERENCES `ultraqc_db`.`samples` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);
	
CREATE TABLE `ultraqc_db`.`metric_per_base_sequence_quality` (
  `SampleID` INT NOT NULL,
  `Base` INT NOT NULL,
  `Mean` DOUBLE NULL,
  `Median` DOUBLE NULL,
  `Lower_Quartile` DOUBLE NULL,
  `Upper_Quartile` DOUBLE NULL,
  `10th_Percentile` DOUBLE NULL,
  `90th_Percentile` DOUBLE NULL,
  PRIMARY KEY (`SampleID`, `Base`),
  CONSTRAINT `SeqQualID`
    FOREIGN KEY (`SampleID`)
    REFERENCES `ultraqc_db`.`samples` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);
	
CREATE TABLE `ultraqc_db`.`metric_per_sequence_quality_scores` (
  `SampleID` INT NOT NULL,
  `Quality` INT NOT NULL,
  `Count` DOUBLE NULL,
  PRIMARY KEY (`SampleID`, `Quality`),
  CONSTRAINT `QualScoreID`
    FOREIGN KEY (`SampleID`)
    REFERENCES `ultraqc_db`.`samples` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);
	
CREATE TABLE `ultraqc_db`.`metric_sequence_length_distribution` (
  `SampleID` INT NOT NULL,
  `Length` INT NOT NULL,
  `Count` DOUBLE NULL,
  PRIMARY KEY (`SampleID`, `Length`),
  CONSTRAINT `SeqLenDistID`
    FOREIGN KEY (`SampleID`)
    REFERENCES `ultraqc_db`.`samples` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);
