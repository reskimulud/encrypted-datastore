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
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.SerializationException
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException

private val defaultJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

/**
 * A [Serializer] that stores a single kotlinx.serialization object in an
 * encrypted file.
 *
 * [format] controls how [T] is converted to bytes before encryption. The
 * default is [Json] with `encodeDefaults = true` and `ignoreUnknownKeys = true`.
 * Pass `ProtoBuf { encodeDefaults = true }` from kotlinx-serialization-protobuf
 * to store Protocol Buffers instead; binary formats are written as raw bytes,
 * without a UTF-8 round trip.
 *
 * Use it with `DataStoreFactory.create` on the JVM or the `dataStore` delegate
 * on Android. Corrupt or tampered files throw [CorruptionException], so a
 * `ReplaceFileCorruptionHandler` can be supplied to DataStore.
 *
 * @param defaultValue value returned when the file does not exist yet.
 * @param serializer serializer for [T].
 * @param aes encryption instance. The key length selects AES-128/192/256-GCM.
 * @param format serialization format, for example [Json] or
 * `ProtoBuf { encodeDefaults = true }`.
 */
class EncryptedSerializer<T>(
    override val defaultValue: T,
    private val serializer: KSerializer<T>,
    private val aes: AES,
    private val format: SerialFormat = defaultJson,
) : Serializer<T> {

    /**
     * Keeps source compatibility with 0.2.0, where the last parameter was a
     * [Json] instance.
     */
    constructor(
        defaultValue: T,
        serializer: KSerializer<T>,
        aes: AES,
        json: Json,
    ) : this(defaultValue, serializer, aes, format = json)

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
            decode(plaintext)
        } catch (exception: SerializationException) {
            throw CorruptionException("Unable to deserialize data", exception)
        }
    }

    override suspend fun writeTo(t: T, output: OutputStream) {
        output.write(aes.encrypt(encode(t)))
    }

    private fun encode(value: T): ByteArray = when (val serialFormat = format) {
        is BinaryFormat -> serialFormat.encodeToByteArray(serializer, value)
        is StringFormat -> serialFormat.encodeToString(serializer, value).encodeToByteArray()
        else -> throw IllegalArgumentException(
            "Unsupported serialization format: ${serialFormat::class.simpleName}",
        )
    }

    private fun decode(bytes: ByteArray): T = when (val serialFormat = format) {
        is BinaryFormat -> serialFormat.decodeFromByteArray(serializer, bytes)
        is StringFormat -> serialFormat.decodeFromString(serializer, bytes.decodeToString())
        else -> throw IllegalArgumentException(
            "Unsupported serialization format: ${serialFormat::class.simpleName}",
        )
    }
}
