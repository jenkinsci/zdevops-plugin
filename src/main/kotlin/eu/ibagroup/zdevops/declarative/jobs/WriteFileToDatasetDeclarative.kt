package eu.ibagroup.zdevops.declarative.jobs

import eu.ibagroup.r2z.zowe.client.sdk.core.ZOSConnection
import eu.ibagroup.r2z.zowe.client.sdk.zosfiles.ZosDsn
import eu.ibagroup.zdevops.declarative.AbstractZosmfAction
import hudson.*
import hudson.FilePath
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import java.io.File
import java.nio.file.Paths

class WriteFileToDatasetDeclarative @DataBoundConstructor constructor(private val dsn: String,
                                                                      private val file: String) :
    AbstractZosmfAction() {

    override val exceptionMessage: String = zMessages.zdevops_declarative_writing_DS_fail(dsn)

    override fun perform(
        run: Run<*, *>,
        workspace: FilePath,
        env: EnvVars,
        launcher: Launcher,
        listener: TaskListener,
        zosConnection: ZOSConnection
    ) {
        listener.logger.println(zMessages.zdevops_declarative_writing_DS_from_file(dsn, file, zosConnection.host, zosConnection.zosmfPort))
        val filePath = Paths.get(file)
        val textFile = if (filePath.isAbsolute) {
            File(file)
        } else {
            val workspacePath = workspace.remote.replace(workspace.name, "")
            File("$workspacePath$file")
        }

        val targetDS = ZosDsn(zosConnection).getDatasetInfo(dsn)
        if (targetDS.recordLength == null) {
            throw AbortException(zMessages.zdevops_declarative_writing_DS_no_info(dsn))
        }
        var ineligibleStrings = 0
        textFile.readLines().forEach {
            if (it.length > targetDS.recordLength!!) {
                ineligibleStrings++
            }
        }
        if (ineligibleStrings > 0) {
            throw AbortException(zMessages.zdevops_declarative_writing_DS_ineligible_strings(ineligibleStrings,dsn))
        } else {
            val textString = textFile.readText().replace("\r","")
            val writeToDS = ZosDsn(zosConnection).writeDsn(dsn, textString.toByteArray())
            listener.logger.println(zMessages.zdevops_declarative_writing_DS_success(dsn))
        }
    }


    @Symbol("writeFileToDS")
    @Extension
    class DescriptorImpl : Companion.DefaultBuildDescriptor("Write file to Dataset Declarative")
}

