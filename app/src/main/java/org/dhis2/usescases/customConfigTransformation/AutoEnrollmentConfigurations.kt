package org.dhis2.usescases.customConfigTransformation

import org.dhis2.usescases.customConfigTransformation.networkModels.MappingRule
import kotlin.collections.List

data class AutoEnrollmentConfigurations(
    val mappingRules: List<MappingRule>
) {
    companion object {
        fun createDefaultEnrollmentConfigObject() {


        }
    }
}