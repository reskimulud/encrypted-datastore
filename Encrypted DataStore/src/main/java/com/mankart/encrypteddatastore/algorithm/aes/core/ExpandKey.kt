/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 06-10-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package com.mankart.encrypteddatastore.algorithm.aes.core

class ExpandKey {
    internal fun expandKey(key: ByteArray): ByteArray {
        return ByteArray(1) {0xff.toByte()}
    }
}