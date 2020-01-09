set -e

# if auto deploy, find the latest version based on ./docker/export-narrative-build-vars.sh deploy
if [ "${REP_DOCKER_TAG_DEPLOY}" == "" ]; then
  echo "REP_DOCKER_TAG_DEPLOY not defined. Finding tag based on ./docker/export-narrative-build-vars.sh deploy..."
  # we LOCK to narrative-reputation version number
  . ./docker/export-rep-build-vars.sh deploy
else
  # Safe/easy way to line up an auto deploy and Docker tag deploy so that we can Slack notify the tag
  echo "${REP_DOCKER_TAG_DEPLOY}" > ./REP_DOCKER_TAG_DEPLOY
fi

# Test for the Docker image...
# check narrative-reputation
REP_DOCKER_TAG_LIST=$(curl -s -u ${USERNAME}:${PASSWORD} https://registry.hub.docker.com/v1/repositories/narrativecompany/narrative-reputation/tags |jq -r '.[].name' | grep -E "^${REP_DOCKER_TAG_DEPLOY}$" || echo "")
if [ "${REP_DOCKER_TAG_DEPLOY}" != "${REP_DOCKER_TAG_LIST}" ]; then
  echo "Docker tag not found in narrative-reputation repository. Exiting..."
  echo "REP_DOCKER_TAG_DEPLOY: ${REP_DOCKER_TAG_DEPLOY}"
  echo "REP_DOCKER_TAG_LIST: ${REP_DOCKER_TAG_LIST}"
  exit 1
fi

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

# narrative-reputation deployment
echo "Deploying rep-application-${K8S_ENV}-properties configmap..."
kubectl --token="${TOKEN}" -n ${K8S_ENV} create configmap rep-application-${K8S_ENV}-yaml --from-file=./kubernetes/narrative-reputation/${K8S_ENV}/application-${K8S_ENV}.yaml  --dry-run -o json | kubectl apply -f -

# There's always only 1 narrative-reputation replica
REPLICAS=1

# Remove all narrative-reputation replicas
echo "Make sure there is no narrative-reputation running..."
kubectl --token="${TOKEN}" -n ${K8S_ENV} patch deployment narrative-reputation -p '{"spec":{"replicas": 0}}' || echo "No need to update needed..."
while kubectl --token="${TOKEN}" -n ${K8S_ENV} get pods | grep Terminating | grep narrative-reputation; do
  echo "Waiting for narrative-reputation containers to be terminated..."
  sleep 3
done

# Deploy narrative-reputation
echo "Deploying Git branch ${GIT_BRANCH} Docker image ${REP_DOCKER_TAG_DEPLOY} to Kubernetes environment ${K8S_ENV}. URL: ${SITE_URL}"
kubectl --token="${TOKEN}" -n ${K8S_ENV} set image deployment/narrative-reputation narrative-reputation=narrativecompany/narrative-reputation:${REP_DOCKER_TAG_DEPLOY} --record
KREPLICAS=\''{"spec":{"replicas": '"${REPLICAS}"'}}'\'
echo "Setting replicas to ${KREPLICAS}"
echo $KREPLICAS | xargs -I {} kubectl --token="${TOKEN}" -n ${K8S_ENV} patch deployment narrative-reputation -p {}
until kubectl --token="${TOKEN}" -n ${K8S_ENV} get pods | grep Running | grep narrative-reputation; do
  echo "Waiting for container to start..."
  sleep 3
done

echo "narrative-reputation:${REP_DOCKER_TAG_DEPLOY} deployed!"

DEPLOYED_IMAGES=$(kubectl --token="${TOKEN}" -n ${K8S_ENV} get pods -o json | jq -r '.items[] | select(.status.phase == "Running") | {pod: .metadata.name, img: .spec.containers[].image} | select(.img | contains("narrativecompany"))' | grep -v -E "{|}")

echo "${DEPLOYED_IMAGES}" > DEPLOYED_IMAGES
