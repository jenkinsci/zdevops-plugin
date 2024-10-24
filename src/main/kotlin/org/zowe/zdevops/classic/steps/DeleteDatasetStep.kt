/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2022
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
import org.zowe.zdevops.logic.deleteDatasetOrMember
import org.zowe.zdevops.utils.validateDatasetName
import org.zowe.zdevops.utils.validateMemberName

class DeleteDatasetStep
/**
 * Constructs a new instance of the DeleteDatasetStep.
 *
 * @param connectionName The name of the z/OS connection to use for deleting the dataset.
 * @param dsn            The name of the dataset to delete.
 * @param member         The name of the member to delete.
 */
@DataBoundConstructor
constructor(
    connectionName: String,
    val dsn: String,
    val member: String?,
    val failOnNotExist: Boolean = false ,
) : AbstractBuildStep(connectionName) {

    override fun perform(
        build: AbstractBuild<*, *>,
        launcher: Launcher,
        listener: BuildListener,
        zosConnection: ZOSConnection
    ) {
        deleteDatasetOrMember(dsn, member, zosConnection, listener, failOnNotExist)
    }

    @Extension
    class DescriptorImpl :
        Companion.DefaultBuildDescriptor(Messages.zdevops_classic_deleteDatasetStep_display_name()) {

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
         * Checks if the member name is valid
         *
         * @param member The dataset member name
         * @return FormValidation.ok() if either the member name is valid or is not provided, or an error message otherwise
         */
        fun doCheckMember(@QueryParameter member: String): FormValidation? {
            return if (member.isNotBlank()) {
                validateMemberName(member)
            } else {
                FormValidation.ok()
            }
        }
    }
}