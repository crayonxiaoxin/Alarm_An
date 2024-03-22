package com.github.crayonxiaoxin.lib_common.global

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AppGlobals {
    private var sApplication: Application? = null
    val application: Application get() = application()!!

    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    fun application(): Application? {
        if (sApplication == null) {
            try {
                val method = Class.forName("android.app.ActivityThread")
                    .getDeclaredMethod("currentApplication")
                sApplication = method.invoke(null, *arrayOf()) as Application
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return sApplication
    }

}

@Deprecated("使用了反射，性能问题", replaceWith = ReplaceWith("Context.toast()"))
fun toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    AppGlobals.application()?.let {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(it, message, duration).show()
        }
    }
}

fun Context?.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    if (this == null) return
    CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(this@toast, message, duration).show()
    }
}

fun Fragment.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    this.context?.toast(message, duration)
}