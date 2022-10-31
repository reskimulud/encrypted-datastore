package io.reskimulud.encrypteddatastore.di

import android.content.Context
import io.reskimulud.encrypteddatastore.data.UserRepository
import io.reskimulud.encrypteddatastore.data.datastore.PreferencesDataStore
import io.reskimulud.encrypteddatastore.presentation.encryptedDataStore
import io.reskimulud.encrypteddatastore.presentation.unencryptedDataStore

object Injection {
    fun provideUserRepository(context: Context): UserRepository {
        val dataStore = PreferencesDataStore.getInstance(context.encryptedDataStore, context.unencryptedDataStore)
        return UserRepository.getInstance(dataStore)
    }
}