package org.dhis2.usescases.customConfigTransformation

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import io.reactivex.Flowable
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import kotlin.collections.map
import org.dhis2.usescases.customConfigTransformation.networkModels.SourceProgramStageDataElement

class AutoEnrollmentManagerImpl(private val d2: D2) : AutoEnrollmentManager {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getTrackedEntityDataValues(dataElementsFromSourceProgram: List<SourceProgramStageDataElement>): Flowable<List<TrackedEntityDataValue>> {
        return d2.trackedEntityModule()
            .trackedEntityDataValues()
            .byDataElement()
            .`in`(dataElementsFromSourceProgram.map { it.sourceDataElement })
            .get()
            .toFlowable()
    }

    override fun getCustomConfigurations(): Flowable<AutoEnrollmentConfigurations> {
        val configEntry = d2.dataStoreModule()
            .dataStore()
            .byNamespace().eq("programMapping")
            .byKey().eq("mapping_rules")
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
