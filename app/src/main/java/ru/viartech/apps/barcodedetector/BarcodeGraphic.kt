package ru.viartech.apps.barcodedetector

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import kotlin.math.abs

class BarcodeGraphic(overlay: GraphicOverlay) : GraphicOverlay.Graphic(overlay) {
    @Volatile
    private var _barcode: FirebaseVisionBarcode? = null

    private val _boxPaint: Paint = Paint()
    private val _filledBoxPaint = Paint()

    init {
        _boxPaint.color = Color.RED
        _boxPaint.style = Paint.Style.STROKE
        _boxPaint.strokeWidth = BOX_STROKE_WIDTH

        _filledBoxPaint.color = Color.WHITE
        _filledBoxPaint.style = Paint.Style.FILL
    }

    fun updateBarcode(barcode: FirebaseVisionBarcode) {
        _barcode = barcode
        postInvalidate()
    }

    override fun draw(canvas: Canvas) {
        if (_barcode == null) return

        val rect = _barcode!!.cornerPoints!!

        val x1 = translateX(rect[0].x.toFloat())
        val y1 = translateY(rect[0].y.toFloat())

        val x2 = translateX(rect[1].x.toFloat())
        val y2 = translateY(rect[1].y.toFloat())

        val x3 = translateX(rect[2].x.toFloat())
        val y3 = translateY(rect[2].y.toFloat())

        val x4 = translateX(rect[3].x.toFloat())
        val y4 = translateY(rect[3].y.toFloat())

        drawScaledTrapeze(canvas, _filledBoxPaint, 1.4f, 1.2f,
                x1, y1, x2, y2, x3, y3, x4, y4)

        drawTrapeze(canvas, _boxPaint, x1, y1, x2, y2, x3, y3, x4, y4)

    }

    private fun drawScaledTrapeze(canvas: Canvas, paint: Paint, scaleX: Float, scaleY: Float,
                    x1: Float, y1: Float, x2: Float, y2: Float,
                    x3: Float, y3: Float, x4: Float, y4: Float) {

        val topOffset = abs(x1- x2) * (scaleX - 1f) / 2f
        val bottomOffset = abs(x3- x4) * (scaleX - 1f) / 2f

        val leftOffset = abs(y1 - y4) * (scaleY - 1f) / 2f
        val rightOffset = abs(y2- y3) * (scaleY - 1f) / 2f

        val path = Path()
        path.moveTo(x1 - topOffset, y1 - leftOffset)
        path.lineTo(x2 + topOffset, y2 - rightOffset)
        path.lineTo(x3 + bottomOffset, y3 + rightOffset)
        path.lineTo(x4 - bottomOffset, y4 + leftOffset)
        path.close()

        canvas.drawPath(path, paint)
    }

    private fun drawTrapeze(canvas: Canvas, paint: Paint,
                                  x1: Float, y1: Float, x2: Float, y2: Float,
                                  x3: Float, y3: Float, x4: Float, y4: Float) {
        val path = Path()
        path.moveTo(x1, y1)
        path.lineTo(x2, y2)
        path.lineTo(x3, y3)
        path.lineTo(x4, y4)
        path.close()

        canvas.drawPath(path, paint)
    }

    private fun drawCircles(canvas: Canvas, paint: Paint, radius: Float,
                            x1: Float, y1: Float, x2: Float, y2: Float,
                            x3: Float, y3: Float, x4: Float, y4: Float) {
        canvas.drawCircle(x1, y1, radius, paint)
        canvas.drawCircle(x2, y2, radius, paint)
        canvas.drawCircle(x3, y3, radius, paint)
        canvas.drawCircle(x4, y4, radius, paint)
    }

    companion object {
        private const val BOX_STROKE_WIDTH = 2.0f
    }
}