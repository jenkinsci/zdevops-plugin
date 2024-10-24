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
import org.kohsuke.stapler.DataBoundSetter
import org.kohsuke.stapler.QueryParameter
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.Messages
import org.zowe.zdevops.classic.AbstractBuildStep
import org.zowe.zdevops.logic.downloadDSOrDSMemberByType
import org.zowe.zdevops.utils.validateDsnOrDsnMemberName

/**
 * The DownloadDatasetStep class is a build step that downloads a dataset or dataset members from a z/OS system.
 */
class DownloadDatasetStep
/**
 * Constructs a new instance of the DownloadDatasetStep.
 *
 * @param connectionName The name of the z/OS connection to use for downloading the dataset.
 * @param dsn            The name of the dataset or the dataset member to download.
 */
@DataBoundConstructor
constructor(
    connectionName: String,
    val dsn: String
) :AbstractBuildStep(connectionName) {

    private var vol: String? = null
    private var returnEtag: Boolean? = true

    @DataBoundSetter
    fun setVol(vol: String) {
        this.vol = if (vol.isNullOrBlank()) null else vol
    }

    @DataBoundSetter
    fun setReturnEtag(returnEtag: Boolean) { this.returnEtag = returnEtag }

    fun getVol(): String? = this.vol

    fun getReturnEtag(): Boolean? = this.returnEtag

    /**
     * Performs the download a dataset or a dataset member step.
     *
     * @param build          The build object.
     * @param launcher       The launcher object.
     * @param listener       The build listener.
     * @param zosConnection  The connection to the z/OS system.
     */
    override fun perform(
        build: AbstractBuild<*, *>,
        launcher: Launcher,
        listener: BuildListener,
        zosConnection: ZOSConnection
    ) {
        val workspace = build.executor?.currentWorkspace!!
        downloadDSOrDSMemberByType(dsn, vol, returnEtag, listener, zosConnection, workspace)
    }


    /**
     * The descriptor for the DownloadDatasetStep class.
     */
    @Extension
    class DescriptorImpl : Companion.DefaultBuildDescriptor(Messages.zdevops_classic_downloadDatasetStep_display_name()) {
        /**
         * Checks if the dataset name is valid
         *
         * @param dsn The dataset name
         * @return FormValidation.ok() if the dataset name is valid, or an error message otherwise
         */
        fun doCheckDsn(@QueryParameter dsn: String): FormValidation? {
            return validateDsnOrDsnMemberName(dsn)
        }

        /**
         * Checks if the volume name is valid.
         *
         * @param vol The volume name.
         * @return FormValidation.ok() if the volume name is valid, or a warning message if it seems invalid.
         */
        fun doCheckVol(@QueryParameter vol: String): FormValidation? {
            return if (vol.length > 7) FormValidation.warning(Messages.zdevops_volume_name_is_invalid_validation())
            else FormValidation.ok()
        }

    }
}