ALTER TABLE `forgot_password_token`
  ADD COLUMN `user_guid` VARCHAR(64) NOT NULL AFTER `tenant_id`;