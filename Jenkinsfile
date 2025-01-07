pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'cicd-jenkins'
        DOCKER_TAG = 'latest'
//         DOCKER_USERNAME = credentials('docker-username')
//         DOCKER_PASSWORD = credentials('docker-password')
    }

    stages {
        stage('Clone Repository') {
            steps {
                git 'https://github.com/JiaG293/cicd-jenkins.git'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh 'docker build -t $DOCKER_IMAGE:$DOCKER_TAG .'
                }
            }
        }

        stage('Run Docker Container') {
            steps {
                script {
//                     sh 'docker stop $DOCKER_IMAGE || true'
//                     sh 'docker rm $DOCKER_IMAGE || true'

//                     buoc dung va xoa container (tuong duong 2 lenh tren)
                    sh 'docker rm -f $DOCKER_IMAGE || true'

                    sh 'docker run -name $DOCKER_IMAGE -d -p 8888:8888 $DOCKER_IMAGE:$DOCKER_TAG'
                }
            }
        }

        /* stage('Push Docker Image to Docker Hub') {
            steps {
                script {
                    sh 'docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD'
                    sh 'docker push $DOCKER_IMAGE:$DOCKER_TAG'
                }
            }
        } */
    }

    post {
        always {
            cleanWs()
        }
    }
}
