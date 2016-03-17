package com.github.shiraji

import java.math.BigInteger
import java.security.MessageDigest


fun String.toMd5(): String {
    return BigInteger(1, MessageDigest.getInstance("MD5").digest(this.toByteArray())).toString(16)
}

fun String.subtract(text: String): String {
    return this.replace(text, "")
}