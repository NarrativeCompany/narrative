def notifyStarted() {
  slackSend channel: "#jenkins",
  color: '#19e0f6',
  message: ":docker: Started <${env.BUILD_URL}|narrative-platform Docker image build... (${env.BUILD_DISPLAY_NAME})>\nParent Job: ${env.PARENT_JOB}\n\n Job started by ${env.SLACK_USER}..."
}

pipeline {

  agent { label 'build' }

  options {
    skipStagesAfterUnstable()
  }
  post {
    success {
      slackSend (channel: "#jenkins",
                color: "good",
                message: ":docker: SUCCESS <${env.BUILD_URL}|narrative-platform Docker images created! (${env.BUILD_DISPLAY_NAME})> :white_check_mark:\nParent Job: ${env.PARENT_JOB}\n\nDocker images:\n${DOCKER_IMAGE}\n\nSuccessfully built by ${env.SLACK_USER}.")
    }
    failure {
      slackSend channel: "#jenkins",
                color: "danger",
                message: "@here FAILED - narrative-platform Docker image build FAILED. :red_circle:\nJob: <${env.BUILD_URL}|${env.JOB_BASE_NAME} ${env.BUILD_DISPLAY_NAME}>\nParent Job: ${env.PARENT_JOB}\n\nFailed to build by ${env.SLACK_USER}."
    }
    unstable {
        slackSend channel: "#jenkins",
                color: "danger",
                message: "@here FAILED - narrative-platform Docker image build FAILED. :red_circle:\nJob: <${env.BUILD_URL}|${env.JOB_BASE_NAME} ${env.BUILD_DISPLAY_NAME}>\nParent Job: ${env.PARENT_JOB}\n\nFailed to build by ${env.SLACK_USER}."
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
      PARENT_JOB = sh(
          script: '''#!/bin/bash
            if [ "$PARENT_JOB" == "self" ]; then
              echo "<${BUILD_URL}|${JOB_BASE_NAME} ${BUILD_DISPLAY_NAME}>"
            else
              echo "${PARENT_JOB}"
            fi
          ''',
          returnStdout: true
          ).trim()
  }
  stages {
    stage('code-checkout') {
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
    stage('docker-build') {
      steps {
        withCredentials([
          [$class: 'UsernamePasswordMultiBinding', credentialsId: 'jenkins-docker-hub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD'],
          file(credentialsId: 'gcloud-jenkins-file', variable: 'GCLOUD_JENKINS_FILE')
        ])
        {
          sh '''#!/bin/bash
            set -e
            echo "Building narrative-web-front-end..."
            . ./docker/export-narrative-build-vars.sh
            ./docker/build-narrative-web-front-end.sh
            echo "Building narrative-core and narrative-reputation..."
            . ./docker/export-rep-build-vars.sh
            ./docker/build-narrative-java.sh
          '''
        }
      }
    }
    stage('docker-report') {
      steps {
          script {
            DOCKER_IMAGE = sh(
              script: '''#!/bin/bash
              DOCKER_TAG=$(cat ./DOCKER_TAG)
              REP_DOCKER_TAG=$(cat ./REP_DOCKER_TAG)
              echo "narrative-web-front-end:${DOCKER_TAG}\nnarrative-core:${DOCKER_TAG}\nnarrative-reputation:${REP_DOCKER_TAG}"
            ''',
            returnStdout: true
            ).trim()
        }

      }
    }
  }
}

