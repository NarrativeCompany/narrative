update mysql.user set host='%';

FLUSH PRIVILEGES;

alter user root identified by '${MYSQL_ROOT_PASSWORD}';
alter user global_user identified by '${MYSQL_GLOBAL_PASSWORD}';
alter user utility_user identified by '${MYSQL_UTILITY_PASSWORD}';

GRANT ALL ON `global`.* TO 'global_user'@'%';
GRANT ALL ON `realm2`.* TO 'realm2_user'@'%';
GRANT ALL ON `realm1`.* TO 'realm1_user'@'%';
GRANT ALL ON `comp2`.* TO 'comp2_user'@'%';
GRANT ALL ON `comp1`.* TO 'comp1_user'@'%';
GRANT ALL ON `utility`.* TO 'utility_user'@'%';
