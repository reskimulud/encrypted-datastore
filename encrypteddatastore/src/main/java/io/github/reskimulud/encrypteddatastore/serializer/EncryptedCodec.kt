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
import io.github.reskimulud.encrypteddatastore.algorithm.Encryptor
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.SerializationException
import kotlinx.serialization.StringFormat
import java.io.InputStream
import java.io.OutputStream
import javax.crypto.BadPaddingException

/**
 * Implementation of [EncryptedSerializer]. Not part of the public API.
 */
internal class EncryptedCodec<T>(
    private val default: T,
    private val serializer: KSerializer<T>,
    private val encryptor: Encryptor,
    private val format: SerialFormat,
) : Serializer<T> {

    override val defaultValue: T = default

    override suspend fun readFrom(input: InputStream): T {
        val data = input.readBytes()
        if (data.isEmpty()) return defaultValue

        val plaintext = try {
            encryptor.decrypt(data)
        } catch (exception: BadPaddingException) {
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
        output.write(encryptor.encrypt(encode(t)))
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
