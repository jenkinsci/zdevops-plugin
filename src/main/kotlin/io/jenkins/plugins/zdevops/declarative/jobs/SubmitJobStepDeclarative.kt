/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package io.jenkins.plugins.zdevops.declarative.jobs

import eu.ibagroup.r2z.zowe.client.sdk.core.ZOSConnection
import eu.ibagroup.r2z.zowe.client.sdk.zosjobs.SubmitJobs
import io.jenkins.plugins.zdevops.declarative.AbstractZosmfAction
import io.jenkins.plugins.zdevops.Messages
import hudson.*
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor

typealias zMessages = Messages

class SubmitJobStepDeclarative @DataBoundConstructor constructor(private val fileToSubmit: String) :
  AbstractZosmfAction() {

  override val exceptionMessage: String = zMessages.zdevops_declarative_ZOSJobs_submitted_fail(fileToSubmit)

  override fun perform(
    run: Run<*, *>,
    workspace: FilePath,
    env: EnvVars,
    launcher: Launcher,
    listener: TaskListener,
    zosConnection: ZOSConnection
  ) {
    listener.logger.println(zMessages.zdevops_declarative_ZOSJobs_submitting(fileToSubmit, zosConnection.host, zosConnection.zosmfPort))
    val submitJobRsp = SubmitJobs(zosConnection).submitJob(fileToSubmit)
    listener.logger.println(zMessages.zdevops_declarative_ZOSJobs_submitted_success(submitJobRsp.jobid, submitJobRsp.jobname, submitJobRsp.owner))
  }


  @Symbol("submitJob")
  @Extension
  class DescriptorImpl : Companion.DefaultBuildDescriptor("Submit Job Declarative")
}