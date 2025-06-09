package org.dhis2.usescases.customConfigTransformation

import org.dhis2.usescases.customConfigTransformation.networkModels.SourceProgramStageDataElement

data class SourceProgramStage(
    val sourceProgramStage: String,
    val sourceProgramStageDataElements: List<SourceProgramStageDataElement>
)
//{
//    fun stageUid(): String {
//        return sourceProgramStage;
//    }
//
//    fun stageDataElements(): List<SourceProgramStageDataElement>{
//        return sourceProgramStageDataElements;
//    }
//}