#!/bin/bash

# Teporary check for region. I expect dealing with multiple regions in the future.... Will be a refactor. For now, it's simple.
if [ "${GOOGLE_ENV}" == "production" ]; then
  REGION=us-central1
else
  REGION=us-east1
fi

# Log into Google Cloud and get the Kubernetes token
gcloud auth revoke --all > /dev/null 2>&1 || echo "No accounts to revoke..."
gcloud auth activate-service-account jenkins@informationtechnology-205813.iam.gserviceaccount.com --key-file=${GCLOUD_JENKINS_FILE}
gcloud beta container clusters get-credentials ${GOOGLE_ENV}-cluster --region ${REGION} --project ${GOOGLE_ENV}-narrative
TOKEN=$(gcloud auth print-access-token --account=jenkins@informationtechnology-205813.iam.gserviceaccount.com)

echo "Get the current blue/green status from the ingress..."
CURRENT_CORE=$(kubectl --token="${TOKEN}" -n ${K8S_ENV} get svc narrative-core -o json | jq -r '.spec.selector.app')

if [ "${CURRENT_CORE}" == 'narrative-core' ]; then
  NEW_CORE='blue-narrative-core'
  NEW_WEB='blue-narrative-web-front-end'
  CURRENT_WEB='narrative-web-front-end'
elif [ "${CURRENT_CORE}" == 'blue-narrative-core' ]; then
  NEW_CORE='narrative-core'
  NEW_WEB='narrative-web-front-end'
  CURRENT_WEB='blue-narrative-web-front-end'
else
  echo "Current service selector app is ${CURRENT_CORE}. Something went wrong; it should be 'narrative-core' or 'blue-narrative-core'. No changes made. Exiting..."
  exit 1
fi

echo "Current deployment is ${CURRENT_CORE}... New deployment is ${NEW_CORE}..."

# Get replicas - core
echo "Getting replicas..."
REPLICAS_CORE=$(kubectl --token="${TOKEN}" -n ${K8S_ENV} get deployment ${CURRENT_CORE} -o json | jq -r '.spec.replicas')
# Get existing narrative-core replicas
if [ "${REPLICAS_CORE}" == '' ]; then
  echo "REPLICAS_CORE variable is empty. Exiting..."
  exit 1
elif [ "${REPLICAS_CORE}" == '0' ]; then
  echo "Found a 0 set replicas. Setting per environment to things going..."
  if [ "${GOOGLE_ENV}" == "dev" ]; then
    REPLICAS_CORE='1'
  else
    REPLICAS_CORE='2'
  fi
fi

# Get replicas - web
echo "Getting replicas..."
REPLICAS_WEB=$(kubectl --token="${TOKEN}" -n ${K8S_ENV} get deployment ${CURRENT_WEB} -o json | jq -r '.spec.replicas')
# Get existing narrative-web-front-end replicas
if [ "${REPLICAS_WEB}" == '' ]; then
  echo "REPLICAS_WEB variable is empty. Exiting..."
  exit 1
elif [ "${REPLICAS_WEB}" == '0' ]; then
  echo "Found a 0 set replicas. Setting per environment to things going..."
  if [ "${GOOGLE_ENV}" == "dev" ]; then
    REPLICAS_WEB='1'
  else
    REPLICAS_WEB='3'
  fi
fi

echo "Export variables..."
export CURRENT_CORE
export NEW_CORE
export REPLICAS_CORE
export CURRENT_WEB
export NEW_WEB
export REPLICAS_WEB

# Template and deploy the core deployment
echo "Templating and deploying the ${NEW_CORE} core deployment and ${NEW_WEB} web deployment..."
echo "Core deployment:        ${NEW_CORE}"
echo "Web deployment:         ${NEW_WEB}"
echo "Git branch:             ${GIT_BRANCH}"
echo "Kubernetes environment: ${K8S_ENV}"
echo "Google Env:             ${GOOGLE_ENV}"
echo "Region:                 ${REGION}"
echo "Replicas - Core:        ${REPLICAS_CORE}"
echo "Replicas - Web:         ${REPLICAS_WEB}"
echo "URL:                    ${SITE_URL}"
