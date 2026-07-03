pipeline {
    agent any

    environment {
        // -----------------------------------------------------------------------
        // CONFIGURE THESE before running the pipeline for the first time.
        // -----------------------------------------------------------------------

        // Your Docker Hub repository in the format: dockerhub-username/repo-name
        DOCKER_REPO = 'your-dockerhub-username/your-app-repo'

        // Your GitOps manifests repository in the format: github.com/username/repo.git
        // This is the repo where deployment.yaml lives (the CD side).
        MANIFESTS_GIT_REPO = 'github.com/your-username/your-gitops-manifests-repo.git'

        // The path inside the cloned manifests repo where deployment.yaml lives.
        // If your file is at k8s/deployment.yaml, set this to k8s/deployment.yaml.
        DEPLOYMENT_YAML_PATH = 'k8s/deployment.yaml'

        // -----------------------------------------------------------------------
        // These must match the names you give the tools in Jenkins → Manage Jenkins → Tools
        // -----------------------------------------------------------------------
        MAVEN_TOOL = 'Maven3'
        JDK_TOOL   = 'JDK17'

        // -----------------------------------------------------------------------
        // These must match the credential IDs you create in Jenkins → Credentials
        // -----------------------------------------------------------------------
        DOCKER_HUB_CREDS_ID = 'docker-hub-credentials'
        GITHUB_CREDS_ID     = 'github-token'

        // Slack channel to post build notifications to (requires Slack plugin configured)
        SLACK_CHANNEL = '#devops-alerts'
    }

    tools {
        maven "${MAVEN_TOOL}"
        jdk   "${JDK_TOOL}"
    }

    options {
        // Fail the build if it runs longer than 1 hour (catches hanging processes)
        timeout(time: 1, unit: 'HOURS')

        // Keep only the last 10 builds to save disk space on the Jenkins agent
        buildDiscarder(logRotator(numToKeepStr: '10'))

        // Prevent two builds of the same job running simultaneously
        disableConcurrentBuilds()

        // Enable coloured console output (requires AnsiColor plugin)
        ansiColor('xterm')
    }

    stages {

        // -----------------------------------------------------------------
        // Stage 1: Clean the workspace and check out a fresh copy of the
        // source code so every build starts from a known-clean state.
        // -----------------------------------------------------------------
        stage('Initialize & Clean') {
            steps {
                deleteDir()
                checkout scm
                // Print the Maven version so the build log shows exactly
                // which toolchain was used for this run.
                sh 'mvn -version'
            }
        }

        // -----------------------------------------------------------------
        // Stage 2: Compile the source code and run the full test suite,
        // including JaCoCo code coverage instrumentation.
        //
        // We run tests HERE (before SonarQube) so the jacoco.xml coverage
        // report exists by the time the scanner reads it in Stage 3.
        // Running 'verify' instead of 'package' triggers the JaCoCo report
        // goal that is bound to the verify lifecycle phase.
        // -----------------------------------------------------------------
        stage('Build & Test') {
            steps {
                sh 'mvn clean verify'
            }
            post {
                always {
                    // Publish JUnit test results to the Jenkins build page
                    junit 'target/surefire-reports/*.xml'

                    // Publish JaCoCo coverage report to the Jenkins build page
                    jacoco(
                        execPattern:   'target/jacoco.exec',
                        classPattern:  '**/classes',
                        sourcePattern: 'src/main/java'
                    )
                }
            }
        }

        // -----------------------------------------------------------------
        // Stage 3: Static Application Security Testing (SAST) via SonarQube.
        //
        // 'SonarQubeServer' must match the name you set in:
        //   Jenkins → Manage Jenkins → System → SonarQube servers
        //
        // The scanner reads the jacoco.xml produced in Stage 2, so coverage
        // figures in SonarQube will be accurate.
        // -----------------------------------------------------------------
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQubeServer') {
                    // -DskipTests so we do not run tests a second time;
                    // the coverage report from Stage 2 is already on disk.
                    sh 'mvn sonar:sonar -DskipTests'
                }
            }
        }

        // -----------------------------------------------------------------
        // Stage 4: Block the pipeline until SonarQube sends its webhook
        // callback with the Quality Gate result.
        //
        // If the gate fails (bugs, vulnerabilities, coverage below threshold)
        // the pipeline aborts here. No image is built for failing code.
        //
        // Prerequisite: configure a webhook in SonarQube pointing to:
        //   http://<jenkins-url>/sonarqube-webhook/
        // -----------------------------------------------------------------
        stage('Quality Gate') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    script {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Build stopped: SonarQube Quality Gate status is '${qg.status}'. Fix the reported issues and re-run."
                        }
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // Stage 5: Build the Docker image using the multi-stage Dockerfile
        // in this repository and push it to Docker Hub.
        //
        // Two tags are pushed:
        //   :<BUILD_NUMBER>  — immutable, traceable, used for GitOps rollback
        //   :latest          — floating convenience tag
        // -----------------------------------------------------------------
        stage('Docker Build & Push') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKER_HUB_CREDS_ID) {
                        def image = docker.build("${DOCKER_REPO}:${BUILD_NUMBER}")
                        image.push()
                        image.push('latest')
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // Stage 6: Update the image tag in the GitOps manifests repository
        // so Argo CD picks up the new version and deploys it to Kubernetes.
        //
        // How this works:
        //   1. Clone the manifests repo using a GitHub Personal Access Token
        //   2. Use sed to replace the image tag in deployment.yaml
        //   3. Commit and push only if the file actually changed
        //   4. The [skip ci] marker prevents this commit from re-triggering CI
        //
        // Security note: credentials are injected via withCredentials and
        // written to a temporary .git-credentials file that is removed
        // immediately after the clone. The password never appears in the
        // Jenkins build log or process list.
        // -----------------------------------------------------------------
        stage('GitOps Manifest Update') {
            steps {
                script {
                    withCredentials([
                        usernamePassword(
                            credentialsId: "${GITHUB_CREDS_ID}",
                            usernameVariable: 'GIT_USERNAME',
                            passwordVariable: 'GIT_PASSWORD'
                        )
                    ]) {
                        // Write credentials to a temporary store file so they
                        // are never interpolated into a shell command string.
                        sh """
                            git config --global credential.helper store
                            echo "https://\${GIT_USERNAME}:\${GIT_PASSWORD}@github.com" > \${HOME}/.git-credentials
                        """

                        sh "git clone https://${MANIFESTS_GIT_REPO} temp_manifests"

                        // Remove the credentials file as soon as the clone is done.
                        sh "rm -f \${HOME}/.git-credentials"
                        sh "git config --global --unset credential.helper || true"

                        dir('temp_manifests') {
                            // Scope git identity to this repo only, not the whole agent.
                            sh 'git config user.email "jenkins-ci@pipeline.local"'
                            sh 'git config user.name "Jenkins CI"'

                            // Replace the image line in deployment.yaml.
                            // The pattern matches any tag on the configured repo,
                            // so it works regardless of what the previous tag was.
                            sh "sed -i 's|image: ${DOCKER_REPO}:.*|image: ${DOCKER_REPO}:${BUILD_NUMBER}|g' ${DEPLOYMENT_YAML_PATH}"

                            // Only commit if sed actually changed something.
                            // This prevents empty commits if the tag is unchanged.
                            def changes = sh(script: 'git status --porcelain', returnStdout: true).trim()
                            if (changes) {
                                sh "git add ${DEPLOYMENT_YAML_PATH}"
                                sh "git commit -m 'ci: bump image tag to ${BUILD_NUMBER} [skip ci]'"
                                sh 'git push origin main'
                            } else {
                                echo 'Deployment manifest already has this image tag. Nothing to commit.'
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline finished successfully. Build #${BUILD_NUMBER} is deployed."
            // Uncomment and configure the Slack plugin to enable notifications.
            // slackSend(
            //     channel: "${SLACK_CHANNEL}",
            //     color: '#36a64f',
            //     message: "PASSED: ${JOB_NAME} #${BUILD_NUMBER} — <${BUILD_URL}|View build>"
            // )
        }
        failure {
            echo "Pipeline failed at build #${BUILD_NUMBER}. Check the console output for details."
            // slackSend(
            //     channel: "${SLACK_CHANNEL}",
            //     color: '#cc0000',
            //     message: "FAILED: ${JOB_NAME} #${BUILD_NUMBER} — <${BUILD_URL}|View build>"
            // )
        }
        cleanup {
            // Always remove the cloned manifests directory to keep the workspace clean.
            sh 'rm -rf temp_manifests || true'
        }
    }
}
