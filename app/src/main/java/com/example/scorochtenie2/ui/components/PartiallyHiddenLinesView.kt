package com.example.scorochtenie2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
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
        maskPaint.color = getBackgroundColor()
        val totalLineCount = layout.lineCount
        val scrollView = tv.parent as? View
        val scrollY = (scrollView as? android.widget.ScrollView)?.scrollY?.toFloat() ?: 0f
        val leftPadding = tv.paddingLeft.toFloat()
        val topPadding = tv.paddingTop.toFloat()
        val rightPadding = tv.paddingRight.toFloat()
        val textWidth = tv.width - tv.paddingLeft - tv.paddingRight
        for (i in 0 until totalLineCount) {
            val lineTop = layout.getLineTop(i).toFloat() + topPadding - scrollY
            val lineBottom = layout.getLineBottom(i).toFloat() + topPadding - scrollY
            val lineHeight = lineBottom - lineTop

            val maskTop = lineTop + lineHeight * 0.65f


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

        textView?.let { tv ->
            val textViewBackground = tv.background
            if (textViewBackground != null) {

                try {
                    val bitmap = textViewBackground.toBitmap(1, 1)
                    val color = bitmap.getPixel(0, 0)

                    return color
                } catch (e: Exception) {

                }
            }
        }

        textView?.parent?.let { parent ->
            if (parent is View) {
                val parentBackground = parent.background
                if (parentBackground != null) {
                    try {
                        val bitmap = parentBackground.toBitmap(1, 1)
                        val color = bitmap.getPixel(0, 0)

                        return color
                    } catch (e: Exception) {

                    }
                }
            }
        }


        val typedValue = TypedValue()
        if (context.theme.resolveAttribute(R.color.background_light, typedValue, true)) {

            return typedValue.data
        }


        if (context.theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)) {

            return typedValue.data
        }

        return Color.WHITE
    }

    fun setTextView(textView: TextView) {
        this.textView = textView
    }

    fun showMask() {
        shouldDrawMask = true

        invalidate()
    }

    fun hideMask() {
        shouldDrawMask = false
        invalidate()
    }

    fun updateMask() {
        invalidate()
    }
}