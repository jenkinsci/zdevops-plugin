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

class WriteToFileDeclarative @DataBoundConstructor constructor(private val destFile: String,
                                                               private val text: String) :
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
        if (text != "") {
            listener.logger.println(zMessages.zdevops_declarative_writing_file_from_input(destFile, zosConnection.host, zosConnection.zosmfPort))
            if (binary == true) {
                ZosUssFile(zosConnection).writeToFileBin(destFile, text.toByteArray())
            } else {
                ZosUssFile(zosConnection).writeToFile(destFile, text.toByteArray())
            }
            listener.logger.println(zMessages.zdevops_declarative_writing_file_success(destFile))
        } else {
            listener.logger.println(zMessages.zdevops_declarative_writing_skip())
        }
    }


    @Symbol("writeToFile")
    @Extension
    class DescriptorImpl : Companion.DefaultBuildDescriptor("Write to Unix file Declarative")
}
