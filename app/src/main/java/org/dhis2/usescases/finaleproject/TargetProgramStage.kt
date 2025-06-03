package org.dhis2.usescases.finaleproject

data class TargetProgramStage(
    val targetProgramStage: String,
    val targetprogramStageDataElements: List<TargetprogramStageDataElement>
)