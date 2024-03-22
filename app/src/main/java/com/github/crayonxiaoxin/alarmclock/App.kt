package com.github.crayonxiaoxin.alarmclock

import android.app.Application
import android.content.Context
import com.github.crayonxiaoxin.alarmclock.data.db.AlarmDatabase
import com.github.crayonxiaoxin.alarmclock.utils.NotificationUtil

class App : Application() {

    // 防止多个相同 db 实例
    private val database by lazy { AlarmDatabase.getDatabase(this) }

    companion object {
        lateinit var appContext: Context
        lateinit var db: AlarmDatabase
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this.applicationContext
        db = database
        NotificationUtil.provide(appContext)
    }
}