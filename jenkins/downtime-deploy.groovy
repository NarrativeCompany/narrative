def notifyStarted() {
  slackSend channel: "#jenkins",
  color: '#19e0f6',
  message: ":arrow_down: :kubernetes: *Running ${env.K8S_ENV} ${env.JOB_BASE_NAME} ...*\nJob: <${env.BUILD_URL}|${env.JOB_BASE_NAME} ${env.BUILD_DISPLAY_NAME}>\nBranch: ${env.GIT_BRANCH}\nJob has started by ${env.SLACK_USER} ... :narrative:"
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
                message: ":arrow_up: :kubernetes: *Completed ${env.K8S_ENV} ${env.JOB_BASE_NAME}. Downtime deployment complete.* :white_check_mark:\nJob: <${env.BUILD_URL}|${env.JOB_BASE_NAME} ${env.BUILD_DISPLAY_NAME}>\n\nSuccessfully deployed by ${env.SLACK_USER}.")
      slackSend (channel: "#jenkins",
                color: "good",
                message: "${env.K8S_ENV} Kubernetes deployed images:\nnarrativecompany/narrativecore:${DOCKER_TAG_DEPLOY}\nnarrativecompany/narrative-web-front-end:${DOCKER_TAG_DEPLOY}")
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
      JOB_NAME_STOP = sh(
          script: '''#!/bin/bash
            if [ "$ENVIRONMENT" == "production" ]; then
              echo "production-narrative-platform-stop-pods"
            else
              echo "narrative-platform-stop-pods"
            fi
          ''',
          returnStdout: true
          ).trim()
      JOB_NAME_START = sh(
          script: '''#!/bin/bash
            if [ "$ENVIRONMENT" == "production" ]; then
              echo "production-narrative-platform-start-pods-with-deploy"
            else
              echo "narrative-platform-start-pods-with-deploy"
            fi
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
    stage ('narrative-core-silence') {
      parallel {
        stage('hpa-silence') {
          steps {
            build job: "/alertmanager_silence", parameters: [[$class: 'StringParameterValue', name: 'GIT_BRANCH_ORIGIN', value: 'origin/master'], [$class: 'StringParameterValue', name: 'ALERTMANAGER', value: "${env.GOOGLE_ENV}"], [$class: 'StringParameterValue', name: 'SILENCE_LABEL', value: "alertname"], [$class: 'StringParameterValue', name: 'SILENCE_VALUE', value: "KubeHPAScaleUp"], [$class: 'StringParameterValue', name: 'SILENCE_DURATION', value: "15"], [$class: 'StringParameterValue', name: 'SLACK_NOTIFY', value: "no"]]
          }
        }
        stage('instance-silence') {
          steps {
            build job: "/alertmanager_silence", parameters: [[$class: 'StringParameterValue', name: 'GIT_BRANCH_ORIGIN', value: 'origin/master'], [$class: 'StringParameterValue', name: 'ALERTMANAGER', value: "${env.GOOGLE_ENV}"], [$class: 'StringParameterValue', name: 'SILENCE_LABEL', value: "instance"], [$class: 'StringParameterValue', name: 'SILENCE_VALUE', value: "${env.SITE_URL}"], [$class: 'StringParameterValue', name: 'SILENCE_DURATION', value: "15"], [$class: 'StringParameterValue', name: 'SLACK_NOTIFY', value: "no"]]
          }
        }
      }
    }
    stage ('stop-pods') {
      steps {
        build job: "${env.JOB_NAME_STOP}", parameters: [[$class: 'StringParameterValue', name: 'GIT_BRANCH_ORIGIN', value: GIT_BRANCH_ORIGIN], [$class: 'StringParameterValue', name: 'COMMIT_SHA', value: "${env.COMMIT_SHA}"], [$class: 'StringParameterValue', name: 'ENVIRONMENT', value: ENVIRONMENT], [$class: 'StringParameterValue', name: 'SLACK_USER', value: SLACK_USER]]
      }
    }
    stage ('start-pods') {
      steps {
        build job: "${env.JOB_NAME_START}", parameters: [[$class: 'StringParameterValue', name: 'GIT_BRANCH_ORIGIN', value: GIT_BRANCH_ORIGIN], [$class: 'StringParameterValue', name: 'COMMIT_SHA', value: "${env.COMMIT_SHA}"], [$class: 'StringParameterValue', name: 'DOCKER_TAG_DEPLOY', value: DOCKER_TAG_DEPLOY], [$class: 'StringParameterValue', name: 'REP_DOCKER_TAG_DEPLOY', value: REP_DOCKER_TAG_DEPLOY], [$class: 'StringParameterValue', name: 'ENVIRONMENT', value: ENVIRONMENT], [$class: 'StringParameterValue', name: 'SLACK_USER', value: SLACK_USER]]
      }
    }
    stage('k8s-image') {
      steps {
        withCredentials([
          [$class: 'UsernamePasswordMultiBinding', credentialsId: 'jenkins-docker-hub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD'],
          file(credentialsId: 'gcloud-jenkins-file', variable: 'GCLOUD_JENKINS_FILE')
        ]) 
          {
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
