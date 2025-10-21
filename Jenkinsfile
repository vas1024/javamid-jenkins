pipeline {
    agent any

    environment {
        PROJECT_NAME = 'jenkins-example'
        REPORT_DIR = 'build/test-results/test'
        JACOCO_HTML = 'build/reports/jacoco/test/html'
        EMAIL_RECIPIENTS = 'team_email@yandex.ru' // нужно заменить на валидный email
        EMAIL_FROM = 'your_email@yandex.ru' // нужно заменить на валидный email
        EMAIL_SUBJECT = 'Jenkins build result'
        TELEGRAM_CHAT_ID = credentials('TELEGRAM_CHAT_ID')
        TELEGRAM_TOKEN = credentials('TELEGRAM_TOKEN')
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
            }
        }

        stage('Get source') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
               
                sh 'mvn clean compile package -DskipTests'
            }
        }

        stage('Tests') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit "${REPORT_DIR}/*.xml"
                }
            }
        }

        stage('Metrics gen') {
            steps {
                echo "key metrics:"
                echo "- Duration: ${currentBuild.durationString}"
                echo "- Author: ${env.BUILD_USER ?: 'N/A'}"
                echo "- Statis: ${currentBuild.currentResult}"
                echo "- Commit: ${env.GIT_COMMIT ?: 'N/A'}"
            }
        }

        stage('HTML report') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    publishHTML(target: [
                        reportDir: "${JACOCO_HTML}",
                        reportFiles: 'index.html',
                        reportName: 'Jacoco Code Coverage',
                        keepAll: true,
                        alwaysLinkToLastBuild: true,
                        allowMissing: true
                    ])
                }
            }
        }

        stage('Уведомления') {
            steps {
                script {
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
//                        emailext(
//                            subject: "${EMAIL_SUBJECT}",
//                            body: """<p>Статус: ${currentBuild.currentResult}</p>
//                                     <p>Ссылка на билд: <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>""",
//                            recipientProviders: [[$class: 'DevelopersRecipientProvider']],
//                            to: "${EMAIL_RECIPIENTS}",
//                            from: "${EMAIL_FROM}",
//                            mimeType: 'text/html'
//                        )
                    }

                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        withEnv(["CHAT_ID=${TELEGRAM_CHAT_ID}", "TOKEN=${TELEGRAM_TOKEN}"]) {
                            sh '''
                                curl -s -X POST https://api.telegram.org/bot$TOKEN/sendMessage \
                                    --data-urlencode chat_id=$CHAT_ID \
                                    --data-urlencode text="Сборка: ${JOB_NAME}/${BRANCH_NAME} #${BUILD_NUMBER}\nСтатус: ${BUILD_STATUS}\nСсылка: ${BUILD_URL}" \
                                    -d parse_mode=HTML
                            '''
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
