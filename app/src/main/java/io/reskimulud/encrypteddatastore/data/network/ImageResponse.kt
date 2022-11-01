/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 01-11-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package io.reskimulud.encrypteddatastore.data.network

import com.google.gson.annotations.SerializedName

data class ImageResponse(
    @field:SerializedName("id")
    val id: String,

    @field:SerializedName("urls")
    val urls: Urls
)

data class Urls(
    @field:SerializedName("small")
    val small: String
)