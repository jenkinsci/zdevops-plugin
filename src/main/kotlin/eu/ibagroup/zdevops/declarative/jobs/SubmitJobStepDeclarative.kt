package eu.ibagroup.zdevops.declarative.jobs

import eu.ibagroup.r2z.zowe.client.sdk.core.ZOSConnection
import eu.ibagroup.r2z.zowe.client.sdk.zosjobs.SubmitJobs
import eu.ibagroup.zdevops.declarative.AbstractZosmfAction
import hudson.*
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor

typealias zMessages = eu.ibagroup.zdevops.Messages

class SubmitJobStepDeclarative @DataBoundConstructor constructor(private val fileToSubmit: String) :
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
    listener.logger.println(zMessages.zdevops_declarative_ZOSJobs_submitting(fileToSubmit, zosConnection.host, zosConnection.zosmfPort))
    val submitJobRsp = SubmitJobs(zosConnection).submitJob(fileToSubmit)
    listener.logger.println(zMessages.zdevops_declarative_ZOSJobs_submitted_success(submitJobRsp.jobid, submitJobRsp.jobname, submitJobRsp.owner))
  }


  @Symbol("submitJob")
  @Extension
  class DescriptorImpl : Companion.DefaultBuildDescriptor("Submit Job Declarative")
}