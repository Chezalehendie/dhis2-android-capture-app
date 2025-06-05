package org.dhis2.usescases.customConfigTransformation

data class SourceProgramStage(
    val sourceProgramStage: String,
    val sourceProgramStageDataElements: List<SourceProgramStageDataElement>
){
    fun stageUid(): String {
        return sourceProgramStage;
    }

    fun stageDataElements(): List<SourceProgramStageDataElement>{
        return sourceProgramStageDataElements;
    }
}