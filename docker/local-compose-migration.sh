#!/bin/bash
set -e
MYDATE=$(date '+%Y-%m-%d-%H-%M-%S')
DOCKER_DIR=${HOME}/.narrative

if [ ! -f local-compose.yml ]; then
  echo "This script is meant to be run from the docker directory of the narrative-platform Git repository. Exiting..."
  exit 1
fi

echo "Stopping local-compose..."
docker-compose -f local-compose.yml down > /dev/null 2>&1 && docker-compose -f local-compose.yml rm --force > /dev/null 2>&1
echo "Migrating volume data from Docker..."
mkdir -p ${DOCKER_DIR}/${MYDATE}
docker container create --name mysql-copy -v docker_mysql_data:/opt/mysql hello-world
docker container create --name solr-copy -v docker_solr_data:/opt/solr hello-world
docker cp mysql-copy:/opt/mysql ${DOCKER_DIR}/${MYDATE}
docker cp solr-copy:/opt/solr ${DOCKER_DIR}/${MYDATE}
rm -f ${DOCKER_DIR}/default/mysql/etc/mtab
rm -f ${DOCKER_DIR}/default/solr/etc/mtab
docker rm mysql-copy solr-copy
echo "Migration complete..."
if [ -d ${DOCKER_DIR}/data ] && [ -L ${DOCKER_DIR}/data ]; then
  echo "Deleting old symlink..."
  rm ${DOCKER_DIR}/data
elif [ -d ${DOCKER_DIR}/data ]; then
  echo "Backing up original data directory..."
  mv ${DOCKER_DIR}/data ${DOCKER_DIR}/original-${MYDATE}
fi
echo "Creating symlink for ${DOCKER_DIR}/data to ${DOCKER_DIR}/${MYDATE}..."
ln -s ${DOCKER_DIR}/${MYDATE} ${DOCKER_DIR}/data
echo "Migration complete!"
