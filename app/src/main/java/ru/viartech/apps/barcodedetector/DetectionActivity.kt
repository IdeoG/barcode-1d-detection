package ru.viartech.apps.barcodedetector

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_detection.*


class DetectionActivity : AppCompatActivity() {
    private lateinit var _detector: FirebaseVisionBarcodeDetector
    private lateinit var _barcodeGraphic: BarcodeGraphic

    private var missedBarcodeFrames = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detection)
    }

    private fun checkCameraPermissions() {
        val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            startDetector()
            return
        }

        RxPermissions(this)
                .request(Manifest.permission.CAMERA)
                .subscribe {
                    if (!it) {
                        Log.i(TAG, "checkCameraPermissions: Permissions denied.")

                        finishAffinity()
                        return@subscribe
                    }

                    startDetector()
                }

    }

    private fun startDetector() {
        Log.i(TAG, "checkCameraPermissions: Permissions granted.")


        configureBarcodeDetector()
        configureCameraView()

        camera_view.start()
    }

    private fun configureCameraView() {
        _barcodeGraphic = BarcodeGraphic(camera_overlay)

        camera_view.addFrameProcessor {

            Log.d(TAG, "configureCameraView: frame = ${it.format}, ${it.rotation}")

            if (it.format == -1) {
                Log.d(TAG, "configureCameraView: invalid format")
                return@addFrameProcessor
            }

            val meta = FirebaseVisionImageMetadata.Builder()
                    .setFormat(it.format)
                    .setHeight(it.size.height)
                    .setWidth(it.size.width)
                    .setRotation(it.rotation / 90)
                    .build()

            val firebaseImage = FirebaseVisionImage.fromByteArray(it.data, meta)


            _detector.detectInImage(firebaseImage)
                    .addOnSuccessListener { barcodes ->

                        for (barcode in barcodes) {
                            Log.i(TAG, "configureCameraView -> value = ${barcode.displayValue}, rect = ${barcode.boundingBox}")
                            _barcodeGraphic.updateBarcode(barcode = barcode)

                            if (barcode.boundingBox!!.width() / barcode.boundingBox!!.height().toFloat() > 5f) {
                                camera_overlay.clear()
                                camera_overlay.add(_barcodeGraphic)

                                missedBarcodeFrames = 0

                                this@DetectionActivity.runOnUiThread { barcode_value.text = barcode.displayValue }
                            }
                        }

                        if (barcodes.size == 0) {
                            missedBarcodeFrames++

                            if (missedBarcodeFrames > 2) {
                                missedBarcodeFrames = 0
                                camera_overlay.clear()
                            }
                            Log.i(TAG, "configureCameraView -> no value")
                            this@DetectionActivity.runOnUiThread { barcode_value.text = "" }
                        }
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                        Log.e(TAG, "configureCameraView -> something went wrong")
                    }


        }

        camera_view.visibility = View.VISIBLE
        camera_view.facing
    }

    private fun configureBarcodeDetector() {
        FirebaseApp.initializeApp(this)

        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_EAN_13)
                .build()

        _detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)
    }

    override fun onResume() {
        super.onResume()

        checkCameraPermissions()
    }

    override fun onPause() {
        super.onPause()

        camera_view.stop()
    }

    override fun onDestroy() {
        super.onDestroy()

        camera_view.destroy()
    }

    companion object {
        private const val TAG = "DetectionActivity"
    }
}
