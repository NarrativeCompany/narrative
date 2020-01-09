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

# Get versions... Fail if not found
REP_VERSION=$(xml_grep --cond='project/version' ./reputation/pom.xml --text_only)
if [ "${REP_VERSION}" == "" ]; then
  echo "Error Maven version was not found. Exiting..."
  echo "Failed in ${0}"
  exit 1
fi
echo "Found narrative-rep version ${REP_VERSION}"

echo "Finding build number from Docker"
# we LOCK to the narrative-core version
REP_DOCKER_TAG_LIST=$(curl -s -u ${USERNAME}:${PASSWORD} https://registry.hub.docker.com/v1/repositories/narrativecompany/narrative-reputation/tags |jq -r '.[].name')

if [[ -z "${BUILD_TYPE}" ]]; then
  echo "No BUILD_TYPE; starting master build"
  # if this is a master/production build, we don't care about build number, so bail out
  REP_DUPLICATE_DOCKER_TAG=$(echo "${REP_DOCKER_TAG_LIST}" | grep -E "^${REP_VERSION}$" || echo "")
  # narrative-reputation check (i kept these loops seperate. easier to update in the future. if we add more stuff, we may want to do a rewrite of this script.
  if [[ -z "${DEPLOY}" ]] && [[ ! -z "${REP_DUPLICATE_DOCKER_TAG}" ]]; then
    echo "narrative-rep - found a docker image already built with tag ${REP_DUPLICATE_DOCKER_TAG}! Exiting..."
    exit 1
  elif [[ ! -z "${DEPLOY}" ]] && [[ -z "${REP_DUPLICATE_DOCKER_TAG}" ]]; then
    echo "narrative-rep - failed to identify a docker image built with tag ${REP_DUPLICATE_DOCKER_TAG}! Exiting..."
    exit 1
  fi
  echo "Building narrative-reputation: ${REP_VERSION}"
  REP_BUILD_SUFFIX=
  REP_DOCKER_TAG=${REP_VERSION}
  REP_DOCKER_TAG_DEPLOY=${REP_VERSION}
else

  REP_BUILD_NUM=$(echo "${REP_DOCKER_TAG_LIST}" | grep -E "^${REP_VERSION}-${BUILD_TYPE}-[0-9]*$" | awk -F'-' '{ print $NF }' | sort -n | tail -n 1)


  if [[ -z "${REP_BUILD_NUM}" ]]; then
    echo "Setting REP_BUILD_NUM to 0..."
    REP_BUILD_NUM=0
  else
    echo "narrative-rep - found before incrementing REP_BUILD_NUM: ${REP_BUILD_NUM}"
  fi

  REP_DOCKER_TAG_DEPLOY=${REP_VERSION}-${BUILD_TYPE}-${REP_BUILD_NUM}

  echo "Incrementing REP_BUILD_NUM..."
  REP_BUILD_NUM=$((REP_BUILD_NUM+1))
  REP_BUILD_SUFFIX=-${BUILD_TYPE}-${REP_BUILD_NUM}
  REP_DOCKER_TAG=${REP_VERSION}${REP_BUILD_SUFFIX}
fi

export REP_BUILD_SUFFIX
export REP_DOCKER_TAG
export REP_DOCKER_TAG_DEPLOY
export REP_VERSION

echo "${REP_BUILD_SUFFIX}" | tee REP_BUILD_SUFFIX
echo "${REP_DOCKER_TAG}" | tee REP_DOCKER_TAG
echo "${REP_DOCKER_TAG_DEPLOY}" | tee REP_DOCKER_TAG_DEPLOY
echo "${REP_VERSION}" | tee REP_VERSION

echo "Current Docker tag: ${REP_DOCKER_TAG_DEPLOY}"
echo "Next Docker tag: ${REP_DOCKER_TAG}"
