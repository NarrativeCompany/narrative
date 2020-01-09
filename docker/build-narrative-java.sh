#!/bin/bash

set -e

# Track how long the build process takes...
SECONDS=0
echo "JAVA Checkpoint 0 is at ${SECONDS} seconds. (Set variables, sleep 10)"

ARG="${1}"

docker login -u "${USERNAME}" -p "${PASSWORD}"

# check the directory...
if ls ./core/src > /dev/null 2>&1; then
  echo "In the proper build directory..."
else
  echo "Make sure this script is run from the docker directory... Exiting..."
  exit 1
fi

if [ -z "${DOCKER_TAG_DEPLOY}" ]; then
  echo "Narrative build vars not set. Exporting them now..."
  . ./docker/export-narrative-build-vars.sh
else
  echo "DOCKER_TAG_DEPLOY variable exists. No need to re-run export-narrative-build-vars.sh..."
fi

if [ -z "${REP_DOCKER_TAG_DEPLOY}" ]; then
  echo "Narrative reputation build vars not set. Exporting them now..."
  . ./docker/export-rep-build-vars.sh
else
  echo "REP_DOCKER_TAG_DEPLOY variable exists. No need to re-run export-rep-build-vars.sh..."
fi

# Set some variables...
GIT_SHA=$(git --no-pager log -n 1 --pretty=format:"%H")
SHORT_GIT_SHA=${GIT_SHA:0:7}
BUILD_DATE=$(date +'%s')

# convert epoch to human date...
BUILD_UTC_DATE=$(date -d @${BUILD_DATE} -u +"%Y-%m-%dT%H:%M:%SZ")

echo "Building narrative-core Docker image..."
echo "Build Date: ${BUILD_UTC_DATE}"
echo "Branch: ${GIT_BRANCH}"
echo "Jenkins Build ID: ${BUILD_ID}"
echo "Git SHA: ${GIT_SHA}"
echo "Short Git SHA: ${SHORT_GIT_SHA}"

docker kill maven-build > /dev/null 2>&1 || echo "No maven-build Docker container running..."

sleep 10
echo "JAVA Checkpoint 1 is at ${SECONDS} seconds. (Maven build)"

echo "Start the Maven build container..."
docker pull maven:3.5.4-jdk-8
docker run -d --rm --name maven-build \
  -e GOOGLE_APPLICATION_CREDENTIALS=/root/jenkins.json \
  -e BUILD_DATE=${BUILD_DATE} \
  -e GIT_BRANCH=${GIT_BRANCH} \
  -e JENKINS_BUILD_ID=${BUILD_ID} \
  -e GIT_SHA=${GIT_SHA} \
  -e SHORT_GIT_SHA=${SHORT_GIT_SHA} \
  -e BUILD_SUFFIX=${BUILD_SUFFIX} \
  -e REP_BUILD_SUFFIX=${REP_BUILD_SUFFIX} \
  -e MAVEN_OPTS=-Xmx2048m \
  -v ${GCLOUD_JENKINS_FILE}:/root/jenkins.json \
  -v ${HOME}/.m2:/root/.m2 \
  -v ${PWD}:/usr/src/mymaven \
  -w /usr/src/mymaven \
  maven:3.5.4-jdk-8 /bin/bash -c "sleep infinity"

# Remove any .mvn things...
# Note that Docker creates this file as the root user, because within the Docker container it runs as root.
docker exec maven-build rm -rf /root/.m2/repository/org/narrative

NARRATIVE_WAR=narrative-core-${DOCKER_TAG}.war
NARRATIVE_REP=narrative-reputation-${REP_DOCKER_TAG}.jar

if [ "${ARG}" == "skip_tests" ]; then
  echo "Building package AND skipping tests..."
  docker exec maven-build mvn -B clean install -Dmaven.test.skip=true
else
  echo "Building package..."
  docker exec maven-build mvn -B clean install
fi
mkdir -p target

# Debug the target directory
echo "narrative-core target directory..."
docker exec maven-build ls -alh /usr/src/mymaven/core/target
docker cp maven-build:/usr/src/mymaven/core/target/${NARRATIVE_WAR} ${PWD}/target/${NARRATIVE_WAR}

