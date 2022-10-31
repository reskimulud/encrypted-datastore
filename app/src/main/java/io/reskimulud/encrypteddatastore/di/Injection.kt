package io.reskimulud.encrypteddatastore.di

import android.content.Context
import io.reskimulud.encrypteddatastore.data.UserRepository
import io.reskimulud.encrypteddatastore.data.datastore.PreferencesDataStore
import io.reskimulud.encrypteddatastore.presentation.dataStore

object Injection {
    fun provideUserRepository(context: Context): UserRepository {
        val dataStore = PreferencesDataStore.getInstance(context.dataStore)
        return UserRepository.getInstance(dataStore)
    }
}