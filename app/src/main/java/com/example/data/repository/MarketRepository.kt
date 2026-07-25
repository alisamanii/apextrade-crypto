package com.example.data.repository

import com.example.data.model.Candle
import com.example.data.model.CryptoPair
import com.example.data.model.OrderBookEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

class MarketRepository(private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.Default)) {

    private val _pairs = MutableStateFlow<List<CryptoPair>>(emptyList())
    val pairs: StateFlow<List<CryptoPair>> = _pairs.asStateFlow()

    private val _selectedPair = MutableStateFlow<CryptoPair?>(null)
    val selectedPair: StateFlow<CryptoPair?> = _selectedPair.asStateFlow()

    private val _candles = MutableStateFlow<List<Candle>>(emptyList())
    val candles: StateFlow<List<Candle>> = _candles.asStateFlow()

    private val _orderBookBids = MutableStateFlow<List<OrderBookEntry>>(emptyList())
    val orderBookBids: StateFlow<List<OrderBookEntry>> = _orderBookBids.asStateFlow()

    private val _orderBookAsks = MutableStateFlow<List<OrderBookEntry>>(emptyList())
    val orderBookAsks: StateFlow<List<OrderBookEntry>> = _orderBookAsks.asStateFlow()

    private val _wsConnected = MutableStateFlow(true)
    val wsConnected: StateFlow<Boolean> = _wsConnected.asStateFlow()

    private val _latencyMs = MutableStateFlow(12)
    val latencyMs: StateFlow<Int> = _latencyMs.asStateFlow()

    init {
        initInitialPairs()
        selectPair("BTC/USDT")
        startLiveWebSocketSimulation()
    }

    private fun initInitialPairs() {
        val initialList = listOf(
            CryptoPair("BTC/USDT", "BTC", "USDT", 94850.50, 4.32, 96200.00, 91500.00, 482910240.0, 94849.80, 94850.50, 0.70, isFavorite = true, isHot = true),
            CryptoPair("ETH/USDT", "ETH", "USDT", 3420.75, -1.15, 3510.00, 3380.00, 219401200.0, 3420.50, 3420.80, 0.30, isFavorite = true, isHot = true),
            CryptoPair("SOL/USDT", "SOL", "USDT", 188.40, 8.75, 192.50, 172.10, 154820900.0, 188.35, 188.45, 0.10, isFavorite = true, isHot = true),
            CryptoPair("AVAX/USDT", "AVAX", "USDT", 32.85, 3.12, 34.10, 31.20, 42910500.0, 32.84, 32.86, 0.02, isFavorite = false, isHot = false),
            CryptoPair("BNB/USDT", "BNB", "USDT", 620.10, 0.85, 632.00, 612.00, 89201000.0, 620.05, 620.15, 0.10, isFavorite = true, isHot = false),
            CryptoPair("DOGE/USDT", "DOGE", "USDT", 0.1425, 12.40, 0.1510, 0.1240, 192830400.0, 0.1424, 0.1426, 0.0002, isFavorite = false, isHot = true),
            CryptoPair("XRP/USDT", "XRP", "USDT", 0.5840, -2.40, 0.6120, 0.5720, 82910400.0, 0.5839, 0.5841, 0.0002, isFavorite = false, isHot = false),
            CryptoPair("SUI/USDT", "SUI", "USDT", 2.15, 15.30, 2.28, 1.84, 112040000.0, 2.148, 2.152, 0.004, isFavorite = true, isHot = true),
            CryptoPair("NEAR/USDT", "NEAR", "USDT", 5.65, 5.80, 5.88, 5.30, 34910200.0, 5.64, 5.66, 0.02, isFavorite = false, isHot = false),
            CryptoPair("LINK/USDT", "LINK", "USDT", 16.40, -0.65, 17.10, 16.10, 28400000.0, 16.39, 16.41, 0.02, isFavorite = false, isHot = false)
        )
        _pairs.value = initialList
    }

    fun selectPair(pairId: String) {
        val target = _pairs.value.firstOrNull { it.id == pairId } ?: return
        _selectedPair.value = target
        generateMockCandles(target.lastPrice)
        generateMockOrderBook(target.lastPrice)
    }

