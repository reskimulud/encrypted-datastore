package io.github.reskimulud.encrypteddatastore.legacy

import io.github.reskimulud.encrypteddatastore.algorithm.aes.AES
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyEncryptedPreferencesTest {

    private val aes = AES.Builder()
        .setKey(KEY)
        .setIv(LEGACY_IV)
        .build()

    @Test
    fun `decrypts a legacy string`() {
        assertEquals(
            "Hello, World!",
            LegacyEncryptedPreferences.decryptString(
                aes,
                "86ec74a37019433fb796ac388579a46d",
            ),
        )
    }

    @Test
    fun `returns null for missing or empty values`() {
        assertNull(LegacyEncryptedPreferences.decryptString(aes, null))
        assertNull(LegacyEncryptedPreferences.decryptString(aes, ""))
    }

    @Test
    fun `decrypts legacy json`() {
        val serializer = MapSerializer(String.serializer(), String.serializer())

        val value = LegacyEncryptedPreferences.decryptJson(
            aes,
            serializer,
            "f81985526928075c523aab3e750de4564819118ba3b5242a59d666ac1eb45992" +
                "1084c208f940d74bea54281e7d3b7389edc058d5c50430b6404f275ab5425cf1",
        )

        assertEquals(
            mapOf(
                "name" to "Reski Mulud",
                "email" to "reski.mulud@gmail.com",
            ),
            value,
        )
    }

    private companion object {
        const val KEY = "0123456789abcdef0123456789abcdef"
        const val LEGACY_IV = "abcdefghijklmnop"
    }
}
