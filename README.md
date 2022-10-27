# Zowe z/OS DevOps Jenkins plugin

## Main features
- Secure and modern connection of Jenkins to the mainframes through the use of zOSMF REST API
- The functionality is based on the Kotlin SDK methods, such as JCL jobs submission, download, allocate, write to the dataset, etc., with a log collected upon finish
- Multiple connections to various mainframes - z/OS Connections List where you can save all the necessary systems and credentials, all data are safely stored under the protection of Jenkins Credentials manager
- Agent-less solution
- Fast execution and functional extensibility

## Installing a plugin by .hpi executable file
The plugins are packaged as self-contained <b>.hpi</b> files, which have all the necessary code, images, and other resources which the plugin needs to operate successfully.

### <b>Already packaged and tested installation .hpi file can be downloaded from a link from a nearby GitHub repository:</b>
### <b>[z/OS DevOps plugin installation .hpi file](https://github.com/IBA-mainframe-dev/Global-Repository-for-Mainframe-Developers/blob/master/Jenkins%20zOS%20DevOps%20plugin%20installable%20hpi/zos-devops.hpi)</b>

Assuming a <b>.hpi</b> file has been downloaded, a logged-in Jenkins administrator may upload the file from within the web UI:
1. Navigate to the <b>Manage Jenkins > Manage Plugins</b> page in the web UI.
2. Click on the <b>Advanced</b> tab. 
3. Choose the <b>.hpi</b> file from your system or enter a URL to the archive file under the <b>Deploy Plugin</b> section. 
4. <b>Deploy</b> the plugin file.

## Manual Jenkins plugin installation (Installation via source code build and .hpi file upload)
1. Download the Jenkins zOS DevOps plugin source code from its [official GitHub repository](https://github.com/zowe/zowe-zdevops-jenkins-plugin)
2. It is necessary to build the project with the help of the Gradle Build Tool
3. Next, you need to generate an installation file: .hpi or .jpi file (both are installation files for the Jenkins plugin). This can be done by executing “gradle jpi” command. Or for example: <b>Gradle -> build -> jpi</b>
4. After building the .hpi/.jpi file, it should appear in a <b><Plugin-project-name>/build/libs/<hpi_file_name>.hpi</b> directory
5. Next you need to login into the Jenkins, move to the <b>“Manage Jenkins” -> “Manage Plugins” -> “Advanced (tab)” -> “Deploy Plugin”</b> (You can select a plugin file from your local system or provide a URL to install a plugin from outside the central plugin repository) <b>-> Specify the path to the generated .hpi/.jpi file</b> (or by dragging the file from Intellij IDEA project to the file upload field in the Jenkins).
6. Click <b>“Deploy”</b>, reboot Jenkins after installation. The Plugin is ready to go!

## Plugin configuration
After successfully installing the plugin, you need to configure it for further work - this will require a minimum of actions.
1. Move to “Manage Jenkins” -> “Configure System” -> scroll down and find the panel with the name - <b>“z/OS Connection List”</b>
2. This setting allows you to add all necessary z/OS systems and configure access to them.
It is necessary to set the connection name (it is also the ID for the call in the code). For the example: ```z/os-connection-name```
3. The URL address and port of the required mainframe to connect via z/OSMF. Example: ```https://<ip-addres>:<port number>```
4. Add credentials (Mainframe User ID + Password) under which you can connect to the system.

You can save as many connections as you like, the system will keep the corresponding user IDs/passwords.

## Use case
- Add a zosmf connection in settings (<b>Manage Jenkins -> Configure System -> z/OS Connection List</b>). Enter a connection name, zosmf url, username and password.
- Create a new item -> ```Pipeline``` and open its configuration.
  Create a <b>zosmf</b> section inside the <b>steps</b> of the <b>stage</b> and pass the connection name as a parameter of the section. Inside the zosmf body invoke necessary zosmf functions (they will be automatically done in a specified connection context). Take a look at the example below:
```groovy
stage ("stage-name") {
    steps {
        // ...
        zosmf("z/os-connection-name") {
            submitJob "//'EXAMPLE.DATASET(JCLJOB)'"
            submitJobSync "//'EXAMPLE.DATASET(JCLJOB)'"
            downloadDS "USER.LIB(MEMBER)"
            downloadDS dsn:"USER.LIB(MEMBER)", vol:"VOL001"
            allocateDS dsn:"STV.TEST5", alcUnit:"TRK", dsOrg:"PS", primary:1, secondary:1, recFm:"FB"
            writeFileToDS dsn:"USER.DATASET", file:"workspaceFile"
            writeFileToDS dsn:"USER.DATASET", file:"D:\\files\\localFile"
            writeToDS dsn:"USER.DATASET", text:"Write this string to dataset"
            writeFileToMember dsn:"USER.DATASET", member:"MEMBER", file:"workspaceFile"
            writeFileToMember dsn:"USER.DATASET", member:"MEMBER", file:"D:\\files\\localFile"
            writeToMember dsn:"USER.DATASET", member:"MEMBER", text:"Write this string to member"
        }
        // ...
    }
}
```

## How to run in Debug:
- ./gradlew server --debug-jvm
- wait until `Attach debugger` appears in console
- click it
- wait until Jenkins is deployed
- open `localhost:8080`
- enjoy
