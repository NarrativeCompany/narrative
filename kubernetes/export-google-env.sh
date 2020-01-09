#!/bin/bash

set -e

. ./kubernetes/export-k8s-env.sh > /dev/null 2>&1

if [[ ${K8S_ENV} == "dev" ]]; then
  GOOGLE_ENV=sandbox
elif [[ ${K8S_ENV} == "staging" ]]; then
  GOOGLE_ENV=staging
elif [[ ${K8S_ENV} == "production" ]]; then
  GOOGLE_ENV=production
else
  echo "K8S_ENV: ${K8S_ENV}"
  echo "Something broke with the Google environment variables. Exiting..."
  exit 1
fi

export GOOGLE_ENV
echo "${GOOGLE_ENV}"
echo "${GOOGLE_ENV}" > GOOGLE_ENV
