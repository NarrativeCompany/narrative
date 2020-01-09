# Configuring the Jenkins Deployment Service Account

This allows for Jenkins to deploy to sandbox-cluster.

If the Jenkins user doesn't exist (note the project is the `informationtechnology` project):

```
gcloud config configurations activate informationtechnology
gcloud iam service-accounts create jenkins --display-name "Jenkins service account."
gcloud iam service-accounts keys create ~/.config/gcloud/jenkins.json --iam-account jenkins@informationtechnology-205813.iam.gserviceaccount.com
```

Save the `jenkins.json` file in 1Password.

Give the service account access to the

Standbox cluster:
```
gcloud projects add-iam-policy-binding sandbox-narrative --role=roles/container.viewer --member=serviceAccount:jenkins@informationtechnology-205813.iam.gserviceaccount.com
```
Staging cluster:
```
gcloud projects add-iam-policy-binding staging-narrative --role=roles/container.viewer --member=serviceAccount:jenkins@informationtechnology-205813.iam.gserviceaccount.com
```

Check the available contexts:
```
kubectl config get-contexts
```

For sandbox, you need the context `gke_sandbox-narrative_us-east1_sandbox-cluster`
For staging, you need the context `gke_staging-narrative_us-east1_staging-cluster`

If the context you need is not on the list, enter (for staging):

```
gcloud beta container clusters get-credentials staging-cluster --region us-east1 --project staging-narrative
```

Now you can apply the cluster role to the Jenkins account:

Select the cluster:
Sandbox
```
kubectl config use-context gke_sandbox-narrative_us-east1_sandbox-cluster
```
Staging
```
kubectl config use-context gke_staging-narrative_us-east1_staging-cluster
```

And now apply the role:
```
kubectl create -f role.yml
```

# Deploy Key

In order for Jenkins to pull the Git repository, add the Jenkins SSH deploy key to the GitHub repo keys.

Create the SSH key and save in 1Password (should already exist as 'jenkins ssh key - narrative-platform repository').
