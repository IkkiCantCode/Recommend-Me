package com.ikki.recommendme.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ikki.recommendme.data.remote.TmdbConfig
import com.ikki.recommendme.domain.model.MediaItem
import com.ikki.recommendme.ui.theme.GoldenStar
import com.ikki.recommendme.ui.theme.posterGradientFor
import java.util.Locale

/**
 * Poster with automatic fallback: real TMDB image when a path exists,
 * branded gradient placeholder (with title) during sample-data mode.
 */
@Composable
fun MediaPoster(
    item: MediaItem,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    showTitle: Boolean = true
) {
    val url = remember(item.posterPath) { TmdbConfig.posterUrl(item.posterPath) }
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier.clip(shape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(posterGradientFor(item.title + item.mediaType.name))
        )
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (showTitle) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = "🎬",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = item.title,
                    color = androidx.compose.ui.graphics.Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

/** Small "TMDB score" pill: ⭐ 8.4 */
@Composable
fun ScoreBadge(
    voteAverage: Double,
    modifier: Modifier = Modifier,
    starColor: androidx.compose.ui.graphics.Color = GoldenStar
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = starColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(3.dp))
        Text(
            text = String.format(Locale.US, "%.1f", voteAverage),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Small translucent pill used as an overlay (e.g. "TV" / "Movie"). */
@Composable
fun TypePill(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.65f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = androidx.compose.ui.graphics.Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** Gradient brush used behind modals/sheets for a bit of extra depth. */
@Composable
fun brandBackground(): Brush = androidx.compose.ui.graphics.Brush.verticalGradient(
    listOf(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.background
    )
)
