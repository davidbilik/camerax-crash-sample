package com.bilikdavid.cameraxsample

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null
    lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    lateinit var previewUseCase: Preview

    companion object {

        const val REQUEST_PERMISSION_CAMERA = 12

        private const val TAG = "CameraPreviewApp"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            bindUseCases()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), REQUEST_PERMISSION_CAMERA)

        }
        btn_take_picture.setOnClickListener {
            stopPreview()
            val imageFile = File(cacheDir, "image.png")
            val fileOptions = ImageCapture.OutputFileOptions.Builder(imageFile)
                .build()
            imageCapture?.takePicture(fileOptions, Executors.newSingleThreadExecutor(), object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    try {
                        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                        Log.d(TAG, "Bitmap retrieved ${bitmap.width}x${bitmap.height}")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                }
            })
        }
    }

    fun stopPreview() {
        cameraProviderFuture.get().unbind(previewUseCase)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CAMERA && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            bindUseCases()
        }
    }

    private fun bindUseCases() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture
            .addListener(Runnable {
                val cameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider)
            }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        previewUseCase = Preview.Builder().build()

        previewUseCase.setSurfaceProvider(preview_view.previewSurfaceProvider)

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetRotation(windowManager.defaultDisplay.rotation)
            .build()

        val imageAnalysisUseCase = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(Executors.newSingleThreadExecutor(), ImageAnalyzer())
            }

        cameraProvider.bindToLifecycle(this, cameraSelector, previewUseCase, imageAnalysisUseCase, imageCapture)
    }

    class ImageAnalyzer : ImageAnalysis.Analyzer {
        override fun analyze(image: ImageProxy) {
            Log.d(TAG, "Analyzing image ${image.width}x${image.height}")

            image.close()
        }

    }
}
