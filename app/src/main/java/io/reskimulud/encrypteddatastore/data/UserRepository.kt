/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 11-10-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package io.reskimulud.encrypteddatastore.data

import io.reskimulud.encrypteddatastore.data.datastore.PreferencesDataStore
import io.reskimulud.encrypteddatastore.data.network.ApiService
import io.reskimulud.encrypteddatastore.data.network.ImageResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Call

class UserRepository(
    private val dataStore: PreferencesDataStore,
    private val apiService: ApiService
) {

    // encrypted datastore
    fun getUserName(): Flow<String> =
        dataStore.getUserName()

    suspend fun updateUserName(name: String) =
        dataStore.setUserName(name)

    fun getUserEmail(): Flow<String> =
        dataStore.getUserEmail()

    suspend fun updateUserEmail(email: String) =
        dataStore.setUserEmail(email)

    fun getUserApiKey(): Flow<String> =
        dataStore.getUserApiKey()

    suspend fun updateUserApiKey(apiKey: String) =
        dataStore.setUserApiKey(apiKey)

    // unencrypted datastore
    suspend fun updateUnencryptedUserName(name: String) =
        dataStore.setUnencryptedUserName(name)

    suspend fun updateUnencryptedUserEmail(email: String) =
        dataStore.setUnencryptedUserEmail(email)

    suspend fun updateUnencryptedUserApiKey(apiKey: String) =
        dataStore.setUnencryptedApiKey(apiKey)


    // get data from internet
    fun getRandomImage(apiKey: String): Call<ImageResponse> =
        apiService.getRandomImage(apiKey)

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        @JvmStatic
        fun getInstance(
            dataStore: PreferencesDataStore,
            apiService: ApiService
        ): UserRepository =
            INSTANCE ?: synchronized(this) {
                val instance = UserRepository(dataStore, apiService)
                INSTANCE = instance
                instance
            }
    }
}