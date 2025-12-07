package com.spectrum.workfolio.config.handler

import com.spectrum.workfolio.config.provider.JwtTokenProvider
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.enums.SNSType
import com.spectrum.workfolio.domain.enums.WorkfolioToken
import com.spectrum.workfolio.redis.model.RedisRefreshToken
import com.spectrum.workfolio.redis.service.RedisRefreshTokenService
import com.spectrum.workfolio.services.AccountService
import com.spectrum.workfolio.utils.TokenUtil
import com.spectrum.workfolio.utils.WorkfolioException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class WorkfolioOAuth2LoginSuccessHandler(
    private val accountService: AccountService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val redisRefreshTokenService: RedisRefreshTokenService,
) : AuthenticationSuccessHandler {

    @Value("\${token.httpOnly}")
    private lateinit var httpOnly: String

    @Value("\${token.secure}")
    private lateinit var secure: String

    @Value("\${app.frontend.url}")
    private lateinit var frontendUrl: String

    @Value("\${app.frontend.domain}")
    private lateinit var frontendDomain: String

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        if (authentication is OAuth2AuthenticationToken) {
            val registrationId = authentication.authorizedClientRegistrationId
            val providerId = handleProviderLogin(registrationId, authentication.principal)
            val account = accountService.getAccountByProviderId(providerId)
                .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_WORKER.message) }
            val token = jwtTokenProvider.generateToken(account)

            saveRefreshToken(token.refreshToken)
            setTokenCookies(response, token)

            val redirectUrl = buildRedirectUrl(token) // URL 리디렉션 (쿼리 파라미터 전달 유지)
            response.sendRedirect(redirectUrl)
        }
    }

    private fun saveRefreshToken(refreshToken: String) {
        val key = jwtTokenProvider.getDataFromToken(refreshToken, "id") as String
        val claims = jwtTokenProvider.parseClaims(refreshToken)
        val exp = claims["exp"] as Long
        val ttlSeconds = TokenUtil.getTokenTtlSeconds(exp)
        val newRedisRefreshTokenEntity = RedisRefreshToken(key, ttlSeconds.toInt(), refreshToken)
        redisRefreshTokenService.saveRefreshToken(newRedisRefreshTokenEntity)
    }

    private fun handleProviderLogin(registrationId: String, oauth2User: OAuth2User): String {
        return when (registrationId.uppercase()) {
            SNSType.KAKAO.name -> oauth2User.attributes["id"].toString()
            else -> throw IllegalStateException("Unsupported OAuth2 provider: $registrationId")
        }
    }

    private fun buildRedirectUrl(token: WorkfolioToken): String {
        return UriComponentsBuilder.fromUriString(frontendUrl)
            .queryParam("access_token", token.accessToken)
            .queryParam("refresh_token", token.refreshToken)
            .toUriString()
    }

    private fun setTokenCookies(response: HttpServletResponse, token: WorkfolioToken) {
        val accessTokenCookie = ResponseCookie.from("accessToken", token.accessToken)
            .httpOnly(httpOnly.toBoolean())
            .secure(secure.toBoolean())
            .path("/")
            .domain(frontendDomain)
            .sameSite("Lax")
            .maxAge(60 * 60 * 2) // 2시간
            .build()

        val refreshTokenCookie = ResponseCookie.from("refreshToken", token.refreshToken)
            .httpOnly(httpOnly.toBoolean())
            .secure(secure.toBoolean())
            .path("/")
            .domain(frontendDomain)
            .sameSite("Lax")
            .maxAge(60 * 60 * 24 * 7) // 7일
            .build()

        response.addHeader("Set-Cookie", accessTokenCookie.toString())
        response.addHeader("Set-Cookie", refreshTokenCookie.toString())
    }
}
