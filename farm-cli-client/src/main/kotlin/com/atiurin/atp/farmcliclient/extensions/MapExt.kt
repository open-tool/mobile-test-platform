package com.atiurin.atp.farmcliclient.extensions

fun Map<String, String>.maskSensitiveData(): Map<String, String> {
    return this.mapValues { (key, value) ->
        if (SENSITIVE_DATA_KEYS.any { sensitiveKey -> key.contains(sensitiveKey, ignoreCase = true) }) {
            "***masked***"
        } else {
            value
        }
    }
}

val SENSITIVE_DATA_KEYS = mutableListOf(
    "KEY",
    "PASSWORD",
    "PWD",
    "SSH",
    "SECRET",
    "TOKEN",
    "AUTH",
    "CREDENTIALS",
    "PRIVATE",
    "PROXY",
    "CERT",
    "SSL",
    "TLS",
    "PEM",
    "PKCS",
    "RSA",
    "DSA",
    "ECDSA",
    "EDDSA",
    "X509",
    "HMAC",
    "JWT",
    "OAUTH",
    "PROD",
    "CONFIG"
)
