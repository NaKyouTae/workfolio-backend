package com.spectrum.workfolio.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

object TimeUtil {
    fun toEpochMilli(at: LocalDateTime): Long {
        return at.toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    fun toEpochMilli(at: LocalDate): Long {
        return at.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }

    fun ofEpochMilli(epochMilli: Long): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneOffset.UTC)
    }

    fun nowToLong(): Long {
        return toEpochMilli(LocalDateTime.now())
    }

    fun nowToString(): String {
        return LocalDateTime.now().toString()
    }

    fun ofEpochMilliNullable(at: Long): LocalDateTime? {
        return if (at != 0L) {
            this.ofEpochMilli(at)
        } else {
            null
        }
    }
}
