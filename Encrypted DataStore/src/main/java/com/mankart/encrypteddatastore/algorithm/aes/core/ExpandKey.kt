/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 06-10-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package com.mankart.encrypteddatastore.algorithm.aes.core

import com.mankart.encrypteddatastore.algorithm.aes.utils.Constant.rCon
import com.mankart.encrypteddatastore.algorithm.aes.utils.Helper.subWord

class ExpandKey {
    private lateinit var w: IntArray

    internal fun expandKey(key: IntArray, nk: Int, nb: Int, nr: Int): IntArray {
        w = IntArray(nb * (nr + 1))

        var temp: Int
        var i = 0
        while (i < nk) {
            w[i] = 0x00000000
            w[i] = w[i] or (key[4 * i] shl 24)
            w[i] = w[i] or (key[4 * i + 1] shl 16)
            w[i] = w[i] or (key[4 * i + 2] shl 8)
            w[i] = w[i] or key[4 * i + 3]

            i++
        }
        i = nk
        while (i < nb * (nr + 1)) {
            temp = w[i - 1]
            if (i % nk == 0) {
                // apply an XOR with a constant round rCon.
                val roundRCon = rCon[i / nk] shl 24
                val tempAfterRotWordAndSubWord = subWord(rotWord(temp))
                temp = tempAfterRotWordAndSubWord xor roundRCon
            }
            if (nk > 6 && i % nk == 4) {
                temp = subWord(temp)
            }
            w[i] = w[i - nk] xor temp
            i++
        }
        return w
    }

    private fun rotWord(word: Int): Int {
        return word shl 8 or (word and -0x1000000 ushr 24)
    }
}