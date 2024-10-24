/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2022
 */

package org.zowe.zdevops.declarative.jobs

import hudson.*
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.declarative.AbstractZosmfAction
import org.zowe.zdevops.logic.deleteDatasetOrMember

/**
 * This class contains delete mainframe dataset operation description
 * which can be used in jenkins declarative script
 *
 * To delete a dataset, specify its name via dsn parameter:
 * ```
 * deleteDataset dsn:"USER1A.TEST.HELLO"
 * ```
 * And out will be:
 * ```
 * Deleting dataset USER1A.TEST.HELLO with connection 172.20.2.2:10443
 * Successfully deleted
 * ```
 * You cannot delete a VSAM dataset this way. Otherwise, you will get output similar to:
 * ```
 * Deleting dataset USER1A.TEST.KSDS with connection 172.20.2.2:10443
 * ISRZ002 Deallocation failed - Deallocation failed for data set 'USER1A.TEST.KSDS'
 * ```
 * To delete a member from the library, the dsn and member parameters must be specified:
 * ```
 * deleteDataset dsn:"USER1A.TEST.LIB", member:"MEMBER1"
 * ```
 * And out will be:
 * ```
 * Deleting member MEMBER1 from dataset "USER1A.JCL.LIB" with connection 172.20.2.2:10443
 * Successfully deleted
 * ```
 * What do you get if a dataset does not exist?
 * ```
 * Deleting dataset USER1A.DS.DOES.NOT.EXIST with connection 172.20.2.2:10443
 * ISRZ002 Data set not cataloged - 'USER1A.DS.DOES.NOT.EXIST' was not found in catalog.
 * ```
 * What do you get if a dataset is busy by a user or a program?
 * ```
 * Deleting dataset USER1A.DS.ISUSED.BY.USER with connection 172.20.2.2:10443
 * ISRZ002 Data set in use - Data set 'USER1A.DS.ISUSED.BY.USER' in use by another user, try later or enter HELP for a list of jobs and users allocated to 'USER1A.DS.ISUSED.BY.USER'.
 * ```
 * It can take 2 params:
 * @param dsn dataset name - sequential or library
 * @param member dataset member name
 */
class DeleteDatasetDeclarative @DataBoundConstructor constructor(
) : AbstractZosmfAction() {

    private var dsn: String = ""
    private var member: String = ""
    private var failOnNotExist: Boolean = false

    @DataBoundSetter
    fun setDsn(dsn: String) { this.dsn = dsn }

    @DataBoundSetter
    fun setMember(member: String) { this.member = member }

    @DataBoundSetter
    fun setFailOnNotExist(failOnNotExist: Boolean) { this.failOnNotExist = failOnNotExist }

    override val exceptionMessage: String = zMessages.zdevops_deleting_ds_fail()

    override fun perform(
        run: Run<*, *>,
        workspace: FilePath,
        env: EnvVars,
        launcher: Launcher,
        listener: TaskListener,
        zosConnection: ZOSConnection
    ) {
        deleteDatasetOrMember(dsn, member, zosConnection, listener, failOnNotExist)
    }

    @Symbol("deleteDataset")
    @Extension
    class DescriptorImpl : Companion.DefaultBuildDescriptor("Delete Dataset or Dataset member Declarative")
}
