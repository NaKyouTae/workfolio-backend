package com.spectrum.workfolio.config.handler

import com.spectrum.workfolio.config.provider.JwtTokenProvider
import com.spectrum.workfolio.domain.model.ExceptionMessageKor
import com.spectrum.workfolio.domain.model.SNSType
import com.spectrum.workfolio.domain.model.WorkfolioToken
import com.spectrum.workfolio.services.AccountService
import com.spectrum.workfolio.utils.WorkfolioException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
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
) : AuthenticationSuccessHandler {

    @Value("\${token.httpOnly}")
    private lateinit var httpOnly: String
    @Value("\${token.secure}")
    private lateinit var secure: String

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        if (authentication is OAuth2AuthenticationToken) {
            val registrationId = authentication.authorizedClientRegistrationId
            val providerId = handleProviderLogin(registrationId, authentication.principal)
            val account = accountService.getAccountByProviderId(providerId)
                .orElseThrow { WorkfolioException(ExceptionMessageKor.USER_NOT_FOUND.message) }
            val token = jwtTokenProvider.generateToken(authentication)

            val redirectUrl = buildRedirectUrl(token) // URL 리디렉션 (쿼리 파라미터 전달 유지)
            response.sendRedirect(redirectUrl)
        }
    }

    private fun handleProviderLogin(registrationId: String, oauth2User: OAuth2User): String {
        return when (registrationId.uppercase()) {
            SNSType.KAKAO.name -> oauth2User.attributes["id"].toString()
            else -> throw IllegalStateException("Unsupported OAuth2 provider: $registrationId")
        }
    }

    private fun buildRedirectUrl(token: WorkfolioToken): String {
        return UriComponentsBuilder.fromUriString("http://localhost:3000")
            .queryParam("access_token", token.accessToken)
            .queryParam("refresh_token", token.refreshToken)
            .toUriString()
    }
}


