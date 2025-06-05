package org.dhis2.usescases.customConfigTransformation

data class SourceProgramStage(
    val sourceProgramStage: String,
    val sourceprogramStageDataElements: List<SourceprogramStageDataElement>
)