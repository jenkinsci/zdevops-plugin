/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2022
 */

package org.zowe.zdevops.declarative.jobs

import hudson.EnvVars
import hudson.Extension
import hudson.FilePath
import hudson.Launcher
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter
import org.zowe.kotlinsdk.*
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.Messages
import org.zowe.zdevops.declarative.AbstractZosmfAction
import org.zowe.zdevops.logic.allocateDataset

/**
 * Represents an action for allocating a dataset in a declarative style
 *
 * @see org.zowe.zdevops.logic.AllocateOperation
 */
class AllocateDatasetDeclarative
/**
 * Constructs a new instance of AllocateDatasetDeclarative
 *
 * @param dsn The name of the dataset to be allocated
 * @param dsOrg The dataset organization
 * @param primary The primary allocation size in cylinders or tracks
 * @param secondary The secondary allocation size in cylinders or tracks
 * @param recFm The record format
 */
@DataBoundConstructor
constructor(
   private val dsn: String,
   private val dsOrg: DatasetOrganization,
   private val primary: Int,
   private var secondary: Int,
   private var recFm: RecordFormat,
   private var failOnExist: Boolean = false) :
    AbstractZosmfAction() {

    private var volser: String? = null
    private var unit: String? = null
    private var alcUnit : AllocationUnit? = null
    private var dirBlk : Int? = null
    private var blkSize: Int? = null
    private var lrecl: Int? = null
    private var storClass: String? = null
    private var mgntClass: String? = null
    private var dataClass: String? = null
    private var avgBlk: Int? = null
    private var dsnType: DsnameType? = null
    private var dsModel: String? = null

    @DataBoundSetter
    fun setVolser(volser: String) { this.volser = volser }
    @DataBoundSetter
    fun setUnit(unit: String) { this.unit = unit }
    @DataBoundSetter
    fun setAlcUnit(alcUnit: AllocationUnit) { this.alcUnit = alcUnit }
    @DataBoundSetter
    fun setDirBlk(dirBlk: Int) { this.dirBlk = dirBlk }
    @DataBoundSetter
    fun setBlkSize(blkSize: Int) { this.blkSize = blkSize }
    @DataBoundSetter
    fun setLrecl(lrecl: Int) { this.lrecl = lrecl }
    @DataBoundSetter
    fun setStorClass(storClass: String) { this.storClass = storClass }
    @DataBoundSetter
    fun setMgntClass(mgntClass: String) { this.mgntClass = mgntClass }
    @DataBoundSetter
    fun setDataClass(dataClass: String) { this.dataClass = dataClass }
    @DataBoundSetter
    fun setAvgBlk(avgBlk: Int) { this.avgBlk = avgBlk }
    @DataBoundSetter
    fun setDsnType(dsnType: DsnameType) { this.dsnType = dsnType }
    @DataBoundSetter
    fun setDsModel(dsModel: String) { this.dsModel = dsModel }

    override val exceptionMessage: String = Messages.zdevops_declarative_DSN_allocated_fail(dsn)

    /**
     * Performs the allocation of the dataset
     *
     * @param run The current build/run
     * @param workspace The workspace where the build is being executed
     * @param env The environment variables for the build
     * @param launcher The launcher for executing commands
     * @param listener The listener for logging messages
     * @param zosConnection The ZOSConnection for interacting with z/OS
     */
    override fun perform(
        run: Run<*, *>,
        workspace: FilePath,
        env: EnvVars,
        launcher: Launcher,
        listener: TaskListener,
        zosConnection: ZOSConnection
    ) {
        allocateDataset(
            listener,
            zosConnection,
            dsn,
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
            dsModel,
            failOnExist,
        )
    }


    /**
     * The DescriptorImpl class represents the descriptor for the AllocateDatasetDeclarative class
     */
    @Symbol("allocateDS")
    @Extension
    class DescriptorImpl : Companion.DefaultBuildDescriptor("Allocate Dataset Declarative")
}
