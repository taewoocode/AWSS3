pipeline {
    agent any
    stages {
        stage('git scm update') {
            steps {
                git url: 'https://github.com/taewoocode/AWSS3', branch: 'main'
            }
        }
        stage('docker build and push') {
            steps {
                sh '''
                sudo docker build -t sorrykim/backup-backend:latest .
                sudo docker push sorrykim/backup-backend:latest
                '''
            }
        }
        stage('deploy k8s') {
            steps {
                sh '''
                sudo kubectl create deploy testpipeline --image=sorrykim/backup-backend:latest
                sudo kubectl expose deploy testpipeline --type=NodePort --port=8081 --target-port=80 --name=testpipeline-svc
                '''
            }
        }
    }
}
