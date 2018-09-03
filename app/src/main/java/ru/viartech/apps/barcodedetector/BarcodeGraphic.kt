package ru.viartech.apps.barcodedetector

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

class BarcodeGraphic(overlay: GraphicOverlay) : GraphicOverlay.Graphic(overlay) {
    @Volatile
    private var _barcode: FirebaseVisionBarcode? = null

    private val _boxPaint: Paint = Paint()
    private val _filledBoxPaint = Paint()

    init {
        _boxPaint.color = Color.WHITE
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

        drawTrapeze(canvas, _filledBoxPaint,
                x1, y1,
                x2, y2,
                x3, y3,
                x4, y4)
    }

    companion object {
        private const val BOX_STROKE_WIDTH = 5.0f
    }

    private fun drawTrapeze(canvas: Canvas, paint: Paint,
                    x1: Float, y1: Float,
                    x2: Float, y2: Float,
                    x3: Float, y3: Float,
                    x4: Float, y4: Float) {

        val path = Path()
        path.moveTo(x1, y1)
        path.lineTo(x2, y2)
        path.lineTo(x3, y3)
        path.lineTo(x4, y4)
        path.close()

        canvas.drawPath(path, paint)
    }
}