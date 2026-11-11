package io.reskimulud.encrypteddatastore.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.reskimulud.encrypteddatastore.algorithm.aes.AES
import io.github.reskimulud.encrypteddatastore.legacy.LegacyEncryptedPreferences
import io.reskimulud.encrypteddatastore.data.UserProfile
import kotlinx.coroutines.flow.first
import java.io.File

private val Context.legacyEncryptedDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "encrypted_users_data")

private val USER_NAME_KEY = stringPreferencesKey("user_name")
private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
private val USER_PHONE_NUMBER_KEY = stringPreferencesKey("user_phone_number")
private val USER_API_KEY = stringPreferencesKey("user_api_key")

/**
 * One-time example migration from the Preferences layout used by 0.0.3.
 * It only runs while the old store still contains data.
 */
suspend fun migrateLegacyPreferences(
    context: Context,
    aes: AES,
    dataStore: DataStore<UserProfile>,
) {
    val legacy = context.legacyEncryptedDataStore.data.first()
    if (legacy.asMap().isEmpty()) return

    fun value(key: Preferences.Key<String>): String =
        LegacyEncryptedPreferences.decryptString(aes, legacy[key]) ?: ""

    // Abort without touching the new store if a value cannot be decrypted
    // (for example, when the key or IV has changed).
    val profile = runCatching {
        UserProfile(
            name = value(USER_NAME_KEY),
            email = value(USER_EMAIL_KEY),
            phoneNumber = value(USER_PHONE_NUMBER_KEY),
            apiKey = value(USER_API_KEY),
        )
    }.getOrNull() ?: return

    dataStore.updateData { profile }
    context.legacyEncryptedDataStore.edit { it.clear() }

    // The old app also wrote a plaintext copy of the same fields.
    File(context.filesDir, "datastore/unencrypted_users_data.preferences_pb").delete()
}
