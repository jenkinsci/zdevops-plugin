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

import hudson.model.TaskListener
import org.zowe.kotlinsdk.*
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.ZosDsn
import org.zowe.zdevops.Messages


/**
 * Allocates a dataset with the specified parameters
 *   @param listener The TaskListener object for logging messages
 *   @param zosConnection The ZOSConnection object representing the connection to the z/OS system
 *   @param dsn The name of the dataset to be allocated
 *   @param volser The volume serial number where the dataset should be allocated
 *   @param unit The allocation unit for the dataset
 *   @param dsOrg The dataset organization
 *   @param alcUnit The allocation unit for the dataset allocation
 *   @param primary The primary allocation size in cylinders or tracks
 *   @param secondary The secondary allocation size in cylinders or tracks
 *   @param dirBlk The directory block size
 *   @param recFm The record format
 *   @param blkSize The block size in bytes
 *   @param lrecl The record length in bytes
 *   @param storClass The storage class
 *   @param mgntClass The management class
 *   @param dataClass The data class
 *   @param avgBlk The average block size
 *   @param dsnType The dataset name type
 *   @param dsModel The dataset model
 */
fun allocateDataset(listener: TaskListener,
                    zosConnection: ZOSConnection,
                    dsn: String,
                    volser: String?,
                    unit: String?,
                    dsOrg: DatasetOrganization,
                    alcUnit: AllocationUnit?,
                    primary: Int,
                    secondary: Int,
                    dirBlk: Int?,
                    recFm: RecordFormat,
                    blkSize: Int?,
                    lrecl: Int?,
                    storClass: String?,
                    mgntClass: String?,
                    dataClass: String?,
                    avgBlk: Int?,
                    dsnType: DsnameType?,
                    dsModel: String?,
                    failOnExist: Boolean,
) {
    listener.logger.println(Messages.zdevops_declarative_DSN_allocating(dsn, zosConnection.host, zosConnection.zosmfPort))
    val alcParms = CreateDataset(
        volser,
        unit,
        dsOrg,
        alcUnit,
        primary,
        secondary,
        dirBlk,
        recFm,
        blkSize,
        lrecl,
        storClass,
        mgntClass,
        dataClass,
        avgBlk,
        dsnType,
        dsModel
    )
    try {
        ZosDsn(zosConnection).createDsn(dsn, alcParms)
        listener.logger.println(Messages.zdevops_declarative_DSN_allocated_success(dsn))
    } catch (allocateDsEx: Exception) {
        listener.logger.println("Dataset allocation failed. Reason: $allocateDsEx")
        if(failOnExist) {
            throw allocateDsEx
        }
        listener.logger.println("The `failOnExist` option is set to false. Continuing with execution.")
    }
}
