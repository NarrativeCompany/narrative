#!/bin/bash

# exit on errors
set -e

# Set this. We'll check if it's null later.
DB_SERVER="${1}"

if docker ps | grep docker_mysql_1; then
  echo "mysql already running..."
else
  echo "Starting seed-mysql-docker-compose.yml..."
  docker-compose -f ./seed-mysql-docker-compose.yml up -d
fi

# echo "Sleeping 10 seconds to let mysql start up..."
# sleep 10

until curl -s http://localhost:3306 | grep mysql; do
  echo "Curing localhost:3306 to make sure the port is up... (the response will be jibberish)"
  sleep 3
done

echo "Installing envsubst"
docker exec docker_mysql_1 /bin/bash -c "apt-get update && apt-get install gettext-base"

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
if docker exec docker_mysql_1 /bin/bash -c 'mysql -u root --password=${MYSQL_ROOT_PASSWORD} -e "show databases;" | grep global'; then
  echo "global database found."
else
  echo "Importing nq002-all-databases..."
  docker exec docker_mysql_1 /bin/bash -c 'mysql -u root --password=${MYSQL_ROOT_PASSWORD} < /opt/narrative-platform/sql/nq002-all-databases.sql'
fi

echo "Setting the DBPartition server address (if argument is defined)..."


echo "Checking the global_user..."
if docker exec docker_mysql_1 /bin/bash -c 'mysql -u global_user --password=${MYSQL_GLOBAL_PASSWORD} -e "use global; show tables"'; then
  echo "global_user can log into the global database."
else
  echo "Global user not configured. Updating..."
  docker exec docker_mysql_1 /bin/bash -c "envsubst < /opt/narrative-platform/mysql/update-mysql-user.sql > /mysql-user.sql"

  echo "Updating users in mysql..."
  docker exec docker_mysql_1 /bin/bash -c 'mysql -u root --password=${MYSQL_ROOT_PASSWORD} < /mysql-user.sql'

  echo "Printing out global tables..."
  docker exec docker_mysql_1 /bin/bash -c 'mysql -u global_user --password=${MYSQL_GLOBAL_PASSWORD} -e "use global; show tables"'

  echo "Printing out utility tables..."
  docker exec docker_mysql_1 /bin/bash -c 'mysql -u utility_user --password=${MYSQL_UTILITY_PASSWORD} -e "use utility; show tables"'
fi

if [ "$1" ]; then
  echo "DB_SERVER set to ${DB_SERVER}."
  docker exec docker_mysql_1 /bin/bash -c "export DBPARTITION_SERVER=${DB_SERVER} && envsubst < /opt/narrative-platform/mysql/update-dbpartition-server.sql > /dbpartition-server.sql"
  docker exec docker_mysql_1 /bin/bash -c 'mysql -u root --password=${MYSQL_ROOT_PASSWORD} < /dbpartition-server.sql'
else
  echo "DB_SERVER NOT set."
fi

echo "MySQL init complete. Stopping docker-compose..."
docker-compose -f ./seed-mysql-docker-compose.yml stop
docker-compose -f ./seed-mysql-docker-compose.yml rm --force

echo "You can now run either the database-compose or docker-compose."
