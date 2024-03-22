package com.github.crayonxiaoxin.alarmclock.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.github.crayonxiaoxin.alarmclock.base.BaseListFragmentS
import com.github.crayonxiaoxin.alarmclock.data.Repository
import com.github.crayonxiaoxin.alarmclock.databinding.FragmentHomeBinding
import com.github.crayonxiaoxin.alarmclock.databinding.ItemAlarmBinding
import com.github.crayonxiaoxin.alarmclock.databinding.LayoutListBinding
import com.github.crayonxiaoxin.alarmclock.model.Alarm
import com.github.crayonxiaoxin.alarmclock.ui.add_alarm.AlarmAddFragment
import com.github.crayonxiaoxin.alarmclock.ui.navigate
import com.github.crayonxiaoxin.alarmclock.utils.show
import kotlinx.coroutines.launch

class HomeFragment :
    BaseListFragmentS<Alarm, ItemAlarmBinding, AlarmAdapter, FragmentHomeBinding>() {
    override fun enableLoadMore(): Boolean {
        return false
    }

    override fun enableRefresh(): Boolean {
        return false
    }

    override fun setRootBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun setListBinding(): LayoutListBinding {
        return rootBinding.layoutList
    }

    override fun initAdapter(): AlarmAdapter {
        return AlarmAdapter()
    }

    override fun initData() {
        rootBinding.add.setOnClickListener {
            navigate?.toPage(AlarmAddFragment(),"alarm-add")
        }
        adapter.setOnItemClickListener { _, data, _ ->
            navigate?.toPage(AlarmAddFragment.newInstance(data),"alarm-update")
        }
        adapter.setOnItemLongClickListener { _, data, _ ->
            AlertDialog.Builder(requireContext())
                .setTitle("删除闹钟")
                .setMessage("你确定要删除该闹钟吗？")
                .setPositiveButton("确定") { dialog, _ ->
                    dialog.dismiss()
                    lifecycleScope.launch {
                        Repository.unsetAlarm(requireContext(), data)
                    }
                }
                .setNegativeButton("取消") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
        adapter.setOnItemEnableClickListener { _, data, _ ->
            lifecycleScope.launch {
                Repository.updateAlarm(requireContext(),data)
            }
        }
        Repository.alarmDao.getAllObx().observe(viewLifecycleOwner) {
            emptyView.show(it.isEmpty())
            adapter.submitList(it)
        }
    }

    override fun refreshData() {

    }
}