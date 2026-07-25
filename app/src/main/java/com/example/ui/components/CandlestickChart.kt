package com.example.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Candle
import com.example.data.model.ChartIndicators
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

@Composable
fun CandlestickChart(
    candles: List<Candle>,
    indicators: ChartIndicators,
    takeProfitPrice: Double? = null,
    stopLossPrice: Double? = null,
    onUpdateTpSl: ((Double?, Double?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkObsidian),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading chart data...", color = TextMuted)
        }
        return
    }

    var selectedCandleIndex by remember { mutableStateOf<Int?>(null) }
    var touchX by remember { mutableStateOf<Float?>(null) }
    var touchY by remember { mutableStateOf<Float?>(null) }

    // Zoom and Pan states
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableFloatStateOf(0.0f) }

    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkObsidian)
            .testTag("candlestick_chart_container")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Selected Candle Info Banner (Crosshair Tooltip)
            if (selectedCandleIndex != null && selectedCandleIndex!! in candles.indices) {
                val selected = candles[selectedCandleIndex!!]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateFormat.format(Date(selected.timestamp)),
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "O:${selected.open}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "H:${selected.high}",
                        style = MaterialTheme.typography.labelSmall.copy(color = NeonGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "L:${selected.low}",
                        style = MaterialTheme.typography.labelSmall.copy(color = CrimsonRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "C:${selected.close}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (selected.isBullish) NeonGreen else CrimsonRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Vol:${String.format("%.1f", selected.volume)}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                }
            }

            // Main Canvas Rendering
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            zoomScale = (zoomScale * zoom).coerceIn(0.5f, 3.5f)
                            panOffset = (panOffset + pan.x).coerceIn(-1000f, 1000f)
                        }
                    }
                    .pointerInput(candles) {
                        detectTapGestures(
                            onTap = { offset ->
                                touchX = offset.x
                                touchY = offset.y
                                val width = size.width - 120f // right padding for Y-axis scale
                                val visibleCandles = (candles.size / zoomScale).toInt().coerceIn(10, candles.size)
                                val candleWidth = width / visibleCandles
                                val index = ((offset.x - panOffset) / candleWidth).toInt().coerceIn(0, candles.lastIndex)
                                selectedCandleIndex = index
                            }
                        )
                    }
                    .pointerInput(candles) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                touchX = offset.x
                                touchY = offset.y
                            },
                            onDrag = { change, _ ->
                                touchX = change.position.x
                                touchY = change.position.y
                                val width = size.width - 120f
                                val visibleCandles = (candles.size / zoomScale).toInt().coerceIn(10, candles.size)
                                val candleWidth = width / visibleCandles
                                val index = ((change.position.x - panOffset) / candleWidth).toInt().coerceIn(0, candles.lastIndex)
                                selectedCandleIndex = index
                            },
                            onDragEnd = {}
                        )
                    }
            ) {
                val fullCanvasWidth = size.width
                val canvasHeight = size.height
                val yAxisWidth = 110f
                val chartWidth = fullCanvasWidth - yAxisWidth

                // Determine Sub-chart height proportions
                val hasSubChart = indicators.showRsi || indicators.showMacd
                val mainPriceAreaHeight = if (hasSubChart) canvasHeight * 0.58f else canvasHeight * 0.76f
                val volumeAreaHeight = canvasHeight * 0.16f
                val subChartHeight = if (hasSubChart) canvasHeight * 0.22f else 0f

                val minPrice = candles.minOf { it.low }
                val maxPrice = candles.maxOf { it.high }
                val priceRange = if (maxPrice - minPrice > 0) maxPrice - minPrice else 1f

                val maxVolume = candles.maxOf { it.volume }
                val volumeRange = if (maxVolume > 0) maxVolume else 1f

                val visibleCandlesCount = (candles.size / zoomScale).toInt().coerceIn(10, candles.size)
                val stepX = chartWidth / visibleCandlesCount
                val candleBodyWidth = (stepX * 0.70f).coerceAtLeast(2f)

                val textPaint = Paint().apply {
                    color = TextMuted.toArgb()
                    textSize = 24f
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.MONOSPACE
                }

                val priceAxisPaint = Paint().apply {
                    color = TextPrimary.toArgb()
                    textSize = 26f
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }

                // 1. Draw Grid Lines & Price Labels on Y-axis
                val gridLines = 5
                for (i in 0..gridLines) {
                    val y = (mainPriceAreaHeight / gridLines) * i
                    drawLine(
                        color = SlateBorder.copy(alpha = 0.4f),
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )

                    // Draw price scale text on Y-axis
                    val priceAtY = maxPrice - (i.toFloat() / gridLines * priceRange)
                    val formattedPrice = when {
                        priceAtY >= 1000 -> String.format("%.1f", priceAtY)
                        priceAtY >= 1 -> String.format("%.4f", priceAtY)
                        else -> String.format("%.6f", priceAtY)
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        formattedPrice,
                        chartWidth + 12f,
                        y + 8f,
                        textPaint
                    )
                }

                // Price to Y coordinate mapping
                fun priceToY(price: Float): Float {
                    val ratio = (price - minPrice) / priceRange
                    return mainPriceAreaHeight - (ratio * mainPriceAreaHeight)
                }

                // 2. Draw Technical Indicators (Bollinger Bands / EMAs)
                if (indicators.showBollingerBands) {
                    val bb = calculateBollingerBands(candles, 20, 2f)
                    if (bb.upper.size == candles.size) {
                        drawTrendLine(bb.upper, stepX, panOffset, ::priceToY, PurpleAccent)
                        drawTrendLine(bb.lower, stepX, panOffset, ::priceToY, PurpleAccent)
                        drawTrendLine(bb.middle, stepX, panOffset, ::priceToY, PurpleAccent.copy(alpha = 0.6f))
                    }
                }

                val ema20List = calculateEMA(candles, 20)
                val ema50List = calculateEMA(candles, 50)

                if (indicators.showEma20 && ema20List.size == candles.size) {
                    drawTrendLine(ema20List, stepX, panOffset, ::priceToY, AmberGold)
                }
                if (indicators.showEma50 && ema50List.size == candles.size) {
                    drawTrendLine(ema50List, stepX, panOffset, ::priceToY, CyanAccent)
                }

                // 3. Draw Candlesticks & Volume
                candles.forEachIndexed { i, candle ->
                    val xCenter = panOffset + (i * stepX) + (stepX / 2f)

                    if (xCenter >= -stepX && xCenter <= chartWidth + stepX) {
                        val highY = priceToY(candle.high)
                        val lowY = priceToY(candle.low)
                        val openY = priceToY(candle.open)
                        val closeY = priceToY(candle.close)

                        val candleColor = if (candle.isBullish) NeonGreen else CrimsonRed

                        if (indicators.isCandlestick) {
                            // Wick
                            drawLine(
                                color = candleColor,
                                start = Offset(xCenter, highY),
                                end = Offset(xCenter, lowY),
                                strokeWidth = 1.5f
                            )

                            // Body
                            val topY = minOf(openY, closeY)
                            val bodyHeight = maxOf(Math.abs(openY - closeY), 2f)

                            drawRect(
                                color = candleColor,
                                topLeft = Offset(xCenter - (candleBodyWidth / 2f), topY),
                                size = Size(candleBodyWidth, bodyHeight)
                            )
                        } else {
                            // Line Chart
                            if (i > 0) {
                                val prevCandle = candles[i - 1]
                                val prevX = panOffset + ((i - 1) * stepX) + (stepX / 2f)
                                val prevY = priceToY(prevCandle.close)
                                drawLine(
                                    color = CyanAccent,
                                    start = Offset(prevX, prevY),
                                    end = Offset(xCenter, closeY),
                                    strokeWidth = 3f
                                )
                            }
                        }

                        // Volume Bar
                        val volRatio = candle.volume / volumeRange
                        val volBarHeight = volRatio * volumeAreaHeight
                        val volBarTop = mainPriceAreaHeight + volumeAreaHeight - volBarHeight

                        drawRect(
                            color = candleColor.copy(alpha = 0.35f),
                            topLeft = Offset(xCenter - (candleBodyWidth / 2f), volBarTop),
                            size = Size(candleBodyWidth, volBarHeight)
                        )
                    }
                }

                // 4. Draw RSI / MACD Sub-Chart Pane if active
                if (indicators.showRsi) {
                    val rsiTop = mainPriceAreaHeight + volumeAreaHeight + 10f
                    val rsiList = calculateRSI(candles, 14)

                    // Sub-chart boundary box
                    drawRect(
                        color = DarkSurfaceElevated,
                        topLeft = Offset(0f, rsiTop),
                        size = Size(chartWidth, subChartHeight)
                    )

                    // Threshold lines (30, 70)
                    val rsi30Y = rsiTop + subChartHeight - (0.3f * subChartHeight)
                    val rsi70Y = rsiTop + subChartHeight - (0.7f * subChartHeight)

                    drawLine(
                        color = CrimsonRed.copy(alpha = 0.6f),
                        start = Offset(0f, rsi70Y),
                        end = Offset(chartWidth, rsi70Y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )
                    drawLine(
                        color = NeonGreen.copy(alpha = 0.6f),
                        start = Offset(0f, rsi30Y),
                        end = Offset(chartWidth, rsi30Y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )

                    drawContext.canvas.nativeCanvas.drawText("RSI (14)", 12f, rsiTop + 28f, textPaint)

                    // Draw RSI Curve
                    val rsiPath = Path()
                    var rsiStarted = false
                    rsiList.forEachIndexed { idx, valF ->
                        val x = panOffset + (idx * stepX) + (stepX / 2f)
                        val y = rsiTop + subChartHeight - ((valF / 100f) * subChartHeight)
                        if (x in -stepX..chartWidth + stepX) {
                            if (!rsiStarted) {
                                rsiPath.moveTo(x, y)
                                rsiStarted = true
                            } else {
                                rsiPath.lineTo(x, y)
                            }
                        }
                    }
                    drawPath(path = rsiPath, color = AmberGold, style = Stroke(width = 2f))
                } else if (indicators.showMacd) {
                    val macdTop = mainPriceAreaHeight + volumeAreaHeight + 10f
                    val macdData = calculateMACD(candles)

                    drawRect(
                        color = DarkSurfaceElevated,
                        topLeft = Offset(0f, macdTop),
                        size = Size(chartWidth, subChartHeight)
                    )
                    drawContext.canvas.nativeCanvas.drawText("MACD (12,26,9)", 12f, macdTop + 28f, textPaint)

                    val zeroY = macdTop + (subChartHeight / 2f)
                    drawLine(
                        color = SlateBorder,
                        start = Offset(0f, zeroY),
                        end = Offset(chartWidth, zeroY),
                        strokeWidth = 1f
                    )

                    // Draw MACD Histogram
                    candles.forEachIndexed { idx, _ ->
                        val x = panOffset + (idx * stepX) + (stepX / 2f)
                        if (x in -stepX..chartWidth + stepX && idx < macdData.histogram.size) {
                            val histVal = macdData.histogram[idx]
                            val barH = (histVal / 500f) * (subChartHeight / 2f)
                            val color = if (histVal >= 0) NeonGreen else CrimsonRed
                            drawRect(
                                color = color.copy(alpha = 0.6f),
                                topLeft = Offset(x - (candleBodyWidth / 2f), if (histVal >= 0) zeroY - barH else zeroY),
                                size = Size(candleBodyWidth, Math.abs(barH))
                            )
                        }
                    }
                }

                // 5. Draw Interactive Order Lines (TP / SL) with Labels
                takeProfitPrice?.let { tp ->
                    val tpY = priceToY(tp.toFloat())
                    drawLine(
                        color = NeonGreen,
                        start = Offset(0f, tpY),
                        end = Offset(chartWidth, tpY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f))
                    )
                    drawRect(
                        color = NeonGreen,
                        topLeft = Offset(chartWidth, tpY - 14f),
                        size = Size(yAxisWidth, 28f)
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        "TP $tp",
                        chartWidth + 6f,
                        tpY + 8f,
                        Paint().apply { color = DarkObsidian.toArgb(); textSize = 22f; isAntiAlias = true; typeface = android.graphics.Typeface.DEFAULT_BOLD }
                    )
                }

                stopLossPrice?.let { sl ->
                    val slY = priceToY(sl.toFloat())
                    drawLine(
                        color = CrimsonRed,
                        start = Offset(0f, slY),
                        end = Offset(chartWidth, slY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f))
                    )
                    drawRect(
                        color = CrimsonRed,
                        topLeft = Offset(chartWidth, slY - 14f),
                        size = Size(yAxisWidth, 28f)
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        "SL $sl",
                        chartWidth + 6f,
                        slY + 8f,
                        Paint().apply { color = TextPrimary.toArgb(); textSize = 22f; isAntiAlias = true; typeface = android.graphics.Typeface.DEFAULT_BOLD }
                    )
                }

                // 6. Crosshair Line & Badge on Touch
                touchX?.let { tx ->
                    touchY?.let { ty ->
                        if (tx <= chartWidth) {
                            drawLine(
                                color = TextSecondary,
                                start = Offset(tx, 0f),
                                end = Offset(tx, canvasHeight),
                                strokeWidth = 1f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                            )
                            drawLine(
                                color = TextSecondary,
                                start = Offset(0f, ty),
                                end = Offset(chartWidth, ty),
                                strokeWidth = 1f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                            )

                            // Crosshair Price Badge
                            val priceAtTouch = maxPrice - (ty / mainPriceAreaHeight * priceRange)
                            drawRect(
                                color = DarkSurfaceElevated,
                                topLeft = Offset(chartWidth, ty - 14f),
                                size = Size(yAxisWidth, 28f)
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                String.format("%.2f", priceAtTouch),
                                chartWidth + 6f,
                                ty + 8f,
                                priceAxisPaint
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawTrendLine(
    values: List<Float>,
    stepX: Float,
    panOffset: Float,
    priceToY: (Float) -> Float,
    color: Color
) {
    val path = Path()
    var started = false

    values.forEachIndexed { i, valF ->
        if (valF > 0) {
            val x = panOffset + (i * stepX) + (stepX / 2f)
            val y = priceToY(valF)
            if (!started) {
                path.moveTo(x, y)
                started = true
            } else {
                path.lineTo(x, y)
            }
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 2.5f)
    )
}

private fun calculateEMA(candles: List<Candle>, period: Int): List<Float> {
    if (candles.size < period) return List(candles.size) { 0f }
    val result = MutableList(candles.size) { 0f }
    val k = 2f / (period + 1)

    var sma = 0f
    for (i in 0 until period) {
        sma += candles[i].close
    }
    sma /= period
    result[period - 1] = sma

    var prevEma = sma
    for (i in period until candles.size) {
        val currentEma = (candles[i].close * k) + (prevEma * (1 - k))
        result[i] = currentEma
        prevEma = currentEma
    }
    return result
}

private data class BollingerBandsResult(
    val upper: List<Float>,
    val middle: List<Float>,
    val lower: List<Float>
)

private fun calculateBollingerBands(candles: List<Candle>, period: Int, multiplier: Float): BollingerBandsResult {
    val upper = MutableList(candles.size) { 0f }
    val middle = MutableList(candles.size) { 0f }
    val lower = MutableList(candles.size) { 0f }

    if (candles.size < period) return BollingerBandsResult(upper, middle, lower)

    for (i in period - 1 until candles.size) {
        val slice = candles.subList(i - period + 1, i + 1)
        val mean = slice.map { it.close }.average().toFloat()
        val variance = slice.map { Math.pow((it.close - mean).toDouble(), 2.0) }.average().toFloat()
        val stdDev = sqrt(variance)

        middle[i] = mean
        upper[i] = mean + (multiplier * stdDev)
        lower[i] = mean - (multiplier * stdDev)
    }

    return BollingerBandsResult(upper, middle, lower)
}

private fun calculateRSI(candles: List<Candle>, period: Int): List<Float> {
    val result = MutableList(candles.size) { 50f }
    if (candles.size <= period) return result

    var gains = 0f
    var losses = 0f

    for (i in 1..period) {
        val diff = candles[i].close - candles[i - 1].close
        if (diff >= 0) gains += diff else losses -= diff
    }

    var avgGain = gains / period
    var avgLoss = losses / period

    for (i in period until candles.size) {
        val diff = candles[i].close - candles[i - 1].close
        if (diff >= 0) {
            avgGain = (avgGain * (period - 1) + diff) / period
            avgLoss = (avgLoss * (period - 1)) / period
        } else {
            avgGain = (avgGain * (period - 1)) / period
            avgLoss = (avgLoss * (period - 1) - diff) / period
        }

        val rs = if (avgLoss == 0f) 100f else avgGain / avgLoss
        val rsi = 100f - (100f / (1f + rs))
        result[i] = rsi
    }

    return result
}

private data class MacdResult(
    val macdLine: List<Float>,
    val signalLine: List<Float>,
    val histogram: List<Float>
)

private fun calculateMACD(candles: List<Candle>): MacdResult {
    val ema12 = calculateEMA(candles, 12)
    val ema26 = calculateEMA(candles, 26)

    val macdLine = MutableList(candles.size) { 0f }
    for (i in candles.indices) {
        macdLine[i] = ema12[i] - ema26[i]
    }

    // Signal line is 9 EMA of MACD line
    val signalLine = MutableList(candles.size) { 0f }
    val k = 2f / (9 + 1)
    var prevSignal = macdLine.firstOrNull() ?: 0f

    for (i in candles.indices) {
        val currSignal = (macdLine[i] * k) + (prevSignal * (1 - k))
        signalLine[i] = currSignal
        prevSignal = currSignal
    }

    val histogram = MutableList(candles.size) { 0f }
    for (i in candles.indices) {
        histogram[i] = macdLine[i] - signalLine[i]
    }

    return MacdResult(macdLine, signalLine, histogram)
}

