def notifyStarted() {
  slackSend channel: "#jenkins",
  color: '#19e0f6',
  message: ":arrow_up: :kubernetes: *Running ${env.K8S_ENV} ${env.JOB_BASE_NAME} ...*\nJob: <${env.BUILD_URL}|${env.JOB_BASE_NAME} ${env.BUILD_DISPLAY_NAME}>\nBranch: ${env.GIT_BRANCH}\nJob has started by ${env.SLACK_USER} ... :narrative:"
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
                message: ":arrow_up: :kubernetes: *Completed ${env.K8S_ENV} ${env.JOB_BASE_NAME}. The system is back up* :white_check_mark:\nJob: <${env.BUILD_URL}|${env.JOB_BASE_NAME} ${env.BUILD_DISPLAY_NAME}>\n\nSuccessfully deployed by ${env.SLACK_USER}.")
    }

    failure {
      slackSend channel: "#jenkins",
                color: "danger",
                message: ":arrow_down: :kubernetes: *FAILED ${env.K8S_ENV} ${env.JOB_BASE_NAME}.* :red_circle: @here\nJob: <${env.BUILD_URL}|${env.JOB_BASE_NAME} ${env.BUILD_DISPLAY_NAME}>\nBranch: ${env.GIT_BRANCH}\nFAILED to deploy to <${env.SITE_URL}> by ${env.SLACK_USER}."
    }

    unstable {
        slackSend channel: "#jenkins",
                color: "danger",
                message: ":arrow_down: :kubernetes: *FAILED ${env.K8S_ENV} ${env.JOB_BASE_NAME}.* :red_circle: @here\nJob: <${env.BUILD_URL}|${env.JOB_BASE_NAME} ${env.BUILD_DISPLAY_NAME}>\nBranch: ${env.GIT_BRANCH}\nFAILED to deploy to <${env.SITE_URL}> by ${env.SLACK_USER}."
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
      SITE_URL = sh(
          script: '''#!/bin/bash
            if [ "$ENVIRONMENT" == "production" ]; then
              echo "https://www.narrative.org"
            else
              echo "https://${ENVIRONMENT}.narrative.org"
            fi
          ''',
          returnStdout: true
          ).trim()
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
    stage('kubernetes-start-pods-with-deploy') {
      steps {
        withCredentials([
          [$class: 'UsernamePasswordMultiBinding', credentialsId: 'jenkins-docker-hub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD'],
          file(credentialsId: 'gcloud-jenkins-file', variable: 'GCLOUD_JENKINS_FILE')
        ]) 
          {
          sh '''#!/bin/bash
            # exit if anything fails and log all commands
            set -e
            DEPLOY_TYPE='start' ./kubernetes/launcher.sh
          ''' 
          sh '''#!/bin/bash
            # exit if anything fails and log all commands
            set -ex
            # Log out of Google Cloud
            gcloud auth revoke --all || echo "No accounts to revoke..."
          '''
        }
      }
    }
  }
}
