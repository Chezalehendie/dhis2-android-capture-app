package org.dhis2.usescases.customConfigTransformation

import io.reactivex.Flowable

interface AutoEnrollmentManager {
    fun getCustomConfigurations(): Flowable<AutoEnrollmentConfigurations>

    fun  getTrackedEntityDataValues(dataElementsFromSourceProgram: SourceprogramStageDataElement): Flowable<List<TrackedEntityDataValue>>
}