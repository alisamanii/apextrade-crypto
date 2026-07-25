package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.MarketRepository
import com.example.data.repository.TradingRepository
import com.example.ui.components.TopTickerBar
import com.example.ui.screens.ChartScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.QuotesScreen
import com.example.ui.screens.TradeScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.theme.ApexTradeTheme
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
import com.example.ui.viewmodel.WalletViewModel

class MainActivity : ComponentActivity() {

    private lateinit var marketRepository: MarketRepository
    private lateinit var tradingRepository: TradingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        marketRepository = MarketRepository()
        tradingRepository = TradingRepository(marketRepository)

        val marketViewModel = MarketViewModel(marketRepository)
        val tradingViewModel = TradingViewModel(marketRepository, tradingRepository)
        val walletViewModel = WalletViewModel(tradingRepository)

        setContent {
            ApexTradeTheme {
                MainTradingApp(
                    marketViewModel = marketViewModel,
                    tradingViewModel = tradingViewModel,
                    walletViewModel = walletViewModel
                )
            }
        }
    }
}

enum class NavDestination(val label: String, val icon: ImageVector) {
    QUOTES("Quotes", Icons.Default.FormatListBulleted),
    CHART("Chart", Icons.Default.CandlestickChart),
    TRADE("Trade", Icons.Default.SwapHoriz),
    WALLET("Wallet", Icons.Default.Wallet),
    PROFILE("Profile", Icons.Default.AccountCircle)
}

@Composable
fun MainTradingApp(
    marketViewModel: MarketViewModel,
    tradingViewModel: TradingViewModel,
    walletViewModel: WalletViewModel
) {
    var selectedTabState by remember { mutableIntStateOf(0) } // 0: Quotes, 1: Chart, 2: Trade, 3: Wallet, 4: Profile

    val selectedPair by marketViewModel.selectedPair.collectAsState()
    val allPairs by marketViewModel.filteredPairs.collectAsState()
    val wsConnected by marketViewModel.wsConnected.collectAsState()
    val latencyMs by marketViewModel.latencyMs.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_trading_scaffold"),
        containerColor = DarkObsidian,
        topBar = {
            TopTickerBar(
                selectedPair = selectedPair,
                allPairs = allPairs,
                onPairSelected = { pairId ->
                    marketViewModel.selectPair(pairId)
                },
                wsConnected = wsConnected,
                latencyMs = latencyMs
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = TextPrimary,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavDestination.values().forEachIndexed { index, destination ->
                    val isSelected = selectedTabState == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabState = index },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                                tint = if (isSelected) NeonGreen else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonGreen else TextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = DarkSurfaceElevated
                        ),
                        modifier = Modifier.testTag("nav_tab_${destination.label.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkObsidian)
        ) {
            when (selectedTabState) {
                0 -> QuotesScreen(
                    viewModel = marketViewModel,
                    onNavigateToChart = {
                        selectedTabState = 1
                    },
                    onNavigateToTrade = {
                        selectedTabState = 2
                    }
                )
                1 -> ChartScreen(
                    marketViewModel = marketViewModel,
                    tradingViewModel = tradingViewModel,
                    onNavigateToTrade = {
                        selectedTabState = 2
                    }
                )
                2 -> TradeScreen(
                    viewModel = tradingViewModel
                )
                3 -> WalletScreen(
                    viewModel = walletViewModel
                )
                4 -> ProfileScreen()
            }
        }
    }
}
