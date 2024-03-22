package com.github.crayonxiaoxin.alarmclock.service

import com.github.crayonxiaoxin.alarmclock.model.Alarm

interface IAlarmService {
    fun onAlarmTriggered(alarm: Alarm)
}