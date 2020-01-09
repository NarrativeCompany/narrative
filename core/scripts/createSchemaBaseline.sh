#!/bin/bash

function err_report() {
  echo "errexit on line $(caller)" >&2
  popd
  exit -1
}

function askForValue()
{
    prompt=$1
    default=$2
    if [[  -z  $3 ]]; then
        read -p "$prompt [$default]: " res
    else
        read -s -p "$prompt [$default]: " res
    fi;

    echo "${res:-$default}"
}

trap err_report ERR

scriptDir=$(cd "$(dirname "$0")"; pwd)
pushd ${scriptDir} > /dev/null

res="$( which mysqldump )"
if [[  -z  "${res}" ]]; then
    echo Error - you must have mysqldump on your path
    exit -1
fi

baselineDir="$scriptDir/../src/main/resources/schema/baseline"

host=$(askForValue 'Please enter the MySQL host name' '127.0.0.1')
port=$(askForValue 'Please enter the MySQL port' '3306')
pass=$(askForValue 'Please enter the global_user password' 'password' 'true')
echo -e
echo -e
echo -e "Updating baseline in ${baselineDir}"

query="SELECT LOWER(partitionType), databaseName, userName, password FROM global.DBPartition group by partitionType, databaseName, userName, password"

mysql -B --column-names=0 -h${host} -uglobal_user -p${pass} -e "${query}" | while read -a row;
do
    partitionType="${row[0]}"
    databaseName="${row[1]}"
    user="${row[2]}"
    pass="${row[3]}"

    targetFile="$baselineDir/${partitionType}_baseline.sql"
    echo "Dumping database $databaseName..."
    mysqldump ${databaseName} --default-character-set=utf8mb4 --ignore-table global.UserReputation --ignore-table global.UserReputationHistory -u ${user} -p${pass} -h ${host} --port ${port} --no-data --single-transaction | sed 's/ AUTO_INCREMENT=[0-9]*//g' > ${targetFile}
done;

echo "Done."

echo -e

popd > /dev/null

exit 0
