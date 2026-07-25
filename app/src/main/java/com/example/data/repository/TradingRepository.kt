package com.example.data.repository

import com.example.data.model.Order
import com.example.data.model.OrderSide
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.model.Position
import com.example.data.model.WalletAsset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class TradingRepository(
    private val marketRepository: MarketRepository,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _totalBalanceUsdt = MutableStateFlow(124580.42)
    val totalBalanceUsdt: StateFlow<Double> = _totalBalanceUsdt.asStateFlow()

    private val _availableMarginUsdt = MutableStateFlow(98250.10)
    val availableMarginUsdt: StateFlow<Double> = _availableMarginUsdt.asStateFlow()

    private val _openOrders = MutableStateFlow<List<Order>>(emptyList())
    val openOrders: StateFlow<List<Order>> = _openOrders.asStateFlow()

    private val _orderHistory = MutableStateFlow<List<Order>>(emptyList())
    val orderHistory: StateFlow<List<Order>> = _orderHistory.asStateFlow()

    private val _positions = MutableStateFlow<List<Position>>(emptyList())
    val positions: StateFlow<List<Position>> = _positions.asStateFlow()

    private val _walletAssets = MutableStateFlow<List<WalletAsset>>(emptyList())
    val walletAssets: StateFlow<List<WalletAsset>> = _walletAssets.asStateFlow()

    init {
        initInitialPositionsAndOrders()
        initWalletAssets()
        listenToPriceUpdatesForPositions()
    }

    private fun initInitialPositionsAndOrders() {
        // Mock sample active position
        val initialPosition = Position(
            id = "POS-BTC-001",
            pairId = "BTC/USDT",
            side = OrderSide.BUY_LONG,
            entryPrice = 92450.00,
            markPrice = 94850.50,
            size = 0.5,
            leverage = 20,
            margin = 2311.25,
            liquidationPrice = 87900.00,
            takeProfit = 98000.00,
            stopLoss = 90000.00
        )

        val initialPosition2 = Position(
            id = "POS-SOL-002",
            pairId = "SOL/USDT",
            side = OrderSide.SELL_SHORT,
            entryPrice = 195.00,
            markPrice = 188.40,
            size = 50.0,
            leverage = 10,
            margin = 975.00,
            liquidationPrice = 212.50,
            takeProfit = 175.00,
            stopLoss = 202.00
        )

        _positions.value = listOf(initialPosition, initialPosition2)

        val initialOrder = Order(
            id = "ORD-ETH-101",
            pairId = "ETH/USDT",
            type = OrderType.LIMIT,
            side = OrderSide.BUY_LONG,
            price = 3350.00,
            amount = 2.0,
            takeProfit = 3600.0,
            stopLoss = 3200.0,
            leverage = 10
        )
        _openOrders.value = listOf(initialOrder)
    }

    private fun initWalletAssets() {
        val assets = listOf(
            WalletAsset("USDT", "Tether USD", 98250.10, 1.0, 98250.10, 0.0, 0.788f),
            WalletAsset("BTC", "Bitcoin", 0.225, 94850.50, 21341.36, 4.32, 0.171f),
            WalletAsset("ETH", "Ethereum", 1.05, 3420.75, 3591.78, -1.15, 0.029f),
            WalletAsset("SOL", "Solana", 7.40, 188.40, 1394.16, 8.75, 0.011f)
        )
        _walletAssets.value = assets
    }

    private fun listenToPriceUpdatesForPositions() {
        externalScope.launch {
            marketRepository.selectedPair.collect { pair ->
                if (pair != null) {
                    _positions.update { list ->
                        list.map { pos ->
                            if (pos.pairId == pair.id) {
                                pos.copy(markPrice = pair.lastPrice)
                            } else pos
                        }
                    }
                }
            }
        }
    }

    fun placeOrder(
        pairId: String,
        type: OrderType,
        side: OrderSide,
        price: Double,
        amount: Double,
        leverage: Int,
        takeProfit: Double?,
        stopLoss: Double?,
        isFutures: Boolean
    ): String {
        val orderId = "ORD-" + UUID.randomUUID().toString().take(6).uppercase()

        if (type == OrderType.MARKET) {
            // Immediately fill and convert to position if Futures or spot trade
            val notionalValue = price * amount
            val marginRequired = notionalValue / leverage

            if (isFutures) {
                val liquidationPrice = if (side == OrderSide.BUY_LONG) {
                    price * (1.0 - (1.0 / leverage) * 0.9)
                } else {
                    price * (1.0 + (1.0 / leverage) * 0.9)
                }

                val newPos = Position(
                    id = "POS-" + UUID.randomUUID().toString().take(6).uppercase(),
                    pairId = pairId,
                    side = side,
                    entryPrice = price,
                    markPrice = price,
                    size = amount,
                    leverage = leverage,
                    margin = marginRequired,
                    liquidationPrice = liquidationPrice,
                    takeProfit = takeProfit,
                    stopLoss = stopLoss
                )
                _positions.update { listOf(newPos) + it }
                _availableMarginUsdt.update { (it - marginRequired).coerceAtLeast(0.0) }
            }

            val filledOrder = Order(
                id = orderId,
                pairId = pairId,
                type = type,
                side = side,
                price = price,
                amount = amount,
                filledAmount = amount,
                status = OrderStatus.FILLED,
                takeProfit = takeProfit,
                stopLoss = stopLoss,
                leverage = leverage
            )
            _orderHistory.update { listOf(filledOrder) + it }
        } else {
            // Limit or Stop-Limit -> Open order
            val newOrder = Order(
                id = orderId,
                pairId = pairId,
                type = type,
                side = side,
                price = price,
                amount = amount,
                status = OrderStatus.OPEN,
                takeProfit = takeProfit,
                stopLoss = stopLoss,
                leverage = leverage
            )
            _openOrders.update { listOf(newOrder) + it }
        }

        return orderId
    }

    fun cancelOrder(orderId: String) {
        val target = _openOrders.value.firstOrNull { it.id == orderId }
        if (target != null) {
            _openOrders.update { list -> list.filterNot { it.id == orderId } }
            val cancelled = target.copy(status = OrderStatus.CANCELLED)
            _orderHistory.update { listOf(cancelled) + it }
        }
    }

    fun closePosition(positionId: String) {
        val target = _positions.value.firstOrNull { it.id == positionId }
        if (target != null) {
            _positions.update { list -> list.filterNot { it.id == positionId } }
            _availableMarginUsdt.update { it + target.margin + target.unrealizedPnL }
            _totalBalanceUsdt.update { it + target.unrealizedPnL }
        }
    }

    fun updatePositionTpSl(positionId: String, newTp: Double?, newSl: Double?) {
        _positions.update { list ->
            list.map {
                if (it.id == positionId) {
                    it.copy(takeProfit = newTp, stopLoss = newSl)
                } else it
            }
        }
    }
}
