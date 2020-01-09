-- Execute me as MySQL root or a user that has the CREATE DATABASE grant

DROP DATABASE IF EXISTS reputation;
DROP USER IF EXISTS reputation_user;
CREATE DATABASE reputation CHARACTER SET utf8 COLLATE utf8_unicode_ci;
