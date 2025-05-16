package org.dhis2.data.mapping

import com.google.gson.annotations.SerializedName

data class MappingConfig(
    val description: String,
    val id: String,
    val isActive: Boolean,
    val name: String,
    val programMappings: List<ProgramMapping>,
    val rules: Rules
)

data class ProgramMapping(
    val elementMappings: List<ElementMapping>,
    val sourceProgram: String,
    val targetProgram: String
)

data class ElementMapping(
    val conditions: List<Condition>? = null,
    val priority: Int,
    val sourceElement: String,
    val targetElement: String,
    val transformation: String
)

data class Condition(
    val type: String,
    val value: Int
)

data class Rules(
    @SerializedName("allowCrossTypeMapping")
    val allowCrossTypeMapping: Boolean,
    @SerializedName("conflictResolution")
    val conflictResolution: String,
    @SerializedName("valueTypeMatching")
    val valueTypeMatching: String
)

data class MappedElement(
    val targetProgram: String,
    val targetElement: String
)