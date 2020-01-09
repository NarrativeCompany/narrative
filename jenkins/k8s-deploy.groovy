def notifyStarted() {
  slackSend channel: "#jenkins",
  color: '#19e0f6',
  message: ":kubernetes: *${env.K8S_ENV}* Started <${env.BUILD_URL}|narrative-platform Kubernetes deploy... (${env.BUILD_DISPLAY_NAME})>\nParent Job: ${env.PARENT_JOB}\n\nJob started by ${env.SLACK_USER}..."
}

pipeline {

  agent { label 'gce' }

  options {
    skipStagesAfterUnstable()
  }
  post {
    success {
      slackSend (channel: "#jenkins",
                color: "good",
                // message: "Job: <${env.BUILD_URL}|${env.JOB_BASE_NAME} ${env.BUILD_DISPLAY_NAME}>\nRepository: <${env.HTML_URL}>\nBranch: ${env.GIT_BRANCH}\nDocker image: ${DOCKER_IMAGE}\nSuccessfully deployed to <${env.SITE_URL}> by ${env.SLACK_USER}.")
                message: ":kubernetes: *${env.K8S_ENV}* SUCCESS <${env.BUILD_URL}|narrative-platform Kubernetes deployed! (${env.BUILD_DISPLAY_NAME})> :white_check_mark:\nParent Job: ${env.PARENT_JOB}\n\nSuccessfully built by ${env.SLACK_USER}.")
      slackSend (channel: "#jenkins",
                color: "good",
                message: "${env.K8S_ENV} Kubernetes deployed images:\nnarrativecompany/narrativecore:${DOCKER_TAG_DEPLOY}\nnarrativecompany/narrative-web-front-end:${DOCKER_TAG_DEPLOY}")
      slackSend (channel: "#jenkins",
                color: "good",
                message: "${env.K8S_ENV} Kubernetes pods:\n```  ${K8S_IMAGE}```")
    }

    failure {
      slackSend channel: "#jenkins",
                color: "danger",
                message: "@here *${env.K8S_ENV}* FAILED - narrative-platform Kubernetes deploy FAILED. :red_circle:\nJob: <${env.BUILD_URL}|${env.JOB_BASE_NAME} ${env.BUILD_DISPLAY_NAME}>\nParent Job: ${env.PARENT_JOB}\n\nFAILED to deploy to <${env.SITE_URL}> by ${env.SLACK_USER}."
    }

    unstable {
        slackSend channel: "#jenkins",
                color: "danger",
                message: "@here *${env.K8S_ENV}* FAILED - narrative-platform Kubernetes deploy FAILED. :red_circle:\nJob: <${env.BUILD_URL}|${env.JOB_BASE_NAME} ${env.BUILD_DISPLAY_NAME}>\nParent Job: ${env.PARENT_JOB}\n\nFAILED to deploy to <${env.SITE_URL}> by ${env.SLACK_USER}."
    }
  }
  environment {
      COMMIT_SHA = sh(
          script: '''#!/bin/bash
            if [ "$COMMIT_SHA" == "auto" ]; then
              echo $GIT_BRANCH_ORIGIN
            else
              echo $COMMIT_SHA
            fi
          ''',
          returnStdout: true
          ).trim()
      GIT_BRANCH = sh(
          script: '''
            echo $GIT_BRANCH_ORIGIN | awk -F'/' '{ print $NF }'
          ''',
          returnStdout: true
          ).trim()
      K8S_ENV = sh(
          script: '''#!/bin/bash
            . ./kubernetes/export-k8s-env.sh
          ''',
          returnStdout: true
          ).trim()
      GOOGLE_ENV = sh(
          script: '''#!/bin/bash
            . ./kubernetes/export-google-env.sh
          ''',
          returnStdout: true
          ).trim()
      KUBECONFIG = sh(
          script: '''#!/bin/bash
            . ./kubernetes/export-kubeconfig.sh
          ''',
          returnStdout: true
          ).trim()
      PARENT_JOB = sh(
          script: '''#!/bin/bash
            if [ "$PARENT_JOB" == "self" ]; then
              echo "<${BUILD_URL}|self>"
            else
              echo "${PARENT_JOB}"
            fi
          ''',
          returnStdout: true
          ).trim()
      SITE_URL = "https://${env.K8S_ENV}.narrative.org"
  }

  stages {
    stage('code-checkout') { // for display purposes
      steps {
        notifyStarted()
         checkout scm: [
             $class: 'GitSCM',
             branches: [[name: "${env.COMMIT_SHA}"]],
             doGenerateSubmoduleConfigurations: false,
             submoduleCfg: [],
             userRemoteConfigs: [[credentialsId: 'jenkins-narrative-platform',
                                  url: 'git@github.com:NarrativeCompany/narrative-platform.git']]
         ]
      }
    }
    stage ('narrative-core-hpa-silence') {
      steps {
        build job: "/alertmanager_silence", parameters: [[$class: 'StringParameterValue', name: 'GIT_BRANCH_ORIGIN', value: 'origin/master'], [$class: 'StringParameterValue', name: 'ALERTMANAGER', value: "${env.GOOGLE_ENV}"], [$class: 'StringParameterValue', name: 'SILENCE_LABEL', value: "alertname"], [$class: 'StringParameterValue', name: 'SILENCE_VALUE', value: "KubeHPAScaleUp"], [$class: 'StringParameterValue', name: 'SILENCE_DURATION', value: "15"], [$class: 'StringParameterValue', name: 'SLACK_NOTIFY', value: "no"]]
      }
    }
    stage('kubernetes-deploy') {
      steps {
        withCredentials([
          [$class: 'UsernamePasswordMultiBinding', credentialsId: 'jenkins-docker-hub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD'],
          file(credentialsId: 'gcloud-jenkins-file', variable: 'GCLOUD_JENKINS_FILE')
        ]) 
          {
          sh '''#!/bin/bash
            # exit if anything fails and log all commands
            set -e
            echo "REPUTATION DEPLOY..."
            ./kubernetes/rep-deployment.sh
            echo "BLUE-GREEN UPTIME DEPLOY..."
            DEPLOY_TYPE='blue-green' ./kubernetes/launcher.sh
          ''' 
          script {
            K8S_IMAGE = sh(
              script: '''#!/bin/bash
              cat ./DEPLOYED_IMAGES
            ''',
            returnStdout: true
            ).trim()
          }
          script {
            DOCKER_TAG_DEPLOY = sh(
              script: '''#!/bin/bash
              cat ./DOCKER_TAG_DEPLOY
            ''',
            returnStdout: true
            ).trim()
          }
          sh '''#!/bin/bash
            # exit if anything fails and log all commands
            set -ex
            # Log out of Google Cloud
            gcloud auth revoke --all > /dev/null 2>&1 || echo "No accounts to revoke..."
          '''
        }
      }
    }
  }
}
