package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeNetworkDao {
    @Query("SELECT * FROM home_networks ORDER BY addedAt DESC")
    fun getAllHomeNetworks(): Flow<List<HomeNetwork>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeNetwork(network: HomeNetwork)

    @Query("DELETE FROM home_networks WHERE ssid = :ssid")
    suspend fun deleteHomeNetwork(ssid: String)
}

@Dao
interface NetworkLogDao {
    @Query("SELECT * FROM network_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllLogs(): Flow<List<NetworkLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: NetworkLog)

    @Query("DELETE FROM network_logs")
    suspend fun clearAllLogs()
}

@Dao
interface AppSettingDao {
    @Query("SELECT * FROM app_settings WHERE `key` = :key")
    fun getSetting(key: String): Flow<AppSetting?>

    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    suspend fun getValueByKey(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: AppSetting)
}
