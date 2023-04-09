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
import org.zowe.kotlinsdk.zowe.client.sdk.zosuss.ZosUssFile
import io.jenkins.plugins.zdevops.declarative.AbstractZosmfAction
import hudson.*
import hudson.FilePath
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter
import java.io.File
import java.nio.file.Paths

class WriteFileToFileDeclarative @DataBoundConstructor constructor(private val destFile: String,
                                                                   private val sourceFile: String) :
    AbstractZosmfAction() {

    private var binary: Boolean? = false

    @DataBoundSetter
    fun setBinary(binary: Boolean) { this.binary = binary }

    override val exceptionMessage: String = zMessages.zdevops_declarative_writing_file_fail(destFile)

    override fun perform(
        run: Run<*, *>,
        workspace: FilePath,
        env: EnvVars,
        launcher: Launcher,
        listener: TaskListener,
        zosConnection: ZOSConnection
    ) {
        listener.logger.println(zMessages.zdevops_declarative_writing_file_from_file(destFile, sourceFile, zosConnection.host, zosConnection.zosmfPort))
        val filePath = Paths.get(sourceFile)
        val textFile = if (filePath.isAbsolute) {
            File(sourceFile)
        } else {
            val workspacePath = workspace.remote.replace(workspace.name, "")
            File("$workspacePath$sourceFile")
        }

        val text = textFile.readBytes()
        if (binary == true) {
            ZosUssFile(zosConnection).writeToFileBin(destFile, text)
        } else {
            ZosUssFile(zosConnection).writeToFile(destFile, text)
        }
        listener.logger.println(zMessages.zdevops_declarative_writing_file_success(destFile))
    }


    @Symbol("writeFileToFile")
    @Extension
    class DescriptorImpl : Companion.DefaultBuildDescriptor("Write file to Unix file Declarative")
}
