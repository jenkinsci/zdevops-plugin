/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2023
 */

package org.zowe.zdevops.declarative.jobs

import hudson.*
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.declarative.AbstractZosmfAction
import org.zowe.zdevops.logic.performTsoCommand

/**
 * A Jenkins Pipeline step for performing a TSO (Time Sharing Option) command on a z/OS system
 * using the Declarative Pipeline syntax.
 *
 * This step allows you to execute TSO commands on a z/OS system and provides integration with
 * Jenkins Pipelines for mainframe automation.
 */
class PerformTsoCommandDeclarative
/**
 * Data-bound constructor for the {@code PerformTsoCommandDeclarative} step.
 *
 * @param acct The z/OS account under which to run the TSO command.
 * @param command The TSO command to be executed.
 */
@DataBoundConstructor
constructor(
    val acct: String,
    val command: String,
) : AbstractZosmfAction() {

    override val exceptionMessage: String = zMessages.zdevops_TSO_command_fail()

    /**
     * Performs the TSO command execution step within a Jenkins Pipeline.
     *
     * @param run The current Jenkins build run.
     * @param workspace The workspace where the build is executed.
     * @param env The environment variables for the build.
     * @param launcher The build launcher.
     * @param listener The build listener.
     * @param zosConnection The z/OS connection to execute the TSO command.
     */
    override fun perform(
        run: Run<*, *>,
        workspace: FilePath,
        env: EnvVars,
        launcher: Launcher,
        listener: TaskListener,
        zosConnection: ZOSConnection
    ) {
        performTsoCommand(zosConnection, listener, acct, command)
    }

    /**
     * Descriptor for the {@code PerformTsoCommandDeclarative} step.
     *
     * This descriptor provides information about the step and makes it available for use
     * within Jenkins Pipelines.
     */
    @Symbol("performTsoCommand")
    @Extension
    class DescriptorImpl : Companion.DefaultBuildDescriptor("Perform TSO command Declarative")
}