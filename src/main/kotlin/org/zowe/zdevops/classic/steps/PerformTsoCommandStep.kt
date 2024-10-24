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
import org.zowe.zdevops.logic.performTsoCommand
import org.zowe.zdevops.utils.validateFieldIsNotEmpty


/**
 * A Jenkins Pipeline step for performing a TSO (Time Sharing Option) command on a z/OS system via freestyle job.
 */
class PerformTsoCommandStep
/**
 * Data-bound constructor for the {@code PerformTsoCommandStep} step.
 *
 * @param connectionName The name of the z/OS connection to be used for executing the TSO command.
 * @param acct The z/OS account number.
 * @param command The TSO command to be executed.
 */
@DataBoundConstructor
constructor(
    connectionName: String,
    val acct: String,
    val command: String,
) : AbstractBuildStep(connectionName) {

    /**
     * Performs the TSO command execution step within a Jenkins Pipeline build.
     *
     * @param build The current Jenkins build.
     * @param launcher The build launcher.
     * @param listener The build listener.
     * @param zosConnection The z/OS connection to execute the TSO command.
     */
    override fun perform(
        build: AbstractBuild<*, *>,
        launcher: Launcher,
        listener: BuildListener,
        zosConnection: ZOSConnection
    ) {
        performTsoCommand(zosConnection, listener, acct, command)
    }


    /**
     * Descriptor for the {@code PerformTsoCommandStep} step.
     *
     * This descriptor provides information about the step and makes it available for use
     * within Jenkins Pipelines.
     */
    @Extension
    class DescriptorImpl : Companion.DefaultBuildDescriptor(Messages.zdevops_classic_performTsoCommandStep_display_name()) {

        /**
         * Performs form validation for the 'acct' parameter to ensure it is not empty.
         *
         * @param acct The z/OS account number field value to validate.
         * @return A {@link FormValidation} object indicating whether the field is valid or contains an error.
         */
        fun doCheckAcct(@QueryParameter acct: String): FormValidation? {
            return validateFieldIsNotEmpty(acct)
        }

        /**
         * Performs form validation for the 'command' parameter to ensure it is not empty.
         *
         * @param command The TSO command field value to validate.
         * @return A {@link FormValidation} object indicating whether the field is valid or contains an error.
         */
        fun doCheckCommand(@QueryParameter command: String): FormValidation? {
            return validateFieldIsNotEmpty(command)
        }

    }
}