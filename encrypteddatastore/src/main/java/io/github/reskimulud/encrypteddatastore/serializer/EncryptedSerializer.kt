/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 06-10-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package io.github.reskimulud.encrypteddatastore.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.github.reskimulud.encrypteddatastore.algorithm.aes.AES
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException

/**
 * A [Serializer] that stores a single kotlinx.serialization object in an
 * encrypted file.
 *
 * Use it with `DataStoreFactory.create` on the JVM or the `dataStore` delegate
 * on Android. Corrupt or tampered files throw [CorruptionException], so a
 * `ReplaceFileCorruptionHandler` can be supplied to DataStore.
 */
class EncryptedSerializer<T>(
    override val defaultValue: T,
    private val serializer: KSerializer<T>,
    private val aes: AES,
    private val json: Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    },
) : Serializer<T> {

    override suspend fun readFrom(input: InputStream): T {
        val data = input.readBytes()
        if (data.isEmpty()) return defaultValue

        val plaintext = try {
            aes.decrypt(data)
        } catch (exception: GeneralSecurityException) {
            throw CorruptionException("Unable to decrypt data", exception)
        } catch (exception: IllegalArgumentException) {
            throw CorruptionException("Unable to decrypt data", exception)
        }

        return try {
            json.decodeFromString(serializer, plaintext.decodeToString())
        } catch (exception: SerializationException) {
            throw CorruptionException("Unable to deserialize data", exception)
        }
    }

    override suspend fun writeTo(t: T, output: OutputStream) {
        val plaintext = json.encodeToString(serializer, t).encodeToByteArray()
        output.write(aes.encrypt(plaintext))
    }
}
