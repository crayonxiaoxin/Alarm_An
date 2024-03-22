package com.github.crayonxiaoxin.alarmclock.ui

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.crayonxiaoxin.alarmclock.R
import com.github.crayonxiaoxin.alarmclock.base.BaseActivity
import com.github.crayonxiaoxin.alarmclock.base.BaseFragment
import com.github.crayonxiaoxin.alarmclock.data.Repository
import com.github.crayonxiaoxin.alarmclock.databinding.ActivityMainBinding
import com.github.crayonxiaoxin.alarmclock.receiver.AlarmReceiver
import com.github.crayonxiaoxin.alarmclock.service.AlarmService
import com.github.crayonxiaoxin.alarmclock.ui.home.HomeFragment
import com.github.crayonxiaoxin.alarmclock.utils.AudioManager
import com.github.crayonxiaoxin.lib_common.global.toast
import com.github.crayonxiaoxin.lib_common.utils.StatusBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.util.Calendar

class MainActivity : BaseActivity(), IMain {
    override val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    private val permissions = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.SCHEDULE_EXACT_ALARM,
        Manifest.permission.READ_EXTERNAL_STORAGE,
    )
    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            Log.e(TAG, "$it")
            val intent = Intent(this, AlarmService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }

    private val requestOverlayPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            Log.e(TAG, "requestOverlayPermission: $it")
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        StatusBar.fitSystemBar(this, decorFitsSystemWindows = true, darkIcons = true)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissions.launch(permissions)

        if (!checkOverlayPermission()) {
            AlertDialog.Builder(this)
                .setTitle("请打开悬浮窗权限")
                .setMessage("请找到该应用并打开显示在其他应用上层开关")
                .setPositiveButton("确定") { dialog, _ ->
                    dialog.dismiss()
                    requestOverlayPermission()
                }
                .setNegativeButton("取消") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        toPage(HomeFragment())

    }

    // 检查悬浮窗权限
    fun checkOverlayPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appOpsManager =
                getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOpsManager.unsafeCheckOpNoThrow(
                    "android:system_alert_window",
                    android.os.Process.myUid(),
                    packageName
                )
            } else {
                appOpsManager.checkOpNoThrow(
                    "android:system_alert_window",
                    android.os.Process.myUid(),
                    packageName
                )
            }
            Log.e(TAG, "checkOverlayPermission: $mode")
            return mode == AppOpsManager.MODE_ALLOWED
        } else {
            return Settings.canDrawOverlays(this)
        }
    }

    // 申请悬浮窗权限
    fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestOverlayPermission.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestOverlayPermission.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${packageName}")
            })
        }
    }


    private var isExit = false

    /**
     * 处理返回键
     *
     * @Author: Lau
     * @Date: 2022/5/26 3:01 下午
     */
    override fun onBackPressed() {
        val fm = supportFragmentManager
        val count = fm.backStackEntryCount
        if (count > 1) {
            with(fm.findFragmentById(R.id.frame_layout)) {
                if (this is BaseFragment) {
                    if (this.onBackPressed()) {
                        // 处理 fragment 返回事件
                        // 如果是菜单中的页面，返回后直接回到主页
//                        if (this is AcctFragment) {
//                            toPage(HomeFragment())
//                        } else {
//                            fm.popBackStack()
//                        }
                        fm.popBackStack()
                    }
                } else {
                    super.onBackPressed()
                }
            }
        } else if (count == 1) {
            if (isExit) {
                this.finish()
            } else {
                isExit = true
                toast(getString(R.string.exit_press_one_more))
                lifecycleScope.launch {
                    delay(1500)
                    isExit = false
                }
            }
        } else {
            super.onBackPressed()
        }
    }

    /**
     * 处理 fragment 跳转
     * @param f 要跳转的fragment
     *
     * @Author: Lau
     * @Date: 2022/5/26 3:00 下午
     */
    override fun toPage(f: Fragment, tag: String?) {
        addFragment(R.id.main_container, f, tag)
    }

    /**
     * 回到上一页
     *
     * @Author: Lau
     * @Date: 2022/5/26 3:00 下午
     */
    override fun back() {
        this.onBackPressed()
    }

}