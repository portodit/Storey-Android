package com.example.storey.ui.customview

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Patterns
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.storey.R

class CustomEditTextEmail : AppCompatEditText {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        addTextChangedListener(onTextChanged = { email, _, _, _ ->
            error =
                if (!Patterns.EMAIL_ADDRESS.matcher(email.toString())
                        .matches() && !email.isNullOrEmpty()
                ) "Invalid Email" else null
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        context.apply {
            hint = "Email"
            background = ContextCompat.getDrawable(this, R.drawable.form_background)
        }
    }
}