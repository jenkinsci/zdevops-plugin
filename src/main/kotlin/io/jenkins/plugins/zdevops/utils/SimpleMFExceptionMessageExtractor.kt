/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2022
 */

package io.jenkins.plugins.zdevops.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hudson.model.TaskListener
import java.lang.NullPointerException

inline fun <R> runMFTryCatchWrappedQuery(listener: TaskListener, call: () -> R): Result<R> {
    try {
        return Result.success(call())
    } catch (e: Exception) {
        val responseMap: Map<String, Any> = Gson().fromJson(e.message, object : TypeToken<Map<String, Any>>() {}.type)
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
