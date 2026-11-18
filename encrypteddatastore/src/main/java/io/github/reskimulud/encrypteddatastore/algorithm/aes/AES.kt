/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 06-10-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package io.github.reskimulud.encrypteddatastore.algorithm.aes

/**
 * Encrypts and decrypts byte arrays with AES-GCM.
 *
 * [Builder] selects the algorithm from the key length: 16 bytes for
 * AES-128-GCM, 24 bytes for AES-192-GCM, and 32 bytes for AES-256-GCM.
 *
 * Every [encrypt] call uses a fresh random IV, so implementations are safe to
 * reuse within a process.
 */
interface AES {

    /**
     * Encrypts [plaintext] into the versioned payload that [decrypt] expects.
     */
    fun encrypt(plaintext: ByteArray): ByteArray

    /**
     * Decrypts data written by this library, including files written by 0.2.0.
     *
     * @throws IllegalArgumentException if the format version is unknown, the
     * data is too short, or the data was written with a different key size.
     * @throws java.security.GeneralSecurityException if the authentication tag
     * does not match, which usually means the key is wrong or the data was
     * tampered with.
     */
    fun decrypt(data: ByteArray): ByteArray

    /**
     * Decrypts a value written by version 0.0.3 (AES-256-CBC, zero padding).
     *
     * @throws IllegalArgumentException if the key is not 32 bytes, no legacy
     * IV was configured, or the ciphertext is not block aligned.
     */
    fun decryptLegacyCbc(data: ByteArray): ByteArray

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
            require(
                key.size == AesImpl.KEY_SIZE_128 ||
                    key.size == AesImpl.KEY_SIZE_192 ||
                    key.size == AesImpl.KEY_SIZE_256,
            ) {
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
            require(bytes.size == AesImpl.BLOCK_SIZE) {
                "Legacy IV must be ${AesImpl.BLOCK_SIZE} bytes (128 bits)"
            }
            this.legacyIv = bytes
            return this
        }

        fun build(): AES {
            val key = requireNotNull(key) { "Key is required" }
            return AesImpl(key = key, iv = legacyIv)
        }
    }
}
