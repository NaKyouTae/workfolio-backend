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

    fun ofEpochMilliNullable(endedAt: Long): LocalDateTime? {
        return if (endedAt != 0L) {
            this.ofEpochMilli(endedAt)
        } else {
            null
        }
    }
}
