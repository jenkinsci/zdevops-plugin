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

import hudson.Extension
import hudson.Launcher
import hudson.model.AbstractBuild
import hudson.model.BuildListener
import hudson.util.FormValidation
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.QueryParameter
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.Messages
import org.zowe.zdevops.classic.AbstractBuildStep
import org.zowe.zdevops.logic.writeToFile
import org.zowe.zdevops.utils.validateFieldIsNotEmpty

/**
 * A build step for writing text(string) to a USS file
 */
class WriteToFileStep
/**
 * Constructs a new instance of WriteToFileStep.
 *
 * @param filePath The name of the z/OS connection
 * @param text The name of the dataset
 * @param binary The name of the member
 */
@DataBoundConstructor
constructor(
    connectionName: String,
    val filePath: String,
    val text: String,
    var binary: Boolean = false,
) : AbstractBuildStep(connectionName) {

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
        zosConnection: ZOSConnection
    ) {
        listener.logger.println(Messages.zdevops_declarative_writing_file_from_input(filePath, zosConnection.host, zosConnection.zosmfPort))
        val textBytes = text.toByteArray()
        writeToFile(listener, zosConnection, filePath, textBytes, binary)
    }

    /**
     * The descriptor for the WriteToFileStep
     */
    @Extension
    class DescriptorImpl : Companion.DefaultBuildDescriptor(Messages.zdevops_classic_writeToFileStep_display_name()) {
        /**
         * Validates the file path
         *
         * @param filePath The file path to write to
         * @return FormValidation.ok() if the path is not empty, or an error message otherwise
         */
        fun doCheckFilePath(@QueryParameter filePath: String): FormValidation? {
            return validateFieldIsNotEmpty(filePath)
        }

        /**
         * Validates the text to write
         *
         * @param text The text to write
         * @return FormValidation.ok() if the text is not empty, or an error message otherwise
         */
        fun doCheckText(@QueryParameter text: String): FormValidation? {
            return validateFieldIsNotEmpty(text)
        }

    }
}