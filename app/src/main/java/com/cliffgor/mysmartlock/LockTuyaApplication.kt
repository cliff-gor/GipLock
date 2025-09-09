package com.cliffgor.mysmartlock

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.thingclips.smart.home.sdk.ThingHomeSdk

class LockTuyaApplication : Application() {

    companion object {
        var isInitialized = mutableStateOf(false)
            private set
    }


    override fun onCreate() {
        super.onCreate()

        try {
            // Attempt to load Tuya’s native security lib
            System.loadLibrary("ThingSmartSecurity")
            Log.d("LockTuyaApp", "✅ ThingSmartSecurity.so loaded")

            Log.d("LockTuyaApp", "Initializing ThingHomeSdk")
            ThingHomeSdk.init(this)
            Log.d("LockTuyaApp", "ThingHomeSdk initialized")
            isInitialized.value = true
        } catch (e: UnsatisfiedLinkError) {
            Log.e("LockTuyaApp", "❌ Failed to load ThingSmartSecurity.so or initialize ThingHomeSdk", e)
        }
    }
}