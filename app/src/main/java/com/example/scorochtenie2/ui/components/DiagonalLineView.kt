package com.example.scorochtenie2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet

import android.view.View
import android.widget.TextView

class DiagonalLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 6f * resources.displayMetrics.density
        style = Paint.Style.STROKE
        isAntiAlias = true
        alpha = (255 * 0.5f).toInt()
    }

    private var textView: TextView? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        textView = (parent as? View)?.findViewById<TextView>(R.id.diagonal_text_view)
        val textView = textView
        if (textView == null) {

            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }


        textView.post {
            val layout = textView.layout
            if (layout != null) {

                val textWidth = layout.width.toFloat()
                val textHeight = textView.measuredHeight

                val totalWidth = (textWidth + textView.paddingLeft + textView.paddingRight).toInt()
                setMeasuredDimension(totalWidth, textHeight)

                invalidate()
            } else {

                val width = textView.measuredWidth + textView.paddingLeft + textView.paddingRight
                val height = textView.measuredHeight
                setMeasuredDimension(width, height)

                invalidate()
            }
        }


        val defaultWidth = textView.measuredWidth + textView.paddingLeft + textView.paddingRight
        val defaultHeight = textView.measuredHeight
        setMeasuredDimension(defaultWidth, defaultHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val textWidth = textView?.layout?.width?.toFloat() ?: width.toFloat()
        val drawWidth = textWidth + (textView?.paddingLeft ?: 0) + (textView?.paddingRight ?: 0)
        canvas.drawLine(0f, 0f, drawWidth, height.toFloat(), paint)

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        textView = (parent as? View)?.findViewById<TextView>(R.id.diagonal_text_view)
        textView?.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (textView?.measuredHeight != measuredHeight || textView?.layout?.width != measuredWidth) {

                requestLayout()
                invalidate()
            }
        }
    }
}