package org.hyperskill.pomodoro.timer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import org.hyperskill.pomodoro.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * TODO: document your custom view class.
 */
class TimerViewSecond : View {

    private var _totalParts: Int = 5
    private var _activeParts: Int = 5
    private var _mColor: Int = Color.RED
    private var _mText: String? = "00:05"
    private var _textSize: Float = 100F

    private var mHandler = Handler(Looper.getMainLooper())
    private lateinit var task: Runnable
    var isRunning = false

    private lateinit var _mCircleBounds: RectF

    private var mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        color = color
    }

    @SuppressLint("ResourceAsColor")
    private var textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = _textSize
        color = R.color.black
    }

    /**
     * The total parts to draw
     */
    var totalParts: Int
        get() = _totalParts
        set(value) {
            _totalParts = value
            invalidatePaintAndMeasurements()
        }

    /**
     * Current part defines the amount of circle peaces out of total
     */
    var activeParts: Int
        get() = _activeParts
        set(value) {
            _activeParts = value
            invalidatePaintAndMeasurements()
        }

    /**
     * The color of circle
     */
    var color: Int
        get() = _mColor
        set(value) {
            _mColor = value
            invalidatePaintAndMeasurements()
        }

    /**
     * The text shown on the timer
     */
    var text: String?
        get() = _mText
        set(value) {
            _mText = value
            invalidatePaintAndMeasurements()
        }

    var mCircleBounds: RectF
        get() = _mCircleBounds
        set(value) {
            _mCircleBounds = value
            invalidatePaintAndMeasurements()
        }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    @SuppressLint("ResourceAsColor")
    private fun init(attrs: AttributeSet?, defStyle: Int) {

        // Load attributes
        val a = context.obtainStyledAttributes(
                attrs, R.styleable.TimerView, defStyle, 0)

        _mText = a.getString(
                R.styleable.TimerView_mText)
        _mColor = a.getColor(
                R.styleable.TimerView_mColor,
                color)

        a.recycle()

        // Update TextPaint and text measurements from attributes
        invalidatePaintAndMeasurements()
    }

    @SuppressLint("ResourceAsColor")
    private fun invalidatePaintAndMeasurements() {
        mPaint.color = color
        invalidate()
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = if (width > height) height else width
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCircleBounds = RectF(0F, 0F, w.toFloat(), h.toFloat())
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paddingLeft = paddingLeft
        val paddingTop = paddingTop

        canvas.apply {
            drawArc(
                    mCircleBounds,
                    270F,
                    -(360F / totalParts) * activeParts,
                    true,
                    mPaint
            )
            drawText(
                    text,
                    paddingLeft + (width.toFloat() - _textSize) / 2,
                    paddingTop + (height.toFloat() + _textSize) / 2,
                    textPaint
            )
        }
    }

    fun start(totalSeconds: Int, cycles: Int) {
        this.totalParts = totalSeconds
        task = createTask(cycles)
        mHandler.postDelayed(task, 0);
    }

    private fun createTask(cycles: Int = 1): Runnable {

        var calendar = GregorianCalendar(0,0,0,0,0,0)
        var cycle = 1
        val date = SimpleDateFormat("mm:ss")

        this.isRunning = true
        this.color = Color.RED
        calendar.set(Calendar.SECOND, totalParts)

        return Runnable {
            run {
                var second = calendar.get(Calendar.SECOND)

                text = date.format(calendar.time)
                activeParts = second

                if (second == 0) {
                    cycle++

                    color = when {
                        cycle == cycles -> Color.YELLOW
                        cycle % 2 == 0 -> Color.GREEN
                        else -> Color.RED
                    }

                    second = totalParts
                    calendar.set(Calendar.SECOND, second)
                    activeParts = second
                    text = date.format(calendar.time)
                }

                if (cycle == cycles) {
                    this.isRunning = false
                    mHandler.removeCallbacks(task)
                } else {
                    calendar.set(Calendar.SECOND, second - 1)
                    mHandler.postDelayed(task, 1000L)
                }
            }
        }
    }

    fun stop() {
        if (this.isRunning) {
            this.isRunning = false
            mHandler.removeCallbacks(task)
            text = "00:05"
            color = Color.RED
            activeParts = totalParts
        }
    }
}