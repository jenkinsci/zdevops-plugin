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

import hudson.*
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.declarative.AbstractZosmfAction
import org.zowe.zdevops.logic.submitJobSync

class SubmitJobSyncStepDeclarative @DataBoundConstructor constructor(private val fileToSubmit: String):
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
      val workspacePath = FilePath(null, workspace.remote.replace(workspace.name,""))
      val linkBuilder: (String?, String, String) -> String = { buildUrl, jobName, jobId ->
          "$buildUrl/execution/node/3/ws/${jobName}.${jobId}/*view*/"
      }
      submitJobSync(fileToSubmit, zosConnection, listener, workspacePath, env["BUILD_URL"], linkBuilder)
  }

  @Symbol("submitJobSync")
  @Extension
  class DescriptorImpl : Companion.DefaultBuildDescriptor("Submit Job Synchronously Declarative")
}
