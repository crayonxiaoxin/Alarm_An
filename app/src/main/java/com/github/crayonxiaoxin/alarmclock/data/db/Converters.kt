package com.github.crayonxiaoxin.alarmclock.data.db

import android.net.Uri
import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromUri2String(value: Uri?): String {
        return value?.toString() ?: ""
    }

    @TypeConverter
    fun string2Uri(value: String?): Uri? {
        if (value.isNullOrEmpty()) return null
        return Uri.parse(value)
    }

}