package io.github.reskimulud.encrypteddatastore.algorithm.aes

import io.github.reskimulud.encrypteddatastore.V1_ENCRYPTED_JSON_HEX
import io.github.reskimulud.encrypteddatastore.V1_JSON_PLAINTEXT
import io.github.reskimulud.encrypteddatastore.hexToBytes
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.security.GeneralSecurityException

class AESTest {

    private val aes = AES.Builder()
        .setKey(KEY_256)
        .setIv(LEGACY_IV)
        .build()

    @Test
    fun `round trips different plaintext lengths`() {
        listOf(
            "",
            "short",
            "Profil pengguna \u2713",
            "x".repeat(1024),
        ).forEach { plaintext ->
            val bytes = plaintext.encodeToByteArray()
            assertArrayEquals(bytes, aes.decrypt(aes.encrypt(bytes)))
        }
    }

    @Test
    fun `encrypt uses a fresh random IV`() {
        val plaintext = "same plaintext".encodeToByteArray()
        val first = aes.encrypt(plaintext)
        val second = aes.encrypt(plaintext)

        assertNotEquals(first.toList(), second.toList())
        assertArrayEquals(plaintext, aes.decrypt(first))
        assertArrayEquals(plaintext, aes.decrypt(second))
    }

    @Test
    fun `encrypt writes the version 2 header and algorithm id`() {
        val encrypted = aes.encrypt("secret".encodeToByteArray())

        assertEquals(0x02.toByte(), encrypted[0])
        assertEquals(0x03.toByte(), encrypted[1])
    }

    @Test
    fun `key size selects the aes variant`() {
        val cases = listOf(
            Triple(KEY_128, 0x01.toByte(), KEY_256),
            Triple(KEY_192, 0x02.toByte(), KEY_256),
            Triple(KEY_256, 0x03.toByte(), KEY_128),
        )

        cases.forEach { (key, expectedAlgorithmId, otherKey) ->
            val instance = AES.Builder().setKey(key).build()
            val plaintext = "secret".encodeToByteArray()
            val encrypted = instance.encrypt(plaintext)

            assertEquals(0x02.toByte(), encrypted[0])
            assertEquals(expectedAlgorithmId, encrypted[1])
            assertArrayEquals(plaintext, instance.decrypt(encrypted))

            val mismatched = AES.Builder().setKey(otherKey).build()
            assertThrows(IllegalArgumentException::class.java) {
                mismatched.decrypt(encrypted)
            }
        }
    }

    @Test
    fun `decrypt rejects tampered ciphertext`() {
        val encrypted = aes.encrypt("secret".encodeToByteArray())
        encrypted[encrypted.lastIndex] = (encrypted.last().toInt() xor 0x01).toByte()

        assertThrows(GeneralSecurityException::class.java) { aes.decrypt(encrypted) }
    }

    @Test
    fun `decrypt rejects unknown format versions`() {
        assertThrows(IllegalArgumentException::class.java) {
            aes.decrypt(byteArrayOf(0x03) + ByteArray(40))
        }
    }

    @Test
    fun `decrypt rejects unknown algorithm ids`() {
        assertThrows(IllegalArgumentException::class.java) {
            aes.decrypt(byteArrayOf(0x02, 0x7f) + ByteArray(30))
        }
    }

    @Test
    fun `decrypt rejects data that is too short`() {
        assertThrows(IllegalArgumentException::class.java) {
            aes.decrypt(byteArrayOf(0x01))
        }
        assertThrows(IllegalArgumentException::class.java) {
            aes.decrypt(byteArrayOf(0x02, 0x03))
        }
    }

    @Test
    fun `instances keep their own key`() {
        val other = AES.Builder()
            .setKey("fedcba9876543210fedcba9876543210")
            .build()
        val encrypted = aes.encrypt("secret".encodeToByteArray())

        assertThrows(GeneralSecurityException::class.java) { other.decrypt(encrypted) }
    }

    @Test
    fun `key must be 16, 24, or 32 bytes`() {
        assertThrows(IllegalArgumentException::class.java) {
            AES.Builder().setKey("too-short").build()
        }
        assertThrows(IllegalArgumentException::class.java) {
            AES.Builder().setKey(ByteArray(20)).build()
        }
    }

    @Test
    fun `legacy IV must be 16 bytes`() {
        assertThrows(IllegalArgumentException::class.java) {
            AES.Builder().setKey(KEY_256).setIv("short").build()
        }
    }

    @Test
    fun `decrypts a file written by version 0_2_0`() {
        assertArrayEquals(
            V1_JSON_PLAINTEXT.encodeToByteArray(),
            aes.decrypt(V1_ENCRYPTED_JSON_HEX.hexToBytes()),
        )
    }

    @Test
    fun `version 1 data requires a 32-byte key`() {
        val aes128 = AES.Builder().setKey(KEY_128).build()

        assertThrows(IllegalArgumentException::class.java) {
            aes128.decrypt(V1_ENCRYPTED_JSON_HEX.hexToBytes())
        }
    }

    @Test
    fun `legacy CBC fixtures decrypt to their original plaintext`() {
        // Generated with a standard AES-256-CBC/NoPadding implementation using
        // KEY_256 and LEGACY_IV, with the plaintext zero-padded to a 16-byte block.
        val fixtures = mapOf(
            "Hello, World!" to "86ec74a37019433fb796ac388579a46d",
            "Profil pengguna \u2713" to
                "729e34041e805e090d09de5a26707f94a7ebb3bfb590d947dbb9decdb4b54a09",
            """{"name":"Reski Mulud","email":"reski.mulud@gmail.com"}""" to
                "f81985526928075c523aab3e750de4564819118ba3b5242a59d666ac1eb45992" +
                "1084c208f940d74bea54281e7d3b7389edc058d5c50430b6404f275ab5425cf1",
        )

        fixtures.forEach { (plaintext, ciphertext) ->
            assertArrayEquals(
                plaintext.encodeToByteArray(),
                aes.decryptLegacyCbc(ciphertext.hexToBytes()),
            )
        }
    }

    @Test
    fun `legacy decryption requires an IV`() {
        val withoutIv = AES.Builder().setKey(KEY_256).build()
        val legacyCiphertext = "86ec74a37019433fb796ac388579a46d".hexToBytes()

        assertThrows(IllegalArgumentException::class.java) {
            withoutIv.decryptLegacyCbc(legacyCiphertext)
        }
    }

    @Test
    fun `legacy decryption requires a 32-byte key`() {
        val aes128 = AES.Builder().setKey(KEY_128).setIv(LEGACY_IV).build()
        val legacyCiphertext = "86ec74a37019433fb796ac388579a46d".hexToBytes()

        assertThrows(IllegalArgumentException::class.java) {
            aes128.decryptLegacyCbc(legacyCiphertext)
        }
    }

    @Test
    fun `legacy decryption rejects non-block-aligned data`() {
        assertThrows(IllegalArgumentException::class.java) {
            aes.decryptLegacyCbc(ByteArray(15))
        }
    }

    private companion object {
        const val KEY_128 = "0123456789abcdef"
        const val KEY_192 = "0123456789abcdef01234567"
        const val KEY_256 = "0123456789abcdef0123456789abcdef"
        const val LEGACY_IV = "abcdefghijklmnop"
    }
}
