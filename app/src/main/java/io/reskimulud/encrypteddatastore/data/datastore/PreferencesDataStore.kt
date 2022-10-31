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
import kotlinx.coroutines.flow.map

class PreferencesDataStore(
    private val encryptedDataStore: DataStore<Preferences>,
    private val unencryptedDataStore: DataStore<Preferences>,
    private val aes: AES
) {

    // Encrypted DataStore (DataStore yang dienkripsi)
    fun getUserEmail(): Flow<String> =
        encryptedDataStore.data.secureMap(aes) {
            it[USER_EMAIL_KEY] ?: ""
        }

    suspend fun setUserEmail(email: String) =
        encryptedDataStore.secureEdit(email, aes) { preferences, encryptedValue ->
            preferences[USER_EMAIL_KEY] = encryptedValue
        }

    // Unencrypted DataStore (DataStore yang tidak dienkripsi)
    suspend fun setUnencryptedUserEmail(email: String) =
        unencryptedDataStore.edit {
            it[USER_EMAIL_KEY] = email
        }

    companion object {
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")

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