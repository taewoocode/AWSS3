pipeline {
    agent any
    stages {
        stage('Git SCM Update') {
            steps {
                git branch: 'main', url: 'https://github.com/taewoocode/AWSS3'
            }
        }
        stage('Docker Build and Push') {
            steps {
                script {
                    def dockerImage = 'sorrykim/backup-backend:latest'
                    sh "docker build -t $dockerImage ."
                    sh "docker push $dockerImage"
                }
            }
        }
        stage('Deploy to Kubernetes') {
            steps {
                script {
                    def deploymentName = 'testpipeline'
                    def imageName = 'sorrykim/backup-backend:latest'
                    sh "kubectl create deployment $deploymentName --image=$imageName"
                    sh "kubectl expose deployment $deploymentName --type=NodePort --port=8081 --target-port=80 --name=${deploymentName}-svc"
                }
            }
        }
    }
    post {
        always {
            echo 'Cleaning up...'
            sh 'docker system prune -af'
        }
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed!'
        }
    }
}
