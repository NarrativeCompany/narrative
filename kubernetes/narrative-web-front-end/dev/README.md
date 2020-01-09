# narrative-web-front-end deployment for dev

Refer to `../jenkins.md` for configuring the Jenkins Deployment Service Account.

To deploy the narrative web front end for dev:

```
kubectl apply -f narrative-web-front-end-deployment.yml
kubectl apply -f narrative-web-front-end-service.yml
kubectl apply -f narrative-web-front-end-ingress.yml
```

Configure the oauth settings (<https://console.cloud.google.com/apis/credentials/oauthclient/1026630561657-4kkm1aoe71ldcvp4rkd886ghjgqemv8a.apps.googleusercontent.com?project=operations-204322&organizationId=149049775531>). This requires two redirects for each site:

```
https://dev.narrative.org
https://dev.narrative.org/oauth2/callback
```

Configure Cloudflare DNS to point to the `sandbox-narrative` GKE cluster ingress (<https://console.cloud.google.com/kubernetes/list?project=sandbox-narrative&organizationId=149049775531>). Set and A record for `dev.narrative.org`. 

