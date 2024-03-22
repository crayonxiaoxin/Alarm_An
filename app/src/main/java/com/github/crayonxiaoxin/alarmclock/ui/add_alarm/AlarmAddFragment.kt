package com.github.crayonxiaoxin.alarmclock.ui.add_alarm

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.crayonxiaoxin.alarmclock.base.BaseFragment
import com.github.crayonxiaoxin.alarmclock.data.Repository
import com.github.crayonxiaoxin.alarmclock.databinding.FragmentAlarmAddBinding
import com.github.crayonxiaoxin.alarmclock.databinding.PopupRepeatBinding
import com.github.crayonxiaoxin.alarmclock.model.Alarm
import com.github.crayonxiaoxin.alarmclock.model.RepeatType
import com.github.crayonxiaoxin.alarmclock.ui.navigate
import com.github.crayonxiaoxin.alarmclock.utils.FileUtil
import com.github.crayonxiaoxin.alarmclock.utils.copyFile
import com.github.crayonxiaoxin.alarmclock.utils.dateStr2Date
import com.github.crayonxiaoxin.alarmclock.utils.fmt
import com.github.crayonxiaoxin.alarmclock.utils.getFileNameFromUri
import com.github.crayonxiaoxin.alarmclock.utils.toFixed
import com.github.crayonxiaoxin.lib_common.global.toast
import com.github.crayonxiaoxin.lib_common.utils.dp
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AlarmAddFragment : BaseFragment() {

    companion object {
        private const val EXTRA_ALARM_OBJ = "extra_alarm_object"
        fun newInstance(alarm: Alarm): AlarmAddFragment {
            return AlarmAddFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_ALARM_OBJ, alarm)
                }
            }
        }
    }

    private lateinit var binding: FragmentAlarmAddBinding

    private var currentUri: Uri? = null
    private var currentDate: String = ""
    private var currentTime: String = ""
    private var currentInterval: Long = 0L

    private var alarm: Alarm? = null

    private val pickFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        it?.let {
            currentUri = it
            val name = context.getFileNameFromUri(it)
            Log.e(TAG, "pick uri: $it,${FileUtil.getFileName(requireContext(),it)}")
            binding.music.setContent(name)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlarmAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 判断是修改还是添加
        arguments?.let {
            alarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable(EXTRA_ALARM_OBJ, Alarm::class.java)
            } else {
                it.getSerializable(EXTRA_ALARM_OBJ) as? Alarm
            }
            alarm?.let {
                currentDate = it.date()
                currentTime = it.time()
                currentUri = it.toUri()
                currentInterval = it.interval
                currentUri?.let {
                    val name = context.getFileNameFromUri(it)
                    binding.music.setContent(name)
                }
            }
        }

        binding.title.text = if (alarm == null) {
            "添加闹钟"
        } else {
            "修改闹钟"
        }

        initDateTimePicker()
        initMusicPicker()
        initRepeatPicker()

        binding.close.setOnClickListener {
            navigate?.back()
        }

        binding.save.setOnClickListener {
            if (currentDate.isNotEmpty() and currentTime.isNotEmpty() and (currentUri != null)) {
                val datetime = "$currentDate $currentTime"
                val date = datetime.dateStr2Date(
                    fmt = "yyyy-MM-dd HH:mm"
                )
                val timestamp = date?.time ?: 0
                // 可为 interval 赋值，拓展为重复闹钟
                val interval = currentInterval
                if (timestamp == 0L) {
                    toast("非法的日期时间")
                } else {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (alarm != null) {
                            val now = System.currentTimeMillis()
                            // 如果与之前的不同，才需要保存到 local
                            if (currentUri != alarm!!.toUri()) {
                                saveFile2Local()
                            }
                            // 更新闹钟
                            Repository.updateAlarm(
                                context = context,
                                alarm = alarm!!.copy(
                                    timestamp = timestamp,
                                    interval = interval,
                                    uri = currentUri.toString(),
                                    enable = if (timestamp >= now || interval > 0) 1 else 0
                                ),
                            )
                            // 返回
                            withContext(Dispatchers.Main) {
                                navigate?.back()
                            }
                        } else {
                            // 保存到 local
                            saveFile2Local()
                            // 设置闹钟
                            Repository.setAlarm(
                                context = context,
                                timestamp = timestamp,
                                interval = interval,
                                uri = currentUri,
                                enable = true,
                            ) {
                                // 返回
                                navigate?.back()
                            }
                        }

                    }
                }
            } else {
                toast("请补充必要的资料")
            }
        }
    }


    // 将文件保存到 app data 中，防止 uri 权限过期
    private fun saveFile2Local() {
        context?.let {
            currentUri = it.copyFile(currentUri) ?: currentUri
        }
    }

    // 选择重复类型
    private fun initRepeatPicker() {
        binding.repeat.setContent(Alarm.repeatType(currentInterval))
        val popupBinding = PopupRepeatBinding.inflate(layoutInflater, null, false)
        val popupWindow = PopupWindow(
            popupBinding.root,
            200.dp,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        binding.repeat.setOnClickListener {
            val adapter = RepeatTypeAdapter(currentInterval).apply {
                setOnItemClickListener { _, data, _ ->
                    currentInterval = data.interval
                    binding.repeat.setContent(Alarm.repeatType(currentInterval))
                    popupWindow.dismiss()
                }
            }
            popupBinding.repeatRecyclerview.layoutManager = LinearLayoutManager(requireContext())
            popupBinding.repeatRecyclerview.adapter = adapter
            adapter.submitList(Alarm.repeatTypeList())
            popupWindow.showAsDropDown(binding.repeat, (-20).dp, (-50).dp, Gravity.END)
        }
    }

    // 选择音频文件
    private fun initMusicPicker() {
        binding.music.setOnClickListener {
            pickFile.launch(arrayOf("audio/*"))
        }
    }

    // 选择日期时间
    private fun initDateTimePicker() {
        val now = Date()
        val c = Calendar.getInstance(Locale.CHINA)
        c.time = now
        if (currentDate.isEmpty()) {
            currentDate = now.fmt("yyyy-MM-dd")
        }
        if (currentTime.isEmpty()) {
            currentTime = now.fmt("HH:mm")
        }
        binding.date.setContent(currentDate)
        binding.time.setContent(currentTime)
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setSelection(c.timeInMillis).build()
        val timePicker = MaterialTimePicker.Builder()
            .setHour(c.get(Calendar.HOUR_OF_DAY))
            .setMinute(c.get(Calendar.MINUTE))
            .setTimeFormat(TimeFormat.CLOCK_24H).build()
        datePicker.addOnCancelListener {
            currentDate = ""
        }
        datePicker.addOnPositiveButtonClickListener {
            currentDate = Date(it).fmt("yyyy-MM-dd")
            binding.date.setContent(currentDate)
        }
        timePicker.addOnCancelListener {
            currentTime = ""
        }
        timePicker.addOnPositiveButtonClickListener {
            currentTime = "${timePicker.hour.toFixed()}:${timePicker.minute.toFixed()}"
            binding.time.setContent(currentTime)
        }
        binding.date.setOnClickListener {
            datePicker.show(parentFragmentManager, "add-alarm-date")
        }

        binding.time.setOnClickListener {
            timePicker.show(parentFragmentManager, "add-alarm-time")
        }
    }
}