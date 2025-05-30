package org.dhis2.usescases.searchTrackEntity.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import org.dhis2.R
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.databinding.ItemSearchErrorBinding
import org.dhis2.databinding.ItemSearchTrackedEntityBinding
import org.dhis2.usescases.searchTrackEntity.SearchTeiModel
import org.dhis2.usescases.searchTrackEntity.ui.mapper.TEICardMapper
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing

class SearchTeiLiveAdapter(
    private val fromRelationship: Boolean,
    private val colorUtils: ColorUtils,
    private val cardMapper: TEICardMapper,
    private val onAddRelationship: (
        teiUid: String,
        relationshipTypeUid: String?,
        isOnline: Boolean,
    ) -> Unit,
    private val onSyncIconClick: (teiUid: String) -> Unit,
    private val onDownloadTei: (teiUid: String, enrollmentUid: String?) -> Unit,
    private val onTeiClick: (teiUid: String, enrollmentUid: String?, isOnline: Boolean) -> Unit,
    private val onImageClick: (imagePath: String) -> Unit,
) : PagingDataAdapter<SearchTeiModel, RecyclerView.ViewHolder>(SearchAdapterDiffCallback()) {

    private enum class SearchItem {
        TEI,
        RELATIONSHIP_TEI,
        ONLINE_ERROR,
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (SearchItem.entries[viewType]) {
            SearchItem.TEI -> SearchTEViewHolder(
                ItemSearchTrackedEntityBinding.inflate(inflater, parent, false),
                onSyncIconClick,
                onDownloadTei,
                colorUtils,
                onTeiClick,
            )

            SearchItem.RELATIONSHIP_TEI -> SearchRelationshipViewHolder(
                ItemSearchTrackedEntityBinding.inflate(inflater, parent, false),
                colorUtils,
                onAddRelationship,
            )

            SearchItem.ONLINE_ERROR -> SearchErrorViewHolder(
                ItemSearchErrorBinding.inflate(inflater, parent, false),
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        var onlineErrorMessage: String? = null
        if (position < snapshot().size) {
            onlineErrorMessage = snapshot()[position]?.onlineErrorMessage
        }
        return when {
            onlineErrorMessage != null -> SearchItem.ONLINE_ERROR.ordinal
            fromRelationship -> SearchItem.RELATIONSHIP_TEI.ordinal
            else -> SearchItem.TEI.ordinal
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SearchTEViewHolder -> {
                getItem(position)?.let {
                    val materialCardView =
                        holder.itemView.findViewById<MaterialCardView>(R.id.cardView)
                    materialCardView.visibility = View.GONE
                    val composeView = holder.itemView.findViewById<ComposeView>(R.id.composeView)
                    composeView.setContent {
                        val card = cardMapper.map(
                            searchTEIModel = it,
                            onSyncIconClick = {
                                onSyncIconClick.invoke(it.selectedEnrollment.uid())
                            },
                            onCardClick = {
                                if (it.isOnline) {
                                    onDownloadTei.invoke(
                                        it.tei.uid(),
                                        it.selectedEnrollment?.uid(),
                                    )
                                } else {
                                    onTeiClick.invoke(
                                        it.tei.uid(),
                                        it.selectedEnrollment?.uid(),
                                        it.isOnline,
                                    )
                                }
                            },
                            onImageClick = { path ->
                                onImageClick(path)
                            },
                        )
                        Column(
                            modifier = Modifier
                                .padding(
                                    start = Spacing.Spacing8,
                                    end = Spacing.Spacing8,
                                    bottom = Spacing.Spacing4,
                                ),
                        ) {
                            if (position == 0) {
                                Spacer(modifier = Modifier.size(Spacing.Spacing8))
                            }
                            ListCard(
                                listAvatar = card.avatar,
                                title = ListCardTitleModel(text = card.title),
                                lastUpdated = card.lastUpdated,
                                additionalInfoList = card.additionalInfo,
                                actionButton = card.actionButton,
                                expandLabelText = card.expandLabelText,
                                shrinkLabelText = card.shrinkLabelText,
                                onCardClick = card.onCardCLick,
                            )
                        }
                    }
                }
            }

            is SearchRelationshipViewHolder ->
                holder.bind(
                    getItem(position)!!,
                    {
                        getItem(holder.absoluteAdapterPosition)?.toggleAttributeList()
                        notifyItemChanged(holder.absoluteAdapterPosition)
                    },
                ) { path: String? ->
                    path?.let { onImageClick(path) }
                }

            is SearchErrorViewHolder -> holder.bind(getItem(position)!!)
        }
    }
}
