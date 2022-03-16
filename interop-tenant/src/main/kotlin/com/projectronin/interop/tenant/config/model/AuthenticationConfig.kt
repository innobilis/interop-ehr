package com.projectronin.interop.tenant.config.model

/**
 * Configuration associated to authentication for a tenant.
 * @property publicKey The public key used for authentication.
 * @property privateKey The private key used for authentication.
 */
data class AuthenticationConfig(
    val publicKey: String,
    val privateKey: String,
) {
    // Override toString() to prevent accidentally leaking the privateKey
    override fun toString(): String = this::class.simpleName!!
}
