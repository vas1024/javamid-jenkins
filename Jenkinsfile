def dockerImage
pipeline {
    agent any

    environment {
        PROJECT_NAME = 'jenkins-example'
        REPORT_DIR = 'build/test-results/test'
        JACOCO_HTML = 'target/site/jacoco'
        EMAIL_RECIPIENTS = 'team_email@yandex.ru' // нужно заменить на валидный email
        EMAIL_FROM = 'your_email@yandex.ru' // нужно заменить на валидный email
        EMAIL_SUBJECT = 'Jenkins build result'
        TELEGRAM_CHAT_ID = credentials('telegram_chat_id')
        TELEGRAM_TOKEN = credentials('telegram_token')
 
        IMAGE_NAME = "ghcr.io/vas1024/jenkins"
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        DOCKER_CREDS = "ghcr"
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
        timestamps()
    }

    stages {
        stage('Init') {
            steps {
                echo "Starting pipeline for project: ${PROJECT_NAME}"
                deleteDir()
            }
    

        }


        stage('Get source') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'java -version'
                sh 'mvn --version'
                sh 'mvn clean compile package -DskipTests'
//                bat 'mvn clean compile package -DskipTests'
            }
        }


//        stage('Tests') {
//            steps {
//                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
//                    
//   //                    sh 'mvn test'
//                    bat 'mvn test'
//                }
//            }
//            post {
//                always {
//                    junit "${REPORT_DIR}/*.xml"
//                }
//            }
//        }




        stage('Metrics gen') {
            steps {
                echo "key metrics:"
                echo "- Duration: ${currentBuild.durationString}"
                echo "- Author: ${env.BUILD_USER ?: 'N/A'}"
                echo "- Statis: ${currentBuild.currentResult}"
                echo "- Commit: ${env.GIT_COMMIT ?: 'N/A'}"
            }
        }

//        stage('HTML report') {
//            steps {
//                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
//                    bat 'mvn jacoco:report'
//                    publishHTML(target: [
//                        reportDir: "${JACOCO_HTML}",
//                        reportFiles: 'index.html',
//                        reportName: 'Jacoco Code Coverage',
//                        keepAll: true,
//                        alwaysLinkToLastBuild: true,
//                        allowMissing: true
//                    ])
//                }
//            }
//        }




        stage('Docker build') {
            steps {
                script {
                    dockerImage = docker.build("${IMAGE_NAME}:${IMAGE_TAG}")
                }
            }
        }

        stage('push image') {
            steps {
                script {
                    docker.withRegistry('https://ghcr.io', 'ghcr_username_password') {
                        dockerImage.push()
                    }
                }
            }
        }



//stage('Push to GHCR') {
//    steps {
//        script {
//            withCredentials([usernamePassword(
//                credentialsId: 'ghcr',  
//                usernameVariable: 'GHCR_USER',
//                passwordVariable: 'GHCR_TOKEN'
//            )]) {
//                bat """
//                    echo %GHCR_TOKEN% | docker login ghcr.io -u %GHCR_USER% --password-stdin
//                    docker push ghcr.io/vas1024/jenkins:${env.BUILD_NUMBER}
//                    docker push ghcr.io/vas1024/jenkins:latest
//                    docker logout ghcr.io
//                """
//            }
//        }
//    }
//}






        stage('Notifications') {
            steps {
                script {
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        withEnv(["CHAT_ID=${TELEGRAM_CHAT_ID}", "TOKEN=${TELEGRAM_TOKEN}"]) {
//                            sh '''
//                                curl -s -X POST https://api.telegram.org/bot$TOKEN/sendMessage \
//                                    --data-urlencode chat_id=$CHAT_ID \
//                                    --data-urlencode text="Сборка: ${JOB_NAME}/${BRANCH_NAME} #${BUILD_NUMBER}\nСтатус: ${BUILD_STATUS}\nСсылка: ${BUILD_URL}" \
//                                    -d parse_mode=HTML
//                            '''
                        
                              sh """
                                  curl -s -X POST https://api.telegram.org/bot%TOKEN%/sendMessage \
                                      --data-urlencode chat_id=%CHAT_ID% \
                                      --data-urlencode text="Build: ${env.JOB_NAME}/${env.BRANCH_NAME}#${env.BUILD_NUMBER}    Status: ${currentBuild.currentResult}" \
                                      --data-urlencode parse_mode=HTML
                              """                        
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Success"
        }
        failure {
            echo "Fail"
        }
    }
}
