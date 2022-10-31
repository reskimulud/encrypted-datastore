/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 11-10-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package io.reskimulud.encrypteddatastore.data

import io.reskimulud.encrypteddatastore.data.datastore.PreferencesDataStore
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val dataStore: PreferencesDataStore
) {

    fun getUserEmail(): Flow<String> =
        dataStore.getUserEmail()

    suspend fun updateUserEmail(email: String) =
        dataStore.setUserEmail(email)

    // unencrypted datastore
    suspend fun updateUnencryptedUserEmail(email: String) =
        dataStore.setUnencryptedUserEmail(email)

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        @JvmStatic
        fun getInstance(dataStore: PreferencesDataStore): UserRepository =
            INSTANCE ?: synchronized(this) {
                val instance = UserRepository(dataStore)
                INSTANCE = instance
                instance
            }
    }
}