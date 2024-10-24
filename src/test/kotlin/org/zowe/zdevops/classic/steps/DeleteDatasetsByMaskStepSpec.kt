package org.zowe.zdevops.classic.steps

import hudson.AbortException
import hudson.FilePath
import hudson.model.Executor
import hudson.model.Item
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
import org.zowe.zdevops.MockResponseDispatcher
import org.zowe.zdevops.MockServerFactory
import org.zowe.zdevops.declarative.jobs.TestBuildListener
import org.zowe.zdevops.declarative.jobs.TestLauncher
import java.io.File
import java.io.PrintStream
import java.nio.file.Paths

class DeleteDatasetsByMaskStepSpec : ShouldSpec({
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
    context("classic/steps module: DeleteDatasetStep") {
        val virtualChannel = TestVirtualChannel()
        val zosConnection = ZOSConnection(mockServer.hostName, mockServer.port.toString(), "test", "test", "https")
        val rootDir = Paths.get("").toAbsolutePath().toString()
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
                val mockDir = Paths.get(rootDir, "src", "test", "resources", "mock", "test_file.txt").toString()
                every { mockInstance.currentWorkspace } returns FilePath(virtualChannel, mockDir)
                return mockInstance
            }
        }

        afterEach {
            responseDispatcher.removeAllEndpoints()
        }
        should("perform DeleteDatasetsByMaskStep operation that deletes datasets") {
            var isDeletingDatasets = false
            var isSuccessfullyDeleted = false
            val taskListener = object : TestBuildListener() {
                override fun getLogger(): PrintStream {
                    val logger = mockk<PrintStream>()
                    every {
                        logger.println(any<String>())
                    } answers {
                        if (firstArg<String>().contains("Deleting dataset")) {
                            isDeletingDatasets = true
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
                "${this.testCase.name.testName}_listDataSets",
                { it?.requestLine?.contains("zosmf/restfiles/ds?dslevel") ?: false },
                { MockResponse().setBody(responseDispatcher.readMockJson("listDataSets") ?: "") }
            )

            responseDispatcher.injectEndpoint(
                this.testCase.name.testName,
                { it?.requestLine?.contains(Regex("DELETE /zosmf/restfiles/ds/.* HTTP/.*")) ?: false },
                { MockResponse().setBody("{}") }
            )

            val deleteDatasetDecl = spyk(
                DeleteDatasetsByMaskStep("test", "TEST.IJMP.DATASET%")
            )
            deleteDatasetDecl.perform(
                build,
                launcher,
                taskListener,
                zosConnection
            )

            assertSoftly { isDeletingDatasets shouldBe true }
            assertSoftly { isSuccessfullyDeleted shouldBe true }
        }
        should("throw AbortException if Dataset List is empty") {
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

            responseDispatcher.injectEndpoint(
                this.testCase.name.testName,
                { it?.requestLine?.contains(Regex("DELETE /zosmf/restfiles/ds/test HTTP/.*")) ?: false },
                { MockResponse().setBody("{}") }
            )

            val deleteDatasetDecl = spyk(
                DeleteDatasetsByMaskStep("test", "TEST.IJMP.DATASET%.NONE", failOnNotExist = true)
            )
            shouldThrow<AbortException> {
                deleteDatasetDecl.perform(
                    build,
                    launcher,
                    taskListener,
                    zosConnection
                )
            }

        }
    }
})