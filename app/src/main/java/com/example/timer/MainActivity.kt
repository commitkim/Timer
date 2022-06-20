package com.example.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.timer.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity(), TimerListener {

    private val handler: Handler = Handler(Looper.getMainLooper())

    private val timerTask: Runnable by lazy {
        object : Runnable {
            override fun run() {
                if (binding.timerView.isTimerEnd()) return
                else handler.postDelayed(this, 1000)
            }
        }
    }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val alarmManager by lazy { this.getSystemService(Context.ALARM_SERVICE) as AlarmManager }

    private val pendingIntent by lazy {
        PendingIntent.getBroadcast(
            this, AlarmReceiver.NOTIFICATION_ID, Intent(this, AlarmReceiver::class.java),
            PendingIntent.FLAG_MUTABLE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.timerView.listener = this
        setContentView(binding.root)
    }

    override fun onTimerStart(time: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                Calendar.getInstance().timeInMillis + time,
                pendingIntent
            )
        }
        startTimer()
    }

    override fun onTimerStop() {
        alarmManager.cancel(pendingIntent)
        handler.removeCallbacksAndMessages(null)
    }

    private fun startTimer() {
        handler.post(timerTask)
    }

}
