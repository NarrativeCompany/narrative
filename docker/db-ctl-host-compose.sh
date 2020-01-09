#!/bin/bash
set -e
DOCKER_DIR=${HOME}/.narrative
DOCKER_DATA=${DOCKER_DIR}/data
DOCKER_DEFAULT=${DOCKER_DIR}/default
CMD="${1}"
ARG="${2}"

function usage {
  echo "  Usage: $0 [CMD] [ARG]

  List images:
    $0 ls

  Make a snapshot of the current DBs and name it 'foo' (this WILL stop any running host-compose...):
    $0 cp foo

  Use the snapshot named 'bar':
    $0 use bar

  Delete the snapshot named 'zed':
    $0 rm zed

"
exit 1;
}

function parse {
  if [ -z "${CMD}" ]; then
    usage
    exit 1
  elif [[ ( ${CMD} == "-h" ) || ${CMD} == "--help" ]]; then
    usage
    exit 0
  elif [[ ( ${CMD} == "cp" ) && ${ARG} == "" ]]; then
    usage
    exit 1
  elif [[ ( ${CMD} == "use" ) && ${ARG} == "" ]]; then
    usage
    exit 1
  elif [[ ( ${CMD} == "rm" ) && ${ARG} == "" ]]; then
    usage
    exit 1
  fi
}

function check {
  if [ ! -f host-compose.yml ]; then
    echo "This script is meant to be run from the docker directory of the narrative-platform Git repository. Exiting..."
    exit 1
  fi
  if [ ! -d "${DOCKER_DIR}" ]; then
    echo "Warning - ${DOCKER_DIR} does not exist. Initializing..."
    MYDATE=$(date '+%Y-%m-%d-%H-%M-%S')
    mkdir -p ${DOCKER_DIR}/${MYDATE}
    ln -s ${DOCKER_DIR}/${MYDATE} ${DOCKER_DATA}
    echo "Creation of ${DOCKER_DATA} complete."
  fi
  if [ ! -L "${DOCKER_DATA}" ]; then
    echo "Warning - ${DOCKER_DATA} is not a symlink. Fixing..."
    MYDATE=$(date '+%Y-%m-%d-%H-%M-%S')
    mv ${DOCKER_DATA} ${DOCKER_DIR}/${MYDATE}
    ln -s ${DOCKER_DIR}/${MYDATE} ${DOCKER_DATA}
  fi
}

function docker_dir_list {
  LIST=$(find ${DOCKER_DIR} -type d -mindepth 1 -maxdepth 1 | awk -F'/' '{ print $NF }' | awk 'NF' | sort)
  ACTIVE=$(readlink ${DOCKER_DATA} | awk -F'/' '{ print $NF }')
}

function echo_list {
  echo "Active snapshot:"
  echo "  ${ACTIVE}"
  echo "List of snapshots:"
  for each in $LIST; do
    echo "  ${each}"
  done
}

function docker_data_cp {
  docker_dir_list
  echo "Stopping host-compose..."
  docker-compose -f host-compose.yml down > /dev/null 2>&1 && docker-compose -f host-compose.yml rm --force > /dev/null 2>&1
  echo "Making a copy of the active data: ${ACTIVE} ..."
  cp -R ${DOCKER_DIR}/${ACTIVE} ${DOCKER_DIR}/${ARG}
  echo "Copy complete!"
  docker_dir_list
  echo_list
}

function docker_data_rm {
  docker_dir_list
  LENGTH=$(echo "${LIST}" | wc -w | awk '{ print $NF }')
  if [ "${LENGTH}" == "1" ]; then
    echo "Only one directory left; there's nothing left to delete!"
    exit 0
  elif [ ! -d "${DOCKER_DIR}/${ARG}" ]; then
    echo "Directory ${ARG} does not exist. Exiting..."
    exit 1
  elif [ "${ARG}" == "${ACTIVE}" ]; then
    echo "Cannot delete the current active snapshot: ${ARG}"
    echo "Exiting..."
    exit 1
  else
    echo "Deleting ${ARG} ..."
    rm -rf ${DOCKER_DIR}/${ARG}
    echo "Done!"
    docker_dir_list
    echo_list
  fi
}

function docker_data_use {
  if [ ! -d "${DOCKER_DIR}/${ARG}" ]; then
    echo "Directory ${ARG} does not exist. Exiting..."
    exit 1
  else
    rm ${DOCKER_DATA}
    ln -s ${DOCKER_DIR}/${ARG} ${DOCKER_DATA}
    docker_dir_list
    echo_list
  fi
}

# main
parse
check
if [ "${CMD}" == "ls" ]; then
  docker_dir_list
  echo_list
elif [ "${CMD}" == "cp" ]; then
  docker_data_cp
elif [ "${CMD}" == "rm" ]; then
  docker_data_rm
elif [ "${CMD}" == "use" ]; then
  docker_data_use
else
  echo "Nothing happened. Probably a bug. Weird."
fi

