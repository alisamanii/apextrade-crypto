package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Candle
import com.example.data.model.ChartIndicators
import com.example.data.model.CryptoPair
import com.example.data.model.MarketTab
import com.example.data.repository.MarketRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MarketViewModel(
    private val repository: MarketRepository
) : ViewModel() {

    val selectedPair: StateFlow<CryptoPair?> = repository.selectedPair
    val candles: StateFlow<List<Candle>> = repository.candles
    val wsConnected: StateFlow<Boolean> = repository.wsConnected
    val latencyMs: StateFlow<Int> = repository.latencyMs

    private val _selectedTab = MutableStateFlow(MarketTab.ALL)
    val selectedTab: StateFlow<MarketTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _chartIndicators = MutableStateFlow(ChartIndicators())
    val chartIndicators: StateFlow<ChartIndicators> = _chartIndicators.asStateFlow()

    val filteredPairs: StateFlow<List<CryptoPair>> = combine(
        repository.pairs,
        _selectedTab,
        _searchQuery
    ) { pairs, tab, query ->
        pairs.filter { pair ->
            val matchesQuery = query.isEmpty() ||
                    pair.symbol.contains(query, ignoreCase = true) ||
                    pair.id.contains(query, ignoreCase = true)

            val matchesTab = when (tab) {
                MarketTab.ALL -> true
                MarketTab.SPOT -> !pair.isFutures
                MarketTab.FUTURES -> pair.isFutures
                MarketTab.HOT -> pair.isHot || pair.isFavorite
                MarketTab.GAINERS -> pair.priceChange24h > 0
                MarketTab.LOSERS -> pair.priceChange24h < 0
            }

            matchesQuery && matchesTab
        }.sortedByDescending { if (tab == MarketTab.GAINERS) it.priceChange24h else if (tab == MarketTab.LOSERS) -it.priceChange24h else it.volume24h }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectPair(pairId: String) {
        repository.selectPair(pairId)
    }

    fun setTab(tab: MarketTab) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(pairId: String) {
        repository.toggleFavorite(pairId)
    }

    fun updateTimeframe(timeframe: String) {
        _chartIndicators.value = _chartIndicators.value.copy(timeframe = timeframe)
    }

    fun toggleCandlestickType() {
        val current = _chartIndicators.value
        _chartIndicators.value = current.copy(isCandlestick = !current.isCandlestick)
    }

    fun toggleEma20() {
        val current = _chartIndicators.value
        _chartIndicators.value = current.copy(showEma20 = !current.showEma20)
    }

    fun toggleEma50() {
        val current = _chartIndicators.value
        _chartIndicators.value = current.copy(showEma50 = !current.showEma50)
    }

    fun toggleBollingerBands() {
        val current = _chartIndicators.value
        _chartIndicators.value = current.copy(showBollingerBands = !current.showBollingerBands)
    }

    fun toggleRsi() {
        val current = _chartIndicators.value
        _chartIndicators.value = current.copy(showRsi = !current.showRsi)
    }

    fun toggleMacd() {
        val current = _chartIndicators.value
        _chartIndicators.value = current.copy(showMacd = !current.showMacd)
    }
}
