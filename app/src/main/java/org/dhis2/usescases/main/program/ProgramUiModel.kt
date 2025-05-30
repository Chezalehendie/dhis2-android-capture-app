package org.dhis2.usescases.main.program

import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.ui.MetadataIconData
import org.hisp.dhis.android.core.common.State
import java.util.Date

data class ProgramUiModel(
    val uid: String,
    val title: String,
    val metadataIconData: MetadataIconData,
    val count: Int,
    val type: String?,
    val typeName: String,
    val programType: String,
    val description: String?,
    val onlyEnrollOnce: Boolean,
    val accessDataWrite: Boolean,
    val state: State,
    val hasOverdueEvent: Boolean,
    val filtersAreActive: Boolean,
    val downloadState: ProgramDownloadState,
    val downloadActive: Boolean = false,
    val stockConfig: AppConfig?,
    val lastUpdated: Date,
) {
    fun countDescription() = "%s %s".format(count, typeName)

    fun isDownloading() = downloadActive || downloadState == ProgramDownloadState.DOWNLOADING

    fun getAlphaValue() = if (isDownloading()) {
        0.4f
    } else {
        1f
    }
}

enum class ProgramDownloadState {
    DOWNLOADING, DOWNLOADED, ERROR, NONE
}
