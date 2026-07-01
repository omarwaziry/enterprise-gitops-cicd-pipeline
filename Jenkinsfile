pipeline {
    agent any

    environment {
        // Docker registry parameters
        DOCKER_REGISTRY      = 'docker.io'
        DOCKER_REPO          = 'omarwazery/devops-showcase-app'
        DOCKER_IMAGE_NAME    = "${DOCKER_REGISTRY}/${DOCKER_REPO}"
        
        // GitOps Manifest repository parameters
        MANIFESTS_GIT_REPO   = 'github.com/omarwaziry/gitops-manifests-repo.git'
        
        // Tool identifiers in Jenkins
        MAVEN_TOOL           = 'Maven3'
        JDK_TOOL             = 'JDK17'
        
        // Credentials identifiers in Jenkins credential manager
        DOCKER_HUB_CREDS_ID  = 'docker-hub-credentials'
        GITHUB_CREDS_ID      = 'github-token'
        SLACK_CHANNEL        = '#devops-alerts'
    }

    tools {
        maven "${MAVEN_TOOL}"
        jdk "${JDK_TOOL}"
    }

    options {
        timeout(time: 1, unit: 'HOURS')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
        ansiColor('xterm')
    }

    stages {
        stage('Initialize & Clean') {
            steps {
                echo 'Cleaning workspace...'
                deleteDir()
                checkout scm
                sh 'mvn -version'
            }
        }

        stage('Maven Build') {
            steps {
                echo 'Compiling and packaging the Spring Boot application (skipping tests for now)...'
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('SonarQube Static Code Analysis') {
            steps {
                echo 'Running SonarQube Quality Analysis...'
                // 'SonarQubeServer' must match the system config configuration in Jenkins
                withSonarQubeEnv('SonarQubeServer') {
                    sh 'mvn sonar:sonar'
                }
            }
        }

        stage('Quality Gate Verification') {
            steps {
                echo 'Waiting for SonarQube Quality Gate webhook callback...'
                timeout(time: 10, unit: 'MINUTES') {
                    // Requires setting up a webhook in SonarQube pointing to Jenkins
                    script {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Pipeline aborted due to SonarQube Quality Gate failure: ${qg.status}"
                        }
                        echo "SonarQube Quality Gate passed successfully with status: ${qg.status}"
                    }
                }
            }
        }

        stage('Execute Unit Tests') {
            steps {
                echo 'Executing unit and integration tests...'
                sh 'mvn test'
            }
            post {
                always {
                    // Record test results inside Jenkins reports
                    junit 'target/surefire-reports/*.xml'
                    // Publish JaCoCo coverage reports
                    jacoco execPattern: 'target/jacoco.exec', classPattern: '**/classes', sourcePattern: 'src/main/java'
                }
            }
        }

   stage('Docker Build & Push') {
            steps {
                echo "Building Docker image: ${DOCKER_IMAGE_NAME}:${BUILD_NUMBER}"
                script {
                    // Use the exact credentials ID variable you configured in Jenkins
                    docker.withRegistry("https://${DOCKER_REGISTRY}", "${DOCKER_HUB_CREDS_ID}") {
                        
                        // 1. Build the image inside the authenticated scope
                        // Make sure DOCKER_IMAGE_NAME is just 'omarwazery/devops-showcase-app' (no docker.io/ prefix)
                        def customImage = docker.build("${DOCKER_IMAGE_NAME}:${BUILD_NUMBER}", "-f Dockerfile .")
                        
                        // 2. Push the build number tag (:15)
                        customImage.push()
                        
                        // 3. Push the latest tag using the native plugin method
                        customImage.push("latest")
                    }
                }
            }
        } 
        stage('GitOps Manifest Update') {
            steps {
                echo 'Updating image tag in Manifest Repository for GitOps deployment...'
                script {
                    withCredentials([usernamePassword(credentialsId: "${GITHUB_CREDS_ID}", passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                        // Configure temporary Git options
                        sh 'git config --global user.email "jenkins-ci@example.com"'
                        sh 'git config --global user.name "Jenkins Automation Pipeline"'
                        
                        // Clone the manifests repository using the checkout token
                        sh "git clone https://${GIT_USERNAME}:${GIT_PASSWORD}@${MANIFESTS_GIT_REPO} temp_manifests"
                        
                        dir('temp_manifests') {
                            // Update the image in deployment.yaml. Matches: "image: group/app:tag" and replaces the tag
                            sh "sed -i 's|image: .*/devops-showcase-app:.*|image: ${DOCKER_IMAGE_NAME}:${BUILD_NUMBER}|g' deployment.yaml"
                            
                            // Check if changes exist before committing to avoid empty commits
                            def status = sh(script: 'git status --porcelain', returnStdout: true).trim()
                            if (status) {
                                sh "git add deployment.yaml"
                                sh "git commit -m 'GitOps Auto-Update: Bumped image tag to v${BUILD_NUMBER} [skip ci]'"
                                sh "git push origin main"
                                echo "Manifests repository updated successfully."
                            } else {
                                echo "No changes detected in manifests repo. Skipping commit."
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully. Sending Slack notification...'
            // slackSend(
            //     channel: "${SLACK_CHANNEL}",
            //     color: '#00FF00',
            //     message: "SUCCESSFUL: Job '${env.JOB_NAME}' [Build #${env.BUILD_NUMBER}] completed successfully.\nView changes: ${env.BUILD_URL}"
            // )
        }
        failure {
            echo 'Pipeline failed. Sending alerts...'
            // slackSend(
            //     channel: "${SLACK_CHANNEL}",
            //     color: '#FF0000',
            //     message: "FAILED: Job '${env.JOB_NAME}' [Build #${env.BUILD_NUMBER}] failed during build stages.\nVerify error logs at: ${env.BUILD_URL}"
            // )
        }
    }
}
