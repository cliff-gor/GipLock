package com.cliffgor.mysmartlock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cliffgor.mysmartlock.LockTuyaApplication
import com.thingclips.smart.home.sdk.ThingHomeSdk

@Composable
fun TuyaConnectionStatusScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {} // Optional: for demonstrating navigation back to login
) {
    var isConnected by remember { mutableStateOf<Boolean?>(null) }
    val isSdkInitialized by LockTuyaApplication.isInitialized

    LaunchedEffect(isSdkInitialized) {
        if (isSdkInitialized) {
            try {
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                !isSdkInitialized -> StatusIndicator("Initializing SDK...", Color.Gray)
                isConnected == true -> StatusIndicator("Connected to Tuya!", Color(0xFF00C853))
                isConnected == false -> StatusIndicator("Not connected to Tuya.", Color(0xFFD50000))
                else -> StatusIndicator("Checking connection...", Color.Gray)
            }
            Spacer(Modifier.height(32.dp))
            Button(onClick = onLogout) {
                Text("Logout")
            }
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