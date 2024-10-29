package org.zowe.zdevops.classic.steps

import hudson.AbortException
import hudson.Extension
import hudson.Launcher
import hudson.model.AbstractBuild
import hudson.model.BuildListener
import hudson.util.FormValidation
import hudson.util.ListBoxModel
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter
import org.kohsuke.stapler.QueryParameter
import org.kohsuke.stapler.bind.JavaScriptMethod
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.Messages
import org.zowe.zdevops.classic.AbstractBuildStep
import org.zowe.zdevops.logic.writeToFile
import org.zowe.zdevops.utils.validateFieldIsNotEmpty
import java.io.File

class WriteFileToFileStep
@DataBoundConstructor
constructor(
    connectionName: String,
    val filePathUSS: String,
    var binary: Boolean = false,
    var fileOption: String?,
) : AbstractBuildStep(connectionName) {

    private var localFilePath: String? = null
    private var workspacePath: String? = null

    @DataBoundSetter
    fun setLocalFilePath(localFilePath: String?) {
        this.localFilePath = localFilePath
    }
    @DataBoundSetter
    fun setWorkspacePath(workspacePath: String?) {
        this.workspacePath = workspacePath
    }
    fun getLocalFilePath(): String? {
        return this.localFilePath
    }
    fun getWorkspacePath(): String? {
        return this.workspacePath
    }

    override fun perform(
        build: AbstractBuild<*, *>,
        launcher: Launcher,
        listener: BuildListener,
        zosConnection: ZOSConnection
    ) {
        val sourcePath = if(workspacePath != null) workspacePath else localFilePath
        listener.logger.println(Messages.zdevops_declarative_writing_file_from_file(filePathUSS, sourcePath, zosConnection.host, zosConnection.zosmfPort))
        val workspace = build.executor?.currentWorkspace
        val file = when (fileOption) {
            DescriptorImpl().localFileOption -> localFilePath?.let { File(it) }
            DescriptorImpl().workspaceFileOption ->  {
                val fileWorkspacePath = workspace?.remote + File.separator + workspacePath
                File(fileWorkspacePath)
            }
            else        -> throw AbortException(Messages.zdevops_classic_write_options_invalid())
        }
        val text = file?.readBytes()
        if (text != null) {
            writeToFile(listener, zosConnection, filePathUSS, text, binary)
        }
    }

    @Extension
    class DescriptorImpl :
        Companion.DefaultBuildDescriptor(Messages.zdevops_classic_writeFileToFileStep_display_name()) {
        private var lastStepId = 0
        private val marker: String = "WFTF"

        val chooseFileOption = "choose"
        val localFileOption = "local"
        val workspaceFileOption = "workspace"

        /**
         * Creates a unique step ID
         *
         * @return The generated step ID
         */
        @JavaScriptMethod
        @Synchronized
        fun createStepId(): String {
            return marker + lastStepId++.toString()
        }

        /**
         * Fills the file option items for the dropdown menu
         *
         * @return The ListBoxModel containing the file option items
         */
        fun doFillFileOptionItems(): ListBoxModel {
            val result = ListBoxModel()

            result.add(Messages.zdevops_classic_write_options_choose(), chooseFileOption)
            result.add(Messages.zdevops_classic_write_options_local(), localFileOption)
            result.add(Messages.zdevops_classic_write_options_workspace(), workspaceFileOption)

            return result
        }

        /**
         * Checks if the file option is valid
         *
         * @param fileOption The selected file option
         * @return FormValidation.ok() if the file option is valid, or an error message otherwise
         */
        fun doCheckFileOption(@QueryParameter fileOption: String): FormValidation? {
            if (fileOption == chooseFileOption || fileOption.isEmpty()) return FormValidation.error(Messages.zdevops_classic_write_options_required())
            return FormValidation.ok()
        }

        /**
         * Checks if the local file path not empty
         *
         * @param localFilePath The local file path
         * @param fileOption The selected file option
         * @return FormValidation.ok() if the local file path is not empty, or an error message otherwise
         */
        fun doCheckLocalFilePath(@QueryParameter localFilePath: String,
                                 @QueryParameter fileOption:    String): FormValidation? {
            return if (fileOption == localFileOption) validateFieldIsNotEmpty(localFilePath)
            else FormValidation.ok()
        }

        /**
         * Checks if the workspace path is not empty
         *
         * @param workspacePath The workspace path
         * @param fileOption The selected file option
         * @return FormValidation.ok() if the workspace path is not empty, or an error message otherwise
         */
        fun doCheckWorkspacePath(@QueryParameter workspacePath: String,
                                 @QueryParameter fileOption:    String): FormValidation? {
            return if (fileOption == workspaceFileOption) validateFieldIsNotEmpty(workspacePath)
            else FormValidation.ok()
        }
    }
}