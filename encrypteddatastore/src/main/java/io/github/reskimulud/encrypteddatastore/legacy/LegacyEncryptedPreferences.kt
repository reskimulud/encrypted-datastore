/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 06-10-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package io.github.reskimulud.encrypteddatastore.legacy

import io.github.reskimulud.encrypteddatastore.algorithm.aes.AES
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * Reads values stored by version 0.0.3, which encrypted each Preferences value
 * with AES-256-CBC and stored it as a hex string.
 *
 * These helpers only decrypt. The application must map its old preference keys
 * to its new serializable object and write that object to the typed DataStore.
 */
object LegacyEncryptedPreferences {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Decrypts a hex-encoded AES-256-CBC value written by 0.0.3.
     *
     * The [AES] instance must use a 32-byte key and the fixed IV that was used
     * by the old implementation. Returns null when [value] is null or empty.
     */
    fun decryptString(aes: AES, value: String?): String? {
        if (value.isNullOrEmpty()) return null
        return aes.decryptLegacyCbc(value.hexToBytes()).decodeToString()
    }

    /**
     * Decrypts a 0.0.3 hex value and parses the resulting JSON with [serializer].
     * Returns null when [value] is null or empty.
     */
    fun <T> decryptJson(aes: AES, serializer: KSerializer<T>, value: String?): T? {
        val plaintext = decryptString(aes, value) ?: return null
        return json.decodeFromString(serializer, plaintext)
    }
}

private fun String.hexToBytes(): ByteArray {
    require(length % 2 == 0) { "Invalid hex string length" }
    return ByteArray(length / 2) { index ->
        substring(index * 2, index * 2 + 2).toInt(16).toByte()
    }
}
