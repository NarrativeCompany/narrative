#!/bin/bash
# This is the MySQL DB seed script for the dev (and maybe staging) environment.

# exit on errors
set -e

if [ "${MYSQL_ROOT_USER}" == "" ]; then
  echo "MYSQL_ROOT_USER not defined. Exiting..."
  exit 1
fi

if [ "${MYSQL_ROOT_PASSWORD}" == "" ]; then
  echo "MYSQL_ROOT_PASSWORD not defined. Exiting..."
  exit 1
fi

if [ "${MYSQL_GLOBAL_PASSWORD}" == "" ]; then
  echo "MYSQL_GLOBAL_PASSWORD not defined. Exiting..."
  exit 1
fi

if [ "${MYSQL_UTILITY_PASSWORD}" == "" ]; then
  echo "MYSQL_UTILITY_PASSWORD not defined. Exiting..."
  exit 1
fi

echo "Installing envsubst"
which envsubst || ( apt-get update && apt-get install -y gettext-base )

echo "Checking for the seed tar.gz file..."
if [ -f ./sql/nq002-all-databases.sql.gz ]; then
  echo "nq002-all-databases.sql.gz found. Extracting..."
  gunzip ./sql/nq002-all-databases.sql.gz
elif [ -f ./sql/nq002-all-databases.sql ]; then
  echo "unzipped nq002-all-databases.sql found."
else
  echo "nq002-all-databases.sql.gz not found. Download it from the Narrative Dropbox."
  exit 1
fi

echo "Checking for the existance of the global database..."
if mysql -h 127.0.0.1 -P 3306 -u ${MYSQL_ROOT_USER} --password=${MYSQL_ROOT_PASSWORD} -e "show databases;" | grep global; then
  echo "global database found."
else
  echo "Importing nq002-all-databases..."
  mysql -h 127.0.0.1 -P 3306 -u ${MYSQL_ROOT_USER} --password=${MYSQL_ROOT_PASSWORD} < /opt/narrative-platform/sql/nq002-all-databases.sql
fi

echo "Checking the global_user..."
if mysql -h 127.0.0.1 -P 3306 -u global_user --password=${MYSQL_GLOBAL_PASSWORD} -e "use global; show tables"; then
  echo "global_user can log into the global database."
else
  echo "Global user not configured. Updating..."
  envsubst < /opt/narrative-platform/mysql/dev-users.sql > /mysql-user.sql

  echo "Updating users in mysql..."
  mysql -h 127.0.0.1 -P 3306 -u ${MYSQL_ROOT_USER} --password=${MYSQL_ROOT_PASSWORD} < /mysql-user.sql

  echo "Printing out global tables..."
  mysql -h 127.0.0.1 -P 3306 -u global_user --password=${MYSQL_GLOBAL_PASSWORD} -e "use global; show tables"

  echo "Printing out utility tables..."
  mysql -h 127.0.0.1 -P 3306 -u utility_user --password=${MYSQL_UTILITY_PASSWORD} -e "use utility; show tables"
fi

echo "DB_SERVER set to 'localhost'."
export DBPARTITION_SERVER=localhost && envsubst < /opt/narrative-platform/mysql/update-dbpartition-server.sql > /dbpartition-server.sql
mysql -h 127.0.0.1 -P 3306 -u ${MYSQL_ROOT_USER} --password=${MYSQL_ROOT_PASSWORD} < /dbpartition-server.sql

echo "Checking for the global procedures..."
if mysql -h 127.0.0.1 -P 3306 -u ${MYSQL_ROOT_USER} --password=${MYSQL_ROOT_PASSWORD} -e "use mysql; show procedure status;" | grep global_user; then
  echo "global procedures exist."
else
  mysql -h 127.0.0.1 -P 3306 -u ${MYSQL_ROOT_USER} --password=${MYSQL_ROOT_PASSWORD} < /opt/narrative-platform/sql/nq002-stored-procedures.sql
fi
