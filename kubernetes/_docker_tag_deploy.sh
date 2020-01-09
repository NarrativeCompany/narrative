# Test for the Docker image...
# check narrative-core
DOCKER_TAG_LIST=$(curl -s -u ${USERNAME}:${PASSWORD} https://registry.hub.docker.com/v1/repositories/narrativecompany/narrative-core/tags |jq -r '.[].name' | grep -E "^${DOCKER_TAG_DEPLOY}$" || echo "")
if [ "${DOCKER_TAG_DEPLOY}" != "${DOCKER_TAG_LIST}" ]; then
  echo "Docker tag not found in narrative-core repository. Exiting..."
  echo "DOCKER_TAG_DEPLOY: ${DOCKER_TAG_DEPLOY}"
  echo "DOCKER_TAG_LIST: ${DOCKER_TAG_LIST}"
  exit 1
fi

# check narrative-web-front-end
DOCKER_TAG_LIST=$(curl -s -u ${USERNAME}:${PASSWORD} https://registry.hub.docker.com/v1/repositories/narrativecompany/narrative-web-front-end/tags |jq -r '.[].name' | grep -E "^${DOCKER_TAG_DEPLOY}$" || echo "")
if [ "${DOCKER_TAG_DEPLOY}" != "${DOCKER_TAG_LIST}" ]; then
  echo "Docker tag not found in narrative-web-front-end repository. Exiting..."
  echo "DOCKER_TAG_DEPLOY: ${DOCKER_TAG_DEPLOY}"
  echo "DOCKER_TAG_LIST: ${DOCKER_TAG_LIST}"
  exit 1
fi

echo "Docker image:           ${DOCKER_TAG_DEPLOY}"