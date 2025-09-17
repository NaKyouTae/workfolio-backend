package com.spectrum.workfolio.redis.service

import com.spectrum.workfolio.redis.model.RedisBlackAccessToken
import com.spectrum.workfolio.redis.repository.RedisBlackAccessTokenRepository
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class RedisBlackAccessTokenService(
    private val redisBlackAccessTokenRepository: RedisBlackAccessTokenRepository,
) {
    fun getBlackAccessToken(key: String): Optional<RedisBlackAccessToken?> {
        val token = redisBlackAccessTokenRepository.findById(key)
        val redisToken = token.map { it }
        return redisToken
    }

    fun saveBlackAccessToken(token: RedisBlackAccessToken) {
        redisBlackAccessTokenRepository.save(token)
    }
}
