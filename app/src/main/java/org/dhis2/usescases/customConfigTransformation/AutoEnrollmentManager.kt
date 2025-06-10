package org.dhis2.usescases.customConfigTransformation

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.dhis2.usescases.customConfigTransformation.networkModels.SourceProgramStageDataElement

interface AutoEnrollmentManager {
    fun getCustomConfigurations(): Single<AutoEnrollmentConfigurations>

    fun  getTrackedEntityDataValues(dataElementsFromSourceProgram: List<SourceProgramStageDataElement>): Flowable<List<TrackedEntityDataValue>>

    fun getTrackedEntityDataValuesByProgramStageAndEnrollment(
        programStageUid: String,
        enrollmentUid: String
    ): List<TrackedEntityDataValue>
}