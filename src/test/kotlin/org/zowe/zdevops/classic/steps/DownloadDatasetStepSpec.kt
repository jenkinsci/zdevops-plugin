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

import hudson.AbortException
import hudson.FilePath
import hudson.model.Executor
import hudson.model.Item
import hudson.util.FormValidation
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrow
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
import org.zowe.zdevops.Messages
import org.zowe.zdevops.MockResponseDispatcher
import org.zowe.zdevops.MockServerFactory
import org.zowe.zdevops.declarative.jobs.TestBuildListener
import org.zowe.zdevops.declarative.jobs.TestItemGroup
import org.zowe.zdevops.declarative.jobs.TestLauncher
import org.zowe.zdevops.declarative.jobs.TestVirtualChannel
import java.io.File
import java.io.PrintStream


class DownloadDatasetStepSpec : ShouldSpec({
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
    context("classic/steps module: DownloadDatasetStep") {
        val virtualChannel = TestVirtualChannel()
        val zosConnection = ZOSConnection(mockServer.hostName, mockServer.port.toString(), "test", "test", "https")
        val trashDir = tempdir()
        val itemGroup = object : TestItemGroup() {
            override fun getRootDirFor(child: Item?): File {
                return trashDir
            }
        }
        val project = TestProject(itemGroup, "test")
        val build = object:TestBuild(project) {
            override fun getExecutor(): Executor {
                val mockInstance = mockk<Executor>()
                val mockDir = tempdir()
                every { mockInstance.currentWorkspace } returns FilePath(virtualChannel, mockDir.absolutePath)
                return mockInstance
            }
        }

        afterEach {
            responseDispatcher.removeAllEndpoints()
        }
        should("perform DownloadDatasetStep operation to download sequential dataset") {
            var isDownloadDatasetStarted = false
            var isDownloaded = false
            val taskListener = object : TestBuildListener() {
                override fun getLogger(): PrintStream {
                    val logger = mockk<PrintStream>()
                    every {
                        logger.println(any<String>())
                    } answers {
                        if (firstArg<String>().contains("Downloading dataset")) {
                            isDownloadDatasetStarted = true
                        } else if (firstArg<String>().contains("has been downloaded successfully")) {
                            isDownloaded = true
                        } else {
                            fail("Unexpected logger message: ${firstArg<String>()}")
                        }
                    }
                    return logger
                }
            }
            val launcher = TestLauncher(taskListener, virtualChannel)

            responseDispatcher.injectEndpoint(
                "${this.testCase.name.testName}_listDataSets",
                { it?.requestLine?.contains("zosmf/restfiles/ds?dslevel") ?: false },
                { MockResponse().setBody(responseDispatcher.readMockJson("listDataSetsPS") ?: "") }
            )
            val retrieveDatasetContentResp = javaClass.classLoader.getResource("mock/retrieveDatasetContentResponse.txt")?.readText()
            responseDispatcher.injectEndpoint(
                "${this.testCase.name.testName}_retrieveDatasetContent",
                { it?.requestLine?.contains("/zosmf/restfiles/ds/") ?: false },
                { MockResponse().setBody(retrieveDatasetContentResp ?: "") }
            )

            val downloadFileDecl = spyk(
                DownloadDatasetStep("test", "TEST")
            )
            downloadFileDecl.setVol("TEST")
            downloadFileDecl.setReturnEtag(false)
            downloadFileDecl.perform(
                build,
                launcher,
                taskListener,
                zosConnection
            )

            assertSoftly { isDownloadDatasetStarted shouldBe true }
            assertSoftly { isDownloaded shouldBe true }
        }

        should("perform DownloadDatasetStep operation to download library dataset") {
            var isDownloadDatasetStarted = false
            var isDownloaded = false
            val taskListener = object : TestBuildListener() {
                override fun getLogger(): PrintStream {
                    val logger = mockk<PrintStream>()
                    every {
                        logger.println(any<String>())
                    } answers {
                        if (firstArg<String>().contains("Starting to download") || firstArg<String>().contains("Downloading dataset")) {
                            isDownloadDatasetStarted = true
                        } else if (firstArg<String>().contains("has been downloaded successfully")) {
                            isDownloaded = true
                        } else {
                            fail("Unexpected logger message: ${firstArg<String>()}")
                        }
                    }
                    return logger
                }
            }
            val launcher = TestLauncher(taskListener, virtualChannel)

            responseDispatcher.injectEndpoint(
                "${this.testCase.name.testName}_listDataSets",
                { it?.requestLine?.contains("zosmf/restfiles/ds?dslevel") ?: false },
                { MockResponse().setBody(responseDispatcher.readMockJson("listDataSets") ?: "") }
            )

            responseDispatcher.injectEndpoint(
                "${this.testCase.name.testName}_listDataSetMembers",
                { it?.requestLine?.contains(Regex("zosmf/restfiles/ds/.*\\/member")) ?: false },
                { MockResponse().setBody(responseDispatcher.readMockJson("listDataSetMembers") ?: "") }
            )

            val retrieveDatasetContentResp = javaClass.classLoader.getResource("mock/retrieveDatasetContentResponse.txt")?.readText()
            responseDispatcher.injectEndpoint(
                "${this.testCase.name.testName}_retrieveDatasetContent",
                { it?.requestLine?.contains("/zosmf/restfiles/ds/") ?: false },
                { MockResponse().setBody(retrieveDatasetContentResp ?: "") }
            )


            val downloadFileDecl = spyk(
                DownloadDatasetStep("test", "TEST.IJMP.DATASET1")
            )
//            downloadFileDecl.setVol("TESTVOL")
            downloadFileDecl.setReturnEtag(false)
            downloadFileDecl.perform(
                build,
                launcher,
                taskListener,
                zosConnection
            )

            assertSoftly { isDownloadDatasetStarted shouldBe true }
            assertSoftly { isDownloaded shouldBe true }
        }

        should("perform DownloadDatasetStep operation to download library member") {
            var isDownloadDatasetStarted = false
            var isDownloaded = false
            val taskListener = object : TestBuildListener() {
                override fun getLogger(): PrintStream {
                    val logger = mockk<PrintStream>()
                    every {
                        logger.println(any<String>())
                    } answers {
                        if (firstArg<String>().contains("Downloading dataset")) {
                            isDownloadDatasetStarted = true
                        } else if (firstArg<String>().contains("has been downloaded successfully")) {
                            isDownloaded = true
                        } else {
                            fail("Unexpected logger message: ${firstArg<String>()}")
                        }
                    }
                    return logger
                }
            }
            val launcher = TestLauncher(taskListener, virtualChannel)

            responseDispatcher.injectEndpoint(
                "${this.testCase.name.testName}_listDataSets",
                { it?.requestLine?.contains("zosmf/restfiles/ds?dslevel") ?: false },
                { MockResponse().setBody(responseDispatcher.readMockJson("listDataSets") ?: "") }
            )
            val retrieveDatasetContentResp = javaClass.classLoader.getResource("mock/retrieveDatasetContentResponse.txt")?.readText()
            responseDispatcher.injectEndpoint(
                "${this.testCase.name.testName}_retrieveDatasetContent",
                { it?.requestLine?.contains("/zosmf/restfiles/ds/") ?: false },
                { MockResponse().setBody(retrieveDatasetContentResp ?: "") }
            )

            val downloadFileDecl = spyk(
                DownloadDatasetStep("test", "TEST(TEST)")
            )
            downloadFileDecl.setVol("TEST")
            downloadFileDecl.setReturnEtag(false)
            downloadFileDecl.perform(
                build,
                launcher,
                taskListener,
                zosConnection
            )

            assertSoftly { isDownloadDatasetStarted shouldBe true }
            assertSoftly { isDownloaded shouldBe true }
        }

        should("throw an AbortException as there is no such dataset") {
            val taskListener = object : TestBuildListener() {
                override fun getLogger(): PrintStream {
                    val logger = mockk<PrintStream>()
                    every {
                        logger.println(any<String>())
                    } answers {}
                    return logger
                }
            }
            val launcher = TestLauncher(taskListener, virtualChannel)
            responseDispatcher.injectEndpoint(
                "${this.testCase.name.testName}_listDataSets",
                { it?.requestLine?.contains("zosmf/restfiles/ds?dslevel") ?: false },
                { MockResponse().setBody(responseDispatcher.readMockJson("emptyDataSetsList") ?: "") }
            )

            val downloadFileDecl = spyk(
                DownloadDatasetStep("test", "TEST")
            )
            downloadFileDecl.setVol("TEST")
            downloadFileDecl.setReturnEtag(false)
            shouldThrow<AbortException> {
                downloadFileDecl.perform(
                    build,
                    launcher,
                    taskListener,
                    zosConnection
                )
            }

        }



    }

    val descriptor = DownloadDatasetStep.DescriptorImpl()
    context("classic/steps module: DownloadDatasetStepDescriptor") {

        should("validate dataset name using doCheckDsn") {
            val validDsn = "test.test.test"
            val invalidDsn = "INVALID_DATASET@"

            descriptor.doCheckDsn(validDsn) shouldBe FormValidation.ok()
            descriptor.doCheckDsn(invalidDsn) shouldBe FormValidation.warning(Messages.zdevops_dataset_name_is_invalid_validation())
            descriptor.doCheckDsn("") shouldBe FormValidation.error(Messages.zdevops_value_must_not_be_empty_validation())
        }

        should("validate volume name using doCheckVol") {
            val validVol = "VOLUME"
            val invalidVol = "INVALID_VOLUME_NAME"

            descriptor.doCheckVol(validVol) shouldBe FormValidation.ok()
            descriptor.doCheckVol(invalidVol) shouldBe FormValidation.warning(Messages.zdevops_volume_name_is_invalid_validation())
        }
    }

})
