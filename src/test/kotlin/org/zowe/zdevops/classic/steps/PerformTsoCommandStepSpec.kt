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
import java.io.File
import java.io.PrintStream

class PerformTsoCommandStepSpec : ShouldSpec({
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
    context("classic/steps module: PerformTsoCommandStep") {
        val trashDir = tempdir()
        val itemGroup = object : TestItemGroup() {
            override fun getRootDirFor(child: Item?): File {
                return trashDir
            }
        }
        val project = TestProject(itemGroup, "test")
        val virtualChannel = TestVirtualChannel()
        val build = TestBuild(project)
        val zosConnection = ZOSConnection(mockServer.hostName, mockServer.port.toString(), "test", "test", "https")

        afterEach {
            responseDispatcher.removeAllEndpoints()
        }
        should("perform PerformTsoCommandStep operation") {
            var isPreExecuteStage = false
            var isCommandExecuted = false
            val taskListener = object : TestBuildListener() {
                override fun getLogger(): PrintStream {
                    val logger = mockk<PrintStream>()
                    every {
                        logger.println(any<String>())
                    } answers {
                        if (firstArg<String>().contains("Issuing command")) {
                            isPreExecuteStage = true
                        } else if (firstArg<String>().contains("The command has been successfully executed")) {
                            isCommandExecuted = true
                        }
                    }
                    return logger
                }
            }
            val launcher = TestLauncher(taskListener, virtualChannel)

            responseDispatcher.injectEndpoint(
                this.testCase.name.testName,
                { it?.requestLine?.contains("POST /zosmf/tsoApp/tso") ?: false },
                { MockResponse()
                    .setResponseCode(200)
                    .setBody(responseDispatcher.readMockJson("startTsoResponse") ?: "") }
            )
            responseDispatcher.injectEndpoint(
                this.testCase.name.testName,
                { it?.requestLine?.contains("GET /zosmf/tsoApp/tso/") ?: false },
                { MockResponse()
                    .setResponseCode(200)
                    .setBody(responseDispatcher.readMockJson("getTsoResponse") ?: "") }
            )
            responseDispatcher.injectEndpoint(
                this.testCase.name.testName,
                { it?.requestLine?.contains("PUT /zosmf/tsoApp/tso/") ?: false },
                { MockResponse()
                    .setResponseCode(200)
                    .setBody(responseDispatcher.readMockJson("sendTsoResponse") ?: "") }
            )
            responseDispatcher.injectEndpoint(
                this.testCase.name.testName,
                { it?.requestLine?.contains("DELETE /zosmf/tsoApp/tso/") ?: false },
                { MockResponse()
                    .setResponseCode(200)
                    .setBody(responseDispatcher.readMockJson("endTsoResponse") ?: "") }
            )

            val performTsoCommandInst = spyk(
                PerformTsoCommandStep(
                    "test",
                    "test",
                    "TIME"
                )
            )
            performTsoCommandInst.perform(
                build,
                launcher,
                taskListener,
                zosConnection
            )
            assertSoftly { isPreExecuteStage shouldBe true }
            assertSoftly { isCommandExecuted shouldBe true }
        }
        should("fail PerformTsoCommand operation") {
            var isPreExecuteStage = false
            var isExecuteCommandFailLogged = false
            val taskListener = object : TestBuildListener() {
                override fun getLogger(): PrintStream {
                    val logger = mockk<PrintStream>()
                    every {
                        logger.println(any<String>())
                    } answers {
                        if (firstArg<String>().contains("Issuing command")) {
                            isPreExecuteStage = true
                        } else if (firstArg<String>().contains("TSO command execution failed")) {
                            isExecuteCommandFailLogged = true
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
                { it?.requestLine?.contains("zosmf/tsoApp/tso/") ?: false },
                { MockResponse()
                    .setResponseCode(500)
                    .setBody("") }
            )

            val performTsoCommandStepInst = spyk(
                PerformTsoCommandStep(
                    "test",
                    "test",
                    "123",
                )
            )
            runCatching {
                performTsoCommandStepInst.perform(
                    build,
                    launcher,
                    taskListener,
                    zosConnection
                )
            }
                .onSuccess {
                    fail("The 'perform' operation will fail")
                }
                .onFailure {
                    assertSoftly { isPreExecuteStage shouldBe true }
                    assertSoftly { isExecuteCommandFailLogged shouldBe true }
                }
        }
    }
})