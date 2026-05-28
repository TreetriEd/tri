package com.example.ui.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.HomeNetwork
import com.example.data.db.NetworkLog
import com.example.data.repository.NetworkRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NetworkOptimizerViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val repository = NetworkRepository(AppDatabase.getDatabase(context))

    // Real-world Manager instances
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    // User settings
    val autoSwitchEnabled = repository.getSetting("AUTO_SWITCH_ENABLED", "true")
        .map { it.toBoolean() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val notificationsEnabled = repository.getSetting("NOTIFICATIONS_ENABLED", "true")
        .map { it.toBoolean() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val wifiThresholdSetting = repository.getSetting("THRESHOLD_WIFI_STRENGTH", "30")
        .map { it.toIntOrNull() ?: 30 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)

    val cellularThresholdSetting = repository.getSetting("THRESHOLD_CELLULAR_STRENGTH", "20")
        .map { it.toIntOrNull() ?: 20 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 20)

    // Db collections
    val homeNetworks = repository.homeNetworks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val logs = repository.logs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // App state: Simulation Mode selector
    val isSimulationMode = MutableStateFlow(true) // Start in simulation mode so users can instantly test on emulator!

    // SIMULATED STATE
    val simulatedWifiEnabled = MutableStateFlow(true)
    val simulatedWifiSsid = MutableStateFlow("CHONG_CHAROEN_WIFI")
    val simulatedWifiStrength = MutableStateFlow(85) // 0 - 100%

    val simulatedCellularEnabled = MutableStateFlow(true)
    val simulatedCellularCarrier = MutableStateFlow("AIS 5G")
    val simulatedCellularStrength = MutableStateFlow(70) // 0 - 100%

    val simulatedActiveNetwork = MutableStateFlow("Wi-Fi") // "Wi-Fi", "Cellular", "None"

    // REAL HARDWARE STATE (Observed dynamically)
    val realWifiEnabled = MutableStateFlow(false)
    val realWifiSsid = MutableStateFlow("None")
    val realWifiStrength = MutableStateFlow(0) // 0 - 100%

    val realCellularEnabled = MutableStateFlow(false)
    val realCellularCarrier = MutableStateFlow("Unknown")
    val realCellularStrength = MutableStateFlow(0) // 0 - 100%

    val realActiveNetwork = MutableStateFlow("None") // "Wi-Fi", "Cellular", "None"

    // Engine recommendation output
    private val _currentRecommendation = MutableStateFlow("Monitor Active: Signals Healthy")
    val currentRecommendation: StateFlow<String> = _currentRecommendation.asStateFlow()

    private var lastRecommendedAction = ""

    init {
        createNotificationChannel()
        startRealHardwareMonitoring()
        startAutoSwitcherJob()
    }

    // Toggle Settings
    fun toggleAutoSwitch(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveSetting("AUTO_SWITCH_ENABLED", enabled.toString())
            repository.insertLog("SYSTEM", "Auto-Switch Optimizer ${if (enabled) "ENABLED" else "DISABLED"}")
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveSetting("NOTIFICATIONS_ENABLED", enabled.toString())
            repository.insertLog("SYSTEM", "Notifications ${if (enabled) "ENABLED" else "DISABLED"}")
        }
    }

    fun updateWifiThreshold(threshold: Int) {
        viewModelScope.launch {
            repository.saveSetting("THRESHOLD_WIFI_STRENGTH", threshold.toString())
        }
    }

    fun updateCellularThreshold(threshold: Int) {
        viewModelScope.launch {
            repository.saveSetting("THRESHOLD_CELLULAR_STRENGTH", threshold.toString())
        }
    }

    // Manage Home Networks
    fun addHomeWifi(ssid: String, label: String) {
        viewModelScope.launch {
            val cleanSsid = ssid.removeSurrounding("\"")
            if (cleanSsid.isNotEmpty()) {
                repository.addHomeNetwork(cleanSsid, label)
                repository.insertLog("SYSTEM", "Added Home Wi-Fi profile: $cleanSsid ($label)")
            }
        }
    }

    fun removeHomeWifi(ssid: String) {
        viewModelScope.launch {
            repository.removeHomeNetwork(ssid)
            repository.insertLog("SYSTEM", "Removed Home Wi-Fi profile: $ssid")
        }
    }

    fun clearLogHistory() {
        viewModelScope.launch {
            repository.clearLogs()
            repository.insertLog("SYSTEM", "Logged network transition history cleared")
        }
    }

    // Simulated Actions
    fun triggerSimulatedWifiDrop() {
        simulatedWifiStrength.value = 0
        viewModelScope.launch {
            repository.insertLog("WIFI", "Triggered Simulation: Wi-Fi signal dropped unexpectedly to 0%", isSimulation = true)
        }
    }

    fun triggerSimulatedCellularDrop() {
        simulatedCellularStrength.value = 0
        viewModelScope.launch {
            repository.insertLog("CELLULAR", "Triggered Simulation: Cellular signal dropped unexpectedly to 0%", isSimulation = true)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startRealHardwareMonitoring() {
        // Track WiFi states
        realWifiEnabled.value = wifiManager?.isWifiEnabled ?: false

        // Register Network Callback to observe real connection shifts
        try {
            val builder = NetworkRequest.Builder()
            connectivityManager?.registerNetworkCallback(builder.build(), object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    updateRealNetworkState()
                }

                override fun onLost(network: Network) {
                    updateRealNetworkState()
                }

                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    updateRealNetworkState()
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Read real cellular carrier names
        realCellularCarrier.value = telephonyManager?.networkOperatorName ?: "Carrier Unavailable"
        realCellularEnabled.value = telephonyManager?.isDataConnectionAllowed ?: false

        // Simple passive monitoring for Cellular strength on older APIs or compat levels
        try {
            telephonyManager?.listen(object : PhoneStateListener() {
                override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
                    super.onSignalStrengthsChanged(signalStrength)
                    val level = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        signalStrength?.level ?: 0 // returns 0 to 4
                    } else {
                        2
                    }
                    realCellularStrength.value = (level * 25) // convert to % (0, 25, 50, 75, 100)
                }
            }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Active polling of SSID & WiFi strength
        viewModelScope.launch {
            while (true) {
                updateRealNetworkState()
                kotlinx.coroutines.delay(8000)
            }
        }
    }

    private fun updateRealNetworkState() {
        val info = wifiManager?.connectionInfo
        realWifiEnabled.value = wifiManager?.isWifiEnabled ?: false

        if (info != null && realWifiEnabled.value && info.networkId != -1) {
            val ssid = info.ssid ?: "<unknown ssid>"
            realWifiSsid.value = if (ssid == "<unknown ssid>" || ssid == WifiManager.UNKNOWN_SSID) {
                // Return placeholder indicating permission is usually required for detail, but we can display connection
                "Connected (Needs Permission)"
            } else {
                ssid.removeSurrounding("\"")
            }
            // RSSI
            val rssi = info.rssi
            val percent = WifiManager.calculateSignalLevel(rssi, 100)
            realWifiStrength.value = percent
        } else {
            realWifiSsid.value = "Disconnected"
            realWifiStrength.value = 0
        }

        // Evaluate actual active network
        val activeNet = connectivityManager?.activeNetwork
        val caps = connectivityManager?.getNetworkCapabilities(activeNet)
        when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> {
                realActiveNetwork.value = "Wi-Fi"
            }
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> {
                realActiveNetwork.value = "Cellular"
            }
            else -> {
                realActiveNetwork.value = "None"
            }
        }

        realCellularEnabled.value = connectivityManager?.getNetworkCapabilities(
            connectivityManager.activeNetwork
        )?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    }

    private fun startAutoSwitcherJob() {
        // Collect states and evaluate transitioning rule
        viewModelScope.launch {
            combine(
                isSimulationMode,
                autoSwitchEnabled,
                wifiThresholdSetting,
                cellularThresholdSetting,
                homeNetworks,
                // Combine simulated variables
                simulatedWifiEnabled,
                simulatedWifiSsid,
                simulatedWifiStrength,
                simulatedCellularEnabled,
                simulatedCellularStrength,
                simulatedActiveNetwork,
                // Combine real variables
                realWifiEnabled,
                realWifiSsid,
                realWifiStrength,
                realCellularEnabled,
                realCellularStrength,
                realActiveNetwork
            ) { values ->
                evaluateSwitchingRules()
            }.collect()
        }
    }

    private suspend fun evaluateSwitchingRules() {
        val isSim = isSimulationMode.value
        val isAuto = autoSwitchEnabled.value

        if (!isAuto) {
            _currentRecommendation.value = "Optimizer Offline (Auto-Switch Disabled)"
            return
        }

        val wifiEnabled = if (isSim) simulatedWifiEnabled.value else realWifiEnabled.value
        val wifiSsid = if (isSim) simulatedWifiSsid.value else realWifiSsid.value
        val wifiStrength = if (isSim) simulatedWifiStrength.value else realWifiStrength.value
        val cellEnabled = if (isSim) simulatedCellularEnabled.value else realCellularEnabled.value
        val cellStrength = if (isSim) simulatedCellularStrength.value else realCellularStrength.value
        val activeNet = if (isSim) simulatedActiveNetwork.value else realActiveNetwork.value

        val wifiThresh = wifiThresholdSetting.value
        val cellThresh = cellularThresholdSetting.value

        val homeSsidList = homeNetworks.value.map { it.ssid.lowercase() }

        var recommendation = "Keep current network settings"
        var actionCode = "STABLE"

        // Rule Check 1: We are on Wi-Fi, and signal goes dead/weak.
        if (activeNet == "Wi-Fi") {
            if (!wifiEnabled || wifiStrength < wifiThresh) {
                if (cellEnabled && cellStrength >= cellThresh) {
                    recommendation = "Automatically switching to Cellular: Wi-Fi signal is critically low ($wifiStrength%)."
                    actionCode = "SWITCH_TO_CELLULAR"
                } else if (!cellEnabled) {
                    recommendation = "Alert: Wi-Fi is weak ($wifiStrength%), and Cellular is disconnected."
                    actionCode = "ALL_DEAD_WIFI_WEAK"
                } else {
                    recommendation = "Alert: Both Wi-Fi ($wifiStrength%) and Cellular ($cellStrength%) signals are low."
                    actionCode = "BOTH_WEAK"
                }
            }
        }
        // Rule Check 2: We are on Cellular, and Cellular is dead or we enter Home WiFi Zone.
        else if (activeNet == "Cellular") {
            val isNearHomeWifi = wifiEnabled && homeSsidList.contains(wifiSsid.lowercase())

            if (isNearHomeWifi) {
                recommendation = "Automatically switching to Home Wi-Fi ($wifiSsid): Home zone entered!"
                actionCode = "SWITCH_TO_WIFI"
            } else if (!cellEnabled || cellStrength < cellThresh) {
                if (wifiEnabled && wifiStrength >= wifiThresh) {
                    recommendation = "Automatically switching to Wi-Fi ($wifiSsid): Cellular signal is critically low ($cellStrength%)."
                    actionCode = "SWITCH_TO_WIFI"
                } else if (!wifiEnabled) {
                    recommendation = "Alert: Cellular is weak ($cellStrength%), and Wi-Fi is disconnected."
                    actionCode = "ALL_DEAD_CELLULAR_WEAK"
                } else {
                    recommendation = "Alert: Both Cellular ($cellStrength%) and Wi-Fi ($wifiStrength%) are low."
                    actionCode = "BOTH_WEAK"
                }
            }
        }
        // Rule Check 3: Offline altogether, but Wi-Fi becomes available
        else {
            if (wifiEnabled && wifiStrength >= wifiThresh) {
                val isHome = homeSsidList.contains(wifiSsid.lowercase())
                recommendation = if (isHome) {
                    "Discovered Home Wi-Fi ($wifiSsid). Switch to Wi-Fi to load internet."
                } else {
                    "Discovered stable Wi-Fi ($wifiSsid). Switch to Wi-Fi to load internet."
                }
                actionCode = "SWITCH_TO_WIFI"
            } else if (cellEnabled && cellStrength >= cellThresh) {
                recommendation = "Discovered stable Cell Carrier. Switch to Cellular to restore internet."
                actionCode = "SWITCH_TO_CELLULAR"
            } else {
                recommendation = "No usable networks found. Searching..."
                actionCode = "SEARCHING"
            }
        }

        _currentRecommendation.value = recommendation

        // Trigger action!
        if (actionCode != "STABLE" && actionCode != "SEARCHING" && actionCode != lastRecommendedAction) {
            triggerSystemTransition(actionCode, recommendation, isSim, wifiSsid)
            lastRecommendedAction = actionCode
        } else if (actionCode == "STABLE") {
            lastRecommendedAction = "STABLE"
        }
    }

    private suspend fun triggerSystemTransition(
        actionCode: String,
        details: String,
        isSim: Boolean,
        wifiSsid: String
    ) {
        // Store log in database
        val eventType = when (actionCode) {
            "SWITCH_TO_CELLULAR" -> "AUTO_SWITCH"
            "SWITCH_TO_WIFI" -> "AUTO_SWITCH"
            else -> "INFO"
        }
        repository.insertLog(eventType, details, isSimulation = isSim)

        // Perform mock actions if simulation mode is active to show beautiful interactive transitions!
        if (isSim) {
            if (actionCode == "SWITCH_TO_CELLULAR") {
                simulatedActiveNetwork.value = "Cellular"
            } else if (actionCode == "SWITCH_TO_WIFI") {
                simulatedActiveNetwork.value = "Wi-Fi"
            }
        }

        // Post Android System Notification
        if (notificationsEnabled.value) {
            showNotification("NetSwitch Optimizer Alert", details)
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val notificationId = 101

        val builder = NotificationCompat.Builder(context, "netswitch_channel")
            .setSmallIcon(android.R.drawable.stat_sys_warning) // Using system resource fallback icon safely
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setOnlyAlertOnce(true)

        notificationManager?.notify(notificationId, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "NetSwitch Mode Changes"
            val descriptionText = "Notifies when cellular/Wi-Fi configurations are dynamically optimized"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("netswitch_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
