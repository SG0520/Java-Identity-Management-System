def sendEmail(Status){
    emailext subject: "CI CD Update || Identity-Java || Job #${BUILD_NUMBER} || Status ${Status}",
                    body: "Project : ${JOB_NAME}<br>Build Status: ${Status}<br>Build URL: $BUILD_URL<br>Build Duration : ${currentBuild.durationString.replace(' and counting', '')}<br> Build Date : ${new Date().format('dd/MM/yyyy')}",mimeType: 'text/html',
                    replyTo: '$DEFAULT_REPLYTO',
                    to:"Youe email",
                    attachLog: true
}
def status


node {
    properties([
    parameters([

        string(name: "BUCKET", defaultValue:"Your Bucket Value", description: "bucketName"),
        //string(name: "BRANCH", defaultValue: "dev", description: "branchName"),
        string(name: "REGION", defaultValue:#"Your value" , description: "regionName")
    ])
    ])
    def gradleHome = tool 'gradle6.3'
    try {
        stage('Clone Repository') { 
            cleanWs()
            git url: 'https://github.com/SG0520/Java-Identity-Management-System', branch: [[name: '*/main']]  , credentialsId: 'SG0520'
            //checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: '*/master']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/Shivamrathi001/devops-webapp.git']]]
        }
        stage('Build') {
            sh """ #!/bin/bash
            ${gradleHome}/bin/gradle --version
            ${gradleHome}/bin/gradle clean build --stacktrace
            """
        }
        stage('Containerize'){
            withDockerRegistry(credentialsId:#your Id, url: #your URL) {
                sh """ #!/bin/bash
                docker build -t "identity-java-${env.BUILD_ID}" .
                docker images
                docker tag "identity-java-${env.BUILD_ID}:latest" <Your host>:<port>/identity-java-${env.BUILD_ID}:Tag
                docker push <Your host>:<port>/identity-java-${env.BUILD_ID}:tag
                docker rmi -f "identity-java-${env.BUILD_ID}" "Your host>:<port>/identity-java-${env.BUILD_ID}:tag"
                echo ${env.BUILD_ID} > buildNumber.txt
                """
            }
        }
        
        stage('Deploy') {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'Your Creds']]) {
                    def JOB_BASE_NAME = "${env.JOB_NAME}".split('/').last()
                    def destinationFile = "${env.JOB_BASE_NAME}/[[name: '*/main']]/${env.JOB_BASE_NAME}.zip"
                    def versionLabel = "${env.JOB_BASE_NAME}#[[name: '*/main']]#${env.BUILD_NUMBER}"
                    def description = "${env.BUILD_URL}"
                    sh """\
                            ls -larth
                            rm -rf ${env.JOB_BASE_NAME}.zip
                            zip -r ${env.JOB_BASE_NAME}.zip buildNumber.txt appspec.yml Dependency_Scripts
                            /usr/local/bin/aws s3 cp ${env.JOB_BASE_NAME}.zip s3://${BUCKET}/${destinationFile}
                            /usr/local/bin/aws s3api put-object-tagging --bucket ${BUCKET} --key ${destinationFile} --tagging '{"TagSet":[{"Key":"Build_Number","Value":"${env.BUILD_NUMBER}"}]}'
                            /usr/local/bin/aws configure set default.region ${REGION}
                            /usr/local/bin/aws deploy create-deployment \
                              --application-name 'cloudcoe-accelerator' \
                              --deployment-config-name CodeDeployDefault.OneAtATime \
                              --deployment-group-name 'identity-java' \
                              --s3-location bucket=${BUCKET},bundleType=zip,key=${destinationFile} > output.json
                            export deployment_id=\$(cat output.json | jq -r '.deploymentId')
                            echo \$deployment_id
                            /usr/local/bin/aws deploy wait deployment-successful --deployment-id \$deployment_id
                         """
                }
        }
        
        stage('post ') {
                status = "SUCCESS"
                sendEmail("SUCCESS")
                cleanWs deleteDirs: true, disableDeferredWipeout: true, patterns: [[pattern: '**/build/**', type: 'EXCLUDE']]
                
        }

        
    }
     catch(Exception e){
        status = "FAILURE"
         sendEmail(status)
         cleanWs deleteDirs: true, disableDeferredWipeout: true, patterns: [[pattern: '**/build/**', type: 'EXCLUDE']]
        throw e;
    }
    finally{
       echo "Done"
    }
}
