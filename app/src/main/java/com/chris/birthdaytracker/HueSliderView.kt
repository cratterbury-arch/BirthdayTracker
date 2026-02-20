package com.chris.birthdaytracker

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class HueSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var hue = 0f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var listener: ((Float) -> Unit)? = null

    fun setOnHueChangedListener(l: (Float) -> Unit) {
        listener = l
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (width == 0 || height == 0) return

        val colors = IntArray(7)
        for (i in 0..6) {
            colors[i] = Color.HSVToColor(floatArrayOf(i * 60f, 1f, 1f))
        }

        val shader = LinearGradient(
            0f, 0f, width.toFloat(), 0f,
            colors, null,
            Shader.TileMode.CLAMP
        )

        paint.shader = shader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {

            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {

                parent?.requestDisallowInterceptTouchEvent(true)

                val x = min(width.toFloat(), max(0f, event.x))
                hue = (x / width) * 360f

                listener?.invoke(hue)
                invalidate()
                performClick()
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}