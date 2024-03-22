package com.github.crayonxiaoxin.alarmclock.model

import android.content.Context
import android.net.Uri
import androidx.annotation.IntRange
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.crayonxiaoxin.alarmclock.utils.getFileNameFromUri
import com.github.crayonxiaoxin.alarmclock.utils.timestamp2DateFmt
import java.io.Serializable
import java.net.URLDecoder

@Entity
data class Alarm(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var timestamp: Long = 0, // 闹钟时间
    var interval: Long = 0, // 重复间隔
    var uri: String?, // 音乐地址
    @IntRange(from = 0, to = 1) var enable: Int,// 是否启用
) : Serializable {
    fun datetime(): String = timestamp.timestamp2DateFmt("yyyy-MM-dd HH:mm")
    fun date(): String = timestamp.timestamp2DateFmt("yyyy-MM-dd")
    fun time(): String = timestamp.timestamp2DateFmt("HH:mm")
    fun toUri(): Uri? {
        return if (uri.isNullOrEmpty()) {
            null
        } else {
//            Uri.parse(URLDecoder.decode(uri, "utf-8"))
            Uri.parse(uri)
//            Uri.parse(Uri.decode(uri))
        }
    }

    fun music(context: Context): String = toUri()?.let { context.getFileNameFromUri(it) } ?: ""
    fun isEnable(): Boolean = enable == 1
    fun requestCode(): Int = id + requestCodeDiff()
    fun content(): String = "已到达提醒时间 ${datetime()}"

    companion object {
        private fun requestCodeDiff(): Int = 1000
        fun idFromRequestCode(requestCode: Int): Int {
            val id = requestCode - requestCodeDiff()
            if (id > 0) return id
            return 0
        }
    }
}
