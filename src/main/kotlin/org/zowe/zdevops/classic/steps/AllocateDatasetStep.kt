/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2023
 */

package org.zowe.zdevops.classic.steps

import hudson.Extension
import hudson.Launcher
import hudson.model.AbstractBuild
import hudson.model.BuildListener
import hudson.util.FormValidation
import hudson.util.ListBoxModel
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter
import org.kohsuke.stapler.QueryParameter
import org.zowe.kotlinsdk.AllocationUnit
import org.zowe.kotlinsdk.DatasetOrganization
import org.zowe.kotlinsdk.DsnameType
import org.zowe.kotlinsdk.RecordFormat
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.Messages
import org.zowe.zdevops.classic.AbstractBuildStep
import org.zowe.zdevops.logic.allocateDataset
import org.zowe.zdevops.utils.validateDatasetName
import java.io.IOException
import javax.servlet.ServletException

/**
 * This class represents a build step that allocates a dataset
 *
 * @see org.zowe.zdevops.logic.AllocateOperation
 */
class AllocateDatasetStep
/**
 * Constructs a new instance of AllocateDatasetStep
 *
 * @param connectionName The name of the z/OS connection
 * @param dsn The name of the dataset to be allocated
 * @param dsOrg The dataset organization
 * @param primary The primary allocation size in cylinders or tracks
 * @param secondary The secondary allocation size in cylinders or tracks
 * @param recFm The record format
 */
