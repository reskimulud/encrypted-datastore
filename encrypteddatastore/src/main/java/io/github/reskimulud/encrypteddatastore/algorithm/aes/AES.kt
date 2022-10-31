/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 06-10-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package io.github.reskimulud.encrypteddatastore.algorithm.aes

import io.github.reskimulud.encrypteddatastore.algorithm.aes.core.DecryptAES
import io.github.reskimulud.encrypteddatastore.algorithm.aes.core.EncryptAES
import io.github.reskimulud.encrypteddatastore.algorithm.aes.core.ExpandKey
import io.github.reskimulud.encrypteddatastore.algorithm.aes.utils.Helper.trim
import io.github.reskimulud.encrypteddatastore.algorithm.aes.utils.Helper.xor
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class AES private constructor(
    builder: Builder,
    expandKey: ExpandKey,
    encryptAES: EncryptAES,
    decryptAES: DecryptAES
) {
    private var key: IntArray = builder.key
    private var iv: ByteArray = builder.iv
    private var w: IntArray

    private val encryptAES: EncryptAES
    private val decryptAES: DecryptAES

    init {
        w = expandKey.expandKey(key, nk, nb, nr)
        this.encryptAES = encryptAES
        this.decryptAES = decryptAES
        w.let {
            this.encryptAES.w = it
            this.decryptAES.w = it
        }
    }

    fun encrypt(text: ByteArray): ByteArray {
        var previousBlock: ByteArray? = null
        val out = ByteArrayOutputStream()
        var i = 0
        while (i < text.size) {
            var part = Arrays.copyOfRange(text, i, i + 16)
            try {
                if (previousBlock == null) previousBlock = iv
                part = xor(previousBlock, part)
                previousBlock = encryptAES.encrypt(part)
                out.write(previousBlock)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            i += 16
        }
        return out.toByteArray()
    }

    fun decrypt(text: ByteArray): ByteArray {
        var previousBlock: ByteArray? = null
        val out = ByteArrayOutputStream()
        var i = 0
        while (i < text.size) {
            val lastBlock = (text.size - i) <= 16
            val part = Arrays.copyOfRange(text, i, i + 16)
            var tmp = decryptAES.decrypt(part)
            try {
                if (previousBlock == null) previousBlock = iv
                tmp = xor(previousBlock, tmp)
                previousBlock = part

                val trimmedByteArray =
                    if (lastBlock) tmp.trim()
                    else tmp

                out.write(trimmedByteArray)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            i += 16
        }
        return out.toByteArray()
    }


    /**
     * Class Builder
     * Untuk membuat instance class AES dengan menerapkan pattern builder
     */
    class Builder {
        internal lateinit var key: IntArray
        internal lateinit var iv: ByteArray

        fun setKey(key: String): Builder {
            val mKey = key.toByteArray()

            if (mKey.size != 32) {
                throw IllegalArgumentException("Panjang kunci hanya diperbolehkan sebesar 256-bit")
            }

            this.key = IntArray(mKey.size)
            for (i in key.indices) {
                this.key[i] = mKey[i].toInt()
            }
            return this
        }

        fun setIv(iv: String): Builder {
            this.iv = iv.toByteArray()
            return this
        }

        fun build(): AES = getInstance(this)
    }

    private companion object {
        private var nb: Int = 4
        private var nr: Int = 14
        private var nk: Int = 8

        @Volatile
        private var INSTANCE: AES? = null

        @JvmStatic
        private fun getInstance(builder: Builder): AES =
            INSTANCE ?: synchronized(this) {
                val expandKey = ExpandKey()
                val encryptAES = EncryptAES(nb, nr)
                val decryptAES = DecryptAES(nb, nr)
                INSTANCE ?: AES(builder, expandKey, encryptAES, decryptAES)
            }.also { INSTANCE = it }
    }
}