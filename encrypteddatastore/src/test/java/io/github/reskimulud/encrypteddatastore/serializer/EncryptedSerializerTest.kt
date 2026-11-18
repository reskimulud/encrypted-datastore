package io.github.reskimulud.encrypteddatastore.serializer

import androidx.datastore.core.CorruptionException
import io.github.reskimulud.encrypteddatastore.V1_ENCRYPTED_JSON_HEX
import io.github.reskimulud.encrypteddatastore.algorithm.Encryptor
import io.github.reskimulud.encrypteddatastore.algorithm.aes.AES
import io.github.reskimulud.encrypteddatastore.hexToBytes
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.InvalidKeyException
import javax.crypto.AEADBadTagException

@OptIn(ExperimentalSerializationApi::class)
class EncryptedSerializerTest {

    private val aes = AES.Builder().setKey(KEY).build()
    private val mapSerializer: KSerializer<Map<String, String>> =
        MapSerializer(String.serializer(), String.serializer())
    private val serializer: EncryptedSerializer<Map<String, String>> = EncryptedSerializer(
        defaultValue = emptyMap<String, String>(),
        serializer = mapSerializer,
        encryptor = aes,
    )
    private val protoSerializer: EncryptedSerializer<ProtoSettings> = EncryptedSerializer(
        defaultValue = ProtoSettings(),
        serializer = ProtoSettings.serializer(),
        encryptor = aes,
        format = ProtoBuf { encodeDefaults = true },
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
    fun `writes and reads protobuf data`() = runBlocking {
        val value = ProtoSettings(
            name = "Reski",
            payload = byteArrayOf(0, 1, -1, 0x7f),
            tags = listOf("a", "b"),
            nested = ProtoNested(counter = 7),
        )
        val output = ByteArrayOutputStream()

        protoSerializer.writeTo(value, output)
        val restored = protoSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

        assertEquals(value.name, restored.name)
        assertArrayEquals(value.payload, restored.payload)
        assertEquals(value.tags, restored.tags)
        assertEquals(value.nested, restored.nested)
    }

    @Test
    fun `accepts a custom json instance`() = runBlocking {
        val custom = EncryptedSerializer(
            defaultValue = emptyMap<String, String>(),
            serializer = mapSerializer,
            encryptor = aes,
            json = Json { encodeDefaults = true },
        )
        val output = ByteArrayOutputStream()

        custom.writeTo(mapOf("name" to "Reski"), output)

        assertEquals(
            mapOf("name" to "Reski"),
            custom.readFrom(ByteArrayInputStream(output.toByteArray())),
        )
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

    @Test
    fun `reads a file written by version 0_2_0`() = runBlocking {
        val stored = V1_ENCRYPTED_JSON_HEX.hexToBytes()

        assertEquals(
            mapOf(
                "name" to "Reski Mulud",
                "email" to "reski.mulud@gmail.com",
            ),
            serializer.readFrom(ByteArrayInputStream(stored)),
        )
    }

    @Test
    fun `accepts a custom encryptor`() = runBlocking {
        val reversing = object : Encryptor {
            override fun encrypt(plaintext: ByteArray): ByteArray = plaintext.reversedArray()

            override fun decrypt(data: ByteArray): ByteArray = data.reversedArray()
        }
        val custom = EncryptedSerializer(
            defaultValue = emptyMap<String, String>(),
            serializer = mapSerializer,
            encryptor = reversing,
        )
        val value = mapOf("name" to "Reski")
        val output = ByteArrayOutputStream()

        custom.writeTo(value, output)

        assertEquals(value, custom.readFrom(ByteArrayInputStream(output.toByteArray())))
    }

    @Test
    fun `reports tag failures as corruption`() {
        val failing = object : Encryptor {
            override fun encrypt(plaintext: ByteArray): ByteArray = plaintext

            override fun decrypt(data: ByteArray): ByteArray = throw AEADBadTagException()
        }
        val custom = EncryptedSerializer(
            defaultValue = emptyMap<String, String>(),
            serializer = mapSerializer,
            encryptor = failing,
        )

        assertThrows(CorruptionException::class.java) {
            runBlocking { custom.readFrom(ByteArrayInputStream(byteArrayOf(0x02))) }
        }
    }

    @Test
    fun `propagates key errors`() {
        val failing = object : Encryptor {
            override fun encrypt(plaintext: ByteArray): ByteArray = plaintext

            override fun decrypt(data: ByteArray): ByteArray = throw InvalidKeyException("locked")
        }
        val custom = EncryptedSerializer(
            defaultValue = emptyMap<String, String>(),
            serializer = mapSerializer,
            encryptor = failing,
        )

        assertThrows(InvalidKeyException::class.java) {
            runBlocking { custom.readFrom(ByteArrayInputStream(byteArrayOf(0x02))) }
        }
    }

    private companion object {
        const val KEY = "0123456789abcdef0123456789abcdef"
    }
}

@Serializable
private data class ProtoSettings(
    val name: String = "",
    val payload: ByteArray = ByteArray(0),
    val tags: List<String> = emptyList(),
    val nested: ProtoNested = ProtoNested(),
)

@Serializable
private data class ProtoNested(
    val counter: Int = 0,
)
