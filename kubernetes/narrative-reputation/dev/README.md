# narrative-reputation deployment for dev

Requirements: 
- Requires the rep-application-dev-yaml configmap
- Requires the rep-application-secret-yaml secret
- Requires the cloud-sql-proxy secret

# Create the application properties and secrets

Create the dev configmap:
```
kubectl config use-context gke_sandbox-narrative_us-east1_sandbox-cluster
kubectl -n dev create configmap rep-application-dev-yaml --from-file=./application-dev.yaml
```

Get the `dev - rep-application-secret.yaml` file from 1Password. Create the secret:

```
kubectl -n dev create secret generic rep-application-secret-yaml --from-file=./application-secret.yaml
```
Remove `application-secret.yaml`.

# Creating the cloud-sql-proxy IAM user and secret

See narrative-core for configuring these credentials. They should already exist.

# Deploy

Set context:

```
kubectl config use-context gke_sandbox-narrative_us-east1_sandbox-cluster
```

Create the dev configmap (see above).


Create the dev secret (see above).

Deploy narrative-reputation:

```
kubectl apply -f narrative-reputation-deployment.yml
```
