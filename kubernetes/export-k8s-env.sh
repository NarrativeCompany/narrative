#!/bin/bash

set -e

GIT_BRANCH=$(echo $GIT_BRANCH_ORIGIN | awk -F'/' '{ print $NF }')

if [[ ${ENVIRONMENT} != "auto" ]]; then
  K8S_ENV=${ENVIRONMENT}
elif [[ ${GIT_BRANCH} == "dev" ]]; then
  K8S_ENV=dev
elif [[ ${GIT_BRANCH} == release-* ]]; then
  K8S_ENV=staging
elif [[ ${GIT_BRANCH} == hotfix-* ]]; then
  K8S_ENV=staging
elif [[ ${GIT_BRANCH} == "master" ]]; then
  K8S_ENV=staging
else
  K8S_ENV=dev
fi

export K8S_ENV
echo "${K8S_ENV}"
echo "${K8S_ENV}" > K8S_ENV
