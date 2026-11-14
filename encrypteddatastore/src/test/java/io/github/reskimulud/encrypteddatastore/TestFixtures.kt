package io.github.reskimulud.encrypteddatastore

/**
 * Generated with the 0.2.0 AES-256-GCM implementation and the same key used by
 * the tests. The format is [0x01][12-byte IV][ciphertext + tag].
 */
internal const val V1_ENCRYPTED_JSON_HEX =
    "01a017ff7e7c0da87e345f37502ad187d478589fcfca9f66ed2b5478499dd94f8549e580" +
        "0449fa7035e3b734fada2c7089d7cd3ec7c5b0e0b6f9c99d47b599b80aa03d7218cd35" +
        "2f75730e97e8bea20c5c4cac"

internal const val V1_JSON_PLAINTEXT =
    """{"name":"Reski Mulud","email":"reski.mulud@gmail.com"}"""

internal fun String.hexToBytes(): ByteArray =
    ByteArray(length / 2) { index ->
        substring(index * 2, index * 2 + 2).toInt(16).toByte()
    }
