package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.WalletAsset
import com.example.ui.components.QuickActionBottomSheet
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.WalletViewModel

@Composable
fun WalletScreen(
    viewModel: WalletViewModel
) {
    val context = LocalContext.current
    val totalBalance by viewModel.totalBalanceUsdt.collectAsState()
    val availableMargin by viewModel.availableMarginUsdt.collectAsState()
    val isPrivacyHidden by viewModel.isPrivacyHidden.collectAsState()
    val walletAssets by viewModel.walletAssets.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    var activeQuickAction by remember { mutableStateOf<String?>(null) }

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
            .padding(12.dp)
            .testTag("wallet_screen")
    ) {
        // Portfolio Balance Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("portfolio_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Portfolio Value", style = MaterialTheme.typography.labelMedium)
                    IconButton(
                        onClick = { viewModel.togglePrivacyHidden() },
                        modifier = Modifier.size(32.dp).testTag("privacy_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isPrivacyHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Balance Visibility",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isPrivacyHidden) "••••••••" else String.format("$%.2f", totalBalance),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "24h PnL: +$3,420.15 (+2.82%)",
                        style = MaterialTheme.typography.labelMedium.copy(color = NeonGreen, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Avail: ${if (isPrivacyHidden) "••••" else String.format("$%.2f", availableMargin)}",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Action Buttons (Deposit, Withdraw, Transfer, P2P)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionButton("Deposit", Icons.Default.ArrowDownward, NeonGreen) {
                activeQuickAction = "Deposit"
            }
            QuickActionButton("Withdraw", Icons.Default.ArrowUpward, CrimsonRed) {
                activeQuickAction = "Withdraw"
            }
            QuickActionButton("Transfer", Icons.Default.SwapHoriz, CyanAccent) {
                activeQuickAction = "Transfer"
            }
            QuickActionButton("P2P Trade", Icons.Default.People, AmberGold) {
                activeQuickAction = "P2P Trade"
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Asset Allocation Donut Chart + Asset List Title
        Text("Asset Allocation", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut Chart Canvas
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(90.dp)) {
                        val sliceColors = listOf(NeonGreen, AmberGold, CyanAccent, PurpleAccent)
                        var startAngle = -90f
                        walletAssets.forEachIndexed { idx, asset ->
                            val sweep = asset.allocationPercent * 360f
                            drawArc(
                                color = sliceColors[idx % sliceColors.size],
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = 18f)
                            )
                            startAngle += sweep
                        }
                    }
                    Text("Assets", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val sliceColors = listOf(NeonGreen, AmberGold, CyanAccent, PurpleAccent)
                    walletAssets.forEachIndexed { idx, asset ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(sliceColors[idx % sliceColors.size])
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${asset.symbol} (${String.format("%.1f%%", asset.allocationPercent * 100)})",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Asset Breakdown List
        Text("Assets Breakdown", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(walletAssets) { asset ->
                WalletAssetRowItem(asset = asset, isHidden = isPrivacyHidden)
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }

    activeQuickAction?.let { action ->
        QuickActionBottomSheet(
            actionTitle = action,
            onDismiss = { activeQuickAction = null },
            onSubmit = { input ->
                viewModel.triggerQuickAction("$action submitted with details: $input")
            }
        )
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)
            .testTag("quick_action_$label")
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = DarkSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = label, tint = color)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun WalletAssetRowItem(
    asset: WalletAsset,
    isHidden: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, SlateBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(asset.symbol, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
                Text(asset.name, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isHidden) "••••" else String.format("$%.2f", asset.valueUsd),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                )
                Text(
                    text = if (isHidden) "••••" else "${asset.amount} ${asset.symbol}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontFamily = FontFamily.Monospace)
                )
            }
        }
    }
}