    fun toggleFavorite(pairId: String) {
        _pairs.update { list ->
            list.map { if (it.id == pairId) it.copy(isFavorite = !it.isFavorite) else it }
        }
        if (_selectedPair.value?.id == pairId) {
            _selectedPair.update { it?.copy(isFavorite = !(it.isFavorite)) }
        }
    }

    private fun generateMockCandles(basePrice: Double) {
        val now = System.currentTimeMillis()
        val intervalMs = 15 * 60 * 1000L
        val candleList = mutableListOf<Candle>()
        var currentClose = basePrice.toFloat() * 0.92f

        for (i in 60 downTo 0) {
            val timestamp = now - (i * intervalMs)
            val delta = (Random.nextFloat() - 0.48f) * (basePrice.toFloat() * 0.015f)
            val open = currentClose
            val close = open + delta
            val high = maxOf(open, close) + Random.nextFloat() * (basePrice.toFloat() * 0.008f)
            val low = minOf(open, close) - Random.nextFloat() * (basePrice.toFloat() * 0.008f)
            val volume = 10f + Random.nextFloat() * 150f

            candleList.add(Candle(timestamp, open, high, low, close, volume))
            currentClose = close
        }
        _candles.value = candleList
    }

    private fun generateMockOrderBook(basePrice: Double) {
        val bids = mutableListOf<OrderBookEntry>()
        val asks = mutableListOf<OrderBookEntry>()
        val step = if (basePrice > 1000) 10.0 else if (basePrice > 1) 0.1 else 0.001

        var accumBid = 0.0
        for (i in 1..8) {
            val p = basePrice - (i * step)
            val amt = 0.5 + Random.nextDouble() * 3.5
            accumBid += amt
            bids.add(OrderBookEntry(p, amt, accumBid, isBid = true))
        }

        var accumAsk = 0.0
        for (i in 1..8) {
            val p = basePrice + (i * step)
            val amt = 0.5 + Random.nextDouble() * 3.5
            accumAsk += amt
            asks.add(OrderBookEntry(p, amt, accumAsk, isBid = false))
        }

        _orderBookBids.value = bids
        _orderBookAsks.value = asks
    }

    private fun startLiveWebSocketSimulation() {
        externalScope.launch {
            var counter = 0
            while (true) {
                delay(800)
                counter++
                
                // Simulate slight ping fluctuation
                _latencyMs.value = 10 + Random.nextInt(12)

                // Fluctuate pair prices
                _pairs.update { list ->
                    list.map { pair ->
                        val changeRatio = (Random.nextDouble() - 0.49) * 0.003
                        val newPrice = (pair.lastPrice * (1.0 + changeRatio)).coerceAtLeast(0.000001)
                        val newBid = newPrice - (pair.spread / 2)
                        val newAsk = newPrice + (pair.spread / 2)
                        val newHigh = maxOf(pair.high24h, newPrice)
                        val newLow = minOf(pair.low24h, newPrice)
                        pair.copy(
                            lastPrice = newPrice,
                            bid = newBid,
                            ask = newAsk,
                            high24h = newHigh,
                            low24h = newLow
                        )
                    }
                }

                // Update selected pair and latest candle
                val currentSelected = _selectedPair.value
                if (currentSelected != null) {
                    val updatedPair = _pairs.value.firstOrNull { it.id == currentSelected.id }
                    if (updatedPair != null) {
                        _selectedPair.value = updatedPair

                        // Update OrderBook around new price
                        generateMockOrderBook(updatedPair.lastPrice)

                        // Update latest candle
                        _candles.update { candleList ->
                            if (candleList.isEmpty()) return@update candleList
                            val lastCandle = candleList.last()
                            val newClose = updatedPair.lastPrice.toFloat()
                            val newHigh = maxOf(lastCandle.high, newClose)
                            val newLow = minOf(lastCandle.low, newClose)
                            val newVol = lastCandle.volume + Random.nextFloat() * 2f

                            val updatedLast = lastCandle.copy(
                                high = newHigh,
                                low = newLow,
                                close = newClose,
                                volume = newVol
                            )

                            candleList.dropLast(1) + updatedLast
                        }
                    }
                }
            }
        }
    }
}
