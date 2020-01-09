#!/bin/bash
set -e

which jq || ( apt-get update && apt-get -y install jq )

echo "wait for zk-01..."
until /opt/solr/server/scripts/cloud-scripts/zkcli.sh -zkhost zk-01:2181 -cmd ls /zookeeper/config 2>&1 ; do
  echo "waiting... zk..."
  sleep 3
done

echo "Pausing 5 seconds before continuing..."
sleep 5

echo "wait for solr-01..."
until curl -s 'http://solr-01:8983/solr/admin/collections?action=LIST' 2>&1 ; do
  echo "waiting... solr..."
  sleep 3
done

echo "wait for solr-02..."
until curl -s 'http://solr-02:8984/solr/admin/collections?action=LIST' 2>&1 ; do
  echo "waiting... solr..."
  sleep 3
done

echo "wait for solr-03..."
until curl -s 'http://solr-03:8985/solr/admin/collections?action=LIST' 2>&1 ; do
  echo "waiting... solr..."
  sleep 3
done

if /opt/solr/server/scripts/cloud-scripts/zkcli.sh -zkhost zk-01:2181 -cmd ls /configs | grep configset1 ; then
  echo "configset1 exists!"
else
  /opt/solr/server/scripts/cloud-scripts/zkcli.sh -zkhost zk-01:2181 -cmd upconfig -confname configset1 -confdir /opt/configset1/
fi

if curl -s 'http://solr-01:8983/solr/admin/collections?action=LIST' | jq -r '.collections[] | select(. == ("narrative-platform"))' | grep narrative-platform ; then
  echo "FOUND!"
else
  echo "Not found. CREATING..."
  curl 'http://solr-01:8983/solr/admin/collections?action=CREATE&name=narrative-platform&numShards=3&replicationFactor=3&maxShardsPerNode=-1&collection.configName=configset1'
fi
