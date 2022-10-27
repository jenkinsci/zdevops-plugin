# z/OS DevOps Jenkins plugin

## Main features
- Secure and modern connection to the mainframes through the use of zOSMF REST API
- The functionality is based on the Kotlin SDK methods, such as JCL jobs submission, download, allocate, write to the dataset, etc., with log collected upon finish
- Multiple connections to various mainframes - z/OS Connections List where you can save all the necessary systems and credentials in advance to access them, everything is hidden and safe, under the protection of Jenkins Credentials manager
- Agent-less solution
- Fast execution and functionality extensibility

## Plugin configuration
After successfully installing the plugin, you need to configure it for further work - this will require a minimum of actions.
1. Move to the <b>“Manage Jenkins” -> “Configure System” -> scroll down and find the panel with the name - “z/OS Connection List”</b> (as in the screenshot below)
   This setting allows to add all the necessary z/OS systems and configure access to them.
2. It is necessary to set the connection name (it is also the ID for the call in the code). For example bellow it would be: ```z/os-connection-name```
3. The URL address and port of the required mainframe to connect via z/OSMF. Example: ```https://<ip-addres>:<port number>```
4. Add credentials (Mainframe User ID + Password) under which you can connect to the system.

You can save as many connections as you like, so as not to remember them and keep the corresponding users/passwords.

## Use case
- Add a zosmf connection in settings (<b>Manage Jenkins -> Configure System -> z/OS Connection List</b>). Enter connection name, zosmf url, username and password.
- Create Pipeline plugin and open its configuration.
  Create a section <b>zosmf</b> inside <b>steps</b> of <b>stage</b> and pass connection name as a parameter of section. Inside zosmf body invoke necessary zosmf functions (they will be automatically done in specified connection context). Take a look at example below:
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

## Manual Jenkins plugin installation (Installation via source code build and .hpi file upload)
1. Download the Jenkins zOS DevOps plugin source code from its [official GitHub repository]()
2. It is necessary to build the project with the help of Gradle Build Tool
3. Next, you need to generate an installation file - .hpi or .jpi file (both are installation files for the jenkins plugin). This can be done by executing “gradle jpi” command. Or for example: <b>Gradle -> build -> jpi</b>
4. After building the .hpi/.jpi file, it should appear in a <b><Plugin-project-name>/build/libs/<hpi_file_name>.hpi</b> directory
5. Next you need to login into Jenkins, move to the <b>“Manage Jenkins” -> “Manage Plugins” -> “Advanced (tab)” -> “Deploy Plugin”</b> (You can select a plugin file from your local system or provide a URL to install a plugin from outside the central plugin repository) <b>-> Specify the path to the generated .hpi/.jpi file</b> (or by dragging the file from Intellij IDEA project to the file upload field in the Jenkins).
6. Click <b>“Deploy”</b>, reboot Jenkins after installation and plugin is ready to go!

## How to run in Debug:
- ./gradlew server --debug-jvm
- wait until `Attach debugger` appears in console
- click it
- wait until Jenkins is deployed
- open `localhost:8080`
- enjoy