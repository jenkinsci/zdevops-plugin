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
import org.zowe.kotlinsdk.DatasetOrganization
import org.zowe.kotlinsdk.RecordFormat
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.MOCK_SERVER_HOST
import org.zowe.zdevops.MockResponseDispatcher
import org.zowe.zdevops.MockServerFactory
import java.io.File
import java.io.PrintStream
import java.nio.file.Paths

class AllocateDatasetDeclarativeSpec : ShouldSpec({
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
  context("declarative/jobs module: AllocateDatasetDeclarative") {
    val virtualChannel = TestVirtualChannel()
    val zosConnection = ZOSConnection(mockServer.hostName, mockServer.port.toString(), "test", "test", "https")
    val rootDir = Paths.get("").toAbsolutePath().toString()
    val trashDir = tempdir()
    val itemGroup = object : TestItemGroup() {
      override fun getRootDirFor(child: Item?): File {
        return trashDir
      }
    }
    val job = TestJob(itemGroup, "test")
    val run = TestRun(job)
    val mockDir = Paths.get(rootDir, "src", "test", "resources", "mock", "here").toString()
    val workspace = FilePath(File(mockDir))
    val env = EnvVars()

    afterEach {
      responseDispatcher.removeAllEndpoints()
    }
    should("perform AllocateDatasetDeclarative operation to allocate a PDS dataset") {
      var isAllocatingDS = false
      var isAllocatedSucc = false
      val taskListener = object : TestBuildListener() {
        override fun getLogger(): PrintStream {
          val logger = mockk<PrintStream>()
          every {
            logger.println(any<String>())
          } answers {
            if (firstArg<String>().contains("Allocating dataset")) {
              isAllocatingDS = true
            } else if (firstArg<String>().contains("has been allocated successfully")) {
              isAllocatedSucc = true
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
        { it?.requestLine?.contains("/zosmf/restfiles/ds/") ?: false },
        { MockResponse().setBody("{}") }
      )

      val allocateDSDecl = spyk(
        AllocateDatasetDeclarative("TEST.IJMP.DATASET1", DatasetOrganization.PO, 1, 1, RecordFormat.F)
      )
      allocateDSDecl.perform(
        run,
        workspace,
        env,
        launcher,
        taskListener,
        zosConnection
      )

      assertSoftly { isAllocatingDS shouldBe true }
      assertSoftly { isAllocatedSucc shouldBe true }
    }
  }
})
