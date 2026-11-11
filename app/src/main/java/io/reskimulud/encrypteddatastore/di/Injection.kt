package io.reskimulud.encrypteddatastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import io.github.reskimulud.encrypteddatastore.algorithm.aes.AES
import io.reskimulud.encrypteddatastore.data.UserProfile
import io.reskimulud.encrypteddatastore.data.UserRepository
import io.reskimulud.encrypteddatastore.data.datastore.createAES
import io.reskimulud.encrypteddatastore.data.datastore.createUserDataStore
import io.reskimulud.encrypteddatastore.data.datastore.migrateLegacyPreferences
import io.reskimulud.encrypteddatastore.data.datastore.userDataStoreFile
import io.reskimulud.encrypteddatastore.data.network.ApiConfig

object Injection {
    private var cachedAes: AES? = null
    private var cachedDataStore: DataStore<UserProfile>? = null

    fun provideUserRepository(context: Context): UserRepository {
        val appContext = context.applicationContext
        val aes = cachedAes ?: createAES().also { cachedAes = it }
        val dataStore = cachedDataStore
            ?: createUserDataStore(appContext, aes).also { cachedDataStore = it }
        val apiService = ApiConfig.getApiService()
        return UserRepository.getInstance(
            dataStore,
            apiService,
            userDataStoreFile(appContext),
        )
    }

    suspend fun migrateLegacyData(context: Context) {
        val aes = cachedAes ?: return
        val dataStore = cachedDataStore ?: return
        migrateLegacyPreferences(context.applicationContext, aes, dataStore)
    }
}
