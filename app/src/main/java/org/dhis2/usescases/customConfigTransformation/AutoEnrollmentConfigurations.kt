package org.dhis2.usescases.customConfigTransformation

data class AutoEnrollmentConfigurations(
    val mappingRules: List<MappingRule>
) {
    companion object {
        fun createDefaultEnrollmentConfigObject() {
    
        }
    }
}