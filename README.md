# Zowe® zDevOps Jenkins® plugin

## About the plugin
The Zowe zDevOps Jenkins plugin is an open-source, secure, and reliable agent-less plugin for Jenkins that makes it possible to perform most of the actual tasks on the mainframe, managing it with a modern native mainframe zOSMF REST API and the capabilities of available zOSMF SDKs. [Zowe](https://www.zowe.org/) is a project hosted by the [Open Mainframe Project](https://www.openmainframeproject.org/), a [Linux Foundation](https://www.linuxfoundation.org/) project.

## Main features
- Secure and modern connection of Jenkins to the mainframes through the use of zOSMF REST API.
- The functionality is based on the Kotlin SDK methods, such as JCL jobs submission, download, allocate, write to the dataset, etc., with a log collected upon completion.
- Multiple connections to various mainframes — z/OS Connections List where you can save all the necessary systems and credentials (all data is safely stored under the protection of Jenkins Credentials manager).
- Agent-less solution.
- z/OSMF connection validation.
- Convenient user interface panels for working with the mainframe
- Fast execution and functional extensibility.

## Before use - Plugin configuration
After successfully installing the plugin, you need to configure it for further work - this will require a minimum of actions.
1. Move to 'Manage Jenkins' -> 'Configure System / System' -> scroll to the very bottom of the list of installed plugins and find the panel with the name - <b>'z/OS Connection List'</b>
2. This setting allows you to add all necessary z/OS systems and configure access to them.
   It is necessary to set the connection name (it is also the ID for declarative methods in the code). For the example: ```z/os-connection-name```
3. The URL address and port of the required mainframe to connect via z/OSMF. Example: ```https://<ip-addres>:<port number>```
4. Add credentials (Mainframe User ID + Password) under which you can connect to the system.

You can save as many connections as you like, the system will keep the corresponding user IDs/passwords.

## Declarative methods brief list
```groovy
stage ("stage-name") {
    steps {
        // ...
        zosmf("z/os-connection-name") {
            submitJob "//'EXAMPLE.DATASET(MEMBER)'"
            submitJobSync "//'EXAMPLE.DATASET(MEMBER)'"
            downloadDS "EXAMPLE.DATASET(MEMBER)"
            downloadDS dsn:"EXAMPLE.DATASET(MEMBER)", vol:"VOL001"
            allocateDS dsn:"EXAMPLE.DATASET", alcUnit:"TRK", dsOrg:"PS", primary:1, secondary:1, recFm:"FB", failOnExist:"False"
            writeFileToDS dsn:"EXAMPLE.DATASET", file:"workspaceFile"
            writeFileToDS dsn:"EXAMPLE.DATASET", file:"D:\\files\\localFile"
            writeToDS dsn:"EXAMPLE.DATASET", text:"Write this string to dataset"
            writeFileToMember dsn:"EXAMPLE.DATASET", member:"MEMBER", file:"workspaceFile"
            writeFileToMember dsn:"EXAMPLE.DATASET", member:"MEMBER", file:"D:\\files\\localFile"
            writeToMember dsn:"EXAMPLE.DATASET", member:"MEMBER", text:"Write this string to member"

            writeToFile destFile: "u/USER/myfile", text: "Write this string to file"
            writeFileToFile destFile: "u/USER/myfile", sourceFile: "myfile.txt"
            writeFileToFile destFile: "u/USER/myfile", sourceFile: "myfile.txt", binary: "true"

            deleteDataset dsn:"EXAMPLE.DATASET", failOnNotExist:"False"
            deleteDataset dsn:"EXAMPLE.DATASET", member:"MEMBER", failOnNotExist:"True"
            deleteDatasetsByMask mask:"EXAMPLE.DATASET.*", failOnNotExist:"False"
        }
        // ...
    }
}
```

## Declarative Methods Detail Description

### allocateDS - Represents an action for allocating a dataset in a declarative style
```groovy
zosmf ("z/os-connection-name") {
    allocateDS(
            // Mandatory Parameters below:
            dsn: "EXAMPLE.DATASET",
            dsOrg: "PS",
            primary: 1,
            secondary: 1,
            recFm: "FB",
            failOnExist:"False",
            // Optional Parameters below:
            volser:"YOURVOL",
            unit:"SYSDA",
            alcUnit:"TRK",
            dirBlk:"5",
            blkSize:"800",
            lrecl:"80",
            storClass:"STORAGECLASS",
            mgntClass:"MGMTCLASS",
            dataClass:"DATACLASS",
            avgBlk:"10",
            dsnType:"LIBRARY",
            dsModel:"MODEL.DATASET.NAME"
    )
}
```
**Mandatory Parameters:**
* ```dsn:"EXAMPLE.DATASET"``` - The name of the dataset to be allocated
* ```dsOrg:"PS"``` - The dataset organization (could be only PO, POE, PS, VS)
* ```primary:"1"``` - The primary allocation size in cylinders or tracks
* ```secondary:"1"``` - The secondary allocation size in cylinders or tracks
* ```recFm:"FB"``` - The record format (could be only F, FB, V, VB, U, VSAM, VA)
* ```failOnExist:"False"``` - If the dataset already exists and the option is enabled, execution will halt. (Boolean parameter, is set to 'False' by default)

**Optional parms:**
* ```volser:"YOURVOL"``` - Volume serial number where the dataset will be allocated.
* ```unit:"SYSDA"``` - Specifies the type of storage device. SYSDA is a common direct access storage device.
* ```alcUnit:"TRK"``` - Allocation units (CYL for cylinders, TRK for tracks).
* ```dirBlk:"5"``` - Directory block records.
* ```blkSize:"800"``` - BLKSIZE=800: Block size of 800 bytes.
* ```lrecl:"80"``` - Logical record length.
* ```storClass:"STORAGECLASS"``` - Storage class for SMS-managed datasets.
* ```mgntClass:"MGMTCLASS"``` - Management class for SMS-managed datasets.
* ```dataClass:"DATACLASS"``` - Data class for SMS-managed datasets.
* ```avgBlk:"10"``` - Average block length.
* ```dsnType:"LIBRARY"``` - Specifies the type of dataset, LIBRARY for a PDS or PDSE.
* ```dsModel:"MODEL.DATASET.NAME"``` - Data set model is a predefined set of attributes that can be used to allocate new data sets with the same characteristics ("LIKE" parameter).


### deleteDataset - Represents an action for deleting datasets and members in a declarative style
```groovy
zosmf ("z/os-connection-name") {
    deleteDataset dsn: "EXAMPLE.DATASET", member:"MEMBER", failOnNotExist:"False"
}
```
**Mandatory Parameters:**
* ```dsn:"EXAMPLE.DATASET"``` - Sequential or library dataset name for deletion
* ```member:"MEMBER"``` - Dataset member name for deletion
* ```failOnNotExist:"False"``` - If the dataset has been deleted and the option is enabled, execution will halt. (Boolean parameter, is set to 'False' by default)

**Expected behavior under various deletion scenarios:**

* To delete a member from the library, the dsn and member parameters must be specified:
    ```
    deleteDataset dsn:"EXAMPLE.DATASET", member:"MEMBER", failOnNotExist:"False"
    ```

* You cannot delete a VSAM dataset this way. Otherwise, you will get output similar to:
    ```
    Deleting dataset EXAMPLE.VSAM.DATASET with connection <ip-address>:10443 
    ISRZ002 Deallocation failed - Deallocation failed for data set 'EXAMPLE.VSAM.DATASET'
    ```

* What do you get if a dataset does not exist?

    ```
    Deleting dataset EXAMPLE.DS.DOES.NOT.EXIST with connection <ip-address>:10443
    ISRZ002 Data set not cataloged - 'EXAMPLE.DS.DOES.NOT.EXIST' was not found in catalog.
    ```

* What do you get if a dataset is busy by a user or a program?

    ```
    Deleting dataset EXAMPLE.DS.ISUSED.BY.USER with connection <ip-address>:10443
    ISRZ002 Data set in use - Data set 'EXAMPLE.DS.ISUSED.BY.USER' in use by another user, try later or enter HELP for a list of jobs and users allocated to 'EXAMPLE.DS.ISUSED.BY.USER'.
    ```

## Use case example
Here you can find an example of a minimal declarative Jenkins pipeline for execution, testing and further modification for your personal needs.
Pipeline can be used either directly inside the ```Pipeline``` code block in the Jenkins server, or in a ```Jenkinsfile``` stored in Git
This pipeline example uses all currently available methods and functionality of the Zowe zDevOps plugin.

**Steps to Execute the Pipeline:**
1. Add a zosmf connection in settings (<b>'Manage Jenkins' -> 'Configure System / System' -> z/OS Connection List</b>). Enter a connection name, zosmf url, username and password.
2. Create a new Jenkins item -> ```Pipeline``` and open its configuration.
3. In the ```Pipeline``` section, paste the code from the example below and replace all the necessary variables with your data
4. Done, enjoy the minimal ready-made pipeline template!

```groovy
pipeline {
    agent any

    environment {
        // Define environment variables
        GIT_REPOSITORY_URL = 'https://github.com/your-username/your-repo.git'  // Replace with your GitHub URL
        GIT_BRANCH = 'main'                                     // Replace with your GitHub branch name
        GIT_USER_CREDENTIAL_ID = 'jenkins-cred-key'             // Replace with your Jenkins GitHub credential ID
        ZOS_CONN_ID = 'z/os-connection-name'                    // Replace with your z/OS Connection ID from zDevOps plugin settings
        HLQ = 'HLQ'                                             // Replace with your z/OS high-level qualifier (HLQ)
        PS_DATASET_1 = "${HLQ}.NEW.TEST1"                       // OPTIONAL: Replace with the dataset names you need
        PS_DATASET_2 = "${HLQ}.NEW.TEST2"                       // OPTIONAL
        PO_DATASET = "${HLQ}.NEW.TEST3"                         // OPTIONAL
        PO_MEMBER = "NEWMEM"                                    // OPTIONAL
        JCL_JOB_TEMPLATE = "jcl_job_example"                    // Replace with the name of your file that contains the JCL job code
        JIRA_URL = 'https://your-jira-instance.atlassian.net'   // Replace with your Jira URL
        JIRA_USER = 'your-jira-email@example.com'               // Replace with your Jira user email
        JIRA_API_TOKEN = 'your-jira-api-token'                  // Replace with your Jira API token
        JIRA_ISSUE_KEY = 'PROJECT-123'                          // Replace with your Jira issue key
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout the source code from Git
                checkout scmGit(
                        branches: [[name: "${GIT_BRANCH}"]],
                        userRemoteConfigs: [[credentialsId:  "${GIT_USER_CREDENTIAL_ID}",
                                             url: "${GIT_REPOSITORY_URL}"]])
            }
        }

        stage('Allocate DSs') {
            steps {
                zosmf("${ZOS_CONN_ID}") {
                    allocateDS dsn:"${PS_DATASET_1}", dsOrg:"PS", primary:1, secondary:1, recFm:"FB", failOnExist:"False"
                    allocateDS dsn:"${PS_DATASET_2}", dsOrg:"PS", primary:1, secondary:1, recFm:"FB", alcUnit:"TRK", failOnExist:"False"
                    allocateDS dsn:"${PO_DATASET}(${PO_MEMBER})", dsOrg:"PO", primary:1, secondary:1, recFm:"FB", failOnExist:"False"
                }
            }
        }

        stage('Add JCL content') {
            steps {
                script {
                    // Read the content of the JCL job into a variable
                    env.JCL_CONTENT = readFile("${JCL_JOB_TEMPLATE}").trim()
                    // Print the content of the file (for debugging purposes)
                    echo "JCL job content:\n${env.JCL_CONTENT}"
                }
                zosmf("${ZOS_CONN_ID}") {
                    writeFileToDS dsn:"${PS_DATASET_2}", file:"${JCL_JOB_TEMPLATE}"
                    writeFileToMember dsn:"${PO_DATASET}", member:"${PO_MEMBER}", file:"${JCL_JOB_TEMPLATE}"
                    writeToDS dsn:"${PS_DATASET_1}", text:"${env.JCL_CONTENT}"
                }
            }
        }

        stage('Add USS content') {
            steps {
                zosmf("${ZOS_CONN_ID}") {
                    writeToFile destFile: "u/${HLQ}/test_file1", text: "${env.JCL_CONTENT}"
                    writeFileToFile destFile: "u/${HLQ}/test_file2", sourceFile: "${JCL_JOB_TEMPLATE}", binary: "true"
                }
            }
        }

        stage('Submit JCL jobs') {
            steps {
                zosmf("${ZOS_CONN_ID}") {
                    submitJob "//'${PS_DATASET_1}'"
                    submitJobSync "//'${PO_DATASET}(NEWMEM)'"
                }
            }
        }

        stage('Download datasets') {
            steps {
                zosmf("${ZOS_CONN_ID}") {
                    downloadDS "${PS_DATASET_1}"
                    downloadDS dsn:"${PS_DATASET_2}"
                }
            }
        }

        stage('Clean up') {
            steps {
                zosmf("${ZOS_CONN_ID}") {
                    deleteDataset dsn:"${PS_DATASET_1}", failOnNotExist:"False"
                    deleteDatasetsByMask mask:"${HLQ}.NEW.*", failOnNotExist:"True"
                }
            }
        }
    }

    post {
        always {
            script {
                def jiraStatus = currentBuild.currentResult == 'SUCCESS' ? 'Build Successful' : 'Build Failed'
                def jiraComment = """
                {
                    "body": "Jenkins build ${jiraStatus} for Job ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}.
                    [View the build here|${env.BUILD_URL}]"
                }
                """

                httpRequest acceptType: 'APPLICATION_JSON',
                        contentType: 'APPLICATION_JSON',
                        httpMode: 'POST',
                        requestBody: jiraComment,
                        url: "${JIRA_URL}/rest/api/2/issue/${JIRA_ISSUE_KEY}/comment",
                        authentication: 'jira-credentials-id'
            }
        }

        success {
            // Notify success (example: send email)
            mail to: '${JIRA_USER}',
                    subject: "SUCCESS: Build ${env.BUILD_NUMBER}",
                    body: "The build ${env.BUILD_NUMBER} was successful."
        }

        failure {
            // Notify failure (example: send email)
            mail to: '${JIRA_USER}',
                    subject: "FAILURE: Build ${env.BUILD_NUMBER}",
                    body: "The build ${env.BUILD_NUMBER} failed. Please check the Jenkins logs for more details."
        }
    }
}
```

## Manual plugin installation by the .hpi executable file
The plugin are packaged as self-contained <b>.hpi</b> files, which have all the necessary code, images, and other resources which the plugin needs to operate successfully.

### <b>Already packaged and tested installation .hpi file can be downloaded from a link from a nearby GitHub repository:</b>
### <b>[Zowe zDevOps plugin installation .hpi file](https://github.com/IBA-mainframe-dev/Global-Repository-for-Mainframe-Developers/blob/master/Jenkins%20zOS%20DevOps%20plugin%20installable%20hpi/zos-devops.hpi)</b>

Assuming a <b>.hpi</b> file has been downloaded, a logged-in Jenkins administrator may upload the file from within the web UI:
1. Navigate to the <b>Manage Jenkins > Plugins</b> page in the web UI.
2. Click on the <b>Advanced</b> tab.
3. Choose the <b>.hpi</b> file from your system or enter a URL to the archive file under the <b>Deploy Plugin</b> section.
4. <b>Deploy</b> the plugin file.

## Manual Jenkins plugin installation (Installation via source code build and .hpi file upload)
1. Download the Zowe zDevOps Jenkins plugin source code from its [official Jenkins GitHub repository](https://github.com/jenkinsci/zdevops-plugin)
2. It is necessary to build the project with the help of the Maven Build Tool
3. To generate the ```target``` dir with generated-sources - you have to run the Maven command: ```mvn localizer:generate```
4. Next, you need to generate an installation file: .hpi or .jpi file (both are installation files for the Jenkins plugin). This can be done by executing Maven command ```mvn install``` or by ```mvn hpi:hpi```.
5. After building the .hpi/.jpi file, it should appear in a <b><Plugin-project-name>/build/libs/<hpi_file_name>.hpi</b> directory
6. Next you need to login into the Jenkins, move to the <b>'Manage Jenkins' -> 'Plugins' -> 'Advanced settings (tab)' -> 'Deploy Plugin'</b> (You can select a plugin file from your local system or provide a URL to install a plugin from outside the central plugin repository) <b>-> Specify the path to the generated .hpi/.jpi file</b> (or by dragging the file from Intellij IDEA project to the file upload field in the Jenkins).
7. Click <b>'Deploy'</b>, reboot Jenkins after installation. The Plugin is ready to go!

## How to run Jenkins plugin in Debug mode in a local Jenkins sandbox

For debugging purposes run following Maven command from plugin project directory:

```./mvnw hpi:run```

Or by ```mvnDebug hpi:run``` - this will copy all the dependencies down (rather than in your jenkins install) and run it in place.

By default, the debugging instance is then available at [http://localhost:8080/jenkins](http://localhost:8080/jenkins) in your browser.

In order to launch Jenkins on a different port than 8080 use this system property:

```./mvnw hpi:run -Djetty.port=8090```

Changing the default context path can be achieved by setting this system property:

```./mvnw hpi:run -Dhpi.prefix=/debug```

## How to run unit-tests with JaCoCo report

```./mvnw clean verify```

The results are available under `target/site/jacoco/index.html`