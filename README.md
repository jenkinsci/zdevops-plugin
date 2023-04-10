# Zowe zDevOps Jenkins plugin

## About  the plugin
The Zowe zDevOps Jenkins Plugin by [IBA Group](https://ibagroupit.com/?utm_campaign=IBA_W-Mainframe&utm_source=jenkins&utm_medium=referral&utm_content=description_zdevops) is an open-source, secure , and reliable agent-less Jenkins plugin that makes it possible to perform most of the actual tasks on the mainframe, managing it with a modern native mainframe zOSMF REST API and the capabilities of available zOSMF SDKs.

## Advantages
- Secure and modern connection of Jenkins to the mainframes through the use of zOSMF REST API.
- The functionality is based on the Kotlin SDK methods, such as JCL jobs submission, download, allocate, write to the dataset, etc., with a log collected upon completion.
- Multiple connections to various mainframes—z/OS Connections List where you can save all the necessary systems and credentials; all data is safely stored under the protection of Jenkins Credentials manager.
- Agent-less.
- z/OSMF connection validation.
- Fast execution and functional extensibility.

## About us
With over 30 years of experience in the mainframe domain, IBA Group is committed to maximizing your mainframe investment and enhancing your IT flexibility.

One of the services we offer is Mainframe DevOps. Our approach is highly flexible, as we work with customers to choose the essential toolset for establishing a pipeline based on their preferences, existing tools, and the latest open-source opportunities (such as Zowe and plugins).

We are also familiar with [Mainframe DevOps Solutions](https://mainframe.ibagroupit.com/?utm_campaign=IBA_W-Mainframe&utm_source=jenkins&utm_medium=referral&utm_content=description_zdevops) of 2023 that can help modernize your mainframe and keep you competitive.

We value your feedback and welcome any suggestions, proposals, or even complaints.

Please feel free to contact us or schedule a call with our Mainframe DevOps expert.

Thank you for considering IBA Group for your mainframe needs.

## Manual plugin installation by the .hpi executable file
The plugin are packaged as self-contained <b>.hpi</b> files, which have all the necessary code, images, and other resources which the plugin needs to operate successfully.

### <b>Already packaged and tested installation .hpi file can be downloaded from a link from a nearby GitHub repository:</b>
### <b>[Zowe zDevOps plugin installation .hpi file](https://github.com/IBA-mainframe-dev/Global-Repository-for-Mainframe-Developers/blob/master/Jenkins%20zOS%20DevOps%20plugin%20installable%20hpi/zos-devops.hpi)</b>

Assuming a <b>.hpi</b> file has been downloaded, a logged-in Jenkins administrator may upload the file from within the web UI:
1. Navigate to the <b>Manage Jenkins > Manage Plugins</b> page in the web UI.
2. Click on the <b>Advanced</b> tab.
3. Choose the <b>.hpi</b> file from your system or enter a URL to the archive file under the <b>Deploy Plugin</b> section.
4. <b>Deploy</b> the plugin file.

## Manual Jenkins plugin installation (Installation via source code build and .hpi file upload)
1. Download the Zowe zDevOps Jenkins plugin source code from its [official GitHub repository](https://github.com/jenkinsci/zdevops-plugin)
2. It is necessary to build the project with the help of the Maven Build Tool
3. To generate the ```target``` dir with generated-sources - you have to run the Maven command: ```mvn localizer:generate```
4. Next, you need to generate an installation file: .hpi or .jpi file (both are installation files for the Jenkins plugin). This can be done by executing Maven command ```mvn install``` or by ```mvn hpi:hpi```.
5. After building the .hpi/.jpi file, it should appear in a <b><Plugin-project-name>/build/libs/<hpi_file_name>.hpi</b> directory
6. Next you need to login into the Jenkins, move to the <b>“Manage Jenkins” -> “Manage Plugins” -> “Advanced (tab)” -> “Deploy Plugin”</b> (You can select a plugin file from your local system or provide a URL to install a plugin from outside the central plugin repository) <b>-> Specify the path to the generated .hpi/.jpi file</b> (or by dragging the file from Intellij IDEA project to the file upload field in the Jenkins).
7. Click <b>“Deploy”</b>, reboot Jenkins after installation. The Plugin is ready to go!

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

        writeToFile destFile: "u/USER/doc", text: "Hello there"
        writeFileToFile destFile: "u/USER/doc", sourceFile: "myfile.txt"
        writeFileToFile destFile: "u/USER/doc", sourceFile: "myfile.txt", binary: "true"

        deleteDataset dsn:"USER.DATASET"
        deleteDataset dsn:"USER.DATASET", member:"MEMBER1"
        deleteDatasetsByMask mask:"USER.DATASET.*"
    }
    // ...
  }
}
```

## How to run Jenkins plugin in Debug mode in a local Jenkins sandbox

For debugging purposes run following Maven command from plugin project directory:

```mvn hpi:run```

Or by ```mvnDebug hpi:run``` - this will copy all the dependencies down (rather than in your jenkins install) and run it in place.

By default, the debugging instance is then available at [http://localhost:8080/jenkins](http://localhost:8080/jenkins) in your browser.

In order to launch Jenkins on a different port than 8080 use this system property:

```mvn hpi:run -Djetty.port=8090```

Changing the default context path can be achieved by setting this system property:

```mvn hpi:run -Dhpi.prefix=/debug```