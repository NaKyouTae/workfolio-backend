package com.spectrum.workfolio.services

import com.spectrum.workfolio.config.provider.JwtTokenProvider
import com.spectrum.workfolio.domain.model.MsgKOR
import com.spectrum.workfolio.domain.model.WorkfolioToken
import com.spectrum.workfolio.redis.model.RedisBlackAccessToken
import com.spectrum.workfolio.redis.model.RedisRefreshToken
import com.spectrum.workfolio.redis.service.RedisBlackAccessTokenService
import com.spectrum.workfolio.redis.service.RedisRefreshTokenService
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val jwtTokenProvider: JwtTokenProvider,
    private val redisRefreshTokenService: RedisRefreshTokenService,
    private val redisBlackAccessTokenService: RedisBlackAccessTokenService,
) {
    fun reissueAccessToken(accessToken: String, refreshToken: String): WorkfolioToken {
        verifiedRefreshToken(refreshToken)
        val rawAccessToken = jwtTokenProvider.removePrefix(accessToken)
        val rawRefreshToken = jwtTokenProvider.removePrefix(refreshToken)
        val key = jwtTokenProvider.getDataFromToken(refreshToken, "id") as String
        val redisRefreshTokenEntity = redisRefreshTokenService.getRefreshToken(key)

        if (!redisRefreshTokenEntity.isPresent) {
            throw WorkfolioException(MsgKOR.NOT_EXISTS_REFRESH_TOKEN.message)
        }

        val redisRefreshToken = redisRefreshTokenEntity.get().value

        if (rawRefreshToken != redisRefreshToken) {
            throw WorkfolioException(MsgKOR.INVALID_REFRESH_TOKEN_NOT_EXISTS.message)
        }

        // 기존 Access Token 블랙 리스트 추가
        val redisBlackAccessToken = RedisBlackAccessToken(rawAccessToken)
        redisBlackAccessTokenService.saveBlackAccessToken(redisBlackAccessToken)

        val authentication = jwtTokenProvider.getAuthentication(rawRefreshToken)
        val jwtToken = jwtTokenProvider.reissueToken(authentication, rawRefreshToken)

        // 신규 Refresh Token 화이트 리스트 추가
        val claims = jwtTokenProvider.parseClaims(jwtToken.refreshToken)
        val exp = claims["exp"] as Long
        val ttlSeconds = getTokenTtlSeconds(exp)
        val newRedisRefreshTokenEntity = RedisRefreshToken(key, ttlSeconds.toInt(), jwtToken.refreshToken)
        redisRefreshTokenService.saveRefreshToken(newRedisRefreshTokenEntity)

        return WorkfolioToken(
            accessToken = jwtToken.accessToken,
            refreshToken = jwtToken.refreshToken,
        )
    }

    private fun getTokenTtlSeconds(exp: Long): Long {
        val now = System.currentTimeMillis() / 1000
        return maxOf((exp - now), 0)
    }

    private fun verifiedRefreshToken(encryptedRefreshToken: String?) {
        requireNotNull(encryptedRefreshToken) {
            MsgKOR.NOT_EXISTS_REFRESH_TOKEN.message
        }
    }
}