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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import io.reskimulud.encrypteddatastore.data.UserProfile
import io.reskimulud.encrypteddatastore.data.UserRepository
import io.reskimulud.encrypteddatastore.data.network.ImageResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {
    val userProfile: LiveData<UserProfile> = repository.userProfile.asLiveData()
    val userName: LiveData<String> = userProfile.map { it.name }
    val userEmail: LiveData<String> = userProfile.map { it.email }
    val userPhoneNumber: LiveData<String> = userProfile.map { it.phoneNumber }
    val userApiKey: LiveData<String> = userProfile.map { it.apiKey }

    private val _encryptedData = MutableLiveData<String>()
    val encryptedData: LiveData<String> = _encryptedData

    private var _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> = _imageUrl

    private var _loadingState = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> = _loadingState

    private var _errorState = MutableLiveData<Boolean>()
    val errorState: LiveData<Boolean> = _errorState

    private var _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun saveProfile(name: String, email: String, phoneNumber: String, apiKey: String) {
        viewModelScope.launch {
            repository.updateProfile {
                UserProfile(name, email, phoneNumber, apiKey)
            }
        }
    }

    fun refreshEncryptedData() {
        viewModelScope.launch {
            _encryptedData.value = repository.readEncryptedFile()
        }
    }

    fun getRandomImage(apiKey: String) {
        _loadingState.value = true
        _errorState.value = false
        _message.value = "Loading Image..."
        val client = repository.getRandomImage(apiKey)
        client.enqueue(object: Callback<ImageResponse> {
            override fun onResponse(
                call: Call<ImageResponse>,
                response: Response<ImageResponse>
            ) {
                viewModelScope.launch(Dispatchers.IO) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        _imageUrl.postValue(
                            responseBody?.urls?.small
                        )
                        _loadingState.postValue(false)
                    } else {
                        Log.e("UserViewModel", "error 400+")
                        _loadingState.postValue(false)
                        _errorState.postValue(true)
                        _message.postValue("Error!. ${response.code()}")
                    }
                }
            }

            override fun onFailure(call: Call<ImageResponse>, t: Throwable) {
                _loadingState.value = false
                _message.value = "Failed to Load Image: ${t.message}"
                _errorState.value = true
                Log.e("UserViewModel", t.message.toString())
            }

        })
    }
}
