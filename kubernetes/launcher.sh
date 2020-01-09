set -e

# bl: we only need to get docker tag info if we are deploying. if we're stopping, then we don't need it
if [ "${DEPLOY_TYPE}" != "stop" ]; then
  # if auto deploy, find the latest version based on ./docker/export-narrative-build-vars.sh deploy
  if [ "${DOCKER_TAG_DEPLOY}" == "" ]; then
    echo "DOCKER_TAG_DEPLOY not defined. Finding tag based on ./docker/export-narrative-build-vars.sh deploy..."
    # we LOCK to narrative-core version number
    . ./docker/export-narrative-build-vars.sh deploy
  else
    # Safe/easy way to line up an auto deploy and Docker tag deploy so that we can Slack notify the tag
    echo "${DOCKER_TAG_DEPLOY}" > ./DOCKER_TAG_DEPLOY
  fi

  . ./kubernetes/_docker_tag_deploy.sh
fi

# Get blue/green
. ./kubernetes/_deployment_information.sh

# Conditional time
if [ "${DEPLOY_TYPE}" == "blue-green" ]; then
  . ./kubernetes/blue-green-deployment.sh
elif [ "${DEPLOY_TYPE}" == "stop" ]; then
  echo "STOP-PODS.SH SCRIPT..."
  . ./kubernetes/stop-pods.sh
elif [ "${DEPLOY_TYPE}" == "start" ]; then
  echo "Running the rep-deployment script... (running ./kubernetes/rep-deployment.sh)"
  ./kubernetes/rep-deployment.sh
  echo "START-PODS.SH SCRIPT..."
  . ./kubernetes/start-pods-with-deploy.sh
else
  echo "PANIC! DIDN'T FIND DEPLOY_TYPE VARIABLE. Exiting..."
  exit 1
fi