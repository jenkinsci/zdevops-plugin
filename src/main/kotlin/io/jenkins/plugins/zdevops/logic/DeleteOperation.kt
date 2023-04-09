/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2022
 */

package io.jenkins.plugins.zdevops.logic

import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.ZosDsn
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.ZosDsnList
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.input.ListParams
import io.jenkins.plugins.zdevops.declarative.jobs.zMessages
import io.jenkins.plugins.zdevops.utils.runMFTryCatchWrappedQuery
import hudson.AbortException
import hudson.model.TaskListener

/**
 * This class contains logic for mainframe datasets deletion
 */
class DeleteOperation {

    companion object {
        private val successMessage: String = zMessages.zdevops_deleting_ds_success()

        fun deleteDatasetsByMask(mask: String, zosConnection: ZOSConnection, listener: TaskListener) {
            if (mask.isEmpty()) {
                throw AbortException(zMessages.zdevops_deleting_datasets_by_mask_but_mask_is_empty())
            }
            listener.logger.println(zMessages.zdevops_deleting_ds_by_mask(mask))
            val dsnList = ZosDsnList(zosConnection).listDsn(mask, ListParams())
            if (dsnList.items.isEmpty()) {
                throw AbortException(zMessages.zdevops_deleting_ds_fail_no_matching_mask())
            }
            dsnList.items.forEach {
                runMFTryCatchWrappedQuery(listener) {
                    listener.logger.println(zMessages.zdevops_deleting_ds(it.name, zosConnection.host, zosConnection.zosmfPort))
                    ZosDsn(zosConnection).deleteDsn(it.name)
                }
            }
            listener.logger.println(successMessage)
        }

        fun deleteDatasetOrMember(dsn: String, member: String, zosConnection: ZOSConnection, listener: TaskListener) {
            if (dsn.isEmpty()) {
                throw AbortException(zMessages.zdevops_deleting_ds_fail_dsn_param_empty())
            }
            val memberNotEmpty = member.isNotEmpty()
            val logMessage = if (memberNotEmpty) zMessages.zdevops_deleting_ds_member(member, dsn, zosConnection.host, zosConnection.zosmfPort)
                             else zMessages.zdevops_deleting_ds(dsn, zosConnection.host, zosConnection.zosmfPort)
            listener.logger.println(logMessage)
            runMFTryCatchWrappedQuery(listener) {
                val response = if (memberNotEmpty) {
                    isMemberNameValid(member)
                    ZosDsn(zosConnection).deleteDsn(dsn, member)
                }
                else ZosDsn(zosConnection).deleteDsn(dsn)
            }
            listener.logger.println(successMessage)
        }

        private fun isMemberNameValid(member: String) {
            if (member.length > 8 || member.isEmpty())
                throw Exception(zMessages.zdevops_member_name_invalid())
        }

    }
}
