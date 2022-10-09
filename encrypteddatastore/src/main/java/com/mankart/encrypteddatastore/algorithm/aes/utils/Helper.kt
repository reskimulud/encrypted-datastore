/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 06-10-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package com.mankart.encrypteddatastore.algorithm.aes.utils

import com.mankart.encrypteddatastore.algorithm.aes.utils.Constant.invSBox
import com.mankart.encrypteddatastore.algorithm.aes.utils.Constant.sBox
import kotlin.experimental.xor

object Helper {
    internal fun subWord(word: Int): Int {
        var subWord = 0
        var i = 24
        while (i >= 0) {
            val `in` = word shl i ushr 24
            subWord = subWord or (sBox[`in`] shl 24 - i)
            i -= 8
        }
        return subWord
    }

    internal fun invSubWord(word: Int): Int {
        var subWord = 0
        var i = 24
        while (i >= 0) {
            val `in` = word shl i ushr 24
            subWord = subWord or (invSBox[`in`] shl 24 - i)
            i -= 8
        }
        return subWord
    }

    internal fun multiple(a: Int, b: Int): Int {
        var a = a
        var b = b
        var sum = 0
        while (a != 0) { // while it is not 0
            if (a and 1 != 0) { // check if the first bit is 1
                sum = sum xor b // add b from the smallest bit
            }
            b = xTime(b) // bit shift left mod 0x11b if necessary;
            a = a ushr 1 // lowest bit of "a" was used so shift right
        }
        return sum
    }

    private fun xTime(b: Int): Int {
        return if (b and 0x80 == 0) {
            b shl 1
        } else b shl 1 xor 0x11b
    }

    internal fun xor(a: ByteArray?, b: ByteArray): ByteArray {
        val result = ByteArray(Math.min(a!!.size, b.size))
        for (j in result.indices) {
            val xor: Byte = a[j] xor b[j]
            result[j] = (0xff and xor.toInt()).toByte()
        }
        return result
    }

    internal fun ByteArray.trim(): ByteArray {
        var indices = this.size - 1
        while (indices >= 0 && this[indices] == 0.toByte()) {
            indices--
        }
        return this.copyOf(indices + 1)
    }
}