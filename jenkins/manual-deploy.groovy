pipeline {

  agent { label 'gce' }

  options {
    skipStagesAfterUnstable()
  }
  environment {
      GIT_BRANCH = sh(
          script: '''
            echo $GIT_BRANCH_ORIGIN | awk -F'/' '{ print $NF }'
          ''',
          returnStdout: true
          ).trim()
  }
  stages {
    stage ('cloudflare-enable-development-mode') {
      when {
        expression {
          ENVIRONMENT == 'staging' ||
          ENVIRONMENT == 'production'
        }
      }
      steps {
        build job: "/cloudflare-enable-development-mode", parameters: [[$class: 'StringParameterValue', name: 'GIT_BRANCH_ORIGIN', value: 'origin/master'], [$class: 'StringParameterValue', name: 'ZONE', value: 'narrative.org'], [$class: 'StringParameterValue', name: 'SLACK_USER', value: SLACK_USER]]
      }
    }
    stage ('solr-configset1-deploy') {
      steps {
        build job: "/narrative-platform-solr-configset1-deploy", parameters: [[$class: 'StringParameterValue', name: 'GIT_BRANCH_ORIGIN', value: GIT_BRANCH_ORIGIN], [$class: 'StringParameterValue', name: 'COMMIT_SHA', value: "${env.COMMIT_SHA}"], [$class: 'StringParameterValue', name: 'ENVIRONMENT', value: ENVIRONMENT], [$class: 'StringParameterValue', name: 'SLACK_USER', value: SLACK_USER]]
      }
    }
    stage ('deploy') {
      steps {
        build job: "/narrative-platform-k8s-deploy", parameters: [[$class: 'StringParameterValue', name: 'GIT_BRANCH_ORIGIN', value: GIT_BRANCH_ORIGIN], [$class: 'StringParameterValue', name: 'COMMIT_SHA', value: "${env.COMMIT_SHA}"], [$class: 'StringParameterValue', name: 'DOCKER_TAG_DEPLOY', value: DOCKER_TAG_DEPLOY], [$class: 'StringParameterValue', name: 'REP_DOCKER_TAG_DEPLOY', value: REP_DOCKER_TAG_DEPLOY], [$class: 'StringParameterValue', name: 'ENVIRONMENT', value: ENVIRONMENT], [$class: 'StringParameterValue', name: 'SLACK_USER', value: SLACK_USER]]
      }
    }
    stage ('cloudflare-cache-purge') {
      when {
        expression {
          ENVIRONMENT == 'staging' ||
          ENVIRONMENT == 'production'
        }
      }
      steps {
        build job: "/cloudflare-cache-purge", parameters: [[$class: 'StringParameterValue', name: 'GIT_BRANCH_ORIGIN', value: 'origin/master'], [$class: 'StringParameterValue', name: 'ZONE', value: 'narrative.org'], [$class: 'StringParameterValue', name: 'SLACK_USER', value: SLACK_USER]]
      }
    }
    stage ('cloudflare-disable-development-mode') {
      when {
        expression {
          ENVIRONMENT == 'staging' ||
          ENVIRONMENT == 'production'
        }
      }
      steps {
        build job: "/cloudflare-disable-development-mode", parameters: [[$class: 'StringParameterValue', name: 'GIT_BRANCH_ORIGIN', value: 'origin/master'], [$class: 'StringParameterValue', name: 'ZONE', value: 'narrative.org'], [$class: 'StringParameterValue', name: 'SLACK_USER', value: SLACK_USER]]
      }
    }
  }
}

