-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `diary_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `diary_db` ;

-- -----------------------------------------------------
-- Table `diary_db`.`diary`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `diary_db`.`diary` (
  `diarySeq` INT NOT NULL AUTO_INCREMENT,
  `content` VARCHAR(255) NOT NULL,
  `createdAt` VARCHAR(15) NOT NULL,
  `deletedAt` VARCHAR(15) NULL DEFAULT NULL,
  `dreamDate` VARCHAR(8) NOT NULL,
  `isDeleted` VARCHAR(1) NOT NULL,
  `isPublic` VARCHAR(1) NOT NULL,
  `title` VARCHAR(50) NOT NULL,
  `updatedAt` VARCHAR(15) NOT NULL,
  `userSeq` INT NOT NULL,
  `videoUrl` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`diarySeq`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;
