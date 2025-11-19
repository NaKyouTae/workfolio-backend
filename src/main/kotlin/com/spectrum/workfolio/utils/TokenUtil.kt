package com.spectrum.workfolio.utils

object TokenUtil {
    fun getTokenTtlSeconds(exp: Long): Long {
        val now = System.currentTimeMillis() / 1000
        return maxOf((exp - now), 0)
    }
}