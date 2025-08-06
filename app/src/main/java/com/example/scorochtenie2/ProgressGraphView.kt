package com.example.scorochtenie2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class ProgressGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.parseColor("#2196F3")
    }

    private val fillPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.parseColor("#802196F3")
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = 12f
        color = Color.GRAY
    }
    
    private fun getTextColor(): Int {
        val typedValue = android.util.TypedValue()
        context.theme.resolveAttribute(android.R.attr.textColorSecondary, typedValue, true)
        return if (typedValue.resourceId != 0) {
            context.getColor(typedValue.resourceId)
        } else {
            Color.GRAY
        }
    }

    private var dataPoints: List<Int> = emptyList()

    fun setData(points: List<Int>) {
        dataPoints = points
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Обновляем цвет текста под текущую тему
        textPaint.color = getTextColor()

        if (dataPoints.isEmpty()) {
            // Рисуем заглушку
            val centerX = width / 2f
            val centerY = height / 2f
            canvas.drawText("График прогресса", centerX - 50f, centerY, textPaint)
            return
        }

        val padding = 40f
        val graphWidth = width - 2 * padding
        val graphHeight = height - 2 * padding

        if (graphWidth <= 0 || graphHeight <= 0) return

        val maxValue = dataPoints.maxOrNull() ?: 100
        val minValue = 0

        val path = Path()
        val fillPath = Path()

        dataPoints.forEachIndexed { index, value ->
            val x = padding + (index.toFloat() / (dataPoints.size - 1)) * graphWidth
            val normalizedValue = (value - minValue).toFloat() / (maxValue - minValue)
            val y = height - padding - normalizedValue * graphHeight

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height - padding)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        // Завершаем fill path
        fillPath.lineTo(width - padding, height - padding)
        fillPath.close()

        // Рисуем заливку
        canvas.drawPath(fillPath, fillPaint)
        
        // Рисуем линию
        canvas.drawPath(path, paint)

        // Рисуем точки
        dataPoints.forEachIndexed { index, value ->
            val x = padding + (index.toFloat() / (dataPoints.size - 1)) * graphWidth
            val normalizedValue = (value - minValue).toFloat() / (maxValue - minValue)
            val y = height - padding - normalizedValue * graphHeight

            canvas.drawCircle(x, y, 6f, paint)
        }

        // Рисуем подписи осей
        canvas.drawText("День 1", padding, height - 10f, textPaint)
        canvas.drawText("День 7", width - padding - 30f, height - 10f, textPaint)
    }
} 