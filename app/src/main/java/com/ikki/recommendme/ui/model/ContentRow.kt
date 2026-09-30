package com.ikki.recommendme.ui.model

import com.ikki.recommendme.domain.model.MediaItem

/** One item inside a horizontal carousel row (with optional explanation). */
data class RowEntry(
    val item: MediaItem,
    val reason: String? = null
)

/** A titled horizontal carousel, e.g. "Recommended for You". */
data class ContentRow(
    val title: String,
    val entries: List<RowEntry>
)
