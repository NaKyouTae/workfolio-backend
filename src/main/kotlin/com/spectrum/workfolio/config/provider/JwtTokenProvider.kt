package com.spectrum.workfolio.config.provider

import com.spectrum.workfolio.domain.entity.primary.Account
import com.spectrum.workfolio.domain.model.WorkfolioToken
import com.spectrum.workfolio.domain.model.ErrorCode
import com.spectrum.workfolio.utils.WorkfolioException
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.*
import javax.crypto.SecretKey

@Component
@PropertySource("classpath:jwt.properties")
class JwtTokenProvider(
    @Value("\${jwt.secret-key}") private val secretKey: String,
    @Value("\${jwt.access.expiration-hours}") private val accessTokenExpTime: Long,
    @Value("\${jwt.refresh.expiration-days}") private val refreshTokenExpDay: Long,
) {
    private val invalidatedTokens = mutableSetOf<String>()

    fun createToken(account: Account): WorkfolioToken {
        val now = ZonedDateTime.now()
        val accessTokenExpiration = now.plusHours(accessTokenExpTime)

        val claims = mapOf<String, Any>(
            "workerId" to account.worker.id,
        )

        val accessToken = Jwts.builder()
            .claims(claims)
            .issuedAt(Date.from(now.toInstant()))
            .expiration(Date.from(accessTokenExpiration.toInstant()))
            .signWith(getSigningKey())
            .compact()

        val refreshTokenExpiration = now.plusDays(refreshTokenExpDay)

        val refreshToken = Jwts.builder()
            .claims(claims)
            .issuedAt(Date.from(now.toInstant()))
            .expiration(Date.from(refreshTokenExpiration.toInstant()))
            .signWith(getSigningKey())
            .compact()

        return WorkfolioToken(
            accessToken,
            refreshToken,
        )
    }

    fun getWorkerId(token: String): String {
        return parseClaims(token)["workerId"] as String
    }

    fun validateToken(str: String): Boolean {
        val token = removePrefix(str)

        if (invalidatedTokens.contains(token)) {
//            throw WorkfolioException("Token has been invalidated", ErrorCode.SIGN)
            return false
        }

        return try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token)
            true
        } catch (e: Exception) {
            when (e) {
                is SecurityException,
                is MalformedJwtException -> {
//                    throw WorkfolioException("Invalid JWT Token", ErrorCode.SIGN)
                }
                is ExpiredJwtException -> {
//                    throw WorkfolioException("Expired JWT Token", ErrorCode.SIGN)
                }
                is UnsupportedJwtException -> {
//                    throw WorkfolioException("Unsupported JWT Token", ErrorCode.SIGN)
                }
                is IllegalArgumentException -> {
//                    throw WorkfolioException("JWT claims string is empty.", ErrorCode.SIGN)
                }
            }
            false
        }
    }

    fun parseClaims(str: String): Claims {
        val token = removePrefix(str)
        return try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).payload
        } catch (e: ExpiredJwtException) {
            e.claims
        }
    }

    fun invalidateToken(str: String) {
        val token = removePrefix(str)

        invalidatedTokens.add(token)
    }

    fun getSigningKey(): SecretKey {
        val keyBytes = Base64.getDecoder().decode(secretKey)
        val signingKey: SecretKey = Keys.hmacShaKeyFor(keyBytes)
        return signingKey
    }

    fun removePrefix(str: String): String {
        return str.replace("Bearer ", "")
    }
}
