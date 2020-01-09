#!/bin/bash

echo "********************************************************"
echo "*** This script will set up a MySQL proxy for one    ***"
echo "*** of your configured CloudSQL projects             ***" 
echo "********************************************************"

port=${1}
if [[ -z ${port} ]]; then
  port=3307
fi

# Create a list of configured projects to display
projectList=($(gcloud config configurations list| tail -n +2 | awk '{print $4}'))

echo "Enter the number of the project you would like to proxy or 'q' to quit:"
select project in ${projectList[@]}; do
   if [[ -z "${project}" ]]; then
      exit -1
   fi
   
   # Sure would be nice if everyone had Bash > 4.0 - would be even better if there
   # were a dynamic way to find the CloudSQL instance descriptor for the project selected
   if [[ "${project}" == "sandbox-narrative" ]]; then
      desc='sandbox-narrative:us-east1:dev-master'
   elif [[ "${project}" == "staging-narrative" ]]; then
      desc='staging-narrative:us-east1:staging-master'
   elif [[ "${project}" == "production-narrative" ]]; then
      desc='production-narrative:us-central1:production-master'
   else 
      echo "Undefined Cloud SQL descriptor for ${project}"
      exit -1
   fi
   
   break;
done
echo -e "\n"

if [[ ! -f /tmp/cloud_sql_proxy ]]; then
  echo "Downloading latest cloud_sql_proxy and starting..."
  curl -o /tmp/cloud_sql_proxy https://dl.google.com/cloudsql/cloud_sql_proxy.darwin.amd64

  chmod +x /tmp/cloud_sql_proxy
fi

/tmp/cloud_sql_proxy -instances=${desc}=tcp:${port}