echo "narrative-reputation target directory..."
docker exec maven-build ls -alh /usr/src/mymaven/reputation/target
docker cp maven-build:/usr/src/mymaven/reputation/target/${NARRATIVE_REP} ${PWD}/target/${NARRATIVE_REP}

echo "Stopping maven-build Docker container..."
docker stop maven-build
echo "Maven build complete."
echo "Maven Tomcat WAR build took ${SECONDS} seconds."
echo "JAVA Checkpoint 2 is at ${SECONDS} seconds. (Build narrative-core Docker image)"

# env substitution
NARRATIVE_WAR=${NARRATIVE_WAR} envsubst < Dockerfile > Dockerfile-narrative-core
NARRATIVE_REP=${NARRATIVE_REP} envsubst < reputation/Dockerfile > Dockerfile-narrative-reputation

# Build narrative-core docker image
echo "Building narrative-core Docker image: ${NARRATIVE_WAR}"
docker pull openjdk:8-jre-stretch
docker build \
  --label "VERSION=${VERSION}" \
  --label "BUILD_UTC_DATE=${BUILD_UTC_DATE}" \
  --label "GIT_BRANCH=${GIT_BRANCH}" \
  --label "JENKINS_BUILD_ID=${BUILD_ID}" \
  --label "GIT_SHA=${GIT_SHA}" \
  -t narrativecompany/narrative-core:latest -f Dockerfile-narrative-core .
echo "narrative-core Docker image complete."
echo "JAVA Checkpoint 3 is at ${SECONDS} seconds. (Build narrative-reputation Docker image)"

# Build narrative-reputation docker image
echo "Building narrative-reputation Docker image: ${NARRATIVE_REP}"
docker build \
  --label "VERSION=${REP_VERSION}" \
  --label "BUILD_UTC_DATE=${BUILD_UTC_DATE}" \
  --label "GIT_BRANCH=${GIT_BRANCH}" \
  --label "JENKINS_BUILD_ID=${BUILD_ID}" \
  --label "GIT_SHA=${GIT_SHA}" \
  -t narrativecompany/narrative-reputation:latest -f Dockerfile-narrative-reputation .
echo "narrative-reputation Docker image complete."
echo "JAVA Checkpoint 4 is at ${SECONDS} seconds. (Push narrative-web-frontend, narrative-core, and narrative-reputation Docker images)"

echo "Tagging the narrative-core build..."
docker tag narrativecompany/narrative-core:latest narrativecompany/narrative-core:${DOCKER_TAG}
docker tag narrativecompany/narrative-core:latest narrativecompany/narrative-core:${GIT_BRANCH}-latest

echo "Tagging the narrative-reputation build..."
docker tag narrativecompany/narrative-reputation:latest narrativecompany/narrative-reputation:${REP_DOCKER_TAG}
docker tag narrativecompany/narrative-reputation:latest narrativecompany/narrative-reputation:${GIT_BRANCH}-latest

# The big push... Maybe make this it's own script later
if [ -z "${JENKINS_URL}" ]; then
  echo "JENKINS_URL not defined. I don't think I'm running in Jenkins, therefore, skipping docker push."
else
  echo "JENKINS_URL FOUND!"
  echo "Pushing narrative-web-front-end and narrative-core..."
  docker push narrativecompany/narrative-web-front-end:${DOCKER_TAG}
  docker push narrativecompany/narrative-web-front-end:${GIT_BRANCH}-latest
  docker push narrativecompany/narrative-core:${DOCKER_TAG}
  docker push narrativecompany/narrative-core:${GIT_BRANCH}-latest
  docker push narrativecompany/narrative-reputation:${REP_DOCKER_TAG}
  docker push narrativecompany/narrative-reputation:${GIT_BRANCH}-latest
  if [ "${GIT_BRANCH}" == "master" ]; then
    docker push narrativecompany/narrative-web-front-end:latest
    docker push narrativecompany/narrative-core:latest
    docker push narrativecompany/narrative-reputation:latest
  fi
fi

echo "Total time: ${SECONDS} seconds."
echo "JAVA Checkpoint 5 is at ${SECONDS} seconds. (END)"
