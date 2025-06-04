package org.dhis2.usescases.customConfigTransformation
import com.google.gson.Gson
import org.hisp.dhis.android.core.D2
import timber.log.Timber

class DatastoreConfigs(private val d2: D2) {

    fun loadDatastoreConfigs(): DatastoreConfigs? {
        return try {
            val datastoreConfigsJson = d2.dataStoreModule().dataStoreDownloader()
                .byNamespace().eq("programMapping")
                .blockingDownload()
                //.firstOrNull()?.value()

            val jsonString = datastoreConfigsJson.toString() // Ensure it's a valid JSON string

            Gson().fromJson(jsonString, DatastoreConfigs::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Error loading datastore configs")
            null
        }
    }
}
