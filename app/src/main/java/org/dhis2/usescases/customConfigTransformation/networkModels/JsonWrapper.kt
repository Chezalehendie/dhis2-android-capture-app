package org.dhis2.usescases.customConfigTransformation.networkModels

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class JsonWrapper(val json: String,
                       val key: String,
                       val value: String)

fun deserializeJsonWrapper(jsonString: String): JsonWrapper {
    return Json { ignoreUnknownKeys = true }.decodeFromString(jsonString)
}