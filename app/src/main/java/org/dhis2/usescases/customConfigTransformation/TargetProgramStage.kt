package org.dhis2.usescases.customConfigTransformation

data class TargetProgramStage(
    val targetProgramStage: String,
    val targetprogramStageDataElements: List<TargetprogramStageDataElement>
)