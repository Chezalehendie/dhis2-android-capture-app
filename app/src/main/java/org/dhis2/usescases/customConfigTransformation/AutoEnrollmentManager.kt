package org.dhis2.usescases.customConfigTransformation

import io.reactivex.Flowable
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue

interface AutoEnrollmentManager {
    fun getCustomConfigurations(): Flowable<AutoEnrollmentConfigurations>

    fun  getTrackedEntityDataValues(dataElementsFromSourceProgram: List<SourceProgramStageDataElement>): Flowable<List<TrackedEntityDataValue>>
}