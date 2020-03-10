package com.bilikdavid.cameraxsample

import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig

/**
 * TODO add class description
 */
class App : Application(), CameraXConfig.Provider {

    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }
}