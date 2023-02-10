/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2022
 */

package io.jenkins.plugins.zdevops.declarative

import hudson.EnvVars
import hudson.Extension
import hudson.FilePath
import hudson.model.*
import hudson.model.Run
import org.jenkinsci.plugins.workflow.steps.Step
import org.jenkinsci.plugins.workflow.steps.StepContext
import org.jenkinsci.plugins.workflow.steps.StepDescriptor
import org.jenkinsci.plugins.workflow.steps.StepExecution
import org.kohsuke.stapler.DataBoundConstructor


class ZosmfStepDeclarative @DataBoundConstructor constructor(private val connectionName: String) : Step() {
  override fun start(context: StepContext): StepExecution {
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