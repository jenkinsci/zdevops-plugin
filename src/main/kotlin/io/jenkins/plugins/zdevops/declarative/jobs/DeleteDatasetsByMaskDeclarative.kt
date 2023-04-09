/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2022
 */

package io.jenkins.plugins.zdevops.declarative.jobs

import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import io.jenkins.plugins.zdevops.declarative.AbstractZosmfAction
import io.jenkins.plugins.zdevops.logic.DeleteOperation
import hudson.*
import hudson.FilePath
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter

/**
 * This class contains bulk delete mainframe datasets operation description
 * which can be used in jenkins declarative script
 *
 * To delete datasets that match a mask, you must specify the mask parameter:
 * ```
 * deleteDatasetsByMask mask:"USER1A.TEST%.HELLO.DS%"
 * ```
 * And output will be similar to:
 * ```
 * Deleting datasets that match the mask "USER1A.TEST%.HELLO.DS%"
 * Deleting dataset USER1A.TEST1.HELLO.DS1 with connection 172.20.2.2:10443
 * Deleting dataset USER1A.TEST1.HELLO.DS2 with connection 172.20.2.2:10443
 * Deleting dataset USER1A.TEST2.HELLO.DS3 with connection 172.20.2.2:10443
 * Successfully deleted
 * ```
 * If it cannot find datasets that match the mask, an exception will be thrown.
 *
 * It is not possible to delete members from the library by mask.
 *
 * What do you get if a dataset does not exist?
 * ```
 * Deleting datasets that match the mask "USER1A.DS.DOES.NOT.EXIST"
 * hudson.AbortException: No data sets matching the mask were found
 * ```
 * What do you get if a dataset is busy by a user or a program?
 * ```
 * ISRZ002 Data set in use - Data set 'USER1A.DS.ISUSED.BY.USER' in use by another user, try later or enter HELP for a list of jobs and users allocated to 'USER1A.DS.ISUSED.BY.USER'.
 * ```
 * It takes 1 param:
 * @param mask a mask that will be used to delete datasets
 */
class DeleteDatasetsByMaskDeclarative @DataBoundConstructor constructor(
) : AbstractZosmfAction() {

    private var mask: String = ""

    @DataBoundSetter
    fun setMask(mask: String) { this.mask = mask }

    override val exceptionMessage: String = zMessages.zdevops_deleting_ds_fail()

    override fun perform(
        run: Run<*, *>,
        workspace: FilePath,
        env: EnvVars,
        launcher: Launcher,
        listener: TaskListener,
        zosConnection: ZOSConnection
    ) {
        DeleteOperation.deleteDatasetsByMask(mask, zosConnection,listener)
    }


    @Symbol("deleteDatasetsByMask")
    @Extension
    class DescriptorImpl : Companion.DefaultBuildDescriptor("Delete Datasets (bulk) by mask Declarative")

}
