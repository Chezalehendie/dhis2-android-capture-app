package org.dhis2.usescases.customConfigTransformation.networkModels

sealed class ProgramMappingRule

data class SourceMappingRule(
    val sourceProgram: String,
    val sourceProgramStages: List<SourceProgramStage>
) : ProgramMappingRule()

data class TargetMappingRule(
    val targetProgram: String,
    val targetProgramStages: List<TargetProgramStage>
) : ProgramMappingRule()