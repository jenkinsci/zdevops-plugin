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

import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.ZosDsn
import org.zowe.zdevops.declarative.AbstractZosmfAction
import hudson.*
import hudson.FilePath
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import java.io.File
import java.nio.file.Paths

class WriteFIleToMemberDeclarative @DataBoundConstructor constructor(private val dsn: String,
                                                                     private val member: String,
                                                                     private val file: String) :
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
        listener.logger.println(zMessages.zdevops_declarative_writing_DS_from_file(dsn, file, zosConnection.host, zosConnection.zosmfPort))
        val filePath = Paths.get(file)
        val textFile = if (filePath.isAbsolute) {
            File(file)
        } else {
            val workspacePath = workspace.remote.replace(workspace.name, "")
            File("$workspacePath$file")
        }

        val targetDS = ZosDsn(zosConnection).getDatasetInfo(dsn)
        val targetDSLRECL = targetDS.recordLength ?: throw AbortException(zMessages.zdevops_declarative_writing_DS_no_info(dsn))
        val ineligibleStrings = textFile
            .readLines()
            .map { it.length }
            .fold(0) { result, currStrLength -> if (currStrLength > targetDSLRECL) result + 1 else result }
        if (ineligibleStrings > 0) {
            throw AbortException(zMessages.zdevops_declarative_writing_DS_ineligible_strings(ineligibleStrings, dsn))
        }
        val textString = textFile.readText().replace("\r","")
        ZosDsn(zosConnection).writeDsn(dsn, member, textString.toByteArray())
        listener.logger.println(zMessages.zdevops_declarative_writing_DS_success(dsn))
    }


    @Symbol("writeFileToMember")
    @Extension
    class DescriptorImpl : Companion.DefaultBuildDescriptor("Write file to Dataset Member Declarative")
}
