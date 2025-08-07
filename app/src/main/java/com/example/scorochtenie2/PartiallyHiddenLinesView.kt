package com.example.scorochtenie2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.get

class PartiallyHiddenLinesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val maskPaint = Paint().apply {
        style = Paint.Style.FILL
    }

    private var textView: TextView? = null
    private var shouldDrawMask = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!shouldDrawMask) return

        val tv = textView ?: return
        val layout = tv.layout ?: return

        // Обновляем цвет маски при каждом рендере
        maskPaint.color = getBackgroundColor()
        Log.d("PartiallyHiddenLines", "Mask color: ${Integer.toHexString(maskPaint.color)}")

        val totalLineCount = layout.lineCount
        val scrollView = tv.parent as? View
        val scrollY = (scrollView as? android.widget.ScrollView)?.scrollY?.toFloat() ?: 0f
        Log.d("PartiallyHiddenLines", "Drawing mask with scrollY: $scrollY")

        // Учитываем padding TextView
        val leftPadding = tv.paddingLeft.toFloat()
        val topPadding = tv.paddingTop.toFloat()
        val rightPadding = tv.paddingRight.toFloat()
        val textWidth = tv.width - tv.paddingLeft - tv.paddingRight

        // Рисуем маски для нижней части всех строк с учётом прокрутки
        for (i in 0 until totalLineCount) {
            val lineTop = layout.getLineTop(i).toFloat() + topPadding - scrollY
            val lineBottom = layout.getLineBottom(i).toFloat() + topPadding - scrollY
            val lineHeight = lineBottom - lineTop

            // Скрываем нижние 40% каждой строки
            val maskTop = lineTop + lineHeight * 0.65f

            // Рисуем маску только для видимых строк
            if (lineBottom > 0 && lineTop < height) {
                canvas.drawRect(
                    leftPadding,
                    maskTop,
                    leftPadding + textWidth,
                    lineBottom,
                    maskPaint
                )
            }
        }
    }

    private fun getBackgroundColor(): Int {
        // Сначала пробуем взять цвет фона из TextView
        textView?.let { tv ->
            val textViewBackground = tv.background
            if (textViewBackground != null) {
                // Если фон TextView определён (не прозрачный), используем его
                try {
                    val bitmap = textViewBackground.toBitmap(1, 1)
                    val color = bitmap.getPixel(0, 0)
                    Log.d(
                        "PartiallyHiddenLines",
                        "TextView background color: ${Integer.toHexString(color)}"
                    )
                    return color
                } catch (e: Exception) {
                    Log.e(
                        "PartiallyHiddenLines",
                        "Failed to get TextView background color: ${e.message}"
                    )
                }
            }
        }

        // Если TextView не имеет фона, проверяем родительский контейнер (ScrollView или LinearLayout)
        textView?.parent?.let { parent ->
            if (parent is View) {
                val parentBackground = parent.background
                if (parentBackground != null) {
                    try {
                        val bitmap = parentBackground.toBitmap(1, 1)
                        val color = bitmap.getPixel(0, 0)
                        Log.d(
                            "PartiallyHiddenLines",
                            "Parent background color: ${Integer.toHexString(color)}"
                        )
                        return color
                    } catch (e: Exception) {
                        Log.e(
                            "PartiallyHiddenLines",
                            "Failed to get parent background color: ${e.message}"
                        )
                    }
                }
            }
        }

        // Если фон родителя не определён, используем surfaceColor из темы
        val typedValue = TypedValue()
        if (context.theme.resolveAttribute(R.color.background_light, typedValue, true)) {
            Log.d(
                "PartiallyHiddenLines",
                "Surface color from theme: ${Integer.toHexString(typedValue.data)}"
            )
            return typedValue.data
        }

        // Fallback: используем android.R.attr.colorBackground
        if (context.theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)) {
            Log.d(
                "PartiallyHiddenLines",
                "System background color: ${Integer.toHexString(typedValue.data)}"
            )
            return typedValue.data
        }

        // Окончательный fallback
        Log.w("PartiallyHiddenLines", "Using fallback color: WHITE")
        return Color.WHITE
    }

    fun setTextView(textView: TextView) {
        this.textView = textView
    }

    fun showMask() {
        shouldDrawMask = true
        Log.d("PartiallyHiddenLines", "Mask shown")
        invalidate()
    }

    fun hideMask() {
        shouldDrawMask = false
        Log.d("PartiallyHiddenLines", "Mask hidden")
        invalidate()
    }

    fun updateMask() {
        invalidate()
    }
}