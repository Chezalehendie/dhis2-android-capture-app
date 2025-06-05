package org.dhis2.usescases.customConfigTransformation
import com.google.gson.Gson
import io.reactivex.Flowable
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue

class AutoEnrollmentManagerImpl(private val d2: D2): AutoEnrollmentManager {

    override fun getTrackedEntityDataValues(dataElementsFromSourceProgram: List<SourceProgramStageDataElement>): Flowable<List<TrackedEntityDataValue>> {
        val dataElementsUid = dataElementsFromSourceProgram.map { it.dataElementUid() }
        return d2.trackedEntityModule()
            .trackedEntityDataValues()
            .byDataElement()
            .`in`(dataElementsUid)
            .get()
            .toFlowable()
    }

    override fun getCustomConfigurations(): Flowable<AutoEnrollmentConfigurations> {
        val  configEntry = d2.dataStoreModule()
            .dataStore().byNamespace()
            .eq("programMapping")
            .byKey().eq("mapping_rules")
            .one().blockingGet()
        return if(configEntry !=null){
            d2.dataStoreModule().dataStore().byNamespace().eq("programMapping")
                .byKey().eq("mapping_rules").one().get()
                .toFlowable().map {
                    Gson().fromJson(
                        it.value(),
                        AutoEnrollmentConfigurations::class.java
                    )
                }
        } else{
            Flowable.empty()
        }
//        else Flowable.just(
//            Gson().fromJson(
//                AutoEnrollmentConfigurations.createDefaultEnrollmentConfigObject(),
//                AutoEnrollmentConfigurations::class.java
//            )
//        )
    }
}
