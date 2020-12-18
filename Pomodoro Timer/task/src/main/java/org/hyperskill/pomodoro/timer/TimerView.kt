package org.hyperskill.pomodoro.timer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.fragment_settings_dialog_view.view.*
import org.hyperskill.pomodoro.R
import org.hyperskill.pomodoro.SettingsDialogFragment
import java.text.SimpleDateFormat
import java.util.*

/**
 * TODO: document your custom view class.
 */
class TimerView : View {

    var isRunning = false

    private var _totalParts: Int = 5
    private var _restTime: Int = 5
    private var _workTime: Int = 5
    private var _cycles: Int = 10
    private var _activeParts: Int = 5
    private var _mColor: Int = Color.RED
    private lateinit var _mText: String
    private var _textSize: Float = 100F

    private val calendar = GregorianCalendar(0,0,0,0,0,0)
    private val date = SimpleDateFormat("mm:ss")
    private var timer: Timer = Timer("counter")
    private var task: TimerTask? = null

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
     * The total cycles to work
     */
    var cycles: Int
        get() = _cycles
        set(value) {
            _cycles = value
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
    var text: String
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

    internal lateinit var listener: TimerListener

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    interface TimerListener {
        fun onJobStart()
        fun onCycleChange(cycle: Int)
        fun onWorkPeriodStart()
        fun onRestPeriodStart()
        fun onJobDone()
    }

    fun configure(workTime: Int? = null, restTime: Int? = null) {
        this._workTime = workTime ?: _workTime
        this._restTime = restTime ?: _restTime

        totalParts = _workTime
        this.resetTimer(totalParts)
        invalidatePaintAndMeasurements()
    }

    @SuppressLint("ResourceAsColor")
    private fun init(attrs: AttributeSet?, defStyle: Int) {

        try {
            listener = context as TimerListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() +
                    " must implement TimerListener"))
        }

        // Load attributes
        val a = context.obtainStyledAttributes(
                attrs, R.styleable.TimerView, defStyle, 0)

        this.configure()
        visibility = INVISIBLE

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

    fun start() {
        task = createTask()
        timer.scheduleAtFixedRate(task, 0, 1000)
    }

    private fun createTask(): TimerTask {

        var cycle = 1

        this.isRunning = true
        listener.onJobStart()

        visibility = VISIBLE

        return object : TimerTask() {
            override fun run() {

                this@TimerView.post {
                    var second = calendar.get(Calendar.SECOND)

                    text = date.format(calendar.time)
                    activeParts = second

                    if (second == 0) {
                        cycle++
                        listener.onCycleChange(cycle)
                        second = defineCycleState(cycle)
                    }

                    if (cycle == cycles) {
                        stop(false)
                    } else {
                        calendar.set(Calendar.SECOND, second - 1)
                    }
                }
            }
        }
    }

    fun defineCycleState(cycle: Int): Int {

        totalParts = when {
            cycle == cycles -> {
                color = Color.YELLOW
                _workTime
            }
            cycle % 2 == 0 -> {
                color = Color.GREEN
                _restTime
            }
            else -> {
                color = Color.RED
                _workTime
            }
        }

        this.resetTimer(totalParts)
        return totalParts
    }

    fun stop(interrupted: Boolean = true) {
        if (this.isRunning) {

            this.isRunning = false
            listener.onJobDone()

            this.task?.cancel()

            if (interrupted)
                color = Color.RED

            this.resetTimer(_workTime)
            visibility = INVISIBLE
        }
    }

    private fun resetTimer(totalParts: Int) {
        calendar.set(Calendar.SECOND, totalParts)
        text = date.format(calendar.time)
        activeParts = totalParts
    }
}