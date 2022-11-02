package eu.ibagroup.zdevops.classic.steps

import eu.ibagroup.r2z.zowe.client.sdk.zosjobs.SubmitJobs
import eu.ibagroup.zdevops.Messages
import eu.ibagroup.zdevops.classic.AbstractBuildStep
import eu.ibagroup.zdevops.config.ZOSConnectionList
import hudson.AbortException
import hudson.Extension
import hudson.Launcher
import hudson.model.AbstractBuild
import hudson.model.BuildListener
import hudson.util.ListBoxModel
import jenkins.model.GlobalConfiguration
import jenkins.model.Jenkins
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
      zosConnection: eu.ibagroup.r2z.zowe.client.sdk.core.ZOSConnection
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

      if (Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {

        GlobalConfiguration.all().get(ZOSConnectionList::class.java)?.connections?.forEach {
          result.add("${it.name} - (${it.url})", it.name)
        }
      }

      return result
    }
  }
}