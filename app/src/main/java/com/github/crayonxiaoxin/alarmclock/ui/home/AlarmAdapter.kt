package com.github.crayonxiaoxin.alarmclock.ui.home

import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.github.crayonxiaoxin.alarmclock.base.BaseListAdapterS
import com.github.crayonxiaoxin.alarmclock.databinding.ItemAlarmBinding
import com.github.crayonxiaoxin.alarmclock.model.Alarm
import com.github.crayonxiaoxin.alarmclock.utils.inflater

class AlarmAdapter : BaseListAdapterS<Alarm, ItemAlarmBinding>(diff) {
    companion object {
        val diff = object : DiffUtil.ItemCallback<Alarm>() {
            override fun areItemsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun setItemViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemAlarmBinding.inflate(parent.inflater(), parent, false))
    }

    private var onItemLongClick: ((position: Int, data: Alarm, holder: ViewHolder) -> Unit)? = null
    fun setOnItemLongClickListener(listener: ((position: Int, data: Alarm, holder: ViewHolder) -> Unit)?) {
        this.onItemLongClick = listener
    }

    private var onItemEnableClick: ((position: Int, data: Alarm, holder: ViewHolder) -> Unit)? =
        null

    fun setOnItemEnableClickListener(listener: ((position: Int, data: Alarm, holder: ViewHolder) -> Unit)?) {
        this.onItemEnableClick = listener
    }

    override fun bindItemViewHolder(position: Int, data: Alarm, holder: ViewHolder) {
        holder.itemView.setOnLongClickListener {
            if (onItemLongClick != null) {
                onItemLongClick!!.invoke(position, data, holder)
                true
            } else {
                false
            }
        }
        holder.binding.status.setOnCheckedChangeListener { _, enable ->
            if (enable != data.isEnable()) {
                val newAlarm = data.copy(enable = if (enable) 1 else 0)
                Log.e("TAG", "bindItemViewHolder: $enable")
                onItemEnableClick?.invoke(position, newAlarm, holder)
            }
        }
        val context = holder.itemView.context
        holder.binding.context = context
        holder.binding.alarm = data
    }
}