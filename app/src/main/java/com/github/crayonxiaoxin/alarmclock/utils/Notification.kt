package com.github.crayonxiaoxin.alarmclock.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.crayonxiaoxin.alarmclock.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class PendingType {
    ACTIVITY,
    BROADCAST,
    SERVICE,
    FOREGROUND_SERVICE
}

@SuppressLint("StaticFieldLeak")
object NotificationUtil {

    const val CHANNEL_ID = "AlarmClock-v1"
    const val CHANNEL_NAME = "AlarmClock-v1"
    const val CHANNEL_DESC = "AlarmClock 通知"
    lateinit var context: Context

    /**
     * 初始化，创建通知渠道
     *
     * 必须先初始化才能使用，否则 context 空指针，可以在 Application 里初始化
     *
     * @Author: Lau
     * @Date: 2022/6/6 6:14 下午
     */
    fun provide(
        context: Context,
        @SuppressLint("InlinedApi") importance: Int = NotificationManager.IMPORTANCE_HIGH
    ) {
        this.context = context
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val mChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            mChannel.description = CHANNEL_DESC
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    /**
     * 删除通知渠道
     *
     * 如果需要更新 importance，先执行删除
     *
     * @Author: Lau
     * @Date: 2022/6/7 10:12 上午
     */
    fun clear(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.deleteNotificationChannel(CHANNEL_ID)
        }
    }

    /**
     * 获取 PendingIntent
     *
     * val intent = Intent(context, MainActivity::class.java).apply {
     *      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
     * }
     *
     * @Author: Lau
     * @Date: 2022/6/6 6:13 下午
     */
    fun pendingIntent(
        intent: Intent,
        type: PendingType = PendingType.ACTIVITY,
        flag: Int = PendingIntent.FLAG_ONE_SHOT
    ): PendingIntent {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or flag
        } else flag
        return when (type) {
            PendingType.ACTIVITY -> PendingIntent.getActivity(context, 0, intent, flag)
            PendingType.BROADCAST -> PendingIntent.getBroadcast(context, 0, intent, flag)
            PendingType.SERVICE -> PendingIntent.getService(context, 0, intent, flag)
            PendingType.FOREGROUND_SERVICE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    PendingIntent.getForegroundService(context, 0, intent, flag)
                } else {
                    PendingIntent.getService(context, 0, intent, flag)
                }
            }
        }
    }


    /**
     * 普通通知
     *
     * @Author: Lau
     * @Date: 2022/6/7 10:57 上午
     */
    fun show(
        title: String,
        content: String = "",
        notifyID: Int = 0,
        priority: Int = NotificationCompat.PRIORITY_HIGH,
        autoCancel: Boolean = true,
        contentIntent: PendingIntent? = null,
        okTitle: String = "OK",
        okCancel: String = "Cancel",
        okIntent: PendingIntent? = null,
        cancelIntent: PendingIntent? = null,
        show: Boolean = true,
        showDuration: Long = 0L
    ): Notification {
        val icon = R.mipmap.ic_launcher
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(priority)
            .setContentIntent(contentIntent)
            .setAutoCancel(autoCancel)
        if (okIntent != null || cancelIntent != null) {
            builder.addAction(icon, okTitle, okIntent)
                .addAction(icon, okCancel, cancelIntent)
        }
        val notification = builder.build()
        if (show) {
            with(NotificationManagerCompat.from(context)) {
                notify(notifyID, notification)
            }
            if (showDuration > 0) {
                CoroutineScope(Dispatchers.Default).launch {
                    delay(showDuration)
                    hide(notifyID)
                }
            }
        }
        return notification
    }

    /**
     * 取消通知
     *
     * @Author: Lau
     * @Date: 2022/6/7 11:08 上午
     */
    fun hide(notifyID: Int = 0) {
        with(NotificationManagerCompat.from(context)) {
            cancel(notifyID)
        }
    }
}