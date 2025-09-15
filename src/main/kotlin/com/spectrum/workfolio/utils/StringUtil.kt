package com.spectrum.workfolio.utils

import java.util.UUID

object StringUtil {
    fun generateRandomString(size: Int): String {
        return UUID.randomUUID().toString().replace("-", "").substring(0, size).uppercase()
    }

    fun generateUUID(prefix: String): String {
        val sf = UUID.randomUUID().toString().replace("-", "").substring(0, 14).uppercase()
        return prefix + sf
    }
}