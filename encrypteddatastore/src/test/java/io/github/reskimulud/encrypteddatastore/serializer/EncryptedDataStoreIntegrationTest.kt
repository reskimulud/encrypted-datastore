package io.github.reskimulud.encrypteddatastore.serializer

import androidx.datastore.core.DataStoreFactory
import io.github.reskimulud.encrypteddatastore.algorithm.aes.AES
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class EncryptedDataStoreIntegrationTest {

    @Test
    fun `typed datastore round trips through a file`() = runBlocking {
        val file = File.createTempFile("encrypted-datastore", ".enc").also { it.delete() }
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val serializer = EncryptedSerializer(
            defaultValue = emptyMap<String, String>(),
            serializer = MapSerializer(String.serializer(), String.serializer()),
            aes = AES.Builder().setKey(KEY).build(),
        )
        val store = DataStoreFactory.create(serializer = serializer, scope = scope) { file }

        try {
            store.updateData { mapOf("name" to "Reski") }
            assertEquals(mapOf("name" to "Reski"), store.data.first())
        } finally {
            scope.cancel()
            file.delete()
            File(file.parentFile, "${file.name}.tmp").delete()
        }
    }

    private companion object {
        const val KEY = "0123456789abcdef0123456789abcdef"
    }
}
