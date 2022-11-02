package eu.ibagroup.zdevops.classic

import eu.ibagroup.r2z.zowe.client.sdk.core.ZOSConnection
import eu.ibagroup.zdevops.Messages
import hudson.EnvVars
import hudson.FilePath
import hudson.Launcher
import hudson.tasks.BuildStepDescriptor
import hudson.tasks.Builder
import jenkins.tasks.SimpleBuildStep
import java.io.PrintWriter
import java.io.StringWriter
import eu.ibagroup.zdevops.config.ZOSConnectionList
import hudson.model.*
import java.net.URL

abstract class AbstractBuildStep(private val connectionName: String) : Builder(), SimpleBuildStep {

    abstract fun perform(build: AbstractBuild<*, *>,
                         launcher: Launcher,
                         listener: BuildListener,
                         zosConnection: ZOSConnection)

    override fun perform(build: AbstractBuild<*, *>,
                         launcher: Launcher,
                         listener: BuildListener): Boolean {

        val connection = ZOSConnectionList.resolve(connectionName, build) ?: let{
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

        perform(build, launcher, listener, zosConnection)
        return true
    }

    companion object {
        open class DefaultBuildDescriptor(private val descriptorDisplayName: String = ""): BuildStepDescriptor<Builder?>() {
            override fun getDisplayName() = descriptorDisplayName
            override fun isApplicable(jobType: Class<out AbstractProject<*, *>>?) = true
        }
    }
}