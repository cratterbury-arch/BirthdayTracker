package com.chris.birthdaytracker

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class ColorPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var hue = 0f
    private var sat = 1f
    private var value = 1f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var listener: ((Int) -> Unit)? = null

    fun setOnColorChangedListener(l: (Int) -> Unit) {
        listener = l
    }

    fun setInitialColor(color: Int) {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hue = hsv[0]
        sat = hsv[1]
        value = hsv[2]
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        val hueBarWidth = 60f
        val svWidth = width - hueBarWidth

        // ---- Saturation / Value square ----
        val hsvColor = floatArrayOf(hue, 1f, 1f)
        val color = Color.HSVToColor(hsvColor)

        val satShader = LinearGradient(
            0f, 0f, svWidth, 0f,
            Color.WHITE, color,
            Shader.TileMode.CLAMP
        )

        val valShader = LinearGradient(
            0f, 0f, 0f, height,
            Color.TRANSPARENT, Color.BLACK,
            Shader.TileMode.CLAMP
        )

        paint.shader = ComposeShader(satShader, valShader, PorterDuff.Mode.MULTIPLY)
        canvas.drawRect(0f, 0f, svWidth, height, paint)

        paint.shader = null

        // ---- Hue slider ----
        val hueColors = IntArray(360) { i ->
            Color.HSVToColor(floatArrayOf(i.toFloat(), 1f, 1f))
        }

        val hueShader = LinearGradient(
            svWidth, 0f,
            width, height,
            hueColors,
            null,
            Shader.TileMode.CLAMP
        )

        paint.shader = hueShader
        canvas.drawRect(svWidth, 0f, width, height, paint)
        paint.shader = null

        // ---- SV Indicator ----
        val indicatorX = sat * svWidth
        val indicatorY = (1 - value) * height

        indicatorPaint.style = Paint.Style.STROKE
        indicatorPaint.strokeWidth = 5f
        indicatorPaint.color = Color.WHITE

        canvas.drawCircle(indicatorX, indicatorY, 20f, indicatorPaint)

        // ---- Hue Indicator ----
        val hueY = (hue / 360f) * height
        canvas.drawRect(
            svWidth,
            hueY - 5,
            width,
            hueY + 5,
            indicatorPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val width = width.toFloat()
        val height = height.toFloat()
        val hueBarWidth = 60f
        val svWidth = width - hueBarWidth

        if (event.x < svWidth) {
            sat = min(1f, max(0f, event.x / svWidth))
            value = 1f - min(1f, max(0f, event.y / height))
        } else {
            hue = min(360f, max(0f, (event.y / height) * 360f))
        }

        val color = Color.HSVToColor(floatArrayOf(hue, sat, value))
        listener?.invoke(color)

        invalidate()
        return true
    }
}