# Overview

This is the documentation and Kubernetes yaml files to verify functionality between the sandbox-cluster and Cloud SQL. It configures the Google Cloud SQL proxy.

# Deploy

To Create the Cloud SQL Proxy Kubernetes secret, first get the dev and sandbox cloud-sql-proxy secret from 1Password (`sandbox-cluster cloud-sql-proxy secret`):

```
kubectl config use-context gke_sandbox-narrative_us-east1_sandbox-cluster
kubectl apply -f dev-cloud-sql-proxy.yml
kubectl apply -f staging-cloud-sql-proxy.yml
```

Delete the `dev-cloud-sql-proxy.yml` and `stagin-cloud-sql-proxy.yml` files.


Create the Cloud SQL Proxy deployment:

```
kubectl apploy -f cloud-sql-proxy-deployment.yml
```

Then verify via `kubectl exec`. Example:

```
➜  cloud_sql_proxy_demo git:(mysql_init_infrastructure_44) ✗ kubectl -n dev exec -i -t cloud-sql-proxy-67b565765b-6ltfq /bin/sh
/ $ telnet localhost:3306
U
5.7.14-google-log�#um 0Fe!n;&,b/&=0mysql_native_password

Connection closed by foreign host
/ $
```

Note that we established a connection to the Cloud SQL instance!

