#!/usr/bin/groovy
pipeline {
    agent any

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