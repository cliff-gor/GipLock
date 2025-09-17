package com.cliffgor.mysmartlock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cliffgor.mysmartlock.ui.theme.MySmartLockTheme
import com.thingclips.smart.home.sdk.ThingHomeSdk

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MySmartLockTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavHost()
                }
            }
        }
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val isSdkInitialized by LockTuyaApplication.isInitialized

    if (!isSdkInitialized) {
        // You can customize this with your own splash screen if you wish
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val isLoggedIn = remember { mutableStateOf(ThingHomeSdk.getUserInstance()?.user != null) }
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn.value) "main" else "login"
    ) {
        composable("login") {
            TuyaLoginScreen(
                onLoginSuccess = {
                    isLoggedIn.value = true
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("main") {
            TuyaConnectionStatusScreen()
        }
    }
}