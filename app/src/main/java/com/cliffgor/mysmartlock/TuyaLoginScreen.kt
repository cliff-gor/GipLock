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
import com.thingclips.smart.android.user.api.IRegisterCallback
import com.thingclips.smart.sdk.api.IResultCallback
import com.thingclips.smart.android.user.bean.User
import com.thingclips.smart.home.sdk.ThingHomeSdk


@Composable
fun TuyaLoginScreen(
    onLoginSuccess: () -> Unit
) {
    var isRegister by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var codeSent by remember { mutableStateOf(false) }

    fun performLogin() {
        if (email.isBlank() || password.isBlank()) {
            error = "Please enter email and password"
            return
        }
        isLoading = true
        error = null

        ThingHomeSdk.getUserInstance().loginWithEmail(
            "254", // countryCode
            email,
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

    fun sendVerificationCode() {
        if (email.isBlank()) {
            error = "Please enter email address"
            return
        }
        isLoading = true
        error = null

        ThingHomeSdk.getUserInstance().sendVerifyCodeWithUserName(
            email,       // userName = email
            "",          // region not needed for email
            "254",          // countryCode not needed for email
            1,           // type 1 = email
            object : IResultCallback {
                override fun onSuccess() {
                    isLoading = false
                    codeSent = true
                    Log.d("TuyaLogin", "📩 Verification code sent to email")
                }

                override fun onError(code: String, errorMsg: String) {
                    isLoading = false
                    error = "Failed to send code: $errorMsg"
                    Log.e("TuyaLogin", "Send code error: $code - $errorMsg")
                }
            }
        )
    }


    fun performRegister() {
        if (email.isBlank() || password.isBlank() || verificationCode.isBlank()) {
            error = "Enter email, password, and code"
            return
        }
        isLoading = true
        error = null

        ThingHomeSdk.getUserInstance().registerAccountWithEmail(
            "254",  // Kenya code
            email,
            password,
            verificationCode,
            object : IRegisterCallback {
                override fun onSuccess(user: User?) {
                    isLoading = false
                    onLoginSuccess()
                    Log.d("TuyaLogin", "✅ Registered user: ${user?.uid}")
                }

                override fun onError(code: String, errMsg: String) {
                    isLoading = false
                    error = "Register failed: $errMsg"
                    Log.e("TuyaLogin", "Register error: $code - $errMsg")
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
            text = if (isRegister) "Register" else "Login",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            label = { Text("Email Address") },
            value = email,
            onValueChange = { email = it },
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

        if (isRegister) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                label = { Text("Verification Code") },
                value = verificationCode,
                onValueChange = { verificationCode = it },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { sendVerificationCode() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Send Code")
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (isRegister) performRegister() else performLogin()
            },
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
                Text(if (isRegister) "Registering..." else "Logging in...")
            } else {
                Text(if (isRegister) "Register" else "Login")
            }
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = { isRegister = !isRegister }) {
            Text(
                text = if (isRegister) "Already have an account? Login" else "New user? Register"
            )
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