@DataBoundConstructor
constructor(
  connectionName: String,
  val dsn: String,
  val dsOrg: DatasetOrganization,
  val primary: Int = 1,
  var secondary: Int,
  var recFm: RecordFormat,
  var failOnExist: Boolean = false,
) : AbstractBuildStep(connectionName){

  private var volser: String? = null
  private var unit: String? = null
  private var alcUnit : AllocationUnit? = null
  private var dirBlk : Int? = null
  private var blkSize: Int? = null
  private var lrecl: Int? = 80
  private var storClass: String? = null
  private var mgntClass: String? = null
  private var dataClass: String? = null
  private var avgBlk: Int? = null
  private var dsnType: DsnameType? = null
  private var dsModel: String? = null

  @DataBoundSetter
  fun setVolser(volser: String?) {
    this.volser = if (volser.isNullOrBlank()) null else volser
  }
  @DataBoundSetter
  fun setUnit(unit: String?) {
    this.unit = if (unit.isNullOrBlank()) null else unit
  }
  @DataBoundSetter
  fun setAlcUnit(alcUnit: AllocationUnit) { this.alcUnit = alcUnit }
  @DataBoundSetter
  fun setDirBlk(dirBlk: Int?) { this.dirBlk = dirBlk }
  @DataBoundSetter
  fun setBlkSize(blkSize: Int?) { this.blkSize = blkSize }
  @DataBoundSetter
  fun setLrecl(lrecl: Int?) { this.lrecl = lrecl }
  @DataBoundSetter
  fun setStorClass(storClass: String?) {
    this.storClass = if (storClass.isNullOrBlank()) null else storClass
  }
  @DataBoundSetter
  fun setMgntClass(mgntClass: String?) {
    this.mgntClass = if (mgntClass.isNullOrBlank()) null else mgntClass
  }
  @DataBoundSetter
  fun setDataClass(dataClass: String?) {
    this.dataClass = if (dataClass.isNullOrBlank()) null else dataClass
  }
  @DataBoundSetter
  fun setAvgBlk(avgBlk: Int?) { this.avgBlk = avgBlk }
  @DataBoundSetter
  fun setDsnType(dsnType: DsnameType) { this.dsnType = dsnType }
  @DataBoundSetter
  fun setDsModel(dsModel: String?) {
    this.dsModel = if (dsModel.isNullOrBlank()) null else dsModel
  }

  fun getVolser(): String? {
    return volser
  }
  fun getUnit(): String? {
    return unit
  }
  fun getAlcUnit(): AllocationUnit? {
    return alcUnit
  }
  fun getDirBlk(): Int? {
    return dirBlk
  }
  fun getBlkSize(): Int? {
    return blkSize
  }
  fun getLrecl(): Int? {
    return lrecl
  }
  fun getStorClass(): String? {
    return storClass
  }
  fun getMgntClass(): String? {
    return mgntClass
  }
  fun getDataClass(): String? {
    return dataClass
  }
  fun getAvgBlk(): Int? {
    return avgBlk
  }
  fun getDsnType(): DsnameType? {
    return dsnType
  }
  fun getDsModel(): String? {
    return dsModel
  }

  /**
   * Performs the dataset allocation build step
   *
   * @param build The current build
   * @param launcher The launcher
   * @param listener The build listener
   * @param zosConnection The z/OS connection
   */
  override fun perform(
    build: AbstractBuild<*, *>,
    launcher: Launcher,
    listener: BuildListener,
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
   * The DescriptorImpl class represents the descriptor for the AllocateDatasetStep class
   */
  @Extension
  class DescriptorImpl : Companion.DefaultBuildDescriptor(Messages.zdevops_classic_allocateDatasetStep_display_name()) {

    /**
     * Fills the allocation unit dropdown list with options
     *
     * @return The list box model for the allocation unit dropdown
     */
    fun doFillAlcUnitItems(): ListBoxModel {
      val result = ListBoxModel()

      result.add("CYL")
      result.add("TRK")

      return result
    }

    /**
     * Fills the record format dropdown list with options
     *
     * @return The list box model for the record format dropdown
     */
    fun doFillRecFmItems(): ListBoxModel {
      val result = ListBoxModel()

      result.add("Fixed-length (F)", "F")
      result.add("Fixed-length, blocked (FB)","FB")
      result.add("Variable-length (V)", "V")
      result.add("Variable-length, blocked (VB)","VB")
      result.add("Undefined-length (U)", "U")
      result.add("Variable-length, ASA print control characters (VA)","VA")

      return result
    }

    /**
     * Fills the dataset name type dropdown list with options
     *
     * @return The list box model for the dataset name type dropdown
     */
    fun doFillDsnTypeItems(): ListBoxModel {
      val result = ListBoxModel()

      result.add("BASIC")
      result.add("LIBRARY")
      result.add("HFS")
      result.add("PDS")
      result.add("LARGE")
      result.add("EXTREQ")
      result.add("EXTPREF")

      return result
    }

    /**
     * Fills the dataset organization dropdown list with options
     *
     * @return The list box model for the dataset organization dropdown
     */
    fun doFillDsOrgItems(): ListBoxModel {
      val result = ListBoxModel()

      result.add("Partitioned organized (PO)", "PO")
      result.add("Partitioned Extended (POE)", "POE")
      result.add("Physical sequentia (PS)", "PS")

      return result
    }

    /**
     * Validates the block size field
     *
     * @param lrecl The record length
     * @param blkSize The block size
     * @return The validation result
     */
    @Throws(IOException::class, ServletException::class)
    fun doCheckBlkSize(@QueryParameter lrecl: String, @QueryParameter blkSize: String): FormValidation? {
      if (lrecl.isEmpty()) return FormValidation.ok()
      try {
          val lreclInt = Integer.parseInt(lrecl)
          val blkSizeInt = Integer.parseInt(blkSize)

          if (lreclInt > blkSizeInt) return FormValidation.warning(Messages.zdevops_classic_allocateDatasetStep_blksize_smaller_than_lrecl_validation())
          return if (blkSizeInt % lreclInt == 0) FormValidation.ok()
          else FormValidation.warning(Messages.zdevops_classic_allocateDatasetStep_blksize_validation_warning())
      } catch (e: NumberFormatException) {
        return FormValidation.warning(Messages.zdevops_value_is_not_number_validation())
      }
    }

    /**
     * Validates the dataset name field
     *
     * @param dsn The dataset name
     * @return The validation result
     */
    fun doCheckDsn(@QueryParameter dsn: String): FormValidation? {
      return validateDatasetName(dsn)
    }

    /**
     * Performs validation for the primary allocation size
     *
     * @param primary The value of the primary allocation size
     * @return The validation result
     */
    fun doCheckPrimary(@QueryParameter primary: String): FormValidation? {
      if (primary.isEmpty()) return FormValidation.ok()
      try {
        val valueInt = primary.toInt()
        if (valueInt == 0) return FormValidation.error(Messages.zdevops_classic_allocateDatasetStep_primary_is_zero_validation())
      } catch (e: NumberFormatException) {
        return FormValidation.error(Messages.zdevops_value_is_not_number_validation())
      }
      return convertStringAndValidateIntPositive(primary)
    }

    /**
     * Performs validation for the secondary allocation size
     *
     * @param secondary The value of the secondary allocation size
     * @return The validation result
     */
    fun doCheckSecondary(@QueryParameter secondary: String): FormValidation? {
      return convertStringAndValidateIntPositive(secondary)
    }

    /**
     * Converts a string value to an integer and validates that it is a positive number
     *
     * @param value The value to be converted and validated
     * @return The validation result as a FormValidation object
     */
    fun convertStringAndValidateIntPositive(value: String): FormValidation? {
      if (value.isEmpty()) return FormValidation.ok()
      return try {
        val valueInt = value.toInt()
        return if (valueInt >= 0) FormValidation.ok()
        else FormValidation.error(Messages.zdevops_value_must_be_positive_number_validation())
      } catch (e: NumberFormatException) {
        FormValidation.error(Messages.zdevops_value_is_not_number_validation())
      }
    }

  }
}