/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2022
 */

package org.zowe.zdevops.declarative.jobs

import hudson.EnvVars
import hudson.FilePath
import hudson.model.Item
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.fail
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.MOCK_SERVER_HOST
import org.zowe.zdevops.MockResponseDispatcher
import org.zowe.zdevops.MockServerFactory
import java.io.File
import java.io.PrintStream
import java.nio.file.Paths

class SubmitJobSyncStepDeclarativeSpec : ShouldSpec({
  lateinit var mockServer: MockWebServer
  lateinit var responseDispatcher: MockResponseDispatcher
  val mockServerFactory = MockServerFactory()

  beforeSpec {
    mockServer = mockServerFactory.startMockServer(MOCK_SERVER_HOST)
    responseDispatcher = mockServerFactory.responseDispatcher
  }
  afterSpec {
    mockServerFactory.stopMockServer()
  }
  context("declarative/jobs module: SubmitJobStep") {
    val virtualChannel = TestVirtualChannel()
    val zosConnection = ZOSConnection(mockServer.hostName, mockServer.port.toString(), "test", "test", "https")
    val trashDir = tempdir()
    val itemGroup = object : TestItemGroup() {
      override fun getRootDirFor(child: Item?): File {
        return trashDir
      }
    }
    val job = TestJob(itemGroup, "test")
    val run = TestRun(job)
    val trashDirWithInternal = Paths.get(trashDir.absolutePath, "test_name").toString()
    val workspace = FilePath(File(trashDirWithInternal))
    val env = EnvVars()

    afterEach {
      responseDispatcher.removeAllEndpoints()
    }
    should("perform SubmitJobSyncStepDeclarative operation without spool files") {
      var isJobSubmitting = false
      var isJobSubmitted = false
      var isWaitingJobFinish = false
      var isJobFinished = false
      var isDownloadingExecutionLog = false
      var isNoSpoolLogs = false

      val taskListener = object : TestBuildListener() {
        override fun getLogger(): PrintStream {
          val logger = mockk<PrintStream>()
          every {
            logger.println(any<String>())
          } answers {
            if (firstArg<String>().contains("Submitting a JOB")) {
              isJobSubmitting = true
            } else if (firstArg<String>().contains("JOB submitted successfully")) {
              isJobSubmitted = true
            } else if (firstArg<String>().contains("Waiting for a JOB finish")) {
              isWaitingJobFinish = true
            } else if (firstArg<String>().contains("JOB was finished. Returned code")) {
              isJobFinished = true
            } else if (firstArg<String>().contains("Downloading execution log")) {
              isDownloadingExecutionLog = true
            } else if (firstArg<String>().contains("There are no logs for")) {
              isNoSpoolLogs = true
            } else {
              fail("Unexpected logger message: ${firstArg<String>()}")
            }
          }
          return logger
        }
      }
      val launcher = TestLauncher(taskListener, virtualChannel)

      responseDispatcher.injectEndpoint(
        "${this.testCase.name.testName}_submitJob",
        { it?.requestLine?.matches(Regex("PUT /zosmf/restjobs/jobs HTTP/.*")) == true },
        { MockResponse().setBody(responseDispatcher.readMockJson("submitJobResponse") ?: "") }
      )
      val getJobsRegex = Regex("GET /zosmf/restjobs/jobs/(?!.*files).* HTTP/.*")
      responseDispatcher.injectEndpoint(
        "${this.testCase.name.testName}_getJob",
        { it?.requestLine?.matches(getJobsRegex) == true },
        { MockResponse().setBody(responseDispatcher.readMockJson("getJobResponse") ?: "") }
      )
      responseDispatcher.injectEndpoint(
        "${this.testCase.name.testName}_getJobSpoolFiles",
        { it?.requestLine?.matches(Regex("GET /zosmf/restjobs/jobs/.*/files HTTP/.*")) == true },
        { MockResponse().setBody("[]") }
      )

      val submitJobSyncStepDeclInst = spyk(
        SubmitJobSyncStepDeclarative("test")
      )
      submitJobSyncStepDeclInst.perform(
        run,
        workspace,
        env,
        launcher,
        taskListener,
        zosConnection
      )

      assertSoftly { isJobSubmitting shouldBe true }
      assertSoftly { isJobSubmitted shouldBe true }
      assertSoftly { isWaitingJobFinish shouldBe true }
      assertSoftly { isJobFinished shouldBe true }
      assertSoftly { isDownloadingExecutionLog shouldBe true }
      assertSoftly { isNoSpoolLogs shouldBe true }
    }
    should("perform SubmitJobSyncStepDeclarative operation with spool files") {
      var isJobSubmitting = false
      var isJobSubmitted = false
      var isWaitingJobFinish = false
      var isJobFinished = false
      var isDownloadingExecutionLog = false
      var isSubmissionLogSaved = false

      val taskListener = object : TestBuildListener() {
        override fun getLogger(): PrintStream {
          val logger = mockk<PrintStream>()
          every {
            logger.println(any<String>())
          } answers {
            if (firstArg<String>().contains("Submitting a JOB")) {
              isJobSubmitting = true
            } else if (firstArg<String>().contains("JOB submitted successfully")) {
              isJobSubmitted = true
            } else if (firstArg<String>().contains("Waiting for a JOB finish")) {
              isWaitingJobFinish = true
            } else if (firstArg<String>().contains("JOB was finished. Returned code")) {
              isJobFinished = true
            } else if (firstArg<String>().contains("Downloading execution log")) {
              isDownloadingExecutionLog = true
            } else if (firstArg<String>().contains("Submission log:")) {
              isSubmissionLogSaved = true
            } else {
              fail("Unexpected logger message: ${firstArg<String>()}")
            }
          }
          return logger
        }
      }
      val launcher = TestLauncher(taskListener, virtualChannel)

      responseDispatcher.injectEndpoint(
        "${this.testCase.name.testName}_submitJob",
        { it?.requestLine?.matches(Regex("PUT /zosmf/restjobs/jobs HTTP/.*")) == true },
        { MockResponse().setBody(responseDispatcher.readMockJson("submitJobResponse") ?: "") }
      )
      val getJobsRegex = Regex("GET /zosmf/restjobs/jobs/(?!.*files).* HTTP/.*")
      responseDispatcher.injectEndpoint(
        "${this.testCase.name.testName}_getJob",
        { it?.requestLine?.matches(getJobsRegex) == true },
        { MockResponse().setBody(responseDispatcher.readMockJson("getJobResponse") ?: "") }
      )
      responseDispatcher.injectEndpoint(
        "${this.testCase.name.testName}_getJobSpoolFiles",
        { it?.requestLine?.matches(Regex("GET /zosmf/restjobs/jobs/.*/files(?!.*records).* HTTP/.*")) == true },
        { MockResponse().setBody(responseDispatcher.readMockJson("getJobSpoolFilesResponse") ?: "") }
      )
      val getSpoolFileRecordsRespBody = javaClass.classLoader.getResource("mock/getSpoolFileRecordsResponse.txt")?.readText()
      responseDispatcher.injectEndpoint(
        "${this.testCase.name.testName}_getSpoolFileRecords",
        { it?.requestLine?.matches(Regex("GET /zosmf/restjobs/jobs/.*/files/.*/records.* HTTP/.*")) == true },
        { MockResponse().setBody(getSpoolFileRecordsRespBody ?: "") }
      )

      val submitJobSyncStepDeclInst = spyk(
        SubmitJobSyncStepDeclarative("test")
      )
      submitJobSyncStepDeclInst.perform(
        run,
        workspace,
        env,
        launcher,
        taskListener,
        zosConnection
      )

      assertSoftly { isJobSubmitting shouldBe true }
      assertSoftly { isJobSubmitted shouldBe true }
      assertSoftly { isWaitingJobFinish shouldBe true }
      assertSoftly { isJobFinished shouldBe true }
      assertSoftly { isDownloadingExecutionLog shouldBe true }
      assertSoftly { isSubmissionLogSaved shouldBe true }
    }
  }
})
