package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.model.OrderSide
import com.example.data.model.OrderType
import com.example.data.model.Position
import com.example.ui.components.LeverageDialog
import com.example.ui.components.OrderBookView
import com.example.ui.components.TpSlDialog
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
import com.example.ui.viewmodel.TradingViewModel

@Composable
fun TradeScreen(
    viewModel: TradingViewModel
) {
    val context = LocalContext.current
    val selectedPair by viewModel.selectedPair.collectAsState()
    val bids by viewModel.orderBookBids.collectAsState()
    val asks by viewModel.orderBookAsks.collectAsState()

    val orderSide by viewModel.orderSide.collectAsState()
    val orderType by viewModel.orderType.collectAsState()
    val isFuturesMode by viewModel.isFuturesMode.collectAsState()
    val leverage by viewModel.leverage.collectAsState()
    val marginType by viewModel.marginType.collectAsState()
    val priceInput by viewModel.priceInput.collectAsState()
    val amountInput by viewModel.amountInput.collectAsState()
    val isTpSlEnabled by viewModel.isTpSlEnabled.collectAsState()
    val tpInput by viewModel.takeProfitInput.collectAsState()
    val slInput by viewModel.stopLossInput.collectAsState()

    val availableMargin by viewModel.availableMarginUsdt.collectAsState()
    val positions by viewModel.positions.collectAsState()
    val openOrders by viewModel.openOrders.collectAsState()
    val orderHistory by viewModel.orderHistory.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    var showLeverageDialog by remember { mutableStateOf(false) }
    var selectedPositionForTpSl by remember { mutableStateOf<Position?>(null) }
    var bottomTabState by remember { mutableIntStateOf(0) } // 0: Positions, 1: Open Orders, 2: Order History
    var showTypeDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkObsidian)
            .testTag("trade_screen")
    ) {
        // Top Spot / Futures & Margin/Leverage Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spot / Futures Toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkObsidian)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (!isFuturesMode) NeonGreen else DarkObsidian)
                        .clickable { viewModel.setIsFuturesMode(false) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "Spot",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (!isFuturesMode) DarkObsidian else TextSecondary
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isFuturesMode) NeonGreen else DarkObsidian)
                        .clickable { viewModel.setIsFuturesMode(true) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "Futures",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isFuturesMode) DarkObsidian else TextSecondary
                        )
                    )
                }
            }

            // Margin Mode & Leverage Button
            if (isFuturesMode) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceElevated)
                            .clickable {
                                viewModel.setMarginType(if (marginType == "Isolated") "Cross" else "Isolated")
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            marginType,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceElevated)
                            .clickable { showLeverageDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("leverage_button")
                    ) {
                        Text(
                            "${leverage}x",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonGreen,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }
        }

        // Main Execution Terminal split: Left (Execution Form), Right (Order Book)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Left Column: Order Form
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Buy / Sell Tabs
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { viewModel.setOrderSide(OrderSide.BUY_LONG) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (orderSide == OrderSide.BUY_LONG) NeonGreen else DarkSurfaceElevated,
                            contentColor = if (orderSide == OrderSide.BUY_LONG) DarkObsidian else TextSecondary
                        ),
                        shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("order_side_buy")
                    ) {
                        Text(if (isFuturesMode) "Open Long" else "Buy", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { viewModel.setOrderSide(OrderSide.SELL_SHORT) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (orderSide == OrderSide.SELL_SHORT) CrimsonRed else DarkSurfaceElevated,
                            contentColor = if (orderSide == OrderSide.SELL_SHORT) TextPrimary else TextSecondary
                        ),
                        shape = RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("order_side_sell")
                    ) {
                        Text(if (isFuturesMode) "Open Short" else "Sell", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // Order Type Selector
                Box {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceElevated)
                            .clickable { showTypeDropdown = true }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = orderType.name,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen
                            )
                        )
                    }

                    DropdownMenu(
                        expanded = showTypeDropdown,
                        onDismissRequest = { showTypeDropdown = false },
                        modifier = Modifier.background(DarkSurfaceElevated)
                    ) {
                        OrderType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name, color = TextPrimary) },
                                onClick = {
                                    viewModel.setOrderType(type)
                                    showTypeDropdown = false
                                }
                            )
                        }
                    }
                }

                // Price Input
                if (orderType != OrderType.MARKET) {
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { viewModel.setPriceInput(it) },
                        label = { Text("Price (USDT)", color = TextMuted, fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = SlateBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("order_price_input")
                    )
                }

                // Amount Input
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { viewModel.setAmountInput(it) },
                    label = { Text("Amount (${selectedPair?.symbol ?: "BTC"})", color = TextMuted, fontSize = 10.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("order_amount_input")
                )

                // Quick Percentage Chips (25%, 50%, 75%, 100%)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(0.25f to "25%", 0.50f to "50%", 0.75f to "75%", 1.0f to "100%").forEach { (ratio, label) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(DarkSurfaceElevated)
                                .clickable { viewModel.setAmountPercent(ratio) }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = TextSecondary))
                        }
                    }
                }

                // Available Margin
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Avail:", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "${String.format("%.2f", availableMargin)} USDT",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary, fontFamily = FontFamily.Monospace)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Submit Action Button
                val btnColor = if (orderSide == OrderSide.BUY_LONG) NeonGreen else CrimsonRed
                val btnText = if (orderSide == OrderSide.BUY_LONG) {
                    if (isFuturesMode) "Buy / Long" else "Buy ${selectedPair?.symbol}"
                } else {
                    if (isFuturesMode) "Sell / Short" else "Sell ${selectedPair?.symbol}"
                }

                Button(
                    onClick = { viewModel.placeOrder() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = btnColor,
                        contentColor = if (orderSide == OrderSide.BUY_LONG) DarkObsidian else TextPrimary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("execute_order_button")
                ) {
                    Text(btnText, fontWeight = FontWeight.ExtraBold)
                }
            }

            // Right Column: Order Book View
            OrderBookView(
                bids = bids,
                asks = asks,
                lastPrice = selectedPair?.lastPrice ?: 0.0,
                isBullish = selectedPair?.isBullish == true,
                onPriceSelect = { selectedPrice ->
                    viewModel.setPriceInput(when {
                        selectedPrice >= 1000 -> String.format("%.2f", selectedPrice)
                        selectedPrice >= 1 -> String.format("%.4f", selectedPrice)
                        else -> String.format("%.6f", selectedPrice)
                    })
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Bottom Orders / Positions Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(DarkSurface)
        ) {
            TabRow(
                selectedTabIndex = bottomTabState,
                containerColor = DarkSurfaceElevated,
                contentColor = TextPrimary,
                indicator = { tabPositions ->
                    if (bottomTabState < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[bottomTabState]),
                            color = NeonGreen
                        )
                    }
                }
            ) {
                Tab(
                    selected = bottomTabState == 0,
                    onClick = { bottomTabState = 0 },
                    text = { Text("Positions (${positions.size})", style = MaterialTheme.typography.labelSmall) }
                )
                Tab(
                    selected = bottomTabState == 1,
                    onClick = { bottomTabState = 1 },
                    text = { Text("Open Orders (${openOrders.size})", style = MaterialTheme.typography.labelSmall) }
                )
                Tab(
                    selected = bottomTabState == 2,
                    onClick = { bottomTabState = 2 },
                    text = { Text("Order History", style = MaterialTheme.typography.labelSmall) }
                )
            }

            when (bottomTabState) {
                0 -> {
                    // Positions List
                    if (positions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No Active Positions", color = TextMuted)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                            items(positions, key = { it.id }) { pos ->
                                PositionCardItem(
                                    position = pos,
                                    onClose = { viewModel.closePosition(pos.id) },
                                    onTpSlClick = { selectedPositionForTpSl = pos }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
                1 -> {
                    // Open Orders List
                    if (openOrders.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No Open Orders", color = TextMuted)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                            items(openOrders, key = { it.id }) { order ->
                                OpenOrderCardItem(
                                    order = order,
                                    onCancel = { viewModel.cancelOrder(order.id) }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
                2 -> {
                    // Order History List
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        items(orderHistory, key = { it.id }) { order ->
                            OrderHistoryCardItem(order = order)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }

    if (showLeverageDialog) {
        LeverageDialog(
            currentLeverage = leverage,
            pairPrice = selectedPair?.lastPrice ?: 1.0,
            onDismiss = { showLeverageDialog = false },
            onConfirm = { viewModel.setLeverage(it) }
        )
    }

    selectedPositionForTpSl?.let { pos ->
        TpSlDialog(
            position = pos,
            onDismiss = { selectedPositionForTpSl = null },
            onConfirm = { tp, sl ->
                viewModel.updatePositionTpSl(pos.id, tp, sl)
            }
        )
    }
}

@Composable
private fun PositionCardItem(
    position: Position,
    onClose: () -> Unit,
    onTpSlClick: () -> Unit
) {
    val sideColor = if (position.side == OrderSide.BUY_LONG) NeonGreen else CrimsonRed
    val pnlColor = if (position.unrealizedPnL >= 0) NeonGreen else CrimsonRed

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkObsidian),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, SlateBorder),
        modifier = Modifier.fillMaxWidth().testTag("position_item_${position.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(sideColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${position.side.name} ${position.leverage}x",
                            style = MaterialTheme.typography.labelSmall.copy(color = sideColor, fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(position.pairId, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed.copy(alpha = 0.2f), contentColor = CrimsonRed),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Close", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Unrealized PnL (ROE%)", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = "${if (position.unrealizedPnL >= 0) "+" else ""}${String.format("$%.2f", position.unrealizedPnL)} (${String.format("%.2f%%", position.roePercent)})",
                        style = MaterialTheme.typography.bodyMedium.copy(color = pnlColor, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Margin", style = MaterialTheme.typography.labelSmall)
                    Text("$${String.format("%.2f", position.margin)}", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Entry: $${position.entryPrice}", style = MaterialTheme.typography.labelSmall)
                Text("Mark: $${position.markPrice}", style = MaterialTheme.typography.labelSmall)
                Text("Liq: $${String.format("%.1f", position.liquidationPrice)}", style = MaterialTheme.typography.labelSmall.copy(color = CrimsonRed))
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onTpSlClick,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = TextPrimary),
                modifier = Modifier.fillMaxWidth().height(28.dp)
            ) {
                Text(
                    text = "TP: ${position.takeProfit ?: "None"} / SL: ${position.stopLoss ?: "None"}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun OpenOrderCardItem(
    order: Order,
    onCancel: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkObsidian),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, SlateBorder),
        modifier = Modifier.fillMaxWidth().testTag("open_order_item_${order.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("${order.type.name} ${order.side.name} ${order.pairId}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text("Price: $${order.price} | Amount: ${order.amount}", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace))
            }

            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = CrimsonRed),
                modifier = Modifier.height(28.dp)
            ) {
                Text("Cancel", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun OrderHistoryCardItem(order: Order) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkObsidian),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("${order.side.name} ${order.pairId}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text("Status: ${order.status.name}", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$${order.price}", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace))
                Text("${order.amount} qty", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
