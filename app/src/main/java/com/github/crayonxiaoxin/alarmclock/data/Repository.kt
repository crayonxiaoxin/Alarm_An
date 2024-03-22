package com.github.crayonxiaoxin.alarmclock.data

import android.content.Context
import android.net.Uri
import com.github.crayonxiaoxin.alarmclock.App
import com.github.crayonxiaoxin.alarmclock.model.Alarm
import com.github.crayonxiaoxin.alarmclock.receiver.AlarmReceiver
import com.github.crayonxiaoxin.lib_common.global.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Repository {
    private val db = App.db
    val alarmDao = db.alarmDao()

    // 设置闹钟
    suspend fun setAlarm(
        context: Context?,
        timestamp: Long = 0, // 闹钟时间
        interval: Long = 0, // 重复间隔
        uri: Uri?, // 音乐地址
        enable: Boolean = true, // 是否启用
        callback: () -> Unit = {}
    ) {
        // 记录在数据库中
        val insertIds = alarmDao.insert(
            Alarm(
                timestamp = timestamp,
                interval = interval,
                uri = uri.toString(),
                enable = if (enable) 1 else 0
            )
        )
        if (insertIds.isNotEmpty()) {
            val alarm = alarmDao.get(insertIds[0].toInt())
            if (alarm != null) {
                val requestCode = alarm.requestCode()
                val now = System.currentTimeMillis()
                val newTimestamp = if (alarm.timestamp < now && interval > 0) {
                    // 已过期且是重复闹钟，设置下一个周期
                    timestamp + interval
                } else timestamp
                // 设置闹钟
                AlarmReceiver.setAlarmClock(
                    context = context,
                    timestamp = newTimestamp,
                    interval = interval,
                    musicUri = uri,
                    requestCode = requestCode,
                )
                // 返回
                withContext(Dispatchers.Main) {
                    callback.invoke()
                }
            }
        } else {
            context.toast("未知错误")
        }
    }

    // 更新闹钟
    suspend fun updateAlarm(context: Context?, alarm: Alarm) {
        // 更新 Alarm
        alarmDao.update(alarm)
        if (alarm.isEnable()) {
            val now = System.currentTimeMillis()
            if (alarm.timestamp >= now) { // 超过当前时间，即：未过期
                // 设置闹钟
                AlarmReceiver.setAlarmClock(
                    context = context,
                    timestamp = alarm.timestamp,
                    interval = alarm.interval,
                    musicUri = alarm.toUri(),
                    requestCode = alarm.requestCode(),
                )
            } else if (alarm.interval > 0) { // 已过期，但是是重复闹钟
                // 设置闹钟，从下一个周期开始
                AlarmReceiver.setAlarmClock(
                    context = context,
                    timestamp = alarm.timestamp + alarm.interval,
                    interval = alarm.interval,
                    musicUri = alarm.toUri(),
                    requestCode = alarm.requestCode(),
                )
            } else {
                cancelAlarm(context, alarm)
            }
        } else {
            cancelAlarm(context, alarm)
        }
    }

    // 取消闹钟并停止音乐
    private fun cancelAlarm(
        context: Context?,
        alarm: Alarm,
    ) {
        AlarmReceiver.unsetAlarmClock(
            context = context,
            musicUri = alarm.toUri(),
            requestCode = alarm.requestCode()
        )
    }

    // 仅取消通知和音乐
    fun removeNotificationAndMusicOnly(alarm: Alarm) {
        AlarmReceiver.removeNotify(
            musicUri = alarm.toUri(),
            requestCode = alarm.requestCode()
        )
    }

    // 删除闹钟
    suspend fun unsetAlarm(context: Context?, alarm: Alarm) {
        cancelAlarm(context, alarm)
        // 删除记录
        alarmDao.delete(alarm)
        context.toast("已取消闹钟")
    }
}