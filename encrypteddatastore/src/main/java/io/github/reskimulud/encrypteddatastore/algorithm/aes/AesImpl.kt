/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 06-10-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package io.github.reskimulud.encrypteddatastore.algorithm.aes

import java.security.SecureRandom
import java.security.SecretKey
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec

/**
 * AES-GCM implementation behind [AES]. Not part of the public API; create one
 * with [AES.Builder].
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
 * The legacy IV is only required to read values written by version 0.0.3,
 * which used AES-256-CBC with a fixed IV and zero padding.
 */
internal class AesImpl(
    private val secretKey: SecretKey,
    private val keySizeBytes: Int,
    iv: ByteArray?,
) : AES {

    private val algorithmId = algorithmIdFor(keySizeBytes)
    private val legacyIv = iv?.copyOf()

    // Non-exportable keys (Android Keystore) reject a caller-supplied IV, so the
    // provider has to generate it. SunJCE requires encoded keys, so that branch
    // is covered on a device rather than in unit tests.
    private val providerGeneratesIv = secretKey.encoded == null

    override fun encrypt(plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(GCM_TRANSFORMATION)
        val iv = if (providerGeneratesIv) {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val generated = requireNotNull(cipher.iv) { "Provider did not return an IV" }
            require(generated.size == GCM_IV_SIZE) {
                "Provider returned a ${generated.size}-byte IV; " +
                    "this format requires $GCM_IV_SIZE bytes"
            }
            generated
        } else {
            ByteArray(GCM_IV_SIZE).also(secureRandom::nextBytes).also { bytes ->
                cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    GCMParameterSpec(TAG_SIZE_BITS, bytes),
                )
            }
        }
        val header = byteArrayOf(FORMAT_VERSION_V2, algorithmId)
        cipher.updateAAD(header)
        return header + iv + cipher.doFinal(plaintext)
    }

    override fun decrypt(data: ByteArray): ByteArray {
        require(data.isNotEmpty()) { "Encrypted data is empty" }
        return when (data[0]) {
            FORMAT_VERSION_V1 -> decryptV1(data)
            FORMAT_VERSION_V2 -> decryptV2(data)
            else -> throw IllegalArgumentException("Unsupported encrypted data format")
        }
    }

    override fun decryptLegacyCbc(data: ByteArray): ByteArray {
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
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
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
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_SIZE_BITS, iv))
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
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_SIZE_BITS, iv))
        cipher.updateAAD(header)
        return cipher.doFinal(ciphertext)
    }

    internal companion object {
        const val KEY_SIZE_128 = 16
        const val KEY_SIZE_192 = 24
        const val KEY_SIZE_256 = 32
        const val BLOCK_SIZE = 16

        private const val GCM_IV_SIZE = 12
        private const val TAG_SIZE_BITS = 128
        private const val TAG_SIZE_BYTES = TAG_SIZE_BITS / 8
        private const val V1_HEADER_SIZE = 1
        private const val V2_HEADER_SIZE = 2
        private const val ALGORITHM_ID_INDEX = 1
        private const val FORMAT_VERSION_V1 = 0x01.toByte()
        private const val FORMAT_VERSION_V2 = 0x02.toByte()
        private const val ALGORITHM_AES_128_GCM = 0x01.toByte()
        private const val ALGORITHM_AES_192_GCM = 0x02.toByte()
        private const val ALGORITHM_AES_256_GCM = 0x03.toByte()
        private const val GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val LEGACY_TRANSFORMATION = "AES/CBC/NoPadding"

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
