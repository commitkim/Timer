package com.example.timer

interface TimerListener {
    fun onTimerStart(time: Long)

    fun onTimerStop()
}