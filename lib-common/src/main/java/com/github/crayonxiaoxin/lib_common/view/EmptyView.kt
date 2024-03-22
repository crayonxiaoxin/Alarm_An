package com.github.crayonxiaoxin.lib_common.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.github.crayonxiaoxin.lib_common.R

class EmptyView : LinearLayout {
    private var title: TextView
    private var icon: ImageView
//    private var action: Button

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context, attrs, defStyleAttr, 0
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {

        LayoutInflater.from(context).inflate(R.layout.layout_empty_view, this, true)

        orientation = VERTICAL
        gravity = Gravity.CENTER

//        action = findViewById(R.id.empty_action)
        icon = findViewById(R.id.empty_icon)
        title = findViewById(R.id.empty_text)
    }

    fun setEmptyIcon(@DrawableRes resId: Int) {
        icon.setImageResource(resId)
    }

    fun setTitle(text: String) {
        if (text.isEmpty()) {
            title.visibility = View.GONE
        } else {
            title.visibility = View.VISIBLE
            title.text = text
        }
    }

//    fun setButton(text: String, onclick: () -> Unit) {
//        if (text.isEmpty()) {
//            action.visibility = View.GONE
//        } else {
//            action.visibility = View.VISIBLE
//            action.text = text
//            action.setOnClickListener { onclick() }
//        }
//    }
}