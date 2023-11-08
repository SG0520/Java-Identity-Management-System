CREATE TABLE `forgot_password_token` (
  `password_token` varchar(64) NOT NULL,
  `created_date` datetime(6) NOT NULL,
  `email` varchar(120) NOT NULL,
  `expire_date` datetime(6) NOT NULL,
  `tenant_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`password_token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;