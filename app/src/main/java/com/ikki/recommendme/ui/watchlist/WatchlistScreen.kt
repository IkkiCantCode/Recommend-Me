package com.ikki.recommendme.ui.watchlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.ikki.recommendme.domain.model.WatchStatus
import com.ikki.recommendme.domain.model.WatchlistEntry
import com.ikki.recommendme.ui.components.EmptyState
import com.ikki.recommendme.ui.components.WatchlistItemCard
import com.ikki.recommendme.ui.navigation.Routes

@Composable
fun WatchlistScreen(
    navController: NavController,
    viewModel: WatchlistViewModel = viewModel(factory = WatchlistViewModel.Factory)
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    // Rating dialog state: which entry + are we also moving it into "Rated"
    var rateTarget by remember { mutableStateOf<WatchlistEntry?>(null) }
    var moveToRated by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(8) }

    val currentEntries = viewModel.entriesFor(selectedTab, entries)

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
    ) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(14.dp))
            Text(
                "Watchlist",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "${entries.size} titles saved",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))

            /* ---------- 3 tabs ---------- */
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WatchlistViewModel.TABS.forEachIndexed { index, title ->
                    val count = viewModel.countFor(index, entries)
                    val isSelected = selectedTab == index
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        else MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setTab(index) }
                    ) {
                        Column(
                            Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                count.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        /* ---------- list / empty state ---------- */
        if (currentEntries.isEmpty()) {
            when (selectedTab) {
                0 -> EmptyState(
                    emoji = "🍿",
                    title = "Nothing in progress",
                    subtitle = "Open any title and tap the bookmark to start your list."
                )
                1 -> EmptyState(
                    emoji = "⭐",
                    title = "No rated titles yet",
                    subtitle = "Score titles 1–10 and they'll show up here."
                )
                else -> EmptyState(
                    emoji = "👋",
                    title = "Nothing dropped",
                    subtitle = "Titles you abandon along the way will appear here."
                )
            }
        } else {
            Column(
                Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                currentEntries.forEach { entry ->
                    WatchlistItemCard(
                        entry = entry,
                        onClick = {
                            navController.navigate(Routes.detail(entry.id, entry.mediaType))
                        },
                        onMove = { status ->
                            if (status == WatchStatus.RATED) {
                                rateTarget = entry
                                moveToRated = true
                                score = entry.userScore ?: 8
                            } else {
                                viewModel.move(entry, status)
                            }
                        },
                        onRateClick = {
                            rateTarget = entry
                            moveToRated = false
                            score = entry.userScore ?: 8
                        },
                        onRemove = { viewModel.remove(entry) }
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    /* ---------- rating dialog (1..10, MyAnimeList style) ---------- */
    rateTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { rateTarget = null },
            title = {
                Text(
                    if (moveToRated) "Rate \"${target.title}\""
                    else "Update score"
                )
            },
            text = {
                Column {
                    Text(
                        "How good was it? Scores feed your recommendations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
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
                    if (moveToRated) {
                        viewModel.move(target, WatchStatus.RATED, score)
                    } else {
                        viewModel.setScore(target, score)
                    }
                    rateTarget = null
                }) { Text("Save", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { rateTarget = null }) { Text("Cancel") }
            }
        )
    }
}
