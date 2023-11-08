-- Add Column
ALTER TABLE users
  ADD COLUMN MODIFIED_BY varchar(255) DEFAULT NULL,
  ADD COLUMN MODIFIED_DATE datetime(6) NOT NULL,
  ADD COLUMN USER_ROLE_ID varchar(255) NOT NULL;

-- Drop column
ALTER TABLE users
  DROP COLUMN USER_TYPE,
  DROP COLUMN USER_NAME;

-- Add Index
ALTER TABLE `mdist1_1`.`users` 
ADD INDEX `FK7x3uo1krtxr8r60py9rd2ys5p_idx` (`USER_ROLE_ID` ASC) VISIBLE;
;

-- Add foreign key constraint
ALTER TABLE users
ADD CONSTRAINT `FK7x3uo1krtxr8r60py9rd2ys5p`
  FOREIGN KEY (`USER_ROLE_ID`)
  REFERENCES `mdist1_1`.`user_role` (`user_role_id`)
  ON DELETE RESTRICT
  ON UPDATE RESTRICT;


  