package com.github.crayonxiaoxin.lib_common.utils

import android.content.Context

fun Context?.getAppVersionName(): String {
    val packageInfo =
        this?.packageManager?.getPackageInfo(this.packageName, 0)
    return (packageInfo?.versionName) ?: ""
}

