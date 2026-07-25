package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoPair
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TopTickerBar(
    selectedPair: CryptoPair?,
    allPairs: List<CryptoPair>,
    onPairSelected: (String) -> Unit,
    wsConnected: Boolean,
    latencyMs: Int,
    onAlertClick: () -> Unit = {}
) {
    var expandedPairDropdown by remember { mutableStateOf(false) }
    var isAlertActive by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("top_ticker_bar"),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pair Selector
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceElevated)
                            .clickable { expandedPairDropdown = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("pair_selector_dropdown"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedPair?.id ?: "BTC/USDT",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Pair",
                            tint = TextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = expandedPairDropdown,
                        onDismissRequest = { expandedPairDropdown = false },
                        modifier = Modifier.background(DarkSurfaceElevated)
                    ) {
                        allPairs.forEach { pair ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = pair.id,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(
                                            text = pair.formattedPrice,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = if (pair.isBullish) NeonGreen else CrimsonRed,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        )
                                    }
                                },
                                onClick = {
                                    onPairSelected(pair.id)
                                    expandedPairDropdown = false
                                }
                            )
                        }
                    }
                }

                // Price and 24h change
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val priceColor = if (selectedPair?.isBullish == true) NeonGreen else CrimsonRed
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = selectedPair?.formattedPrice ?: "0.00",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = priceColor,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${if (selectedPair?.isBullish == true) "+" else ""}${selectedPair?.priceChange24h ?: 0.0}%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = priceColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Alert Bell
                    IconButton(
                        onClick = {
                            isAlertActive = !isAlertActive
                            onAlertClick()
                        },
                        modifier = Modifier.size(36.dp).testTag("alert_bell_button")
                    ) {
                        Icon(
                            imageVector = if (isAlertActive) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = "Price Alert",
                            tint = if (isAlertActive) NeonGreen else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Ticker 24h details row + WS Connection indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text("24h High", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = String.format("%.2f", selectedPair?.high24h ?: 0.0),
                            style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary)
                        )
                    }
                    Column {
                        Text("24h Low", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = String.format("%.2f", selectedPair?.low24h ?: 0.0),
                            style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary)
                        )
                    }
                    Column {
                        Text("24h Vol", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "${((selectedPair?.volume24h ?: 0.0) / 1_000_000.0).let { String.format("%.1fM", it) }}",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary)
                        )
                    }
                }

                // Connection badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkObsidian)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (wsConnected) NeonGreen else CrimsonRed)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (wsConnected) "${latencyMs}ms" else "DISCONNECTED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (wsConnected) NeonGreen else CrimsonRed,
                            fontSize = 9.sp
                        )
                    )
                }
            }
        }
    }
}
