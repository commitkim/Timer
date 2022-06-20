package com.example.timer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.lang.Math.*

class TimerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    lateinit var listener: TimerListener

    private val circlePaint = Paint().apply {
        strokeWidth = 170f
        isAntiAlias = true
        color = Color.RED
        style = Paint.Style.STROKE
    }

    private val stringPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        isAntiAlias = true
        textSize = 100f
    }

    private val baseCirclePaint = Paint().apply {
        strokeWidth = 200f
        isAntiAlias = true
        color = Color.LTGRAY
        style = Paint.Style.STROKE
    }

    private var isTouching = false
    private lateinit var center: PointF
    private lateinit var absoluteCenter: PointF
    private lateinit var rect: RectF
    private var radius: Float = 0f
    private var sweepAngle = 0.0
    private var isTimerStop = false
    private lateinit var currentPoint: PointF

    fun isTimerEnd(): Boolean =
         if (!isTouching && sweepAngle == 0.0) {
            isTimerStop = true
            true
        } else {
            onTick()
            false
        }


    private fun onTick() {
        sweepAngle -= TICK
        if (sweepAngle < 0) sweepAngle = 0.0
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        center = PointF(w / 2f, h / 2f)

        val points = IntArray(2)
        getLocationOnScreen(points)
        absoluteCenter = PointF(points[0] + center.x, points[1] + center.y)
        radius = min(w.toFloat(), h.toFloat()) * RADIUS_RATE
        rect = RectF(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawCircle(center.x, center.y, radius, baseCirclePaint)
        canvas?.drawArc(rect, 270f, sweepAngle.toFloat(), false, circlePaint)
        canvas?.drawText(
            sweepAngle.toFormatString(),
            center.x - (stringPaint.measureText(sweepAngle.toFormatString()) / 2),
            center.y - (stringPaint.descent() + stringPaint.ascent()) / 2,
            stringPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> onActionDown(it.rawX, it.rawY)
                MotionEvent.ACTION_MOVE -> onActionMove(it.rawX, it.rawY)
                MotionEvent.ACTION_UP -> onActionUp()
            }
            invalidate()
        }
        return true
    }

    private fun onActionDown(x: Float, y: Float) {
        currentPoint = PointF(x, y)
        isTouching = true
        listener.onTimerStop()
    }

    private fun onActionUp() {
        isTouching = false
        if (isTimerStop && sweepAngle != 0.0) {
            isTimerStop = false
        }
        listener.onTimerStart(sweepAngle.toMillisecond())
    }

    private fun onActionMove(x: Float, y: Float) {
        val prevRadius = atan2(
            (currentPoint.y - absoluteCenter.y).toDouble(),
            (currentPoint.x - absoluteCenter.x).toDouble()
        )
        currentPoint.x = x
        currentPoint.y = y
        if (absoluteCenter.distanceTo(PointF(x, y)) < 5000) {
            return
        }
        val currentRadius = atan2(
            (y - absoluteCenter.y).toDouble(),
            (x - absoluteCenter.x).toDouble()
        )
        val radiusDiff = currentRadius - prevRadius
        onSwipe(toDegrees(radiusDiff))
    }

    private fun onSwipe(degree: Double) {
        if (degree > 50 || degree < -50) return
        sweepAngle += degree
        if (sweepAngle > 360.0) sweepAngle = 360.0
        else if (sweepAngle < 0.0) sweepAngle = 0.0
    }

    private fun Double.toFormatString(): String {
        val time = (this * 10).toInt()
        return "${time / 60}분 ${if (time % 60 < 10) "0" + (time % 60) else time % 60}초"
    }

    private fun PointF.distanceTo(point: PointF): Float {
        val dx = this.x - point.x
        val dy = this.y - point.y
        return dx * dx + dy * dy
    }

    private fun Double.toMillisecond() = (this * 10 * 1000).toLong()

    companion object {
        private const val RADIUS_RATE = 0.35f
        private const val TICK = 0.1f
    }

}