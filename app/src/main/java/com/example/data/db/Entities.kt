package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_networks")
data class HomeNetwork(
    @PrimaryKey val ssid: String,
    val label: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "network_logs")
data class NetworkLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String, // "INFO", "WIFI_CONNECT", "CELLULA_CONNECT", "AUTO_SWITCH", "SYSTEM"
    val description: String,
    val isSimulation: Boolean = false
)

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey val key: String,
    val value: String
)
