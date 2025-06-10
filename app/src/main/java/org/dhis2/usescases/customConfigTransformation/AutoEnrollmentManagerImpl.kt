package org.dhis2.usescases.customConfigTransformation

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.serialization.json.Json
import org.dhis2.usescases.customConfigTransformation.networkModels.JsonWrapper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import kotlin.collections.map
import org.dhis2.usescases.customConfigTransformation.networkModels.SourceProgramStageDataElement
import timber.log.Timber

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

    override fun getTrackedEntityDataValuesByProgramStageAndEnrollment(
        programStageUid: String,
        enrollmentUid: String
    ): List<TrackedEntityDataValue>{

        val events = d2.eventModule().events()
            .byEnrollmentUid().eq(enrollmentUid)
            .byProgramStageUid().`in`(programStageUid)
            .blockingGet()

        return if (events.isNotEmpty()) {
            d2.trackedEntityModule().trackedEntityDataValues()
                .byEvent().`in`(events.map { it.uid() })
                .blockingGet()
        } else {
            listOf()
        }
    }

    override fun getCustomConfigurations(): Single<AutoEnrollmentConfigurations> {
        val configEntry = d2.dataStoreModule()
            .dataStore()
            .byNamespace().eq("programMapping")
            .byKey().eq("mapping_rules")
            .one().blockingGet()

        return if (configEntry != null) {
            d2.dataStoreModule().dataStore().byNamespace().eq("programMapping")
                .byKey().eq("mapping_rules").one().get()
                .map {

                    val formattedJson = it.value()?.split("json=")?.get(1)?.split(")")?.get(0)

                    Gson().fromJson(
                        formattedJson,
                        AutoEnrollmentConfigurations::class.java
                    )
                }
        } else {
            Timber.tag("CHECK_ENROLLMENNT").d("Not found")
            Single.never()
        }
    }
}
