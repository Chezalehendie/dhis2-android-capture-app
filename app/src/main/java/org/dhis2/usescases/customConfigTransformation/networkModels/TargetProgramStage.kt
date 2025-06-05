package org.dhis2.usescases.customConfigTransformation

data class TargetProgramStage(
    val targetProgramStage: String,
    val targetprogramStageDataElements: List<TargetProgramStageDataElement>
){
    fun stageUid(): String {
        return targetProgramStage;
    }

    fun stageDataElements(): List<TargetProgramStageDataElement>{
        return targetprogramStageDataElements;
    }
}