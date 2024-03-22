package com.github.crayonxiaoxin.alarmclock.ui.add_alarm

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.github.crayonxiaoxin.alarmclock.base.BaseListAdapterS
import com.github.crayonxiaoxin.alarmclock.databinding.ItemRepeatBinding
import com.github.crayonxiaoxin.alarmclock.model.RepeatType
import com.github.crayonxiaoxin.alarmclock.utils.inflater

class RepeatTypeAdapter(private val selected: Long) :
    BaseListAdapterS<RepeatType, ItemRepeatBinding>(diff) {
    companion object {
        val diff = object : DiffUtil.ItemCallback<RepeatType>() {
            override fun areItemsTheSame(oldItem: RepeatType, newItem: RepeatType): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: RepeatType, newItem: RepeatType): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun setItemViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemRepeatBinding.inflate(parent.inflater(), parent, false))
    }

    override fun bindItemViewHolder(position: Int, data: RepeatType, holder: ViewHolder) {
        holder.binding.repeat = data
        holder.binding.selected = selected
    }
}