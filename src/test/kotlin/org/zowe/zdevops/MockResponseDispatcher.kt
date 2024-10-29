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

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

data class ValidationListElem(
    val name: String,
    val validator: (RecordedRequest?) -> Boolean,
    val handler: (RecordedRequest?) -> MockResponse
)

class MockResponseDispatcher : Dispatcher() {

    private var validationList = mutableListOf<ValidationListElem>()

    private fun getResourceText(resourcePath: String): String? {
        return javaClass.classLoader.getResource(resourcePath)?.readText()
    }

    fun readMockJson(mockFilePath: String): String? {
        return getResourceText("mock/${mockFilePath}.json")
    }

    fun injectEndpoint(
        name: String,
        validator: (RecordedRequest?) -> Boolean,
        handler: (RecordedRequest?) -> MockResponse
    ) {
        validationList.add(ValidationListElem(name, validator, handler))
    }

    fun removeEndpoint(name: String) {
        validationList.removeAll { it.name == name }
    }

    fun removeAllEndpoints() {
        validationList.clear()
    }

    override fun dispatch(request: RecordedRequest): MockResponse {
        return validationList
            .firstOrNull { it.validator(request) }
            ?.handler
            ?.let { it(request) }
            ?: MockResponse().setBody("Response is not implemented").setResponseCode(404)
    }
}
