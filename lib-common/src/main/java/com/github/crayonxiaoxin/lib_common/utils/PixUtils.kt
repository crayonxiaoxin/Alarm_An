package com.github.crayonxiaoxin.lib_common.utils

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.github.crayonxiaoxin.lib_common.global.AppGlobals

object PixUtils {

    @JvmStatic
    fun dp2px(dp: Int): Int {
        return (getDisplayMetrics().density * dp + 0.5f).toInt()
    }

    @JvmStatic
    fun getScreenWidth(): Int {
        return getDisplayMetrics().widthPixels
    }

    @JvmStatic
    fun getScreenHeight(): Int {
        if (isFullScreenDevice()) {
            AppGlobals.application()?.getSystemService(Context.WINDOW_SERVICE)?.let {
                val windowManager = it as WindowManager
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val bounds = windowManager.currentWindowMetrics.bounds
                    bounds.height()
                } else {
                    val point = Point()
                    windowManager.defaultDisplay.getRealSize(point)
                    point.y
                }
            }
        } else {
            return getDisplayMetrics().heightPixels
        }
        return 0
    }

    private var mHasCheckFullScreen: Boolean = false
    private var mIsFullScreen: Boolean = false
    fun isFullScreenDevice(): Boolean {
        if (mHasCheckFullScreen) {
            return mIsFullScreen
        }
        mIsFullScreen = false
        // 低于 API 21 的都不会是全面屏
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false
        }
        AppGlobals.application()?.getSystemService(Context.WINDOW_SERVICE)?.let {
            val windowManager = it as WindowManager
            val point = Point()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bounds = windowManager.currentWindowMetrics.bounds
                point.set(bounds.width(), bounds.height())
            } else {
                windowManager.defaultDisplay.getRealSize(point)
            }
            val width: Int
            val height: Int
            if (point.x < point.y) {
                width = point.x
                height = point.y
            } else {
                width = point.y
                height = point.x
            }
            mIsFullScreen = height / width >= 1.97
        }
        mHasCheckFullScreen = true
        return mIsFullScreen
    }

    @JvmStatic
    fun getDisplayMetrics(): DisplayMetrics {
        return AppGlobals.application.resources.displayMetrics
    }
}

val Int.dp get() = PixUtils.dp2px(this)