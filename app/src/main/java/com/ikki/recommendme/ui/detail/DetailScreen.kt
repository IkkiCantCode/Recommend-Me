package com.ikki.recommendme.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ikki.recommendme.data.remote.TmdbConfig
import com.ikki.recommendme.domain.model.MediaItem
import com.ikki.recommendme.domain.model.MediaType
import com.ikki.recommendme.domain.model.WatchStatus
import com.ikki.recommendme.ui.components.EmptyState
import com.ikki.recommendme.ui.components.GradientButton
import com.ikki.recommendme.ui.components.MediaPoster
import com.ikki.recommendme.ui.components.PosterCard
import com.ikki.recommendme.ui.components.ScoreBadge
import com.ikki.recommendme.ui.model.RowEntry
import com.ikki.recommendme.ui.navigation.Routes
import com.ikki.recommendme.ui.theme.posterGradientFor
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    id: Int,
    mediaType: MediaType,
    navController: NavController,
    viewModel: DetailViewModel = viewModel(
        factory = DetailViewModel.factory(id, mediaType)
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showStatusSheet by remember { mutableStateOf(false) }
    var showScoreDialog by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(8) }

    val detail = state.detail
    val item = detail?.item

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            /* ---------------- hero ---------------- */
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(330.dp)
            ) {
                val backdropUrl = remember(item?.backdropPath) {
                    TmdbConfig.backdropUrl(item?.backdropPath)
                }
                if (backdropUrl != null) {
                    AsyncImage(
                        model = backdropUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                posterGradientFor(item?.title ?: "placeholder")
                            )
                    )
                }
                // scrim so the text stays readable
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Black.copy(alpha = 0.55f),
                                0.5f to Color.Black.copy(alpha = 0.15f),
                                1f to MaterialTheme.colorScheme.background
                            )
                        )
                )

                // top bar (back + bookmark)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CircleIconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    if (!state.loading) {
                        CircleIconButton(onClick = { viewModel.toggleBookmark() }) {
                            Icon(
                                imageVector = if (state.isBookmarked)
                                    Icons.Filled.Bookmark
                                else Icons.Outlined.BookmarkBorder,
                                contentDescription = if (state.isBookmarked)
                                    "Remove from watchlist" else "Add to watchlist",
                                tint = if (state.isBookmarked)
                                    MaterialTheme.colorScheme.primary else Color.White
                            )
                        }
                    }
                }

                // title block at the bottom of the hero
                if (item != null) {
                    Row(
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        MediaPoster(
                            item = item,
                            modifier = Modifier
                                .width(104.dp)
                                .aspectRatio(2f / 3f),
                            cornerRadius = 12.dp
                        )
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = listOfNotNull(
                                        item.year?.toString(),
                                        if (item.mediaType == MediaType.MOVIE) "Movie" else "TV Show"
                                    ).joinToString(" • "),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Spacer(Modifier.width(12.dp))
                                ScoreBadge(
                                    voteAverage = item.voteAverage,
                                    starColor = com.ikki.recommendme.ui.theme.GoldenStar
                                )
                            }
                        }
                    }
                }
            }

            /* ---------------- body ---------------- */
            if (state.loading || detail == null || item == null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    // genre pills
                    if (item.genres.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item.genres.forEach { genre ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)
                                ) {
                                    Text(
                                        genre,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // watchlist action
                    if (!state.isBookmarked) {
                        GradientButton(
                            text = "Bookmark — Add to Watchlist",
                            onClick = { viewModel.toggleBookmark() }
                        )
                    } else {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showStatusSheet = true }
                            ) {
                                Row(
                                    Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Status: ${state.status?.display ?: WatchStatus.WATCHING.display}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "▾",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (state.userScore != null) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    modifier = Modifier.clickable {
                                        score = state.userScore ?: 8
                                        showScoreDialog = true
                                    }
                                ) {
                                    Text(
                                        "⭐ ${state.userScore}/10",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(
                                            horizontal = 14.dp,
                                            vertical = 14.dp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    detail.tagline?.takeIf { it.isNotBlank() }?.let { tagline ->
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "“$tagline”",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // synopsis
                    if (item.overview.isNotBlank()) {
                        Spacer(Modifier.height(20.dp))
                        SectionTitle("Synopsis")
                        Spacer(Modifier.height(8.dp))
                        Text(
                            item.overview,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // details table
                    Spacer(Modifier.height(20.dp))
                    SectionTitle("Details")
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            detail.item.releaseDate?.let {
                                InfoRow("Released", it)
                            }
                            detail.status?.let { InfoRow("Status", it) }
                            InfoRow("Original language", detail.originalLanguage.uppercase(Locale.US))
                            detail.runtimeMinutes?.let { InfoRow("Runtime", "${it} min") }
                            detail.numberOfSeasons?.let { InfoRow("Seasons", "$it") }
                            detail.numberOfEpisodes?.let { InfoRow("Episodes", "$it") }
                            InfoRow(
                                "TMDB score",
                                String.format(Locale.US, "%.1f / 10 (%d votes)",
                                    item.voteAverage, item.voteCount)
                            )
                            if (item.mediaType == MediaType.TV) {
                                InfoRow("Type", "TV Show")
                            }
                        }
                    }

                    // cast
                    if (detail.cast.isNotEmpty()) {
                        Spacer(Modifier.height(20.dp))
                        SectionTitle("Cast")
                        Spacer(Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            items(detail.cast.size) { index ->
                                val person = detail.cast[index]
                                Column(
                                    Modifier.width(84.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val profileUrl = TmdbConfig.profileUrl(person.profilePath)
                                    Box(
                                        Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(posterGradientFor(person.name)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (profileUrl != null) {
                                            AsyncImage(
                                                model = profileUrl,
                                                contentDescription = person.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Text(
                                                person.name.split(" ")
                                                    .mapNotNull { it.firstOrNull() }
                                                    .take(2)
                                                    .joinToString(""),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        person.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    person.character?.let {
                                        Text(
                                            it,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // similar titles
                    if (detail.similar.isNotEmpty()) {
                        Spacer(Modifier.height(22.dp))
                        SectionTitle("More like this")
                        Spacer(Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(detail.similar.size) { index ->
                                val similar = detail.similar[index]
                                PosterCard(
                                    entry = RowEntry(similar),
                                    onClick = {
                                        navController.navigate(
                                            Routes.detail(similar.id, similar.mediaType)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    if (!state.isBookmarked) {
                        Spacer(Modifier.height(18.dp))
                        Text(
                            "Tip: bookmark titles to build your watchlist →",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(28.dp))
                    Spacer(Modifier.navigationBarsPadding())
                }
            }
        }
    }

    /* ---------------- status bottom sheet ---------------- */
    if (showStatusSheet && detail != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showStatusSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                Modifier
                    .padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
            ) {
                Text(
                    "Move \"${detail.item.title}\" to",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(10.dp))
                WatchStatus.entries.forEach { status ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showStatusSheet = false
                                if (status == WatchStatus.RATED) {
                                    score = state.userScore ?: 8
                                    showScoreDialog = true
                                } else {
                                    viewModel.moveTo(status)
                                }
                            }
                            .padding(vertical = 14.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            when (status) {
                                WatchStatus.WATCHING -> "🍿"
                                WatchStatus.RATED -> "⭐"
                                WatchStatus.DROPPED -> "👋"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            status.display,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (state.status == status) FontWeight.Bold
                            else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.weight(1f))
                        if (state.status == status) {
                            Text(
                                "✓",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    /* ---------------- score dialog ---------------- */
    if (showScoreDialog && detail != null) {
        AlertDialog(
            onDismissRequest = { showScoreDialog = false },
            title = { Text("Rate \"${detail.item.title}\"") },
            text = {
                Column {
                    Text(
                        "Scores 1–10 shape your hybrid recommendations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "$score / 10",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Slider(
                        value = score.toFloat(),
                        onValueChange = { score = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.moveTo(WatchStatus.RATED, score)
                    showScoreDialog = false
                }) { Text("Save", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showScoreDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CircleIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
    ) {
        content()
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(140.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
