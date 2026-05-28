package com.example.data.repository

import com.example.data.db.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NetworkRepository(private val db: AppDatabase) {

    private val homeNetworkDao = db.homeNetworkDao()
    private val networkLogDao = db.networkLogDao()
    private val appSettingDao = db.appSettingDao()

    val homeNetworks: Flow<List<HomeNetwork>> = homeNetworkDao.getAllHomeNetworks()
    val logs: Flow<List<NetworkLog>> = networkLogDao.getAllLogs()

    fun getSetting(key: String, defaultValue: String): Flow<String> {
        return appSettingDao.getSetting(key).map { it?.value ?: defaultValue }
    }

    suspend fun getSettingSuspend(key: String, defaultValue: String): String {
        return appSettingDao.getValueByKey(key) ?: defaultValue
    }

    suspend fun saveSetting(key: String, value: String) {
        appSettingDao.insertSetting(AppSetting(key, value))
    }

    suspend fun insertLog(eventType: String, description: String, isSimulation: Boolean = false) {
        networkLogDao.insertLog(
            NetworkLog(
                eventType = eventType,
                description = description,
                isSimulation = isSimulation
            )
        )
    }

    suspend fun clearLogs() {
        networkLogDao.clearAllLogs()
    }

    suspend fun addHomeNetwork(ssid: String, label: String) {
        homeNetworkDao.insertHomeNetwork(HomeNetwork(ssid, label))
    }

    suspend fun removeHomeNetwork(ssid: String) {
        homeNetworkDao.deleteHomeNetwork(ssid)
    }
}
