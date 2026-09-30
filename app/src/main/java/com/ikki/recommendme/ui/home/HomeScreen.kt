package com.ikki.recommendme.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ikki.recommendme.domain.model.MediaItem
import com.ikki.recommendme.ui.components.AppSearchBar
import com.ikki.recommendme.ui.components.CarouselRow
import com.ikki.recommendme.ui.components.EmptyState
import com.ikki.recommendme.ui.components.PosterGrid
import com.ikki.recommendme.ui.components.SortFilterSheet
import com.ikki.recommendme.ui.navigation.Routes

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var sheetVisible by remember { mutableStateOf(false) }

    fun openDetail(item: MediaItem) {
        navController.navigate(Routes.detail(item.id, item.mediaType))
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
    ) {
        /* ---------- header + search ---------- */
        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Hi, ${state.profile?.name?.substringBefore(' ') ?: "there"} 👋",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Recommend Me",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                AppSearchBar(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    onSearch = { /* results update automatically */ },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(10.dp))
                IconButton(
                    onClick = { sheetVisible = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Icon(
                        Icons.Filled.Tune,
                        contentDescription = "Sort & filter",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (state.usingSampleData && !state.loading) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "⚡ Sample data — paste your TMDB API key for live content",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        when {
            state.loading -> Box(
                Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            state.isSearching -> SearchResultsContent(
                state = state,
                onOpenDetail = ::openDetail
            )

            else -> Column {
                state.rows.forEach { row ->
                    CarouselRow(row = row, onItemClicked = ::openDetail)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    SortFilterSheet(
        visible = sheetVisible,
        currentSort = state.sort,
        currentGenre = state.genreFilter,
        genres = state.availableGenres,
        onSort = viewModel::setSort,
        onGenre = viewModel::setGenreFilter,
        onDismiss = { sheetVisible = false }
    )
}

@Composable
private fun SearchResultsContent(
    state: HomeViewModel.HomeUiState,
    onOpenDetail: (MediaItem) -> Unit
) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        if (state.searching) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${state.displayedResults.size} result" +
                    if (state.displayedResults.size == 1) "" else "s",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = listOfNotNull(
                    state.sort.display,
                    state.genreFilter
                ).joinToString(" • "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(12.dp))

        if (state.displayedResults.isEmpty()) {
            EmptyState(
                emoji = "🔍",
                title = "No matches found",
                subtitle = "Try a different title or clear the genre filter."
            )
        } else {
            PosterGrid(
                items = state.displayedResults,
                onItemClicked = onOpenDetail
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}
