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
 * AES-256-GCM implementation backing the library.
 *
 * New data is written as `[version][12-byte IV][ciphertext][128-bit tag]`.
 * [Builder.setIv] is only required to read values written by version 0.0.3,
 * which used AES-256-CBC with a fixed IV and zero padding.
 */
class AES private constructor(
    private val key: SecretKeySpec,
    private val legacyIv: ByteArray?,
) {

    fun encrypt(plaintext: ByteArray): ByteArray {
        val iv = ByteArray(GCM_IV_SIZE).also(secureRandom::nextBytes)
        val cipher = Cipher.getInstance(GCM_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_SIZE_BITS, iv))
        return byteArrayOf(FORMAT_VERSION) + iv + cipher.doFinal(plaintext)
    }

    fun decrypt(data: ByteArray): ByteArray {
        require(data.isNotEmpty() && data[0] == FORMAT_VERSION) {
            "Unsupported encrypted data format"
        }
        require(data.size >= HEADER_SIZE + TAG_SIZE_BYTES) {
            "Encrypted data is too short"
        }

        val iv = data.copyOfRange(1, HEADER_SIZE)
        val ciphertext = data.copyOfRange(HEADER_SIZE, data.size)
        val cipher = Cipher.getInstance(GCM_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_SIZE_BITS, iv))
        return cipher.doFinal(ciphertext)
    }

    /**
     * Decrypts a value written by version 0.0.3 (AES-256-CBC, zero padding).
     * Trailing zero bytes are removed, matching the old implementation.
     */
    fun decryptLegacyCbc(data: ByteArray): ByteArray {
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

    class Builder {
        private var key: ByteArray? = null
        private var legacyIv: ByteArray? = null

        fun setKey(key: String): Builder {
            val bytes = key.toByteArray(Charsets.UTF_8)
            require(bytes.size == KEY_SIZE) { "Key must be $KEY_SIZE bytes (256 bits)" }
            this.key = bytes
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
            return AES(SecretKeySpec(key, KEY_ALGORITHM), legacyIv?.copyOf())
        }
    }

    private companion object {
        const val KEY_ALGORITHM = "AES"
        const val KEY_SIZE = 32
        const val BLOCK_SIZE = 16
        const val GCM_IV_SIZE = 12
        const val TAG_SIZE_BITS = 128
        const val TAG_SIZE_BYTES = TAG_SIZE_BITS / 8
        const val HEADER_SIZE = 1 + GCM_IV_SIZE
        const val FORMAT_VERSION = 0x01.toByte()
        const val GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        const val LEGACY_TRANSFORMATION = "AES/CBC/NoPadding"

        val secureRandom = SecureRandom()
    }
}
