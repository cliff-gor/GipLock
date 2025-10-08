package com.cliffgor.mysmartlock.activation

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingGetHomeListCallback
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import com.thingclips.smart.sdk.api.IThingActivator
import com.thingclips.smart.sdk.api.IThingActivatorGetToken
import com.thingclips.smart.sdk.api.IThingSmartActivatorListener
import com.thingclips.smart.sdk.bean.DeviceBean
import com.thingclips.smart.home.sdk.builder.ActivatorBuilder
import com.thingclips.smart.sdk.enums.ActivatorModelEnum
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Timeout in seconds (must be Long)
private const val WIFI_ACTIVATION_TIMEOUT_SEC = 120L
private const val MAX_LOG_LINES = 200

data class ActivationUiState(
    val isLoading: Boolean = false,
    val homeId: Long? = null,
    val ssid: String = "",
    val password: String = "",
    val logs: List<String> = emptyList(),
    val successDeviceId: String? = null,
    val error: String? = null,
    val currentToken: String? = null,
    val tokenTimestamp: Long = 0
)

class DeviceActivationViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(ActivationUiState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events = _events.asSharedFlow()

    private var activator: IThingActivator? = null

    init {
        ensureHomeSelected()
    }

    fun setWifi(ssid: String, password: String) {
        _state.value = _state.value.copy(ssid = ssid, password = password)
    }

    private fun emitEvent(event: String) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    private fun appendLog(line: String) {
        val new = _state.value.logs + line
        _state.value = _state.value.copy(logs = new.takeLast(MAX_LOG_LINES))
    }

    private fun clearLogs() {
        _state.value = _state.value.copy(logs = emptyList())
    }

    fun ensureHomeSelected() {
        viewModelScope.launch {
            appendLog("Loading homes...")
            ThingHomeSdk.getHomeManagerInstance().queryHomeList(
                object : IThingGetHomeListCallback {
                    override fun onSuccess(homeList: MutableList<HomeBean>?) {
                        val existing = homeList?.firstOrNull()
                        if (existing != null) {
                            appendLog("Selected existing home: ${existing.name} (${existing.homeId})")
                            _state.value = _state.value.copy(homeId = existing.homeId)
                        } else {
                            appendLog("No homes found. Creating default home...")
                            createDefaultHome()
                        }
                    }
                    override fun onError(code: String?, error: String?) {
                        val msg = "Query homes failed: $code $error"
                        appendLog(msg)
                        _state.value = _state.value.copy(error = msg)
                        emitEvent(msg)
                    }
                }
            )
        }
    }

    private fun createDefaultHome() {
        ThingHomeSdk.getHomeManagerInstance().createHome(
            "My Home",
            0.0,
            0.0,
            "",
            emptyList(),
            object : IThingHomeResultCallback {
                override fun onSuccess(bean: HomeBean?) {
                    val hid = bean?.homeId
                    appendLog("Created home: ${bean?.name} ($hid)")
                    _state.value = _state.value.copy(homeId = hid)
                }
                override fun onError(code: String?, error: String?) {
                    val msg = "Create home failed: $code $error"
                    appendLog(msg)
                    _state.value = _state.value.copy(error = msg)
                    emitEvent(msg)
                }
            }
        )
    }

    // Updated for Jetpack Compose - accepts context from Composable
    fun startEzActivation(context: Context) {
        stopActivation()

        val homeId = _state.value.homeId ?: run {
            val msg = "No home selected/created yet."
            _state.value = _state.value.copy(error = msg)
            appendLog(msg)
            emitEvent(msg)
            return
        }

        if (_state.value.ssid.isBlank() || _state.value.password.isBlank()) {
            val msg = "SSID and password are required"
            _state.value = _state.value.copy(error = msg)
            appendLog(msg)
            emitEvent(msg)
            return
        }

        _state.value = _state.value.copy(
            isLoading = true,
            error = null,
            successDeviceId = null
        )
        clearLogs()
        appendLog("Fetching activator token for home=$homeId ...")

        // Check if we have a valid token (10 minutes = 600000 milliseconds)
        val currentTime = System.currentTimeMillis()
        if (_state.value.currentToken != null &&
            (currentTime - _state.value.tokenTimestamp) < 600000) {
            appendLog("Using cached token (valid for 10 minutes)")
            startEzActivationWithToken(_state.value.currentToken!!, homeId, context)
        } else {
            getNewToken(homeId, context)
        }
    }

    private fun getNewToken(homeId: Long, context: Context) {
        ThingHomeSdk.getActivatorInstance().getActivatorToken(
            homeId,
            object : IThingActivatorGetToken {
                override fun onSuccess(token: String) {
                    appendLog("Got new token (valid for 10 minutes)")
                    _state.value = _state.value.copy(
                        currentToken = token,
                        tokenTimestamp = System.currentTimeMillis()
                    )
                    startEzActivationWithToken(token, homeId, context)
                }

                override fun onFailure(code: String, error: String) {
                    val errMsg = "Token error: $code $error"
                    _state.value = _state.value.copy(isLoading = false, error = errMsg)
                    appendLog(errMsg)
                    emitEvent(errMsg)
                }
            }
        )
    }

    private fun startEzActivationWithToken(token: String, homeId: Long, context: Context) {
        try {
            appendLog("Starting EZ activation with SSID: ${_state.value.ssid}")

            val builder = ActivatorBuilder()
                .setContext(context) // Use the context passed from Composable
                .setSsid(_state.value.ssid)
                .setPassword(_state.value.password)
                .setTimeOut(WIFI_ACTIVATION_TIMEOUT_SEC)
                .setActivatorModel(ActivatorModelEnum.THING_EZ)
                .setToken(token)
                .setListener(object : IThingSmartActivatorListener {
                    override fun onError(code: String, error: String) {
                        val msg = "EZ activator error: $code $error"
                        appendLog(msg)
                        _state.value = _state.value.copy(isLoading = false, error = msg)
                        emitEvent(msg)
                        stopActivation()
                    }

                    override fun onActiveSuccess(devResp: DeviceBean) {
                        val msg = "EZ device activated: ${devResp.name} (${devResp.devId})"
                        appendLog(msg)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            successDeviceId = devResp.devId
                        )
                        emitEvent("Activation successful: ${devResp.devId}")
                        // Token expires immediately after successful pairing
                        _state.value = _state.value.copy(currentToken = null, tokenTimestamp = 0)
                        stopActivation()
                    }

                    override fun onStep(step: String, data: Any) {
                        appendLog("EZ step: $step - ${data.toString()}")
                    }
                })

            activator = ThingHomeSdk.getActivatorInstance().newMultiActivator(builder)
            activator?.start()
            appendLog("EZ activation started with timeout: ${WIFI_ACTIVATION_TIMEOUT_SEC}s")
        } catch (e: Exception) {
            val msg = "Failed to start activation: ${e.message}"
            appendLog(msg)
            _state.value = _state.value.copy(isLoading = false, error = msg)
            emitEvent(msg)
        }
    }

    fun stopActivation() {
        try {
            activator?.stop()
            activator?.onDestroy()
            appendLog("Activation stopped and destroyed")
        } catch (e: Exception) {
            val msg = "Failed to stop activator: ${e.message}"
            appendLog(msg)
            emitEvent(msg)
        }
        activator = null
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun clearSuccess() {
        _state.value = _state.value.copy(successDeviceId = null)
    }

    fun invalidateToken() {
        _state.value = _state.value.copy(currentToken = null, tokenTimestamp = 0)
        appendLog("Token invalidated")
    }

    override fun onCleared() {
        super.onCleared()
        stopActivation()
    }
}