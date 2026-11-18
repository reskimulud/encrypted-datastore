/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 06-10-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package io.github.reskimulud.encrypteddatastore.serializer

import androidx.datastore.core.Serializer
import io.github.reskimulud.encrypteddatastore.algorithm.aes.AES
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.json.Json

private val defaultJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

/**
 * A [Serializer] that stores a single kotlinx.serialization object in an
 * encrypted file.
 *
 * Use it with `DataStoreFactory.create` on the JVM or the `dataStore` delegate
 * on Android. Corrupt or tampered files throw
 * [androidx.datastore.core.CorruptionException], so a
 * `ReplaceFileCorruptionHandler` can be supplied to DataStore.
 *
 * Only authentication failures and malformed headers are reported as
 * [CorruptionException][androidx.datastore.core.CorruptionException]. Other key
 * errors, such as a locked Android Keystore key, are propagated to the caller.
 */
class EncryptedSerializer<T> internal constructor(
    delegate: Serializer<T>,
) : Serializer<T> by delegate {

    /**
     * @param defaultValue value returned when the file does not exist yet.
     * @param serializer serializer for [T].
     * @param aes encryption instance. The key length selects
     * AES-128/192/256-GCM.
     * @param format serialization format, for example [Json] or
     * `ProtoBuf { encodeDefaults = true }`. The default is [Json] with
     * `encodeDefaults = true` and `ignoreUnknownKeys = true`. Binary formats
     * are written as raw bytes, without a UTF-8 round trip.
     */
    constructor(
        defaultValue: T,
        serializer: KSerializer<T>,
        aes: AES,
        format: SerialFormat = defaultJson,
    ) : this(EncryptedCodec(defaultValue, serializer, aes, format))

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
}
