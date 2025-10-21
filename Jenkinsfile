pipeline {
    agent any

    environment {
        PROJECT_NAME = 'jenkins-example'
        REPORT_DIR = 'build/test-results/test'
        JACOCO_HTML = 'target/site/jacoco'
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
               
//                sh 'mvn clean compile package -DskipTests'
                 bat 'mvn clean compile package -DskipTests'
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

stage('Tests') {
    steps {
        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
            bat '''
                echo Running tests...
                mvn test
                echo Maven test execution finished
            '''
        }
    }
    post {
        always {
            bat '''
                echo Checking test reports...
                dir target /S | findstr ".xml" || echo No XML files found!
                if exist target\\surefire-reports (
                    echo Surefire reports directory exists
                    dir target\\surefire-reports /B
                ) else (
                    echo ERROR: surefire-reports directory not found!
                )
            '''
            junit "target/surefire-reports/*.xml"
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

stage('Tests and Coverage') {
    steps {
        bat '''
            echo Step 1: Clean previous build...
            echo mvn clean
            
            echo Step 2: Run tests WITH JaCoCo agent to collect coverage data...
            mvn test jacoco:prepare-agent test
            
            echo Step 3: Generate HTML report from collected data...
            mvn jacoco:report
            
            echo Step 4: Verify reports were generated...
            if exist target\\site\\jacoco\\index.html (
                echo SUCCESS: JaCoCo HTML report created!
            ) else (
                echo ERROR: Still no JaCoCo report!
                echo Checking what files exist:
                dir target /S | findstr ".exec .xml .html" || echo No relevant files found
            )
        '''
    }
    post {
        always {
            junit "target/surefire-reports/*.xml"
            
            script {
                if (fileExists('target/site/jacoco/index.html')) {
                    publishHTML(target: [
                        reportDir: 'target/site/jacoco',
                        reportFiles: 'index.html', 
                        reportName: 'JaCoCo Code Coverage',
                        keepAll: true
                    ])
                    echo "JaCoCo HTML report published"
                } else {
                    echo "JaCoCo HTML report not available"
                }
            }
        }
    }
}

stage('HTML Report Debug') {
    steps {
        bat '''
            echo === CHECKING JACOCO GENERATION ===
            mvn jacoco:report
            
            echo === CHECKING WHAT WAS GENERATED ===
            dir target /S | findstr ".html" || echo No HTML files found!
            
            if exist target\\site (
                echo Contents of target/site:
                dir target\\site /S /B
            ) else (
                echo ERROR: target/site directory does not exist!
            )
            
            echo === CHECKING POM CONFIGURATION ===
            mvn help:effective-pom | findstr "jacoco" || echo No jacoco configuration found!
        '''
    }
}

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
                        
                              bat """
                                  curl -s -X POST https://api.telegram.org/bot%TOKEN%/sendMessage ^
                                      --data-urlencode chat_id=%CHAT_ID% ^
                                      --data-urlencode text="Build: ${env.JOB_NAME}/${env.BRANCH_NAME}#${env.BUILD_NUMBER}    Status: ${currentBuild.currentResult}" ^
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
