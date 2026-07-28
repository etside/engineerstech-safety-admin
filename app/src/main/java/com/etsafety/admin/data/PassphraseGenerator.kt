package com.etsafety.admin.data

import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object PassphraseGenerator {

    private const val SECRET_KEY = "eT_Premium_Secret_2024"
    private const val ALGORITHM = "HmacSHA256"

    fun generatePassphrase(identifier: String, tier: String, expiryTimestamp: Long): String {
        val data = "$identifier|$tier|$expiryTimestamp"
        val mac = Mac.getInstance(ALGORITHM)
        val secretKeySpec = SecretKeySpec(SECRET_KEY.toByteArray(), ALGORITHM)
        mac.init(secretKeySpec)
        val hash = mac.doFinal(data.toByteArray())

        // Convert first 12 bytes of hash to hex segments
        val hex = hash.take(12).joinToString("") { "%02x".format(it) }.uppercase()

        // Format: ETG-XXXX-XXXX-XXXX
        val segment1 = hex.substring(0, 4)
        val segment2 = hex.substring(4, 8)
        val segment3 = hex.substring(8, 12)

        return "ETG-$segment1-$segment2-$segment3"
    }
}
