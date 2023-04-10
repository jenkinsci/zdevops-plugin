/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2022
 */

package io.jenkins.plugins.zdevops.classic.steps

import org.zowe.kotlinsdk.zowe.client.sdk.zosjobs.SubmitJobs
import io.jenkins.plugins.zdevops.Messages
import io.jenkins.plugins.zdevops.classic.AbstractBuildStep
import io.jenkins.plugins.zdevops.config.ZOSConnectionList
import hudson.AbortException
import hudson.Extension
import hudson.Launcher
import hudson.model.AbstractBuild
import hudson.model.BuildListener
import hudson.util.ListBoxModel
import jenkins.model.GlobalConfiguration
import org.kohsuke.stapler.DataBoundConstructor
import java.io.PrintWriter
import java.io.StringWriter

class SubmitJobStep
@DataBoundConstructor
constructor(
  connectionName: String,
  val jobName: String
) : AbstractBuildStep(connectionName) {
  override fun perform(
      build: AbstractBuild<*, *>,
      launcher: Launcher,
      listener: BuildListener,
      zosConnection: org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
  ) {
    runCatching {
      listener.logger.println(Messages.zdevops_classic_ZOSJobs_submitting(jobName, zosConnection.host, zosConnection.zosmfPort))
      val submitJobRsp = SubmitJobs(zosConnection).submitJob(jobName)
      listener.logger.println(
          Messages.zdevops_classic_ZOSJobs_submitted_success(
              submitJobRsp.jobid,
              submitJobRsp.jobname,
              submitJobRsp.owner
          )
      )
    }.onFailure {
      val sw = StringWriter()
      it.printStackTrace(PrintWriter(sw))
      listener.logger.println(sw.toString())
      throw AbortException(Messages.zdevops_classic_ZOSJobs_submitted_fail(jobName))
    }
  }


  @Extension
  class DescriptorImpl : Companion.DefaultBuildDescriptor(Messages.zdevops_classic_submitJobStep_display_name()) {
    fun doFillConnectionNameItems(): ListBoxModel {
      val result = ListBoxModel()

      GlobalConfiguration.all().get(ZOSConnectionList::class.java)?.connections?.forEach {
        result.add("${it.name} - (${it.url})", it.name)
      }

      return result
    }
  }
}
