package com.spectrum.workfolio.redis.model

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.io.Serializable
import java.time.LocalDateTime

@RedisHash(value = "user-black-access-token", timeToLive = 3600)
data class RedisBlackAccessToken(
    @Id
    val id: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
) : Serializable
