pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/JiaG293/cicd-jenkins.git'
            }
        }
        stage('Build') {
            steps {
                sh './gradlew clean build'
            }
        }
        stage('Test') {
            steps {
                sh './gradlew test'
            }
        }
        stage('Package') {
            steps {
                sh './gradlew bootJar'
            }
        }
        stage('Build Docker Image') {
                    steps {
                        script {
                            def imageName = "cicd-jenkins:latest"
                            sh """
                                docker build -t ${imageName} .
                            """
                        }
                    }
                }
        stage('Deploy to Docker') {
                            steps {
                                script {
                                    def containerName = "cicd-jenkins"
                                    sh """
                                        docker stop ${containerName} || true
                                        docker rm ${containerName} || true
                                        docker run -d --name ${containerName} -p 8888:8888 cicd-jenkins:latest
                                    """
                                }
                            }
                        }
//         stage('Deploy') {
//             steps {
//                 sh 'scp build/libs .jar user@server:/path/to/deploy'
//             }
//         }

    }
    post {
        success {
            echo 'Build and Deploy succeeded!'
        }
        failure {
            echo 'Build or Deploy failed!'
        }
    }
}
