package org.dhis2.usescases.customConfigTransformation.networkModels

data class MappingRule(
    val sourceProgram: String,
    val sourceProgramStages: List<SourceProgramStage>,
    val targetProgram: String,
    val targetProgramStages: List<TargetProgramStage>
)