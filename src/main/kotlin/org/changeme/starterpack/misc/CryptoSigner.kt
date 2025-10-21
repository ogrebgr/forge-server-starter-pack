package org.changeme.starterpack.misc

import com.goterl.lazysodium.LazySodium
import com.goterl.lazysodium.utils.Key
import javax.inject.Inject
import kotlin.text.toByteArray

interface CryptoVerifier {
    fun verify(message: String, signature: String): Boolean
    fun verify(message: ByteArray, signature: ByteArray): Boolean
    fun verify(message: String, signature: ByteArray): Boolean
}

interface CryptoSigner {
    fun sign(message: String): String
    fun getKeyIndex(): Int
}

interface CryptoSignerVerifier : CryptoSigner, CryptoVerifier


class CryptoSignerImpl @Inject constructor(
    private val lazySodium: LazySodium,
    private val secretKey: Key,
    private val keyIndex: Int,
) : CryptoSigner {
    override fun sign(message: String): String {
        return lazySodium.cryptoSignDetached(message, secretKey)
    }

    override fun getKeyIndex(): Int {
        return keyIndex
    }
}

class CryptoSignerVerifierImpl @Inject constructor(
    private val lazySodium: LazySodium,
    private val secretKey: Key,
    private val publicKey: Key,
    private val keyIndex: Int,
) : CryptoSignerVerifier {
    override fun sign(message: String): String {
        return lazySodium.cryptoSignDetached(message, secretKey)
    }

    override fun getKeyIndex(): Int {
        return keyIndex
    }

    override fun verify(message: String, signature: String): Boolean {
        return lazySodium.cryptoSignVerifyDetached(signature, message, publicKey)
    }

    override fun verify(message: ByteArray, signature: ByteArray): Boolean {
        return lazySodium.cryptoSignVerifyDetached(signature, message, message.size, publicKey.asBytes)
    }

    override fun verify(message: String, signature: ByteArray): Boolean {
        val msgB = message.toByteArray()
        return lazySodium.cryptoSignVerifyDetached(signature, msgB, msgB.size, publicKey.asBytes)
    }
}


class CryptoVerifierImpl @Inject constructor(private val lazySodium: LazySodium, private val publicKey: Key) : CryptoVerifier {
    constructor(lazySodium: LazySodium, publicKeyStr: String) : this(lazySodium, Key.fromHexString(publicKeyStr))


    override fun verify(message: String, signature: String): Boolean {
        return lazySodium.cryptoSignVerifyDetached(signature, message, publicKey)
    }

    override fun verify(message: ByteArray, signature: ByteArray): Boolean {
        return lazySodium.cryptoSignVerifyDetached(signature, message, message.size, publicKey.asBytes)
    }

    override fun verify(message: String, signature: ByteArray): Boolean {
        val msgB = message.toByteArray()
        return lazySodium.cryptoSignVerifyDetached(signature, msgB, msgB.size, publicKey.asBytes)
    }
}