package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.OrderBookEntry
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class OrderBookDisplayMode {
    BOTH, BIDS_ONLY, ASKS_ONLY
}

@Composable
fun OrderBookView(
    bids: List<OrderBookEntry>,
    asks: List<OrderBookEntry>,
    lastPrice: Double,
    isBullish: Boolean,
    modifier: Modifier = Modifier,
    onPriceSelect: ((Double) -> Unit)? = null
) {
    var displayMode by remember { mutableIntStateOf(0) } // 0: Both, 1: Bids, 2: Asks

    val totalBidVol = remember(bids) { bids.sumOf { it.amount } }
    val totalAskVol = remember(asks) { asks.sumOf { it.amount } }
    val totalVol = totalBidVol + totalAskVol
    val bidRatio = if (totalVol > 0) (totalBidVol / totalVol).toFloat() else 0.5f
    val askRatio = 1f - bidRatio

    val avgAskVol = remember(asks) { if (asks.isNotEmpty()) asks.map { it.amount }.average() else 1.0 }
    val avgBidVol = remember(bids) { if (bids.isNotEmpty()) bids.map { it.amount }.average() else 1.0 }

    val lowestAsk = asks.firstOrNull()?.price ?: 0.0
    val highestBid = bids.firstOrNull()?.price ?: 0.0
    val spread = if (lowestAsk > 0 && highestBid > 0) (lowestAsk - highestBid) else 0.0
    val spreadPercent = if (lastPrice > 0) (spread / lastPrice) * 100 else 0.0

    Column(
        modifier = modifier
            .background(DarkSurface)
            .padding(6.dp)
            .testTag("order_book_view")
    ) {
        // Mode Selector Bar & Liquidity Ratio Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Order Book",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(DarkObsidian)
                    .padding(2.dp)
            ) {
                listOf("All", "Bids", "Asks").forEachIndexed { index, label ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (displayMode == index) DarkSurfaceElevated else Color.Transparent)
                            .clickable { displayMode = index }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                color = if (displayMode == index) NeonGreen else TextMuted
                            )
                        )
                    }
                }
            }
        }

        // Market Liquidity Bar (Buy Wall % vs Sell Wall %)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "B ${String.format("%.1f%%", bidRatio * 100)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = NeonGreen)
                )
                Text(
                    text = "Liquidity Wall",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = TextMuted)
                )
                Text(
                    text = "A ${String.format("%.1f%%", askRatio * 100)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = CrimsonRed)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(bidRatio)
                        .height(4.dp)
                        .background(NeonGreen)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(CrimsonRed)
                )
            }
        }

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Price (USDT)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = TextMuted))
            Text("Amount", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = TextMuted))
            Text("Total", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = TextMuted))
        }

        val visibleCount = if (displayMode == 0) 6 else 12

        // Asks (Sells in Red)
        if (displayMode == 0 || displayMode == 2) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                asks.take(visibleCount).reversed().forEach { ask ->
                    OrderBookRow(
                        entry = ask,
                        maxTotal = asks.lastOrNull()?.total ?: 1.0,
                        avgVol = avgAskVol,
                        onPriceSelect = onPriceSelect
                    )
                }
            }
        }

        // Spread / Current Price Bar
        if (displayMode == 0) {
            val priceColor = if (isBullish) NeonGreen else CrimsonRed
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(DarkObsidian)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            lastPrice >= 1000 -> String.format("%.2f", lastPrice)
                            lastPrice >= 1 -> String.format("%.4f", lastPrice)
                            else -> String.format("%.6f", lastPrice)
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = priceColor,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Spread ${String.format("%.2f", spread)} (${String.format("%.2f%%", spreadPercent)})",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = TextMuted)
                    )
                }
            }
        }

        // Bids (Buys in Green)
        if (displayMode == 0 || displayMode == 1) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                bids.take(visibleCount).forEach { bid ->
                    OrderBookRow(
                        entry = bid,
                        maxTotal = bids.lastOrNull()?.total ?: 1.0,
                        avgVol = avgBidVol,
                        onPriceSelect = onPriceSelect
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderBookRow(
    entry: OrderBookEntry,
    maxTotal: Double,
    avgVol: Double,
    onPriceSelect: ((Double) -> Unit)?
) {
    val barRatio = (entry.total / maxTotal).coerceIn(0.0, 1.0).toFloat()
    val isWall = entry.amount >= (avgVol * 1.8)
    val barColor = if (entry.isBid) {
        if (isWall) NeonGreen.copy(alpha = 0.35f) else NeonGreen.copy(alpha = 0.16f)
    } else {
        if (isWall) CrimsonRed.copy(alpha = 0.35f) else CrimsonRed.copy(alpha = 0.16f)
    }
    val textColor = if (entry.isBid) NeonGreen else CrimsonRed

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .clickable { onPriceSelect?.invoke(entry.price) }
    ) {
        // Depth background bar
        Box(
            modifier = Modifier
                .fillMaxWidth(barRatio)
                .height(20.dp)
                .align(Alignment.CenterEnd)
                .background(barColor)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when {
                        entry.price >= 1000 -> String.format("%.2f", entry.price)
                        entry.price >= 1 -> String.format("%.4f", entry.price)
                        else -> String.format("%.6f", entry.price)
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = textColor,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                )
                if (isWall) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(AmberGold)
                            .padding(horizontal = 2.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "WALL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = DarkObsidian,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }
            }

            Text(
                text = String.format("%.3f", entry.amount),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            )

            Text(
                text = String.format("%.2f", entry.total),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                )
            )
        }
    }
}

