#!/bin/bash

set -e

# Track how long the build process takes...
SECONDS=0
echo "NWFE Checkpoint 0 is at ${SECONDS} seconds. (Set variables)"

docker login -u "${USERNAME}" -p "${PASSWORD}"

if ls ./react-ui > /dev/null 2>&1; then
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

# Build docker image
echo "Get the Git SHA... (Jenkins environment variable, but this is fast for non-Jenkins builds)"
GIT_SHA=$(git --no-pager log -n 1 --pretty=format:"%H")
SHORT_GIT_SHA=${GIT_SHA:0:7}
BUILD_DATE=$(date +'%s')
FULL_VERSION=${DOCKER_TAG}-${SHORT_GIT_SHA}

# convert epoch to human date...
BUILD_UTC_DATE=$(date -d @${BUILD_DATE} -u +"%Y-%m-%dT%H:%M:%SZ")

VERSION_FILE=version.txt

echo "Building narrative-web-front-end Docker image..."
echo "Version: ${FULL_VERSION}" | tee ${VERSION_FILE}
echo "Build Date: ${BUILD_UTC_DATE}" | tee -a ${VERSION_FILE}
echo "Branch: ${GIT_BRANCH}" | tee -a ${VERSION_FILE}
echo "Jenkins Build ID: ${BUILD_ID}" | tee -a ${VERSION_FILE}
echo "Git SHA: ${GIT_SHA}" | tee -a ${VERSION_FILE}

# set the version in index.tsx
sed -E -i "s/window\['NARRATIVE_VERSION'\] = 'local'/window['NARRATIVE_VERSION'] = '${FULL_VERSION}'/w changelog.txt" react-ui/packages/web/src/index.tsx
if [[ -s changelog.txt ]]; then
  rm changelog.txt
  echo "Version in index.tsx set to ${FULL_VERSION}"
else
  rm changelog.txt
  echo "Failed setting version in index.tsx. Has the format changed? Exiting..."
  exit 1
fi

echo "NWFE Checkpoint 1 is at ${SECONDS} seconds. (Yarn builds)"

if docker ps | grep build-frontend; then
  echo "Node build container already running..."
else
  echo "Start up the Node build container..."
  docker pull node:11.3
  docker run --name build-frontend --rm -d -v ${PWD}/react-ui:/opt/src --entrypoint=/bin/bash node:11.3 -c "sleep infinity"
fi

echo "First yarn install..."
docker exec build-frontend /bin/bash -c "cd /opt/src && yarn"

# the BUILD_SUFFIX will be empty for production builds. in that case, let's not generate a sourcemap.
if [[ -z "${BUILD_SUFFIX}" ]]; then
  GENERATE_SOURCEMAP=false
else
  # for all non-production builds, generate the sourcemap
  GENERATE_SOURCEMAP=true
fi

echo "yarn run generate shared..."
docker exec build-frontend /bin/bash -c "cd /opt/src/packages/shared && yarn run generate"

echo "yarn run build shared..."
docker exec build-frontend /bin/bash -c "cd /opt/src/packages/shared && GENERATE_SOURCEMAP=${GENERATE_SOURCEMAP} yarn run build"

echo "yarn run build web..."
docker exec build-frontend /bin/bash -c "cd /opt/src/packages/web && GENERATE_SOURCEMAP=${GENERATE_SOURCEMAP} yarn run build"

echo "NWFE Checkpoint 2 is at ${SECONDS} seconds. (Build narrative-web-frontend Docker image)"

docker pull nginx:stable
docker build \
  --label "VERSION=${VERSION}" \
  --label "BUILD_UTC_DATE=${BUILD_UTC_DATE}" \
  --label "GIT_BRANCH=${GIT_BRANCH}" \
  --label "JENKINS_BUILD_ID=${BUILD_ID}" \
  --label "GIT_SHA=${GIT_SHA}" \
  -t narrativecompany/narrative-web-front-end:latest \
  -f ./docker/Dockerfile-narrative-web-front-end .

echo "narrative-web-front-end Docker image complete."

echo "Stopping the build-frontend Docker container..."
docker stop build-frontend

echo "Tagging the new build..."
docker tag narrativecompany/narrative-web-front-end:latest narrativecompany/narrative-web-front-end:${DOCKER_TAG}
docker tag narrativecompany/narrative-web-front-end:latest narrativecompany/narrative-web-front-end:${GIT_BRANCH}-latest

echo "Total time: ${SECONDS} seconds."
echo "NWFE Checkpoint 3 is at ${SECONDS} seconds. (END)"

