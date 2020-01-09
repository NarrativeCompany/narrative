# narrative-reputation deployment for staging

Requirements: 
- Requires the rep-application-staging-yaml configmap
- Requires the rep-application-secret-yaml secret
- Requires the cloud-sql-proxy secret

# Create the application properties and secrets

Create the staging configmap:
```
kubectl config use-context gke_staging-narrative_us-east1_staging-cluster
kubectl -n staging create configmap rep-application-staging-yaml --from-file=./application-staging.yaml
```

Get the `staging - rep-application-secret.yaml` file from 1Password. Create the secret:

```
kubectl -n staging create secret generic rep-application-secret-yaml --from-file=./application-secret.yaml
```
Remove `application-secret.yaml`.

# Creating the cloud-sql-proxy IAM user and secret

See narrative-core for configuring these credentials. They should already exist.

# Deploy

Set context:

```
kubectl config use-context gke_staging-narrative_us-east1_staging-cluster
```

Create the staging configmap (see above).


Create the staging secret (see above).

Deploy narrative-reputation:

```
kubectl apply -f narrative-reputation-deployment.yml
```
