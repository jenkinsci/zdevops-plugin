/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2023
 */

package org.zowe.zdevops.classic.steps

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
import org.zowe.zdevops.declarative.jobs.TestBuildListener
import org.zowe.zdevops.declarative.jobs.TestItemGroup
import org.zowe.zdevops.declarative.jobs.TestLauncher
import org.zowe.zdevops.declarative.jobs.TestVirtualChannel
import java.io.File
import java.io.PrintStream

class WriteToFileStepSpec : ShouldSpec({
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
    context("classic/steps module: WriteToFileStep") {
        val virtualChannel = TestVirtualChannel()
        val zosConnection = ZOSConnection(mockServer.hostName, mockServer.port.toString(), "test", "test", "https")
        val tempDir = tempdir()
        val itemGroup = object : TestItemGroup() {
            override fun getRootDirFor(child: Item?): File {
                return tempDir
            }
        }
        val project = TestProject(itemGroup, "test")
        val build = TestBuild(project)

        afterEach {
            responseDispatcher.removeAllEndpoints()
        }
        should("perform WriteToFileStep operation to write text to a USS file") {
            var isWritingToFile = false
            var isWritten = false
            val taskListener = object : TestBuildListener() {
                override fun getLogger(): PrintStream {
                    val logger = mockk<PrintStream>()
                    every {
                        logger.println(any<String>())
                    } answers {
                        if (firstArg<String>().contains("Writing to Unix file")) {
                            isWritingToFile = true
                        } else if (firstArg<String>().contains("Data has been written to Unix file")) {
                            isWritten = true
                        } else {
                            fail("Unexpected logger message: ${firstArg<String>()}")
                        }
                    }
                    return logger
                }
            }
            val filePath = "/u/TEST/test.txt"
            val launcher = TestLauncher(taskListener, virtualChannel)
            responseDispatcher.injectEndpoint(
                "${this.testCase.name.testName}_UssFile",
                { it?.requestLine?.contains("zosmf/restfiles/fs${filePath}") ?: false },
                { MockResponse().setBody("") }
            )

            val writeTextToFileDecl = spyk(
                WriteToFileStep("test", filePath, "TEXT TO WRITE")
            )
            writeTextToFileDecl.perform(
                build,
                launcher,
                taskListener,
                zosConnection
            )
            assertSoftly { isWritingToFile shouldBe true }
            assertSoftly { isWritten shouldBe true }
        }
    }
})