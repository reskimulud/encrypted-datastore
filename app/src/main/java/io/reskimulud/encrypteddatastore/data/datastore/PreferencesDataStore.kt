/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 11-10-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package io.reskimulud.encrypteddatastore.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.reskimulud.encrypteddatastore.BuildConfig.CIPHER_KEY
import io.github.reskimulud.encrypteddatastore.BuildConfig.IV
import io.github.reskimulud.encrypteddatastore.EncryptedDataStore.secureEdit
import io.github.reskimulud.encrypteddatastore.EncryptedDataStore.secureMap
import io.github.reskimulud.encrypteddatastore.algorithm.aes.AES
import kotlinx.coroutines.flow.Flow

class PreferencesDataStore(
    private val encryptedDataStore: DataStore<Preferences>,
    private val unencryptedDataStore: DataStore<Preferences>,
    private val aes: AES
) {

    // Encrypted DataStore (DataStore yang dienkripsi)
    fun getUserName(): Flow<String> =
        encryptedDataStore.data.secureMap(aes, DEFAULT_VALUE) {
            it[USER_NAME_KEY] ?: DEFAULT_VALUE
        }

    suspend fun setUserName(name: String) =
        encryptedDataStore.secureEdit(name, aes) { preferences, encryptedName ->
            preferences[USER_NAME_KEY] = encryptedName
        }

    fun getUserEmail(): Flow<String> =
        encryptedDataStore.data.secureMap(aes, DEFAULT_VALUE) {
            it[USER_EMAIL_KEY] ?: DEFAULT_VALUE
        }

    suspend fun setUserEmail(email: String) =
        encryptedDataStore.secureEdit(email, aes) { preferences, encryptedEmail ->
            preferences[USER_EMAIL_KEY] = encryptedEmail
        }

    fun getUserApiKey(): Flow<String> =
        encryptedDataStore.data.secureMap(aes, DEFAULT_VALUE) {
            it[USER_API_KEY] ?: DEFAULT_VALUE
        }

    suspend fun setUserApiKey(apiKey: String) =
        encryptedDataStore.secureEdit(apiKey, aes) { preferences, encryptedApiKey ->
            preferences[USER_API_KEY] = encryptedApiKey
        }

    // Unencrypted DataStore (DataStore yang tidak dienkripsi)
    suspend fun setUnencryptedUserName(name: String) =
        unencryptedDataStore.edit {
            it[USER_NAME_KEY] = name
        }

    suspend fun setUnencryptedUserEmail(email: String) =
        unencryptedDataStore.edit {
            it[USER_EMAIL_KEY] = email
        }

    suspend fun setUnencryptedApiKey(apiKey: String) =
        unencryptedDataStore.edit {
            it[USER_API_KEY] = apiKey
        }

    companion object {
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_API_KEY = stringPreferencesKey("user_api_key")

        private const val DEFAULT_VALUE = "empty"

        @Volatile
        private var INSTANCE: PreferencesDataStore? = null

        @JvmStatic
        fun getInstance(
            encryptedDataStore: DataStore<Preferences>,
            unencryptedDataStore: DataStore<Preferences>
        ): PreferencesDataStore =
            INSTANCE ?: synchronized(this) {
                val aes = AES.Builder()
                    .setKey(CIPHER_KEY)
                    .setIv(IV)
                    .build()
                val instance = PreferencesDataStore(encryptedDataStore, unencryptedDataStore, aes)
                INSTANCE = instance
                instance
            }
    }
}