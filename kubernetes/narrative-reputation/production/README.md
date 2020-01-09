# narrative-reputation deployment for production

Requirements: 
- Requires the rep-application-production-yaml configmap
- Requires the rep-application-secret-yaml secret
- Requires the cloud-sql-proxy secret

# Create the application properties and secrets

Create the production configmap:
```
kubectl config use-context gke_production-narrative_us-central1_production-cluster
kubectl -n production create configmap rep-application-production-yaml --from-file=./application-production.yaml
```

Get the `production - rep-application-secret.yaml` file from 1Password. Create the secret:

```
kubectl -n production create secret generic rep-application-secret-yaml --from-file=./application-secret.yaml
```
Remove `application-secret.yaml`.

# Creating the cloud-sql-proxy IAM user and secret

See narrative-core for configuring these credentials. They should already exist.

# Deploy

Set context:

```
kubectl config use-context gke_production-narrative_us-central1_production-cluster
```

Create the production configmap (see above).


Create the production secret (see above).

Deploy narrative-reputation:

```
kubectl apply -f narrative-reputation-deployment.yml
```
