pipeline {

  agent { label 'gce' }

  options {
    skipStagesAfterUnstable()
  }
  environment {
      PARENT_JOB = "<${env.BUILD_URL}|${env.JOB_BASE_NAME} ${env.BUILD_DISPLAY_NAME}>"
  }
  stages {
    stage ('narrative-platform-docker-build') {
      steps {
        build job: "/narrative-platform-docker-build", parameters: [[$class: 'StringParameterValue', name: 'GIT_BRANCH_ORIGIN', value: GIT_BRANCH_ORIGIN], [$class: 'StringParameterValue', name: 'COMMIT_SHA', value: "${env.COMMIT_SHA}"], [$class: 'StringParameterValue', name: 'PARENT_JOB', value: "${env.PARENT_JOB}"], [$class: 'StringParameterValue', name: 'SLACK_USER', value: SLACK_USER]]
      }
    }
    stage ('/narrative-platform-downtime-deploy') {
      steps {
        build job: "/narrative-platform-downtime-deploy", parameters: [[$class: 'StringParameterValue', name: 'GIT_BRANCH_ORIGIN', value: GIT_BRANCH_ORIGIN], [$class: 'StringParameterValue', name: 'COMMIT_SHA', value: "${env.COMMIT_SHA}"], [$class: 'StringParameterValue', name: 'ENVIRONMENT', value: "dev"], [$class: 'StringParameterValue', name: 'SLACK_USER', value: SLACK_USER]]
      }
    }
    stage ('narrative-platform-solr-configset1-deploy') {
      steps {
        build job: "/narrative-platform-solr-configset1-deploy", parameters: [[$class: 'StringParameterValue', name: 'GIT_BRANCH_ORIGIN', value: GIT_BRANCH_ORIGIN], [$class: 'StringParameterValue', name: 'SLACK_USER', value: SLACK_USER]]
      }
    }
  }
}

