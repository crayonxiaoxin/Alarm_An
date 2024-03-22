package com.github.crayonxiaoxin.alarmclock.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import com.github.crayonxiaoxin.alarmclock.BuildConfig
import com.github.crayonxiaoxin.alarmclock.R
import com.github.crayonxiaoxin.alarmclock.data.Repository
import com.github.crayonxiaoxin.alarmclock.databinding.DialogAlarmBinding
import com.github.crayonxiaoxin.alarmclock.model.Alarm
import com.github.crayonxiaoxin.alarmclock.receiver.AlarmReceiver
import com.github.crayonxiaoxin.alarmclock.ui.MainActivity
import com.github.crayonxiaoxin.alarmclock.utils.AudioManager
import com.github.crayonxiaoxin.alarmclock.utils.NotificationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AlarmService : Service(), IAlarmService {

    val alarmServiceScope = CoroutineScope(Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder {
        return AlarmBinder(this)
    }

    class AlarmBinder(private val service: Service) : Binder() {
        val mService get() = service
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        configForeground()  // 前台服务
        configReceiver()    // 广播接收
        return START_STICKY
    }

    private fun configForeground() {
        val pi = NotificationUtil.pendingIntent(
            intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        )
        val notification = NotificationUtil.show(
            title = "前台服务运行中",
            autoCancel = false,
            contentIntent = pi,
            show = false
        )
        startForeground(BuildConfig.VERSION_CODE, notification)
    }

    private val alarmReceiver = object : AlarmReceiver() {
        private val TAG = "AlarmReceiver"

        override fun onReceive(context: Context?, intent: Intent?) {
            Log.e(TAG, "onReceive: $intent")
            if (context == null || intent == null) return
            when (intent.action) {
                ACTION_ALARM_CLOCK -> {
                    Log.e(TAG, "onReceive: 收到设置的闹钟")
                    val alarmId = intent.getIntExtra(EXTRA_ALARM_ID, 0)
                    alarmServiceScope.launch {
                        val alarm = Repository.alarmDao.get(alarmId) ?: return@launch
                        // 发出通知
                        val pendingIntent = NotificationUtil.pendingIntent(Intent(
                            context, MainActivity::class.java
                        ).apply {
                            this.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            this.action = ACTION_ALARM_CLOCK + "_receive"
                        })
                        NotificationUtil.show(
                            getString(R.string.app_name),
                            alarm.content(),
                            alarm.requestCode(),
                            contentIntent = pendingIntent
                        )
                        // 获取音乐 uri
                        val musicUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(
                                EXTRA_ALARM_MUSIC_URI,
                                Uri::class.java
                            )
                        } else {
                            intent.getParcelableExtra(EXTRA_ALARM_MUSIC_URI) as? Uri
                        }
                        Log.e(TAG, "onReceive: uri => $musicUri" )
                        // 播放音乐
                        musicUri?.let {
                            AudioManager.playMp3FromUri(context, musicUri, true)
                        }
                        // 做 callback 提醒 ui
                        onAlarmTriggered(alarm)
                    }

                }

                else -> {
                    Log.e(TAG, "$intent")
                }
            }

        }
    }

    private fun configReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(AlarmReceiver.ACTION_ALARM_CLOCK)
            addCategory("android.intent.category.DEFAULT")
            priority = Int.MAX_VALUE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(alarmReceiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(alarmReceiver, intentFilter)
        }
    }

    override fun onDestroy() {
        unregisterReceiver(alarmReceiver)
        super.onDestroy()
    }

    override fun onAlarmTriggered(alarm: Alarm) {
        alarmServiceScope.launch(Dispatchers.Main) {
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            windowManager?.let {
                val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                }
                val layoutParams = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    type,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
                val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
                inflater?.let {
                    val binding = DialogAlarmBinding.inflate(it)
                    binding.title.text = getString(R.string.app_name)
                    binding.content.text = alarm.content()
                    binding.cancel.setOnClickListener {
                        windowManager.removeView(binding.root)
                        alarmServiceScope.launch {
                            Repository.updateAlarm(this@AlarmService, alarm.copy(enable = 0))
                        }
                    }
                    windowManager.addView(binding.root, layoutParams)
                }
            }
        }
    }
}