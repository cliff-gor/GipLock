package com.cliffgor.mysmartlock

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.thingclips.smart.android.user.api.ILoginCallback
import com.thingclips.smart.android.user.bean.User
import com.thingclips.smart.home.sdk.ThingHomeSdk

@Composable
fun TuyaLoginScreen(
    onLoginSuccess: () -> Unit
) {
    var countryCode by remember { mutableStateOf("254") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun performLogin() {
        if (phone.isBlank() || password.isBlank()) {
            error = "Please enter phone and password"
            return
        }
        isLoading = true
        error = null

        ThingHomeSdk.getUserInstance().loginWithPhone(
            countryCode,
            phone,
            password,
            object : ILoginCallback {
                override fun onSuccess(user: User?) {
                    isLoading = false
                    onLoginSuccess()
                }
                override fun onError(code: String, errMsg: String) {
                    isLoading = false
                    error = "Login failed: $errMsg"
                    Log.e("TuyaLogin", "Login error: $code - $errMsg")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Tuya Login",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        OutlinedTextField(
            label = { Text("Country Code") },
            value = countryCode,
            onValueChange = { countryCode = it },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            label = { Text("Phone Number") },
            value = phone,
            onValueChange = { phone = it },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            label = { Text("Password") },
            value = password,
            onValueChange = { password = it },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { performLogin() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Logging in...")
            } else {
                Text("Login")
            }
        }
        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}