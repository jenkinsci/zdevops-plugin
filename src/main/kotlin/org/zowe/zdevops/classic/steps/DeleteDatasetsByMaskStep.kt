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
import org.kohsuke.stapler.DataBoundConstructor
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.Messages
import org.zowe.zdevops.classic.AbstractBuildStep
import org.zowe.zdevops.logic.deleteDatasetsByMask

class DeleteDatasetsByMaskStep
/**
 * Constructs a new instance of the DeleteDatasetsByMaskStep.
 *
 * @param connectionName The name of the z/OS connection to use for deleting the dataset.
 * @param dsnMask        The name of a mask to find datasets for deletion.
 */
@DataBoundConstructor
constructor(
    connectionName: String,
    val dsnMask: String,
    val failOnNotExist: Boolean = false,
) : AbstractBuildStep(connectionName) {

    override fun perform(
        build: AbstractBuild<*, *>,
        launcher: Launcher,
        listener: BuildListener,
        zosConnection: ZOSConnection
    ) {
        deleteDatasetsByMask(dsnMask, zosConnection, listener, failOnNotExist)
    }

    @Extension
    class DescriptorImpl :
        Companion.DefaultBuildDescriptor(Messages.zdevops_classic_deleteDatasetsByMaskStep_display_name()) {}
}