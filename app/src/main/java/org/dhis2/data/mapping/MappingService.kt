package org.dhis2.data.mapping

interface MappingService {
    fun getMappedElement(sourceElement: String, sourceProgram: String, targetProgram: String): MappedElement?
    fun autoPopulate(sourceData: Map<String, Any>, sourceProgram: String, targetProgram: String): Map<String, Any>
    fun shouldOverrideValue(existingValue: Any?, newValue: Any?): Boolean
}