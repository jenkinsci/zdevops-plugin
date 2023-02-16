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
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.ZosDsn
import io.jenkins.plugins.zdevops.declarative.AbstractZosmfAction
import hudson.*
import hudson.FilePath
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor

class WriteToMemberDeclarative @DataBoundConstructor constructor(private val dsn: String,
                                                                 private val member: String,
                                                                 private val text: String) :
    AbstractZosmfAction() {

    override val exceptionMessage: String = zMessages.zdevops_declarative_writing_DS_fail(dsn)

    override fun perform(
        run: Run<*, *>,
        workspace: FilePath,
        env: EnvVars,
        launcher: Launcher,
        listener: TaskListener,
        zosConnection: ZOSConnection
    ) {
        if (text != "") {
            listener.logger.println(zMessages.zdevops_declarative_writing_DS_from_input(dsn, zosConnection.host, zosConnection.zosmfPort))

            val stringList = text.split('\n')
            val targetDS = ZosDsn(zosConnection).getDatasetInfo(dsn)
            if (targetDS.recordLength == null) {
                throw AbortException(zMessages.zdevops_declarative_writing_DS_no_info(dsn))
            }
            var ineligibleStrings = 0
            stringList.forEach {
                if (it.length > targetDS.recordLength!!) {
                    ineligibleStrings++
                }
            }
            if (ineligibleStrings > 0) {
                throw AbortException(zMessages.zdevops_declarative_writing_DS_ineligible_strings(ineligibleStrings,dsn))
            } else {
                val textByteArray = text.replace("\r","").toByteArray()
                val writeToDS = ZosDsn(zosConnection).writeDsn(dsn, member, textByteArray)
                listener.logger.println(zMessages.zdevops_declarative_writing_DS_success(dsn))
            }
        } else {
            listener.logger.println(zMessages.zdevops_declarative_writing_skip())
        }
    }


    @Symbol("writeToMember")
    @Extension
    class DescriptorImpl : Companion.DefaultBuildDescriptor("Write to Dataset Member Declarative")
}
