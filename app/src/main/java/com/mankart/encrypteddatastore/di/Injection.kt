package com.mankart.encrypteddatastore.di

import android.content.Context
import com.mankart.encrypteddatastore.data.UserRepository
import com.mankart.encrypteddatastore.data.datastore.PreferencesDataStore
import com.mankart.encrypteddatastore.presentation.dataStore

object Injection {
    fun provideUserRepository(context: Context): UserRepository {
        val dataStore = PreferencesDataStore.getInstance(context.dataStore)
        return UserRepository.getInstance(dataStore)
    }
}