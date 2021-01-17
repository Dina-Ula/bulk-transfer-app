#!/usr/bin/groovy
pipeline {
    agent any

    tools {
        maven '3.6.3' 
    }

    stages {

        stage('Build') {
            steps {
                echo 'Building..'
                withMaven {
                    sh "mvn clean verify"
                }
            }
        }

        stage('Test') {
            steps {
                echo 'Testing..'
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying....'
            }
        }
    }
}