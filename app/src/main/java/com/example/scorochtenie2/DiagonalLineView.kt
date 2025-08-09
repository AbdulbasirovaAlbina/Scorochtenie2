package com.example.scorochtenie2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView

class DiagonalLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 6f * resources.displayMetrics.density // шире
        style = Paint.Style.STROKE
        isAntiAlias = true
        alpha = (255 * 0.5f).toInt() // полупрозрачная
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val textView = (parent as? View)?.findViewById<TextView>(R.id.diagonal_text_view)
        val height = textView?.measuredHeight ?: MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
        Log.d("DiagonalLineView", "Measured size: ${width}x${height}")

        textView?.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (textView.measuredHeight != measuredHeight) {
                requestLayout()
                invalidate()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawLine(0f, 0f, width.toFloat(), height.toFloat(), paint)
        Log.d("DiagonalLineView", "Drawing line with size: ${width}x${height}")
    }
}