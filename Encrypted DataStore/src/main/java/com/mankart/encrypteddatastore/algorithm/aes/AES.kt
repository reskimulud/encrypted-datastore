/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 06-10-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package com.mankart.encrypteddatastore.algorithm.aes
import com.mankart.encrypteddatastore.algorithm.aes.core.DecryptAES
import com.mankart.encrypteddatastore.algorithm.aes.core.EncryptAES
import com.mankart.encrypteddatastore.algorithm.aes.core.ExpandKey

class AES private constructor(
    builder: Builder,
    expandKey: ExpandKey,
    encryptAES: EncryptAES,
    decryptAES: DecryptAES
) {
    private var key: ByteArray = builder.key
    private var iv: ByteArray = builder.iv

    private var w: ByteArray
    private var nb: Int = 0
    private var nk: Int = 0
    private var nr: Int = 0

    init {
        nb = 4
        nk = 8
        nr = 14

        w = expandKey.expandKey(key)
    }


    /**
     * Class Builder
     * Untuk membuat instance class AES dengan menerapkan pattern builder
     */
    class Builder {
        internal lateinit var key: ByteArray
        internal lateinit var iv: ByteArray

        fun setKey(key: String): Builder {
            this.key = key.toByteArray()
            return this
        }

        fun setIv(iv: String): Builder {
            this.iv = iv.toByteArray()
            return this
        }

        fun build(): AES = getInstance(this)
    }

    companion object {
        @Volatile
        private var INSTANCE: AES? = null

        @JvmStatic
        fun getInstance(builder: Builder): AES =
            INSTANCE ?: synchronized(this) {
                val expandKey = ExpandKey()
                val encryptAES = EncryptAES()
                val decryptAES = DecryptAES()
                INSTANCE ?: AES(builder, expandKey, encryptAES, decryptAES)
            }.also { INSTANCE = it }
    }
}