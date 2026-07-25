package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoPair
import com.example.data.model.MarketTab
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MarketViewModel

@Composable
fun QuotesScreen(
    viewModel: MarketViewModel,
    onNavigateToChart: (String) -> Unit,
    onNavigateToTrade: (String) -> Unit
) {
    val pairs by viewModel.filteredPairs.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val tabs = MarketTab.values()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkObsidian)
            .testTag("quotes_screen")
    ) {
        // Search Bar
        Box(modifier = Modifier.padding(12.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search BTC, ETH, SOL...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = SlateBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("quotes_search_input")
            )
        }

        // Tab Filters
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = DarkSurface,
            contentColor = TextPrimary,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                if (selectedTab.ordinal < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                        color = NeonGreen
                    )
                }
            }
        ) {
            tabs.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { viewModel.setTab(tab) },
                    text = {
                        Text(
                            text = tab.name,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == tab) NeonGreen else TextSecondary
                            )
                        )
                    }
                )
            }
        }

        // Quotes Watchlist Table
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Name / Vol", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1.3f))
            Text("Bid / Ask", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1.2f))
            Text("24h Change", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            items(pairs, key = { it.id }) { pair ->
                CryptoPairWatchlistItem(
                    pair = pair,
                    onFavoriteToggle = { viewModel.toggleFavorite(pair.id) },
                    onItemClick = {
                        viewModel.selectPair(pair.id)
                        onNavigateToChart(pair.id)
                    },
                    onQuickTradeClick = {
                        viewModel.selectPair(pair.id)
                        onNavigateToTrade(pair.id)
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun CryptoPairWatchlistItem(
    pair: CryptoPair,
    onFavoriteToggle: () -> Unit,
    onItemClick: () -> Unit,
    onQuickTradeClick: () -> Unit
) {
    val priceColor = if (pair.isBullish) NeonGreen else CrimsonRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .testTag("watchlist_item_${pair.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, SlateBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Symbol info & favorite star
            Row(
                modifier = Modifier.weight(1.3f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (pair.isFavorite) Icons.Default.Star else Icons.Outlined.Star,
                        contentDescription = "Favorite",
                        tint = if (pair.isFavorite) NeonGreen else TextMuted
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = pair.symbol,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Text(
                            text = "/${pair.quoteAsset}",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                    }
                    Text(
                        text = "Vol $${String.format("%.1fM", pair.volume24h / 1_000_000.0)}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }
            }

            // Bid / Ask & Spread
            Column(
                modifier = Modifier.weight(1.2f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = pair.formattedPrice,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = priceColor
                    )
                )
                Text(
                    text = "B:${pair.bid} / A:${pair.ask}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 24h Change Pill Badge & Quick Trade Action
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (pair.isBullish) NeonGreen.copy(alpha = 0.2f) else CrimsonRed.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${if (pair.isBullish) "+" else ""}${pair.priceChange24h}%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = priceColor,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}
