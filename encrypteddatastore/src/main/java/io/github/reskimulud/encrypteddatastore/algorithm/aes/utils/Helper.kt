/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 06-10-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package io.github.reskimulud.encrypteddatastore.algorithm.aes.utils

import io.github.reskimulud.encrypteddatastore.algorithm.aes.utils.Constant.invSBox
import io.github.reskimulud.encrypteddatastore.algorithm.aes.utils.Constant.sBox
import kotlin.experimental.xor
import kotlin.math.min

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

    @Suppress("NAME_SHADOWING")
    internal fun multiple(a: Int, b: Int): Int {
        var a = a
        var b = b
        var sum = 0
        while (a != 0) {
            if (a and 1 != 0) {
                sum = sum xor b
            }
            b = xTime(b)
            a = a ushr 1
        }
        return sum
    }

    private fun xTime(b: Int): Int {
        return if (b and 0x80 == 0) {
            b shl 1
        } else b shl 1 xor 0x11b
    }

    internal fun xor(a: ByteArray?, b: ByteArray): ByteArray {
        val result = ByteArray(min(a!!.size, b.size))
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