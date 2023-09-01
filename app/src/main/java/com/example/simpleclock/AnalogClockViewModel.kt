package com.example.simpleclock

import android.icu.text.SimpleDateFormat
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class AnalogClockViewModel : ViewModel() {
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val currentTime = mutableStateOf(timeFormat.format(Date()))

    val currentFormattedTime: State<String> = currentTime

    init {
        updateCurrentTime()
    }

    private fun updateCurrentTime() {
        // UIスレッドでコードを実行するためにMainScopeを使用
        val mainScope = MainScope()
        mainScope.launch(Dispatchers.Default) {
            while (true) {
                // バックグラウンドで時間を更新
                val newTime = timeFormat.format(Date())
                // UIスレッドでUIを更新
                mainScope.launch(Dispatchers.Main) {
                    currentTime.value = newTime
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }
}
