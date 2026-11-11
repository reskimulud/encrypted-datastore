package io.github.reskimulud.encrypteddatastore.serializer

import androidx.datastore.core.CorruptionException
import io.github.reskimulud.encrypteddatastore.algorithm.aes.AES
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class EncryptedSerializerTest {

    private val aes = AES.Builder().setKey(KEY).build()
    private val mapSerializer = MapSerializer(String.serializer(), String.serializer())
    private val serializer = EncryptedSerializer(
        defaultValue = emptyMap<String, String>(),
        serializer = mapSerializer,
        aes = aes,
    )

    @Test
    fun `writes and reads an encrypted object`() = runBlocking {
        val value = mapOf(
            "name" to "Reski Mulud",
            "email" to "reski.mulud@gmail.com",
        )
        val output = ByteArrayOutputStream()

        serializer.writeTo(value, output)
        val stored = output.toByteArray()

        assertFalse(stored.decodeToString().contains("Reski"))
        assertEquals(value, serializer.readFrom(ByteArrayInputStream(stored)))
    }

    @Test
    fun `returns the default value for an empty file`() = runBlocking {
        assertEquals(
            emptyMap<String, String>(),
            serializer.readFrom(ByteArrayInputStream(ByteArray(0))),
        )
    }

    @Test
    fun `rejects tampered data`() {
        val output = ByteArrayOutputStream()
        runBlocking { serializer.writeTo(mapOf("name" to "Reski"), output) }
        val stored = output.toByteArray().also {
            it[it.lastIndex] = (it.last().toInt() xor 0x01).toByte()
        }

        assertThrows(CorruptionException::class.java) {
            runBlocking { serializer.readFrom(ByteArrayInputStream(stored)) }
        }
    }

    @Test
    fun `rejects data that is not valid json`() {
        val stored = aes.encrypt("not json".encodeToByteArray())

        assertThrows(CorruptionException::class.java) {
            runBlocking { serializer.readFrom(ByteArrayInputStream(stored)) }
        }
    }

    private companion object {
        const val KEY = "0123456789abcdef0123456789abcdef"
    }
}
