package org.dhis2.usescases.customConfigTransformation
import com.google.gson.Gson
import io.reactivex.Flowable
import org.hisp.dhis.android.core.D2

class AutoEnrollmentManagerImpl(private val d2: D2): AutoEnrollmentManager {

    override fun <trackedEntityDataValues> getTrackedEntityDataValues(dataElement: SourceprogramStageDataElement): Flowable<List<trackedEntityDataValues>> {
        return d2.trackedEntityModule()
            .trackedEntityDataValues()
            .byDataElement().eq(value = dataElement.toString())
            .get()
            .toFlowable() as Flowable<List<trackedEntityDataValues>>
    }

    override fun getCustomConfigurations(): Flowable<AutoEnrollmentConfigurations> {
        val configEntry = d2.dataStoreModule()
            .dataStore().byNamespace()
            .eq("programMapping")
            .byKey().eq("")
            .one().blockingGet()
        return if (configEntry != null) {
            d2.dataStoreModule().dataStore().byNamespace().eq("programMapping")
                .byKey().eq("mapping_rules").one().get()
                .toFlowable().map {
                    Gson().fromJson(
                        it.value(),
                        AutoEnrollmentConfigurations::class.java
                    )
                }
        } else {
            Flowable.empty()
        }
    }
}
