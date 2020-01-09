// def notifyStarted() {
//   slackSend channel: "#jenkins",
//   color: '#19e0f6',
//   message: "Job: <${env.BUILD_URL}|${env.JOB_BASE_NAME} ${env.BUILD_DISPLAY_NAME}>\nBranch: ${env.GIT_BRANCH}\nJob has started by ${env.SLACK_USER} ... :narrative:"
// }

pipeline {

  agent { label 'gce' }

  options {
    skipStagesAfterUnstable()
  }
  post {
    // success {
    //   slackSend (channel: "#jenkins",
    //             color: "good",
    //             message: "Job: <${env.BUILD_URL}|${env.JOB_BASE_NAME} ${env.BUILD_DISPLAY_NAME}>\nBranch: ${env.GIT_BRANCH}\nDocker image: ${DOCKER_IMAGE}\nSuccessfully deployed to <${env.SITE_URL}> by ${env.SLACK_USER}.")
    //   slackSend (channel: "#jenkins",
    //             color: "good",
    //             message: "${env.K8S_ENV} Kubernetes deployed image:\n```${K8S_IMAGE}```")
    // }

    failure {
      slackSend channel: "#jenkins",
                color: "danger",
                message: "@here \nJob: <${env.BUILD_URL}|${env.JOB_BASE_NAME} ${env.BUILD_DISPLAY_NAME}>\nBranch: ${env.GIT_BRANCH}\nFAILED to deploy by ${env.SLACK_USER}."
    }

    unstable {
        slackSend channel: "#jenkins",
                color: "danger",
                message: "@here \nJob: <${env.BUILD_URL}|${env.JOB_BASE_NAME} ${env.BUILD_DISPLAY_NAME}>\nBranch: ${env.GIT_BRANCH}\nFAILED to deploy by ${env.SLACK_USER}."
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
  }

  stages {
    stage('code-checkout') { // for display purposes
      steps {
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
    stage('solr-configset1-deploy') {
      steps {
        withCredentials([
          file(credentialsId: 'gcloud-jenkins-file', variable: 'GCLOUD_JENKINS_FILE'),
          file(credentialsId: 'ansible_vault_ops', variable: 'ANSIBLE_VAULT_OPS'),
          file(credentialsId: 'prod_bot_ops_ssh_key', variable: 'PROD_BOT_OPS_SSH_KEY'),
          file(credentialsId: 'bot_ops_ssh_key', variable: 'BOT_OPS_SSH_KEY')
        ])
        {
          script {
            sh(
              script: '''#!/bin/bash
              set -e
              cd ansible/
              export ANSIBLE_VAULT_PASSWORD_FILE=${ANSIBLE_VAULT_OPS}
              if [ "${GOOGLE_ENV}" == "production" ]; then
                export ANSIBLE_PRIVATE_KEY_FILE=${PROD_BOT_OPS_SSH_KEY}
              else
                export ANSIBLE_PRIVATE_KEY_FILE=${BOT_OPS_SSH_KEY}
              fi
              export GCE_EMAIL=jenkins@informationtechnology-205813.iam.gserviceaccount.com
              export GCE_CREDENTIALS_FILE_PATH=${GCLOUD_JENKINS_FILE}
              export GCE_PROJECT=${GOOGLE_ENV}-narrative
              gcloud auth activate-service-account jenkins@informationtechnology-205813.iam.gserviceaccount.com --key-file=${GCLOUD_JENKINS_FILE}
              mkdir -p ${HOME}/.ssh
              export BOTO_CONFIG=/dev/null
              gsutil cp gs://narrative-devops/known_hosts ${HOME}/.ssh/known_hosts
              ansible-playbook -i inventory/${GOOGLE_ENV}-narrative solr-configset1-deploy.yml
            ''',
            )
          }
        }
      }
    }
  }
}
