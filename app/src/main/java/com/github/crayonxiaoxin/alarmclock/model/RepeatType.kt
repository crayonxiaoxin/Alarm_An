package com.github.crayonxiaoxin.alarmclock.model

import java.io.Serializable


data class RepeatType(
    var interval: Long,
    var label: String,
) : Serializable