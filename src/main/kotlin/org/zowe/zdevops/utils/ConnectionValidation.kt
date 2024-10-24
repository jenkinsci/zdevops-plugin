/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2024
 */

package org.zowe.zdevops.utils

import hudson.AbortException
import hudson.model.TaskListener
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.ZosDsnList
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.input.ListParams
import org.zowe.zdevops.Messages
import org.zowe.zdevops.config.ZOSConnectionList
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URL


/**
 * Gets a list of datasets
 * Calls the listDsn function of ZosDsnList to list data set names.
 * Passes a test data set name ('HELLO.THERE').
 *
 * @param zosConnection The ZOSConnection object representing the connection to the z/OS system.
 */
fun getTestDatasetList(zosConnection: ZOSConnection) {
  ZosDsnList(zosConnection).listDsn(Messages.zdevops_config_ZOSConnection_validation_testDS(), ListParams())
}

/**
 * Validates a z/OS connection.
 *
 * @param zosConnection The ZOSConnection object representing the connection to the z/OS system.
 */
fun validateConnection(zosConnection: ZOSConnection) {
  try {
    getTestDatasetList(zosConnection)
  } catch (connectException: Exception) {
    val connExMessage = "Failed to connect to z/OS (${zosConnection.user}@${zosConnection.host}:${zosConnection.zosmfPort}): ${connectException.message}"
    throw AbortException(connExMessage)
  }
}

/**
 * Retrieves zOS connection by its name from the zOS connection list in Jenkins configuration.
 *
 * This function attempts to resolve the connection from the ZOSConnectionList
 * using the provided connection name. If the connection  is not found in the list, an
 * IllegalArgumentException is thrown with a detailed error message, and the stack trace
 * is logged using the provided TaskListener.
 *
 * @param connectionName The name of the connection to resolve.
 * @param listener The TaskListener used to log messages and exceptions.
 * @return A ZOSConnection object containing the resolved connection details.
 * @throws IllegalArgumentException If the connection configuration cannot be resolved.
 */
fun getZoweZosConnection(connectionName: String, listener: TaskListener?): ZOSConnection {
  val connection = ZOSConnectionList.resolve(connectionName) ?: run {
    val exception = IllegalArgumentException(Messages.zdevops_config_ZOSConnection_resolve_unknown(connectionName))
    val sw = StringWriter()
    exception.printStackTrace(PrintWriter(sw))
    listener?.logger?.println(sw.toString())
    throw exception
  }

  val connURL = URL(connection.url)
  return ZOSConnection(connURL.host, connURL.port.toString(), connection.username, connection.password, connURL.protocol)
}
