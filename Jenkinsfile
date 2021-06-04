/*
    This pipeline is used to build a Spring Boot application, run unit tests, Sonar scan, Fortify scan for any vulnerabilities
    Finally if all good, an image is built and pushed to Public DockerHub
    Thereafter an Ansible Playbook is used to deploy the image into selected servers that can be controlled using hosts file in GitHub
    Playbook installs required software and launch the application in docker container

*/

node {
    def mvnHome
    def dockerTool
    def dockerUser
    def dockerApp
    def ansibleHome
    def imageTag
    
    properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '10', daysToKeepStr: '', numToKeepStr: '10')), rateLimitBuilds([count: 1, durationName: 'minute', userBoost: false]), pipelineTriggers([githubPush()])])

    try{

    stage('Preparation') { 
        println "Checkout code from GitHub"
        
        mvnHome = tool '576677_maven'
        dockerHome = tool '576677_docker'
        dockerUser = 'sathishchandran'
        dockerApp = 'devops-spboot'
        ansibleHome = tool '576677_ansible'
        
    }
    stage('Git Checkout'){
        println "Checkout code from GitHub"
        git 'https://github.com/sathish-chandran/batch10.git'
        imageTag = sh(returnStdout: true, script: "git log -n 1 --pretty=format:'%h'").trim()
        println("$imageTag")
    }
    stage('Run Unit Test and Publish Results'){
        println("Running unit test")
        try{
            withEnv(["MVN_HOME=$mvnHome"]){
                sh '"$MVN_HOME/bin/mvn" test'
            } 
        }finally {
                println("Publish Surefire report")
                junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml',skipPublishingChecks: true
        }

    }
    stage('Build') {
        println "Running maven build"
        withEnv(["MVN_HOME=$mvnHome"]) {
               sh '"$MVN_HOME/bin/mvn" clean package -DskipTests' 
                
        }
    }
    stage('Sonar Scan') {
        println "Performing Sonar Scan"
        withEnv(["MVN_HOME=$mvnHome"]) {
                sh '''
                "$MVN_HOME/bin/mvn" sonar:sonar -Dsonar.projectKey=bootcamp10 \
                -Dsonar.host.url=http://34.136.3.230:9000 \
                -Dsonar.login=da9179a989df45920fd5db9ecfb518e62cc14c89'''
        }
    }
    stage("Fortify App Scan"){
        try{
        //fodStaticAssessment bsiToken: '', entitlementPreference: 'SingleScanOnly', inProgressBuildResultType: 'FailBuild', inProgressScanActionType: 'Queue', personalAccessToken: 'my-docker-toke', releaseId: '151886', remediationScanPreferenceType: 'RemediationScanIfAvailable', srcLocation: '', tenantId: '', username: ''
        //I ran out of free trial entitlement. Will attach the scan report separately 
        //Scan report will be attached separately
        }
        catch(Error e){
            println("Failue while Fortify scan")
            error 'Failed'
            throw e
        }
    }

    stage("Build Docker Image"){
        println("Building docker image")
        withEnv(["DOCKER_HOME=$dockerHome","DOCKER_USER=$dockerUser","DOCKER_APP=$dockerApp","TAG=$imageTag"]) {
                sh 'sudo "$DOCKER_HOME/bin/docker" build -t "$DOCKER_USER/$DOCKER_APP":"$TAG" .'
        }
    }
    stage("Docker Login"){
        println("Login to Docker Hub")

        withCredentials([string(credentialsId: 'my-docker-toke', variable: 'docker_token')]) {
            try{
                withEnv(["DOCKER_HOME=$dockerHome"]) {
                    sh 'sudo "$DOCKER_HOME/bin/docker" login -u sathishchandran -p $docker_token'
                }
            }catch(Exception e){
                    println("Build failed due to incorrect login")
                    error("DOCKER LOGIN FAILURE")
                    throw e
            }
        }
    }
    stage("Push Docker Image"){
        println("Pusing docker image to dockerhub")
        withEnv(["DOCKER_HOME=$dockerHome","DOCKER_USER=$dockerUser","DOCKER_APP=$dockerApp","TAG=$imageTag"]) {
                sh 'sudo "$DOCKER_HOME/bin/docker" push ${DOCKER_USER}/${DOCKER_APP}:${TAG}'
        }
    }
    stage("Run Ansible Playbook"){
        println("Running Ansible Playbook to Install new version of Application")
        ansiColor('xterm'){
        ansiblePlaybook becomeUser: 'sathish_chandran_gcp', colorized: true, credentialsId: 'jenkins-ansible', disableHostKeyChecking: true, installation: '576677_ansible', inventory: 'ansible/hosts', playbook: 'ansible/playbook.yml'
        }
    }
    } catch(Exception e){
        println("Exception occured");
        println("Setting the current build as failed for any exceptions");
        currentBuild.result = 'FAILURE'
    }finally{
        println("Sending Email...")
        emailext body: "Job ${env.JOB_NAME} with build number ${env.BUILD_NUMBER} is ${currentBuild.currentResult}\n Please click: ${env.BUILD_URL} for more information",
                to: 'sathish.chandran.gcp@gmail.com',
                subject: "Jenkins Build ${currentBuild.currentResult}: Job ${env.JOB_NAME}"
        println("Clean up workspace")
        deleteDir()
    }
}
