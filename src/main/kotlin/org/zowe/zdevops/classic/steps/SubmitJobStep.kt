/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2022
 */

package org.zowe.zdevops.classic.steps

import hudson.AbortException
import hudson.Extension
import hudson.Launcher
import hudson.model.AbstractBuild
import hudson.model.BuildListener
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.bind.JavaScriptMethod
import org.zowe.zdevops.Messages
import org.zowe.zdevops.classic.AbstractBuildStep
import org.zowe.zdevops.logic.submitJob
import org.zowe.zdevops.logic.submitJobSync

class SubmitJobStep
@DataBoundConstructor
constructor(
  connectionName: String,
  val jobName: String,
  val sync: Boolean,
  val checkRC: Boolean,
) : AbstractBuildStep(connectionName) {
  override fun perform(
      build: AbstractBuild<*, *>,
      launcher: Launcher,
      listener: BuildListener,
      zosConnection: org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
  ) {
    if (sync) {
      val workspace = build.executor?.currentWorkspace!!
      val linkBuilder: (String?, String, String) -> String = { jobUrl, jobName, jobId ->
        "${jobUrl}ws/${jobName}.${jobId}/*view*/"
      }
      val jobResult = submitJobSync(jobName, zosConnection, listener, workspace, build.getEnvironment(listener)["JOB_URL"], linkBuilder)
      if (checkRC && !jobResult.equals("CC 0000")) {
        throw AbortException("Job RC code is not 0000")
      }
    } else {
      submitJob(jobName, zosConnection, listener)
    }
  }


  @Extension
  class DescriptorImpl : Companion.DefaultBuildDescriptor(Messages.zdevops_classic_submitJobStep_display_name()) {
    private var lastStepId = 0
    private val marker: String = "SJ"

    /**
     * Creates a unique step ID
     *
     * @return The generated step ID
     */
    @JavaScriptMethod
    @Synchronized
    fun createStepId(): String {
      return marker + lastStepId++.toString()
    }
  }
}
