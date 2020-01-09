#!/bin/bash

CMD="${1}"
ARG="${2}"

function usage {
  echo "  Usage: $0 [CMD] [ARG]

  List images:
    $0 ls

  Make a snapshot of the current DBs and name it 'foo' (this WILL stop any running local-compose...):
    $0 cp foo

  Move the current DBs to 'foo' (this WILL stop any running local-compose...):
    $0 mv foo

  Use the snapshot named 'bar':
    $0 use bar

  Delete the snapshot named 'zed':
    $0 rm zed

  Delete the current, active snapshot:
    $0 rm_active

  Delete the current active snapshot and revert to the specified snapshot:
    $0 revert_to snapshot

"
  exit 1;
}

function parse {
  if [[ -z "${CMD}" ]]; then
    usage
    exit 1
  elif [[ ( ${CMD} == "-h" ) || ${CMD} == "--help" ]]; then
    usage
    exit 0
  elif [[ ( ${CMD} == "cp" ) && ${ARG} == "" ]]; then
    usage
    exit 1
  elif [[ ( ${CMD} == "mv" ) && ${ARG} == "" ]]; then
    usage
    exit 1
  elif [[ ( ${CMD} == "use" ) && ${ARG} == "" ]]; then
    usage
    exit 1
  elif [[ ( ${CMD} == "rm" ) && ${ARG} == "" ]]; then
    usage
    exit 1
  elif [[ ( ${CMD} == "revert_to" ) && ${ARG} == "" ]]; then
    usage
    exit 1
  fi
}

function check {
  if [[ ! -f local-compose.yml ]]; then
    echo "This script is meant to be run from the docker directory of the narrative-platform Git repository. Exiting..."
    exit 1
  fi
}

function check_exists {
  if ! is_in_list ${1}; then
    echo "Volume ${2} does not exist. Exiting..."
    exit 1
  fi
}

function docker_dir_list {
  prefix="docker_mysql_data"
  LIST=($(docker volume ls -q | grep ${prefix} | sed -E "s/${prefix}\-?/${1}/g" | sort))
}

function is_in_list {
  vol_prefix="volume-"
  docker_dir_list ${vol_prefix}
  for each in "${LIST[@]}"; do
    if [[ "${each}" == "${vol_prefix}${1}" ]]; then
      # bl: return 0 to indicate a success state since the arg was found in the list
      return 0
    fi
  done
  # bl: return 1 to indicate an error state since the arg was not found in the list
  return 1
}

function echo_list {
  echo "List of snapshots:"
  if is_in_list ""; then
    echo "  **ACTIVE**"
  fi

  docker_dir_list ""
  for each in "${LIST[@]}"; do
    echo "  ${each}"
  done
}

function clone_active {
  echo "Copying the active data to ${1} ..."
  ./docker_clone_volume.sh docker_mysql_data docker_mysql_data-${1}
  ./docker_clone_volume.sh docker_solr_data docker_solr_data-${1}
  echo "Copy complete!"
}

function stop_local_compose {
  echo "Stopping local-compose..."
  docker-compose -f local-compose.yml down > /dev/null 2>&1 && docker-compose -f local-compose.yml rm --force > /dev/null 2>&1
}

function docker_data_cp {
  if is_in_list ${ARG}; then
    read -p "Volume ${ARG} already exists! Do you wish to overwrite? Enter YES to continue: " res
    if [[ ( ${res} != "YES") ]]; then
      echo "Exiting..."
      exit 1
    fi
    docker_data_rm
  fi
  stop_local_compose
  clone_active "${ARG}"
  echo_list
}

function rm_volumes {
  docker volume rm docker_mysql_data${1} docker_solr_data${1}
}

function docker_data_rm {
  check_exists "${ARG}" "${ARG}"
  echo "Deleting ${ARG} volumes..."
  rm_volumes "-${ARG}"
  echo "Done!"
  echo_list
}

function docker_data_rm_active {
  check_exists "" "active"
  read -p "Are you sure you want to delete the active volumes? Enter YES to continue: " res
  if [[ ( ${res} != "YES") ]]; then
    echo "Exiting..."
    exit 1
  fi
  stop_local_compose
  echo "Deleting active volumes..."
  rm_volumes ""
  echo "Done!"
  echo_list
}

function docker_data_use {
  check_exists "${ARG}" "${ARG}"
  stop_local_compose

  docker volume inspect docker_mysql_data > /dev/null 2>&1

  if [[ "${?}" == "0" ]]; then
    echo "Making a backup of the active data ..."
    CUR_DATE=$(date '+%Y-%m-%d-%H-%M-%S')
    clone_active "${CUR_DATE}"
    echo "Backup complete."

    echo "Removing active volumes ..."
    rm_volumes ""
    echo "Active volumes removed ..."
  else
    echo "Active volume doesn't exist, so skipping backup ..."
  fi

  echo "Copying ${ARG} to active ..."
  ./docker_clone_volume.sh docker_mysql_data-${ARG} docker_mysql_data
  ./docker_clone_volume.sh docker_solr_data-${ARG} docker_solr_data
  echo "Copy complete!"

  echo_list
}

# main
parse
check
if [[ "${CMD}" == "ls" ]]; then
  echo_list
elif [[ "${CMD}" == "cp" ]]; then
  docker_data_cp
elif [[ "${CMD}" == "mv" ]]; then
  docker_data_cp
  docker_data_rm_active
elif [[ "${CMD}" == "rm" ]]; then
  docker_data_rm
elif [[ "${CMD}" == "rm_active" ]]; then
  docker_data_rm_active
elif [[ "${CMD}" == "revert_to" ]]; then
  docker_data_rm_active
  docker_data_use
elif [[ "${CMD}" == "use" ]]; then
  docker_data_use
else
  echo "Nothing happened. Probably a bug. Weird."
fi

