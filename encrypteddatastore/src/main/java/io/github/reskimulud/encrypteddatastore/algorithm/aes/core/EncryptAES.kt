/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 06-10-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package io.github.reskimulud.encrypteddatastore.algorithm.aes.core

import io.github.reskimulud.encrypteddatastore.algorithm.aes.utils.Helper.multiple
import io.github.reskimulud.encrypteddatastore.algorithm.aes.utils.Helper.subWord
import kotlin.experimental.and

internal class EncryptAES(nb: Int, nr: Int) {
    private var actual = 0
    private var nb = 4
    private var nr = 0
    internal lateinit var w: IntArray


    // state
    private var state: Array<Array<IntArray>>

    init {
        this.nb = nb
        this.nr = nr
        state = Array(2) { Array(4) { IntArray(this.nb) } }
    }

    internal fun encrypt(text: ByteArray): ByteArray {
        require(text.size == 16) { "Hanya blok 16 karakter yang bisa di enkripsi" }
        val out = ByteArray(text.size)
        for (i in 0 until nb) { // columns
            for (j in 0..3) { // rows
                state[0][j][i] = (text[i * nb + j] and 0xff.toByte()).toInt()
            }
        }
        cipher(state[0], state[1])
        for (i in 0 until nb) {
            for (j in 0..3) {
                out[i * nb + j] = (state[1][j][i] and 0xff).toByte()
            }
        }
        return out
    }

    private fun cipher(`in`: Array<IntArray>, out: Array<IntArray>): Array<IntArray> {
        for (i in `in`.indices) {
            for (j in `in`.indices) {
                out[i][j] = `in`[i][j]
            }
        }
        actual = 0
        addRoundKey(out, actual)
        actual = 1
        while (actual < nr) {
            subBytes(out)
            shiftRows(out)
            mixColumns(out)
            addRoundKey(out, actual)
            actual++
        }
        subBytes(out)
        shiftRows(out)
        addRoundKey(out, actual)
        return out
    }

    private fun addRoundKey(s: Array<IntArray>, round: Int): Array<IntArray> {
        for (c in 0 until nb) {
            for (r in 0..3) {
                s[r][c] = s[r][c] xor (w[round * nb + c] shl r * 8 ushr 24)
            }
        }
        return s
    }

    private fun subBytes(state: Array<IntArray>): Array<IntArray> {
        for (i in 0..3) {
            for (j in 0 until nb) {
                state[i][j] = subWord(state[i][j]) and 0xFF
            }
        }
        return state
    }

    private fun shiftRows(state: Array<IntArray>): Array<IntArray> {

        // baris 1
        var temp1: Int = state[1][0]
        var i = 0
        while (i < nb - 1) {
            state[1][i] = state[1][(i + 1) % nb]
            i++
        }
        state[1][nb - 1] = temp1

        // baris 2, bergeser 1-byte
        temp1 = state[2][0]
        var temp2: Int = state[2][1]
        i = 0
        while (i < nb - 2) {
            state[2][i] = state[2][(i + 2) % nb]
            i++
        }
        state[2][nb - 2] = temp1
        state[2][nb - 1] = temp2

        // baris 3, bergeser 2-bytes
        temp1 = state[3][0]
        temp2 = state[3][1]
        val temp3: Int = state[3][2]
        i = 0
        while (i < nb - 3) {
            state[3][i] = state[3][(i + 3) % nb]
            i++
        }
        state[3][nb - 3] = temp1
        state[3][nb - 2] = temp2
        state[3][nb - 1] = temp3
        return state
    }

    private fun mixColumns(state: Array<IntArray>): Array<IntArray> {
        var temp0: Int
        var temp1: Int
        var temp2: Int
        var temp3: Int
        for (c in 0 until nb) {
            temp0 = multiple(0x02, state[0][c]) xor multiple(0x03, state[1][c]) xor state[2][c] xor state[3][c]
            temp1 = state[0][c] xor multiple(0x02, state[1][c]) xor multiple(0x03, state[2][c]) xor state[3][c]
            temp2 = state[0][c] xor state[1][c] xor multiple(0x02, state[2][c]) xor multiple(0x03, state[3][c])
            temp3 = multiple(0x03, state[0][c]) xor state[1][c] xor state[2][c] xor multiple(0x02, state[3][c])
            state[0][c] = temp0
            state[1][c] = temp1
            state[2][c] = temp2
            state[3][c] = temp3
        }
        return state
    }
}