package org.dhis2.usescases.finaleproject

data class SourceProgramStage(
    val sourceProgramStage: String,
    val sourceprogramStageDataElements: List<SourceprogramStageDataElement>
)