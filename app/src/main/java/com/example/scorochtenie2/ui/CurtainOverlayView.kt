package com.example.scorochtenie2

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.addListener

class CurtainOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var animator: ValueAnimator? = null
    private var offsetPx: Float = 0f

    // Visual params - уменьшены на 20% (еще на 20% тоньше)
    private val barHeightPx: Float get() = (24f * 0.8f * 0.8f) * resources.displayMetrics.density
    private val gapHeightPx: Float get() = (20f * 0.8f * 0.8f) * resources.displayMetrics.density
    private val patternHeightPx: Float get() = barHeightPx + gapHeightPx

    // Colors for stripes (semi-transparent) - убрали прозрачность на 10%
    private val colors: IntArray = intArrayOf(
        0x66FF5252, // red 40% alpha (было 30%)
        0x664CAF50, // green 40% alpha (было 30%)
        0x664285F4, // blue 40% alpha (было 30%)
        0x66FFC107  // amber 40% alpha (было 30%)
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (width == 0 || height == 0) return

        var y = -offsetPx
        var index = 0

        // Draw repeating colored bars from top to bottom
        while (y < height) {
            paint.color = colors[index % colors.size]
            val top = y
            val bottom = (y + barHeightPx).coerceAtMost(height.toFloat())
            if (bottom > 0) {
                canvas.drawRect(0f, top, width.toFloat(), bottom, paint)
            }
            y += patternHeightPx
            index++
        }
    }

    fun start(speedMsPerWord: Long, totalWords: Int, onEnd: (() -> Unit)? = null) {
        stop()
        
        // Total duration proportional to words count
        val perWordMs = (60_000L / speedMsPerWord).coerceAtLeast(50L)
        val totalDuration = (totalWords * perWordMs).coerceAtLeast(1_000L)

        animator = ValueAnimator.ofFloat(0f, patternHeightPx).apply {
            duration = totalDuration
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                val fraction = animation.animatedFraction
                // Move a few patterns across the view during the whole duration
                // Увеличиваем количество циклов для более плавного движения
                val cycles = 8f
                offsetPx = (fraction * cycles * patternHeightPx) % patternHeightPx
                invalidate()
            }
            addListener(onEnd = {
                stop()
                onEnd?.invoke()
            })
            start()
        }
    }

    fun stop() {
        animator?.cancel()
        animator = null
        offsetPx = 0f
        invalidate()
    }
}


