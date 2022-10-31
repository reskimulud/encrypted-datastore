/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 11-10-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package io.reskimulud.encrypteddatastore.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.reskimulud.encrypteddatastore.data.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(
    private val repository: UserRepository
): ViewModel() {
    val userName: LiveData<String> = repository.getUserName().asLiveData()
    val userEmail: LiveData<String> = repository.getUserEmail().asLiveData()
    val userApiKey: LiveData<String> = repository.getUserApiKey().asLiveData()

    fun setUserName(name: String) {
        viewModelScope.launch {
            Log.e("ViewModel", "SetUserName")
            repository.updateUserName(name)
            repository.updateUnencryptedUserName(name)
        }
    }

    fun setUserEmail(email: String) {
        viewModelScope.launch {
            Log.e("ViewModel", "SetUserEmail")
            repository.updateUserEmail(email)
            repository.updateUnencryptedUserEmail(email)
        }
    }

    fun setUserApiKey(apiKey: String) {
        viewModelScope.launch {
            Log.e("ViewModel", "SetUserApiKey")
            repository.updateUserApiKey(apiKey)
            repository.updateUnencryptedUserApiKey(apiKey)
        }
    }
}