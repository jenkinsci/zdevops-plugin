/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2023
 */

package org.zowe.zdevops.logic

import hudson.AbortException
import hudson.model.TaskListener
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.ZosDsn
import org.zowe.kotlinsdk.zowe.client.sdk.zosuss.ZosUssFile
import org.zowe.zdevops.Messages
import org.zowe.zdevops.utils.runMFTryCatchWrappedQuery



/**
 * Validates the text to be written to a dataset
 *
 * @param listener      The listener for logging messages
 * @param zosConnection The ZOSConnection for interacting with z/OS
 * @param dsn           The name of the dataset
 * @param text          The text content to be written
 * @throws AbortException if the text is empty or contains ineligible strings.
 */
private fun validateTextForDataset(
    listener: TaskListener,
    zosConnection: ZOSConnection,
    dsn: String,
    text: String,
    ) {
    if(text == "") {
        listener.logger.println(Messages.zdevops_declarative_writing_skip())
        return
    }

    val stringList = text.split('\n')
    val targetDS = ZosDsn(zosConnection).getDatasetInfo(dsn)
    if (targetDS.recordLength == null) {
        throw AbortException(Messages.zdevops_declarative_writing_DS_no_info(dsn))
    }
    var ineligibleStrings = 0
    stringList.forEach {
        if (it.length > targetDS.recordLength!!) {
            ineligibleStrings++
        }
    }
    if (ineligibleStrings > 0) {
        throw AbortException(Messages.zdevops_declarative_writing_DS_ineligible_strings(ineligibleStrings,dsn))
    }
}

/**
 * Writes the text content to a dataset
 *
 * @param listener      The listener for logging messages
 * @param zosConnection The ZOSConnection for interacting with z/OS
 * @param dsn           The name of the dataset
 * @param text          The text content to be written
 * @throws AbortException if the text is not valid for the dataset or an error occurs during the write operation
 */
fun writeToDataset(listener: TaskListener,
                   zosConnection: ZOSConnection,
                   dsn: String,
                   text: String,
                   ) {
    validateTextForDataset(listener, zosConnection, dsn, text)
    val textByteArray = text.replace("\r","").toByteArray()
    runMFTryCatchWrappedQuery(listener) {
        ZosDsn(zosConnection).writeDsn(dsn, textByteArray)
    }
    listener.logger.println(Messages.zdevops_declarative_writing_DS_success(dsn))
}


/**
 * Writes the text content to a member
 *
 * @param listener      The listener for logging messages
 * @param zosConnection The ZOSConnection for interacting with z/OS
 * @param dsn           The name of the dataset
 * @param member        The name of the member
 * @param text          The text content to be written
 * @throws AbortException if the text is not valid for the dataset or an error occurs during the write operation
 */
fun writeToMember(listener: TaskListener,
                  zosConnection: ZOSConnection,
                  dsn: String,
                  member: String,
                  text: String,) {
    validateTextForDataset(listener, zosConnection, dsn, text)
    val textByteArray = text.replace("\r","").toByteArray()
    runMFTryCatchWrappedQuery(listener) {
        ZosDsn(zosConnection).writeDsn(dsn, member, textByteArray)
    }
    listener.logger.println(Messages.zdevops_declarative_writing_DS_success(dsn))
}

//TODO: docs
fun writeToFile(listener: TaskListener,
                zosConnection: ZOSConnection,
                destFile: String,
                textBytes: ByteArray,
                binary: Boolean?,) {
    if (textBytes.isNotEmpty()) {
        runMFTryCatchWrappedQuery(listener) {
            if (binary == true) {
                ZosUssFile(zosConnection).writeToFileBin(destFile, textBytes)
            } else {
                ZosUssFile(zosConnection).writeToFile(destFile, textBytes)
            }
        }
        listener.logger.println(Messages.zdevops_declarative_writing_file_success(destFile))
    } else {
        listener.logger.println(Messages.zdevops_declarative_writing_skip())
    }
}
