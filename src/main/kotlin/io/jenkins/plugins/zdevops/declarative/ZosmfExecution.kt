/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package io.jenkins.plugins.zdevops.declarative

import hudson.FilePath
import org.jenkinsci.plugins.workflow.steps.BodyExecutionCallback
import org.jenkinsci.plugins.workflow.steps.StepContext
import org.jenkinsci.plugins.workflow.steps.StepExecution

class ZosmfExecution(var connectionName: String, context: StepContext) : StepExecution(context) {

  override fun start(): Boolean {
    val workspace = context.get(FilePath::class.java)?.createTextTempFile("zosmf", "connection", connectionName)
    context.newBodyInvoker()
      .withContext(workspace)
      .withCallback(ZosmfExecutionCallback(context, workspace))
      .start()
    return false
  }


  companion object {

    class ZosmfExecutionCallback(private val parentContext: StepContext, private val contextFilePath: FilePath?) :
      BodyExecutionCallback() {

      override fun onSuccess(context: StepContext?, result: Any?) {
        contextFilePath?.delete()
        parentContext.onSuccess(result)
      }

      override fun onFailure(context: StepContext?, t: Throwable) {
        contextFilePath?.delete()
        parentContext.onFailure(t)
      }

    }
  }
}