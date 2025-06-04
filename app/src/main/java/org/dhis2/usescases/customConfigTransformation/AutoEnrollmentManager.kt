package org.dhis2.usescases.customConfigTransformation

import io.reactivex.Flowable

interface AutoEnrollmentManager {
    fun getCustomConfigurations(): Flowable<AutoEnrollmentConfigurations>

    fun <trackedEntityDataValues> getTrackedEntityDataValues(dataElement: SourceprogramStageDataElement): Flowable<List<trackedEntityDataValues>>
}