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

import hudson.FilePath
import hudson.model.Executor
import hudson.model.Item
import hudson.util.FormValidation
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.fail
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.mockk.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.MOCK_SERVER_HOST
import org.zowe.zdevops.Messages
import org.zowe.zdevops.MockResponseDispatcher
import org.zowe.zdevops.MockServerFactory
import java.io.File
import java.io.PrintStream
import java.nio.file.Paths

class WriteFileToDatasetStepSpec : ShouldSpec({
    lateinit var mockServer: MockWebServer
    lateinit var responseDispatcher: MockResponseDispatcher
    val mockServerFactory = MockServerFactory()

    beforeSpec {
        mockServer = mockServerFactory.startMockServer(MOCK_SERVER_HOST)
        responseDispatcher = mockServerFactory.responseDispatcher
    }
    afterSpec {
        mockServerFactory.stopMockServer()
        clearAllMocks()
    }
    context("classic/steps module: WriteFileToDatasetStep") {
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
                val mockDir = Paths.get(rootDir, "src", "test", "resources", "mock").toString()
                every { mockInstance.currentWorkspace } returns FilePath(virtualChannel, mockDir)
                return mockInstance
            }
        }

        afterEach {
            responseDispatcher.removeAllEndpoints()
        }
        should("perform WriteFileToDatasetStep operation to write a file to a dataset") {
            var isWritingToDataset = false
            var isWritten = false
            val taskListener = object : TestBuildListener() {
                override fun getLogger(): PrintStream {
                    val logger = mockk<PrintStream>()
                    every {
                        logger.println(any<String>())
                    } answers {
                        if (firstArg<String>().contains("Writing to dataset")) {
                            isWritingToDataset = true
                        } else if (firstArg<String>().contains("Data has been written to dataset")) {
                            isWritten = true
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
                { it?.requestLine?.contains("zosmf/restfiles/ds") ?: false },
                { MockResponse().setBody(responseDispatcher.readMockJson("listDataSets") ?: "") }
            )

            val writeFileToDatasetDecl = spyk(
                WriteFileToDatasetStep("test", "TEST.IJMP.DATASET1", "workspace")
            )
            writeFileToDatasetDecl.setWorkspacePath("test_file.txt")
            writeFileToDatasetDecl.perform(
                build,
                launcher,
                taskListener,
                zosConnection
            )
            assertSoftly { isWritingToDataset shouldBe true }
            assertSoftly { isWritten shouldBe true }
        }
    }

    val descriptor = WriteFileToDatasetStep.DescriptorImpl()
    context("classic/steps module: WriteFileToDatasetStep.DescriptorImpl") {

        should("validate dataset name") {
            descriptor.doCheckDsn("") shouldBe FormValidation.error(Messages.zdevops_value_must_not_be_empty_validation())
            descriptor.doCheckDsn("MY_DATASET") shouldBe FormValidation.error(Messages.zdevops_dataset_name_is_invalid_validation())
        }

        should("validate file option") {
            descriptor.doCheckFileOption("") shouldBe FormValidation.error(Messages.zdevops_classic_write_options_required())
            descriptor.doCheckFileOption(descriptor.localFileOption) shouldBe FormValidation.ok()
        }

        should("validate local file path") {
            descriptor.doCheckLocalFilePath("", fileOption = descriptor.localFileOption) shouldBe FormValidation.error(
                Messages.zdevops_value_must_not_be_empty_validation())
            descriptor.doCheckLocalFilePath("D:\\file.txt", fileOption = descriptor.localFileOption) shouldBe  FormValidation.ok()
            descriptor.doCheckLocalFilePath("", fileOption = descriptor.chooseFileOption) shouldBe FormValidation.ok()
        }

        should("validate workspace file path") {
            descriptor.doCheckWorkspacePath("", fileOption = descriptor.workspaceFileOption) shouldBe FormValidation.error(
                Messages.zdevops_value_must_not_be_empty_validation())
            descriptor.doCheckWorkspacePath("D:\\file.txt", fileOption = descriptor.workspaceFileOption) shouldBe  FormValidation.ok()
            descriptor.doCheckWorkspacePath("", fileOption = descriptor.chooseFileOption) shouldBe FormValidation.ok()
        }
    }
})
