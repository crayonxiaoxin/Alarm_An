package com.github.crayonxiaoxin.lib_common.view

import android.graphics.Outline
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.Nullable
import com.github.crayonxiaoxin.lib_common.R
import com.github.crayonxiaoxin.lib_common.utils.dp

object ViewHelper {

    const val RADIUS_ALL = 0
    const val RADIUS_LEFT = 1
    const val RADIUS_TOP = 2
    const val RADIUS_RIGHT = 3
    const val RADIUS_BOTTOM = 4

    fun setViewOutline(
        view: View,
        @Nullable attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) {
        val typedArray =
            view.context.obtainStyledAttributes(
                attrs,
                R.styleable.viewOutlineStrategy,
                defStyleAttr,
                defStyleRes
            )
        val radius =
            typedArray.getDimensionPixelOffset(R.styleable.viewOutlineStrategy_clipRadius, 0)
        val side = typedArray.getInt(R.styleable.viewOutlineStrategy_clipSide, RADIUS_ALL)
        typedArray.recycle()

        setViewOutline(view, radius, side)
    }

    fun setViewOutline(view: View, radius: Int, side: Int) {
        if (radius <= 0) return
        view.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val width = view.width
                val height = view.height
                if (width <= 0 || height <= 0) return
                var left = 0
                var right = width
                var top = 0
                var bottom = height
                when (side) {
                    RADIUS_LEFT -> right += radius
                    RADIUS_RIGHT -> left -= radius
                    RADIUS_TOP -> bottom += radius
                    RADIUS_BOTTOM -> top -= radius
                    RADIUS_ALL -> {
                    }
                }
                outline.setRoundRect(left, top, right, bottom, radius.toFloat())
            }
        }
        view.clipToOutline = true
        view.postInvalidate()
    }
}