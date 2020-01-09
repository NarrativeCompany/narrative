#!/bin/bash

set -e

DEPLOY="${1}"

if [[ ${GIT_BRANCH} == "dev" ]]; then
  BUILD_TYPE=SNAPSHOT
elif [[ ${GIT_BRANCH} == release-* ]]; then
  BUILD_TYPE=RC
elif [[ ${GIT_BRANCH} == hotfix-* ]]; then
  BUILD_TYPE=HOTFIX
elif [[ ${GIT_BRANCH} == "master" ]]; then
  BUILD_TYPE=
else
  BUILD_TYPE=${GIT_BRANCH}.SNAPSHOT
fi

VERSION=$(xml_grep --cond='project/version' ./core/pom.xml --text_only)
if [ "${VERSION}" == "" ]; then
  echo "Error - Maven version was not found. Exiting..."
  echo "Failed in ${0}"
  exit 1
fi
echo "Found version ${VERSION}"

echo "Finding build number from Docker"
# we LOCK to the narrative-core version
DOCKER_TAG_LIST=$(curl -s -u ${USERNAME}:${PASSWORD} https://registry.hub.docker.com/v1/repositories/narrativecompany/narrative-core/tags |jq -r '.[].name')

if [[ -z "${BUILD_TYPE}" ]]; then
  echo "No BUILD_TYPE; starting master build"
  # if this is a master/production build, we don't care about build number, so bail out
  DUPLICATE_DOCKER_TAG=$(echo "${DOCKER_TAG_LIST}" | grep -E "^${VERSION}$" || echo "")
  if [[ -z "${DEPLOY}" ]] && [[ ! -z "${DUPLICATE_DOCKER_TAG}" ]]; then
    echo "found a docker image already built with tag ${DUPLICATE_DOCKER_TAG}!"
    exit 1
  elif [[ ! -z "${DEPLOY}" ]] && [[ -z "${DUPLICATE_DOCKER_TAG}" ]]; then
    echo "failed to identify a docker image built with tag ${DUPLICATE_DOCKER_TAG}!"
    exit 1
  fi
  echo "not docker image found. good! continuing to build ${VERSION}"
  BUILD_SUFFIX=
  DOCKER_TAG=${VERSION}
  DOCKER_TAG_DEPLOY=${VERSION}
else

  BUILD_NUM=$(echo "${DOCKER_TAG_LIST}" | grep -E "^${VERSION}-${BUILD_TYPE}-[0-9]*$" | awk -F'-' '{ print $NF }' | sort -n | tail -n 1)


  if [[ -z "${BUILD_NUM}" ]]; then
    echo "Setting BUILD_NUM to 0..."
    BUILD_NUM=0
  else
    echo "Found before incrementing BUILD_NUM: ${BUILD_NUM}"
  fi

  DOCKER_TAG_DEPLOY=${VERSION}-${BUILD_TYPE}-${BUILD_NUM}

  echo "Incrementing BUILD_NUM..."
  BUILD_NUM=$((BUILD_NUM+1))
  BUILD_SUFFIX=-${BUILD_TYPE}-${BUILD_NUM}
  DOCKER_TAG=${VERSION}${BUILD_SUFFIX}
fi

export BUILD_SUFFIX
export DOCKER_TAG
export DOCKER_TAG_DEPLOY

echo "${BUILD_SUFFIX}" | tee BUILD_SUFFIX
echo "${DOCKER_TAG}" | tee DOCKER_TAG
echo "${DOCKER_TAG_DEPLOY}" | tee DOCKER_TAG_DEPLOY

echo "Current Docker tag: ${DOCKER_TAG_DEPLOY}"
echo "Next Docker tag: ${DOCKER_TAG}"
