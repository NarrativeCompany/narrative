# narrative-core deployment for staging

Requirements: 
- Requires the narrative-web-front-end steps to be completed first
- Requires the application-staging-properties configmap
- Requires the application-secret-properties secret
- Requires the cloud-storage-credentials secret
- Requires the kyc-certification-files secret
- Requires the cloud-sql-proxy secret

# Create the application properties and secrets

Create the staging configmap:

```
kubectl config use-context gke_staging-narrative_us-east1_staging-cluster
kubectl -n staging create configmap application-staging-properties --from-file=./application-staging.properties
```

Get the `staging - application-secret.properties` file from 1Password. Create the secret:

```
kubectl -n staging create secret generic application-secret-properties --from-file=./application-secret.properties
```
Remove `application-secret.properties`.

Get the `narrative-core - narrative-ga-key.json` file from 1Password. Create the secret:

```
kubectl -n dev create secret generic google-analytics-api-key-json --from-file=./narrative-ga-key.json
```
Remove `narrative-ga-key.json`.

# Configure the cloud-storage-credentials

Get the `cloud-storage-credentials staging-images.json` secret (<https://console.cloud.google.com/iam-admin/serviceaccounts?organizationId=149049775531&project=staging-narrative>), the file can be found in 1Password. Create the file `staging-images.json`, then create the secret:

```
kubectl -n staging create secret generic cloud-storage-credentials --from-file=./staging-images.json
```
Remove `staging-images.json`.

# Configure the kyc-certification-files

Get the `kyc-certification-files staging-certification-files.json` secret (<https://console.cloud.google.com/iam-admin/serviceaccounts?organizationId=149049775531&project=staging-narrative>), the file can be found in 1Password. Create the file `staging-certification-files.json`, then create the secret:

```
kubectl -n staging create secret generic kyc-certification-files --from-file=./staging-certification-files.json
```
Remove `staging-certification-files.json`.

# Creating the cloud-sql-proxy IAM user and secret

Go to the Google Cloud UI - <https://console.cloud.google.com/iam-admin/iam?project=staging-narrative>

Click on "Service Accounts. Click "+ Create Service Account".

Service account name and service account ID should be `staging-sql`.  Project Role is "Cloud SQL Client". Check "Furnish a new private key". Click "Save".

The JSON keyfile will be downloaded, something like `staging-narrative-f0c86041bb33.json`. Rename this to `staging-sql.json`

Create the secret:

```
kubectl config use-context gke_staging-narrative_us-east1_staging-cluster
kubectl -n staging create secret generic cloud-sql-proxy --from-file=./staging-sql.json
```

Add the cloud-sql-proxy secret to 1Password - `cloud-sql-proxy - staging-sql service account JSON`.

Remove `staging-sql.json`.

# Deploy

Deploy narrative-core:

```
kubectl apply -f narrative-core-deployment.yml
kubectl apply -f narrative-core-service.yml
kubectl apply -f narrative-core-ingress.yml
```
