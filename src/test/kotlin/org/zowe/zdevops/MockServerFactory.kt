/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2022
 */

package org.zowe.zdevops

import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import java.net.InetAddress
import java.util.concurrent.TimeUnit

class MockServerFactory {
    lateinit var mockServer: MockWebServer
    lateinit var responseDispatcher: MockResponseDispatcher

    fun startMockServer (host: String): MockWebServer {
        val localhost = InetAddress.getByName(host).canonicalHostName
        val localhostCertificate = HeldCertificate.Builder()
            .addSubjectAlternativeName(localhost)
            .duration(10, TimeUnit.MINUTES)
            .build()
        val serverCertificates = HandshakeCertificates.Builder()
            .heldCertificate(localhostCertificate)
            .build()
        mockServer = MockWebServer()
        responseDispatcher = MockResponseDispatcher()
        mockServer.dispatcher = responseDispatcher
        mockServer.useHttps(serverCertificates.sslSocketFactory(), false)
        mockServer.start()
        return mockServer
    }

    fun stopMockServer() {
        mockServer.shutdown()
    }
}