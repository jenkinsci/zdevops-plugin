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

import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import io.jenkins.plugins.zdevops.config.ZOSConnectionList
import io.jenkins.plugins.zdevops.Messages
import hudson.AbortException
import hudson.EnvVars
import hudson.FilePath
import hudson.Launcher
import hudson.model.AbstractProject
import hudson.model.Run
import hudson.model.TaskListener
import hudson.tasks.BuildStepDescriptor
import hudson.tasks.Builder
import jenkins.tasks.SimpleBuildStep
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URL
import java.nio.charset.StandardCharsets

abstract class AbstractZosmfAction : Builder(), SimpleBuildStep {

  final override fun requiresWorkspace(): Boolean {
    return true
  }
  abstract val exceptionMessage: String
  abstract fun perform(run: Run<*, *>, workspace: FilePath, env: EnvVars, launcher: Launcher, listener: TaskListener, zosConnection: ZOSConnection)

  override fun perform(run: Run<*, *>, workspace: FilePath, env: EnvVars, launcher: Launcher, listener: TaskListener) {
    val connectionName = workspace.read().readBytes().toString(StandardCharsets.UTF_8)
    val connection = ZOSConnectionList.resolve(connectionName, run) ?: let {

      val exception = IllegalArgumentException(Messages.zdevops_config_ZOSConnection_resolve_unknown(connectionName))
      val sw = StringWriter()
      exception.printStackTrace(PrintWriter(sw))
      listener.logger.println(sw.toString())
      throw exception
    }
    val connURL = URL(connection.url)
    val zoweConnection = ZOSConnection(
      connURL.host, connURL.port.toString(), connection.username, connection.password, connURL.protocol
    )
    runCatching {
      perform(run, workspace, env, launcher, listener, zoweConnection)
    }.onFailure {
      val sw = StringWriter()
      it.printStackTrace(PrintWriter(sw))
      listener.logger.println(sw.toString())
      throw AbortException(exceptionMessage)
    }
  }

  companion object {
    open class DefaultBuildDescriptor(private val descriptorDisplayName: String = ""): BuildStepDescriptor<Builder?>() {
      override fun getDisplayName() = descriptorDisplayName
      override fun isApplicable(jobType: Class<out AbstractProject<*, *>>?) = true
    }
  }

}
