/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2023
 */

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
import org.zowe.zdevops.logic.writeToMember
import org.zowe.zdevops.utils.validateDatasetName
import org.zowe.zdevops.utils.validateFieldIsNotEmpty
import org.zowe.zdevops.utils.validateMemberName
import java.io.File

/**
 * A build step for writing a file to a member in a dataset
 *
 * @see org.zowe.zdevops.logic.WriteOperation
 */
class WriteFileToMemberStep
/**
 * Constructs a new instance of WriteFileToMemberStep.
 *
 * @param connectionName The name of the z/OS connection
 * @param dsn The name of the dataset
 * @param member The name of the member
 * @param fileOption The option for selecting the file source
 */
@DataBoundConstructor
constructor(
    connectionName: String,
    val dsn: String,
    val member: String,
    var fileOption: String?,
): AbstractBuildStep(connectionName) {

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

    /**
     * Performs the write operation
     *
     * @param build The build object
     * @param launcher The launcher for executing commands
     * @param listener The listener for logging messages
     * @param zosConnection The ZOSConnection for interacting with z/OS
     */
    override fun perform(
        build: AbstractBuild<*, *>,
        launcher: Launcher,
        listener: BuildListener,
        zosConnection: ZOSConnection,
    ) {
        val workspace = build.executor?.currentWorkspace
        val file = when (fileOption) {
            DescriptorImpl().localFileOption -> localFilePath?.let { File(it) }
            DescriptorImpl().workspaceFileOption ->  {
                val fileWorkspacePath = workspace?.remote + '\\' + workspacePath
                File(fileWorkspacePath)
            }
            else        -> throw AbortException(Messages.zdevops_classic_write_options_invalid())
        }
        listener.logger.println(Messages.zdevops_declarative_writing_DS_from_file(dsn, file?.name, zosConnection.host, zosConnection.zosmfPort))
        val fileContent = file?.readText()
        if (fileContent != null) {
            writeToMember(listener, zosConnection, dsn, member, fileContent)
        }
    }

    /**
     * The descriptor for the WriteFileToMemberStep
     */
    @Extension
    class DescriptorImpl :
        Companion.DefaultBuildDescriptor(Messages.zdevops_classic_writeFileToMemberStep_display_name()) {

        private var lastStepId = 0
        private val marker: String = "WFTM"

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
         * Checks if the dataset name is valid
         *
         * @param dsn The dataset name
         * @return FormValidation.ok() if the dataset name is valid, or an error message otherwise
         */
        fun doCheckDsn(@QueryParameter dsn: String): FormValidation? {
            return validateDatasetName(dsn)
        }

        /**
         * Validates the member name
         *
         * @param member The member name
         * @return FormValidation.ok() if the member name is valid, or an error message otherwise
         */
        fun doCheckMember(@QueryParameter member: String): FormValidation? {
            return validateMemberName(member)?: validateFieldIsNotEmpty(member)
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