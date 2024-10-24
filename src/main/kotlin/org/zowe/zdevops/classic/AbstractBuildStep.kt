/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2022
 */

package org.zowe.zdevops.classic

import hudson.Launcher
import hudson.model.AbstractBuild
import hudson.model.AbstractProject
import hudson.model.BuildListener
import hudson.tasks.BuildStepDescriptor
import hudson.tasks.Builder
import hudson.util.FormValidation
import hudson.util.ListBoxModel
import jenkins.model.GlobalConfiguration
import jenkins.tasks.SimpleBuildStep
import org.kohsuke.stapler.QueryParameter
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.Messages
import org.zowe.zdevops.config.ZOSConnectionList
import org.zowe.zdevops.utils.validateConnection
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URL
import javax.servlet.ServletException

/**
 * The AbstractBuildStep class is an abstract class that serves as the base for the plugin build steps in Jenkins.
 */
abstract class AbstractBuildStep(val connectionName: String) : Builder(), SimpleBuildStep {
    /**
     * Performs the build step.
     *
     * @param build          The build object.
     * @param launcher       The launcher object.
     * @param listener       The build listener.
     * @param zosConnection  The connection to the z/OS system.
     */
    abstract fun perform(build: AbstractBuild<*, *>,
                         launcher: Launcher,
                         listener: BuildListener,
                         zosConnection: ZOSConnection)
    /**
     * Performs the build step.
     *
     * @param build          The build object.
     * @param launcher       The launcher object.
     * @param listener       The build listener.
     * @return True if the build step is successful, false otherwise.
     * @throws IllegalArgumentException if the z/OS connection cannot be resolved.
     */
    override fun perform(build: AbstractBuild<*, *>,
                         launcher: Launcher,
                         listener: BuildListener): Boolean {

        val connection = ZOSConnectionList.resolve(connectionName) ?: let{
            val exception = IllegalArgumentException(Messages.zdevops_config_ZOSConnection_resolve_unknown(connectionName))
            val sw = StringWriter()
            exception.printStackTrace(PrintWriter(sw))
            listener.logger.println(sw.toString())
            throw exception
        }
        val connURL = URL(connection.url)
        val zosConnection = ZOSConnection(
            connURL.host, connURL.port.toString(), connection.username, connection.password, connURL.protocol
        )

        validateConnection(zosConnection)

        perform(build, launcher, listener, zosConnection)
        return true
    }

    companion object {
        /**
         * The default descriptor for the plugin build steps.
         *
         * @param descriptorDisplayName The display name for the plugin build step
         */
        open class DefaultBuildDescriptor(private val descriptorDisplayName: String = ""): BuildStepDescriptor<Builder?>() {
            /**
             * Gets the display name for the build step.
             *
             * @return The display name.
             */
            override fun getDisplayName() = descriptorDisplayName
            /**
             * Checks if the build step is applicable to the specified job type
             *
             * @param jobType The type of the job.
             * @return True if the build step is applicable, false otherwise.
             */
            override fun isApplicable(jobType: Class<out AbstractProject<*, *>>?) = true

            /**
             * Fills the connection name items for the build step configuration.
             *
             * @return The ListBoxModel containing the connection name items.
             */
            fun doFillConnectionNameItems(): ListBoxModel {
                val result = ListBoxModel()

                GlobalConfiguration.all().get(ZOSConnectionList::class.java)?.connections?.forEach {
                    result.add("${it.name} - (${it.url})", it.name)
                }

                return result
            }

            /**
             * Checks if the connection name is valid.
             *
             * @param connectionName The name of the z/OS connection.
             * @return FormValidation.ok() if the connection name is valid, or an error message otherwise.
             */
            @Throws(IOException::class, ServletException::class)
            open fun doCheckConnectionName(@QueryParameter connectionName: String): FormValidation? {
                val result = ListBoxModel()
                GlobalConfiguration.all().get(ZOSConnectionList::class.java)?.connections?.forEach {
                    result.add("${it.name} - (${it.url})", it.name)
                }
                return if (result.isNotEmpty()) FormValidation.ok() else FormValidation.error(Messages.zdevops_config_ZOSConnectionList_validation_error())
            }

        }
    }
}
