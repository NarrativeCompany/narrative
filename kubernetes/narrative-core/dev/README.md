# narrative-core deployment for dev

Requirements: 
- Requires the narrative-web-front-end steps to be completed first
- Requires the application-dev-properties configmap
- Requires the application-secret-properties secret
- Requires the google-analytics-api-key-json secret
- Requires the cloud-storage-credentials secret
- Requires the kyc-certification-files secret
- Requires the cloud-sql-proxy secret

# Create the application properties and secrets

Create the dev configmap:

```
kubectl config use-context gke_sandbox-narrative_us-east1_sandbox-cluster
kubectl -n dev create configmap application-dev-properties --from-file=./application-dev.properties
```

Get the `dev - application-secret.properties` file from 1Password. Create the secret:

```
kubectl -n dev create secret generic application-secret-properties --from-file=./application-secret.properties
```
Remove `application-secret.properties`.

Get the `narrative-core - narrative-ga-key.json` file from 1Password. Create the secret:

```
kubectl -n dev create secret generic google-analytics-api-key-json --from-file=./narrative-ga-key.json
```
Remove `narrative-ga-key.json`.

# Configure the cloud-storage-credentials

Get the `cloud-storage-credentials dev-images.json` secret (<https://console.cloud.google.com/iam-admin/serviceaccounts?organizationId=149049775531&project=sandbox-narrative>), the file can be found in 1Password. Create the file `dev-images.json`, then create the secret:

```
kubectl -n dev create secret generic cloud-storage-credentials --from-file=./dev-images.json
```
Remove `dev-images.json`.

# Configure the kyc-certification-files

Get the `kyc-certification-files dev-certification-files.json` secret (<https://console.cloud.google.com/iam-admin/serviceaccounts?organizationId=149049775531&project=sandbox-narrative>), the file can be found in 1Password. Create the file `dev-certification-files.json`, then create the secret:

```
kubectl -n dev create secret generic kyc-certification-files --from-file=./dev-certification-files.json
```
Remove `dev-certification-files.json`.

# Creating the cloud-sql-proxy IAM user and secret

Go to the Google Cloud UI - <https://console.cloud.google.com/iam-admin/iam?project=sandbox-narrative>

Click on "Service Accounts. Click "+ Create Service Account".

Service account name and service account ID should be `sandbox-sql`.  Project Role is "Cloud SQL Client". Check "Furnish a new private key". Click "Save".

The JSON keyfile will be downloaded, something like `sandbox-narrative-f0c86041bb33.json`. Rename this to `sandbox-sql.json`

Create the secret:

```
kubectl config use-context gke_sandbox-narrative_us-east1_sandbox-cluster
kubectl -n dev create secret generic cloud-sql-proxy --from-file=./sandbox-sql.json
```

Add the cloud-sql-proxy secret to 1Password - `cloud-sql-proxy - sandbox-sql service account JSON`.

Remove `sandbox-sql.json`.

# Deploy

Deploy narrative-core:

```
kubectl apply -f narrative-core-deployment.yml
kubectl apply -f narrative-core-service.yml
kubectl apply -f narrative-core-ingress.yml
```
