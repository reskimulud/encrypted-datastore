/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 06-10-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package io.github.reskimulud.encrypteddatastore.algorithm.aes

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-GCM implementation backing the library.
 *
 * The key length selects the algorithm:
 * - 16 bytes: AES-128-GCM
 * - 24 bytes: AES-192-GCM
 * - 32 bytes: AES-256-GCM
 *
 * New data is written with format version 2:
 * `[0x02][algorithm id][12-byte IV][ciphertext][128-bit tag]`.
 * The two header bytes are authenticated as additional data, so a changed
 * algorithm id makes decryption fail instead of decoding with the wrong key
 * size.
 *
 * Files written by 0.2.0 use format version 1:
 * `[0x01][12-byte IV][ciphertext][128-bit tag]`.
 * Those files are always AES-256-GCM and can still be read with a 32-byte key.
 *
 * [Builder.setIv] is only required to read values written by version 0.0.3,
 * which used AES-256-CBC with a fixed IV and zero padding.
 */
class AES private constructor(
    private val key: SecretKeySpec,
    private val keySizeBytes: Int,
    private val algorithmId: Byte,
    private val legacyIv: ByteArray?,
) {

    /**
     * Encrypts [plaintext] with a fresh random IV and returns
     * `[version][algorithm id][IV][ciphertext + tag]`.
     */
    fun encrypt(plaintext: ByteArray): ByteArray {
        val iv = ByteArray(GCM_IV_SIZE).also(secureRandom::nextBytes)
        val header = byteArrayOf(FORMAT_VERSION_V2, algorithmId)
        val cipher = Cipher.getInstance(GCM_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_SIZE_BITS, iv))
        cipher.updateAAD(header)
        return header + iv + cipher.doFinal(plaintext)
    }

    /**
     * Decrypts data written by this version or by 0.2.0.
     *
     * @throws IllegalArgumentException if the format version is unknown, the
     * data is too short, or the file was written with a different key size.
     * @throws java.security.GeneralSecurityException if the tag does not match,
     * which usually means the key is wrong or the data was tampered with.
     */
    fun decrypt(data: ByteArray): ByteArray {
        require(data.isNotEmpty()) { "Encrypted data is empty" }
        return when (data[0]) {
            FORMAT_VERSION_V1 -> decryptV1(data)
            FORMAT_VERSION_V2 -> decryptV2(data)
            else -> throw IllegalArgumentException("Unsupported encrypted data format")
        }
    }

    /**
     * Decrypts a value written by version 0.0.3 (AES-256-CBC, zero padding).
     * Trailing zero bytes are removed, matching the old implementation.
     *
     * @throws IllegalArgumentException if the key is not 32 bytes, no legacy IV
     * was configured, or the ciphertext is not block aligned.
     */
    fun decryptLegacyCbc(data: ByteArray): ByteArray {
        require(keySizeBytes == KEY_SIZE_256) {
            "Legacy data was encrypted with AES-256; set a 32-byte key"
        }
        val iv = requireNotNull(legacyIv) {
            "An IV must be set to decrypt data written by version 0.0.3"
        }
        require(data.isNotEmpty() && data.size % BLOCK_SIZE == 0) {
            "Legacy ciphertext must be a non-empty multiple of $BLOCK_SIZE bytes"
        }

        val cipher = Cipher.getInstance(LEGACY_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        return cipher.doFinal(data).dropLastWhile { it == 0.toByte() }.toByteArray()
    }

    private fun decryptV1(data: ByteArray): ByteArray {
        require(keySizeBytes == KEY_SIZE_256) {
            "Data was written with AES-256-GCM; configure a 32-byte key"
        }
        require(data.size >= V1_HEADER_SIZE + GCM_IV_SIZE + TAG_SIZE_BYTES) {
            "Encrypted data is too short"
        }

        val iv = data.copyOfRange(V1_HEADER_SIZE, V1_HEADER_SIZE + GCM_IV_SIZE)
        val ciphertext = data.copyOfRange(V1_HEADER_SIZE + GCM_IV_SIZE, data.size)
        val cipher = Cipher.getInstance(GCM_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_SIZE_BITS, iv))
        return cipher.doFinal(ciphertext)
    }

    private fun decryptV2(data: ByteArray): ByteArray {
        require(data.size >= V2_HEADER_SIZE + GCM_IV_SIZE + TAG_SIZE_BYTES) {
            "Encrypted data is too short"
        }

        val header = data.copyOfRange(0, V2_HEADER_SIZE)
        val storedAlgorithmId = data[ALGORITHM_ID_INDEX]
        require(storedAlgorithmId == algorithmId) {
            "Data was encrypted with ${algorithmName(storedAlgorithmId)}, " +
                "but this instance uses ${algorithmName(algorithmId)}"
        }

        val iv = data.copyOfRange(V2_HEADER_SIZE, V2_HEADER_SIZE + GCM_IV_SIZE)
        val ciphertext = data.copyOfRange(V2_HEADER_SIZE + GCM_IV_SIZE, data.size)
        val cipher = Cipher.getInstance(GCM_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_SIZE_BITS, iv))
        cipher.updateAAD(header)
        return cipher.doFinal(ciphertext)
    }

    /**
     * Builds an [AES] instance. The key length is validated here so a wrong
     * key fails before any data is read or written.
     */
    class Builder {
        private var key: ByteArray? = null
        private var legacyIv: ByteArray? = null

        /**
         * Sets the key as UTF-8 bytes. The byte length must be 16, 24, or 32.
         */
        fun setKey(key: String): Builder = setKey(key.toByteArray(Charsets.UTF_8))

        /**
         * Sets a raw key. The length must be 16, 24, or 32 bytes and selects
         * AES-128, AES-192, or AES-256 respectively.
         */
        fun setKey(key: ByteArray): Builder {
            require(key.size == KEY_SIZE_128 || key.size == KEY_SIZE_192 || key.size == KEY_SIZE_256) {
                "Key must be 16, 24, or 32 bytes (128, 192, or 256 bits)"
            }
            this.key = key.copyOf()
            return this
        }

        /**
         * Sets the fixed IV used by version 0.0.3 data. Not used for new data.
         */
        fun setIv(iv: String): Builder {
            val bytes = iv.toByteArray(Charsets.UTF_8)
            require(bytes.size == BLOCK_SIZE) { "Legacy IV must be $BLOCK_SIZE bytes (128 bits)" }
            this.legacyIv = bytes
            return this
        }

        fun build(): AES {
            val key = requireNotNull(key) { "Key is required" }
            return AES(
                key = SecretKeySpec(key, KEY_ALGORITHM),
                keySizeBytes = key.size,
                algorithmId = algorithmIdFor(key.size),
                legacyIv = legacyIv?.copyOf(),
            )
        }
    }

    private companion object {
        const val KEY_ALGORITHM = "AES"
        const val KEY_SIZE_128 = 16
        const val KEY_SIZE_192 = 24
        const val KEY_SIZE_256 = 32
        const val BLOCK_SIZE = 16
        const val GCM_IV_SIZE = 12
        const val TAG_SIZE_BITS = 128
        const val TAG_SIZE_BYTES = TAG_SIZE_BITS / 8
        const val V1_HEADER_SIZE = 1
        const val V2_HEADER_SIZE = 2
        const val ALGORITHM_ID_INDEX = 1
        const val FORMAT_VERSION_V1 = 0x01.toByte()
        const val FORMAT_VERSION_V2 = 0x02.toByte()
        const val ALGORITHM_AES_128_GCM = 0x01.toByte()
        const val ALGORITHM_AES_192_GCM = 0x02.toByte()
        const val ALGORITHM_AES_256_GCM = 0x03.toByte()
        const val GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        const val LEGACY_TRANSFORMATION = "AES/CBC/NoPadding"

        val secureRandom = SecureRandom()

        fun algorithmIdFor(keySize: Int): Byte = when (keySize) {
            KEY_SIZE_128 -> ALGORITHM_AES_128_GCM
            KEY_SIZE_192 -> ALGORITHM_AES_192_GCM
            KEY_SIZE_256 -> ALGORITHM_AES_256_GCM
            else -> throw IllegalArgumentException("Unsupported key size: $keySize")
        }

        fun algorithmName(algorithmId: Byte): String = when (algorithmId) {
            ALGORITHM_AES_128_GCM -> "AES-128-GCM"
            ALGORITHM_AES_192_GCM -> "AES-192-GCM"
            ALGORITHM_AES_256_GCM -> "AES-256-GCM"
            else -> "unknown algorithm 0x" +
                (algorithmId.toInt() and 0xFF).toString(16).padStart(2, '0')
        }
    }
}
