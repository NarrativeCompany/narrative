#!/bin/bash
set -e

# Put static page in place
echo "Creating system-update-page ingress..."
kubectl --token="${TOKEN}" apply -f ./kubernetes/system-update-page/${K8S_ENV}/system-update-page-ingress.yml

# Remove ingresses
echo "Deleting the narrative-web-front-end ingress and narrative-core ingress..."
kubectl --token="${TOKEN}" -n ${K8S_ENV} delete ingress prerender || echo "Ingress prerender already deleted..."
kubectl --token="${TOKEN}" -n ${K8S_ENV} delete ingress narrative-core || echo "Ingress narrative-core already deleted..."

# Remove all narrative-core replicas
echo "Make sure there are no ${CURRENT_CORE} pods running..."
kubectl --token="${TOKEN}" -n ${K8S_ENV} patch deployment ${CURRENT_CORE} -p '{"spec":{"replicas": 0}}' || echo "No need to update needed..."
while kubectl --token="${TOKEN}" -n ${K8S_ENV} get pods | grep Terminating | grep ${CURRENT_CORE}; do
  echo "Waiting for ${CURRENT_CORE} pods to be terminated..."
  sleep 3
done

# Remove all narrative-web-front-end replicas
echo "Make sure there are no ${CURRENT_WEB} pods running..."
kubectl --token="${TOKEN}" -n ${K8S_ENV} patch deployment ${CURRENT_WEB} -p '{"spec":{"replicas": 0}}' || echo "No need to update needed..."
while kubectl --token="${TOKEN}" -n ${K8S_ENV} get pods | grep Terminating | grep ${CURRENT_WEB}; do
  echo "Waiting for ${CURRENT_WEB} pods to be terminated..."
  sleep 3
done

# Remove all narrative-reputation replicas
echo "Make sure there is no narrative-reputation running..."
kubectl --token="${TOKEN}" -n ${K8S_ENV} patch deployment narrative-reputation -p '{"spec":{"replicas": 0}}' || echo "No need to update needed..."
while kubectl --token="${TOKEN}" -n ${K8S_ENV} get pods | grep Terminating | grep narrative-reputation; do
  echo "Waiting for narrative-reputation containers to be terminated..."
  sleep 3
done

# Flush Redis, to clear out cached data
echo "Flushing Redis..."
REDIS_POD=$(kubectl --token="${TOKEN}" -n ${K8S_ENV} get pods | grep redis-interactive | head -n 1 | awk '{ print $1 }')
if [ "${K8S_ENV}" == "production" ]; then
  kubectl --token="${TOKEN}" -n ${K8S_ENV} exec ${REDIS_POD} -- /bin/bash -c 'redis-cli -n 0 -h 10.0.16.4 flushdb'
else
  kubectl --token="${TOKEN}" -n ${K8S_ENV} exec ${REDIS_POD} -- /bin/bash -c 'redis-cli -n 0 -h 10.0.0.4 flushdb'
fi

echo "stop-pods complete!"

