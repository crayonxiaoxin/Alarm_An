package com.github.crayonxiaoxin.lib_common.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import com.github.crayonxiaoxin.lib_common.R

class LoadingDialog : AlertDialog {

    private var loadingText: String = "Loading"

    constructor(context: Context?) : super(context)
    constructor(context: Context?, themeResId: Int) : super(context, themeResId)
    constructor(
        context: Context?,
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?
    ) : super(context, cancelable, cancelListener)

    fun setLoadingText(loadingText: String) {
        this.loadingText = loadingText
    }

    override fun show() {
        super.show()
        setContentView(R.layout.layout_loading_view)
        val loadingText: TextView = findViewById(R.id.loading_text)
        loadingText.text = this.loadingText
        window?.let {
            val attributes = it.attributes
            attributes.width = WindowManager.LayoutParams.WRAP_CONTENT
            attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes.gravity = Gravity.CENTER
            attributes.dimAmount = 0.35f
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.attributes = attributes
        }
    }

}