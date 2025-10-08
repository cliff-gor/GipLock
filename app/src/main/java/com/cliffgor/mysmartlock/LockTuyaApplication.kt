package com.cliffgor.mysmartlock

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk

class LockTuyaApplication : Application() {

    companion object {
        var isInitialized = mutableStateOf(false)
            private set
    }

    override fun onCreate() {
        super.onCreate()

        try {
            Log.d("LockTuyaApp", "Starting Tuya SDK initialization...")

            // 1. Initialize the core Tuya SDK
            ThingHomeSdk.init(this)
            Log.d("LockTuyaApp", "ThingHomeSdk initialized successfully")

            // 2. (Optional) Enable debug mode for development
            ThingHomeSdk.setDebugMode(true)
            Log.d("LockTuyaApp", "Debug mode enabled")

            // 3. Initialize the Optimus SDK with logging
            Log.d("LockTuyaApp", "Initializing ThingOptimusSdk...")
            ThingOptimusSdk.init(this)
            Log.d("LockTuyaApp", "ThingOptimusSdk initialized successfully")

            // 4. Verify both SDKs are working
            Log.d("LockTuyaApp", "All Tuya SDKs initialized and ready")
            isInitialized.value = true

        } catch (e: Exception) {
            Log.e("LockTuyaApp", "Failed to initialize Tuya SDKs", e)
            // You might want to track which SDK failed
            isInitialized.value = false
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d("LockTuyaApp", "Cleaning up Tuya SDKs...")
        ThingHomeSdk.onDestroy()
    }
}