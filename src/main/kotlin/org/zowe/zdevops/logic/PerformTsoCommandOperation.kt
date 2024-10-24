/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2023
 */

package org.zowe.zdevops.logic

import hudson.AbortException
import hudson.model.TaskListener
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.kotlinsdk.zowe.client.sdk.zostso.IssueTso
import org.zowe.kotlinsdk.zowe.client.sdk.zostso.input.StartTsoParams
import org.zowe.zdevops.Messages

/**
 * Executes a TSO (Time Sharing Option) command on a z/OS system using the provided z/OS connection.
 *
 * This function allows you to send a TSO command to a z/OS system, and capture the response
 *
 * @param zosConnection The z/OS connection through which the TSO command will be executed.
 * @param listener The Jenkins build listener for logging and monitoring the execution.
 * @param acct The z/OS account number.
 * @param command The TSO command to be executed.
 *
 * @throws AbortException if the TSO command execution fails, with the error message indicating
 *                       the reason for the failure.
 */
fun performTsoCommand(
    zosConnection: ZOSConnection,
    listener: TaskListener,
    acct: String,
    command: String,
    ) {
    listener.logger.println(Messages.zdevops_issue_TSO_command(command))
    try {
        val tsoCommandResponse = IssueTso(zosConnection).issueTsoCommand(acct, command, StartTsoParams(), failOnPrompt = true)
        listener.logger.println(tsoCommandResponse.commandResponses)
    } catch (ex: Exception) {
        listener.logger.println(Messages.zdevops_TSO_command_fail())
        throw ex
    }
    listener.logger.println(Messages.zdevops_TSO_command_success())
}