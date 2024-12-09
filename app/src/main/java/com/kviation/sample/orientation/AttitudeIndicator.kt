package com.kviation.sample.orientation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.View

class AttitudeIndicator @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) :
    View(context, attrs) {
    private val mXfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    private val mBitmapPaint = Paint()
    private val mEarthPaint: Paint
    private val mPitchLadderPaint: Paint
    private val mMinPlanePaint: Paint
    private val mBottomPitchLadderPaint: Paint

    // These are created once and reused in subsequent onDraw calls.
    private var mSrcBitmap: Bitmap? = null
    private var mSrcCanvas: Canvas? = null
    private var mDstBitmap: Bitmap? = null

    private var mWidth = 0
    private var mHeight = 0

    var pitch: Float = 0f // Degrees
        private set
    var roll: Float = 0f // Degrees, left roll is positive
        private set

    fun setAttitude(pitch: Float, roll: Float) {
        this.pitch = pitch
        this.roll = roll
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
    }

    private val src: Bitmap
        get() {
            if (mSrcBitmap == null) {
                mSrcBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
                mSrcCanvas = Canvas(mSrcBitmap!!)
            }
            val canvas = mSrcCanvas

            val centerX = (mWidth / 2).toFloat()
            val centerY = (mHeight / 2).toFloat()

            // Background
            canvas!!.drawColor(SKY_COLOR)

            // Save the state without any rotation/translation so
            // we can revert back to it to draw the fixed components.
            canvas.save()

            // Orient the earth to reflect the pitch and roll angles
            canvas.rotate(roll, centerX, centerY)
            canvas.translate(0f, (pitch / TOTAL_VISIBLE_PITCH_DEGREES) * mHeight)

            // Draw the earth as a rectangle, well beyond the view bounds
            // to account for large nose-down pitch.
            canvas.drawRect(-mWidth.toFloat(), centerY, (mWidth * 2).toFloat(), (mHeight * 2).toFloat(), mEarthPaint)

            // Draw white horizon and top pitch ladder
            val ladderStepY = (mHeight / 12).toFloat()
            canvas.drawLine(-mWidth.toFloat(), centerY, (mWidth * 2).toFloat(), centerY, mPitchLadderPaint)
            for (i in 1..4) {
                val y = centerY - ladderStepY * i
                val width = (mWidth / 8).toFloat()
                canvas.drawLine(centerX - width / 2, y, centerX + width / 2, y, mPitchLadderPaint)
            }

            // Draw the bottom pitch ladder
            val bottomLadderStepX = (mWidth / 12).toFloat()
            val bottomLadderStepY = (mWidth / 12).toFloat()
            canvas.drawLine(
                centerX, centerY, centerX - bottomLadderStepX * 3.5f, centerY
                        + bottomLadderStepY * 3.5f, mBottomPitchLadderPaint
            )
            canvas.drawLine(
                centerX, centerY, centerX + bottomLadderStepX * 3.5f, centerY
                        + bottomLadderStepY * 3.5f, mBottomPitchLadderPaint
            )
            for (i in 1..3) {
                val y = centerY + bottomLadderStepY * i
                canvas.drawLine(
                    centerX - bottomLadderStepX * i, y, centerX + bottomLadderStepX * i, y,
                    mBottomPitchLadderPaint
                )
            }

            // Return to normal to draw the miniature plane
            canvas.restore()

            // Draw the nose dot
            canvas.drawPoint(centerX, centerY, mMinPlanePaint)

            // Half-circle of miniature plane
            val minPlaneCircleRadiusX = (mWidth / 6).toFloat()
            val minPlaneCircleRadiusY = (mHeight / 6).toFloat()
            val wingsCircleBounds = RectF(
                centerX - minPlaneCircleRadiusX, centerY
                        - minPlaneCircleRadiusY, centerX + minPlaneCircleRadiusX, centerY + minPlaneCircleRadiusY
            )
            canvas.drawArc(wingsCircleBounds, 0f, 180f, false, mMinPlanePaint)

            // Wings of miniature plane
            val wingLength = (mWidth / 6).toFloat()
            canvas.drawLine(
                centerX - minPlaneCircleRadiusX - wingLength, centerY, centerX
                        - minPlaneCircleRadiusX, centerY, mMinPlanePaint
            )
            canvas.drawLine(
                centerX + minPlaneCircleRadiusX, centerY, (centerX + minPlaneCircleRadiusX
                        + wingLength), centerY, mMinPlanePaint
            )

            // Draw vertical post
            canvas.drawLine(
                centerX, centerY + minPlaneCircleRadiusY, centerX, (centerY
                        + minPlaneCircleRadiusY + mHeight / 3), mMinPlanePaint
            )

            return mSrcBitmap!!
        }

    private val dst: Bitmap
        get() {
            if (mDstBitmap == null) {
                mDstBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
                val c = Canvas(mDstBitmap!!)
                c.drawColor(Color.TRANSPARENT)

                val p = Paint(Paint.ANTI_ALIAS_FLAG)
                p.color = Color.RED
                c.drawOval(RectF(0f, 0f, mWidth.toFloat(), mHeight.toFloat()), p)
            }
            return mDstBitmap!!
        }

    override fun onDraw(canvas: Canvas) {
        if (LOG_FPS) {
            countFps()
        }

        val src = src
        val dst = dst

        val sc = saveLayer(canvas)
        canvas.drawBitmap(dst, 0f, 0f, mBitmapPaint)
        mBitmapPaint.setXfermode(mXfermode)
        canvas.drawBitmap(src, 0f, 0f, mBitmapPaint)
        mBitmapPaint.setXfermode(null)

        canvas.restoreToCount(sc)
    }

    private fun saveLayer(canvas: Canvas): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canvas.saveLayer(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), null)
        } else {
            canvas.saveLayer(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), null, Canvas.ALL_SAVE_FLAG)
        }
    }

    private var frameCountStartedAt: Long = 0
    private var frameCount: Long = 0

    init {
        mBitmapPaint.isFilterBitmap = false

        mEarthPaint = Paint()
        mEarthPaint.isAntiAlias = true
        mEarthPaint.color = EARTH_COLOR

        mPitchLadderPaint = Paint()
        mPitchLadderPaint.isAntiAlias = true
        mPitchLadderPaint.color = Color.WHITE
        mPitchLadderPaint.strokeWidth = 3f

        mBottomPitchLadderPaint = Paint()
        mBottomPitchLadderPaint.isAntiAlias = true
        mBottomPitchLadderPaint.color = Color.WHITE
        mBottomPitchLadderPaint.strokeWidth = 3f
        mBottomPitchLadderPaint.alpha = 128

        mMinPlanePaint = Paint()
        mMinPlanePaint.isAntiAlias = true
        mMinPlanePaint.color = MIN_PLANE_COLOR
        mMinPlanePaint.strokeWidth = 5f
        mMinPlanePaint.style = Paint.Style.STROKE
    }

    private fun countFps() {
        frameCount++
        if (frameCountStartedAt == 0L) {
            frameCountStartedAt = System.currentTimeMillis()
        }
        val elapsed = System.currentTimeMillis() - frameCountStartedAt
        if (elapsed >= 1000) {
            LogUtil.i("FPS: $frameCount")
            frameCount = 0
            frameCountStartedAt = System.currentTimeMillis()
        }
    }

    companion object {
        private const val LOG_FPS = false

        private val SKY_COLOR = Color.parseColor("#36B4DD")
        private val EARTH_COLOR = Color.parseColor("#865B4B")
        private val MIN_PLANE_COLOR = Color.parseColor("#E8D4BB")
        private const val TOTAL_VISIBLE_PITCH_DEGREES = (45 * 2 // � 45�
                ).toFloat()
    }
}
