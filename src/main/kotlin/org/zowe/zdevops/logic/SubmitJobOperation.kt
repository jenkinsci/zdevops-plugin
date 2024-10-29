/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2022
 */

package org.zowe.zdevops.logic

import hudson.AbortException
import hudson.FilePath
import hudson.console.HyperlinkNote
import hudson.model.TaskListener
import org.zowe.kotlinsdk.Job
import org.zowe.kotlinsdk.SpoolFile
import org.zowe.kotlinsdk.SubmitJobRequest
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.kotlinsdk.zowe.client.sdk.zosjobs.GetJobs
import org.zowe.kotlinsdk.zowe.client.sdk.zosjobs.MonitorJobs
import org.zowe.kotlinsdk.zowe.client.sdk.zosjobs.SubmitJobs
import org.zowe.zdevops.Messages
import org.zowe.zdevops.utils.extractSubmitJobMessage
import org.zowe.zdevops.utils.runMFTryCatchWrappedQuery
import java.io.File

/**
 * Submits a z/OS job
 *
 * @param fileToSubmit The path to the job to submit. It may reside either in dataset or in USS file.
 * @param zosConnection The z/OS connection to use for job submission.
 * @param listener The task listener to log information and handle exceptions.
 * @return The response containing information about the submitted job.
 * @throws AbortException If job submission fails.
 */
fun submitJob(
    fileToSubmit: String,
    zosConnection: ZOSConnection,
    listener: TaskListener,
): SubmitJobRequest? {
    var submitJobRsp: SubmitJobRequest? = null
    try {
        listener.logger.println(Messages.zdevops_declarative_ZOSJobs_submitting(fileToSubmit, zosConnection.host, zosConnection.zosmfPort))
        submitJobRsp = SubmitJobs(zosConnection).submitJob(fileToSubmit)
        listener.logger.println(Messages.zdevops_declarative_ZOSJobs_submitted_success(submitJobRsp.jobid, submitJobRsp.jobname, submitJobRsp.owner))
    } catch (e: Exception) {
        listener.logger.println(e.message?.let { extractSubmitJobMessage(it) })
        throw AbortException(Messages.zdevops_classic_ZOSJobs_submitted_fail(fileToSubmit))
    }
    return submitJobRsp
}

/**
 * Submits a z/OS job synchronously, monitors its execution, retrieves the job log, and returns the job's return code.
 *
 * @param fileToSubmit The path to the job to submit. It may reside either in dataset or in USS file.
 * @param zosConnection The z/OS connection to use for job submission and monitoring.
 * @param listener The task listener to log information and handle exceptions.
 * @param workspacePath The workspace path to store the job log.
 * @param buildUrl The URL of the build (optional).
 * @param linkBuilder A function to build hyperlinks.
 * @return The return code of the executed job.
 * @throws IllegalStateException If necessary information is missing from the system response.
 */
fun submitJobSync(
    fileToSubmit: String,
    zosConnection: ZOSConnection,
    listener: TaskListener,
    workspacePath: FilePath,
    buildUrl: String?,
    linkBuilder: (String?, String, String) -> String
): String? {
    val submitJobRsp = submitJob(fileToSubmit, zosConnection, listener)
    listener.logger.println(Messages.zdevops_declarative_ZOSJobs_submitted_waiting())

    val jobId = submitJobRsp?.jobid ?: throw IllegalStateException("System response doesn't contain JOB ID.")
    val jobName = submitJobRsp.jobname ?: throw IllegalStateException("System response doesn't contain JOB name.")
    lateinit var finalResult: Job
    runMFTryCatchWrappedQuery(listener) {
        finalResult = MonitorJobs(zosConnection).waitForJobOutputStatus(jobName, jobId)
    }
    listener.logger.println(Messages.zdevops_declarative_ZOSJobs_submitted_executed(finalResult.returnedCode))

    listener.logger.println(Messages.zdevops_declarative_ZOSJobs_getting_log())
    lateinit var spoolFiles: List<SpoolFile>
    runMFTryCatchWrappedQuery(listener) {
        spoolFiles = GetJobs(zosConnection).getSpoolFilesForJob(finalResult)
    }
    if (spoolFiles.isNotEmpty()) {
        val fullLog = spoolFiles.joinToString { GetJobs(zosConnection).getSpoolContent(it) }
        val logPath = "$workspacePath/${finalResult.jobName}.${finalResult.jobId}"
        val file = File(logPath)
        file.writeText(fullLog)
        listener.logger.println(Messages.zdevops_declarative_ZOSJobs_got_log(
            HyperlinkNote.encodeTo(
                linkBuilder(buildUrl, finalResult.jobName, finalResult.jobId),
                "${finalResult.jobName}.${finalResult.jobId}"
            )
        ))
    } else {
        listener.logger.println(Messages.zdevops_no_spool_files(submitJobRsp.jobid))
    }

    return finalResult?.returnedCode
}