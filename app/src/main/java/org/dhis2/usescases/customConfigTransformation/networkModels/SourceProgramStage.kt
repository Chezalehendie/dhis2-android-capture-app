package org.dhis2.usescases.customConfigTransformation.networkModels

data class SourceProgramStage(
    val sourceProgramStage: String,
    val sourceProgramStageDataElements: List<SourceProgramStageDataElement>
)
