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
import androidx.lifecycle.viewModelScope
import io.reskimulud.encrypteddatastore.data.UserRepository
import io.reskimulud.encrypteddatastore.data.network.ImageResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserViewModel(
    private val repository: UserRepository
): ViewModel() {
    val userName: LiveData<String> = repository.getUserName().asLiveData()
    val userEmail: LiveData<String> = repository.getUserEmail().asLiveData()
    val userApiKey: LiveData<String> = repository.getUserApiKey().asLiveData()

    private var _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> = _imageUrl

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

    fun getRandomImage(apiKey: String) {
        val client = repository.getRandomImage(apiKey)
        client.enqueue(object: Callback<List<ImageResponse>> {
            override fun onResponse(
                call: Call<List<ImageResponse>>,
                response: Response<List<ImageResponse>>
            ) {
                viewModelScope.launch(Dispatchers.IO) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        _imageUrl.postValue(
                            responseBody?.get(0)?.urls?.small
                        )
                    }
                }
            }

            override fun onFailure(call: Call<List<ImageResponse>>, t: Throwable) {
                Log.e("UserViewModel", t.message.toString())
            }

        })
    }
}