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
import hudson.FilePath
import hudson.model.TaskListener
import org.apache.commons.io.IOUtils
import org.zowe.kotlinsdk.DatasetOrganization
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.ZosDsnDownload
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.ZosDsnList
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.input.DownloadParams
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.input.ListParams
import org.zowe.zdevops.Messages
import java.io.File
import java.io.InputStream
import java.io.StringWriter



/**
 * Downloads one PS dataset or a PDS member from the z/OS system.
 *
 * @param dsn              The name of the dataset to download.
 * @param vol              The volume on which the dataset resides.
 * @param returnEtag       Specifies whether to return the ETag value for the dataset. If it is not present, the default is to only send an Etag for data sets smaller than a system determined length, which is at least 8 MB
 * @param zosConnection    The connection to the z/OS system.
 * @param workspace        The workspace where the dataset will be downloaded.
 * @param listener         The listener for capturing task progress and logs.
 */
fun downloadDS(
    dsn: String,
    vol: String?,
    returnEtag: Boolean?,
    zosConnection: ZOSConnection,
    workspace: FilePath,
    listener: TaskListener
) {
    var downloadedDSN: InputStream?
    try {
        downloadedDSN = ZosDsnDownload(zosConnection).downloadDsn(dsn, DownloadParams(dsn, returnEtag, vol))
    } catch (e:Exception) {
        throw AbortException("Can't download $dsn ${ if(vol.isNullOrBlank()) "on volume $vol" else ""} due to ${e.message}")
    }
    val writer = StringWriter()
    IOUtils.copy(downloadedDSN, writer, "UTF-8")
    val file = File("$workspace\\$dsn")
    file.writeText(writer.toString())
    listener.logger.println(Messages.zdevops_declarative_DSN_downloaded_success(dsn))
}

/**
 * Downloads a dataset or dataset members from the z/OS system.
 *
 * @param dsn              The name of the dataset or dataset member to download.
 * @param vol              The volume on which the dataset resides.
 * @param returnEtag       Specifies whether to return the ETag value for the dataset.
 * @param listener         The listener for capturing task progress and logs.
 * @param zosConnection    The connection to the z/OS system.
 * @param workspace        The workspace where the dataset will be downloaded.
 */
fun downloadDSOrDSMemberByType(
    dsn: String,
    vol: String?,
    returnEtag: Boolean?,
    listener: TaskListener,
    zosConnection: ZOSConnection,
    workspace: FilePath
) {
    listener.logger.println(Messages.zdevops_declarative_DSN_downloading(dsn, vol, zosConnection.host, zosConnection.zosmfPort))
    val dsnMemberPattern = Regex("[\\w#\$@.-]{1,}\\([\\w#\$@]{1,8}\\)") //means it's a PDS member
    if (dsn.contains(dsnMemberPattern)) {
        downloadDS(dsn, vol, returnEtag, zosConnection, workspace, listener)
    } else {
        val dsnList = ZosDsnList(zosConnection).listDsn(dsn, ListParams(vol))
        if (dsnList.items.isEmpty()) {
            throw AbortException("Can't find $dsn ${ if(vol.isNullOrBlank()) "" else "on volume $vol"}")
        }
        when (dsnList.items.first().datasetOrganization) {
            DatasetOrganization.PS -> downloadDS(dsn, vol, returnEtag, zosConnection, workspace, listener)
            DatasetOrganization.PO, DatasetOrganization.POE -> {
                listener.logger.println(Messages.zdevops_declarative_DSN_downloading_members(dsn))
                ZosDsnList(zosConnection).listDsnMembers(dsn, ListParams(vol)).items.forEach {
                    downloadDS("${dsn}(${it.name})", vol, returnEtag, zosConnection, workspace, listener)
                }
            }
            else -> listener.logger.println(Messages.zdevops_declarative_DSN_downloading_invalid_dsorg())
        }
    }
}
