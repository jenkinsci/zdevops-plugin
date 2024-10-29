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
import org.zowe.zdevops.logic.writeToDataset
import org.zowe.zdevops.utils.validateDatasetName
import org.zowe.zdevops.utils.validateFieldIsNotEmpty

/**
 * A build step for writing text(string) to a dataset
 */
class WriteToDatasetStep
/**
 * Constructs a new instance of WriteToDatasetStep
 *
 * @param connectionName The name of the connection
 * @param dsn The name of the dataset
 * @param text The text to write to the dataset
 */
@DataBoundConstructor
constructor(
    connectionName: String,
    val dsn: String,
    val text: String
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
        listener.logger.println(Messages.zdevops_declarative_writing_DS_from_input(dsn, zosConnection.host, zosConnection.zosmfPort))
        writeToDataset(listener, zosConnection, dsn, text)
    }

    /**
     * The descriptor for the WriteToDatasetStep
     */
    @Extension
    class DescriptorImpl : Companion.DefaultBuildDescriptor(Messages.zdevops_classic_writeToDSStep_display_name()) {
        /**
         * Validates the dataset name
         *
         * @param dsn The dataset name
         * @return FormValidation.ok() if the dataset name is valid, or an error message otherwise
         */
        fun doCheckDsn(@QueryParameter dsn: String): FormValidation? {
            return validateDatasetName(dsn)
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