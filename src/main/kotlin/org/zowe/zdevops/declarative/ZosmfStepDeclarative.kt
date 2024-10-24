/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2022
 */

package org.zowe.zdevops.declarative

import hudson.EnvVars
import hudson.Extension
import hudson.FilePath
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.plugins.workflow.steps.Step
import org.jenkinsci.plugins.workflow.steps.StepContext
import org.jenkinsci.plugins.workflow.steps.StepDescriptor
import org.jenkinsci.plugins.workflow.steps.StepExecution
import org.kohsuke.stapler.DataBoundConstructor
import org.zowe.zdevops.utils.getZoweZosConnection
import org.zowe.zdevops.utils.validateConnection


class ZosmfStepDeclarative @DataBoundConstructor constructor(private val connectionName: String) : Step() {
  override fun start(context: StepContext): StepExecution {
    val listener: TaskListener? = context.get(TaskListener::class.java)
    val zosConnection =  getZoweZosConnection(connectionName, listener)

    validateConnection(zosConnection)

    return ZosmfExecution(connectionName, context)
  }

  @Extension
  class DescriptorImpl : StepDescriptor() {
    override fun getRequiredContext(): Set<Class<*>> {
      return setOf<Class<*>>(
        Run::class.java,
        FilePath::class.java,
        TaskListener::class.java,
        EnvVars::class.java
      )
    }

    override fun takesImplicitBlockArgument(): Boolean {
      return true
    }

    override fun getFunctionName(): String {
      return "zosmf"
    }
  }

}
