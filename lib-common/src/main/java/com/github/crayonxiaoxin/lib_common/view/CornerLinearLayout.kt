package com.github.crayonxiaoxin.lib_common.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class CornerLinearLayout : LinearLayout {
    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context, attrs, defStyleAttr, 0
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        ViewHelper.setViewOutline(this, attrs, defStyleAttr, defStyleRes)
    }

    fun setViewOutline(radius: Int, side: Int) {
        ViewHelper.setViewOutline(this, radius, side)
    }
}