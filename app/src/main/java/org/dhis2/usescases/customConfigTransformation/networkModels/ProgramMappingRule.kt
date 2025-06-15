package org.dhis2.usescases.customConfigTransformation.networkModels

/**
 * A sealed base class representing a mapping rule within a cross-program
 * configuration. It can either define source-side or target-side program mappings.
 *
 * Used to describe how data should be transferred between DHIS2 programs.
 */

sealed class ProgramMappingRule

/**
 * Defines the source program and its relevant stages from which data will be pulled.
 *
 * @property sourceProgram UID of the source DHIS2 program.
 * @property sourceProgramStages A list of stages in the source program that contain
 * the data elements to be copied.
 */

data class SourceMappingRule(
    val sourceProgram: String,
    val sourceProgramStages: List<SourceProgramStage>
) : ProgramMappingRule()

/**
 * Defines the target program and its relevant stages to which data will be written.
 *
 * @property targetProgram UID of the target DHIS2 program.
 * @property targetProgramStages A list of stages in the target program where
 * mapped data elements will be auto-populated.
 */

data class TargetMappingRule(
    val targetProgram: String,
    val targetProgramStages: List<TargetProgramStage>
) : ProgramMappingRule()