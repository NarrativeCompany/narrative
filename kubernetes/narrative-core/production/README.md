# narrative-core deployment for production

Requirements: 
- Requires the narrative-web-front-end steps to be completed first
- Requires the application-production-properties configmap
- Requires the application-secret-properties secret
- Requires the google-analytics-api-key-json secret
- Requires the cloud-storage-credentials secret
- Requires the kyc-certification-files secret
- Requires the cloud-sql-proxy secret

# Create the application properties and secrets

Create the production configmap:

```
kubectl config use-context gke_production-narrative_us-central1_production-cluster
kubectl -n production create configmap application-production-properties --from-file=./application-production.properties
```

Get the `production application-secret.properties` file from 1Password. Create the secret:

```
kubectl -n production create secret generic application-secret-properties --from-file=./application-secret.properties
```
Remove `application-secret.properties`.

Get the `narrative-core - narrative-ga-key.json` file from 1Password. Create the secret:

```
kubectl -n dev create secret generic google-analytics-api-key-json --from-file=./narrative-ga-key.json
```
Remove `narrative-ga-key.json`.

# Configure the cloud-storage-credentials

Get the `cloud-storage-credentials production-images.json` secret (<https://console.cloud.google.com/iam-admin/serviceaccounts?organizationId=149049775531&project=production-narrative>), the file can be found in 1Password. Create the file `production-images.json`, then create the secret:

```
kubectl -n production create secret generic cloud-storage-credentials --from-file=./production-images.json
```
Remove `production-images.json`.

# Configure the kyc-certification-files

Get the `kyc-certification-files production-certification-files.json` secret (<https://console.cloud.google.com/iam-admin/serviceaccounts?organizationId=149049775531&project=production-narrative>), the file can be found in 1Password. Create the file `production-certification-files.json`, then create the secret:

```
kubectl -n production create secret generic kyc-certification-files --from-file=./production-certification-files.json
```
Remove `production-certification-files.json`.

# Creating the cloud-sql-proxy IAM user and secret

Go to the Google Cloud UI - <https://console.cloud.google.com/iam-admin/iam?folder=&organizationId=149049775531&project=production-narrative>

Click on "Service Accounts. Click "+ Create Service Account".

Service account name and service account ID should be `production-sql`.  Project Role is "Cloud SQL Client". Check "Furnish a new private key". Click "Save".

The JSON keyfile will be downloaded, something like `production-narrative-f0c86041bb33.json`. Rename this to `production-sql.json`

Create the secret:

```
kubectl config use-context gke_production-narrative_us-central1_production-cluster
kubectl -n production create secret generic cloud-sql-proxy --from-file=./production-sql.json
```

Add the cloud-sql-proxy secret to 1Password - `cloud-sql-proxy - production-sql service account JSON`.

Remove `production-sql.json`.

# Deploy

Deploy narrative-core:

```
kubectl apply -f narrative-core-deployment.yml
kubectl apply -f narrative-core-service.yml
kubectl apply -f narrative-core-ingress.yml
```
