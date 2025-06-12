package org.dhis2.usescases.customConfigTransformation.networkModels
import com.google.gson.*
import java.lang.reflect.Type

class ProgramMappingRuleDeserializer : JsonDeserializer<ProgramMappingRule> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ProgramMappingRule {
        val jsonObject = json.asJsonObject

        return when {
            jsonObject.has("sourceProgram") -> {
                context.deserialize<SourceMappingRule>(
                    json,
                    SourceMappingRule::class.java
                )
            }
            jsonObject.has("targetProgram") -> {
                context.deserialize<TargetMappingRule>(
                    json,
                    TargetProgramStage::class.java
                )
            }
            else -> throw JsonParseException("Unknown mapping rule type - must be either source or target")
        }
    }
}