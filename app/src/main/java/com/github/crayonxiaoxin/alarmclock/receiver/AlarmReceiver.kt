package com.github.crayonxiaoxin.alarmclock.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import com.github.crayonxiaoxin.alarmclock.BuildConfig
import com.github.crayonxiaoxin.alarmclock.model.Alarm
import com.github.crayonxiaoxin.alarmclock.utils.AudioManager
import com.github.crayonxiaoxin.alarmclock.utils.NotificationUtil
import com.github.crayonxiaoxin.lib_common.global.toast
import java.util.Calendar

open class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"

        // 设备启动
        const val ACTION_BOOT = "android.intent.action.BOOT_COMPLETED"

        // 自定义
        const val ACTION_ALARM_CLOCK = BuildConfig.APPLICATION_ID + "." + "action_alarm_clock"

        const val EXTRA_ALARM_MUSIC_URI = "alarm_music_uri"
        const val EXTRA_ALARM_ID = "alarm_id"

        // 设置闹钟
        fun setAlarmClock(
            context: Context?,
            timestamp: Long = System.currentTimeMillis(),
            interval: Long = 1000 * 60 * 1,
            musicUri: Uri? = null,
            requestCode: Int = 0,
        ) {
            context?.let {
                val alarmManager = it.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                if (alarmManager != null) {
                    // 设置意图
                    val intent = Intent(it, AlarmReceiver::class.java).apply {
                        setPackage(it.packageName)
                        action = ACTION_ALARM_CLOCK
                        musicUri?.let {
                            putExtra(EXTRA_ALARM_ID, Alarm.idFromRequestCode(requestCode))
                            putExtra(EXTRA_ALARM_MUSIC_URI, musicUri)
                        }
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        it,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    // 如果意图已存在，先取消，防止重复设置
                    if (pendingIntent != null) {
                        alarmManager.cancel(pendingIntent)
                    }
                    // 根据实际情况设置 Alarm 类型
                    val calendar: Calendar = Calendar.getInstance().apply {
                        timeInMillis = timestamp
                    }

                    if (interval > 0) { // 重复执行
                        alarmManager.setRepeating(
                            AlarmManager.RTC_WAKEUP, // 可唤醒设备
                            calendar.timeInMillis, // 第一次调用
                            interval,// 重复调用的间隔
                            pendingIntent, // 意图
                        )
                    } else { // 只执行一次
                        val now = System.currentTimeMillis()
                        val next = calendar.timeInMillis
                        var delay = next - now
                        delay = if (delay > 0) delay else 0
                        val triggerAtTime = SystemClock.elapsedRealtime() + delay
                        // 保证低电耗模式运行
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent
                        )
                    }

                    Log.e(TAG, "setAlarm: ")
                    context.toast("已设置闹钟")
                }
            }
        }

        // 取消闹钟
        fun unsetAlarmClock(
            context: Context?,
            musicUri: Uri? = null,
            requestCode: Int = 0,
        ) {
            context?.let {
                val alarmManager = it.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                if (alarmManager != null) {
                    // 设置意图
                    val intent = Intent(it, AlarmReceiver::class.java).apply {
                        setPackage(it.packageName)
                        action = ACTION_ALARM_CLOCK
                        musicUri?.let {
                            putExtra(EXTRA_ALARM_ID, Alarm.idFromRequestCode(requestCode))
                            putExtra(EXTRA_ALARM_MUSIC_URI, musicUri)
                        }
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        it,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    // 如果意图已存在，先取消
                    if (pendingIntent != null) {
                        // 取消任务
                        alarmManager.cancel(pendingIntent)
                        // 取消音乐
                        musicUri?.let {
                            AudioManager.stopMp3()
                        }
                        // 取消通知
                        NotificationUtil.hide(requestCode)
                    }
                }
            }
        }
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        when (intent.action) {
            ACTION_BOOT -> {
                // Set the alarm here when the device boot.
            }

            ACTION_ALARM_CLOCK -> {
                Log.e(TAG, "onReceive: 收到设置的闹钟 - 转发")
                // 转发给动态注册的 receiver
                context.sendBroadcast(Intent().apply {
                    action = ACTION_ALARM_CLOCK
                    intent.extras?.let { putExtras(it) }
                })
            }

            else -> {
                Log.e(TAG, "$intent")
            }
        }

    }
}