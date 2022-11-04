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