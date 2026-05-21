package io.github.krisalord.auth.token

import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class RefreshTokenHashing(
    private val pepper: String
) {

    fun hmacSha256(raw: String): String {
        val mac = Mac.getInstance("HmacSHA256")

        val key = SecretKeySpec(
            pepper.toByteArray(StandardCharsets.UTF_8),
            "HmacSHA256"
        )

        mac.init(key)

        val digest = mac.doFinal(
            raw.toByteArray(StandardCharsets.UTF_8)
        )

        return digest.joinToString("") {
            "%02x".format(it)
        }
    }
}