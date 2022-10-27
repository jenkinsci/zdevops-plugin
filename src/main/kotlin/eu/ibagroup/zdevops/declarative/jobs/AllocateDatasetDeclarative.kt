package eu.ibagroup.zdevops.declarative.jobs

import eu.ibagroup.r2z.*
import eu.ibagroup.r2z.zowe.client.sdk.core.ZOSConnection
import eu.ibagroup.r2z.zowe.client.sdk.zosfiles.ZosDsn
import eu.ibagroup.zdevops.declarative.AbstractZosmfAction
import hudson.*
import hudson.FilePath
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter

class AllocateDatasetDeclarative @DataBoundConstructor constructor(private val dsn: String,
                                                                   private val dsOrg: DatasetOrganization,
                                                                   private val primary: Int,
                                                                   private var secondary: Int,
                                                                   private var recFm: RecordFormat) :
    AbstractZosmfAction() {

    private var volser: String? = null
    private var unit: String? = null
//    private var dsOrg: DatasetOrganization? = null
    private var alcUnit : AllocationUnit? = null
//    private var primary: Int? = null
//    private var secondary: Int? = null
    private var dirBlk : Int? = null
//    private var recFm: RecordFormat? = null
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
//    @DataBoundSetter
//    fun setDsOrg(dsOrg: DatasetOrganization) { this.dsOrg = dsOrg }
    @DataBoundSetter
    fun setAlcUnit(alcUnit: AllocationUnit) { this.alcUnit = alcUnit }
//    @DataBoundSetter
//    fun setPrimary(primary: Int) { this.primary = primary }
//    @DataBoundSetter
//    fun setSecondary(secondary: Int) { this.secondary = secondary }
    @DataBoundSetter
    fun setDirBlk(dirBlk: Int) { this.dirBlk = dirBlk }
    @DataBoundSetter
//    fun setRecFm(recFm: RecordFormat) { this.recFm = recFm }
//    @DataBoundSetter
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

    override val exceptionMessage: String = zMessages.zdevops_declarative_DSN_allocated_fail(dsn)

    override fun perform(
        run: Run<*, *>,
        workspace: FilePath,
        env: EnvVars,
        launcher: Launcher,
        listener: TaskListener,
        zosConnection: ZOSConnection
    ) {
        listener.logger.println(zMessages.zdevops_declarative_DSN_allocating(dsn, zosConnection.host, zosConnection.zosmfPort))
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
        val allocatedDS = ZosDsn(zosConnection).createDsn(dsn, alcParms)
        listener.logger.println(zMessages.zdevops_declarative_DSN_allocated_success(dsn))
    }


    @Symbol("allocateDS")
    @Extension
    class DescriptorImpl : Companion.DefaultBuildDescriptor("Allocate Dataset Declarative")
}

