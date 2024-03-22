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
                // 设置闹钟
                AlarmReceiver.setAlarmClock(
                    context = context,
                    timestamp = timestamp,
                    interval = 0,
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
        val requestCode = alarm.requestCode()
        // 更新 Alarm
        alarmDao.update(alarm)
        if (alarm.isEnable()) {
            val now = System.currentTimeMillis()
            if (alarm.timestamp >= now) {
                // 设置闹钟
                AlarmReceiver.setAlarmClock(
                    context = context,
                    timestamp = alarm.timestamp,
                    interval = alarm.interval,
                    musicUri = alarm.toUri(),
                    requestCode = requestCode,
                )
            } else {
                cancelAlarm(context, alarm, requestCode)
            }
        } else {
            cancelAlarm(context, alarm, requestCode)
        }
    }

    // 取消闹钟并停止音乐
    private fun cancelAlarm(
        context: Context?,
        alarm: Alarm,
        requestCode: Int
    ) {
        AlarmReceiver.unsetAlarmClock(
            context = context,
            musicUri = alarm.toUri(),
            requestCode = requestCode
        )
    }

    // 删除闹钟
    suspend fun unsetAlarm(context: Context?, alarm: Alarm) {
        val requestCode = alarm.requestCode()
        cancelAlarm(context, alarm, requestCode)
        // 删除记录
        alarmDao.delete(alarm)
        context.toast("已取消闹钟")
    }
}