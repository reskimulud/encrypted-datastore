/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 06-10-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package com.mankart.encrypteddatastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.mankart.encrypteddatastore.algorithm.aes.AES
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object EncryptedDataStore {
    suspend inline fun <reified T> DataStore<Preferences>.secureEdit(
        value: T,
        aes: AES,
        crossinline editStore: (MutablePreferences, String) -> Unit
    ) {
        edit {
            val text = Json.encodeToString(value)
            val encryptedValue = aes.encrypt(text.toByteArray())
            editStore.invoke(it, encryptedValue.decodeToString())
        }
    }

    inline fun <reified T> Flow<Preferences>.secureMap(
        aes: AES,
        crossinline fetchValue: (value: Preferences) -> String,
    ): Flow<T> =
        map { preference ->
            val value = fetchValue(preference)

            if (value.isNotEmpty() && value != "") {
                val decryptedValue = aes.decrypt(value.toByteArray())
                val jsonEncode = Json { encodeDefaults = true }
                jsonEncode.decodeFromString(decryptedValue.decodeToString())
            } else {
                value as T
            }
        }
}