package io.reskimulud.encrypteddatastore.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import io.github.reskimulud.encrypteddatastore.BuildConfig
import io.github.reskimulud.encrypteddatastore.algorithm.aes.AES
import io.github.reskimulud.encrypteddatastore.serializer.EncryptedSerializer
import io.reskimulud.encrypteddatastore.data.UserProfile
import java.io.File

const val USER_DATA_STORE_FILE = "user_profile.enc"

fun createAES(): AES {
    val builder = AES.Builder().setKey(BuildConfig.CIPHER_KEY)
    if (BuildConfig.IV.isNotEmpty()) builder.setIv(BuildConfig.IV)
    return builder.build()
}

fun createUserDataStore(context: Context, aes: AES): DataStore<UserProfile> =
    DataStoreFactory.create(
        serializer = EncryptedSerializer(
            defaultValue = UserProfile(),
            serializer = UserProfile.serializer(),
            aes = aes,
        ),
    ) {
        userDataStoreFile(context)
    }

fun userDataStoreFile(context: Context): File {
    val directory = File(context.filesDir, "datastore")
    directory.mkdirs()
    return File(directory, USER_DATA_STORE_FILE)
}
