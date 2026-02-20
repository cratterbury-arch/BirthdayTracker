package com.chris.birthdaytracker

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class HsvColorPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var hue = 0f
    private var sat = 1f
    private var value = 1f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.WHITE
    }

    private var listener: ((Int) -> Unit)? = null

    fun setOnColorChangedListener(l: (Int) -> Unit) {
        listener = l
    }

    fun setHue(newHue: Float) {
        hue = newHue
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (width == 0 || height == 0) return

        val baseColor = Color.HSVToColor(floatArrayOf(hue, 1f, 1f))

        val satShader = LinearGradient(
            0f, 0f, width.toFloat(), 0f,
            Color.WHITE, baseColor,
            Shader.TileMode.CLAMP
        )

        val valShader = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            Color.TRANSPARENT, Color.BLACK,
            Shader.TileMode.CLAMP
        )

        paint.shader =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ComposeShader(satShader, valShader, BlendMode.MULTIPLY)
            } else {
                ComposeShader(satShader, valShader, PorterDuff.Mode.MULTIPLY)
            }

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null

        val indicatorX = sat * width
        val indicatorY = (1 - value) * height

        canvas.drawCircle(indicatorX, indicatorY, 20f, indicatorPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (width == 0 || height == 0) return true

        sat = min(1f, max(0f, event.x / width))
        value = 1f - min(1f, max(0f, event.y / height))

        val color = Color.HSVToColor(floatArrayOf(hue, sat, value))
        listener?.invoke(color)

        invalidate()
        return true
    }
}