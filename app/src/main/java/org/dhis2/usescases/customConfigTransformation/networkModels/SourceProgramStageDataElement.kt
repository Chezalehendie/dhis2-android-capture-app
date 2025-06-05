package org.dhis2.usescases.customConfigTransformation

data class SourceProgramStageDataElement(
    val sourceDataElement: String
) {
    fun dataElementUid(): String {
        return sourceDataElement
    }
}

