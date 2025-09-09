package com.cliffgor.mysmartlock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cliffgor.mysmartlock.ui.theme.MySmartLockTheme
import com.thingclips.smart.home.sdk.ThingHomeSdk
import kotlin.isInitialized

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MySmartLockTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TuyaConnectionStatusScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun TuyaConnectionStatusScreen(modifier: Modifier = Modifier) {
    var isConnected by remember { mutableStateOf<Boolean?>(null) }
    val isSdkInitialized by LockTuyaApplication.isInitialized

    LaunchedEffect(isSdkInitialized) {
        if (isSdkInitialized) {
            try {
                // Try a harmless SDK call to test connection
                val user = ThingHomeSdk.getUserInstance()?.user
                isConnected = user != null
            } catch (e: Exception) {
                android.util.Log.e("LockTuya", "Error checking Tuya connection", e)
                isConnected = false
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            !isSdkInitialized -> StatusIndicator("Initializing SDK...", Color.Gray)
            isConnected == true -> StatusIndicator("Connected to Tuya!", Color(0xFF00C853)) // Green
            isConnected == false -> StatusIndicator("Not connected to Tuya.", Color(0xFFD50000)) // Red
            else -> StatusIndicator("Checking connection...", Color.Gray)
        }
    }
}

@Composable
fun StatusIndicator(text: String, color: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(220.dp, 80.dp)
            .background(color = color, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Text(text, color = Color.White)
    }
}