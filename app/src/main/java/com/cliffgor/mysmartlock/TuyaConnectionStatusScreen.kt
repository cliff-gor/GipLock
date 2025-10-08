package com.cliffgor.mysmartlock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.sdk.bean.DeviceBean
import com.thingclips.smart.home.sdk.callback.IThingGetHomeListCallback
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import android.util.Log
import androidx.compose.ui.tooling.preview.Preview
import com.thingclips.smart.home.sdk.callback.IThingResultCallback
import com.thingclips.smart.optimus.lock.api.IThingLockManager
import com.thingclips.smart.optimus.lock.api.TempPasswordBuilder
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk
import com.thingclips.smart.sdk.api.IResultCallback
import com.thingclips.thinglock.videolock.bean.OnlineTempPassword
import org.json.JSONObject


@Preview(
    backgroundColor = 0xFFFFFFFF,
)
@Composable
fun TuyaConnectionStatusScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
    onAddDevice: () -> Unit = {}
) {
    var isConnected by remember { mutableStateOf<Boolean?>(null) }
    var devices by remember { mutableStateOf<List<DeviceBean>>(emptyList()) }
    var isLoadingDevices by remember { mutableStateOf(false) }
    var deviceError by remember { mutableStateOf<String?>(null) }
    var expandedDeviceId by remember { mutableStateOf<String?>(null) }
    val isSdkInitialized by remember { mutableStateOf(true) }

    // Define loadDevices function at composable level
    fun loadDevices() {
        isLoadingDevices = true
        deviceError = null

        ThingHomeSdk.getHomeManagerInstance().queryHomeList(object : IThingGetHomeListCallback {
            override fun onSuccess(homeList: MutableList<HomeBean>?) {
                val home = homeList?.firstOrNull()
                if (home != null) {
                    ThingHomeSdk.newHomeInstance(home.homeId).getHomeDetail(object : IThingHomeResultCallback {
                        override fun onSuccess(homeBean: HomeBean?) {
                            isLoadingDevices = false
                            val deviceList = homeBean?.deviceList ?: emptyList()
                            devices = deviceList
                            Log.d("TuyaDevices", "Loaded ${deviceList.size} devices")


                            // Enhanced logging for device status
                            Log.d("TuyaDevices", "Loaded ${deviceList.size} devices")
                            deviceList.forEach { device ->
                                Log.d("DeviceStatus",
                                    "Device: ${device.name ?: "Unnamed"} | " +
                                            "ID: ${device.devId.take(8)}... | " +
                                            "Online: ${device.isOnline} | " +
                                            "Product ID: ${device.productId}"
                                )
                            }
                        }

                        override fun onError(errorCode: String?, errorMsg: String?) {
                            isLoadingDevices = false
                            deviceError = "Error loading devices: $errorCode - $errorMsg"
                            Log.e("TuyaDevices", "Error loading devices: $errorCode - $errorMsg")
                        }
                    })
                } else {
                    isLoadingDevices = false
                    deviceError = "No home found. Please create a home first."
                    Log.w("TuyaDevices", "No home found for loading devices")
                }
            }

            override fun onError(errorCode: String?, errorMsg: String?) {
                isLoadingDevices = false
                deviceError = "Error querying homes: $errorCode - $errorMsg"
                Log.e("TuyaDevices", "Error querying homes: $errorCode - $errorMsg")
            }
        })
    }

    LaunchedEffect(isSdkInitialized) {
        if (isSdkInitialized) {
            try {
                val user = ThingHomeSdk.getUserInstance()?.user
                isConnected = user != null
                Log.d("TuyaDebug", "User logged in: ${user != null}")

                // Load devices when connected
                if (user != null) {
                    loadDevices()
                }
            } catch (e: Exception) {
                Log.e("TuyaConnection", "Error checking Tuya connection", e)
                isConnected = false
            }
        }
    }

    LaunchedEffect(isSdkInitialized) {
        if (isSdkInitialized) {
            try {
                // Test if we can access the lock manager
                val lockManager = ThingOptimusSdk.getManager(IThingLockManager::class.java)
                if (lockManager != null) {
                    Log.d("LockTest", "✓ Lock manager accessible")
                } else {
                    Log.e("LockTest", "✗ Lock manager is null")
                }
            } catch (e: Exception) {
                Log.e("LockTest", "✗ Error accessing lock manager: ${e.message}")
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Connection Status Section
        when {
            !isSdkInitialized -> StatusIndicator("Initializing SDK...", Color.Gray)
            isConnected == true -> StatusIndicator("Connected to Tuya!", Color(0xFF00C853))
            isConnected == false -> StatusIndicator("Not connected to Tuya.", Color(0xFFD50000))
            else -> StatusIndicator("Checking connection...", Color.Gray)
        }

        Spacer(Modifier.height(16.dp))

        // Action Buttons
        Button(
            onClick = onAddDevice,
            enabled = isSdkInitialized && (isConnected == true)
        ) {
            Text("Add Device")
        }

        Spacer(Modifier.height(8.dp))

        // Refresh Devices Button
        Button(
            onClick = {
                if (isConnected == true) {
                    loadDevices()
                }
            },
            enabled = isSdkInitialized && (isConnected == true)
        ) {
            Text("Refresh Devices")
        }

        Spacer(Modifier.height(16.dp))

        // Devices List Section
        if (isConnected == true) {
            if (isLoadingDevices) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text("Loading devices...")
                }
            } else if (deviceError != null) {
                Text(deviceError!!, color = MaterialTheme.colorScheme.error)
            } else {
                Text(
                    "My Devices (${devices.size})",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(8.dp))

                if (devices.isEmpty()) {
                    Text("No devices found. Add your first device!")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 500.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(devices) { device ->
                            ExpandableDeviceItem(
                                device = device,
                                isExpanded = expandedDeviceId == device.devId,
                                onToggleExpand = {
                                    expandedDeviceId = if (expandedDeviceId == device.devId) null else device.devId
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = onLogout) {
            Text("Logout")
        }
    }
}


@Composable
fun ExpandableDeviceItem(
    device: DeviceBean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val dps = device.dps // Access device data points
    val batteryLevel = dps["145"] as? Int ?: -1
    val powerSource = dps["146"] as? Int ?: -1

    val  lockManager = ThingOptimusSdk.getManager(IThingLockManager::class.java)
    val thingLockDevice = lockManager?.getWifiLock(device.devId)
    var isLockOperating by remember { mutableStateOf(false) }
    var lockOperationError by remember { mutableStateOf<String?>(null) }


//    LaunchedEffect(device) {
//        Log.d("DeviceDebug", "Device DPs: ${device.dps}")
//        Log.d("DeviceDebug", "Device Model: ${device.productId}")
//        // The schemaList property might not be available in your current SDK version.
//        // device.schemaList?.forEach { schema ->
//        //    Log.d("DeviceDebug", "Schema: ${schema.id} - Mode: ${schema.mode} - Type: ${schema.type} - Name: ${schema.name}")
//        // }
//
//        // Alternative: Try to use getDpCodes if available
//        try {
//            // val dpCodes = device.getDpCodes() // If this method exists
//            // Log.d("DeviceDebug", "DP Codes: $dpCodes")
//        } catch (e: Exception) {
//            Log.d("DeviceDebug", "Alternative method also failed: ${e.message}")
//        }
//    }

    // ADD THIS: Real-time device status monitoring
    LaunchedEffect(device.isOnline) {
        Log.d("DeviceStatus",
            "Device ${device.name ?: "Unnamed"} online status changed: ${device.isOnline}"
        )

        // Additional check: Try to get fresh device status
        try {
            val freshDevice = ThingHomeSdk.getDataInstance()?.getDeviceBean(device.devId)
            freshDevice?.let {
                Log.d("DeviceStatus",
                    "Fresh device check - Online: ${it.isOnline}, " +
                            "Network: ${it.isLocalOnline}, " +
                            "IP: ${it.ip ?: "Unknown"}"
                )
            }
        } catch (e: Exception) {
            Log.e("DeviceStatus", "Error getting fresh device status: ${e.message}")
        }
    }

    LaunchedEffect(thingLockDevice) {
        if (thingLockDevice != null) {
            Log.d("LockDebug", "IThingWifiLock methods available")
            // You can check the available methods in Android Studio by typing:
            // thingLockDevice. (with the dot) and see what autocomplete suggests
        }
    }

    Card(
        onClick = onToggleExpand,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Device header (always visible)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.name ?: "Unnamed Device",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${device.devId.take(8)}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (device.isOnline) "🟢 Online" else "🔴 Offline",
                        color = if (device.isOnline) Color(0xFF00C853) else Color(0xFFD50000),
                        style = MaterialTheme.typography.bodySmall
                    )
                    BatteryStatusDisplay(batteryLevel, powerSource)

                }
//                Icon(
//                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
//                    contentDescription = if (isExpanded) "Collapse" else "Expand"
//                )
            }

            // Expanded section with controls
            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Divider()

                    // Battery Details
                    if (batteryLevel != -1) {
                        Text("Battery Details", style = MaterialTheme.typography.titleSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Battery Level:")
                            Text("$batteryLevel%", fontWeight = FontWeight.Medium)
                        }
                        if (powerSource != -1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Power Source:")
                                Text(
                                    if (powerSource == 0) "Battery" else "Mains Power",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // PIN Management Section
                    Text("PIN Management", style = MaterialTheme.typography.titleSmall)

                    var newPin by remember { mutableStateOf("") }
                    var isGeneratingPin by remember { mutableStateOf(false) }
                    var pinError by remember { mutableStateOf<String?>(null) }

                    Button(
                        onClick = {
                            isGeneratingPin = true
                            pinError = null

                            // Generate 6-digit random PIN
                            val randomPin = "%07d".format((0..999999).random())
                            val uniqueName = "Guest ${java.text.SimpleDateFormat("HH:mm").format(java.util.Date())}"
                            try {
                                // Create and configure TempPasswordBuilder using method chaining
                                val tempPasswordBuilder = TempPasswordBuilder()
                                    .password(randomPin)                    // Required: 7-digit password
                                    .name(uniqueName)                      // Required: Name for the password
                                    .effectiveTime(System.currentTimeMillis()) // Required: Start time (milliseconds)
                                    .invalidTime(System.currentTimeMillis() + (2 * 60 * 60 * 1000)) // Required: End time (2 hours from now)
                                    .countryCode("254")                     // Optional: Country code
                                    .phone("")                              // Optional: Phone number

                                // Use the correct method signature
                                thingLockDevice?.createTempPassword(
                                    tempPasswordBuilder,
                                    object : IThingResultCallback<Boolean> {
                                        override fun onSuccess(result: Boolean) {
                                            isGeneratingPin = false
                                            newPin = randomPin
                                            Log.d("LockPIN", "Temporary PIN created successfully: $randomPin")
                                        }

                                        override fun onError(errorCode: String, errorMessage: String) {
                                            isGeneratingPin = false
                                            pinError = "Failed to create PIN: $errorCode - $errorMessage"
                                            Log.e("LockPIN", "Failed to create PIN: $errorCode - $errorMessage")
                                        }
                                    }
                                ) ?: run {
                                    isGeneratingPin = false
                                    pinError = "Lock device not available"
                                }
                            } catch (e: Exception) {
                                isGeneratingPin = false
                                pinError = "Error: ${e.message}"
                                Log.e("LockPIN", "Exception creating temp password: ${e.message}")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isGeneratingPin && thingLockDevice != null
                    ) {
                        if (isGeneratingPin) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Generating...")
                        } else {
                            Text("Generate Temporary PIN")
                        }
                    }

                    // Show error if any
                    if (pinError != null) {
                        Text(
                            pinError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (newPin.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("New PIN Generated:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                newPin,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Valid for 2 hours",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Lock Control Section
                    Text("Lock Control", style = MaterialTheme.typography.titleSmall)

                    if (lockOperationError != null) {
                        Text(
                            lockOperationError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (device.isOnline && !isLockOperating) {
                                    isLockOperating = true
                                    lockOperationError = null
                                    controlLock(device.devId, true)
                                    // Consider adding a delay before resetting isLockOperating
                                    // or reset it in the callback if you modify controlLock
                                } else if (!device.isOnline) {
                                    lockOperationError = "Device is offline"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isLockOperating && device.isOnline
                        ) {
                            if (isLockOperating) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Locking...")
                            } else {
                                Text("Lock")
                            }
                        }

                        Button(
                            onClick = {
                                if (device.isOnline && !isLockOperating) {
                                    isLockOperating = true
                                    lockOperationError = null
                                    controlLock(device.devId, false)
                                } else if (!device.isOnline) {
                                    lockOperationError = "Device is offline"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isLockOperating && device.isOnline
                        ) {
                            if (isLockOperating) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Unlocking...")
                            } else {
                                Text("Unlock")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BatteryStatusDisplay(batteryLevel: Int, powerSource: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Battery level indicator with color coding
        val (batteryColor, statusText) = when {
            batteryLevel == -1 -> Pair(Color.Gray, "Unknown")
            batteryLevel > 50 -> Pair(Color(0xFF00C853), "$batteryLevel%")
            batteryLevel > 20 -> Pair(Color(0xFFFFC107), "$batteryLevel%")
            else -> Pair(Color(0xFFD50000), "$batteryLevel%")
        }

        // Simple battery indicator
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(batteryColor, shape = MaterialTheme.shapes.small)
        )

        Text(
            text = statusText,
            style = MaterialTheme.typography.bodySmall
        )

        // Power source indicator
        if (powerSource != -1) {
            Text(
                text = if (powerSource == 0) "🔋" else "⚡",
                style = MaterialTheme.typography.bodySmall
            )
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

// Helper functions for device control

private fun controlLock(deviceId: String, lock: Boolean) {
    try {
        val device = ThingHomeSdk.newDeviceInstance(deviceId)

        // Test 1: DP "1" as Boolean (Most common)
        // val dps = hashMapOf("1" to lock)

        // Test 2: DP "101" as String
        // val dps = hashMapOf("101" to if (lock) "lock" else "unlock")

        // Test 3: DP "102" as Integer (Enum)
        // val dps = hashMapOf("102" to if (lock) 1 else 0)

        // Test 4: DP "66" as Boolean (from your device log)
        val dps = hashMapOf("66" to lock)

        Log.d("LockControl", "Sending command: ${JSONObject(dps)} to device: $deviceId")

        device.publishDps(JSONObject(dps).toString(), object : IResultCallback {
            override fun onSuccess() {
                Log.d("LockControl", "SUCCESS! Correct DP found.")
            }
            override fun onError(code: String, error: String?) {
                Log.e("LockControl", "Failed with DP ${JSONObject(dps)} - Code: $code")
            }
        })
    } catch (e: Exception) {
        Log.e("LockControl", "Exception: ${e.message}")
    }
}