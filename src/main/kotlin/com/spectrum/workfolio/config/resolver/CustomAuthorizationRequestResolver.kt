package com.spectrum.workfolio.config.resolver

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest

class CustomAuthorizationRequestResolver(
    clientRegistrationRepository: ClientRegistrationRepository,
    private val authorizationRequestBaseUri: String,
) : OAuth2AuthorizationRequestResolver {

    private val defaultResolver =
        DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, authorizationRequestBaseUri)

    override fun resolve(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        return customize(defaultResolver.resolve(request), null)
    }

    override fun resolve(request: HttpServletRequest, clientRegistrationId: String): OAuth2AuthorizationRequest? {
        return customize(defaultResolver.resolve(request, clientRegistrationId), clientRegistrationId)
    }

    private fun customize(authorizationRequest: OAuth2AuthorizationRequest?, clientRegistrationId: String?): OAuth2AuthorizationRequest? {
        if (authorizationRequest == null) return null

        // Kakao의 경우 profile_nickname만 필수로 요청하고 나머지는 옵션으로 처리
        val scopes = if (clientRegistrationId == "kakao") {
            // Kakao인 경우 profile_nickname만 필수 scope로 설정
            setOf("profile_nickname")
        } else {
            // 다른 OAuth 제공자의 경우 기존 scope 유지
            authorizationRequest.scopes
        }

        return OAuth2AuthorizationRequest.from(authorizationRequest)
            .scopes(scopes)
            .additionalParameters { it["prompt"] = "login" }
            .build()
    }
}
