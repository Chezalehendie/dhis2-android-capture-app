package org.dhis2.data.mapping

class MappingServiceImpl(private val mappingConfig: List<MappingConfig>) : MappingService {

    override fun getMappedElement(sourceElement: String, sourceProgram: String, targetProgram: String): MappedElement? {
        val programMapping = mappingConfig
            .flatMap { it.programMappings }
            .find { it.sourceProgram == sourceProgram && it.targetProgram == targetProgram }

        return programMapping?.elementMappings?.find { it.sourceElement == sourceElement } as MappedElement?
    }

    override fun autoPopulate(sourceData: Map<String, Any>, sourceProgram: String, targetProgram: String): Map<String, Any> {
        val targetData = mutableMapOf<String, Any>()

        val programMapping = mappingConfig
            .flatMap { it.programMappings }
            .find { it.sourceProgram == sourceProgram && it.targetProgram == targetProgram }

        programMapping?.elementMappings?.forEach { elementMapping ->
            val sourceValue = sourceData[elementMapping.sourceElement]
            if (sourceValue != null) {
                val transformedValue = applyTransformation(sourceValue, elementMapping.transformation)
                if (shouldOverrideValue(targetData[elementMapping.targetElement], transformedValue)) {
                    targetData[elementMapping.targetElement] = transformedValue
                }
            }
        }

        return targetData
    }

    override fun shouldOverrideValue(existingValue: Any?, newValue: Any?): Boolean {
        // Example logic: Always override if the new value is not null
        return newValue != null
    }

    private fun applyTransformation(value: Any, transformation: String): Any {
        return when (transformation) {
            "DIRECT_COPY" -> value
            else -> value // Add other transformation logic if needed
        }
    }
}