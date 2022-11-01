/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 06-10-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package io.github.reskimulud.encrypteddatastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.github.reskimulud.encrypteddatastore.algorithm.aes.AES
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

            val hexStringFromByteArray =
                encryptedValue.joinToString(""){ "%02x".format(it.toInt() and 0xFF) }

            editStore.invoke(it, hexStringFromByteArray)
        }
    }

    inline fun <reified T> Flow<Preferences>.secureMap(
        aes: AES,
        defaultValue: T,
        crossinline fetchValue: (value: Preferences) -> String,
    ): Flow<T> =
        map { preference ->
            val value = fetchValue(preference)

            if (value.isNotEmpty() && value != defaultValue) {
                val byteArrayFromHexString =
                    value.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

                val decryptedValue = aes.decrypt(byteArrayFromHexString)
                val jsonEncode = Json { encodeDefaults = true }
                jsonEncode.decodeFromString(decryptedValue.decodeToString())
            } else {
                defaultValue
            }
        }
}