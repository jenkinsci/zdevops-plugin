package eu.ibagroup.zdevops.declarative.jobs

import eu.ibagroup.r2z.zowe.client.sdk.core.ZOSConnection
import eu.ibagroup.r2z.zowe.client.sdk.zosjobs.GetJobs
import eu.ibagroup.r2z.zowe.client.sdk.zosjobs.MonitorJobs
import eu.ibagroup.r2z.zowe.client.sdk.zosjobs.SubmitJobs
import eu.ibagroup.zdevops.declarative.AbstractZosmfAction
import hudson.*
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import java.io.File

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
      listener.logger.println(zMessages.zdevops_declarative_ZOSJobs_submitting(fileToSubmit, zosConnection.host, zosConnection.zosmfPort))
      val submitJobRsp = SubmitJobs(zosConnection).submitJob(fileToSubmit)
      listener.logger.println(zMessages.zdevops_declarative_ZOSJobs_submitted_success(submitJobRsp.jobid, submitJobRsp.jobname, submitJobRsp.owner))
      listener.logger.println(zMessages.zdevops_declarative_ZOSJobs_submitted_waiting())
      val jobId = submitJobRsp.jobid ?: throw IllegalStateException("System response doesn't contain JOB ID.")
      val jobName = submitJobRsp.jobname ?: throw IllegalStateException("System response doesn't contain JOB name.")
      val finalResult = MonitorJobs(zosConnection).waitForJobOutputStatus(jobName, jobId)
      listener.logger.println(zMessages.zdevops_declarative_ZOSJobs_submitted_executed(finalResult.returnedCode))

      listener.logger.println(zMessages.zdevops_declarative_ZOSJobs_getting_log())
      val spoolFiles = GetJobs(zosConnection).getSpoolFilesForJob(finalResult)
      if (spoolFiles.isNotEmpty()) {
        var fullLog = spoolFiles.joinToString { GetJobs(zosConnection).getSpoolContent(it) } //spoolFiles.forEach { fullLog = fullLog.plus(GetJobs(zosConnection).getSpoolContent(it)) }
        if (fullLog != null) {
          val workspacePath = workspace.remote.replace(workspace.name, "")
          val logPath = "$workspacePath${finalResult.jobName}.${finalResult.jobId}"
          val file = File(logPath)
          file.writeText(fullLog!!)
          listener.logger.println(zMessages.zdevops_declarative_ZOSJobs_got_log(logPath))
        } else {
          listener.logger.println(zMessages.zdevops_spool_content_error(submitJobRsp.jobid))
        }
      } else {
        listener.logger.println(zMessages.zdevops_no_spool_files(submitJobRsp.jobid))
      }
  }

  @Symbol("submitJobSync")
  @Extension
  class DescriptorImpl : Companion.DefaultBuildDescriptor("Submit Job Synchronously Declarative")
}