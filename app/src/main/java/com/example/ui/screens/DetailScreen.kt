package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.WatchlistItem
import com.example.data.preferences.KnownProviders
import com.example.ui.components.TvButton
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GreenWatched
import com.example.ui.theme.RedDropped
import com.example.ui.theme.TvCardBg
import com.example.ui.theme.TvSurface
import org.json.JSONArray

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    item: WatchlistItem,
    isInWatchlist: Boolean,
    subscribedProviders: Set<String>,
    onBack: () -> Unit,
    onAddToList: (WatchlistItem) -> Unit,
    onPlayOnProvider: (WatchlistItem) -> Unit,
    onOpenTrailer: (String) -> Unit,
    onAdvanceEpisode: (WatchlistItem) -> Unit,
    onOpenStatusDialog: (WatchlistItem) -> Unit,
    onOpenDeleteDialog: (WatchlistItem) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val scrollState = rememberScrollState()

    // Parse providers from JSON
    val providerList = parseProvidersJson(item.providersJson)
    val inUserSubs = providerList.filter { it.type == "sub" || KnownProviders.matchesAny(it.name, subscribedProviders) }
    val otherFlats = providerList.filter { it.type == "flatrate" && !KnownProviders.matchesAny(it.name, subscribedProviders) }
    val buyRent = providerList.filter { it.type == "buy_rent" }

    Box(modifier = modifier.fillMaxSize()) {
        // High-resolution cinematic backdrop image as background
        if (!item.backdropUrl.isNullOrBlank()) {
            AsyncImage(
                model = item.backdropUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Multi-stop cinematic dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xBB090D16),
                            Color(0xEA090D16),
                            Color(0xFF090D16)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFA090D16),
                            Color(0xD9090D16),
                            Color(0x99090D16)
                        )
                    )
                )
        )

        // Main Content Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 20.dp)
        ) {
            // Top Nav: Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TvButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("detail_back_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Zurück",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Zurück", fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body: Left Poster + Right Info Column
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                // Left: Large Movie/Series Poster
                Column(
                    modifier = Modifier.width(220.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, CyanPrimary, RoundedCornerShape(16.dp))
                            .testTag("detail_poster"),
                        colors = CardDefaults.cardColors(containerColor = TvCardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (!item.posterUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = item.posterUrl,
                                contentDescription = item.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(TvCardBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (item.isMovie) Icons.Default.Movie else Icons.Default.Tv,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Badge under Poster
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = when (item.status) {
                            WatchlistItem.STATUS_WATCHING -> AmberAccent
                            WatchlistItem.STATUS_WATCHED -> GreenWatched
                            WatchlistItem.STATUS_DROPPED -> RedDropped
                            else -> TvSurface
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text(
                            text = when (item.status) {
                                WatchlistItem.STATUS_WATCHING -> "▶ Gerade am ansehen"
                                WatchlistItem.STATUS_WATCHED -> "✓ Gesehen"
                                WatchlistItem.STATUS_DROPPED -> "✗ Abgebrochen"
                                else -> "Noch nicht gesehen"
                            },
                            color = if (item.status == WatchlistItem.STATUS_WATCHING) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }

                // Right: Metadata, Streaming, Actions
                Column(modifier = Modifier.weight(1f)) {
                    // Title
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 34.sp
                    )

                    // Original title (if different)
                    if (!item.originalTitle.isNullOrBlank() && !item.originalTitle.equals(item.title, ignoreCase = true)) {
                        Text(
                            text = "Original: ${item.originalTitle}",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Metadata row: Year, Runtime/Seasons, Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (!item.releaseYear.isNullOrBlank()) {
                            Surface(
                                color = TvSurface,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = item.releaseYear,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Runtime or Seasons count
                        if (item.isMovie && item.runtime != null && item.runtime > 0) {
                            val hours = item.runtime / 60
                            val mins = item.runtime % 60
                            val runtimeText = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
                            Text(
                                text = runtimeText,
                                color = Color(0xFFCBD5E1),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else if (item.isTv) {
                            val seasons = item.numberOfSeasons ?: 1
                            val eps = item.numberOfEpisodes
                            val epText = if (eps != null) " ($eps Folgen)" else ""
                            Text(
                                text = "$seasons ${if (seasons == 1) "Staffel" else "Staffeln"}$epText",
                                color = Color(0xFFCBD5E1),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // TMDb Rating
                        if (item.voteAverage > 0.0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AmberGlow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.1f", item.voteAverage),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "/10",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Genres Chips
                    if (item.genres.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item.genres.split(",").forEach { genre ->
                                Surface(
                                    color = Color(0x3306B6D4),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6606B6D4))
                                ) {
                                    Text(
                                        text = genre.trim(),
                                        color = CyanNeon,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Overview
                    if (!item.overview.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = item.overview,
                            color = Color(0xFFE2E8F0),
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // STREAMING PROVIDERS SECTION
                    StreamingProvidersSection(
                        inUserSubs = inUserSubs,
                        otherFlats = otherFlats,
                        buyRent = buyRent,
                        primaryName = item.primaryProviderName,
                        primaryLogo = item.primaryProviderLogo
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // ACTION BUTTONS (D-Pad navigable)
                    ActionButtonsSection(
                        item = item,
                        isInWatchlist = isInWatchlist,
                        onAddToList = onAddToList,
                        onPlayOnProvider = onPlayOnProvider,
                        onOpenTrailer = onOpenTrailer,
                        onAdvanceEpisode = onAdvanceEpisode,
                        onOpenStatusDialog = onOpenStatusDialog,
                        onOpenDeleteDialog = onOpenDeleteDialog
                    )

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

data class ParsedProvider(val name: String, val logo: String?, val type: String)

fun parseProvidersJson(json: String?): List<ParsedProvider> {
    if (json.isNullOrBlank()) return emptyList()
    val list = mutableListOf<ParsedProvider>()
    try {
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                ParsedProvider(
                    name = obj.optString("name"),
                    logo = obj.optString("logo").takeIf { it.isNotBlank() },
                    type = obj.optString("type")
                )
            )
        }
    } catch (e: Exception) {
        // Fallback
    }
    return list
}

@Composable
fun StreamingProvidersSection(
    inUserSubs: List<ParsedProvider>,
    otherFlats: List<ParsedProvider>,
    buyRent: List<ParsedProvider>,
    primaryName: String?,
    primaryLogo: String?
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = TvSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Streaming-Verfügbarkeit",
                color = CyanNeon,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 1. In deinen Abos enthalten
            if (inUserSubs.isNotEmpty() || (!primaryName.isNullOrBlank() && inUserSubs.isEmpty() && otherFlats.isEmpty())) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Surface(
                        color = Color(0x3310B981),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GreenWatched)
                    ) {
                        Text(
                            text = "In deinen Abos ✓",
                            color = GreenWatched,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    val displayProviders = if (inUserSubs.isNotEmpty()) inUserSubs else listOf(
                        ParsedProvider(name = primaryName ?: "", logo = primaryLogo, type = "sub")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        displayProviders.forEach { prov ->
                            ProviderChip(name = prov.name, logo = prov.logo, isHighlight = true)
                        }
                    }
                }
            }

            // 2. Andere Flatrates
            if (otherFlats.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Andere Flatrates:",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(110.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        otherFlats.take(4).forEach { prov ->
                            ProviderChip(name = prov.name, logo = prov.logo, isHighlight = false)
                        }
                    }
                }
            }

            // 3. Kaufen & Leihen
            if (buyRent.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Kaufen / Leihen:",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.width(110.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        buyRent.take(4).forEach { prov ->
                            ProviderChip(name = prov.name, logo = prov.logo, isHighlight = false)
                        }
                    }
                }
            }

            if (inUserSubs.isEmpty() && otherFlats.isEmpty() && buyRent.isEmpty() && primaryName.isNullOrBlank()) {
                Text(
                    text = "Keine Flatrate-Streaming-Informationen für diese Region hinterlegt.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun ProviderChip(name: String, logo: String?, isHighlight: Boolean) {
    Surface(
        color = if (isHighlight) Color(0xFF1E293B) else Color(0xFF0F172A),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isHighlight) CyanPrimary else Color(0xFF334155)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!logo.isNullOrBlank()) {
                val fullUrl = if (logo.startsWith("http")) logo else "https://image.tmdb.org/t/p/w92$logo"
                AsyncImage(
                    model = fullUrl,
                    contentDescription = name,
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = name,
                color = if (isHighlight) CyanNeon else Color.White,
                fontSize = 12.sp,
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActionButtonsSection(
    item: WatchlistItem,
    isInWatchlist: Boolean,
    onAddToList: (WatchlistItem) -> Unit,
    onPlayOnProvider: (WatchlistItem) -> Unit,
    onOpenTrailer: (String) -> Unit,
    onAdvanceEpisode: (WatchlistItem) -> Unit,
    onOpenStatusDialog: (WatchlistItem) -> Unit,
    onOpenDeleteDialog: (WatchlistItem) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (!isInWatchlist) {
            // [ ➕ Zur Liste hinzufügen ]
            TvButton(
                onClick = { onAddToList(item) },
                isPrimary = true,
                modifier = Modifier.testTag("action_add_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("➕ Zur Liste hinzufügen", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        } else {
            // [ ▶️ Jetzt auf (Anbieter) schauen ]
            val providerName = item.primaryProviderName ?: "Streaming"
            TvButton(
                onClick = { onPlayOnProvider(item) },
                isPrimary = true,
                modifier = Modifier.testTag("action_play_provider_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("▶️ Auf $providerName schauen", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // [ 🎞️ Trailer ansehen ]
            if (!item.trailerYoutubeKey.isNullOrBlank()) {
                TvButton(
                    onClick = { onOpenTrailer(item.trailerYoutubeKey) },
                    modifier = Modifier.testTag("action_trailer_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = AmberAccent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("🎞️ Trailer ansehen", color = AmberAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            // Only TV Series: [ +1 Folge gesehen (Aktuell: S01 E04) ]
            if (item.isTv) {
                val epText = "S${item.currentSeason.toString().padStart(2, '0')} E${item.currentEpisode.toString().padStart(2, '0')}"
                TvButton(
                    onClick = { onAdvanceEpisode(item) },
                    modifier = Modifier.testTag("action_advance_episode_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = CyanNeon)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+1 Folge (Aktuell: $epText)",
                            color = CyanNeon,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // [ 🔄 Status ändern ]
            TvButton(
                onClick = { onOpenStatusDialog(item) },
                modifier = Modifier.testTag("action_change_status_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("🔄 Status ändern", fontSize = 14.sp)
                }
            }

            // [ 🗑️ Löschen ]
            TvButton(
                onClick = { onOpenDeleteDialog(item) },
                modifier = Modifier.testTag("action_delete_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = RedDropped)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("🗑️ Löschen", color = RedDropped, fontSize = 14.sp)
                }
            }
        }
    }
}
