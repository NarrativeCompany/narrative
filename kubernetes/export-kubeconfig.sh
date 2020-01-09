#!/bin/bash

set -e

. ./kubernetes/export-google-env.sh > /dev/null 2>&1

KUBECONFIG=$HOME/.kube/config.${GOOGLE_ENV}
export KUBECONFIG
echo "${KUBECONFIG}"
echo "${KUBECONFIG}" > KUBECONFIG.${GOOGLE_ENV}
