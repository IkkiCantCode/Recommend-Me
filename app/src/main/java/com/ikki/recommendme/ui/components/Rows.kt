package com.ikki.recommendme.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.ikki.recommendme.domain.model.MediaItem
import com.ikki.recommendme.ui.model.ContentRow

/** Horizontal carousel with a section header (Home + Search pages). */
@Composable
fun CarouselRow(
    row: ContentRow,
    onItemClicked: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        if (row.entries.isNotEmpty()) {
            SectionHeader(title = row.title)
            Spacer(Modifier.height(10.dp))
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(row.entries, key = { it.item.key }) { entry ->
                    PosterCard(entry = entry, onClick = { onItemClicked(entry.item) })
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

/** Responsive 2-4 column poster grid (search results / filtered results). */
@Composable
fun PosterGrid(
    items: List<MediaItem>,
    onItemClicked: (MediaItem) -> Unit,
    modifier: Modifier = Modifier,
    reasonFor: (MediaItem) -> String? = { null }
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val columns = (screenWidth / 135).coerceIn(2, 4)
    Column(modifier.fillMaxWidth()) {
        items.chunked(columns).forEach { chunk ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                chunk.forEach { item ->
                    PosterCard(
                        entry = com.ikki.recommendme.ui.model.RowEntry(item, reasonFor(item)),
                        onClick = { onItemClicked(item) },
                        width = null,
                        modifier = Modifier.weight(1f)
                    )
                }
                // fill the empty cells so cards stay aligned
                repeat(columns - chunk.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
