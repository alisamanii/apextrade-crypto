package com.example.data.model

enum class OrderType {
    LIMIT, MARKET, STOP_LIMIT, OCO
}

enum class OrderSide {
    BUY_LONG, SELL_SHORT
}

enum class OrderStatus {
    OPEN, FILLED, CANCELLED
}

enum class MarketTab {
    ALL, SPOT, FUTURES, HOT, GAINERS, LOSERS
}

data class CryptoPair(
    val id: String,
    val symbol: String,
    val quoteAsset: String = "USDT",
    val lastPrice: Double,
    val priceChange24h: Double,
    val high24h: Double,
    val low24h: Double,
    val volume24h: Double,
    val bid: Double,
    val ask: Double,
    val spread: Double,
    val isFavorite: Boolean = false,
    val isHot: Boolean = false,
    val isFutures: Boolean = true
) {
    val formattedPrice: String
        get() = when {
            lastPrice >= 1000 -> String.format("%.2f", lastPrice)
            lastPrice >= 1 -> String.format("%.4f", lastPrice)
            else -> String.format("%.6f", lastPrice)
        }

    val isBullish: Boolean get() = priceChange24h >= 0
}

data class Candle(
    val timestamp: Long,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Float
) {
    val isBullish: Boolean get() = close >= open
}

data class OrderBookEntry(
    val price: Double,
    val amount: Double,
    val total: Double,
    val isBid: Boolean
)

data class Order(
    val id: String,
    val pairId: String,
    val type: OrderType,
    val side: OrderSide,
    val price: Double,
    val amount: Double,
    val filledAmount: Double = 0.0,
    val status: OrderStatus = OrderStatus.OPEN,
    val timestamp: Long = System.currentTimeMillis(),
    val takeProfit: Double? = null,
    val stopLoss: Double? = null,
    val leverage: Int = 10
)

data class Position(
    val id: String,
    val pairId: String,
    val side: OrderSide,
    val entryPrice: Double,
    var markPrice: Double,
    val size: Double,
    val leverage: Int,
    val margin: Double,
    val liquidationPrice: Double,
    var takeProfit: Double? = null,
    var stopLoss: Double? = null
) {
    val unrealizedPnL: Double
        get() {
            val priceDiff = if (side == OrderSide.BUY_LONG) (markPrice - entryPrice) else (entryPrice - markPrice)
            return (priceDiff / entryPrice) * size * entryPrice * leverage
        }

    val roePercent: Double
        get() = if (margin > 0) (unrealizedPnL / margin) * 100 else 0.0
}

data class WalletAsset(
    val symbol: String,
    val name: String,
    val amount: Double,
    val priceUsd: Double,
    val valueUsd: Double = amount * priceUsd,
    val change24h: Double = 0.0,
    val allocationPercent: Float = 0f
)

data class ChartIndicators(
    val showEma20: Boolean = true,
    val showEma50: Boolean = true,
    val showEma200: Boolean = false,
    val showBollingerBands: Boolean = false,
    val showRsi: Boolean = false,
    val showMacd: Boolean = false,
    val isCandlestick: Boolean = true,
    val timeframe: String = "15m"
)
