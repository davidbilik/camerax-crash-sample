package com.bilikdavid.cameraxsample

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null
    lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    companion object {

        const val REQUEST_PERMISSION_CAMERA = 12
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
            imageCapture?.takePicture(Executors.newSingleThreadExecutor(), object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    Log.d("MainActivity", "Image captured: ${image.width}x${image.height}")
                }
            })
        }
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
        val preview: Preview = Preview.Builder()
            .build()

        preview.setSurfaceProvider(preview_view.previewSurfaceProvider)

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetRotation(windowManager.defaultDisplay.rotation)
            .build()

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
    }
}
