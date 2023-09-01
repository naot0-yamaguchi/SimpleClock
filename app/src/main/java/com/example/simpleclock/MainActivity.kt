package com.example.simpleclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simpleclock.ui.theme.SimpleClockTheme
import kotlinx.coroutines.delay
import java.util.Calendar

class MainActivity : ComponentActivity() {
    private val digitalClockViewModel: DigitalClockViewModel by viewModels()
    private val analogClockViewModel: AnalogClockViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleClockTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ClockScreen(digitalClockViewModel, analogClockViewModel)
                }
            }
        }
    }
}

@Composable
fun ClockScreen(digitalClockViewModel: DigitalClockViewModel, analogClockViewModel: AnalogClockViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        DigitalClock(digitalClockViewModel)
        AnalogClock()
    }
}

@Composable
fun DigitalClock(viewModel: DigitalClockViewModel) {
    val currentTime by viewModel.currentFormattedTime

    Text(
        text = currentTime,
        modifier = Modifier.padding(16.dp),
        style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
    )
}

@Composable
fun AnalogClock() {
    // メモリの使用量を減らすために、System.currentTimeMillis() の値をLaunchedEffect内で更新する
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(currentTimeMillis) {
        // 1秒ごとに時間を更新
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    Canvas(
        modifier = Modifier.fillMaxSize(),
        onDraw = {
            // アナログ時計の背景を描画
            drawCircle(
                color = Color.Gray,
                radius = size.minDimension / 2f,
                center = center
            )

            // 現在の時間から針の角度を計算
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = currentTimeMillis
            val hours = calendar.get(Calendar.HOUR)
            val minutes = calendar.get(Calendar.MINUTE)
            val seconds = calendar.get(Calendar.SECOND)

            val hoursAngle = 360f * (hours + minutes / 60f) / 12f - 90
            val minutesAngle = 360f * minutes / 60f + 360f * (seconds / 60f) / 60f - 90
            val secondsAngle = 360f * seconds / 60f - 90

            // 時針を描画
            drawLine(
                color = Color.Black,
                start = center,
                end = center + polarToCartesian(hoursAngle, size.minDimension / 3f),
                strokeWidth = 10f
            )

            // 分針を描画
            drawLine(
                color = Color.Black,
                start = center,
                end = center + polarToCartesian(minutesAngle, size.minDimension / 2.5f),
                strokeWidth = 5f
            )

            // 秒針を描画
            drawLine(
                color = Color.Red,
                start = center,
                end = center + polarToCartesian(secondsAngle, size.minDimension / 2.5f),
                strokeWidth = 2f
            )

            // 時針用のラベルを描画
            val labelRadius = size.minDimension / 2.7f
            val labelOffset = size.minDimension / 30f
            for (i in 0 until 12) {
                val labelAngle = 360f * (i / 12f) - 90f // ラベルの角度を調整
                val xOffset = kotlin.math.cos(Math.toRadians(labelAngle.toDouble())).toFloat() * labelRadius
                val yOffset = kotlin.math.sin(Math.toRadians(labelAngle.toDouble())).toFloat() * labelRadius
                val xTextOffset = kotlin.math.cos(Math.toRadians(labelAngle.toDouble())).toFloat() * (labelRadius - labelOffset)
                val yTextOffset = kotlin.math.sin(Math.toRadians(labelAngle.toDouble())).toFloat() * (labelRadius - labelOffset)

                drawCircle(
                    color = Color.Black,
                    radius = 10f,
                    center = center + Offset(xOffset, yOffset)
                )

                val label = (i).toString()
                drawIntoCanvas { canvas ->
                    val textPaint = android.graphics.Paint()
                    textPaint.color = Color.Black.toArgb()
                    textPaint.textSize = 24f
                    textPaint.textAlign = android.graphics.Paint.Align.CENTER
                    canvas.nativeCanvas.drawText(
                        label,
                        center.x + xTextOffset,
                        center.y + yTextOffset,
                        textPaint
                    )
                }
            }

            // 分針用の線を描画
            val minuteLineRadius = size.minDimension / 2.2f
            for (i in 0 until 60) {
                val minuteLineAngle = 360f * (i / 60f)
                val xOffset = kotlin.math.cos(Math.toRadians(minuteLineAngle.toDouble())).toFloat() * minuteLineRadius
                val yOffset = kotlin.math.sin(Math.toRadians(minuteLineAngle.toDouble())).toFloat() * minuteLineRadius

                drawLine(
                    color = Color.Black,
                    start = center + Offset(xOffset, yOffset),
                    end = center + Offset(xOffset / 1.2f, yOffset / 1.2f),
                    strokeWidth = 2f
                )
            }
        }
    )
}

private fun polarToCartesian(degrees: Float, radius: Float): Offset {
    val radians = Math.toRadians(degrees.toDouble())
    val x = (radius * kotlin.math.cos(radians)).toFloat()
    val y = (radius * kotlin.math.sin(radians)).toFloat()
    return Offset(x, y)
}
