package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CryptoPair
import com.example.data.model.Order
import com.example.data.model.OrderBookEntry
import com.example.data.model.OrderSide
import com.example.data.model.OrderType
import com.example.data.model.Position
import com.example.data.repository.MarketRepository
import com.example.data.repository.TradingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TradingViewModel(
    private val marketRepository: MarketRepository,
    private val tradingRepository: TradingRepository
) : ViewModel() {

    val selectedPair: StateFlow<CryptoPair?> = marketRepository.selectedPair
    val orderBookBids: StateFlow<List<OrderBookEntry>> = marketRepository.orderBookBids
    val orderBookAsks: StateFlow<List<OrderBookEntry>> = marketRepository.orderBookAsks

    val totalBalanceUsdt: StateFlow<Double> = tradingRepository.totalBalanceUsdt
    val availableMarginUsdt: StateFlow<Double> = tradingRepository.availableMarginUsdt
    val positions: StateFlow<List<Position>> = tradingRepository.positions
    val openOrders: StateFlow<List<Order>> = tradingRepository.openOrders
    val orderHistory: StateFlow<List<Order>> = tradingRepository.orderHistory

    private val _orderSide = MutableStateFlow(OrderSide.BUY_LONG)
    val orderSide: StateFlow<OrderSide> = _orderSide.asStateFlow()

    private val _orderType = MutableStateFlow(OrderType.LIMIT)
    val orderType: StateFlow<OrderType> = _orderType.asStateFlow()

    private val _isFuturesMode = MutableStateFlow(true)
    val isFuturesMode: StateFlow<Boolean> = _isFuturesMode.asStateFlow()

    private val _leverage = MutableStateFlow(20)
    val leverage: StateFlow<Int> = _leverage.asStateFlow()

    private val _marginType = MutableStateFlow("Isolated")
    val marginType: StateFlow<String> = _marginType.asStateFlow()

    private val _priceInput = MutableStateFlow("")
    val priceInput: StateFlow<String> = _priceInput.asStateFlow()

    private val _amountInput = MutableStateFlow("")
    val amountInput: StateFlow<String> = _amountInput.asStateFlow()

    private val _isTpSlEnabled = MutableStateFlow(false)
    val isTpSlEnabled: StateFlow<Boolean> = _isTpSlEnabled.asStateFlow()

    private val _takeProfitInput = MutableStateFlow("")
    val takeProfitInput: StateFlow<String> = _takeProfitInput.asStateFlow()

    private val _stopLossInput = MutableStateFlow("")
    val stopLossInput: StateFlow<String> = _stopLossInput.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        viewModelScope.launch {
            selectedPair.collect { pair ->
                if (pair != null && _priceInput.value.isEmpty()) {
                    _priceInput.value = pair.formattedPrice
                }
            }
        }
    }

    fun setOrderSide(side: OrderSide) {
        _orderSide.value = side
    }

    fun setOrderType(type: OrderType) {
        _orderType.value = type
        if (type == OrderType.MARKET) {
            selectedPair.value?.let {
                _priceInput.value = it.formattedPrice
            }
        }
    }

    fun setIsFuturesMode(isFutures: Boolean) {
        _isFuturesMode.value = isFutures
    }

    fun setLeverage(lev: Int) {
        _leverage.value = lev.coerceIn(1, 125)
    }

    fun setMarginType(type: String) {
        _marginType.value = type
    }

    fun setPriceInput(price: String) {
        _priceInput.value = price
    }

    fun setAmountInput(amount: String) {
        _amountInput.value = amount
    }

    fun setAmountPercent(percent: Float) {
        val avail = availableMarginUsdt.value
        val pairPrice = priceInput.value.toDoubleOrNull() ?: selectedPair.value?.lastPrice ?: 1.0
        val lev = leverage.value
        val maxNotional = avail * lev * percent
        val qty = if (pairPrice > 0) maxNotional / pairPrice else 0.0

        _amountInput.value = when {
            qty >= 100 -> String.format("%.1f", qty)
            qty >= 1 -> String.format("%.3f", qty)
            else -> String.format("%.4f", qty)
        }
    }

    fun toggleTpSlEnabled() {
        _isTpSlEnabled.value = !_isTpSlEnabled.value
    }

    fun setTakeProfitInput(tp: String) {
        _takeProfitInput.value = tp
    }

    fun setStopLossInput(sl: String) {
        _stopLossInput.value = sl
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun placeOrder() {
        val pair = selectedPair.value ?: return
        val price = if (_orderType.value == OrderType.MARKET) pair.lastPrice else (_priceInput.value.toDoubleOrNull() ?: pair.lastPrice)
        val amount = _amountInput.value.toDoubleOrNull() ?: 0.0

        if (amount <= 0) {
            _toastMessage.value = "Please enter a valid order amount"
            return
        }

        val tp = if (_isTpSlEnabled.value) _takeProfitInput.value.toDoubleOrNull() else null
        val sl = if (_isTpSlEnabled.value) _stopLossInput.value.toDoubleOrNull() else null

        val orderId = tradingRepository.placeOrder(
            pairId = pair.id,
            type = _orderType.value,
            side = _orderSide.value,
            price = price,
            amount = amount,
            leverage = _leverage.value,
            takeProfit = tp,
            stopLoss = sl,
            isFutures = _isFuturesMode.value
        )

        _toastMessage.value = "Order Executed successfully: $orderId"
        _amountInput.value = ""
    }

    fun cancelOrder(orderId: String) {
        tradingRepository.cancelOrder(orderId)
        _toastMessage.value = "Order $orderId cancelled"
    }

    fun closePosition(positionId: String) {
        tradingRepository.closePosition(positionId)
        _toastMessage.value = "Position $positionId closed"
    }

    fun updatePositionTpSl(positionId: String, tp: Double?, sl: Double?) {
        tradingRepository.updatePositionTpSl(positionId, tp, sl)
        _toastMessage.value = "TP/SL updated for $positionId"
    }
}
