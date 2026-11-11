/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 11-10-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package io.reskimulud.encrypteddatastore.data

import androidx.datastore.core.DataStore
import io.reskimulud.encrypteddatastore.data.network.ApiService
import io.reskimulud.encrypteddatastore.data.network.ImageResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.io.File

class UserRepository(
    private val dataStore: DataStore<UserProfile>,
    private val apiService: ApiService,
    private val encryptedFile: File,
) {

    val userProfile: Flow<UserProfile> = dataStore.data

    suspend fun updateProfile(transform: (UserProfile) -> UserProfile) {
        dataStore.updateData(transform)
    }

    suspend fun readEncryptedFile(): String = withContext(Dispatchers.IO) {
        if (!encryptedFile.exists()) {
            ""
        } else {
            encryptedFile.readBytes().joinToString("") { "%02x".format(it.toInt() and 0xFF) }
        }
    }

    // get data from internet
    fun getRandomImage(apiKey: String): Call<ImageResponse> =
        apiService.getRandomImage(apiKey)

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        @JvmStatic
        fun getInstance(
            dataStore: DataStore<UserProfile>,
            apiService: ApiService,
            encryptedFile: File,
        ): UserRepository =
            INSTANCE ?: synchronized(this) {
                val instance = UserRepository(dataStore, apiService, encryptedFile)
                INSTANCE = instance
                instance
            }
    }
}
