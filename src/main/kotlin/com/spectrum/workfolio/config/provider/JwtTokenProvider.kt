package com.spectrum.workfolio.config.provider

import com.spectrum.workfolio.config.exception.JwtAuthenticationException
import com.spectrum.workfolio.config.service.WorkerDetailService
import com.spectrum.workfolio.domain.dto.CustomUserDetails
import com.spectrum.workfolio.domain.model.WorkfolioToken
import com.spectrum.workfolio.domain.model.MsgKOR
import com.spectrum.workfolio.utils.WorkfolioException
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.SecretKey

@Component
@PropertySource("classpath:jwt.properties")
class JwtTokenProvider(
    @Value("\${jwt.secret-key}") private val secretKey: String,
    @Value("\${jwt.access.expiration-hours}") private val accessTokenExpirationHours: Long,
    @Value("\${jwt.refresh.expiration-days}") private val refreshTokenExpirationDays: Long,
    private val workerUserDetailService: WorkerDetailService,
) {

    fun generateToken(auth: Authentication): WorkfolioToken {
        val claims = setClaims(auth)
        val issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        val accessTokenExpiration = issuedAt.plus(accessTokenExpirationHours, ChronoUnit.DAYS)
        val refreshTokenExpiration = issuedAt.plus(refreshTokenExpirationDays, ChronoUnit.DAYS)

        val accessToken = createJwtToken(
            claims = claims,
            subject = auth.name,
            issuedAt = issuedAt,
            expiration = accessTokenExpiration,
        )

        val refreshToken = createJwtToken(
            claims = claims,
            subject = auth.name,
            expiration = refreshTokenExpiration,
        )

        return WorkfolioToken(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    fun reissueToken(
        auth: Authentication,
        originalRefreshToken: String,
    ): WorkfolioToken {
        val claims = setClaims(auth)
        val issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        val accessTokenExpiration = issuedAt.plus(accessTokenExpirationHours, ChronoUnit.DAYS)

        val accessToken = createJwtToken(
            claims = claims,
            subject = auth.name,
            issuedAt = issuedAt,
            expiration = accessTokenExpiration,
        )

        val originalRefreshTokenClaims = parseClaims(originalRefreshToken)
        val refreshTokenExpirationDate = originalRefreshTokenClaims.expiration.toInstant()

        val refreshToken = createJwtToken(
            claims = claims,
            subject = auth.name,
            expiration = refreshTokenExpirationDate,
        )

        return WorkfolioToken(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    fun getAuthentication(token: String): Authentication {
        val claims = parseClaims(token)
        val id = claims["id"].toString()
        val userDetails = workerUserDetailService.loadUserByUsername(id)
        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }

    // 토큰 정보를 검증하는 메서드
    fun validateToken(token: String): Boolean {
        try {
            val cleanedToken = removePrefix(token.trim())
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(cleanedToken)
            return true
        } catch (e: SecurityException) {
            throw JwtAuthenticationException(MsgKOR.INVALID_JWT_TOKEN.message)
        } catch (e: MalformedJwtException) {
            throw JwtAuthenticationException(MsgKOR.INVALID_JWT_TOKEN.message)
        } catch (e: ExpiredJwtException) {
            throw JwtAuthenticationException(MsgKOR.EXPIRED_JWT_TOKEN.message)
        } catch (e: UnsupportedJwtException) {
            throw JwtAuthenticationException(MsgKOR.UNSUPPORTED_JWT_TOKEN.message)
        } catch (e: IllegalArgumentException) {
            throw JwtAuthenticationException(MsgKOR.MISSING_JWT_CLAIMS_TOKEN.message)
        }
    }

    fun getDataFromToken(token: String, field: String): Any {
        val cleanedToken = removePrefix(token)
        if (validateToken(cleanedToken)) {
            val claims = parseClaims(cleanedToken)
            return claims[field]
                ?: throw WorkfolioException(MsgKOR.INVALID_JWT_TOKEN.message)
        } else {
            throw WorkfolioException(MsgKOR.INVALID_JWT_TOKEN.message)
        }
    }

    private fun createJwtToken(
        claims: Map<String, Any>,
        subject: String,
        issuedAt: Instant? = null,
        expiration: Instant,
    ): String {
        val builder = Jwts.builder()
            .claims(claims)
            .subject(subject)
            .expiration(Date.from(expiration))
            .signWith(getSigningKey())

        if (issuedAt != null) {
            builder.issuedAt(Date.from(issuedAt))
        }

        return builder.compact()
    }

    fun parseClaims(token: String): Claims {
        return try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).payload
        } catch (e: ExpiredJwtException) {
            e.claims
        }
    }

    private fun getSigningKey(): SecretKey {
        val keyBytes = Base64.getDecoder().decode(secretKey)
        return Keys.hmacShaKeyFor(keyBytes)
    }

    private fun setClaims(auth: Authentication): Map<String, Any> {
        val claims = mutableMapOf<String, Any>()
        val user = (auth.principal as CustomUserDetails)
        val id = user.username
        val roles = user.authorities

        claims["id"] = id
        claims["roles"] = roles
        claims["jit"] = UUID.randomUUID().toString()

        return claims
    }

    fun removePrefix(str: String): String = str.replace("Bearer ", "")
}