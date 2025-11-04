package com.spectrum.workfolio.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object TimeUtil {
    fun toEpochMilli(at: LocalDateTime): Long {
        return at.toInstant(ZoneId.systemDefault().rules.getOffset(at)).toEpochMilli()
    }

    fun toEpochMilli(at: LocalDate): Long {
        return at.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun ofEpochMilli(epochMilli: Long): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault())
    }

    fun now(): LocalDateTime {
        return LocalDateTime.now()
    }

    fun nowToLong(): Long {
        return toEpochMilli(LocalDateTime.now())
    }

    fun nowToString(): String {
        return LocalDateTime.now().toString()
    }

    fun ofEpochMilliNullable(at: Long?): LocalDateTime? {
        return if (at != null && at != 0L) {
            this.ofEpochMilli(at)
        } else {
            null
        }
    }

    fun toEpochMilliNullable(at: LocalDateTime?): Long? {
        return at?.let { this.toEpochMilli(it) }
    }

    fun dateStart(date: LocalDateTime): LocalDateTime {
        return date.toLocalDate().atStartOfDay()
    }

    fun dateEnd(date: LocalDateTime): LocalDateTime {
        return date.toLocalDate().atTime(23, 59, 59, 999_999_999)
    }

    fun dateStart(date: String): LocalDateTime {
        return LocalDate.parse(date).atStartOfDay()
    }

    fun dateEnd(date: String): LocalDateTime {
        return LocalDate.parse(date).atTime(23, 59, 59, 999_000_000)
    }

    fun isFullDay(startedAt: LocalDateTime, endedAt: LocalDateTime): Boolean {
        val startDate = startedAt.toLocalDate()
        val endDate = endedAt.toLocalDate()

        return startDate == endDate &&
            startedAt.toLocalTime() == LocalTime.MIDNIGHT &&
            endedAt.toLocalTime() == LocalTime.of(23, 59, 59, 999_000_000)
    }

    fun isSameDay(startedAt: LocalDateTime, endedAt: LocalDateTime): Boolean {
        return startedAt.toLocalDate() == endedAt.toLocalDate()
    }
}
