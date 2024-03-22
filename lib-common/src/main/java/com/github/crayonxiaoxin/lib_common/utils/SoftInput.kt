package com.github.crayonxiaoxin.lib_common.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

fun View.showSoftInput() {
    val inputMethodManager =
        this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    inputMethodManager?.let {
        it.showSoftInput(this, InputMethodManager.RESULT_SHOWN)
        it.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }
}

fun View.hideSoftInput() {
    val inputMethodManager =
        this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    inputMethodManager?.hideSoftInputFromWindow(this.windowToken, 0)
}