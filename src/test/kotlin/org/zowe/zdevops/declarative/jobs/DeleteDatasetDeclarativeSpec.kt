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
import io.kotest.matchers.string.shouldContain
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

class DeleteDatasetDeclarativeSpec : ShouldSpec({
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
  context("declarative/jobs module: DeleteDatasetDeclarative") {
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
    val workspace = FilePath(File(""))
    val env = EnvVars()

    afterEach {
      responseDispatcher.removeAllEndpoints()
    }
    should("fails to perform DeleteDatasetDeclarative operation as no dataset name is provided") {
      val taskListener = object : TestBuildListener() {
        override fun getLogger(): PrintStream {
          val logger = mockk<PrintStream>()
          every {
            logger.println(any<String>())
          } answers {
            fail("Unexpected logger message: ${firstArg<String>()}")
          }
          return logger
        }
      }
      val launcher = TestLauncher(taskListener, virtualChannel)

      responseDispatcher.injectEndpoint(
        this.testCase.name.testName,
        { it?.requestLine?.contains("zosmf/restjobs/jobs") ?: false },
        { MockResponse().setBody(responseDispatcher.readMockJson("submitJobResponse") ?: "") }
      )

      val deleteDatasetDecl = spyk(
        DeleteDatasetDeclarative()
      )
      runCatching {
        deleteDatasetDecl.perform(
          run,
          workspace,
          env,
          launcher,
          taskListener,
          zosConnection
        )
      }
        .onSuccess { fail("The function should throw an error") }
        .onFailure {
          assertSoftly { it.message shouldContain "Unable to delete: no dsn keyword present" }
        }

    }
    should("perform DeleteDatasetDeclarative operation that deletes a dataset") {
      var isDeletingDataset = false
      var isSuccessfullyDeleted = false
      val taskListener = object : TestBuildListener() {
        override fun getLogger(): PrintStream {
          val logger = mockk<PrintStream>()
          every {
            logger.println(any<String>())
          } answers {
            if (firstArg<String>().contains("Deleting dataset")) {
              isDeletingDataset = true
            } else if (firstArg<String>().contains("Successfully deleted")) {
              isSuccessfullyDeleted = true
            } else {
              fail("Unexpected logger message: ${firstArg<String>()}")
            }
          }
          return logger
        }
      }
      val launcher = TestLauncher(taskListener, virtualChannel)

      responseDispatcher.injectEndpoint(
        this.testCase.name.testName,
        { it?.requestLine?.contains(Regex("DELETE /zosmf/restfiles/ds/test HTTP/.*")) ?: false },
        { MockResponse().setBody("{}") }
      )

      val deleteDatasetDecl = spyk(
        DeleteDatasetDeclarative()
      )
      deleteDatasetDecl.setDsn("test")
      deleteDatasetDecl.perform(
        run,
        workspace,
        env,
        launcher,
        taskListener,
        zosConnection
      )

      assertSoftly { isDeletingDataset shouldBe true }
      assertSoftly { isSuccessfullyDeleted shouldBe true }
    }
    should("perform DeleteDatasetDeclarative operation that deletes a member") {
      var isDeletingMember = false
      var isSuccessfullyDeleted = false
      val taskListener = object : TestBuildListener() {
        override fun getLogger(): PrintStream {
          val logger = mockk<PrintStream>()
          every {
            logger.println(any<String>())
          } answers {
            if (firstArg<String>().contains("Deleting member")) {
              isDeletingMember = true
            } else if (firstArg<String>().contains("Successfully deleted")) {
              isSuccessfullyDeleted = true
            } else {
              fail("Unexpected logger message: ${firstArg<String>()}")
            }
          }
          return logger
        }
      }
      val launcher = TestLauncher(taskListener, virtualChannel)

      responseDispatcher.injectEndpoint(
        this.testCase.name.testName,
        { it?.requestLine?.contains("DELETE /zosmf/restfiles/ds/test(test)") ?: false },
        { MockResponse().setBody("{}") }
      )

      val deleteDatasetDecl = spyk(
        DeleteDatasetDeclarative()
      )
      deleteDatasetDecl.setDsn("test")
      deleteDatasetDecl.setMember("test")
      deleteDatasetDecl.perform(
        run,
        workspace,
        env,
        launcher,
        taskListener,
        zosConnection
      )

      assertSoftly { isDeletingMember shouldBe true }
      assertSoftly { isSuccessfullyDeleted shouldBe true }
    }
    should("fails to perform DeleteDatasetDeclarative operation as the member name is not valid") {
      var isDeletingMember = false
      val taskListener = object : TestBuildListener() {
        override fun getLogger(): PrintStream {
          val logger = mockk<PrintStream>()
          every {
            logger.println(any<String>())
          } answers {
            if (firstArg<String>().contains("Deleting member")) {
              isDeletingMember = true
            } else {
              fail("Unexpected logger message: ${firstArg<String>()}")
            }
          }
          return logger
        }
      }
      val launcher = TestLauncher(taskListener, virtualChannel)

      val deleteDatasetDecl = spyk(
        DeleteDatasetDeclarative()
      )
      deleteDatasetDecl.setDsn("test")
      deleteDatasetDecl.setMember("testlongmembername")
      runCatching {
        deleteDatasetDecl.perform(
          run,
          workspace,
          env,
          launcher,
          taskListener,
          zosConnection
        )
      }
        .onSuccess { fail("The function should throw an error") }
        .onFailure {
          assertSoftly { it.message shouldContain "Invalid member name: must be 1-8 characters" }
        }

      assertSoftly { isDeletingMember shouldBe true }
    }
  }
})
