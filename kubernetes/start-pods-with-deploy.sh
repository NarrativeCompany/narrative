#!/bin/bash
set -e
# narrative-core deployment
echo "Deploying application-${K8S_ENV}-properties configmap..."
kubectl --token="${TOKEN}" -n ${K8S_ENV} create configmap application-${K8S_ENV}-properties --from-file=./kubernetes/narrative-core/${K8S_ENV}/application-${K8S_ENV}.properties  --dry-run -o json | kubectl apply -f -

# just one replica
K8S_ENV=${K8S_ENV} GOOGLE_ENV=${GOOGLE_ENV} REPLICAS=1 DOCKER_TAG=${DOCKER_TAG_DEPLOY} REGION=${REGION} APP=${NEW_CORE} envsubst < ./kubernetes/narrative-core/template-narrative-core-start-pods-deployment.yml > core-deployment.yml

kubectl --token="${TOKEN}" apply -f core-deployment.yml

until kubectl --token="${TOKEN}" -n ${K8S_ENV} get pods | grep Running | grep "^${NEW_CORE}"; do
  echo "Waiting for container to start..."
  kubectl --token="${TOKEN}" -n ${K8S_ENV} get pods
  echo "Is other app: ${NEW_CORE} home?"
  sleep 3
done
echo "Wait for port 9090 to be open..."
NARRATIVE_CORE_POD=$(kubectl --token="${TOKEN}" -n ${K8S_ENV} get pods | grep Running | grep "^${NEW_CORE}" | head -n 1 | awk '{ print $1 }')
until kubectl --token="${TOKEN}" -n ${K8S_ENV} exec ${NARRATIVE_CORE_POD} -c narrative-core -- /bin/bash -c "cat < /dev/tcp/127.0.0.1/9090"; do
  echo "narrative-core pod not ready... waiting..."
  sleep 5
done

# Remove all narrative-core replicas
echo "Stop the ${NEW_CORE} deployment pod..."
kubectl --token="${TOKEN}" -n ${K8S_ENV} patch deployment ${NEW_CORE} -p '{"spec":{"replicas": 0}}' || echo "No need to update needed..."
while kubectl --token="${TOKEN}" -n ${K8S_ENV} get pods | grep Terminating | grep ${NEW_CORE}; do
  echo "Waiting for ${NEW_CORE} pods to be terminated..."
  sleep 3
done

# bl: instead of updating replicas, we need to switch to a normal deployment that doesn't have any readiness checks
# echo "Updating replicas to ${REPLICAS_CORE}"
# KREPLICAS=\''{"spec":{"replicas": '"${REPLICAS_CORE}"'}}'\'
# echo "${KREPLICAS}"
# echo $KREPLICAS | xargs -I {} kubectl --token="${TOKEN}" -n ${K8S_ENV} patch deployment ${NEW_CORE} -p {}

echo "Applying ${NEW_CORE} core-deployment with ${REPLICAS_CORE} replicas"
K8S_ENV=${K8S_ENV} GOOGLE_ENV=${GOOGLE_ENV} REPLICAS=${REPLICAS_CORE} DOCKER_TAG=${DOCKER_TAG_DEPLOY} REGION=${REGION} APP=${NEW_CORE} envsubst < ./kubernetes/narrative-core/template-narrative-core-deployment.yml > core-deployment.yml

kubectl --token="${TOKEN}" apply -f core-deployment.yml

until kubectl --token="${TOKEN}" -n ${K8S_ENV} get pods | grep Running | grep ${NEW_CORE}; do
  echo "Waiting for ${NEW_CORE} container to start..."
  sleep 3
done
echo "Wait for port 9090 to be open..."
NARRATIVE_CORE_POD=$(kubectl --token="${TOKEN}" -n ${K8S_ENV} get pods | grep Running | grep "^${NEW_CORE}" | head -n 1 | awk '{ print $1 }')
until kubectl --token="${TOKEN}" -n ${K8S_ENV} exec ${NARRATIVE_CORE_POD} -c narrative-core -- /bin/bash -c "cat < /dev/tcp/127.0.0.1/9090"; do
  echo "narrative-core pod not ready... waiting..."
  sleep 5
done

# narrative-web-front-end deployment
K8S_ENV=${K8S_ENV} GOOGLE_ENV=${GOOGLE_ENV} DOCKER_TAG=${DOCKER_TAG_DEPLOY} REPLICAS=${REPLICAS_WEB} REGION=${REGION} WEB=${NEW_WEB} envsubst < ./kubernetes/narrative-web-front-end/template-narrative-web-front-end-deployment.yml > web-front-end-deployment.yml

kubectl --token="${TOKEN}" apply -f web-front-end-deployment.yml

until kubectl --token="${TOKEN}" -n ${K8S_ENV} get pods | grep Running | grep "^${NEW_WEB}"; do
  echo "Waiting for container to start..."
  sleep 3
done
kubectl --token="${TOKEN}" -n ${K8S_ENV} rollout status deployment ${NEW_WEB}
echo "narrative-web-front-end pod is ready..."

echo "Pausing 15 seconds to let final pod drain for web-front-end..."
sleep 15

echo "Now to change the service for:"
echo "narrative-core:          ${CURRENT_CORE} to ${NEW_CORE}"
echo "narrative-web-front-end: ${CURRENT_WEB} to ${NEW_WEB}"
K8S_ENV=${K8S_ENV} APP=${NEW_CORE} WEB=${NEW_WEB} envsubst < ./kubernetes/narrative-core/template-narrative-service.yml > service.yml
kubectl apply -f service.yml

# Recreate the narrative-core and prerender ingresses
echo "Creating the prerender ingress and narrative-core ingress..."
kubectl --token="${TOKEN}" apply -f ./kubernetes/narrative-core/${K8S_ENV}/narrative-core-ingress.yml
kubectl --token="${TOKEN}" apply -f ./kubernetes/prerender/${K8S_ENV}/prerender-ingress.yml

# Remove the system-update ingress
echo "Deleting the system-update-page ingress..."
kubectl --token="${TOKEN}" -n ${K8S_ENV} delete ingress system-update-page  || echo "system-update-page ingress is already deleted."

echo "Site deployed!"

DEPLOYED_IMAGES=$(kubectl --token="${TOKEN}" -n ${K8S_ENV} get pods -o json | jq -r '.items[] | select(.status.phase == "Running") | {pod: .metadata.name, img: .spec.containers[].image} | select(.img | contains("narrativecompany"))' | grep -v -E "{|}")

echo "${DEPLOYED_IMAGES}" > DEPLOYED_IMAGES

echo "start-pods-with-deploy complete!"

