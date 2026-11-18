/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 06-10-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package io.github.reskimulud.encrypteddatastore.algorithm

/**
 * Encrypts and decrypts byte arrays.
 *
 * The payload returned by [encrypt] must be self-contained, so [decrypt] can
 * read it without extra parameters. Implementations are safe to share within a
 * process.
 */
interface Encryptor {

    /**
     * Encrypts [plaintext] into the payload that [decrypt] expects.
     */
    fun encrypt(plaintext: ByteArray): ByteArray

    /**
     * Decrypts a payload written by [encrypt].
     *
     * @throws java.security.GeneralSecurityException if authentication fails,
     * which usually means the key is wrong or the data was tampered with.
     */
    fun decrypt(data: ByteArray): ByteArray
}
