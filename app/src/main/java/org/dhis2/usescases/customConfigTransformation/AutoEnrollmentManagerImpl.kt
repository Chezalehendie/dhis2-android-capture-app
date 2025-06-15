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


/**
 * Implementation of the [AutoEnrollmentManager] responsible for handling
 * automatic population of data elements between DHIS2 programs based on
 * predefined configuration mappings.
 *
 * This class fetches TEI data values from a source program and prepares them
 * for use in a target program during event or enrollment creation.
 *
 * @property d2 DHIS2 SDK instance used for accessing TEIs and data values.
 */


class AutoEnrollmentManagerImpl(private val d2: D2) : AutoEnrollmentManager {

    /**
     * Retrieves tracked entity data values for a given TEI in a specified program and stage.
     *
     * The data returned can be used to auto-populate fields in another program,
     * based on configuration mappings stored in DHIS2.
     *
     * @param teiUid The UID of the tracked entity instance (TEI).
     * @param programUid The UID of the program from which data should be fetched.
     * @param stageUid The UID of the program stage to filter data values.
     * @return A [Flowable] list of [TrackedEntityDataValue] items matching the query.
     */

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
        //Timber.tag("CHECK_ENROLLMENT").d("Events: $events")

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
                    Timber.tag("CHECK_ENROLLMENT").d(it.value(), "The wrapped String from Store")

                    val formattedJson = it.value()?.split("json=")?.get(1)?.split(")")?.get(0)

                    Timber.tag("CHECK_ENROLLMENT").d("Deserialized String: $formattedJson")

                    Gson().fromJson(
                        formattedJson,
                        AutoEnrollmentConfigurations::class.java
                    )
                }
        } else {
            Timber.tag("CHECK_ENROLLMENT").d("Not found")
            Single.never()
        }
    }
}
