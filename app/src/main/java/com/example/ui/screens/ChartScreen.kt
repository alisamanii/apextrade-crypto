package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.CandlestickChart
import com.example.ui.components.TpSlDialog
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MarketViewModel
import com.example.ui.viewmodel.TradingViewModel

@Composable
fun ChartScreen(
    marketViewModel: MarketViewModel,
    tradingViewModel: TradingViewModel,
    onNavigateToTrade: (String) -> Unit
) {
    val selectedPair by marketViewModel.selectedPair.collectAsState()
    val candles by marketViewModel.candles.collectAsState()
    val indicators by marketViewModel.chartIndicators.collectAsState()
    val positions by tradingViewModel.positions.collectAsState()

    val timeframes = listOf("1m", "5m", "15m", "1h", "4h", "1D", "1W")

    // Get active position for current pair if any to display TP/SL lines
    val currentPosition = positions.firstOrNull { it.pairId == selectedPair?.id }
    var showTpSlDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkObsidian)
            .testTag("chart_screen")
    ) {
        // Timeframe & Chart Style Toolbar
        Surface(
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, SlateBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Timeframe buttons
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    timeframes.forEach { tf ->
                        val isSelected = indicators.timeframe == tf
                        Button(
                            onClick = { marketViewModel.updateTimeframe(tf) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) NeonGreen else DarkSurfaceElevated,
                                contentColor = if (isSelected) DarkObsidian else TextSecondary
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("timeframe_button_$tf")
                        ) {
                            Text(tf, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                // Candlestick vs Line toggle
                IconButton(
                    onClick = { marketViewModel.toggleCandlestickType() },
                    modifier = Modifier.testTag("chart_type_toggle")
                ) {
                    Icon(
                        imageVector = if (indicators.isCandlestick) Icons.Default.BarChart else Icons.Default.ShowChart,
                        contentDescription = "Chart Type",
                        tint = NeonGreen
                    )
                }
            }
        }

        // Technical Indicator Toggle Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = indicators.showEma20,
                onClick = { marketViewModel.toggleEma20() },
                label = { Text("EMA 20", color = if (indicators.showEma20) AmberGold else TextMuted) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AmberGold.copy(alpha = 0.2f),
                    containerColor = DarkSurfaceElevated
                ),
                modifier = Modifier.testTag("indicator_ema20")
            )

            FilterChip(
                selected = indicators.showEma50,
                onClick = { marketViewModel.toggleEma50() },
                label = { Text("EMA 50", color = if (indicators.showEma50) CyanAccent else TextMuted) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                    containerColor = DarkSurfaceElevated
                ),
                modifier = Modifier.testTag("indicator_ema50")
            )

            FilterChip(
                selected = indicators.showBollingerBands,
                onClick = { marketViewModel.toggleBollingerBands() },
                label = { Text("BOLL", color = if (indicators.showBollingerBands) NeonGreen else TextMuted) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NeonGreen.copy(alpha = 0.2f),
                    containerColor = DarkSurfaceElevated
                ),
                modifier = Modifier.testTag("indicator_bollinger")
            )

            FilterChip(
                selected = indicators.showRsi,
                onClick = { marketViewModel.toggleRsi() },
                label = { Text("RSI", color = if (indicators.showRsi) NeonGreen else TextMuted) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NeonGreen.copy(alpha = 0.2f),
                    containerColor = DarkSurfaceElevated
                ),
                modifier = Modifier.testTag("indicator_rsi")
            )

            FilterChip(
                selected = indicators.showMacd,
                onClick = { marketViewModel.toggleMacd() },
                label = { Text("MACD", color = if (indicators.showMacd) NeonGreen else TextMuted) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NeonGreen.copy(alpha = 0.2f),
                    containerColor = DarkSurfaceElevated
                ),
                modifier = Modifier.testTag("indicator_macd")
            )
        }

        // Active Order / Position Overlay Indicator on Chart
        if (currentPosition != null) {
            Surface(
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "POS: ${currentPosition.side.name} ${currentPosition.leverage}x",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (currentPosition.side == com.example.data.model.OrderSide.BUY_LONG) NeonGreen else CrimsonRed,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PnL: ${if (currentPosition.unrealizedPnL >= 0) "+" else ""}${String.format("$%.2f", currentPosition.unrealizedPnL)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (currentPosition.unrealizedPnL >= 0) NeonGreen else CrimsonRed,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Button(
                        onClick = { showTpSlDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkObsidian, contentColor = TextPrimary),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Drag/Modify TP/SL", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Main Chart Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            CandlestickChart(
                candles = candles,
                indicators = indicators,
                takeProfitPrice = currentPosition?.takeProfit,
                stopLossPrice = currentPosition?.stopLoss
            )
        }

        // Bottom Dock Execution Buttons (Buy / Sell)
        Surface(
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        tradingViewModel.setOrderSide(com.example.data.model.OrderSide.BUY_LONG)
                        onNavigateToTrade(selectedPair?.id ?: "BTC/USDT")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = DarkObsidian),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("chart_buy_button")
                ) {
                    Text("BUY / LONG", fontWeight = FontWeight.ExtraBold)
                }

                Button(
                    onClick = {
                        tradingViewModel.setOrderSide(com.example.data.model.OrderSide.SELL_SHORT)
                        onNavigateToTrade(selectedPair?.id ?: "BTC/USDT")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = TextPrimary),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("chart_sell_button")
                ) {
                    Text("SELL / SHORT", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }

    if (showTpSlDialog && currentPosition != null) {
        TpSlDialog(
            position = currentPosition,
            onDismiss = { showTpSlDialog = false },
            onConfirm = { newTp, newSl ->
                tradingViewModel.updatePositionTpSl(currentPosition.id, newTp, newSl)
            }
        )
    }
}
