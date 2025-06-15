package org.dhis2.usescases.customConfigTransformation.networkModels

/**
 * A simple wrapper class used for sending or receiving JSON-based configurations
 * to and from remote or local data sources (DHIS2 DataStore).
 *
 * This model is typically used when pushing structured configuration data
 * (like cross-program mapping rules) to a key-value storage system.
 *
 * @property json The raw JSON string representing the configuration object.
 * @property key The unique key under which the configuration is stored.
 * @property value An optional or derived value used for indexing, display, or lookup.
 */

data class JsonWrapper(val json: String,
                       val key: String,
                       val value: String)
