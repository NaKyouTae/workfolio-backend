package com.spectrum.workfolio.redis.service

import com.spectrum.workfolio.redis.model.RedisRefreshToken
import com.spectrum.workfolio.redis.repository.RedisRefreshTokenRepository
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class RedisRefreshTokenService(
    private val redisRefreshTokenRepository: RedisRefreshTokenRepository,
) {

    fun getRefreshToken(key: String): Optional<RedisRefreshToken> {
        return redisRefreshTokenRepository.findById(key)
    }

    fun saveRefreshToken(token: RedisRefreshToken) {
        redisRefreshTokenRepository.save(token)
    }
}
