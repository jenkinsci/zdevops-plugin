/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2022
 */

package org.zowe.zdevops.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hudson.model.TaskListener

/**
 * A utility function for executing a try-catch block around a specified z/OSMF call and handling exceptions (e.g. getting log message).
 *
 * @param listener The task listener to log information and handle exceptions.
 * @param call The lambda function to be executed.
 * @return A [Result] containing the result of the call if successful, or an error message if an exception occurs.
 */
inline fun <R> runMFTryCatchWrappedQuery(listener: TaskListener, call: () -> R): Result<R> {
    try {
        return Result.success(call())
    } catch (e: Exception) {
        lateinit var responseMap: Map<String, Any>
        try {
            responseMap = Gson().fromJson(e.message, object : TypeToken<Map<String, Any>>() {}.type)
        } catch (eInternal: Exception) {
            if (eInternal.message?.contains(Regex("Expected .* but was STRING")) == true) {
                listener.logger.println(e.message)
            } else {
                listener.logger.println(eInternal.message)
            }
        }
        var errorContent: Any
        try {
            errorContent = responseMap.get("details") as ArrayList<*>
            errorContent.forEach {listener.logger.println(it)}
        } catch (e: NullPointerException) {
            errorContent = responseMap.get("message") as String
            listener.logger.println(errorContent)
        }
        throw Exception(e)
    }
}

/**
 * Extracts a submit job message from HTTP JSON response.
 *
 * @param httpJson The HTTP JSON response containing the message.
 * @return The extracted submit job message.
 */
fun extractSubmitJobMessage(httpJson: String) : String? {
    val regex = Regex("message=(.*?)(\\\\n|\\z)")
    val matchResult = regex.find(httpJson)
    return matchResult?.groups?.get(1)?.value
}