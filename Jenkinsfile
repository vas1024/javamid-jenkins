pipeline {
    agent any

    environment {
        PROJECT_NAME = 'jenkins-example'
        REPORT_DIR = 'build/test-results/test'
        JACOCO_HTML = 'build/reports/jacoco/test/html'
        EMAIL_RECIPIENTS = 'team_email@yandex.ru' // ����� �������� �� �������� email
        EMAIL_FROM = 'your_email@yandex.ru' // ����� �������� �� �������� email
        EMAIL_SUBJECT = '��������� ������ Jenkins'
        TELEGRAM_CHAT_ID = credentials('-4834224227')
        TELEGRAM_TOKEN = credentials('7328854811:AAFYII_NhVkjhMePR66vz9FmFaulL66Vb2I')
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
        timestamps()
    }

    stages {
        stage('�������������') {
            steps {
                echo "������ ��������� ��� �������: ${PROJECT_NAME}"
            }
        }

        stage('��������� ����������') {
            steps {
                checkout scm
            }
        }

        stage('������ �������') {
            steps {
               
                sh 'mvn clean compile package -DskipTests'
            }
        }

        stage('������ ������') {
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

        stage('��������� ������') {
            steps {
                echo "�������� ������� ������:"
                echo "- ������������: ${currentBuild.durationString}"
                echo "- �����: ${env.BUILD_USER ?: 'N/A'}"
                echo "- ������: ${currentBuild.currentResult}"
                echo "- ������: ${env.GIT_COMMIT ?: 'N/A'}"
            }
        }

        stage('����� � HTML') {
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

        stage('�����������') {
            steps {
                script {
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
//                        emailext(
//                            subject: "${EMAIL_SUBJECT}",
//                            body: """<p>������: ${currentBuild.currentResult}</p>
//                                     <p>������ �� ����: <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>""",
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
                                    --data-urlencode text="������: ${JOB_NAME}/${BRANCH_NAME} #${BUILD_NUMBER}\n������: ${BUILD_STATUS}\n������: ${BUILD_URL}" \
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
            echo "������ ��������� �������."
        }
        failure {
            echo "������ ����������� � �������."
        }
    }
}