package org.dhis2.usescases.customConfigTransformation.networkModels

data class TargetProgramStage(
    val targetProgramStage: String,
    val targetProgramStageDataElements: List<TargetProgramStageDataElement>
)